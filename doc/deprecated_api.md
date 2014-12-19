# 本文档描述已经删除的api

## (不要了)新建标签（不要了，因为现在的标签是用户自己输入进去的，根本没有选择界面，不需要管理员去创建标签。标签都是用户输入后，系统发现之前没有就创建一个）
    POST /labels.json
    curl -X POST -H "Content-Type:application/json" http://127.0.0.1:3000/labels.json -d "{\"label_name\":\"美食\"}"
    #返回格式例如：
    HTTP STATUS: 201
    HTTP BODY:
    {"id":1,"label_name":"美食","created_at":"2014-11-12T14:51:13.597Z","updated_at":"2014-11-12T14:51:13.597Z"}

## 删除标签（不要了，因为现在的标签是用户自己输入进去的，根本没有选择界面，不需要管理员去创建标签。标签都是用户输入后，系统发现之前没有就创建一个）
    DELETE /labels/美食.json
    curl -X DELETE -H "Content-Type:application/json" http://127.0.0.1:3000/labels/美食.json
    返回格式例如：
    HTTP STATUS: 204
