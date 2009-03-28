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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;
import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.address.PostalCode.RecordType;
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
public class Address {
	private static final Logger logger = Logger.getLogger(Address.class);
	
	/**
	 * The french short form for general delivery. This form should replace
	 * any general delivery term used for a french general delivery unless
	 * it is an exact match to the long form. 
	 */
	public static final String GENERAL_DELIVERY_FRENCH = "PR";
	
	/**
	 * The english short form for general delivery. This form should replace
	 * any general delivery term used for a english general delivery unless
	 * it is an exact match to the long form. 
	 */
	public static final String GENERAL_DELIVERY_ENGLISH = "GD";
	
	/**
	 * The french long form for general delivery. 
	 */
	public static final String GENERAL_DELIVERY_FRENCH_LONG = "POSTE RESTANTE";
	
	/**
	 * The english long form for general delivery. 
	 */
	public static final String GENERAL_DELIVERY_ENGLISH_LONG = "GENERAL DELIVERY";
	
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
	 * This set contains lists of street directions. All valid street directions are contained in this set.
	 * Street directions grouped in the same list are alternates of each other. This means if an address
	 * has one street direction and the correct street direction is in the same list then the street
	 * direction is valid.
	 */
	public static final Set<List<String>> STREET_DIRECTIONS_SET = new HashSet<List<String>>();
	
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

	/**
	 * This map contains each province code with a list of alternative valid
	 * abbreviations.
	 */
	public static final Map<String, Set<String>> COMMON_PROVINCE_CODE_ABBREV = new HashMap<String, Set<String>>();
	
	/**
	 * Each set in this set is a grouping of valid alternatives. If the street type given
	 * is in the same set as the street type of the correct postal code then the street
	 * type given should not be modified. 
	 */
	public static final Set<List<String>> STREET_TYPE_ALTERNATIVES = new HashSet<List<String>>();
	
