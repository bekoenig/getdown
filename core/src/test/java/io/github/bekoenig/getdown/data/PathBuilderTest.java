//
// Getdown - application installer, patcher and launcher
// Copyright (C) 2004-2018 Getdown authors
// https://github.com/bekoenig/getdown/blob/master/LICENSE

package io.github.bekoenig.getdown.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PathBuilderTest {

    @Mock
    private Application application;
    @Mock
    private Resource firstJar;
    @Mock
    private Resource secondJar;

    @TempDir
    private Path appdir;

    @BeforeEach
    void setupFilesAndResources() throws IOException {
        File firstJarFile = appdir.resolve("a.jar").toFile();
        firstJarFile.createNewFile();
        File secondJarFile = appdir.resolve("b.jar").toFile();
        secondJarFile.createNewFile();

        when(firstJar.getFinalTarget()).thenReturn(firstJarFile);
        when(secondJar.getFinalTarget()).thenReturn(secondJarFile);
        when(application.getActiveCodeResources()).thenReturn(Arrays.asList(firstJar, secondJar));
    }

    @Test
    void shouldBuildDefaultClassPath() {
        ClassPath classPath = PathBuilder.buildDefaultClassPath(application);
        assertEquals("a.jar" + File.pathSeparator + "b.jar", classPath.asArgumentString(appdir.toFile()));
    }

    @Test
    void shouldBuildCachedClassPath() throws IOException {
        when(application.getAppDir()).thenReturn(appdir.toFile());
        when(application.getDigest(firstJar)).thenReturn("first");
        when(application.getDigest(secondJar)).thenReturn("second");
        when(application.getCodeCacheRetentionDays()).thenReturn(1);

        ClassPath classPath = PathBuilder.buildCachedClassPath(application);

        StringBuilder expected = new StringBuilder();
        expected
            .append(".cache").append(File.separator).append("fi").append(File.separator).append("first.jar")
            .append(File.pathSeparator)
            .append(".cache").append(File.separator).append("se").append(File.separator).append("second.jar");
        assertEquals(expected.toString(), classPath.asArgumentString(appdir.toFile()));
    }

}
