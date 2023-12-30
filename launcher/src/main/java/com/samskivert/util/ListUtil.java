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
 * List util is for the times when you just can't bear the overhead of an
 * {@link java.util.ArrayList} object to manage your list of objects. It
 * provides a suite of list management routines that operate on bare
 * {@link Object} arrays. Some of those routines mimic the
 * behavior of array lists, others provide other more specialized
 * (generally faster but making requirements of the caller) list behavior.
 *
 * <p> An example is probably in order:
 *
 * <pre>
 * Object[] list = null;
 * String foo = "foo";
 * String bar = "bar";
 *
 * // add our objects to a list
 * list = ListUtil.add(list, foo);
 * list = ListUtil.add(list, bar);
 *
 * // remove foo from the list (does so by clearing out that index, but it
 * // doesn't slide subsequent elements down)
 * ListUtil.clear(list, foo);
 *
 * // use the version of remove that calls equals() rather than just
 * // checking for equality of references
 * String anotherBar = "bar";
 * ListUtil.clear(list, anotherBar);
 *
 * // append our objects to the end of the list letting list util know
 * // that we're tracking the list size
 * list = ListUtil.add(list, 0, foo);
 * list = ListUtil.add(list, 1, bar);
 *
 * // remove the elements from the list, compacting it to preserve
 * // element continuity
 * ListUtil.remove(list, 0);
 * ListUtil.remove(list, bar);
 * </pre>
 *
 * See the documentation for the individual functions for their exact
 * behavior.
 */
public class ListUtil
{
    /**
     * Adds the specified element to the first empty slot in the specified
     * list. Begins searching for empty slots at zeroth index.
     *
     * @param list the list to which to add the element. Can be null.
     * @param element the element to add.
     *
     * @return a reference to the list with element added (might not be
     * the list you passed in due to expansion, or allocation).
     */
    public static Object[] add (Object[] list, Object element)
    {
        return add(list, 0, element);
    }

    /**
     * Adds the specified element to the next empty slot in the specified
     * list. Begins searching for empty slots at the specified index. This
     * can be used to quickly add elements to a list that preserves
     * consecutivity by calling it with the size of the list as the first
     * index to check.
     *
     * @param list the list to which to add the element. Can be null.
     * @param startIdx the index at which to start looking for a spot.
     * @param element the element to add.
     *
     * @return a reference to the list with element added (might not be
     * the list you passed in due to expansion, or allocation).
     */
    public static Object[] add (Object[] list, int startIdx, Object element)
    {
        requireNotNull(element);

        // make sure we've got a list to work with
        if (list == null) {
            list = new Object[DEFAULT_LIST_SIZE];
        }

        // search for a spot to insert yon element; assuming we'll insert
        // it at the end of the list if we don't find one
        int llength = list.length;
        int index = llength;
        for (int i = startIdx; i < llength; i++) {
            if (list[i] == null) {
                index = i;
                break;
            }
        }

        // expand the list if necessary
        if (index >= llength) {
            list = accomodate(list, index);
        }

        // stick the element on in
        list[index] = element;

        return list;
    }

    /**
     * Searches through the list checking to see if the element supplied
     * is already in the list (using reference equality to check for
     * existence) and adds it if it is not.
     *
     * @param list the list to which to add the element. Can be null.
     * @param element the element to test and add.
     *
     * @return a reference to the list with element added (might not be
     * the list you passed in due to expansion, or allocation) or null if
     * the element was already in the original array.
     */
    public static Object[] testAndAddRef (Object[] list, Object element)
    {
        return testAndAdd(REFERENCE_COMP, list, element);
    }

