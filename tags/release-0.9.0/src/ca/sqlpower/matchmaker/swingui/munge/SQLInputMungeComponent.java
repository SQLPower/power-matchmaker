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

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.validation.swingui.FormValidationHandler;

/**
 * This is a component for a lower case munge step. It has no options at all.
 */
public class SQLInputMungeComponent extends AbstractMungeComponent {

	public SQLInputMungeComponent(MungeStep step, FormValidationHandler handler, MatchMakerSession session) {
		super(step, handler, session);
	}

	@Override
	protected JPanel buildUI() {
		JPanel content = new JPanel();
		content.setLayout(new FlowLayout());
		content.add(new JButton(new ShowAllOutputNamesAction("Show All")));
		content.add(new JButton(new HideAllOutputNamesAction("Hide All")));
		setOutputShowNames(true);
		return content;
	}
		
	@Override
	public void remove() {
	}
	
	@Override
	public JPopupMenu getPopupMenu() {
		if (Logger.getLogger(AbstractMungeComponent.class).isDebugEnabled()) {
			return super.getPopupMenu();
		}
		return null;
	}
}
