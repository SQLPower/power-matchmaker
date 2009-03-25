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

import javax.swing.JList;
import javax.swing.ListCellRenderer;

import ca.sqlpower.matchmaker.address.Address;
import ca.sqlpower.matchmaker.address.AddressDatabase;
import ca.sqlpower.matchmaker.address.AddressResult;

public class AddressListCellRenderer implements ListCellRenderer {

    /**
     * The address to compare against when rendering the label. If null,
     * no comparison will be made.
     */
    private final Address comparisonAddress;
	private AddressDatabase addressDatabase;
	private final boolean showValidCheckmark;
    
    public AddressListCellRenderer(Address comparisonAddress, AddressDatabase addressDatabase, boolean showValidCheckmark) {
        this.comparisonAddress = comparisonAddress;
        this.addressDatabase = addressDatabase;
		this.showValidCheckmark = showValidCheckmark;
    }
    
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		AddressLabel addressLabel;
		if (value instanceof Address) {
			addressLabel = new AddressLabel((Address)value, comparisonAddress, addressDatabase, showValidCheckmark);
		} else if (value instanceof AddressResult) {
			AddressResult addressResult = (AddressResult) value;
			Address address1;
			if (addressResult.getOutputAddress().isEmptyAddress()) {
				try {
					address1 = Address.parse(
						addressResult.getInputAddress().getUnparsedAddressLine1(), addressResult.getInputAddress().getMunicipality(), addressResult.getInputAddress().getProvince(),
						addressResult.getInputAddress().getPostalCode(), addressResult.getInputAddress().getCountry(), addressDatabase);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				addressResult.setOutputAddress(address1);
			} else {
				address1 = addressResult.getOutputAddress();
			}
			addressLabel = new AddressLabel(address1, comparisonAddress, addressDatabase, showValidCheckmark);
		} else {
			throw new ClassCastException("Attempting to cast " + value.getClass() + " to Address or AddressResult for rendering an AddressLabel.");
		}

		if (isSelected) {
			addressLabel.setBackground(list.getSelectionBackground());
		}
		
		return addressLabel;
	}

}
