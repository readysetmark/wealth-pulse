(ns wealthpulse.parser
	(:require [instaparse.core :as insta]
		[clojure.string :as string]))

; Journal records
(defrecord Header [date status code payee note])
(defrecord Amount [quantity commodity])
(defrecord Entry [header account account-lineage entry-type amount note])


(defn transform-header
	"Transform a header from the parse tree into a Header record."
	[[_ & values]]
	(let [collect-values
			(fn [coll [key value]]
				(cond
					(= key :date) (assoc coll :date (.parse (java.text.SimpleDateFormat. "yyyy/MM/dd") value))
					(= key :status) (assoc coll :status (if (= value \!) :uncleared :cleared))
					(= key :code) (assoc coll :code (string/trim value))
					(= key :payee) (assoc coll :payee (string/trim value))
					(= key :note) (assoc coll :note (string/trim value))))
		  header-map (reduce collect-values {} values)]
		(map->Header header-map)))


(defn transform-account
  "Transform a parse-tree account into a map containing :account, :account-lineage, and :entry-type."
  [value]
  (let [first-char (first value)
        entry-type (cond
                      (= first-char \[) :virtual-balanced
                      (= first-char \() :virtual-unbalanced
                      :else :balanced)
        account (string/trim (string/replace value #"[\[\]\(\)]" ""))
        build-account-lineage (fn [coll val]
                                  (if (= (count coll) 0)
                                      (cons val coll)
                                      (cons (clojure.string/join ":" [(first coll) val]) coll)))
        account-lineage (reduce build-account-lineage [] (string/split account #":"))]
    {:account account :entry-type entry-type :account-lineage account-lineage}))


(defn transform-amount
  "Transform a parse-tree amount into an Amount record."
  [first-val second-val]
  (let [quantity (if (= (first first-val) :quantity)
                   (bigdec (string/replace (first (drop 1 first-val)) "," ""))
                   (bigdec (string/replace (first (drop 1 second-val)) "," "")))
        commodity (cond
                     (= (first first-val) :commodity) (string/trim (first (drop 1 first-val)))
                     (not (nil? second-val)) (string/trim (first (drop 1 second-val)))
                     :else nil)]
    (->Amount quantity commodity)))


(defn transform-entry
  "Transform a parse-tree entry into an Entry record."
  [header [_ & values]]
  (let [collect-values
          (fn [coll [key value opt?]]
            (cond
               (= key :account) (merge coll (transform-account value))
               (= key :amount) (assoc coll :amount (transform-amount value opt?))
               (= key :note) (assoc coll :note (string/trim value))))
        entry-map (assoc (reduce collect-values {} values) :header header)]
    (map->Entry entry-map)))


(defn transform-entries
	"Transform a list of parse-tree entries into entries."
	[[_ & entries] header]
	(map (partial transform-entry header) entries))


(defn transform-transaction
	"Transform a parse-tree transaction into entries."
	[[_ header entries]]
	(let [header (transform-header header)]
		(transform-entries entries header)))



(def parse-transaction
	"Parses a string containing a single transaction."
	(insta/parser "src/wealthpulse/transaction.grammar"))


(defn parse-journal
	"Parse a ledger journal file."
	[filepath]
	(let [file-contents (slurp filepath)
		  extract-transaction-regex #"(?m)^\d.+(?:\r\n|\r|\n)(?:[ \t]+.+(?:\r\n|\r|\n))+"
		  transaction-strings (re-seq extract-transaction-regex file-contents)]
		(map (comp transform-transaction parse-transaction) transaction-strings)))
    ;(map parse-transaction transaction-strings)))
    ; will need to flatten the list of entries before returning.
    ; Should I return a set, then we can use select / project?
