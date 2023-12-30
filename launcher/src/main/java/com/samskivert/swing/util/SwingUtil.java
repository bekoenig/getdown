//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2011 Michael Bayne, et al.
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.samskivert.swing.util;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;

/**
 * Miscellaneous useful Swing-related utility functions.
 */
public class SwingUtil
{
    /**
     * Center the given window within the screen boundaries.
     *
     * @param window the window to be centered.
     */
    public static void centerWindow (Window window)
    {
        Rectangle bounds;
        try {
            bounds = GraphicsEnvironment.getLocalGraphicsEnvironment().
                getDefaultScreenDevice().getDefaultConfiguration().getBounds();
        } catch (Throwable t) {
            Toolkit tk = window.getToolkit();
            Dimension ss = tk.getScreenSize();
            bounds = new Rectangle(ss);
        }

        int width = window.getWidth(), height = window.getHeight();
        window.setBounds(bounds.x + (bounds.width-width)/2, bounds.y + (bounds.height-height)/2,
                         width, height);
    }

    /**
     * Activates anti-aliasing in the supplied graphics context on both text and 2D drawing
     * primitives.
     *
     * @return an object that should be passed to {@link #restoreAntiAliasing} to restore the
     * graphics context to its original settings.
     */
    public static Object activateAntiAliasing (Graphics2D gfx)
    {
        RenderingHints ohints = gfx.getRenderingHints(), nhints = new RenderingHints(null);
        nhints.add(ohints);
        nhints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        nhints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        gfx.setRenderingHints(nhints);
        return ohints;
    }

    /**
     * Restores anti-aliasing in the supplied graphics context to its original setting.
     *
     * @param rock the results of a previous call to {@link #activateAntiAliasing} or null, in
     * which case this method will NOOP. This alleviates every caller having to conditionally avoid
     * calling restore if they chose not to activate earlier.
     */
    public static void restoreAntiAliasing (Graphics2D gfx, Object rock)
    {
        if (rock != null) {
            gfx.setRenderingHints((RenderingHints)rock);
        }
    }

    /**
     * Returns true if anti-aliasing is desired by default. This currently checks the value of the
     * <code>swing.aatext</code> property, but will someday switch to using Java Desktop Properties
     * which in theory get their values from OS preferences.
     */
    public static boolean getDefaultTextAntialiasing ()
    {
        return _defaultTextAntialiasing;
    }

    /** Used by {@link #getDefaultTextAntialiasing}. */
    protected static boolean _defaultTextAntialiasing;
    static {
        try {
            _defaultTextAntialiasing = Boolean.getBoolean("swing.aatext");
        } catch (Exception e) {
            // security exception due to running in a sandbox, no problem
        }
    }
}
