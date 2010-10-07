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

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.address.Address;
import ca.sqlpower.matchmaker.address.steps.ValidateStepUtil;
import ca.sqlpower.swingui.ColourScheme;
import ca.sqlpower.swingui.SPSUtils;

public class AddressLabel extends JComponent {
	
	private static final Logger logger = Logger.getLogger(AddressLabel.class);

	private Address currentAddress;
	
	private static final int BORDER_SPACE = 12;
	/**
	 * If non-null, fields in {@link #currentAddress} that differ from fields in this
	 * address will be rendered in a different colour.
	 */
	private Address comparisonAddress;
	
	/**
	 * The colour "differing" fields will be rendered in. Non-differing fields
	 * will be rendered in this component's foreground colour.
	 * 
	 * @see #setForeground(Color)
	 */
	private Color comparisonColour = ColourScheme.BREWER_SET19.get(1);
	
	/**
	 * Each rectangle listens the mouse click for each address field 
	 * so that user can edit the address label field by field. 
	 */
	private Rectangle2D addressLine1Hotspot = new Rectangle();
	private Rectangle2D municipalityHotspot = new Rectangle();
	private Rectangle2D provinceHotsopt = new Rectangle();
	private Rectangle2D postalCodeHotspot = new Rectangle();
	
	/**
	 * Each textField is for user editing the the addressLabel.
	 * They will be always invisible unless the user click on 
	 * corresponding address field.
	 */
	private final JTextField addressTextField;
	private final JTextField municipalityTextField;
	private final JTextField provinceTextField;
	private final JTextField postalCodeTextField;

	/**
	 * The icon for valid address. It will appear as soon
	 * as the address is corrected. 
	 */
	private ImageIcon checkIcon = SPSUtils.createIcon("check", "Check");
	
	/**
	 * Red color for any missing fields in the addressLabel.
	 */
    private Color missingFieldColour = ColourScheme.BREWER_SET19.get(0);
    
    /**
     * The border for every addressLabel.
     */
    private CompoundBorder border;
    
    /**
     * The internal JLabel which will do "..." when the length of the
     * address is longer than the width of addressLabel automatically.  
     * The only thing they are doing and should do is to support "..." feature.
     */
	private JLabel addressLabel = new JLabel();
	private JLabel municipalityLabel = new JLabel();
	private JLabel provinceLabel = new JLabel();

	private JLabel postalCodeLabel = new JLabel();

	/**
	 * This will control if the address label is allowed to have checkmarks beside it as it
	 * is an address being corrected in the pool or if it is a suggestion that does not show
	 * a checkmark ever.
	 */
	private final boolean allowsValidateCheck;

	/**
	 * Tracks if the address being displayed is actually valid.
	 */
	private boolean addressValid;
	
