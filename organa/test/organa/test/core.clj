(ns organa.test.core
  (:use [ptanglogs.core])
  (:use [clojure.test])
  (:use [midje.sweet]))

(def line-source "2013-12-13 08:00:00,924 INFO  [STDOUT] (http-10.12.20.22-17011-1) 08:00:00.923 INFO  f.a.t.aspect.LogServiceAspect - [TIME MONITOR] 08:00:00;RS_OW_AgencyDataSupplierService;208 ms")

(fact "it should extract service name and duration"
      (parse-line line-source) => ["2013-12-13 08:00:00,924" "RS_OW_AgencyDataSupplierService" 208])
