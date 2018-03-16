package io.jenkins.tools.warpackager.lib.impl;

import io.jenkins.tools.warpackager.lib.config.Config;
import io.jenkins.tools.warpackager.lib.config.DependencyInfo;
import io.jenkins.tools.warpackager.lib.config.GroovyHookInfo;
import io.jenkins.tools.warpackager.lib.config.SourceInfo;
import io.jenkins.tools.warpackager.lib.util.SimpleManifest;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Model;
import org.codehaus.plexus.util.FileUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Builds WAR according to the specified config.
 * @author Oleg Nenashev
 * @since TODO
 */
public class Builder {

    private static final Logger LOGGER = Logger.getLogger(Builder.class.getName());

    private final File buildRoot;
    private final Config config;

    // Context
    private Map<String, String> versionOverrides = new HashMap<>();


    public Builder(Config config) {
        this.config = config;
        this.buildRoot = new File(config.buildSettings.getTmpDir(), "build");
    }

    public void build() throws IOException, InterruptedException {

        // Cleanup the temporary directory
        final File tmpDir = config.buildSettings.getTmpDir();

        if (tmpDir.exists()) {
            LOGGER.log(Level.INFO, "Cleaning up the temporary directory {0}", tmpDir);
            FileUtils.deleteDirectory(tmpDir);
        }
        Files.createDirectories(buildRoot.toPath());

        // Build core and plugins
        buildIfNeeded(config.war);
        for (DependencyInfo plugin : config.plugins) {
            buildIfNeeded(plugin);
        }

        // Prepare Groovy Hooks
        Map<String, File> hooks = new HashMap<>();
        if (config.groovyHooks != null) {
            for (GroovyHookInfo hook : config.groovyHooks) {
                hooks.put(hook.id, checkoutIfNeeded(hook.id, hook.source));
            }
        }

        // Generate POM
        File warBuildDir = new File(tmpDir, "prebuild");
        Files.createDirectories(warBuildDir.toPath());
        MavenHPICustomWARPOMGenerator gen = new MavenHPICustomWARPOMGenerator(config, "-prebuild");
        Model model = gen.generatePOM(versionOverrides);
        gen.writePOM(model, warBuildDir);

        // Build WAR using Maven HPI plugin
        processFor(warBuildDir, "mvn", "clean", "package");

        // Add System properties
        File srcWar = new File(warBuildDir, "target/" + config.bundle.artifactId + "-prebuild.war");
        File explodedWar = new File(warBuildDir, "exploded-war");

        // Patch WAR
        new JenkinsWarPatcher(config, srcWar, explodedWar)
                .removeMetaInf()
                .addSystemProperties(config.systemProperties)
                .addHooks(hooks);

        File warOutputDir = new File(tmpDir, "output");
        SimpleManifest manifest = SimpleManifest.parseFile(srcWar);
        MavenWARPackagePOMGenerator finalWar = new MavenWARPackagePOMGenerator(config, explodedWar);
        finalWar.writePOM(finalWar.generatePOM(manifest.getMain()), warOutputDir);
        processFor(warOutputDir, "mvn", "clean", "package");

        // TODO: Support custom output destinations
        // File dstWar = new File(warBuildDir, "target/" + config.bundle.artifactId + ".war");
    }

    //TODO: Merge with buildIfNeeded
    private File checkoutIfNeeded(@Nonnull String id, @Nonnull SourceInfo source) throws IOException, InterruptedException {
        File componentBuildDir = new File(buildRoot, id);
        Files.createDirectories(componentBuildDir.toPath());

        switch (source.getType()) {
            case FILESYSTEM:
                assert source.dir != null;
                LOGGER.log(Level.INFO, "Will checkout {0} from local directory: {1}", new Object[] {id, source.dir});
                return new File(source.dir);
            case GIT:
                LOGGER.log(Level.INFO, "Will checkout {0} from git: {1}", new Object[] {id, source});
                break;
            default:
                throw new IOException("Unsupported checkout source: " + source.getType());
        }

        // Git checkout and build
        processFor(componentBuildDir, "git", "clone", source.git, ".");
        String checkoutId = source.getCheckoutId();
        if (checkoutId != null) {
            processFor(componentBuildDir, "git", "checkout", checkoutId);
        }
        String commit = readFor(componentBuildDir, "git", "log", "--format=%H", "-n", "1");
        LOGGER.log(Level.INFO, "Checked out {0}, commitId: {1}", new Object[] {id, commit});
        return componentBuildDir;
    }

