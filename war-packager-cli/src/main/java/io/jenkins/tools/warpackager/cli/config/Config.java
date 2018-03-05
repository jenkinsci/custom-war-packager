package io.jenkins.tools.warpackager.cli.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javax.annotation.CheckForNull;
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

    public PackageInfo bundle;
    public DependencyInfo war;
    public Collection<DependencyInfo> plugins;
    public Map<String, String> systemProperties;

    public static Config loadConfig(@CheckForNull File configPath) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        if (configPath != null) {
            if (configPath.exists() && configPath.isFile()) {
                try (FileInputStream istream = new FileInputStream(configPath)) {
                    return mapper.readValue(istream, Config.class);
                }
            } else {
                throw new FileNotFoundException("Cannot find the configuration file " + configPath);
            }
        } else {
            LOGGER.log(Level.WARNING, "Loading the default configuration sample from resource {0}/sample.yml", Config.class);
            try (InputStream istream = Config.class.getResourceAsStream("sample.yml")) {
                return mapper.readValue(istream, Config.class);
            }
        }
    }
}
