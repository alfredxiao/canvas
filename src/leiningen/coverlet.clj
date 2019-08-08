(ns leiningen.coverlet
  (:require [clojure.walk :as walk]
            [leiningen.core.eval :as eval]
            [leiningen.core.main :as main])
  (:import (clojure.lang ExceptionInfo)))

(defn- coverlet-plugin-dep [project]
  (->> (:plugins project)
       (filter #(= (first %) 'coverlet/coverlet))
       (first)))

(defn ^:pass-through-help coverlet
  [project & args]
  (when (odd? (count args))
    (prn "Usage: lein coverlet :option-name OPTION_VALUE\n -- number of arguments must be even")
    (System/exit 1))
  (let [project (update-in project [:dependencies] conj (coverlet-plugin-dep project))
        opts (merge (select-keys project [:source-paths :test-paths :test-selectors])
                    (when-not (empty? args)
                      (walk/keywordize-keys (apply assoc {} args))))]
    (try
      (eval/eval-in-project project
                            `(coverlet.core/evaluate-test-coverage '~opts)
                            '(require 'coverlet.core))
      (catch ExceptionInfo e
        (main/exit (:exit-code (ex-data e) 1))))))
