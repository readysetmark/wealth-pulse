(ns wealthpulse.journal
  (:require [wealthpulse.parser :as parser]
            [wealthpulse.query :as query]))


(defrecord Journal [entries outstanding-payees])

; Global state
; TODO: Hmm... revisit this decision. D:
(def ^:dynamic *journal* (atom (Journal. nil nil)))

(defn load-journal
  "Load journal from ledger-file-path."
  [ledger-file-path]
  (let [entries (parser/parse-journal ledger-file-path)
        outstanding-payees (query/outstanding-payees entries)]
    (reset! *journal* (Journal. entries outstanding-payees))))