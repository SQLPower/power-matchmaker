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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.antlr.runtime.RecognitionException;
import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.address.Address;
import ca.sqlpower.swingui.ColourScheme;
import static ca.sqlpower.matchmaker.address.AddressValidator.different;
import ca.sqlpower.matchmaker.address.AddressResult;
import ca.sqlpower.matchmaker.swingui.MMSUtils;

public class AddressLabel extends JComponent {
	
	private static final Logger logger = Logger.getLogger(AddressLabel.class);

	private Address currentAddress;
	private Address revertToAddress;
	
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
	
	private Rectangle2D addressLine1Hotspot;
	
	/**
	 * if list is null, then this AddressLabel is not editable.
	 * else, otherwise.
	 */
	private JList list;
	private boolean isSelected;

    private Color missingFieldColour = ColourScheme.BREWER_SET19.get(0);
	
    public AddressLabel(Address address, boolean isSelected, JList list) {
        this(address, null, isSelected, list);
    }
    
    public AddressLabel(Address address, Address comparisonAddress, boolean isSelected, JList list) {
		this.currentAddress = this.revertToAddress = address;
        this.comparisonAddress = comparisonAddress;
		this.isSelected = isSelected;
		this.list = list;
		this.setOpaque(true);
		//setBackground(Color.WHITE);
		setFont(Font.decode("plain 12"));
		FontMetrics fm = getFontMetrics(getFont());
		if (list != null) {
			setPreferredSize(new Dimension(fm.stringWidth(getProperLabelLength()) + 14, fm.getHeight() * 3));
		} else {
			setPreferredSize(new Dimension(fm.charWidth('m') * 35 , fm.getHeight() * 5));
			setMaximumSize(new Dimension(fm.charWidth('m') * 42, fm.getHeight() * 10));
		}
		AddressLabelBorderFactory borderFactory = new AddressLabelBorderFactory();
		EmptyBorder emptyBorder = new EmptyBorder(3,4,3,4);
		CompoundBorder border = borderFactory.generateAddressLabelBorder(Color.LIGHT_GRAY, 2, 5, true, emptyBorder);
		setBorder(border);
		if (list == null) {
			addMouseListener(new MouseListener() {

				public void mouseClicked(MouseEvent e) {
					logger.info("Stub call: MouseListener.mouseClicked()");
				}

				public void mouseEntered(MouseEvent e) {
					logger.debug("Stub call: MouseListener.mouseEntered()");
				}

				public void mouseExited(MouseEvent e) {
					logger.debug("Stub call: MouseListener.mouseExited()");
				}

				public void mousePressed(MouseEvent e) {
					logger.debug("Stub call: MouseListener.mousePressed()");
				}

				public void mouseReleased(MouseEvent e) {
					logger.debug("Stub call: MouseListener.mouseReleased()");
				}
				
			});
		}
	}
	
