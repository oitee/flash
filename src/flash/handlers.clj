(ns flash.handlers
  (:require
   [buddy.core.codecs :as codecs]
   [buddy.core.kdf :as kdf]
   [buddy.core.nonce :as nonce]
   [flash.db :as db]
   [flash.model :as model]
   [flash.utils :as utils])
  )



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
  "Calls the appropriate model function(model/get-users) which returns a 
   list of all the current users from the database"
  [_]
  {:body (db/sql "SELECT name FROM users")})


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
            (model/verify-room-details chatroom user-name)]
        (if error
          (throw (ex-info "Hey, your input is bad" ret))
          (model/insert-message message-txt chatroom-id user-id))))))


(defn get-messages
  [request]
  (let [user-name (get (:form-params request) "user")
        chatroom (get (:form-params request) "chatroom")
        user-id (:users/id (first (db/sql "SELECT id from users where name=?" user-name)))
        chatroom-id (:chatroom/id (first (db/sql "SELECT id FROM chatroom where name=?" chatroom)))
        from_ts (utils/format-timestamp (get (:form-params request) "from_ts"))]
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




