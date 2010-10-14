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

package ca.sqlpower.matchmaker.swingui.munge;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchMakerTranslateGroup;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.munge.TranslateWordMungeStep;
import ca.sqlpower.validation.swingui.FormValidationHandler;

/**
 * This is the component for a translate word munge step. It has a two options, 
 * a checkbox to decide whether to use regular expressions, and a combo box to
 * select the translate group to use.
 */
public class TranslateWordMungeComponent extends AbstractMungeComponent {

	private JCheckBox useRegex;
	private JCheckBox caseSensitive;
	private JComboBox translateGroup; 

	public TranslateWordMungeComponent(MungeStep ms, FormValidationHandler handler, MatchMakerSession session) {
		super(ms, handler, session);
	}
	
	@Override
	protected JPanel buildUI() {
		JPanel content = new JPanel();
		TranslateWordMungeStep temp = (TranslateWordMungeStep) getStep();
		
		useRegex = new JCheckBox("Use Regular Expressions");
		useRegex.setSelected(temp.isRegex());
		useRegex.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				TranslateWordMungeStep temp = (TranslateWordMungeStep) getStep();
				temp.setRegex(useRegex.isSelected());
			}
			
		});
		
		caseSensitive = new JCheckBox("Case Sensitive");
		caseSensitive.setSelected(temp.isCaseSensitive());
		caseSensitive.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				TranslateWordMungeStep temp = (TranslateWordMungeStep) getStep();
				temp.setCaseSensitive(caseSensitive.isSelected());
			}
			
		});
		
		if(temp.getSession()!= null) {
			
			// Fills the combo box with the translate groups only if the session exists
			MatchMakerTranslateGroup[] translateGroups =
			    temp.getSession().getTranslations().getChildren().toArray(new MatchMakerTranslateGroup[0]);
			translateGroup = new JComboBox(translateGroups);
			
			// Sets the combo box to select the translate group in the parameter
			if (temp.getTranslateGroup() != null) {
			    translateGroup.setSelectedItem(temp.getTranslateGroup());
			    
			// Sets the parameter to the first translate group in the list if the step
			// did not specific a translate group.
			} else if (translateGroup.getSelectedItem() != null) {
					temp.setTranslateGroup((MatchMakerTranslateGroup)translateGroup.getSelectedItem());
			}
			
		} else {
			translateGroup = new JComboBox();
		}
		translateGroup.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				TranslateWordMungeStep temp = (TranslateWordMungeStep) getStep();
				if (translateGroup.getSelectedItem() != null) {
					temp.setTranslateGroup((MatchMakerTranslateGroup)translateGroup.getSelectedItem());
				}
			}
			
		});
		
		content.setLayout(new GridLayout(4,1));
		content.add(new JLabel("Translate Group:"));
		content.add(translateGroup);
		content.add(useRegex);
		content.add(caseSensitive);
		return content;
	}
}
