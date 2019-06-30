package io.jenkins.tools.warpackager.lib.model.bom;

import hudson.util.VersionNumber;
import io.jenkins.tools.warpackager.lib.config.Config;
import io.jenkins.tools.warpackager.lib.impl.BOMBuilder;
import io.jenkins.tools.warpackager.lib.model.ResolvedDependencies;
import io.jenkins.tools.warpackager.lib.model.ResolvedWARDependency;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.jvnet.hudson.test.For;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

@For(BOM.class)
public class BOMTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void roundtrip() throws IOException {
        BOM bom = BOM.load(BOMTest.class, "BOM.yml");
        spotcheck(bom);

        File target = new File(tmp.getRoot(), "bom.yml");
        bom.write(target);
        BOM reloaded = BOM.load(target);
        spotcheck(reloaded);
    }

    private void spotcheck(BOM bom) throws AssertionError {
        assertNotNull(bom.metadata);
        assertNotNull(bom.spec.core);
        assertNotNull(bom.spec.plugins);
        assertNotNull(bom.spec.environments);
        assertEquals("aws", bom.spec.environments.get(0).name);
    }

    @Test
    public void shouldProduceBOMfromConfig() throws Exception {
        Config cfg = Config.loadDemoConfig();
        ResolvedDependencies deps = new ResolvedDependencies(new ResolvedWARDependency(cfg.war.groupId,
                new VersionNumber(cfg.war.source.version), cfg.war));
        BOMBuilder bldr = new BOMBuilder(cfg).withResolvedDependencies(deps);
        BOM bom = bldr.build();

        File target = new File(tmp.getRoot(), "bom.yml");
        bom.write(target);

        BOM reloaded = BOM.load(target);
    }

}
