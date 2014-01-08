(ns organa.core
  (:use [organa.common])
  (:use [clojure.string :only [replace-first]])
  (:use [incanter.core :only [$ $data $rollup col-names conj-cols dataset]])
  (:use [incanter.stats :only [mean sd quantile]])
  (:require [incanter.io :as incanterio]))


(def time-pattern #"^([0-9- :,]*) INFO.*TIME MONITOR.*;(.*);(.*) ms$")
(def error-pattern #"^([0-9- :,]*).* ERROR (.*)$")

(defn time-to-reading
  "filter out line and convert duration into a long"
  [[s t l d]]
  [t l (str-to-long d)] )

(defn error-to-reading
  "filter out line"
  [[s t l]]
  (cond
   (re-matches #".*\w*at .*" l) nil
   (re-matches #".*WorkerThread#.*" l) [t (replace-first l #"\(WorkerThread#.*\)" "")]
   (re-matches #".*HealthDetailsServiceWeb - unable.*" l) [t (replace-first l #"\[.*\]" "")]
   (re-matches #".*LifeSummaryServiceWeb - unable.*" l) [t (replace-first l #"\[.*\]" "")]
   (re-matches #".*SLF4J: Found binding in.*" l) [t (replace-first l #"\[.*\]" "")]
   :else [t l] ))

(defn readings-to-dataset
  "build a dataset from a list of readings"
  [readings]
  (dataset [:timestamp :label :duration] readings))

(defn compute-metric
  "compute a metric grouped by label"
  [ds [metric f]]
  (col-names ($rollup f :duration :label ds) [:label metric])) 

(defn q [p ds] (quantile ds :probs [p])) 

(defn compute-stats
  "compute stats grouped by label"
  [ds]
  (let [metrics {:count count :mean mean :sd sd
		 :min (partial q 0) :q90 (partial q 0.90) :q95 (partial q 0.95) :max (partial q 1)}]
	 (map (partial compute-metric ds) metrics)))


(defn stats
  "build the stats dataset from the readings dataset"
   [ds]
   (let [result (compute-stats ds)]
     (col-names 
      (conj-cols
       ($ :label (nth result 0)) 
       ($ :count (nth result 0)) 
       (number-format ($ :mean (nth result 1))) 
       (number-format ($ :sd (nth result 2)))
       (number-format ($ :min (nth result 3)))
       (number-format ($ :q90 (nth result 4))) 
       (number-format ($ :q95 (nth result 5)))
       (number-format ($ :max (nth result 6))) )
      [:label :count :mean :sd :min :q90 :q95 :max])))

(defn counters
  "build the counters dataset from the readings dataset"
   [ds]
   (let [result (compute-stats ds)]
     (col-names 
      (conj-cols
       (quoted-text ($ :label (nth result 0)))
       ($ :count (nth result 0)) )
      [:label :count])))


(defn time-analysis
  "compute and save response time statistics"
  [filename]
  (let [parser (partial (partial parse-line time-pattern) time-to-reading) ]
    (save-csv (stats (readings-to-dataset (parse-file  filename parser)))
	  "stats.csv" )))

(defn errors-analysis
  "compute and save error counters"
  [filename]
  (let [parser (partial (partial parse-line error-pattern) error-to-reading)]
    (save-csv (counters (readings-to-dataset (parse-file  filename parser)))
	  "errors.csv")))

(defn -main
  "usage: time|errors logs.txt"
  [& args]
  {:pre  [(and (string? (nth args 0)) (string? (nth args1)))] } 
  (let [ [mode filename] args]
    (cond
     (= "time" mode) (time-analysis filename)
     (= "errors" mode) (errors-analysis filename)
     :else (println (str "invalid mode " mode)) ))) 
 
