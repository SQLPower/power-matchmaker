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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import ca.sqlpower.util.LevenshteinDistance;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;
import com.sleepycat.persist.StoreConfig;

public class AddressDatabase {

    private static final Logger logger = Logger.getLogger(AddressDatabase.class);

	public static final String QUEBEC_PROVINCE_CODE = "QC";
    
    private final File databaseEnvironmentLocation;
    private Environment env;
    private final List<EntityStore> storesToClose = new ArrayList<EntityStore>();
    private PrimaryIndex<String, Municipality> municipalityPK;
    private SecondaryIndex<String, String, Municipality> municipalitySK;
    
    private PrimaryIndex<String, LargeVolumeReceiver> largeVolumeReceiverPK;
    
    PrimaryIndex<Long, PostalCode> postalCodePK;
    SecondaryIndex<String, Long, PostalCode> postalCodeSK;
    SecondaryIndex<String, Long, PostalCode> postalCodeProvince;
    SecondaryIndex<String, Long, PostalCode> postalCodeMunicipality;
    SecondaryIndex<String, Long, PostalCode> postalCodeStreet;
    private SecondaryIndex<String, Long, PostalCode> postalStreetTypeCode;
    SecondaryIndex<Integer, Long, PostalCode> postalCodeRecordType;
    private SecondaryIndex<String, Long, PostalCode> postalCodeDIName;
    
    /**
     * This map stores all valid address types (like STREET and AVENUE) to their short form stored
     * in the database.
     */
    private final Map<String, String> validAddressTypes = new HashMap<String, String>();
    
    /**
     * This set stores all of the address types used in Quebec. This is needed as address types in
     * french come before the street name instead of after it.
     */
    private final Set<String> frenchAddressTypes = new HashSet<String>();
    
    private void open() throws DatabaseException {
        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setAllowCreate(false);
        envConfig.setReadOnly(true);
        env = new Environment(databaseEnvironmentLocation, envConfig);
        
        StoreConfig storeConfig = new StoreConfig();
        storeConfig.setReadOnly(true);
        
        EntityStore store = new EntityStore(env, "Municipality", storeConfig);
        storesToClose.add(store);
        municipalityPK = store.getPrimaryIndex(String.class, Municipality.class);
        municipalitySK = store.getSecondaryIndex(municipalityPK, String.class, "alternateNames");
        
        store = new EntityStore(env, "LargeVolumeReceiver", storeConfig);
        storesToClose.add(store);
        largeVolumeReceiverPK = store.getPrimaryIndex(String.class, LargeVolumeReceiver.class);
        
        store = new EntityStore(env, "PostalCode", storeConfig);
        storesToClose.add(store);
        postalCodePK = store.getPrimaryIndex(Long.class, PostalCode.class);
        postalCodeSK = store.getSecondaryIndex(postalCodePK, String.class, "postalCode");
        postalCodeProvince = store.getSecondaryIndex(postalCodePK, String.class, "provinceCode");
        postalCodeMunicipality = store.getSecondaryIndex(postalCodePK, String.class, "municipalityName");
        postalCodeStreet = store.getSecondaryIndex(postalCodePK, String.class, "streetName");
        postalStreetTypeCode = store.getSecondaryIndex(postalCodePK, String.class, "streetTypeCode");
        postalCodeRecordType = store.getSecondaryIndex(postalCodePK, Integer.class, "recordTypeNumber");
        postalCodeDIName = store.getSecondaryIndex(postalCodePK, String.class, "deliveryInstallationQualifierName");
    }

