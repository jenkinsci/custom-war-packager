package io.jenkins.tools.warpackager.lib.config;

import org.junit.Test;
import org.jvnet.hudson.test.For;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Verifies the configuration management in the library
 * @author Oleg Nenashev
 */
@For(Config.class)
public class ConfigTest {

    @Test
    public void shouldBeAbleToLoadDefaultsFromSample() throws Exception {
        Config cfg = Config.loadDemoConfig();
        assertEquals("mywar", cfg.bundle.artifactId);
        assertEquals("jenkins-war", cfg.war.artifactId);
        assertNotNull("Build settings should be initialized with defaults", cfg.buildSettings);
    }
}
