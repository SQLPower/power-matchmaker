/*
 * Copyright (c) 2007, SQL Power Group Inc.
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.i 
 */

package ca.sqlpower.matchmaker.util;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

/**
 * A Hibernate UserType which can save and load a ViewSpec object.
 * <p>
 * FIXME: this user type is not complete because it ignores the catalog, schema, and viewName properties.
 */
public class StringsToViewSpec implements UserType {
	
	public Object assemble(Serializable cached, Object owner) throws HibernateException {
		return deepCopy(cached);
	}

	public Object deepCopy(Object value) throws HibernateException {
		if ( !( value instanceof ViewSpec) ) {
			return null;
		}
		ViewSpec oldValue = (ViewSpec)value;
		return new ViewSpec(oldValue.getSelect(),
				oldValue.getFrom(),oldValue.getWhere());
	}

	public Serializable disassemble(Object value) throws HibernateException {
		return (Serializable)deepCopy(value);
	}

	public boolean equals(Object x, Object y) throws HibernateException {
		if ( x == null && y != null ) {
			return false;
		} else if ( x != null && y == null ) {
			return false;
		} else if ( x == null && y == null ) {
			return true;
		} else {
			return x.equals(y);
		}
	}

	public int hashCode(Object x) throws HibernateException {
		return (x==null)?0:x.hashCode();
	}

	public boolean isMutable() {
		return true;
	}

	public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
		if (names.length != sqlTypes().length ) {
            throw new HibernateException(
                    "The column name list for this user type is not" +
                    " the same length as the ViewSpec properties setting.  " +
                    "(names.length="+names.length+"; number of properties="
                    +sqlTypes().length+")");
        }
        if (rs.getString(names[0]) == null &&
                rs.getString(names[1]) == null &&
                rs.getString(names[2]) == null) {
            return null;
        } else {
            return new ViewSpec(rs.getString(names[0]),
                                rs.getString(names[1]),
                                rs.getString(names[2]));
        }
	}

	public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
		if ( value == null ) {
			st.setString(index,null);
			st.setString(index+1,null);
			st.setString(index+2,null);
		} else {
			st.setString(index,((ViewSpec)value).getSelect());
			st.setString(index+1,((ViewSpec)value).getFrom());
			st.setString(index+2,((ViewSpec)value).getWhere());
		}
		
	}

	public Object replace(Object original, Object target, Object owner) throws HibernateException {
		return deepCopy(original);
	}

	public Class returnedClass() {
		return ViewSpec.class;
	}

	public int[] sqlTypes() {
		int [] type = {Types.VARCHAR,Types.VARCHAR,Types.VARCHAR};
		return type;
	}
}
