/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2018 CloudBees Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.jenkins.tools.warpackager.lib.util;

import io.jenkins.tools.warpackager.lib.model.bom.ComponentReference;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Helps reading manifests from files.
 * The original implementation has been taken from the Pipeline Utility Steps plugin for Jenkins.
 * @author Robert Sandell &lt;rsandell@cloudbees.com&gt;
 * @author Oleg Nenashev
 */
public class SimpleManifest {

    private Map<String, String> main;
    private Map<String, Map<String, String>> entries;

    protected SimpleManifest(Map<String, String> main, Map<String, Map<String, String>> entries) {
        this.main = Collections.unmodifiableMap(main);
        this.entries = Collections.unmodifiableMap(entries);
    }

    protected SimpleManifest(Manifest mf) {
        this(extractMainAttributes(mf), extractEntries(mf));
    }

    public Map<String, Map<String, String>> getEntries() {
        return entries;
    }

    public Map<String, String> getMain() {
        return main;
    }

    protected static Map<String, Map<String, String>> extractEntries(Manifest mf) {
        Map<String, Map<String, String>> mapMap = new HashMap<>();
        Map<String, Attributes> entries = mf.getEntries();
        for (Map.Entry<String, Attributes> entrySection : entries.entrySet()) {
            Map<String, String> map = new HashMap<>();
            for (Map.Entry<Object, Object> entry : entrySection.getValue().entrySet()) {
                map.put(entry.getKey().toString(), entry.getValue().toString());
            }
            mapMap.put(entrySection.getKey(), map);
        }
        return mapMap;
    }

    protected static Map<String, String> extractMainAttributes(Manifest mf) {
        Map<String, String> map = new HashMap<>();
        Attributes attributes = mf.getMainAttributes();
        for (Map.Entry<Object, Object> entry : attributes.entrySet()) {
            map.put(entry.getKey().toString(), entry.getValue().toString());
        }
        return map;
    }

    public static SimpleManifest parseText(String text) throws IOException {
        Manifest manifest = new Manifest(new ByteArrayInputStream(text.getBytes("UTF-8")));
        return new SimpleManifest(manifest);
    }

    public static SimpleManifest parseFile(File file) throws IOException, InterruptedException {

        if (!file.exists()) {
            throw new FileNotFoundException(file + " does not exist.");
        } else if (file.isDirectory()) {
            throw new FileNotFoundException(file + " is a directory.");
        }
        String lcName = file.getName().toLowerCase();
        if (lcName.endsWith(".jar") || lcName.endsWith(".war") || lcName.endsWith(".ear") || lcName.endsWith(".jpi") || lcName.endsWith(".hpi")) {
            try (ZipFile zip = new ZipFile(file)) {
                ZipEntry e = zip.getEntry("META-INF/MANIFEST.MF");
                if (e == null) {
                    throw new FileNotFoundException("Cannot find META-INF/MANIFEST.MF in archive " + file);
                }
                try (InputStream is = zip.getInputStream(e)) {
                    return new SimpleManifest(new Manifest(is));
                }
            }
        } else {
            try (InputStream is = new FileInputStream(file)) {
                return new SimpleManifest(new Manifest(is));
            }
        }
    }

    public static ComponentReference readPluginManifest(@Nonnull File sourceHPI) throws IOException, InterruptedException {
        Map<String, String> manifest = SimpleManifest.parseFile(sourceHPI).getMain();

        ComponentReference res = new ComponentReference();
        res.setGroupId(manifest.get("Group-Id"));
        res.setArtifactId(manifest.get("Short-Name"));
        res.setVersion(manifest.get("Plugin-Version").split("\\s+")[0]);

        return res;
    }
}
