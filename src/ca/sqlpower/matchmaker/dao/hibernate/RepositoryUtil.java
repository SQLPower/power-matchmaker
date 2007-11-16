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

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.ArchitectSessionContext;
import ca.sqlpower.architect.ArchitectSessionContextImpl;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.ddl.DDLStatement;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sql.SQL;
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
    public static final Version MIN_PL_SCHEMA_VERSION = new Version(6, 0, 0);

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
     * @throws ArchitectException If there are problems connecting to or populating the
     * target database, or if there are problems with the built-in Architect project that
     * describes the MatchMaker repository
     * @throws SQLException If there are errors in SQL queries used during this operation
     * @throws IOException If there is a problem reading the built-in Architect project
     * @throws ClassNotFoundException If the DDL Generator for the target database cannot be created
     * @throws IllegalAccessException If the DDL Generator for the target database cannot be created
     * @throws InstantiationException If the DDL Generator for the target database cannot be created
     */
    public static List<String> makeRepositoryCreationScript(SQLObject target) 
    throws SQLException, ArchitectException, IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        SQLDatabase database = ArchitectUtils.getAncestor(target, SQLDatabase.class);
        SQLCatalog catalog = ArchitectUtils.getAncestor(target, SQLCatalog.class);
        SQLSchema schema = ArchitectUtils.getAncestor(target, SQLSchema.class);
        SPDataSource targetDS = database.getDataSource();

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
     * repository is out-of-date, it will be updated if possible. In all other
     * cases, an informative exception will be thrown, explaining what is wrong
     * with the repository.
     * 
     * @param ds The data source that points to the repository to create or update.
     * @throws SQLException If the repository in ds could not be created, or existed
     * but was unfit for use for some reason (the exception message will explain the
     * reason).
     */
    public static void createOrUpdateRepositorySchema(SPDataSource ds) throws RepositoryException {
        logger.debug(
                "Attempting to check MatchMaker repository at " + ds.getName() +
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
                throw new RepositoryException(
                        "Found repository information table, but no " +
                        "version number was present. Perhaps the repository " +
                        "was only partially created, or a recent upgrade attempt " +
                        "failed.");
            }
            
            Version reposVersion;
            try {
                reposVersion = new Version(rs.getString(1));
            } catch (VersionFormatException e) {
                throw new RepositoryException(
                        "Found a repository version number, but it was not correctly-formatted." +
                        " Perhaps someone tried updating it manually and got it wrong?", e);
            }
            int reposDiff = reposVersion.compareTo(MIN_PL_SCHEMA_VERSION);
            if (reposDiff < 0) {
                // in the future, this would be the place to perform the repository upgrade
                throw new RepositoryException(
                        "Your repository is version " + reposVersion + ", which is not " +
                        "compatible with the required version, " + MIN_PL_SCHEMA_VERSION + "." +
                        " A manual repository upgrade is required.");
            } else if (reposDiff > 0) {
                throw new RepositoryException(
                        "Your repository is version " + reposVersion + ", which is not " +
                        "compatible with the required version, " + MIN_PL_SCHEMA_VERSION + "." +
                        " A manual repository upgrade is required.");
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
    private static void createRepositorySchema(SPDataSource ds) throws RepositoryException {
        SQLDatabase db = null;
        try {
            db = new SQLDatabase(ds);
            SQLObject target = db.getChildByNameIgnoreCase(ds.getPlSchema());
            if (target == null) {
                throw new RepositoryException(
                        "Requested repository owner \"" + ds.getPlSchema() + "\" not found. " +
                        "Please create it and try again.");
            }
            Connection con = db.getConnection();
            Statement stmt = con.createStatement();
            List<String> script = makeRepositoryCreationScript(target);
            for (String sql : script) {
                stmt.execute(sql);
            }
            stmt.close();
            con.close();
        } catch (RepositoryException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RepositoryException("Failed to create repository. See nested cause for more details.", ex);
        } finally {
            if (db != null) {
                try {
                    db.disconnect();
                } catch (Exception ex) {
                    logger.error("Couldn't close database! Squishing this exception:", ex);
                }
            }
        }
    }



}
