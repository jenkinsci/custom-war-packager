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

    private static final String DEFAULT_WORKSPACE = "/build";

    @Nonnull
    private DependencyInfo source;

    @CheckForNull
    private DockerBuildSettings docker;

    @CheckForNull
    private String runWorkspace;

    private boolean noSandbox;

    public void setSource(@Nonnull DependencyInfo source) {
        this.source = source;
    }

    public void setDocker(@CheckForNull DockerBuildSettings docker) {
        this.docker = docker;
    }

    public void setRunWorkspace(@CheckForNull String runWorkspace) {
        this.runWorkspace= runWorkspace;
    }

    public void setNoSandbox(boolean noSandbox) {
        this.noSandbox = noSandbox;
    }

    @Nonnull
    public DependencyInfo getSource() {
        return source;
    }

    @CheckForNull
    public DockerBuildSettings getDocker() {
        return docker;
    }

    @Nonnull
    public String getRunWorkspace() {
        return runWorkspace != null ? runWorkspace : DEFAULT_WORKSPACE;
    }

    public boolean isNoSandbox() {
        return noSandbox;
    }
}
