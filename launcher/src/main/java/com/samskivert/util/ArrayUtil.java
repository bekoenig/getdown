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

import java.util.Arrays;

/**
 * Miscellaneous utility routines for working with arrays.
 */
public class ArrayUtil
{
    /**
     * Return the String representation of the specified Object, which may or may not be an array.
     */
    public static String safeToString (Object o)
    {
        return ((o == null) || !o.getClass().isArray()) ? String.valueOf(o) : toString(o);
    }

    /**
     * Return the String representation of the specified Object, which <em>must</em> be an array.
     *
     * @throws IllegalArgumentException if array is not actually an array.
     */
    public static String toString (Object array)
    {
        if (array instanceof Object[]) {
            return Arrays.deepToString((Object[])array); // go deep, baby

        } else if (array instanceof int[]) {
            return Arrays.toString((int[])array);

        } else if (array instanceof byte[]) {
            return Arrays.toString((byte[])array);

        } else if (array instanceof char[]) {
            return Arrays.toString((char[])array);

        } else if (array instanceof short[]) {
            return Arrays.toString((short[])array);

        } else if (array instanceof long[]) {
            return Arrays.toString((long[])array);

        } else if (array instanceof float[]) {
            return Arrays.toString((float[])array);

        } else if (array instanceof double[]) {
            return Arrays.toString((double[])array);

        } else if (array instanceof boolean[]) {
            return Arrays.toString((boolean[])array);
        }
        throw new IllegalArgumentException("Not an array: " + array);
    }
}
