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

(defn remove-blank-values [record]
  (into {} (filter second record)))

(defn char-range [start-char end-char]
  (map char (range (int start-char) (inc (int end-char))))
  )

(def safe-chars
  (remove #{\0 \o \1 \l \2 \z \6 \b \9 \g \q} (concat (char-range \a \z) (char-range \0 \9)))
  )

(defn rand-str [len]
  (apply str (repeatedly len #(rand-nth safe-chars)))
  )

; 数据库路径
(def db-path "ths.db")

; login用的salt
(def login-salt "TONGHANG-SECRET")

; 生成环信帐号用的salt
(def huanxin-salt "HUANXIN-SECRET")

; 头像存储目录
(def image-path "./images")

; huanxin
(def huanxin-url-root "http://a1.easemob.com/tonghang/tonghang")

; jpush
(def jpush-master-secret "ed32167a8641ab1ee8658b3a")

(def jpush-app-key "eb4e79c4ab182d725ec2ff15")

(def notify-key "axced79c4ab182d725ec2ff15")

(FileUtils/forceMkdir (File. image-path))

(defn refine-user-id-str [user_id]
  (let [s (str user_id)
        p (.indexOf s ".")]
    (if (= p -1)
      s
      (.substring s 0 p)
      )
    )
  )

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

(def site-root "http://10.0.2.2:3000")

;(defn -main []
;  (println (sha1-hmac "hello" token-salt)) )

;(defn -main []
;  (println (String. (base64/encode (.getBytes "hello") )) ))

;(defn -main []
;  (println (String. (base64/decode (.getBytes "aGVsbG8=") )) ))
; "1;liusong1111@gmail.com;d4b629a80934567e04530ebbd2fbe4e128e85ed0"
;(defn -main []
;  (println (generate-login-token 1 "liusong1111@gmail.com")))

;(defn -main []
;  (println (parse-login-token "1;liusong1111@gmail.com;d4b629a80934567e04530ebbd2fbe4e128e85ed0")))

;(defn -main []
;  (println (rand-str 8)))