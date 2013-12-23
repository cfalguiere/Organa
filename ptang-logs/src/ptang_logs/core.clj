(ns ptang-logs.core
  (:require [clojure.java.io :as io])
  (:import java.util.regex.Pattern))

(def pattern #"^([0-9- :,]*) INFO.*TIME MONITOR.*;(.*);(.*) ms$")

(defn str-to-long [x] (Long/parseLong (apply str (filter #(Character/isDigit %) x))))

(defn to-reading
  "filter out line and convert duration into a long"
  [[s t l d]]
  [t l (str-to-long d)] )

(defn parse-line
  "returns the reading as a vector"
  [source]
  (first (map to-reading (re-seq pattern source))))

(defn filter-matching
  [readings]
  "filter out non matching lines"
   (remove nil? readings))
 
(defn parse-file 
  "returns a list of readings"
  [filename]
  (filter-matching
   (with-open [rdr (io/reader filename)]
       (doall (map parse-line (line-seq rdr))))))

(defn -main [& args]
 (println (parse-file  "sample.log")))

