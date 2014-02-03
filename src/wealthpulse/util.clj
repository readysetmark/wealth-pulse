(ns wealthpulse.util)

(defn bigdec= [x y]
  "Determine if two bigdecs are within 0.00001 of each other (i.e. equal)."
  (let [diff (- x y)]
    (and (<= diff 0.00001M) (>= diff -0.00001M))))
