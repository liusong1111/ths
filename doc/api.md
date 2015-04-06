# 登录
    curl -i -X POST http://127.0.0.1:3000/login.json -d "email=liusong1111@gmail.com&password=aaaaaa"
    curl -i -X POST http://127.0.0.1:3000/login.json -d "email=wu@gmail.com&password=aaaaaa"
    # 返回格式例如
    HTTP STATUS: 200
    HTTP BODY:
    {"code":"ok","token":"1;liusong1111@gmail.com;d4b629a80934567e04530ebbd2fbe4e128e85ed0","user":{"labels":["美食","java"],"email":"liusong1111@gmail.com","sex":"male","phone":null,"city":"beijing","username":"sliu","huanxin_username":"4c36aba13d16f79ed79a29eec4bfbde0163e2d4f","id":1,"image":"http://10.0.2.2:3000/signs/1/0015.jpg","created_at":"2014-12-25 09:26:53","birth":"1980-01-01"}}
    #注：回复的body里，有token字段和huanxin_user字段
    #后续所有请求，请求的headers里，必须带一个头"x-token:收到的token"
    #huanxin_user是该用户在环信上的用户名
    如果登录失败，返回：
    HTTP STATUS: 200
    HTTP BODY:
    {"code":"fail","message":"用户名或密码不正确"}
    或者
    {"code":"fail","message":"您已被同行管理员封号，如有疑问请联系我们"}

# 忘记密码
    curl -X POST http://127.0.0.1:3000/forget_password.json -d "email=liusong1111@gmail.com"
    返回HTTP STATUS: 200
    请app提示用户，密码重置链接已发送到该邮箱，请查收邮件


# 用户管理

## 得到用户列表
    curl http://127.0.0.1:3000/users.json
    # 返回格式例如：
    HTTP STATUS: 200
    HTTP BODY:
    [{"labels":["美食","java"],"email":"liusong1111@gmail.com","sex":"male","phone":null,"city":"beijing","username":"sliu","huanxin_username":"4c36aba13d16f79ed79a29eec4bfbde0163e2d4f","id":1,"image":"http://10.0.2.2:3000/signs/1/0015.jpg","created_at":"2014-12-25 09:26:53","birth":"1980-01-01"},{"labels":["美食","java"],"email":"liusong1111@gmail.com","sex":"male","phone":null,"city":"beijing","username":"sliu","huanxin_username":"4c36aba13d16f79ed79a29eec4bfbde0163e2d4f","id":1,"image":"http://10.0.2.2:3000/signs/1/0015.jpg","created_at":"2014-12-25 09:26:53","birth":"1980-01-01"}]

## 标签下的用户
    curl http://127.0.0.1:3000/users.json?label_name=美食

## 参与话题的用户
      curl http://127.0.0.1:3000/users.json?topic_id=1

## 搜用户
    curl http://127.0.0.1:3000/users.json?q=美&page=1
    注：按username或label_name模糊搜索

## 查看用户信息
    curl http://127.0.0.1:3000/users/1.json
    HTTP STATUS: 200
    HTTP BODY:
    {"labels":["美食","java"],"email":"liusong1111@gmail.com","sex":"male","phone":null,"city":"beijing","username":"sliu","huanxin_username":"4c36aba13d16f79ed79a29eec4bfbde0163e2d4f","id":1,"image":"http://10.0.2.2:3000/signs/1/0015.jpg","created_at":"2014-12-25 09:26:53","birth":"1980-01-01"}

## 注册用户
    curl -X POST http://127.0.0.1:3000/users.json -F "image=@0015.jpg" -F "username=liusong" -F "password=aaaaaa" -F "email=liusong1111@gmail.com" -F "sex=male" -F "birth=1980-01-01" -F "city=beijing"
    curl -X POST http://127.0.0.1:3000/users.json -F "image=@0015.jpg" -F "username=wu" -F "password=aaaaaa" -F "email=wu@gmail.com" -F "sex=male" -F "birth=1980-01-01" -F "city=beijing"
    #返回格式例如：
    HTTP STATUS: 201
    HTTP BODY:
    {"labels":[],"email":"liusong1111@gmail.com","sex":"male","phone":null,"city":"beijing","username":"sliu1","huanxin_username":"4c36aba13d16f79ed79a29eec4bfbde0163e2d4f","id":1,"image":"http://10.0.2.2:3000/signs/1/0015.jpg","created_at":"2014-12-25 09:26:53","birth":"1980-01-01"}
    注: 本步server会在环信上注册帐号，用户名为对email的sha1编码。
    response里含huanxin_username字段
    如果注册出错，则返回类似
    {"code": "error", "message": "输入邮箱已存在"}
    可判断是否code值为"error"，如果是，提示message对应的值

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

