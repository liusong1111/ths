# TODO
  * response json format
  * auth token

# 登录(TODO: user.labels;; 用邮箱登录)
    curl -i -X POST  -H "Content-Type:application/json" http://127.0.0.1:3000/login.json -d "{\"username\":\"sliu\",\"password\":\"aaaaaa\"}"
    # 返回格式例如
    HTTP STATUS: 200
    HTTP BODY:
    {"code":"ok","user":{"id":2,"username":"sliu","password":"aaaaaa","email":"liusong1111@gmail.com","phone":"15522602848","created_at":"2014-11-12T14:33:27.000Z","updated_at":"2014-11-12T14:33:27.000Z","labels":["美食","java1"]}}
# 用户管理

## 得到用户列表(TODO: labels)
    curl http://127.0.0.1:3000/users.json
    # 返回格式例如：
    HTTP STATUS: 200
    HTTP BODY:
    [{"id":2,"username":"sliu","password":"aaaaaa","email":"liusong1111@gmail.com","phone":"15522602848","created_at":"2014-11-12T14:33:27.000Z","updated_at":"2014-11-12T14:33:27.000Z","labels":["美食","java1"]}]

## 注册用户(TODO: last-insert-row-id:2)
    POST /users.json
    curl -X POST -H "Content-Type:application/json" http://127.0.0.1:3000/users.json -d "{\"username\":\"sliu\",\"password\":\"aaaaaa\",\"email\":\"liusong1111@gmail.com\",\"phone\":\"15522602848\"}"
    #返回格式例如：
    HTTP STATUS: 201
    HTTP BODY:
    {"id":1,"username":"sliu","password":"aaaaaa","email":"liusong1111@gmail.com","phone":"15522602848","created_at":"2014-11-12T14:36:51.722Z","updated_at":"2014-11-12T14:36:51.722Z"}

## 修改用户信息
    PATCH/PUT /users/1.json
    curl -X PUT -H "Content-Type:application/json" http://127.0.0.1:3000/users/1.json -d "{\"username\":\"sliu\",\"password\":\"aaaaaa\",\"email\":\"liusong1111@gmail.com\",\"phone\":\"15522602849\"}"
    #返回格式例如：
    HTTP STATUS: 200
    HTTP BODY:
    {"id":1,"username":"sliu","password":"aaaaaa","email":"liusong1111@gmail.com","phone":"15522602849","created_at":"2014-11-12T14:36:51.000Z","updated_at":"2014-11-12T14:37:45.166Z"}

## 设置某人的标签
    curl -X PUT -H "Content-Type:application/json" http://127.0.0.1:3000/users/2/update_labels.json -d "{\"labels\": [\"美食\",\"java\"]}"
    #返回格式例如：
    HTTP STATUS: 201
    HTTP BODY:
    {"id":2,"username":"sliu","password":"aaaaaa","email":"liusong1111@gmail.com","phone":"15522602848","created_at":"2014-11-12T14:33:27.000Z","updated_at":"2014-11-12T14:33:27.000Z","labels":["美食","java"]}


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

## 新建标签
    POST /labels.json
    curl -X POST -H "Content-Type:application/json" http://127.0.0.1:3000/labels.json -d "{\"label_name\":\"美食\"}"
    #返回格式例如：
    HTTP STATUS: 201
    HTTP BODY:
    {"id":1,"label_name":"美食","created_at":"2014-11-12T14:51:13.597Z","updated_at":"2014-11-12T14:51:13.597Z"}

## 删除标签(重要：只有管理员才有权限)
    DELETE /labels/美食.json
    curl -X DELETE -H "Content-Type:application/json" http://127.0.0.1:3000/labels/美食.json
    返回格式例如：
    HTTP STATUS: 204




# 主题管理

## 得到话题列表
    GET /topics.json
    curl http://127.0.0.1:3000/topics.json
    # 返回格式例如：
    HTTP STATUS: 200
    HTTP BODY:
    [{"id":1,"subject":"清蒸鲈鱼怎么做？","body":"材料\n鲈鱼，葱，姜，料酒，李锦记蒸鱼豉油，油\n做法\n1.鲈鱼洗净，用葱姜料酒腌一会儿去腥。\n2.放蒸锅蒸８分钟，时间到不要开锅，再焐几分钟为好。\n3.把鱼取出，倒掉汁水。\n4.放上葱丝，浇上李锦记蒸鱼豉油，锅热油，再浇到鱼上。","label_name":"美食","creator_id":1,"url":"http://127.0.0.1:3000/topics/1.json"}]

## 发表话题
    POST /topics.json
    curl -X POST -H "Content-Type:application/json" http://127.0.0.1:3000/topics.json -d "{\"subject\":\"清蒸鲈鱼怎么做？\",\"body\":\"材料\n鲈鱼，葱，姜，料酒，李锦记蒸鱼豉油，油\n做法\n1.鲈鱼洗净，用葱姜料酒腌一会儿去腥。\n2.放蒸锅蒸８分钟，时间到不要开锅，再焐几分钟为好。\n3.把鱼取出，倒掉汁水。\n4.放上葱丝，浇上李锦记蒸鱼豉油，锅热油，再浇到鱼上。\",\"label_name\":\"美食\"}"
    #返回格式例如：
    HTTP STATUS: 201
    HTTP BODY:
    {"id":1,"subject":"清蒸鲈鱼怎么做？","body":"材料\n鲈鱼，葱，姜，料酒，李锦记蒸鱼豉油，油\n做法\n1.鲈鱼洗净，用葱姜料酒腌一会儿去腥。\n2.放蒸锅蒸８分钟，时间到不要开锅，再焐几分钟为好。\n3.把鱼取出，倒掉汁水。\n4.放上葱丝，浇上李锦记蒸鱼豉油，锅热油，再浇到鱼上。","label_name":"美食","creator_id":1,"created_at":"2014-11-12T15:00:11.554Z","updated_at":"2014-11-12T15:00:11.554Z"}

## 修改话题
    PATCH/PUT /topics/1.json
    curl -X PUT -H "Content-Type:application/json" http://127.0.0.1:3000/topics/1.json -d "{\"subject\":\"清蒸鲈鱼怎么做？\",\"body\":\"蒸一下就好了\",\"label_name\":\"美食\"}"
    #返回格式例如：
    HTTP STATUS: 200
    HTTP BODY:
    {"id":1,"subject":"清蒸鲈鱼怎么做？","body":"蒸一下就好了","label_name":"美食","creator_id":1,"created_at":"2014-11-12T15:00:11.000Z","updated_at":"2014-11-12T15:04:03.225Z"}

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
    curl -X POST -H "Content-Type:application/json" http://127.0.0.1:3000/topics/2/replies.json -d "{\"body\":\"爱怎么做怎么做\"}"
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

