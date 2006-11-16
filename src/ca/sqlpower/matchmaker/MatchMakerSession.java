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
     * search the match by name via DAO
     * @param name
     * This method is used to check if a match is valid for updating or not.  
	 * A match is not valid for updating if an existing match already has the
	 * same name.
     * @param match the match to ignore in checking the validation
     * @param name the name the match want to change to
     * @return true if the name is not taken, false if the name exists
     */
    public boolean isThisMatchNameAcceptable(Match match, String name);
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
}
