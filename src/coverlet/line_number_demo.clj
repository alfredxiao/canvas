(ns coverlet.line-number-demo
  (:require [clojure.tools.reader :as r]
            [clojure.tools.reader.reader-types :as rt]
            [clojure.inspector :as insp]
            [clojure.test :refer :all])
  (:import (java.io PushbackReader StringReader BufferedReader)
           (clojure.lang LineNumberingPushbackReader)))

(def stats (atom {}))

(defn simple-or [x y]
  (if x
    x
    y))

(deftest test-simple-or
  (is (= true (simple-or true false))))

;; below are clojure source code reader, from source text to clojure forms

(defn- code->reader [code]
  (-> code
      (StringReader.)
      (LineNumberingPushbackReader.)
      (rt/indexing-push-back-reader)))

(defn- clj-file->reader [source-path]
  (-> source-path
      (slurp)
      (code->reader)))

(defn reader->forms [rdr]
  (loop [forms []]
    (if-let [form (r/read {:eof       nil
                           :features  #{:clj}
                           :read-cond :allow}
                          rdr)]
      (recur (conj forms form))
      forms)))

;;  Below are for wrapping a form with coverage tracking
(defn wrappable? [form]
  (let [keywords #{'if 'defn 'defn- 'when 'case 'cond 'condp '-> '->>}]
    (cond
      (symbol? form) (not (contains? keywords form))
      (seq? form)    (or (= 'defn (first form))
                         (contains? keywords (first form)))
      :else false)))

(def safe-inc (fnil inc 0))

(defn- capture-coverage [line]
  (swap! stats update line safe-inc))

(defn- wrap-single [form]
  (let [line (-> form meta :line)]
    (if (wrappable? form)
      (with-meta
        `(do
           (capture-coverage ~line)
           ~form)
        (-> form meta))
      form)))

(defn wrap-all [form]
  (wrap-single
    (if (seq? form)
      (with-meta (map wrap-all form) (meta form))
      form)))

(defn update-list [alist idx f & args]
  (with-meta (apply list (apply update (vec alist) idx f args))
             (meta alist)))

;;; below are test coverage reporting

(def this-file-path "./src/coverlet/line_number_demo.clj")

(defn report-coverage [orig-form coverage]
  (println "Coverage Report: ============== ")
  (prn coverage)
  (let [{:keys [line end-line]} (meta orig-form)
        this-file-lines (-> this-file-path
                            (slurp)
                            (StringReader.)
                            (BufferedReader.)
                            (line-seq))]
    (println "ln / hit :  -== code ==-")
    (doseq [line-num (range line (inc end-line))]
      (println (format "%2d / %2d : %s"
                       line-num
                       (if-let [hit (get coverage line-num)] hit 0)
                       (nth this-file-lines (dec line-num)))))))

;; below are demo procedures

(defn demo []
  (let [orig-form (let [all-forms (-> this-file-path
                                      (clj-file->reader)
                                      (reader->forms))]
                    (nth all-forms 2))
        wrapped-form (-> orig-form
                         (update-list 3 wrap-all)
                         (wrap-single))]
    (def wrapped-form wrapped-form)
    (reset! stats {})
    (eval wrapped-form)
    (test-simple-or)
    (report-coverage orig-form @stats)))