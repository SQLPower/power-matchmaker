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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.antlr.runtime.RecognitionException;
import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.address.Address;
import ca.sqlpower.matchmaker.address.AddressDatabase;
import ca.sqlpower.matchmaker.address.AddressResult;
import ca.sqlpower.matchmaker.address.AddressValidator;
import ca.sqlpower.matchmaker.swingui.MMSUtils;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.sleepycat.je.DatabaseException;

/**
 * This data entry panel is for validating and editing a single {@link Address}.
 * This is different from the {@link AddressValidationPanel} as it is contained in
 * the {@link AddressValidationPanel} and the other panel shows all possible addresses
 * that needs corrections while this allows editing and is only one address.
 */
public class AddressValidationEntryPanel implements DataEntryPanel {
	
	private static final Logger logger = Logger.getLogger(AddressValidationEntryPanel.class);
	
	private final JPanel panel;
	private final AddressResult addressResult;
	private final AddressDatabase addressDatabase;
    
	//XXX: Add these 2 buttons to the panel and implement the functions
//    private JButton undoButton;
//    private JButton redoButton;
    
    /**
     * This is the selected AddressLabel which is in the middle of the 
     * validation screen waiting to be corrected.
     */
    private AddressLabel selectedAddressLabel;

    /**
     * This is a temporary parent pointer for this entry panel so it can call
     * the temporary save method on it's parent which is also temporary.
     */
	private final AddressValidationPanel parent;
	
	/**
	 * This validator will be used to correct the last modified address from the user.
	 */
	private AddressValidator addressValidator;
	
	/**
	 * This builder puts together the error list below the main label. The builder
	 * is a class level variable to allow editing the error list.
	 */
    private	DefaultFormBuilder problemsBuilder;
    
	/**
	 * The list of suggestion labels
	 */
	private JList suggestionList;
	
	/**
     * The result after validation step
     */
    private List<ValidateResult> validateResult;
	
	public AddressValidationEntryPanel(AddressValidationPanel parent, AddressResult addressResult, AddressDatabase addressDatabase) {
		this.parent = parent;
		this.addressResult = addressResult;
		this.addressDatabase = addressDatabase;
		panel = new JPanel();
		buildUI();
	}
	
	private void buildUI() {
		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(
				"fill:pref:grow,4dlu,fill:pref",
		"pref,4dlu,pref,4dlu,fill:pref:grow"), panel);
		builder.setDefaultDialogBorder();
		CellConstraints cc = new CellConstraints();

		
		try {
			final Address address1;
			
			if (addressResult.getOutputAddress().isEmptyAddress()) {
				address1 = Address.parse(
					addressResult.getInputAddress().getUnparsedAddressLine1(), addressResult.getInputAddress().getMunicipality(), addressResult.getInputAddress().getProvince(),
					addressResult.getInputAddress().getPostalCode(), addressResult.getInputAddress().getCountry(), addressDatabase);
				addressResult.setOutputAddress(address1);
			} else {
				address1 = addressResult.getOutputAddress();
			}
			
			JButton saveButton = new JButton("Save");
			selectedAddressLabel = new AddressLabel(address1, null, addressDatabase, true);
			selectedAddressLabel.addPropertyChangeListener(new PropertyChangeListener() {
			
				public void propertyChange(PropertyChangeEvent evt) {
					if (evt.getPropertyName().equals("currentAddress")) {
						// update the suggestionList
						addressValidator = new AddressValidator(addressDatabase, (Address) evt.getNewValue());
						suggestionList.setModel(new JList(addressValidator.getSuggestions().toArray()).getModel());
						// update the problem details
						updateProblemDetails();
						// Auto save changes
						save();
					}
				}
			});
			selectedAddressLabel.setFont(selectedAddressLabel.getFont().deriveFont((float) (selectedAddressLabel.getFont().getSize() + 3)));
			
			JButton revertButton = new JButton("Revert");
			revertButton.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					try {
						logger.debug("Revert Address: " + addressResult.toString());
						Address address = Address.parse(
								addressResult.getInputAddress().getUnparsedAddressLine1(), addressResult.getInputAddress().getMunicipality(), addressResult.getInputAddress().getProvince(),
								addressResult.getInputAddress().getPostalCode(), addressResult.getInputAddress().getCountry(), addressDatabase);
						addressResult.setOutputAddress(selectedAddressLabel.getCurrentAddress());
						selectedAddressLabel.setCurrentAddress(address);
					} catch (RecognitionException e1) {
						e1.printStackTrace();
					} catch (DatabaseException e1) {
						throw new RuntimeException("A database exception occurred while parsing the address" + e1);
					}
				}
				
			});
			
			saveButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					save();
				}
			});
			
