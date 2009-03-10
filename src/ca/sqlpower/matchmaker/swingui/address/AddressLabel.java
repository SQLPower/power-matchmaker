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

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.address.Address;
import ca.sqlpower.swingui.ColourScheme;
import static ca.sqlpower.matchmaker.address.AddressValidator.different;

public class AddressLabel extends JComponent {
	
	private static final Logger logger = Logger.getLogger(AddressLabel.class);

	private Address address;
	
	/**
	 * If non-null, fields in {@link #address} that differ from fields in this
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
	private JList list;
	private boolean isSelected;

    private Color missingFieldColour = ColourScheme.BREWER_SET19.get(0);
	
    public AddressLabel(Address address, boolean isSelected, JList list) {
        this(address, null, isSelected, list);
    }
    
    public AddressLabel(Address address, Address comparisonAddress, boolean isSelected, JList list) {
		this.address = address;
        this.comparisonAddress = comparisonAddress;
		this.isSelected = isSelected;
		this.list = list;
		this.setOpaque(true);
		//setBackground(Color.WHITE);
		setFont(Font.decode("plain 12"));
		FontMetrics fm = getFontMetrics(getFont());
		setPreferredSize(new Dimension(fm.charWidth('m') * 33, fm.getHeight() * 3));
		setMaximumSize(new Dimension(fm.charWidth('m') * 40, fm.getHeight() * 10));
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
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
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
		if (!isFieldMissing(address.getStreetAddress())) {
		    if (comparisonAddress != null && different(address.getStreetAddress(), comparisonAddress.getStreetAddress())) {
		        g2.setColor(comparisonColour);
		    } else {
                g2.setColor(getForeground());
		    }
			g2.drawString(address.getStreetAddress(), x, y);
			addressLine1Hotspot = fm.getStringBounds(address.getStreetAddress(), g2);
		} else {
			g2.setColor(missingFieldColour);
			g2.drawString("Street Address Missing", x, y);
			addressLine1Hotspot = fm.getStringBounds("Street Address Missing", g2);
		}
		y += fm.getHeight();
		if (!isFieldMissing(address.getMunicipality())) {
		    if (comparisonAddress != null && different(address.getMunicipality(), comparisonAddress.getMunicipality())) {
                g2.setColor(comparisonColour);
		    } else {
		        g2.setColor(getForeground());
		    }
			g2.drawString(address.getMunicipality(), x, y);
			x += fm.stringWidth(address.getMunicipality() + " ");
		} else {
			g2.setColor(missingFieldColour);
			g2.drawString("Municipality Missing", x, y);
			x += fm.stringWidth("Municipality Missing" + " ");
		}
		if (!isFieldMissing(address.getProvince())) {
            if (comparisonAddress != null && different(address.getProvince(), comparisonAddress.getProvince())) {
                g2.setColor(comparisonColour);
            } else {
                g2.setColor(getForeground());
            }
			g2.drawString(address.getProvince(), x, y);
			x += fm.stringWidth(address.getProvince() + " ");
		} else {
			g2.setColor(missingFieldColour);
			g2.drawString("Province Missing", x, y);
			x += fm.stringWidth("Province Missing" + " ");
		}
		if (!isFieldMissing(address.getPostalCode())) {
            if (comparisonAddress != null && different(address.getPostalCode(), comparisonAddress.getPostalCode())) {
                g2.setColor(comparisonColour);
            } else {
                g2.setColor(getForeground());
            }
			g2.drawString(address.getPostalCode(), x, y);
			x += fm.stringWidth(address.getPostalCode() + " ");
		} else {
			g2.setColor(missingFieldColour);
			g2.drawString("PostalCode Missing", x, y);
			x += fm.stringWidth("PostalCode Missing");
		}
//		if (lineLength(address) > getPreferredSize().width) {
//			setPreferredSize(new Dimension(lineLength(address),fm.getHeight()*3));
//			logger.info(lineLength(address));
//		}
	}
	
	private boolean isFieldMissing(String str) {
		if (str == null) {
			return true;
		}
		return str.trim().equals("null") || str.trim().equals("");
	}

	private int lineLength(Address address) {
		int length1 = 0;
		int length2 = 0;
		if (isFieldMissing(address.getStreetAddress())) {
			length1 = "Street Address Missing".length();
		} else {
			length1 = address.getStreetAddress().length();
		}
		if (isFieldMissing(address.getMunicipality())) {
			length2 += "Municipality Missing".length();
		} else {
			length2 += address.getMunicipality().length();
		}
		if (isFieldMissing(address.getProvince())) {
			length2 += "Province Missing".length();
		} else {
			length2 += address.getProvince().length();
		}
		if (isFieldMissing(address.getPostalCode())) {
			length2 += "PostalCode Missing".length();
		} else {
			length2 += address.getPostalCode().length();
		}
		return Math.max(length1, length2);
	}
}
