(ns leiningen.canvas
  (:require [leiningen.core.eval :as eval]
            [leiningen.core.main :as main])
  (:import (clojure.lang ExceptionInfo)))

(defn- canvas-plugin-dep [project]
  (->> (:plugins project)
       (filter #(= (first %) 'canvas/canvas))
       (first)))

(defn ^:pass-through-help canvas
  [project & args]
  (let [project (update-in project [:dependencies] conj (canvas-plugin-dep project))
        test-selectors (:test-selectors project)
        opts           {:src-ns-path (vec (:source-paths project))
                        :test-ns-path (vec (:test-paths project))}]
    (try
      (eval/eval-in-project project
                            `(canvas.core/evaluate-test-coverage '~(:src-ns-path opts) '~(:test-ns-path opts))
                            '(require 'canvas.core))
      (catch ExceptionInfo e
        (main/exit (:exit-code (ex-data e) 1))))))
