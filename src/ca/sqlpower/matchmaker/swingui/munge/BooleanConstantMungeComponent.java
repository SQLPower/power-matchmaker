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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.munge.BooleanConstantMungeStep;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.validation.swingui.FormValidationHandler;

/**
 * This is the component for a constant boolean with a dropdown menu for the three items. 
 */
public class BooleanConstantMungeComponent extends AbstractMungeComponent {
	
	public BooleanConstantMungeComponent(MungeStep ms, FormValidationHandler handler, MatchMakerSession session) {
		super(ms, handler, session);
	}

	@Override
	protected JPanel buildUI() {
		JPanel content = new JPanel();
		
		String[] vals = new String[]{BooleanConstantMungeStep.TRUE, BooleanConstantMungeStep.FALSE, BooleanConstantMungeStep.NULL};
		
		final JComboBox box = new JComboBox(vals);
		box.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				getStep().setParameter(BooleanConstantMungeStep.BOOLEAN_VALUE, (String)box.getSelectedItem());
			}
		});
		content.add(box);
		return content;
	}
}
