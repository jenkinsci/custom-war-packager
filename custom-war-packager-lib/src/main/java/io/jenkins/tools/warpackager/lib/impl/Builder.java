package io.jenkins.tools.warpackager.lib.impl;

import hudson.util.VersionNumber;
import io.jenkins.tools.warpackager.lib.config.CasCConfig;
import io.jenkins.tools.warpackager.lib.config.Config;
import io.jenkins.tools.warpackager.lib.config.ConfigException;
import io.jenkins.tools.warpackager.lib.config.DockerBuildSettings;
import io.jenkins.tools.warpackager.lib.config.JenkinsfileRunnerSettings;
import io.jenkins.tools.warpackager.lib.config.LibraryInfo;
import io.jenkins.tools.warpackager.lib.config.DependencyInfo;
import io.jenkins.tools.warpackager.lib.config.WARResourceInfo;
import io.jenkins.tools.warpackager.lib.impl.jenkinsfileRunner.JenkinsfileRunnerDockerBuilder;
import io.jenkins.tools.warpackager.lib.model.ResolvedDependencies;
import io.jenkins.tools.warpackager.lib.model.ResolvedDependency;
import io.jenkins.tools.warpackager.lib.model.ResolvedResourceDependency;
import io.jenkins.tools.warpackager.lib.model.ResolvedWARDependency;
import io.jenkins.tools.warpackager.lib.model.bom.BOM;
import io.jenkins.tools.warpackager.lib.model.bom.ComponentReference;
import io.jenkins.tools.warpackager.lib.model.plugins.PluginInfoProvider;
import io.jenkins.tools.warpackager.lib.util.SimpleManifest;
import org.apache.maven.model.Model;
import org.codehaus.plexus.util.FileUtils;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static io.jenkins.tools.warpackager.lib.util.SystemCommandHelper.*;

/**
 * Builds WAR according to the specified config.
 * @author Oleg Nenashev
 * @since TODO
 */
public class Builder extends PackagerBase {

    private static final Logger LOGGER = Logger.getLogger(Builder.class.getName());

    private final File buildRoot;

    public Builder(Config config) {
        super(config);
        this.buildRoot = new File(config.buildSettings.getTmpDir(), "build");
    }

    /**
     * Performs spot-check of the input configuration.
     * It does not guarantee that the configuration is fully correct.
     */
    public void verifyConfig() throws IOException {
        if (config.casc != null && !config.casc.isEmpty()) {
            DependencyInfo dep = config.findPlugin(CasCConfig.CASC_PLUGIN_ARTIFACT_ID);
            if (dep == null) {
                throw new IOException("CasC section is declared, but CasC plugin is not declared in the plugins list");
            }
        }

        // Verify settings
        if (config.bundle == null) {
            throw new IOException("Bundle Information must be defined by configuration file or BOM");
        }

        // Build core and plugins
        if (config.war == null) {
            throw new IOException("Neither Jenkins core nor Jenkins war have been defined by configuration file or BOM/POM");
        }
    }

