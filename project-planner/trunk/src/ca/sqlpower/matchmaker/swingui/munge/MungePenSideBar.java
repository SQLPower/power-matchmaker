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
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.matchmaker.swingui.SwingSessionContext;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * The panel this class makes will contain a panel above and below a panel with its title and
 * sub title. The panel will have a button at the top that will allow collapsing and expanding.
 * If the panel is collapsed the entire panel will display just its title.
 */
public class MungePenSideBar {
	
	/**
	 * The dark blue colour to be used as a background to the project steps
	 * side bar title.
	 */
	private static final Color DARK_BLUE = new Color(0x003082);
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(MungePenSideBar.class);
	
	/**
	 * The icon to display for the hideShow button if the mouse is over the
	 * button and the toolbar is not expanded.
	 */
	private static final Icon PLUS_ON = new ImageIcon(ClassLoader.getSystemResource("icons/chevrons_left2.png"));
	
	/**
	 * The icon to display for the hideShow button if the mouse is not over the
	 * button and the toolbar is not expanded.
	 */
	private static final Icon PLUS_OFF = new ImageIcon(ClassLoader.getSystemResource("icons/chevrons_left1.png"));
	
	/**
	 * The icon to display for the hideShow button if the mouse is over the
	 * button and the toolbar is expanded.
	 */
	private static final Icon MINUS_ON = new ImageIcon(ClassLoader.getSystemResource("icons/chevrons_right2.png"));
	
	/**
	 * The icon to display for the hideShow button if the mouse is not over the
	 * button and the toolbar is expanded.
	 */
	private static final Icon MINUS_OFF = new ImageIcon(ClassLoader.getSystemResource("icons/chevrons_right1.png"));
	
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
	 * The button that collapses and expands the entire library
	 * tool bar.
	 */
	private JButton hideShow;
	
	/**
	 * Defines if the tool bar is collaped and or expanded.
	 */
	private boolean collapsed;
	
	/**
	 * Constructs a toolbar to display information about selected steps in the
	 * munge pen as well as listing all of the munge steps available.
	 * 
	 * @param mungePen
	 *            The munge pen to drag items from the library to. Also listens
	 *            for step selection to show information about steps.
	 * @param swingSession
	 *            The session that contains the munge pen.
	 */
	public MungePenSideBar(MungePen mungePen, MatchMakerSwingSession swingSession, MungeProcess process) {
		String title = "PROJECT STEPS";
		String subTitle = "(Drag into playpen)";
		collapsed = false;
		
		hideShow = new JButton(new AbstractAction(){
			public void actionPerformed(ActionEvent e) {
				collapsed = !collapsed;
				
				toolbar.updateUI();
				hideShow.updateUI();

				if (collapsed) {
					hideShow.setIcon(PLUS_ON);
				} else {
					hideShow.setIcon(MINUS_ON);
				}
				
				hideShow.repaint();
			}
		});
		
		hideShow.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseEntered(MouseEvent e) {
				if (collapsed) {
					hideShow.setIcon(PLUS_ON);
				} else {
					hideShow.setIcon(MINUS_ON);
				}
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				if (collapsed) {
					hideShow.setIcon(PLUS_OFF);
				} else {
					hideShow.setIcon(MINUS_OFF);
				}
			}
		});
		
		hideShow.setIcon(MINUS_OFF);
		
		FormLayout layout = new FormLayout("pref", "pref, pref, fill:pref:grow");
		mainPanel = logger.isDebugEnabled() ? new FormDebugPanel(layout) : new JPanel(layout);
		PanelBuilder pb = new PanelBuilder(layout, mainPanel);
		CellConstraints cc = new CellConstraints();
		
		pb.add(new MungeStepInfoComponent(mungePen).getPanel(), cc.xy(1, 1));

		MungeStepLibrary msl = new MungeStepLibrary(mungePen, ((SwingSessionContext) swingSession.getContext()).getStepMap());
		pb.add(msl.getScrollPane(), cc.xy(1, 3));
		
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
		titlePanel.setBackground(DARK_BLUE);
		titlePanel.setForeground(Color.WHITE);
		pb.add(titlePanel, cc.xy(1, 2));
		
		toolbar = new JToolBar();
		toolbar.setLayout(new BorderLayout());
		toolbar.add(hideShow, BorderLayout.NORTH);
		toolbar.add(pb.getPanel(), BorderLayout.CENTER);
		toolbar.setBorder(BorderFactory.createRaisedBevelBorder());
		toolbar.setFloatable(false);
	}

	public JToolBar getToolbar() {
		return toolbar;
	}
	
}
