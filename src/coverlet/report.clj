(ns coverlet.report
  (:require [coverlet.instrument :as ins]))

(defn- user? [ns]
  (= "user" (name (ns-name ns))))

(defn- console [stats]
  (let [max-nsname-len (->> stats keys
                            (map name)
                            (map count)
                            (apply max))
        nsname-fmt-str (str "  %s %-" (inc max-nsname-len) "s%s")
        max-chars-per-line (+ max-nsname-len 20)
        title " Coverlet Test Coverage Report "
        half-long-equal-sign (apply str (repeat (/ (- max-chars-per-line
                                                      (count title))
                                                   2)
                                                "="))]
    (println (str half-long-equal-sign title half-long-equal-sign))
    (doseq [[nsname ns-stats] stats]
      (println nsname)
      (doseq [[fn-name fn-stat] ns-stats]
        (let [visibility-indicator (if (:private fn-stat) "-" "+")
              tested-indicator (if (:tested? fn-stat) "T" " ")]
          (println (format nsname-fmt-str visibility-indicator fn-name
                           (if (zero? (:hit fn-stat))
                             "/  /"
                             (format "%s %2d" tested-indicator (:hit fn-stat))))))))
    (println (apply str (repeat max-chars-per-line "=")))))

(defn report [nss reporter]
  (let [stats (into {}
                    (for [n nss :when (not (user? n))]
                      (let [ns-stats (into {}
                                           (for [[_ v] (ns-interns n) :when (ins/instrumentable? v)]
                                             [(:name (meta v)) (let [mt (meta v)]
                                                                 {:private (:private mt)
                                                                  :line    (:line mt)
                                                                  :tested? (:coverlet.instrument/tested? mt)
                                                                  :hit     (:coverlet.instrument/hit mt)})]))]
                        (when-not (empty? ns-stats)
                          [(ns-name n) ns-stats]))))]
    (case reporter
      (console stats))))
