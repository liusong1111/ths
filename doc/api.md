# 登录
    curl -i -X POST http://127.0.0.1:3000/login.json -d "email=liusong1111@gmail.com&password=aaaaaa"
    # 返回格式例如
    HTTP STATUS: 200
    HTTP BODY:
    {"code":"ok","token":"1;liusong1111@gmail.com;d4b629a80934567e04530ebbd2fbe4e128e85ed0","user":{"labels":["美食","java"],"email":"liusong1111@gmail.com","sex":"male","phone":null,"city":"beijing","username":"sliu","huanxin_username":"4c36aba13d16f79ed79a29eec4bfbde0163e2d4f","id":1,"image":"http://10.0.2.2:3000/signs/1/0015.jpg","created_at":"2014-12-25 09:26:53","birth":"1980-01-01"}}
    #注：回复的body里，有token字段和huanxin_user字段
    #后续所有请求，请求的headers里，必须带一个头"x-token:收到的token"
    #huanxin_user是该用户在环信上的用户名
    如果登录失败，返回：
    HTTP STATUS: 401
    HTTP BODY:
    {"code":"fail","message":"用户名或密码不正确"}

# 用户管理

## 得到用户列表
    curl http://127.0.0.1:3000/users.json
    # 返回格式例如：
    HTTP STATUS: 200
    HTTP BODY:
    [{"labels":["美食","java"],"email":"liusong1111@gmail.com","sex":"male","phone":null,"city":"beijing","username":"sliu","huanxin_username":"4c36aba13d16f79ed79a29eec4bfbde0163e2d4f","id":1,"image":"http://10.0.2.2:3000/signs/1/0015.jpg","created_at":"2014-12-25 09:26:53","birth":"1980-01-01"},{"labels":["美食","java"],"email":"liusong1111@gmail.com","sex":"male","phone":null,"city":"beijing","username":"sliu","huanxin_username":"4c36aba13d16f79ed79a29eec4bfbde0163e2d4f","id":1,"image":"http://10.0.2.2:3000/signs/1/0015.jpg","created_at":"2014-12-25 09:26:53","birth":"1980-01-01"}]

## 标签下的用户
    curl http://127.0.0.1:3000/users.json?label_name=美食

## 搜用户
    curl http://127.0.0.1:3000/users.json?q=美&page=1
    注：按username或label_name模糊搜索

## 查看用户信息
    curl http://127.0.0.1:3000/users/1.json
    HTTP STATUS: 200
    HTTP BODY:
    {"labels":["美食","java"],"email":"liusong1111@gmail.com","sex":"male","phone":null,"city":"beijing","username":"sliu","huanxin_username":"4c36aba13d16f79ed79a29eec4bfbde0163e2d4f","id":1,"image":"http://10.0.2.2:3000/signs/1/0015.jpg","created_at":"2014-12-25 09:26:53","birth":"1980-01-01"}

## 注册用户
    curl -X POST http://127.0.0.1:3000/users.json -F "image=@0015.jpg" -F "username=sliu1" -F "password=aaaaaa" -F "email=liusong1111@gmail.com" -F "sex=male" -F "birth=1980-01-01" -F "city=beijing"
    #返回格式例如：
    HTTP STATUS: 201
    HTTP BODY:
    {"labels":[],"email":"liusong1111@gmail.com","sex":"male","phone":null,"city":"beijing","username":"sliu1","huanxin_username":"4c36aba13d16f79ed79a29eec4bfbde0163e2d4f","id":1,"image":"http://10.0.2.2:3000/signs/1/0015.jpg","created_at":"2014-12-25 09:26:53","birth":"1980-01-01"}
    注: 本步server会在环信上注册帐号，用户名为对email的sha1编码。
    response里含huanxin_username字段

