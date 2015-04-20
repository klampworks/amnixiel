(ns amnixiel.test
    (:require [clojure.java.io :as io]
              [clojure.xml :as xml]
              [amnixiel.parser :as parser]
              [amnixiel.emitter :as emitter]
              [clojure.zip :as zip]))

(defn main [f]
    (def root (-> f io/resource io/file 
               (xml/parse parser/startparse-sax) zip/xml-zip))
    (print (clojure.data.xml/indent-str 
        (first (emitter/kml (parser/parse-networks root))))))
