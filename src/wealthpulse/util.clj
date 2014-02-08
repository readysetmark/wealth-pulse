(ns wealthpulse.util)

(defn bigdec= [x y]
  "Determine if two bigdecs are within 0.00001 of each other (i.e. equal)."
  (let [diff (- x y)]
    (and (<= diff 0.00001M) (>= diff -0.00001M))))


(defn get-first-of-month
  "Get the first day of the calendar month."
  [calendar]
  (let [date-format (java.text.SimpleDateFormat. "yyyy/MM/dd")
        first-of-month (doto calendar
                         (.set java.util.Calendar/DAY_OF_MONTH 1))]
    (.parse date-format (.format date-format (.getTime first-of-month)))))


(defn get-last-of-month
  "Get the last day of the calendar month."
  [calendar]
  (let [date-format (java.text.SimpleDateFormat. "yyyy/MM/dd")
        first-of-month (doto calendar
                         (.add java.util.Calendar/MONTH 1)
                         (.set java.util.Calendar/DAY_OF_MONTH 1)
                         (.add java.util.Calendar/DAY_OF_MONTH -1))]
    (.parse date-format (.format date-format (.getTime first-of-month)))))
