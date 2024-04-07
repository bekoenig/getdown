//
// Getdown - application installer, patcher and launcher
// Copyright (C) 2004-2018 Getdown authors
// https://github.com/bekoenig/getdown/blob/master/LICENSE

package io.github.bekoenig.getdown.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests {@link FileUtil}.
 */
class FileUtilTest {
    @Test
    void testReadLines() throws IOException {
        String data = "This is a test\nof a file with\na few lines\n";
        List<String> lines = FileUtil.readLines(new StringReader(data));
        String[] linesBySplit = data.split("\n");
        assertEquals(linesBySplit.length, lines.size());
        for (int ii = 0; ii < lines.size(); ii++) {
            assertEquals(linesBySplit[ii], lines.get(ii));
        }
    }

    @Test
    void shouldCopyFile(@TempDir Path folder) throws IOException {
        File source = folder.resolve("source.txt").toFile();
        source.createNewFile();
        File target = folder.resolve("target.txt").toFile();
        assertFalse(target.exists());
        FileUtil.copy(source, target);
        assertTrue(target.exists());
    }
}
