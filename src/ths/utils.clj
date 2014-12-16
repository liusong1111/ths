(ns ths.utils
  (:require [cheshire.core :as json]))

(defn json-response [data & [status]]
  {:status  (or status 200)
   :headers {"Content-Type" "application/json"}
   :body    (json/generate-string data)})

; 数据库路径
(def db-path "ths.db")