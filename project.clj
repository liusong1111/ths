(defproject ths "0.1.0-SNAPSHOT"
            :description "TongHang Server"
            :url "http://example.com/FIXME"
            :min-lein-version "2.0.0"
            :dependencies [[org.clojure/clojure "1.6.0"]
                           [compojure "1.3.1"]
                           [ring/ring-defaults "0.1.2"]
                           ;[ring/ring-json "0.3.1"]
                           [ring-middleware-format "0.4.0"]
                           [http-kit "2.1.16"]
                           [korma "0.4.0"]
                           [cheshire "5.3.1"]
                           ;[mysql/mysql-connector-java "5.1.26"]
                           [org.xerial/sqlite-jdbc "3.7.2"]
                           [clj-postgresql "0.4.0"]
                           [com.taoensso/timbre "3.3.1"]
                           [com.postspectacular/rotor "0.1.0"]
                           [commons-io/commons-io "2.4"]
                           [pandect "0.4.1"]
                           [org.clojure/data.codec "0.1.0"]
                           [http-kit "2.1.16"]
                           [cheshire "5.3.1"]
                           [clj-time "0.8.0"]
                           [com.draines/postal "1.11.3"]
                           [metosin/compojure-api "0.17.0"]
                           [metosin/ring-http-response "0.5.2"]
                           [metosin/ring-swagger-ui "2.0.24"]
                           [crypto-random "1.2.0"]
                           [cn.jpush.api/jpush-client "3.2.3"]
                           ]
            :main "ths.jpush"
            :plugins [
                      [lein-ring "0.8.13"]
                      [lein-bin "0.3.4"]
                      ]
            :ring {:handler ths.routes/app
                   :init    ths.routes/init
                   :destroy ths.routes/destroy
                   }
            :profiles
            {:uberjar {:aot :all}
             :dev     {:dependencies [[javax.servlet/servlet-api "2.5"]
                                      [ring-mock "0.1.5"]]}})
