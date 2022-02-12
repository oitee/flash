(ns flash.handlers
  (:gen-class)
  (:require [flash.db :as db]
            [clojure.data.json :as json]))






(defn insert-users
  [request]
  (let [user-name (:id (:params request))
        q (str "INSERT INTO users (id, name, created_at, updated_at) VALUES 
                ('" (str (java.util.UUID/randomUUID)) "', '" user-name "', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)")]
    (if (empty? (db/sql (str "SELECT * from users WHERE name='" user-name "'")))
     (do (db/sql q)
      (json/write-str {:status true, :message (str "User name inserted:" user-name)}))
     (json/write-str {:status false, :message (str "Username already exists: " user-name)}))))

(defn get-users
  [request]
  (json/write-str (db/sql "SELECT name FROM users")))

