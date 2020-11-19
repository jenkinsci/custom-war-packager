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

    private boolean buildx;

    private String output;

    @CheckForNull
    private String tag;

    /**
     * String custom field to store whatever you want.
     */
    private String customSettings;

    private String platform;

    public void setBase(@CheckForNull String base) {
        this.base = base;
    }

    public void setOutputDir(@CheckForNull String outputDir) {
        this.outputDir = outputDir;
    }

    public void setBuild(boolean build) {
        this.build = build;
    }

    public void setBuildx(boolean buildx) {
        this.buildx = buildx;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public void setTag(@CheckForNull String tag) {
        this.tag = tag;
    }

    public void setCustomSettings(String customSettings) {
        this.customSettings = customSettings;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
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

    public boolean isBuildx() {
        return buildx;
    }

    public String getOutput() {
        return output;
    }

    @CheckForNull
    public String getTag() {
        return tag;
    }

    public String getCustomSettings() {
        return customSettings;
    }

    public String getPlatform() {
        return platform;
    }
}
