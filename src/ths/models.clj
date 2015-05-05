(ns ths.models
  (:require [clojure.string :as str]
            [cheshire.core :as json]
            [clj-time.core :as t]
            [clojure.pprint :refer :all]
            )
  (:use korma.config
        korma.core
        korma.db
        ths.utils
        ))

(declare users labels topics replies user_labels is-friend? users-show)

;(defdb db-spec (sqlite3 {:db db-path}))
(defdb db-spec (sqlite3 {:db "/Users/sliu/tmp/ths.db"}))

(defentity users
           (has-many user_labels {:fk :user_id})
           (has-many topics {:fk :user_id})
           (transform (fn [v]
                        (-> v
                            (assoc :labels (map :label_name (:user_labels v)))
                            (dissoc :user_labels :password)
                            ;(assoc :image (if (str/blank? (:image v)) nil (str site-root "/signs/" (:id v) "/" (:image v))))
                            (assoc :image (if (str/blank? (:image v)) nil (str "/signs/" (:id v) "/" (:image v))))
                            )
                        ))
           )

(defentity labels
           (pk :label_name))

(defentity topics
           (has-many replies {:fk :topic_id :order ["kkkk"]})
           (belongs-to users {:fk :user_id})
           (transform (fn [v]
                        (-> v
                            (assoc :user (first (select users (where {:id (:user_id v)}) (limit 1)))))))
           )

(defentity replies
           (belongs-to topics {:fk :topic_id})
           (transform (fn [v]
                        (-> v
                            (assoc :user (first (select users (where {:id (:user_id v)}) (limit 1)))))))
           )

(defentity user_labels
           (belongs-to users {:fk :user_id})
           (belongs-to labels {:fk :label_name}))

(defentity invitations
           (transform (fn [v]
                        (-> v
                            (assoc :inviter (users-show (:inviter_id v)))
                            )))
           )

(defentity system_config)


(defentity friends)

(defentity feedbacks)

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

(defn record-login [user_id]
  (update users
          (set-fields {:last_login_at (t/now)})
          (where {:id user_id})
          )
  )

(defn users-not-login-today []
  (select users
          (where (or {:last_login_at nil} {:last_login_at [< (t/plus (t/now) (t/days -1))]}))
          (where {:huanxin_username [not= nil]})
          (order :last_login_at :ASC))
  )

(defn users-all [q label_name topic_id page]
  (if topic_id
    (distinct (concat
                (select users
                        (join user_labels)
                        (with user_labels)
                        (modifier "DISTINCT")
                        (join topics)
                        (where {:topics.id topic_id})
                        )
                (select users
                        (join user_labels)
                        (with user_labels)
                        (modifier "DISTINCT")
                        (join replies (= :replies.user_id :users.id))
                        (where {:replies.topic_id topic_id})
                        )
                ))
    (cond-> (select* users)
            true (join user_labels)
            true (with user_labels)
            true (modifier "DISTINCT")
            ;(not (clojure.string/blank? q)) (where (or {:user_labels.label_name [like (str "%" q "%")]} {:username [like (str "%" q "%")]}))
            (not (clojure.string/blank? q)) (where {:username [like (str "%" q "%")]})
            (not (clojure.string/blank? label_name)) (where {:user_labels.label_name label_name})
            true (limit 20)
            true (offset (* (- page 1) 20))
            ;true (as-sql)
            ;true (println)
            true (select)

            )
    )
  )



(defn users-show [id]
  (first (select users
                 (with user_labels)
                 (where {:id id})
                 (limit 1))))

(defn users-by-huanxin-username [huanxin-username]
  (first (select users
                 (with user_labels)
                 (where {:huanxin_username huanxin-username})
                 (limit 1))))

