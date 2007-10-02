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

import javax.swing.JButton;
import javax.swing.JPanel;

import ca.sqlpower.matchmaker.munge.MungeStep;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This is a component for a concat munge step. It has two options, one button
 * that adds inputs and one button to clean up the unused inputs.
 */
public class ConcatMungeComponent extends AbstractMungeComponent {
	
	private JButton addInputButton;
	private JButton removeInputsButton;

	public ConcatMungeComponent(MungeStep step) {
		super(step);
	}

	@Override
	protected JPanel buildUI() {
		JPanel content = new JPanel();
		addInputButton = new JButton(new AddInputAction("Add Input"));
		removeInputsButton = new JButton(
				new RemoveUnusedInputAction("Clean Up"));
		addInputButton.setBackground(getBg());
		removeInputsButton.setBackground(getBg());

		FormLayout layout = new FormLayout(
				"4dlu,pref,4dlu", // columns
				"4dlu,pref,4dlu,pref,4dlu"); // rows
		CellConstraints cc = new CellConstraints();
		content.setLayout(layout);
		content.add(addInputButton, cc.xy(2,2, "c,c"));
		content.add(removeInputsButton, cc.xy(2,4, "c,c"));
		return content;
	}

}
