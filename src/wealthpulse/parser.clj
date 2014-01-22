(ns wealthpulse.parser
	(:require [instaparse.core :as insta]
		[clojure.string :as string]))

; Journal records
(defrecord Header [date status code payee note])

(defn process-header
	"Transform a header from the parse tree into a Header record."
	[[_ & values]]
	(let [collect-values
			(fn [coll [key value]] 
				(cond
					(= key :date) (assoc coll :date (.parse (java.text.SimpleDateFormat. "yyyy/MM/dd") value))
					(= key :status) (assoc coll :status (if (= value \!) :uncleared :cleared))
					(= key :code) (assoc coll :code (string/trim value))
					(= key :payee) (assoc coll :payee (string/trim value))
					(= key :note) (assoc coll :note (string/trim value))))]
		(reduce collect-values {} values)))

(defn process-transaction
	"Transform a parse-tree transaction into entries."
	[[_ header entries]]
	(process-header header))
	;(doall (map println [header entries])))


(defn transform-transaction
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
