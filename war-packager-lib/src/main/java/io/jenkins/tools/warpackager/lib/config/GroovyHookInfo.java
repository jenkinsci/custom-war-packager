package io.jenkins.tools.warpackager.lib.config;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Describes groovy hook to be used.
 * @author Oleg Nenashev
 */
@SuppressFBWarnings(value = "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", justification = "Comes from YAML")
public class GroovyHookInfo {
    public String type;
    public String id;
    public SourceInfo source;
}
