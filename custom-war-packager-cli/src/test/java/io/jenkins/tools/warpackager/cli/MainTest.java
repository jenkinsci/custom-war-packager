package io.jenkins.tools.warpackager.cli;

import org.junit.Test;
import org.jvnet.hudson.test.For;

/**
 * @author Oleg Nenashev
 */
@For(Main.class)
public class MainTest {

    @Test
    public void demoShouldPass() throws Exception {
        Main.main(new String[] {"-demo"});
    }
}
