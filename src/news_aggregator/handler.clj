(ns news-aggregator.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(defroutes app-routes
  (GET "/" [] (ring.util.response/redirect "/index.html"))
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))