## 搜索话题（根据标签或搜索关键字）
    按关键字：
    curl http://127.0.0.1:3000/topics.json?q=清蒸&page=1
    按标签：
    curl http://127.0.0.1:3000/topics.json?label_name=美食&page=1
    返回结果格式与上面的“得到话题列表"相同。

## 查看某用户的话题列表
    GET /topics.json?user_id=1&page=1
    curl http://127.0.0.1:3000/topics.json?user_id=1&page=1

## 查看某话题的详细聊天信息（包括回复、发件人信息）(TODO: replies以降序排列）
    GET /topics/1.json
    curl http://127.0.0.1:3000/topics/1.json
    返回
    {
      "user": {
        "labels": [],
        "email": "liusong1111@gmail.com",
        "sex": "male",
        "phone": null,
        "city": "beijing",
        "username": "liusong",
        "huanxin_username": "4c36aba13d16f79ed79a29eec4bfbde0163e2d4f",
        "id": 1,
        "image": "/signs/1/0015.jpg",
        "created_at": "2014-12-28 05:44:25",
        "birth": "1980-01-01"
      },
      "replies": [
        {
          "user": {
            "labels": [],
            "email": "wu@gmail.com",
            "sex": "male",
            "phone": null,
            "city": "beijing",
            "username": "wu",
            "huanxin_username": "0b1e2ab34877d89a38be262315ace928c4a6b764",
            "id": 2,
            "image": "/signs/2/0015.jpg",
            "created_at": "2014-12-28 05:49:09",
            "birth": "1980-01-01"
          },
          "created_at": "2014-12-28 05:58:57",
          "user_id": 2,
          "body": "爱怎么做怎么做",
          "topic_id": 1,
          "id": 1
        }
      ],
      "created_at": "2014-12-28 05:58:01",
      "huanxin_group_id": "1419746282317232",
      "user_id": 1,
      "label_name": "美食",
      "body": "材料\n鲈鱼，葱，姜，料酒，李锦记蒸鱼豉油，油\n做法\n1.鲈鱼洗净，用葱姜料酒腌一会儿去腥。\n2.放蒸锅蒸８分钟，时间到不要开锅，再焐几分钟为好。\n3.把鱼取出，倒掉汁水。\n4.放上葱丝，浇上李锦记蒸鱼豉油，锅热油，再浇到鱼上。",
      "subject": "清蒸鲈鱼怎么做？",
      "id": 1
    }


## 发表话题
    curl -X POST -H "Content-Type:application/json" -H "x-token:1;liusong1111@gmail.com;d4b629a80934567e04530ebbd2fbe4e128e85ed0" http://127.0.0.1:3000/topics.json -d "{\"subject\":\"清蒸鲈鱼怎么做？\",\"body\":\"材料\n鲈鱼，葱，姜，料酒，李锦记蒸鱼豉油，油\n做法\n1.鲈鱼洗净，用葱姜料酒腌一会儿去腥。\n2.放蒸锅蒸８分钟，时间到不要开锅，再焐几分钟为好。\n3.把鱼取出，倒掉汁水。\n4.放上葱丝，浇上李锦记蒸鱼豉油，锅热油，再浇到鱼上。\",\"label_name\":\"美食\"}"
    #返回格式例如：
    HTTP STATUS: 201
    HTTP BODY:
    {"id":1,"subject":"清蒸鲈鱼怎么做？","body":"材料\n鲈鱼，葱，姜，料酒，李锦记蒸鱼豉油，油\n做法\n1.鲈鱼洗净，用葱姜料酒腌一会儿去腥。\n2.放蒸锅蒸８分钟，时间到不要开锅，再焐几分钟为好。\n3.把鱼取出，倒掉汁水。\n4.放上葱丝，浇上李锦记蒸鱼豉油，锅热油，再浇到鱼上。","label_name":"美食","user_id":1,"created_at":"2014-11-12T15:00:11.554Z","updated_at":"2014-11-12T15:00:11.554Z"}


# 回复管理

## 得到某话题的回复列表
    curl http://127.0.0.1:3000/topics/2/replies.json
    # 返回格式例如：
    HTTP STATUS: 200
    HTTP BODY:
    [{"id":5,"topic_id":2,"body":"爱怎么做怎么做","user_id":null},{"id":6,"topic_id":2,"body":"爱怎么做怎么做","user_id":null},{"id":7,"topic_id":2,"body":"爱怎么做怎么做","user_id":null}]

