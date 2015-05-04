(ns amnixiel.main
    (:require [clojure.java.io :as io]
              [clojure.xml :as xml]
              [amnixiel.parser :as parser]
              [amnixiel.emitter :as emitter]
              [clojure.zip :as zip]))

(defn err [& msg]
    (binding [*out* *err*]
        (println (clojure.string/join msg))))
    
(defn open [fs]
    (let [r (group-by #(.exists %) (map io/file fs))]
        (doseq [dne (r false)]
                (err "File " (.getName dne) " does not exist. Skipping..."))
        (r true)))

(defn parse [f]
    (try  (-> f 
            (xml/parse parser/startparse-sax) zip/xml-zip)
        (catch Exception ex 
            (err "File " (.getName f) " could not be parsed. Skipping...")
                nil)))


(defn main [& f]
    (print (clojure.data.xml/indent-str (first
        (emitter/kml 
            (reduce #(concat %1
                (if-let [f (parse %2)]
                    (parser/parse-networks f)))
             '() (open *command-line-args*)))))))
