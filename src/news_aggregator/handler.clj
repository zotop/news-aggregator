(ns news-aggregator.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [org.httpkit.client :as http]
            [clojure.data.json :as json]
            [ring.middleware.json :as middleware]
            [ring.util.response :refer [response]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(defn parseGuardianResult [[head & tail] finalResult]
	(if (not-empty head)
		
		(let [elem {:title (:webTitle head) :url (:webUrl head)}]
			(parseGuardianResult tail (conj finalResult elem))
		)
		
		{:guardian finalResult}
	) 
)

(defn callApi 
	[searchTerm]
	(if (not= searchTerm nil) 
		(do
			(let [resp1 (http/get (str "http://content.guardianapis.com/search?order-by=newest&api-key=test&q=" searchTerm))]
			(def results (:results (:response (json/read-str (:body @resp1) :key-fn keyword))))
			(response  (parseGuardianResult results []))
			)
		)
		(str "Query parameter 'searchTerm' needs to be specified")
	)
	
)

(defroutes app-routes
  (GET "/" [] (ring.util.response/content-type
                     (ring.util.response/resource-response "index.html" {:root "public"}) "text/html"))

  (GET "/api" {params :params} [] (callApi (get params :searchTerm)))

  (route/not-found "Not Found"))

(def app
  (-> app-routes
      (middleware/wrap-json-response)
      (wrap-defaults site-defaults)
  )
)

