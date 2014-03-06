(ns wealthpulse.journal
  (:require [wealthpulse.parser :as parser]
            [wealthpulse.query :as query])
  (:import [java.io File]))


(defrecord State [journal outstanding-payees last-modified])

(defn load-journal
  "Load journal from ledger-file-path."
  [app-state ledger-file-path]
  (let [thread-id (.getId (Thread/currentThread))
        file (File. ledger-file-path)
        last-modified (.lastModified file)
        journal (parser/parse-journal ledger-file-path)
        outstanding-payees (query/outstanding-payees journal)]
    (do
      (println (str "(" thread-id ") Loaded journal from: " ledger-file-path))
      (reset! app-state (State. journal outstanding-payees last-modified)))))


(defn watch-and-load
  "Load journal from ledger-file-path and then set up a watch on the file.
  Keep reloading the file when it is modified."
  [app-state ledger-file-path]
  (letfn [(reload-when-modified
          []
          (let [file (File. ledger-file-path)]
            (while true
                   (do
                     (if (> (.lastModified file) (:last-modified @app-state))
                         (load-journal app-state ledger-file-path))
                     (Thread/sleep 5000)))))]
    (do
      (load-journal app-state ledger-file-path)
      (.start (Thread. reload-when-modified))
      app-state)))