	static {
		STREET_DIRECTIONS_SET.add(new ArrayList<String>(Arrays.asList(new String[]{"N", "NORTH", "NORD"})));
		STREET_DIRECTIONS_SET.add(new ArrayList<String>(Arrays.asList(new String[]{"S", "SOUTH", "SUD"})));
		STREET_DIRECTIONS_SET.add(new ArrayList<String>(Arrays.asList(new String[]{"E", "EAST", "EST"})));
		STREET_DIRECTIONS_SET.add(new ArrayList<String>(Arrays.asList(new String[]{"W", "O", "WEST", "OUEST"})));
		STREET_DIRECTIONS_SET.add(new ArrayList<String>(Arrays.asList(new String[]{"NW", "NO", "NORTH-WEST", "NORD-OUEST"})));
		STREET_DIRECTIONS_SET.add(new ArrayList<String>(Arrays.asList(new String[]{"NE", "NORTH-EAST", "NORD-EST"})));
		STREET_DIRECTIONS_SET.add(new ArrayList<String>(Arrays.asList(new String[]{"SW", "SO", "SOUTH-WEST", "SUD-OUEST"})));
		STREET_DIRECTIONS_SET.add(new ArrayList<String>(Arrays.asList(new String[]{"SE", "SOUTH-EAST", "SUD-EST"})));
		
		RURAL_ROUTE_TYPES_LONG.put("RURAL ROUTE", "RR");
		RURAL_ROUTE_TYPES_LONG.put("ROUTE RURALE", "RR");
		RURAL_ROUTE_TYPES_LONG.put("SUBURBAN SERVICE", "SS");
		RURAL_ROUTE_TYPES_LONG.put("SERVICE SUBURBAN", "SS");
		RURAL_ROUTE_TYPES_LONG.put("MOBILE ROUTE", "MR");
		RURAL_ROUTE_TYPES_LONG.put("RTE", "RR");
		
		Set<String> altProvinces = new HashSet<String>();
		altProvinces.add("TE");
		altProvinces.add("NF");
		altProvinces.add("NE");
		altProvinces.add("NEWFOUNDLAND");
		altProvinces.add("NEWFOUNDLAND AND LABRADOR");
		COMMON_PROVINCE_CODE_ABBREV.put("NL", new HashSet<String>(altProvinces));
		altProvinces.clear();
		altProvinces.add("NO");
		altProvinces.add("NOVA SCOTIA");
		COMMON_PROVINCE_CODE_ABBREV.put("NS", new HashSet<String>(altProvinces));
		altProvinces.clear();
		altProvinces.add("IL");
		altProvinces.add("PR");
		altProvinces.add("PRINCE EDWARD ISLAND");
		COMMON_PROVINCE_CODE_ABBREV.put("PE", new HashSet<String>(altProvinces));
		altProvinces.clear();
		altProvinces.add("NE");
		altProvinces.add("NO");
		altProvinces.add("NEW BRUNSWICK");
		COMMON_PROVINCE_CODE_ABBREV.put("NB", new HashSet<String>(altProvinces));
		altProvinces.clear();
		altProvinces.add("PQ");
		altProvinces.add("QU");
		altProvinces.add("QUEBEC");
		COMMON_PROVINCE_CODE_ABBREV.put("QC", new HashSet<String>(altProvinces));
		altProvinces.clear();
		altProvinces.add("ONTARIO");
		COMMON_PROVINCE_CODE_ABBREV.put("ON", new HashSet<String>(altProvinces));
		altProvinces.clear();
		altProvinces.add("MA");
		altProvinces.add("MANITOBA");
		COMMON_PROVINCE_CODE_ABBREV.put("MB", new HashSet<String>(altProvinces));
		altProvinces.clear();
		altProvinces.add("SA");
		altProvinces.add("SL");
		altProvinces.add("SASKATCHEWAN");
		COMMON_PROVINCE_CODE_ABBREV.put("SK", new HashSet<String>(altProvinces));
		altProvinces.clear();
		altProvinces.add("AL");
		altProvinces.add("ALBERTA");
		COMMON_PROVINCE_CODE_ABBREV.put("AB", new HashSet<String>(altProvinces));
		altProvinces.clear();
		altProvinces.add("CO");
		altProvinces.add("BR");
		altProvinces.add("BRITISH COLUMBIA");
		COMMON_PROVINCE_CODE_ABBREV.put("BC", new HashSet<String>(altProvinces));
		altProvinces.clear();
		altProvinces.add("NORTHWEST TERRITORIES");
		COMMON_PROVINCE_CODE_ABBREV.put("NT", new HashSet<String>(altProvinces));
		altProvinces.clear();
		altProvinces.add("YU");
		altProvinces.add("YN");
		altProvinces.add("YUKON");
		COMMON_PROVINCE_CODE_ABBREV.put("YT", new HashSet<String>(altProvinces));
		altProvinces.clear();
		altProvinces.add("NUNAVUT");
		COMMON_PROVINCE_CODE_ABBREV.put("NU", new HashSet<String>(altProvinces));
		altProvinces.clear();
		
		STREET_TYPE_ALTERNATIVES.add(new ArrayList<String>(Arrays.asList(new String[]{"STREET", "ST", "RUE"})));
		STREET_TYPE_ALTERNATIVES.add(new ArrayList<String>(Arrays.asList(new String[]{"AVENUE", "AVE", "AV"})));
		STREET_TYPE_ALTERNATIVES.add(new ArrayList<String>(Arrays.asList(new String[]{"BOULEVARD", "BLVD", "BOUL"})));
	}
	
	/**
	 * List of accepted short forms for DI types.
	 */
	public static final List<String> DELIVERY_INSTALLATION_TYPES = new ArrayList<String>(Arrays.asList(new String[]{"BDP", "CC", "CDO", "CMC", "CPC", "CSP", "LCD", "PDF", "PO", "RPO", "STN", "SUCC"}));
	
	public static final List<String> DELIVERY_INSTALLATION_TYPES_LONG = new ArrayList<String>(Arrays.asList(new String[]{"BUREAU DE POSTE", "CONCESSION COMMERCIALE", "COMMERCIAL DEALERSHIP OUTLET", "COMMUNITY MAIL CENTRE", "CENTRE POSTAL COMMUNAUTAIRE", "COMPTOIR SERVICE POSTAL", "COMPTOIR POSTAL", "LETTER CARRIER DEPOT", "LETTER CARRIER", "POSTE DE FACTEURS", "POST OFFICE", "RETAIL POSTAL OUTLET", "POSTAL OUTLET", "STATION", "SUCCURSALE"}));
	
