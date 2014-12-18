(ns ths.routes
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            ;[ring.middleware.json :refer [wrap-json-params wrap-json-response]]
            [ring.middleware.format-params :refer [wrap-restful-params]]
            [com.postspectacular.rotor :as rotor]
            [clojure.java.io :as io]
            [taoensso.timbre :as timbre]
            [ths.models :as m])
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
                      :code         "ok"
                      :token        (generate-login-token (:id user) email)
                      :huanxin_user (generate-huanxin-username email)
                      :user         user
                      })
      {:status  401
       :headers {}
       :body    ""}
      ))
  )

;; users
(defn users-index [q label_name page]
  (json-response (m/users-all q label_name page)))

(defn users-show [id]
  (json-response (m/users-show id)))

(defn users-create [username password email phone]
  (json-response (m/users-create username password email phone)))

(defn users-update [id username password email phone]
  (json-response (m/users-update id username password email phone)))

(defn users-update-labels [id labels]
  (json-response (m/users-update-labels id labels)))

(defn users-destroy [id]
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
  (json-response (m/topics-create current_user_id subject body label_name)))

(defn topics-update [id subject body label_name]
  (json-response (m/topics-update id subject body label_name)))

(defn topics-destroy [id]
  (json-response (m/topics-destroy id))
  )

(defn topic-replies-index [topic_id]
  (json-response (m/topic-replies-index topic_id)))

(defn topic-replies-create [current_user_id topic_id body]
  (json-response (m/topic-replies-create current_user_id topic_id body)))

(defn topic-replies-update [topic_id reply_id body]
  (json-response (m/topic-replies-update topic_id reply_id body)))

(defn topic-replies-destroy [topic_id reply_id]
  (json-response (m/topic-replies-destroy topic_id reply_id)))

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

           ;; users
           (GET "/users.json" [q label_name page] (users-index q label_name (Integer. (or page "1"))))
           (GET "/users/:id.json" [id] (users-show id))
           (POST "/users.json" [username password email phone] (users-create username password email phone))
           (PUT "/users/:id.json" [id username password email phone] (users-update id username password email phone))
           (PUT "/users/:id/update_labels.json" [id labels] (users-update-labels id labels))
           (DELETE "/users/:id.json" [id] (users-destroy id))

           ;; labels
           (GET "/labels.json" [q] (labels-index q))
           (POST "/labels.json" [label_name] (labels-create label_name))
           (DELETE "/labels/:label_name.json" [label_name] (labels-destroy label_name))

           ;; topics
           (GET "/topics.json" [current_user_id user_id page] (topics-index current_user_id user_id (Integer. (or page "1"))))
           (GET "/topics/:id.json" [id] (topics-show id))
           (POST "/topics.json" [current_user_id subject body label_name] (topics-create current_user_id subject body label_name))
           (PUT "/topics/:id.json" [id subject body label_name] (topics-update id subject body label_name))
           (DELETE "/topics/:id.json" [id] (topics-destroy id))

           ;; replies
           (GET "/topics/:topic_id/replies.json" [topic_id] (topic-replies-index topic_id))
           (POST "/topics/:topic_id/replies.json" [current_user_id topic_id body] (topic-replies-create current_user_id topic_id body))
           (PUT "/topics/:topic_id/replies/:reply_id.json" [topic_id reply_id body] (topic-replies-update topic_id reply_id body))
           (DELETE "/topics/:topic_id/replies/:reply_id.json" [topic_id reply_id] (topic-replies-destroy topic_id reply_id))

           ;; friends
           (GET "/users/:id/invitations" [id] (invitations-index id))
           (POST "/invitations.json" [current_user_id invitee_id reason] (invitations-create current_user_id invitee_id reason))
           (POST "/invitations/:id/agree.json" [current_user_id id] (invitations-agree current_user_id id))
           (POST "/invitations/:id/refuse.json" [current_user_id id] (invitations-refuse current_user_id id))
           (DELETE "/friends/:friend_id" [current_user_id friend_id] (friends-destroy current_user_id friend_id))
           (GET "/users/:id/friends" [id] (friends-index id))


           (route/not-found "Not Found"))

(def app
  (->
    (wrap-parse-login-token app-routes)
    (wrap-defaults api-defaults)
    ;(wrap-json-params {:keywords? true :bigdecimals? true})
    (wrap-restful-params)
    )
  )
