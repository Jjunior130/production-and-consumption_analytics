(ns production-and-consumption-analytics.views
 (:require [re-frame.core :as re-frame]
           [cljs-time.core :as t]
           [production-and-consumption-analytics.util :refer [tempid abs]]
           [posh.reagent :refer [posh!] :as posh]
           [production-and-consumption-analytics.db :refer [conn]]
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
           [goog.string :as gstring]
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
          [:pull '[*
                   {:transactioner/total-unit
                    [:unit/name :unit/si-conversion-factor
                     :db/id]}
                   {:transactioner/selected-unit
                    [:unit/name :unit/si-conversion-factor
                     :db/id]}
                   {:transactioner/dimension
                    [:dimension/formula]}]
           id])
       timer (atom {:value 0
                    :enabled? false
                    :running? false})
       edit (atom {:label false
                   :interval false
                   :timestamp false
                   :total false})]
  (fn [& {:keys [id]}]
   (let [{amount :transactioner/amount
          {selected-unit-sicf :unit/si-conversion-factor
           selected-unit-id :db/id} :transactioner/selected-unit
          hidden :transactioner/hidden?
          units :transactioner/units
          type :transactioner/type
          label :transactioner/label
          {formula :dimension/formula} :transactioner/dimension
          {total-unit-sicf :unit/si-conversion-factor
           total-unit-id :db/id} :transactioner/total-unit
          timestamp :transactioner/timestamp} @t
         timer-value (:value @timer)
         timer-running (:running? @timer)
         timer-enabled (:enabled? @timer)
         ts
         (sequence (filter (comp (partial = formula) last))
          @(re-frame/subscribe
            [:q '[:find ?a ?sicf ?t ?f
                  :in $ ?tnr
                  :where
                  [?t :transaction/transactioner ?tnr]
                  [?t :transaction/unit ?u]
                  [?t :transaction/amount ?a]
                  [?u :unit/si-conversion-factor ?sicf]
                  [?u :unit/dimension ?d]
                  [?d :dimension/formula ?f]]
                 id]))
         tsc (count ts)
         total
         (reduce
          (fn [r [a sicf]]
              (+ r (* a sicf)))
          0
          ts)

         nt*
         (/ (+ total
               (case type
                :production (* amount
                               selected-unit-sicf)
                :consumption (- (* amount
                                   selected-unit-sicf))))
            total-unit-sicf)
         interval (t/period
                   :hours (quot timer-value 36000)
                   :minutes (mod (quot timer-value 600) 60)
                   :seconds (mod (quot timer-value 10) 60)
                   :millis (rem timer-value 10))]
    (when-not timer-enabled
     (swap! timer assoc :running? false))
    (when timer-running
     (js/setTimeout #(swap! timer update :value inc) 100))
    [:div.panel.panel-primary
     [h-box
      :children [[title
                  :label [h-box
                          :children [(if-not (:label @edit)
                                      [re-com.text/label
                                       :label [:h1 label]
                                       :on-click #(swap! edit assoc :label true)]
                                      [input-text
                                       :model label
                                       :on-change
                                       #(do
                                         (when-not @(re-frame/subscribe
                                                      [:q '[:find ?tnr .
                                                            :in $ ?l
                                                            :where
                                                            [?tnr :transactioner/label ?l]]
                                                       %])
                                          (re-frame/dispatch
                                             [:transact [[:db/add id :transactioner/label %]]]))
                                         (swap! edit assoc :label false))])
                                     (apply str formula)]]
                  :level :level2]
                 (if-not hidden
                  [v-box
                   :children [[re-com.text/label
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
                               :on-click
                               #(do (swap! timer assoc :running? false)
                                 (re-frame/dispatch
                                  [:transact [[:db/add id
                                               :transactioner/type (case type
                                                                    :production :consumption
                                                                    :consumption :production)]]]))]
                              [h-box
                               :children [(if (:total @edit)
                                           [input-text
                                            :model
                                            (str nt*)
                                            :on-change
                                            (fn [nt]
                                             (swap! edit assoc :total false)
                                             (let [nt
                                                   ((fnil identity
                                                     total) (some-> ((comp first (partial re-find #"(\-?\d+\.)?\d+([eE][-+]?\d+)?")) nt)
                                                                    js/Number
                                                                    (* total-unit-sicf)))]
                                              (re-frame/dispatch
                                               [:transact
                                                (let [[diff ntype]
                                                      ((juxt abs #(cond (neg? %)
                                                                        :consumption
                                                                        (zero? %)
                                                                        nil
                                                                        (pos? %)
                                                                        :production))
                                                       (- nt total))]
                                                 (if (pos? diff)
                                                  [{:transactioner/amount (/ diff
                                                                             selected-unit-sicf)
                                                    :db/id id :transactioner/timestamp (t/now)
                                                    :transactioner/type ntype}]
                                                  [[:db/add id :transactioner/amount 0]
                                                   (when timestamp
                                                    [:db/retract id :transactioner/timestamp timestamp])]))])))]
                                           [re-com.text/label
                                            :label (if (and (> 1e21 (abs nt*))
                                                            (>= (abs nt*) 1e-6))
                                                    (cljs.pprint/cl-format nil "~:d" nt*)
                                                    nt*)
                                            :on-click
                                            #(do
                                              (swap! timer assoc :running? false)
                                              (swap! edit assoc :total true))])
                                          [single-dropdown
                                           :filter-box? true
                                           :choices (sort-by :unit/si-conversion-factor units)
                                           :model total-unit-id
                                           :id-fn #(:db/id %)
                                           :label-fn #(:unit/name %)
                                           :group-fn #(str (:unit/si-conversion-factor %))
                                           :on-change #(re-frame/dispatch
                                                        [:transact [[:db/add id
                                                                     :transactioner/total-unit %]]])]]]]]
                  [h-box
                   :children [[re-com.text/label
                                :label (if (and (> 1e21 (abs nt*))
                                                (>= (abs nt*) 1e-6))
                                        (cljs.pprint/cl-format nil "~:d" nt*)
                                        nt*)
                                :on-click
                                #(do
                                  (swap! timer assoc :running? false)
                                  (swap! edit assoc :total true))]
                              [single-dropdown
                               :filter-box? true
                               :choices (sort-by :unit/si-conversion-factor units)
                               :model total-unit-id
                               :id-fn #(:db/id %)
                               :label-fn #(:unit/name %)
                               :group-fn #(str (:unit/si-conversion-factor %))
                               :on-change #(re-frame/dispatch
                                            [:transact [[:db/add id
                                                         :transactioner/total-unit %]]])]]])
                 [hyperlink
                  :label [:span {:aria-hidden false}]
                  :class "glyphicon glyphicon-remove"
                  :on-click #((re-frame/dispatch
                               [:transact
                                [(cond
                                  hidden
                                  [:db/add id :transactioner/hidden? false]
                                  (zero? tsc)
                                  [:db.fn/retractEntity id]
                                  :else
                                  [:db/add id :transactioner/hidden? true])]]))]]
      :justify :between
      :class "panel-heading"]
     [v-box
      :children (if hidden
                 []
                 [[h-box
                   :children [[checkbox
                               :model timer-enabled
                               :on-change
                               (fn []
                                   (swap! timer update :enabled? not)
                                   (when timestamp
                                    (re-frame/dispatch
                                       [:transact [[:db/retract id
                                                    :transactioner/timestamp timestamp]]])))]
                              (when timer-enabled
                               (if-not timer-running
                                [h-box
                                 :children [[button
                                             :label "Clock-in"
                                             :class (case type
                                                     :consumption "btn btn-danger"
                                                     :production "btn btn-success")
                                             :disabled? timer-running
                                             :on-click
                                             (fn []
                                                 (when timestamp
                                                  (re-frame/dispatch
                                                     [:transact [[:db/retract id
                                                                  :transactioner/timestamp timestamp]]]))
                                                 (reset! timer {:value 0
                                                                :enabled? true
                                                                :running? true})
                                                 (swap! edit assoc :interval false))]
                                            (when (or timestamp
                                                      (> timer-value 0))
                                             [button
                                              :label ""
                                              :class "zmdi zmdi-play btn btn-warning"
                                              :on-click
                                              #(do (when timestamp
                                                    (re-frame/dispatch
                                                       [:transact [[:db/retract id
                                                                    :transactioner/timestamp timestamp]]]))
                                                (swap! edit assoc :interval false)
                                                (swap! timer assoc :running? true))])]]
                                [button
                                 :label "Clock-out"
                                 :class (case type
                                         :consumption "btn btn-success"
                                         :production "btn btn-danger")
                                 :on-click
                                 #(do
                                   (swap! timer assoc :running? false)
                                   (re-frame/dispatch
                                    [:transact [[:db/add id :transactioner/timestamp (t/now)]]]))]))
                              [hyperlink
                               :label [:span]
                               :class "zmdi zmdi-refresh-alt"
                               :on-click
                               #(do (when timestamp
                                     (re-frame/dispatch
                                        [:transact [[:db/retract id
                                                     :transactioner/timestamp timestamp]]]))
                                 (reset! timer {:value 0
                                                :enabled? true
                                                :running? false}))
                               :disabled? (not timer-enabled)]]]
                  [h-box
                   :children [(if-not (:interval @edit)
                               [re-com.text/label
                                :label
                                [:div {:style {:font-family "Digital"
                                               :font-size "2em"}}
                                 (cond
                                  (= 0 (:minutes interval))
                                  (str (apply str (interpose ":" (map #(% interval)
                                                                      [:seconds :millis])))
                                       (if (= 1 (:seconds interval))
                                        " second"
                                        " seconds"))
                                  (= 0 (:hours interval))
                                  (let [[m s ms] (map #(% interval)
                                                      [:minutes :seconds :millis])]
                                   (str (apply str (interpose ":" [m (goog.string.format "%02d" s) ms]))
                                        (if (= 1 (:minutes interval))
                                         " minute"
                                         " minutes")))
                                  :default
                                  (let [[h m s ms] (map #(% interval)
                                                        [:hours :minutes :seconds :millis])]
                                   (str (apply
                                         str (interpose ":" [h (goog.string.format "%02d" m) (goog.string.format "%02d" s) ms]))
                                        (if (= 1 (:hours interval))
                                         " hour"
                                         " hours"))))]
                                :on-click #(do (swap! edit assoc :interval true)
                                               (swap! timer assoc :running? false))]
                               [input-text
                                :model (cond
                                        (= 0 (:minutes interval))
                                        (str (apply str (interpose ":" (map #(% interval)
                                                                            [:seconds :millis]))))
                                        (= 0 (:hours interval))
                                        (let [[m s ms] (map #(% interval)
                                                            [:minutes :seconds :millis])]
                                         (str (apply str (interpose ":" [m (goog.string.format "%02d" s) ms]))))
                                        :default
                                        (let [[h m s ms] (map #(% interval)
                                                              [:hours :minutes :seconds :millis])]
                                         (str (apply
                                               str (interpose ":" [h (goog.string.format "%02d" m) (goog.string.format "%02d" s)
                                                                   ms])))))
                                :on-change
                                #(do (swap! edit assoc :interval false)
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
                               :size "auto"
                               :class (str "form-group " (if (or timer-enabled
                                                                 (not timestamp))
                                                          "has-error"
                                                          "has-success"))
                               :child [input-text
                                       :model (str amount)
                                       :on-change
                                       (fn [a]
                                        (let [a ((comp
                                                  abs
                                                  js/Number
                                                  first
                                                  (partial re-find #"(\-?\d+\.)?\d+([eE][-+]?\d+)?"))
                                                 a)]
                                         (re-frame/dispatch
                                          [:transact
                                            [[:db/add id :transactioner/amount a]
                                             (if (pos? a)
                                              [:db/add id :transactioner/timestamp (t/now)]
                                              (when timestamp
                                               [:db/retract id :transactioner/timestamp timestamp]))]])))
                                       :width "100%"
                                       :class "form-control"]]
                              [single-dropdown
                               :filter-box? true
                               :choices (sort-by :unit/si-conversion-factor units)
                               :model selected-unit-id
                               :id-fn #(:db/id %)
                               :label-fn #(:unit/name %)
                               :group-fn #(str (:unit/si-conversion-factor %))
                               :on-change
                               #(re-frame/dispatch
                                 [:transact [[:db/add id
                                              :transactioner/selected-unit %]]])]]]
                  [h-box
                   :children
                   (into
                    [(let [disable-submit?
                           (or ((complement pos?) amount)
                               (and timer-enabled
                                    timer-running)
                               (not timestamp))]
                      [button
                       :label "Submit"
                       :class "btn btn-lg btn-primary"
                       :disabled? disable-submit?
                       :on-click
                       #(when-not @(re-frame/subscribe
                                    [:q '[:find ?t .
                                          :in $ ?ts ?tnr
                                          :where
                                          [?t :transaction/timestamp ?ts]
                                          [?t :transaction/transactioner ?tnr]]
                                     timestamp id])
                         (re-frame/dispatch
                          [:transact (let [transaction-id (tempid)]
                                      [(merge {:db/id transaction-id
                                               :transaction/transactioner id
                                               :transaction/amount (case type
                                                                    :production amount
                                                                    :consumption (- amount))
                                               :transaction/unit selected-unit-id
                                               :transaction/timestamp timestamp}
                                              (if (and timer-enabled
                                                       timestamp)
                                               {:transaction/interval timer-value}))
                                       [:db/retract id :transactioner/timestamp timestamp]
                                       [:db/add id :transactioner/amount 0]])]))])]
                    [(when timestamp
                      [h-box
                       :children
                       (let [day (t/day timestamp)
                             month (t/month timestamp)
                             year (t/year timestamp)
                             hour (t/hour timestamp)
                             minute (t/minute timestamp)
                             second (t/second timestamp)
                             time-string (->> [hour minute second]
                                              (sequence (comp (map (partial goog.string.format "%02d"))
                                                              (interpose ":")))
                                              (apply str))]
                        [[datepicker-dropdown
                          :model timestamp
                          :on-change
                          #(let [new-day (t/day %)
                                 new-month (t/month %)
                                 new-year (t/year %)]
                            (re-frame/dispatch
                             [:transact [[:db/add id
                                          :transactioner/timestamp (t/date-time new-year new-month
                                                                                new-day hour
                                                                                minute second)]]]))
                          :selectable-fn #(t/before? % (t/today))]
                         [h-box
                          :children
                          [(if-not (:timestamp @edit)
                            [re-com.text/label
                             :label [:div {:style {:font-family "Digital"}} time-string]
                             :on-click #(swap! edit assoc :timestamp true)
                             :style {:font-size "2em"}]
                            [input-text
                             :model time-string
                             :on-change #(let [[new-hour new-minute new-second] (map int (split % #":"))]

                                          (re-frame/dispatch
                                           [:transact [[:db/add id
                                                        :transactioner/timestamp (t/date-time year month
                                                                                              day new-hour
                                                                                              new-minute new-second)]]])
                                          (swap! edit assoc :timestamp false))
                             :validation-regex #"^([0-9]|[0-1][0-9]|2[0-3])?"])
                           [:i.zmdi.zmdi-time.zmdi-hc-2x]]]])])])]
                  [:p.text-xs-right (str "Recent #" tsc)]])
      :class "panel-body"]]))))


