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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;
import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.address.parse.AddressLexer;
import ca.sqlpower.matchmaker.address.parse.AddressParser;
import ca.sqlpower.util.LevenshteinDistance;

import com.sleepycat.je.DatabaseException;

/**
 * A class for representing any North American address (urban, rural, or post office box).
 * Instances of this class are mutable, because they serve as working areas for the
 * validation and correction logic. Methods are provided so client code can discover any
 * changes made to the address fields by validation and correction code.
 */
public class Address implements AddressInterface {
	private static final Logger logger = Logger.getLogger(Address.class);
	
	/**
	 * The french short form for general delivery. This form should replace
	 * any general delivery term used for a french general delivery. 
	 */
	public static final String GENERAL_DELIVERY_FRENCH = "PR";
	
	/**
	 * The english short form for general delivery. This form should replace
	 * any general delivery term used for a english general delivery. 
	 */
	public static final String GENERAL_DELIVERY_ENGLISH = "GD";
	
	/**
	 * The accepted english translation for a lock box.
	 */
	public static final String LOCK_BOX_ENGLISH = "PO BOX";
	
	/**
	 * The accepted french translation for a lock box.
	 */
	public static final String LOCK_BOX_FRENCH = "CP";
	
	/**
	 * Other possible lock box options that are accepted. Stored here instead of using Levenshtein
	 * as if the distance defined is 2 or more then they will match almost anything. These also
	 * need to be translated to their accepted french or english translation.
	 */
	public static final List<String> PO_BOX_VALID_ALT = new ArrayList<String>(Arrays.asList(new String[]{"BP", "POBX", "BOX"}));
	
	/**
	 * The list of accepted street directions.
	 */
	public static final List<String> STREET_DIRECTIONS = new ArrayList<String>(Arrays.asList(new String[]{"N", "S", "E", "W", "NW", "NE", "SW", "SE", "NO", "SO"}));
	
	/**
	 * The list of long forms of street direction names.
	 */
	public static final List<String> STREET_DIRECTIONS_LONG = new ArrayList<String>(Arrays.asList(new String[]{"NORTH", "EAST", "SOUTH", "WEST", "NORTH-WEST", "NORTH-EAST", "SOUTH-WEST", "SOUTH-EAST", "NORD-OUEST", "SUD-OUEST", "NORD-EST", "SUD-EST"}));
	
	/**
	 * If an address's suite type matches one of these exactly is an accepted symbol.
	 */
	public static final List<String> SUITE_TYPES = new ArrayList<String>(Arrays.asList(new String[]{"APT", "SUITE", "UNIT", "APP", "BUREAU", "UNITE", "PH", "RM", "TH", "TWNHSE", "PIECE", "SALLE"}));
	
	/**
	 * The list of long forms of suite types. These can be matched with minor spelling mistakes.
	 */
	public static final List<String> SUITE_TYPES_LONG = new ArrayList<String>(Arrays.asList(new String[]{"APARTMENT", "SUITE", "UINT", "APPARTEMENT", "BUREAU", "UNITE", "PENTHOUSE", "ROOM", "TOWNHOUSE", "PIECE", "SALLE"}));
	
	/**
	 * The list of short forms of rural route types that are valid.
	 */
	public static final List<String> RURAL_ROUTE_TYPES = new ArrayList<String>(Arrays.asList(new String[]{"RR", "SS", "MR"}));
	
	/**
	 * The map of long forms of accepted rural route types. Can be matched with minor spelling mistakes.
	 * The keys are the accepted long format of the rural route types. The values are what the long formats
	 * map to for a valid address.
	 */
	public static final Map<String, String> RURAL_ROUTE_TYPES_LONG = new HashMap<String, String>();
	
	static {
		RURAL_ROUTE_TYPES_LONG.put("RURAL ROUTE", "RR");
		RURAL_ROUTE_TYPES_LONG.put("ROUTE RURALE", "RR");
		RURAL_ROUTE_TYPES_LONG.put("SUBURBAN SERVICE", "SS");
		RURAL_ROUTE_TYPES_LONG.put("SERVICE SUBURBAN", "SS");
		RURAL_ROUTE_TYPES_LONG.put("MOBILE ROUTE", "MR");
		RURAL_ROUTE_TYPES_LONG.put("RTE", "RR");
	}
	
