package io.jenkins.tools.warpackager.lib.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author Oleg Nenashev
 * @since TODO
 */
public class SystemCommandHelper {

    private SystemCommandHelper() {}

    public static void processFor(File buildDir, String ... args) throws IOException, InterruptedException {
        ProcessBuilder bldr = new ProcessBuilder(args).inheritIO();
        int res = runFor(buildDir, args);
        if (res != 0) {
            throw new IOException("Command failed with exit code " + res + ": " + StringUtils.join(bldr.command(), ' '));
        }
    }

    public static int runFor(File buildDir, String ... args) throws IOException, InterruptedException {
        ProcessBuilder bldr = new ProcessBuilder(args).inheritIO();
        bldr.directory(buildDir);
        return bldr.start().waitFor();
    }

    public static String readFor(File buildDir, String ... args) throws IOException, InterruptedException {
        ProcessBuilder bldr = new ProcessBuilder(args);
        bldr.directory(buildDir);
        Process proc = bldr.start();
        int res = proc.waitFor();
        String out = IOUtils.toString(proc.getInputStream(), Charset.defaultCharset()).trim();
        if (res != 0) {
            throw new IOException("Command failed with exit code " + res + ": " + StringUtils.join(bldr.command(), ' ') + ". " + out);
        }
        return out;
    }
}
