# 忘记密码，发送重置密码到邮箱（server api:
POST /forget_password?email=xx@yy

#按标签搜索用户





#查看用户信息
GET /users/1.json
(返回基本信息+tags)

#查看某用户的话题
GET /topics?user_id=1&page=2

#查看某话题的详细聊天信息（包括回复）
GET /topics/1
返回
{
  subject: "xxx",
  label_name: "yyy",
  body: "yyy",
  user_id: 33,
  created_at: "2014-11-33 ...",
  replies: [
    {
        body: "kkk",
        user_id: 33,
        created_at: "2014-11-13",
        updated_at: "...",
        user: {
          image: "aa.jpg"
        }
    }
  ]
}

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

