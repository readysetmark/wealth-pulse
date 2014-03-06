(ns wealthpulse.core
  (:require [wealthpulse.web :as web]
            [ring.adapter.jetty :as jetty])
  (:gen-class))

(defn -main
  "Run jetty using the wealthpulse web handler."
  [& args]
  (jetty/run-jetty web/handler {:host "127.0.0.1"
                                :port 8080}))
