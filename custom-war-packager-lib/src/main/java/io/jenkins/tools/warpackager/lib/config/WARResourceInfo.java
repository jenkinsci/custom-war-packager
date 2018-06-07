package io.jenkins.tools.warpackager.lib.config;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Abstraction for all resources being injected into WARs.
 * @author Oleg Nenashev
 * @since TODO
 */
@SuppressFBWarnings(value = "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", justification = "JSON Deserialization")
public abstract class WARResourceInfo {
    public String id;
    public SourceInfo source;

    public abstract String getResourceType();

    /**
     * Gets relative path to the resource within WAR.
     */
    public abstract String getDestination();
}
