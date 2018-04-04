#!/usr/bin/env bash
CLI_JAR=$(ls ../../custom-war-packager-cli/target/custom-war-packager-cli-*-jar-with-dependencies.jar)
java -jar ${CLI_JAR} -configPath packager-config.yml

