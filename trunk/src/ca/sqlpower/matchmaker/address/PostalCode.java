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

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

/**
 * All the information we get from the postal system about a particular postal code.
 * This record type is the real meat of the address validation database.
 */
@Entity
public class PostalCode {
	
	public enum RecordType {
		STREET(1),
		STREET_AND_ROUTE(2),
		LOCK_BOX(3),
		ROUTE(4),
		GENERAL_DELIVERY(5);
		
		/**
		 * The type code for each record type defined by Canada Post.
		 */
		private final int recordTypeCode;
		
		private RecordType(int typeCode) {
			recordTypeCode = typeCode;
			
		}
		
		public static RecordType getTypeForCode(int code) {
			for (RecordType type : values()) {
				if (code == type.getRecordTypeCode()) {
					return type;
				}
			}
			throw new IllegalStateException("No known record type for code " + code);
		}
		
		public int getRecordTypeCode() {
			return recordTypeCode;
		}
	}

    /**
     * The postal or ZIP code, formatted in all caps with no spaces (for a
     * Canadian postal code, this means exactly 6 characters).
     */
    @PrimaryKey(sequence="PostalCodePrimaryKey")
    private Long primaryKey;
    
    @SecondaryKey(relate=Relationship.MANY_TO_ONE)
    private String postalCode;
    
    private AddressType addressType;
    
    @SecondaryKey(relate=Relationship.MANY_TO_ONE)
    private String provinceCode;
    
    private String directoryAreaName;
    private String deliveryInstallationPostalCode;
    
    @SecondaryKey(relate=Relationship.MANY_TO_ONE)
    private String municipalityName;

    @SecondaryKey(relate=Relationship.MANY_TO_ONE)
    private String streetName;
    
    @SecondaryKey(relate=Relationship.MANY_TO_ONE)
    private String streetTypeCode;
    private String streetDirectionCode;
    private AddressSequenceType streetAddressSequenceType;
    private Integer streetAddressToNumber;
    private Integer streetAddressFromNumber;
    private String streetAddressNumberSuffixToCode;
    private String streetAddressNumberSuffixFromCode;

    private String routeServiceBoxToNumber;
    private String routeServiceBoxFromNumber;
    private String routeServiceTypeDescription;
    private String routeServiceNumber;

    private String suiteToNumber;
    private String suiteFromNumber;

    /**
     * This is the RecordType's number. RecordType cannot be a secondary key
     * as it is an enum so to create a secondary key as the recordType we will
     * use this int.
     */
    @SecondaryKey(relate=Relationship.MANY_TO_ONE)
    private int recordTypeNumber;
    
    private String deliveryInstallationAreaName;
    @SecondaryKey(relate=Relationship.MANY_TO_ONE)
    private String deliveryInstallationQualifierName;
    private String deliveryInstallationTypeDescription;
    
    /**
     * This value can contain non-numeric values
     */
    private String lockBoxBagFromNumber;
    /**
     * This value can contain non-numeric values
     */
    private String lockBoxBagToNumber;
    
