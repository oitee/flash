(ns flash.utils
  (:require
   [clj-time.coerce :as ctcc]
   [clj-time.format :as ctf]))

(defn format-timestamp
  [time]
  (try
    (int (/ (ctcc/to-long (ctf/parse (ctf/formatter "YYYY-MM-dd HH:mm:ss")
                                     time))
            1000))
    (catch Exception e nil)))
