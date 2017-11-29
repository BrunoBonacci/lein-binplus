(ns leiningen.bin
  "Create a standalone executable for your project."
  (:require [clojure.java.io :as io]
            [clostache.parser :refer [render]]
            [leiningen.uberjar :refer [uberjar]]
            [me.raynes.fs :as fs]
            [clojure.string :as str]))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;                                                                            ;;
;;                      ---==| T E M P L A T E S |==----                      ;;
;;                                                                            ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def BOOTCLASSPATH-TEMPLATE
  ":;exec java {{{jvm-opts}}} -D{{{project-name}}}.version={{{version}}} -Xbootclasspath/a:$0 {{{main}}} \"$@\"\n@echo off\r\njava {{{win-jvm-opts}}} -D{{{project-name}}}.version={{{version}}} -Xbootclasspath/a:\"%~f0\" {{{main}}} %*\r\ngoto :eof\r\n")

(def NORMAL-TEMPLATE
  ":;exec java {{{jvm-opts}}} -D{{{project-name}}}.version={{{version}}} -jar $0 \"$@\"\n@echo off\r\njava {{{win-jvm-opts}}} -D{{{project-name}}}.version={{{version}}} -jar \"%~f0\" %*\r\ngoto :eof\r\n")

(def LEIN-JVM-OPTS ["-XX:+TieredCompilation" "-XX:TieredStopAtLevel=1" "-XX:-OmitStackTraceInFastThrow"])



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;                                                                            ;;
;;                       ---==| P R E A M B L E |==----                       ;;
;;                                                                            ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn preamble-template
  [{:keys [bootclasspath custom-preamble] :as options}]
  (cond
    custom-preamble (str custom-preamble "\r\n")
    bootclasspath   BOOTCLASSPATH-TEMPLATE
    :else           NORMAL-TEMPLATE))


(defn render-preamble
  [template {:keys [main project-name version jvm-opts] :as opts}]
  (-> (render template opts)
      (str/replace #"\\\$" "\\$")))


(defn jvm-opts [project]
  (str/join " "
            (or (get-in project [:bin :jvm-opts])
               (if (= (:jvm-opts project) LEIN-JVM-OPTS)
                 ["-server -Dfile.encoding=UTF-8"]
                 (:jvm-opts project)))))

(defn- sanitize-jvm-opts-for-win
  "turns linux style vars \"$FOO\" into win style \"%FOO\"."
  [opts]
  (str/replace opts #"\$([a-zA-Z0-9_]+)" "%$1%"))


(defn options [project]
  {:project-name    (:name project)
   :version         (:version project)

   :main            (:main project)
   :bootclasspath   (get-in project [:bin :bootclasspath] false)
   :jvm-opts        (jvm-opts project)
   :win-jvm-opts    (sanitize-jvm-opts-for-win (jvm-opts project))
   :custom-preamble (get-in project [:bin :custom-preamble])
   })


(defn preamble
  [opts]
  (-> (preamble-template opts)
      (render-preamble opts)))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;                                                                            ;;
;;                         ---==| B I N A R Y |==----                         ;;
;;                                                                            ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn- write-preamble [out ^String preamble]
  (.write out (.getBytes preamble)))


(defn writing-bin [binfile uberjar preamble]
  (println "Creating standalone executable:" (str binfile))
  (io/make-parents binfile)
  (with-open [bin (io/output-stream binfile)]
    (write-preamble bin preamble)
    (io/copy (fs/file uberjar) bin))
  (fs/chmod "+x" binfile))


(defn- copy-bin [project binfile]
  (when-let [bin-path (get-in project [:bin :bin-path])]
    (let [bin-path (fs/expand-home bin-path)
          new-binfile (fs/file bin-path (fs/base-name binfile))]
      (println "Copying binary to" bin-path)
      (fs/chmod "+x" (fs/copy+ binfile new-binfile)))))



(defn bin
  "Create a standalone console executable for your project.

  Add :main to your project.clj to specify the namespace that contains your
  -main function."
  [{:keys [main] :as project}]
  (if-not main
    (println "Cannot create bin without :main namespace in project.clj")
    (let [opts    (options project)
          binfile (fs/file (fs/file (:target-path project))
                           (or (get-in project [:bin :name])
                              (str (:name project) "-" (:version project))))
          uberjar (uberjar project)]
      (writing-bin binfile uberjar (preamble opts))
      (copy-bin project binfile))))
