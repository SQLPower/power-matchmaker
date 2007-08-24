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

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

public class CustomBooleanType implements UserType {

	Boolean b;

    public CustomBooleanType() {

    }

    public static class BooleanUtil {
    	public static String toYNString(Boolean bool){
    		String ynString;

    		if(bool != null){
    			if(bool.booleanValue()){
    				ynString = "Y";
    			} else {
    				ynString = "N";
    			}
    		} else {
    			ynString = "N";
    		}

    		return ynString;
    	}

    	public static Boolean parseYN(String in){
    		if (in != null && (in.equalsIgnoreCase("y") ||
    				in.equalsIgnoreCase("yes") ||
    				in.equalsIgnoreCase("true") ||
    				in.equalsIgnoreCase("1")))
    		{
    			return Boolean.TRUE;
    		}
    		return Boolean.FALSE;
    	}
    }

    public int[] sqlTypes() {

        return new int[] { Types.CHAR };

    }

    public Class returnedClass() {

        return Boolean.class;

    }

    public boolean equals(Object x, Object y) throws HibernateException {

        return (x == y) || (x != null && y != null && (x.equals(y)));

    }

    public Object nullSafeGet(ResultSet inResultSet, String[] names, Object o)

                                            throws HibernateException, SQLException {

        String val = (String)Hibernate.STRING.nullSafeGet(inResultSet, names[0]);


        b = BooleanUtil.parseYN(val);
        return b;
    }

    public void nullSafeSet(PreparedStatement inPreparedStatement, Object o, int i)

                                            throws HibernateException, SQLException {

        String val = BooleanUtil.toYNString((Boolean)o);

        inPreparedStatement.setString(i, val);

    }

    public Object deepCopy(Object o) throws HibernateException {

        if (o==null) return null;

        return new Boolean(((Boolean)o).booleanValue());

    }

    public boolean isMutable() {

        return false;

    }

	public Object replace(Object origional, Object target, Object owner) throws HibernateException {
		return deepCopy(origional);
	}

	public Object assemble(Serializable arg0, Object arg1) throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	public Serializable disassemble(Object arg0) throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	public int hashCode(Object o) throws HibernateException {
		 if (o==null) return 0;
	     return ((Boolean)o).hashCode();
	}




}