	/**
	 * List of accepted short forms for DI types.
	 */
	public static final List<String> DELIVERY_INSTALLATION_TYPES = new ArrayList<String>(Arrays.asList(new String[]{"BDP", "CC", "CDO", "CMC", "CPC", "CSP", "LCD", "PDF", "PO", "RPO", "STN", "SUCC"}));
	
	public static final List<String> DELIVERY_INSTALLATION_TYPES_LONG = new ArrayList<String>(Arrays.asList(new String[]{"BUREAU DE POSTE", "CONCESSION COMMERCIALE", "COMMERCIAL DEALERSHIP OUTLET", "COMMUNITY MAIL CENTRE", "CENTRE POSTAL COMMUNAUTAIRE", "COMPTOIR SERVICE POSTAL", "LETTER CARRIER DEPOT", "POSTE DE FACTEURS", "POST OFFICE", "RETAIL POSTAL OUTLET", "STATION", "SUCCURSALE"}));
	
	public static boolean isDeliveryInstallationType(String s) {
		if (s == null) return false;
		if (DELIVERY_INSTALLATION_TYPES.contains(s)) return true;
		for (String diType : DELIVERY_INSTALLATION_TYPES_LONG) {
			if (LevenshteinDistance.computeLevenshteinDistance(s, diType) <= diType.length()/4) return true;
		}
		return false;
	}
	
	public static boolean isRuralRoute(String s) {
		if (s == null) return false;
		if (RURAL_ROUTE_TYPES.contains(s)) return true;
		for (String routeType : RURAL_ROUTE_TYPES_LONG.keySet()) {
			if (LevenshteinDistance.computeLevenshteinDistance(s, routeType) <= routeType.length()/4) return true;
		}
		return false;
	}
	
	/**
	 * Returns the short form of a rural route string. The rural route
	 * string can have some misspelling and be matched. If nothing is matched
	 * the rural route type will be null.
	 */
	public static String getRuralRouteShortForm(String s) {
		if (s == null) return null;
		for (String routeType : RURAL_ROUTE_TYPES_LONG.keySet()) {
			if (LevenshteinDistance.computeLevenshteinDistance(s, routeType) <= routeType.length()/4) return RURAL_ROUTE_TYPES_LONG.get(routeType);
		}
		return null;
	}
	
	public static boolean isSuiteType(String s) {
		if (s == null) return false;
		for (String suiteType : SUITE_TYPES) {
			if (LevenshteinDistance.computeLevenshteinDistance(s, suiteType) <= 2) return true;
		}
		for (String suiteType : SUITE_TYPES_LONG) {
			if (LevenshteinDistance.computeLevenshteinDistance(s, suiteType) <= suiteType.length()/4) return true;
		}
		return false;
	}
	
	public static boolean isSuiteTypeExactMatch(String s) {
		if (SUITE_TYPES.contains(s) || SUITE_TYPES_LONG.contains(s)) return true;
		return false;
	}
	
	public static boolean isStreetDirection(String s) {
		if (STREET_DIRECTIONS.contains(s)) return true;
		for (String direction : STREET_DIRECTIONS_LONG) {
			if (LevenshteinDistance.computeLevenshteinDistance(direction, s) <= direction.length()/4) return true;
		}
		return false;
	}
	
	public static boolean isLockBox(String s) {
		if (s.equals(LOCK_BOX_ENGLISH) || s.equals(LOCK_BOX_FRENCH) || PO_BOX_VALID_ALT.contains(s)) {
			return true;
		}
		if (LevenshteinDistance.computeLevenshteinDistance("CASE POSTALE", s) <= 2) {
			return true;
		}
		return false;
	}
	
	/**
	 * This method will return true if the string given is close to an accepted
	 * string to describe a general delivery. False will be returned otherwise.
	 */
	public static boolean isGeneralDelivery(String gdString) {
		return isGeneralDeliveryEnglish(gdString) || isGeneralDeliveryFrench(gdString);
	}
	
	/**
	 * This method will return true if the string given is close to an accepted
	 * string to describe a general delivery in French. False will be returned otherwise.
	 */
	static boolean isGeneralDeliveryFrench(String gdString) {
		if (gdString.equals(GENERAL_DELIVERY_FRENCH)) {
			return true;
		}
		return false;
	}

