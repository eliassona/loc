(ns loc.core
  (:use [clojure.pprint])
  (:require [apartial.core :refer [apartial]])
  (:import [java.io File]))

(defn java-filter [f] (.endsWith (.getName f) ".java"))
(defn scala-filter [f] (.endsWith (.getName f) ".scala"))
(defn clojure-filter [f] (.endsWith (.getName f) ".clj"))


(defn count-loc [f] (count (.split (slurp f) "\n")))
(defprotocol ILoc
  (loc [this filter-fn]))

(extend-protocol ILoc
  File
  (loc [f filter-fn]
    (if (.isFile f)
    (if (filter-fn f)
      (count-loc f)
      0)
    (reduce + (map (apartial loc _ filter-fn) (.listFiles f)))))
  String 
  (loc [filename filter-fn] (loc (File. filename) filter-fn))
  java.util.List
  (loc [l filter-fn] (map (apartial loc _ filter-fn) l)) 
  )
  

(defn package-loc 
  ([package-dir sub-dir]
  (apply hash-map (mapcat (fn [f] [(keyword (.getName f)), {(keyword sub-dir) (loc (File. f sub-dir) #(or (java-filter %) (scala-filter %)))}]) (filter #(.isDirectory %) (.listFiles (File. package-dir))))))
  ([package-dir]
    (merge-with 
      merge
      (package-loc package-dir "src")
      (package-loc package-dir "test")
      (package-loc package-dir "build")
   )))
