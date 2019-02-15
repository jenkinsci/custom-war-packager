#!/usr/local/bin/bash
set -e

current_directory=$(pwd)
working_directory="$current_directory/.testing"
jenkinsfile_runner_tag="jenkins-experimental/jenkinsfile-runner-test-image"
version="256.0-test"

test_framework_directory="$current_directory/.jenkinsfile-runner-test-framework"

. $test_framework_directory/init-jfr-test-framework.inc

oneTimeSetUp() {
  echo "Compiling CWP"
  mvn clean package -f "$test_framework_directory/source/pom.xml"

  echo "Moving CLI jar file to $current_directory"
  find $test_framework_directory/source/custom-war-packager-cli/target -name '*-with-dependencies.jar' -exec mv {} $current_directory/cwp.jar ';'
  cwp_jar="$current_directory/cwp.jar"
}

#
# Use a JCasC configuration to build the JFR image, then get the system message that was set in
# casc.yml to verify that it was applied.
#
test_cwp_casc_simple() {
  jfr_tag=$(execute_cwp_jar_and_generate_docker_image "$working_directory" "${cwp_jar}" "$version" "$current_directory/test_resources/test_cwp_casc_simple/packager-config.yml" "$jenkinsfile_runner_tag" | grep 'Successfully tagged')
  execution_should_success "$?" "$jenkinsfile_runner_tag" "$jfr_tag"

  run_jfr_docker_image_with_jfr_options "$jenkinsfile_runner_tag" "$current_directory/test_resources/test_cwp_casc_simple/Jenkinsfile" "--no-sandbox"
  jenkinsfile_execution_should_succeed "$?"
  logs_contains "Jenkins configured automatically by Jenkins Configuration as Code Plugin"
}

#
# Use a JCasC configuration to build the JFR image with a tool, in this case a maven instance.  The
# current solution is a bit of a hack, as I could not determine how to get CasC to download a maven
# instance, so I am doing that in the Jenkinsfile.
#
test_cwp_jcasc_with_tool() {
  jfr_tag=$(execute_cwp_jar_and_generate_docker_image "$working_directory" "${cwp_jar}" "$version" "$current_directory/test_resources/test_cwp_casc_with_tool/packager-config.yml" "$jenkinsfile_runner_tag" | grep 'Successfully tagged')
  execution_should_success "$?" "$jenkinsfile_runner_tag" "$jfr_tag"

  run_jfr_docker_image "$jenkinsfile_runner_tag" "$current_directory/test_resources/test_cwp_casc_with_tool/Jenkinsfile"
  jenkinsfile_execution_should_succeed "$?"
  logs_contains "Apache Maven 3.3.3"
}

#
# Use a JCasC configuration to build the JFR image with a global library.  Then try to
# reference methods in that library
#
test_jcasc_with_pipeline_library() {
  jfr_tag=$(execute_cwp_jar_and_generate_docker_image "$working_directory" "${cwp_jar}" "$version" "$current_directory/test_resources/test_cwp_casc_with_pipeline_library/packager-config.yml" "$jenkinsfile_runner_tag" | grep 'Successfully tagged')
  execution_should_success "$?" "$jenkinsfile_runner_tag" "$jfr_tag"

  run_jfr_docker_image "$jenkinsfile_runner_tag" "$current_directory/test_resources/test_cwp_casc_with_pipeline_library/Jenkinsfile"
  jenkinsfile_execution_should_succeed "$?"
  logs_contains "On Jenkins Infra? false"
}

#
# Test building Jenkinsfile-runner with a simple groovy hook
#
test_cwp_with_groovy_hooks() {
  jfr_tag=$(execute_cwp_jar_and_generate_docker_image "$working_directory" "${cwp_jar}" "$version" "$current_directory/test_resources/test_cwp_with_groovy_hooks/packager-config.yml" "$jenkinsfile_runner_tag" | grep 'Successfully tagged')
  execution_should_success "$?" "$jenkinsfile_runner_tag" "$jfr_tag"

  run_jfr_docker_image "$jenkinsfile_runner_tag" "$current_directory/test_resources/test_cwp_with_groovy_hooks/Jenkinsfile"
  jenkinsfile_execution_should_succeed "$?"
  logs_contains "System configuration by Groovy Hooks"
}


init_framework
