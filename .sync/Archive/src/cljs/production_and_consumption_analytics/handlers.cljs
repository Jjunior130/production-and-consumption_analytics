(ns production-and-consumption-analytics.handlers
  (:require [re-frame.core :as re-frame]
            [production-and-consumption-analytics.db :as db]
            [datascript.core :as d]
            [production-and-consumption-analytics.util :refer [add-data]]
            [production-and-consumption-analytics.units :refer [units-data]]
            [clojure.set :as s]))

(re-frame/register-handler
  :initialize-db
  (fn [_ _]
    db/default-db))

(re-frame/register-handler
  :add-conn
  re-frame/trim-v
  (fn [db [conn]]
    (assoc db :ds conn)))

(re-frame/register-handler
  :load-units
  (fn [db [_ units-data]]
    (let [conn (:ds db)]
      (add-data conn
                (:units units-data)
                (:fundamental-units units-data)
                (s/difference (:fundamentals units-data) #{"bit" "dollar"}))
      db)))

(re-frame/register-handler
 :transact
 (fn [db [_ transaction & {:keys [ds-id] :or {ds-id :ds}}]]
   (let [conn (get db ds-id)]
     (d/transact conn transaction)
     db)))

(re-frame/register-handler
  :set-active-panel
  (fn [db [_ active-panel]]
   (assoc db :active-panel active-panel)))

(re-frame/register-handler
  :set-unit
  [(re-frame/path :input :unit) re-frame/trim-v]
  (fn [old-unit [new-unit]]
    new-unit))

(re-frame/register-handler
  :set-input-amount
  [(re-frame/path :input :amount) re-frame/trim-v]
  (fn [old-amount [new-amount]]
    (let [v (js/Number new-amount)]
      (if-not ((some-fn js/isNaN zero? neg?) v)
        v
        js/NaN))))

(re-frame/register-handler
  :record-production
  [(re-frame/path :transaction-history) re-frame/trim-v]
  (fn [transaction-history [[type amount] timestamp]]
    (conj transaction-history {:physical-quantity [type (if (pos? amount)
                                                         amount
                                                         (- amount))]
                               :timestamp timestamp})))

(re-frame/register-handler
  :record-consumption
  [(re-frame/path :transaction-history) re-frame/trim-v]
  (fn [transaction-history [[type amount] timestamp]]
    (conj transaction-history {:physical-quantity [type (if (neg? amount)
                                                         amount
                                                         (- amount))]
                               :timestamp timestamp})))
