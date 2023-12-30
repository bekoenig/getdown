//
// Getdown - application installer, patcher and launcher
// Copyright (C) 2004-2018 Getdown authors
// https://github.com/bekoenig/getdown/blob/master/LICENSE

package io.github.bekoenig.getdown.launcher;

import java.io.IOException;

/**
 * Thrown when it's detected that multiple instances of the same getdown installer are running.
 */
public class MultipleGetdownRunning extends IOException
{
    public MultipleGetdownRunning ()
    {
        super("m.another_getdown_running");
    }

}
