(ns ths.routes
  (:import (org.apache.commons.io FileUtils)
           (java.io File))
  (:require
    ;[compojure.core :refer :all]
    [compojure.route :as route]
    [compojure.api.legacy :refer :all]
    [ring.middleware.defaults :refer [wrap-defaults]]
    ;[ring.middleware.json :refer [wrap-json-params wrap-json-response]]
    [ring.middleware.format-params :refer [wrap-restful-params]]
    [com.postspectacular.rotor :as rotor]
    [clojure.java.io :as io]
    [taoensso.timbre :as timbre]
    [ths.models :as m]
    [ths.huanxin :as h]
    [ths.email :as mailer]
    [cheshire.core :as json]
    [ring.util.response :refer [file-response]]
    [ring.util.http-response :refer [ok]]
    [schema.core :as s]
    [compojure.api.sweet :refer :all]
    [crypto.random :as crypto]
    [ths.jpush :as jpush]
    [cronj.core :as cronj])
  (:use ths.utils)
  )

(defn notify-using-app-handler [t opts]
  (let [users (m/users-not-login-today)]
    (doall
      (for [user users]
        (let [huanxin_username (:huanxin_username user)
              content (str (:username user) "，您已经一天没看同行了，快来看看吧")
              ]
          (jpush/jpush-it huanxin_username content)
          )
        )
      ))
  (timbre/info "notify-using-app")
  )

(def notify-using-app-job
  (cronj/cronj :entries [{
                          :id       "notify-using-app"
                          :handler  notify-using-app-handler
                          :schedule "0 0 19 * * * *"        ;每天19点发一次
                          ;:schedule "0 * * * * * *"
                          ;:schedule "/2 * * * * * *"
                          :opts     {}
                          }]))

(defn init []
  (timbre/set-config! [:timestamp-pattern] "yyyy-MM-dd HH:mm:ss")
  (timbre/set-config!
    [:appenders :rotor]
    {:min-level             :info
     :enabled?              true
     :async?                false                           ; should be always false for rotor
     :max-message-per-msecs nil
     :fn                    rotor/append})
  (timbre/set-config!
    [:shared-appender-config :rotor]
    {:path "production.log" :max-size (* 1024 1024) :backlog 100})

  (cronj/start! notify-using-app-job)
  (timbre/info "TongHang Server is starting"))

(defn destroy []
  (cronj/stop! notify-using-app-job)
  (timbre/info "TongHang Server is shutting down"))

(defn login [email password]
  (let [user (m/auth email password)]
    (if user
      (if (= (:status user) "封号")
        (json-response {
                        :code    "fail"
                        :message "您已被同行管理员封号，如有疑问请联系我们"
                        })
        (do
          (m/record-login (:id user))
          (json-response {
                          :code  "ok"
                          :token (generate-login-token (:id user) email)
                          :user  user
                          })
          )
        )
      (json-response {
                      :code    "fail"
                      :message "用户名或密码不正确"
                      })
      ))
  )

(defn forget_password [email]
  (when-let [user (m/users-by-email email)]
    (when-let [password (rand-str 8)]
      (m/users-update (:id user) {:password password})
      (mailer/send-email-for-forget-password (:username user) password email)
      (json-response {
                      :code "ok"
                      })
      )
    )

  )

;; users
(defn users-index [q label_name topic_id page]
  (json-response (m/users-all q label_name topic_id page)))

(defn users-show [id]
  (json-response (m/users-show id)))

(defn users-create [username password email phone sex birth city image]
  (if-let [conflict-user (m/users-by-email email)]
    (json-response {
                    :code    "error"
                    :message "输入邮箱已存在"
                    })
    (let [huanxin_username (generate-huanxin-username email)
          user (m/users-create username password email phone sex birth city huanxin_username (:filename image))
          user_id (:id user)
          sign-image-path (str image-path "/" (refine-user-id-str user_id))
          _ (when (:tempfile image)
              (FileUtils/forceMkdir (File. sign-image-path))
              (io/copy (:tempfile image) (io/file sign-image-path (:filename image))))]
      (h/users-create huanxin_username password username)
      (json-response user)
      )
    )
  )

(defn users-update [id attrs image]
  (m/users-update id attrs)
  (when (:tempfile image)
    (FileUtils/forceMkdir (File. (str image-path "/" (refine-user-id-str id))))
    (io/copy (:tempfile image) (io/file image-path (refine-user-id-str id) (:filename image)))
    )
  (if-let [password (:password attrs)]
    (h/users-update-password (:huanxin_username (m/users-show id)) password)
    )
  (json-response (m/users-show id))
  )

(defn users-update-labels [id labels]
  (m/users-update-labels id labels)
  (json-response (m/users-show id)))

