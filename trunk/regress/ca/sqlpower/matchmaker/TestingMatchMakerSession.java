package ca.sqlpower.matchmaker;

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
	MatchMakerSessionContext context;


	public TestingMatchMakerSession() {
		folders =  new ArrayList<PlFolder>();
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
        // TODO Auto-generated method stub
        return null;
    }

	public boolean isThisMatchNameAcceptable(Match match,String name) {
		return false;
	}

	public Match getMatchByName(String name) {
		return null;
	}

    public String createNewUniqueName() {
        // TODO Auto-generated method stub
        return null;
    }

}
