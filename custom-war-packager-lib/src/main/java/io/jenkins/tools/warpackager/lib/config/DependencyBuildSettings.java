package io.jenkins.tools.warpackager.lib.config;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * @author Oleg Nenashev
 * @since TODO
 */
@SuppressFBWarnings(value = "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", justification = "JSON Deserialization")
public class DependencyBuildSettings {

    public static final DependencyBuildSettings DEFAULT = new DependencyBuildSettings();

    public boolean buildOriginalVersion;

    public boolean noCache;
}
