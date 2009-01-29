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

/**
 * A class for representing any North American address (urban, rural, or post office box).
 * Instances of this class are mutable, because they serve as working areas for the
 * validation and correction logic. Methods are provided so client code can discover any
 * changes made to the address fields by validation and correction code.
 */
public class Address {

    public static enum Type {
        URBAN, RURAL, PO_BOX
    }
    
    /**
     * This is the original input string, as provided by client code.
     */
    private String unparsedAddress;
    
    /**
     * The suite, unit, or apartment number. If the address does not have
     * a suite number, this will be null.
     */
    private String suite;

    /**
     * A flag to indicate if the suite number is a prefix (for example, <i>2110-4950 Yonge St</i>
     * has its suite number as a prefix, and <i>4950 Yonge St Suite 2110</i> does not).
     */
    private boolean suitePrefix;

    /**
     * The street number, excluding suffix. This field will never be null for
     * an URBAN address, and will always be null for other address types.
     */
    private String streetNumber;
    
    /**
     * The street number suffix (1/2, 1/4, 3/4, or a letter A-Z) if this address
     * has one.
     */
    private String streetNumberSuffix;
    
    /**
     * The street name, rural route, or post office box identifier. This field is never
     * null.
     */
    private String street;
    
    /**
     * The street type. Most urban addresses have this field, but it can be null for
     * any address type.
     */
    private String streetType;
    
    /**
     * The street direction (N, S, E, W, and so on). Null if this address does not have
     * a street direction. For French addresses, West is represented by "O" (Ouest). Other
     * compass directions are represented by the same letter in French and English.
     */
    private String streetDirection;
    
    /**
     * A flag to indicate if the street direction is a prefix (for example, <i>157 N 4th St</i>
     * has its direction as a prefix, and <i>157 4th St N</i> does not).
     */
    private boolean directionPrefix;
    
    /**
     * The name of the city, or town this address belongs to. Note that large cities (such
     * as Toronto) are divided up internally into several municipality names by Canada Post. 
     */
    private String municipality;
    private transient boolean municipalityChanged;
    
    /**
     * The two-letter province or state abbreviation.
     */
    private String province;
    
    /**
     * The postal or zip code for this address.
     */
    private String postalCode;
    
    /**
     * The two-letter ISO country code for this address.
     */
    private String country;
    
    /**
     * The address type. See {@link Type} for details.
     */
    private Type type;
    
    /**
     * Creates a new Address record with all fields set to their defaults (usually null).
     */
    public Address() {
        
    }
    
    /**
     * Resets the flags that track field changes. If you plan to check if the
     * correction exercise has modified fields, you should call this after
     * setting up (or parsing) the original address, but before requesting
     * address corrections.
     */
    public void resetChangeFlags() {
        municipalityChanged = false;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (suite != null && suitePrefix) {
            sb.append(suite).append("-");
        }
        if (streetNumber != null) {
            sb.append(streetNumber).append(" ");
        }
        if (streetDirection != null && directionPrefix) {
            sb.append(streetDirection).append(" ");
        }
        if (streetNumberSuffix != null) {
            sb.append(streetNumberSuffix).append(" ");
        }
        sb.append(street);
        if (streetType != null) {
            sb.append(" ").append(streetType);
        }
        if (streetDirection != null && !directionPrefix) {
            sb.append(" ").append(streetDirection);
        }
        if (suite != null && !suitePrefix) {
            sb.append(" ").append(suite);
        }
        
        sb.append("\n");
        sb.append(municipality);
        sb.append(" ").append(province);
        sb.append("  ").append(postalCode);

        sb.append("\n");
        sb.append(country);
        
        return sb.toString();
    }

    public String getUnparsedAddress() {
        return unparsedAddress;
    }

    public void setUnparsedAddress(String unparsedAddress) {
        this.unparsedAddress = unparsedAddress;
    }

    public String getSuite() {
        return suite;
    }

    public void setSuite(String suite) {
        this.suite = suite;
    }

    public boolean isSuitePrefix() {
        return suitePrefix;
    }

    public void setSuitePrefix(boolean suitePrefix) {
        this.suitePrefix = suitePrefix;
    }

    public String getStreetNumber() {
        return streetNumber;
    }

    public void setStreetNumber(String streetNumber) {
        this.streetNumber = streetNumber;
    }

    public String getStreetNumberSuffix() {
        return streetNumberSuffix;
    }

    public void setStreetNumberSuffix(String streetNumberSuffix) {
        this.streetNumberSuffix = streetNumberSuffix;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getStreetType() {
        return streetType;
    }

    public void setStreetType(String streetType) {
        this.streetType = streetType;
    }

    public String getStreetDirection() {
        return streetDirection;
    }

    public void setStreetDirection(String streetDirection) {
        this.streetDirection = streetDirection;
    }

    public boolean isDirectionPrefix() {
        return directionPrefix;
    }

    public void setDirectionPrefix(boolean directionPrefix) {
        this.directionPrefix = directionPrefix;
    }

    public String getMunicipality() {
        return municipality;
    }

    public void setMunicipality(String municipality) {
        if ( (municipality == null && this.municipality == null) 
                || (municipality != null && municipality.equals(this.municipality)) ) {
            return;
        }
        municipalityChanged = true;
        this.municipality = municipality;
    }
    
    public boolean isMunicipalityChanged() {
        return municipalityChanged;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
    
    
}
