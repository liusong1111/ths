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
(defn http-request [method uri & [params callback]]
  (let [reply (promise)
        _ (http/request
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
              (let [result (if (and (not error) body)
                             (json/parse-string body true)
                             )
                    response (assoc response :result result)]
                (logger/info response)
                (if callback (callback response))
                (deliver reply response))
              )
            )
        ]
    reply
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

; 建群组/发话题
(defn groups-create [groupname owner]
  (http-request :post "/chatgroups" {
                                     :groupname groupname
                                     :desc      ""
                                     :public    true
                                     :approval  false
                                     :owner     owner
                                     :members   [owner]
                                     }))
; 向群组加成员
(defn groups-add-member [huanxin-group-id huanxin-username]
  (http-request :post (str "/chatgroups/" huanxin-group-id "/users/" huanxin-username)))

; 发送文本消息
(defn messages-post-text [target_type target msg from image-url user-id]
  (http-request :post "/messages" {
                                   ;users 给用户发消息, chatgroups 给群发消息
                                   :target_type target_type

                                   ;注意这里需要用数组,数组长度建议不大于20, 即使只有一个用户,
                                   ;也要用数组 ['u1'], 给用户发送时数组元素是用户名,给群组发送时
                                   ;数组元素是groupid
                                   :target      target

                                   :msg         {
                                                 :type "txt"
                                                 :msg  msg
                                                 }

                                   ;表示这个消息是谁发出来的, 可以没有这个属性, 那么就会显示是admin, 如果有的话, 则会显示是这个用户发出的
                                   :from        from

                                   ;扩展属性, 由app自己定义.可以没有这个字段，但是如果有，值不能是“ext:null“这种形式，否则出错
                                   :ext {
                                         :imageurl (str image-url)
                                         :userid (str user-id)
                                         }
                                   }))

(defn -main []
  (println "fetching...")
  (println (get-token))
  ;(println @(users-create "liusong" "liusong" nil))
  ;(println @(users-update-password "liusong" "liusong1"))
  ;(println @(users-update-password "4c36aba13d16f79ed79a29eec4bfbde0163e2d4f" "liusong"))
  (println "ok!"))