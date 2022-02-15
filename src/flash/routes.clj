(ns flash.routes
(:gen-class)
  (:require [compojure.core :as compojure]
           [compojure.route :as compojure.route]
            [flash.handlers :as handler]))

(compojure/defroutes app
  (compojure/POST "/signup" [] handler/sign-up)
  (compojure/GET "/get-users" [] handler/get-users)
  (compojure/POST "/insert-message" [] handler/insert-message)
  (compojure/POST "/last-messages" [] handler/get-messages)
  (compojure.route/not-found "Not found"))





