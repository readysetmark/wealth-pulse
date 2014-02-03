(ns wealthpulse.query
  (:require [clojure.string :as string]))


(defn select-entries-with
  "Returns vector of entries where account contains one of the strings in accounts-with."
  [entries tokens]
  (let [entry-if-account-contains (fn [entry] #(if (.contains (string/lower-case (:account entry)) (string/lower-case %)) entry))]
    (filter #(some (entry-if-account-contains %) tokens) entries)))


(defn exclude-entries-with
  "Returns vector of entries where entries with accounts containing one of the strings in exclude-accounts-with have been removed."
  [entries tokens]
  (let [entry-if-not-account-contains (fn [entry] #(if (not (.contains (string/lower-case (:account entry)) (string/lower-case %))) entry))]
    (filter #(some (entry-if-not-account-contains %) tokens) entries)))


(defn select-entries-within-period
  "Returns vector of entries where entry date is within period-start and period-end."
  [entries & {:keys [period-start period-end]}]
  (let [satisfies-period-start? #(or (nil? period-start) (>= (.compareTo (get-in % [:header :date]) period-start) 0))
        satisfies-period-end? #(or (nil? period-end) (<= (.compareTo (get-in % [:header :date]) period-end) 0))]
    (filter #(and (satisfies-period-start? %) (satisfies-period-end? %)) entries)))


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
