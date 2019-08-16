(ns coverlet.report
  (:require [coverlet.instrument :as ins]))

(defn- user? [ns]
  (= "user" (name (ns-name ns))))

(def ^:private tick-fn-covered-n-pass "\u2714")
(def ^:private tick-fn-covered-but-fail "\u2718")
(def ^:private tick-fn-not-covered "\u2B1A")
(def ^:private tick-ns-fully-covered "\u2705")
(def ^:private tick-ns-partial-coverage "\u26C8")
(def ^:private tick-ns-no-coverage "\u274C")
(def ^:private border-sign "\u2550")

(def ^:private percentage->signs
  {(/ 1 4)  "\u00BC"
   (/ 1 2)  "\u00BD"
   (/ 3 4)  "\u00BE"
   (/ 1 7)  "\u2150"
   (/ 1 9)  "\u2151"
   (/ 1 10) "\u2152"
   (/ 1 3)  "\u2153"
   (/ 2 3)  "\u2154"
   (/ 1 5)  "\u2155"
   (/ 2 5)  "\u2156"
   (/ 3 5)  "\u2157"
   (/ 4 5)  "\u2158"
   (/ 1 6)  "\u2159"
   (/ 5 6)  "\u215A"
   (/ 1 8)  "\u215B"
   (/ 3 8)  "\u215C"
   (/ 5 8)  "\u215D"
   (/ 7 8)  "\u215E"})

(def ^:private percentages-sorted
  (sort (keys percentage->signs)))

(defn- closer-percentage [percentage [left right]]
  (when (<= left percentage right)
    (let [n1 (.numerator percentage)
          n2 (.denominator percentage)
          l1 (.numerator left)
          l2 (.denominator left)
          r1 (.numerator right)
          r2 (.denominator right)
          mid (* n1 l2 r2)
          d1 (- mid (* l1 n2 r2))
          d2 (- (* r1 l2 n2) mid)]
      (if (< d1 d2) left right))))

(defn- percentage-sign [tested fn-count]
  (percentage->signs
    (let [percentage (/ tested fn-count)
          percentage-pairs (partition 2 (interleave percentages-sorted
                                                    (rest percentages-sorted)))
          min-percentage (first percentages-sorted)
          max-percentage (last percentages-sorted)]
      (cond
        (<= percentage min-percentage) min-percentage
        (>= percentage max-percentage) max-percentage
        :else (->> percentage-pairs
                   (map (partial closer-percentage percentage))
                   (remove nil?)
                   (first))))))

;; Report Line Format
;; [indicator][name][cov-sign][hit]
;; e.g. [⛈ ][toy-robot.core       ][½][   ]
;;      [  ] [+ play               ][✔][  3]
;;      [  ] [+ go                 ][⬚][   ]

(defn- line-fmt [width]
  (str "%-2s%-" width "s %s %3s"))

(defn- format-ns-headline [width name tested-count fn-count]
  (format (line-fmt width)
          (cond
            (= 0 fn-count) "?"
            (= 0 tested-count) tick-ns-no-coverage
            (= tested-count fn-count) tick-ns-fully-covered
            :else tick-ns-partial-coverage)
          name
          (if (< 0 tested-count fn-count)
            (percentage-sign tested-count fn-count)
            "")
          ""))

(defn- format-fn-line [width private? name tested? hit]
  (format (line-fmt width)
          ""
          (str (if private? "- " "+ ") name)
          (if tested? tick-fn-covered-n-pass tick-fn-not-covered)
          (if tested? hit "")))



(defn- console [stats]
  (let [max-nsname-len (->> stats
                            (keys)
                            (map name)
                            (map count)
                            (apply max))
        max-fn-name-len (->> stats
                             (vals)
                             (map keys)
                             (flatten)
                             (map name)
                             (map count)
                             (apply max))
        name-width (+ 3 (max max-nsname-len max-fn-name-len))
        max-chars-per-line (+ max-nsname-len 20)
        title " Coverlet Report "
        half-long-equal-sign (apply str (repeat (/ (- max-chars-per-line
                                                      (count title))
                                                   2)
                                                border-sign))]
    (println)
    (println (str half-long-equal-sign title half-long-equal-sign))
    (doseq [[nsname ns-stat] stats]
      (let [fn-stats (vals ns-stat)
            fn-count (count fn-stats)
            tested-count (count (filter :tested? fn-stats))]
        (println (format-ns-headline name-width nsname tested-count fn-count)))
      (doseq [[fn-name fn-stat] ns-stat]
        (let [private? (:private fn-stat)]
          (println (format-fn-line name-width private? fn-name (:tested? fn-stat) (:hit fn-stat))))))
    (println (apply str (repeat max-chars-per-line border-sign)))))

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
    (console stats)))