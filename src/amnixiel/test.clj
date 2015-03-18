(ns amnixiel.test
    (:import (javax.xml.parsers SAXParser SAXParserFactory))
    (:require [clojure.java.io :as io]
              [clojure.xml :as xml]
              [clojure.zip :as zip])
    (:use
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

(defn parse-ssid-block [m]
    (into {}
        (let [ssid (xml1-> m :SSID)]
                {:essid (text (xml1-> ssid :essid))
                 :encryption (map text (xml-> ssid :encryption))})))

(defn parse-gps-block [m]
    (into {}
        (let [gps-info (xml1-> m :gps-info)]
            {:lon (text (xml1-> gps-info :max-lon))
             :lat (text (xml1-> gps-info :max-lat))})))
            
(defn parse-network-block [m]
    (merge {:first-time (attr m :first-time)
          :last-time (attr m :last-time) 
          :bssid (text (xml1-> m :BSSID))
          :channel (text (xml1-> m :channel))} 
         (parse-ssid-block m)
         (parse-gps-block m)))
    
(defn mkdesc-content []
    (element :div {}
        (element :p {:style "font-size:8pt;font-family:monospace;"} "(1, 2)")
        (element :ul {}
            (element :li {} "BSSID : 3")
            (element :li {} "Channel : 4")
            (element :li {} "Max Rate : 5")
            (element :li {} "Encrypt : 6"))))
           
(defn mkdesc []
    (sexp-as-element [:description {} 
        [:-cdata (strip-meta (emit-str (mkdesc-content)))]]))

(defn network->kml [n]
    (element :Placemark {}
        (mkdesc)
        (element :name {} "a")
        (element :Point {}
            (element :extrude {} "1")
            (element :alitiudeMode {} "relativeToGround")
            (element :coordinates {} "b,c,0"))))

(defn parse-networks [root]
    (into {}
          (for [m (xml-> root :wireless-network)]
            (parse-network-block m))))

(defn main [f]
    (def root (-> f io/resource io/file 
               (xml/parse startparse-sax) zip/xml-zip))
    (parse-networks root))

(println "\n")
(main "test-mini.xml")
(println "\n")

(defn strip-meta 
    "Strip the initial <?xml enocding=blah?> from an xml string.
     It does not look like the Clojure data.xml library provides an alternative
     for not printing this metadata."
     [xml-str]
    (clojure.string/replace xml-str #"<\?[^>]+\?>" ""))

