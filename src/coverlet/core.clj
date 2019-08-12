(ns coverlet.core
  (:require [clojure.test]
            [clojure.tools.namespace.find :as ns-find]
            [clojure.java.io :as io]
            [coverlet.instrument :as ins]
            [coverlet.report :refer [report]]))

(defn- doseq-with [values f]
  (doseq [v values]
    (f v)))

(defn- paths->nss [paths]
  (apply concat (->> paths
                     (map io/file)
                     (map ns-find/find-namespaces-in-dir))))

(defn evaluate-test-coverage
  [{:keys [source-paths test-paths reporter]}]
  (let [target-nss (paths->nss test-paths)
        test-nss (paths->nss source-paths)]
    (doseq-with target-nss require)
    (doseq-with test-nss require)
    (try
      (doseq-with target-nss ins/instrument-ns!)
      (apply clojure.test/run-tests test-nss)
      (report target-nss reporter)
      (finally
        (mapv ins/uninstrument-ns! target-nss)))))