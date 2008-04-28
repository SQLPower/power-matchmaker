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

import org.apache.log4j.Logger;
import org.hibernate.Session;

import ca.sqlpower.sql.SPDataSourceType;


/**
 * A collection of static methods that make it easier to work with Hibernate.
 */
public class HibernateUtil {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(HibernateUtil.class);
    
    /**
     * @deprecated Use a MatchMakerSession instead of this static method.
     */
    public static Session primarySession() {
        throw new UnsupportedOperationException("You have to use MatchMakerSession now.");
    }

    /**
     * This method shouldn't exist. We should put hibernate dialect information in the
     * data source type section of the PL.INI, and not do any cross-referencing.
     */
	public static String guessHibernateDialect(SPDataSourceType dsType){
		if (dsType == null ) throw new IllegalArgumentException("No dialect for a null database type");
		String dbString = dsType.getName().toLowerCase();

		if( dbString.startsWith("oracle")) {
			return "org.hibernate.dialect.OracleDialect";
		} else if (dbString.contains("sql server")) {
			return "org.hibernate.dialect.SQLServerDialect";
		} else if (dbString.contains("db2")) {
			return "org.hibernate.dialect.DB2Dialect";
		} else if (dbString.startsWith("postgres")) {
			return "org.hibernate.dialect.PostgreSQLDialect";
		} else if (dbString.equals("hsqldb")) {
			return "org.hibernate.dialect.HSQLDialect";
		} else if (dbString.contains("derby")) {
			return "org.hibernate.dialect.DerbyDialect";
		} else if (dbString.contains("mysql")) {
			return "org.hibernate.dialect.MySQLDialect";
		} else {
			throw new IllegalArgumentException("I don't know the hibernate dialect for " + dbString);
		}

	}
}
