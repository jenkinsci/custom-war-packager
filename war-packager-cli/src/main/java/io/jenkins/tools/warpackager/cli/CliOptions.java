package io.jenkins.tools.warpackager.cli;

import org.kohsuke.args4j.Option;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.File;

public class CliOptions {

    @Option(name = "-configPath", usage = "Path to the configuration YAML. See the tool's README for format")
    public File configPath;

    @Option(name = "-tmpDir", usage = "Temporary directory for generated files and the output WAR. Defaults to 'tmp'")
    public File tmpDir;

    @Option(name = "-version", usage = "Version of WAR to be set. Defaults to '1.0-SNAPSHOT'")
    public String version;

    @Option(name = "-demo", usage = "Enables demo mode with predefined config file")
    public boolean demo;

    @CheckForNull
    public File getConfigPath() {
        return configPath;
    }

    @Nonnull
    public File getTmpDir() {
        return tmpDir != null ? tmpDir : new File("tmp");
    }

    @Nonnull
    public String getVersion() {
        return version != null ? version : "1.0-SNAPSHOT";
    }

    public boolean isDemo() {
        return demo;
    }
}