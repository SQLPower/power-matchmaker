/*
 * Copyright (c) 2007, SQL Power Group Inc.
 *
 * This file is part of Power*MatchMaker.
 *
 * Power*MatchMaker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*MatchMaker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.i 
 */

package ca.sqlpower.matchmaker.swingui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MatchMakerCriteriaGroup;
import ca.sqlpower.matchmaker.MatchMakerObject;

public class MatchMakerTreeCellRenderer extends DefaultTreeCellRenderer {

	final private Icon matchIcon = new ImageIcon(getClass().getResource("/icons/gears_16.png"));
	final private Icon groupIcon = new ImageIcon(getClass().getResource("/icons/gear_16.png"));

	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {

        String text;
        if (value instanceof MatchMakerObject) {
            text = (((MatchMakerObject) value).getName());
        } else {
            text = value.toString();
        }
        
		super.getTreeCellRendererComponent(tree, text, selected, expanded, leaf, row, hasFocus);

		if (value instanceof Match) {
			setIcon(matchIcon);
		} else if (value instanceof MatchMakerCriteriaGroup) {
            MatchMakerCriteriaGroup group = (MatchMakerCriteriaGroup) value;
            if (group.getColour() == null) {
                setIcon(groupIcon);
            } else {
                setIcon(new ColoredIcon(groupIcon, group.getColour()));
            }
		}
		return this;
	}

    /**
     * Applies a colour tint over the given icon when painted.
     */
    private class ColoredIcon implements Icon {

        private Icon sourceIcon;
        private Color tint;
        
        public ColoredIcon(Icon source, Color tint) {
            this.sourceIcon = source;
            this.tint = tint;
        }

        public int getIconHeight() {
            return sourceIcon.getIconHeight();
        }

        public int getIconWidth() {
            return sourceIcon.getIconWidth();
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            BufferedImage img = new BufferedImage(getIconWidth(), getIconHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = (Graphics2D) img.getGraphics();
            sourceIcon.paintIcon(c, g2, 0, 0);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.5f));
            g2.setColor(tint);
            g2.fillRect(0, 0, getIconWidth(), getIconHeight());
            g2.dispose();
            
            g.drawImage(img, x, y, null);
        }
     
        
    }
}
