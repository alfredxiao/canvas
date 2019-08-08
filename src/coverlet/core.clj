(ns coverlet.core
  (:require [clojure.test]
            [clojure.tools.namespace.find :as ns-find]
            [clojure.java.io :as io]
            [coverlet.instrument :as ins]
            [coverlet.report :refer [report]]))

(defn- doseq-with [values op]
  (doseq [v values]
    (op v)))

(defn evaluate-test-coverage
  [{:keys [source-paths test-paths reporter]}]
  (let [target-nss (apply concat (->> source-paths
                                     (map io/file)
                                     (map ns-find/find-namespaces-in-dir)))
        test-nss (apply concat (->> test-paths
                                   (map io/file)
                                   (map ns-find/find-namespaces-in-dir)))]
    (doseq-with target-nss require)
    (doseq-with test-nss require)
    (try
      (doseq-with target-nss ins/instrument-ns!)
      (apply clojure.test/run-tests test-nss)
      (report target-nss reporter)
      (finally
        (mapv ins/uninstrument-ns! target-nss)))))