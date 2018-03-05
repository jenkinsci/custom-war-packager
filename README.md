Jenkins WAR Packager
===

:exclamation: This tool is under development

A small packaging tool, which bundles custom Jenkins WAR files using the YAML specification.
Generally the tool is a wrapper on the top of Maven HPI's plugin 
[Custom WAR Mojo](https://jenkinsci.github.io/maven-hpi-plugin/custom-war-mojo.html).

Differences:

* It can build package custom branches with unreleased/unstaged packages
* It can run as a CLI tool outside Maven
* It takes YAML specification instead of Maven `pom.xml`

### Usage

The tool is a wrapper, which invokes various CLI commands to package the WAR. After the build the generated WAR file will be put to `tmp/output/${artifactId}.war`.

```shell
java -jar war-packager-cli.jar -configPath=mywar.yml -version=1.0-SNAPSHOT -tmpDir=tmp
```

To run the tool in a demo mode with [this config](./war-packager-cli/src/main/resources/io/jenkins/tools/warpackager/cli/config/sample.yml), just use the following command:

```shell
java -jar war-packager-cli.jar -demo
```

#### Prerequisites

* Maven 3.5.0 or above
* Java 8
* Git (if any Git sources are defined)

#### Configuration file

Example:

```yaml
bundle:
  groupId: "io.github.oleg-nenashev"
  artifactId: "mywar"
  description: "Just a WAR auto-generation-sample"
war:
  groupId: tools
  artifactId: tools
  source:
    version: 2.107
plugins:
  - groupId: tools
    artifactId: "matrix-project"
    source:
      version: 1.9
  - groupId: tools
    artifactId: "durable-task"
    source:
      git: https://github.com/jglick/durable-task-plugin.git
      branch: watch-JENKINS-38381
# TODO: System Properties support Not fully implemented yet
#systemProperties: {
#     jenkins.model.Jenkins.slaveAgentPort: "50000",
#     jenkins.model.Jenkins.slaveAgentPortEnforce: "true"}
# TODO: Groovy hooks support is not implemented yet
#groovy-hooks:
#  init:
#    source: 
#      git: https://github.com/oleg-nenashev/test-init-scripts.git
#      dir: scripts
```

### Limitations

Currently the tool is a PoC, some major features are missing:

* No caching of build artifacts, `git` dependencies will be always rebuilt
* All built artifacts with Git source are being installed to the local repository
  * Versions are unique for every commit, so beware of local repo pollution
* System properties will work only for a custom ``
* Error handling and reporting is far from perfect
