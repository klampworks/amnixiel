(ns amnixiel.test
    (:import (javax.xml.parsers SAXParser SAXParserFactory))
    (:require [clojure.java.io :as io]
              [clojure.xml :as xml]
              [amnixiel.colours :as colours]
              [amnixiel.parser :as parser]
              [amnixiel.emitter :as emitter]
              [clojure.zip :as zip])
    (:use
              [clojure.core.strint]
              [clojure.data.xml]
              [clojure.data.zip.xml]))

(defn main [f]
    (def root (-> f io/resource io/file 
               (xml/parse parser/startparse-sax) zip/xml-zip))
    (print (indent-str (first (emitter/kml (parser/parse-networks root))))))
