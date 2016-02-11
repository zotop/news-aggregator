(ns news-aggregator.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [org.httpkit.client :as http]
            [clojure.data.json :as json]
            [ring.middleware.json :as middleware]
            [ring.util.response :refer [response]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(def NY_TIMES_API "http://api.nytimes.com/svc/search/v2/articlesearch.json?sort=newest&api-key=sample-key&q=")

(def GUARDIAN_API "http://content.guardianapis.com/search?order-by=relevance&use-date=last-modified&api-key=test&q=")

(defn parseNYTimesSearchResults [[head & tail] parsedResults]
	(if (not-empty head)
		
		(let [elem {:title (:main (:headline head)) :url (:web_url head)}]
			(parseNYTimesSearchResults tail (conj parsedResults elem))
		)
		
		{:nytimes parsedResults}
	) 
)

(defn parseGuardianSearchResults [[head & tail] parsedResults]
	(if (not-empty head)

		(let [elem {:title (:webTitle head) :url (:webUrl head)}]
			(parseGuardianSearchResults tail (conj parsedResults elem))
		)

		{:guardian parsedResults}
	) 
)

(defn callNewYorkTimesApi 
	[searchTerm]
		(let [response (http/get (str NY_TIMES_API (ring.util.codec/url-encode searchTerm)))]
			(def results (:docs (:response (json/read-str (:body @response) :key-fn keyword))))
			(parseNYTimesSearchResults results [])
		)
)

(defn callGuardianApi 
	[searchTerm]
		(let [response (http/get (str GUARDIAN_API (ring.util.codec/url-encode searchTerm)))]
			(def results (:results (:response (json/read-str (:body @response) :key-fn keyword))))
			(parseGuardianSearchResults results [])
		)
)

(defn callApis [searchTerm]
	(if (not= searchTerm nil) 
	 (response (conj {} (callNewYorkTimesApi searchTerm) (callGuardianApi searchTerm)))
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