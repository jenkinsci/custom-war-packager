package io.jenkins.tools.warpackager.cli;

import io.jenkins.tools.warpackager.lib.config.Config;
import io.jenkins.tools.warpackager.lib.impl.Builder;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.File;
import java.io.IOException;

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

        final Config cfg;
        if (options.isDemo()) {
            System.out.println("Running build in the demo mode");
            cfg = Config.loadDemoConfig();
        } else {
            final File configPath = options.getConfigPath();
            if (configPath == null) {
                throw new IOException("-configPath or -demo must be defined");
            }
            cfg = Config.loadConfig(configPath);
        }

        // Override Build Settings by CLI arguments
        cfg.buildSettings.setTmpDir(options.getTmpDir());
        cfg.buildSettings.setVersion(options.getVersion());
        cfg.buildSettings.setMvnSettingsFile(options.getMvnSettingsFile());
        cfg.buildSettings.setBOM(options.getBOMPath());
        cfg.buildSettings.setEnvironmentName(options.getEnvironment());
        cfg.buildSettings.setInstallArtifacts(options.isInstallArtifacts());
        cfg.buildSettings.setUpdateCenterUrl(options.getUpdateCenterUrl());
        if (options.batchMode) {
            cfg.buildSettings.addMavenOption("--batch-mode");
            cfg.buildSettings.addMavenOption("-Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn");
        }

        new Builder(cfg).build();
    }
}