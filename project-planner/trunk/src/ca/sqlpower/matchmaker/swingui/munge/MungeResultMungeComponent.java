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

import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.validation.swingui.FormValidationHandler;

/**
 * This is a component for dedupe reslt munge step. It has two options, one button
 * that adds inputs and one button to clean up the unused inputs.
 */
public class MungeResultMungeComponent extends AbstractMungeComponent {
	
	private JButton addInputButton;
	private JButton removeInputsButton;
	
	public MungeResultMungeComponent(MungeStep ms, FormValidationHandler handler, MatchMakerSession session) {
		super(ms, handler,session);
	}
	
	@Override
	protected JPanel buildUI() {
		JPanel content = new JPanel(new FlowLayout());
		addInputButton = new JButton(new AddInputAction("Add Input"));
		removeInputsButton = new JButton(
				new RemoveUnusedInputAction("Clean Up"));

		content.add(addInputButton);
		content.add(removeInputsButton);
		return content;
	}

	@Override
	public void remove() {
	}
	
	@Override
	public JPopupMenu getPopupMenu() {
		return null;
	}
}
