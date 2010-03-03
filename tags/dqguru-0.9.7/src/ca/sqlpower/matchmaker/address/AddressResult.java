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

package ca.sqlpower.matchmaker.address;

import java.util.List;

/**
 * An object representation of an invalid address record
 * as stored in the Address Correction Result table.
 */
public class AddressResult {
	
	static enum StorageState {
		/**
		 * Address doesn't exist yet in the database
		 */
		NEW("NEW"),		
		
		/**
		 * Address exists in the database and hasn't been changed in the
		 * AddressPool since the last load or store call.
		 */
		CLEAN("CLEAN"),
		
		/**
		 * Address exists in the database, but has been changed in the
		 * AddressPool since the last load or store call.
		 */
		DIRTY("DIRTY"),
		
		/**
		 * Address has been marked to be deleted. On the next store call, delete
		 * this address from the address pool.
		 */
		DELETE("DELETE"); 
		
		private String name;
		
		StorageState(String name) {
			this.name = name;
		}
		
		public String toString() {
			return name;
		}
		
	}

	/**
	 * The primary key values of the original address from the database so we 
	 * can tell where the original address came from.
	 */
	private List<Object> keyValues;
	
	/**
	 * This is the address from the database unparsed.
	 */
	private final Address inputAddress;
	
	/**
	 * The address result after it has been parsed and validated.
	 */
	private Address outputAddress;
	
	/**
	 * The state the current address is in based on changes from the database.
	 * If the storage state is dirty then it has been changed and not saved.
	 */
	private StorageState storageState;
	
	/**
	 * Tracks if the last address placed in the outputAddress variable is considered
	 * valid or not.
	 */
	private boolean valid;

	public AddressResult(List<Object> keyValues, String addressLine1,
			String addressLine2, String municipality, String province,
			String postalCode, String country) {
		this.keyValues = keyValues;
		inputAddress = new Address();
		getInputAddress().setUnparsedAddressLine1(addressLine1);
		getInputAddress().setUnparsedAddressLine2(addressLine2);
		getInputAddress().setMunicipality(municipality);
		getInputAddress().setProvince(province);
		getInputAddress().setPostalCode(postalCode);
		getInputAddress().setCountry(country);
		
		outputAddress = new Address(inputAddress);
		storageState = StorageState.NEW;
		valid = false;
	}
	
	public AddressResult(List<Object> keyValues, String addressLine1,
			String addressLine2, String municipality, String province,
			String postalCode, String country, Address outputAddress,
			Boolean isValid) {
		this.keyValues = keyValues;
		inputAddress = new Address();
		getInputAddress().setUnparsedAddressLine1(addressLine1);
		getInputAddress().setUnparsedAddressLine2(addressLine2);
		getInputAddress().setMunicipality(municipality);
		getInputAddress().setProvince(province);
		getInputAddress().setPostalCode(postalCode);
		getInputAddress().setCountry(country);
		
		this.outputAddress = outputAddress;
		storageState = StorageState.NEW;
		if (isValid == null) {
			valid = false;
		} else {
			valid = isValid;
		}
	}
	
	public List<Object> getKeyValues() {
		return keyValues;
	}

	public Address getOutputAddress() {
		return outputAddress;
	}
	
	public Address getInputAddress() {
		return inputAddress;
	}

	public void setOutputAddress(Address address) {
		if (outputAddress.equals(address)) return;
		this.outputAddress = address;
	}
	
	public void markClean() {
		storageState = StorageState.CLEAN;
	}
	
	public void markDirty() {
		storageState = StorageState.DIRTY;
	}
	
	public void markDelete() {
		storageState = StorageState.DELETE;
	}
	
	StorageState getStorageState() {
		return storageState;
	}
	
	public String toString() {
		return getInputAddress() + ", " + 
			   outputAddress;
	}

	public void setValid(boolean isValid) {
			this.valid = isValid;
		
	}

	public boolean isValid() {
		return valid;
	}
}
