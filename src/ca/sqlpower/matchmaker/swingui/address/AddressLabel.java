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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.antlr.runtime.RecognitionException;
import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.address.Address;
import ca.sqlpower.matchmaker.address.AddressDatabase;
import ca.sqlpower.matchmaker.address.AddressInterface;
import ca.sqlpower.matchmaker.address.AddressResult;
import ca.sqlpower.matchmaker.swingui.MMSUtils;
import ca.sqlpower.swingui.ColourScheme;

public class AddressLabel extends JComponent {
	
	private static final Logger logger = Logger.getLogger(AddressLabel.class);

	private AddressInterface currentAddress;
	private AddressInterface revertToAddress;
	
	private static final int BORDER_SPACE = 12;
	/**
	 * If non-null, fields in {@link #currentAddress} that differ from fields in this
	 * address will be rendered in a different colour.
	 */
	private Address comparisonAddress;
	
	/**
	 * if list is null, then this AddressLabel is not editable.
	 * else, otherwise.
	 */
	private JList list;
	private boolean isSelected;
	
	private AddressDatabase addressDatabase;
	
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
    
    private boolean isAddressValid = false;
	
    public AddressLabel(AddressInterface address, boolean isSelected, JList list, AddressDatabase addressDatabase) {
        this(address, null, isSelected, list, addressDatabase);
    }
    
    public AddressLabel(AddressInterface address, Address comparisonAddress, boolean isSelected, JList list, AddressDatabase addressDatabase) {
		this.currentAddress = this.revertToAddress = address;
        this.comparisonAddress = comparisonAddress;
		this.isSelected = isSelected;
		this.list = list;
		this.addressDatabase = addressDatabase;
		this.setOpaque(true);
		
		updateTextFields(list);

	}

	void updateTextFields(JList list) {
		addressTextField = new JTextField(currentAddress.getAddress());
		add(addressTextField);		
		municipalityTextField = new JTextField(currentAddress.getMunicipality());
		add(municipalityTextField);		
		provinceTextField = new JTextField(currentAddress.getProvince());
		add(provinceTextField);		
		postalCodeTextField = new JTextField(currentAddress.getPostalCode());
		add(postalCodeTextField);
		
		//setBackground(Color.WHITE);
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
			addMouseListener(new MouseListener() {
				public void mouseClicked(MouseEvent e) {
					currentAddress = saveAddressChanges();
					repaint();
					if (isClickingRightArea(e, addressLine1Hotspot)) {
						setTextFieldsInvisible(addressTextField, municipalityTextField, provinceTextField, postalCodeTextField);
						addressTextField.setVisible(true);
						addressTextField.requestFocus();
					} else if (isClickingRightArea(e, municipalityHotspot)) {
						setTextFieldsInvisible(addressTextField, municipalityTextField, provinceTextField, postalCodeTextField);
						municipalityTextField.setVisible(true);
						municipalityTextField.requestFocus();
					} else if (isClickingRightArea(e, provinceHotsopt)) {
						setTextFieldsInvisible(addressTextField, municipalityTextField, provinceTextField, postalCodeTextField);
						provinceTextField.setVisible(true);
						provinceTextField.requestFocus();
					} else if (isClickingRightArea(e, postalCodeHotspot)) {
						setTextFieldsInvisible(addressTextField, municipalityTextField, provinceTextField, postalCodeTextField);
						postalCodeTextField.setVisible(true);
						postalCodeTextField.requestFocus();
					} else {
						setTextFieldsInvisible(addressTextField, municipalityTextField, provinceTextField, postalCodeTextField);
					}
					addressTextField.addKeyListener(new AddressKeyAdapter());
					municipalityTextField.addKeyListener(new AddressKeyAdapter());
					provinceTextField.addKeyListener(new AddressKeyAdapter());
					postalCodeTextField.addKeyListener(new AddressKeyAdapter());
				}

				public void mouseEntered(MouseEvent e) {
					//Do nothing
				}

				public void mouseExited(MouseEvent e) {
					//Do nothing
				}

				public void mousePressed(MouseEvent e) {
					//Do nothing
				}

				public void mouseReleased(MouseEvent e) {
					//Do nothing
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
		// set the check icon for validated address results
		if (currentAddress instanceof AddressResult) {
			if (isAddressValid) {
				checkIcon.paintIcon(this, g2, x, y);
				x += checkIcon.getIconWidth() + 4;
			}
		}
		if (!isFieldMissing(currentAddress.getAddress())) {
		    if (comparisonAddress != null && different(currentAddress.getAddress(), comparisonAddress.getStreetAddress())) {
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
	
	private boolean isFieldMissing(String str) {
		if (str == null) {
			return true;
		}
		return str.trim().equals("null") || str.trim().equals("");
	}
	
	private boolean isClickingRightArea(MouseEvent e, Rectangle2D rec) {
		logger.info(e.getPoint());
		logger.info((rec.getX() + BORDER_SPACE) + "  " + rec.getY());
		logger.info(rec.getWidth() + "  " + rec.getHeight());
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
	
	Address saveAddressChanges() {
		try {
			return Address.parse(addressTextField.getText(), municipalityTextField.getText(),
										   provinceTextField.getText(), postalCodeTextField.getText(), "Canada", addressDatabase);
		} catch (RecognitionException e) {
			MMSUtils.showExceptionDialog(
					getParent(),
					"There was an error while trying to parse this address",
					e);
		}
		return new Address();
	}
	
	class AddressKeyAdapter implements KeyListener {

		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				logger.debug("Enter Key Received");
				logger.debug(e.getSource());
				if (e.getSource() instanceof JTextField) {
					((JTextField)e.getSource()).setVisible(false);
					currentAddress = saveAddressChanges();
					repaint();
				}
			}
		}

		public void keyReleased(KeyEvent e) {
			// Do nothing
		}

		public void keyTyped(KeyEvent e) {
			// Do nothing
		}
		
	}
}