    /** Helper function for {@link #testAndAddRef}, etc. */
    protected static Object[] testAndAdd (
        EqualityComparator eqc, Object[] list, Object element)
    {
        requireNotNull(element);

        // make sure we've got a list to work with
        if (list == null) {
            list = new Object[DEFAULT_LIST_SIZE];
        }

        // search for a spot to insert yon element; we'll insert it at the
        // end of the list if we don't find a spot
        int llength = list.length;
        int index = llength;
        for (int i = 0; i < llength; i++) {
            Object elem = list[i];
            if (elem == null) {
                // only update our target index if we haven't already
                // found a spot to put the element
                if (index == llength) {
                    index = i;
                }

            } else if (eqc.equals(elem, element)) {
                // oops, it's already in the list
                return null;
            }
        }

        // expand the list if necessary
        if (index >= llength) {
            list = accomodate(list, index);
        }

        // stick the element on in
        list[index] = element;

        return list;
    }

    /**
     * Looks for an object that is referentially equal to the supplied
     * element (<code>list[idx] == element</code>).
     *
     * @return true if a matching element was found, false otherwise.
     */
    public static boolean containsRef (Object[] list, Object element)
    {
        return contains(REFERENCE_COMP, list, element);
    }

    /**
     * Looks for an element that is functionally equal to the supplied
     * element (<code>list[idx].equals(element)</code>).
     *
     * @return true if a matching element was found, false otherwise.
     */
    public static boolean contains (Object[] list, Object element)
    {
        return contains(EQUALS_COMP, list, element);
    }

    /** Helper function for {@link #containsRef}, etc. */
    protected static boolean contains (
        EqualityComparator eqc, Object[] list, Object element)
    {
        return (-1 != indexOf(eqc, list, element));
    }

    /** Helper function for {@link #contains}, etc. */
    protected static int indexOf (
        EqualityComparator eqc, Object[] list, Object element)
    {
        requireNotNull(element);
        if (list != null) {
            for (int ii = 0, nn = list.length; ii < nn; ii++) {
                if (eqc.equals(list[ii], element)) {
                    return ii;
                }
            }
        }
        return -1;
    }

    /**
     * Clears out the first element that is referentially equal to the
     * supplied element (<code>list[idx] == element</code>).
     *
     * @return the element that was removed or null if it was not found.
     */
    public static Object clearRef (Object[] list, Object element)
    {
        return clear(REFERENCE_COMP, list, element);
    }

    /**
     * Clears out the first element that is functionally equal to the
     * supplied element (<code>list[idx].equals(element)</code>).
     *
     * @return the object that was cleared from the array or null if no
     * matching object was found.
     */
    public static Object clear (Object[] list, Object element)
    {
        return clear(EQUALS_COMP, list, element);
    }

    /** Helper function for {@link #clearRef}, etc. */
    protected static Object clear (
        EqualityComparator eqc, Object[] list, Object element)
    {
        int dex = indexOf(eqc, list, element);
        if (dex == -1) {
            return null;
        }
        Object elem = list[dex];
        list[dex] = null;
        return elem;
    }

    /**
     * Removes the first element that is referentially equal to the
     * supplied element (<code>list[idx] == element</code>). The elements
     * after the removed element will be slid down the array one spot to
     * fill the place of the removed element.
     *
     * @return the object that was removed from the array or null if no
     * matching object was found.
     */
    public static Object removeRef (Object[] list, Object element)
    {
        return remove(REFERENCE_COMP, list, element);
    }

    /** Helper function for {@link #removeRef}, etc. */
    protected static Object remove (
        EqualityComparator eqc, Object[] list, Object element)
    {
        return remove(list, indexOf(eqc, list, element));
    }

    /**
     * Removes the element at the specified index. The elements after the
     * removed element will be slid down the array one spot to fill the
     * place of the removed element. If a null array is supplied or one
     * that is not large enough to accomodate this index, null is
     * returned.
     *
     * @return the object that was removed from the array or null if no
     * object existed at that location.
     */
    public static Object remove (Object[] list, int index)
    {
        if (list == null) {
            return null;
        }

        int llength = list.length;
        if (llength <= index || index < 0) {
            return null;
        }

        Object elem = list[index];
        System.arraycopy(list, index+1, list, index, llength-(index+1));
        list[llength-1] = null;
        return elem;
    }

