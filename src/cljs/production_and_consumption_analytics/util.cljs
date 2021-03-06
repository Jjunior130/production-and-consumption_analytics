(ns production-and-consumption-analytics.util
 (:require [datascript.core :as d]
           [clojure.set :as s]
           [cljs-time.core :as t]))

(defn abs
  "Returns the absolute value of a number."
  [x]
  (if (neg? x) (- x) x))

(def tempid (let [n (atom 0)]
              (fn []
                (swap! n dec))))

(defn dimension-id [conn formula]
  (d/q '[:find ?e .
         :in $ ?formula
         :where
         [?e :dimension/formula ?formula]]
       @conn
       formula))

(defn transact-all-units [conn units fundamentals]
  (doseq [[name {si-conversion-factor :v formula :u} :as unit] units]
    (d/transact conn
                (let [id (tempid)]
                  [{:db/id id
                    :unit/name name
                    :unit/si-conversion-factor si-conversion-factor
                    :unit/dimension (dimension-id conn formula)}
                   (if (fundamentals name)
                     [:db/add id :unit/si-base? true])]))))

(defn transact-all-dimensions [conn all-dimensions]
 (doseq [[f n] all-dimensions]
  (d/transact conn
              (let [id (tempid)]
               [[:db/add id :dimension/formula f]
                (when n
                 [:db/add id :dimension/name n])]))))

(defn add-data [conn units dimensions fundamentals]
  ;; transact all dimension's formula
 (let [all-dimensions (merge (into {} (comp (map (comp :u val))
                                            (map #(vector % nil)))
                                   units)
                             dimensions)]
    (transact-all-dimensions conn all-dimensions))
  ;; transact all units
 (transact-all-units conn units fundamentals))


