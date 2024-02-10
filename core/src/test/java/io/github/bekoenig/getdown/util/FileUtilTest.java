//
// Getdown - application installer, patcher and launcher
// Copyright (C) 2004-2018 Getdown authors
// https://github.com/bekoenig/getdown/blob/master/LICENSE

package io.github.bekoenig.getdown.util;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests {@link FileUtil}.
 */
public class FileUtilTest {
    @Test
    public void testReadLines() throws IOException {
        String data = "This is a test\nof a file with\na few lines\n";
        List<String> lines = FileUtil.readLines(new StringReader(data));
        String[] linesBySplit = data.split("\n");
        assertEquals(linesBySplit.length, lines.size());
        for (int ii = 0; ii < lines.size(); ii++) {
            assertEquals(linesBySplit[ii], lines.get(ii));
        }
    }

    @Test
    public void shouldCopyFile() throws IOException {
        File source = _folder.newFile("source.txt");
        File target = new File(_folder.getRoot(), "target.txt");
        assertFalse(target.exists());
        FileUtil.copy(source, target);
        assertTrue(target.exists());
    }

    @Rule
    public final TemporaryFolder _folder = new TemporaryFolder();
}
