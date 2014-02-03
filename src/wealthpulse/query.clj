(ns wealthpulse.query
  (:require [clojure.string :as string])
  (:use [wealthpulse.util :only [bigdec=]]))


; Filters

(defn account-contains?
  "Returns a function to testing whether an entry account contains a token."
  [entry]
  #(.contains (string/lower-case (:account entry)) (string/lower-case %)))


(defn select-entries-with
  "Returns vector of entries where account contains one of the strings in accounts-with."
  [tokens entries]
  (filter #(some (account-contains? %) tokens) entries))


(defn exclude-entries-with
  "Returns vector of entries where entries with accounts containing one of the strings in exclude-accounts-with have been removed."
  [tokens entries]
  (filter #(not (some (account-contains? %) tokens)) entries))


(defn filter-by-account
  "Returns vector of entries where account contains at least one token from accounts-with and does not contain any tokens from exclude-accounts-with."
  [entries & {:keys [accounts-with exclude-accounts-with]}]
  (cond (and (not (nil? accounts-with)) (not (nil? exclude-accounts-with))) (exclude-entries-with exclude-accounts-with (select-entries-with accounts-with entries))
        (not (nil? accounts-with)) (select-entries-with accounts-with entries)
        (not (nil? exclude-accounts-with)) (exclude-entries-with exclude-accounts-with entries)
        :else entries))


(defn select-entries-within-period
  "Returns vector of entries where entry date is within period-start and period-end."
  [entries & {:keys [period-start period-end]}]
  (let [satisfies-period-start? #(or (nil? period-start) (>= (.compareTo (get-in % [:header :date]) period-start) 0))
        satisfies-period-end? #(or (nil? period-end) (<= (.compareTo (get-in % [:header :date]) period-end) 0))]
    (filter #(and (satisfies-period-start? %) (satisfies-period-end? %)) entries)))


(defn filter-entries
  "Returns a vector of entries filtered by account and period."
  [entries {:keys [accounts-with exclude-accounts-with period-start period-end]}]
  (select-entries-within-period (filter-by-account entries
                                                   :accounts-with accounts-with
                                                   :exclude-accounts-with exclude-accounts-with)
                                :period-start period-start
                                :period-end period-end))


(defn filter-zero-accounts
  "Filters a vector of [account, sum] tuples, removing all tuples where sum = 0."
  [account-sums]
  (filter #(not (bigdec= (nth % 1) 0M)) account-sums))



; Calculations

(defn sum-by-account
  "Returns a vector of [account, sum] tuples for all accounts in the account lineage for each entry in entries."
  [entries]
  (let [add-amount-for-accounts (fn [entry] (fn [coll account] (assoc coll account (+ (get coll account 0M) (:amount entry)))))
        for-each-account-in-lineage-add-amount (fn [coll entry] (reduce (add-amount-for-accounts entry) coll (:account-lineage entry)))
        account-sums (reduce for-each-account-in-lineage-add-amount {} entries)]
    (map #(identity [% (account-sums %)]) (keys account-sums))))





; Queries

(defn balance
  "Returns a tuple of [account-balances, total-balance] that match the filters, where
  account-balances is a vector of [account, amount] tuples.
  Valid filters are:
    :accounts-with :: select entries containing any of these strings.
    :exclude-accounts-with :: filter out entries containing any of these strings.
    :period-start :: select entries on or after this date.
    :period-end :: select entries up to and including this date."
  [journal {:keys [accounts-with exclude-accounts-with period-start period-end] :as filters}]
  (let [filtered-entries (filter-entries journal filters)
        account-sums (-> filtered-entries
                         sum-by-account
                         filter-zero-accounts)]
    account-sums)
  ;filter parent accounts where amount is the same as single child
  ;calculate total balance
  ;return amounts & total balance
  )


;wealthpulse.core=> (clojure.set/project (clojure.set/select #(.contains (clojure.string/lower-case (:account %)) "assets") (set fjournal)) [:account :amount :commodity])