## 修改用户信息
    curl -X PUT http://127.0.0.1:3000/users/1.json -F "username=sliu" -F "password=aaaaaa" -F "email=liusong1111@gmail.com" -F "sex=male" -F "birth=1980-01-01"
    #返回格式例如：
    HTTP STATUS: 200
    HTTP BODY:
    {"labels":[],"email":"liusong1111@gmail.com","sex":"male","phone":null,"city":"beijing","username":"sliu","huanxin_username":"4c36aba13d16f79ed79a29eec4bfbde0163e2d4f","id":1,"image":"http://10.0.2.2:3000/signs/1/0015.jpg","created_at":"2014-12-25 09:26:53","birth":"1980-01-01"}
    注：本步如果修改了密码，则server会通知环信修改密码。
    另注：如果改邮箱，因为环信帐号是根据邮箱算出来的，也需要修改，环信尚不提供修改用户名的接口，因此，最好在app端就不让更改邮箱。

## 设置某人的标签
    curl -X PUT -H "Content-Type:application/json" http://127.0.0.1:3000/users/1/update_labels.json -d "{\"labels\": [\"美食\",\"java\"]}"
    #返回格式例如：
    HTTP STATUS: 201
    HTTP BODY:
    {"labels":["美食","java"],"email":"liusong1111@gmail.com","sex":"male","phone":null,"city":"beijing","username":"sliu","huanxin_username":"4c36aba13d16f79ed79a29eec4bfbde0163e2d4f","id":1,"image":"http://10.0.2.2:3000/signs/1/0015.jpg","created_at":"2014-12-25 09:26:53","birth":"1980-01-01"}


## 删除用户
    DELETE /users/1.json
    curl -X DELETE -H "Content-Type:application/json" http://127.0.0.1:3000/users/1.json
    返回格式例如：
    HTTP STATUS: 204

# 标签管理

## 得到标签列表
    GET /labels.json
    curl http://127.0.0.1:3000/labels.json
    # 返回格式例如：
    HTTP STATUS: 200
    HTTP BODY:
    [{"id":1,"label_name":"美食","url":"http://127.0.0.1:3000/labels/1.json"}]

## 搜标签
    GET /labels.json?q=美食


# 主题管理

## 得到话题列表
    GET /topics.json
    curl http://127.0.0.1:3000/topics.json
    # 返回格式例如：
    HTTP STATUS: 200
    HTTP BODY:
    [{"id":1,"subject":"清蒸鲈鱼怎么做？","body":"材料\n鲈鱼，葱，姜，料酒，李锦记蒸鱼豉油，油\n做法\n1.鲈鱼洗净，用葱姜料酒腌一会儿去腥。\n2.放蒸锅蒸８分钟，时间到不要开锅，再焐几分钟为好。\n3.把鱼取出，倒掉汁水。\n4.放上葱丝，浇上李锦记蒸鱼豉油，锅热油，再浇到鱼上。","label_name":"美食","user_id":1,"url":"http://127.0.0.1:3000/topics/1.json"}]


## 查看某用户的话题列表
    GET /topics.json?user_id=1&page=1
    curl http://127.0.0.1:3000/topics.json?user_id=1&page=1

## 查看某话题的详细聊天信息（包括回复）(TODO: replies以降序排列）
    GET /topics/1.json
    curl http://127.0.0.1:3000/topics/1.json
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


## 发表话题
    POST /topics.json
    curl -X POST -H "Content-Type:application/json" -H "x-token:1;liusong1111@gmail.com;d4b629a80934567e04530ebbd2fbe4e128e85ed0" http://127.0.0.1:3000/topics.json -d "{\"subject\":\"清蒸鲈鱼怎么做？\",\"body\":\"材料\n鲈鱼，葱，姜，料酒，李锦记蒸鱼豉油，油\n做法\n1.鲈鱼洗净，用葱姜料酒腌一会儿去腥。\n2.放蒸锅蒸８分钟，时间到不要开锅，再焐几分钟为好。\n3.把鱼取出，倒掉汁水。\n4.放上葱丝，浇上李锦记蒸鱼豉油，锅热油，再浇到鱼上。\",\"label_name\":\"美食\"}"
    #返回格式例如：
    HTTP STATUS: 201
    HTTP BODY:
    {"id":1,"subject":"清蒸鲈鱼怎么做？","body":"材料\n鲈鱼，葱，姜，料酒，李锦记蒸鱼豉油，油\n做法\n1.鲈鱼洗净，用葱姜料酒腌一会儿去腥。\n2.放蒸锅蒸８分钟，时间到不要开锅，再焐几分钟为好。\n3.把鱼取出，倒掉汁水。\n4.放上葱丝，浇上李锦记蒸鱼豉油，锅热油，再浇到鱼上。","label_name":"美食","user_id":1,"created_at":"2014-11-12T15:00:11.554Z","updated_at":"2014-11-12T15:00:11.554Z"}

