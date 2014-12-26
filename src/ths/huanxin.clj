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
        (let [fetcher (or fetcher (->> (fetch-token) (swap! token-info assoc :fetcher)))]
          @fetcher
          )
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
      (swap! token-info assoc :fetcher (future (Thread/sleep 2000) (fetch-token)))
      )

    (let [data (json/parse-string body true)
          {:keys [access_token expires_in]} data]
      (future (Thread/sleep (- expires_in 1200)) (fetch-token))
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

;;------
; http client接口封装
(defn http-request [method uri & [params]]
  (http/request
    {
     :url     (str huanxin-url-root uri)
     :method  method
     :headers {
               "Content-Type"  "application/json"
               "Authorization" (str "Bearer " (get-token))
               "Accept"        "application/json"
               }
     :body    (if params (json/generate-string params))
     }
    (fn [{:keys [status headers body error opts] :as response}]
      (if error
        (logger/error error)
        (let [data (json/parse-string body true)]
          (logger/info data)))
      )
    )
  )
;;------
; 注册单个用户
(defn users-create [username password nickname]
  (http-request :post "/users"
                {
                 :username username
                 :password password
                 :nickname nickname
                 }))

; 修改用户的密码
(defn users-update-password [username newpassword]
  (http-request :put (str "/users/" username "/password")
                {
                 :newpassword newpassword
                 })
  )

; 删除用户
(defn users-destroy [username]
  (http-request :delete (str "/users/" username)))

(defn -main []
  (println "fetching...")
  (println (get-token))
  ;(println @(users-create "liusong" "liusong" nil))
  ;(println @(users-update-password "liusong" "liusong1"))
  ;(println @(users-update-password "4c36aba13d16f79ed79a29eec4bfbde0163e2d4f" "liusong"))
  (println "ok!"))