    public void build() throws IOException, InterruptedException {
        // Cleanup the temporary directory
        final File tmpDir = config.buildSettings.getTmpDir();
        PluginInfoProvider pluginInfoProvider = config.getPluginInfoProvider(mavenHelper, tmpDir);
        pluginInfoProvider.init();

        if (tmpDir.exists()) {
            LOGGER.log(Level.INFO, "Cleaning up the temporary directory {0}", tmpDir);
            FileUtils.deleteDirectory(tmpDir);
        }
        Files.createDirectories(buildRoot.toPath());

        // Load BOM if needed
        final File pathToBom = config.buildSettings.getBOM();
        if (pathToBom != null) {
            BOM bom = BOM.load(pathToBom);
            LOGGER.log(Level.INFO, "Overriding settings by BOM file: {0}", pathToBom);
            config.overrideByBOM(bom, config.buildSettings.getEnvironmentName());
        }
        // Load POM if needed
        final File pathToPom = config.buildSettings.getPOM();
        if (pathToPom != null) {
            File downloadDir = new File(tmpDir, "hpiDownloads");
            Files.createDirectory(downloadDir.toPath());
            config.overrideByPOM(downloadDir, pathToPom, config.buildSettings.isPomIgnoreRoot(), pluginInfoProvider);
        }

        verifyConfig();

        ResolvedDependencies resolvedDependencies = resolveDependencies(pluginInfoProvider);

        // Generate POM
        File warBuildDir = new File(tmpDir, "prebuild");
        Files.createDirectories(warBuildDir.toPath());
        MavenHPICustomWARPOMGenerator gen = new MavenHPICustomWARPOMGenerator(config, "-prebuild");
        Model model = gen.generatePOM(resolvedDependencies);
        gen.writePOM(model, warBuildDir);

        // Build WAR using Maven HPI plugin
        mavenHelper.run(warBuildDir, "clean", "package");

        // Add System properties
        File srcWar = new File(warBuildDir, "target/" + config.bundle.artifactId + "-prebuild.war");
        File explodedWar = new File(warBuildDir, "exploded-war");

        // Patch WAR
        new JenkinsWarPatcher(config, srcWar, explodedWar)
                .removeMetaInf()
                .addSystemProperties(config.systemProperties)
                .replaceLibs(resolvedDependencies.getLibraries())
                .excludeLibs()
                .addResources(resolvedDependencies.getResources());

        File warOutputDir = config.buildSettings.getOutputDir();
        SimpleManifest manifest = SimpleManifest.parseFile(srcWar);
        MavenWARPackagePOMGenerator finalWar = new MavenWARPackagePOMGenerator(config, explodedWar);
        finalWar.writePOM(finalWar.generatePOM(manifest.getMain()), warOutputDir);
        mavenHelper.run(warOutputDir, "clean", config.buildSettings.isInstallArtifacts() ? "install" : "package");

        // Produce BOM
        // TODO: append status to the original BOM?
        BOM bom = new BOMBuilder(config)
                .withPluginsDir(new File(explodedWar, "WEB-INF/plugins"))
                .withResolvedDependencies(resolvedDependencies)
                .build();
        bom.write(config.getOutputBOM());
        // TODO: also install WAR if config.buildSettings.isInstallArtifacts() is set

        // Build Docker if needed
        DockerBuildSettings dockerBuild = config.buildSettings.getDocker();
        if (dockerBuild != null) {
            LOGGER.log(Level.INFO, "Building Dockerfile");
            new JenkinsDockerfileBuilder(config, dockerBuild, config.buildSettings.getOutputDir())
                    .withPlugins(new File(explodedWar, "WEB-INF/plugins"))
                    .withInitScripts(new File(explodedWar, "WEB-INF"))
                    .build();
        }

        // Build Jenkinsfile Runner if needed
        JenkinsfileRunnerSettings jenkinsfileRunner = config.buildSettings.getJenkinsfileRunner();
        if (jenkinsfileRunner != null) {
            if (!jenkinsfileRunner.getSource().isNeedsBuild()) {
                throw new IOException("Jenkinsfile Runner always requires build");
            }
            if (!config.war.artifactId.equals("jenkins-war")) {
                throw new IOException("Jenkinsfile Runner packager can package only 'jenkins-war' so far");
            }

            String jenkinsVersion = ComponentReference.fromResolvedDependency(resolvedDependencies.getWar(), true).getVersion();
            resolveDependency(jenkinsfileRunner.getSource(), "jar",
                    Arrays.asList(
                            "-Djenkins.version=" + jenkinsVersion
                            /*, "-Djenkins.testharness.version=2.38"*/), null);
            File outputDir = config.buildSettings.getOutputDir();

            org.apache.commons.io.FileUtils.copyDirectory(
                    new File(buildRoot, jenkinsfileRunner.getSource().artifactId + "/app/target/appassembler"),
                    new File(outputDir, "jenkinsfileRunner"));

            // TODO: replace directory copy once Jenkinsfile Runner creates an archive for Jenkinsfile runner
            //File jenkinsfileBuilderJar = new File(outputDir, "jenkinsfile-runner.jar");
            //String version = ComponentReference.resolveFrom(jenkinsfileRunner.source, true, versionOverrides).getVersion();
            //mavenHelper.downloadArtifact(outputDir, jenkinsfileRunner.source,
             //       version, "jar-with-dependencies", jenkinsfileBuilderJar);

            DockerBuildSettings jenkinsfileRunnerDocker = jenkinsfileRunner.getDocker();
            if (jenkinsfileRunnerDocker != null) {
                if (config.buildSettings.getDocker() != null) {
                    //TODO: should be fixed later
                    throw new IOException("Currently it is not possible to build Docker and Jenkinsfile Runner Docker at the same time");
                }
                new JenkinsfileRunnerDockerBuilder(config, jenkinsfileRunnerDocker, outputDir)
                        .withPlugins(new File(explodedWar, "WEB-INF/plugins"))
                        .withResolvedDependencies(resolvedDependencies)
                        .withRunWorkspace(jenkinsfileRunner.getRunWorkspace())
                        .withNoSandbox(jenkinsfileRunner.isNoSandbox())
                        .build();
            }
        }

        // TODO: Support custom output destinations
        // File dstWar = new File(warBuildDir, "target/" + config.bundle.artifactId + ".war");
    }

