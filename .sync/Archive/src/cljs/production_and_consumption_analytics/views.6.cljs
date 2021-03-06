(ns production-and-consumption-analytics.views
 (:require [re-frame.core :as re-frame]
           [cljs-time.core :as t]
           [production-and-consumption-analytics.util :refer [tempid]]
           [posh.reagent :refer [posh!] :as posh]
           [production-and-consumption-analytics.db :refer [conn]]
           [production-and-consumption-analytics.components :as components]
           [datascript.core :as d]
           [reagent-modals.modals :as reagent-modals]
           [reagent.ratom :refer [atom]]
           [re-com.buttons :refer [md-circle-icon-button row-button button hyperlink]]
           [re-com.box :refer [v-box h-box box gap line scroller border]]
           [re-com.selection-list :refer [selection-list]]
           [re-com.tabs :refer [horizontal-pill-tabs]]
           [re-com.splits :refer [h-split v-split]]
           [re-com.text :refer [title label]]
           [re-com.alert :refer [alert-list alert-box]]
           [re-com.typeahead :refer [typeahead]]
           [re-com.dropdown :refer [single-dropdown]]
           [re-com.misc :refer [throbber input-text input-textarea slider radio-button checkbox]]
           [re-com.datepicker :refer [datepicker datepicker-dropdown]]
           [re-com.input-time :refer [input-time]]
           goog.string.format
           [clojure.string :refer [split blank?]]))

;; home

(defn analysis []
 (let [transaction-history (re-frame/subscribe
                            [:q '[:find ?type ?amount ?u-name ?timestamp
                                  :where
                                  [?e :transaction/type ?type]
                                  [?e :transaction/physical-quantity ?t-p-q-e]
                                  [?t-p-q-e :physical-quantity/unit ?u-e]
                                  [?t-p-q-e :physical-quantity/amount ?amount]
                                  [?u-e :unit/name ?u-name]
                                  [?e :transaction/timestamp ?timestamp]]])]
  (fn []
   [:section {:style {:height "11em"}}
    [:div.lead
     "production & consumption history: " (pr-str @transaction-history)]
    [:div.container
     [:div.row
      [:div.col-sm-12
       [:div.btn-group.btn-group-lg.btn-group-justified
        [:a.btn.btn-info.btn-lg
         "Filter"]
        [:a.btn.btn-info.btn-info-outline.btn-lg
         "Analyze"]]]]]])))

