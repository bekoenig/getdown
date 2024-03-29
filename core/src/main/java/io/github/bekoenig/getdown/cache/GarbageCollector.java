//
// Getdown - application installer, patcher and launcher
// Copyright (C) 2004-2018 Getdown authors
// https://github.com/bekoenig/getdown/blob/master/LICENSE

package io.github.bekoenig.getdown.cache;

import io.github.bekoenig.getdown.data.Resource;
import io.github.bekoenig.getdown.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Stream;

/**
 * Collects elements in the {@link ResourceCache cache} which became unused and deletes them
 * afterwards.
 */
public class GarbageCollector {
    /**
     * Collect and delete the garbage in the cache.
     */
    public static void collect(File cacheDir, final long retentionPeriodMillis) {
        try {
            Files.walkFileTree(cacheDir.toPath(), new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
                    File file = path.toFile();
                    File cachedFile = getCachedFile(file);
                    File lastAccessedFile = getLastAccessedFile(file);
                    if (!cachedFile.exists() || !lastAccessedFile.exists()) {
                        if (cachedFile.exists()) {
                            FileUtil.deleteHarder(cachedFile);
                        } else {
                            FileUtil.deleteHarder(lastAccessedFile);
                        }
                    } else if (shouldDelete(lastAccessedFile, retentionPeriodMillis)) {
                        FileUtil.deleteHarder(lastAccessedFile);
                        FileUtil.deleteHarder(cachedFile);
                    }

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    try (Stream<Path> list = Files.list(dir)) {
                        if (!list.findAny().isPresent()) {
                            FileUtil.deleteHarder(dir.toFile());
                        }
                    }

                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Collect and delete garbage in the native cache. It tries to find a jar file with a matching
     * last modified file, and deletes the entire directory accordingly.
     */
    public static void collectNative(File cacheDir, final long retentionPeriodMillis) {
        File[] subdirs = cacheDir.listFiles();
        if (subdirs != null) {
            for (File dir : subdirs) {
                if (dir.isDirectory()) {
                    // Get all the native jars or zips in the directory (there should only be one)
                    for (File file : dir.listFiles()) {
                        if (!Resource.isJar(file) && !Resource.isZip(file)) {
                            continue;
                        }
                        File cachedFile = getCachedFile(file);
                        File lastAccessedFile = getLastAccessedFile(file);
                        if (!cachedFile.exists() || !lastAccessedFile.exists() ||
                            shouldDelete(lastAccessedFile, retentionPeriodMillis)) {
                            FileUtil.deleteDirHarder(dir);
                        }
                    }
                } else {
                    // @TODO There shouldn't be any loose files in native/ but if there are then
                    // what? Delete them? file.delete();
                }
            }
        }
    }

    private static boolean shouldDelete(File lastAccessedFile, long retentionMillis) {
        return System.currentTimeMillis() - lastAccessedFile.lastModified() > retentionMillis;
    }

    private static File getLastAccessedFile(File file) {
        return isLastAccessedFile(file) ? file : new File(
            file.getParentFile(), file.getName() + ResourceCache.LAST_ACCESSED_FILE_SUFFIX);
    }

    private static boolean isLastAccessedFile(File file) {
        return file.getName().endsWith(ResourceCache.LAST_ACCESSED_FILE_SUFFIX);
    }

    private static File getCachedFile(File file) {
        return !isLastAccessedFile(file) ? file : new File(
            file.getParentFile(), file.getName().substring(0, file.getName().lastIndexOf('.')));
    }
}