(defn users-by-email [email]
  (first (select users
                 (with user_labels)
                 (where {:email email})
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
(defn topics-index [current_user_id q label_name user_id page]
  (cond-> (select* topics)
          (not (clojure.string/blank? user_id)) (where {:user_id user_id})
          (not (clojure.string/blank? q)) (where (or {:subject [like (str "%" q "%")]} {:body [like (str "%" q "%")]}))
          (not (clojure.string/blank? label_name)) (where {:label_name label_name})
          true (order :created_at :DESC)
          true (limit 20)
          true (offset (* (- page 1) 20))
          true (select)
          )
  )

(defn topics-show [id]
  (first (select topics
                 (join users)
                 ;(with users)
                 (with replies)
                 (where {:id id})
                 (limit 1)))
  )

(defn topics-by-huanxin-group-id [huanxin-group-id]
  (first (select topics
                 (join users)
                 ;(with users)
                 (with replies)
                 (where {:huanxin_group_id huanxin-group-id})
                 (limit 1)))
  )

(defn topics-create [current_user_id subject body label_name]
  (-> (insert topics
              (values {:subject    subject
                       :body       body
                       :label_name label_name
                       :user_id    current_user_id}))
      ((keyword "last_insert_rowid()"))
      topics-show
      )
  )

(defn topics-update [id subject body label_name]
  (update topics
          (set-fields {
                       :subject    subject
                       :body       body
                       :label_name label_name
                       })
          (where {:id id})))

(defn topics-update-huanxin-group-id [id huanxin_group_id]
  (update topics
          (set-fields {
                       :huanxin_group_id huanxin_group_id
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

(defn topic-replies-show [reply_id]
  (first (select replies
                 (where {:id reply_id})
                 (limit 1)))
  )

(defn topic-replies-create [current_user_id topic_id body]
  (-> (insert replies
              (values {:topic_id topic_id
                       :body     body
                       :user_id  current_user_id
                       }))
      ((keyword "last_insert_rowid()"))
      topic-replies-show
      )
  )

(defn topic-replies-update [topic_id reply_id body]
  (update replies
          (set-fields {:body body})
          (where {:id reply_id})))

(defn topic-replies-destroy [topic_id reply_id]
  (delete replies
          (where {:id reply_id})))

(defn recommendations [current_user_id page]
  (let [user (users-show current_user_id)
        labels (:labels user)
        -offset (* (- page 1) 20)
        -limit 20
        -labels (clojure.string/join "," (map #(format "'%s'" %) labels))
        _ (println page)
        _ (println -labels)
        ;-labels ""
        ;;labels-string (join "," labels)
        sql-topics (str "select *,(label_name in (" -labels ")) m from topics order by m DESC,created_at DESC limit  " -limit " offset " -offset)
        sql-users (str "select *,exists (select * from user_labels where label_name in (" -labels ") and user_labels.user_id = users.id) m,(select count(1) from user_labels where user_labels.user_id = users.id) c from users where c > 0 order by m DESC, users.created_at DESC limit  " -limit " offset " -offset)
        topic-ids (map :id (exec-raw sql-topics :results))
        user-ids (map :id (exec-raw sql-users :results))

        -topics (select topics
                        (where {:id [in topic-ids]})
                        )
        -users (map #(users-show %) user-ids)
        ; (select users
        ;               (join user_labels)
        ;               (with user_labels)
        ;               (where {:id [in user-ids]
        ;                       ;:user_labels.label_name [in labels]
        ;                       })
        ;               (modifier "DISTINCT")
        ;               )
        ]
    (->> (into
           (vec (for [user -users]
                  (assoc user :type "user"
                              :is_friend (is-friend? current_user_id (:id user))
                              :is_matched? (not (empty? (clojure.set/intersection (set labels) (set (:labels user)) ) ))
                              )
                  ))
           (vec (for [topic -topics]
                  (assoc topic :type "topic"
                               :is_matched? (contains? (set labels) (:label_name topic))
                               )
                  ))
           )
         (sort-by #(vec (map % [:is_matched? :created_at])))
         (reverse)
         )
    )

  )


;; friends
(defn invitations-index [invitee_id]
  (select invitations
          (where {:invitee_id invitee_id})
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

(defn is-friend? [user-id friend-id]
  (not (nil? (first (select friends
                            (where {:user_id   user-id
                                    :friend_id friend-id
                                    })))))
  )

(defn friends-index [user_id]
  (let [my-friends (select friends
                           (where {:user_id user_id})
                           (order :created_at :DESC))
        friend-ids (map :friend_id my-friends)
        ]
    (select users
            (with user_labels)
            (modifier "DISTINCT")
            (where {:id [in friend-ids]}))
    ))

;; feedbacks
(defn feedbacks-show [id]
  (first (select feedbacks
                 (where {:id id})
                 (limit 1))))

(defn feedbacks-create [user_id content]
  (-> (insert feedbacks
              (values {
                       :user_id user_id
                       :content content
                       }))
      ((keyword "last_insert_rowid()"))
      (feedbacks-show)
      ))

(defn system-config-index []
  (if-let [o (first (select system_config))]
    (assoc o :can_register_user (= (:can_register_user o) 1)
             :can_login (= (:can_login o) 1)
             :can_upgrade_silently (= (:can_upgrade_silently o) 1))
    )
  )

(defn -main []
  ;(pprint (map #(select-keys % [:label_name :labels]) (recommendations 1 1)))
  (pprint (recommendations 14 1))
  ;(pprint (recommendations 11 1))
  ;(println (json/generate-string (topics-show 1)))
  )



