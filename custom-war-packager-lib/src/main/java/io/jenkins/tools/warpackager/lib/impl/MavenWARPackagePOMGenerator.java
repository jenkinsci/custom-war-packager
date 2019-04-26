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

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * Generates pom.xml for Maven WAR Plugin.
 * @author Oleg Nenashev
 * @since TODO
 */
public class MavenWARPackagePOMGenerator extends POMGenerator {

    private final File sourceWar;

    public MavenWARPackagePOMGenerator(Config config, File sourceWar) {
        super(config);
        this.sourceWar = sourceWar;
    }

    public Model generatePOM(Map<String, String> injectedManifestEntries) throws IOException {
        Model model = new Model();
        model.setModelVersion("4.0.0");
        model.setGroupId(config.bundle.groupId);
        model.setArtifactId(config.bundle.artifactId);
        model.setDescription(config.bundle.description);
        model.setVersion(config.buildSettings.getVersion());
        model.setPackaging("war");

        addUTF8SourceEncodingProperty(model);

        HashMap<String, String> manifestEntries = new HashMap<>(injectedManifestEntries);
        manifestEntries.put("Build-Time", "${maven.build.timestamp}");
        manifestEntries.put("Built-By", "${user.name}");
        addIfNotNull(manifestEntries, "Implementation-Title", config.bundle.title);
        addIfNotNull(manifestEntries,"Implementation-Vendor", config.bundle.vendor);
        addIfNotNull(manifestEntries,"Implementation-Version", config.buildSettings.getVersion());

        // Maven HPI Plugin
        /* Sample:
            <build>
              <plugins>
                <plugin>
                  <artifactId>maven-war-plugin</artifactId>
                  <version>3.0.0</version>
                  <goals>
                    <goal>war</goal>
                  </goals>
                  <configuration>
                      <filteringDeploymentDescriptors>true</filteringDeploymentDescriptors>
                      <webResources>
                          <resource>
                              <directory>war-tmp</directory>
                          </resource>
                      </webResources>
                      <archive>
                          <manifest>
                              <mainClass>Main</mainClass>
                          </manifest>
                          <manifestEntries>
                            <Implementation-Title>Test</Implementation-Title>
                            <Implementation-Vendor>${project.organization.name}</Implementation-Vendor>
                            <Implementation-Version>${project.version}</Implementation-Version>
                            <Hudson-Version>1.395</Hudson-Version>
                            <Jenkins-Version>1.2.3</Jenkins-Version>
                            <Build-Time>${maven.build.timestamp}</Build-Time>
                            <Remoting-Minimum-Supported-Version>2.60</Remoting-Minimum-Supported-Version>
                            <Remoting-Embedded-Version>3.17</Remoting-Embedded-Version>
                          </manifestEntries>
                      </archive>
                  </configuration>
                </plugin>
              </plugin
         */

        // Maven repositories
        addRepositories(model);

        Plugin mavenHPIPlugin = new Plugin();
        mavenHPIPlugin.setGroupId("org.apache.maven.plugins");
        mavenHPIPlugin.setArtifactId("maven-war-plugin");
        mavenHPIPlugin.setVersion("3.0.0");
        mavenHPIPlugin.setConfiguration(generateMavenWarPluginConfiguration(manifestEntries));
        PluginExecution execution = new PluginExecution();
        execution.setId("package-war");
        execution.addGoal("war");
        execution.setConfiguration(generateCustomWarGoalConfiguration(manifestEntries));
        mavenHPIPlugin.addExecution(execution);

        Build build = new Build();
        build.addPlugin(mavenHPIPlugin);
        model.setBuild(build);

        return model;
    }

    private static void addIfNotNull(@Nonnull Map<String, String> map, @Nonnull String key, @CheckForNull String value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    private Xpp3Dom generateMavenWarPluginConfiguration(Map<String, String> manifestEntries) {
        Xpp3Dom webXml = new Xpp3Dom("webXml");
        webXml.setValue(new File(sourceWar, "WEB-INF/web.xml").getAbsolutePath());

        Xpp3Dom dom = new Xpp3Dom("configuration");
        dom.addChild(webXml);
        return dom;
    }

    private Xpp3Dom generateCustomWarGoalConfiguration(Map<String, String> manifestEntries) {
        Xpp3Dom filteringDeploymentDescriptors = new Xpp3Dom("filteringDeploymentDescriptors");
        filteringDeploymentDescriptors.setValue("true");

        Xpp3Dom webResources = new Xpp3Dom("webResources");
        Xpp3Dom directory = new Xpp3Dom("directory");
        directory.setValue(sourceWar.getAbsolutePath());
        Xpp3Dom resource = new Xpp3Dom("resource");
        resource.addChild(directory);
        webResources.addChild(resource);

        //
        Xpp3Dom archive = new Xpp3Dom("archive");
        Xpp3Dom manifest = new Xpp3Dom("manifest");
        Xpp3Dom mainClass = new Xpp3Dom("mainClass");
        mainClass.setValue("Main");
        manifest.addChild(mainClass);
        archive.addChild(manifest);

        Xpp3Dom manifestEntriesNode = addKeyValueMapArray("manifestEntries", manifestEntries);
        archive.addChild(manifestEntriesNode);

        Xpp3Dom dom = new Xpp3Dom("configuration");
        dom.addChild(filteringDeploymentDescriptors);
        dom.addChild(webResources);
        dom.addChild(archive);
        return dom;
    }

    private Xpp3Dom addKeyValueMapArray(String rootName, Map<String, String> args) {
        Xpp3Dom dom = new Xpp3Dom(rootName);
        for (Map.Entry<String, String> entry : args.entrySet()) {
            Xpp3Dom node = new Xpp3Dom(entry.getKey());
            node.setValue(entry.getValue());
            dom.addChild(node);
        }

        return dom;
    }

    public void writePOM(Model model, File targetDir) throws IOException {
        if (!targetDir.exists()) {
            Files.createDirectories(targetDir.toPath());
        }
        File pom = new File(targetDir, "pom.xml");
        try(OutputStream ostream = new FileOutputStream(pom)) {
            new MavenXpp3Writer().write(ostream, model);
        }
    }
}
