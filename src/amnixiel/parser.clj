(ns amnixiel.parser
    (:import (javax.xml.parsers SAXParser SAXParserFactory))
    (:use
              [clojure.data.zip.xml]))

(defn or-nil [f]
    (try
        (f)
         (catch Exception e nil)))

(defn parse-ssid-block [m]
    (or-nil
    #(into {}
        (let [ssid (xml1-> m :SSID)]
                {:essid (text (xml1-> ssid :essid))
                 :encryption (map text (xml-> ssid :encryption))}))))

(defn parse-gps-block [m]
    (or-nil 
    #(into {}
        (let [gps-info (xml1-> m :gps-info)]
            {:lon (text (xml1-> gps-info :max-lon))
             :lat (text (xml1-> gps-info :max-lat))}))))
            
(defn startparse-sax
    "Skip DTDs."
    [s ch]
    (let [factory (SAXParserFactory/newInstance)]
        (.setFeature factory 
            "http://apache.org/xml/features/nonvalidating/load-external-dtd"
            false)
        (let [^SAXParser parser (.newSAXParser factory)]
            (.parse parser s ch))))

(defn parse-network-block [m]
    (when-let [gps (parse-gps-block m)]
        (when-let [ssid (parse-ssid-block m)]
            (merge {
                 ; :first-time (attr m :first-time)
                 ; :last-time (attr m :last-time) 
                  :bssid (text (xml1-> m :BSSID))
                  :channel (text (xml1-> m :channel))} 
                 ssid
                 gps))))
    
(defn uniq [in]
    (loop [src in acc () se #{}]
        (let [f (first src) b (:bssid f)]
            (cond 
                (not (seq src)) acc
                (contains? se b) (recur (rest src) acc se)
                :else (recur (rest src) (conj acc f) (conj se b))))))
    
(defn parse-networks [root]
    (uniq (map #(parse-network-block %) (xml-> root :wireless-network))))

