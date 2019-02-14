# Just a Makefile for automatic testing
.PHONY: all

all: clean init test

clean:
	rm -rf .jenkinsfile-runner-test-framework
	rm -rf cwp.jar

init:
	git clone https://github.com/jenkinsci/jenkinsfile-runner-test-framework .jenkinsfile-runner-test-framework && cd .jenkinsfile-runner-test-framework && git checkout 4ed3c35650fe880345b77ee4776b29bd00944fb3
	rsync -avq --exclude-from='exclude-rsync.txt' $(shell pwd)/../ $(shell pwd)/.jenkinsfile-runner-test-framework/source
	$(MAKE) -C .jenkinsfile-runner-test-framework

test:
	./tests.sh