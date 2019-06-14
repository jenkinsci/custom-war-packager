# Just a Makefile for manual testing
.PHONY: all

ARTIFACT_ID = pom-input-demo
VERSION = 2.121.1-pom-input-SNAPSHOT

all: clean build

clean:
	rm -rf tmp

build: tmp/output/target/${ARTIFACT_ID}-${VERSION}.war

tmp/output/target/${ARTIFACT_ID}-${VERSION}.war:
	java \
	    -jar $(shell ls ../../custom-war-packager-cli/target/custom-war-packager-cli-*-jar-with-dependencies.jar) \
	    -configPath packager-config.yml -version ${VERSION}

run: tmp/output/target/${ARTIFACT_ID}-${VERSION}.war
	docker run --rm \
	    -p 8080:8080 \
	    jenkins-experimental/demo-pom-input
