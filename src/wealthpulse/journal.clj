(ns wealthpulse.journal
  (:require [wealthpulse.parser :as parser]
            [wealthpulse.query :as query]))


(defrecord State [journal outstanding-payees])

(defn load-journal
  "Load journal from ledger-file-path."
  [app-state ledger-file-path]
  (let [journal (parser/parse-journal ledger-file-path)
        outstanding-payees (query/outstanding-payees journal)]
    (reset! app-state (State. journal outstanding-payees))))