	public Address getRevertToAddress() {
		return revertToAddress;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
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
		if (!isFieldMissing(currentAddress.getStreetAddress())) {
		    if (comparisonAddress != null && different(currentAddress.getStreetAddress(), comparisonAddress.getStreetAddress())) {
		        g2.setColor(comparisonColour);
		    } else {
                g2.setColor(getForeground());
		    }
			g2.drawString(currentAddress.getStreetAddress(), x, y);
			addressLine1Hotspot = fm.getStringBounds(currentAddress.getStreetAddress(), g2);
		} else {
			g2.setColor(missingFieldColour);
			g2.drawString("Street Address Missing", x, y);
			addressLine1Hotspot = fm.getStringBounds("Street Address Missing", g2);
		}
		y += fm.getHeight();
		if (!isFieldMissing(currentAddress.getMunicipality())) {
		    if (comparisonAddress != null && different(currentAddress.getMunicipality(), comparisonAddress.getMunicipality())) {
                g2.setColor(comparisonColour);
		    } else {
		        g2.setColor(getForeground());
		    }
			g2.drawString(currentAddress.getMunicipality(), x, y);
			x += fm.stringWidth(currentAddress.getMunicipality() + " ");
		} else {
			g2.setColor(missingFieldColour);
			g2.drawString("Municipality Missing", x, y);
			x += fm.stringWidth("Municipality Missing" + " ");
		}
		if (!isFieldMissing(currentAddress.getProvince())) {
            if (comparisonAddress != null && different(currentAddress.getProvince(), comparisonAddress.getProvince())) {
                g2.setColor(comparisonColour);
            } else {
                g2.setColor(getForeground());
            }
			g2.drawString(currentAddress.getProvince(), x, y);
			x += fm.stringWidth(currentAddress.getProvince() + " ");
		} else {
			g2.setColor(missingFieldColour);
			g2.drawString("Province Missing", x, y);
			x += fm.stringWidth("Province Missing" + " ");
		}
		if (!isFieldMissing(currentAddress.getPostalCode())) {
            if (comparisonAddress != null && different(currentAddress.getPostalCode(), comparisonAddress.getPostalCode())) {
                g2.setColor(comparisonColour);
            } else {
                g2.setColor(getForeground());
            }
			g2.drawString(currentAddress.getPostalCode(), x, y);
			x += fm.stringWidth(currentAddress.getPostalCode() + " ");
		} else {
			g2.setColor(missingFieldColour);
			g2.drawString("PostalCode Missing", x, y);
			x += fm.stringWidth("PostalCode Missing");
		}		
	}
	
	public void setAddress(Address address) {
		this.currentAddress = address;
		repaint();
	}
	
	private boolean isFieldMissing(String str) {
		if (str == null) {
			return true;
		}
		return str.trim().equals("null") || str.trim().equals("");
	}

	/**
	 * Get the longer line of 2 lines addressLabel
	 * @param address
	 * @return the longer line of addressLabel
	 */
	private String lineLength(Address address) {
		String streetAddress1 = "";
		String streetAddress2 = "";
		if (isFieldMissing(address.getStreetAddress())) {
			streetAddress1 += "Street Address Missing";
		} else {
			streetAddress1 += address.getStreetAddress();
		}
		streetAddress1 += "  ";
		if (isFieldMissing(address.getMunicipality())) {
			streetAddress2 += "Municipality Missing";
		} else {
			streetAddress2 += address.getMunicipality();
		}
		streetAddress2 += " ";
		if (isFieldMissing(address.getProvince())) {
			streetAddress2 += "Province Missing";
		} else {
			streetAddress2 += address.getProvince();
		}
		streetAddress2 += " ";
		if (isFieldMissing(address.getPostalCode())) {
			streetAddress2 += "PostalCode Missing";
		} else {
			streetAddress2 += address.getPostalCode();
		}
		streetAddress2 += "  ";
		if (streetAddress1.length() > streetAddress2.length()) {
			return streetAddress1;
		} else {
			return streetAddress2;
		}
	}
	
	/**
	 * Find the longest String line in a list of addresses
	 * @return the longest String line of a list of addresses
	 */
	private String getProperLabelLength() {
		String properLength = "";
		String temp = "";
		Address tempAddress = null;
		for (int i = 0; i <list.getModel().getSize(); i++) {
			if (list.getModel().getElementAt(i) instanceof AddressResult) {
				AddressResult a = (AddressResult)list.getModel().getElementAt(i);
				try {
					tempAddress = Address.parse(a.getAddressLine1(), a.getMunicipality(), a.getProvince(), a.getPostalCode(), a.getCountry());
				} catch (RecognitionException e) {
					MMSUtils
					.showExceptionDialog(
							getParent(),
							"There was an error while trying to parse this address",
							e);
				}
			} else if (list.getModel().getElementAt(i) instanceof Address){
				tempAddress = (Address)list.getModel().getElementAt(i);
			}
			temp = lineLength(tempAddress);
			if (temp.length() > properLength.length()) {
				properLength = temp;
			}
		}
		return properLength;
	}
}