    /**
     * Resolves dependencies which will be used for the WAR build.
     * Packaging (Jenkinsfile Runner, Docker) is not in the scope for the resolution.
     * @param pluginInfoProvider Info provider for plugins, which may be used to resolve group IDs
     */
    private ResolvedDependencies resolveDependencies(@CheckForNull PluginInfoProvider pluginInfoProvider) throws IOException, InterruptedException {

        // Start with libraries if needed
        List<String> coreComponentVersionOverrides = new LinkedList<>();
        if (config.war.libraries != null) {
            for (LibraryInfo ci : config.war.libraries) {
                ResolvedDependency dep = resolveDependency(ci.source, "jar");
                //TODO: record overrides for the core(?)
                // deps.addLibrary(dep);
                coreComponentVersionOverrides.add("-D" + ci.getProperty() + "=" + dep.getVersion());
            }
        }
        ResolvedDependencies deps = new ResolvedDependencies(new ResolvedWARDependency(
                resolveDependency(config.war, "war", coreComponentVersionOverrides, null)));


        if (config.plugins != null) {
            for (DependencyInfo plugin : config.plugins) {
                deps.addPlugin(resolveDependency(plugin, "hpi", Collections.emptyList(), pluginInfoProvider));
            }
        }

        // Prepare library patches
        if (config.libPatches != null) {
            for(DependencyInfo library : config.libPatches) {
                deps.addLibrary(resolveDependency(library, "jar"));
            }
        }

        // Prepare Resources
        for (WARResourceInfo extraWarResource : config.getAllExtraResources()) {
            deps.addResource(resolveResource(extraWarResource));
        }
        return deps;
    }

    //TODO: Merge with resolveDependency
    private ResolvedResourceDependency resolveResource(@Nonnull WARResourceInfo res) throws IOException, InterruptedException {
        File componentBuildDir = new File(buildRoot, res.id);
        Files.createDirectories(componentBuildDir.toPath());

        switch (res.source.getType()) {
            case FILESYSTEM:
                assert res.source.dir != null;
                LOGGER.log(Level.INFO, "Will checkout {0} from local directory: {1}", new Object[] {res.id, res.source.dir});
                return new ResolvedResourceDependency(new File(res.source.dir), res);
            case GIT:
                LOGGER.log(Level.INFO, "Will checkout {0} from git: {1}", new Object[] {res.id, res.source});
                break;
            default:
                throw new IOException("Unsupported checkout source: " + res.source.getType());
        }

        // Git checkout and build
        processFor(componentBuildDir, "git", "clone", res.source.git, ".");
        String checkoutId = res.source.getCheckoutId();
        if (checkoutId != null) {
            processFor(componentBuildDir, "git", "checkout", checkoutId);
        }
        String commit = readFor(componentBuildDir, "git", "log", "--format=%H", "-n", "1");
        LOGGER.log(Level.INFO, "Checked out {0}, commitId: {1}", new Object[] {res.id, commit});
        return new ResolvedResourceDependency(componentBuildDir, res);
    }

    private ResolvedDependency resolveDependency(@Nonnull DependencyInfo dep, @Nonnull String packaging) throws IOException, InterruptedException {
        return resolveDependency(dep, packaging, null, null);
    }

