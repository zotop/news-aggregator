(ns news-aggregator.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [org.httpkit.client :as http]
            [clojure.data.json :as json]
            [ring.middleware.json :as middleware]
            [ring.util.response :refer [response]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(defn callNewYorkTimesApi 
	[searchTerm]
		(let [resp1 (http/get (str "http://api.nytimes.com/svc/search/v2/articlesearch.json?sort=newest&api-key=sample-key&q=" searchTerm))]
			(def results (:docs (:response (json/read-str (:body @resp1) :key-fn keyword))))
			(parseNYTimesResult results [])
		)
)

(defn parseNYTimesResult [[head & tail] finalResult]
	(if (not-empty head)
		
		(let [elem {:title (:main (:headline head)) :url (:web_url head)}]
			(parseNYTimesResult tail (conj finalResult elem))
		)
		
		{:nytimes finalResult}
	) 
)

(defn parseGuardianResult [[head & tail] finalResult]
	(if (not-empty head)

		(let [elem {:title (:webTitle head) :url (:webUrl head)}]
			(parseGuardianResult tail (conj finalResult elem))
		)

		{:guardian finalResult}
	) 
)

(defn callGuardianApi 
	[searchTerm]
		(let [resp1 (http/get (str "http://content.guardianapis.com/search?order-by=newest&api-key=test&q=" searchTerm))]
			(def results (:results (:response (json/read-str (:body @resp1) :key-fn keyword))))
			(parseGuardianResult results [])
		)
)

(defn callApis [searchTerm]
	(if (not= searchTerm nil) 
	 (response (conj [] (callNewYorkTimesApi searchTerm) (callGuardianApi searchTerm)))
	 (str "Query Parameter 'searchTerm' needs to be specified")
	)
)

(defroutes app-routes
  (GET "/" [] (ring.util.response/content-type
                     (ring.util.response/resource-response "index.html" {:root "public"}) "text/html"))

  (GET "/api" {params :params} [] (callApis (get params :searchTerm)))

  (route/not-found "Not Found"))

(def app
  (-> app-routes
      (middleware/wrap-json-response)
      (wrap-defaults site-defaults)
  )
)

