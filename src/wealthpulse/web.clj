(ns wealthpulse.web
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.json :as json]
            [ring.util.response :as response]
            [wealthpulse.parser :as parser]
            [wealthpulse.query :as query])
  (:import [java.text NumberFormat]))


; TODO: this is here temporarily and should be moved somewhere more appropriate
(defn annotate-balances
  [[balances total]]
  (let [format-balance #(.format (NumberFormat/getCurrencyInstance) %)
        sorted-balances (vec (map (fn [[account balance]] {:account account :balance (format-balance balance)}) (sort balances)))]
    (conj sorted-balances {:account "Total" :balance (format-balance total)})))



(defn api-routes
  "Define API routes."
  [journal]
  (routes
    (GET "/nav" [] (response/response
                      [{:title "Balance Sheet" :url "#/balance?parameters=assets liabilities :exclude units :title Balance Sheet"}
                       {:title "Net Worth" :url "#/networth"}
                       {:title "Income journalment - Current Month" :url "#/balance?parameters=income expenses :period this month :title Income Statement"}
                       {:title "Income Statement - Previous Month" :url "#/balance?parameters=income expenses :period last month :title Income Statement"}]))
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