	/**
	 * This method will return true if the string given is close to an accepted
	 * string to describe a general delivery in English. False will be returned otherwise.
	 */
	public static boolean isGeneralDeliveryEnglish(String gdString) {
		if (gdString.equals(GENERAL_DELIVERY_ENGLISH)) {
			return true;
		}
		
		if (LevenshteinDistance.computeLevenshteinDistance("GENERAL DELIVERY", gdString) <= 2) {
			return true;
		}
		if (LevenshteinDistance.computeLevenshteinDistance("GEN DELIVERY", gdString) <= 1) {
			return true;
		}
		return false;
		
	}

    public static enum Type {
        URBAN, RURAL, LOCK_BOX, GD
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
     * The street type ordering must be preserved, so if the street type comes before the street
     * name as in Quebec then we must keep track of it.
     */
    private boolean streetTypePrefix = false;
    
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
    
    private String generalDeliveryName;
    
    private String deliveryInstallationType;
    
    private String deliveryInstallationName;
    
    private String lockBoxType;
    
    private Integer lockBoxNumber;
    
    private String ruralRouteType;
    
    private Integer ruralRouteNumber;
    
    /**
     * If the parser cannot correctly separate the address line the whole address line will
     * be placed here to keep the input address line the same for output.
     */
    private String failedParsingString;
    
    /**
     * Creates a new Address record with all fields set to their defaults (usually null).
     */
    public Address() {
        
    }

