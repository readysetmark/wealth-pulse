(ns wealthpulse.parser
	(:require [instaparse.core :as insta]
		        [clojure.string :as string]))


; Move this out eventually
(defn bigdec= [x y]
  (let [diff (- x y)]
    (and (<= diff 0.00001M) (>= diff -0.00001M))))



;
; Journal records
;

(defrecord Header [date status code payee note])
(defrecord Entry [header account account-lineage entry-type amount commodity note])



;
; Verify balances and autobalance
;

(defn verify-virtual-unbalanced
  "Virtual unbalanced transactions must have an amount (since they are unbalanced)"
  [entries]
  (let [vu-entries (filter #(and (= (:entry-type %) :virtual-unbalanced)
                                 (nil? (:amount %)))
                           entries)]
    (if (> (count vu-entries) 0)
        (throw
          (Exception. (with-out-str
                        (print "This entry is a virtual unbalanced entry with no amount:"
                               (first vu-entries)))))
        entries)))

(defn verify-balanced
  "Generic balance checker for balanced entry types. Balanced entries should sum 0 or have only 1 amount missing (which can be auto-balanced).
  Returns a map with :sum, :commodity, and :num-nil.
  NOTE: The possibility of different commodities is completely ignored right now. (TODO)"
  [entry-type entries]
  (let [b-entries (filter #(= (:entry-type %) entry-type) entries)
        commodity (some #(if (not (nil? (:commodity %))) (:commodity %)) b-entries)
        sum (reduce (fn [sum entry]
                      (if (nil? (:amount entry))
                          sum
                          (+ sum (:amount entry))))
                    0
                    b-entries)
        num-nil (count (filter #(nil? (:amount %)) b-entries))]
    {:sum sum :commodity commodity :num-nil num-nil}))


(defn autobalance
  "Balanced entry can be autobalanced as long as there is only 1 amount missing."
  [entry-type entries]
  (let [stats (verify-balanced entry-type entries)]
    (cond (> (:num-nil stats) 1) (throw (Exception. (with-out-str
                                                      (print "This transaction is missing more than one amount for" entry-type ":" entries))))
          (= (:num-nil stats) 1) (map #(if (nil? (:amount %))
                                           (merge % {:amount (* -1 (:sum stats)) :commodity (:commodity stats)})
                                           %)
                                      entries)
          (not (bigdec= (:sum stats) 0M)) (throw (Exception. (with-out-str
                                                      (print "This transaction does not balance for" entry-type ". Remainder is" (:sum stats) ":" entries))))
          :else entries)))


(defn balance-transaction
  "Verifies that transaction balances for all entry types and autobalances transactions if one amount is missing."
  [entries]
  ((comp (partial autobalance :virtual-balanced)
         (partial autobalance :balanced)
         verify-virtual-unbalanced)
     entries))

;
; Parse tree transforms
;

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
  "Transform a parse-tree amount into a map containing :amount and :commodity."
  [first-val second-val]
  (let [quantity (if (= (first first-val) :quantity)
                   (bigdec (string/replace (nth first-val 1) "," ""))
                   (bigdec (string/replace (nth second-val 1) "," "")))
        commodity (cond
                     (= (first first-val) :commodity) (string/trim (nth first-val 1))
                     (not (nil? second-val)) (string/trim (nth second-val 1))
                     :else nil)]
    {:amount quantity :commodity commodity}))


(defn transform-entry
  "Transform a parse-tree entry into an Entry record."
  [header [_ & values]]
  (let [collect-values
          (fn [coll [key value opt?]]
            (cond
               (= key :account) (merge coll (transform-account value))
               (= key :amount) (merge coll (transform-amount value opt?))
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



;
; Parsing
;

(def parse-transaction
	"Parses a string containing a single transaction."
	(insta/parser "src/wealthpulse/transaction.grammar"))


(defn parse-journal
	"Parse a ledger journal file."
	[filepath]
	(let [file-contents (slurp filepath)
		  extract-transaction-regex #"(?m)^\d.+(?:\r\n|\r|\n)(?:[ \t]+.+(?:\r\n|\r|\n))+"
		  transaction-strings (re-seq extract-transaction-regex file-contents)]
		(flatten (map (comp balance-transaction transform-transaction parse-transaction) transaction-strings))))
