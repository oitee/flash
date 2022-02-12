(ns flash.core
  (:gen-class)
  (:require [flash.db :as db]
            [ring.adapter.jetty :as jetty]
            [compojure.core :as compojure]
            [compojure.route :as compojure-route]))

(compojure/defroutes app 
                     (compojure/GET "/" [] "Hello World")
(compojure.route/not-found "Not found"))



(defn -main
  []
  (db/setup-db "jdbc:postgresql://localhost:5432/meetings?user=postgres")
  (comment (clojure.pprint/pprint (db/sql "SELECT * FROM entities"))
           )
  (jetty/run-jetty app {:port 5000})
  )






