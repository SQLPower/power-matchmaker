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

package ca.sqlpower.matchmaker.swingui.munge;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.munge.AbstractMungeStep;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.munge.StringToNumberMungeStep;
import ca.sqlpower.validation.swingui.FormValidationHandler;

/**
 * the munge component for the string to number munge component. It only has one check box 
 * indicating if the program should terminate on an error.
 */
public class StringToNumberMungeComponent extends AbstractMungeComponent {

	private JCheckBox contOnError;
	
	public StringToNumberMungeComponent(MungeStep ms, FormValidationHandler handler, MatchMakerSession session) {
		super(ms, handler, session);
	}

	@Override
	protected JPanel buildUI() {
		JPanel content = new JPanel();
		contOnError = new JCheckBox("Continue on Error");
		contOnError.setSelected(((AbstractMungeStep)getStep()).getBooleanParameter(
				StringToNumberMungeStep.CONTINUE_ON_MALFORMED_NUMBER));
		
		contOnError.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				AbstractMungeStep temp = (AbstractMungeStep) getStep();
				temp.setParameter(StringToNumberMungeStep.CONTINUE_ON_MALFORMED_NUMBER, contOnError.isSelected());
			}
			
		});
		content.add(contOnError);
		return content;
	}
}