    /**
     * Creates a copy of the given address.
     * 
     * @param source The address to copy.
     */
    public Address(Address source) {
        country = source.country;
        deliveryInstallationName = source.deliveryInstallationName;
        deliveryInstallationType = source.deliveryInstallationType;
        directionPrefix = source.directionPrefix;
        failedParsingString = source.failedParsingString;
        generalDeliveryName = source.generalDeliveryName;
        lockBoxNumber = source.lockBoxNumber;
        lockBoxType = source.lockBoxType;
        municipality = source.municipality;
        postalCode = source.postalCode;
        province = source.province;
        ruralRouteNumber = source.ruralRouteNumber;
        ruralRouteType = source.ruralRouteType;
        street = source.street;
        streetDirection = source.streetDirection;
        streetNumber = source.streetNumber;
        streetNumberSuffix = source.streetNumberSuffix;
        streetType = source.streetType;
        streetTypePrefix = source.streetTypePrefix;
        suite = source.suite;
        suitePrefix = source.suitePrefix;
        suiteType = source.suiteType;
        type = source.type;
        unparsedAddress = source.unparsedAddress;
        
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
            String country, AddressDatabase addressDatabase) throws RecognitionException {
    	
    	Address a;
    	if (streetAddress != null) {
    		AddressLexer lexer = new AddressLexer(new ANTLRStringStream(streetAddress.toUpperCase()));
	        TokenStream addressTokens = new CommonTokenStream(lexer);
	        AddressParser p = new AddressParser(addressTokens);
	        p.setAddressDatabase(addressDatabase);
	        try {
				p.setPostalCode(postalCode);
			} catch (DatabaseException e) {
				throw new RuntimeException(e);
			}
	        p.address();
	        a = p.getAddress();
	        
	        if (a.getSuite() == null && a.getStreetNumber() != null && a.getStreet() != null &&
					a.getStreet().contains(" ") && isInteger(a.getStreet().substring(0, a.getStreet().indexOf(' ')))) {
				//Parser has trouble with #suite #streetNumber streetName vs #streetNumber #streetName.
				//If the case is #suite #streetNumber streetName the street number ends up in the street name
				a.setSuite(a.getStreetNumber().toString());
				a.setStreetNumber(Integer.parseInt(a.getStreet().substring(0, a.getStreet().indexOf(' '))));
				a.setStreet(a.getStreet().substring(a.getStreet().indexOf(' ') + 1));
				a.setSuitePrefix(true);
			}
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
    
    private static boolean isInteger(String substring) {
    	try {
    		Integer.parseInt(substring);
    		return true;
    	} catch (Exception e) {
    		//string was not an int
    	}
		return false;
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
        sb.append(getAddress());
        
        sb.append("\n");
        sb.append(municipality);
        sb.append(" ").append(province);
        sb.append("  ").append(postalCode);

        sb.append("\n");
        sb.append(country);
        
        return sb.toString();
    }
    
    public String getAddress() {
    	if (type == null) {  //parse failure on address line
    		return failedParsingString;
    	}
    	switch (type) {
    	case URBAN:
    		return getStreetAddress();
    	case RURAL:
    		return getRuralRouteAddress();
    	case LOCK_BOX:
    		return getLockBoxAddress();
    	case GD:
    		return getGeneralDeliveryAddress();
   		default:
   			throw new IllegalStateException("Address type " + type + " is unknown");
    	}
    }

	private String getRuralRouteAddress() {
		StringBuilder sb = new StringBuilder();
		sb.append(ruralRouteType);
		if (ruralRouteNumber != null) {
			sb.append(" ").append(ruralRouteNumber);
		}
		if (street != null) {
			sb.append(" ").append(getStreetAddress());
		} else {
			if (deliveryInstallationType != null) {
				sb.append(" ").append(deliveryInstallationType);
			}
			if (deliveryInstallationName != null) {
				sb.append(" ").append(deliveryInstallationName);
			}
		}
		return sb.toString();
	}

	private String getLockBoxAddress() {
		StringBuilder sb = new StringBuilder();
		sb.append(lockBoxType).append(" ").append(lockBoxNumber);
		if (deliveryInstallationType != null) {
			sb.append(" ").append(deliveryInstallationType);
		}
		if (deliveryInstallationName != null) {
			sb.append(" ").append(deliveryInstallationName);
		}
		return sb.toString();
	}

	public String getGeneralDeliveryAddress() {
		StringBuilder sb = new StringBuilder();
		if (generalDeliveryName != null) {
			sb.append(generalDeliveryName).append(" ");
		}
		if (deliveryInstallationType != null) {
			sb.append(deliveryInstallationType).append(" ");
		}
		if (deliveryInstallationName != null) {
			sb.append(deliveryInstallationName);
		}
		return sb.toString();
	}

	/**
	 * Returns the street address of this {@link Address}. This includes
	 * everything except the municipality, province/state, country, and postal
	 * codes.
	 */
	public String getStreetAddress() {
		StringBuilder sb = new StringBuilder();
		
		if (suite != null && suitePrefix) {
            sb.append(suite).append("-");
        }
        if (streetNumber != null) {
            sb.append(streetNumber);
            if (streetNumberSuffix != null) {
                sb.append(streetNumberSuffix);
            }
            sb.append(" ");
        }
        if (streetDirection != null && directionPrefix) {
            sb.append(streetDirection).append(" ");
        }
        
        if (streetType != null && streetTypePrefix) {
            sb.append(streetType).append(" ");
        }
        sb.append(street);
        if (streetType != null && !streetTypePrefix) {
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
    	logger.debug("setting type to " + type);
        this.type = type;
    }

    public void setStreetTypePrefix(boolean streetTypeAtStart) {
			this.streetTypePrefix = streetTypeAtStart;
		
	}

	public boolean isStreetTypePrefix() {
		return streetTypePrefix;
	}

	public void setGeneralDeliveryName(String generalDeliveryName) {
			this.generalDeliveryName = generalDeliveryName;
		
	}

	public String getGeneralDeliveryName() {
		return generalDeliveryName;
	}

	public void setDeliveryInstallationType(String deliveryInstallationType) {
			this.deliveryInstallationType = deliveryInstallationType;
		
	}

	public String getDeliveryInstallationType() {
		return deliveryInstallationType;
	}

	public void setDeliveryInstallationName(String deliveryInstallationName) {
			this.deliveryInstallationName = deliveryInstallationName;
		
	}

	public String getDeliveryInstallationName() {
		return deliveryInstallationName;
	}

	public void setLockBoxType(String lockBoxType) {
			this.lockBoxType = lockBoxType;
		
	}

	public String getLockBoxType() {
		return lockBoxType;
	}

	public void setLockBoxNumber(Integer lockBoxNumber) {
			this.lockBoxNumber = lockBoxNumber;
		
	}

	public Integer getLockBoxNumber() {
		return lockBoxNumber;
	}

	public void setRuralRouteType(String ruralRouteType) {
			this.ruralRouteType = ruralRouteType;
		
	}

	public String getRuralRouteType() {
		return ruralRouteType;
	}

	public void setRuralRouteNumber(Integer ruralRouteNumber) {
			this.ruralRouteNumber = ruralRouteNumber;
		
	}

	public Integer getRuralRouteNumber() {
		return ruralRouteNumber;
	}

	public void setFailedParsingString(String failedParsingString) {
			this.failedParsingString = failedParsingString;
		
	}

	public String getFailedParsingString() {
		return failedParsingString;
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
