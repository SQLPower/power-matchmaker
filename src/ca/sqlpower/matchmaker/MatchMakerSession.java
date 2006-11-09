package ca.sqlpower.matchmaker;

import java.util.Date;
import java.util.List;

import ca.sqlpower.architect.SQLDatabase;

/**
 * The MatchMakerSession interface represents one person's login to
 * a PL Schema, and is the main container for session information
 * throughout the MatchMaker business model.
 *
 * <p>To make one of these, use the
 * {@link MatchMakerSessionContext#createSession(ArchitectDataSource, String, String)}
 * method.
 *
 * <p>This interface makes a strong committment to keeping the MatchMaker
 * metadata in a SQL database.  This might come back to haunt us some day.
 *
 * @version $Id$
 */
public interface MatchMakerSession {

	/**
	 * The database connection to the PL Schema for this session.
	 */
	public SQLDatabase getDatabase();

	/**
	 * The PL Schema user for this session.  Often but not necessarily
	 * the same as the DB User.
	 */
	public String getAppUser();

	/**
	 * The actual RDBMS user name that we are connected to the database as.
	 */
	public String getDBUser();

	/**
	 * The time this session was created.
	 */
	public Date getSessionStartTime();

	/**
	 * All of the Match folders that the current user can see.
	 */
	public List<PlFolder> getFolders();
}
