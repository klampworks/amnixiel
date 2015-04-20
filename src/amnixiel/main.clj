(ns amnixiel.main
    (:require [clojure.java.io :as io]
              [clojure.xml :as xml]
              [amnixiel.parser :as parser]
              [amnixiel.emitter :as emitter]
              [clojure.zip :as zip]))

(defn main [& [f]]
    (print 
        (emitter/kml (map #(parser/parse-networks 
                (-> % io/resource io/file 
                  (xml/parse parser/startparse-sax) zip/xml-zip)) 
             *command-line-args*))))

    ;(print (clojure.data.xml/indent-str 
    ;    (first (emitter/kml (parser/parse-networks root))))))
