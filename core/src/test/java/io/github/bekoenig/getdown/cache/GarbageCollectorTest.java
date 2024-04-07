//
// Getdown - application installer, patcher and launcher
// Copyright (C) 2004-2018 Getdown authors
// https://github.com/bekoenig/getdown/blob/master/LICENSE

package io.github.bekoenig.getdown.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Validates that cache garbage is collected and deleted correctly.
 */
abstract class GarbageCollectorTest {

    static class GarbageCollectorJarTest extends GarbageCollectorTest {
        GarbageCollectorJarTest() {
            super(".jar");
        }
    }

    static class GarbageCollectorZipTest extends GarbageCollectorTest {
        GarbageCollectorZipTest() {
            super(".zip");
        }
    }

    @TempDir
    private Path folder;

    private File cachedFile;
    private File lastAccessedFile;

    private final String extension;

    GarbageCollectorTest(String extension) {
        this.extension = extension;
    }

    @BeforeEach
    void setupFiles() throws IOException {
        cachedFile = folder.resolve("abc123" + extension).toFile();
        cachedFile.createNewFile();
        lastAccessedFile = folder.resolve(
            "abc123" + extension + ResourceCache.LAST_ACCESSED_FILE_SUFFIX).toFile();
        lastAccessedFile.createNewFile();
    }

    @Test
    void shouldDeleteCacheEntryIfRetentionPeriodIsReached() {
        gcNow();
        assertFalse(cachedFile.exists());
        assertFalse(lastAccessedFile.exists());
    }

    @Test
    void shouldDeleteCacheFolderIfFolderIsEmpty() {
        gcNow();
        assertFalse(folder.toFile().exists());
    }

    private void gcNow() {
        GarbageCollector.collect(folder.toFile(), -1);
    }

    @Test
    void shouldKeepFilesInCacheIfRententionPeriodIsNotReached() {
        GarbageCollector.collect(folder.toFile(), TimeUnit.DAYS.toMillis(1));
        assertTrue(cachedFile.exists());
        assertTrue(lastAccessedFile.exists());
    }

    @Test
    void shouldDeleteCachedFileIfLastAccessedFileIsMissing() {
        assumeTrue(lastAccessedFile.delete());
        gcNow();
        assertFalse(cachedFile.exists());
    }

    @Test
    void shouldDeleteLastAccessedFileIfCachedFileIsMissing() {
        assumeTrue(cachedFile.delete());
        gcNow();
        assertFalse(lastAccessedFile.exists());
    }
}
