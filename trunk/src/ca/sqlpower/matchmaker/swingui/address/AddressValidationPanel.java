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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.Collection;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.antlr.runtime.RecognitionException;
import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.address.Address;
import ca.sqlpower.matchmaker.address.AddressDatabase;
import ca.sqlpower.matchmaker.address.AddressResult;
import ca.sqlpower.matchmaker.address.AddressValidator;
import ca.sqlpower.matchmaker.swingui.MMSUtils;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.matchmaker.swingui.NoEditEditorPane;
import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;

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
     * This is the right component of the {{@link #horizontalSplitPane}
     */
    private JPanel editPane;
    
    /**
     * This is the left part of the {@link #editPane}
     */
    private JPanel leftPane;
    
    /**
     * This is the right part of the {@link #editPane}
     */
    private JPanel rightPane;
    
    /**
     * The result after validation step
     */
    private List<ValidateResult> validateResult;
    
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
    
    public AddressValidationPanel(MatchMakerSwingSession session, Collection<AddressResult> results) {
		try {
			addressDatabase = new AddressDatabase(new File(session.getContext().getAddressCorrectionDataPath()));
			addressResults = results;

			Object[] addressArray = addressResults.toArray();
			int k = 0;
			int j = 0;
			for (int i = 0; i < addressArray.length; i++) {
				allResults.add(0, addressArray[i]);
				if (((AddressResult)addressArray[i]).isValidated()) {
					validResults.add(j, addressArray[i]);
				} else {
					invalidResults.add(k, addressArray[i]);
				}
			}
			
			final JList needsValidationList = new JList(allResults);
			needsValidationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			needsValidationList.addListSelectionListener(new AddressListCellSelectionListener());
			needsValidationList.setCellRenderer(new AddressListCellRenderer(null));
			JScrollPane addressPane = new JScrollPane(needsValidationList);
			addressPane.setPreferredSize(new Dimension(250, 1000));
			
			validateResultPane = new JPanel();
			validateResultPane.setLayout(new BoxLayout(validateResultPane, BoxLayout.Y_AXIS));
			String[] comboBoxOptions = { "show all", "show invalid result only", "Show valid result only" }; 
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

	class AddressListCellRenderer implements ListCellRenderer {

	    /**
	     * The address to compare against when rendering the label. If null,
	     * no comparison will be made.
	     */
	    private final Address comparisonAddress;
	    
        public AddressListCellRenderer(Address comparisonAddress) {
            this.comparisonAddress = comparisonAddress;
	    }
        
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			AddressLabel address;
			if (value instanceof Address) {
				address = new AddressLabel((Address) value, comparisonAddress, isSelected, list);
				address.setOpaque(true);
				return address;
			} else {
				// TODO improve this (have address and addressresult implement a common interface)
				AddressResult selected = (AddressResult) value;
				Address address1;
				try {
					address1 = Address.parse(
							selected.getAddressLine1(), selected
									.getMunicipality(), selected.getProvince(),
							selected.getPostalCode(), selected.getCountry(), addressDatabase);
				} catch (RecognitionException e) {
					throw new RuntimeException(e);
				}
				address = new AddressLabel(address1, comparisonAddress, isSelected, list);
				return address;
			}
		}

	}
	
	class AddressListCellSelectionListener implements ListSelectionListener {

		public void valueChanged(ListSelectionEvent e) {
			leftPane = new JPanel();
			leftPane.setLayout(new BoxLayout(leftPane, BoxLayout.Y_AXIS));
			JButton button = new JButton("Revert");
			button.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					selectedAddressLabel.setAddress(selectedAddressLabel.getRevertToAddress());
				}
				
			});
			leftPane.add(button);
			leftPane.add(Box.createVerticalStrut(20));
			
			rightPane = new JPanel();
			rightPane.setLayout(new BoxLayout(rightPane, BoxLayout.Y_AXIS));
			JLabel suggestLabel = new JLabel("Suggestions:");
			suggestLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
			suggestLabel.setFont(new Font(null, Font.BOLD, 13));
			rightPane.add(Box.createRigidArea(new Dimension(0,10)));
			rightPane.add(suggestLabel);
			rightPane.add(Box.createRigidArea(new Dimension(0,10)));
			
			editPane = new JPanel();
			editPane.setLayout(new BoxLayout(editPane, BoxLayout.X_AXIS));
			editPane.add(leftPane);
			editPane.add(Box.createHorizontalStrut(10));
			editPane.add(rightPane);
			editPane.add(Box.createHorizontalStrut(10));
			
			//remember user's choice of the divider's location
			horizontalSplitPane.setDividerLocation(horizontalSplitPane.getDividerLocation());			
						
			final AddressResult selected = (AddressResult) ((JList)e.getSource()).getSelectedValue();
			horizontalSplitPane.setRightComponent(editPane);	
			
			if (selected != null) {
				try {
					Address address1 = Address.parse(
							selected.getAddressLine1(), selected
									.getMunicipality(), selected.getProvince(),
							selected.getPostalCode(), selected.getCountry(), addressDatabase);
					AddressValidator validator = new AddressValidator(addressDatabase, address1);
				    validateResult = validator.getResults();

					selectedAddressLabel = new AddressLabel(address1, false, null);
					selectedAddressLabel.setFont(new Font("Times New Roman", Font.PLAIN, 16));
					leftPane.add(selectedAddressLabel);
					leftPane.add(Box.createVerticalStrut(10));
					JLabel problems = new JLabel("Problems:");
					leftPane.add(problems);
					problems.setFont(new Font(null, Font.BOLD, 13));
					for (ValidateResult vr : validateResult) {
						logger.debug("THIS IS NOT EMPTY!!!!!!!!!!!!!!!!!");
						if (vr.getStatus() == Status.FAIL) {
							leftPane.add(new JLabel("Fail: " + vr.getMessage(),
									new ImageIcon(AddressValidationPanel.class
											.getResource("icons/fail.png")),
									JLabel.LEFT));
						} else if (vr.getStatus() == Status.WARN) {
							leftPane.add(new JLabel("Warning: "
									+ vr.getMessage(), new ImageIcon(
									AddressValidationPanel.class
											.getResource("icons/warn.png")),
									JLabel.LEFT));
						}
					}
					leftPane.add(Box.createVerticalStrut(200));

					JList suggestionList = new JList(validator.getSuggestions().toArray());
					suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					suggestionList.setCellRenderer(new AddressListCellRenderer(address1));
					//suggestionList.addListSelectionListener(new SuggestionListCellSelectionListener());
					suggestionList.addMouseListener(new MouseListener() {

						public void mouseClicked(MouseEvent e) {
							final Address selected = (Address) ((JList)e.getSource()).getSelectedValue();
							selectedAddressLabel.setAddress(selected);
						}

						public void mouseEntered(MouseEvent e) {
							// Do nothing							
						}

						public void mouseExited(MouseEvent e) {
							// Do nothing							
						}

						public void mousePressed(MouseEvent e) {
							// Do nothing
						}

						public void mouseReleased(MouseEvent e) {
							// Do nothing
						}
						
					});
					JScrollPane scrollList = new JScrollPane(suggestionList);
					scrollList.setPreferredSize(new Dimension(300, 50));
					rightPane.add(scrollList);
					rightPane.add(Box.createVerticalStrut(10));
					rightPane.setMinimumSize(new Dimension(220, 500));
					rightPane.setMaximumSize(new Dimension(250, 600));

					for (Component comp : leftPane.getComponents()) {
						if (comp instanceof JComponent) {
							((JComponent) comp)
									.setAlignmentX(Component.LEFT_ALIGNMENT);
						}
					}
					for (Component comp : rightPane.getComponents()) {
						if (comp instanceof JComponent) {
							((JComponent) comp)
									.setAlignmentX(Component.LEFT_ALIGNMENT);
						}
					}
					logger.debug("THe size of right pane is "
							+ rightPane.getBounds().height);
					logger.debug("The size of left pane is "
							+ leftPane.getBounds().height);
					logger.debug(horizontalSplitPane.getBounds().height);
					logger.debug(horizontalSplitPane.getLeftComponent()
							.getBounds().height);

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
