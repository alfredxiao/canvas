(ns canvas.core
  (:require [clojure.test]
            [clojure.string :refer [starts-with? ends-with?]]
            [clojure.tools.namespace.find :as ns-find]
            [clojure.pprint :refer [pprint]]
            [clojure.java.io :as io]))

(defn- log-info [& msgs]
  #_(apply println msgs))

(defn- instrument
  [f v]
  (fn [& args]
    (alter-meta! v assoc ::tested? true)
    (alter-meta! v update ::hit inc)
    (apply f args)))

(defn- instrumentable?
  [v]
  (and (.isBound v)
       (fn? @v)
       (not (:macro (meta v)))
       (not (:test (meta v)))))

(defn instrument-var! [v]
  (when (instrumentable? v)
    (log-info "canvas: instrumenting function " v)
    (alter-meta! v assoc ::tested? false)
    (alter-meta! v assoc ::hit 0)
    (alter-meta! v assoc ::original @v)
    (alter-var-root v instrument v)))

(defn uninstrument-var! [v]
  (when (instrumentable? v)
    (let [root (::original (meta v))]
      (assert root "No root binding to restore!")
      (alter-meta! v dissoc ::tested?)
      (alter-meta! v dissoc ::hit)
      (alter-meta! v dissoc ::original)
      (alter-var-root v (constantly root)))))

(defn apply-to-each-var [f ns]
  (doseq [[_ v] (ns-interns ns)]
    (f v)))

(defn instrument-ns! [& nz]
  (doseq [n nz]
    (log-info "canvas: instrumenting publics of ns:" (ns-name n))
    (apply-to-each-var instrument-var! n)))

(defn uninstrument-ns! [& nss]
  (doseq [ns nss]
    (apply-to-each-var uninstrument-var! ns)))

(defn- user? [ns]
  (= "user" (name (ns-name ns))))

(defn report [nz]
  (into {}
        (for [n nz :when (not (user? n))]
          [(ns-name n)
           (into {}
                 (for [[_ v] (ns-interns n) :when (instrumentable? v)]
                   [(:name (meta v)) (let [mt (meta v)]
                                        {:scope   (if (:private mt) :private :public)
                                         :line    (:line mt)
                                         :tested? (::tested? mt)
                                         :hit     (::hit mt)})]))])))

(defn- doseq-with [values op]
  (doseq [v values]
    (op v)))

(defn evaluate-test-coverage
  [{:keys [source-paths test-paths] :as opts}]
  (let [target-nz (apply concat (->> source-paths
                                     (map io/file)
                                     (map ns-find/find-namespaces-in-dir)))
        test-nz (apply concat (->> test-paths
                                   (map io/file)
                                   (map ns-find/find-namespaces-in-dir)))]
    (doseq-with target-nz require)
    (doseq-with test-nz require)
    (try
      (doseq-with target-nz instrument-ns!)
      (apply clojure.test/run-tests test-nz)
      (pprint (report target-nz))
      (finally
        (mapv uninstrument-ns! target-nz)))))