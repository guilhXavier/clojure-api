(ns lojinha-back.core
  (:require [org.httpkit.server :as server]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer :all]
            [clojure.pprint :as pp]
            [clojure.string :as str]
            [clojure.data.json :as json])
  (:gen-class))

(defn get-parameter [req param-name] (get (:params req) param-name))

(def people-collection (atom []))

(defn add-person [firstname surname]
  (swap! people-collection conj {:firstname (str/capitalize firstname)
                                 :surname (str/capitalize surname)}))

(defn people-handler [req]
  {:status 200
   :headers {"Content-Type" "text/json"}
   :body    (str (json/write-str @people-collection))})

(defn add-person-handler [req]
  {:status 200
   :headers {"Content-Type" "text/json"}
   :body (->
           (let [get-req-param (partial get-parameter req)]
             (str (json/write-str (add-person (get-req-param :firstname) (get-req-param :surname))))))})

(defn simple-body-page [req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body    "Hello World!"})

(defn request-example [req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (->>
           (pp/pprint req)
           (str "Request Object:" req))})

(defn hello-name [req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (->
           (pp/pprint req)
           (str "Hello " (:name (:params req))))})

(add-person "Functional" "Human")
(add-person "Micky" "Mouse")

(defroutes app-routes
           (GET "/" [] simple-body-page)
           (GET "/request" [] request-example)
           (GET "/hello" [] hello-name)
           (GET "/people" [] people-handler)
           (POST "/people/add" [] add-person-handler)
           (route/not-found "Error, page not found!"))

(defn -main
  "This is our main entry point"
  [& args]
  (let [port (Integer/parseInt (or (System/getenv "PORT") "3000"))]
    (server/run-server (wrap-defaults #'app-routes site-defaults)
                       {:port port})
    (println (str "Running webserver at http:/127.0.0.1:" port "/"))))
