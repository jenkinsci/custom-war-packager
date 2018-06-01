package io.jenkins.tools.warpackager.lib.config;

/**
 * Abstraction for all resources being injected into WARs.
 * @author Oleg Nenashev
 * @since TODO
 */
public abstract class WARResourceInfo {
    public String id;
    public SourceInfo source;

    public abstract String getResourceType();

    /**
     * Gets relative path to the resource within WAR.
     */
    public abstract String getDestination();
}
