/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of DQguru
 *
 * DQguru is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DQguru is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */


package ca.sqlpower.matchmaker.swingui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Rectangle;

import org.apache.log4j.Logger;

/**
 * A custom layout for the source table record viewers of the match validation
 * facility.
 * <p>
 * The layout is to line up all the components beside each other, giving them
 * equal height, and their preferred widths plus a bit of buffer space. We
 * provide the buffer space in the layout rather than by giving the source table
 * record viewers their own borders because there are alternating row background
 * colours that have to be butted up against each other to keep the illusion of
 * a continuous row across components.
 */
public class RecordViewerLayout implements LayoutManager {

    Logger logger = Logger.getLogger(RecordViewerLayout.class);
    
    /**
     * The amount of extra width to give each component above and beyond
     * its preferred width.
     */
    private int buffer;
    
    /**
     * Creates a new layout manager with the specified amount of
     * per-component extra width.
     * 
     * @param buffer The amount of extra width, in pixels, to allocate
     * to each component.
     */
    public RecordViewerLayout(int bufferWidth) {
        super();
        this.buffer = bufferWidth;
    }

    /**
     * Performs the layout as described in the class comment.
     */
    public void layoutContainer(Container parent) {
        if (parent.getComponentCount() < 1) return;
        final int height = Math.max(
                parent.getComponent(0).getPreferredSize().height,
                parent.getHeight());
        int x = 0;
        for (Component c : parent.getComponents()) {
            Dimension ps = c.getPreferredSize();
            Rectangle r = new Rectangle(x, 0, ps.width + buffer, height);
            c.setBounds(r);
            x += ps.width + buffer;
        }
        int leftover = parent.getWidth() - x;
        if (leftover > 0) {
            logger.debug("Expanding last component to use up " + leftover + " left over pixels");
            final Component lastComponent = parent.getComponent(parent.getComponentCount() - 1);
            lastComponent.setSize(lastComponent.getWidth() + leftover, lastComponent.getHeight());
        }
    }

    /**
     * Just returns the preferred layout size.
     */
    public Dimension minimumLayoutSize(Container parent) {
        return preferredLayoutSize(parent);
    }

    public Dimension preferredLayoutSize(Container parent) {
        if (parent.getComponentCount() == 0) return new Dimension(0, 0);
        final int height = parent.getComponent(0).getPreferredSize().height;
        int x = 0;
        for (Component c : parent.getComponents()) {
            Dimension ps = c.getPreferredSize();
            x += ps.width + buffer;
        }
        return new Dimension(x, height);
    }

    public void removeLayoutComponent(Component comp) {
        // great, we don't care
    }

    public void addLayoutComponent(String name, Component comp) {
        // great, we don't care
    }

    /**
     * Returns the buffer that this layout manager uses for padding each cell
     */
    public int getBuffer() {
        return buffer;
    }

}
