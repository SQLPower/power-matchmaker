/*
 * Copyright (c) 2009, SQL Power Group Inc.
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

package ca.sqlpower.matchmaker.swingui.address;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.SQLException;
import java.util.Collection;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.address.AddressDatabase;
import ca.sqlpower.matchmaker.address.AddressPool;
import ca.sqlpower.matchmaker.address.AddressResult;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.matchmaker.swingui.NoEditEditorPane;
import ca.sqlpower.sqlobject.SQLObjectException;

import com.sleepycat.je.DatabaseException;

public class AddressValidationPanel extends NoEditEditorPane {

    private static final Logger logger = Logger.getLogger(AddressValidationPanel.class);
    
    /**
     * A collection of invalid addresses
     */
    private Collection<AddressResult> addressResults;
    
    private AddressDatabase addressDatabase;
    
    /**
     * The horizontal split pane in the validation screen
     */
    private JSplitPane horizontalSplitPane; 
    
    /**
     * This is the left component of the {{@link #horizontalSplitPane}
     */
    private JPanel validateResultPane;

    /**
     * This is the comboBox with 3 addresses display options :
     * Show all, Show Invalid only and Show Valid only
     */
    private JComboBox displayComboBox;
    
    /**
     * This is the list model which stores all the addresses
     */
    private DefaultListModel allResults = new DefaultListModel();
    
    /**
     * This is the list model which stores only the valid addresses
     */
    private DefaultListModel validResults = new DefaultListModel();
    
    /**
     * This is the list model which stores only the invalid addresses
     */
    private DefaultListModel invalidResults = new DefaultListModel();
    
    private JList needsValidationList;
    
    private AddressPool pool;
    
    public AddressValidationPanel(MatchMakerSwingSession session, AddressPool pool) {
		try {
			addressDatabase = new AddressDatabase(new File(session.getContext().getAddressCorrectionDataPath()));
		} catch (DatabaseException e) {
			throw new RuntimeException("A database exception occured while trying to connect to the Berkley DB", e);
		} 
		this.pool = pool;
		addressResults = pool.getAddressResults(logger);

		Object[] addressArray = addressResults.toArray();
		for (int i = 0; i < addressArray.length; i++) {
			AddressResult address =(AddressResult)addressArray[i];
			allResults.add(0, address);
//			if (!address.getOutputAddress().isEmptyAddress()) {
				if (address.isValid()) {
					validResults.add(0, address);
				} else {
					invalidResults.add(0, address);
				}
//			} else {
//				invalidResults.add(0, address);
//			}
		}

		needsValidationList = new JList(allResults);
		needsValidationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		needsValidationList.addListSelectionListener(new AddressListCellSelectionListener());
		needsValidationList.setCellRenderer(new AddressListCellRenderer(null, true));
		JScrollPane addressPane = new JScrollPane(needsValidationList);
		addressPane.setPreferredSize(new Dimension(250, 1000));

		validateResultPane = new JPanel();
		validateResultPane.setLayout(new BoxLayout(validateResultPane, BoxLayout.Y_AXIS));
		String[] comboBoxOptions = { "Show All", "Show Invalid Results Only", "Show Valid Result Only" }; 
		displayComboBox = new JComboBox(comboBoxOptions);
		displayComboBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox)e.getSource();
				int index = cb.getSelectedIndex();
				if (index == 0) {
					needsValidationList.setModel(allResults);
				} else if (index == 1) {
					needsValidationList.setModel(invalidResults);
				} else {
					needsValidationList.setModel(validResults);
				}
			}

		});			
		validateResultPane.add(displayComboBox);
		validateResultPane.add(addressPane);

		horizontalSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, validateResultPane,
				new JLabel("To begin address validation, please select an address from the list.",	JLabel.CENTER));
		setPanel(horizontalSplitPane);
	}

	@Override
	public JSplitPane getPanel() {
		return (JSplitPane) super.getPanel();
	}

	class AddressListCellSelectionListener implements ListSelectionListener {

		public void valueChanged(ListSelectionEvent e) {
			logger.debug("Value changed in the panel's list to " + ((JList)e.getSource()).getSelectedValue());
			
			//remember user's choice of the divider's location
			horizontalSplitPane.setDividerLocation(horizontalSplitPane.getDividerLocation());			
						
			final AddressResult selected = (AddressResult) ((JList)e.getSource()).getSelectedValue();
			
			if (selected != null) {
				AddressValidationEntryPanel editor = new AddressValidationEntryPanel(AddressValidationPanel.this, selected, addressDatabase);
				horizontalSplitPane.setRightComponent(editor.getPanel());
				
			} else {
				horizontalSplitPane.setRightComponent(
						new JLabel("To begin address validation, please select an address from the list.",	JLabel.CENTER));
			}
			
		}
		
	}
	
	/**
	 * This is a temporary method for saving an address result from the {@link AddressValidationEntryPanel}
	 * until we have list models properly listening to the pool.
	 */
	void saveAddressResult(AddressResult addressResult, boolean containsProblems) {
		pool.addAddress(addressResult, logger);
		try {
			if(containsProblems) {
				if(invalidResults.contains(addressResult)) {
					invalidResults.removeElement(addressResult);
					validResults.addElement(addressResult);
				}
			} else {
				if(validResults.contains(addressResult)) {
					validResults.removeElement(addressResult);
					invalidResults.addElement(addressResult);
				}
			}
			needsValidationList.repaint();
			pool.store(logger, false, false);
		} catch (SQLException ex) {
			throw new RuntimeException("An error occured while trying to save this address, ex");
		} catch (SQLObjectException ex) {
			throw new RuntimeException("An error occured while trying to save this address, ex");
		}
		
	}
	
}
