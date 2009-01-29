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

import junit.framework.TestCase;

public class AddressTest extends TestCase {

    private Address address;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        address = new Address();
    }
    
    public void testMunicipalityChangedFlag() throws Exception {
        assertFalse(address.isMunicipalityChanged());
        address.setMunicipality("moocow");
        assertTrue(address.isMunicipalityChanged());
    }
    
    public void testResetChangeFlags() throws Exception {
        address.setMunicipality("moocow");

        address.resetChangeFlags();
        
        assertFalse(address.isMunicipalityChanged());
    }
}
