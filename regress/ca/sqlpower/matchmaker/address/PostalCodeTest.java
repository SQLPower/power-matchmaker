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
import ca.sqlpower.matchmaker.address.Address.Type;

public class PostalCodeTest extends TestCase {

    private PostalCode routeAndGD;

    private PostalCode urbanBuilding;
    
    @Override
    protected void setUp() throws Exception {
        // 2,1,ON,COLLINGWOOD,LISA'S,LANE,3,999999,000000,COLLINGWOOD,RR0003,0,0,0,0,0,0,L9Y3Z2,L9Y2L0 
        routeAndGD = new PostalCode();
        routeAndGD.setAddressType(AddressType.STREET_ADDRESS);
        routeAndGD.setDeliveryInstallationPostalCode("L9Y2L0");
        routeAndGD.setMunicipalityName("COLLINGWOOD");
        routeAndGD.setPostalCode("L9Y3Z2");
        routeAndGD.setProvinceCode("ON");
        routeAndGD.setRouteServiceBoxFromNumber("000000");
        routeAndGD.setRouteServiceBoxToNumber("999999");
        routeAndGD.setStreetName("LISA'S");
        routeAndGD.setStreetTypeCode("LANE");
        
        // A,1,ON,TORONTO,YONGE,ST,,2,004950,,,,0,0,0,0,0,0,004950, ,,NORTH YORK,OFFICE BUILDING,2,M2N6K1,M2N1K0,
        urbanBuilding = new PostalCode();
        urbanBuilding.setAddressType(AddressType.STREET_ADDRESS);
        urbanBuilding.setBuildingName("OFFICE BUILDING");
        urbanBuilding.setDeliveryInstallationPostalCode("M2N1K0");
        urbanBuilding.setDirectoryAreaName("TORONTO");
        urbanBuilding.setMunicipalityName("NORTH YORK");
        urbanBuilding.setPostalCode("M2N6K1");
        urbanBuilding.setProvinceCode("ON");
        urbanBuilding.setStreetAddressFromNumber(4950);
        urbanBuilding.setStreetAddressToNumber(4950);
        urbanBuilding.setStreetName("YONGE");
        urbanBuilding.setStreetTypeCode("ST");

        // TODO: street address, rural route, GD, government, government lock box, LVR, LVR lock box, and postal installation
    }

    public void testContainsBuilding() throws Exception {
        Address sqlpWWHQ = new Address();
        sqlpWWHQ.setMunicipality("NORTH YORK");
        sqlpWWHQ.setProvince("ON");
        sqlpWWHQ.setStreet("YONGE");
        sqlpWWHQ.setStreetNumber(4950);
        sqlpWWHQ.setStreetType("ST");
        sqlpWWHQ.setSuite("2110");
        sqlpWWHQ.setType(Type.URBAN);
        
        assertTrue(urbanBuilding.containsAddress(sqlpWWHQ));
    }

    public void testContainsBuildingWrongStreetNumber() throws Exception {
        Address sqlpWWHQ = new Address();
        sqlpWWHQ.setMunicipality("NORTH YORK");
        sqlpWWHQ.setProvince("ON");
        sqlpWWHQ.setStreet("YONGE");
        sqlpWWHQ.setStreetNumber(4980); // wrong address should trigger negative response
        sqlpWWHQ.setStreetType("ST");
        sqlpWWHQ.setSuite("2110");
        sqlpWWHQ.setType(Type.URBAN);
        
        assertFalse(urbanBuilding.containsAddress(sqlpWWHQ));
    }
    
    public void testContainsBuildingWrongMunicipality() throws Exception {
        Address sqlpWWHQ = new Address();
        sqlpWWHQ.setMunicipality("GREGORY");
        sqlpWWHQ.setProvince("ON");
        sqlpWWHQ.setStreet("YONGE");
        sqlpWWHQ.setStreetNumber(4950);
        sqlpWWHQ.setStreetType("ST");
        sqlpWWHQ.setSuite("2110");
        sqlpWWHQ.setType(Type.URBAN);
        
        assertFalse(urbanBuilding.containsAddress(sqlpWWHQ));
    }

    public void testContainsBuildingWrongProvince() throws Exception {
        Address sqlpWWHQ = new Address();
        sqlpWWHQ.setMunicipality("NORTH YORK");
        sqlpWWHQ.setProvince("NU");
        sqlpWWHQ.setStreet("YONGE");
        sqlpWWHQ.setStreetNumber(4950);
        sqlpWWHQ.setStreetType("ST");
        sqlpWWHQ.setSuite("2110");
        sqlpWWHQ.setType(Type.URBAN);
        
        assertFalse(urbanBuilding.containsAddress(sqlpWWHQ));
    }

    public void testContainsBuildingWrongStreetName() throws Exception {
        Address sqlpWWHQ = new Address();
        sqlpWWHQ.setMunicipality("NORTH YORK");
        sqlpWWHQ.setProvince("ON");
        sqlpWWHQ.setStreet("MAIN");
        sqlpWWHQ.setStreetNumber(4950);
        sqlpWWHQ.setStreetType("ST");
        sqlpWWHQ.setSuite("2110");
        sqlpWWHQ.setType(Type.URBAN);
        
        assertFalse(urbanBuilding.containsAddress(sqlpWWHQ));
    }

    public void testContainsBuildingWrongStreetType() throws Exception {
        Address sqlpWWHQ = new Address();
        sqlpWWHQ.setMunicipality("NORTH YORK");
        sqlpWWHQ.setProvince("ON");
        sqlpWWHQ.setStreet("YONGE");
        sqlpWWHQ.setStreetNumber(4950);
        sqlpWWHQ.setStreetType("RD");
        sqlpWWHQ.setSuite("2110");
        sqlpWWHQ.setType(Type.URBAN);
        
        assertFalse(urbanBuilding.containsAddress(sqlpWWHQ));
    }

}