    private ResolvedDependency resolveDependency(@Nonnull DependencyInfo dep, @Nonnull String packaging,
                                                 @CheckForNull List<String> extraMavenArgs,
                                                 @CheckForNull PluginInfoProvider pluginInfoProvider)
            throws IOException, InterruptedException {
        String resolvedGroupdId = dep.groupId;
        if (resolvedGroupdId == null && pluginInfoProvider != null) {
            resolvedGroupdId = pluginInfoProvider.locateGroupId(dep);
        }
        if (resolvedGroupdId == null) {
            throw new ConfigException("Cannot resolve groupID for the dependency " + dep);
        }

        //TODO: add Caching support if commit is defined
        if (!dep.isNeedsBuild()) {
            LOGGER.log(Level.INFO, "Component {0}: no build required", dep);
            return new ResolvedDependency(resolvedGroupdId, new VersionNumber(dep.source.version), dep);
        }

        File componentBuildDir = new File(buildRoot, dep.artifactId);
        Files.createDirectories(componentBuildDir.toPath());

        if (dep.source == null) {
            throw new IOException("Source is not defined for dependency " + dep);
        }

        final String newVersion;
        switch (dep.source.getType()) {
            case GIT:
                LOGGER.log(Level.INFO, "Will checkout {0} from git: {1}", new Object[] {dep.artifactId, dep.source});

                String gitRemote = dep.source.git;
                if (gitRemote == null) {
                    throw new IllegalStateException("Building dependency " + dep + "in Git mode, but Git source is not set" );
                }

                String commit = dep.source.commit;
                final String checkoutId = dep.source.getCheckoutId();
                if (commit == null) { // we use ls-remote to fetch the commit ID
                    String res = readFor(componentBuildDir, "git", "ls-remote", gitRemote, checkoutId != null ? checkoutId : "master");
                    commit = res.split("\\s+")[0];
                }

                //TODO if caching is disabled, a nice-looking version can be retrieved
                // We cannot retrieve actual base version here without checkout. 256.0 prevents dependency check failures
                newVersion = String.format("256.0-%s-%s-SNAPSHOT", checkoutId != null ? checkoutId : "default", commit);

                if (mavenHelper.artifactExists(componentBuildDir, dep, newVersion, packaging)) {
                    if (dep.build != null && dep.build.noCache) {
                        LOGGER.log(Level.INFO, "Snapshot version exists for {0}: {1}, but caching is disabled. Will run the build",
                                new Object[]{dep, newVersion});
                    } else {
                        LOGGER.log(Level.INFO, "Snapshot version exists for {0}: {1}. Skipping the build",
                                new Object[]{dep, newVersion});
                        return new ResolvedDependency(resolvedGroupdId, new VersionNumber(newVersion), dep);
                    }
                } else {
                    LOGGER.log(Level.INFO, "Snapshot is missing for {0}: {1}. Will run the build",
                            new Object[] {dep, newVersion});
                }

                processFor(componentBuildDir, "git", "clone", gitRemote, ".");
                processFor(componentBuildDir, "git", "checkout", commit);
                break;
            case FILESYSTEM:
                assert dep.source.dir != null;
                LOGGER.log(Level.INFO, "Will checkout {0} from local directory: {1}",
                        new Object[] {dep.artifactId, dep.source.dir});
                File sourceDir = new File(dep.source.dir);
                org.apache.commons.io.FileUtils.copyDirectory(sourceDir, componentBuildDir);
                newVersion = String.format("256.0-%s-SNAPSHOT", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                break;
            default:
                throw new IOException("Unsupported checkout source: " + dep.source.getType());
        }

        // Install artifact with default version if required
        String[] args = {"clean", "install", "-DskipTests", "-Dfindbugs.skip=true", "-Denforcer.skip=true"};
        String[] combined = args;
        if(extraMavenArgs!= null && !extraMavenArgs.isEmpty()) {
            combined = Stream.concat(Arrays.stream(args), extraMavenArgs.stream())
                    .toArray(String[]::new);
        }
        if (dep.getBuildSettings().buildOriginalVersion) {
            mavenHelper.run(componentBuildDir, combined);
        }

        // Build artifact with a custom version
        LOGGER.log(Level.INFO, "Set new version for {0}: {1}", new Object[] {dep.artifactId, newVersion});
        mavenHelper.run(componentBuildDir, "versions:set", "-DnewVersion=" + newVersion);
        mavenHelper.run(componentBuildDir, combined);

        return new ResolvedDependency(resolvedGroupdId, new VersionNumber(newVersion), dep);
    }
}
