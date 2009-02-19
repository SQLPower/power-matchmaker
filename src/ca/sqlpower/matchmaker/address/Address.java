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

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;

import ca.sqlpower.matchmaker.address.parse.AddressLexer;
import ca.sqlpower.matchmaker.address.parse.AddressParser;

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
     * a suite number, this will be null. Note that the suite "number" can
     * be numeric, alphabetic, or alphanumeric. It is always represented
     * here by a string.
     */
    private String suite;

    /**
     * A flag to indicate if the suite number is a prefix (for example, <i>2110-4950 Yonge St</i>
     * has its suite number as a prefix, and <i>4950 Yonge St Suite 2110</i> does not).
     */
    private boolean suitePrefix;

    /**
     * The suite type, as specified in the address. Typical values include "APT", "UNIT",
     * "SUITE", and so on. If the suite number is a prefix, the suite type should not be
     * printed.
     */
    private String suiteType;
    
    /**
     * The street number, excluding suffix. This field will never be null for
     * an URBAN address, and will always be null for other address types.
     */
    private Integer streetNumber;
    
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
     * Creates a new address by parsing all of the "line 1" information and
     * taking the rest of the information individually. If the value for any of
     * the fields is not known, pass in null for that field.
     * 
     * @param streetAddress
     *            The street address as it should appear on the envelope. This
     *            information will be parsed to provide all the individual
     *            street address fields (street name, street number, street
     *            direction, suite, and so on). Example input strings that are
     *            parseable:
     *            <ul>
     *             <li>4950 Yonge St
     *             <li>4950 Yonge St Suite 2110
     *             <li>2110-4950 Yonge St
     *             <li>500 Front St W
     *             <li>300 The Esplanade
     *            </ul>
     * @param municipality
     *            The city, village, town, or suburb name
     * @param province
     *            The two-letter province or state abbreviation
     * @param postalCode
     *            The postal or zip code
     * @param country
     *            The ISO two-letter country code
     * @throws RecognitionException 
     */
    public static Address parse(String streetAddress, String municipality, String province, String postalCode,
            String country) throws RecognitionException {
    	
    	// XXX: Stupid (and hopefully temporary) workaround to prevent the parser from failing.
		// Apparently it expects the municipality, province, and postal code to
		// be in the streetAddress
		streetAddress = streetAddress + " FOO FOO FOO";
    	
    	Address a;
    	if (streetAddress != null) {
    		AddressLexer lexer = new AddressLexer(new ANTLRStringStream(streetAddress.toUpperCase()));
	        TokenStream addressTokens = new CommonTokenStream(lexer);
	        AddressParser p = new AddressParser(addressTokens);
	        p.streetAddress();
	        a = p.getAddress();
    	} else {
    		a = new Address();
    	}
        a.municipality = municipality;
        a.province = province;
        a.postalCode = postalCode;
        a.country = country;
        a.resetChangeFlags();
        return a;
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
            if (suiteType != null) {
                sb.append(" ").append(suiteType);
            }
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

    public String getSuiteType() {
        return suiteType;
    }
    
    public void setSuiteType(String suiteType) {
        this.suiteType = suiteType;
    }
    
    public Integer getStreetNumber() {
        return streetNumber;
    }

    public void setStreetNumber(Integer streetNumber) {
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

    public void normalize() {
        if (municipality != null) setMunicipality(municipality.toUpperCase());
        if (suite != null) setSuite(suite.toUpperCase());
        if (streetNumberSuffix != null) setStreetNumberSuffix(streetNumberSuffix.toUpperCase());
        if (street != null) setStreet(street.toUpperCase());
        if (streetType != null) setStreetType(streetType.toUpperCase());
        if (streetDirection != null) setStreetDirection(streetDirection.toUpperCase());
        if (province != null) setProvince(province.toUpperCase());
        if (postalCode != null) setPostalCode(postalCode.toUpperCase());
        if (country != null) setCountry(country.toUpperCase());
    }
    
}
