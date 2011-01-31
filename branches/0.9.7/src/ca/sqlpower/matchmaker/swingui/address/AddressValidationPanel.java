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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.address.AddressDatabase;
import ca.sqlpower.matchmaker.address.AddressPool;
import ca.sqlpower.matchmaker.address.AddressResult;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.matchmaker.swingui.NoEditEditorPane;
import ca.sqlpower.sqlobject.SQLObjectException;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.sleepycat.je.DatabaseException;

public class AddressValidationPanel extends NoEditEditorPane {

    private static final Logger logger = Logger.getLogger(AddressValidationPanel.class);
    
    /**
     * A collection of invalid addresses
     */
    private final List<AddressResult> addressResults = new ArrayList<AddressResult>();
    
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
    
    /**
     * The number of addresses to display at a time. 
     */
    private int displayCount = 5;
    
    /**
     * The total number of addresses that can be contained in the address pool. Used
     * to decide if there are further pages the user can move to with the next button.
     */
    private long correctableAddressCount;
    
    /**
     * The number of the first address in the list. Used to decide if there is an address
     * that can be moved to with the next and previous buttons.
     */
    private long currentAddressBeingDisplayed = 0;
    
    private final List<Object> startPoint;
    private final List<Object> endPoint;
    private final int pkeyChildCount;

	private final JButton prevButton = new JButton(new AbstractAction("Prev") {
		public void actionPerformed(ActionEvent e) {
			updateDisplayedAddresses(false, startPoint, false);
			nextButton.setEnabled(true);
		}
	});

	private final JButton nextButton = new JButton(new AbstractAction("Next") {
		public void actionPerformed(ActionEvent e) {
			updateDisplayedAddresses(true, endPoint, false);
			prevButton.setEnabled(true);
		}
	});

    public AddressValidationPanel(MatchMakerSwingSession session, AddressPool pool, Project project) {
    	try {
    		pkeyChildCount = project.getSourceTableIndex().getChildCount();
    	} catch (SQLObjectException e) {
    		throw new RuntimeException("A database exception occured while trying to connect to the Berkley DB", e);
    	}
    	startPoint = new ArrayList<Object>(pkeyChildCount);
    	endPoint = new ArrayList<Object>(pkeyChildCount);
		try {
			addressDatabase = new AddressDatabase(new File(session.getContext().getAddressCorrectionDataPath()));
		} catch (DatabaseException e) {
			throw new RuntimeException("A database exception occured while trying to connect to the Berkley DB", e);
		} 
		this.pool = pool;
		try {
			correctableAddressCount = pool.findAddressCount();
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
		}

		updateDisplayedAddresses(true, startPoint, true);

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
		JPanel pageCountSelectorPanel = new JPanel();
		pageCountSelectorPanel.setLayout(new BoxLayout(pageCountSelectorPanel, BoxLayout.X_AXIS));
		pageCountSelectorPanel.add(new JLabel("# addresses per page"));
		final JTextField displayCountField = new JTextField(Integer.toString(displayCount));
		pageCountSelectorPanel.add(displayCountField);
		displayCountField.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				try {
					displayCount = Integer.parseInt(displayCountField.getText());
					updateDisplayedAddresses(true, startPoint, true);
				} catch (NumberFormatException ex) {
					displayCountField.setText(Integer.toString(displayCount));
				}
			}
			public void focusGained(FocusEvent e) {
				//do nothing
			}
		});
		validateResultPane.add(pageCountSelectorPanel);
		
		DefaultFormBuilder pageButtonBuilder = new DefaultFormBuilder(new FormLayout("pref:grow, 3dlu, pref:grow"));

		prevButton.setEnabled(false);
		pageButtonBuilder.append(prevButton);
		pageButtonBuilder.append(nextButton);
		validateResultPane.add(pageButtonBuilder.getPanel());
		
		DefaultFormBuilder pageLabelBuilder = new DefaultFormBuilder(new FormLayout("fill:pref:grow"));
		validateResultPane.add(pageLabelBuilder.getPanel());
		validateResultPane.add(addressPane);

		horizontalSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, validateResultPane,
				new JLabel("To begin address validation, please select an address from the list.",	JLabel.CENTER));
		setPanel(horizontalSplitPane);
	}

	/**
	 * Call this method to change the addresses displayed depending on the
	 * {@link #displayCount} and {@link #displayPage}.
	 */
    private void updateDisplayedAddresses(boolean forward, List<Object> queryPoint, boolean includeStartPoint) {
    	addressResults.clear();
    	try {
			pool.load(logger, false, displayCount, forward, queryPoint, includeStartPoint);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
    	addressResults.addAll(pool.getAddressResults(logger));

    	allResults.clear();
    	validResults.clear();
    	invalidResults.clear();
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
		
		if (!queryPoint.isEmpty() && !forward && startPoint.equals(queryPoint)) {
			currentAddressBeingDisplayed -= displayCount;
		} else if (!queryPoint.isEmpty() && forward && endPoint.equals(queryPoint)) {
			currentAddressBeingDisplayed += displayCount;
		}
		if (currentAddressBeingDisplayed < 0) {
			currentAddressBeingDisplayed = 0;
		}
		
		
		startPoint.clear();
		endPoint.clear();
		if (addressArray.length > 0) {
			int startArrayPoint;
			int endArrayPoint;
			if (forward) {
				startArrayPoint = 0;
				endArrayPoint = addressArray.length - 1;
			} else {
				startArrayPoint = addressArray.length - 1;
				endArrayPoint = 0;
			}
			for (int i = 0; i < pkeyChildCount; i++) {
				startPoint.add(((AddressResult) addressArray[startArrayPoint]).getKeyValues().get(i));
				endPoint.add(((AddressResult) addressArray[endArrayPoint]).getKeyValues().get(i));
			}
		}
		prevButton.setEnabled(true);
		nextButton.setEnabled(true);
		if (currentAddressBeingDisplayed == 0) {
			prevButton.setEnabled(false);
    	} 
		if (currentAddressBeingDisplayed >= correctableAddressCount - displayCount) {
    		nextButton.setEnabled(false);
    	}
		//DisplayCount == 0 means all values are being displayed
		if (displayCount == 0) {
			prevButton.setEnabled(false);
			nextButton.setEnabled(false);
		}
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
