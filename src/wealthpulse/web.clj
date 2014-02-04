(ns wealthpulse.web
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.json :as json]
            [ring.util.response :as response]))


(defn api-routes
  "Define API routes."
  [state]
  (routes
    (GET "/nav" [] (response/response {:listening-to state}))
    (GET "/balance" [] "...")
    (GET "/networth" [] "...")))


(defn app-routes
  "Define application routes."
	[state]
	(routes
    (context "/api" [] (-> (handler/api (api-routes state))
                           (json/wrap-json-body)
                           (json/wrap-json-response)))
		(handler/site
      (routes
        ;(GET "/" [] (str "<h1>hello " state "</h1>"))
        ;(GET "/" [] (response/redirect "/index.html"))
        (GET "/" [] (response/resource-response "index.html" {:root "public"}))
        (route/resources "/")
        (route/not-found "Not Found")))))


(def handler
  (app-routes "Pillows"))
