Changelog
====

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

