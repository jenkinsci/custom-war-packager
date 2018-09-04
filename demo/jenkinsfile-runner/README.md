Custom WAR Packager. Jenkinsfile Builder demo
===

This demo demonstrates building of Jenkinsfile Runner Docker images
with Custom WAR Packager.

To build the demo...

1) Build Custom WAR Packager in the root (`mvn clean install -DskipTests`)
2) Build the demo in this directory (`make clean build`)
3) Run the demo (`make run`)

You can experiment with other `Jenkinsfile`s if needed.
Once the Docker image is built, the demo Jenkinsfile Runner can be started simply as..

```
docker run --rm -v $PWD/Jenkinsfile:/workspace/Jenkinsfile jenkins-experimental/cwp-jenkinsfile-runner-demo
``` 

