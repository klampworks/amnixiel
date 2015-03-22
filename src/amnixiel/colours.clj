(ns amnixiel.colours
    (use 
        [clojure.core.strint]))

(defn ire [s]
    (re-pattern (<< "(?i)~{s}")))

(defn is-enc [s]
    #(boolean (re-find (ire s) %)))

(defn is-none [s] ((is-enc "none") s))
(defn is-wep [s] ((is-enc "wep") s))
(defn is-tkip [s] ((is-enc "tkip") s))
(defn is-wpa2 [s] ((is-enc "wpa2") s))

(defn score-enc [x]
    (cond
        (is-none x) 1
        (is-wep x) 2
        (is-tkip x) 3
        (is-wpa2 x) 4
        :else 99))

(defn score-encs [encs]
    (reduce min (map score-enc encs)))

(defn pick-colour [encs]
    (def score->colour
        {1 :red     
         2 :red 
         3 :orange 
         4 :green
         99 :black})
     (score->colour (score-encs encs)))

(defn colour->hex [c]
    ; Colours are 0xaabbggrr
    (def c->h
        {:red "ff0000ff"
         :orange "ff00a0ff"
         :green "ff00ff00"
         :black "ff000000"})
    (c->h c))

(defn colour->id [c]
    (def c->i
        {:red "red"
         :orange "orange"
         :green "green"
         :black "black"})
     (c->i c))
