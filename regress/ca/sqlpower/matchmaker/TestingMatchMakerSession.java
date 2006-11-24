package ca.sqlpower.matchmaker;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.matchmaker.dao.MatchMakerDAO;

public class TestingMatchMakerSession implements MatchMakerSession {
	Date date = new Date();
	String appUser = "App User";
	String dbUser = "DB User";
	SQLDatabase db = new SQLDatabase();
	List<PlFolder> folders;
    TranslateGroupParent translateGroupParent;
	MatchMakerSessionContext context;
	Connection con;
	List<String> warnings = new ArrayList<String>();
    
	public TestingMatchMakerSession() {
		folders =  new ArrayList<PlFolder>();
        translateGroupParent= new TranslateGroupParent(this);
	}

	public String getAppUser() {
		return appUser;
	}

	public String getDBUser() {
		return dbUser;
	}

	public SQLDatabase getDatabase() {
		return db;
	}

	public List<PlFolder> getFolders() {
		return folders;
	}

	public Date getSessionStartTime() {
		return date;
	}

	public void setAppUser(String appUser) {
		this.appUser = appUser;
	}

	public void setSessionStartTime(Date date) {
		this.date = date;
	}

	public void setDatabase(SQLDatabase db) {
		this.db = db;
	}

	public void setDBUser(String dbUser) {
		this.dbUser = dbUser;
	}

	public void setFolders(List<PlFolder> folders) {
		this.folders = folders;
	}

	public MatchMakerSessionContext getContext() {
		// TODO add the context to the test session
		return null;
	}

    public PlFolder findFolder(String foldername) {
        for (PlFolder folder : folders){
            if (folder.getName().equals(foldername)) return folder;
        }
        return null;
    }

    public <T extends MatchMakerObject> MatchMakerDAO<T> getDAO(Class<T> businessClass) {
        if (businessClass == MatchMakerTranslateGroup.class){
            return (MatchMakerDAO<T>) new MatchMakerTranslateGroupDAOStub();
        }
        return null;
    }

    public Connection getConnection() {
        return con;
    }

    public void setConnection(Connection con) {
        this.con = con;
    }

    public Match getMatchByName(String name) {
		return null;
	}

    public String createNewUniqueName() {
        // TODO Auto-generated method stub
        return null;
    }

	public boolean isThisMatchNameAcceptable(String name) {
		// TODO Auto-generated method stub
		return false;
	}

    public long countMatchByName(String name) {    
        return 0;
    }

    /**
     * Prints the message to syserr and appends it to the warnings list.
     * 
     * @see #getWarnings()
     */
    public void handleWarning(String message) {
        System.err.println("TestingMatchMakerSession.handleWarning(): got warning: "+message);
        warnings.add(message);
    }

    /**
     * Returns the real warning list.  Feel free to modify it if you want, but your changes
     * will affect the session's real list of warnings.
     */
    public List<String> getWarnings() {
        return warnings;
    }

    /**
     * Replaces this session's warning list.  If you set this to null or an unmodifiable
     * list, handleWarning() will stop working.
     */
    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    /**
     * Does nothing.
     */
    public void addWarningListener(WarningListener l) {
        // TODO Auto-generated method stub
    }

    /**
     * Does nothing.
     */
    public void removeWarningListener(WarningListener l) {
        // TODO Auto-generated method stub
    }

    public TranslateGroupParent getTranslations() {
        if (translateGroupParent == null){
            translateGroupParent = new TranslateGroupParent(this);
        }
        return translateGroupParent;
    }
    
    
}
