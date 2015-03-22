(ns amnixiel.test
    (:import (javax.xml.parsers SAXParser SAXParserFactory))
    (:require [clojure.java.io :as io]
              [clojure.xml :as xml]
              [clojure.zip :as zip])
    (:use
              [clojure.core.strint]
              [clojure.data.xml]
              [clojure.data.zip.xml]))

(defn startparse-sax
    "Skip DTDs."
    [s ch]
    (let [factory (SAXParserFactory/newInstance)]
        (.setFeature factory 
            "http://apache.org/xml/features/nonvalidating/load-external-dtd"
            false)
        (let [^SAXParser parser (.newSAXParser factory)]
            (.parse parser s ch))))

(defn or-nil [f]
    (try
        (f)
         (catch Exception e nil)))

(defn parse-ssid-block [m]
    (or-nil
    #(into {}
        (let [ssid (xml1-> m :SSID)]
                {:essid (text (xml1-> ssid :essid))
                 :encryption (map text (xml-> ssid :encryption))}))))

(defn parse-gps-block [m]
    (or-nil 
    #(into {}
        (let [gps-info (xml1-> m :gps-info)]
            {:lon (text (xml1-> gps-info :max-lon))
             :lat (text (xml1-> gps-info :max-lat))}))))
            
(defn parse-network-block [m]
    (when-let [gps (parse-gps-block m)]
        (when-let [ssid (parse-ssid-block m)]
            (merge {
                 ; :first-time (attr m :first-time)
                 ; :last-time (attr m :last-time) 
                  :bssid (text (xml1-> m :BSSID))
                  :channel (text (xml1-> m :channel))} 
                 ssid
                 gps))))
    
(defn mkdesc-content [n]
    (element :div {}
        (element :p {:style "font-size:8pt;font-family:monospace;"} 
            (<< "(~(n :lon),~(n :lat))"))
        (element :ul {}
            (element :li {} (<< "BSSID : ~(n :bssid)"))
            (element :li {} (<< "Channel : ~(n :channel)"))
            (element :li {} (<< "Encrypt : ~(clojure.string/join \\space (n :encryption))")))))
           
(defn mkdesc [n]
    (sexp-as-element [:description {} 
        [:-cdata (strip-meta (emit-str (mkdesc-content n)))]]))

(defn ire [s]
    (re-pattern (<< "(?i)~{s}")))

(defn is-enc [s]
    #(boolean (re-find (ire s) %)))

(defn is-none [s] ((is-enc "none") s))
(defn is-wep [s] ((is-enc "wep") s))
(defn is-tkip [s] ((is-enc "tkip") s))
(defn is-wpa2 [s] ((is-enc "wpa2") s))

(defn score-enc [x]
    (cond
        (is-none x) 1
        (is-wep x) 2
        (is-tkip x) 3
        (is-wpa2 x) 4
        :else 99))

(defn score-encs [encs]
    (reduce min (map score-enc encs)))

(defn pick-colour [encs]
    (def score->colour
        {1 :red     
         2 :red 
         3 :orange 
         4 :green
         99 :black})
     (score->colour (score-encs encs)))

(defn colour->hex [c]
    ; Colours are 0xaabbggrr
    (def c->h
        {:red "ff0000ff"
         :orange "ff00a0ff"
         :green "ff00ff00"
         :black "ff000000"})
    (c->h c))

(defn colour->id [c]
    (def c->i
        {:red "red"
         :orange "orange"
         :green "green"
         :black "black"})
     (c->i c))

(print (colour->id (pick-colour '("WPA2+AES" "WPA+TKIP"))))

(defn network->kml [n]
    (element :Placemark {}
        (mkdesc n)
        (element :name {} (n :essid))
        (element :Point {}
            (element :extrude {} "1")
            (element :alitiudeMode {} "relativeToGround")
            (element :coordinates {} (<< "~(n :lon),~(n :lat),0")))
        (element :styleUrl {} "#red")))

(defn style->kml [c]
    (element :style {:id (colour->id c)}
        (element :LabelStyle {}
            (element :color {} (colour->hex c)))))

(defn kml [n]
    (def root (zip/xml-zip (element :Document {}
            (style->kml :red)
            (style->kml :orange)
            (style->kml :green)
            (style->kml :black))))
    (defn app [e] (zip/append-child root e))
        (reduce #(
           zip/append-child %1 (network->kml %2)) root (remove nil? n)))

(defn parse-networks [root]
          (map #(parse-network-block %) (xml-> root :wireless-network)))

(defn main [f]
    (def root (-> f io/resource io/file 
               (xml/parse startparse-sax) zip/xml-zip))
    (kml (parse-networks root)))

(println "\n")
(print (indent-str (main "test-mini.xml")))
(println "\n")

(defn strip-meta 
    "Strip the initial <?xml enocding=blah?> from an xml string.
     It does not look like the Clojure data.xml library provides an alternative
     for not printing this metadata."
     [xml-str]
    (clojure.string/replace xml-str #"<\?[^>]+\?>" ""))