//			undoButton = new JButton("Undo");
			
//			redoButton = new JButton("Redo");
			
			JLabel suggestLabel = new JLabel("Suggestions:");
			suggestLabel.setFont(suggestLabel.getFont().deriveFont(Font.BOLD));
			
			this.addressValidator = new AddressValidator(addressDatabase, address1);
			problemsBuilder = new DefaultFormBuilder(new FormLayout("fill:pref:grow"));
			updateProblemDetails();
			suggestionList = new JList(addressValidator.getSuggestions().toArray());
			logger.debug("There are " + addressValidator.getSuggestions().size() + " suggestions.");
			suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			suggestionList.setCellRenderer(new AddressListCellRenderer(address1, addressDatabase, false));
			suggestionList.addMouseListener(new MouseAdapter() {
				public void mouseReleased(MouseEvent e) {
					logger.debug("Mouse Clicked on suggestion list " + ((JList)e.getSource()).getSelectedValue());
					final Address selected = (Address) ((JList) e.getSource()).getSelectedValue();
					if (selected != null) {
						//XXX:This does not update the model currently
						selectedAddressLabel.setCurrentAddress(selected);
					}
				}
			});
			JScrollPane scrollList = new JScrollPane(suggestionList);
			scrollList.setPreferredSize(new Dimension(230, 1000));

			ButtonBarBuilder bbb = new ButtonBarBuilder();
			bbb.addRelatedGap();
			bbb.addGridded(revertButton);
			bbb.addRelatedGap();
			bbb.addGridded(saveButton);
			bbb.addRelatedGap();
			builder.add(bbb.getPanel(), cc.xy(1, 1));
			builder.add(suggestLabel, cc.xy(3, 1));
			builder.add(selectedAddressLabel, cc.xy(1, 3));
			builder.add(problemsBuilder.getPanel(), cc.xy(1, 5));
			builder.add(scrollList, cc.xywh(3, 3, 1, 3));
			
		} catch (RecognitionException e1) {
			MMSUtils
					.showExceptionDialog(
							getPanel(),
							"There was an error while trying to parse this address",
							e1);
		} catch (DatabaseException e1) {
			MMSUtils
			.showExceptionDialog(
					getPanel(),
					"There was a database error while trying to parse this address",
					e1);
		}
	}
	
	public void updateProblemDetails() {
		validateResult = addressValidator.getResults();
		logger.debug("The size of the Problems is : " + validateResult.size());
		problemsBuilder.getPanel().removeAll();
		//XXX:This recreates the panel based on the same panel.
    	problemsBuilder = new DefaultFormBuilder(new FormLayout("fill:pref:grow"), problemsBuilder.getPanel());
		JLabel problemsHeading = new JLabel("Problems:");
		problemsHeading.setFont(new Font(null, Font.BOLD, 13));
		problemsBuilder.append(problemsHeading);
		for (ValidateResult vr : validateResult) {
			logger.debug("The Problem details are: " + vr);
			if (vr.getStatus() == Status.FAIL) {
				problemsBuilder.append(new JLabel("Fail: "
						+ vr.getMessage(), new ImageIcon(
						AddressValidationPanel.class
								.getResource("icons/fail.png")),
						JLabel.LEFT));
			} else if (vr.getStatus() == Status.WARN) {
				problemsBuilder.append(new JLabel("Warning: "
						+ vr.getMessage(), new ImageIcon(
						AddressValidationPanel.class
								.getResource("icons/warn.png")),
						JLabel.LEFT));
			}
		}
		problemsBuilder.getPanel().revalidate();
		problemsBuilder.getPanel().repaint();
	}
	
	public boolean applyChanges() {
		// TODO Auto-generated method stub
		logger.debug("Stub call: AddressValidationEntryPanel.applyChanges()");
		return false;
	}

	public void discardChanges() {
		// TODO Auto-generated method stub
		logger.debug("Stub call: AddressValidationEntryPanel.discardChanges()");

	}

	public JComponent getPanel() {
		return panel;
	}

	public boolean hasUnsavedChanges() {
		// TODO Auto-generated method stub
		logger.debug("Stub call: AddressValidationEntryPanel.hasUnsavedChanges()");
		return false;
	}
	  
	private void save() {
		addressResult.setOutputAddress(selectedAddressLabel.getCurrentAddress());
		logger.debug("SAVING: " + addressResult);
		parent.saveAddressResult(addressResult, validateResult.size() == 0);
	}

}
