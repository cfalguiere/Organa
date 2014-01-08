(ns organa.test.common
  (:use [organa.common])
  (:use [clojure.test])
  (:use [midje.sweet]))

(fact "it should convert a string into a long"
      (str-to-long "123456789") => 123456789)

(defn test-mapper
  [[s t l]]
  [t l] )

(fact "it should return a reading with 2 columns"
      (parse-line #"(\d*) (.*)" test-mapper "12 ab") => ["12" "ab"])

(fact "it should return a rounded number as a string"
      (number-format [12.34 45.67]) => ["12" "46"])

(fact "it should return a quoted text"
      (quoted-text ["ab" "cd"]) => ["\"ab\"" "\"cd\""])
