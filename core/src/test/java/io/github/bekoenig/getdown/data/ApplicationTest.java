//
// Getdown - application installer, patcher and launcher
// Copyright (C) 2004-2018 Getdown authors
// https://github.com/bekoenig/getdown/blob/master/LICENSE

package io.github.bekoenig.getdown.data;

import io.github.bekoenig.getdown.util.Config;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junitpioneer.jupiter.SetSystemProperty;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ApplicationTest {

    @TempDir Path folder;

    Application createApp() {
        List<EnvConfig.Note> notes = new ArrayList<>();
        EnvConfig env = EnvConfig.create(new String[] { folder.toAbsolutePath().toString() }, notes);
        EnvConfigTest.checkNoNotes(notes);
        return new Application(env);
    }

    @Test
    void testBaseConfig() throws Exception {
        Application app = createApp();
        URL appbase = new URL("https://test.com/foo/bar/");
        Config config = new Config(Config.parseData(toReader(
            "appbase", appbase.toString()
        ), Config.createOpts(true)));
        app.initBase(config);

        assertEquals(appbase, app.getRemoteURL(""));
    }

    @Test
    void testVersionedBase() throws Exception {
        Application app = createApp();
        String rootAppbase = "https://test.com/foo/bar/";
        Config config = new Config(Config.parseData(toReader(
            "appbase", rootAppbase + "%VERSION%",
            "version", "42"
        ), Config.createOpts(true)));
        app.initBase(config);

        assertEquals(new URL(rootAppbase + "42/"), app.getRemoteURL(""));
    }

    @Test
    void testEnvVarBase() throws Exception {
        // fiddling to make test work on Windows or Unix
        String evar = System.getenv("USER") == null ? "USERNAME" : "USER";
        Application app = createApp();
        String rootAppbase = "https://test.com/foo/%ENV." + evar + "%/";
        Config config = new Config(Config.parseData(toReader(
            "appbase", rootAppbase + "%VERSION%",
            "version", "42"
        ), Config.createOpts(true)));
        app.initBase(config);

        String expectAppbase = "https://test.com/foo/" + System.getenv(evar) + "/42/";
        assertEquals(new URL(expectAppbase), app.getRemoteURL(""));
    }

    private static StringReader toReader(String... pairs) {
        StringBuilder builder = new StringBuilder();
        for (int ii = 0; ii < pairs.length; ii += 2) {
            builder.append(pairs[ii]).append("=").append(pairs[ii + 1]).append("\n");
        }
        return new StringReader(builder.toString());
    }

    @Test
    void prepareProcessArguments_withCommandLineOnly() throws IOException {
        // GIVEN
        Application app = createApp();
        List<String> args = Arrays.asList("java-path", "some-arg", "some-more-arg");

        // WHEN
        String[] sargs = app.prepareProcessArguments(args);

        // THEN
        assertThat(sargs).containsExactly("java-path", "some-arg", "some-more-arg");
    }

    @Test
    @SetSystemProperty(key = "os.name", value = "Windows")
    void prepareProcessArguments_withArgumentsFile() throws IOException {
        // GIVEN
        Application app = createApp();
        String someLongArg = Stream.generate(() -> "X").limit(32735).collect(joining());
        List<String> args = new ArrayList<>(Arrays.asList("java-path", "some-arg", "some-more-arg",
            someLongArg));

        // WHEN
        String[] sargs = app.prepareProcessArguments(args);

        // THEN
        assertThat(folder.resolve("arguments.txt").toFile()).exists().hasContent("some-arg some-more-arg " + someLongArg);
        assertThat(sargs).containsExactly("java-path", "@arguments.txt");
    }
}
