package io.jenkins.tools.warpackager.lib.impl;

//TODO: This code should finally go to the Standard Maven HPI Plugin

import com.sun.org.apache.xml.internal.security.utils.XMLUtils;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import io.jenkins.tools.warpackager.lib.config.Config;
import io.jenkins.tools.warpackager.lib.config.GroovyHookInfo;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Custom stub for patching WAR files
 * @author Oleg Nenashev
 * @since TODO
 */
public class JenkinsWarPatcher {

    private static final Logger LOGGER = Logger.getLogger(JenkinsWarPatcher.class.getName());

    private final Config config;
    private final File srcWar;
    private final File dstDir;

    public JenkinsWarPatcher(@Nonnull Config config, @Nonnull File src, @Nonnull File dstDir) throws IOException {
        if (src.equals(dstDir)) {
            throw new IOException("Source and destination are the same: " + src);
        }
        this.config = config;
        this.srcWar = src;
        this.dstDir = dstDir;
        Files.createDirectories(dstDir.toPath());
        explode(new HashSet<>(Arrays.asList("WEB-INF/web.xml")));
    }

    @Nonnull
    private void explode(@Nonnull Set<String> excludes) throws IOException {
        try (ZipFile zip = new ZipFile(srcWar)) {
            Enumeration<? extends ZipEntry> it = zip.entries();
            while (it.hasMoreElements()) {
                ZipEntry e = it.nextElement();
                if (!excludes.contains(e.getName()) && !e.isDirectory()) {
                    File f = new File(dstDir, e.getName());
                    Path parent = f.toPath().getParent();
                    if (!Files.exists(parent)) {
                        Files.createDirectories(parent);
                    }
                    try(InputStream content = zip.getInputStream(e) ; FileOutputStream out = new FileOutputStream(f)) {
                        out.write(IOUtils.toByteArray(content));
                    }
                }
            }
        } catch (Exception ex) {
            throw new IOException("Failed copy " + srcWar, ex);
        }
    }

    public JenkinsWarPatcher removeMetaInf() throws IOException {
        File p = new File(dstDir, "META-INF");
        if (p.exists()) {
            FileUtils.deleteDirectory(p);
        }
        return this;
    }

    public JenkinsWarPatcher addHooks(@Nonnull Map<String, File> hooks) throws IOException {
        for (Map.Entry<String, File> hookSrc : hooks.entrySet()) {
            final String hookId = hookSrc.getKey();
            GroovyHookInfo hook = config.getHookById(hookId);
            addHook(hook, hookSrc.getValue());
        }

        return this;
    }

    public void addHook(@Nonnull GroovyHookInfo hook, File path) throws IOException {
        File targetDir = new File(dstDir, "WEB-INF/" + hook.type + ".groovy.d");
        if (!targetDir.exists()) {
            Files.createDirectories(targetDir.toPath());
        }

        if (path.isFile()) {
            Files.copy(path.toPath(), new File(targetDir, path.getName()).toPath());
        } else {
            FileUtils.copyDirectory(path, targetDir);
        }

    }

    @Nonnull
    private Document readXMLResource(String path) throws IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try (ZipFile zip = new ZipFile(srcWar)) {
            try(InputStream webXml = zip.getInputStream(zip.getEntry(path))) {
                DocumentBuilder db = dbf.newDocumentBuilder();
                return db.parse(webXml);
            }
        } catch (Exception ex) {
            throw new IOException("Failed to parse XML " + path + " from " + srcWar, ex);
        }
    }

    @Nonnull
    private void writeXMLResource(String path, Document doc) throws IOException {
        File out = new File(dstDir, path);
        Path parent = out.toPath().getParent();
        if (!Files.exists(parent)) {
            Files.createDirectories(parent);
        }
        try(FileOutputStream ostream = new FileOutputStream(out)) {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(ostream);
            transformer.transform(source, result);
        } catch (TransformerException ex) {
            throw new IOException("Failed to generate the XML resource", ex);
        }
    }

    public JenkinsWarPatcher addSystemProperties(Map<String, String> systemProperties) throws IOException {

        LOGGER.log(Level.WARNING, "The logic support only System properties using the jenkins.util.SystemProperties engine");

        Document doc = readXMLResource("WEB-INF/web.xml");

        Set<String> overridden = new HashSet<>();
        NodeList nodes = doc.getElementsByTagName("context-param");
        for (int i = 0 ; i < nodes.getLength() ; ++i) {
            Node node = nodes.item(i);
            Node paramName = findByName(node, "param-name");
            if (paramName == null) {
                throw new IOException("No param-name in node " + node);
            }
            String propertyName = paramName.getTextContent();
            if (systemProperties.containsKey(propertyName)) {
                LOGGER.log(Level.INFO, "Overriding property {0}", propertyName);
                setByName(node, "param-value", systemProperties.get(propertyName));
            }
            overridden.add(propertyName);
        }

        // Add non-overridden values
        if (systemProperties != null) {
            for (Map.Entry<String, String> entry : systemProperties.entrySet()) {
                if (overridden.contains(entry.getKey())) {
                    continue; // Already modified
                }

                LOGGER.log(Level.INFO, "Adding property {0}", entry.getKey());
                final Element newParam = doc.createElement("context-param");
                setByName(newParam, "param-name", entry.getKey());
                setByName(newParam, "param-value", entry.getValue());
                doc.getDocumentElement().appendChild(newParam);
            }
        }

        writeXMLResource("WEB-INF/web.xml", doc);
        return this;
    }

    @CheckForNull
    private static Node findByName(@Nonnull Node parent, @Nonnull String name) {
        NodeList nodes = parent.getChildNodes();
        for (int i = 0 ; i < nodes.getLength() ; ++i) {
            Node node = nodes.item(i);
            if (name.equals(node.getNodeName())) {
                return node;
            }
        }
        return null;
    }

    @CheckForNull
    private static void setByName(@Nonnull Node parent, @Nonnull String name, @Nonnull String value) {
        Node node = findByName(parent, name);
        if (node != null) {
            node.setTextContent(value);
        } else {
            Element newNode = parent.getOwnerDocument().createElement(name);
            newNode.setTextContent(value);
            parent.appendChild(newNode);
        }
    }

}
