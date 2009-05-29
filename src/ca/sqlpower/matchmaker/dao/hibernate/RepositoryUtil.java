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

package ca.sqlpower.matchmaker.dao.hibernate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.ArchitectSessionContext;
import ca.sqlpower.architect.ArchitectSessionContextImpl;
import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.ddl.DDLStatement;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.JDBCDataSourceType;
import ca.sqlpower.sql.SQL;
import ca.sqlpower.sqlobject.SQLCatalog;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectUtils;
import ca.sqlpower.sqlobject.SQLSchema;
import ca.sqlpower.util.Version;
import ca.sqlpower.util.VersionFormatException;

/**
 * A collection of utilities for dealing with (creating, verifying, upgrading,
 * deleting) a MatchMaker repository database.
 */
public class RepositoryUtil {

    private static final Logger logger = Logger.getLogger(RepositoryUtil.class);
    
    /**
     * The minimum PL Schema version according to that we can work with.
     * Should be checked every time a session is created.
     */
    public static final Version MIN_PL_SCHEMA_VERSION = new Version(6, 0, 3);

    /**
     * Class can not be instantiated.
     */
    private RepositoryUtil() {}
    
    /**
     * Loads the built-in Power*Architect project that defines the MatchMaker
     * repository, then uses a DDL Generator to forward engineer that database
     * structure into the given target database.
     * 
     * @param target The existing database, catalog, or schema in which to create
     * the new repository structures.  The given SQLObject must be capable of
     * holding tables as children.
     * @return The list of SQL Statements that must be executed in the target database
     * to make it into a new MatchMaker repository
     * @throws SQLObjectException If there are problems connecting to or populating the
     * target database, or if there are problems with the built-in Architect project that
     * describes the MatchMaker repository
     * @throws SQLException If there are errors in SQL queries used during this operation
     * @throws IOException If there is a problem reading the built-in Architect project
     * @throws ClassNotFoundException If the DDL Generator for the target database cannot be created
     * @throws IllegalAccessException If the DDL Generator for the target database cannot be created
     * @throws InstantiationException If the DDL Generator for the target database cannot be created
     */
    public static List<String> makeRepositoryCreationScript(SQLObject target) 
    throws SQLException, SQLObjectException, IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        SQLDatabase database = SQLObjectUtils.getAncestor(target, SQLDatabase.class);
        SQLCatalog catalog = SQLObjectUtils.getAncestor(target, SQLCatalog.class);
        SQLSchema schema = SQLObjectUtils.getAncestor(target, SQLSchema.class);
        JDBCDataSource targetDS = database.getDataSource();

        logger.debug("Generating DDL for new repository in data source: " + targetDS.getName());
        logger.debug("Target Catalog: " + catalog + "; Schema: " + schema);
        
        // Load the architect file containing the default repository schema
        ArchitectSessionContext mmRepositoryContext = new ArchitectSessionContextImpl();
        InputStream reposProjectInStream = ClassLoader.getSystemResourceAsStream("ca/sqlpower/matchmaker/dao/hibernate/mm_repository.architect");
        ArchitectSession mmRepositorySession = mmRepositoryContext.createSession(reposProjectInStream);
        reposProjectInStream.close();
        mmRepositorySession.getTargetDatabase().setDataSource(targetDS);
        
        // Add all the DDL statements to the script
        logger.debug("DDL Statements Follow:");
        DDLGenerator ddlg = DDLUtils.createDDLGenerator(targetDS);
        ddlg.setTargetCatalog(catalog == null ? null : catalog.getName());
        ddlg.setTargetSchema(schema == null ? null : schema.getName());
        List<DDLStatement> statements = ddlg.generateDDLStatements(mmRepositorySession.getTargetDatabase().getTables());
        List<String> retval = new ArrayList<String>();
        for (DDLStatement statement : statements) {
            String sql = statement.getSQLText();
            logger.debug(sql);
            retval.add(sql);
        }
        
