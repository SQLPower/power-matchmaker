package ca.sqlpower.matchmaker.dao.hibernate;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
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

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.matchmaker.AbstractMatchMakerObject;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MatchMakerCriteria;
import ca.sqlpower.matchmaker.MatchMakerCriteriaGroup;
import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchMakerSessionContext;
import ca.sqlpower.matchmaker.MatchMakerTranslateGroup;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.TranslateGroupParent;
import ca.sqlpower.matchmaker.WarningListener;
import ca.sqlpower.matchmaker.dao.MatchCriteriaGroupDAO;
import ca.sqlpower.matchmaker.dao.MatchDAO;
import ca.sqlpower.matchmaker.dao.MatchMakerDAO;
import ca.sqlpower.matchmaker.dao.MatchMakerTranslateGroupDAO;
import ca.sqlpower.matchmaker.dao.PlFolderDAO;
import ca.sqlpower.matchmaker.util.HibernateUtil;
import ca.sqlpower.security.PLSecurityException;
import ca.sqlpower.security.PLSecurityManager;
import ca.sqlpower.security.PLUser;
import ca.sqlpower.util.UnknownFreqCodeException;

/**
 * An implementation of MatchMakerSession that uses Hibernate to
 * look up and store the business objects.
 */
public class MatchMakerHibernateSessionImpl implements MatchMakerHibernateSession {

    private static final Logger logger = Logger.getLogger(MatchMakerHibernateSessionImpl.class);

    private MatchMakerObject<MatchMakerObject, MatchMakerTranslateGroup> translateGroupParent = new AbstractMatchMakerObject<MatchMakerObject, MatchMakerTranslateGroup>(){

        @Override
        public void addChild(MatchMakerTranslateGroup child) {
            getDAO(MatchMakerTranslateGroup.class).save(child);
            super.addChild(child);
        }
        
        @Override
        public void removeChild(MatchMakerTranslateGroup child) {
            if(!isUsedInBusinessModel(child)) {
                getDAO(MatchMakerTranslateGroup.class).delete(child);
                super.removeChild(child);
            } else {
                handleWarning("Sorry the translate group "+child.getName()+" is in use and can't be deleted");
            }
        }
        /**
         * check and see if the translate group tg exists in the folder hierachy.
         * @param tg the translate group.
         * @return true if tg exists in the folder hierachy false if it dosn't.
         */
        public boolean isUsedInBusinessModel(MatchMakerTranslateGroup tg) {
            for (MatchMakerObject mmo :getFolders()){
                // found the translate group
                if(checkMMOContainsTranslateGroup(mmo,tg) == true) return true;
            }
            return false;
        }
        
        /**
         * recursivly check and see if there is a criterion with the passed in translate group. 
         * @param mmo the object and decendents we want to check.
         * @param tg the translate group that we want to know if it is used.
         * @return true if tg is used by mmo or a decendent false otherwise.
         */
        private boolean checkMMOContainsTranslateGroup(MatchMakerObject mmo,MatchMakerTranslateGroup tg){
            if (mmo instanceof MatchMakerCriteria){
                MatchMakerCriteria criteria = (MatchMakerCriteria) mmo;
                if(tg.equals(criteria.getTranslateGroup())){
                    return true;
                }
            }
            if ( mmo instanceof Match ){
                Match matchChild = (Match) mmo;
                for (MatchMakerCriteriaGroup critGroup : matchChild.getMatchCriteriaGroups()){
                    if (checkMMOContainsTranslateGroup(critGroup, tg)) return true;
                }
            } else {
                for (int i = 0; i< mmo.getChildCount(); i++){
                    MatchMakerObject child = (MatchMakerObject) mmo.getChildren().get(i);
                    if (checkMMOContainsTranslateGroup(child, tg)) return true;
                }
            }
            
            return false;
        }

        @Override
        public boolean equals(Object obj) {
            return this==obj;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }
        
    };
    
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

    private PlFolderDAO folderDAO;
    private MatchDAO matchDAO;
    private MatchCriteriaGroupDAO matchMakerCriteriaGroupDAO;
    private MatchMakerTranslateGroupDAO matchMakerTranslateGroupDAO;

    private List<WarningListener> warningListeners = new ArrayList<WarningListener>();

