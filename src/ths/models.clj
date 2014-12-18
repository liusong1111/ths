(ns ths.models
  (:require [clojure.string :as str])
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

;; auth
(defn auth [email password]
  (first (select users
                 (where {:email    email
                         :password password})
                 (with user_labels (with labels))
                 (limit 1)))
  )

;; users
(defn users-all []
  (select users))

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
;(defn labels-index [q]
;  (-> (present-where (select* labels)
;                     q {:label_name [like (str "%" q "%")]}
;                     )
;      (select))
;
;  )

(defn labels-index [q]
  (cond-> (select* labels)
          (not (str/blank? q)) (where {:label_name [like (str "%" q "%")]})
          true (select)
          )
  )

(defn labels-create [label_name]
  (insert labels
          (values {:label_name label_name})))

(defn labels-destroy [label_name]
  (delete labels
          (where {:label_name label_name})))

;; topics
(defn topics-index []
  (select topics
          (order :created_at :DESC)
          (limit 20)))

(defn topics-create [subject body label_name]
  (insert topics
          (values {:subject    subject
                   :body       body
                   :label_name label_name})))

(defn topics-update [id subject body label_name]
  (update topics
          (set-fields {
                       :subject    subject
                       :body       body
                       :label_name label_name
                       })
          (where {:id id})))

(defn topics-destroy [id]
  (delete topics
          (where {:id id}))
  (delete replies
          (where {:topic_id id})))

;; replies
(defn topic-replies-index [topic_id]
  (select replies
          (where {:topic_id topic_id})
          (order :created_at :DESC)
          (limit 20)))

(defn topic-replies-create [topic_id body]
  (insert replies
          (values {:topic_id topic_id
                   :body     body
                   })))

(defn topic-replies-update [topic_id reply_id body]
  (update replies
          (set-fields {:body body})
          (where {:id reply_id})))

(defn topic-replies-destroy [topic_id reply_id]
  (delete replies
          (where {:id reply_id})))
