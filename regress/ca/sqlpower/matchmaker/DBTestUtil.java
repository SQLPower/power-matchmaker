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

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ddl.HSQLDBDDLGenerator;
import ca.sqlpower.matchmaker.dao.hibernate.TestingConnection;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;

/**
 * A collection of useful static methods that you will probably need when
 * developing database-related test cases.
 */
public class DBTestUtil {

    private static final Logger logger = Logger.getLogger(DBTestUtil.class);
    
    /**
     * A cache of the connections we've made so far.
     */
    private static final Map<SPDataSource, Connection> connections =
        new HashMap<SPDataSource, Connection>();

    /**
     * Returns a JDBC connection to the given data source.  Once a successful connection
     * is established to a particular data source, the same one will be returned over
     * and over.  That means that test methods using this facility should make sure they
     * leave the connection in a good default state (not disabled and not closed).
     * 
     * @param dataSource The data source to connect to
     * @return A TestingConnection which is connected to the given data source. Don't close it.
     * @throws InstantiationException If the JDBC driver can't be created
     * @throws IllegalAccessException If the JDBC driver can't be created
     * @throws ClassNotFoundException If the JDBC driver can't be found
     * @throws SQLException If there is a JDBC error (invalid username, password, or database url)
     */
    public static TestingConnection connectToDatabase(JDBCDataSource dataSource)
    throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
        
        if (connections.get(dataSource) == null) {
            logger.info("*** Connecting to Database: "+dataSource);
            Connection mycon = dataSource.createConnection();
            if (mycon == null) {
                throw new SQLException("Couldn't connect to datasource " + dataSource +
                        " (data source returned null connection)");
            }
            connections.put(dataSource, mycon);
        }
        return new TestingConnection(connections.get(dataSource));
    }

    /**
     * Returns a new SPDataSource which is configured to connect to
     * our SQL Server 2000 test database.  The pl schema is in plautotest.
     */
    public static JDBCDataSource getSqlServerDS() { 
    	/*
    	 * Setup information for SQL Server
    	 */
    	PlDotIni<JDBCDataSource> pl = new PlDotIni<JDBCDataSource>(JDBCDataSource.class);
    	try {
    		pl.read(new File("testbed/pl.regression.ini"));
    	} catch (IOException e) {
    		throw new RuntimeException("Could not read from the pl.regression.ini file. " +
    									"Did you remember to set the pl.regression.ini in the testbed folder?", e);
    	}
    	
    	return pl.getDataSource("Test Sql Server");
    }

    /**
     * Returns a new SPDataSource which is configured to connect to
     * our Oracle 8i test database.  The PL schema is in mm_test.
     */
    public static JDBCDataSource getOracleDS() { 
        /*
         * Setup information for Oracle
         */
    	PlDotIni<JDBCDataSource> pl = new PlDotIni<JDBCDataSource>(JDBCDataSource.class);
    	try {
    		pl.read(new File("testbed/pl.regression.ini"));
    	} catch (IOException e) {
    		throw new RuntimeException("Could not read from the pl.regression.ini file", e);
    	}
    	
    	logger.debug("data source type name for Test Oracle is " + pl.getDataSource("Test Oracle").getParentType().getName());
    	
    	return pl.getDataSource("Test Oracle");
    }

    /**
     * Returns a new SPDataSource which is configured to create
     * an in-memory (non persistent) HSQLDB instance.  The PL schema is in pl.
     */
    public static JDBCDataSource getHSQLDBInMemoryDS() {
        final String hsqlUserName = "sa";
        final String hsqlPassword = "";
        final String hsqlUrl = "jdbc:hsqldb:mem:aname";
        
        JDBCDataSource hsqlDataSource = new JDBCDataSource(new PlDotIni<JDBCDataSource>(JDBCDataSource.class));
        hsqlDataSource.getParentType().setJdbcDriver("org.hsqldb.jdbcDriver");
        hsqlDataSource.getParentType().setDDLGeneratorClass(HSQLDBDDLGenerator.class.getName());
        hsqlDataSource.setName("In-memory HSQLDB");
    
        hsqlDataSource.setUser(hsqlUserName);
        hsqlDataSource.setPass(hsqlPassword);
        hsqlDataSource.setPlDbType("hsql");
        hsqlDataSource.setPlSchema("pl");
        hsqlDataSource.setUrl(hsqlUrl);
        
        try {
            Connection con = connectToDatabase(hsqlDataSource);
            createHsqlPlSchemaIfNecessary(con);
            // have to leave connection open (see connectToDatabase())
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        
        return hsqlDataSource;
    }

    /**
     * Creates a schema called "pl" in the given HSQLDB connection.
     */
    private static void createHsqlPlSchemaIfNecessary(Connection con) throws SQLException {
        DatabaseMetaData dbmd = con.getMetaData();
        ResultSet rs = dbmd.getSchemas();
        boolean foundPlSchema = false;
        while (rs.next()) {
            if ("pl".equalsIgnoreCase(rs.getString("TABLE_SCHEM"))) {
                foundPlSchema = true;
            }
        }
        rs.close();
        if (!foundPlSchema) {
            Statement stmt = con.createStatement();
            stmt.executeUpdate("CREATE SCHEMA pl AUTHORIZATION DBA");
            stmt.close();
        }
    }
}
