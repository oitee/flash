(ns flash.core
  (:gen-class)
  (:require [flash.db :as db]
            [ring.adapter.jetty :as jetty]
            [flash.routes :as routes]))


(defn -main
  []
  (db/setup-db "jdbc:postgresql://localhost:5432/chat?user=postgres")
  (jetty/run-jetty routes/app {:port 5000})
  )





