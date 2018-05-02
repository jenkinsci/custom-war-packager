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
public class Specification {

    @Nonnull
    @JsonProperty(required = true)
    ComponentReference core;

    @CheckForNull
    List<ComponentReference> components;

    @CheckForNull
    List<ComponentReference> plugins;

    @CheckForNull
    List<Environment> environments;

    public void setCore(ComponentReference core) {
        this.core = core;
    }

    public void setComponents(@CheckForNull List<ComponentReference> components) {
        this.components = components;
    }

    public void setPlugins(@CheckForNull List<ComponentReference> plugins) {
        this.plugins = plugins;
    }

    public void setEnvironments(@CheckForNull List<Environment> environments) {
        this.environments = environments;
    }

    @CheckForNull
    public Environment getEnvironment(@Nonnull String name) {
        if (environments == null) {
            return null;
        }

        for (Environment env : environments) {
            if (name.equals(env.name)) {
                return env;
            }
        }
        return null;
    }

    @Nonnull
    public ComponentReference getCore() {
        return core;
    }

    @Nonnull
    public List<ComponentReference> getPlugins() {
        return plugins != null ? plugins : Collections.emptyList();
    }

    @Nonnull
    public List<ComponentReference> getComponents() {
        return components != null ? components : Collections.emptyList();
    }

    @Nonnull
    public List<Environment> getEnvironments() {
        return environments != null ? environments : Collections.emptyList();
    }
}