	/**
	 * Given a string s, and the correct street type type this will return
	 * true if the string s is a valid alternate to type and should not
	 * be altered.
	 */
	public static boolean isStreetTypeValidAlternate(String s, String type) {
		if (s == null) return false;
		for (List<String> alternates : STREET_TYPE_ALTERNATIVES) {
			if (alternates.contains(s) && alternates.contains(type)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Given a province code that is not the standard code for a province this 
	 * method will return a set of all valid province codes that could match
	 * the given code. 
	 */
	public static Set<String> getValidProvinceCodesFromAlt(String code) {
		Set<String> validCodes = new HashSet<String>();
		for (Map.Entry<String, Set<String>> entry : COMMON_PROVINCE_CODE_ABBREV.entrySet()) {
			if (entry.getValue().contains(code)) {
				validCodes.add(entry.getKey());
			}
		}
		return validCodes;
	}
	
	public static boolean isDeliveryInstallationType(String s) {
		if (s == null) return false;
		if (DELIVERY_INSTALLATION_TYPES.contains(s)) return true;
		for (String diType : DELIVERY_INSTALLATION_TYPES_LONG) {
			if (LevenshteinDistance.computeLevenshteinDistance(s, diType) <= Math.round(diType.length()/4.0d)) return true;
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
			if (LevenshteinDistance.computeLevenshteinDistance(s, suiteType) <= suiteType.length()/4) return true;
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
		for (List<String> directions : STREET_DIRECTIONS_SET) {
			for (String direction : directions) {
				if (LevenshteinDistance.computeLevenshteinDistance(direction, s) <= direction.length()/3) return true;
			}
		}
		return false;
	}
	
	/**
	 * Street directions are grouped together if they are equivalent (eg: N, NORTH and NORD).
	 * If two street directions are given with correct spelling and they are in the same 
	 * equivalent group then true is returned as they are alternates of each other. If 
	 * one of them is incorrectly spelled or if they are not equivalent then false is returned.
	 */
	public static boolean isStreetDirectionsEquivalent(String s1, String s2) {
		for (List<String> directions : STREET_DIRECTIONS_SET) {
			if (directions.contains(s1) && directions.contains(s2)) return true;
		}
		return false;
	}
	
	public static boolean isLockBox(String s) {
		if (s == null) return false;
		if (s.equals(LOCK_BOX_ENGLISH) || s.equals(LOCK_BOX_FRENCH) || PO_BOX_VALID_ALT.contains(s)) {
			return true;
		}
		if (LevenshteinDistance.computeLevenshteinDistance("CASE POSTALE", s) <= 2) {
			return true;
		}
		return false;
	}
	
	/**
	 * Checks if the lock box type is an exact match to one of the accepted
	 * and valid lock box types.
	 */
	public static boolean isLockBoxExactMatch(String s) {
		if (s == null) return false;
		if (s.equals(LOCK_BOX_ENGLISH) || s.equals(LOCK_BOX_FRENCH)) {
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
		if (gdString == null) return false;
		if (gdString.equals(GENERAL_DELIVERY_FRENCH)) {
			return true;
		}
		if (LevenshteinDistance.computeLevenshteinDistance(GENERAL_DELIVERY_FRENCH_LONG, gdString) <= GENERAL_DELIVERY_FRENCH_LONG.length()/4) {
			return true;
		}
		return false;
	}

	/**
	 * This method will return true if the string given is close to an accepted
	 * string to describe a general delivery in English. False will be returned otherwise.
	 */
	public static boolean isGeneralDeliveryEnglish(String gdString) {
		if (gdString == null) return false;
		if (gdString.equals(GENERAL_DELIVERY_ENGLISH)) {
			return true;
		}
		
		if (LevenshteinDistance.computeLevenshteinDistance(GENERAL_DELIVERY_ENGLISH_LONG, gdString) <= 2) {
			return true;
		}
		if (LevenshteinDistance.computeLevenshteinDistance("GEN DELIVERY", gdString) <= 1) {
			return true;
		}
		return false;
		
	}
	
	public static boolean isGeneralDeliveryExactMatch(String gdString) {
		if (gdString == null) return false;
		if (gdString.equals(GENERAL_DELIVERY_ENGLISH) || gdString.equals(GENERAL_DELIVERY_FRENCH) || gdString.equals(GENERAL_DELIVERY_FRENCH_LONG) || gdString.equals(GENERAL_DELIVERY_ENGLISH_LONG)) {
			return true;
		}
		return false;
	}
    
    /**
     * This is the original input string, as provided by client code.
     */
    private String unparsedAddressLine1;
    
    /**
     * This is the other original input string, as provided by client code.
     * This being the other input string as being line 2.
     */
    private String unparsedAddressLine2;
    
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
    private RecordType type;
    
    private String generalDeliveryName;
    
    private String deliveryInstallationType;
    
    private String deliveryInstallationName;
    
    private String lockBoxType;
    
    private String lockBoxNumber;
    
    private String ruralRouteType;
    
    private String ruralRouteNumber;
    
    /**
     * This is a boolean for mixed types. For each address the urban street name could come before
     * the rural route number or the rural route number could come before the street name.
     */
    private Boolean urbanBeforeRural = null; 
    
    /**
     * If the parser cannot correctly separate the address line the whole address line will
     * be placed here to keep the input address line the same for output.
     */
    private String failedParsingString;
    
    /**
     * Additional information is stored here if it comes after the valid street address.
     */
    private String additionalInformationSuffix;
    
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
        unparsedAddressLine1 = source.unparsedAddressLine1;
        urbanBeforeRural = source.urbanBeforeRural;
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
     * @throws DatabaseException 
     */
    public static Address parse(String streetAddress, String municipality, String province, String postalCode,
            String country, AddressDatabase addressDatabase) throws RecognitionException, DatabaseException {
    	
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
					a.getStreet().contains(" ") && isInteger(a.getStreet().substring(0, a.getStreet().indexOf(' '))) &&
					!addressDatabase.containsStreetName(a.getStreet())) {
				//Parser has trouble with #suite #streetNumber streetName vs #streetNumber #streetName.
				//If the case is #suite #streetNumber streetName the street number ends up in the street name
				a.setSuite(a.getStreetNumber().toString());
				a.setStreetNumber(Integer.parseInt(a.getStreet().substring(0, a.getStreet().indexOf(' '))));
				a.setStreet(a.getStreet().substring(a.getStreet().indexOf(' ') + 1));
				a.setSuitePrefix(true);
			}
	        
	        if (a.getDeliveryInstallationName() != null) {
				String diName = a.getDeliveryInstallationName().trim();
				while (diName.length() > 0) {
					if (addressDatabase.containsDeliveryInstallationName(diName)) {
						a.setDeliveryInstallationName(diName.trim());
						a.setAdditionalInformationSuffix(a.getDeliveryInstallationName().substring(diName.length()).trim());
						break;
					}
					if (diName.lastIndexOf(' ') < 0) {
						break;
					}
					diName = diName.substring(0, diName.lastIndexOf(' ')).trim();
				}
			}
	        if (a.getStreet() != null) {
				String streetName = a.getStreet().trim();
				while (streetName.length() > 0) {
					if (addressDatabase.containsStreetName(streetName)) {
						a.setStreet(streetName.trim());
						a.setAdditionalInformationSuffix(a.getStreet().substring(streetName.length()).trim());
						break;
					}
					if (streetName.lastIndexOf(' ') < 0) {
						break;
					}
					streetName = streetName.substring(0, streetName.lastIndexOf(' ')).trim();
				}
			}
	        
    	} else {
    		a = new Address();
    	}
    	a.setUnparsedAddressLine1(streetAddress);
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
    		StringBuffer sb = new StringBuffer();
    		if (unparsedAddressLine1 != null) {
    			sb.append(unparsedAddressLine1);
    		}
    		if (unparsedAddressLine1 != null && unparsedAddressLine1.length() > 0 && unparsedAddressLine2 != null && unparsedAddressLine2.length() > 0) {
    			sb.append(" ");
    		}
    		if (unparsedAddressLine2 != null) {
    			sb.append(unparsedAddressLine2);
    		}
    		return sb.toString();
    	}
    	String address;
    	switch (type) {
    	case STREET:
    		address = getStreetAddress();
    		break;
    	case ROUTE:
    		address = getRuralRouteAddress();
    		break;
    	case LOCK_BOX:
    		address = getLockBoxAddress();
    		break;
    	case GENERAL_DELIVERY:
    		address = getGeneralDeliveryAddress();
    		break;
    	case STREET_AND_ROUTE:
    		if (urbanBeforeRural) {
    			address = getStreetAddress() + " " + getRuralRouteAddress();
    			break;
    		} else {
    			address = getRuralRouteAddress() + " " + getStreetAddress();
    			break;
    		}
   		default:
   			throw new IllegalStateException("Address type " + type + " is unknown");
    	}
    	if (additionalInformationSuffix != null && additionalInformationSuffix.trim().length() > 0) {
			address = address + " " + additionalInformationSuffix;
		}
    	return address;
    }

	private String getRuralRouteAddress() {
		StringBuilder sb = new StringBuilder();
		sb.append(ruralRouteType);
		if (ruralRouteNumber != null) {
			sb.append(" ").append(ruralRouteNumber);
		}
		if (deliveryInstallationType != null) {
			sb.append(" ").append(deliveryInstallationType);
		}
		if (deliveryInstallationName != null) {
			sb.append(" ").append(deliveryInstallationName);
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
			sb.append(generalDeliveryName);
		}
		if (deliveryInstallationType != null) {
			sb.append(" ").append(deliveryInstallationType);
		}
		if (deliveryInstallationName != null) {
			sb.append(" ").append(deliveryInstallationName);
		}
		return sb.toString().trim();
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
            if (streetNumberSuffix != null && streetNumberSuffix.trim().length() > 0) {
            	try {
            		Integer.parseInt(streetNumberSuffix.substring(0, 1));
            		sb.append(" ");
            	} catch (NumberFormatException e) {
            		//If the street number suffix does not start with a number place it
            		//directly behind the street number without a space.
            	}
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
        if (streetType != null && streetType.trim().length() > 0 && !streetTypePrefix) {
            sb.append(" ").append(streetType);
        }
        if (streetDirection != null && streetDirection.trim().length() > 0 && !directionPrefix) {
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

    public String getUnparsedAddressLine1() {
        return unparsedAddressLine1;
    }

    public void setUnparsedAddressLine1(String unparsedAddress) {
        this.unparsedAddressLine1 = unparsedAddress;
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

    public RecordType getType() {
        return type;
    }

    public void setType(RecordType type) {
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

	public void setLockBoxNumber(String lockBoxNumber) {
			this.lockBoxNumber = lockBoxNumber;
		
	}

	public String getLockBoxNumber() {
		return lockBoxNumber;
	}

	public void setRuralRouteType(String ruralRouteType) {
			this.ruralRouteType = ruralRouteType;
		
	}

	public String getRuralRouteType() {
		return ruralRouteType;
	}

	public void setRuralRouteNumber(String ruralRouteNumber) {
			this.ruralRouteNumber = ruralRouteNumber;
		
	}

	public String getRuralRouteNumber() {
		return ruralRouteNumber;
	}

	public void setFailedParsingString(String failedParsingString) {
			this.failedParsingString = failedParsingString;
		
	}

	public String getFailedParsingString() {
		return failedParsingString;
	}

	public void setUrbanBeforeRural(Boolean urbanBeforeRural) {
			this.urbanBeforeRural = urbanBeforeRural;
		
	}

	public Boolean isUrbanBeforeRural() {
		return urbanBeforeRural;
	}

	public void setAdditionalInformationSuffix(
			String additionalInformationSuffix) {
			this.additionalInformationSuffix = additionalInformationSuffix;
		
	}

	public String getAdditionalInformationSuffix() {
		return additionalInformationSuffix;
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
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Address)) {
			return false;
		}
		Address a = (Address) obj;
		if ((country == null && a.getCountry() != null) || (country != null && !country.equals(a.getCountry()))) {
			return false;
		}
		if ((deliveryInstallationName == null && a.getDeliveryInstallationName() != null) 
				|| (deliveryInstallationName != null && !deliveryInstallationName.equals(a.getDeliveryInstallationName()))) {
			return false;
		}
		if ((deliveryInstallationType == null && a.getDeliveryInstallationType() != null) 
				|| (deliveryInstallationType != null && !deliveryInstallationType.equals(a.getDeliveryInstallationType()))) {
			return false;
		}
		if (directionPrefix != a.isDirectionPrefix()) {
			return false;
		}
		if ((failedParsingString == null && a.getFailedParsingString() != null) 
				|| (failedParsingString != null && !failedParsingString.equals(a.getFailedParsingString()))) {
			return false;
		}
		if ((generalDeliveryName == null && a.getGeneralDeliveryName() != null) 
				|| (generalDeliveryName != null && !generalDeliveryName.equals(a.getGeneralDeliveryName()))) {
			return false;
		}
		if ((lockBoxNumber == null && a.getLockBoxNumber() != null) 
				|| (lockBoxNumber != null && !lockBoxNumber.equals(a.getLockBoxNumber()))) {
			return false;
		}
		if ((lockBoxType == null && a.getLockBoxType() != null) 
				|| (lockBoxType != null && !lockBoxType.equals(a.getLockBoxType()))) {
			return false;
		}
		if ((municipality == null && a.getMunicipality() != null) 
				|| (municipality != null && !municipality.equals(a.getMunicipality()))) {
			return false;
		}
		if ((postalCode == null && a.getPostalCode() != null) 
				|| (postalCode != null && !postalCode.equals(a.getPostalCode()))) {
			return false;
		}
		if ((province == null && a.getProvince() != null) 
				|| (province != null && !province.equals(a.getProvince()))) {
			return false;
		}
		if ((ruralRouteNumber == null && a.getRuralRouteNumber() != null) 
				|| (ruralRouteNumber != null && !ruralRouteNumber.equals(a.getRuralRouteNumber()))) {
			return false;
		}
		if ((ruralRouteType == null && a.getRuralRouteType() != null) 
				|| (ruralRouteType != null && !ruralRouteType.equals(a.getRuralRouteType()))) {
			return false;
		}
		if ((street == null && a.getStreet() != null) 
				|| (street != null && !street.equals(a.getStreet()))) {
			return false;
		}
		if ((streetDirection == null && a.getStreetDirection() != null) 
				|| (streetDirection != null && !streetDirection.equals(a.getStreetDirection()))) {
			return false;
		}
		if ((streetNumber == null && a.getStreetNumber() != null) 
				|| (streetNumber != null && !streetNumber.equals(a.getStreetNumber()))) {
			return false;
		}
		if ((streetNumberSuffix == null && a.getStreetNumberSuffix() != null) 
				|| (streetNumberSuffix != null && !streetNumberSuffix.equals(a.getStreetNumberSuffix()))) {
			return false;
		}
		if ((streetType == null && a.getStreetType() != null) 
				|| (streetType != null && !streetType.equals(a.getStreetType()))) {
			return false;
		}
		if (streetTypePrefix != a.isStreetTypePrefix()) {
			return false;
		}
		if ((suite == null && a.getSuite() != null) 
				|| (suite != null && !suite.equals(a.getSuite()))) {
			return false;
		}
		if (suitePrefix != a.isSuitePrefix()) {
			return false;
		}
		if ((suiteType == null && a.getSuiteType() != null) 
				|| (suiteType != null && !suiteType.equals(a.getSuiteType()))) {
			return false;
		}
		if ((type == null && a.getType() != null) 
				|| (type != null && !type.equals(a.getType()))) {
			return false;
		}
		if ((urbanBeforeRural == null && a.isUrbanBeforeRural() != null) 
				|| (urbanBeforeRural != null && !urbanBeforeRural.equals(a.isUrbanBeforeRural()))) {
			return false;
		}
		if ((unparsedAddressLine1 == null && a.getUnparsedAddressLine1() != null) 
				|| (unparsedAddressLine1 != null && !unparsedAddressLine1.equals(a.getUnparsedAddressLine1()))) {
			return false;
		}
		if ((unparsedAddressLine2 == null && a.getUnparsedAddressLine2() != null) 
				|| (unparsedAddressLine2 != null && !unparsedAddressLine2.equals(a.getUnparsedAddressLine2()))) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public int hashCode() {
		int result = 17;
		int addValue = 0;
		if (country != null) {
			addValue = country.hashCode();
		}
		result = 31 * result + addValue;
		addValue = 0;
		if (deliveryInstallationName != null) {
			addValue = deliveryInstallationName.hashCode();
		}
		result = 31 * result + addValue;
		addValue = 0;
		if (deliveryInstallationType != null) {
			addValue = deliveryInstallationType.hashCode();
		}
		result = 31 * result + addValue;
		addValue = 0;
		if (directionPrefix) {
			addValue = 1;
		}
		result = 31 * result + addValue;
		addValue = 0;
		if (failedParsingString != null) {
			addValue = failedParsingString.hashCode();
		}
		result = 31 * result + addValue;
		addValue = 0;
		if (generalDeliveryName != null) {
			addValue = generalDeliveryName.hashCode();
		}
		result = 31 * result + addValue;
		addValue = 0;
		if (lockBoxNumber != null) {
			addValue = lockBoxNumber.hashCode();
		}
		result = 31 * result + addValue;
		addValue = 0;
		if (lockBoxType != null) {
			addValue = lockBoxType.hashCode();
		}
		result = 31 * result + addValue;
		addValue = 0;
		if (municipality!= null) {
			addValue = municipality.hashCode();
		}
		result = 31 * result + addValue;
		addValue = 0;
		if (postalCode != null) {
			addValue = postalCode.hashCode();
		}
		result = 31 * result + addValue;
		addValue = 0;
		if (province != null) {
			addValue = province.hashCode();
		}
		result = 31 * result + addValue;
		addValue = 0;
		if (ruralRouteNumber != null) {
			addValue = ruralRouteNumber.hashCode();
		}
		result = 31 * result + addValue;
		addValue = 0;
		if (ruralRouteType != null) {
			addValue = ruralRouteType.hashCode();
		}
		result = 31 * result + addValue;
		addValue = 0;
		if (street != null) {
			addValue = street.hashCode();
		}
		result = 31 * result + addValue;
		addValue = 0;
		if (streetDirection != null) {
			addValue = streetDirection.hashCode();
		}
		result = 31 * result + addValue;
		addValue = 0;
		if (streetNumber != null) {
			addValue = streetNumber.hashCode();
		}
		result = 31 * result + addValue;
		addValue = 0;
		if (streetNumberSuffix != null) {
			addValue = streetNumberSuffix.hashCode();
		}
		result = 31 * result + addValue;
		addValue = 0;
		if (streetType != null) {
			addValue = streetType.hashCode();
		}
		result = 31 * result + addValue;
		addValue = 0;
		if (streetTypePrefix) {
			addValue = 1;
		}
		result = 31 * result + addValue;
		addValue = 0;
		if (suite != null) {
			addValue = suite.hashCode();
		}
		result = 31 * result + addValue;
		addValue = 0;
		if (suitePrefix) {
			addValue = 1;
		}
		result = 31 * result + addValue;
		addValue = 0;
		if (suiteType != null) {
			addValue = suiteType.hashCode();
		}
		result = 31 * result + addValue;
		addValue = 0;
		if (type != null) {
			addValue = type.hashCode();
		}
		result = 31 * result + addValue;
		addValue = 0;
		if (urbanBeforeRural != null) {
			addValue = urbanBeforeRural.hashCode();
		}
		result = 31 * result + addValue;
		addValue = 0;
		if (unparsedAddressLine1 != null) {
			addValue = unparsedAddressLine1.hashCode();
		}
		result = 31 * result + addValue;
		addValue = 0;
		if (unparsedAddressLine2 != null) {
			addValue = unparsedAddressLine2.hashCode();
		}
		result = 31 * result + addValue;
		
		return result;
	}
	
	/**
	 * An 'empty' address is one that was loaded from the address result table
	 * with all null fields. This is currently being used to determine if a
	 * particular {@link AddressResult} has an empty output address.
	 */
	public boolean isEmptyAddress() {
		logger.debug("country:" + country + "municipality:" + municipality + "postalcode:" + postalCode + "province:" + province);
		logger.debug("street:" + street + "streetDirection:" + streetDirection + "streetNumberSuffix:" + streetNumberSuffix);
		logger.debug("streetNumber:" + streetNumber + "streetType:" + streetType + "suite:" + suite);
		return country == null &&
				municipality == null &&
				postalCode == null &&
				province == null &&
				street == null &&
				streetDirection == null &&
				streetNumberSuffix == null &&
				(streetNumber == null || streetNumber == 0) &&
				streetType == null &&
				suite == null;
	}
	
	static Address getEmptyAddress() {
		Address a = new Address();
		a.setStreetNumber(Integer.valueOf(0));
		return a;
	}

	public void setUnparsedAddressLine2(String unparsedAddressLine2) {
			this.unparsedAddressLine2 = unparsedAddressLine2;
		
	}

	public String getUnparsedAddressLine2() {
		return unparsedAddressLine2;
	}
}