(defn transactioner [& {:keys [id]}]
 (let [t (re-frame/subscribe
          [:pull '[*] id])

       timer (atom {:value 0
                    :enabled? false
                    :running? false})
       edit (atom {:label false
                   :interval false
                   :timestamp false})]
  (fn [& {:keys [id]}]
   (let [{amount :transactioner/amount
          selected-unit :transactioner/selected-unit
          units :transactioner/units
          type :transactioner/type
          label :transactioner/label
          formula :transactioner/formula
          timestamp :transactioner/timestamp} @t
         timer-value (:value @timer)
         timer-running (:running? @timer)
         timer-enabled (:enabled? @timer)
         interval (t/period
                   :hours (quot timer-value 36000)
                   :minutes (mod (quot timer-value 600) 60)
                   :seconds (mod (quot timer-value 10) 60)
                   :millis (rem timer-value 10))]
    (when timer-running
     (js/setTimeout #(swap! timer update :value inc) 100))
    [:div.panel.panel-primary
     [h-box
      :children [[title
                  :label [h-box
                          :children [(if-not (:label @edit)
                                      [re-com.text/label
                                       :label [:h1 label]
                                       :on-click #(swap! edit update :label not)]

                                      [input-text
                                       :model label
                                       :on-change #(do (re-frame/dispatch
                                                        [:transact [[:db/add id :transactioner/label %]]])
                                                    (swap! edit update :label not))])
                                     (apply str formula)]]
                  :level :level2]
                 [re-com.text/label
                  :label (case type
                          :production [h-box
                                       :children [[re-com.text/label
                                                   :label "consumption"
                                                   :style {:font-size "2.5em"}]
                                                  [:i.zmdi.zmdi-arrow-right.zmdi-hc-4x
                                                   {:style {}}]
                                                  [re-com.text/label
                                                   :label "production"
                                                   :class "label label-success"
                                                   :style {:font-size "2.5em"}]]]
                          :consumption [h-box
                                        :children [[re-com.text/label
                                                    :label "consumption"
                                                    :class "label label-danger"
                                                    :style {:font-size "2.5em"}]
                                                   [:i.zmdi.zmdi-arrow-left.zmdi-hc-4x
                                                    {:style {}}]
                                                   [re-com.text/label
                                                    :label "production"
                                                    :style {:font-size "2.5em"}]]])
                  :on-click #(do (swap! timer assoc :running? false)
                              (re-frame/dispatch
                               [:transact [[:db/add id :transactioner/type (case type
                                                                            :production :consumption
                                                                            :consumption :production)]]]))]

                 [hyperlink
                  :label [:span {:aria-hidden false}]
                  :class "glyphicon glyphicon-remove"
                  :on-click #(re-frame/dispatch
                              [:transact [[:db.fn/retractEntity id]]])]]
      :justify :between
      :class "panel-heading"]
     [v-box
      :children [[h-box
                  :children [[checkbox
                              :model timer-enabled
                              :on-change (fn []
                                          (swap! timer update :enabled? not)
                                          (when-not timer-enabled
                                           (swap! timer assoc :running? false))
                                          (re-frame/dispatch
                                              [:transact [[:db/retract id :transactioner/timestamp timestamp]]]))]
                             (when timer-enabled
                              (if-not timer-running
                               [h-box
                                :children [[button
                                            :label "Clock-in"
                                            :class (case type
                                                    :consumption "btn btn-danger"
                                                    :production "btn btn-success")
                                            :disabled? timer-running
                                            :on-click (fn []
                                                       (re-frame/dispatch
                                                        [:transact [[:db/retract id :transactioner/timestamp timestamp]]])
                                                       (reset! timer {:value 0
                                                                      :enabled? true
                                                                      :running? true})
                                                       (swap! edit assoc :interval false))]

                                           (when (or timestamp
                                                     (> timer-value 0))
                                            [button
                                             :label ""
                                             :class "zmdi zmdi-play btn btn-warning"
                                             :on-click #(do (re-frame/dispatch
                                                             [:transact [[:db/retract id :transactioner/timestamp timestamp]]])
                                                         (swap! timer assoc :running? true)
                                                         (swap! edit assoc :interval false))])]]


                               [button
                                :label "Clock-out"
                                :class (case type
                                        :consumption "btn btn-success"
                                        :production "btn btn-danger")
                                :on-click #(let [interval timer-value]
                                            (swap! timer assoc :value interval)
                                            (re-frame/dispatch
                                             [:transact [[:db/add id :transactioner/timestamp (t/now)]]])
                                            (swap! timer assoc :running? false))]))
                             [hyperlink
                              :label [:span]
                              :class "zmdi zmdi-refresh-alt"
                              :on-click #(do (re-frame/dispatch
                                              [:transact [[:db/retract id :transactioner/timestamp timestamp]]])
                                          (reset! timer {:value 0
                                                         :enabled? true
                                                         :running? false}))
                              :disabled? (not timer-enabled)]]]
                 [h-box
                  :children [(if-not (:interval @edit)
                              [re-com.text/label
                               :label [:div {:style {:font-family "Digital"
                                                     :font-size "2em"}}
                                       (cond
                                        (= 0 (:minutes interval)) (str (apply str (interpose ":" (map #(% interval)
                                                                                                      [:seconds :millis])))
                                                                       (if (= 1 (:seconds interval))
                                                                        " second"
                                                                        " seconds"))
                                        (= 0 (:hours interval)) (let [[m s ms] (map #(% interval)
                                                                                    [:minutes :seconds :millis])]
                                                                 (str (apply str (interpose ":" [m (goog.string.format "%02d" s) ms]))
                                                                      (if (= 1 (:minutes interval))
                                                                       " minute"
                                                                       " minutes")))
                                        :default (let [[h m s ms] (map #(% interval)
                                                                       [:hours :minutes :seconds :millis])]
                                                  (str (apply str (interpose ":" [h (goog.string.format "%02d" m) (goog.string.format "%02d" s) ms]))
                                                       (if (= 1 (:hours interval))
                                                        " hour"
                                                        " hours"))))]
                               :on-click #(do (swap! edit assoc :interval true)
                                              (swap! timer assoc :running? false))]
                              [input-text
                               :model (cond
                                       (= 0 (:minutes interval)) (str (apply str (interpose ":" (map #(% interval)
                                                                                                     [:seconds :millis]))))
                                       (= 0 (:hours interval)) (let [[m s ms] (map #(% interval)
                                                                                   [:minutes :seconds :millis])]
                                                                (str (apply str (interpose ":" [m (goog.string.format "%02d" s) ms]))))
                                       :default (let [[h m s ms] (map #(% interval)
                                                                      [:hours :minutes :seconds :millis])]
                                                 (str (apply str (interpose ":" [h (goog.string.format "%02d" m) (goog.string.format "%02d" s) ms])))))
                               :on-change #(do (swap! edit assoc :interval false)
                                            (swap! timer assoc :value (let [m-t-s (map int (split % #":"))]
                                                                       (case (count m-t-s)
                                                                        2 (let [[s ms] m-t-s]
                                                                           (+ (* s 10) ms))
                                                                        3 (let [[m s ms] m-t-s]
                                                                           (+ (* m 600) (* s 10) ms))
                                                                        4 (let [[h m s ms] m-t-s]
                                                                           (+ (* h 36000) (* m 600) (* s 10) ms))))))
                               :change-on-blur? true
                               :validation-regex #"^([0-9]+)?:?([0-5][0-9])?:?([0-5][0-9])?:?[0-9]?$"])
                             [:i.zmdi.zmdi-time-interval.zmdi-hc-2x]]

                  :style (if timer-enabled
                          {}
                          {:display "none"})]
                 [h-box
                  :children [[box
                              :child [input-text
                                      :model amount
                                      :on-change #(re-frame/dispatch
                                                   [:transact [[:db/add id :transactioner/amount %]
                                                               (if-not (and (not timer-enabled)
                                                                            (blank? %))
                                                                (when-not timer-enabled
                                                                 [:db/add id :transactioner/timestamp (t/now)])
                                                                [:db/retract id :transactioner/timestamp timestamp])]])
                                      :validation-regex #"^\d*\.?\d+$|^$|^\d+\.$"
                                      :width "100%"
                                      :class "form-control"]
                              :size "auto"
                              :class (str "form-group " (if (and timer-enabled
                                                             (not timestamp))
                                                         "has-error"
                                                         "has-success"))]
                             [single-dropdown
                              :filter-box? true
                              :choices (sort-by :unit/si-conversion-factor units)
                              :model (:db/id selected-unit)
                              :id-fn #(:db/id %)
                              :label-fn #(:unit/name %)
                              :group-fn #(str (:unit/si-conversion-factor %))
                              :on-change #(re-frame/dispatch
                                           [:transact [[:db/add id :transactioner/selected-unit %]]])]]]
                 [h-box
                  :children [(let [timestamp-fn (if timestamp
                                                 #(identity timestamp)
                                                 #(t/now))
                                   disable-submit? (or ((some-fn js/isNaN zero? neg?) (js/Number amount))
                                                       (and timer-enabled
                                                            (or timer-running
                                                                (not timestamp))))]
                              [button
                               :label "Submit"
                               :class "btn btn-lg btn-primary"
                               :disabled? disable-submit?
                               :on-click #(re-frame/dispatch
                                           [:transact (let [transaction-id (tempid)
                                                            t-p-q-id (tempid)]
                                                       [{:db/id t-p-q-id
                                                         :physical-quantity/amount (js/Number amount)
                                                         :physical-quantity/unit (:db/id selected-unit)}
                                                        [:db/add id :transactioner/timestamp (timestamp-fn)]
                                                        (merge {:db/id transaction-id
                                                                :transaction/type type
                                                                :transaction/physical-quantity t-p-q-id
                                                                :transaction/timestamp (timestamp-fn)}
                                                               (if (and timer-enabled
                                                                    timestamp)
                                                                {:transaction/interval timer-value}))])])])
                             (when timestamp
                              [h-box
                               :children (let [day (t/day timestamp)
                                               month (t/month timestamp)
                                               year (t/year timestamp)
                                               hour (t/hour timestamp)
                                               minute (t/minute timestamp)
                                               second (t/second timestamp)
                                               time (map (partial goog.string.format "%02d")
                                                         [hour minute second])
                                               time-string (apply str (interpose ":" time))]
                                          [[datepicker-dropdown
                                            :model timestamp
                                            :on-change #(let [new-day (t/day %)
                                                              new-month (t/month %)
                                                              new-year (t/year %)]
                                                         (re-frame/dispatch
                                                          [:transact [[:db/add id :transactioner/timestamp (t/date-time new-year new-month
                                                                                                                        new-day hour
                                                                                                                        minute second)]]]))
                                            :selectable-fn #(t/before? % (t/today))]
                                           [h-box
                                            :children [(if-not (:timestamp @edit)
                                                        [re-com.text/label
                                                         :label [:div {:style {:font-family "Digital"}} time-string]
                                                         :on-click #(swap! edit assoc :timestamp true)
                                                         :style {:font-size "2em"}]
                                                        [input-text
                                                         :model time-string
                                                         :on-change #(let [[new-hour new-minute new-second] (map int (split % #":"))]

                                                                      (re-frame/dispatch
                                                                       [:transact [[:db/add id :transactioner/timestamp (t/date-time year month
                                                                                                                                     day new-hour
                                                                                                                                     new-minute new-second)]]])
                                                                      (swap! edit assoc :timestamp false))
                                                         :validation-regex #"^([0-9]|[0-1][0-9]|2[0-3])?"])
                                                       [:i.zmdi.zmdi-time.zmdi-hc-2x]]]])])]]]
      :class "panel-body"]]))))

