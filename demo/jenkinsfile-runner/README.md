Custom WAR Packager. Jenkinsfile Builder demo
===

This demo demonstrates building of Jenkinsfile Runner Docker images
with Custom WAR Packager.

To build the, run `make clean build`

You can experiment with other `Jenkinsfile`s if needed.
Once the Docker image is built, the demo Jenkinsfile Runner can be started simply as..

    docker run --rm -v $PWD/demo/Jenkinsfile:/workspace/Jenkinsfile jenkins-experimental/cwp-jenkinsfile-runner-demo


or in Kubernetes

    kubectl create configmap jenkinsfile --from-file=demo/Jenkinsfile
    kubectl create -f demo/kubernetes.yaml

