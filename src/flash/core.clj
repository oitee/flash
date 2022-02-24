(ns flash.core
  (:gen-class)
  (:require [flash.db :as db]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.params :as params]
            [flash.routes :as routes]
            [clojure.data.json :as json]))

(defn wrap-json
  "Return the response as a JSON body."
  [handler]
  (fn [request]
    (let [response (handler request)]
      (assoc response :body (json/write-str (:body response))))))

(defn wrap-exceptions
  "If the `handler` throws an exception, catch it and return a generic
500 error message."
  [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception e
        (println e)
        {:status 500
         :body {:status :error
                :error :middleware
                :message "Caught a problem in wrap-exceptions. Look at the terminal to see what the issue is."}}))))

(def app
  (-> #'routes/app
      params/wrap-params
      wrap-exceptions
      wrap-json))

(defn -main
  []
  (db/setup-db "jdbc:postgresql://localhost:5432/chat?user=postgres")
  (jetty/run-jetty #'app {:port 5000 :join? false}))