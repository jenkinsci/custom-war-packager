#!/bin/bash
set -e

current_directory=$(pwd)
working_directory="$current_directory/.testing"
test_framework_directory="$current_directory/.jenkinsfile-runner-test-framework"
cwp_jar="$current_directory/cwp.jar"

jenkinsfile_runner_tag="jenkins-experimental/jenkinsfile-runner-test-image"
version="256.0-test"

. $test_framework_directory/init-jfr-test-framework.inc

oneTimeSetUp() {
  echo "Compiling CWP"
  mvn clean package -f "$test_framework_directory/source/pom.xml"

  echo "Moving CLI jar file to $current_directory"
  find $test_framework_directory/source/custom-war-packager-cli/target -name '*-with-dependencies.jar' -exec mv {} $cwp_jar ';'
}

#
# Use a JCasC configuration to build the JFR image, then get the system message that was set in
# casc.yml to verify that it was applied.
#
test_cwp_casc_simple() {
  jfr_tag=$(execute_cwp_jar_and_generate_docker_image "$working_directory" "$cwp_jar" "$version" "$current_directory/test_resources/test_cwp_casc_simple/packager-config.yml" "$jenkinsfile_runner_tag" | grep 'Successfully tagged')
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
  jfr_tag=$(execute_cwp_jar_and_generate_docker_image "$working_directory" "$cwp_jar" "$version" "$current_directory/test_resources/test_cwp_casc_with_tool/packager-config.yml" "$jenkinsfile_runner_tag" | grep 'Successfully tagged')
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
  jfr_tag=$(execute_cwp_jar_and_generate_docker_image "$working_directory" "$cwp_jar" "$version" "$current_directory/test_resources/test_cwp_casc_with_pipeline_library/packager-config.yml" "$jenkinsfile_runner_tag" | grep 'Successfully tagged')
  execution_should_success "$?" "$jenkinsfile_runner_tag" "$jfr_tag"

  run_jfr_docker_image "$jenkinsfile_runner_tag" "$current_directory/test_resources/test_cwp_casc_with_pipeline_library/Jenkinsfile"
  jenkinsfile_execution_should_succeed "$?"
  logs_contains "On Jenkins Infra? false"
}

#
# Test building Jenkinsfile-runner with a simple groovy hook
#
test_cwp_with_groovy_hooks() {
  jfr_tag=$(execute_cwp_jar_and_generate_docker_image "$working_directory" "$cwp_jar" "$version" "$current_directory/test_resources/test_cwp_with_groovy_hooks/packager-config.yml" "$jenkinsfile_runner_tag" | grep 'Successfully tagged')
  execution_should_success "$?" "$jenkinsfile_runner_tag" "$jfr_tag"

  run_jfr_docker_image "$jenkinsfile_runner_tag" "$current_directory/test_resources/test_cwp_with_groovy_hooks/Jenkinsfile"
  jenkinsfile_execution_should_succeed "$?"
  logs_contains "System configuration by Groovy Hooks"
}

#
# Use a JFR tag to build the JFR image, validates the Jenkinsfile is executed properly.
#
test_cwp_jfr() {
  jfr_tag=$(execute_cwp_jar_and_generate_docker_image "$working_directory" "$cwp_jar" "$version" "$current_directory/test_resources/test_cwp_jfr/packager-config.yml" "$jenkinsfile_runner_tag" | grep 'Successfully tagged')
  execution_should_success "$?" "$jenkinsfile_runner_tag" "$jfr_tag"

  run_jfr_docker_image "$jenkinsfile_runner_tag" "$current_directory/test_resources/test_cwp_jfr/Jenkinsfile"
  jenkinsfile_execution_should_succeed "$?"
  logs_contains "Jenkins Evergreen uses the following Core version"
}

#
# Build a JFR image using a commit for a plugin and validates the built and installed version matches that commit.
#
test_cwp_commit() {
  jfr_tag=$(execute_cwp_jar_and_generate_docker_image "$working_directory" "$cwp_jar" "$version" "$current_directory/test_resources/test_cwp_commit/packager-config.yml" "$jenkinsfile_runner_tag" | grep 'Successfully tagged')
  execution_should_success "$?" "$jenkinsfile_runner_tag" "$jfr_tag"

  run_jfr_docker_image_with_jfr_options "$jenkinsfile_runner_tag" "$current_directory/test_resources/test_cwp_commit/Jenkinsfile" "--no-sandbox"
  jenkinsfile_execution_should_succeed "$?"
  logs_contains "91273a413bbd5452bd32a17f67f9bd8ac7c164c6-91273a413bbd5452bd32a17f67f9bd8ac7c164c6-SNAPSHOT"
}

