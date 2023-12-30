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

package com.samskivert.util;

/**
 * <cite>Write once, run anywhere.</cite> Well, at least that's what it
 * said in the brochures. For those less than fresh days, you might need
 * to use this class to work around bugs on particular operating systems.
 */
public class RunAnywhere
{
    /**
     * Returns true if we're running in a JVM that identifies its
     * operating system as MacOS.
     */
    public static final boolean isMacOS ()
    {
        return _isMacOS;
    }

    /** Flag indicating that we're on MacOS; initialized when this class
     * is first loaded. */
    protected static boolean _isMacOS;

    static {
        try {
            String osname = System.getProperty("os.name");
            osname = (osname == null) ? "" : osname;
            _isMacOS = (osname.indexOf("Mac OS") != -1 ||
                        osname.indexOf("MacOS") != -1);
        } catch (Exception e) {
            // dang, can't grab system properties; we'll just pretend
            // we're not on any of these OSes
        }
    }
}
