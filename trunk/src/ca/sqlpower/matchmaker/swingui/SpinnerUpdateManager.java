/*
 * Copyright (c) 2010, SQL Power Group Inc.
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

package ca.sqlpower.matchmaker.swingui;

import java.beans.PropertyChangeEvent;

import javax.swing.JButton;
import javax.swing.JSpinner;

import ca.sqlpower.object.SPObject;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.validation.swingui.FormValidationHandler;

public class SpinnerUpdateManager extends AbstractUIUpdateManager {

	JSpinner spinner;
	
	public SpinnerUpdateManager(JSpinner spinner, SPObject spo,
			String propertyName, FormValidationHandler handler,
			DataEntryPanel dep, JButton refreshButton) {
		super(spinner, spo, propertyName, handler, dep, refreshButton);
		
		this.spinner = spinner;
	}
	
	protected boolean updateUI(PropertyChangeEvent evt) {
		if (spinner.getValue().equals(evt.getOldValue()) ||
				spinner.getValue().equals(evt.getNewValue())) {
			spinner.setValue(evt.getNewValue());
			return true;
		}
		return false;
	}

}
