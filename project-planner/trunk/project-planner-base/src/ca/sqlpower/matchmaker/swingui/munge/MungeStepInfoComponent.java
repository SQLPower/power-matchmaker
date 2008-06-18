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
import java.awt.Dimension;
import java.awt.Font;
import java.io.IOException;
import java.net.URL;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import ca.sqlpower.util.BrowserUtil;

/**
 * Creates the component that will listen to the munge pen for the selection
 * of munge step components and update itself with component information.
 */
public class MungeStepInfoComponent {
	
	/**
	 * The medium blue colour to be used as a background to the munge step
	 * information area.
	 */
	private static final Color MEDIUM_BLUE = new Color(0xb7c5dc);
	
	/**
	 * The scroll pane that holds info component.
	 */
	private JScrollPane scrollPane;
	
	/**
	 * Default font to use.
	 */
	private Font font = (Font)UIManager.get("Label.font");
	
	private final static int SCROLLPANE_HEIGHT = 75;
	
	/**
	 *	Updates the info with given step description 
	 */
	private void updateInfo(StepDescription sd) {
		StringBuilder stepInfoText = new StringBuilder();
		
		// begins by setting font
		// bad approach in diving by 4 but the html text with the same font appears huge
		stepInfoText.append("<html><body><font face =\"" + font.getFamily() 
				+ "\" size=\"" + font.getSize()/4 + "\">");
		
		stepInfoText.append("<b>" + sd.getName() + "</b>");
		
		if (sd.getDescription() != null) {
			stepInfoText.append("<br>" + sd.getDescription());
		}
		
		if (sd.getInfoURL() != null && sd.getInfoURL().trim().length() > 0) {
			stepInfoText.append("<br><a href=\"" + sd.getInfoURL() + "\">More Info</a>");
		}
		
		stepInfoText.append("</font>");
		
		stepInfo.setText(stepInfoText.toString());
		
		// "anti-autoscroll"
		stepInfo.setCaretPosition(0);
	}
	
	/**
	 * If the user clicks on the url in this pane then a web browser should open with the
	 * page specified by the selected munge step's more information URL.
	 */

	private JEditorPane stepInfo;
	
	public MungeStepInfoComponent(MungeStepLibrary msl) {
		HTMLEditorKit htmlKit = new HTMLEditorKit();
        stepInfo = new JEditorPane();
        stepInfo.setText("test");
		stepInfo.setEditorKit(htmlKit);
        stepInfo.setEditable(false);
        stepInfo.setBackground(MEDIUM_BLUE);
        
        /* Jump to the URL (in the user's configured browser)
         * when a link is clicked.
         */
        stepInfo.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent evt) {
                if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    URL url = evt.getURL();
                    try {
                        BrowserUtil.launch(url.toString());
                    } catch (IOException e1) {
                        throw new RuntimeException("Unexpected error in launch", e1);
                    }
                }
            }
        });
		
		msl.getTree().addTreeSelectionListener(new TreeSelectionListener(){
			public void valueChanged(TreeSelectionEvent e) {
				TreePath tp = e.getPath();
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) tp.getLastPathComponent();
				if (tp != null && node.getUserObject() instanceof StepDescription) {
					updateInfo(((StepDescription) node.getUserObject()));
				}
			}
		});
		
		scrollPane = new JScrollPane(stepInfo);
		scrollPane.setPreferredSize(new Dimension(msl.getScrollPane().getPreferredSize().width, SCROLLPANE_HEIGHT));
	}
	
	public JComponent getScrollPane() {
		return scrollPane;
	}
}
