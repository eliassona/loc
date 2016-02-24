(ns loc.core
  (:import [java.io File]))


(defmacro dbg [body]
  `(let [x# ~body]
     (println "dbg:" '~body "=" x#)
     x#))

(defn java-filter [f] (.endsWith (.getName f) ".java"))
(defn scala-filter [f] (.endsWith (.getName f) ".scala"))
(defn clojure-filter [f] (.endsWith (.getName f) ".clj"))


(defn count-loc [f] (count (.split (slurp f) "\n")))

(defn _loc [filter-fn f]
  (if (.isFile f)
    (if (filter-fn f)
      (count-loc f)
      0)
    (reduce + (map (partial _loc filter-fn) (.listFiles f)))))
  
(defn loc 
  ([filter-fn f-name]
  (_loc filter-fn (File. f-name)))  
  ([f-name]
    (loc java-filter f-name)))
