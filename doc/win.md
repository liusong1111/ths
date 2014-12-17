# 安装lein
从下面链接下载 leiningen-win-installer 1.0,双击安装。

[http://leiningen-win-installer.djpowell.net/]

参考上述网页的截图，安装路径保留默认的，JDK路径确认选的正确，点安装。

# 墙 墙 墙！！！
由于lein依赖的leiningen-2.5.0-standalone.jar需要从amazon上下载，被强了。请从我这儿索要该jar包，把它复制到lein安装目录的self-installs下，如：

    c:\Users\你的用户名\.lein\self-installs\


# 验证lein是否安装成功
执行cmd命令，在命令行下执行：

    lein repl

进入clojure交互界面，输入代码：

    (+ 2 2)

回车，如果显示结果 4 ，表示成功。输入：

    exit

退出lein交互界面。

# 从github上下载代码

# 执行代码
cmd下，依次执行：

    cd 代码根目录
    lein ring server-headless

它第一次运行会下载依赖包，较慢，最后命令行停在 已监听3000端口 这个提示上。
打开浏览器(建议用chrome浏览器)，访问：
[http://127.0.0.1:3000/api/users]

正常应显示一个json串



