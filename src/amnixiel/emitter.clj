(ns amnixiel.emitter
    (:require 
              [amnixiel.colours :as colours]
              [clojure.zip :as zip])
    (:use
              [clojure.core.strint]
              [clojure.data.xml]))

(defn strip-meta 
    "Strip the initial <?xml enocding=blah?> from an xml string.
     It does not look like the Clojure data.xml library provides an alternative
     for not printing this metadata."
     [xml-str]
    (clojure.string/replace xml-str #"<\?[^>]+\?>" ""))

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

(defn network->kml [n]
    (element :Placemark {}
        (mkdesc n)
        (element :name {} (n :essid))
        (element :Point {}
            (element :extrude {} "1")
            (element :alitiudeMode {} "relativeToGround")
            (element :coordinates {} (<< "~(n :lon),~(n :lat),0")))
        (element :styleUrl {} 
            (<< "#~(colours/colour->id (colours/pick-colour (n :encryption)))"))))

(defn style->kml [c]
    (element :style {:id (colours/colour->id c)}
        (element :LabelStyle {}
            (element :color {} (colours/colour->hex c)))))

(defn kml [n]
    (def root (zip/xml-zip (element :Document {}
            (style->kml :red)
            (style->kml :orange)
            (style->kml :green)
            (style->kml :black))))
    (defn app [e] (zip/append-child root e))
        (reduce #(
           zip/append-child %1 (network->kml %2)) root (remove nil? n)))
