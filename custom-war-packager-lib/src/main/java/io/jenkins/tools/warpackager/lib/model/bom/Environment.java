package io.jenkins.tools.warpackager.lib.model.bom;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * @author Oleg Nenashev
 * @since TODO
 */
@SuppressFBWarnings(value = "NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR", justification = "Fields come from JSON")
public class Environment {

    @Nonnull
    @JsonProperty(required = true)
    String name;

    @CheckForNull
    List<ComponentReference> components;

    @CheckForNull
    List<ComponentReference> plugins;

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public List<ComponentReference> getComponents() {
        return components != null ? components : Collections.emptyList();
    }

    @Nonnull
    public List<ComponentReference> getPlugins() {
        return plugins != null ? plugins : Collections.emptyList();
    }

    public void setName(@Nonnull String name) {
        this.name = name;
    }

    public void setComponents(@CheckForNull List<ComponentReference> components) {
        this.components = components;
    }

    public void setPlugins(@CheckForNull List<ComponentReference> plugins) {
        this.plugins = plugins;
    }
}
