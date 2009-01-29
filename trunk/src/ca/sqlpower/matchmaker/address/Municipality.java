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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

/**
 * A lookup object that contains all official municipality names as well
 * as their aliases/alternate names.
 */
@Entity
public class Municipality {

    @Persistent
    public static class ValidAlternateName {
        
        /**
         * The valid alternate municipality name. This name may appear in a valid
         * address if and only if the address falls inside one of the FSA entries.
         */
        private String name;
        
        /**
         * The postal code regions in which this alternate name is valid.
         */
        private Set<String> fsas;

        public ValidAlternateName() {
            
        }
        
        public ValidAlternateName(String name, Set<String> fsas) {
            this.name = name;
            this.fsas = fsas;
        }

        public ValidAlternateName(String name, String ... fsas) {
            this.name = name;
            this.fsas = new HashSet<String>(Arrays.asList(fsas));
        }

        public String getName() {
            return name;
        }

        public Set<String> getFsas() {
            return fsas;
        }
        
        @Override
        public String toString() {
            return name + " " + fsas;
        }
    }

    /**
     * Creates the key string for a given municipality and province name. This
     * is the string that can be looked up via the Berkeley DB primary key for
     * this class.
     * 
     * @param municipality
     *            The official name of the city, town, or part of a city
     * @param provinceCode
     *            The two-letter for the province or state that the municipality
     *            is in.
     * @return The key, which is composed of the two pieces of information
     *         provided.
     */
    public final static String createKey(String municipality, String provinceCode) {
        return provinceCode + "," + municipality;
    }
    
    /**
     * The unique identifier for the official municipality name. The key
     * values are of the form "PR,OFFICIAL NAME" (two-letter province code
     * followed by a comma then the full official municipality name, in all caps).
     */
    @PrimaryKey
    private String key;

    /**
     * All names for this municipality, including valid alternate names, the
     * official municipality name, and invalid alternate names. If a name in
     * this set is not also in the {@link #validAlternateNames} set, and it's
     * not the official municipality name, it must be replaced by the official
     * name when validating an address.
     */
    @SecondaryKey(relate=Relationship.MANY_TO_MANY)
    private Set<String> alternateNames;
    
    /**
     * The subset of alternate names which are allowed by the postal system. An alternate
     * name that is in the {@link #alternateNames} but not in this set must be replaced
     * by the official municipality name in order for an address record to be considered valid.
     * A name in both sets may appear in a valid address record.
     */
    private Map<String, ValidAlternateName> validAlternateNames;

    /**
     * Default constructor. Does nothing.
     */
    public Municipality() {
        
    }
    
    public Municipality(String province, String officialName,
            Collection<ValidAlternateName> validAlternateNames, Collection<String> additionalAlternateNames) {
        key = province + "," + officialName;
        
        this.validAlternateNames = new HashMap<String, ValidAlternateName>();
        for (ValidAlternateName van : validAlternateNames) {
            this.validAlternateNames.put(van.getName(), van);
        }
        
        alternateNames = new HashSet<String>();
        for (ValidAlternateName van : validAlternateNames) {
            alternateNames.add(van.getName());
        }
        alternateNames.addAll(additionalAlternateNames);
        alternateNames.add(officialName);
    }
    
    public String getProvince() {
        return key.substring(0, 2);
    }
    
    public String getOfficialName() {
        return key.substring(3);
    }
    
    @Override
    public String toString() {
        return "key: " + key + " valid alternates: " + validAlternateNames.values() + "; invalid alternates: " + alternateNames;
    }

    /**
     * Produces a hash code consistent with equals().
     */
    @Override
    public int hashCode() {
        return key.hashCode();
    }

    /**
     * Determines equality based on the municipality's province code and official name.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Municipality other = (Municipality) obj;
        if (key == null) {
            if (other.key != null) {
                return false;
            }
        } else if (!key.equals(other.key)) {
            return false;
        }
        return true;
    }

    /**
     * Determines if the given municipality name is acceptable for this
     * municipality within the given postal code's FSA.
     * 
     * @param municipality
     *            The name to test (not case sensitive). Null is allowed, but
     *            will always result in this method returning false.
     * @param postalCode The postal code to verify against.
     * @return
     */
    public boolean isNameAcceptable(String municipality, String postalCode) {
        if (municipality == null) {
            return false;
        }
        ValidAlternateName van = validAlternateNames.get(municipality.toUpperCase());
        if (van == null) {
            return false;
        }
        
        if (van.getFsas().isEmpty()) {
            return true;
        } else if (postalCode == null || postalCode.length() < 3) {
            // this alternate name is only valid in part of the region, and we were
            // not given a postal code to verify against, so false.
            return false;
        } else if (van.getFsas().contains(postalCode.substring(0, 3).toUpperCase())) {
            return true;
        } else {
            return false;
        }
    }
    
}
