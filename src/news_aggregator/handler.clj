(ns news-aggregator.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(defroutes app-routes
  (GET "/" [] (ring.util.response/content-type
                     (ring.util.response/resource-response "index.html" {:root "public"}) "text/html"))
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))
