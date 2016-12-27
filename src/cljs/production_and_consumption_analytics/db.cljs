(ns production-and-consumption-analytics.db
  (:require [production-and-consumption-analytics.units :refer [units-data]]
            [datascript.core :as d]
            [posh.reagent :refer [posh!] :as posh]))

(def schema
  {:transaction/transactioner {:db/cardinality :db.cardinality/one}
   :transaction/amount {:db/cardinality :db.cardinality/one}
   :transaction/unit {:db/valueType :db.type/ref
                      :db/cardinality :db.cardinality/one}
   :transaction/timestamp {:db/cardinality :db.cardinality/one}
   :unit/name {}
   :unit/si-conversion-factor {:db/cardinality :db.cardinality/one}
   :unit/dimension {:db/valueType :db.type/ref
                    :db/cardinality :db.cardinality/one}
   :dimension/formula {:db/cardinality :db.cardinality/one}
   :dimension/name {:db/cardinality :db.cardinality/many}})

(defonce conn (d/create-conn schema))

(posh! conn)

(def default-db
  {})
