//
// Getdown - application installer, patcher and launcher
// Copyright (C) 2004-2018 Getdown authors
// https://github.com/bekoenig/getdown/blob/master/LICENSE

package io.github.bekoenig.getdown.util;

import org.junit.jupiter.api.Test;

import static io.github.bekoenig.getdown.util.StringUtil.couldBeValidUrl;
import static io.github.bekoenig.getdown.util.StringUtil.join;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests {@link StringUtil}.
 */
class StringUtilTest {
    @Test
    void testCouldBeValidUrl() {
        assertTrue(couldBeValidUrl("http://www.foo.com/"));
        assertTrue(couldBeValidUrl("http://www.foo.com/A-B-C/1_2_3/~bar/q.jsp?x=u+i&y=2;3;4#baz%20baz"));
        assertTrue(couldBeValidUrl("https://user:secret@www.foo.com/"));

        assertFalse(couldBeValidUrl("http://www.foo.com & echo hello"));
        assertFalse(couldBeValidUrl("http://www.foo.com\""));
    }

    @Test
    void testJoin() {
        assertThrows(NullPointerException.class, () -> join(null, "\n "));
        assertEquals("", join(new String[]{}, "\n "));
        assertEquals("a,ccc,b", join(new String[]{"a", "ccc", "b"}, ","));
        assertEquals("a,,b", join(new String[]{"a", null, "b"}, ","));
        assertEquals("a\n b\n \n c", join(new String[]{"a", "b", null, "c"}, "\n "));
    }
}
