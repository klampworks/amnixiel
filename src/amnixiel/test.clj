(ns amnixiel.test
    (:import (javax.xml.parsers SAXParser SAXParserFactory))
    (:require [clojure.java.io :as io]
              [clojure.xml :as xml]
              [clojure.zip :as zip])
    (:use
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

(defn main [f]
    (def root (-> f io/resource io/file 
               (xml/parse startparse-sax) zip/xml-zip))
    (into {}
          (for [m (xml-> root :wireless-network)]
                  {
                   :first-time (attr m :first-time)
                   :last-time (attr m :last-time) 
                   :bssid (text (xml1-> m :BSSID))
                   })))

(println "\n")
(main "test-mini.xml")
(println "\n")

(comment
    (zip-xml/xml-> root :head :meta)
    (keyword (zip-xml/attr m :type))
    (zip-xml/text m))

