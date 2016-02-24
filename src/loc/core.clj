(ns loc.core
  (:use [clojure.pprint])
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

(defn package-loc 
  ([package-dir sub-dir]
  (apply hash-map (mapcat (fn [f] [(keyword (.getName f)), {(keyword sub-dir) (_loc #(or (java-filter %) (scala-filter %)) (File. f sub-dir))}]) (filter #(.isDirectory %) (.listFiles (File. package-dir))))))
  ([package-dir]
    (merge-with 
      merge
      (package-loc package-dir "src")
      (package-loc package-dir "test")
      (package-loc package-dir "build")
   )))