(defn select-dimension [& {:keys [label id]}]
 (let [dimension (re-frame/subscribe
                  [:pull '[:dimension/name :dimension/formula
                           {:unit/_dimension [:unit/name :unit/si-conversion-factor :unit/dimension]}] id])
       transactioners (re-frame/subscribe
                       [:q '[:find [?transactioner ...]
                             :where
                             [~id :dimension/formula ?f]
                             [?transactioner :transactioner/formula ?f]]])]
  (fn [& {:keys [label id]}]
   (let [transactioner-count (->> @transactioners
                                  count)
         dimension-name (if-let [name (first (:dimension/name @dimension))]
                         name
                         (str (:dimension/formula @dimension)))]
    [hyperlink
     :label [h-box
             :children [[title
                         :label dimension-name
                         :level :level2]
                        [re-com.text/label
                         :label (str transactioner-count)
                         :class "badge"]]]
     :on-click #(re-frame/dispatch
                  [:transact (let [transactioner-id (tempid)
                                   units (:unit/_dimension @dimension)]
                              [{:db/id transactioner-id
                                :transactioner/label dimension-name
                                :transactioner/units units
                                :transactioner/type (rand-nth '[:production :consumption])
                                :transactioner/amount ""
                                :transactioner/selected-unit (rand-nth units)
                                :transactioner/formula (:dimension/formula @dimension)}])])]))))

