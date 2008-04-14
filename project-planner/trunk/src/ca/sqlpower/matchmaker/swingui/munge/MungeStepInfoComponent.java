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
import java.awt.Component;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;

import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.util.BrowserUtil;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

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
	 * This listener will listen on all munge steps in the munge pen and update
	 * the description field when a component receives focus.
	 */
	private FocusListener mungeStepFocusListener = new FocusListener() {
		public void focusLost(FocusEvent e) {
		}
		public void focusGained(FocusEvent e) {
			if (e.getComponent() instanceof AbstractMungeComponent) {
				AbstractMungeComponent comp = (AbstractMungeComponent) e.getComponent();
				MungeStep mungeStep = comp.getStep();
				stepInfoText.setText(mungeStep.getName() +
						(mungeStep.getDescription() != null ? "\n" + mungeStep.getDescription() : ""));
				if (mungeStep.getInfoURL() != null && mungeStep.getInfoURL().trim().length() > 0) {
					moreInfo.setText("<html><body><p><a href=\"" + mungeStep.getInfoURL() + "\">More Info</a>");
				} else {
					moreInfo.setText("");
				}
			}
		}
	};
	
	/**
	 * The text pane that will display the step description for selected steps
	 * in the munge pen.
	 */
	private JTextPane stepInfoText = new JTextPane();

	/**
	 * This panel holds all of the text components that describe the selected munge
	 * component.
	 */
	private JPanel stepInfoPanel;
	
	/**
	 * If the user clicks on the url in this pane then a web browser should open with the
	 * page specified by the selected munge step's more information URL.
	 */

	private JEditorPane moreInfo;
	
	public MungeStepInfoComponent(MungePen mungePen) {
		stepInfoText = new JTextPane();
		stepInfoText.setEditable(false);
		stepInfoText.setBackground(null);
		
		mungePen.addContainerListener(new ContainerListener() {
			public void componentAdded(ContainerEvent e) {
				e.getChild().addFocusListener(mungeStepFocusListener);
			}
			public void componentRemoved(ContainerEvent e) {
				e.getChild().removeFocusListener(mungeStepFocusListener);
			}
		});
		for (Component comp : mungePen.getComponents()) {
			comp.addFocusListener(mungeStepFocusListener);
		}
		
		HTMLEditorKit htmlKit = new HTMLEditorKit();
        moreInfo = new JEditorPane();
        moreInfo.setText("test");
		moreInfo.setEditorKit(htmlKit);
        moreInfo.setEditable(false);
        moreInfo.setBackground(null);
        
        /* Jump to the URL (in the user's configured browser)
         * when a link is clicked.
         */
        moreInfo.addHyperlinkListener(new HyperlinkListener() {
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
		
		stepInfoPanel = new JPanel();
		stepInfoPanel.setBackground(MEDIUM_BLUE);
		FormLayout infoPanelLayout = new FormLayout("pref:grow", "pref, pref");
		stepInfoPanel.setLayout(infoPanelLayout);
		CellConstraints infoPanelCC = new CellConstraints();
		stepInfoPanel.add(stepInfoText, infoPanelCC.xy(1, 1));
		stepInfoPanel.add(moreInfo, infoPanelCC.xy(1, 2));
	}
	
	public JPanel getPanel() {
		return stepInfoPanel;
	}
}
