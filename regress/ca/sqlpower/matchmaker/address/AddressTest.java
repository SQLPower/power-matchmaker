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

import ca.sqlpower.matchmaker.address.Address.Type;
import junit.framework.TestCase;

public class AddressTest extends TestCase {

    /** SQLPower Word Wide Headquarters! */
    private Address sqlpWWHQ;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        sqlpWWHQ = new Address();
        sqlpWWHQ.setCountry("CA");
        sqlpWWHQ.setMunicipality("NORTH YORK");
        sqlpWWHQ.setPostalCode("M2N6K1");
        sqlpWWHQ.setProvince("ON");
        sqlpWWHQ.setStreet("YONGE");
        sqlpWWHQ.setStreetNumber(4950);
        sqlpWWHQ.setStreetType("ST");
        sqlpWWHQ.setSuite("2110");
        sqlpWWHQ.setType(Type.URBAN);
        sqlpWWHQ.resetChangeFlags();
    }
    
    public void testMunicipalityChangedFlag() throws Exception {
        assertFalse(sqlpWWHQ.isMunicipalityChanged());
        sqlpWWHQ.setMunicipality("moocow");
        assertTrue(sqlpWWHQ.isMunicipalityChanged());
    }
    
    public void testResetChangeFlags() throws Exception {
        sqlpWWHQ.setMunicipality("moocow");

        sqlpWWHQ.resetChangeFlags();
        
        assertFalse(sqlpWWHQ.isMunicipalityChanged());
    }
}
