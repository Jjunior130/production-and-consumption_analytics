(ns production-and-consumption-analytics.core
    (:require [reagent.core :as reagent]
              [re-frame.core :as re-frame]
              [devtools.core :as devtools]
              [production-and-consumption-analytics.handlers]
              [production-and-consumption-analytics.subs]
              [production-and-consumption-analytics.db :refer [conn]]
              [production-and-consumption-analytics.routes :as routes]
              [production-and-consumption-analytics.views :as views]
              [production-and-consumption-analytics.config :as config]
              [production-and-consumption-analytics.util :refer [tempid]]
              [production-and-consumption-analytics.units :refer [units-data]]
              [datascript.core :as d]))

(defn dev-setup []
  (when config/debug?
    (println "dev mode")
    (devtools/install!)))

(defn mount-root []
 (reagent/render [views/main-panel]
  (.getElementById js/document "app")))

(defn ^:export init []
  (routes/app-routes)
  (re-frame/dispatch-sync [:initialize-db])
  (re-frame/dispatch-sync [:add-conn conn])
  (re-frame/dispatch-sync [:load-units units-data])
  (dev-setup)
  (mount-root))
