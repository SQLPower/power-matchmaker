/*
 * Copyright (c) 2009, SQL Power Group Inc.
 *
 * This file is part of DQguru
 *
 * DQguru is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DQguru is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.matchmaker.address;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;
import ca.sqlpower.matchmaker.address.Municipality.ValidAlternateName;

public class MunicipalityTest extends TestCase {

    private Municipality northYork;
    private Municipality niagaraOnTheLake;
    
    @Override
    protected void setUp() throws Exception {
        Set<ValidAlternateName> validNames = new HashSet<ValidAlternateName>();
        validNames.add(new ValidAlternateName("DON MILLS", "M3A", "M3B", "M3C", "M4A"));
        validNames.add(new ValidAlternateName("DOWNSVIEW", "M3H", "M3J", "M3K", "M3L", "M3M", "M3N"));
        
        Set<String> otherNames = new HashSet<String>();
        otherNames.add("N.YORK");
        otherNames.add("NEWTOWNBROOK");
        
        northYork = new Municipality("ON", "NORTH YORK", validNames, otherNames);
        
        
        validNames = new HashSet<ValidAlternateName>();
        validNames.add(new ValidAlternateName("NIAGARA-LK"));
        validNames.add(new ValidAlternateName("NIAGARA-ON-THE-LKE"));
        
        otherNames = new HashSet<String>();
        
        niagaraOnTheLake = new Municipality("ON", "NIAGARA ON THE LAKE", validNames, otherNames);
    }
    
    public void testNullMunicipality() throws Exception {
        assertFalse(niagaraOnTheLake.isNameAcceptable(null, null));
        assertFalse(northYork.isNameAcceptable(null, null));
    }
    
    public void testValidAlternateNameNoFSARequirement_pass() throws Exception {
        assertTrue(niagaraOnTheLake.isNameAcceptable("NIAGARA-LK", null));
        assertTrue(niagaraOnTheLake.isNameAcceptable("NIAGARA-LK", ""));
        assertTrue(niagaraOnTheLake.isNameAcceptable("NIAGARA-LK", "anything goes"));
    }

    public void testValidAlternateNameNoFSARequirement_fail() throws Exception {
        assertFalse(niagaraOnTheLake.isNameAcceptable("COMPLETELY WRONG", null));
        assertFalse(niagaraOnTheLake.isNameAcceptable("COMPLETELY WRONG", ""));
        assertFalse(niagaraOnTheLake.isNameAcceptable("COMPLETELY WRONG", "anything goes"));
    }

    public void testValidAlternateNameWithinFSA_pass() throws Exception {
        assertTrue(northYork.isNameAcceptable("DOWNSVIEW", "M3H4G4"));
        assertTrue(northYork.isNameAcceptable("DOWNSVIEW", "M3H"));
    }

    public void testValidAlternateNameWithinFSA_fail() throws Exception {
        assertFalse(northYork.isNameAcceptable("DOWNSVIEW", "M2N7A9"));
        assertFalse(northYork.isNameAcceptable("DOWNSVIEW", ""));
        assertFalse(northYork.isNameAcceptable("DOWNSVIEW", null));
    }
    
    public void testInvalidAlternateName() {
        assertFalse(northYork.isNameAcceptable("N.YORK", null));
        assertFalse(northYork.isNameAcceptable("N.YORK", ""));
        assertFalse(northYork.isNameAcceptable("N.YORK", "anything goes"));
    }
    
    public void testOfficialNameAcceptable() {
        assertTrue(niagaraOnTheLake.isNameAcceptable("NIAGARA ON THE LAKE", "ANYTHING GOES"));
    }
    
    public void testGetProvince() throws Exception {
        assertEquals("ON", northYork.getProvince());
    }
    
    public void testGetOfficialName() throws Exception {
        assertEquals("NORTH YORK", northYork.getOfficialName());
    }
    
    public void testEquals() throws Exception {
        Municipality ny2 = new Municipality(
                "ON", "NORTH YORK",
                new HashSet<ValidAlternateName>(),
                new HashSet<String>());
        assertTrue(ny2.equals(northYork));
    }

    public void testEqualsNull() throws Exception {
        assertFalse(northYork.equals(null));
    }
}
