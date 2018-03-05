package io.jenkins.tools.warpackager.cli;

import java.io.IOException;

import io.jenkins.tools.warpackager.cli.config.Config;
import io.jenkins.tools.warpackager.cli.impl.Builder;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        CliOptions options = new CliOptions();
        CmdLineParser p = new CmdLineParser(options);
        try {
            p.parseArgument(args);
        } catch (CmdLineException ex) {
            throw new IOException("Failed to read command-line arguments", ex);
        }

        Config cfg = Config.loadConfig(options.getConfigPath());

        Builder bldr = new Builder(options, cfg);
        bldr.build();
    }
}