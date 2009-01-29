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
import java.util.HashSet;
import java.util.Set;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;
import com.sleepycat.persist.StoreConfig;

public class AddressDatabase {

    private final File databaseEnvironmentLocation;
    private Environment env;
    private EntityStore store;
    private PrimaryIndex<String, Municipality> municipalityPK;
    private SecondaryIndex<String, String, Municipality> municipalitySK;
    
    private PrimaryIndex<String, PostalCode> postalCodePK;
    private SecondaryIndex<String, String, PostalCode> postalCodeProvince;
    private SecondaryIndex<String, String, PostalCode> postalCodeMunicipality;
    private SecondaryIndex<String, String, PostalCode> postalCodeStreet;
    
    private void open() throws DatabaseException {
        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setAllowCreate(false);
        envConfig.setReadOnly(true);
        env = new Environment(databaseEnvironmentLocation, envConfig);
        
        StoreConfig storeConfig = new StoreConfig();
        storeConfig.setReadOnly(true);
        store = new EntityStore(env, "Municipality", storeConfig);
        
        municipalityPK = store.getPrimaryIndex(String.class, Municipality.class);
        municipalitySK = store.getSecondaryIndex(municipalityPK, String.class, "alternateNames");
        
        postalCodePK = store.getPrimaryIndex(String.class, PostalCode.class);
        postalCodeProvince = store.getSecondaryIndex(postalCodePK, String.class, "provinceCode");
        postalCodeMunicipality = store.getSecondaryIndex(postalCodePK, String.class, "municipalityName");
        postalCodeStreet = store.getSecondaryIndex(postalCodePK, String.class, "streetName");
    }
    
    public void close() throws DatabaseException {
        store.close();
        env.close();
    }
    
    /**
     * @param databaseEnvironmentLocation
     * @throws DatabaseException 
     */
    public AddressDatabase(File databaseEnvironmentLocation) throws DatabaseException {
        this.databaseEnvironmentLocation = databaseEnvironmentLocation;
        open();
    }


    public void correctMunicipality(Address a) {
        try {
            Set<Municipality> municipalities = findMunicipality(a.getMunicipality(), a.getProvince());
            
            if (municipalities.size() == 1) {
                a.setMunicipality(municipalities.iterator().next().getOfficialName());
            }
        } catch (DatabaseException e) {
            throw new RuntimeException(e);
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
     *            municipality
     * @param province
     *            the two-letter province code to restrict the search to. If no
     *            provincial restriction is desired, this parameter can be set
     *            to null.
     * @return The set of municipalities that match the search criteria
     */
    public Set<Municipality> findMunicipality(String name, String province) throws DatabaseException {
        String upperName = name.toUpperCase();
        
        // TODO check if province code is valid, and ignore if not
        
        Set<Municipality> results = new HashSet<Municipality>();
        
        EntityCursor<Municipality> hits = municipalitySK.entities(upperName, true, upperName, true);
        for (Municipality m : hits) {
            if (province == null || province.equals(m.getProvince())) {
                results.add(m);
            }
            System.out.println(m);
        }
        hits.close();
        return results;
    }
}
