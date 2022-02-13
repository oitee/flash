(ns flash.core
  (:gen-class)
  (:require [flash.db :as db]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.params :as params]
            [flash.routes :as routes]
            [clojure.data.json :as json]))

(defn wrap-json
  [handler]
  (fn [request] 
    (clojure.pprint/pprint "wrap-json")
    (let [response (handler request)]
      (assoc response :body (json/write-str (:body response))))))

(defn wrap-exceptions 
  [handler] 
  (fn [request]
    (clojure.pprint/pprint "wrap-exceptions")
    (try
      (let [response (handler request)]
        (clojure.pprint/pprint response)
        response)
      (catch Exception e
        (clojure.pprint/pprint e)
        {:status 500 :body {:status "false" :message "Something went wrong"}}))))

(defn -main
  []
  (db/setup-db "jdbc:postgresql://localhost:5432/chat?user=postgres")
  (jetty/run-jetty (wrap-json (wrap-exceptions (params/wrap-params routes/app))) {:port 5000}))





