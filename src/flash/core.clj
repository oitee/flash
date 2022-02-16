(ns flash.core
  (:gen-class)
  (:require [flash.db :as db]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.params :as params]
            [flash.routes :as routes]
            [clojure.data.json :as json]
            ))

(defn wrap-json
  [handler]
  (fn [request]
    (let [response (handler request)]
      (assoc response :body (json/write-str (:body response))))))

(defn wrap-exceptions 
  [handler] 
  (fn [request]
    (try
      (let [response (handler request)]
        response)
      (catch Exception e
        (println e)
        {:status 500 :body {:status "false" :message "Something went wrong"}}))))

(def app 
  (-> #'routes/app
      params/wrap-params
      wrap-exceptions
      wrap-json))

(defn -main
  []
  (db/setup-db "jdbc:postgresql://localhost:5432/chat?user=postgres")
  (jetty/run-jetty #'app {:port 5000 :join? false}))



