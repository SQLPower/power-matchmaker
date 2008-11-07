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


package ca.sqlpower.matchmaker.swingui;

import javax.swing.JTextField;

import org.apache.log4j.Logger;

import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.swingui.SPDataSourcePanel;

/**
 * The MMDataSourcePanel is an SPDataSourcePanel with an extra field
 * for setting the PL schema owner.
 */
public class MMDataSourcePanel extends SPDataSourcePanel {
	private static final Logger logger = Logger.getLogger(MMDataSourcePanel.class);
	
	/**
	 * The PL schema owner for this connection.
	 */
	private JTextField repositorySchemaOwnerField;

	/**
	 * Sets up the repository owner field.
	 * 
	 * @param ds The data source to edit.
	 */
	MMDataSourcePanel(SPDataSource ds) {
		super(ds);
		repositorySchemaOwnerField = new JTextField(ds.getPlSchema());
		repositorySchemaOwnerField.putClientProperty(EXTRA_FIELD_LABEL_PROP, "Repository Qualifier");
		addExtraField(repositorySchemaOwnerField);
	}
	
	@Override
	public boolean applyChanges() {
		boolean success = super.applyChanges();
		if (!success) return false;
		
		logger.debug("Setting respository owner to " + repositorySchemaOwnerField.getText());
		getDbcs().setPlSchema(repositorySchemaOwnerField.getText());
		
		return true;
	}
}
