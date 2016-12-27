(ns production-and-consumption-analytics.subs
 (:require-macros [reagent.ratom :refer [reaction]])
 (:require [re-frame.core :as re-frame]
           [posh.reagent :as posh]
           [datascript.core :as d]))

(re-frame/register-sub
 :active-panel
 (fn [db _]
  (reaction (:active-panel @db))))

(re-frame/register-sub
 :db
 (fn [db _]
  (reaction @db)))

(re-frame/register-sub
 :transaction-history
 (fn [db]
  (let [conn (:ds @db)]
   (posh/q '[:find ?type ?amount ?u-name ?timestamp
             :where
             [?e :transaction/type ?type]
             [?e :transaction/physical-quantity ?t-p-q-e]
             [?t-p-q-e :physical-quantity/unit ?u-e]
             [?t-p-q-e :physical-quantity/amount ?amount]
             [?u-e :unit/name ?u-name]
             [?e :transaction/timestamp ?timestamp]]
           conn))))

(re-frame/register-sub
 :pull
 (fn [db [_ pattern entity]]
  (let [conn (:ds @db)]
   (posh/pull conn pattern entity))))

(re-frame/register-sub
 :q
 (fn [db [_ query & args]]
  (let [conn (:ds @db)]
   (apply posh/q query conn args))))

(re-frame/register-sub
 :input
 (fn [db]
  (let [conn (:ds @db)]
   (posh/q '[:find ?unit-name ?amount ?p-q
             :where
             [?input :input/physical-quantity ?p-q]
             [?p-q :physical-quantity/amount ?amount]
             [?p-q :physical-quantity/unit ?unit]
             [?unit :unit/name ?unit-name]]
            conn))))

(re-frame/register-sub
 :units
 (fn [db]
  (let [conn (:ds @db)]
   (posh/q '[:find ?unit
             :where
             [?unit :unit/name]]
           conn))))
