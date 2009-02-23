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

package ca.sqlpower.matchmaker.swingui.address;

import java.awt.Component;
import java.awt.Dimension;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Vector;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.address.AddressPool;
import ca.sqlpower.matchmaker.address.AddressResult;
import ca.sqlpower.matchmaker.swingui.MMSUtils;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.matchmaker.swingui.NoEditEditorPane;
import ca.sqlpower.sqlobject.SQLObjectException;

public class AddressValidationPanel extends NoEditEditorPane {

    private static final Logger logger = Logger.getLogger(AddressValidationPanel.class);
    
    private final MatchMakerSwingSession session;

    private final Project project;
    
    private Collection<AddressResult> addresses;
    
    private Vector<AddressResult> addressDetails = new Vector<AddressResult>();
    
    public AddressValidationPanel(MatchMakerSwingSession session, Project project) {
        this.session = session;
        this.project = project;
		AddressPool pool = new AddressPool(project);
		try {
			pool.load(logger);
			addresses = pool.getAddressResults(logger);
			for (AddressResult result : addresses) {
				addressDetails.add(result);
			}

			JList needsValidationList = new JList(addressDetails);
			needsValidationList.setCellRenderer(new IconCellRenderer());
			JScrollPane addressPane = new JScrollPane(needsValidationList);
			addressPane.setPreferredSize(new Dimension(250, 50));
	       	setPanel(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, addressPane,
					new JLabel("To begin address validation, please select an address from the list.",	JLabel.CENTER)));
		} catch (SQLException e) {
			MMSUtils.showExceptionDialog(
							getPanel(),
							"A SQL Exception occured while trying to load the invalid addresses",
							e);
		} catch (SQLObjectException e) {
			MMSUtils.showExceptionDialog(
							getPanel(),
							"An error occured while trying to load the invalid addresses",
							e);
		}
	}

	@Override
	public JSplitPane getPanel() {
		return (JSplitPane) super.getPanel();
	}

	class IconCellRenderer extends DefaultListCellRenderer {

		final ImageIcon canadaIcon = new ImageIcon(AddressValidationPanel.class.getResource("countryIcons/canada.png"));
		final ImageIcon usaIcon = new ImageIcon(AddressValidationPanel.class.getResource("countryIcons/usa.png"));
		
		
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {

			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			
			AddressResult address = (AddressResult)value;
			setText(address.htmlToString());
			if (address.getCountry().equals("Canada")) {
				setIcon(canadaIcon);
			} else if (address.getCountry().equals("USA")) {
				setIcon(usaIcon);
			} else {
				//not support these kind of countries yet
			}
			return this;
		}
	}

}
