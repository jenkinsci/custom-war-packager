package io.jenkins.tools.warpackager.lib.impl;

import io.jenkins.tools.warpackager.lib.config.Config;
import io.jenkins.tools.warpackager.lib.util.MavenHelper;

/**
 * Base class for WAR packaging.
 */
public class PackagerBase {

    protected final Config config;
    protected final MavenHelper mavenHelper;

    public PackagerBase(Config config) {
        this.config = config;
        this.mavenHelper = new MavenHelper(config);
    }
}
