(defproject production-and-consumption-analytics "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.8.51"]
                 [reagent "0.5.1"]
                 [re-com "0.9.0"]
                 [binaryage/devtools "0.6.1"]
                 [re-frame "0.7.0"]
                 [secretary "1.2.3"]
                 [bux "0.3.0"] ;; Currency units
                 [com.andrewmcveigh/cljs-time "0.4.0"] ;; Time
                 [posh "0.5.3.3"] ;; immutable in-memory database
                 [com.rpl/specter "0.13.1"] ;; Data transformation
                 [frinj "0.2.5"] ;; Unit of measurement
                 [datascript-transit "0.2.1"]
                 [org.clojars.frozenlock/reagent-modals "0.2.5"]]


  :plugins [[lein-cljsbuild "1.1.3"]]

  :min-lein-version "2.5.3"

  :source-paths ["src/clj"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :figwheel {:css-dirs ["resources/public/css"]}

  :profiles
  {:dev
   {:dependencies [[org.clojure/tools.namespace "0.2.11"]]

    :plugins      [[lein-figwheel "0.5.4-3"]]}}


  :cljsbuild
  {:builds
   [{:id           "dev"
     :source-paths ["src/cljs"]
     :figwheel     {:on-jsload "production-and-consumption_analytics.core/mount-root"}
     :compiler     {:main                 production-and-consumption_analytics.core
                    :output-to            "resources/public/js/compiled/app.js"
                    :output-dir           "resources/public/js/compiled/out"
                    :asset-path           "js/compiled/out"
                    :source-map-timestamp true}}

    {:id           "min"
     :source-paths ["src/cljs"]
     :compiler     {:main            production-and-consumption_analytics.core
                    :output-to       "resources/public/js/compiled/app.js"
                    :optimizations   :advanced
                    :closure-defines {goog.DEBUG false}
                    :pretty-print    false}}]})
