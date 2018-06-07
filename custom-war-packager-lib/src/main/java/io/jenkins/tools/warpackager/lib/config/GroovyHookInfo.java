package io.jenkins.tools.warpackager.lib.config;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Describes groovy hook to be used.
 * @author Oleg Nenashev
 */
@SuppressFBWarnings(value = "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", justification = "Comes from YAML")
public class GroovyHookInfo extends WARResourceInfo {
    public String type;

    @Override
    public String getResourceType() {
        return "groovy-hooks." + type;
    }

    @Override
    public String getDestination() {
        return "WEB-INF/" + type + ".groovy.d";
    }
}
