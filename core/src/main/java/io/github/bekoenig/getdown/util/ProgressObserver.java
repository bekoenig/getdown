//
// Getdown - application installer, patcher and launcher
// Copyright (C) 2004-2018 Getdown authors
// https://github.com/bekoenig/getdown/blob/master/LICENSE

package io.github.bekoenig.getdown.util;

/**
 * Used to communicate progress.
 */
public interface ProgressObserver
{
    /**
     * Informs the observer that we have completed the specified
     * percentage of the process.
     */
    void progress (int percent);
}
