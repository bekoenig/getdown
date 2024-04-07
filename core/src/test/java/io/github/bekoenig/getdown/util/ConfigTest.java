//
// Getdown - application installer, patcher and launcher
// Copyright (C) 2004-2018 Getdown authors
// https://github.com/bekoenig/getdown/blob/master/LICENSE

package io.github.bekoenig.getdown.util;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests {@link Config}.
 */
class ConfigTest {
    private static final Random _rando = new Random();

    static class Pair {
        final String key;
        final String value;

        Pair(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    static final Pair[] SIMPLE_PAIRS = {
        new Pair("one", "two"),
        new Pair("three", "four"),
        new Pair("five", "six"),
        new Pair("seven", "eight"),
        new Pair("nine", "ten"),
    };

    @Test
    void testSimplePairs() throws IOException {
        List<String[]> pairs = Config.parsePairs(
            toReader(SIMPLE_PAIRS), Config.createOpts(true));
        for (int ii = 0; ii < SIMPLE_PAIRS.length; ii++) {
            assertEquals(SIMPLE_PAIRS[ii].key, pairs.get(ii)[0]);
            assertEquals(SIMPLE_PAIRS[ii].value, pairs.get(ii)[1]);
        }
    }

    @Test
    void testCreateOps_withCheckPlatform() {
        String originalOsName = System.getProperty("os.name");
        String originalOsArch = System.getProperty("os.arch");

        System.setProperty("os.name", "linux");
        System.setProperty("os.arch", "x86_64");
        Config.ParseOpts opts1 = Config.createOpts(true);
        assertEquals("linux", opts1.osname);
        assertEquals("x86_64", opts1.osarch);

        System.setProperty("os.name", "linux");
        System.clearProperty("os.arch");
        Config.ParseOpts opts2 = Config.createOpts(true);
        assertEquals("linux", opts2.osname);
        assertEquals("", opts2.osarch);

        System.clearProperty("os.name");
        System.setProperty("os.arch", "x86_64");
        Config.ParseOpts opts3 = Config.createOpts(true);
        assertEquals("", opts3.osname);
        assertEquals("x86_64", opts3.osarch);

        System.clearProperty("os.name");
        System.clearProperty("os.arch");
        Config.ParseOpts opts4 = Config.createOpts(true);
        assertEquals("", opts4.osname);
        assertEquals("", opts4.osarch);

        // Restore original state
        System.setProperty("os.name", originalOsName);
        System.setProperty("os.arch", originalOsArch);
    }

    @Test
    void testQualifiedPairs() throws IOException {
        Pair linux = new Pair("one", "[linux] two");
        Pair mac = new Pair("three", "[mac os x] four");
        Pair linuxAndMac = new Pair("five", "[linux, mac os x] six");
        Pair linux64 = new Pair("seven", "[linux-x86_64] eight");
        Pair linux64s = new Pair("nine", "[linux-x86_64, linux-amd64] ten");
        Pair mac64 = new Pair("eleven", "[mac os x-x86_64] twelve");
        Pair win64 = new Pair("thirteen", "[windows-x86_64] fourteen");
        Pair notWin = new Pair("fifteen", "[!windows] sixteen");
        Pair[] pairs = {linux, mac, linuxAndMac, linux64, linux64s, mac64, win64, notWin};

        Config.ParseOpts opts = Config.createOpts(false);
        opts.osname = "linux";
        opts.osarch = "i386";
        List<String[]> parsed = Config.parsePairs(toReader(pairs), opts);
        assertTrue(exists(parsed, linux.key));
        assertFalse(exists(parsed, mac.key));
        assertTrue(exists(parsed, linuxAndMac.key));
        assertFalse(exists(parsed, linux64.key));
        assertFalse(exists(parsed, linux64s.key));
        assertFalse(exists(parsed, mac64.key));
        assertFalse(exists(parsed, win64.key));
        assertTrue(exists(parsed, notWin.key));

        opts.osarch = "x86_64";
        parsed = Config.parsePairs(toReader(pairs), opts);
        assertTrue(exists(parsed, linux.key));
        assertFalse(exists(parsed, mac.key));
        assertTrue(exists(parsed, linuxAndMac.key));
        assertTrue(exists(parsed, linux64.key));
        assertTrue(exists(parsed, linux64s.key));
        assertFalse(exists(parsed, mac64.key));
        assertFalse(exists(parsed, win64.key));
        assertTrue(exists(parsed, notWin.key));

        opts.osarch = "amd64";
        parsed = Config.parsePairs(toReader(pairs), opts);
        assertTrue(exists(parsed, linux.key));
        assertFalse(exists(parsed, mac.key));
        assertTrue(exists(parsed, linuxAndMac.key));
        assertFalse(exists(parsed, linux64.key));
        assertTrue(exists(parsed, linux64s.key));
        assertFalse(exists(parsed, mac64.key));
        assertFalse(exists(parsed, win64.key));
        assertTrue(exists(parsed, notWin.key));

        opts.osname = "mac os x";
        opts.osarch = "x86_64";
        parsed = Config.parsePairs(toReader(pairs), opts);
        assertFalse(exists(parsed, linux.key));
        assertTrue(exists(parsed, mac.key));
        assertTrue(exists(parsed, linuxAndMac.key));
        assertFalse(exists(parsed, linux64.key));
        assertFalse(exists(parsed, linux64s.key));
        assertTrue(exists(parsed, mac64.key));
        assertFalse(exists(parsed, win64.key));
        assertTrue(exists(parsed, notWin.key));

        opts.osname = "windows";
        opts.osarch = "i386";
        parsed = Config.parsePairs(toReader(pairs), opts);
        assertFalse(exists(parsed, linux.key));
        assertFalse(exists(parsed, mac.key));
        assertFalse(exists(parsed, linuxAndMac.key));
        assertFalse(exists(parsed, linux64.key));
        assertFalse(exists(parsed, linux64s.key));
        assertFalse(exists(parsed, mac64.key));
        assertFalse(exists(parsed, win64.key));
        assertFalse(exists(parsed, notWin.key));

        opts.osarch = "x86_64";
        parsed = Config.parsePairs(toReader(pairs), opts);
        assertFalse(exists(parsed, linux.key));
        assertFalse(exists(parsed, mac.key));
        assertFalse(exists(parsed, linuxAndMac.key));
        assertFalse(exists(parsed, linux64.key));
        assertFalse(exists(parsed, linux64s.key));
        assertFalse(exists(parsed, mac64.key));
        assertTrue(exists(parsed, win64.key));
        assertFalse(exists(parsed, notWin.key));

        opts.osarch = "amd64";
        parsed = Config.parsePairs(toReader(pairs), opts);
        assertFalse(exists(parsed, linux.key));
        assertFalse(exists(parsed, mac.key));
        assertFalse(exists(parsed, linuxAndMac.key));
        assertFalse(exists(parsed, linux64.key));
        assertFalse(exists(parsed, linux64s.key));
        assertFalse(exists(parsed, mac64.key));
        assertFalse(exists(parsed, win64.key));
        assertFalse(exists(parsed, notWin.key));
    }

    static boolean exists(List<String[]> pairs, String key) {
        for (String[] pair : pairs) {
            if (pair[0].equals(key)) {
                return true;
            }
        }
        return false;
    }

    static StringReader toReader(Pair[] pairs) {
        StringBuilder builder = new StringBuilder();
        for (Pair pair : pairs) {
            // throw some whitespace in to ensure it's trimmed
            builder.append(whitespace()).append(pair.key).
                append(whitespace()).append("=").
                append(whitespace()).append(pair.value).
                append(whitespace()).append("\n");
        }
        return new StringReader(builder.toString());
    }

    static String whitespace() {
        return _rando.nextBoolean() ? " " : "";
    }
}
