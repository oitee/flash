(ns flash.model
  (:require [flash.db :as db]))

(defn insert-user
  [user password]
  (let [user-id (java.util.UUID/randomUUID)
        select-query (str "SELECT * from users WHERE name='" user "'")
        insert-query (str "INSERT INTO users (id, name, password, created_at, updated_at) VALUES"
                          "('" user-id "', '" user "', '" password "', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)")]
    
    (if (empty? (db/sql select-query))
      (do (db/sql insert-query)
          user-id)
      nil)))



