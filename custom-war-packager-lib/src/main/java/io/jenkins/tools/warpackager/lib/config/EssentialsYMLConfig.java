package io.jenkins.tools.warpackager.lib.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.annotation.CheckForNull;

/**
 * Wrapper for {@code essentials.yml} configuration files.
 * It does not load all properties, but just {@code packaging} section for {@link Config}.
 * @author Oleg Nenashev
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EssentialsYMLConfig {

    @CheckForNull
    public Packaging packaging;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Packaging {

        @CheckForNull
        public Config config;

        @CheckForNull
        public String configFile;
    }
}
