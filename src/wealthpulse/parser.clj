(ns wealthpulse.parser
  (:require [instaparse.core :as insta]))


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
