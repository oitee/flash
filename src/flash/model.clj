(ns flash.model
  (:require [flash.db :as db])
  (:import
   (java.util UUID)))

(comment (defn insert-user
           [user password]
           (let [user-id (java.util.UUID/randomUUID)
                 select-query (str "SELECT * from users WHERE name=?")
                 insert-query "INSERT INTO users (id, name, password, created_at, updated_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)"]
             (if (empty? (db/sql select-query user))
               (do (db/sql insert-query user-id user password)
                   user-id)
               nil)))
         )



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
                              (db/sql "SELECT id FROM chatroom where name=?")
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