    /**
     * XXX this is untestable unless you're connected to a database right now.
     *   It should be given a PLSecurityManager implementation rather than creating one.
     */
	public MatchMakerHibernateSessionImpl(
            MatchMakerSessionContext context,
			ArchitectDataSource ds)
		throws PLSecurityException, UnknownFreqCodeException,
				SQLException, ArchitectException {
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
        
        sm = new PLSecurityManager(con,
				 					dbUser.toUpperCase(),
				 					ds.getPass(),
                                    false);  // since this is a database login, we don't require correct app-level password
		appUser = sm.getPrincipal();
		sessionStartTime = new Date();
		this.hibernateSessionFactory = buildHibernateSessionFactory(ds);

        folderDAO = new PlFolderDAOHibernate(this);
        matchDAO = new MatchDAOHibernate(this);
        matchMakerCriteriaGroupDAO = new MatchMakerCriteriaGroupDAOHibernate(this);
        matchMakerTranslateGroupDAO = new MatchMakerTranslateGroupDAOHibernate(this);
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
    
	public List<PlFolder> getFolders() {
		PlFolderDAO folderDAO = (PlFolderDAO) getDAO(PlFolder.class);
		return folderDAO.findAll();
	}

    public PlFolder findFolder(String foldername) {
        for (PlFolder folder : getFolders()){
            if (folder.getName().equals(foldername)) return folder;
        }
        return null;
    }

    public <T extends MatchMakerObject> MatchMakerDAO<T> getDAO(Class<T> businessClass) {
        if (businessClass == PlFolder.class) {
            return (MatchMakerDAO<T>) folderDAO;
        } else if (businessClass == Match.class) {
            return (MatchMakerDAO<T>) matchDAO;
        } else if (businessClass == MatchMakerCriteriaGroup.class){
            return (MatchMakerDAO<T>) matchMakerCriteriaGroupDAO;
        } else if (businessClass == MatchMakerTranslateGroup.class){
            return (MatchMakerDAO<T>) matchMakerTranslateGroupDAO;
        } else {
            throw new IllegalArgumentException("I don't know how to create a DAO for "+businessClass.getName());
        }
    }

    public Connection getConnection() {
        try {
            return database.getConnection();
        } catch (ArchitectException ex) {
            throw new RuntimeException("Couldn't acquire connection to PL Schema Database", ex);
        }
    }

    /**
     * Creates a session from the hibernate session factory, allowing the factory
     * to get its database connection using our &uuml;ber-cool connection provider.
     */
    public Session openSession() {
        return hibernateSessionFactory.openSession();
    }


    /**
     * Creates or retrieves a Hibernate SessionFactory object for the
     * given database.  Never creates two SessionFactory objects for
     * the same jdbcurl+user+password combination.
     *
     * @param ds The connection specification for the session factory you want.
     * @return A Hibernate SessionFactory for the given data source.
     */
    private SessionFactory buildHibernateSessionFactory(ArchitectDataSource ds) {
        SessionFactory factory;
        Configuration cfg = new Configuration();

        cfg.configure(getClass().getResource("/ca/sqlpower/matchmaker/dao/hibernate/hibernate.cfg.xml"));

        // last-minute configuration overrides for stuff that can only be known at runtime
        cfg.setProperty("hibernate.default_schema",ds.getPlSchema());
        cfg.setProperty("hibernate.dialect", HibernateUtil.plDbType2Dialect(ds.getPlDbType()));
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

    public Match getMatchByName(String name) {
    	return matchDAO.findByName(name);
    }

	public boolean isThisMatchNameAcceptable(String name) {
		return matchDAO.isThisMatchNameAcceptable(name);
	}

    public String createNewUniqueName() {
        String name = "New Match";
        if (getMatchByName(name) == null) {
            return name;
        } else{
            int num=1;
            //Iterates until it finds a name that does not conflict with
            //existing match names
            while(getMatchByName(name+num) != null) {
                num++;
                name = "New Match" + num;
            }
            return name;
        }

    }

	public long countMatchByName(String name) {
		return matchDAO.countMatchByName(name);
	}

    public TranslateGroupParent getTranslations() {
        MatchMakerTranslateGroupDAO matchMakerTranslateGroupDAO = (MatchMakerTranslateGroupDAO) getDAO(MatchMakerTranslateGroup.class);
        List<MatchMakerTranslateGroup> groups = matchMakerTranslateGroupDAO.findAll();
        TranslateGroupParent tgp = new TranslateGroupParent(this);
        for (MatchMakerTranslateGroup g: groups) {
            tgp.addChild(g);
        }
        return tgp;
    }

}