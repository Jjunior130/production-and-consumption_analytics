(ns production-and-consumption-analytics.util
 (:require-macros [com.rpl.specter :refer [transform select select-one]])
 (:require [datascript.core :as d]
           [clojure.set :as s]
           [cljs-time.core :as t]
           [com.rpl.specter :refer [ALL FIRST LAST view selected? not-selected?]]))

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

(defn transact-dimension-formulas [conn all-formulas]
  (doseq [formula all-formulas]
    (d/transact conn
                [[:db/add (tempid) :dimension/formula formula]])))

(defn transact-dimension-names [conn dimensions]
  (doseq [[formula name :as dimension] dimensions]
    (d/transact conn
                [[:db/add (dimension-id conn formula) :dimension/name name]])))

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
 (doseq [[n f] all-dimensions]
  (d/transact conn
              (let [id (tempid)]
               [[:db/add id :dimension/formula f]
                (when n
                 [:db/add id :dimension/name n])]))))

(defn add-data [conn units dimensions fundamentals]
  ;; transact all dimension's formula
  (let [all-dimensions
        (->> units
             (select [ALL LAST :u
                      (not-selected? #(contains? (->> dimensions
                                                      (select [ALL LAST]))
                                                 %))
                      (view #(vector nil %))])
             (concat dimensions))]
    (transact-all-dimensions conn all-dimensions))
  ;; transact all units
  (transact-all-units conn units fundamentals))


