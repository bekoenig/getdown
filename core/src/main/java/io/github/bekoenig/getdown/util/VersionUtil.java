//
// Getdown - application installer, patcher and launcher
// Copyright (C) 2004-2018 Getdown authors
// https://github.com/bekoenig/getdown/blob/master/LICENSE

package io.github.bekoenig.getdown.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.bekoenig.getdown.data.SysProps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Version related utilities.
 */
public final class VersionUtil
{
    private static final Logger LOGGER = LoggerFactory.getLogger(VersionUtil.class);

    /**
     * Reads a version number from a file.
     */
    public static long readVersion (File vfile)
    {
        long fileVersion = -1;
        try (BufferedReader bin =
             new BufferedReader(new InputStreamReader(Files.newInputStream(vfile.toPath()), UTF_8))) {
            String vstr = bin.readLine();
            if (!StringUtil.isBlank(vstr)) {
                fileVersion = Long.parseLong(vstr);
            }
        } catch (Exception e) {
            LOGGER.info("Unable to read version file", e);
        }

        return fileVersion;
    }

    /**
     * Writes a version number to a file.
     */
    public static void writeVersion (File vfile, long version) throws IOException
    {
        try (PrintStream out = new PrintStream(Files.newOutputStream(vfile.toPath()))) {
            out.println(version);
        } catch (Exception e) {
            LOGGER.warn("Unable to write version file", e);
        }
    }

    /**
     * Parses {@code versStr} using {@code versRegex} into a (long) integer version number.
     * @see SysProps#parseJavaVersion
     */
    public static long parseJavaVersion (String versRegex, String versStr)
    {
        Matcher m = Pattern.compile(versRegex).matcher(versStr);
        if (!m.matches()) return 0L;

        long vers = 0L;
        for (int ii = 1; ii <= m.groupCount(); ii++) {
            String valstr = m.group(ii);
            int value = (valstr == null) ? 0 : parseInt(valstr);
            vers *= 100;
            vers += value;
        }
        return vers;
    }

    /**
     * Reads and parses the version from the {@code release} file bundled with a JVM.
     */
    public static long readReleaseVersion (File relfile, String versRegex)
    {
        try (BufferedReader in =
             new BufferedReader(new InputStreamReader(Files.newInputStream(relfile.toPath()), UTF_8))) {
            String line = null, relvers = null;
            while ((line = in.readLine()) != null) {
                if (line.startsWith("JAVA_VERSION=")) {
                    relvers = line.substring("JAVA_VERSION=".length()).replace('"', ' ').trim();
                }
            }

            if (relvers == null) {
                LOGGER.atWarn()
                    .setMessage("No JAVA_VERSION line in 'release' file")
                    .addKeyValue("file", relfile)
                    .log();
                return 0L;
            }
            return parseJavaVersion(versRegex, relvers);

        } catch (Exception e) {
            LOGGER.atWarn()
                .setMessage("Failed to read version from 'release' file")
                .addKeyValue("file", relfile)
                .setCause(e)
                .log();
            return 0L;
        }
    }

    private static int parseInt (String str) {
        int value = 0;
        for (int ii = 0, ll = str.length(); ii < ll; ii++) {
            char c = str.charAt(ii);
            if (c >= '0' && c <= '9') {
                value *= 10;
                value += (c - '0');
            }
        }
        return value;
    }
}
