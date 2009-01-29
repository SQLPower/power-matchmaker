/*
 * Copyright (c) 2009, SQL Power Group Inc.
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

import javax.swing.JPanel;

import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.validation.swingui.FormValidationHandler;

public class AddressCorrectionMungeComponent extends AbstractMungeComponent {

	public AddressCorrectionMungeComponent(MungeStep step,
			FormValidationHandler handler, MatchMakerSession s) {
		super(step, handler, s);
		setOutputShowNames(true);
		setInputShowNames(true);
	}

	@Override
	protected JPanel buildUI() {
		return new JPanel();
	}

}
