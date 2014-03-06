(ns wealthpulse.journal
  (:require [wealthpulse.parser :as parser]
            [wealthpulse.query :as query])
  (:import [java.io File]))


(defrecord Journal [entries outstanding-payees last-modified])


(defn create-journal-module [] (atom (->Journal nil nil nil)))


(defn get-entries [journal-module] (:entries @journal-module))
(defn get-outstanding-payees [journal-module] (:outstanding-payees @journal-module))
(defn get-last-modified [journal-module] (:last-modified @journal-module))


(defn load-journal
  "Load journal from ledger-file-path and update journal-module."
  [journal-module ledger-file-path]
  (let [thread-id (.getId (Thread/currentThread))
        file (File. ledger-file-path)
        last-modified (.lastModified file)
        journal (parser/parse-journal ledger-file-path)
        outstanding-payees (query/outstanding-payees journal)]
    (do
      (println (str "(" thread-id ") Loaded journal from: " ledger-file-path))
      (reset! journal-module (->Journal journal outstanding-payees last-modified)))))


(defn watch-and-load
  "Load journal from ledger-file-path and then set up a watch on the file.
  Keep reloading the file when it is modified."
  [journal-module ledger-file-path]
  (letfn [(reload-when-modified
          []
          (let [file (File. ledger-file-path)]
            (while true
                   (do
                     (if (> (.lastModified file) (:last-modified @journal-module))
                         (load-journal journal-module ledger-file-path))
                     (Thread/sleep 5000)))))]
    (do
      (load-journal journal-module ledger-file-path)
      (.start (Thread. reload-when-modified))
      journal-module)))
