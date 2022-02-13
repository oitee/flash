(defproject flash "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [com.github.seancorfield/next.jdbc "1.2.761"]
                 [org.postgresql/postgresql "42.2.10"]
                 [ring/ring-core "1.9.5"]
                 [ring/ring-jetty-adapter "1.9.5"]
                 [compojure "1.6.2"]
                 [org.clojure/data.json "2.4.0"]
                 [clojure.java-time "0.3.3"]]
  :repl-options {:init-ns flash.core}
  :main flash.core)
