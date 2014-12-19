(ns ths.utils
  (:require [cheshire.core :as json]
            [clojure.data.codec.base64 :as base64]
            [ring.util.codec :as codec]
            [pandect.core :refer :all])
  (:import (org.apache.commons.io FileUtils)
           (java.io File)))

(defn json-response [data & [status]]
  {:status  (or status 200)
   :headers {"Content-Type" "application/json;charset=UTF-8"}
   :body    (json/generate-string data)})

; 数据库路径
(def db-path "ths.db")

; login用的salt
(def login-salt "TONGHANG-SECRET")

; 生成环信帐号用的salt
(def huanxin-salt "HUANXIN-SECRET")

; 头像存储目录
(def image-path "./images")

(FileUtils/forceMkdir (File. image-path))


(defn generate-huanxin-username [email]
  (sha1-hmac email huanxin-salt))

(defn generate-login-token [user_id email]
  (str user_id ";" email ";" (sha1-hmac email login-salt))
  )

(defn parse-login-token [f]
  (let [[user_id email token] (clojure.string/split f #";" 3)
        verify-token (generate-login-token user_id email)]
    (when (= verify-token f)
      [user_id email])
    )
  )

(defn wrap-parse-login-token [handler]
  (fn [request]
    ;(println request)
    (handler (if-let [token (or (get-in request [:params :token]) (get-in request [:headers "x-token"]))]
               (let [[current_user_id current_user_email] (parse-login-token token)
                     request (assoc-in request [:params :current_user_id] current_user_id)
                     request (assoc-in request [:params :current_user_email] current_user_email)]
                 request)
               request
               ))

    ))

;(defn -main []
;  (println (sha1-hmac "hello" token-salt)) )

;(defn -main []
;  (println (String. (base64/encode (.getBytes "hello") )) ))

;(defn -main []
;  (println (String. (base64/decode (.getBytes "aGVsbG8=") )) ))
; "1;liusong1111@gmail.com;d4b629a80934567e04530ebbd2fbe4e128e85ed0"
(defn -main []
  (println (generate-login-token 1 "liusong1111@gmail.com")))

;(defn -main []
;  (println (parse-login-token "1;liusong1111@gmail.com;d4b629a80934567e04530ebbd2fbe4e128e85ed0")))