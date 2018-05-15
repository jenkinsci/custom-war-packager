package io.jenkins.tools.warpackager.lib.config;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Defines settings for {@link io.jenkins.tools.warpackager.lib.impl.Builder}.
 * These settings can be passed from the configuration file, but they can be also overridden
 * by tools bundling the library.
 * @author Oleg Nenashev
 * @since TODO
 */
public class BuildSettings {

    public static final String DEFAULT_TMP_DIR_NAME = "tmp";
    public static final File DEFAULT_TMP_DIR = new File(DEFAULT_TMP_DIR_NAME);
    public static final String DEFAULT_VERSION = "1.0-SNAPSHOT";

    private File tmpDir;
    private String version;
    @CheckForNull
    private File mvnSettingsFile;
    @CheckForNull
    private List<String> mvnOptions;
    @CheckForNull
    private File bom;
    @CheckForNull
    private String environmentName;
    @CheckForNull
    private DockerBuildSettings docker;

    /**
     * If {@code true}, the final artifacts will be installed to the local repo.
     * It is mainly required for the CLI mode where we may want to install artifacts as well.
     */
    private boolean installArtifacts;

    public void setTmpDir(File tmpDir) {
        this.tmpDir = tmpDir;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setMvnSettingsFile(@CheckForNull File mvnSettingsFile) {
        this.mvnSettingsFile = mvnSettingsFile;
    }

    public void setBOM(@CheckForNull File bom) {
        this.bom = bom;
    }

    public void setEnvironmentName(@CheckForNull String environmentName) {
        this.environmentName = environmentName;
    }

    public void setInstallArtifacts(boolean installArtifacts) {
        this.installArtifacts = installArtifacts;
    }

    @Nonnull
    public File getTmpDir() {
        return tmpDir != null ? tmpDir : DEFAULT_TMP_DIR;
    }

    @Nonnull
    public File getOutputDir() {
        return new File(getTmpDir(), "output");
    }

    @Nonnull
    public String getVersion() {
        return version != null ? version : DEFAULT_VERSION;
    }

    @CheckForNull
    public File getBOM() {
        return bom;
    }

    @CheckForNull
    public String getEnvironmentName() {
        return environmentName;
    }

    @CheckForNull
    public File getMvnSettingsFile() {
        return mvnSettingsFile;
    }

    public boolean isInstallArtifacts() {
        return installArtifacts;
    }

    public void addMavenOption(@Nonnull String option) {
        if (mvnOptions == null) {
            mvnOptions = new ArrayList<>();
        }
        mvnOptions.add(option);
    }

    @Nonnull
    public List<String> getMvnOptions() {
        return mvnOptions != null ? Collections.unmodifiableList(mvnOptions) : Collections.emptyList();
    }

    @CheckForNull
    public DockerBuildSettings getDocker() {
        return docker;
    }
}
