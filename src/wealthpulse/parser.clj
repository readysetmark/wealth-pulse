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
		(->Header (:date header-map) (:status header-map) (:code header-map) (:payee header-map) (:note header-map))))


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


(defn transform-entry
  "Transform a parse-tree entry into an Entry record."
  [[_ & values]]
  (let [collect-values
          (fn [coll [key value & rest]]
            (cond
               (= key :account) (merge coll (transform-account value))
               (= key :amount) (identity coll) ; need to handle quantity, quantity * commodity, commodity * quantity (and negative quantities)
               (= key :note) (assoc coll :note (string/trim value))))]
    (println (reduce collect-values {} values))))

(defn transform-entries
	"Transform a list of parse-tree entries into entries."
	[[_ & entries] header]
	(map transform-entry entries))


(defn transform-transaction
	"Transform a parse-tree transaction into entries."
	[[_ header entries]]
	(let [header (transform-header header)]
		(transform-entries entries header)))


(defn transform-transactionZ
	"Transform a parsed transaction into a more usable form."
	[transaction]
	(insta/transform
		{:date (fn [x] [:date (.parse (java.text.SimpleDateFormat. "yyyy/MM/dd") x)])
		 :status (fn [x] [:status (if (= x \!) :uncleared :cleared)])
		 :code (fn [x] [:code (string/trim x)])
		 :payee (fn [x] [:payee (string/trim x)])
		 :note (fn [x] [:note (string/trim x)])
		 :quantity (fn [x] [:quantity (bigdec (string/replace x "," ""))])
		 :transaction (fn [header entries] (map (fn [entry] (concat entry [header])) (rest entries)))}
		transaction))


(def parse-transaction
	"Parses a string containing a single transaction."
	(insta/parser "src/wealthpulse/transaction.grammar"))


(defn parse-journal
	"Parse a ledger journal file."
	[filepath]
	(let [file-contents (slurp filepath)
		  extract-transaction-regex #"(?m)^\d.+(?:\r\n|\r|\n)(?:[ \t]+.+(?:\r\n|\r|\n))+"
		  transaction-strings (re-seq extract-transaction-regex file-contents)]
		(map parse-transaction transaction-strings)))
