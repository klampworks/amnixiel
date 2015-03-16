(ns amnixiel.test
    (:import (javax.xml.parsers SAXParser SAXParserFactory)))

(require '[clojure.java.io :as io])
(require '[clojure.xml :as xml])
(require '[clojure.zip :as zip])
(require '[clojure.data.zip.xml :as zip-xml])

(defn startparse-sax
    "Skip DTDs."
    [s ch]
    (let [factory (SAXParserFactory/newInstance)]
        (.setFeature factory "http://apache.org/xml/features/nonvalidating/load-external-dtd" false)
        (let [^SAXParser parser (.newSAXParser factory)]
            (.parse parser s ch))))

(defn main []
    (def root (-> "example.nzb" io/resource io/file 
               (xml/parse startparse-sax) zip/xml-zip))
    (into {}
          (for [m (zip-xml/xml-> root :head :meta)]
                  [(keyword (zip-xml/attr m :type))
                           (zip-xml/text m)])))

(main)

(comment
    (zip-xml/xml-> root :head :meta)
    (keyword (zip-xml/attr m :type))
    (zip-xml/text m))

