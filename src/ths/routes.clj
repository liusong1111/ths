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

(defn login [username password]
  (let [user (m/auth username password)]
    (if user
      (json-response {
                      :code "ok"
                      :user user
                      })
      {:status  401
       :headers {}
       :body    ""}
      ))
  )

;; users
(defn users-index []
  (json-response (m/users-all)))

(defn users-create [username password email phone]
  (json-response (m/users-create username password email phone)))

(defn users-update [id username password email phone]
  (json-response (m/users-update id username password email phone)))

(defn users-update-labels [id labels]
  (json-response (m/users-update-labels id labels)))

(defn users-destroy [id]
  (json-response (m/users-destroy id)))

;; labels
(defn labels-index []
  (json-response (m/labels-index)))

(defn labels-create [label_name]
  (json-response (m/labels-create label_name)))

(defn labels-destroy [label_name]
  (json-response (m/labels-destroy label_name)))

;; topics
(defn topics-index []
  (json-response (m/topics-index)))

(defn topics-create [subject body label_name]
  (json-response (m/topics-create subject body label_name)))

(defn topics-update [id subject body label_name]
  (json-response (m/topics-update id subject body label_name)))

(defn topics-destroy [id]
  (json-response (m/topics-destroy id))
  )

(defn topic-replies-index [topic_id]
  (json-response (m/topic-replies-index topic_id)))

(defn topic-replies-create [topic_id body]
  (json-response (m/topic-replies-create topic_id body)))

(defn topic-replies-update [topic_id reply_id body]
  (json-response (m/topic-replies-update topic_id reply_id body)))

(defn topic-replies-destroy [topic_id reply_id]
  (json-response (m/topic-replies-destroy topic_id reply_id)))

(defroutes app-routes
           (GET "/" [] "Hello World")

           (GET "/demo.json" [] (json-response {:code "OK" :message "welcome"}))
           (POST "/demo.json" [name] (json-response {:code "OK" :message (str "created:" name)}))
           (PUT "/demo.json" [name] (json-response {:code "OK" :message (str "updated:" name)}))
           (DELETE "/demo.json" [name] (json-response {:code "OK" :message (str "deleted:" name)}))

           ;; login
           (POST "/login.json" [username password] (login username password))

           ;; users
           (GET "/users.json" [] (users-index))
           (POST "/users.json" [username password email phone] (users-create username password email phone))
           (PUT "/users/:id.json" [id username password email phone] (users-update id username password email phone))
           (PUT "/users/:id/update_labels.json" [id labels] (users-update-labels id labels))
           (DELETE "/users/:id.json" [id] (users-destroy id))

           ;; labels
           (GET "/labels.json" [] (labels-index))
           (POST "/labels.json" [label_name] (labels-create label_name))
           (DELETE "/labels/:label_name.json" [label_name] (labels-destroy label_name))

           ;; topics
           (GET "/topics.json" [] (topics-index))
           (POST "/topics.json" [subject body label_name] (topics-create subject body label_name))
           (PUT "/topics/:id.json" [id subject body label_name] (topics-update id subject body label_name))
           (DELETE "/topics/:id.json" [id] (topics-destroy id))

           ;; replies
           (GET "/topics/:topic_id/replies.json" [topic_id] (topic-replies-index topic_id))
           (POST "/topics/:topic_id/replies.json" [topic_id body] (topic-replies-create topic_id body))
           (PUT "/topics/:topic_id/replies/:reply_id.json" [topic_id reply_id body] (topic-replies-update topic_id reply_id body))
           (DELETE "/topics/:topic_id/replies/:reply_id.json" [topic_id reply_id] (topic-replies-destroy topic_id reply_id))

           (route/not-found "Not Found"))

(def app
  (->
    (wrap-defaults app-routes api-defaults)
    ;(wrap-json-params {:keywords? true :bigdecimals? true})
    (wrap-restful-params)
    )
  )
