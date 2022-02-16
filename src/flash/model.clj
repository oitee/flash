(ns flash.model
  (:require [flash.db :as db]))

(defn insert-user
  [user password]
  (let [user-id (java.util.UUID/randomUUID)
        select-query (str "SELECT * from users WHERE name=?")
        insert-query "INSERT INTO users (id, name, password, created_at, updated_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)"]
    
    (if (empty? (db/sql select-query user))
      (do (db/sql insert-query user-id user password)
          user-id)
      nil)))



