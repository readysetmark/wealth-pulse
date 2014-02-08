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
                                          :accountStyle {:padding-left (str padding "px")}}))
                                     sorted-balances))]
    (conj annotated-balances {:key "Total"
                              :account ""
                              :balance (format-balance total)
                              :rowClass "grand_total"})))


; TODO: I think eventually most of this should be moved to the front end (title, subtitle do not belong here)
(defn handle-balance
  "Handle Balance api request. Possible parameters:
    accountsWith
    excludeAccountsWith
    period
    periodStart
    periodEnd
    title"
  [journal params]
  (let [date-formatter (java.text.SimpleDateFormat. "MMMM d, yyyy")
        period-start (if (contains? params :periodStart) (.parse (java.text.SimpleDateFormat. "yyyy/MM/dd") (:periodStart params)))
        period-end (if (contains? params :periodEnd) (.parse (java.text.SimpleDateFormat. "yyyy/MM/dd") (:periodEnd params)))
        subtitle (cond (and period-start period-end) (str "For the period of " (.format date-formatter period-start) " to " (.format date-formatter period-end))
                       (not (nil? period-start)) (str "Since " (.format date-formatter period-start))
                       (not (nil? period-end)) (str "Up to " (.format date-formatter period-end))
                       :else (str "As of " (.format date-formatter (java.util.Date.))))]
    {:title (get params :title "Balance Sheet")
     :subtitle subtitle
     :balances (annotate-balances (query/balance journal {:accounts-with (string/split (:accountsWith params) #" ")
                                                          :exclude-accounts-with (string/split (:excludeAccountsWith params) #" ")
                                                          :period-start period-start
                                                          :period-end period-end}))}))


(defn api-routes
  "Define API routes."
  [journal]
  (routes
    (GET "/nav" [] (response/response
                      {:reports [{:key "Balance Sheet" :title "Balance Sheet" :url "#/balance?accountsWith=assets+liabilities&excludeAccountsWith=units&title=Balance+Sheet"}
                                 {:key "Net Worth" :title "Net Worth" :url "#/networth"}
                                 {:key "Income Statement - Current Month" :title "Income Statement - Current Month" :url "#/balance?accountsWith=income+expenses&period=this+month&title=Income+Statement"}
                                 {:key "Income Statement - Previous Month" :title "Income Statement - Previous Month" :url "#/balance?accountsWith=income+expenses&period=last+month&title=Income+Statement"}]
                       :payees []}))
    (GET "/balance" [& params] (response/response (handle-balance journal params)))
    (GET "/networth" [& params] (get params :history "No history parameter supplied."))))


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
