package io.jenkins.tools.warpackager.lib.model.bom;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.jenkins.tools.warpackager.lib.config.Config;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Defines BOM structure according to Jenkins JEP-TODO.
 * @author Oleg Nenashev
 * @since TODO
 */
@SuppressFBWarnings(value = "NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR", justification = "Fields come from JSON")
public class BOM {

    @CheckForNull
    Metadata metadata;

    @Nonnull
    @JsonProperty(required = true)
    public Specification spec;

    @CheckForNull
    Specification status;

    public void setMetadata(@CheckForNull Metadata metadata) {
        this.metadata = metadata;
    }

    public void setSpec(@Nonnull Specification spec) {
        this.spec = spec;
    }

    public void setStatus(@CheckForNull Specification status) {
        this.status = status;
    }

    @CheckForNull
    public Metadata getMetadata() {
        return metadata;
    }

    @Nonnull
    public Specification getSpec() {
        return spec;
    }

    @CheckForNull
    public Specification getStatus() {
        return status;
    }

    public void write(@Nonnull File configPath) throws IOException {
        try (FileOutputStream ostream = new FileOutputStream(configPath)) {
            write(ostream);
        }
    }

    public void write(@Nonnull OutputStream ostream) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.writeValue(ostream, this);
    }

    public static BOM load(@Nonnull File configPath) throws IOException {
        if (configPath.exists() && configPath.isFile()) {
            try (FileInputStream istream = new FileInputStream(configPath)) {
                return load(istream);
            }
        }
        throw new FileNotFoundException("Cannot find the BOM file " + configPath);
    }

    public static BOM load(@Nonnull InputStream istream) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        BOM loaded = mapper.readValue(istream, BOM.class);
        //if (loaded.spec == null) {
        //    throw new IOException("BOM Specification must be present");
        //}
        return loaded;
    }

    public static BOM load(Class<?> clazz, String resource) throws IOException {
        try (InputStream istream = clazz.getResourceAsStream(resource)) {
            if (istream == null) {
                throw new FileNotFoundException(String.format("Cannot load BOM from the resource file: %s/%s", clazz, resource));
            }
            return load(istream);
        }
    }
}
