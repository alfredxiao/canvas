(defproject coverlet "0.1.8"
  :description "Clojure plugin for leiningen that reports on test coverage."
  :url "https://github.com/alfredxiao/coverlet"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/tools.reader "1.3.2"]
                 [org.clojure/tools.namespace "0.3.1"]]
  :profiles {:dev {:dependencies [[org.clojure/tools.trace "0.7.10"]]}}
  :deploy-repositories {"clojars" {:url "https://clojars.org/repo"
                                   :sign-releases false}}
  :eval-in-leiningen true
  :repl-options {:init-ns coverlet.core})
