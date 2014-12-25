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
           (transform (fn [v]
                        (-> v
                            (assoc :labels (map :label_name (:user_labels v)))
                            (dissoc :user_labels :password)
                            (assoc :image (if (str/blank? (:image v)) nil (str site-root "/signs/" (:id v) "/" (:image v)))))
                        ))
           )

(defentity labels
           (pk :label_name))

(defentity topics
           (has-many replies {:fk :topic_id}))

(defentity replies
           (belongs-to topics {:fk :topic_id}))

(defentity user_labels
           (belongs-to users {:fk :user_id})
           (belongs-to labels {:fk :label_name}))

(defentity invitations)

(defentity friends)

;; ------------------

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
(defn users-all [q label_name page]
  (cond-> (select* users)
          true (join user_labels)
          true (with user_labels)
          (not (clojure.string/blank? q)) (where (or {(keyword "user_labels.label_name") [like (str "%" q "%")]} {:username [like (str "%" q "%")]}))
          (not (clojure.string/blank? label_name)) (where {(keyword "user_labels.label_name") label_name})
          true (limit 20)
          true (offset (* (- page 1) 20))
          true (select)
          ))

(defn users-show [id]
  (first (select users
                 (with user_labels)
                 (where {:id id})
                 (limit 1))))

(defn users-create [username password email phone sex birth city huanxin_username image]
  (-> (insert users
              (values {:username         username
                       :password         password
                       :email            email
                       :phone            phone
                       :sex              sex
                       :birth            birth
                       :city             city
                       :huanxin_username huanxin_username
                       :image            image}))
      ((keyword "last_insert_rowid()"))
      users-show
      )
  )

(defn users-update [id attrs]
  (update users
          (set-fields attrs)
          (where {:id id})))

(defn users-update-labels [id label_names]
  (delete user_labels
          (where {:user_id id}))
  (doall (for [label_name label_names]
           (do
             (insert user_labels
                     (values {:user_id id :label_name label_name}
                             ))
             (if (not (first (select labels
                                     (where {:label_name label_name})
                                     (limit 1))))
               (insert labels
                       (values {:label_name label_name}))
               )
             ))
         )
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
(defn topics-index [current_user_id user_id page]
  (cond-> (select* topics)
          (not (clojure.string/blank? user_id)) (where {:user_id user_id})
          true (order :created_at :DESC)
          true (limit 20)
          true (offset (* (- page 1) 20))
          true (select)
          )
  )

(defn topics-show [id]
  (first (select topics
                 (with replies)
                 (where {:id id})
                 (limit 1)))
  )

(defn topics-create [current_user_id subject body label_name]
  (insert topics
          (values {:subject    subject
                   :body       body
                   :label_name label_name
                   :user_id    current_user_id})))

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

(defn topic-replies-create [current_user_id topic_id body]
  (insert replies
          (values {:topic_id topic_id
                   :body     body
                   :user_id  current_user_id
                   })))

(defn topic-replies-update [topic_id reply_id body]
  (update replies
          (set-fields {:body body})
          (where {:id reply_id})))

(defn topic-replies-destroy [topic_id reply_id]
  (delete replies
          (where {:id reply_id})))

;; friends
(defn invitations-index [inviter_id]
  (select invitations
          (where {:inviter_id inviter_id})
          (order :created_at :DESC)))

(defn invitations-create [current_user_id invitee_id reason]
  (insert invitations
          (values {:inviter_id current_user_id
                   :invitee_id invitee_id
                   :reason     reason}))
  )

(defn invitations-agree [current_user_id invitation_id]
  (if-let [invitation (first (select invitations
                                     (where {:id invitation_id})
                                     (limit 1)))]
    (do (update invitations
                (set-fields {:status "agreed"})
                (where {:id invitation_id}))
        (insert friends
                (values {:user_id   (:inviter_id invitation)
                         :friend_id (:invitee_id invitation)}))
        (insert friends
                (values {:user_id   (:invitee_id invitation)
                         :friend_id (:inviter_id invitation)})))

    )
  )

(defn invitations-refuse [current_user_id invitation_id]
  (if-let [invitation (first (select invitations
                                     (where {:id invitation_id})
                                     (limit 1)))]
    (do (update invitations
                (set-fields {:status "refused"})
                (where {:id invitation_id}))
        (delete friends
                (where (or {:user_id   (:inviter_id invitation)
                            :friend_id (:invitee_id invitation)}
                           {:user_id   (:invitee_id invitation)
                            :friend_id (:inviter_id invitation)}
                           ))
                ))

    )
  )

(defn friends-destroy [current_user_id friend_id]
  (delete friends
          (where (or {:user_id   current_user_id
                      :friend_id friend_id}
                     {:user_id   friend_id
                      :friend_id current_user_id}))))

(defn friends-index [user_id]
  (let [my-friends (select friends
                           (where {:user_id user_id})
                           (order :created_at :DESC))
        friend-ids (map :friend_id my-friends)
        ]
    (select users
            (with user_labels)
            (where {:id [in friend-ids]}))
    ))



