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

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.validation.swingui.FormValidationHandler;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class ProjectPlannerMungeComponent extends AbstractMungeComponent {
	
	private static final Logger logger = Logger.getLogger(ProjectPlannerMungeComponent.class);

	public ProjectPlannerMungeComponent(MungeStep step,
			FormValidationHandler handler, MatchMakerSession session, Icon mainIcon) {
		super(step, handler, session, mainIcon);
	}

	@Override
	protected JPanel buildUI() {
		JPanel panel = new JPanel();
		FormLayout layout = new FormLayout("100px", "pref, pref:grow");
		panel.setLayout(layout);
		CellConstraints cc = new CellConstraints();
		
		logger.debug("The main icon for this component is " + getMainIcon());
		
		JLabel iconLabel = new JLabel(getMainIcon());
		
		JTextPane text = new JTextPane();
		text.setBorder(null);
		text.setBackground(null);
		text.setOpaque(false);
		DefaultStyledDocument doc = new DefaultStyledDocument();
		MutableAttributeSet standard = new SimpleAttributeSet();
		StyleConstants.setAlignment(standard, StyleConstants.ALIGN_CENTER);
		doc.setParagraphAttributes(0, 0, standard, true);
		text.setDocument(doc);
		text.setText(getStep().getName());
		
		panel.add(iconLabel, cc.xy(1, 1));
		panel.add(text, cc.xy(1, 2));
		
		return panel;
	}

}
