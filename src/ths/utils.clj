(ns ths.utils
  (:require [cheshire.core :as json]
            [clojure.data.codec.base64 :as base64]
            [ring.util.codec :as codec]
            [pandect.core :refer :all]))

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

(defn generate-huanxin-username [email]
  (sha1-hmac email huanxin-salt)
  )

(defn generate-login-token [email]
  (str email ";" (sha1-hmac email login-salt))
  )

(defn parse-login-token [f]
  (let [[email token] (clojure.string/split f #";" 2)
        verify-token (generate-login-token email)]
    (when (= verify-token f)
      email)
    )
  )

(defn wrap-parse-login-token [handler]
  (fn [request]
    ;(println request)
    (handler (if-let [token (or (get-in request [:params :token]) (get-in request [:headers "x-token"]))]
               (assoc-in request [:params :current_email] (parse-login-token token))
               request
               ))

    ))

;(defn -main []
;  (println (sha1-hmac "hello" token-salt)) )

;(defn -main []
;  (println (String. (base64/encode (.getBytes "hello") )) ))

;(defn -main []
;  (println (String. (base64/decode (.getBytes "aGVsbG8=") )) ))

; "bGl1c29uZzExMTFAZ21haWwuY29tO2Q0YjYyOWE4MDkzNDU2N2UwNDUzMGViYmQyZmJlNGUxMjhlODVlZDA="
; "bGl1c29uZzExMTFAZ21haWwuY29tO2Q0YjYyOWE4MDkzNDU2N2UwNDUzMGViYmQyZmJlNGUxMjhlODVlZDA%3D"
; "liusong1111@gmail.com;d4b629a80934567e04530ebbd2fbe4e128e85ed0"
;(defn -main []
;  (println (generate-login-token "liusong1111@gmail.com")))

(defn -main []
  (println (parse-login-token "liusong1111@gmail.com;d4b629a80934567e04530ebbd2fbe4e128e85ed0")))