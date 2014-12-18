# 忘记密码，发送重置密码到邮箱（server api:
POST /forget_password?email=xx@yy

#按标签搜索用户




# 请求加好友
POST /invitations/1?user_id=2&reason=xxx

# 同意加好友
POST /invitations/agree/1?user_id=2

# 拒绝加好友
POST /invitations/refuse/1?user_id=2

# 删好友
DELETE /friends/1?user_id=2

# 得到某人的好友列表
GET /friends/1

# 得到某人的好友申请列表
GET /invitations/1

