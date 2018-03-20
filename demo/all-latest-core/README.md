Jenkins WAR Packager Demo. All-latest core
===

This demo builds a Jenkins WAR which includes...

* Master branch of the Jenkins core
* Latest versions of Stapler, Remoting and XStream
* Latest versions of some modules (full list - TODO)
  * Some slave installer modules have obsolete tooling, they need to be updated before inclusion
* Latest version of Lib Task Reactor


Limitations:

* Only component JARs are updated, the builder does not update dependencies
* WAR Packager is able to replace/add libs to WEB-INF/lib only, components
like Extras Executable War cannot be updated right now
