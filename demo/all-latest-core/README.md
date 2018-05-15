Jenkins WAR Packager Demo. All-latest core
===

This demo builds a Jenkins WAR which includes...

* Master branch of the Jenkins core
* Latest versions of Stapler, Remoting and XStream
* Latest versions of some modules (full list - TODO)
  * Some agent installer modules have obsolete tooling, they need to be updated before inclusion
* Latest version of Lib Task Reactor
* Docker Packaging for the build

Limitations:

* Only component JARs are updated, the builder does not update dependencies
* WAR Packager is able to replace/add libs to WEB-INF/lib only, components
like Extras Executable War cannot be updated right now


### Usage

To build the demo just run the `sh build.sh` command.
It will produce a `tmp/output/target/jenkins-all-latest-1.0-SNAPSHOT.war` file.
You can run this file as a common Jenkins WAR file.