    /**
     * Returns the number of elements prior to the first null in the supplied list.
     * @deprecated This is incompatible with things like clearRef(), which leave a null space.
     */
    @Deprecated public static int size (Object[] list)
    {
        if (list == null) {
            return 0;
        }
        int llength = list.length;
        for (int ii = 0; ii < llength; ii++) {
            if (list[ii] == null) {
                return ii;
            }
        }
        return llength;
    }

    /**
     * Creates a new list that will accomodate the specified index and
     * copies the contents of the old list to the first.
     */
    protected static Object[] accomodate (Object[] list, int index)
    {
        int size = list.length;
        // expand size by powers of two until we're big enough
        while (size <= index) {
            size = Math.max(size * 2, DEFAULT_LIST_SIZE);
        }

        // create a new list and copy the contents
        Object[] newlist = new Object[size];
        System.arraycopy(list, 0, newlist, 0, list.length);
        return newlist;
    }

    /**
     * Throws a NullPointerException if the element is null.
     */
    protected static void requireNotNull (Object element)
    {
        if (element == null) {
            throw new NullPointerException("ListUtil does not support null elements.");
        }
    }

    /**
     * Run some tests.
     */
    public static void main (String[] args)
    {
        Object[] list = null;
        String foo = "foo";
        String bar = "bar";

        list = ListUtil.add(list, foo);
        System.out.println("add(foo): " + StringUtil.toString(list));

        list = ListUtil.add(list, bar);
        System.out.println("add(bar): " + StringUtil.toString(list));

        ListUtil.clearRef(list, foo);
        System.out.println("clear(foo): " + StringUtil.toString(list));

        String newBar = new String("bar"); // prevent java from cleverly
                                           // referencing the same string
                                           // from the constant pool
        System.out.println("containsRef(newBar): " +
                           ListUtil.containsRef(list, newBar));
        System.out.println("contains(newBar): " +
                           ListUtil.contains(list, newBar));

        ListUtil.clear(list, newBar);
        System.out.println("clear(newBar): " + StringUtil.toString(list));

        list = ListUtil.add(list, 0, foo);
        list = ListUtil.add(list, 1, bar);
        System.out.println("Added foo+bar: " + StringUtil.toString(list));

        ListUtil.removeRef(list, foo);
        System.out.println("removeRef(foo): " + StringUtil.toString(list));

        list = ListUtil.add(list, 0, foo);
        list = ListUtil.add(list, 1, bar);
        System.out.println("Added foo+bar: " + StringUtil.toString(list));

        ListUtil.remove(list, 0);
        System.out.println("remove(0): " + StringUtil.toString(list));

        ListUtil.remove(list, 0);
        System.out.println("remove(0): " + StringUtil.toString(list));

        Object[] tl = ListUtil.testAndAddRef(list, bar);
        if (tl == null) {
            System.out.println("testAndAddRef(bar): failed: " +
                               StringUtil.toString(list));
        } else {
            list = tl;
            System.out.println("testAndAddRef(bar): added: " +
                               StringUtil.toString(list));
        }

        String biz = "biz";
        tl = ListUtil.testAndAddRef(list, biz);
        if (tl == null) {
            System.out.println("testAndAddRef(biz): failed: " +
                               StringUtil.toString(list));
        } else {
            list = tl;
            System.out.println("testAndAddRef(biz): added: " +
                               StringUtil.toString(list));
        }
    }

    /** Used to allow the same code to optionally use reference equality
     * and {@link Object#equals}.equality. */
    protected static interface EqualityComparator
    {
        public boolean equals(Object o1, Object o2);
    }

    protected static final EqualityComparator REFERENCE_COMP =
        new EqualityComparator() {
            public boolean equals (Object o1, Object o2)
            {
                return o1 == o2;
            }
        };

    protected static final EqualityComparator EQUALS_COMP =
        new EqualityComparator() {
            public boolean equals (Object o1, Object o2)
            {
                return ObjectUtil.equals(o1, o2);
            }
        };

    /**
     * The size of a list to create if we have to create one entirely
     * from scratch rather than just expand it.
     */
    protected static final int DEFAULT_LIST_SIZE = 4;
}