    public String getPostalCode() {
        return postalCode;
    }



    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }



    public AddressType getAddressType() {
        return addressType;
    }



    public void setAddressType(AddressType addressType) {
        this.addressType = addressType;
    }



    public String getProvinceCode() {
        return provinceCode;
    }



    public void setProvinceCode(String provinceCode) {
        this.provinceCode = provinceCode;
    }



    public String getDirectoryAreaName() {
        return directoryAreaName;
    }



    public void setDirectoryAreaName(String directoryAreaName) {
        this.directoryAreaName = directoryAreaName;
    }



    public String getDeliveryInstallationPostalCode() {
        return deliveryInstallationPostalCode;
    }



    public void setDeliveryInstallationPostalCode(
            String deliveryInstallationPostalCode) {
        this.deliveryInstallationPostalCode = deliveryInstallationPostalCode;
    }



    public String getMunicipalityName() {
        return municipalityName;
    }



    public void setMunicipalityName(String municipalityName) {
        this.municipalityName = municipalityName;
    }



    public String getStreetName() {
        return streetName;
    }



    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }



    public String getStreetTypeCode() {
        return streetTypeCode;
    }



    public void setStreetTypeCode(String streetTypeCode) {
        this.streetTypeCode = streetTypeCode;
    }



    public String getStreetDirectionCode() {
        return streetDirectionCode;
    }



    public void setStreetDirectionCode(String streetDirectionCode) {
        this.streetDirectionCode = streetDirectionCode;
    }



    public AddressSequenceType getStreetAddressSequenceType() {
        return streetAddressSequenceType;
    }



    public void setStreetAddressSequenceType(
            AddressSequenceType streetAddressSequenceType) {
        this.streetAddressSequenceType = streetAddressSequenceType;
    }



    public Integer getStreetAddressToNumber() {
        return streetAddressToNumber;
    }



    public void setStreetAddressToNumber(Integer streetAddressToNumber) {
        this.streetAddressToNumber = streetAddressToNumber;
    }



    public Integer getStreetAddressFromNumber() {
        return streetAddressFromNumber;
    }



    public void setStreetAddressFromNumber(Integer streetAddressFromNumber) {
        this.streetAddressFromNumber = streetAddressFromNumber;
    }



    public String getStreetAddressNumberSuffixToCode() {
        return streetAddressNumberSuffixToCode;
    }



    public void setStreetAddressNumberSuffixToCode(
            String streetAddressNumberSuffixToCode) {
        this.streetAddressNumberSuffixToCode = streetAddressNumberSuffixToCode;
    }



    public String getStreetAddressNumberSuffixFromCode() {
        return streetAddressNumberSuffixFromCode;
    }



    public void setStreetAddressNumberSuffixFromCode(
            String streetAddressNumberSuffixFromCode) {
        this.streetAddressNumberSuffixFromCode = streetAddressNumberSuffixFromCode;
    }



    public String getRouteServiceBoxToNumber() {
        return routeServiceBoxToNumber;
    }



    public void setRouteServiceBoxToNumber(String routeServiceBoxToNumber) {
        this.routeServiceBoxToNumber = routeServiceBoxToNumber;
    }



    public String getRouteServiceBoxFromNumber() {
        return routeServiceBoxFromNumber;
    }



    public void setRouteServiceBoxFromNumber(String routeServiceBoxFromNumber) {
        this.routeServiceBoxFromNumber = routeServiceBoxFromNumber;
    }



    public String getRouteServiceTypeDescription() {
        return routeServiceTypeDescription;
    }



    public void setRouteServiceTypeDescription(String routeServiceTypeDescription) {
        this.routeServiceTypeDescription = routeServiceTypeDescription;
    }



    public String getRouteServiceNumber() {
        return routeServiceNumber;
    }



    public void setRouteServiceNumber(String routeServiceNumber) {
        this.routeServiceNumber = routeServiceNumber;
    }



    public String getSuiteToNumber() {
        return suiteToNumber;
    }



    public void setSuiteToNumber(String suiteToNumber) {
        this.suiteToNumber = suiteToNumber;
    }



    public String getSuiteFromNumber() {
        return suiteFromNumber;
    }



    public void setSuiteFromNumber(String suiteFromNumber) {
        this.suiteFromNumber = suiteFromNumber;
    }



    public RecordType getRecordType() {
		return RecordType.getTypeForCode(recordTypeNumber);
	}


    public void setRecordType(int type) {
    	recordTypeNumber = type;
    }

	public void setRecordType(RecordType recordType) {
		recordTypeNumber = recordType.getRecordTypeCode();
	}



	public void setPrimaryKey(Long primaryKey) {
			this.primaryKey = primaryKey;
		
	}



	public Long getPrimaryKey() {
		return primaryKey;
	}



	public String getDeliveryInstallationAreaName() {
		return deliveryInstallationAreaName;
	}



	public void setDeliveryInstallationAreaName(String deliveryInstallationAreaName) {
		this.deliveryInstallationAreaName = deliveryInstallationAreaName;
	}



	public String getDeliveryInstallationQualifierName() {
		return deliveryInstallationQualifierName;
	}



	public void setDeliveryInstallationQualifierName(
			String deliveryInstallationQualifierName) {
		this.deliveryInstallationQualifierName = deliveryInstallationQualifierName;
	}



	public String getDeliveryInstallationTypeDescription() {
		return deliveryInstallationTypeDescription;
	}



	public void setDeliveryInstallationTypeDescription(
			String deliveryInstallationTypeDescription) {
		this.deliveryInstallationTypeDescription = deliveryInstallationTypeDescription;
	}



	public String getLockBoxBagFromNumber() {
		return lockBoxBagFromNumber;
	}



	public void setLockBoxBagFromNumber(String lockBoxBagFromNumber) {
		this.lockBoxBagFromNumber = lockBoxBagFromNumber;
	}



	public String getLockBoxBagToNumber() {
		return lockBoxBagToNumber;
	}



	public void setLockBoxBagToNumber(String lockBoxBagToNumber) {
		this.lockBoxBagToNumber = lockBoxBagToNumber;
	}



	@Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PostalCode: ").append(postalCode);
        sb.append(" addressType: " + addressType);
        sb.append(" provinceCode: ").append(provinceCode);
        sb.append(" directoryAreaName: ").append(directoryAreaName);
        sb.append(" deliveryInstallationPostalCode: ").append(deliveryInstallationPostalCode);
        sb.append(" municipalityName: ").append(municipalityName);
        sb.append(" streetName: ").append(streetName);
        sb.append(" streetTypeCode: ").append(streetTypeCode);
        sb.append(" streetDirectionCode: ").append(streetDirectionCode);
        sb.append(" streetAddressSequenceType: ").append(streetAddressSequenceType);
        sb.append(" streetAddressToNumber: ").append(streetAddressToNumber);
        sb.append(" streetAddressFromNumber: ").append(streetAddressFromNumber);
        sb.append(" streetAddressNumberSuffixToCode: ").append(streetAddressNumberSuffixToCode);
        sb.append(" streetAddressNumberSuffixFromCode: ").append(streetAddressNumberSuffixFromCode);
        sb.append(" routeServiceBoxToNumber: ").append(routeServiceBoxToNumber);
        sb.append(" routeServiceBoxFromNumber: ").append(routeServiceBoxFromNumber);
        sb.append(" routeServiceTypeDescription: ").append(routeServiceTypeDescription);
        sb.append(" routeServiceNumber: ").append(routeServiceNumber);
        sb.append(" suiteToNumber: ").append(suiteToNumber);
        sb.append(" suiteFromNumber: ").append(suiteFromNumber);
        
        return sb.toString();
    }

    /**
     * Determines whether or not this postal code contains the given address.
     * The fields considered when determining a match are province,
     * municipality, street name, street type, street direction, street number,
     * and suite number.
     * 
     * @param a The address to match. The address itself must not be null, but the
     * fields that the address does not have must be null. For example, an address
     * that does not have a suite number must return a null (as opposed to empty)
     * suite number.
     *   
     * @return
     */
    public boolean containsAddress(Address a) {
        if (!nullSafeEquals(getProvinceCode(), a.getProvince())) return false;
        if (!nullSafeEquals(getMunicipalityName(), a.getMunicipality())) return false;
        if (a.getType() == RecordType.STREET) {
        	if (!nullSafeEquals(getStreetName(), a.getStreet())) return false;

        	Integer from = getStreetAddressFromNumber();
        	Integer to = getStreetAddressToNumber();

        	// in English: if there is a from and to street number, but the address falls outside it, FAIL!
        	if (from != null && to != null && (a.getStreetNumber() < from || a.getStreetNumber() > to)) {
        		return false;
        	}
        }
        if (a.getType() == RecordType.ROUTE) {
        	if (a.getRuralRouteNumber() != null && getRouteServiceNumber() != null 
        			&& !getRouteServiceNumber().equals(a.getRuralRouteNumber())) {
        		return false;
        	}
        }
        if (a.getType() == RecordType.LOCK_BOX) {
        	return containsLockBoxNumber(a);
        }
        
        return true;
    }

    /**
     * Returns true if the address given has a lock box number between the to and from
     * lock box number of this postal code inclusive. If the to or from lock box numbers
     * on this postal code or the lock box number on the address given is not an integer
     * then true will also be returned. Returns false otherwise.
     */
	public boolean containsLockBoxNumber(Address a) {
		try {
			int from = Integer.parseInt(getLockBoxBagFromNumber());
			int to = Integer.parseInt(getLockBoxBagToNumber());
			int addressNumber = Integer.parseInt(a.getLockBoxNumber());

			// in English: if there is a from and to lock box number, but the address falls outside it, FAIL!
			if (addressNumber < from || addressNumber > to) {
				return false;
			}
		} catch (NumberFormatException e) {
			//Lock box numbers can be non-numeric, only checking the range
			//at current if it is numeric.
		}
		return true;
	}
    
    /**
     * Returns true if o1.equals(o2) or both o1 and o2 are null.
     */
    private static boolean nullSafeEquals(Object o1, Object o2) {
        if (o1 == null && o2 == null) {
            return true;
        }
        if (o1 == null) {
            return false;
        }
        return o1.equals(o2);
    }

	/**
	 * Returns true if the street address numbers covered by this postal code
	 * contains all the street address numbers covered by the given postal code.
	 * If the given postal code covers the exact same range as this postal code
	 * than false will be returned. If the given postal code is not completely
	 * contained by this postal code than false will be returned.
	 * <p>
	 * This should only be used on and with STREET and STREET_AND_RURAL type postal codes.
	 */
    public boolean contains(PostalCode pc) {
		if (pc.getStreetAddressFromNumber() == null || pc.getStreetAddressToNumber() == null || getStreetAddressFromNumber() == null || getStreetAddressToNumber() == null) return false;
		if (pc.getStreetAddressFromNumber() == getStreetAddressFromNumber() && pc.getStreetAddressToNumber() == getStreetAddressToNumber()) return false;
		if (((pc.getStreetAddressFromNumber() > getStreetAddressFromNumber() && pc.getStreetAddressToNumber() <= getStreetAddressToNumber()) 
				|| (pc.getStreetAddressToNumber() < getStreetAddressToNumber() && pc.getStreetAddressFromNumber() >= getStreetAddressFromNumber())) && 
				(getStreetAddressSequenceType() == AddressSequenceType.CONSECUTIVE ||
						getStreetAddressSequenceType() == pc.getStreetAddressSequenceType())) return true;
		return false;
    }
    
    /**
     * Returns true if the address street number is contained in this postal code.
     * <p>
     * This should only be used on STREET and STREET_AND_RURAL type postal codes and addresses.
     */
    public boolean contains(Address a) {
    	if (a.getStreetNumber() >= getStreetAddressFromNumber() && a.getStreetNumber() <= getStreetAddressToNumber() &&
		(getStreetAddressSequenceType() == AddressSequenceType.CONSECUTIVE || 
			(getStreetAddressSequenceType() == AddressSequenceType.ODD && a.getStreetNumber() % 2 == 1) ||
			(getStreetAddressSequenceType() == AddressSequenceType.EVEN && a.getStreetNumber() % 2 == 0))) return true;
    	return false;
    }
    
}
