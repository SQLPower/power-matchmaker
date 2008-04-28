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

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * The panel this class makes will contain a panel above and below a panel with its title and
 * sub title. This is now mainly a convenience class for building the side bars to the left
 * and right of the main editor screens of the Project Planner.
 */
public class MungePenSideBar {
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(MungePenSideBar.class);
	
	/**
	 * The component that will hold the munge step description, the munge step
	 * library and the expand/collapse button.
	 */
	private JToolBar toolbar;
	
	/**
	 * The panel that will store the step description and munge step library.
	 * This is separate from the toolbar as we will need to hide this based
	 * on the expand/collapse button.
	 */
	private JPanel mainPanel;
	
	/**
	 * The background colour to use for the title pane.
	 */
	private Color titleBackgroundColor;
	
	/**
	 * Constructs a toolbar to display information about selected steps in the
	 * munge pen as well as listing all of the munge steps available.
	 * 
	 * @param topPanel
	 *            the JComponent that will be displayed above the title of this
	 *            side bar.
	 * @param bottomPanel
	 *            the JComponent that will be displayed below the title of this
	 *            side bar.
	 */
	public MungePenSideBar(JComponent topPanel, JComponent bottomPanel, String title, String subTitle, Color titleBackgroundColor) {
		this.titleBackgroundColor = titleBackgroundColor;
		
		FormLayout layout = new FormLayout("fill:pref:grow", "pref, pref, fill:pref:grow");
		mainPanel = logger.isDebugEnabled() ? new FormDebugPanel(layout) : new JPanel(layout);
		PanelBuilder pb = new PanelBuilder(layout, mainPanel);
		CellConstraints cc = new CellConstraints();
		
		pb.add(topPanel, cc.xy(1, 1));

		pb.add(bottomPanel, cc.xy(1, 3));
		
		JPanel titlePanel = new JPanel(new FormLayout("pref", "pref, pref"));
		titlePanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		CellConstraints titleCC = new CellConstraints();
		JLabel titleLabel = new JLabel(title);
		titleLabel.setBackground(null);
		titleLabel.setForeground(null);
		titlePanel.add(titleLabel, titleCC.xy(1, 1));
		JLabel subTitleLabel = new JLabel(subTitle);
		subTitleLabel.setBackground(null);
		subTitleLabel.setForeground(null);
		titlePanel.add(subTitleLabel, titleCC.xy(1, 2));
		titlePanel.setBackground(titleBackgroundColor);
		titlePanel.setForeground(Color.WHITE);
		pb.add(titlePanel, cc.xy(1, 2));
		
		toolbar = new JToolBar();
		toolbar.setLayout(new BorderLayout());
		toolbar.add(pb.getPanel(), BorderLayout.CENTER);
		toolbar.setBorder(BorderFactory.createRaisedBevelBorder());
		toolbar.setFloatable(false);
	}

	public JToolBar getToolbar() {
		return toolbar;
	}
	
}