(defn select-dimension [& {:keys [label id]}]
 (let [dimension (re-frame/subscribe
                  [:pull '[:dimension/name :dimension/formula
                           {:unit/_dimension
                            [:unit/name :unit/si-conversion-factor :unit/dimension :unit/si-base?]}]
                   id])
       us (re-frame/subscribe
           [:q '[:find [?u ...]
                 :in $ ?d
                 :where
                 [?u :unit/dimension ?d]]
            id])
       transactioners (re-frame/subscribe
                       [:q '[:find ?tnr ?l
                             :in $ ?d
                             [?tnr :transactioner/dimension ?d]
                             [?tnr :transactioner/label ?l]]
                        id])]
  (fn [& {:keys [label id]}]
   (let [transactioner-count
         (->> @transactioners
              count)
         {f :dimension/formula
          dn :dimension/name} @dimension
         dimension-name
         (or (first dn)
             (str f))
         units
         (map #(deref
                (re-frame/subscribe
                 [:pull '[:db/id :unit/si-conversion-factor :unit/name]
                  %])
                @us))]
    [v-box
     :children [[hyperlink
                 :label [h-box
                         :children [[title
                                     :label dimension-name
                                     :level :level2]
                                    [re-com.text/label
                                     :label (str transactioner-count)
                                     :class "badge"]]]
                 :disabled? ((complement pos?) (count units))
                 :on-click
                 #(when-not
                   (((comp set (partial
                                map
                                (fn [tnr]
                                 (:transactioner/label
                                  (deref
                                   (re-frame/subscribe
                                    [:pull '[:transactioner/label] tnr]))))))
                     @transactioners)
                    dimension-name)
                   (re-frame/dispatch
                    [:transact (let [transactioner-id (tempid)]
                                [{:db/id transactioner-id
                                  :transactioner/label dimension-name
                                  :transactioner/units units
                                  :transactioner/type (rand-nth '[:production :consumption])
                                  :transactioner/amount 0
                                  :transactioner/total-unit (let [ou (filter (comp (partial == 1)
                                                                                   :unit/si-conversion-factor) units)
                                                                  si-base (filter :unit/si-base? ou)]
                                                             (cond
                                                              (seq si-base)
                                                              ((comp :db/id first)
                                                               si-base)
                                                              (seq ou)
                                                              ((comp :db/id rand-nth)
                                                               ou)
                                                              :else ((comp :db/id rand-nth)
                                                                     units)))
                                  :transactioner/selected-unit ((comp :db/id rand-nth)
                                                                units)
                                  :transactioner/dimension id}])]))]
                (for [[tnr l] @transactioners]
                 ^{:key tnr}
                 (let [{l :transactioner/label
                        {total-unit-sicf :unit/si-conversion-factor
                         total-unit-id :db/id} :transactioner/total-unit
                        :as tnr}
                       @(re-frame/subscribe
                         [:pull '[:transactioner/label
                                  {:transactioner/total-unit
                                   [:unit/name :unit/si-conversion-factor]}]
                          tnr])
                       ts
                       @(re-frame/subscribe
                         [:q '[:find ?t ?a ?sicf
                               :in $ ?tnr
                               :where
                               [?t :transaction/transactioner ?tnr]
                               [?t :transaction/amount ?a]
                               [?t :transaction/unit ?u]
                               [?u :unit/si-conversion-factor ?sicf]]
                          tnr])
                       a
                       (transduce
                        (map
                         (fn [[_ ta tsicf]]
                          (* ta tsicf)))
                        +
                        ts)]
                  [h-box
                   :children [l a [single-dropdown
                                   :filter-box? true
                                   :choices (sort-by :unit/si-conversion-factor units)
                                   :model total-unit-id
                                   :id-fn #(:db/id %)
                                   :label-fn #(:unit/name %)
                                   :group-fn #(str (:unit/si-conversion-factor %))
                                   :on-change #(re-frame/dispatch
                                                [:transact [[:db/add id
                                                             :transactioner/total-unit %]]])]
                              (when @(re-frame/subscribe
                                      [:q '[:find ?h .
                                            :in $ ?tnr
                                            :where
                                            [?tnr :transactioner/hidden? ?h]]
                                       tnr])
                               [button
                                :label "show"
                                :on-click
                                #(re-frame/dispatch
                                  [:transact [[:db/add tnr :transactioner/hidden? false]]])])]]))]]))))



