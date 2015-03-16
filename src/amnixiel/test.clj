(ns amnixiel.test)
(require '[clojure.java.io :as io])
(require '[clojure.xml :as xml])
(require '[clojure.zip :as zip])
(require '[clojure.data.zip.xml :as zip-xml])

(defn main []
    (def root (-> "example.nzb" io/resource io/file 
               xml/parse zip/xml-zip))
    (into {}
          (for [m (zip-xml/xml-> root :head :meta)]
                  [(keyword (zip-xml/attr m :type))
                           (zip-xml/text m)])))

(main)

(comment
    (zip-xml/xml-> root :head :meta)
    (keyword (zip-xml/attr m :type))
    (zip-xml/text m))

