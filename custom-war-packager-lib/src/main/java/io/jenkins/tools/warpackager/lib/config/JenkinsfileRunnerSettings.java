package io.jenkins.tools.warpackager.lib.config;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Defines build settings for Jenkinsfile Runner.
 * @author Oleg Nenashev
 * @since TODO
 */
@SuppressFBWarnings(value = "NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR",
    justification = "Jackson does it in this case")
public class JenkinsfileRunnerSettings {

    @Nonnull
    private DependencyInfo source;

    @CheckForNull
    private DockerBuildSettings docker;

    public void setDocker(@CheckForNull DockerBuildSettings docker) {
        this.docker = docker;
    }

    public void setSource(@Nonnull DependencyInfo source) {
        this.source = source;
    }

    @Nonnull
    public DependencyInfo getSource() {
        return source;
    }

    @CheckForNull
    public DockerBuildSettings getDocker() {
        return docker;
    }
}
