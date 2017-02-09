# lein-binplus

A Leiningen plugin for producing standalone console executables that
work on OS X, Linux, and Windows.

It basically just takes your uberjar and stuffs it in another file
with some fancy magical execution stuff.

**NOTE: This is a fork of
[Raynes/lein-bin](https://github.com/Raynes/lein-bin)
with the addition of several bug-fixes and improvements.**

Sadly, [@Raynes passed away last December (2016)](https://cemerick.com/2016/12/07/rip-anthony-grimes/)
therefore I decided to fork the project and carry on the development in my own copy.

It is compatible with the original and it is a *drop-in replacement* for it,
you just need to update the dependency.

## Latest version

[![Clojars Project](https://img.shields.io/clojars/v/lein-binplus.svg)](https://clojars.org/lein-binplus)

## Usage

This is a leiningen plugin. If you're using lein 1, run `lein plugin
install lein-binplus <current-version>` to install it.  If you're using
lein 2, add the plugin to your default profile in
`~/.lein/profiles.clj`.

To install it in your `profile.clj` just add the dependency like:

``` clojure
{:user
 {:plugins
  [[lein-binplus "0.6.1"]]}}

```

To install it in a single project add it in your `project.clj` as follow:

``` clojure
  :profiles {:uberjar {:aot :all}
             :dev {:plugins [[lein-binplus "0.6.1"]]}}

```

Your project needs to have a `:main` field specifying the namespace
that contains your `-main` function.  If you have that, just run `lein
bin` and it'll produce a standalone executable for your project. Note
that your main namespace currently needs to be AOT compiled (it just
needs to have `:gen-class` specified in its `ns` declaration).

You can also supply a `:bin` key like so:

        ;; add this in your project
        :bin {:name "runme"
              :bin-path "~/bin"
              :bootclasspath false
              :jvm-opts ["-server" "-Dfile.encoding=utf-8" "$JVM_OPTS" ]}

  * `:name`: Name the file something other than `project-version`
  * `:bin-path`: If specified, also copy the file into `bin-path`,
    which is presumably on your $PATH.
  * `:bootclasspath`: Supply the uberjar to java via
    `-Xbootclasspath/a` instead of `-jar`.  Sometimes this can speed
    up execution, but may not work with all classloaders.
  * `:jvm-opts`: If specified, supply the Java options to be used in
    the executable jar. When present it overrides the project's
    `:jvm-opts`. It support environment variables as well.

## Advanced use.

This plugin works by adding a custom script at the beginning of a JAR file
as explained in [this blog post](http://skife.org/java/unix/2011/06/20/really_executable_jars.html).

The default "preamble" assumes that you have java in your PATH and that you wish
to run your executable jar across multiple platforms (Linux, OSX and Windows).

However if you need a specific set of options or a different preamble you
can add a `:custom-preamble` string in your `:bin` section of your `project.clj`.


        ;; specify a custom preamble
        :bin {:name "runme"
              :custom-preamble "#!/bin/sh\nexec java {{{jvm-opts}}} -jar $0 \"$@\"\n"}

The custom preamble contains a placeholder `{{{jvm-opts}}}` which will be replaced
at creation time with the `:jvm-opts` specified in your project.

Here a list of possible substitutions:

  - `{{{project-name}}}` - the name specified in your `project.clj`
  - `{{{version}}}` - the version specified in your `project.clj`
  - `{{{main}}}` - the main namespace as of `:main` in your `project.clj`
  - `{{{jvm-opts}}}` - the `:jvm-opts` specified in your `:bin` section,
     if present, or the `:jvm-opts` of your `project.clj`.
     The default options are: `["-server" "-Dfile.encoding=UTF-8"]`
     This might contains environment variables in the form of:
     `$VAR1` like `$HOME`.
  - `{{{win-jvm-opts}}}` - is a Windows compatible version of `{{{jvm-opts}}}`.
     This mainly contains environment variable in the Windows format.
     For example `$HOME` becomes `%HOME%`.

## License

The original plugin Copyright (C) 2012 Anthony Grimes, Justin Balthrop, Jason Whitlark

This enhanced version Copyright (C) 2016 Bruno Bonacci

Distributed under the Eclipse Public License, the same as Clojure.
