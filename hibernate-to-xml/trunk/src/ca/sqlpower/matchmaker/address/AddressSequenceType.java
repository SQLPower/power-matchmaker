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

public enum AddressSequenceType {
    ODD(1),
    EVEN(2),
    CONSECUTIVE(3),
    ROUTE_SERVICE_BOX_CONSECUTIVE(4);
    
    private final int code;
    
    private AddressSequenceType(int code) {
        this.code = code;
    }
    
    public static AddressSequenceType forCode(int code) {
        for (AddressSequenceType st : values()) {
            if (st.code == code) {
                return st;
            }
        }
        throw new IllegalArgumentException("Unknown sequence type code: " + code);
    }
}