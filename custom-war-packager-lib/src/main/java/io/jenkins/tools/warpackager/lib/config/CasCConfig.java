package io.jenkins.tools.warpackager.lib.config;

/**
 * Provides integration with Configuration-as-Code plugin.
 * @author Oleg Nenashev
 * @since TODO
 */
public class CasCConfig extends WARResourceInfo {

    public static final String CASC_PLUGIN_ARTIFACT_ID = "configuration-as-code";

    @Override
    public String getDestination() {
        return "WEB-INF/jenkins.yaml.d";
    }

    @Override
    public String getResourceType() {
        return "jenkins.yaml";
    }
}
