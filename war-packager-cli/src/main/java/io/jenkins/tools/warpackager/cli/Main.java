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
        if (args.length == 0) {
            System.out.println("Usage: java -jar war-packager-cli.jar -configPath=mywar.yml [-version=1.0-SNAPSHOT] [-tmpDir=tmp]\n");
            p.printUsage(System.out);
            return;
        }

        try {
            p.parseArgument(args);
        } catch (CmdLineException ex) {
            p.printUsage(System.out);
            throw new IOException("Failed to read command-line arguments", ex);
        }

        if (options.getConfigPath() == null && !options.isDemo()) {
            throw new IOException("-configPath or -demo must be defined");
        }
        Config cfg = Config.loadConfig(options.isDemo() ? null : options.getConfigPath());

        Builder bldr = new Builder(options, cfg);
        bldr.build();
    }
}