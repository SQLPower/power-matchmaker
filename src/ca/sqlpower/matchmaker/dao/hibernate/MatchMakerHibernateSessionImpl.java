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

import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.matchmaker.FolderParent;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.MatchMakerConfigurationException;
import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchMakerSessionContext;
import ca.sqlpower.matchmaker.MatchMakerTranslateGroup;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.TableMergeRules;
import ca.sqlpower.matchmaker.TranslateGroupParent;
import ca.sqlpower.matchmaker.WarningListener;
import ca.sqlpower.matchmaker.dao.MungeProcessDAO;
import ca.sqlpower.matchmaker.dao.ProjectDAO;
import ca.sqlpower.matchmaker.dao.MatchMakerDAO;
import ca.sqlpower.matchmaker.dao.MatchMakerTranslateGroupDAO;
import ca.sqlpower.matchmaker.dao.PlFolderDAO;
import ca.sqlpower.matchmaker.dao.TableMergeRuleDAO;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.util.HibernateUtil;
import ca.sqlpower.security.PLSecurityException;
import ca.sqlpower.security.PLSecurityManager;
import ca.sqlpower.security.PLUser;
import ca.sqlpower.sql.DefaultParameters;
import ca.sqlpower.sql.PLSchemaException;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.util.UnknownFreqCodeException;
import ca.sqlpower.util.Version;
import ca.sqlpower.util.VersionFormatException;

/**
 * An implementation of MatchMakerSession that uses Hibernate to
 * look up and store the business objects.
 */
public class MatchMakerHibernateSessionImpl implements MatchMakerHibernateSession {

    private static final Logger logger = Logger.getLogger(MatchMakerHibernateSessionImpl.class);

     /**
     * The ID of the next instance we will create.  Used for Hibernate integration (ugh?)
     */
    private static long nextInstanceID = 0L;

    /**
     * The map used by {@link #getSpecificInstance(String)}.
     */
    private static final Map<String, MatchMakerSession> sessions = new HashMap<String, MatchMakerSession>();

    /**
     * A stupid hook to de-staticify the creation of a MatchMakerSessionConnectionProvider
     * instance that has to be associated with a particular session.
     * @param mmSessionId The ID that was generated in createSession().
     * @return The MatchMakerSession instance with the given ID, or null if there is no such session.
     */
    static MatchMakerSession getSpecificInstance(String mmSessionId) {
        return sessions.get(mmSessionId);
    }

    /**
     * The ID of this instance. A string version of this value is the key in the {@link #sessions} map.
     */
    private final long instanceID;

    private final MatchMakerSessionContext context;
    private final SessionFactory hibernateSessionFactory;
	private final SQLDatabase database;
	private PLSecurityManager sm;
	private PLUser appUser;
	private String dbUser;
	private Date sessionStartTime;

    private FolderParent currentProjectFolderParent;
    private FolderParent backupProjectFolderParent;

    private PlFolderDAO folderDAO;
    private ProjectDAO projectDAO;
    private MungeProcessDAO mungeProcessDAO;
    private MatchMakerTranslateGroupDAO matchMakerTranslateGroupDAO;
    private TableMergeRuleDAO tableMergeRulesDAO;

    private List<WarningListener> warningListeners = new ArrayList<WarningListener>();

    private TranslateGroupParent tgp;

	private Session hSession;

    /**
     * A snapshot of the def_param table when we logged in.
     */
    private final DefaultParameters defParam;

    /**
     * The version of the Power*Loader schema we're connected to.
     */
    private final Version plSchemaVersion;

