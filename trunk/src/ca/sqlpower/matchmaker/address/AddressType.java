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

public enum AddressType {
    STREET_ADDRESS(1),
    INSTALLATION_ADDRESS(2);
    
    private final int code;
    
    AddressType(int code) {
        this.code = code;
    }
    
    public static AddressType forCode(int code) {
        if (code == 1) {
            return STREET_ADDRESS;
        } else if (code == 2) {
            return INSTALLATION_ADDRESS;
        } else {
            throw new IllegalArgumentException("Unknown address type code: " + code);
        }
    }
    
    public int getCode() {
        return code;
    }
}