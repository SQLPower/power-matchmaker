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

import java.awt.Component;

import javax.swing.JList;
import javax.swing.ListCellRenderer;

import ca.sqlpower.matchmaker.address.Address;
import ca.sqlpower.matchmaker.address.AddressResult;

public class AddressListCellRenderer implements ListCellRenderer {

    /**
     * The address to compare against when rendering the label. If null,
     * no comparison will be made.
     */
    private final Address comparisonAddress;
	private final boolean showValidCheckmark;
    
    public AddressListCellRenderer(Address comparisonAddress, boolean showValidCheckmark) {
        this.comparisonAddress = comparisonAddress;
		this.showValidCheckmark = showValidCheckmark;
    }
    
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		AddressLabel addressLabel;
		if (value instanceof Address) {
			addressLabel = new AddressLabel((Address)value, comparisonAddress, showValidCheckmark, false);
		} else if (value instanceof AddressResult) {
			AddressResult addressResult = (AddressResult) value;
			Address address1;
			address1 = addressResult.getOutputAddress();
			addressLabel = new AddressLabel(address1, comparisonAddress, showValidCheckmark, addressResult.isValid());
		} else {
			throw new ClassCastException("Attempting to cast " + value.getClass() + " to Address or AddressResult for rendering an AddressLabel.");
		}

		if (isSelected) {
			addressLabel.setBackground(list.getSelectionBackground());
		}
		
		return addressLabel;
	}

}
