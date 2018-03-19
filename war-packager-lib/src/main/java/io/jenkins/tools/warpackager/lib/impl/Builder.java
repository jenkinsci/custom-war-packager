package io.jenkins.tools.warpackager.lib.impl;

import io.jenkins.tools.warpackager.lib.config.Config;
import io.jenkins.tools.warpackager.lib.config.DependencyInfo;
import io.jenkins.tools.warpackager.lib.config.GroovyHookInfo;
import io.jenkins.tools.warpackager.lib.config.SourceInfo;
import io.jenkins.tools.warpackager.lib.util.MavenHelper;
import io.jenkins.tools.warpackager.lib.util.SimpleManifest;
import org.apache.maven.model.Model;
import org.codehaus.plexus.util.FileUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.jenkins.tools.warpackager.lib.util.SystemCommandHelper.*;

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
        if (config.plugins != null) {
            for (DependencyInfo plugin : config.plugins) {
                buildIfNeeded(plugin);
            }
        }

        // Prepare library patches
        if (config.libPatches != null) {
            for(DependencyInfo library : config.libPatches) {
                buildIfNeeded(library);
            }
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
                .replaceLibs(versionOverrides)
                .excludeLibs()
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
        if (commit == null) { // we use ls-remote to fetch the commit ID
            String res = readFor(componentBuildDir, "git", "ls-remote", dep.source.git, checkoutId != null ? checkoutId : "master");
            commit = res.split("\\s+")[0];
        }

        //TODO if caching is disabled, aa nice-looking version can be retrieved
        // We cannot retrieve actual base version here without checkout. 256.0 prevents dependency check failures
        String newVersion = String.format("256.0-%s-%s-SNAPSHOT", checkoutId != null ? checkoutId : "default", commit);
        versionOverrides.put(dep.artifactId, newVersion);

        // TODO: add no-cache option?
        if (MavenHelper.artifactExists(componentBuildDir, dep, newVersion)) {
            LOGGER.log(Level.INFO, "Snapshot version exists for {0}: {1}. Skipping the build",
                    new Object[] {dep, newVersion});
            return;
        } else {
            LOGGER.log(Level.INFO, "Snapshot is missing for {0}: {1}. Will run the build",
                    new Object[] {dep, newVersion});
        }

        processFor(componentBuildDir, "git", "clone", dep.source.git, ".");
        processFor(componentBuildDir, "git", "checkout", commit);
        //String baseVersion = readFor(componentBuildDir,"mvn", "-q", "org.codehaus.mojo:exec-maven-plugin:1.3.1:exec", "-Dexec.executable=echo", "--non-recursive", "-Dexec.args='${project.version}'");

        // Install artifact with default version
        // TODO: Make it optional, required for cross-dependencies between objects
        processFor(componentBuildDir, "mvn", "clean", "install", "-DskipTests", "-Dfindbugs.skip=true", "-Denforcer.skip=true");

        // Build artifact with a custom version
        LOGGER.log(Level.INFO, "Set new version for {0}: {1}", new Object[] {dep.artifactId, newVersion});
        processFor(componentBuildDir,"mvn", "versions:set", "-DnewVersion=" + newVersion);
        processFor(componentBuildDir, "mvn", "clean", "install", "-DskipTests", "-Dfindbugs.skip=true", "-Denforcer.skip=true");
    }
}
