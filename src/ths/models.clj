(ns ths.models
  (:use korma.config
        korma.core
        korma.db
        ths.utils
        ))

(declare users labels topics replies user_labels)

(defdb db-spec (sqlite3 {:db db-path}))

(defentity users
           (has-many user_labels {:fk :user_id})
           (many-to-many labels :user_labels))

(defentity labels
           (pk :label_name))

(defentity topics
           (has-many replies {:fk :topic_id}))

(defentity replies
           (belongs-to topics {:fk :topic_id}))

(defentity user_labels
           (database db-spec)
           (belongs-to users {:fk :user_id})
           (belongs-to labels {:fk :label_name}))

;; ------------------
(defn auth [username password]
  (first (select users
                 (where {:username username
                         :password password})
                 (with user_labels (with labels))
                 (limit 1)))
  )
(defn users-create [username password email phone]
  (insert users
          (values {:username username
                   :password password
                   :email    email
                   :phone    phone})))

(defn users-update [id username password email phone]
  (update users
          (set-fields {:username username
                       :password password
                       :email    email
                       :phone    phone})
          (where {:id id})))

(defn users-update-labels [id labels]
  (delete user_labels
          (where {:user_id id}))
  (doall (for [label labels]
           (insert user_labels
                   (values {:user_id id :label_name label}
                           ))))
  ;(insert user_labels
  ;        (values (vec (map #(assoc {:user_id id} :label_name %) labels))
  ;                ))
  )

(defn users-destroy [id]
  (delete users
          (where {:id id}))
  (delete user_labels
          (where {:user_id id})))

;; labels

(defn labels-index []
  (select labels))


(defn users-all []
  (select users))