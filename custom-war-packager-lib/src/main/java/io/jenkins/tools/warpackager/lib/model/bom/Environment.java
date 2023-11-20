package io.jenkins.tools.warpackager.lib.model.bom;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import java.util.Collections;
import java.util.List;

/**
 * @author Oleg Nenashev
 * @since TODO
 */
@SuppressFBWarnings(value = "NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR", justification = "Fields come from JSON")
public class Environment {

    @NonNull
    @JsonProperty(required = true)
    String name;

    @CheckForNull
    List<ComponentReference> components;

    @CheckForNull
    List<ComponentReference> plugins;

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public List<ComponentReference> getComponents() {
        return components != null ? components : Collections.emptyList();
    }

    @NonNull
    public List<ComponentReference> getPlugins() {
        return plugins != null ? plugins : Collections.emptyList();
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public void setComponents(@CheckForNull List<ComponentReference> components) {
        this.components = components;
    }

    public void setPlugins(@CheckForNull List<ComponentReference> plugins) {
        this.plugins = plugins;
    }
}
