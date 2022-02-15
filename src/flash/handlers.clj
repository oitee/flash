(ns flash.handlers
  (:gen-class)
  (:require [flash.db :as db]
            [flash.model :as model]
            [clj-time.core :as ctc]
            [clj-time.format :as ctf]
            [clj-time.coerce :as ctcc]
            [buddy.core.codecs :as codecs]
            [buddy.core.kdf :as kdf]
            [buddy.core.nonce :as nonce]))



(defn format-timestamp
  [time]
  (try
    (ctcc/to-long (ctf/parse (ctf/formatter "YYYY-MM-dd HH:mm:ss")
                             time))
    (catch Exception e nil)))


(defn generate-hash
  [password salt]
  (let [pbkdf2 (kdf/engine {:key password
                            :salt salt
                            :alg :pbkdf2
                            :digest :sha256
                            :iterations 100})]
    (-> (kdf/get-bytes pbkdf2 8)
        (codecs/bytes->hex))))


(defn hash-password
  [password]
  (let [salt (nonce/random-bytes 8)
        hex-salt (codecs/bytes->hex salt)
        hashed-password (generate-hash password salt)]
    (str hashed-password ":" hex-salt)))


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
      {:body {:status "false", :message (str "Either username or chatroom does not exist:" user-name ", " chatroom)}})))


(defn get-messages
  [request]
  (let [user-name (get (:form-params request) "user")
        chatroom (get (:form-params request) "chatroom")
        user-id (:users/id (first (db/sql (str "SELECT id from users where name='" user-name "'"))))
        chatroom-id (:chatroom/id (first (db/sql (str "SELECT id FROM chatroom where name='" chatroom "'"))))
        from_ts (format-timestamp (get (:form-params request) "from_ts"))]
    (if (and user-id chatroom-id chatroom-id from_ts)
      {:body (db/sql
              (str "WITH t AS (SELECT m.id as message_id, m.contents, c.name as chatroom, u.name as username, m.created_at  from (messages m JOIN users u ON m.user_id=u.id) JOIN chatroom c ON c.id=m.chat_room WHERE m.chat_room='" chatroom-id "' AND m.created_at < to_timestamp('" from_ts "') ORDER BY m.created_at DESC LIMIT 50) SELECT * FROM t ORDER BY created_at ASC"))}
      {:body {:status false, :message (str "Either username, chatroom or timestamp is invalid" user-name ", " chatroom ", " from_ts)}})))


(defn sign-up
  [request]
  (let [username (get (:form-params request) "username")
        password (get (:form-params request) "password")]
    (if (and username password (> (count password) 6))
      (let [hashed-password (hash-password password)
            user-id (model/insert-user username hashed-password)]
        (if user-id
          {:body {:status "true"
                  :user-id user-id}}
          {:status 400
           :body {:status "false"
                  :message "Failed to enter the new user. Try with a different username"}})
        )
      (let [message (if (<= (count password) 6)
                      "Password should have at least seven characters"
                      (str "Either username or password invalid:" username ", " password))]
        {:status 500
         :body {:status "false"
                :message message}}))))



(format-timestamp "2022-03-09 09:00:11")
(nil? (format-timestamp "2022-03-09 25:00:11"))





