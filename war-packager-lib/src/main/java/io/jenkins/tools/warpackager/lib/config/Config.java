package io.jenkins.tools.warpackager.lib.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
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
    public DependencyInfo war;
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

    private static Config load(@Nonnull InputStream istream) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Config loaded = mapper.readValue(istream, Config.class);
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
            return load(istream);
        }
    }

    public static Config loadConfig(@Nonnull File configPath) throws IOException {
        if (configPath.exists() && configPath.isFile()) {
            try (FileInputStream istream = new FileInputStream(configPath)) {
                return load(istream);
            }
        }
        throw new FileNotFoundException("Cannot find the configuration file " + configPath);
    }

    // TODO: make the destination configurable
    public File getOutputWar() {
        return new File(buildSettings.getTmpDir(), "/output/target/" + bundle.artifactId + ".war");
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
}
