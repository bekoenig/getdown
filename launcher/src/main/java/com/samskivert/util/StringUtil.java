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

import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.util.Enumeration;
import java.util.Iterator;

import com.samskivert.annotation.ReplacedBy;

/**
 * String related utility functions.
 */
public class StringUtil
{
    /**
     * @return true if the string is null or empty, false otherwise.
     *
     * @deprecated use isBlank instead.
     */
    @Deprecated
    public static boolean blank (String value)
    {
        return isBlank(value);
    }

    /**
     * @return true if the string is null or consists only of whitespace, false otherwise.
     */
    public static boolean isBlank (String value)
    {
        for (int ii = 0, ll = (value == null) ? 0 : value.length(); ii < ll; ii++) {
            if (!Character.isWhitespace(value.charAt(ii))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a new string based on <code>source</code> with all instances of <code>before</code>
     * replaced with <code>after</code>.
     *
     * @deprecated java.lang.String.replace() was added in 1.5
     */
    @Deprecated @ReplacedBy(value="java.lang.String.replace()", reason="since 1.5")
    public static String replace (String source, String before, String after)
    {
        int pos = source.indexOf(before);
        if (pos == -1) {
            return source;
        }

        StringBuilder sb = new StringBuilder(source.length() + 32);

        int blength = before.length();
        int start = 0;
        while (pos != -1) {
            sb.append(source.substring(start, pos));
            sb.append(after);
            start = pos + blength;
            pos = source.indexOf(before, start);
        }
        sb.append(source.substring(start));

        return sb.toString();
    }

    /**
     * Converts the supplied object to a string. Normally this is accomplished via the object's
     * built in <code>toString()</code> method, but in the case of arrays, <code>toString()</code>
     * is called on each element and the contents are listed like so:
     *
     * <pre>
     * (value, value, value)
     * </pre>
     *
     * Arrays of ints, longs, floats and doubles are also handled for convenience.
     *
     * <p> Additionally, <code>Enumeration</code> or <code>Iterator</code> objects can be passed
     * and they will be enumerated and output in a similar manner to arrays. Bear in mind that this
     * uses up the enumeration or iterator in question.
     *
     * <p> Also note that passing null will result in the string "null" being returned.
     */
    public static String toString (Object val)
    {
        StringBuilder buf = new StringBuilder();
        toString(buf, val);
        return buf.toString();
    }

    /**
     * Like the single argument {@link #toString(Object)} with the additional function of
     * specifying the characters that are used to box in list and array types. For example, if "["
     * and "]" were supplied, an int array might be formatted like so: <code>[1, 3, 5]</code>.
     */
    public static String toString (Object val, String openBox, String closeBox)
    {
        StringBuilder buf = new StringBuilder();
        toString(buf, val, openBox, closeBox);
        return buf.toString();
    }

    /**
     * Converts the supplied value to a string and appends it to the supplied string buffer. See
     * the single argument version for more information.
     *
     * @param buf the string buffer to which we will append the string.
     * @param val the value from which to generate the string.
     */
    public static void toString (StringBuilder buf, Object val)
    {
        toString(buf, val, "(", ")");
    }

    /**
     * Converts the supplied value to a string and appends it to the supplied string buffer. The
     * specified boxing characters are used to enclose list and array types. For example, if "["
     * and "]" were supplied, an int array might be formatted like so: <code>[1, 3, 5]</code>.
     *
     * @param buf the string buffer to which we will append the string.
     * @param val the value from which to generate the string.
     * @param openBox the opening box character.
     * @param closeBox the closing box character.
     */
    public static void toString (StringBuilder buf, Object val, String openBox, String closeBox)
    {
        toString(buf, val, openBox, closeBox, ", ");
    }

    /**
     * Converts the supplied value to a string and appends it to the supplied string buffer. The
     * specified boxing characters are used to enclose list and array types. For example, if "["
     * and "]" were supplied, an int array might be formatted like so: <code>[1, 3, 5]</code>.
     *
     * @param buf the string buffer to which we will append the string.
     * @param val the value from which to generate the string.
     * @param openBox the opening box character.
     * @param closeBox the closing box character.
     * @param sep the separator string.
     */
    public static void toString (
        StringBuilder buf, Object val, String openBox, String closeBox, String sep)
    {
        if (val instanceof byte[]) {
            buf.append(openBox);
            byte[] v = (byte[])val;
            for (int i = 0; i < v.length; i++) {
                if (i > 0) {
                    buf.append(sep);
                }
                buf.append(v[i]);
            }
            buf.append(closeBox);

        } else if (val instanceof short[]) {
            buf.append(openBox);
            short[] v = (short[])val;
            for (short i = 0; i < v.length; i++) {
                if (i > 0) {
                    buf.append(sep);
                }
                buf.append(v[i]);
            }
            buf.append(closeBox);

        } else if (val instanceof int[]) {
            buf.append(openBox);
            int[] v = (int[])val;
            for (int i = 0; i < v.length; i++) {
                if (i > 0) {
                    buf.append(sep);
                }
                buf.append(v[i]);
            }
            buf.append(closeBox);

        } else if (val instanceof long[]) {
            buf.append(openBox);
            long[] v = (long[])val;
            for (int i = 0; i < v.length; i++) {
                if (i > 0) {
                    buf.append(sep);
                }
                buf.append(v[i]);
            }
            buf.append(closeBox);

        } else if (val instanceof float[]) {
            buf.append(openBox);
            float[] v = (float[])val;
            for (int i = 0; i < v.length; i++) {
                if (i > 0) {
                    buf.append(sep);
                }
                buf.append(v[i]);
            }
            buf.append(closeBox);

        } else if (val instanceof double[]) {
            buf.append(openBox);
            double[] v = (double[])val;
            for (int i = 0; i < v.length; i++) {
                if (i > 0) {
                    buf.append(sep);
                }
                buf.append(v[i]);
            }
            buf.append(closeBox);

        } else if (val instanceof Object[]) {
            buf.append(openBox);
            Object[] v = (Object[])val;
            for (int i = 0; i < v.length; i++) {
                if (i > 0) {
                    buf.append(sep);
                }
                toString(buf, v[i], openBox, closeBox);
            }
            buf.append(closeBox);

        } else if (val instanceof boolean[]) {
            buf.append(openBox);
            boolean[] v = (boolean[])val;
            for (int i = 0; i < v.length; i++) {
                if (i > 0) {
                    buf.append(sep);
                }
                buf.append(v[i] ? "t" : "f");
            }
            buf.append(closeBox);

        } else if (val instanceof Iterable<?>) {
            toString(buf, ((Iterable<?>)val).iterator(), openBox, closeBox);

        } else if (val instanceof Iterator<?>) {
            buf.append(openBox);
            Iterator<?> iter = (Iterator<?>)val;
            for (int i = 0; iter.hasNext(); i++) {
                if (i > 0) {
                    buf.append(sep);
                }
                toString(buf, iter.next(), openBox, closeBox);
            }
            buf.append(closeBox);

        } else if (val instanceof Enumeration<?>) {
            buf.append(openBox);
            Enumeration<?> enm = (Enumeration<?>)val;
            for (int i = 0; enm.hasMoreElements(); i++) {
                if (i > 0) {
                    buf.append(sep);
                }
                toString(buf, enm.nextElement(), openBox, closeBox);
            }
            buf.append(closeBox);

        } else if (val instanceof Point2D) {
            Point2D p = (Point2D)val;
            buf.append(openBox);
            coordsToString(buf, (int)p.getX(), (int)p.getY());
            buf.append(closeBox);

        } else if (val instanceof Dimension2D) {
            Dimension2D d = (Dimension2D)val;
            buf.append(openBox);
            buf.append(d.getWidth()).append("x").append(d.getHeight());
            buf.append(closeBox);

        } else if (val instanceof Rectangle2D) {
            Rectangle2D r = (Rectangle2D)val;
            buf.append(openBox);
            buf.append(r.getWidth()).append("x").append(r.getHeight());
            coordsToString(buf, (int)r.getX(), (int)r.getY());
            buf.append(closeBox);

        } else {
            buf.append(val);
        }
    }

    /**
     * Formats a pair of coordinates such that positive values are rendered with a plus prefix and
     * negative values with a minus prefix.  Examples would look like: <code>+3+4</code>
     * <code>-5+7</code>, etc.
     */
    public static void coordsToString (StringBuilder buf, int x, int y)
    {
        if (x >= 0) {
            buf.append("+");
        }
        buf.append(x);
        if (y >= 0) {
            buf.append("+");
        }
        buf.append(y);
    }

    /**
     * Splits the supplied string into components based on the specified separator string.
     */
    public static String[] split (String source, String sep)
    {
        // handle the special case of a zero-component source
        if (isBlank(source)) {
            return new String[0];
        }

        int tcount = 0, tpos = -1, tstart = 0;

        // count up the number of tokens
        while ((tpos = source.indexOf(sep, tpos+1)) != -1) {
            tcount++;
        }

        String[] tokens = new String[tcount+1];
        tpos = -1; tcount = 0;

        // do the split
        while ((tpos = source.indexOf(sep, tpos+1)) != -1) {
            tokens[tcount] = source.substring(tstart, tpos);
            tstart = tpos+1;
            tcount++;
        }

        // grab the last token
        tokens[tcount] = source.substring(tstart);

        return tokens;
    }
}
