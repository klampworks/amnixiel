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

(def test-xml (element :foo {:foo-attr "foo value"}
                     (element :bar {:bar-attr "bar value"}
                       (element :baz {} "The baz value1")
                       (element :baz {} "The baz value2")
                       (element :baz {} "The baz value3"))))

;(print (main "test.xml"))

;(print (indent-str (first (main "test.xml"))))


(defn main [f]
    (def root (-> f io/resource io/file 
               (xml/parse parser/startparse-sax) zip/xml-zip))
    (print (indent-str (first (emitter/kml (parser/parse-networks root))))))
    ;(parse-networks root))

;(print (indent-str (main "test-mini.xml")))
;(print (first (main "test.xml")))
;(print (main "test.xml"))
;(print (first (main "test.xml")))
;(print "\n>>\n\n")
;(print (indent-str (first (main "test.xml"))))
;(println "\n")

;(def test-xml (element :foo {:foo-attr "foo value"}
;                     (element :bar {:bar-attr "bar value"}
;                       (element :baz {} "The baz value1")
;                       (element :baz {} "The baz value2")
;                       (element :baz {} "The baz value3"))))

;(print (strip-meta (indent-str (mkdesc))))

