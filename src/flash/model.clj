(ns flash.model
  (:require [flash.db :as db]))

(defn show-users 
  []
  (clojure.pprint/pprint (db/sql "SELECT * from users")))