# Just a Makefile for manual testing
.PHONY: all

ARTIFACT_ID = jenkins-external-task-logging-elk-demo
VERSION = 2.121.1-elk-SNAPSHOT

all: clean build

clean:
	rm -rf tmp

build: tmp/output/target/${ARTIFACT_ID}-${VERSION}.war

tmp/output/target/${ARTIFACT_ID}-${VERSION}.war:
	java \
	    -jar $(shell ls ../../custom-war-packager-cli/target/custom-war-packager-cli-*-jar-with-dependencies.jar) \
	    -configPath packager-config.yml -version ${VERSION}

run: tmp/output/target/${ARTIFACT_ID}-${VERSION}.war
	docker-compose rm -fv
	docker-compose up --build --force-recreate jenkins elk
