(ns ths.jpush
  (:require [ths.utils :refer :all])
  (:import
    (cn.jpush.api.push.model.audience Audience)
    (cn.jpush.api JPushClient)
    (cn.jpush.api.push.model Platform PushPayload)
    (cn.jpush.api.push.model.notification Notification)
    (cn.jpush.api.common.resp APIRequestException))
  (:gen-class)
  )

(def jpush-client (JPushClient. jpush-master-secret jpush-app-key 3))

(defn build-push-object [audience content]
  (.build
    (doto (PushPayload/newBuilder)
      (.setPlatform (Platform/all))
      (.setAudience (Audience/alias (into-array String [audience])))
      (.setNotification (Notification/alert content))
      ))
  )

(defn jpush-it [audience content]
  (try (let [payload (build-push-object audience content)]
         (.sendPush jpush-client payload)
         ;(println push-object)
         ;(println jpush-client)
         )
       (catch APIRequestException e
         (println e)
         (println (.getStatus e))
         (println (.getErrorCode e))
         (println (.getErrorMessage e))
         ))

  )

(defn -main []
  (jpush-it "user1-alias" "hahaha")
  ;(println "kkk")
  )