(ns news-aggregator.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.json :as middleware]
            [ring.util.response :refer [response]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(defroutes app-routes
  (GET "/" [] (ring.util.response/content-type
                     (ring.util.response/resource-response "index.html" {:root "public"}) "text/html"))
  (GET  "/api" [] (response [{:name "Widget 1"} {:name "Widget 2"}]))
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      (middleware/wrap-json-response)
  )
)

