package io.jenkins.tools.warpackager.cli.config;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Oleg Nenashev
 * @since TODO
 */
public class ConfigTest {

    @Test
    public void shouldBeAbleToLoadDefaultsFromSample() throws Exception {
        Config cfg = Config.loadConfig(null);
        assertEquals("mywar", cfg.bundle.artifactId);
        assertEquals("jenkins-war", cfg.war.artifactId);
    }
}
