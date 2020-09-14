(defproject wealthpulse "0.5.0-SNAPSHOT"
  :description "Web frontend for a ledger file written in Clojure"
  :url "https://github.com/readysetmark/wealth-pulse"
  :license {:name "MIT"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [instaparse "1.4.10"]
                 [compojure "1.6.2"]
                 [ring/ring-jetty-adapter "1.8.1"]
                 [ring/ring-json "0.5.0"]]
  :plugins [[lein-ring "0.12.5"]]
  :ring {:init wealthpulse.web/init
         :handler wealthpulse.web/handler}
  :main ^:skip-aot wealthpulse.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                                  [ring-mock "0.1.5"]]}})
