package io.jenkins.tools.warpackager.lib.config;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Abstraction over core libraries that have to be modified via system properties in core at build time
 */
@SuppressFBWarnings(value = "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", justification = "JSON Deserialization")
public class LibraryInfo {

    public String property;

    public DependencyInfo source;

    public String getProperty() {
        return property;
    }

    public DependencyInfo getSource() {
        return source;
    }
}
