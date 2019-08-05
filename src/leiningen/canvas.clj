(ns leiningen.canvas
  (:require [clojure.walk :as walk]
            [leiningen.core.eval :as eval]
            [leiningen.core.main :as main])
  (:import (clojure.lang ExceptionInfo)))

(defn- canvas-plugin-dep [project]
  (->> (:plugins project)
       (filter #(= (first %) 'canvas/canvas))
       (first)))

(defn ^:pass-through-help canvas
  [project & args]
  (when (odd? (count args))
    (prn "Usage: lein canvas :option-name OPTION_VALUE\n -- number of arguments must be even")
    (System/exit 1))
  (let [project (update-in project [:dependencies] conj (canvas-plugin-dep project))
        opts (merge (select-keys project [:source-paths :test-paths :test-selectors])
                    (when-not (empty? args)
                      (walk/keywordize-keys (apply assoc {} args))))]
    (try
      (eval/eval-in-project project
                            `(canvas.core/evaluate-test-coverage '~opts)
                            '(require 'canvas.core))
      (catch ExceptionInfo e
        (main/exit (:exit-code (ex-data e) 1))))))
