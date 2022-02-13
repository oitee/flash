(ns flash.handlers
  (:gen-class)
  (:require [flash.db :as db]
            [clojure.data.json :as json])
  (:import (java.text SimpleDateFormat)))



(defn format-timestamp
  [time]
  (try
    (/ (SimpleDateFormat/.getTime (.parse (SimpleDateFormat. "yyyy-MM-dd HH:mm:ss") time)) 1000)
    (catch Exception e nil)))




(defn insert-user
  [request]
  (let [user-name (:id (:params request))
        new-user-id (str (java.util.UUID/randomUUID))]
    (if (empty? (db/sql (str "SELECT * from users WHERE name='" user-name "'")))
     (do (db/sql (str "INSERT INTO users (id, name, created_at, updated_at) VALUES 
                ('" new-user-id "', '" user-name "', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)"))
         {:body {:status true, :message (str "User name inserted: " user-name), :user-id new-user-id}})
     {:body {:status false, :message (str "Username already exists: " user-name)}})))

(defn get-users
  [request]
  (db/sql "SELECT name FROM users"))



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
          {:body {:status "true", :message-id new-message-id}})
      {:body {:status "false", :message (str "Either username or chatroom does not exist:" user-name ", " chatroom)}})
    )   
  )


(defn get-messages
  [request]
  (let [user-name (get (:form-params request) "user")
        chatroom (get (:form-params request) "chatroom")
        user-id (:users/id (first (db/sql (str "SELECT id from users where name='" user-name "'"))))
        chatroom-id (:chatroom/id (first (db/sql (str "SELECT id FROM chatroom where name='" chatroom "'"))))
        from_ts (format-timestamp (get (:form-params request) "from_ts"))]
    (comment (str "username:" user-name ", chatroom: " chatroom ", user-id:" user-id, ", chatroom-id: " chatroom-id ", from_ts: " from_ts))
    
    (if-not (or (nil? user-id) (nil? chatroom-id) (nil? chatroom-id))
      {:body (db/sql
              (str "WITH t AS (SELECT m.id as message_id, m.contents, c.name as chatroom, u.name as username, m.created_at  from (messages m JOIN users u ON m.user_id=u.id) JOIN chatroom c ON c.id=m.chat_room WHERE m.chat_room='" chatroom-id "' AND m.created_at < to_timestamp('" from_ts "') ORDER BY m.created_at DESC LIMIT 50) SELECT * FROM t ORDER BY created_at ASC"))}
      {:body {:status false, :message (str "Either username, chatroom or timestamp is invalid" user-name ", " chatroom ", " from_ts)}})))


;; SELECT m.id as message_id, m.contents, c.name as chatroom, u.name as username, m.created_at  from 
;; (messages m JOIN users u ON m.user_id=u.id) 
;; JOIN chatroom c ON c.id=m.chat_room
;; WHERE m.chat_room='2db98b67-88db-4902-971a-7128fa12e34b' AND m.created_at > '2022-02-05 12:19:49.060958+00' ORDER BY m.created_at ASC LIMIT 50;





(format-timestamp "2022-03-09 09:00:11")
(nil? (format-timestamp "2022-03-09 25:00:11"))



;; (def date (java.util.Date.))
;; (str date "11/11/2011")

;; (ring.util.time/format-date 11112011)
;; (time/local-date 2015 9 28)