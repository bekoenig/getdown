//
// Getdown - application installer, patcher and launcher
// Copyright (C) 2004-2018 Getdown authors
// https://github.com/bekoenig/getdown/blob/master/LICENSE

package io.github.bekoenig.getdown.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;

/**
 * Useful routines for launching Java applications from within other Java
 * applications.
 */
public final class LaunchUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(LaunchUtil.class);

    /**
     * The default directory into which a local VM installation should be unpacked.
     */
    public static final String LOCAL_JAVA_DIR = "java_vm";

    /**
     * Writes a {@code version.txt} file into the specified application directory and
     * attempts to relaunch Getdown in that directory which will cause it to upgrade to the newly
     * specified version and relaunch the application.
     *
     * @param appdir         the directory in which the application is installed.
     * @param getdownJarName the name of the getdown jar file in the application directory. This is
     *                       probably {@code getdown-pro.jar} or {@code getdown-retro-pro.jar} if you are using
     *                       the results of the standard build.
     * @param newVersion     the new version to which Getdown will update when it is executed.
     * @param javaLocalDir   the name of the directory (inside {@code appdir}) that contains a
     *                       locally installed JRE. Defaults to {@link #LOCAL_JAVA_DIR} if null is passed.
     * @return true if the relaunch succeeded. If true is returned, the application may exit
     * after making this call as it will be upgraded and restarted. If false is returned, the
     * application should tell the user that they must restart the application manually.
     * @throws IOException thrown if we were unable to create the {@code version.txt} file
     *                     in the supplied application directory. If the version.txt file cannot be created, restarting
     *                     Getdown will not cause the application to be upgraded, so the application will have to
     *                     resort to telling the user that it is in a bad way.
     */
    public static boolean updateVersionAndRelaunch(
        File appdir, String getdownJarName, String newVersion, String javaLocalDir)
        throws IOException {
        // create the file that instructs Getdown to upgrade
        File vfile = new File(appdir, "version.txt");
        try (PrintStream ps = new PrintStream(Files.newOutputStream(vfile.toPath()))) {
            ps.println(newVersion);
        }

        // make sure that we can find our getdown.jar file and can safely launch children
        File pro = new File(appdir, getdownJarName);
        if (!pro.exists()) {
            return false;
        }

        // do the deed
        String javaDir = StringUtil.isBlank(javaLocalDir) ? LOCAL_JAVA_DIR : javaLocalDir;
        String javaBin = getJVMBinaryPath(new File(appdir, javaDir), false);
        String[] args = {javaBin, "-jar", pro.toString(), appdir.getPath()};
        LOGGER.atInfo()
            .setMessage("Running {}")
            .addArgument(() -> StringUtil.join(args, "\n  "))
            .log();
        try {
            Runtime.getRuntime().exec(args, null);
            return true;
        } catch (IOException ioe) {
            LOGGER.warn("Failed to run getdown", ioe);
            return false;
        }
    }

    /**
     * Resolves a path to a JVM binary.
     *
     * @param javaLocalDir JRE location within appdir.
     * @param windebug     if true we will use java.exe instead of javaw.exe on Windows.
     * @return the path to the JVM binary used to launch this process.
     */
    public static String getJVMBinaryPath(File javaLocalDir, boolean windebug) {
        // first look in our application directory for an installed VM
        String vmpath = checkJVMPath(javaLocalDir.getAbsolutePath(), windebug);

        // then fall back to the VM in which we're already running
        if (vmpath == null) {
            vmpath = checkJVMPath(System.getProperty("java.home"), windebug);
        }

        // then throw up our hands and hope for the best
        if (vmpath == null) {
            LOGGER.atWarn()
                .setMessage("Unable to find java!")
                .addKeyValue("local", javaLocalDir)
                .addKeyValue("java.home", System.getProperty("java.home"))
                .log();
            vmpath = "java";
        }

        // Oddly, the Mac OS X specific java flag -Xdock:name will only work if java is launched
        // from /usr/bin/java, and not if launched by directly referring to <java.home>/bin/java,
        // even though the former is a symlink to the latter! To work around this, see if the
        // desired jvm is in fact pointed to by /usr/bin/java and, if so, use that instead.
        if (isMacOS()) {
            try {
                File localVM = new File("/usr/bin/java").getCanonicalFile();
                if (localVM.equals(new File(vmpath).getCanonicalFile())) {
                    vmpath = "/usr/bin/java";
                }
            } catch (IOException ioe) {
                LOGGER.warn("Failed to check Mac OS canonical VM path.", ioe);
            }
        }

        return vmpath;
    }

    /**
     * Upgrades Getdown by moving an installation managed copy of the Getdown jar file over the
     * non-managed copy (which would be used to run Getdown itself).
     *
     * <p> If the upgrade fails for a variety of reasons, warnings are logged but no other actions
     * are taken. There's not much else one can do other than try again next time around.
     */
    public static void upgradeGetdown(File oldgd, File curgd, File newgd) {
        // we assume getdown's jar file size changes with every upgrade, this is not guaranteed,
        // but in reality it will, and it allows us to avoid pointlessly upgrading getdown every
        // time the client is updated which is unnecessarily flirting with danger
        if (!newgd.exists() || newgd.length() == curgd.length()) {
            return;
        }

        LOGGER.info("Updating Getdown with {}...", newgd);

        // clear out any old getdown
        if (oldgd.exists()) {
            FileUtil.deleteHarder(oldgd);
        }

        // now try updating using renames
        if (!curgd.exists() || curgd.renameTo(oldgd)) {
            if (newgd.renameTo(curgd)) {
                FileUtil.deleteHarder(oldgd); // yay!
                try {
                    // copy the moved file back to newgd so that we don't end up
                    // downloading another copy next time
                    FileUtil.copy(curgd, newgd);
                } catch (IOException e) {
                    LOGGER.warn("Error copying updated Getdown back", e);
                }
                return;
            }

            LOGGER.warn("Unable to renameTo({}).", oldgd);
            // try to unfuck ourselves
            if (!oldgd.renameTo(curgd)) {
                LOGGER.warn("Oh God, why dost thee scorn me so.");
            }
        }

        // that didn't work, let's try copying it
        LOGGER.info("Attempting to upgrade by copying over {}...", curgd);
        try {
            FileUtil.copy(newgd, curgd);
        } catch (IOException ioe) {
            LOGGER.warn("Mayday! Brute force copy method also failed.", ioe);
        }
    }

    /**
     * Returns true if we're running in a JVM that identifies its operating system as Windows.
     */
    public static boolean isWindows() {
        return _isWindows;
    }

    /**
     * Returns true if we're running in a JVM that identifies its operating system as MacOS.
     */
    public static boolean isMacOS() {
        return _isMacOS;
    }

    /**
     * Returns true if we're running in a JVM that identifies its operating system as Linux.
     */
    public static boolean isLinux() {
        return _isLinux;
    }

    /**
     * Checks whether a Java Virtual Machine can be located in the supplied path.
     */
    private static String checkJVMPath(String vmhome, boolean windebug) {
        String vmbase = vmhome + File.separator + "bin" + File.separator;
        String vmpath = vmbase + "java";
        if (new File(vmpath).exists()) {
            return vmpath;
        }

        if (!windebug) {
            vmpath = vmbase + "javaw.exe";
            if (new File(vmpath).exists()) {
                return vmpath;
            }
        }

        vmpath = vmbase + "java.exe";
        if (new File(vmpath).exists()) {
            return vmpath;
        }

        return null;
    }

    /**
     * Flag indicating that we're on Windows; initialized when this class is first loaded.
     */
    private static boolean _isWindows;
    /**
     * Flag indicating that we're on MacOS; initialized when this class is first loaded.
     */
    private static boolean _isMacOS;
    /**
     * Flag indicating that we're on Linux; initialized when this class is first loaded.
     */
    private static boolean _isLinux;

    static {
        try {
            String osname = System.getProperty("os.name", "");
            _isWindows = (osname.contains("Windows"));
            _isMacOS = (osname.contains("Mac OS") || osname.contains("MacOS"));
            _isLinux = (osname.contains("Linux"));
        } catch (Exception e) {
            // can't grab system properties; we'll just pretend we're not on any of these OSes
        }
    }
}
