(defproject wealthpulse "0.1.0-SNAPSHOT"
  :description "Web frontend for a ledger file written in Clojure"
  :url "https://github.com/readysetmark/wealth-pulse"
  :license {:name "MIT"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.5.1"]]
  :main ^:skip-aot wealthpulse.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
