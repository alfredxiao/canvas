(ns coverlet.instrument)

(defn- instrument
  [f v]
  (fn [& args]
    (alter-meta! v assoc ::tested? true)
    (alter-meta! v update ::hit inc)
    (apply f args)))

(defn instrumentable?
  [v]
  (and (.isBound v)
       (fn? @v)
       (not (:macro (meta v)))
       (not (:test (meta v)))))

(defn instrument-var! [v]
  (when (instrumentable? v)
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
    (apply-to-each-var instrument-var! n)))

(defn uninstrument-ns! [& nss]
  (doseq [ns nss]
    (apply-to-each-var uninstrument-var! ns)))
