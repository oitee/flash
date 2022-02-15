(ns flash.handlers
  (:gen-class)
  (:require [flash.db :as db]
            [clojure.data.json :as json]
            [clj-time.core :as ctc]
            [clj-time.format :as ctf]
            [clj-time.coerce :as ctcc]
            [buddy.core.codecs :as codecs]
            [buddy.core.kdf :as kdf]
            [buddy.core.nonce :as nonce])
  (:import (java.text SimpleDateFormat)))



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
                            :iterations 1})]

    {:status true
     :salt salt
     :salted-hash (-> (kdf/get-bytes pbkdf2 8)
                      (codecs/bytes->hex))}))


(defn hash-password
  [password]
  (let [salt (nonce/random-bytes 8)
        hex-salt (codecs/bytes->hex salt)]
    (assoc (generate-hash password salt) :hex-salt hex-salt)))




;; (defn insert-user
;;   [request]
;;   (let [user-name (:id (:params request))
;;         new-user-id (str (java.util.UUID/randomUUID))]
;;     (if (empty? (db/sql (str "SELECT * from users WHERE name='" user-name "'")))
;;      (do (db/sql (str "INSERT INTO users (id, name, created_at, updated_at) VALUES 
;;                 ('" new-user-id "', '" user-name "', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)"))
;;          {:body {:status true, :message (str "User name inserted: " user-name), :user-id new-user-id}})
;;      {:body {:status false, :message (str "Username already exists: " user-name)}})))

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
  (println "Here1")
  (let [username (get (:form-params request) "username")
        password (get (:form-params request) "password")]
    (if (and username password (> (count password) 6))
      (let [password-hash (hash-password password)]
        {:body {:salted-password (:salted-hash password-hash)
                :salt (:hex-salt password-hash)}})
      (let [message (if (<= (count password) 6)
                      "Password should have at least seven characters"
                      (str "Either username or password invalid:" username ", " password))]
        {:status 500
         :body {:status "false"
                :message message}}))))



(format-timestamp "2022-03-09 09:00:11")
(nil? (format-timestamp "2022-03-09 25:00:11"))

(clojure.pprint/pprint (hash-password "peacock"))


;; (let [salt (:salt (hash-password "peacock"))
;;       hex-salt (codecs/bytes->hex salt)
;;       bytes-salt (codecs/hex->bytes hex-salt)
;;       hex-salt-again (codecs/bytes->hex bytes-salt)]
;;   (= hex-salt hex-salt-again)
;;   )

;; (codecs/hex->bytes )


(def pbkdf2 (kdf/engine {:key "my password"
                         :salt (nonce/random-bytes 8)
                         :alg :pbkdf2
                         :digest :sha256
                         :iterations 1}))

(-> (kdf/get-bytes pbkdf2 8)
    (codecs/bytes->hex))