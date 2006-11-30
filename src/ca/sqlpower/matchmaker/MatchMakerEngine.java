/**
 * 
 */
package ca.sqlpower.matchmaker;

import java.io.IOException;
import java.io.InputStream;

/**
 * the matchmaker engine interface represents the contexts that required 
 * to run the engine, the engine now is a C program uses ODBC to connect
 * to database as we create this interface. we may change it to java base
 * in the futrue.
 *  
 */
public interface MatchMakerEngine {

	/**
	 * start the engine!
	 * @throws EngineSettingException if not all the preconditions met
	 * @throws IOException 
	 */
	public void run() throws EngineSettingException, IOException;
	
	public boolean isRunning();
	
	/**
	 * stop the engine
	 */
	public void abort();
	
	/**
	 * returns the engine exit code, null if the engine has not been run yet.
	 * @throws InterruptedException 
	 */
	public Integer getEngineReturnCode();
	
	/**
	 * checks the preconditions that required by the engine, true is everything is OK.
	 * @throws EngineSettingException 
	 */
	public boolean checkPreConditions() throws EngineSettingException;
	
	/**
	 * Create the command line to run the match engine or for display
	 * @param session -- the session that contains the database that we are 
	 * going to run engine on 
	 * @param match  -- the match object that we want to create command line for
	 * @param userPrompt -- true if you want to append USER_PARAMPT=Y to the command line
	 * @return the string of the command line
	 */
	public String createCommandLine(MatchMakerSession session, Match match, boolean userPrompt);
	
	/**
	 * returns the standard error of the engine, if it's running, otherwise returns null
	 */
	public InputStream getEngineErrorOutput();

	/**
	 * returns the standard output of the engine, if it's running, otherwise returns null
	 */
	public InputStream getEngineStandardOutput();
}
