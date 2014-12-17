(ns ths.routes
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            ;[ring.middleware.json :refer [wrap-json-params wrap-json-response]]
            [ring.middleware.format :refer [wrap-restful-format]]
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


(defn users-index []
  (json-response (m/users-all)))

(defroutes app-routes
           (GET "/" [] "Hello World")
           ; http://d1.apk8.com:8020/game_m/zhaotonglei.apk
           (POST "/login.json" [username password] (login username password))
           (GET "/users.json" [] (users-index))
           (route/not-found "Not Found"))

(def app
  (->
    (wrap-defaults app-routes api-defaults)
    ;(wrap-json-params {:keywords? true :bigdecimals? true})
    (wrap-restful-format)
    )
  )
