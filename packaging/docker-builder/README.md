Docker Package for Custom WAR Packager Environment
===

This image packages all tools needed by Custom WAR Packager to operate correctly.
It **DOES NOT** include the Custom WAR Packager itself, the tool should be retrieved from a Maven plugin.

### Usage

The image can be used within Jenkins Pipeline definitions.
In the [Custom WAR Packager CI Demo](https://github.com/oleg-nenashev/jenkins-custom-war-packager-ci-demo) uses this image from the 'docker' label
(the agent is provisioned by a Cloud plugin like Kubernetes or Docker plugin).

### Building Image

```sh
docker build -t onenashev/custom-war-packager-builder .
```