#
# Build a JFR image and validates the Jenkinsfile can operate with the workspace (defined by default to "/build").
#
test_cwp_workspace() {
  jfr_tag=$(execute_cwp_jar_and_generate_docker_image "$working_directory" "$cwp_jar" "$version" "$current_directory/test_resources/test_cwp_workspace/packager-config.yml" "$jenkinsfile_runner_tag" | grep 'Successfully tagged')
  execution_should_success "$?" "$jenkinsfile_runner_tag" "$jfr_tag"

  run_jfr_docker_image_with_docker_options "$jenkinsfile_runner_tag" "$current_directory/test_resources/test_cwp_workspace/Jenkinsfile" "-v $working_directory/files:/build"

  jenkinsfile_execution_should_succeed "$?"
  file_contains_text "This is the message to find in the logs" "message.txt" "$working_directory/files"
}

#
# Build a JFR image and validates the Jenkinsfile can operate with the workspace defined to a custom directory.
#
test_cwp_workspace_non_default() {
  jfr_tag=$(execute_cwp_jar_and_generate_docker_image "$working_directory" "$cwp_jar" "$version" "$current_directory/test_resources/test_cwp_workspace_non_default/packager-config.yml" "$jenkinsfile_runner_tag" | grep 'Successfully tagged')
  execution_should_success "$?" "$jenkinsfile_runner_tag" "$jfr_tag"

  run_jfr_docker_image_with_docker_options "$jenkinsfile_runner_tag" "$current_directory/test_resources/test_cwp_workspace_non_default/Jenkinsfile" "-v $working_directory/files:/anotherBuildDirectory -v $working_directory/defaultFiles:/build"

  jenkinsfile_execution_should_succeed "$?"
  file_does_not_exist_in_workspace "message.txt" "$working_directory/defaultFiles"
  file_contains_text "This is the message to find in the logs" "message.txt" "$working_directory/files"
}

#
# Build a JFR image and validates the classloading happens from the bundle of Jenkins core and not from the version that JFR provides.
# Note how jenkins.version for JFR is 2.89 so if classes introduced in later version are available is because the
# version specified in the configuration is provided when building JFR.
#
test_cwp_classloading() {
  jfr_tag=$(execute_cwp_jar_and_generate_docker_image "$working_directory" "$cwp_jar" "$version" "$current_directory/test_resources/test_cwp_classloading/packager-config.yml" "$jenkinsfile_runner_tag" | grep 'Successfully tagged')
  execution_should_success "$?" "$jenkinsfile_runner_tag" "$jfr_tag"

  run_jfr_docker_image_with_jfr_options "$jenkinsfile_runner_tag" "$current_directory/test_resources/test_cwp_classloading/Jenkinsfile" "--no-sandbox"

  jenkinsfile_execution_should_succeed "$?"
}

#
# Builds a docker image from pom and checks the war exploded
#
test_war_from_pom() {
  # war section
  jfr_tag=$(execute_cwp_jar_and_generate_docker_image "$working_directory" "$cwp_jar" "$version" "$current_directory/test_resources/test_war_from_pom/packager-config.yml" "$jenkinsfile_runner_tag" | grep 'Successfully tagged')
  execution_should_success "$?" "$jenkinsfile_runner_tag" "$jfr_tag"
  war_version=$(grep version "$working_directory"/out/tmp/prebuild/exploded-war/META-INF/maven/org.jenkins-ci.main/jenkins-war/pom.properties)
  logs_contains "2.121.1" "$war_version"
  logs_not_contains "2.130" "$war_version"
  logs_not_contains "2.150.3" "$war_version"

  # Missing war section and war information read from jenkins-war.version property in pom
  jfr_tag=$(execute_cwp_jar_and_generate_docker_image "$working_directory" "$cwp_jar" "$version" "$current_directory/test_resources/test_war_from_pom/packager-config-with-war-property.yml" "$jenkinsfile_runner_tag" | grep 'Successfully tagged')
  execution_should_success "$?" "$jenkinsfile_runner_tag" "$jfr_tag"
  war_version=$(grep version "$working_directory"/out/tmp/prebuild/exploded-war/META-INF/maven/org.jenkins-ci.main/jenkins-war/pom.properties)
  logs_not_contains "2.121.1" "$war_version"
  logs_not_contains "2.130" "$war_version"
  logs_contains "2.150.3" "$war_version"

  # war section overridden by jenkins.version
  jfr_tag=$(execute_cwp_jar_and_generate_docker_image "$working_directory" "$cwp_jar" "$version" "$current_directory/test_resources/test_war_from_pom/packager-config-with-property.yml" "$jenkinsfile_runner_tag" | grep 'Successfully tagged')
  execution_should_success "$?" "$jenkinsfile_runner_tag" "$jfr_tag"
  war_version=$(grep version "$working_directory"/out/tmp/prebuild/exploded-war/META-INF/maven/org.jenkins-ci.main/jenkins-war/pom.properties)
  logs_not_contains "2.121.1" "$war_version"
  logs_contains "2.130" "$war_version"
  logs_not_contains "2.150.3" "$war_version"

  # war section overridden by dependency
  jfr_tag=$(execute_cwp_jar_and_generate_docker_image "$working_directory" "$cwp_jar" "$version" "$current_directory/test_resources/test_war_from_pom/packager-config-with-dependency.yml" "$jenkinsfile_runner_tag" | grep 'Successfully tagged')
  execution_should_success "$?" "$jenkinsfile_runner_tag" "$jfr_tag"
  war_version=$(grep version "$working_directory"/out/tmp/prebuild/exploded-war/META-INF/maven/org.jenkins-ci.main/jenkins-war/pom.properties)
  logs_not_contains "2.121.1" "$war_version"
  logs_not_contains "2.130" "$war_version"
  logs_contains "2.150.3" "$war_version"
}

