package io.jenkins.tools.warpackager.lib.config;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.File;

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


    public void setTmpDir(File tmpDir) {
        this.tmpDir = tmpDir;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setMvnSettingsFile(@CheckForNull File mvnSettingsFile) {
        this.mvnSettingsFile = mvnSettingsFile;
    }

    @Nonnull
    public File getTmpDir() {
        return tmpDir != null ? tmpDir : DEFAULT_TMP_DIR;
    }

    @Nonnull
    public String getVersion() {
        return version != null ? version : DEFAULT_VERSION;
    }

    @CheckForNull
    public File getMvnSettingsFile() {
        return mvnSettingsFile;
    }
}
