###
# Image name: onenashev/custom-war-packager-builder
###
FROM maven:3.5.0-jdk-8
MAINTAINER Oleg Nenashev <o.v.nenashev@gmail.com>

LABEL Description="This is a Jenkins agent image, which packages tools needed by Jenkins Custom WAR Packager" Vendor="Jenkins project" Version="0.1"

RUN apt-get -y update && apt-get install -y git && rm -rf /var/lib/apt/lists/*

#TODO: Consider moving image to the "jenkins" user instead of root
RUN mkdir /root/.jenkins
VOLUME /root/.jenkins
VOLUME /root/.m2
WORKDIR /root
