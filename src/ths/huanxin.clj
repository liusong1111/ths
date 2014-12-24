(ns ths.huanxin
  (:require [org.httpkit.client :as http]
            [ths.utils :refer :all]
            [cheshire.core :as json]
            [clj-time.core :as t]
            [taoensso.timbre :as logger]))

(declare token-info fetch-token)

(defn get-token []
  (let [ti @token-info
        {:keys [token expire-at fetcher]} ti]
    (if (or (not token) (not expire-at) (t/after? (t/now) expire-at))
      (do
        @(:fetcher ti)
        ;(get-token)
        (recur)
        )
      token
      )
    )
  )

(defn get-token-callback [{:keys [status headers body error opts] :as response}]
  (if error
    (do
      (logger/error "error while fetching token")
      (swap! token-info assoc :fetcher (fetch-token))
      )

    (let [data (json/parse-string body true)
          {:keys [access_token expires_in]} data]
      (swap! token-info assoc
             :fetcher nil
             :expire-at (t/plus (t/now) (t/seconds expires_in) (t/minutes -10))
             :token access_token)
      )
    ))

(defn fetch-token []
  (http/post (str huanxin-url-root "/token")
             {
              :headers {"Content-Type" "application/json"
                        "Accept"       "application/json"}
              :body    (json/generate-string {
                                              :grant_type    "client_credentials"
                                              :client_id     "YXA6zpmeoHX8EeS5LFOLSMeZrA"
                                              :client_secret "YXA62xaG_k1OsmSdGYtjIKE3XbO0ahw"
                                              })
              }
             get-token-callback
             )
  )

(def token-info (atom {:fetcher (fetch-token)}))

(defn -main []
  (println "preparing...")
  (println (get-token))
  ;(println)
  ;(println (t/after? (t/now) (t/plus (t/now) (t/seconds 3) (t/minutes -10))))
  (println "ok!"))