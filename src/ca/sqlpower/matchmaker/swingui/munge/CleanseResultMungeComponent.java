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

import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.validation.swingui.FormValidationHandler;

/**
 * This is the component for a cleanse result munge step.
 */
public class CleanseResultMungeComponent extends AbstractMungeComponent {
	
	public CleanseResultMungeComponent(MungeStep ms, FormValidationHandler handler, MatchMakerSession session) {
		super(ms, handler, session);
		content.setLayout(new FlowLayout());
		content.add(new JButton(new HideShowAllLabelsAction("Show All", true, false, true)));
		content.add(new JButton(new HideShowAllLabelsAction("Hide All", true, false, false)));
		setInputShowNames(true);
	}

	@Override
	protected JPanel buildUI() {
		return new JPanel();
	}
	
	@Override
	public JPopupMenu getPopupMenu() {
		return null;
	}
	
	@Override
	public void remove() {
	}
	
}
