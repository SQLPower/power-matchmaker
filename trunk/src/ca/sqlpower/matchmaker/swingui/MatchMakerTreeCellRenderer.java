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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
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

import ca.sqlpower.matchmaker.FolderParent;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MatchMakerFolder;
import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.TableMergeRules;
import ca.sqlpower.matchmaker.TranslateGroupParent;
import ca.sqlpower.matchmaker.munge.AbstractMungeStep;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.swingui.MatchMakerTreeModel.MatchActionNode;
import ca.sqlpower.matchmaker.swingui.MatchMakerTreeModel.MatchActionType;

public class MatchMakerTreeCellRenderer extends DefaultTreeCellRenderer {

	final private Icon matchIcon = new ImageIcon(getClass().getResource("/icons/match_project.png"));
	final private Icon mungeIcon = new ImageIcon(getClass().getResource("/icons/cog.png"));
	final private Icon mergeIcon = new ImageIcon(getClass().getResource("/icons/cog_double.png"));
	final private Icon sourceMergeIcon = new ImageIcon(getClass().getResource("/icons/cog_double_star.png"));
	final private Icon folderIcon = new ImageIcon(getClass().getResource("/icons/famfamfam/folder.png"));
	final private Icon validateIcon = new ImageIcon(getClass().getResource("/icons/famfamfam/tick.png"));
	final private Icon infoIcon = new ImageIcon(getClass().getResource("/icons/famfamfam/page_white_gear.png"));
	final private Icon matchEngineIcon = new ImageIcon(getClass().getResource("/icons/famfamfam/cog_go.png"));
	final private Icon mungeCompIcon = new ImageIcon(getClass().getResource("/icons/famfamfam/application_form.png"));
	final private Icon mergeEngineIcon = new ImageIcon(getClass().getResource("/icons/cog_double_go.png"));

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
		} else if (value instanceof MungeProcess) {
            MungeProcess group = (MungeProcess) value;
            if (group.getColour() == null) {
                setIcon(mungeIcon);
            } else {
                setIcon(new ColoredIcon(mungeIcon, group.getColour()));
            }
		} else if (value instanceof TableMergeRules) {
			TableMergeRules rule = (TableMergeRules) value;
			if (rule.isSourceMergeRule()) {
				setIcon(sourceMergeIcon);
			} else {
				setIcon(mergeIcon);
			}
		} else if (value instanceof MatchActionNode) {
			MatchActionType val = ((MatchActionNode) value).getActionType();
			if (val.equals(MatchActionType.VALIDATE_MATCHES)) {
				setIcon(validateIcon);
			} else if (val.equals(MatchActionType.RUN_MATCH)) {
				setIcon(matchEngineIcon);
			} else if (val.equals(MatchActionType.RUN_MERGE)) {
				setIcon(mergeEngineIcon);
			} else { 
				setIcon(infoIcon);
			}
		} else if (value instanceof PlFolder ||	 value instanceof MatchMakerFolder ||
				value instanceof TranslateGroupParent || value instanceof FolderParent ){
			setIcon(folderIcon);
		} else if (value instanceof AbstractMungeStep) {
			setIcon(mungeCompIcon);
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
