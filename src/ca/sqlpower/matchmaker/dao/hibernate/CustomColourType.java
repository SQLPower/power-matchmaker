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

package ca.sqlpower.matchmaker.dao.hibernate;

import java.awt.Color;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

/**
 * Maps hexidecimal strings representing 24-bit RGB colour values to and from
 * instances of java.awt.Color.
 */
public class CustomColourType implements UserType {

    /**
     * Not implemented.
     */
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return null;
    }

    /**
     * Not implemented.
     */
    public Serializable disassemble(Object value) throws HibernateException {
        return null;
    }

    public Object deepCopy(Object value) throws HibernateException {
        if (value == null) {
            return null;
        } else {
            Color colorValue = (Color) value;
            return new Color(colorValue.getRGB());
        }
    }

    public boolean equals(Object x, Object y) throws HibernateException {
        if (x == null) {
            return y == null;
        } else {
            return x.equals(y);
        }
    }

    public int hashCode(Object x) throws HibernateException {
        if (x == null) {
            return 0;
        } else {
            return x.hashCode();
        }
    }

    public boolean isMutable() {
        return true;
    }

    public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
        String colorHexValue = rs.getString(names[0]);
        if (colorHexValue == null) {
            return null;
        } else {
            return Color.decode(colorHexValue);
        }
    }

    public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index, Types.VARCHAR);
        } else {
            Color colorValue = (Color) value;
            st.setString(index, "#"+Integer.toHexString(colorValue.getRGB() & 0xFFFFFF));
        }
    }

    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return deepCopy(original);
    }

    public Class returnedClass() {
        return Color.class;
    }

    public int[] sqlTypes() {
        return new int[] { Types.VARCHAR };
    }

}
