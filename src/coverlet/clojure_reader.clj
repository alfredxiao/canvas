(ns coverlet.clojure-reader
  (:require [clojure.tools.reader :as r]
            [clojure.tools.reader.reader-types :as rt]
            [clojure.inspector :as insp])
  (:import (java.io PushbackReader StringReader)
           (clojure.lang LineNumberingPushbackReader)))


(defn- source->reader [source-path]
  (-> source-path
      (slurp)
      (StringReader.)
      (LineNumberingPushbackReader.)
      (rt/indexing-push-back-reader)))

(defn source->forms [source-path]
  (let [rdr (source->reader source-path)]
    (loop [forms []]
      (if-let [form (r/read {:eof nil
                             :features #{:clj}
                             :read-cond :allow}
                            rdr)]
        (recur (conj forms form))
        forms))))