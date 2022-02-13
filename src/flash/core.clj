(ns flash.core
  (:gen-class)
  (:require [flash.db :as db]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.params :as params]
            [flash.routes :as routes]))


(defn -main
  []
  (db/setup-db "jdbc:postgresql://localhost:5432/chat?user=postgres")
  (jetty/run-jetty (params/wrap-params routes/app) {:port 5000})
  )





