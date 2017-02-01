(defproject lein-binplus "0.5.0-SNAPSHOT"
  :description "A leiningen plugin for generating standalone console
  executables for your project."
  :url "https://github.com/BrunoBonacci/lein-binplus"

  :scm {:name "github"
        :url "https://github.com/BrunoBonacci/lein-binplus"}

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[me.raynes/fs "1.4.6"]]
  :eval-in-leiningen true

  :deploy-repositories [["releases" :clojars]
                        ["snapshots" :clojars]])