(defn users-destroy [id]
  (if-let [user (m/users-show id)]
    (h/users-destroy (:huanxin_username user)))
  (json-response (m/users-destroy id)))

;; labels
(defn labels-index [q]
  (json-response (m/labels-index q)))

(defn labels-create [label_name]
  (json-response (m/labels-create label_name)))

(defn labels-destroy [label_name]
  (json-response (m/labels-destroy label_name)))

;; topics
(defn topics-index [current_user_id q label_name user_id page]
  (json-response (m/topics-index current_user_id q label_name user_id page)))

(defn topics-show [id]
  (json-response (m/topics-show id)))

(defn topics-create [current_user_id subject body label_name]
  (let [topic (m/topics-create current_user_id subject body label_name)
        user (m/users-show current_user_id)
        huanxin-username (:huanxin_username user)
        image-url (:image user)
        user-id (:id user)
        {:keys [error result]} @(h/groups-create (str (:subject topic)) huanxin-username)
        huanxin-group-id (if (not error) (get-in result [:data :groupid]))
        _ @(h/messages-post-text "chatgroups" [huanxin-group-id] body huanxin-username image-url user-id)
        _ (if (not error) (m/topics-update-huanxin-group-id (:id topic) huanxin-group-id))
        topic (assoc topic :huanxin_group_id huanxin-group-id)
        ]
    (json-response topic)
    )
  )

;; 不要了
;(defn topics-update [id subject body label_name]
;  (json-response (m/topics-update id subject body label_name)))
;
;(defn topics-destroy [id]
;  (json-response (m/topics-destroy id))
;  )

(defn topic-replies-index [topic_id]
  (json-response (m/topic-replies-index topic_id)))

(defn topic-replies-create [current_user_id topic_id body]
  (let [
        user (m/users-show current_user_id)
        user-id (:id user)
        huanxin_username (:huanxin_username user)
        image-url (:image user)
        reply (m/topic-replies-create current_user_id topic_id body)
        topic (m/topics-show topic_id)
        huanxin_group_id (:huanxin_group_id topic)
        _ @(h/groups-add-member huanxin_group_id huanxin_username)
        _ @(h/messages-post-text "chatgroups" [huanxin_group_id] body huanxin_username image-url user-id)
        ]
    (json-response reply)
    )
  )

;; 不要了
;(defn topic-replies-update [topic_id reply_id body]
;  (json-response (m/topic-replies-update topic_id reply_id body)))
;
;(defn topic-replies-destroy [topic_id reply_id]
;  (json-response (m/topic-replies-destroy topic_id reply_id)))

(defn recommendations [current_user_id page]
  (json-response (m/recommendations current_user_id page))
  )

(defn huanxin-hid2sids [entries]
  (println entries)
  (let [entries (doall (for [entry entries]
                         (if (:is_group entry)
                           (assoc (m/topics-by-huanxin-group-id (:username entry)) :type "topic")
                           (assoc (m/users-by-huanxin-username (:username entry)) :type "user")
                           )))]
    (json-response entries))
  )

;; friends
(defn invitations-index [user_id]
  (json-response (m/invitations-index user_id)))

(defn invitations-create [current_user_id invitee_id reason]
  (let [inviter (m/users-show current_user_id)
        invitee (m/users-show invitee_id)
        inviter-username (:username inviter)
        invitee-huanxin-username (:huanxin_username invitee)]
    (jpush/jpush-it invitee-huanxin-username (str inviter-username "请求加您为好友"))
    (json-response (m/invitations-create current_user_id invitee_id reason))
    )
  )

(defn invitations-agree [current_user_id invitation_id]
  (json-response (m/invitations-agree current_user_id invitation_id)))

(defn invitations-refuse [current_user_id invitation_id]
  (json-response (m/invitations-refuse current_user_id invitation_id)))

(defn friends-destroy [current_user_id friend_id]
  (json-response (m/friends-destroy current_user_id friend_id)))

(defn friends-index [user_id]
  (json-response (m/friends-index user_id)))

;; feedbacks
(defn feedbacks-create [user_id content]
  (json-response (m/feedbacks-create user_id content)))