(defn home-panel []
 (let [dimension-ids (re-frame/subscribe [:q '[:find [?dimension ...]
                                               :where
                                               [?dimension :dimension/formula]]])
       transactioner-ids (re-frame/subscribe [:q '[:find [?transactioner ...]
                                                   :where
                                                   [?transactioner :transactioner/label]]])]
  (fn []
   [:div
    [reagent-modals/modal-window]
    [v-box
     :children [[analysis]
                [v-box
                 :children (concat [[md-circle-icon-button
                                     :md-icon-name "zmdi-plus"
                                     :on-click (fn []
                                                   (reagent-modals/modal!
                                                    (for [dimension-id @dimension-ids]
                                                         [select-dimension
                                                          :id dimension-id])))
                                     :size :larger
                                     :emphasise? true
                                     :class "center-block"]]
                                   (for [id @transactioner-ids]
                                    [transactioner
                                     :id id]))]]

     :class "container"]
    [:div [:a {:href "#/about"} "go to About Page"]]])))

;; about

(defn about-panel []
 (fn []
  [:div "This is the About Page."
   [:div [:a {:href "#/"} "go to Home Page"]]]))


;; main

(defmulti panels identity)
(defmethod panels :home-panel [] [home-panel])
(defmethod panels :about-panel [] [about-panel])
(defmethod panels :default [] [:div])

(defn show-panel
 [panel-name]
 [panels panel-name])

(defn main-panel []
 (let [active-panel (re-frame/subscribe [:active-panel])]
  (fn []
   [show-panel @active-panel])))
