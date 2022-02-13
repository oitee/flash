(ns flash.handlers
  (:gen-class)
  (:require [flash.db :as db]
            [clojure.data.json :as json]))






(defn insert-users
  [request]
  (let [user-name (:id (:params request))
        new-user-id (str (java.util.UUID/randomUUID))]
    (if (empty? (db/sql (str "SELECT * from users WHERE name='" user-name "'")))
     (do (db/sql (str "INSERT INTO users (id, name, created_at, updated_at) VALUES 
                ('" new-user-id "', '" user-name "', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)"))
      (json/write-str {:status true, :message (str "User name inserted: " user-name), :user-id new-user-id}))
     (json/write-str {:status false, :message (str "Username already exists: " user-name)}))))

(defn get-users
  [request]
  (json/write-str (db/sql "SELECT name FROM users")))



(defn insert-message
  [request]
  (let [user-name (get (:form-params request) "user")
        message (get (:form-params request) "message")
        chatroom (get (:form-params request) "chatroom")
        user-id (:users/id (first (db/sql (str "SELECT id from users where name='" user-name "'"))))
        chatroom-id (:chatroom/id (first (db/sql (str "SELECT id FROM chatroom where name='" chatroom "'"))))
        new-message-id (str (java.util.UUID/randomUUID))]
    (if-not 
     (or (nil? user-id) (nil? chatroom-id))
    
      (do (db/sql (str "INSERT INTO messages (id, contents, user_id, chat_room, created_at, updated_at) VALUES ('" new-message-id
                                   "', '" message "', '" user-id, "', '" chatroom-id, "', ", "CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)"))
          (json/write-str {:status "true", :message-id new-message-id}))
      (json/write-str {:status "false", :message (str "Either username or chatroom does not exist:" user-name ", " chatroom)}))
    )   
  )
