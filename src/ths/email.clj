(ns ths.email
  (:require [postal.core :as postal]))

(defn send-email-for-forget-password [username new-password to]
  (postal/send-message
    {
     :host "smtp.126.com"
     :user "tonghangtonghang@126.com"
     :pass "th2015"
     }
    {
     :from    "tonghangtonghang@126.com"
     :to      [to]
     ;:cc ""
     :subject "【同行】密码已重置"
     :body    (str "尊敬的" username "，您好！\n\n"
                   "您的同行帐户密码已更改，最新密码是："
                   new-password "\n请牢记新密码。\n\n"
                   "祝您使用快乐！")
     ;:X-Tra "else..."
     })
  )

(defn -main []
  ;(send-email-for-forget-password "刘松" "111111" "42279444@qq.com")
  )

