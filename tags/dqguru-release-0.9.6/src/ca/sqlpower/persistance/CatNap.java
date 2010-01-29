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

package ca.sqlpower.persistance;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;

import ca.sqlpower.sql.SQL;

/**
 * The Catnap class contains utilities for persisting our beans
 * to the database.  It is nothing like Hibernate.
 *
 * It only supports numbers, strings and booleans for one.
 *
 */
public abstract class CatNap {


	public static void load(Connection con, String tableName, Object loadTo, String where) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, SQLException {
		BeanUtils.describe(loadTo);
        Statement stmt = null;
        StringBuffer sql = new StringBuffer();
        try{
        	sql.append("SELECT * FROM "+tableName + " WHERE " + where );
        	sql.append("\n");

        	stmt = con.createStatement();
        	ResultSet rs = stmt.executeQuery(sql.toString());
        	while (rs.next()) {
        		ResultSetMetaData metaData = rs.getMetaData();
				for (int i = 0; i < metaData.getColumnCount(); i++){
        			String beanPropertyName = underscoreToCamelCaps(metaData.getColumnName(i).toLowerCase());

        			BeanUtils.setProperty(loadTo, beanPropertyName,rs.getObject(i));

        		}
        	}
        } catch (SQLException ex) {
        	System.err.println("Catnap: Insert failed. Statement was:\n"+sql);
        	throw ex;
        } finally {
        	try {
        		if (stmt != null) stmt.close();
        	} catch (SQLException ex) {
        		System.err.println("Catnap: Couldn't close the statement.  Damn.  But at least you won a stack trace:");
        		ex.printStackTrace();
        	}
        }
	}

    public static void rename(Connection con, Object newBean, String tableName, String oldWhere) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, SQLException {
    	insert(con,newBean,tableName);
    	delete(con,tableName,oldWhere);
    }


    public static void insert(Connection con, Object bean, String tableName) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, SQLException {
        Statement stmt = null;
        StringBuffer sql = new StringBuffer();
        try {
            Map<String,Object> beanProps = BeanUtils.describe(bean);
            sql.append("INSERT INTO "+tableName+" (");
            boolean first = true;
            for (Map.Entry<String,Object> ent : beanProps.entrySet()) {
                if (first) {
                    first = false;
                } else {
                    sql.append(", ");
                }
                sql.append(camelToUnderscore(ent.getKey()));
            }

            sql.append("\n) VALUES (");
            first = true;
            for (Map.Entry<String, Object> ent : beanProps.entrySet()) {
                if (first) {
                    first = false;
                } else {
                    sql.append(", ");
                }
                if (ent.getValue() instanceof Boolean) {
                	if((Boolean) ent.getValue()) {
                		sql.append(SQL.quote('Y'));
                	} else {
                		sql.append(SQL.quote('N'));
                	}
                } else if(ent.getValue() instanceof Number) {
                	sql.append(ent.getValue());

                } else if (ent.getValue() instanceof String){
                	sql.append(SQL.quote(ent.getValue().toString()));
                }
                // ignore all other types
            }
            sql.append("\n)");

            stmt = con.createStatement();
            stmt.executeUpdate(sql.toString());
        } catch (SQLException ex) {
            System.err.println("Catnap: Insert failed. Statement was:\n"+sql);
            throw ex;
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException ex) {
                System.err.println("Catnap: Couldn't close the statement.  Damn.  But at least you won a stack trace:");
                ex.printStackTrace();
            }
        }
    }

    public static void delete(Connection con, String tableName, String where) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, SQLException {
        Statement stmt = null;
        StringBuffer sql = new StringBuffer();
        try {
            sql.append("DELETE FROM "+tableName);
            sql.append(" WHERE "+where);
            sql.append("\n)");

            stmt = con.createStatement();
            stmt.executeUpdate(sql.toString());
        } catch (SQLException ex) {
            System.err.println("Catnap: Insert failed. Statement was:\n"+sql);
            throw ex;
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException ex) {
                System.err.println("Catnap: Couldn't close the statement.  Damn.  But at least you won a stack trace:");
                ex.printStackTrace();
            }
        }
    }

    public static void update(Connection con, Object bean, String tableName, String where) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, SQLException {
        Statement stmt = null;
        StringBuffer sql = new StringBuffer();
        try {
            Map<String,Object> beanProps = BeanUtils.describe(bean);
            sql.append("UPDATE "+tableName+" SET ");
            boolean first = true;
            for (Map.Entry<String,Object> ent : beanProps.entrySet()) {
                if (first) {
                    first = false;
                } else {
                    sql.append(", ");
                }
                sql.append(camelToUnderscore(ent.getKey()));
                sql.append("=");
                if (ent.getValue() instanceof Boolean) {
                	if((Boolean) ent.getValue()) {
                		sql.append(SQL.quote('Y'));
                	} else {
                		sql.append(SQL.quote('N'));
                	}
                } else if(ent.getValue() instanceof Number) {
                	sql.append(ent.getValue());

                } else if (ent.getValue() instanceof String){
                	sql.append(SQL.quote(ent.getValue().toString()));
                }
                // ignore all other types
            }
            sql.append(" WHERE "+where);
            sql.append("\n)");

            stmt = con.createStatement();
            stmt.executeUpdate(sql.toString());
        } catch (SQLException ex) {
            System.err.println("Catnap: Insert failed. Statement was:\n"+sql);
            throw ex;
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException ex) {
                System.err.println("Catnap: Couldn't close the statement.  Damn.  But at least you won a stack trace:");
                ex.printStackTrace();
            }
        }
    }

    /**
     * Converts camelCaps strings into their equivalent strings having
     * underscores instead of camelCaps.  For example,
     * <tt>richardStallmanCantReadThisBecauseHeIsDyslexic</tt> becomes
     * <tt>richard_stallman_cant_read_this_because_he_is_dyslexic</tt>.
     *
     * @param camel The string to convert.  Must not be null.
     * @return The converted string.
     */
    public static String camelToUnderscore(String camel) {
        StringBuffer result = new StringBuffer(camel.length() * 2);
        for (char ch : camel.toCharArray()) {
            if (Character.isUpperCase(ch)) {
                result.append('_');
                result.append(Character.toLowerCase(ch));
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }

    /**
     * Converts underscore strings into their equivalent strings having
     * camelCaps instead of underscore.  For example,
     * <tt>richard_stallman_cant_read_this_because_he_is_dyslexic</tt> becomes
     * <tt>richardStallmanCantReadThisBecauseHeIsDyslexic</tt>.
     *
     * @param underScore The string to convert.  Must not be null.
     * @return The converted string.
     */
    public static String underscoreToCamelCaps(String underScore) {
        StringBuffer result = new StringBuffer(underScore.length() * 2);
        char[] chars = underScore.toLowerCase().toCharArray();
        for (int i = 0; i < underScore.length(); i++) {
            if ('_' == chars[i]) {
                i++;
                if (i >= chars.length) break;
                result.append(Character.toUpperCase(chars[i]));
            } else {
                result.append(chars[i]);
            }
        }
        return result.toString();
    }

}
