package io.jenkins.tools.warpackager.cli;

import io.jenkins.tools.warpackager.lib.config.BuildSettings;
import org.kohsuke.args4j.Option;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.File;

public class CliOptions {

    @Option(name = "-configPath", usage = "Path to the configuration YAML. See the tool's README for format")
    public File configPath;

    @Option(name = "-mvnSettingsFile", usage = "Path to a custom Maven settings file to be used within the build")
    public File mvnSettingsFile;

    @Option(name = "-tmpDir", usage = "Temporary directory for generated files and the output WAR. Defaults to '" + BuildSettings.DEFAULT_TMP_DIR_NAME + "'")
    public File tmpDir;

    @Option(name = "-version", usage = "Version of WAR to be set. Defaults to '" + BuildSettings.DEFAULT_VERSION + "'")
    public String version;

    @Option(name = "-demo", usage = "Enables demo mode with predefined config file")
    public boolean demo;

    @Option(name = "--batch-mode", usage = "Enables the batch mode for the build")
    public boolean batchMode;

    @Option(name = "--bomPath", usage = "Path to the BOM file. If defined, it will override settings in Config YAML")
    public File bomPath;

    @Option(name = "--environment", usage = "Environment to be used")
    public String environment;

    @Option(name = "--installArtifacts", usage = "If set, the final artifacts will be automatically installed to the local repository (current version - only WAR)")
    public boolean installArtifacts;

    @Option(name = "--updateCenterUrl", usage = "URL of the update center. If defined, it will override settings in Config YAML")
    public String updateCenterUrl;

    @CheckForNull
    public File getConfigPath() {
        return configPath;
    }

    @Nonnull
    public File getTmpDir() {
        return tmpDir != null ? tmpDir : BuildSettings.DEFAULT_TMP_DIR;
    }

    @Nonnull
    public String getVersion() {
        return version != null ? version : BuildSettings.DEFAULT_VERSION;
    }

    @CheckForNull
    public File getMvnSettingsFile() {
        return mvnSettingsFile;
    }

    @CheckForNull
    public File getBOMPath() {
        return bomPath;
    }

    @CheckForNull
    public String getEnvironment() {
        return environment;
    }

    public boolean isDemo() {
        return demo;
    }

    public boolean isInstallArtifacts() {
        return installArtifacts;
    }

    @CheckForNull
    public String getUpdateCenterUrl() {
        return updateCenterUrl;
    }
}