## 发表回复
    curl -X POST -H "Content-Type:application/json" -H "x-token:2;wu@gmail.com;bd3a6915b964f219a06576d3a8efa390bdef4312" http://127.0.0.1:3000/topics/1/replies.json -d "{\"body\":\"爱怎么做怎么做\"}"
    #返回格式例如：
    HTTP STATUS: 201
    HTTP BODY:
    {"id":2,"topic_id":2,"body":"爱怎么做怎么做","user_id":1,"created_at":"2014-11-12T15:14:22.924Z","updated_at":"2014-11-12T15:14:22.924Z"}


# 好友

## 得到某人的好友申请列表
    curl http://127.0.0.1:3000/users/2/invitations.json

    response:
    [
      {
        "inviter": {
          "labels": [
            "美食",
            "java"
          ],
          "email": "liusong1111@gmail.com",
          "sex": "male",
          "phone": null,
          "city": "beijing",
          "username": "liusong",
          "huanxin_username": "4c36aba13d16f79ed79a29eec4bfbde0163e2d4f",
          "id": 1,
          "image": "/signs/1/0015.jpg",
          "created_at": "2014-12-28 05:44:25",
          "birth": "1980-01-01"
        },
        "created_at": "2015-01-10 14:04:54",
        "status": "pending",
        "reason": "hello",
        "invitee_id": 2,
        "inviter_id": 1,
        "id": 1
      }
    ]

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
    curl http://127.0.0.1:3000/friends.json  -H "x-token:1;liusong1111@gmail.com;d4b629a80934567e04530ebbd2fbe4e128e85ed0"
    返回：
    [
      {
        "labels": [],
        "email": "wu@gmail.com",
        "sex": "male",
        "phone": null,
        "city": "beijing",
        "username": "wu",
        "huanxin_username": "0b1e2ab34877d89a38be262315ace928c4a6b764",
        "id": 2,
        "image": "/signs/2/0015.jpg",
        "created_at": "2014-12-28 05:49:09",
        "birth": "1980-01-01"
      }
    ]

# 首页

## 找签客首页
    curl http://127.0.0.1:3000/recommendations.json?page=1
    response:
    [
      {
        "labels": [],
        "email": "wu1@gmail.com",
        "sex": "male",
        "phone": null,
        "city": "beijing",
        "username": "wu1",
        "type": "user",
        "huanxin_username": "66d71b5d88f8f1cdbf1f2a57cb2b048b49ec99a2",
        "id": 3,
        "image": "/signs/3/0015.jpg",
        "created_at": "2014-12-28 08:53:51",
        "birth": "1980-01-01",
        "is_friend": false
      },
      {
        "labels": [],
        "email": "wu@gmail.com",
        "sex": "male",
        "phone": null,
        "city": "beijing",
        "username": "wu",
        "type": "user",
        "huanxin_username": "0b1e2ab34877d89a38be262315ace928c4a6b764",
        "id": 2,
        "image": "/signs/2/0015.jpg",
        "created_at": "2014-12-28 05:49:09",
        "birth": "1980-01-01",
        "is_friend": false
      },
      {
        "labels": [
          "美食",
          "java"
        ],
        "email": "liusong1111@gmail.com",
        "sex": "male",
        "phone": null,
        "city": "beijing",
        "username": "liusong",
        "type": "user",
        "huanxin_username": "4c36aba13d16f79ed79a29eec4bfbde0163e2d4f",
        "id": 1,
        "image": "/signs/1/0015.jpg",
        "created_at": "2014-12-28 05:44:25",
        "birth": "1980-01-01",
        "is_friend": false
      },
      {
        "type": "topic",
        "user": {
          "labels": [],
          "email": "liusong1111@gmail.com",
          "sex": "male",
          "phone": null,
          "city": "beijing",
          "username": "liusong",
          "huanxin_username": "4c36aba13d16f79ed79a29eec4bfbde0163e2d4f",
          "id": 1,
          "image": "/signs/1/0015.jpg",
          "created_at": "2014-12-28 05:44:25",
          "birth": "1980-01-01"
        },
        "created_at": "2014-12-28 05:58:01",
        "huanxin_group_id": "1419746282317232",
        "user_id": 1,
        "label_name": "美食",
        "body": "材料\n鲈鱼，葱，姜，料酒，李锦记蒸鱼豉油，油\n做法\n1.鲈鱼洗净，用葱姜料酒腌一会儿去腥。\n2.放蒸锅蒸８分钟，时间到不要开锅，再焐几分钟为好。\n3.把鱼取出，倒掉汁水。\n4.放上葱丝，浇上李锦记蒸鱼豉油，锅热油，再浇到鱼上。",
        "subject": "清蒸鲈鱼怎么做？",
        "id": 1
      }
    ]


