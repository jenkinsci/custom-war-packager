# Just a Makefile for manual testing
.PHONY: all

ARTIFACT_ID = pom-input-it
VERSION = 2.176.1-pom-input-SNAPSHOT

all: clean build

clean:
	rm -rf tmp

build: tmp/output/target/${ARTIFACT_ID}-${VERSION}.war

tmp/output/target/${ARTIFACT_ID}-${VERSION}.war:
	java \
	    -jar $(shell ls ../../../../custom-war-packager-cli/target/custom-war-packager-cli-*-jar-with-dependencies.jar) \
	    -configPath config.yml -version ${VERSION}