    /**
     * Tries to close all entity stores and then the BDB environment. Traps and
     * logs exceptions along the way, because otherwise the larger pieces such
     * as the whole environment will not get closed.
     * <p>
     * Note to people finding this code: This is <i>not</i> in line with our
     * standard policy for handling exceptions (which is that you have to let
     * them propagate or show them to a user). Don't take this as an example of
     * the right way to deal with an exception in MatchMaker code. Think of it
     * as an exception to the rule about exceptions (ha ha, pun).
     */
    public void close() {
        for (EntityStore store : storesToClose) {
            try {
                store.close();
            } catch (Exception ex) {
                logger.error("Failed to close entity store " + store + ". Squishing this exception:", ex);
            }
        }
        
        storesToClose.clear();
        
        try {
            env.close();
        } catch (Exception ex) {
            logger.error("Failed to close BDB environment " + env + ". Squishing this exception:", ex);
        }
    }
    
    /**
     * @param databaseEnvironmentLocation
     * @throws DatabaseException 
     */
    public AddressDatabase(File databaseEnvironmentLocation) throws DatabaseException {
        this.databaseEnvironmentLocation = databaseEnvironmentLocation;
        open();
        loadFullAddressTypes();
        loadFrenchStreetTypes();
    }
    
    private void loadFrenchStreetTypes() throws DatabaseException {
    	BufferedReader reader = null;
    	try {
    		reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("src/ca/sqlpower/matchmaker/address/StreetAddressTypes-French.property"))));
    		String line = reader.readLine();
    		while (line != null) {
    			frenchAddressTypes.add(line);
    			line = reader.readLine();
    		}
    	} catch (FileNotFoundException e) {
    		throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
    		if (reader != null) {
    			try {
    				reader.close();
    			} catch (Exception e) {
    				//Squishing exception to allow any other exception to make it through.
    			}
    		}
    	}
    }

    private void loadFullAddressTypes() throws DatabaseException {
    	logger.debug("There are " + postalStreetTypeCode.map().keySet().size() + " keys");
    	for (String addressType : postalStreetTypeCode.map().keySet()) {
    		validAddressTypes.put(addressType, addressType);
    		logger.debug("Inserting address type from database " + addressType);
    	}
    	BufferedReader reader = null;
    	try {
    		reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("src/ca/sqlpower/matchmaker/address/StreetAddressType.property"))));
    		String line = reader.readLine();
    		while (line != null) {
    			String[] mapping = line.split("=");
    			if (mapping.length != 2) {
    				throw new IllegalStateException("Street Address Type property file has an invalid line at " + line + ". " +
    						"Each line should map one alternative street type spelling to the accepted abbreviation.");
    			}
    			validAddressTypes.put(mapping[0], mapping[1]);
    			logger.debug("Inserting address type from file " + mapping[0] + " to " + mapping[1]);
    			line = reader.readLine();
    		}
    	} catch (FileNotFoundException e) {
    		throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
    		if (reader != null) {
    			try {
    				reader.close();
    			} catch (Exception e) {
    				//Squishing exception to allow any other exception to make it through.
    			}
    		}
    	}
	}

    /**
     * Returns the set of Municipalities that have the given name as their
     * official name or one of their alternates, restricting the results to the
     * given province (by 2-letter province code). If the given province code is
     * null or invalid (TODO), all matching municipalities will be returned.
     * 
     * @param name
     *            The official, valid alternate, or invalid alternate name of a
     *            municipality. If null, no matches will be returned (you get an
     *            empty set).
     * @param province
     *            the two-letter province code to restrict the search to. If no
     *            provincial restriction is desired, this parameter can be set
     *            to null.
     * @return The set of municipalities that match the search criteria
     */
    public Set<Municipality> findMunicipality(String name, String province) throws DatabaseException {
        if (name == null) {
            return Collections.emptySet();
        }
        String upperName = name.toUpperCase().trim();
        
        // TODO check if province code is valid, and ignore if not
        
        Set<Municipality> results = new HashSet<Municipality>();
        
        EntityCursor<Municipality> hits = municipalitySK.entities(upperName, true, upperName, true);
        for (Municipality m : hits) {
            if (province == null || province.equals(m.getProvince())) {
                results.add(m);
            }
            logger.debug("Found municipality: " + m);
        }
        hits.close();
        return results;
    }
    
    public boolean containsStreetType(String streetType) {
    	logger.debug("Looking for street type " + streetType);
    	if (streetType == null) return false;
    	for (String type : validAddressTypes.keySet()) {
    		if (LevenshteinDistance.computeLevenshteinDistance(streetType, type) <= type.length()/3) {
    			return true;
    		}
    	}
    	return false;
    }

	/**
	 * Returns true if the street name given is an exact match to a street in
	 * the database. Returns false otherwise.
	 */
    public boolean containsStreetName(String streetName) {
    	if (streetName == null) return false;
    	EntityCursor<PostalCode> cursor = null;
    	try {
			cursor = postalCodeStreet.entities(streetName, true, streetName, true);
			if (cursor.next() != null) {
				return true;
			}
		} catch (DatabaseException e) {
			throw new RuntimeException(e);
		} finally {
			if (cursor != null) {
				try {
					cursor.close();
				} catch (DatabaseException e) {
					//squishing exception to allow actual exception to go through.
				}
			}
		}
		return false;
    }
    
    public boolean isStreetTypeFrench(String streetType) {
    	return frenchAddressTypes.contains(validAddressTypes.get(streetType));
    }
    
    /**
     * Returns the short form of the given street type. This will convert street types
     * like STREET to ST and BOULEVARD to BLVD. This will return null if the street type
     * given is not a valid street type.
     */
    public String getShortFormStreetType(String streetType) {
    	return validAddressTypes.get(streetType);
    }

    /**
     * Returns all known information about the given postal code.
     * 
     * @param postalCode
     *            The code. The lookup is not case sensitive, and all
     *            non-alplanumeric characters are ignored, so the arguments
     *            "A1A1A1" and "a1a 1A1" are equivalent. This argument must not
     *            be null.
     * @return The PostalCode objects for the given code, or an empty set if the given
     *         code does not exist in this database.
     * @throws DatabaseException if the lookup fails due to database problems
     */
    public Set<PostalCode> findPostalCode(String postalCode) throws DatabaseException {
        String pcNormalized = postalCode.toUpperCase().replaceAll("[^A-Z0-9]", "");
        EntityCursor<PostalCode> pcCursor = postalCodeSK.entities(pcNormalized, true, pcNormalized, true);
        Set<PostalCode> postalCodes = new HashSet<PostalCode>();
        for (PostalCode pc : pcCursor) {
        	postalCodes.add(pc);
        }
        pcCursor.close();
        return postalCodes;
    }

    /**
     * Returns true if the postal code is a large volume receiver postal code.
     * False otherwise.
     */
	public boolean containsLVRPostalCode(String postalCode) throws DatabaseException {
		if (postalCode == null) return false;
		EntityCursor<LargeVolumeReceiver> cursor = largeVolumeReceiverPK.entities(postalCode, true, postalCode, true);
		try {
			if (cursor.next() != null) {
				return true;
			}
		} finally {
			cursor.close();
		}
		return false;
	}
	
	/**
	 * Only one large volume receiver should be found for each postal code.
	 * If there is no large volume receiver for the given postal code then
	 * the LVR returned will be null.
	 */
	public LargeVolumeReceiver findLargeVolumeReceiver(String postalCode) throws DatabaseException {
		if (postalCode == null) return null;
		EntityCursor<LargeVolumeReceiver> cursor = largeVolumeReceiverPK.entities(postalCode, true, postalCode, true);
		LargeVolumeReceiver lvr = cursor.next();
		cursor.close();
		return lvr;
	}
	
	/**
	 * Returns true if the database has a delivery installation with
	 * the given name. Returns false otherwise.
	 */
	public boolean containsDeliveryInstallationName(String diName) throws DatabaseException {
		if (diName == null) return false;
		EntityCursor<PostalCode> cursor = postalCodeDIName.entities(diName, true, diName, true);
		try {
			if (cursor.next() != null) return true;
		} finally {
			cursor.close();
		}
		return false;
	}
}
