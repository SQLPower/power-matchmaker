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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.matchmaker.util;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

import ca.sqlpower.matchmaker.Project.ProjectMode;

public class TypeStringToProjectTypeEnum implements UserType {

	public Object assemble(Serializable cached, Object owner)
			throws HibernateException {
		return deepCopy(cached);
	}

	public Object deepCopy(Object value) throws HibernateException {
		return value;
	}

	public Serializable disassemble(Object value) throws HibernateException {
		return (Serializable) deepCopy(value);
	}

	public boolean equals(Object x, Object y) throws HibernateException {
		if (x == null && y == null) return true;
		if (y != null && x!= null && x.getClass() == y.getClass()) {
			return x.equals(y);
		} else {
			return false;
		}
	}

	public int hashCode(Object x) throws HibernateException {
		if ( !(x instanceof ProjectMode)) {
			return 0;
		} else {
			return x.hashCode();
		}
	}
	/**
	 * Enums are not mutable
	 */
	public boolean isMutable() {
		return false;
	}

	public Object nullSafeGet(ResultSet rs, String[] names, Object owner)
			throws HibernateException, SQLException {
		String typeString = rs.getString(names[0]);
		if (typeString != null) {
			return ProjectMode.getTypeByString(typeString);
		} else {
			return null;
		}
	}

	public void nullSafeSet(PreparedStatement st, Object value, int index)
			throws HibernateException, SQLException {
		if (value != null) {
			st.setString(index, ((ProjectMode) value).toString());
		} else {
			st.setString(index, null);
		}
	}

	public Object replace(Object original, Object target, Object owner)
			throws HibernateException {
		return original;
	}

	public Class returnedClass() {
		return ProjectMode.class;
	}

	public int[] sqlTypes() {
		int[] types = {Types.VARCHAR };
		return types;
	}

}
