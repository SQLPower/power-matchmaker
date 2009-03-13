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
public class AddressResult implements AddressInterface {
	
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

	public String getAddress() {
		StringBuilder sb = new StringBuilder();
		if (addressLine1 != null) {
			sb.append(addressLine1);
		} 
		if (addressLine2 != null) {
			if (addressLine1 != null) {
				sb.append(" ");
			}
			sb.append(addressLine2);
		}
		return sb.toString();
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