## 来信-环信用户/组列表兑换系统列表
    curl -X POST http://127.0.0.1:3000/huanxin/hid2sids.json -H "Content-Type:application/json" -d '{"entries": [{"username":"4c36aba13d16f79ed79a29eec4bfbde0163e2d4f","is_group":false},{"username":"0b1e2ab34877d89a38be262315ace928c4a6b764","is_group":false},{"username":"1419746282317232","is_group":true}]}'
    response:
    [
      {
        "labels": [],
        "email": "liusong1111@gmail.com",
        "sex": "male",
        "phone": null,
        "city": "beijing",
        "username": "liusong",
        "type": "user",
        "huanxin_username": "4c36aba13d16f79ed79a29eec4bfbde0163e2d4f",
        "id": 1,
        "image": "/signs/1/0015.jpg",
        "created_at": "2014-12-28 05:44:25",
        "birth": "1980-01-01"
      },
      {
        "labels": [],
        "email": "wu@gmail.com",
        "sex": "male",
        "phone": null,
        "city": "beijing",
        "username": "wu",
        "type": "user",
        "huanxin_username": "0b1e2ab34877d89a38be262315ace928c4a6b764",
        "id": 2,
        "image": "/signs/2/0015.jpg",
        "created_at": "2014-12-28 05:49:09",
        "birth": "1980-01-01"
      },
      {
        "type": "topic",
        "label_name": "美食",
        "replies": [
          {
            "created_at": "2014-12-28 05:58:57",
            "user_id": 2,
            "body": "爱怎么做怎么做",
            "topic_id": 1,
            "id": 1
          }
        ],
        "id": 1,
        "user_id": 1,
        "body": "材料\n鲈鱼，葱，姜，料酒，李锦记蒸鱼豉油，油\n做法\n1.鲈鱼洗净，用葱姜料酒腌一会儿去腥。\n2.放蒸锅蒸８分钟，时间到不要开锅，再焐几分钟为好。\n3.把鱼取出，倒掉汁水。\n4.放上葱丝，浇上李锦记蒸鱼豉油，锅热油，再浇到鱼上。",
        "user": {
          "labels": [],
          "email": "liusong1111@gmail.com",
          "sex": "male",
          "phone": null,
          "city": "beijing",
          "username": "liusong",
          "huanxin_username": "4c36aba13d16f79ed79a29eec4bfbde0163e2d4f",
          "id": 1,
          "image": "/signs/1/0015.jpg",
          "created_at": "2014-12-28 05:44:25",
          "birth": "1980-01-01"
        },
        "subject": "清蒸鲈鱼怎么做？",
        "created_at": "2014-12-28 05:58:01",
        "huanxin_group_id": "1419746282317232"
      }
    ]


## 发送用户反馈意见

    curl -X POST http://127.0.0.1:3000/feedbacks.json -H "x-token:1;liusong1111@gmail.com;d4b629a80934567e04530ebbd2fbe4e128e85ed0"  -H "Content-Type:application/json" -d '{"content":"就是任性"}'

    response:
    {"created_at":"2015-01-10 13:53:31","content":"就是任性","user_id":1,"id":1}
    
## 得到系统全局配置
    curl http://127.0.0.1:3000/system_config.json
    得到
    {
    "can_upgrade_silently": false,
    "can_login": true,
    "can_register_user": true,
    "id": 1
    }
    

## 由后台管理平台向api平台发消息，让api平台发送极光推送
    curl -X GET "http://127.0.0.1:3000/push.json?user_id=1&content=您被管理员封号&secret=axced79c4ab182d725ec2ff15"
    response:
    {"code": "ok"}
    或
    {"code" "fail", "message": "secret不正确"}
    
    其中，user_id是users表中的id字段；content是要推送的正文;secret是我们为了安全起见与后台管理平台约定好的密码。
    "您被同行管理员封号，如有疑问请联系我们"
    "您被同行管理员解封，如继续使用"
    "您被同行管理员删除，如有疑问请联系我们"
    
    


