
(ns flash.db
  (:require [next.jdbc :as jdbc]))

(def db nil)

(defn setup-db
  [jdbc-url]
  (let [ds (jdbc/get-datasource {:jdbcUrl jdbc-url})]
    (alter-var-root #'db (fn [x] ds))
    db))

(defn sql
  [& query]
  (jdbc/execute! db query))

