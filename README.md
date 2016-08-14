# lein-binplus

A Leiningen plugin for producing standalone console executables that
work on OS X, Linux, and Windows.

It basically just takes your uberjar and stuffs it in another file
with some fancy magical execution stuff.

**NOTE: This is a fork of
[Raynes/lein-bin](https://github.com/Raynes/lein-bin)
with the addition of several bug-fixes and improvements.**

I've initially send a [pull request](https://github.com/Raynes/lein-bin/pull/26)
but after few months which I've been waiting, I felt that the project was
neglected and decided to push my own plugin.

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
  [[lein-binplus "0.4.2"]]}}

```

To install it in a single project add it in your `project.clj` as follow:

``` clojure
  :profiles {:uberjar {:aot :all}
             :dev {:plugins [[lein-binplus "0.4.2"]]}}

```

Your project needs to have a `:main` field specifying the namespace
that contains your `-main` function.  If you have that, just run `lein
bin` and it'll produce a standalone executable for your project. Note
that your main namespace currently needs to be AOT compiled (it just
needs to have `:gen-class` specified in its `ns` declaration).

You can also supply a `:bin` key like so:

        :bin {:name "runme"
              :bin-path "~/bin"
              :bootclasspath true
              :jvm-opts ["-server" "$JVM_OPTS" "-Dfile.encoding=utf-8"]}

  * `:name`: Name the file something other than `project-version`
  * `:bin-path`: If specified, also copy the file into `bin-path`,
    which is presumably on your $PATH.
  * `:bootclasspath`: Supply the uberjar to java via
    `-Xbootclasspath/a` instead of `-jar`.  Sometimes this can speed
    up execution, but may not work with all classloaders.
  * `:jvm-opts`: If specified, supply the Java options to be used in
    the executable jar. When present it overrides the project's
    `:jvm-opts`. It support environment variables as well.

## License

The original plugin Copyright (C) 2012 Anthony Grimes, Justin Balthrop, Jason Whitlark

This enhanced version Copyright (C) 2016 Bruno Bonacci

Distributed under the Eclipse Public License, the same as Clojure.
