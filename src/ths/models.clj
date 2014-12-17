(ns ths.models
  (:use korma.config
        korma.core
        korma.db
        ths.utils
        ))

(declare users labels topics replies user_labels)

(defdb db-spec (sqlite3 {:db db-path}))

(defentity users (database db-spec))

(defentity labels (database db-spec))

(defentity topics (database db-spec)
           (has-many replies))

(defentity replies (database db-spec)
           (belongs-to topics))

(defentity user_labels (database db-spec))


(defn auth [username password]
  (first (select users
                 (where {:username username
                         :password password})
                 (limit 1)))
  )

(defn users-all []
  (select users))