test_war_not_from_pom() {
  # war section
  jfr_tag=$(execute_cwp_jar_and_generate_docker_image "$working_directory" "$cwp_jar" "$version" "$current_directory/test_resources/test_war_not_from_pom/packager-config.yml" "$jenkinsfile_runner_tag" | grep 'Successfully tagged')
  execution_should_success "$?" "$jenkinsfile_runner_tag" "$jfr_tag"
  war_version=$(grep version "$working_directory"/out/tmp/prebuild/exploded-war/META-INF/maven/org.jenkins-ci.main/jenkins-war/pom.properties)
  logs_contains "2.121.1" "$war_version"
  logs_not_contains "2.130" "$war_version"
  logs_not_contains "2.150.3" "$war_version"

  # Ignore jenkins-war.version property in pom
  jfr_tag=$(execute_cwp_jar_and_generate_docker_image "$working_directory" "$cwp_jar" "$version" "$current_directory/test_resources/test_war_not_from_pom/packager-config-with-war-property.yml" "$jenkinsfile_runner_tag" | grep 'Successfully tagged')
  execution_should_success "$?" "$jenkinsfile_runner_tag" "$jfr_tag"
  war_version=$(grep version "$working_directory"/out/tmp/prebuild/exploded-war/META-INF/maven/org.jenkins-ci.main/jenkins-war/pom.properties)
  logs_contains "2.121.1" "$war_version"
  logs_not_contains "2.130" "$war_version"
  logs_not_contains "2.150.3" "$war_version"

  # war section and ignores jenkins.version
  jfr_tag=$(execute_cwp_jar_and_generate_docker_image "$working_directory" "$cwp_jar" "$version" "$current_directory/test_resources/test_war_not_from_pom/packager-config-with-property.yml" "$jenkinsfile_runner_tag" | grep 'Successfully tagged')
  execution_should_success "$?" "$jenkinsfile_runner_tag" "$jfr_tag"
  war_version=$(grep version "$working_directory"/out/tmp/prebuild/exploded-war/META-INF/maven/org.jenkins-ci.main/jenkins-war/pom.properties)
  logs_contains "2.121.1" "$war_version"
  logs_not_contains "2.130" "$war_version"
  logs_not_contains "2.150.3" "$war_version"

  # war section and ignores dependency
  jfr_tag=$(execute_cwp_jar_and_generate_docker_image "$working_directory" "$cwp_jar" "$version" "$current_directory/test_resources/test_war_not_from_pom/packager-config-with-dependency.yml" "$jenkinsfile_runner_tag" | grep 'Successfully tagged')
  execution_should_success "$?" "$jenkinsfile_runner_tag" "$jfr_tag"
  war_version=$(grep version "$working_directory"/out/tmp/prebuild/exploded-war/META-INF/maven/org.jenkins-ci.main/jenkins-war/pom.properties)
  logs_contains "2.121.1" "$war_version"
  logs_not_contains "2.130" "$war_version"
  logs_not_contains "2.150.3" "$war_version"
}

#
# Recreate the classloading test to verify how no-sandbox images can be created by specifying such in the configuration
#
test_cwp_sandbox_configuration() {
  jfr_tag=$(execute_cwp_jar_and_generate_docker_image "$working_directory" "$cwp_jar" "$version" "$current_directory/test_resources/test_cwp_sandbox_configuration/packager-config.yml" "$jenkinsfile_runner_tag" | grep 'Successfully tagged')
  execution_should_success "$?" "$jenkinsfile_runner_tag" "$jfr_tag"

  run_jfr_docker_image "$jenkinsfile_runner_tag" "$current_directory/test_resources/test_cwp_sandbox_configuration/Jenkinsfile"

  jenkinsfile_execution_should_succeed "$?"
}

init_framework
