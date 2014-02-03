
wealthpulse.core=> (clojure.set/project (clojure.set/select #(.contains (clojure.string/lower-case (:account %)) "assets") (set fjou
rnal)) [:account :amount :commodity])