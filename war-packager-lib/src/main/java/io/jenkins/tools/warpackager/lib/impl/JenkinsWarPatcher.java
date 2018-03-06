package io.jenkins.tools.warpackager.lib.impl;

//TODO: This code should finally go to the Standard Maven HPI Plugin

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Custom stub for patching WAR files
 * @author Oleg Nenashev
 * @since TODO
 */
public class JenkinsWarPatcher {

    private static final Logger LOGGER = Logger.getLogger(JenkinsWarPatcher.class.getName());

    private final File warFile;

    public JenkinsWarPatcher(File war) {
        this.warFile = war;
    }

    public void addSystemProperties(Map<String, String> systemProperties) throws IOException {
        if (systemProperties != null && !systemProperties.isEmpty()) {
            LOGGER.log(Level.WARNING, "Support of system properties has not been implemented yet. Arguments are ignored");
        }
        /*if (systemProperties.isEmpty()) {
            return;
        }

        try (ZipFile zip = new ZipFile(warFile)) {
            try(InputStream webXml = zip.getInputStream(zip.getEntry("WEB-INF/web.xml"))) {
                String xml = IOUtils.toString(webXml, Charset.defaultCharset());
            }
        }*/
    }

}
