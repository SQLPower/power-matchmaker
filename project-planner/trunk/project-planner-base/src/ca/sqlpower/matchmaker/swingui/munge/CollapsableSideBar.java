/*
 * Copyright (c) 2008, SQL Power Group Inc.
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

package ca.sqlpower.matchmaker.swingui.munge;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.border.EtchedBorder;

import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.VerticalTextPanel;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This wraps a component of a horizontal JSplitPane with expand/collapse buttons.
 * 
 */
public class CollapsableSideBar {
	
	private final JPanel expandedPanel;
	private int dividerBorder = 20;
	
	private static final ImageIcon RIGHT_ICON = SPSUtils.createIcon("chevrons_right1", "");
	private static final ImageIcon LEFT_ICON = SPSUtils.createIcon("chevrons_left1", "");
	
	/**
	 * Builds the ui of the component, call getPanel() for the component.
	 * 
	 * @param sideComp Component to be wrapped
	 * @param parent Parent splitpane
	 * @param text Text to display when collapsed
	 * @param bgColor Background color to use when collapsed
	 * @param rightComponent Indicates whether the component was the right component in its parent
	 */
	public CollapsableSideBar(final JComponent sideComp, final JSplitPane parent,
			String text, Color bgColor, final boolean rightComponent) {
		expandedPanel = new JPanel(new FormLayout("fill:pref:grow", "pref,fill:pref:grow"));
        final JPanel collapsedPanel = new JPanel(new FormLayout("fill:20dlu:grow", "pref,fill:pref:grow"));
        CellConstraints cc = new CellConstraints();
        
        final VerticalTextPanel collapsedText = new VerticalTextPanel(text);
        collapsedText.setFont(collapsedText.getFont().deriveFont(Font.BOLD));
		collapsedText.setBackground(bgColor);
		collapsedText.setForeground(Color.WHITE);
		collapsedText.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		collapsedText.setOpaque(true);
		
		final JButton expandButton = new JButton(new AbstractAction(){
			public void actionPerformed(ActionEvent arg0) {
				parent.setDividerSize(10);
				if (rightComponent) {
					parent.setDividerLocation(parent.getWidth() - sideComp.getPreferredSize().width - dividerBorder);
					parent.setRightComponent(expandedPanel);
				} else {
					parent.setDividerLocation(sideComp.getPreferredSize().width + dividerBorder);
					parent.setLeftComponent(expandedPanel);
				}
			}
			
		});
        
        final JButton collapseButton = new JButton(new AbstractAction(){
        	public void actionPerformed(ActionEvent arg0) {
        		parent.setDividerSize(0);
        		if (rightComponent) {
					parent.setDividerLocation(parent.getWidth() - expandButton.getPreferredSize().width - dividerBorder);
        			parent.setRightComponent(collapsedPanel);
        		} else {
        			parent.setDividerLocation(expandButton.getPreferredSize().width + dividerBorder);
        			parent.setLeftComponent(collapsedPanel);
        		}
			}
        });
        
        if (rightComponent) {
        	collapseButton.setIcon(RIGHT_ICON);
        	expandButton.setIcon(LEFT_ICON);
        } else {
        	collapseButton.setIcon(LEFT_ICON);
        	expandButton.setIcon(RIGHT_ICON);
        }
        expandedPanel.add(collapseButton, cc.xy(1, 1));
        expandedPanel.add(sideComp, cc.xy(1, 2, "f,f"));
        
        collapsedPanel.add(expandButton, cc.xy(1, 1));
		collapsedPanel.add(collapsedText, cc.xy(1, 2, "f,f"));
		
	}
	
	public JPanel getPanel() {
		return expandedPanel;
	}

	public int getDividerBorder() {
		return dividerBorder;
	}

	/**
	 * Sets the divider border which decides how far 
	 * away from the side bar the divider is.
	 */
	public void setDividerBorder(int dividerBorder) {
		this.dividerBorder = dividerBorder;
	}
	
	
}
