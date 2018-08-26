package io.jenkins.tools.warpackager.lib.config;

import javax.annotation.CheckForNull;

/**
 * Defines build settings for Jenkinsfile Runner.
 * @author Oleg Nenashev
 * @since TODO
 */
public class JenkinsfileRunnerSettings {

    public DependencyInfo source;

    @CheckForNull
    public DockerBuildSettings docker;
}
