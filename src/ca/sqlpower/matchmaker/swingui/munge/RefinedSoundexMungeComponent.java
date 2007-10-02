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

import javax.swing.JLabel;
import javax.swing.JPanel;

import ca.sqlpower.matchmaker.munge.MungeStep;

public class RefinedSoundexMungeComponent extends AbstractMungeComponent {

	public RefinedSoundexMungeComponent(MungeStep step) {
		super(step);
	}

	@Override
	protected JPanel buildUI() {
		JPanel content = new JPanel();
		content.add(new JLabel("No options available."));
		return content;
	}

}
