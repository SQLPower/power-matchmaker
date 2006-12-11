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
import ca.sqlpower.matchmaker.TranslateGroupParent;
import ca.sqlpower.matchmaker.WarningListener;
import ca.sqlpower.matchmaker.dao.MatchMakerDAO;
import ca.sqlpower.util.Version;

/**
 * A true stub implementation. Every method does nothing (except log the call at DEBUG level).
 */
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
		logger.debug("Stub call: StubMatchMakerSession.isThisMatchNameAcceptable()");
		return false;
	}

	public Match getMatchByName(String name) {
		logger.debug("Stub call: StubMatchMakerSession.getMatchByName()");
		return null;
	}

    public String createNewUniqueName() {
        logger.debug("Stub call: StubMatchMakerSession.getNewUniqueName()");
        return null;
    }

    public long countMatchByName(String name) {
        logger.debug("Stub call: StubMatchMakerSession.countMatchByName()");
        return 0;
    }

    public void handleWarning(String message) {
        logger.warn("Stub call: StubMatchMakerSession.handleWarning("+message+")");
    }

    public void addWarningListener(WarningListener l) {
        logger.debug("Stub call: StubMatchMakerSession.addWarningListener()");
    }

    public void removeWarningListener(WarningListener l) {
        logger.debug("Stub call: StubMatchMakerSession.removeWarningListener()");
    }

    public TranslateGroupParent getTranslations() {
        logger.debug("Stub call: StubMatchMakerSession.getTranslations()");
        return null;
    }

    public Version getPLSchemaVersion() {
        logger.debug("Stub call: StubMatchMakerSession.getPLSchemaVersion()");
        return null;
    }
    
}
