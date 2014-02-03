(ns wealthpulse.query
  (:require [clojure.string :as string]))


(defn select-entries-with
  "Returns vector of entries where account contains one of the strings in accounts-with."
  [journal accounts-with]
  (let [contains-one-of? (fn [entry] #(if (.contains (string/lower-case (:account entry)) (string/lower-case %)) entry))]
    (filter #(some (contains-one-of? %) accounts-with) journal)))




(defn balance
  "Returns a tuple of [account-balances, total-balance] that match the filters, where
  account-balances is a vector of [account, amount] tuples.
  Valid filters are:
    :accounts-with :: select entries containing any of these strings.
    :exclude-accounts-with :: filter out entries containing any of these strings.
    :period-start :: select entries on or after this date.
    :period-end :: select entries up to and including this date."
  [journal filters]
  ;filter entries
  ;sum entries by account
  ;discard accounts with 0 balance
  ;filter parent accounts where amount is the same as single child
  ;calculate total balance
  ;return amounts & total balance
  )


;wealthpulse.core=> (clojure.set/project (clojure.set/select #(.contains (clojure.string/lower-case (:account %)) "assets") (set fjournal)) [:account :amount :commodity])
