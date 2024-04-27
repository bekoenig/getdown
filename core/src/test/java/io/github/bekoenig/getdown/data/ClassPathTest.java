//
// Getdown - application installer, patcher and launcher
// Copyright (C) 2004-2018 Getdown authors
// https://github.com/bekoenig/getdown/blob/master/LICENSE

package io.github.bekoenig.getdown.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.LinkedHashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link ClassPath}.
 */
class ClassPathTest {
    @TempDir
    private Path folder;

    private File firstJar, secondJar;
    private ClassPath classPath;

    @BeforeEach
    void createJarsAndSetupClassPath() throws IOException {
        firstJar = folder.resolve("a.jar").toFile();
        firstJar.createNewFile();
        secondJar = folder.resolve("b.jar").toFile();
        secondJar.createNewFile();

        LinkedHashSet<File> classPathEntries = new LinkedHashSet<>();
        classPathEntries.add(firstJar);
        classPathEntries.add(secondJar);
        classPath = new ClassPath(classPathEntries);
    }

    @Test
    void shouldCreateValidArgumentString() {

        assertEquals(
            "a.jar" + File.pathSeparator + "b.jar",
            classPath.asArgumentString(folder.toFile()));
    }

    @Test
    void shouldProvideJarUrls() throws URISyntaxException {
        URL[] actualUrls = classPath.asUrls();
        assertEquals(firstJar, new File(actualUrls[0].toURI()));
        assertEquals(secondJar, new File(actualUrls[1].toURI()));
    }
}
