(ns ptang-logs.core
  (:use [incanter.core :only [$ $data $rollup col-names conj-cols save]])
  (:use [incanter.stats :only [mean sd quantile]])
  (:require [clojure.java.io :as io]
	    [incanter.core :as incanter]
	    [incanter.io :as incanterio]))

;; chemin de readlines
;;http://blog.magpiebrain.com/tag/incanter/

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

(defn response-time-summary
  "build the summary of a dataset"
  [ds] 
   (zipmap [ :count :mean :sd :min :q95 :max]
      (flatten (incanter/with-data ($ :duration ds)
	   [ (count $data) (mean $data) (sd $data) (quantile $data :probs[0 0.95 1]) ] ))))

(defn readings-to-dataset
  "build a dataset from a list of readings"
  [readings]
  (incanter/dataset [:timestamp :label :duration] readings))

(defn compute-metric
  "compute a metric grouped by label"
  [ds [metric f]]
  (col-names ($rollup f :duration :label ds) [:label metric])) 

(defn q [p ds] (quantile ds :probs [p])) 

(defn compute-stats
  "compute stats grouped by label"
  [ds]
  (let [metrics {:count count :mean mean :sd sd
		 :min (partial q 0) :q95 (partial q 0.95) :max (partial q 1)}]
	 (map (partial compute-metric ds) metrics)))

(defn stats
   [ds]
   (let [result (compute-stats ds)]
     (col-names 
      (conj-cols
       ($ :label (nth result 0)) 
       ($ :count (nth result 0)) 
       ($ :mean (nth result 1)) 
       ($ :sd (nth result 2)) 
       ($ :min (nth result 3)) 
       ($ :q95 (nth result 4)) 
       ($ :max (nth result 5)) )
      [:label :count :mean :sd :min :q95 :max])))
  
(defn -main [filename]
   (save (stats
	  (readings-to-dataset (parse-file  filename))) "stats.csv")) 
 
