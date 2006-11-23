package ca.sqlpower.matchmaker;

import java.sql.Connection;
import java.util.Date;
import java.util.List;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.matchmaker.dao.MatchMakerDAO;
import ca.sqlpower.matchmaker.dao.hibernate.MatchMakerHibernateSessionContext;

/**
 * The MatchMakerSession interface represents one person's login to
 * a PL Schema, and is the main container for session information
 * throughout the MatchMaker business model.
 *
 * <p>To make one of these, use the
 * {@link MatchMakerHibernateSessionContext#createSession(ArchitectDataSource, String, String)}
 * method.
 *
 * <p>This interface makes a strong committment to keeping the MatchMaker
 * metadata in a SQL database.  This might come back to haunt us some day.
 *
 * @version $Id$
 */
public interface MatchMakerSession {

    /**
     * The session context that created this session.
     */
    public MatchMakerSessionContext getContext();

	/**
	 * The database connection to the PL Schema for this session.
	 */
	public SQLDatabase getDatabase();

    /**
     * create a new matchmakersession specific error report
     */
    
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

    /**
     * Returns the folder that matches with the name
     * @param foldername the name of the folder that is desired
     * @return the folder with that matches with the foldername, returns null if no results are avaiable
     */
    public PlFolder findFolder(String foldername);

    /**
     * Returns the DAO Object for the given business class
     * @param <T> the business class of the DAO Object
     * @return the object that is of the business class
     */
    public <T extends MatchMakerObject> MatchMakerDAO<T> getDAO(Class<T> businessClass);

    /**
     * Get a connection to the current database
     */
    public Connection getConnection();

    /**
	 * check to see if there is any match under given name
	 * @param name
	 * @return true if no match found under given name, false otherwise
	 */
    public boolean isThisMatchNameAcceptable(String name);

    /**
	 * count match entity by given name
	 * @param name
	 * @return number of match entity
	 */
    public long countMatchByName(String name);

    /**
     * find the Match Object by name, search by the DAO
     * @param name the name of the match desired
     * @return match object or null if not found
     */
    public Match getMatchByName(String name);

    /**
     * This method creates an unique name for the match such that it does not
     * conflict with existing match names.  The foromat of the name should be
     * New_Match#
     * @return an unique non-conflicting name in New_Match# form (unless New_Match
     *          is already an acceptable name)
     */
    public String createNewUniqueName();
    
    /**
     * Tells the session implementation about a warning message that should be
     * displayed to the user as soon as possible.  Warnings are different from
     * exceptions: they do not prevent completion of the current task.  They are
     * simply assumptions and workarounds in the business logic that the user should
     * be aware of when they happen.
     * <p>
     * A call to this method may invoke some implementation-specific handling mechanism,
     * but should also always result in all subscribed WarningListeners being notified
     * with the message as well.
     * 
     * @param message The message to display to the user.
     */
    public void handleWarning(String message);
    
    /**
     * Subscribes the given listener to the incessant stream of warning messages
     * generated as a result of bad metadata.
     */
    public void addWarningListener(WarningListener l);
    
    /**
     * Removes the given listener to the incessant stream of warning messages
     * generated as a result of bad metadata.
     * 
     * @param l The listener to remove. If it was not actually listening, calling
     * this method has no effect.
     */
    public void removeWarningListener(WarningListener l);
    
}