        // Now get the post-creation script and all all its statements to the script
        logger.debug("Post-Create Script Statements Follow:");
        BufferedReader br = null;
        try {
            InputStream postCreateScript = RepositoryUtil.class.getClassLoader().getResourceAsStream(
                "ca/sqlpower/matchmaker/dao/hibernate/post_create.sql");
            br = new BufferedReader(new InputStreamReader(postCreateScript));
            StringBuilder statement = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                if (";".equals(line.trim())) {
                    String sql = statement.toString();
                    logger.debug(sql);
                    retval.add(sql);
                    statement = new StringBuilder();
                } else if (!line.startsWith("--")) {
                    
                    // Some platforms don't support the USER keyword
                    // This simplistic fix will, of course, break if anything has "USER" as a substring
                    // But we control the contents of this script, so we can ensure that won't happen.
                    line = line.replace("{USER}", SQL.quote(targetDS.getUser()));
                    
                    // These statements will insert the repository catalog and schema names in case the 
                    // repository is not being created in the default catalog and schema.  
                    line = line.replace("{CATALOG}", 
                    		catalog == null || catalog.getName().length() == 0 ? "" : catalog.getName() + "." );
                    line = line.replace("{SCHEMA}", 
                    		schema == null || schema.getName().length() == 0 ? "" : schema.getName() + "." );
                    
                    statement.append(line + "\n");
                }
                line = br.readLine();
            }
        } finally {
            if (br != null) {
                br.close();
            }
        }

        logger.debug("End Of Repository Creation Script");
        
        return retval;
    }

    /**
     * Attempts to evaluate the validity of the MatchMaker repository in the
     * given data source. If the repository appears not to exist at all, it will
     * be created using the script generated by
     * {@link #makeRepositoryCreationScript(SQLObject)}. If there is some
     * semblance of a repository already present, its version will be checked.
     * If it is up-to-date, this method will return without side effects. If the
     * repository is out-of-date or in other cases, an informative exception 
     * will be thrown, explaining what is wrong with the repository.
     * 
     * @param ds The data source that points to the repository to create or update.
     * @throws SQLException If the repository in ds could not be created, or existed
     * but was unfit for use for some reason (the exception message will explain the
     * reason).
     */
    public static void createOrUpdateRepositorySchema(JDBCDataSource ds) throws RepositoryException {
        logger.debug(
                "Attempting to check DQguru repository at " + ds.getName() +
                " with owner " + ds.getPlSchema());
        if (ds.getPlSchema() == null || ds.getPlSchema().length() == 0) {
            throw new RepositoryException(
                    "Data Source \""+ds.getName()+"\" does not" +
                    " specify the repository owner");
        }
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            con = ds.createConnection();
            stmt = con.createStatement();
            try {
                rs = stmt.executeQuery(
                        "SELECT param_value FROM mm_schema_info WHERE param_name='schema_version'");
            } catch (SQLException ex) {
                logger.info("Failed to select from mm_schema_info table. Attempting to create new repository...", ex);
                stmt.close();
                stmt = null;
                con.close();
                con = null;
                createRepositorySchema(ds);
                return;
            }
            if (!rs.next()) {
                throw new RepositoryVersionException("Couldn't determine the repository schema version!",
						new SQLException("There is no schema_version entry in the mm_schema_info table."));
            }
            
            Version reposVersion;
            try {
                reposVersion = new Version(rs.getString(1));
            } catch (VersionFormatException e) {
                throw new RepositoryVersionException(
                        "Invalid repository schema version!", e);
            }
            int reposDiff = reposVersion.compareTo(MIN_PL_SCHEMA_VERSION);
            if (reposDiff != 0) {
                throw new RepositoryVersionException(
                		"Incompatible repository schema version!", reposVersion, MIN_PL_SCHEMA_VERSION);
            }
                
            logger.debug("Repository check passed.");
        } catch (RepositoryException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RepositoryException(
                    "Could not verify, create, or update the repository schema." +
                    " See the nested exception for details.", ex);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    logger.error("Couldn't close result set! Squishing this exception:", ex);
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ex) {
                    logger.error("Couldn't close statement! Squishing this exception:", ex);
                }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException ex) {
                    logger.error("Couldn't close connection! Squishing this exception:", ex);
                }
            }
        }
    }

    /**
     * Uses the {@link #makeRepositoryCreationScript(SQLObject)} method to generate
     * a script, then executes it in the given data source.
     * 
     * @param ds
     * @throws RepositoryException If anything goes wrong while trying to create the schema
     */
    private static void createRepositorySchema(JDBCDataSource ds) throws RepositoryException {
        SQLDatabase db = null;
        Connection con = null;
        Statement stmt = null;
        try {
            db = new SQLDatabase(ds);
            SQLObject target = db.getChildByNameIgnoreCase(ds.getPlSchema());
            if (target == null) {
                throw new RepositoryException(
                        "Requested repository owner \"" + ds.getPlSchema() + "\" not found. " +
                        "Please create it and try again.");
            }
            con = db.getConnection();
            stmt = con.createStatement();
            List<String> script = makeRepositoryCreationScript(target);
            for (String sql : script) {
                stmt.execute(sql);
            }
        } catch (RepositoryException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RepositoryException("Failed to create repository. See nested cause for more details.", ex);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ex) {
                    logger.error("Couldn't close statement! Squishing this exception:", ex);
                }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException ex) {
                    logger.error("Couldn't close connection! Squishing this exception:", ex);
                }
            }
            if (db != null) {
                try {
                    db.disconnect();
                } catch (Exception ex) {
                    logger.error("Couldn't close database! Squishing this exception:", ex);
                }
            }
        }
    }
    
    /**
	 * Upgrades the repository schema to the given required version of the
	 * MatchMaker. If it errors arise, it will attempt to perform a rollback on
	 * the platforms that support the function. Then, it will also invalidate
	 * the repository schema version.
	 * 
	 * @param dbSource
	 *            Database connection to the repository schema.
	 * @param curVer
	 *            Current version of the repository schema.
	 * @param reqVer
	 *            Required version of the MatchMaker.
	 * 
	 * @throws SQLException
	 *             When upgrade scripts execution failed.
	 * @throws IOException
	 *             When upgrade scripts read failed.
	 * @throws SAXException,ParserConfigurationException
	 *             When upgrade scripts parsing failed.
	 * @throws ClassNotFoundException, IllegalAccessException, InstantiationException 
	 *             When creating  ddlg from the datasource fails.
	 */
    public static void upgradeSchema(JDBCDataSource dbSource, Version curVer, Version reqVer) 
    		throws SQLException, ParserConfigurationException, SAXException, IOException,
    		InstantiationException, IllegalAccessException, ClassNotFoundException {
    	logger.debug("Creating DDLG from datasource.");
    	DDLGenerator ddlg = DDLUtils.createDDLGenerator(dbSource);

    	logger.debug("Loading upgrade scripts.");
    	String schemaQualifier = dbSource.getPlSchema() + ".";
    	List<String> upgradeStmts = readUpgradeScripts(schemaQualifier, dbSource.getParentType(), curVer, reqVer);

    	Connection con = null;
    	Statement stmt = null;
    	String lastSql = null;
    	
  		try {
  			con = dbSource.createConnection();
    		stmt = con.createStatement();
    		
    		con.setAutoCommit(false);
  			
    		logger.debug("Executing upgrade sql scripts");
    		for (String sql : upgradeStmts) {
    			lastSql = sql;
    			stmt.execute(lastSql);
    		}
    		
    		logger.debug("Commiting upgrade");
    		con.commit();
    	} catch (SQLException e) {
    		logger.error("Repository schema upgrade failed at:\n" + lastSql, e);

    		if (lastSql != null) {
    			e.setNextException(new SQLException("Last sql statement executed: " + lastSql));

    			boolean invalidateSchema = false;
    			
    			if (ddlg.supportsRollback()) {
    				try {
    					logger.debug("Attempting to rollback upgrade.");
    					con.rollback();
    				} catch (SQLException ex) {
    					logger.error("Rollback failed, adding to exception:", ex);
    					e.setNextException(ex);
    					invalidateSchema = true;
    				}
    			} else {
    				invalidateSchema = true;
    			}
    			
    			if (invalidateSchema) {
    				try {
    					logger.debug("Attempting to invalidate schema version.");
    					invalidateSchemaVersion(dbSource);
    				} catch (SQLException ex) {
    					logger.error("Couldn't invalidate schema version, adding to original exception:", ex);
    					e.setNextException(ex);
    				}
    			}
    		}

    		throw e;
    	} finally {
    		con.setAutoCommit(true);
    		if (stmt != null) {
    			try {
    				stmt.close();
    			} catch (SQLException ex) {
    				logger.error("Couldn't close statement! Squishing this exception:", ex);
    			}
    		}
    		
    		try {
    			con.close();
    		} catch (SQLException ex) {
    			logger.error("Couldn't close database! Squishing this exception:", ex);
    		}
    	}
    }

    /**
	 * Sets the repository schema version to an invalid value.
	 * 
	 * @param dbSource
	 *            Database connection to the repository schema
	 * 
	 * @throws SQLException
	 */
    private static void invalidateSchemaVersion(JDBCDataSource dbSource) throws SQLException {
    	String schemaQualifier = dbSource.getPlSchema() + ".";

    	Connection con = null;
    	Statement stmt = null;

    	try {
    		con = dbSource.createConnection();
    		stmt = con.createStatement();
    		
    		stmt.execute("UPDATE " + schemaQualifier + "MM_SCHEMA_INFO SET PARAM_VALUE = 'INVALID' WHERE PARAM_NAME = 'schema_version'");
    	} catch (SQLException e) {
    		logger.error("Could not invalidate schema version!", e);
    		throw e;
    	} finally {
    		if (stmt != null) {
    			try {
    				stmt.close();
    			} catch (SQLException ex) {
    				logger.error("Couldn't close statement! Squishing this exception:", ex);
    			}
    		}
    		if (con != null) {
    			try {
    				con.close();
    			} catch (SQLException ex) {
    				logger.error("Couldn't close database! Squishing this exception:", ex);
    			}
    		}
    	}
	}
    
	/**
	 * Reads in the list of sql statements that need be ran to upgrade the
	 * current repository schema.
	 * 
	 * @param repositorySchemaQualifier
	 *            The prefix to put on a table name in order to qualify its name
	 *            within the database. For example, if the MatchMaker repository
	 *            is in schema fred, pass in "fred." (trailing dot is
	 *            important).
	 * @param dbSourceType
	 *            Indicates the database type of the repository schema.
	 * @param curVer
	 *            Version of the repository schema.
	 * @param reqVer
	 *            Required version of the MatchMaker.
	 * 
	 * @return The list of sql statements for the upgrade.
	 * 
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    private static List<String> readUpgradeScripts(final String repositorySchemaQualifier, JDBCDataSourceType dbSourceType,
			Version curVer, Version reqVer) throws ParserConfigurationException, SAXException, IOException {
		
    	String scriptResourcePath = "ca/sqlpower/matchmaker/dao/hibernate/upgrade_"+curVer+"_"+reqVer+".xml";
    	// TODO this will not work if we're going more than one version up.
    	// need a way to know which version numbers exist between current and required.
    	InputStream xmlIn = RepositoryUtil.class.getClassLoader().
    		getResourceAsStream(scriptResourcePath);
    	if (xmlIn == null) {
    		throw new UnsupportedOperationException("There is not an upgrade path from version "+curVer+" to "+reqVer+".");
    	}
    	
    	final List<String> sqlStmts = new ArrayList<String>();
		final String targetPlatform = dbSourceType.getName();
		
    	DefaultHandler handler = new DefaultHandler() {
    		
    		/**
    		 * Current text within the &lt;sql&gt; element.
    		 */
    		StringBuilder currentText;
    		
    		/**
    		 * Tracks whether or not the current &lt;sql&gt; element matches
    		 * the platform we're building a script for.
    		 */
    		boolean foundSQL;
    		
    		/**
    		 * The platform-specific sql we found within the current statement.
    		 */
    		String sql;
    		
    		@Override
    		public void startElement(String uri, String localName, String name,	Attributes attributes) throws SAXException {
    			if (name.equals("statement")) {
    				foundSQL = false;
    				sql = null;
    			} else if (name.equals("sql")) {
    				// only consider the first match
    				if (sql == null) {
    					currentText = new StringBuilder();
    					String platform = attributes.getValue("platform");
    					foundSQL = targetPlatform.matches(platform);
    				}
    			} else if (name.equals("table")) {
    				currentText.append(repositorySchemaQualifier).append(attributes.getValue("name"));
    			}
    		}
    		
    		@Override
    		public void endElement(String uri, String localName, String name)
    				throws SAXException {
    			if (name.equals("statement")) {
    				if (sql != null) {
    					sqlStmts.add(sql);
    				}
    			} else if (name.equals("sql")) {
    				if (foundSQL) {
    					sql = currentText.toString();
    					foundSQL = false;
    				}
    			}
    		}
    		
            @Override
            public void characters(char[] ch, int start, int length) throws SAXException {
                if (currentText != null) {
                    currentText.append(ch, start, length);
                }
            }
    		
    	};
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		parser.parse(xmlIn, handler);
		
		if (logger.isDebugEnabled()) {
			logger.debug("Upgrade script from "+curVer+" to "+reqVer+" for "+targetPlatform+":");
			for (String sqlstmt : sqlStmts) {
				logger.debug(sqlstmt);
			}
		}
    	
		return sqlStmts;
    }
}