    private void buildIfNeeded(DependencyInfo dep) throws IOException, InterruptedException {
        //TODO: add Caching support if commit is defined
        if (!dep.isNeedsBuild()) {
            LOGGER.log(Level.INFO, "Component {0}: no build required", dep);
            return;
        }

        File componentBuildDir = new File(buildRoot, dep.artifactId);
        Files.createDirectories(componentBuildDir.toPath());

        if (dep.source == null) {
            throw new IOException("Source is not defined for dependency " + dep);
        }

        switch (dep.source.getType()) {
            case GIT:
                LOGGER.log(Level.INFO, "Will checkout {0} from git: {1}", new Object[] {dep.artifactId, dep.source});
                break;
            default:
                throw new IOException("Unsupported checkout source: " + dep.source.getType());
        }

        // Git checkout and build
        String commit = dep.source.commit;
        final String checkoutId = dep.source.getCheckoutId();
        if (commit == null) { // we will need to checkout to determine the commit and caching status
            processFor(componentBuildDir, "git", "clone", dep.source.git, ".");
            if (checkoutId != null) {
                processFor(componentBuildDir, "git", "checkout", checkoutId);
            }
            commit = readFor(componentBuildDir, "git", "log", "--format=%H", "-n", "1");
        }

        String baseVersion = readFor(componentBuildDir,"mvn", "-q", "org.codehaus.mojo:exec-maven-plugin:1.3.1:exec", "-Dexec.executable=echo", "--non-recursive", "-Dexec.args='${project.version}'");
        String newVersion = String.format("%s-%s-%s-SNAPSHOT", baseVersion.replace("-SNAPSHOT", ""), checkoutId != null ? checkoutId : "default", commit);

        // TODO: add no-cache option?
        if (artifactExists(componentBuildDir, dep, baseVersion) && artifactExists(componentBuildDir, dep, baseVersion)) {
            LOGGER.log(Level.INFO, "Both snapshot version exist for {0}: {1} and {2}. Skipping the build",
                    new Object[] {dep, baseVersion, newVersion});
            return;
        } else {
            LOGGER.log(Level.INFO, "Some snapshots are missing for {0}: {1} and {2}. Will run the build",
                    new Object[] {dep, baseVersion, newVersion});
        }

        if (dep.source.commit != null) { // here we checkout the commit if we had not done it before
            processFor(componentBuildDir, "git", "clone", dep.source.git, ".");
            processFor(componentBuildDir, "git", "checkout", dep.source.commit);
        }

        // Install artifact with default version
        // TODO: Make it optional, required for cross-dependencies between objects
        processFor(componentBuildDir, "mvn", "clean", "install", "-DskipTests", "-Dfindbugs.skip=true");

        // Build artifact with a custom version
        LOGGER.log(Level.INFO, "Set new version for {0}: {1}", new Object[] {dep.artifactId, newVersion});
        processFor(componentBuildDir,"mvn", "versions:set", "-DnewVersion=" + newVersion);
        versionOverrides.put(dep.artifactId, newVersion);
        processFor(componentBuildDir, "mvn", "clean", "install", "-DskipTests", "-Dfindbugs.skip=true");
    }

    private boolean artifactExists(File buildDir, DependencyInfo dep, String version) throws IOException, InterruptedException {
        String gai = dep.groupId + ":" + dep.artifactId + ":" + version;
        int res = runFor(buildDir, "mvn", "dependency:get", "-Dartifact=" + gai, "-o");
        return res == 0;
    }

    private void processFor(File buildDir, String ... args) throws IOException, InterruptedException {
        ProcessBuilder bldr = new ProcessBuilder(args).inheritIO();
        int res = runFor(buildDir, args);
        if (res != 0) {
            throw new IOException("Command failed with exit code " + res + ": " + StringUtils.join(bldr.command(), ' '));
        }
    }

    private int runFor(File buildDir, String ... args) throws IOException, InterruptedException {
        ProcessBuilder bldr = new ProcessBuilder(args).inheritIO();
        bldr.directory(buildDir);
        return bldr.start().waitFor();
    }

    private String readFor(File buildDir, String ... args) throws IOException, InterruptedException {
        ProcessBuilder bldr = new ProcessBuilder(args);
        bldr.directory(buildDir);
        Process proc = bldr.start();
        int res = proc.waitFor();
        String out = IOUtils.toString(proc.getInputStream(), Charset.defaultCharset()).trim();
        if (res != 0) {
            throw new IOException("Command failed with exit code " + res + ": " + StringUtils.join(bldr.command(), ' ') + ". " + out);
        }
        return out;
    }

}
