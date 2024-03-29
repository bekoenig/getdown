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

package io.github.bekoenig.getdown.launcher.swing;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

/**
 * Group layout managers lay out widgets in horizontal or vertical groups.
 */
public abstract class GroupLayout
    implements LayoutManager2 {
    /**
     * The group layout managers supports two constraints: fixedness
     * and weight. A fixed component will not be stretched along the major
     * axis of the group. Those components that are stretched will have
     * the extra space divided among them according to their weight
     * (specifically receiving the ratio of their weight to the total
     * weight of all of the free components in the container).
     * <p>
     * To add a component with the fixed constraints, use the FIXED constant.
     */
    public static class Constraints {
        /**
         * Constructs a new constraints object with the specified weight,
         * which is only applicable with the STRETCH policy.
         */
        public Constraints(int weight) {
            _weight = weight;
        }

        /**
         * Is this Constraints specifying fixed?
         */
        public final boolean isFixed() {
            return (this == FIXED);
        }

        /**
         * Get the weight.
         */
        public final int getWeight() {
            return _weight;
        }

        /**
         * The weight of this component relative to the other components
         * in the container. Only valid if the layout policy is STRETCH.
         */
        protected int _weight = 1;
    }

    /**
     * A class used to make our policy constants type-safe.
     */
    public static class Policy {
    }

    /**
     * A class used to make our policy constants type-safe.
     */
    public static class Justification {
    }

    /**
     * A constraints object that indicates that the component should be
     * fixed and have the default weight of one. This is so commonly used
     * that we create and make this object available here.
     */
    public final static Constraints FIXED = new Constraints(Integer.MIN_VALUE);

    /**
     * Do not adjust the widgets on this axis.
     */
    public final static Policy NONE = new Policy();

    /**
     * Stretch all the widgets to their maximum possible size on this axis.
     */
    public final static Policy STRETCH = new Policy();

    /**
     * Stretch all the widgets to be equal to the size of the largest widget on this axis.
     */
    public final static Policy EQUALIZE = new Policy();

    /**
     * Only valid for off-axis policy, this leaves widgets alone unless they are larger in the
     * off-axis direction than their container, in which case it constrains them to fit on the
     * off-axis.
     */
    public final static Policy CONSTRAIN = new Policy();

    /**
     * A justification constant.
     */
    public final static Justification CENTER = new Justification();

    /**
     * A justification constant.
     */
    public final static Justification LEFT = new Justification();

    /**
     * A justification constant.
     */
    public final static Justification RIGHT = new Justification();

    /**
     * A justification constant.
     */
    public final static Justification TOP = new Justification();

    /**
     * A justification constant.
     */
    public final static Justification BOTTOM = new Justification();

    /**
     * The default gap between components, in pixels.
     */
    public static final int DEFAULT_GAP = 5;

    public void addLayoutComponent(String name, Component comp) {
        // nothing to do here
    }

    public void removeLayoutComponent(Component comp) {
        if (_constraints != null) {
            _constraints.remove(comp);
        }
    }

    public void addLayoutComponent(Component comp, Object constraints) {
        if (constraints != null) {
            if (constraints instanceof Constraints) {
                if (_constraints == null) {
                    _constraints = new HashMap<>();
                }
                _constraints.put(comp, (Constraints) constraints);

            } else {
                throw new RuntimeException(
                    "GroupLayout constraints object must be of type GroupLayout.Constraints");
            }
        }
    }

    public float getLayoutAlignmentX(Container target) {
        // we don't support alignment like this
        return 0f;
    }

    public float getLayoutAlignmentY(Container target) {
        // we don't support alignment like this
        return 0f;
    }

    public Dimension minimumLayoutSize(Container parent) {
        return getLayoutSize(parent, MINIMUM);
    }

    public Dimension preferredLayoutSize(Container parent) {
        return getLayoutSize(parent, PREFERRED);
    }

    public Dimension maximumLayoutSize(Container parent) {
        return getLayoutSize(parent, MAXIMUM);
    }

    protected abstract Dimension getLayoutSize(Container parent, int type);

    public abstract void layoutContainer(Container parent);

    public void invalidateLayout(Container target) {
        // nothing to do here
    }

    /**
     * Get the Constraints for the specified child component.
     *
     * @return a Constraints object, never null.
     */
    protected Constraints getConstraints(Component child) {
        if (_constraints != null) {
            Constraints c = _constraints.get(child);
            if (c != null) {
                return c;
            }
        }

        return DEFAULT_CONSTRAINTS;
    }

    /**
     * Computes dimensions of the children widgets that are useful for the
     * group layout managers.
     */
    protected DimenInfo computeDimens(Container parent, int type) {
        int count = parent.getComponentCount();
        DimenInfo info = new DimenInfo();
        info.dimens = new Dimension[count];

        for (int i = 0; i < count; i++) {
            Component child = parent.getComponent(i);
            if (!child.isVisible()) {
                continue;
            }

            Dimension csize;
            switch (type) {
                case MINIMUM:
                    csize = child.getMinimumSize();
                    break;

                case MAXIMUM:
                    csize = child.getMaximumSize();
                    break;

                default:
                    csize = child.getPreferredSize();
                    break;
            }

            info.count++;
            info.totwid += csize.width;
            info.tothei += csize.height;

            if (csize.width > info.maxwid) {
                info.maxwid = csize.width;
            }
            if (csize.height > info.maxhei) {
                info.maxhei = csize.height;
            }

            Constraints c = getConstraints(child);
            if (c.isFixed()) {
                info.fixwid += csize.width;
                info.fixhei += csize.height;
                info.numfix++;

            } else {
                info.totweight += c.getWeight();

                if (csize.width > info.maxfreewid) {
                    info.maxfreewid = csize.width;
                }
                if (csize.height > info.maxfreehei) {
                    info.maxfreehei = csize.height;
                }
            }

            info.dimens[i] = csize;
        }

        return info;
    }

    /**
     * Creates a {@link JPanel} that is configured with an {@link HGroupLayout} with a
     * configuration conducive to containing a row of buttons. Any supplied buttons are added to
     * the box.
     */
    public static JPanel makeButtonBox(Justification justification, Component... buttons) {
        JPanel box = new JPanel(new HGroupLayout(NONE, justification));
        for (Component button : buttons) {
            box.add(button);
            box.setOpaque(false);
        }
        return box;
    }

    protected Policy _policy = NONE;
    protected Policy _offpolicy = CONSTRAIN;
    protected int _gap = DEFAULT_GAP;
    protected Justification _justification = CENTER;
    protected Justification _offjust = CENTER;

    protected HashMap<Component, Constraints> _constraints;

    protected static final int MINIMUM = 0;
    protected static final int PREFERRED = 1;
    protected static final int MAXIMUM = 2;

    /**
     * All children added without a Constraints object are
     * constrained by this Constraints object.
     */
    protected static final Constraints DEFAULT_CONSTRAINTS = new Constraints(1);
}
