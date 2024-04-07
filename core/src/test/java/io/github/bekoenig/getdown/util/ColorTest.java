//
// Getdown - application installer, patcher and launcher
// Copyright (C) 2004-2018 Getdown authors
// https://github.com/bekoenig/getdown/blob/master/LICENSE

package io.github.bekoenig.getdown.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests {@link Color}.
 */
class ColorTest {
    @Test
    void testBrightness() {
        assertEquals(0, Color.brightness(0xFF000000), 0.0000001);
        assertEquals(1, Color.brightness(0xFFFFFFFF), 0.0000001);
        assertEquals(0.0117647, Color.brightness(0xFF010203), 0.0000001);
        assertEquals(1, Color.brightness(0xFF00FFC8), 0.0000001);
    }
}
