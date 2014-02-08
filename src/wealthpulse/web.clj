(ns wealthpulse.web
  (:use compojure.core)
  (:require [clojure.string :as string]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.json :as json]
            [ring.util.response :as response]
            [wealthpulse.parser :as parser]
            [wealthpulse.query :as query])
  (:import [java.text NumberFormat]))


; TODO: this is here temporarily and should be moved somewhere more appropriate
(defn annotate-balances
  [[balances total]]
  (let [padding-left-base 8
        indent-padding 20
        sorted-balances (sort-by first balances)
        get-account-display (fn [account]
                              (let [[parentage indent] (reduce (fn [[parentage indent] [other-account _]]
                                                               (if (and (.startsWith account other-account)
                                                                        (not= account other-account)
                                                                        (= (.charAt account (.length other-account)) \:))
                                                                   [other-account (inc indent)]
                                                                   [parentage indent]))
                                                             ["" 0]
                                                             sorted-balances)
                                    account-display (if (not (.isEmpty parentage))
                                                        (.substring account (inc (.length parentage)))
                                                        account)]
                                [account-display indent]))
        format-balance #(.format (NumberFormat/getCurrencyInstance) %)
        annotated-balances (vec (map (fn [[account balance]]
                                       (let [[account-display indent] (get-account-display account)
                                             padding (+ padding-left-base (* indent indent-padding))]
                                         {:key account
                                          :account account-display
                                          :balance (format-balance balance)
                                          :balanceClass (string/lower-case (first (string/split account #":")))
                                          :accountStyle {:padding-left (str padding "px;")}}))
                                     sorted-balances))]
    (conj annotated-balances {:key "Total"
                              :account ""
                              :balance (format-balance total)
                              :rowClass "grand_total"})))



(defn api-routes
  "Define API routes."
  [journal]
  (routes
    (GET "/nav" [] (response/response
                      {:reports [{:title "Balance Sheet" :url "#/balance?parameters=assets liabilities :exclude units :title Balance Sheet"}
                                 {:title "Net Worth" :url "#/networth"}
                                 {:title "Income Statement - Current Month" :url "#/balance?parameters=income expenses :period this month :title Income Statement"}
                                 {:title "Income Statement - Previous Month" :url "#/balance?parameters=income expenses :period last month :title Income Statement"}]
                       :payees []}))
    (GET "/balance" [] (response/response
                          {:title "Balance Sheet"
                           :subtitle "As of today"
                           :balances (annotate-balances (query/balance journal {:accounts-with ["assets" "liabilities"] :exclude-accounts-with ["units"]}))}))
    (GET "/networth" [] "...")))


(defn app-routes
  "Define application routes."
	[journal]
	(routes
    (context "/api" [] (-> (handler/api (api-routes journal))
                           (json/wrap-json-body)
                           (json/wrap-json-response)))
		(handler/site
      (routes
        (GET "/" [] (response/resource-response "index.html" {:root "public"}))
        (route/resources "/")
        (route/not-found "Not Found")))))


(def handler
  (let [ledger-file (.get (System/getenv) "LEDGER_FILE")]
    (app-routes (parser/parse-journal ledger-file))))
