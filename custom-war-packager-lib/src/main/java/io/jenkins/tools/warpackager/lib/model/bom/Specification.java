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
public class Specification {

    @NonNull
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
    public Environment getEnvironment(@NonNull String name) {
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

    @NonNull
    public ComponentReference getCore() {
        return core;
    }

    @NonNull
    public List<ComponentReference> getPlugins() {
        return plugins != null ? plugins : Collections.emptyList();
    }

    @NonNull
    public List<ComponentReference> getComponents() {
        return components != null ? components : Collections.emptyList();
    }

    @NonNull
    public List<Environment> getEnvironments() {
        return environments != null ? environments : Collections.emptyList();
    }
}
