package io.jenkins.tools.warpackager.cli;

import io.jenkins.tools.warpackager.lib.config.BuildSettings;
import org.kohsuke.args4j.Option;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.File;

public class CliOptions {

    @Option(name = "-configPath", usage = "Path to the configuration YAML. See the tool's README for format")
    public File configPath;

    @Option(name = "-tmpDir", usage = "Temporary directory for generated files and the output WAR. Defaults to '" + BuildSettings.DEFAULT_TMP_DIR_NAME + "'")
    public File tmpDir;

    @Option(name = "-version", usage = "Version of WAR to be set. Defaults to '" + BuildSettings.DEFAULT_VERSION + "'")
    public String version;

    @Option(name = "-demo", usage = "Enables demo mode with predefined config file")
    public boolean demo;

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

    public boolean isDemo() {
        return demo;
    }
}