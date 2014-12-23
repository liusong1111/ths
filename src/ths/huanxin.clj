(ns ths.huanxin
  (:require [org.httpkit.client :as http]
            [ths.utils :refer :all]
            [cheshire.core :as json]
            [clj-time.core :as t]
            [taoensso.timbre :as logger]))

(def token-info (atom nil))

(defn get-token []
  (let [ti @token-info
        {:keys [token expire-at status]} ti
        ]
    (if (> expire-at t/now)
      token
      (fetch-token)
      )
    )
  (let [{:keys [timestamp token]} (@-cached-token)]
    (if (or (not timestamp) (<= (t/plus timestamp (t/days 5)) (t/now))))
    (get-token))
  )

(defn get-token-callback [{:keys [status headers body error opts] :as response}]
  (if error
    (swap token-info :status :done)
    (let [data (json/parse-string body true)
          {:keys [access_token expires_in]}]
      (swap token-info :status :done
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
             (fn [{:keys [status headers body error] :as response}]
               (println body)
               ))
  )

(defn -main []
  (get-token))