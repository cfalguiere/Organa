(ns organa.common
  (:use [clojure.test :only [function?]] )
  (:use [incanter.core :only [save dataset?]])
  (:require [clojure.java.io :as io]) )

(defn str-to-long
  "convert a string into a long"
  [x]
  {:pre  [(string? x)]}
  (Long/parseLong (apply str (filter #(Character/isDigit %) x))))

(defn parse-line
  "returns the file line as a vector representing the reading"
  [pattern mapper source]
  {:pre  [(and  (function? mapper) (string? source))]} ;; TODO test pre pattern
  (first (map mapper (re-seq pattern source))))

(defn filter-matching
  "filter out non matching lines"
  [readings]
  {:pre  [(seq? readings)]} 
  (remove nil? readings))

(defn parse-file 
  "read the file and returns a list of readings"
  [filename parser]
  {:pre  [(and  (function? parser) (string? filename))]} 
  (filter-matching
   (with-open [rdr (io/reader filename)]
     (doall (map parser (line-seq rdr))))))

(defn number-format
  "convert a map of floats into a map of string representing the rounded number"
  [mf]
  ;{:pre  [(and (vector? mf) (float? (first mf)))] } ;; NaN
 ; {:pre  [(vector? mf)] } ;; check whether the pre fails with sample
  (map #(format "%.0f" %) mf))

(defn quoted-text
  "convert a map of strings into a map of quoted strings"
  [ms]
  {:pre  [(and (vector? ms) (string? (first ms)))] } 
  (map #(format "\"%s\"" %) ms))

(defn save-csv
  "save the dataset onto a csv file with french delimiter"
  [ds filename]
  {:pre  [(and (dataset? ds) (string? filename))] } 
  (save ds filename :delim \;)) 
