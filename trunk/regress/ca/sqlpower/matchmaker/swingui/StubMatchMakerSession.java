package ca.sqlpower.matchmaker.swingui;

import java.sql.Connection;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchMakerSessionContext;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.dao.MatchMakerDAO;

public class StubMatchMakerSession implements MatchMakerSession{

    private static final Logger logger = Logger.getLogger(StubMatchMakerSession.class);

    public PlFolder findFolder(String foldername) {
        logger.debug("Stub call: StubMatchMakerSession.findFolder()");
        return null;
    }

    public String getAppUser() {
        logger.debug("Stub call: StubMatchMakerSession.getAppUser()");
        return null;
    }

    public MatchMakerSessionContext getContext() {
        logger.debug("Stub call: StubMatchMakerSession.getContext()");
        return null;
    }

    public <T extends MatchMakerObject> MatchMakerDAO<T> getDAO(Class<T> businessClass) {
        logger.debug("Stub call: StubMatchMakerSession.getDAO()");
        return null;
    }

    public String getDBUser() {
        logger.debug("Stub call: StubMatchMakerSession.getDBUser()");
        return null;
    }

    public SQLDatabase getDatabase() {
        logger.debug("Stub call: StubMatchMakerSession.getDatabase()");
        return null;
    }

    public List<PlFolder> getFolders() {
        logger.debug("Stub call: StubMatchMakerSession.getFolders()");
        return null;
    }

    public Date getSessionStartTime() {
        logger.debug("Stub call: StubMatchMakerSession.getSessionStartTime()");
        return null;
    }

    public Connection getConnection() {
        logger.debug("Stub call: StubMatchMakerSession.getConnection()");
        return null;
    }

	public boolean isThisMatchNameAcceptable(String name) {
		// TODO Auto-generated method stub
		logger.debug("Stub call: StubMatchMakerSession.isThisMatchNameAcceptable()");
		return false;
	}

	public Match getMatchByName(String name) {
		// TODO Auto-generated method stub
		logger.debug("Stub call: StubMatchMakerSession.getMatchByName()");
		return null;
	}

    public String createNewUniqueName() {
        // TODO Auto-generated method stub
        logger.debug("Stub call: StubMatchMakerSession.getNewUniqueName()");
        return null;
    }

}
