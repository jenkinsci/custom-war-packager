package io.jenkins.tools.warpackager.lib.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * @author Oleg Nenashev
 * @since TODO
 */
public class SystemCommandHelper {

    // https://stackoverflow.com/a/228499
    private static final String OS_NAME = System.getProperty("os.name");
    private static final boolean IS_WINDOWS = OS_NAME != null && OS_NAME.startsWith("Windows");
    //https://stackoverflow.com/a/17120829
    private static final String[] WINDOWS_PROCESS_ARGS_PREFIX = {"cmd.exe", "/C"};

    private SystemCommandHelper() {}

    private static ProcessBuilder createProcessBuilder(final String[] args) {
        String[] combined = args;
        if (IS_WINDOWS) {
            combined = Stream.concat(Arrays.stream(WINDOWS_PROCESS_ARGS_PREFIX), Arrays.stream(args))
                    .toArray(String[]::new);
        }
        return new ProcessBuilder(combined);
    }

    public static void processFor(File buildDir, String ... args) throws IOException, InterruptedException {
        ProcessBuilder bldr = createProcessBuilder(args).inheritIO();
        int res = runFor(buildDir, args);
        if (res != 0) {
            throw new IOException("Command failed with exit code " + res + ": " + StringUtils.join(bldr.command(), ' '));
        }
    }

    public static int runFor(File buildDir, String ... args) throws IOException, InterruptedException {
        ProcessBuilder bldr = createProcessBuilder(args).inheritIO();
        bldr.directory(buildDir);
        return bldr.start().waitFor();
    }

    public static String readFor(File buildDir, String ... args) throws IOException, InterruptedException {
        ProcessBuilder bldr = createProcessBuilder(args);
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
