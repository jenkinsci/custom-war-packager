#!/bin/bash
set -e

current_directory=$(pwd)
test_framework_directory="$current_directory/.jenkinsfile-runner-test-framework"

. $test_framework_directory/init-jfr-test-framework.inc

oneTimeSetUp() {
  echo "Compiling CWP"
  mvn clean package -f "$test_framework_directory/source/pom.xml"

  echo "Moving CLI jar file to $current_directory"
  find $test_framework_directory/source/custom-war-packager-cli/target -name '*-with-dependencies.jar' -exec mv {} $current_directory/cwp.jar ';'
}

test_example() {
  echo "Executing a test. Test nothing. It is only used to create structure"
  ls "$current_directory/cwp.jar"
}

init_framework
