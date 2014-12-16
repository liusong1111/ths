# 数据库初始化

    rm ths.db
    sqlite3 ths.db
    .read db/schema.sql
    .quit

# 运行

    lein ring server-headless

# 生产环境运行

    #打包
    lein production ring uberjar
    #执行
    java -jar target/ths-0.1.0-SNAPSHOT-standalone.jar
