(ns ilcdb-clj.core
  (:gen-class)
  (:require [monger.core :as mg]
            [monger.credentials :as mcr]
            [monger.operators :refer :all]
            [monger.collection :as mc]
            [clojure.string :as string]
            [java-time :as java-time]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.reload :refer [wrap-reload]]
            [compojure.core :refer [defroutes GET]]
            [compojure.route :refer [not-found]]
            [ring.handler.dump :refer [handle-dump]]))

(defn capitalize-words
  "Capitalize every word in a string"
  [s]
  (->> (string/split (str s) #"\b")
       (map string/capitalize)
       string/join))

(defn get-age
  "From a birth date string, compute the current age."
  [utc-date-str]
  (.getYears (java-time/period (java-time/local-date "yyyy-MM-dd'T'HH:mm:ssZ" utc-date-str)
                               (java-time/local-date))))

(defn get-case-completion
  ""
  [case default-completion]
  (:completionDate case default-completion))

                                        ;(defn sort-cases-by-completion
                                        ;  ""
                                        ;  [cases]
                                        ;  (let [now (java-time/local-date)]
                                        ;    (map cases (assoc :completionDate (get-case-completion %) %))
                                        ;           ))

(defn get-attorney
  ""
  [cases]
  "Laurel")

(defn get-client-list-client
  "Returns a Map with the fields (:name :phone :income :num-in-house :age :race :birth-country :city-count-state :file-location :attorney) needed by the client list."
  [client-map]
  {:name (capitalize-words (str (client-map :lastName) ", " (client-map :firstName)))
   :phone (client-map :phone)
   :income (client-map :householdIncomeLevel)
   :num-in-house (client-map :numberInHousehold)
   :age (get-age (client-map :dateOfBirth))
   :race (capitalize-words (client-map :race))
   :birth-country (capitalize-words ((client-map :birthPlace) :country))
   :city-county-state (capitalize-words (str
                                         ((client-map :address) :city)  ", "
                                         ((client-map :address) :county) ", "
                                         ((client-map :address) :state)))
   :file-location (client-map :fileLocation)
   :attorney (get-attorney (client-map :cases))})

(defn -main-ilcdb
  ""
  [& args]
  (let [admin-db   "ilcdb"
        u    "ilcdb"
        p    (.toCharArray "snuggl3s")
        cred (mcr/create u admin-db p)
        host "127.0.0.1"
        conn (mg/connect-with-credentials host cred)
        db   (mg/get-db conn "ilcdb")
        coll "clients"
        clients (mc/find-maps db coll)
        female-clients (mc/find-one-as-map db coll { :gender "female"}) ]
    (println (str "# of clients: " (count clients)))
    (println (str "# of female clients: " (count female-clients)))
    (prn (str "client list client: " (get-client-list-client (first clients))))
                                        ;(println (str "first client: " (first clients)))
                                        ;(println  (str "db: " db))
                                        ;(println  (str "conn: " conn))

    (mg/disconnect conn)))

(defn greet [req]
  {:status 200
   :body "Hello, World!"
   :headers {}})

(defn goodbye [req]
  {:status 200
   :body "Goodbye, Cruel World!"
   :headers {}})

(defn about [req]
  {:status 200
   :body "I'm Steve and I created this webapp."
   :headers {}})

(defn yo [req]
  (let [name (get-in req [:route-params :name])]
    {:status 200
     :body (str "Yo! " name "!")
     :headers {}}))

(def ops {"+" +
          "-" -
          "*" *
          ":" /})

(defn calc [req]
  (let [a (Integer. (get-in req [:route-params :a]))
        b (Integer. (get-in req [:route-params :b]))
        op (get-in req [:route-params :op])
        f (get ops op)]
    (if f
      {:status 200
       :body (str (f a b))
       :headers {}}
      {:status 404
       :body (str "Unknown operator: " op)
       :headers {}})))

(defroutes app
  (GET "/" [] greet)
  (GET "/goodbye" [] goodbye)
  (GET "/about" [] about)
  (GET "/request" [] handle-dump)
  (GET "/yo/:name" [] yo)
  (GET "/calc/:a/:op/:b" [] calc)
  (not-found "Page not found."))

(defn -main [port]
(jetty/run-jetty app                 {:port (Integer. port)}))

(defn -dev-main [port]
(jetty/run-jetty (wrap-reload #'app) {:port (Integer. port)}))
