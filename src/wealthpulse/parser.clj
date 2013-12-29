(ns wealthpulse.parser
	(:require [instaparse.core :as insta]
		[clojure.string :as string]))


; Should use java.util.Date instead of java.text.SimpleDateFormat?

(defn transform-transaction
	"Transform a parsed transaction into a more usable form."
	[transaction]
	(insta/transform 
		{:date (fn [x] [:date (.parse (java.text.SimpleDateFormat. "yyyy/MM/dd") x)])
		 :status (fn [x] [:status (if (= x \!) :uncleared :cleared)])
		 :code (fn [x] [:code (string/trim x)])
		 :payee (fn [x] [:payee (string/trim x)])
		 :note (fn [x] [:note (string/trim x)])
		 :quantity (fn [x] [:quantity (bigdec (string/replace x "," ""))])}
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

