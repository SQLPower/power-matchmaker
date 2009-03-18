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

import static ca.sqlpower.matchmaker.address.AddressValidator.different;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.antlr.runtime.RecognitionException;
import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.address.Address;
import ca.sqlpower.matchmaker.address.AddressDatabase;
import ca.sqlpower.matchmaker.address.AddressInterface;
import ca.sqlpower.matchmaker.address.AddressResult;
import ca.sqlpower.matchmaker.address.AddressValidator;
import ca.sqlpower.matchmaker.swingui.MMSUtils;
import ca.sqlpower.swingui.ColourScheme;
import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class AddressLabel extends JComponent {
	
	private static final Logger logger = Logger.getLogger(AddressLabel.class);

	private AddressInterface currentAddress;
	private AddressInterface revertToAddress;
	
	private static final int BORDER_SPACE = 12;
	/**
	 * If non-null, fields in {@link #currentAddress} that differ from fields in this
	 * address will be rendered in a different colour.
	 */
	private AddressInterface comparisonAddress;
	
	/**
	 * if list is null, then this AddressLabel is not editable.
	 * else, otherwise.
	 */
	private JList list;
	private boolean isSelected;
	private AddressDatabase addressDatabase;
	private JList suggestionList;
	 
    /**
     * The result after validation step
     */
    private List<ValidateResult> validateResult;
	
	/**
	 * The colour "differing" fields will be rendered in. Non-differing fields
	 * will be rendered in this component's foreground colour.
	 * 
	 * @see #setForeground(Color)
	 */
	private Color comparisonColour = ColourScheme.BREWER_SET19.get(1);
	
	private Rectangle2D addressLine1Hotspot = new Rectangle();
	private Rectangle2D municipalityHotspot = new Rectangle();
	private Rectangle2D provinceHotsopt = new Rectangle();
	private Rectangle2D postalCodeHotspot = new Rectangle();
	
	private JTextField addressTextField;
	private JTextField municipalityTextField;
	private JTextField provinceTextField;
	private JTextField postalCodeTextField;

	private ImageIcon checkIcon = new ImageIcon(getClass().getResource("icons/check.png"));
    private Color missingFieldColour = ColourScheme.BREWER_SET19.get(0);
    
    private AddressValidator addressValidator;
    
    private	DefaultFormBuilder problemsBuilder;
    
    public AddressLabel(AddressInterface address, boolean isSelected, JList list, AddressDatabase addressDatabase) {
        this(address, null, isSelected, list, addressDatabase);
    }
    
    public AddressLabel(AddressInterface address, AddressInterface comparisonAddress, 
    					boolean isSelected, JList list, final AddressDatabase addressDatabase) {
    	this.currentAddress = this.revertToAddress = address;
        this.comparisonAddress = comparisonAddress;
		this.isSelected = isSelected;
		this.list = list;
		this.addressDatabase = addressDatabase;
		// Generate the related suggestionList for the middle bigger addressLabel only
		if (currentAddress instanceof Address  && list == null) {
			this.addressValidator = new AddressValidator(addressDatabase, (Address) currentAddress);
			problemsBuilder = new DefaultFormBuilder(new FormLayout("fill:pref:grow"));
			updateProblemDetails(addressValidator);
			suggestionList = new JList(addressValidator.getSuggestions().toArray());
			suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			suggestionList.setCellRenderer(new AddressListCellRenderer(currentAddress, addressDatabase));
			suggestionList.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					logger.debug("Mouse Clicked on suggestion list " + ((JList)e.getSource()).getSelectedValue());
					final Address selected = (Address) ((JList) e.getSource()).getSelectedValue();
					if (selected != null) {
						setAddress(selected);
						addressValidator = new AddressValidator(addressDatabase, (Address) currentAddress);
						suggestionList.setModel(new JList(addressValidator.getSuggestions().toArray()).getModel());
						updateTextFields();
						updateProblemDetails(addressValidator);
					}
				}
			});
		}
		this.setOpaque(true);
		
		addressTextField = new JTextField(currentAddress.getAddress());
		add(addressTextField);		
		municipalityTextField = new JTextField(currentAddress.getMunicipality());
		add(municipalityTextField);		
		provinceTextField = new JTextField(currentAddress.getProvince());
		add(provinceTextField);		
		postalCodeTextField = new JTextField(currentAddress.getPostalCode());
		add(postalCodeTextField);
		
		setFont(Font.decode("plain 12"));
		FontMetrics fm = getFontMetrics(getFont());
		if (list != null) {
			setPreferredSize(new Dimension(fm.charWidth('m') * 40, fm.getHeight() * 3));
		} else {
			setPreferredSize(new Dimension(fm.charWidth('m') * 35 , fm.getHeight() * 5));
			setMaximumSize(new Dimension(fm.charWidth('m') * 42, fm.getHeight() * 10));
		}
		EmptyBorder emptyBorder = new EmptyBorder(3,4,3,4);
		CompoundBorder border = AddressLabelBorderFactory.generateAddressLabelBorder(Color.LIGHT_GRAY, 2, 5, emptyBorder);
		setBorder(border);
		
		setTextFieldsInvisible(addressTextField, municipalityTextField, provinceTextField, postalCodeTextField);
		// Add a MouseListener for the bigger selected label in the center
		// of the Validation screen.
		if (list == null) {
			addMouseListener(new MouseAdapter() {
				
				public void mouseClicked(MouseEvent e) {
					if (isClickingRightArea(e, addressLine1Hotspot)) {
						addressTextField.setText(currentAddress.getAddress());
						setTextFieldsInvisible(addressTextField, municipalityTextField, provinceTextField, postalCodeTextField);
						addressTextField.setVisible(true);
						addressTextField.requestFocus();
					} else if (isClickingRightArea(e, municipalityHotspot)) {
						municipalityTextField.setText(currentAddress.getMunicipality());
						setTextFieldsInvisible(addressTextField, municipalityTextField, provinceTextField, postalCodeTextField);
						municipalityTextField.setVisible(true);
						municipalityTextField.requestFocus();
					} else if (isClickingRightArea(e, provinceHotsopt)) {
						provinceTextField.setText(currentAddress.getProvince());
						setTextFieldsInvisible(addressTextField, municipalityTextField, provinceTextField, postalCodeTextField);
						provinceTextField.setVisible(true);
						provinceTextField.requestFocus();
					} else if (isClickingRightArea(e, postalCodeHotspot)) {
						postalCodeTextField.setText(currentAddress.getPostalCode());
						setTextFieldsInvisible(addressTextField, municipalityTextField, provinceTextField, postalCodeTextField);
						postalCodeTextField.setVisible(true);
						postalCodeTextField.requestFocus();
					} else {
						setTextFieldsInvisible(addressTextField, municipalityTextField, provinceTextField, postalCodeTextField);
					}
					//TODO Figure out where to remove these listeners
					addressTextField.addKeyListener(new AddressKeyAdapter());
					addressTextField.addFocusListener(new TextFieldFocusAdapter());
					municipalityTextField.addKeyListener(new AddressKeyAdapter());
					municipalityTextField.addFocusListener(new TextFieldFocusAdapter());
					provinceTextField.addKeyListener(new AddressKeyAdapter());
					provinceTextField.addFocusListener(new TextFieldFocusAdapter());
					postalCodeTextField.addKeyListener(new AddressKeyAdapter());
					postalCodeTextField.addFocusListener(new TextFieldFocusAdapter());
				}
				
			});
		}

	}
	
	public AddressInterface getRevertToAddress() {
		return revertToAddress;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		final Graphics2D g2 = (Graphics2D) g;
		g2.setColor(getBackground());
		g2.fillRect(0, 0, getWidth(), getHeight());
		// TODO g2.translate(border.getLeft(), border.getTop())
		FontMetrics fm = getFontMetrics(getFont());
		int y = fm.getHeight()+fm.stringWidth(" ");
		int x = 4+fm.stringWidth("  ");
		if (list != null) {
			if (isSelected) {
				g2.setColor(list.getSelectionBackground());
				g2.fillRect(0, 0, getWidth(), getHeight());
			} else {
				g2.setColor(list.getBackground());
				g2.fillRect(0, 0, getWidth(), getHeight());
			}
		}
		// set the check icon for validated addressResult labels
		if (currentAddress instanceof AddressResult) {
			if (((AddressResult)currentAddress).isValidated()) {
				checkIcon.paintIcon(this, g2, x, y);
				x += checkIcon.getIconWidth() + 4;
				repaint();
			}
		}
		if (!isFieldMissing(currentAddress.getAddress())) {
		    if (comparisonAddress != null && different(currentAddress.getAddress(), comparisonAddress.getAddress())) {
		        g2.setColor(comparisonColour);
		    } else {
                g2.setColor(getForeground());
		    }
			g2.drawString(currentAddress.getAddress(), x, y);
			addressLine1Hotspot.setRect(x, y - fm.getAscent(), fm.stringWidth(currentAddress.getAddress()), fm.getHeight());
		} else {
			g2.setColor(missingFieldColour);
			g2.drawString("Street Address Missing", x, y);
			addressLine1Hotspot.setRect(x, y - fm.getAscent(), fm.stringWidth("Street Address Missing"), fm.getHeight());
		}
		setTextBounds(addressTextField, addressLine1Hotspot);
		y += fm.getHeight();
		if (!isFieldMissing(currentAddress.getMunicipality())) {
		    if (comparisonAddress != null && different(currentAddress.getMunicipality(), comparisonAddress.getMunicipality())) {
                g2.setColor(comparisonColour);
		    } else {
		        g2.setColor(getForeground());
		    }
			g2.drawString(currentAddress.getMunicipality(), x, y);
			municipalityHotspot.setRect(x, y - fm.getAscent(), fm.stringWidth(currentAddress.getMunicipality()), fm.getHeight());
			x += fm.stringWidth(currentAddress.getMunicipality() + " ");
		} else {
			g2.setColor(missingFieldColour);
			g2.drawString("Municipality Missing", x, y);
			municipalityHotspot.setRect(x, y - fm.getAscent(), fm.stringWidth("Municipality Missing"), fm.getHeight());
			x += fm.stringWidth("Municipality Missing" + " ");
		}
		setTextBounds(municipalityTextField, municipalityHotspot);
		if (!isFieldMissing(currentAddress.getProvince())) {
            if (comparisonAddress != null && different(currentAddress.getProvince(), comparisonAddress.getProvince())) {
                g2.setColor(comparisonColour);
            } else {
                g2.setColor(getForeground());
            }
			g2.drawString(currentAddress.getProvince(), x, y);
			provinceHotsopt.setRect(x, y - fm.getAscent(), fm.stringWidth(currentAddress.getProvince()), fm.getHeight());
			x += fm.stringWidth(currentAddress.getProvince() + " ");
		} else {
			g2.setColor(missingFieldColour);
			g2.drawString("Province Missing", x, y);
			provinceHotsopt.setRect(x, y - fm.getAscent(), fm.stringWidth("Province Missing"), fm.getHeight());
			x += fm.stringWidth("Province Missing" + " ");
		}
		setTextBounds(provinceTextField, provinceHotsopt);
		if (!isFieldMissing(currentAddress.getPostalCode())) {
            if (comparisonAddress != null && different(currentAddress.getPostalCode(), comparisonAddress.getPostalCode())) {
                g2.setColor(comparisonColour);
            } else {
                g2.setColor(getForeground());
            }
			g2.drawString(currentAddress.getPostalCode(), x, y);
			postalCodeHotspot.setRect(x, y - fm.getAscent(), fm.stringWidth(currentAddress.getPostalCode()), fm.getHeight());
			x += fm.stringWidth(currentAddress.getPostalCode() + " ");
		} else {
			g2.setColor(missingFieldColour);
			g2.drawString("PostalCode Missing", x, y);
			postalCodeHotspot.setRect(x, y - fm.getAscent(), fm.stringWidth("PostalCode Missing"), fm.getHeight());
			x += fm.stringWidth("PostalCode Missing");
		}
		setTextBounds(postalCodeTextField, postalCodeHotspot);
	}
	
	public void setAddress(AddressInterface address) {
		this.currentAddress = address;
		repaint();
	}
	
	public Address getAddress() {
		try {
			return (Address)currentAddress;
		} catch (ClassCastException e) {
			MMSUtils.showExceptionDialog(this.getParent(), "Current address is AddressResult type, expecting Address Type ", e);
		}
		return null;
	}
	
	public JList getSuggestionList() {
		return suggestionList;
	}
	
	public void setSuggestionList(JList suggestionList) {
		this.suggestionList.setModel(suggestionList.getModel());
		repaint();
	}
	
	public DefaultFormBuilder getProblemBuilder() {
		return problemsBuilder;
	}
	
	private boolean isFieldMissing(String str) {
		if (str == null) {
			return true;
		}
		return str.trim().equals("null") || str.trim().equals("");
	}
	
	private boolean isClickingRightArea(MouseEvent e, Rectangle2D rec) {
		logger.debug(e.getPoint());
		logger.debug((rec.getX() + BORDER_SPACE) + "  " + rec.getY());
		logger.debug(rec.getWidth() + "  " + rec.getHeight());
		if (e.getX() >= rec.getX() && e.getX() <= rec.getX() + rec.getWidth()) {
			if (e.getY() >= rec.getY() && e.getY() <= rec.getY() + rec.getHeight()) {
				return true;
			}
		}
		return false;
	}
	
	private void setTextBounds(JTextField textField, Rectangle2D rec) {
		textField.setBounds((int)rec.getX(), (int)rec.getY(), (int)rec.getWidth() + 3, textField.getPreferredSize().height);
	}
	
	private void setTextFieldsInvisible(JTextField ... textField) {
		for (JTextField tf: textField) {
			tf.setVisible(false);
		}
	}
	
	/**
	 * This method should be called after user edit the addressLabel 
	 * manually to parse the address again
	 * @return The new parsed user edited address
	 */
	private Address getChangedAddress() {
		try {
			return Address.parse(addressTextField.getText().toUpperCase(), municipalityTextField.getText().toUpperCase(),
										   provinceTextField.getText().toUpperCase(), postalCodeTextField.getText().toUpperCase(),
										   "CANADA", addressDatabase);
		} catch (RecognitionException e) {
			MMSUtils.showExceptionDialog(
					getParent(),
					"There was an error while trying to parse this address",
					e);
		}
		return new Address();
	}
	
	
	class TextFieldFocusAdapter extends FocusAdapter {

		public void focusLost(FocusEvent e) {
			((JTextField)e.getSource()).setVisible(false);
			AddressInterface newCurrentAddress = getChangedAddress();
			logger.debug("Current Address = " + currentAddress.getPostalCode() + "\nHashcode: " + currentAddress.hashCode());
			logger.debug("New Current Address = " + newCurrentAddress.getPostalCode() + "\nHashcode: " + newCurrentAddress.hashCode());
			if(!(currentAddress.equals(newCurrentAddress))) {
					currentAddress = newCurrentAddress;
					addressValidator = new AddressValidator(addressDatabase, (Address)currentAddress);
					suggestionList.setModel(new JList(addressValidator.getSuggestions().toArray()).getModel());
					//update the problem details
					updateProblemDetails(addressValidator);
					repaint();
				}
			}
		
	};
	
	/**
	 * This is the AddressKeyAdapter for all 4 textFields.
	 * It updates currentAddress, suggestionList and problem details
	 */
	class AddressKeyAdapter extends KeyAdapter {

		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				logger.debug("Enter Key Received");
				logger.debug(e.getSource());
				if (e.getSource() instanceof JTextField) {
					// hide the text field 
					((JTextField)e.getSource()).setVisible(false);
					// update currentAddress
					currentAddress = getChangedAddress();
					// update the suggestionList
					addressValidator = new AddressValidator(addressDatabase, (Address)currentAddress);
					suggestionList.setModel(new JList(addressValidator.getSuggestions().toArray()).getModel());
					// update the problem details
					updateProblemDetails(addressValidator);
					repaint();
				}
			}
		}
		
	}
	
	public void updateProblemDetails(AddressValidator addressValidator) {
		validateResult = addressValidator.getResults();
		logger.debug("The size of the Problems is : " + validateResult.size());
		problemsBuilder.getPanel().removeAll();
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
	
	private void updateTextFields() {
		addressTextField.setText(currentAddress.getAddress());
		municipalityTextField.setText(currentAddress.getMunicipality());
		provinceTextField.setText(currentAddress.getProvince());
		postalCodeTextField.setText(currentAddress.getPostalCode());
	}
}
