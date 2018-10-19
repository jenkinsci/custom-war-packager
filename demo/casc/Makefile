# Just a Makefile for manual testing
.PHONY: all

ARTIFACT_ID = jenkins-casc-demo
VERSION = 256.0-test

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
	    jenkins-experimental/custom-war-packager-casc-demo

pct: tmp/output/target/${ARTIFACT_ID}-${VERSION}.war
	docker run --rm \
	    -v ${HOME}/.m2:/root/.m2 \
	    -v $(shell pwd)/pct_out:/pct/out \
	    -v $(shell pwd)/pct_tmp:/pct/tmp \
		-v $(shell pwd)/tmp/output/target/${ARTIFACT_ID}-${VERSION}.war:/pct/jenkins.war:ro \
		-e ARTIFACT_ID=role-strategy \
		-e INSTALL_BUNDLED_SNAPSHOTS=true \
		jenkins/pct \
		-mavenProperties jenkins-test-harness.version=2.38:jth.jenkins-war.path=/pct/jenkins.war