(defroutes* legacy-app-routes
            (GET "/" [] "Hello World")

            (GET "/download/peer.apk" [] (file-response "peer.apk"))

            (GET "/demo.json" [current_user_email] (json-response {:code "OK" :message (str "welcome:" current_user_email)}))
            (POST "/demo.json" [name] (json-response {:code "OK" :message (str "created:" name)}))
            (PUT "/demo.json" [name] (json-response {:code "OK" :message (str "updated:" name)}))
            (DELETE "/demo.json" [name] (json-response {:code "OK" :message (str "deleted:" name)}))

            ;; login
            (POST "/login.json" [email password] (login email password))
            ;; forget password
            (POST "/forget_password.json" [email] (forget_password email))

            ;; users
            (GET "/users.json" [q label_name topic_id page] (users-index q label_name topic_id (Integer. (or page "1"))))
            (GET "/users/:id.json" [id] (users-show id))
            (POST "/users.json" [username password email phone sex birth city image] (users-create username password email phone sex birth city image))
            (PUT "/users/:id.json" {params :params} (let [id (:id params)
                                                          image (:image params)
                                                          attrs (-> (select-keys params [:username :password :email :phone :sex :birth :city])
                                                                    (assoc :image (:filename image))
                                                                    remove-blank-values
                                                                    )] (users-update id attrs image)))
            (PUT "/users/:id/update_labels.json" [id labels] (users-update-labels id labels))
            (DELETE "/users/:id.json" [id] (users-destroy id))

            (GET "/signs/*" [*]
                 (ring.util.response/response (File. (str image-path "/" *)))
                 )

            ;; labels
            (GET "/labels.json" [q] (labels-index q))
            ; 不要了
            ;(POST "/labels.json" [label_name] (labels-create label_name))
            ;(DELETE "/labels/:label_name.json" [label_name] (labels-destroy label_name))

            ;; topics
            (GET "/topics.json" [current_user_id q label_name user_id page] (topics-index current_user_id q label_name user_id (Integer. (or page "1"))))
            (GET "/topics/:id.json" [id] (topics-show id))
            (POST "/topics.json" [current_user_id subject body label_name] (topics-create current_user_id subject body label_name))
            ;; 不要了
            ;(PUT "/topics/:id.json" [id subject body label_name] (topics-update id subject body label_name))
            ;(DELETE "/topics/:id.json" [id] (topics-destroy id))

            ;; replies
            (GET "/topics/:topic_id/replies.json" [topic_id] (topic-replies-index topic_id))
            (POST "/topics/:topic_id/replies.json" [current_user_id topic_id body] (topic-replies-create current_user_id topic_id body))
            ;; 不要了
            ;(PUT "/topics/:topic_id/replies/:reply_id.json" [topic_id reply_id body] (topic-replies-update topic_id reply_id body))
            ;(DELETE "/topics/:topic_id/replies/:reply_id.json" [topic_id reply_id] (topic-replies-destroy topic_id reply_id))

            (GET "/recommendations.json" [current_user_id page] (recommendations current_user_id (Integer. (or page "1"))))
            (POST "/huanxin/hid2sids.json" [entries] (huanxin-hid2sids entries))

            ;; friends
            (GET "/users/:id/invitations.json" [id] (invitations-index id))
            (POST "/invitations.json" [current_user_id invitee_id reason] (invitations-create current_user_id invitee_id reason))
            (POST "/invitations/:id/agree.json" [current_user_id id] (invitations-agree current_user_id id))
            (POST "/invitations/:id/refuse.json" [current_user_id id] (invitations-refuse current_user_id id))
            (DELETE "/friends/:friend_id.json" [current_user_id friend_id] (friends-destroy current_user_id friend_id))
            (GET "/friends.json" [current_user_id] (friends-index current_user_id))

            ;; feedback
            (POST "/feedbacks.json" [current_user_id content] (feedbacks-create current_user_id content))
            (GET "/push" [user_id content token]
                 (if (not= token notify-key)
                   (json-response {
                                   :code "fail"
                                   :message "TOKEN不正确"
                                   })
                   (let [user (m/users-show user_id)
                         huanxin-user-name (:huanxin_username user)
                         ]
                     (jpush/jpush-it huanxin-user-name content)
                     (json-response {
                                     :code "ok"
                                     })
                     )
                   )
                 )

            (route/not-found "Not Found"))

(defapi app-routes
        (swagger-ui)
        (swagger-docs)
        (swaggered "hello"
                   :description "it's a hello"
                   (GET* "/hi" []
                         :query-params [q :- String]
                         :summary "这是个HI，呼呼"
                         (ok {:total (str "hello," q)})
                         )
                   )
        legacy-app-routes
        )


(def api-defaults
  "A default configuration for a HTTP API."
  {:params    {:urlencoded true
               :multipart  true
               :keywordize true}
   :responses {:not-modified-responses true
               :absolute-redirects     true
               :content-types          true}})

(def app
  (->
    (wrap-parse-login-token app-routes)
    (wrap-defaults api-defaults)
    ;(wrap-json-params {:keywords? true :bigdecimals? true})
    (wrap-restful-params)
    )
  )

;(defn -main []
;  (users-create "abc" "abc" "abc@g.com" "" "" "" "" {
;                                                     :filename "abc.jpg"
;                                                     :tempfile (File. "/Users/sliu/devices.sql")
;                                                     }))

;(defn -main []
;  (println (crypto/base32 8)))

;(defn -main []
;  (notify-using-app-handler nil nil))
