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

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.munge.SubstringMungeStep;
import ca.sqlpower.validation.swingui.FormValidationHandler;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This is the component for SubstringMungeStep. It contains 
 * two JSpinners for users to set the begin and end index.
 */
public class SubstringMungeComponent extends AbstractMungeComponent {

	private JSpinner begin;
	private JSpinner end;
	
	public SubstringMungeComponent(MungeStep ms, FormValidationHandler handler, MatchMakerSession session) {
		super(ms, handler, session);
	}
	
	@Override
	protected JPanel buildUI() {
		SubstringMungeStep step = (SubstringMungeStep) getStep();
        if (step == null) throw new NullPointerException("Null step!");
		int beginIndex = step.getBegIndex();
		SpinnerNumberModel beginNumberModel = new SpinnerNumberModel(beginIndex, 0, Integer.MAX_VALUE, 1);
		
		begin = new JSpinner(beginNumberModel);
		begin.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				SubstringMungeStep step = (SubstringMungeStep) getStep();
				step.setBegIndex((Integer) begin.getValue());
			}
		
		});
		
		int endIndex = step.getEndIndex();
		SpinnerNumberModel endNumberModel = new SpinnerNumberModel(endIndex, 0, Integer.MAX_VALUE, 1);
		
		end = new JSpinner(endNumberModel);
		end.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				SubstringMungeStep step = (SubstringMungeStep) getStep();
				step.setEndIndex((Integer) end.getValue());
			}
		
		});
		
		FormLayout layout = new FormLayout(
				"4dlu,pref,4dlu,fill:pref:grow,4dlu", // columns
				"4dlu,pref,4dlu,pref,4dlu"); // rows
		CellConstraints cc = new CellConstraints();

		JPanel content = new JPanel(layout);
		
		content.add(new JLabel("Begin Index:"), cc.xy(2,2));
		content.add(begin, cc.xy(4,2));
		content.add(new JLabel("End Index:"), cc.xy(2,4));
		content.add(end, cc.xy(4,4));
		return content;
	}
}
