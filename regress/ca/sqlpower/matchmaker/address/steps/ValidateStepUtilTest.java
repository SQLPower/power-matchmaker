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

package ca.sqlpower.matchmaker.address.steps;

import java.io.File;

import ca.sqlpower.matchmaker.address.Address;
import ca.sqlpower.matchmaker.address.AddressDatabase;
import ca.sqlpower.matchmaker.address.PostalCode;
import junit.framework.TestCase;

public class ValidateStepUtilTest extends TestCase {
    
    /**
     * Path pointing to the directory containing the address database.
     */
    private AddressDatabase addressDB;
    
    @Override
    protected void setUp() throws Exception {
        String bdbPath = System.getProperty("ca.sqlpower.matchmaker.test.addressDB");
        if (bdbPath == null) {
            throw new RuntimeException(
                    "Please define the system property ca.sqlpower.matchmaker.test.addressDB" +
                    " to point to the directory where your BDB instance is which contains addresses");
        }
        addressDB = new AddressDatabase(new File(bdbPath));
    }
    
    /**
     * Tests that if the street type is in Quebec it should
     * come after a number
     */
    public void testStreetTypeQCAfterNumber() throws Exception {
        Address a = new Address();
        a.setStreet("34");
        a.setStreetType("COUR");
        PostalCode pc = new PostalCode();
        pc.setProvinceCode("QC");
        
        assertFalse(ValidateStepUtil.isStreetTypePrefix(addressDB, a, pc));
    }
    
    /**
     * Tests that if the street type is in Quebec it should
     * come after a number followed by E (ie: 37E).
     */
    public void testStreetTypeQCAfterNumberE() throws Exception {
        Address a = new Address();
        a.setStreet("34E");
        a.setStreetType("COUR");
        PostalCode pc = new PostalCode();
        pc.setProvinceCode("QC");
        
        assertFalse(ValidateStepUtil.isStreetTypePrefix(addressDB, a, pc));
    }
    
    /**
     * Tests that if the street type is in Quebec it should
     * come after a number followed by RE (ie: 37RE);
     */
    public void testStreetTypeQCAfterNumberRE() throws Exception {
        Address a = new Address();
        a.setStreet("34RE");
        a.setStreetType("COUR");
        PostalCode pc = new PostalCode();
        pc.setProvinceCode("QC");
        
        assertFalse(ValidateStepUtil.isStreetTypePrefix(addressDB, a, pc));
    }
    
    /**
     * Tests that if the street type is in Quebec it should
     * come before a generic street name
     */
    public void testStreetTypeQCBeforeString() throws Exception {
        Address a = new Address();
        a.setStreet("A STREET NAME");
        a.setStreetType("COUR");
        PostalCode pc = new PostalCode();
        pc.setProvinceCode("QC");
        
        assertTrue(ValidateStepUtil.isStreetTypePrefix(addressDB, a, pc));
    }

}
