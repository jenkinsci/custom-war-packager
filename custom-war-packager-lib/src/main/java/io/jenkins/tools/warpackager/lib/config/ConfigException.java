package io.jenkins.tools.warpackager.lib.config;

import java.io.IOException;

/**
 * Represents configuration exceptions like lack of configuration fields.
 * @since TODO
 */
public class ConfigException extends IOException {

    public ConfigException(String message) {
        super(message);
    }

    public ConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
