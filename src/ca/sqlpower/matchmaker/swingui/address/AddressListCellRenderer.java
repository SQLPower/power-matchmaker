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

import org.antlr.runtime.RecognitionException;

import ca.sqlpower.matchmaker.address.Address;
import ca.sqlpower.matchmaker.address.AddressDatabase;
import ca.sqlpower.matchmaker.address.AddressInterface;
import ca.sqlpower.matchmaker.address.AddressResult;

public class AddressListCellRenderer implements ListCellRenderer {

    /**
     * The address to compare against when rendering the label. If null,
     * no comparison will be made.
     */
    private final AddressInterface comparisonAddress;
	private AddressDatabase addressDatabase;
    
    public AddressListCellRenderer(AddressInterface comparisonAddress, AddressDatabase addressDatabase) {
        this.comparisonAddress = comparisonAddress;
        this.addressDatabase = addressDatabase;
    }
    
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		if (value instanceof AddressResult) {
			AddressResult result = (AddressResult) value;
			Address address;
			if (result.getOutputAddress().isEmptyAddress()) {
				try {
					address = Address.parse(
							result.getInputAddress(), result
							.getInputMunicipality(), result.getInputProvince(),
							result.getInputPostalCode(), result.getInputCountry(), addressDatabase);
					result.setOutputAddress(address);
				} catch (RecognitionException e) {
					throw new RuntimeException("An error occured while trying to parse the address", e);
				}
			}
			return new AddressLabel(result, comparisonAddress, isSelected, list, addressDatabase);
		}
		return new AddressLabel((AddressInterface)value, comparisonAddress, isSelected, list, addressDatabase);
	}

}