	/**
	 * @param address The address to display on the label.
	 * @param comparisonAddress
	 * @param allowsValidateCheck If true this label can have a valid check beside it if it is valid.
	 * @param valid If the allowsValidateCheck is true then if this is true the label will start with a
	 * valid check mark. If this is false the label will not have a valid check mark. If allowsValidateCheck
	 * is false then this flag will not be used.
	 */
    public AddressLabel(Address address, Address comparisonAddress, boolean allowsValidateCheck, boolean valid) {
    	this.setAddressValid(valid);
		setBackground(Color.WHITE);
    	this.currentAddress = address;
		
        this.comparisonAddress = comparisonAddress;
		this.allowsValidateCheck = allowsValidateCheck;		
		
		this.setOpaque(true);
		
		addressTextField = new JTextField(currentAddress.getAddress());
		add(addressTextField);		
		municipalityTextField = new JTextField(currentAddress.getMunicipality());
		add(municipalityTextField);		
		provinceTextField = new JTextField(currentAddress.getProvince());
		add(provinceTextField);		
		postalCodeTextField = new JTextField(currentAddress.getPostalCode());
		add(postalCodeTextField);
		
		EmptyBorder emptyBorder = new EmptyBorder(3,4,3,4);
		border = AddressLabelBorderFactory.generateAddressLabelBorder(Color.LIGHT_GRAY, 2, 5, emptyBorder);
		setBorder(border);
		
		setTextFieldsInvisible(addressTextField, municipalityTextField, provinceTextField, postalCodeTextField);
		// Add a MouseListener for the bigger selected label in the center
		// of the Validation screen.
		addMouseListener(new MouseAdapter() {

			public void mouseReleased(MouseEvent e) {
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
			}
		});
		//TODO Figure out where to remove these listeners
		AddressKeyAdapter addressKeyAdapter = new AddressKeyAdapter();
		addressTextField.addKeyListener(addressKeyAdapter);
		TextFieldFocusAdapter textFieldFocusAdapter = new TextFieldFocusAdapter();
		addressTextField.addFocusListener(textFieldFocusAdapter);
		municipalityTextField.addKeyListener(addressKeyAdapter);
		municipalityTextField.addFocusListener(textFieldFocusAdapter);
		provinceTextField.addKeyListener(addressKeyAdapter);
		provinceTextField.addFocusListener(textFieldFocusAdapter);
		postalCodeTextField.addKeyListener(addressKeyAdapter);
		postalCodeTextField.addFocusListener(textFieldFocusAdapter);
		
		if (getFont() == null) {
			setFont(postalCodeLabel.getFont());
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		final Graphics2D g2 = (Graphics2D) g;
		g2.setColor(getParent().getBackground());
		g2.fillRect(0, 0, getWidth(), getHeight());
		g2.setColor(getBackground());
		g2.fillRoundRect(((EmptyBorder)border.getOutsideBorder()).getBorderInsets().left,
			  	         ((EmptyBorder)border.getOutsideBorder()).getBorderInsets().top, 
			  	         getWidth() - 2 * ((EmptyBorder)border.getOutsideBorder()).getBorderInsets().left, 
			  	         getHeight() - 2 * ((EmptyBorder)border.getOutsideBorder()).getBorderInsets().top, 
			  	         ((LineBorder)border.getInsideBorder()).getThickness()*10, 
			  	         ((LineBorder)border.getInsideBorder()).getThickness()*10);

		FontMetrics fm = getFontMetrics(getFont());
		int y = fm.getHeight()+fm.stringWidth(" ");
		int x = 4+fm.stringWidth("M");
		
		if (logger.isDebugEnabled()) {
			Rectangle clip = g2.getClipBounds();
			if (clip != null) {
				g2.setColor(Color.green);
				clip.width--;
				clip.height--;
				g2.draw(clip);
				g2.setColor(getBackground());
				logger.debug("Clipping region: "+g2.getClip()); //$NON-NLS-1$
			} else {
				logger.debug("Null clipping region"); //$NON-NLS-1$
			}
		}
		
		// set the check icon for validated addressResult labels
		if (allowsValidateCheck && addressValid) {
			checkIcon.paintIcon(this, g2, x, y);
			x += checkIcon.getIconWidth() + 4;
		}
		if (!isFieldMissing(currentAddress.getAddress())) {
			logger.debug("Current Address is: " + currentAddress);
		    if (comparisonAddress != null && ValidateStepUtil.different(currentAddress.getAddress(), comparisonAddress.getAddress())) {
		        addressLabel.setForeground(comparisonColour);
		    } else {
		    	addressLabel.setForeground(getForeground());
		    }
		    // Set the JLabel for addressLine
		    g2.translate(x,y - fm.getAscent());
			addressLabel.setText(currentAddress.getAddress());
			addressLabel.setBounds(0, 0, getWidth() - x - fm.charWidth('M'), fm.getHeight());
			addressLabel.paint(g2);
			g2.translate(-x, -y + fm.getAscent());
			
			addressLine1Hotspot.setRect(x, y - fm.getAscent(), fm.stringWidth(currentAddress.getAddress()), fm.getHeight());
		} else {
			// Set the JLabel for addressLine
			g2.translate(x,y - fm.getAscent());
			addressLabel.setForeground(missingFieldColour);
			addressLabel.setText("Street Address Missing");
			addressLabel.setBounds(0, 0, getWidth() - x - fm.charWidth('M'), fm.getHeight());
			addressLabel.paint(g2);
			g2.translate(-x, -y + fm.getAscent());
			
			addressLine1Hotspot.setRect(x, y - fm.getAscent(), fm.stringWidth("Street Address Missing"), fm.getHeight());
		}
		setTextBounds(addressTextField, addressLine1Hotspot);
		y += fm.getHeight();
		if (!isFieldMissing(currentAddress.getMunicipality())) {
		    if (comparisonAddress != null && ValidateStepUtil.different(currentAddress.getMunicipality(), comparisonAddress.getMunicipality())) {
                municipalityLabel.setForeground(comparisonColour);
		    } else {
		    	municipalityLabel.setForeground(getForeground());
		    }
		    // Set the JLabel for municipality
		    g2.translate(x,y - fm.getAscent());
		    municipalityLabel.setText(currentAddress.getMunicipality());
		    municipalityLabel.setBounds(0, 0, getWidth() - x - fm.charWidth('M'), fm.getHeight());
		    municipalityLabel.paint(g2);
			g2.translate(-x, -y + fm.getAscent());
			
			municipalityHotspot.setRect(x, y - fm.getAscent(), fm.stringWidth(currentAddress.getMunicipality()), fm.getHeight());
			x += fm.stringWidth(currentAddress.getMunicipality() + " ");
		} else {
		    // Set the JLabel for municipality
			g2.translate(x,y - fm.getAscent());
			municipalityLabel.setForeground(missingFieldColour);
			municipalityLabel.setText("Municipality Missing");
			municipalityLabel.setBounds(0, 0, getWidth() - x - fm.charWidth('M'), fm.getHeight());
			municipalityLabel.paint(g2);
			g2.translate(-x, -y + fm.getAscent());
			
			municipalityHotspot.setRect(x, y - fm.getAscent(), fm.stringWidth("Municipality Missing"), fm.getHeight());
			x += fm.stringWidth("Municipality Missing" + " ");
		}
		setTextBounds(municipalityTextField, municipalityHotspot);
		if (!isFieldMissing(currentAddress.getProvince())) {
            if (comparisonAddress != null && ValidateStepUtil.different(currentAddress.getProvince(), comparisonAddress.getProvince())) {
            	provinceLabel.setForeground(comparisonColour);
            } else {
            	provinceLabel.setForeground(getForeground());
            }
            // Set the JLabel for Province
            g2.translate(x,y - fm.getAscent());
			provinceLabel.setText(currentAddress.getProvince());
			provinceLabel.setBounds(0, 0, getWidth() - x - fm.charWidth('M'), fm.getHeight());
			provinceLabel.paint(g2);
			g2.translate(-x, -y + fm.getAscent());
			
			provinceHotsopt.setRect(x, y - fm.getAscent(), fm.stringWidth(currentAddress.getProvince()), fm.getHeight());
			x += fm.stringWidth(currentAddress.getProvince() + "  ");
		} else {
			 // Set the JLabel for Province
            g2.translate(x,y - fm.getAscent());
            provinceLabel.setForeground(missingFieldColour);
			provinceLabel.setText("Province Missing");
			provinceLabel.setBounds(0, 0, getWidth() - x - fm.charWidth('M'), fm.getHeight());
			provinceLabel.paint(g2);
			g2.translate(-x, -y + fm.getAscent());
			
			provinceHotsopt.setRect(x, y - fm.getAscent(), fm.stringWidth("Province Missing"), fm.getHeight());
			x += fm.stringWidth("Province Missing" + "  ");
		}
		setTextBounds(provinceTextField, provinceHotsopt);
		if (!isFieldMissing(currentAddress.getPostalCode())) {
            if (comparisonAddress != null && ValidateStepUtil.different(currentAddress.getPostalCode(), comparisonAddress.getPostalCode())) {
            	postalCodeLabel.setForeground(comparisonColour);
            } else {
            	postalCodeLabel.setForeground(getForeground());
            }   
            String str;
            if (currentAddress.getPostalCode().length() == 6) {
            	str = currentAddress.getPostalCode().substring(0, 3) + " ";
            	str += currentAddress.getPostalCode().substring(3, 6);
            } else {
            	str = currentAddress.getPostalCode();
            }
            	// Set the JLabel for postalCode
            	g2.translate(x,y - fm.getAscent());
    			postalCodeLabel.setText(str);
    			postalCodeLabel.setBounds(0, 0, getWidth() - x - fm.charWidth('M'), fm.getHeight());
    			postalCodeLabel.paint(g2);
    			g2.translate(-x, -y + fm.getAscent());
    			
            	postalCodeHotspot.setRect(x, y - fm.getAscent(), fm.stringWidth(currentAddress.getPostalCode() + " "), fm.getHeight());
		} else {
			// Set the JLabel for postalCode
        	g2.translate(x,y - fm.getAscent());     
        	postalCodeLabel.setForeground(missingFieldColour);
			postalCodeLabel.setText("PostalCode Missing");
			postalCodeLabel.setBounds(0, 0, getWidth() - x - fm.charWidth('M'), fm.getHeight());
			postalCodeLabel.paint(g2);
			g2.translate(-x, -y + fm.getAscent());
			
			postalCodeHotspot.setRect(x, y - fm.getAscent(), fm.stringWidth("PostalCode Missing"), fm.getHeight());
			x += fm.stringWidth("PostalCode Missing");
		}
		setTextBounds(postalCodeTextField, postalCodeHotspot);
	}
	
	@Override
	public void setFont(Font font) {
		super.setFont(font);
		FontMetrics fm = getFontMetrics(getFont());
		setPreferredSize(new Dimension(fm.charWidth('m') * 22, fm.getHeight() * 3));
		addressLabel.setFont(getFont());
		municipalityLabel.setFont(getFont());
		provinceLabel.setFont(getFont());
		postalCodeLabel.setFont(getFont());
		repaint();
	}
	
	public void setCurrentAddress(Address address) {
		logger.debug("oldAddress: " + currentAddress + ";  newAddress: " + address);
		Address oldValue = currentAddress;
		this.currentAddress = address;
		firePropertyChange("currentAddress", oldValue, currentAddress);
		updateTextFields();
	}
	
	public Address getCurrentAddress() {
		return currentAddress;
	}
	
	private boolean isFieldMissing(String str) {
		if (str == null) {
			return true;
		}
		return str.trim().toUpperCase().equals("null") || str.trim().equals("");
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
		String postalCode = postalCodeTextField.getText().replaceAll("\\s+", "");
		Address a = new Address();
		a.setUnparsedAddressLine1(addressTextField.getText().trim().toUpperCase());
		a.setMunicipality(municipalityTextField.getText().trim().toUpperCase());
		a.setProvince(provinceTextField.getText().trim().toUpperCase());
		a.setPostalCode(postalCode.toUpperCase());
		a.setCountry("CANADA");
		return a;
	}
	
	
	class TextFieldFocusAdapter extends FocusAdapter {

		public void focusLost(FocusEvent e) {
			((JTextField)e.getSource()).setVisible(false);
			logger.debug("Address before parsing is:" + currentAddress.toString());
			Address newCurrentAddress = getChangedAddress();
			logger.debug("Address before parsing is:" + newCurrentAddress.toString());
			if(!(currentAddress.equals(newCurrentAddress))) {
					logger.debug("Addresses are different");
					setCurrentAddress(newCurrentAddress);
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
					Address newAddress = getChangedAddress();
					if(!(currentAddress.equals(newAddress))) {
						// update currentAddress
						setCurrentAddress(newAddress);
					}
				}
			}
		}
		
	}
	
	void updateTextFields() {
		addressTextField.setText(currentAddress.getAddress());
		municipalityTextField.setText(currentAddress.getMunicipality());
		provinceTextField.setText(currentAddress.getProvince());
		postalCodeTextField.setText(currentAddress.getPostalCode());
	}

	public void setAddressValid(boolean addressValid) {
		this.addressValid = addressValid;
		repaint();
	}

	public boolean isAddressValid() {
		return addressValid;
	}
}