    /**
     * XXX this is untestable unless you're connected to a database right now.
     *   It should be given a PLSecurityManager implementation rather than creating one.
     *  
     * @throws ArchitectException if there was a problem connecting to the database
     * @throws MatchMakerConfigurationException If there are some user settings that are
     * not set up properly. 
     */
	public MatchMakerHibernateSessionImpl(
            MatchMakerSessionContext context,
			SPDataSource ds)
		throws PLSecurityException, UnknownFreqCodeException,
				SQLException, PLSchemaException, VersionFormatException, ArchitectException,
                MatchMakerConfigurationException {
        this.instanceID = nextInstanceID++;
        sessions.put(String.valueOf(instanceID), this);

        this.context = context;
		database = new SQLDatabase(ds);
		dbUser = ds.getUser();

		final Connection con = database.getConnection();
		final DatabaseMetaData dbmd = con.getMetaData();
        logger.info("Connected to repository database.");
        logger.info("Database product name: "+dbmd.getDatabaseProductName());
        logger.info("Database product version: "+dbmd.getDatabaseProductVersion());
        logger.info("Database driver name: "+dbmd.getDriverName());
        logger.info("Database driver version: "+dbmd.getDriverVersion());

        try {
            defParam = new DefaultParameters(con, null, ds.getPlSchema());
            plSchemaVersion = defParam.getPLSchemaVersion();
        } catch (SQLException e) {
            String plSchema = ds.getPlSchema();
            if (plSchema == null || plSchema.length() == 0) {
                // this case is unlikely to happen because we have to check for null when setting up hibernate 
            	plSchema = "not set";
            }
			SQLException exception = new SQLException(
                    "Couldn't determine Repository schema version for database " + ds.getDisplayName() + ".\n" +
                    "Please check that you have set the Repository Schema Owner correctly in the DataSource Configuration\n" +
                    "(Repository Schema Owner currently " + plSchema + ").");
            exception.setNextException(e);
            throw exception;
        }

        if (plSchemaVersion.compareTo(MatchMakerSessionContext.MIN_PL_SCHEMA_VERSION) < 0) {
            throw new PLSchemaException(
                    "The MatchMaker requires a newer version of the Repository Schema" +
                    " than is installed in the "+ds.getDisplayName()+" database.",
                    plSchemaVersion.toString(), MatchMakerSessionContext.MIN_PL_SCHEMA_VERSION.toString());
        }
        sm = new PLSecurityManager(con,
				 					dbUser.toUpperCase(),
				 					ds.getPass(),
                                    false);  // since this is a database login, we don't require correct app-level password
		appUser = sm.getPrincipal();
		sessionStartTime = new Date();
		this.hibernateSessionFactory = buildHibernateSessionFactory(ds);

		hSession = hibernateSessionFactory.openSession();
        folderDAO = new PlFolderDAOHibernate(this);
        projectDAO = new ProjectDAOHibernate(this);
        mungeProcessDAO = new MungeProcessDAOHibernate(this);
        matchMakerTranslateGroupDAO = new MatchMakerTranslateGroupDAOHibernate(this);
        tableMergeRulesDAO = new TableMergeRulesDAOHibernate(this);
        con.close();
	}

    public MatchMakerSessionContext getContext() {
        return context;
    }

	public SQLDatabase getDatabase() {
		return database;
	}

	public String getAppUser() {
		return appUser.getUserId();
	}

	public String getDBUser() {
		return dbUser;
	}

	public Date getSessionStartTime() {
		return sessionStartTime;
	}

    /**
     * Logs the warning to the log4j logger at WARN level as well as telling all the
     * warning listeners about the warning.
     */
    public void handleWarning(String message) {
        logger.warn("handleWarning: received warning message: "+message);
        synchronized (warningListeners) {
            for (int i = warningListeners.size()-1; i >= 0; i--) {
                warningListeners.get(i).handleWarning(message);
            }
        }
    }

    public void addWarningListener(WarningListener l) {
        synchronized (warningListeners) {
            warningListeners.add(l);
        }
    }

    public void removeWarningListener(WarningListener l) {
        synchronized (warningListeners) {
            warningListeners.remove(l);
        }
    }


    public PlFolder findFolder(String foldername) {
        for (PlFolder folder : getCurrentFolderParent().getChildren()){
            if (folder.getName().equals(foldername)) return folder;
        }
        return null;
    }

    public <T extends MatchMakerObject> MatchMakerDAO<T> getDAO(Class<T> businessClass) {
        if (businessClass == PlFolder.class) {
            return (MatchMakerDAO<T>) folderDAO;
        } else if (businessClass == Project.class) {
            return (MatchMakerDAO<T>) projectDAO;
        } else if (businessClass == MungeProcess.class){
            return (MatchMakerDAO<T>) mungeProcessDAO;
        } else if (businessClass == MatchMakerTranslateGroup.class){
            return (MatchMakerDAO<T>) matchMakerTranslateGroupDAO;
        } else if (businessClass == TableMergeRules.class) {
        	return (MatchMakerDAO<T>) tableMergeRulesDAO;
        } else {
            throw new IllegalArgumentException("I don't know how to create a DAO for "+businessClass.getName());
        }
    }

    /**
     * @throws ArchitectRuntimeException If it fails to connect to the database
     */
    public Connection getConnection() {
    	try {
            return database.getConnection();
        } catch (ArchitectException ex) {
            throw new ArchitectRuntimeException(ex);
        }
    }

    /**
     * Creates a session from the hibernate session factory, allowing the factory
     * to get its database connection using our &uuml;ber-cool connection provider.
     */
    public Session openSession() {
        return hSession;
    }


    /**
     * Creates or retrieves a Hibernate SessionFactory object for the
     * given database.  Never creates two SessionFactory objects for
     * the same jdbcurl+user+password combination.
     *
     * @param ds The connection specification for the session factory you want.
     * @return A Hibernate SessionFactory for the given data source.
     * 
     * @throws MatchMakerConfigurationException If the given data source is not
     * properly configured. 
     */
    private SessionFactory buildHibernateSessionFactory(SPDataSource ds) throws MatchMakerConfigurationException {
        SessionFactory factory;
        Configuration cfg = new Configuration();

        URL configFile = getClass().getResource("/ca/sqlpower/matchmaker/dao/hibernate/hibernate.cfg.xml");
		if (configFile == null) {
			throw new RuntimeException("Could not classload hibernate.cfg.xml");
		}
        cfg.configure(configFile);

        // last-minute configuration overrides for stuff that can only be known at runtime
        if (ds.getPlSchema() == null || ds.getPlSchema().trim().length() == 0) {
            throw new MatchMakerConfigurationException(
                    "Cannot connect to repository: Data source \"" + ds.getDisplayName() +
                    "\" does not have the Repository Schema Owner set.");
        }
        cfg.setProperty("hibernate.default_schema",ds.getPlSchema());
        cfg.setProperty("hibernate.dialect", HibernateUtil.guessHibernateDialect(ds.getParentType()));
        cfg.setProperty(
                Environment.CONNECTION_PROVIDER,
                MatchMakerHibernateSessionConnectionProvider.class.getName());
        cfg.setProperty(
                MatchMakerHibernateSessionConnectionProvider.PROP_SESSION_ID,
                String.valueOf(instanceID));
        if (logger.isDebugEnabled()) {
            cfg.setProperty("hibernate.show_sql", "true");
            cfg.setProperty("hibernate.jdbc.batch_size", "0");
        }
        // Create the SessionFactory from hibernate.cfg.xml
        factory = cfg.buildSessionFactory();
        return factory;
    }