#_(->> (re-frame/subscribe
        [:q '[:find ?l ?f
              :where
              [?t :transaction/name ?l]
              [?t :transaction/physical-quantity ?pq]
              [?pq :physical-quantity/unit ?u]
              [?u :unit/dimension ?d]
              [?d :dimension/formula ?f]]])
     deref
     (map (comp set)))


#_((juxt (comp
          (partial
           map
           (fn [[f tns]]
            [f (->> (group-by second tns)
                    (map
                     (fn [[n tns]]
                      [n tns])))]))
          (partial group-by last))
         (comp
          (partial
           map
           (fn [[f tns]]
            [f (->> (group-by last tns)
                    (map
                     (fn [[n tns]]
                      [n tns])))]))
          (partial group-by second)))
   (deref (re-frame/subscribe
           [:q '[:find ?t ?l ?f
                 :where
                 [?t :transaction/name ?l]
                 [?t :transaction/physical-quantity ?pq]
                 [?pq :physical-quantity/unit ?u]
                 [?u :unit/dimension ?d]
                 [?d :dimension/formula ?f]]])))





(defn select-by-dimensions []
 (let [dimension-ids (re-frame/subscribe
                      [:q '[:find [?dids ...]
                            :where
                            [?dids :dimension/formula]]])]
  (fn []
   [v-box
    :children (for [dimension-id @dimension-ids]
               ^{:key dimension-id}
               [select-dimension
                :id dimension-id])])))

(defn transactioners []
 (let [transactioner-ids
       (re-frame/subscribe
        [:q '[:find [?transactioner ...]
              :where
              [?transactioner :transactioner/label]]])]
  (fn []
   [v-box
    :children [(for [id @transactioner-ids]
                ^{:key id}
                [transactioner
                  :id id])]])))

(defn select-by-labels []
 (let [ls
       (deref (re-frame/subscribe
               [:q '[:find [?ls ...]
                     :where
                     [_ :transactioner/label ?ls]]]))]))

(defn home-panel []
 [:div
  [reagent-modals/modal-window]
  [v-box
   :children [[transactioners]
              [md-circle-icon-button
                :md-icon-name "zmdi-plus"
                :on-click (fn []
                           (reagent-modals/modal!
                            [v-box
                             :children [[select-by-labels]
                                        [select-by-dimensions]]]))
                :size :larger
                :emphasise? true
                :class "center-block"]]]
  [:div [:a {:href "#/about"} "go to About Page"]]])

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
