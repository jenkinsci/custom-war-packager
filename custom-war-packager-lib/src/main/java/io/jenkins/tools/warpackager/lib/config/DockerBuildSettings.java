package io.jenkins.tools.warpackager.lib.config;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Settings for Docker packaging
 * @author Oleg Nenashev
 */
public class DockerBuildSettings {

    public static final String DEFAULT_BASE = "jenkins/jenkins:lts";
    public static final String DEFAULT_OUTPUT_DIR = "docker";

    @CheckForNull
    private String base;

    @CheckForNull
    private String outputDir;

    public void setBase(@CheckForNull String base) {
        this.base = base;
    }

    public void setOutputDir(@CheckForNull String outputDir) {
        this.outputDir = outputDir;
    }

    @Nonnull
    public String getBase() {
        return base != null ? base : DEFAULT_BASE;
    }

    @Nonnull
    public String getOutputDir() {
        return outputDir != null ? outputDir : DEFAULT_OUTPUT_DIR;
    }
}
