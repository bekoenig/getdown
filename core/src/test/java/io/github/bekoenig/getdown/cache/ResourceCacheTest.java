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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Asserts the correct functionality of the {@link ResourceCache}.
 */
abstract class ResourceCacheTest {

    static class ResourceCacheJarTest extends ResourceCacheTest {
        ResourceCacheJarTest() {
            super(".jar");
        }
    }

    static class ResourceCacheZipTest extends ResourceCacheTest {
        ResourceCacheZipTest() {
            super(".zip");
        }
    }

    private static final long YESTERDAY = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1);

    @TempDir
    private Path folder;

    private File fileToCache;
    private ResourceCache cache;

    private final String extension;

    ResourceCacheTest(String extension) {
        this.extension = extension;
    }

    @BeforeEach
    void setupCache() throws IOException {
        fileToCache = folder.resolve("filetocache" + extension).toFile();
        fileToCache.createNewFile();
        Path cacheFolder = folder.resolve(".cache");
        Files.createDirectory(cacheFolder);
        cache = new ResourceCache(cacheFolder.toFile());
    }

    @Test
    void shouldCacheFile() throws IOException {
        assertEquals("abc123" + extension, cacheFile().getName());
    }

    private File cacheFile() throws IOException {
        return cache.cacheFile(fileToCache, "abc123", "abc123");
    }

    @Test
    void shouldTrackFileUsage() throws IOException {
        String name = "abc123" + extension + ResourceCache.LAST_ACCESSED_FILE_SUFFIX;
        File lastAccessedFile = new File(cacheFile().getParentFile(), name);
        assertTrue(lastAccessedFile.exists());
    }

    @Test
    void shouldNotCacheTheSameFile() throws Exception {
        File cachedFile = cacheFile();
        cachedFile.setLastModified(YESTERDAY);
        long expectedLastModified = cachedFile.lastModified();
        // caching it another time
        File sameCachedFile = cacheFile();
        assertEquals(expectedLastModified, sameCachedFile.lastModified());
    }

    @Test
    void shouldRememberWhenFileWasRequested() throws Exception {
        File cachedFile = cacheFile();
        String name = cachedFile.getName() + ResourceCache.LAST_ACCESSED_FILE_SUFFIX;
        File lastAccessedFile = new File(cachedFile.getParentFile(), name);
        lastAccessedFile.setLastModified(YESTERDAY);
        long lastAccessed = lastAccessedFile.lastModified();
        // caching it another time
        cacheFile();
        assertTrue(lastAccessedFile.lastModified() > lastAccessed);
    }
}