    public Project getProjectByName(String name) {
    	return projectDAO.findByName(name);
    }

	public boolean isThisProjectNameAcceptable(String name) {
		return projectDAO.isThisProjectNameAcceptable(name);
	}

    public String createNewUniqueName() {
        String name = "New Project";
        if (getProjectByName(name) == null) {
            return name;
        } else{
            int num=1;
            //Iterates until it finds a name that does not conflict with
            //existing project names
            while(getProjectByName(name+num) != null) {
                num++;
                name = "New Project" + num;
            }
            return name;
        }

    }

	public long countProjectByName(String name) {
		return projectDAO.countProjectByName(name);
	}

    public TranslateGroupParent getTranslations() {
        if (tgp == null) {
            MatchMakerTranslateGroupDAO matchMakerTranslateGroupDAO = (MatchMakerTranslateGroupDAO) getDAO(MatchMakerTranslateGroup.class);
            List<MatchMakerTranslateGroup> groups = matchMakerTranslateGroupDAO.findAll();
            tgp = new TranslateGroupParent(this);
            tgp.setName("Translation Groups");
            for (MatchMakerTranslateGroup g: groups) {
                tgp.addChild(g);
            }
        }
        return tgp;
    }

    /**
     * Retrieves all the PL Folders from the database, or returns the
     * list that was previously retrieved by a call to this method.
     */
	public FolderParent getCurrentFolderParent() {
		if (currentProjectFolderParent == null) {
			currentProjectFolderParent = new FolderParent(this);
            currentProjectFolderParent.setName("Current Projects");
			PlFolderDAO folderDAO = (PlFolderDAO) getDAO(PlFolder.class);
			for(PlFolder f :folderDAO.findAll()) {
				currentProjectFolderParent.addChild(f);
			}
		}
		return currentProjectFolderParent;
	}

	/**
     * Retrieves all the PL Folders from the database that have backed-up project
     * transactions, or returns the list that was previously retrieved by a call
     * to this method.
     * <p>
     * TODO implement backups
     */
	public FolderParent getBackupFolderParent() {
        if (backupProjectFolderParent == null) {
            backupProjectFolderParent = new FolderParent(this);
            backupProjectFolderParent.setName("Backup Projects");
        }
		return backupProjectFolderParent;
	}

    public Version getPLSchemaVersion() {
        return plSchemaVersion;
    }

    public SQLTable findPhysicalTableByName(String catalog, String schema, String tableName) throws ArchitectException {
    	logger.debug("Session.findSQLTableByName:" +
    			catalog + "." + schema + "." + tableName);
    	if (tableName == null || tableName.length() == 0) return null;
		SQLDatabase currentDB = getDatabase();
		SQLDatabase tempDB = null;
		try {
			tempDB = new SQLDatabase(currentDB.getDataSource());
			return tempDB.getTableByName(
					catalog,
					schema,
					tableName);
		} finally {
			if (tempDB != null) tempDB.disconnect();
		}
    }

    public boolean tableExists(String catalog, String schema,
    		String tableName) throws ArchitectException {
    	return (findPhysicalTableByName(catalog,schema,tableName) != null);
    }

    public boolean tableExists(SQLTable table) throws ArchitectException {
    	if ( table == null ) return false;
    	return tableExists(table.getCatalogName(),
    			table.getSchemaName(),
    			table.getName());
    }

    /**
     * this method requires real JDBC connection and create sql statement
     * on the connection.
     */
    public boolean canSelectTable(SQLTable table) {

		Connection con = null;
		Statement stmt = null;
		StringBuffer sql = new StringBuffer();
		try {
            con = getConnection();
			sql.append("select * from ");
			sql.append(DDLUtils.toQualifiedName(table));
			stmt = con.createStatement();
			stmt.executeQuery(sql.toString());
			return true;
		} catch (SQLException e) {
			logger.debug("sql error: select statement:[" +
					sql.toString() + "]\n" + e.getMessage() );
			return false;
		} finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                logger.warn("Couldn't close statement", e);
            }
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException e) {
                logger.warn("Couldn't close connection", e);
            }
		}
	}
}