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

    private boolean build;

    private boolean push;

    private String registryURL;

    private String credentialsId;

    @CheckForNull
    private String tag;

    public void setBase(@CheckForNull String base) {
        this.base = base;
    }

    public void setOutputDir(@CheckForNull String outputDir) {
        this.outputDir = outputDir;
    }

    public void setBuild(boolean build) {
        this.build = build;
    }

    public void setTag(@CheckForNull String tag) {
        this.tag = tag;
    }

    public void setPush(boolean push) {
        this.push = push;
    }

    public void setRegistryURL(String registryURL) {
        this.registryURL = registryURL;
    }

    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    @Nonnull
    public String getBase() {
        return base != null ? base : DEFAULT_BASE;
    }

    @Nonnull
    public String getOutputDir() {
        return outputDir != null ? outputDir : DEFAULT_OUTPUT_DIR;
    }

    public boolean isBuild() {
        return build;
    }

    public boolean isPush() {
        return push;
    }

    public String getRegistryURL() {
        return registryURL;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    @CheckForNull
    public String getTag() {
        return tag;
    }
}
