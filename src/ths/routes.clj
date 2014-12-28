(ns ths.routes
  (:import (org.apache.commons.io FileUtils)
           (java.io File))
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults]]
            ;[ring.middleware.json :refer [wrap-json-params wrap-json-response]]
            [ring.middleware.format-params :refer [wrap-restful-params]]
            [com.postspectacular.rotor :as rotor]
            [clojure.java.io :as io]
            [taoensso.timbre :as timbre]
            [ths.models :as m]
            [ths.huanxin :as h]
            [cheshire.core :as json]
            [postal.core :as mailer])
  (:use ths.utils)
  )

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

  (timbre/info "TongHang Server is starting"))

(defn destroy []
  (timbre/info "TongHang Server is shutting down"))

(defn login [email password]
  (let [user (m/auth email password)]
    (if user
      (json-response {
                      :code  "ok"
                      :token (generate-login-token (:id user) email)
                      :user  user
                      })
      (json-response {
                      :code    "fail"
                      :message "用户名或密码不正确"
                      } 401)
      ))
  )

(defn forget_password [email]
  (json-response {
                  :code "ok"
                  })
  )

;; users
(defn users-index [q label_name topic_id page]
  (json-response (m/users-all q label_name topic_id page)))

(defn users-show [id]
  (json-response (m/users-show id)))

(defn users-create [username password email phone sex birth city image]
  (let [huanxin_username (generate-huanxin-username email)
        user (m/users-create username password email phone sex birth city huanxin_username (:filename image))
        user_id (:id user)
        sign-image-path (str image-path "/" user_id)
        _ (when (:tempfile image)
            (FileUtils/forceMkdir (File. sign-image-path))
            (io/copy (:tempfile image) (io/file sign-image-path (:filename image))))]
    (h/users-create huanxin_username password username)
    (json-response user)
    )
  )

(defn users-update [id attrs image]
  (m/users-update id attrs)
  (when (:tempfile image)
    (FileUtils/forceMkdir (File. (str image-path "/" id)))
    (io/copy (:tempfile image) (io/file image-path id (:filename image)))
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
(defn topics-index [current_user_id user_id page]
  (json-response (m/topics-index current_user_id user_id page)))

(defn topics-show [id]
  (json-response (m/topics-show id)))

(defn topics-create [current_user_id subject body label_name]
  (let [topic (m/topics-create current_user_id subject body label_name)
        user (m/users-show current_user_id)
        {:keys [error result]} @(h/groups-create (str (:subject topic)) (:huanxin_username user))
        huanxin-group-id (if (not error) (get-in result [:data :groupid]))
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
        huanxin_username (:huanxin_username user)
        reply (m/topic-replies-create current_user_id topic_id body)
        topic (m/topics-show topic_id)
        huanxin_group_id (:huanxin_group_id topic)
        _ (h/messages-post-text "chatgroups" [huanxin_group_id] body huanxin_username)
        _ (h/groups-add-member huanxin_group_id huanxin_username)
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

(defn recommendations [current_user_id]
  (json-response (m/recommendations current_user_id))
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
  (json-response (m/invitations-create current_user_id invitee_id reason)))

(defn invitations-agree [current_user_id invitation_id]
  (json-response (m/invitations-agree current_user_id invitation_id)))

(defn invitations-refuse [current_user_id invitation_id]
  (json-response (m/invitations-refuse current_user_id invitation_id)))

(defn friends-destroy [current_user_id friend_id]
  (json-response (m/friends-destroy current_user_id friend_id)))

(defn friends-index [user_id]
  (json-response (m/friends-index user_id)))

(defroutes app-routes
           (GET "/" [] "Hello World")

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
           (GET "/topics.json" [current_user_id user_id page] (topics-index current_user_id user_id (Integer. (or page "1"))))
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

           (GET "/recommendations.json" [current_user_id] (recommendations current_user_id))
           (POST "/huanxin/hid2sids.json" [entries] (huanxin-hid2sids entries))

           ;; friends
           (GET "/users/:id/invitations.json" [id] (invitations-index id))
           (POST "/invitations.json" [current_user_id invitee_id reason] (invitations-create current_user_id invitee_id reason))
           (POST "/invitations/:id/agree.json" [current_user_id id] (invitations-agree current_user_id id))
           (POST "/invitations/:id/refuse.json" [current_user_id id] (invitations-refuse current_user_id id))
           (DELETE "/friends/:friend_id.json" [current_user_id friend_id] (friends-destroy current_user_id friend_id))
           (GET "/users/:id/friends.json" [id] (friends-index id))

           (route/not-found "Not Found"))

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

(defn -main []
  (println (mailer/send-message {
                                 :from    "liusong1111@gmail.com"
                                 :to      ["liusong1111@gmail.com"]
                                 :subject "这是一个测试"
                                 :body    "哈哈\n呼呼"
                                 })))
