package io.jenkins.tools.warpackager.lib.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.jenkins.tools.warpackager.lib.impl.plugins.UpdateCenterPluginInfoProvider;
import io.jenkins.tools.warpackager.lib.model.bom.BOM;
import io.jenkins.tools.warpackager.lib.model.bom.ComponentReference;
import io.jenkins.tools.warpackager.lib.model.bom.Environment;
import io.jenkins.tools.warpackager.lib.model.bom.Metadata;
import io.jenkins.tools.warpackager.lib.model.bom.Specification;
import io.jenkins.tools.warpackager.lib.model.plugins.PluginInfoProvider;
import io.jenkins.tools.warpackager.lib.util.MavenHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Oleg Nenashev
 * @since TODO
 */
@SuppressFBWarnings(value = "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", justification = "JSON Deserialization")
public class Config {

    private static final Logger LOGGER = Logger.getLogger(Config.class.getName());

    public BuildSettings buildSettings;
    public PackageInfo bundle;
    // Nonnull after the build starts
    public WarInfo war;
    @CheckForNull
    public Collection<DependencyInfo> plugins;
    @CheckForNull
    public Collection<DependencyInfo> libPatches;
    @CheckForNull
    public Collection<DependencyInfo> libExcludes;
    @CheckForNull
    public Map<String, String> systemProperties;
    @CheckForNull
    public Collection<GroovyHookInfo> groovyHooks;
    @CheckForNull
    public Collection<CasCConfig> casc;

