Changelog
====

| WARNING: Changelogs have been moved to [GitHub Releases](https://github.com/jenkinsci/custom-war-packager/releases) |
| --- |

## New releases

See [GitHub Releases](https://github.com/jenkinsci/custom-war-packager/releases).

### 1.6

Release date: Mar 27, 2019

* [JENKINS-55703](https://issues.jenkins-ci.org/browse/JENKINS-55703), 
  [#71](https://github.com/jenkinsci/custom-war-packager/pull/71) - 
  Add new `runWorkspace` and `no-sandbox` options which sets those same options
  for the Jenkinsfile Runner docker image  
* [JENKINS-55568](https://issues.jenkins-ci.org/browse/JENKINS-55568), 
  [#72](https://github.com/jenkinsci/custom-war-packager/pull/72) - 
  Add support of passing the Jenkins Core version from the pom file

### 1.5

Release date: Dec 10, 2018

* [#52](https://github.com/jenkinsci/custom-war-packager/pull/52),
  [#62](https://github.com/jenkinsci/custom-war-packager/pull/62) - 
  Add a new `pomIgnoreRoot` option which skips the root artifact when using `pom.xml` as a plugin list
* [#51](https://github.com/jenkinsci/custom-war-packager/pull/51) -
  Performance: Cache non-Plugin dependencies when using `pom.xml` as a plugin list
* [#57](https://github.com/jenkinsci/custom-war-packager/issues/57) -
  Fix encoding warning from Maven Resources Plugin when building packages

### 1.4

Release date: Nov 28, 2018

* [#45](https://github.com/jenkinsci/custom-war-packager/pull/45) - 
Introduce an official Docker image for Custom WAR Packager
  * https://hub.docker.com/r/jenkins/custom-war-packager
* [#49](https://github.com/jenkinsci/custom-war-packager/issues/49) -
Custom WAR Packager did not work correctly on Windows
* [JENKINS-54340](https://issues.jenkins-ci.org/browse/JENKINS-54340), [#54](https://github.com/jenkinsci/custom-war-packager/pull/54) -
Custom WAR Packager was throwing error for `casc` sections 
when JCasC plugin was defined in BOM or pom.xml input
* [#55](https://github.com/jenkinsci/custom-war-packager/pull/55) -
Maven Plugin was not published for `1.3` due to the Javadoc issue
* [#47](https://github.com/jenkinsci/custom-war-packager/pull/47) -
Fix the External build logging demo after changes in upstream dependencies

### 1.3

Release date: Oct 21, 2018

* [JENKINS-54151](https://issues.jenkins-ci.org/browse/JENKINS-54151) - 
  Fix compatibility with JCasC Plugin 1.0 and above in 
  the Jenkinsfile Runner mode
* Pre-release versions of Jenkins Configuration as Code Plugin
  are no longer supported

### 1.2

Release date: Sep 04, 2018

* **EXPERIMENTAL**: Support of building Jenkinsfile Runner binaries and Docker images
  * The feature is based on [kohsuke/jenkinsfile-runner](https://github.com/kohsuke/jenkinsfile-runner),
    but it will be updated to use the upstream [jenkinsci/jenkinsfile-runner](https://github.com/jenkinsci/jenkinsfile-runner)
  * There may be incompatible changes
  * [Demo](./demo/jenkinsfile-runner)

### 1.1

Release date: Aug 03, 2018

* [JENKINS-51302](https://issues.jenkins-ci.org/browse/JENKINS-51302) -
Add support to core libraries that need a rebuild of war file for proper classloading in tests.
  * [Demo for Stapler](./demo/stapler)

### 1.0

Release date: Jul 17, 2018

* Offer CWP as CLI and as a Maven Plugin
* Support building plugins from custom branches with unreleased/unstaged packages
* Support specifications based on a custom YAML format and on
[Bill of Materials](https://github.com/jenkinsci/jep/tree/master/jep/309)
* Support specifying plugin lists via `pom.xml`
* Support patching WAR contents like bundled libraries, system properties
* Support self-configuration via [Groovy Hook Scripts](https://wiki.jenkins.io/display/JENKINS/Groovy+Hook+Script)
or [Configuration-as-Code Plugin](https://github.com/jenkinsci/configuration-as-code-plugin) YAML files
* Support building Docker images during the build

### 0.1-alpha-3..0.1.-alpha-8

There was a number alpha releases for experiments.
See the commit history if you use them, but it is recommended to use 1.0.

### 0.1-alpha-2

Release date: Mar 26, 2018

Initial public release, there may be incompatible changes in the future.

