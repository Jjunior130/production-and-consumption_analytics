(ns production-and-consumption-analytics.core
  (:require [com.rpl.specter :refer :all]
            [com.rpl.specter.macros :refer :all]))

(ns-publics 'com.rpl.specter.macros)

;; (def l-o-p-q
;;   (rv/parse
;;     (clojure.core/slurp "http://www.technologyuk.net/physics/measurement_and_units/physical_quantities_and_si_units.shtml")))

;; (def l-o-p-q-wiki
;;   (rv/parse
;;     (slurp "https://en.wikipedia.org/wiki/List_of_physical_quantities")))

;; (def p-q-u-wiki
;;   (rv/parse
;;     (slurp "https://en.wikipedia.org/wiki/Conversion_of_units")))

;; ;; getting all dimensions, and physical quantities

;; (clojure.pprint/pprint
;;   (group-by :dimension
;;             (remove #(nil? (:dimension %))
;;                     (rv/extract-from l-o-p-q-wiki "table tbody tr"
;;                                      [:dimension :physical-quantity :symbol :si-unit]
;;                                      "td:eq(4)" (comp (fn [d-str]
;;                                                         (case d-str
;;                                                           nil d-str
;;                                                           "1" 1
;;                                                           "Dimensionless" 1
;;                                                           (map #(vector (keyword (first (re-seq #"." %)))
;;                                                                         (let [v (apply str (rest %))]
;;                                                                           (case v
;;                                                                             "" 1
;;                                                                             (clojure.edn/read-string v))))
;;                                                                (clojure.string/split d-str #" "))))
;;                                                       rv/text)
;;                                      "td:eq(0)" rv/text
;;                                      "td:eq(1)" (comp #(case %
;;                                                          "" nil
;;                                                          %)
;;                                                       rv/text)
;;                                      "td:eq(3)" (comp #(case %
;;                                                          "" nil
;;                                                          %)
;;                                                       rv/text)))))

;; ;; getting all units for each physical quantities

;; (clojure.pprint/pprint
;;   (rv/extract-from p-q-u-wiki "table"
;;                    [:physical-quantity]
;;                    "+ h3" rv/text))


