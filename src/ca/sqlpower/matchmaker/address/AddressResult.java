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

package ca.sqlpower.matchmaker.address;

import java.util.List;

import org.apache.log4j.Logger;

/**
 * An object representation of an invalid address record
 * as stored in the Address Correction Result table.
 */
public class AddressResult {
	
	private static final Logger logger = Logger.getLogger(AddressResult.class); 

	static enum StorageState {
		NEW("NEW"),
		CLEAN("CLEAN"),
		DIRTY("DIRTY");
		
		private String name;
		
		StorageState(String name) {
			this.name = name;
		}
		
		public String toString() {
			return name;
		}
		
	}
	
	private List<Object> keyValues;
	
	private String addressLine1;
	private String addressLine2;
	private String municipality;
	private String province;
	private String postalCode;
	private String country;
	private Address outputAddress;
	
	/**
	 * Whether or not the user or any auto-validation system has marked this
	 * address record as validated.
	 */
	private boolean isValidated;
	private StorageState storageState;

	public AddressResult(List<Object> keyValues, String addressLine1,
			String addressLine2, String municipality, String province,
			String postalCode, String country, boolean isValidated) {
		this.keyValues = keyValues;
		this.addressLine1 = addressLine1;
		this.addressLine2 = addressLine2;
		this.municipality = municipality;
		this.province = province;
		this.postalCode = postalCode;
		this.country = country;
		this.isValidated = isValidated;
		
		outputAddress = new Address();
		storageState = StorageState.NEW;
	}
	
	public AddressResult(List<Object> keyValues, String addressLine1,
			String addressLine2, String municipality, String province,
			String postalCode, String country, Address outputAddress, boolean isValidated) {
		this.keyValues = keyValues;
		this.addressLine1 = addressLine1;
		this.addressLine2 = addressLine2;
		this.municipality = municipality;
		this.province = province;
		this.postalCode = postalCode;
		this.country = country;
		this.isValidated = isValidated;
		
		this.outputAddress = outputAddress;
		storageState = StorageState.NEW;
	}
	
	public List<Object> getKeyValues() {
		return keyValues;
	}

	public String getAddressLine1() {
		return addressLine1;
	}

	public String getAddressLine2() {
		return addressLine2;
	}

	public String getMunicipality() {
		return municipality;
	}

	public String getProvince() {
		return province;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public String getCountry() {
		return country;
	}

	public boolean isValidated() {
		return isValidated;
	}

	public Address getOutputAddress() {
		return outputAddress;
	}
	
	public void markClean() {
		storageState = StorageState.CLEAN;
	}
	
	public void markDirty() {
		storageState = StorageState.DIRTY;
	}
	
	StorageState getStorageState() {
		return storageState;
	}
	
	/**
	 * Returns a String which is a HTML format so that Street Lines and 
	 * Municipality,Province,Country and Postal code can show on 3 different lines but 
	 * within the same item.  
	 *  
	 * @return a HTML formatted String of the address.
	 */
	public String htmlToString() {
		StringBuffer sb = new StringBuffer();
		sb.append("<html><p align=left>");
		sb.append(getAddressLine1() != null ? getAddressLine1() : "");
		if (addressLine1 != null && !addressLine1.trim().equals("")) {
			sb.append(" ");
		}
		sb.append(getAddressLine2() != null ? getAddressLine2() : "");
		
		if ( !isThisLineExist(addressLine1) && !isThisLineExist(addressLine2) ) {
			sb.append("<font color=\"FF0000\">Address Line Missing</font>");
		}
		sb.append("<br>");
		sb.append(isThisLineExist(municipality) ? getMunicipality() : "<font color=\"FF0000\">Municipality Missing</font>");
		sb.append(" ");
		sb.append(isThisLineExist(province) ? getProvince() : "<font color=\"FF0000\">Province Missing</font>");
		sb.append(" ");
		sb.append(isThisLineExist(country) ? getCountry() : "<font color=\"FF0000\">Country Missing</font>");
		sb.append("<br>");
		sb.append(isThisLineExist(postalCode) ? postalCode : "<font color=\"FF0000\">Postal Code Missing</font>");
		sb.append("</p>" + "</html>");
		return sb.toString();
	}
	/**
	 * Determine whether the first line of htmlToString(addressline1 and addressline2
	 * should exist or not. If they are nulls or empty Strings or either, the first line
	 * of htmlToString should be the next line( municipality,province and country).
	 * @return true if the first line of address has meaning, false otherwise.
	 */
	private boolean isThisLineExist(String str) {
		boolean line = (str == null || str.trim().equals(""));
		return !line;

	}
	
	public String toString() {
		return addressLine1 + " " + 
			   addressLine2 + " " + 
			   municipality + " " + 
			   province + " " + 
			   country + " " +
			   postalCode + " " + 
			   outputAddress;
	}
}
