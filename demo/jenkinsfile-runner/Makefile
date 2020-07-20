# Just a Makefile for manual testing
.PHONY: all

ARTIFACT_ID = jenkinsfile-runner-demo
VERSION = 256.0-test
CWP_VERSION = 1.3

all: clean build

clean:
	rm -rf tmp

.build/cwp-cli-${CWP_VERSION}.jar:
	rm -rf .build
	mkdir -p .build
	wget -O .build/cwp-cli-${CWP_VERSION}.jar https://repo.jenkins-ci.org/releases/io/jenkins/tools/custom-war-packager/custom-war-packager-cli/${CWP_VERSION}/custom-war-packager-cli-${CWP_VERSION}-jar-with-dependencies.jar

build: .build/cwp-cli-${CWP_VERSION}.jar
	java -jar .build/cwp-cli-${CWP_VERSION}.jar \
	     -configPath packager-config.yml -version ${VERSION}

build-devel:
	java -jar ../../custom-war-packager-cli/target/custom-war-packager-cli-*-SNAPSHOT-jar-with-dependencies.jar \
		-configPath packager-config.yml -version ${VERSION}

build-devel-debug:
	java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005 \
	    -jar ../../custom-war-packager-cli/target/custom-war-packager-cli-*-SNAPSHOT-jar-with-dependencies.jar \
		-configPath packager-config.yml -version ${VERSION}

run:
	docker run --rm -v $(shell pwd)/demo/Jenkinsfile:/workspace/Jenkinsfile \
	    jenkins-experimental/cwp-jenkinsfile-runner-demo
