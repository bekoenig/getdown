# Getdown Releases

## 2.0.1 -
* Introduction of ProxyVole dependency library as default proxy detection, old usage of JRegistryKey
  kept, require setting property `use_proxy` to value `legacy`


## 2.0.0 - May 01, 2024

* GroupId and package root have moved from `com.threerings.getdown` to `io.github.bekoenig.getdown`

* Used arguments file for long command line on windows

* Replaced custom logging by slf4j and logback (look up [migrating from 1.8 to 2.0] for details)

* Increased compile target from java 6 to java 8

* Dropped dependency [samskivert library](https://github.com/samskivert/samskivert) and inlined necessary code

* Removed custom logging facade

* Replaced base64 implementation from android with java.util.Base64

* Disabled obfuscation and compression with proguard for better troubleshooting

* Used classifier `jar-with-dependencies` for core and launcher with bundled dependencies

* Fixed process calculation for zip files

* Added fallback to binary mode on failure in zip digest computation

* Added support for user defined host whitelist from system property

* Removed workaround for obsoleted Windows ME and Windows 98

* Added logging for process output on debugging mode

* Introduced system property `use_proxy` to toggle proxy detection and info request
  (default is `true` to keep old behaviour)

## 1.8.7 - May 24, 2022

* Paths in classpath are specified relative to appdir to avoid excessively long command lines.

* When updating unpacked jar archives, the old unpacked archive is deleted more robustly to avoid
  issues when the new archive does not contain subdirectories that the old archive once contained.

* Added support for manual addition of classpath entries via the `classpath` directive.

* Reinstated env var support in `appbase` property.

* Fixed issue with `myIpAddress()` in PAC proxy support.

* Pack200 support removed. It is no longer supported by the JVM.

## 1.8.6 - June 4, 2019

* Fixed issues with PAC proxy support: added `myIpAddress()`, fixed `dnsResolve()`, fixed crash
  when detecting PAC proxy.

* Reverted env var support in `appbase` property. It's causing problems that need to be
  investigated.

## 1.8.5 - May 29, 2019

* Fixed issues with proxy information not getting properly passed through to app.
  Via [#216](//github.com/threerings/getdown/pull/216).

* `appbase` and `latest` properties in `getdown.txt` now process env var subtitutions.

* Added support for [Proxy Auto-config](https://en.wikipedia.org/wiki/Proxy_auto-config) via PAC
  files.

* Proxy handling can now recover from credentials going out of date. It will detect the error and
  ask for updated credentials.

* Added `try_no_proxy` system property. This instructs Getdown to always first try to run without a
  proxy, regardless of whether it has been configured to use a proxy in the past. And if it can run
  without a proxy, it does so for that session, but retains the proxy config for future sessions in
  which the proxy may again be needed.

* Added `revalidate_policy` config to control when Getdown revalidates resources (by hashing them
  and comparing that hash to the values in `digest.txt`). The default, `after_update`, only
  validates resources after the app is updated. A new mode, `always`, validates resources prior to
  every application launch.

## 1.8.4 - May 14, 2019

* Added `verify_timeout` config to allow customization of the default (60 second) timeout during
  the resource verification process. Apparently in some pathological situations, this is needed.
  Woe betide the users who have to stare at an unmoving progress bar for more than 60 seconds.
  Via [#198](//github.com/threerings/getdown/pull/198)
  and [901682d](//github.com/threerings/getdown/commit/901682d).

* Added `java_local_dir` config to allow custom location for Java if `java_location` is specified.
  Via [#206](//github.com/threerings/getdown/pull/206).

* `messages_XX.properties` files are now all maintained in UTF-8 encoding and then converted to
  escaped ISO-8859-1 during the build process.

* Resources and unpacked resources now support `.zip` files as well as `.jar` files.
  Via [#210](//github.com/threerings/getdown/pull/210).

* Fixed issue when path to JVM contained spaces. Via [#214](//github.com/threerings/getdown/pull/214).

## 1.8.3 - Apr 10, 2019

* Added support for `nresource` resources which must be jar files that contain native libraries.
  Prior to launching the application, these resources will be unpacked and their contents added to
  the `java.library.path` system property.

* When the app is updated to require a new version of the JVM, that JVM will be downloaded and used
  immediately during that app invocation (instead of one invocation later).
  Via [#169](//github.com/threerings/getdown/pull/169).

* When a custom JVM is installed, old JVM files will be deleted prior to unpacking the new JVM.
  Via [#170](//github.com/threerings/getdown/pull/170).

* Number of concurrent downloads now defaults to num-cores minus one. Though downloads are I/O
  bound rather than CPU bound, this still turns out to be a decent default.

* Avoid checking for proxy config if `https.proxyHost` is set. This matches existing behavior when
  `http.proxyHost` is set.

* Added support for proxy authentication. A deployment must also use the
  `com.threerings.getdown.spi.ProxyAuth` service provider interface to persist the proxy
  credentials supplied by the user. Otherwise they will be requested every time Getdown runs, which
  is not a viable user experience.

* The Getdown window can be now closed by pressing the `ESC` key.
  Via [#191](//github.com/threerings/getdown/pull/191).

* If no `appdir` is specified via the command line or system property, the current working
  directory will be used as the `appdir`. Via [8d59367](//github.com/threerings/getdown/commit/8d59367)

* A basic Russian translation has been added. Thanks [@sergiorussia](//github.com/sergiorussia)!

## 1.8.2 - Nov 27, 2018

* Fixed a data corruption bug introduced at last minute into 1.8.1 release. Oops.

## 1.8.1 - Nov 26, 2018

* If both an `appbase` and `appdir` are provided via some means (bootstrap properties file, system
  property, etc.) and the app dir does not yet exist, Getdown will create it.

* Added `max_concurrent_downloads` setting to `getdown.txt`. Controls what you would expect.
  Defaults to two.

* `bootstrap.properties` can now contain system properties which will be set prior to running
  Getdown. They must be prefixed by `sys.`: for example `sys.silent = true` will set the `silent`
  system property to `true`.

* If Getdown is run in a headless JVM, it will avoid showing a UI but will attempt to install and
  launch the application anyhow. Note that passing `-Dsilent` will override this behavior (because
  in silent mode the default is only to install the app, not also launch it).

* Fixed issue with `appid` not being properly used when specified via command line arg.

* Fixed issue with running Getdown on single CPU systems (or virtual systems). It was attempting to
  create a thread pool of size zero, which failed.

* Fixed issue with backslashes (or other regular expression escape characters) in environment
  variables being substituted into app arguments.

## 1.8.0 - Oct 19, 2018

* Added support for manually specifying the thread pool size via `-Dthread_pool_size`. Also reduced
  the default thread pool size to `num_cpus-1` from `num_cpus`.

* Added support for bundling a `bootstrap.properties` file with the Getdown jar file, which can
  specify defaults for `appdir`, `appbase` and `appid`.

* Added support for a host URL whitelist. Getdown can be custom built to refuse to operate with any
  URL that does not match the built-time-specified whitelist. See `core/pom.xml` for details.

* Removed the obsolete support for running Getdown in a signed applet. Applets are no longer
  supported by any widely used browser.

* Split the project into multiple Maven modules. See the notes on [migrating from 1.7 to 1.8] for
  details.

* A wide variety of small cleanups resulting from a security review generously performed by a
  prospective user. This includes various uses of deterministic locales and encodings instead of
  the platform default locale/encoding, in cases where platform/locale-specific behavior is not
  desired or needed.

* Made use of `appid` fall back to main app class if no `appid`-specific class is specified.

* Added support for marking resources as executable (via `xresource`).

* Fixed issue where entire tracking URL was being URL encoded.

* Changed translations to avoid the use of the term 'game'. Use 'app' instead.

## 1.7.1 - Jun 6, 2018

* Made it possible to use `appbase_domain` with `https` URLs.

* Fixed issue with undecorated splash window being unclosable if failures happen early in
  initialization process. (#57)

* Added support for transparent splash window. (#92)

* Fixed problem with unpacked code resources (`ucode`) and `pack.gz` files. (#95)

* Changed default Java version regex to support new Java 9+ version formats. (#93)

* Ensure correct signature algorithm is used for each version of digest files. (#91)

* Use more robust delete in all cases where Getdown needs to delete files. This should fix issues
  with lingering files on Windows (where sometimes delete fails spuriously).

## 1.7.0 - Dec 12, 2017

* Fixed issue with `Digester` thread pool not being shutdown. (#89)

* Fixed resource unpacking, which was broken by earlier change introducing resource installation
  (downloading to `_new` files and then renaming into place). (#88)

* The connect and read timeouts specified by system properties are now used for all the various
  connections made by Getdown.

* Proxy detection now uses a 5 second connect/read timeout, to avoid stalling for a long time in
  certain problematic network conditions.

* Getdown is now built against JDK 1.7 and requires JDK 1.7 (or newer) to run. Use the latest
  Getdown 1.6.x release if you need to support Java 1.6.

## 1.6.4 - Sep 17, 2017

* `digest.txt` (and `digest2.txt`) computation now uses parallel jobs. Each resource to be verified
  is a single job and the jobs are doled out to a thread pool with #CPUs threads. This allows large
  builds to proceed faster as most dev machines have more than one core.

* Resource verification is now performed in parallel (similar to the `digest.txt` computation, each
  resource is a job farmed out to a thread pool). For large installations on multi-core machines,
  this speeds up the verification phase of an installation or update.

* Socket reads now have a 30 second default timeout. This can be changed by passing
  `-Dread_timeout=N` (where N is seconds) to the JVM running Getdown.

* Fixed issue with failing to install a downloaded and validated `_new` file.

* Added support for "strict comments". In this mode, Getdown only treats `#` as starting a comment
  if it appears in column zero. This allows `#` to occur on the right hand side of configuration
  values (like in file names). To enable, put `strict_comments = true` in your `getdown.txt` file.

## 1.6.3 - Apr 23, 2017

* Fixed error parsing `cache_retention_days`. (#82)

* Fixed error with new code cache. (9e23a426)

## 1.6.2 - Feb 12, 2017

* Fixed issue with installing local JVM, caused by new resource installation process. (#78)

* Local JVM now uses absolute path to avoid issues with cwd.

* Added `override_appbase` system property. This enables a Getdown app that normally talks to some
  download server to be installed in such a way that it instead talks to some other download
  server.

## 1.6.1 - Feb 12, 2017

* Fix issues with URL path encoding when downloading resources. (84af080b0)

* Parsing `digest.txt` changed to allow `=` to appear in the filename. In `getdown.txt` we split on
  the first `=` because `=` never appears in a key but may appear in a value. But in `digest.txt`
  the format is `filename = hash` and `=` never appears in the hash but may appear in the filename,
  so there we want to split on the _last_ `=` not the first.

* Fixed bug with progress tracking and reporting. (256e0933)

* Fix executable permissions on `jspawnhelper`. (#74)

## 1.6 - Nov 5, 2016

* This release and all those before it are considered ancient history. Check the commit history for
  more details on what was in each of these releases.

## 1.0 - Sep 21, 2010

* The first Maven release of Getdown.

## 0.1 - July 19, 2004

* The first production use of Getdown (on https://www.puzzlepirates.com which is miraculously still
  operational as of 2018 when this changelog was created).

[migrating from 1.7 to 1.8]: https://github.com/threerings/getdown/wiki/Migrate17to18
