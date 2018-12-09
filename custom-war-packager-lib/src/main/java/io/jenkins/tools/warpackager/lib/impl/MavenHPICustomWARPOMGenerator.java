package io.jenkins.tools.warpackager.lib.impl;

import io.jenkins.tools.warpackager.lib.config.Config;
import io.jenkins.tools.warpackager.lib.config.DependencyInfo;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.Repository;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Generates pom.xml for Maven HPI Plugin's custom-war step.
 * @author Oleg Nenashev
 * @since TODO
 */
public class MavenHPICustomWARPOMGenerator extends POMGenerator {

    private final String outputFileSuffix;

    public MavenHPICustomWARPOMGenerator(Config config, String outputFileSuffix) {
        super(config);
        this.outputFileSuffix = outputFileSuffix;
    }

    public Model generatePOM(Map<String, String> versionOverrides) throws IOException {
        Model model = new Model();
        model.setModelVersion("4.0.0");
        model.setGroupId(config.bundle.groupId);
        model.setArtifactId(config.bundle.artifactId);
        if (config.bundle.description != null) {
            model.setDescription(config.bundle.description);
        }
        model.setVersion(config.buildSettings.getVersion());

        addUTF8SourceEncodingProperty(model);
        
        // WAR Dependency
        Dependency dep = config.war.toDependency(versionOverrides);
        dep.setScope("test");
        dep.setType("war");
        model.addDependency(dep);

        // Plugins
        if (config.plugins != null) {
            for (DependencyInfo plugin : config.plugins) {
                Dependency pluginDep = plugin.toDependency(versionOverrides);
                pluginDep.setScope("runtime");
                model.addDependency(pluginDep);
            }
        }

        // Maven HPI Plugin
        /* Sample:
              <plugin>
                <groupId>≈/groupId>
                <artifactId>maven-hpi-plugin</artifactId>
                <version>2.2</version>
                <executions>
                  <execution>
                    <id>package-war</id>
                    <goals>
                      <goal>custom-war</goal>
                    </goals>
                    <configuration>
                      <outputFile>${build.directory}/tools-modified.war</outputFile>
                    </configuration>
                  </execution>
                </executions>
              </plugin>
         */

        // Maven repositories
        addRepositories(model);

        Plugin mavenHPIPlugin = new Plugin();
        mavenHPIPlugin.setGroupId("org.jenkins-ci.tools");
        mavenHPIPlugin.setArtifactId("maven-hpi-plugin");
        mavenHPIPlugin.setVersion("2.2"); // TODO: make configurable
        PluginExecution execution = new PluginExecution();
        execution.setId("package-war");
        execution.addGoal("custom-war");
        execution.setConfiguration(generateCustomWarGoalConfiguration());
        mavenHPIPlugin.addExecution(execution);

        Build build = new Build();
        build.addPlugin(mavenHPIPlugin);
        model.setBuild(build);

        return model;
    }

    private Object generateCustomWarGoalConfiguration() {
        Map<String, String> config = new HashMap<>();
        config.put("outputFile", "${project.build.directory}/${project.artifactId}" + outputFileSuffix + ".war");
        return generateCustomWarGoalConfigurationDom(config);
    }

    private Object generateCustomWarGoalConfigurationDom(Map<String, String> args) {
        Xpp3Dom dom = new Xpp3Dom("configuration");
        for (Map.Entry<String, String> entry : args.entrySet()) {
            Xpp3Dom node = new Xpp3Dom(entry.getKey());
            node.setValue(entry.getValue());
            dom.addChild(node);
        }

        // Also add System Properties
        /*
        Xpp3Dom systemPropertiesDom = new Xpp3Dom("systemProperties");
        for (Map.Entry<String, String> entry : config.systemProperties.entrySet()) {
            Xpp3Dom node = new Xpp3Dom(entry.getKey());
            node.setValue(entry.getValue());
            systemPropertiesDom.addChild(node);
        }
        dom.addChild(systemPropertiesDom);
*/

        return dom;
    }

    public void writePOM(Model model, File targetDir) throws IOException {
        File pom = new File(targetDir, "pom.xml");
        try(OutputStream ostream = new FileOutputStream(pom)) {
            new MavenXpp3Writer().write(ostream, model);
        }
    }
}
