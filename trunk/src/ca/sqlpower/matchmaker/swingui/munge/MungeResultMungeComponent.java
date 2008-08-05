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

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.validation.swingui.FormValidationHandler;

import com.jgoodies.forms.factories.ButtonBarFactory;

/**
 * This is a component for dedupe result munge step. It has two options, one button
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
		addInputButton = new JButton(new AddInputAction("Add Input"));
		removeInputsButton = new JButton(new RemoveUnusedInputAction("Clean Up"));

		return ButtonBarFactory.buildAddRemoveBar(addInputButton, removeInputsButton);
	}

	@Override
	public void remove() {
	}
	
	@Override
	public JPopupMenu getPopupMenu() {
		return null;
	}
}