## 修改话题
    PATCH/PUT /topics/1.json
    curl -X PUT -H "Content-Type:application/json" http://127.0.0.1:3000/topics/1.json -d "{\"subject\":\"清蒸鲈鱼怎么做？\",\"body\":\"蒸一下就好了\",\"label_name\":\"美食\"}"
    #返回格式例如：
    HTTP STATUS: 200
    HTTP BODY:
    {"id":1,"subject":"清蒸鲈鱼怎么做？","body":"蒸一下就好了","label_name":"美食","user_id":1,"created_at":"2014-11-12T15:00:11.000Z","updated_at":"2014-11-12T15:04:03.225Z"}

## 删除话题
    DELETE /topics/1.json
    curl -X DELETE -H "Content-Type:application/json" http://127.0.0.1:3000/topics/1.json
    返回格式例如：
    HTTP STATUS: 204

# 回复管理

## 得到某话题的回复列表
    curl http://127.0.0.1:3000/topics/2/replies.json
    # 返回格式例如：
    HTTP STATUS: 200
    HTTP BODY:
    [{"id":5,"topic_id":2,"body":"爱怎么做怎么做","user_id":null},{"id":6,"topic_id":2,"body":"爱怎么做怎么做","user_id":null},{"id":7,"topic_id":2,"body":"爱怎么做怎么做","user_id":null}]

## 发表回复
    curl -X POST -H "Content-Type:application/json" -H "x-token:1;liusong1111@gmail.com;d4b629a80934567e04530ebbd2fbe4e128e85ed0" http://127.0.0.1:3000/topics/1/replies.json -d "{\"body\":\"爱怎么做怎么做\"}"
    #返回格式例如：
    HTTP STATUS: 201
    HTTP BODY:
    {"id":2,"topic_id":2,"body":"爱怎么做怎么做","user_id":1,"created_at":"2014-11-12T15:14:22.924Z","updated_at":"2014-11-12T15:14:22.924Z"}

## 修改回复(可不实现)
    curl -X PUT -H "Content-Type:application/json" http://127.0.0.1:3000/topics/2/replies/4.json -d "{\"body\":\"这个好啊\"}"
    #返回格式例如：
    HTTP STATUS: 200
    HTTP BODY:
    {"id":4,"topic_id":2,"body":"这个好啊","user_id":null,"created_at":"2014-11-12T15:17:20.000Z","updated_at":"2014-11-12T15:22:58.212Z"}

## 删除回复(可不实现)
    curl -X DELETE -H "Content-Type:application/json" http://127.0.0.1:3000/topics/2/replies/4.json
    返回格式例如：
    HTTP STATUS: 204

# 好友

## 得到某人的好友申请列表
    curl http://127.0.0.1:3000/users/1/invitations.json

## 请求加好友
    curl -X POST http://127.0.0.1:3000/invitations.json -H "x-token:1;liusong1111@gmail.com;d4b629a80934567e04530ebbd2fbe4e128e85ed0" -d "invitee_id=2&reason=hello"

## 同意加好友
    # 其中1是invitation的id
    curl -X POST http://127.0.0.1:3000/invitations/1/agree.json

## 拒绝加好友
    # 其中1是invitation的id
    curl -X POST http://127.0.0.1:3000/invitations/1/refuse.json

## 删好友
    # 注： 2是好友ID
    curl -X DELETE  -H "x-token:1;liusong1111@gmail.com;d4b629a80934567e04530ebbd2fbe4e128e85ed0"  http://127.0.0.1:3000/friends/2.json

## 得到某人的好友列表
    curl http://127.0.0.1:3000/users/1/friends.json

