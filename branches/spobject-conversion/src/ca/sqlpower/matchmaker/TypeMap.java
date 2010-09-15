/*
 * Copyright (c) 2008, SQL Power Group Inc.
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

package ca.sqlpower.matchmaker;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.Date;

/**
 * A class that contains a mapping of java.sql data types to Java class types that the
 * MatchMaker munge steps support. At this point, the munge steps support BigDecimal, 
 * Date, Boolean, and String. This simplification of the datatypes is to greatly reduce
 * the number of cases we need to handle in the munge process, and at this point, we do 
 * not feel such fine grained type handling is necessary.
 */
public class TypeMap {
	
    /**
     * Returns the Java class associated with the given SQL type code.
     * 
     * @param type
     *            The type ID number. See {@link Types} for the official list.
     * @return The class for the given type. Defaults to java.lang.String if the
     *         type code is unknown, since almost every SQL type can be
     *         represented as a string if necessary.
     */
	public static Class<?> typeClass(int type) {
        switch (type) {
        case Types.VARCHAR:
        case Types.VARBINARY:
        case Types.STRUCT:
        case Types.REF:
        case Types.OTHER:
        case Types.NULL:
        case Types.LONGVARCHAR:
        case Types.LONGVARBINARY:
        case Types.JAVA_OBJECT:
        case Types.DISTINCT:
        case Types.DATALINK:
        case Types.CLOB:
        case Types.CHAR:
        case Types.BLOB:
        case Types.BINARY:
        case Types.ARRAY:
        default:
            return String.class;

        case Types.TINYINT:
        case Types.SMALLINT:
        case Types.REAL:
        case Types.NUMERIC:
        case Types.INTEGER:
        case Types.FLOAT:
        case Types.DOUBLE:
        case Types.DECIMAL:
        case Types.BIGINT:
            return BigDecimal.class;

        case Types.BIT:
        case Types.BOOLEAN:
            return Boolean.class;
        
        case Types.TIMESTAMP:
        case Types.TIME:
        case Types.DATE:
            return Date.class;
        }
    }
}
