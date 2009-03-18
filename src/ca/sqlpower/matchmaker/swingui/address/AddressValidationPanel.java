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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.SQLException;
import java.util.Collection;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.antlr.runtime.RecognitionException;
import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.address.Address;
import ca.sqlpower.matchmaker.address.AddressDatabase;
import ca.sqlpower.matchmaker.address.AddressPool;
import ca.sqlpower.matchmaker.address.AddressResult;
import ca.sqlpower.matchmaker.address.AddressValidator;
import ca.sqlpower.matchmaker.swingui.MMSUtils;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.matchmaker.swingui.NoEditEditorPane;
import ca.sqlpower.sqlobject.SQLObjectException;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
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
     * This builds the right component of the {{@link #horizontalSplitPane}
     */
    private DefaultFormBuilder builder;    
    
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
    
    /**
     * This is the selected AddressLabel which is in the middle of the 
     * validation screen waiting to be corrected.
     */
    private AddressLabel selectedAddressLabel;
    
    private JList needsValidationList;
    
    private AddressPool pool;
    
    public AddressValidationPanel(MatchMakerSwingSession session, AddressPool pool) {
		try {
			addressDatabase = new AddressDatabase(new File(session.getContext().getAddressCorrectionDataPath()));
			this.pool = pool;
			addressResults = pool.getAddressResults(logger);

			Object[] addressArray = addressResults.toArray();
			for (int i = 0; i < addressArray.length; i++) {
				allResults.add(0, addressArray[i]);
				if (((AddressResult)addressArray[i]).isValidated()) {
					validResults.add(0, addressArray[i]);
				} else {
					invalidResults.add(0, addressArray[i]);
				}
			}
			
			needsValidationList = new JList(allResults);
			needsValidationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			needsValidationList.addListSelectionListener(new AddressListCellSelectionListener());
			needsValidationList.setCellRenderer(new AddressListCellRenderer(null, addressDatabase));
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
		} catch (DatabaseException e) {
			MMSUtils.showExceptionDialog(
					        getPanel(), 
					        "A database exception occured while trying to load the invalid addresses", 
					        e);
		} 
	}

	@Override
	public JSplitPane getPanel() {
		return (JSplitPane) super.getPanel();
	}

	class AddressListCellSelectionListener implements ListSelectionListener {

		public void valueChanged(ListSelectionEvent e) {
			
			//remember user's choice of the divider's location
			horizontalSplitPane.setDividerLocation(horizontalSplitPane.getDividerLocation());			
						
			final AddressResult selected = (AddressResult) ((JList)e.getSource()).getSelectedValue();
			
			if (selected != null) {
				
				builder = new DefaultFormBuilder(new FormLayout(
						"fill:pref:grow,4dlu,fill:pref",
				"pref,4dlu,pref,4dlu,fill:pref:grow"));
				builder.setDefaultDialogBorder();
				CellConstraints cc = new CellConstraints();

				
				try {
					Address address1;
					
					if (selected.getOutputAddress().isEmptyAddress()) {
						address1 = Address.parse(
							selected.getAddressLine1(), selected
							.getMunicipality(), selected.getProvince(),
							selected.getPostalCode(), selected.getCountry(), addressDatabase);
					} else {
						address1 = selected.getOutputAddress();
					}
					
					selectedAddressLabel = new AddressLabel(address1, null, false, null, addressDatabase);
					selectedAddressLabel.setFont(new Font("Times New Roman", Font.PLAIN, 16));
					JButton revertButton = new JButton("Revert");
					
					revertButton.addActionListener(new ActionListener() {
						
						public void actionPerformed(ActionEvent e) {
							selectedAddressLabel.setAddress(selectedAddressLabel.getRevertToAddress());
							AddressValidator addressValidator = new AddressValidator(addressDatabase,selectedAddressLabel.getAddress());
							JList suggestionList = new JList(addressValidator.getSuggestions().toArray());
							selectedAddressLabel.setSuggestionList(suggestionList);
							selectedAddressLabel.updateProblemDetails(addressValidator);
						}
						
					});
					
					JButton saveButton = new JButton("Save");
					saveButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							selected.setOutputAddress(selectedAddressLabel.getAddress());
							pool.addAddress(selected, logger);
							try {
								needsValidationList.repaint();
								pool.store(logger, false, false);
							} catch (SQLException ex) {
								throw new RuntimeException("An error occured while trying to save this address, ex");
							} catch (SQLObjectException ex) {
								throw new RuntimeException("An error occured while trying to save this address, ex");
							}
						}
					});
					
					JLabel suggestLabel = new JLabel("Suggestions:");
					suggestLabel.setFont(new Font(null, Font.BOLD, 13));
					
					JScrollPane scrollList = new JScrollPane(selectedAddressLabel.getSuggestionList());
					scrollList.setPreferredSize(new Dimension(200, 500));
					
					ButtonBarBuilder bbb = new ButtonBarBuilder();
					bbb.addRelatedGap();
					bbb.addGridded(revertButton);
					bbb.addRelatedGap();
					bbb.addGridded(saveButton);
					bbb.addRelatedGap();
					builder.add(bbb.getPanel(), cc.xy(1, 1));
					builder.add(suggestLabel, cc.xy(3, 1));
					builder.add(selectedAddressLabel, cc.xy(1, 3));
					builder.add(selectedAddressLabel.getProblemBuilder().getPanel(), cc.xy(1, 5));
					builder.add(scrollList, cc.xywh(3, 3, 1, 3));
					
					horizontalSplitPane.setRightComponent(builder.getPanel());

				} catch (RecognitionException e1) {
					MMSUtils
							.showExceptionDialog(
									getPanel(),
									"There was an error while trying to parse this address",
									e1);
				}
			} else {
				horizontalSplitPane.setRightComponent(
						new JLabel("To begin address validation, please select an address from the list.",	JLabel.CENTER));
			}
			
		}
		
	}

}
