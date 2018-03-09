package io.jenkins.tools.warpackager.lib.impl;

//TODO: This code should finally go to the Standard Maven HPI Plugin

import com.sun.org.apache.xml.internal.security.utils.XMLUtils;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
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
public class JenkinsWarPatcher implements Closeable {

    private static final Logger LOGGER = Logger.getLogger(JenkinsWarPatcher.class.getName());

    private final File srcWar;
    private final ZipOutputStream ostream;

    public JenkinsWarPatcher(@Nonnull File src, @Nonnull File dst) throws IOException {
        if (src.equals(dst)) {
            throw new IOException("Source and destination are the same: " + src);
        }
        this.srcWar = src;
        ostream = new ZipOutputStream(new FileOutputStream(dst));
        copy(new HashSet<>(Arrays.asList("WEB-INF/web.xml")));
    }

    @Override
    public void close() throws IOException {
        ostream.close();
    }

    @Nonnull
    private void copy(@Nonnull Set<String> excludes) throws IOException {
        try (ZipFile zip = new ZipFile(srcWar)) {
            Enumeration<? extends ZipEntry> it = zip.entries();
            while (it.hasMoreElements()) {
                ZipEntry e = it.nextElement();
                if (!excludes.contains(e.getName())) {
                    ostream.putNextEntry(e);
                    try(InputStream content = zip.getInputStream(e)) {
                        ostream.write(IOUtils.toByteArray(content));
                    }
                }
            }
        } catch (Exception ex) {
            throw new IOException("Failed copy " + srcWar, ex);
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
        ZipEntry e = new ZipEntry(path);
        ostream.putNextEntry(e);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(baos);
            transformer.transform(source, result);
        } catch (TransformerException ex) {
            throw new IOException("", ex);
        }
        ostream.write(baos.toByteArray());
    }

    public void addSystemProperties(Map<String, String> systemProperties) throws IOException {

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