    private static Config load(@Nonnull InputStream istream, boolean isEssentialsYML) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        final Config loaded;
        if (isEssentialsYML) {
            LOGGER.log(Level.INFO, "Loading from the essentials.yml format");
            EssentialsYMLConfig cfg = mapper.readValue(istream, EssentialsYMLConfig.class);
            if (cfg.packaging == null) {
                throw new IOException("essentials.yml does not have the packaging section");
            }
            if(cfg.packaging.config != null) {
                loaded = cfg.packaging.config;
            } else if (cfg.packaging.configFile != null){
                LOGGER.log(Level.INFO, "Loading config from external file defined in essentials.yml: {0}",
                        cfg.packaging.configFile);
                loaded = loadConfig(new File(cfg.packaging.configFile));
            } else {
                throw new IOException("essentials.yml does not have `packaging.config` or `packaging.configFile`");
            }
        } else {
            loaded = mapper.readValue(istream, Config.class);
        }
        if (loaded.buildSettings == null) {
            loaded.buildSettings = new BuildSettings();
        }
        return loaded;
    }

    public static Config loadDemoConfig() throws IOException {
        LOGGER.log(Level.WARNING, "Loading the default configuration sample from resource {0}/sample.yml", Config.class);
        try (InputStream istream = Config.class.getResourceAsStream("sample.yml")) {
            if (istream == null) {
                throw new FileNotFoundException(String.format("Cannot load the demo config: %s/sample.yml", Config.class));
            }
            return load(istream, false);
        }
    }

    /**
     * Loads configuration file.
     * Both standard and {@code essentials.yml} formats are supported.
     * The format is determined by the name.
     * @param configPath Path to the configuration file.
     * @return Loaded configuration
     * @throws IOException Loading error
     */
    public static Config loadConfig(@Nonnull File configPath) throws IOException {
        if (configPath.exists() && configPath.isFile()) {
            try (FileInputStream istream = new FileInputStream(configPath)) {
                return load(istream, "essentials.yml".equals(configPath.getName()));
            }
        }
        throw new FileNotFoundException("Cannot find the configuration file " + configPath);
    }

    // TODO: make the destination configurable
    public File getOutputWar() {
        return new File(buildSettings.getTmpDir(), "/output/target/" + bundle.artifactId + "-" + buildSettings.getVersion() + ".war");
    }

    public File getOutputBOM() {
        return new File(buildSettings.getTmpDir(), "/output/target/" + bundle.artifactId + "-" + buildSettings.getVersion() + ".bom.yml");
    }

    @CheckForNull
    public GroovyHookInfo getHookById(@Nonnull String id) {
        if (groovyHooks == null) {
            return null;
        }

        for (GroovyHookInfo hook : groovyHooks) {
            if (id.equals(hook.id)) {
                return hook;
            }
        }
        return null;
    }

    public List<WARResourceInfo> getAllExtraResources() {
        final List<WARResourceInfo> list = new ArrayList<>();
        if (groovyHooks != null) {
            list.addAll(groovyHooks);
        }
        if (casc != null) {
            list.addAll(casc);
        }
        return list;
    }

    @CheckForNull
    public WARResourceInfo findResourceById(@Nonnull String id) {
        for (WARResourceInfo hook : getAllExtraResources()) {
            if (id.equals(hook.id)) {
                return hook;
            }
        }
        return null;
    }

    @CheckForNull
    public DependencyInfo findPlugin(@Nonnull String artifactId) {
        if (plugins == null) {
            return null;
        }

        for (DependencyInfo plugin : plugins) {
            if (artifactId.equals(plugin.artifactId)) {
                return plugin;
            }
        }
        return null;
    }

    //TODO: support appending plugins in POM/BOM

    //TODO: add MANY options to make it configurable

    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH", justification = "plugins is initialized before")
    public void overrideByPOM(@Nonnull File tmpDir, @Nonnull File pom, final boolean pomIgnoreRoot) throws IOException, InterruptedException {
        MavenXpp3Reader rdr = new MavenXpp3Reader();
        Model model;
        try(FileInputStream istream = new FileInputStream(pom)) {
            model = rdr.read(istream);
        } catch (Exception ex) {
            throw new IOException("Failed to read POM: " + pom, ex);
        }

        MavenHelper helper = new MavenHelper(this);
        if (buildSettings.isPomIncludeWar()) {
            war = null;
            String jenkinsVersion = model.getProperties().getProperty("jenkins-war.version");
            if (StringUtils.isBlank(jenkinsVersion)) {
                jenkinsVersion = model.getProperties().getProperty("jenkins.version");
            }
            if (StringUtils.isNotBlank(jenkinsVersion)) {
                ComponentReference core = new ComponentReference();
                core.setVersion(jenkinsVersion);
                war = core.toWARDependencyInfo();
            }
        }

        plugins = new ArrayList<>();

        File destination = new File(tmpDir, "dependencies.txt");
        if (!destination.exists()) {
            if(!destination.createNewFile()){
                throw new IOException("Unable to create dependencies file");
            }
        }

        List<DependencyInfo> deps = helper.listDependenciesFromPom(tmpDir, pom, destination);
        PluginInfoProvider pluginInfoProvider = getPluginInfoProvider(helper, tmpDir);
        pluginInfoProvider.init();
        for (DependencyInfo dep : deps) {
            processMavenDep(pluginInfoProvider, dep, plugins);
        }

        if (!pomIgnoreRoot) {
            // Add the artifact itself, no validation as we assume the pom is from a plugin
            DependencyInfo res = new DependencyInfo();
            res.artifactId = model.getArtifactId();
            res.groupId = model.getGroupId();
            res.source = new SourceInfo();
            res.source.version = model.getVersion();
            plugins.add(res);
        }
    }

    private PluginInfoProvider getPluginInfoProvider(MavenHelper helper, File tmpDir) {
        return buildSettings != null ? buildSettings.getPluginInfoProvider(helper, tmpDir) : UpdateCenterPluginInfoProvider.DEFAULT;
    }

    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "Impossible in this case as every DependencyInfo has it's Source")
    private void processMavenDep(PluginInfoProvider pluginInfoProvider, DependencyInfo res, Collection<DependencyInfo> plugins) throws InterruptedException, IOException {
        if ("jar".equals(res.type) && buildSettings.isPomIncludeWar() && "org.jenkins-ci.main".equals(res.groupId) && "jenkins-core".equals(res.artifactId)) {
            ComponentReference core = new ComponentReference();
            core.setVersion(res.getSource().version);
            war = core.toWARDependencyInfo();
        } else if ("war".equals(res.type) && buildSettings.isPomIncludeWar() && "org.jenkins-ci.main".equals(res.groupId) && "jenkins-war".equals(res.artifactId)) {
            ComponentReference core = new ComponentReference();
            core.setVersion(res.getSource().version);
            war = core.toWARDependencyInfo();
        } else if (pluginInfoProvider.isPlugin(res)) { // Consult with the plugin info provider
            plugins.add(res);
        } else {
            LOGGER.log(Level.INFO, "Skipping dependency, not an HPI file: " + res);
        }
    }

    public void overrideByBOM(@Nonnull BOM bom, @CheckForNull String environmentName) throws IOException {
        final Specification spec = bom.getSpec();

        if (buildSettings.isBomIncludeWar()) {
            war = spec.getCore().toWARDependencyInfo();
        }

        // Bundle information
        // TODO: better merge logic?
        PackageInfo bomPackageInfo = null;
        Metadata metadata = bom.getMetadata();
        if (metadata != null) {
            bomPackageInfo = metadata.toPackageInfo();
        }
        if (bomPackageInfo != null) {
            this.bundle = bomPackageInfo;
        }

        Environment env = null;
        if (environmentName != null) {
            env = spec.getEnvironment(environmentName);
        }

        // Plugins
        plugins = new ArrayList<>();
        for (ComponentReference ref : spec.getPlugins()) {
            plugins.add(ref.toDependencyInfo());
        }
        if (env != null) {
            for (ComponentReference ref : env.getPlugins()) {
                plugins.add(ref.toDependencyInfo());
            }
        }

        // TODO(oleg_nenashev): I cannot determine component type.
        // We assume that all components are libs, but it will block roundtrips since BOMBuilder also injects scripts
        libPatches = new ArrayList<>();
        for (ComponentReference ref : spec.getComponents()) {
            libPatches.add(ref.toDependencyInfo());
        }
        if (env != null) {
            for (ComponentReference ref : env.getComponents()) {
                libPatches.add(ref.toDependencyInfo());
            }
        }
    }
}
