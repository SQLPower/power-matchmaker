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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

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
    
    private final File databaseEnvironmentLocation;
    private Environment env;
    private final List<EntityStore> storesToClose = new ArrayList<EntityStore>();
    private PrimaryIndex<String, Municipality> municipalityPK;
    private SecondaryIndex<String, String, Municipality> municipalitySK;
    
    PrimaryIndex<String, PostalCode> postalCodePK;
    SecondaryIndex<String, String, PostalCode> postalCodeProvince;
    SecondaryIndex<String, String, PostalCode> postalCodeMunicipality;
    SecondaryIndex<String, String, PostalCode> postalCodeStreet;
    
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
        
        store = new EntityStore(env, "PostalCode", storeConfig);
        storesToClose.add(store);
        postalCodePK = store.getPrimaryIndex(String.class, PostalCode.class);
        postalCodeProvince = store.getSecondaryIndex(postalCodePK, String.class, "provinceCode");
        postalCodeMunicipality = store.getSecondaryIndex(postalCodePK, String.class, "municipalityName");
        postalCodeStreet = store.getSecondaryIndex(postalCodePK, String.class, "streetName");
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

    /**
     * Returns all known information abotu the given postal code.
     * 
     * @param postalCode
     *            The code. The lookup is not case sensitive, and all
     *            non-alplanumeric characters are ignored, so the arguments
     *            "A1A1A1" and "a1a 1A1" are equivalent. This argument must not
     *            be null.
     * @return The PostalCode object for the given code, or null if the given
     *         code does not exist in this database.
     * @throws DatabaseException if the lookup fails due to database problems
     */
    public PostalCode findPostalCode(String postalCode) throws DatabaseException {
        String pcNormalized = postalCode.toUpperCase().replaceAll("[^A-Z0-9]", "");
        PostalCode pc = postalCodePK.get(pcNormalized);
        return pc;
    }
}
