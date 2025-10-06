## What is it?

Getdown (yes, it's the funky stuff) is a system for deploying Java applications to end-user
computers, as well as keeping those applications up to date.

It was designed as a replacement
for [Java Web Start](https://docs.oracle.com/javase/8/docs/technotes/guides/javaws/)
due to limitations in Java Web Start's architecture which are outlined in the
[rationale](https://github.com/bekoenig/getdown/wiki/Rationale) section.

Note: Getdown was designed *in 2004* as an alternative to Java Web Start, because of design choices
made by JWS that were problematic to the use cases its authors had. It is _not_ a drop-in
replacement for JWS, aimed to help the developers left in the lurch by the deprecation of JWS in
Java 9. It may still be a viable alternative for developers looking to replace JWS, but don't
expect to find feature parity with JWS.

## How do I use it?

A tutorial and more detailed specification are available from the [Documentation] page.

Note that because one can not rely on users having a JRE installed, you must create a custom
installer for each platform that you plan to support (Windows, macOS, Linux) that installs a JRE,
the Getdown launcher jar file, a stub configuration file that identifies the URL at which your real
app manifest is hosted, and whatever the appropiate "desktop integration" is that provides an icon
the user can click on. We have some details on the
[installers](https://github.com/bekoenig/getdown/wiki/Installers) documentation page, though it
is unfortunately not very detailed.

## How does it work?

The main design and operation of Getdown is detailed on the
[design](https://github.com/bekoenig/getdown/wiki/Design) page. You can also browse the
[javadoc documentation] and [source code] if you're interested in implementation details.

## Where can I see it in action?

Getdown was originally written by developers at [OOO](https://en.wikipedia.org/wiki/Three_Rings_Design) for the deployment of their Java-based
massively multiplayer games.

Getdown is implemented in Java, and is designed to deploy and update JVM-based applications. While
it would be technically feasible to use Getdown to deploy non-JVM-based applications, it is not
currently supported and it is unlikely that the overhead of bundling a JVM just to run Getdown
would be worth it if the JVM were not also being used to run the target application.

## Release notes

See [CHANGELOG.md](CHANGELOG.md) for release notes.

## Obtaining Getdown

Getdown will likely need to be integrated into your build. We have separate instructions for
[build integration]. You can also download the individual jar files from Maven Central if needed.
Getdown is comprised of three Maven artifacts (jar files), though you probably only need the first
one:

* [getdown-launcher](https://mvnrepository.com/artifact/io.github.bekoenig.getdown/getdown-launcher)
  contains code that you actually run to update and launch your app. It also contains the tools
* needed to build a Getdown app distribution.

* [getdown-core](https://mvnrepository.com/artifact/io.github.bekoenig.getdown/getdown-core) contains the
  core logic for downloading, verifying, patching and launching an app as well as the core logic
  for creating an app distribution. It does not contain any user interface code. You would only
  use this artifact if you were planning to integrate Getdown directly into your app.

* [getdown-ant](https://mvnrepository.com/artifact/io.github.bekoenig.getdown/getdown-ant) contains an Ant
  task for building a Getdown app distribution. See the [build integration] instructions for
  details.

You can also:

* [Check out the code](https://github.com/bekoenig/getdown) and build it yourself.
* Browse the [source code] online.
* View the [javadoc documentation] online.

## JVM Version Requirements

* Getdown version 2.0.x requires Java 8 VM or newer.

## Migrating from Getdown 1.8 to Getdown 2.0

See [this document](https://github.com/bekoenig/getdown/wiki/Migrating-from-1.8-to-2.0) on the
changes needed to migrate from Getdown 1.8 to 2.0.

## Building

Getdown is built with Maven in the standard ways. Invoke the following commands, for fun and
profit:

```
% mvn compile  # builds the classes
% mvn test     # builds and runs the unit tests
% mvn package  # builds and creates jar file
% mvn install  # builds, jars and installs in your local Maven repository
```

## Discussion

Feel free to pop over in the [GitHub project discussions](https://github.com/bekoenig/getdown/discussions) to ask questions and get (and give) answers.

[Documentation]: https://github.com/bekoenig/getdown/wiki
[source code]: https://github.com/bekoenig/getdown/tree/master/src/main/java/com/threerings/getdown/launcher
[javadoc documentation]: https://github.com/bekoenig/getdown/apidocs/
[build integration]: https://github.com/bekoenig/getdown/wiki/Build-Integration
