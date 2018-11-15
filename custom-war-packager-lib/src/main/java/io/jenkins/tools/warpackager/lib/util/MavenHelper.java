package io.jenkins.tools.warpackager.lib.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.jenkins.tools.warpackager.lib.config.BuildSettings;
import io.jenkins.tools.warpackager.lib.config.Config;
import io.jenkins.tools.warpackager.lib.config.DependencyInfo;
import io.jenkins.tools.warpackager.lib.config.SourceInfo;

import javax.annotation.CheckReturnValue;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.jenkins.tools.warpackager.lib.util.SystemCommandHelper.processFor;
import static io.jenkins.tools.warpackager.lib.util.SystemCommandHelper.runFor;

/**
 * @author Oleg Nenashev
 */
public class MavenHelper {

    private static final Logger LOGGER = Logger.getLogger(MavenHelper.class.getName());

    private Config cfg;

    public MavenHelper(Config cfg) {
        this.cfg = cfg;
    }

    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED", justification = "Passed parameter is true")
    public void run(File buildDir, String ... args) throws IOException, InterruptedException {
        run(buildDir, true, args);
    }

    @CheckReturnValue
    public int run(File buildDir, boolean failOnError, String ... args) throws IOException, InterruptedException {
        ArrayList<String> callArgs = new ArrayList<>();
        callArgs.add("mvn");
        if (cfg.buildSettings != null) {
            File settingsFile = cfg.buildSettings.getMvnSettingsFile();
            if (settingsFile != null) {
                callArgs.add("-s");
                callArgs.add(settingsFile.getAbsolutePath());
            }
            Collections.addAll(callArgs, args);
            callArgs.addAll(cfg.buildSettings.getMvnOptions());
        }

        if (failOnError) {
            processFor(buildDir, callArgs.toArray(args));
            return 0;
        }
        return runFor(buildDir, callArgs.toArray(args));
    }

    public boolean artifactExistsInLocalCache(DependencyInfo dep, String version, String packaging) {
        final String folder = "repository";
        String path = getDependencyPath(folder, dep, version, packaging);
        LOGGER.log(Level.INFO, "Checking {0}", path);
        File expectedFile = new File(path);
        return expectedFile.exists();
    }

    private static String getDependencyPath(final String folder, final DependencyInfo dep, final String version, final String packaging) {
        return String.format("~/.m2/%s/%s/%s/%s/%s-%s.%s",
                    folder,
                    dep.groupId.replaceAll("\\.", "/"),
                    dep.artifactId,
                    version,
                    dep.artifactId,
                    version,
                    packaging);
    }

    public boolean artifactExists(File buildDir, DependencyInfo dep, String version, String packaging) throws IOException, InterruptedException {
        String gai = dep.groupId + ":" + dep.artifactId + ":" + version;
        int res = run(buildDir, false,"dependency:get",
                "-Dartifact=" + gai,
                "-Dpackaging=" + packaging,
                "-Dtransitive=false", "-q", "-B");
        final boolean found = res == 0;
        return found;
    }

    public void downloadJAR(File buildDir, DependencyInfo dep, String version, File destination)
            throws IOException, InterruptedException {
        downloadArtifact(buildDir, dep, version, "jar", destination);
    }

    public List<DependencyInfo> listDependenciesFromPom(File buildDir, File pom, File destination) throws IOException, InterruptedException {
        List<DependencyInfo> dependencies = new LinkedList<>();
        int listed = run(buildDir, true, "dependency:list", "-B", "-DoutputFile=" + destination.getAbsolutePath(),  "-DincludeScope=runtime", "-DexcludeClassifiers=tests", "-f", pom.getAbsolutePath(), "-q");
        if (listed == 0) {
            try (Stream<String> stream = Files.lines(destination.toPath())) {
                stream.skip(2).filter(line -> !line.isEmpty()).forEach(line -> {
                    line = line.trim();
                    String[] dependencyData = line.split(":");
                    DependencyInfo dep = new DependencyInfo();
                    dep.groupId = dependencyData[0].trim();
                    dep.artifactId = dependencyData[1].trim();
                    dep.source = new SourceInfo();
                    dep.source.version = dependencyData[3].trim();
                    dependencies.add(dep);
                });
            }
            return dependencies;
        } else {
            throw new IOException("Unable to list dependencies for pom file");
        }
    }

    public void downloadArtifact(File buildDir, DependencyInfo dep, String version, String packaging, File destination)
            throws IOException, InterruptedException {
        run(buildDir, "com.googlecode.maven-download-plugin:download-maven-plugin:1.4.0:artifact",
                "-DgroupId=" + dep.groupId,
                "-DartifactId=" + dep.artifactId,
                "-Dversion=" + version,
                "-DoutputDirectory=" + destination.getParentFile().getAbsolutePath(),
                "-DoutputFileName=" + destination.getName(),
                "-Dtype=" + packaging,
                "-q");
    }

}
