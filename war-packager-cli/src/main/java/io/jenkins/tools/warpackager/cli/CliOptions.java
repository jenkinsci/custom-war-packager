package io.jenkins.tools.warpackager.cli;

import org.kohsuke.args4j.Option;

import java.io.File;

public class CliOptions {

    @Option(name = "configPath")
    public File configPath;

    @Option(name = "tmpDir")
    public File tmpDir;

    @Option(name = "version")
    public String version;

    public File getConfigPath() {
        return configPath;
    }

    public File getTmpDir() {
        return tmpDir != null ? tmpDir : new File("tmp");
    }

    public String getVersion() {
        return version != null ? version : "1.0-SNAPSHOT";
    }
}