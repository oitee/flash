(ns flash.handlers
  "hello@otee.dev"
  (:require
   [buddy.core.codecs :as codecs]
   [buddy.core.kdf :as kdf]
   [buddy.core.nonce :as nonce]
   [clj-time.coerce :as ctcc]
   [clj-time.format :as ctf]
   [flash.db :as db]
   [flash.model :as model])
  (:import
   (java.util UUID)))

(defn format-timestamp
  [time]
  (try
    (int (/ (ctcc/to-long (ctf/parse (ctf/formatter "YYYY-MM-dd HH:mm:ss")
                                     time))
            1000))
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
  [_]
  {:body (db/sql "SELECT name FROM users")})


(defn insert-message
  "Given a new `message-txt` and the `chatroom-id` where it was written,
store it permanently."
  [message-txt chatroom-id user-id]
  (let [new-message-id (UUID/randomUUID)]
    (db/sql (str "INSERT INTO messages (id, contents, user_id, chat_room, created_at, updated_at) "
                 "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)")
            new-message-id
            message-txt
            user-id
            chatroom-id)
    {:body {:status :success, :message-id new-message-id}}))

(defn verify-room-details
  "Given a `chatroom` string and a `user` string, check if such entities
  actually exist. Return them if they do, throw an error otherwise."
  [chatroom user]
  (let [user-id (try (->> user
                         (db/sql "SELECT id from users where name=?")
                         first
                         :users/id)
                     (catch org.postgresql.util.PSQLException _))
        chatroom-id (try (->> chatroom
                             (db/sql "SELECT id FROM chatroom where name=")
                             first
                             :chatroom/id)
                         (catch org.postgresql.util.PSQLException _))]
    (cond
      (nil? user-id)
      {:status :error
       :error :bad-user
       :message (str "User does not exist: " user)}

      (nil? chatroom-id)
      {:status :error
       :error :bad-chatroom-id
       :message (str "Chatroom does not exist: " chatroom)}

      :else
      {:chatroom-id chatroom-id
       :user-id user-id})))

(defn insert-message-handler
  "Takes a `request`, extracts all the params needed to insert a new
message into the provided chatroom."
  [{:keys [form-params]}]
  (let [user-name (get form-params "user")
        message-txt (get form-params "message")
        chatroom (get form-params "chatroom")]
    (cond
      (nil? user-name)
      {:body {:status :error
              :error :bad-user
              :message (str "Username not provided:" user-name)}}
      (nil? chatroom)
      {:body {:status :error
              :error :bad-chatroom-id
              :message (str "Chatroom not provided:" chatroom)}}
      :else
      (let [{:keys [chatroom-id user-id error] :as ret}
            (verify-room-details chatroom user-name)]
        (if error
          (throw (ex-info "Hey, your input is bad" ret))
          (insert-message message-txt chatroom-id user-id))))))


(defn get-messages
  [request]
  (let [user-name (get (:form-params request) "user")
        chatroom (get (:form-params request) "chatroom")
        user-id (:users/id (first (db/sql "SELECT id from users where name=?" user-name)))
        chatroom-id (:chatroom/id (first (db/sql "SELECT id FROM chatroom where name=?" chatroom)))
        from_ts (format-timestamp (get (:form-params request) "from_ts"))]
    (clojure.pprint/pprint (str (str "SELECT m.id as message_id, m.contents, c.name as chatroom, u.name as username, m.created_at "
                                     "from (messages m JOIN users u ON m.user_id=u.id) JOIN chatroom c ON c.id=m.chat_room WHERE m.chat_room=? "
                                     "AND m.created_at > to_timestamp(?) ORDER BY m.created_at ASC LIMIT 50 ") chatroom-id "  " from_ts))
    (if (and user-id chatroom chatroom-id from_ts)
      {:body (db/sql
              (str "SELECT m.id as message_id, m.contents, c.name as chatroom, u.name as username, m.created_at "
                   "from (messages m JOIN users u ON m.user_id=u.id) JOIN chatroom c ON c.id=m.chat_room WHERE m.chat_room=? "
                   "AND m.created_at > to_timestamp(?) ORDER BY m.created_at ASC LIMIT 50 ")
              chatroom-id from_ts)}
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
                  :message "Failed to enter the new user. Try with a different username"}}))
      (let [message (if (<= (count password) 6)
                      "Password should have at least seven characters"
                      (str "Either username or password invalid:" username ", " password))]
        {:status 500
         :body {:status "false"
                :message message}}))))



(format-timestamp "2022-03-09 09:00:11")
(nil? (format-timestamp "2022-03-09 25:00:11"))
