(defproject ilcdb-clj "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [com.novemberain/monger "3.1.0"]
                 [clojure.java-time "0.3.2"]
                 [javax.servlet/servlet-api "2.5"]
                 [ring "1.7.1"]
                 [compojure "1.6.1"]]
  :min-lein-version "2.0.0"
  :uberjar-name "ilcdb-clj.jar"
  :main ilcdb-clj.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:main ilcdb-clj.core/-dev-main}})
