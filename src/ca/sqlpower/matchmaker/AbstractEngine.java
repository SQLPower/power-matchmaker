/*
 * Copyright (c) 2008, SQL Power Group Inc.
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

package ca.sqlpower.matchmaker;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.CancellationException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.security.EmailNotification;
import ca.sqlpower.security.PLSecurityException;
import ca.sqlpower.security.EmailNotification.EmailRecipient;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.util.Email;
import ca.sqlpower.util.UnknownFreqCodeException;
/**
 * Common ground for all C engines.  This class handles events
 * output capture, monitoring and starting and stoping the engine. 
 *
 */
public abstract class AbstractEngine implements MatchMakerEngine {

	private final static Logger logger = Logger.getLogger(AbstractEngine.class);

    /**
	 * The session that this engine operates in.
	 */
	private MatchMakerSession session;
    
    /**
     * The project this engine operates on.
     */
	private Project project;
	
	/**
	 * Gets set to true when the process has started.
	 */
	private boolean started = false;
	
	/**
	 * Gets set to true when the process has terminated.
	 */
	private boolean finished = false;
	
	/**
	 * Gets set to true when the user attempts to cancel the engine run.
	 */
	private boolean cancelled;
	
	/**
	 * The users who should be emailed for a green status
	 */
	protected List<EmailRecipient> greenUsers;
	
	/**
	 * The users who should be emailed for a yellow status
	 */
	protected List<EmailRecipient> yellowUsers;
	
	/**
	 * The users who should be emailed for a red status
	 */
	protected List<EmailRecipient> redUsers;
	
	/**
	 * The email for sending info of the engine
	 */
	protected Email email;
	
	/**
	 * The level at which to show the engine debugging
	 */
	private Level messageLevel = Level.INFO;
	
	protected Project getProject() {
		return project;
	}

	protected void setProject(Project project) {
		this.project = project;
	}

	protected MatchMakerSession getSession() {
		return session;
	}

	protected void setSession(MatchMakerSession session) {
		this.session = session;
	}

	public EngineInvocationResult call() throws EngineSettingException, SourceTableException {
		try {
			try {
				checkPreconditions();
			} catch (SQLObjectException e) {
				throw new RuntimeException(e);
			}
			
            finished = false;
			started = true;

			getLogger().info("Engine process completed normally.");

			return EngineInvocationResult.SUCCESS;
		} finally {
			finished = true;
		}
	}

	///////// Monitorable support ///////////
	
	/**
	 * Right now the job size is always indeterminant
	 */
	public Integer getJobSize() {
		return null;
	}

	public String getMessage() {
		if(started && !finished){
			return "Running MatchMaker Engine";
		} else {
			return "";
		}
	}

	public int getProgress() {
		// since this is always indeterminant  
		return 0;
	}

	public boolean hasStarted() {
		return started;
	}
	
	// The engine is done when it has an exit code
	public boolean isFinished() {
		return finished;
	}

	public synchronized void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
        // TODO interrupt the engine thread
	}
    
    public synchronized boolean isCancelled() {
        return cancelled;
    }

    /**
     * Checks if this engine has been cancelled by another thread.  If so,
     * throws a CancellationException.
     *
     * @throws CancellationException if this engine has been cancelled
     */
    protected void checkCancelled() {
        if (isCancelled()) {
            throw new CancellationException("User-requested abort");
        }
    }
    
	/**
	 * check the DSN setting for the current database connection,
	 * that's required by the matchmaker odbc engine, since we will not
	 * use this odbc engine forever, check for not null is acceptable for now.
	 */
	protected static boolean hasODBCDSN(SPDataSource dataSource) {
		final String odbcDsn = dataSource.getOdbcDsn();
		if ( odbcDsn == null || odbcDsn.length() == 0 ) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * returns true if the log file of this project is readable.
	 */
	public static boolean canReadLogFile(MatchMakerSettings settings) {
		File file = settings.getLog();
		
		if (file == null) return false;
		
		return file.canRead();
	}

	/**
	 * returns true if the log file of this project is writable.
	 */
	public static boolean canWriteLogFile(MatchMakerSettings settings) {
	    File file = settings.getLog();
	    if (file == null) {
	    	logger.debug("file is null.");
	    	return false;
	    }
	    if (file.exists()) {
	        return file.canWrite();
	    } else {
	        try {
	            file.createNewFile();
	        } catch (IOException e) {
	            logger.debug("IOException thrown when testing write assuming failure");
	            return false;
	        }
	        // See java bug 4939819 (File.canWrite doesn't work properly on windows,
	        // so we would have to assume that the file is writable at this point)
	        // boolean canWrite = file.canWrite();
	        boolean canWrite = true;
	        file.delete();
	        return canWrite;
	    }
	}
    
    public String[] createCommandLine() {
        String javaHome = System.getProperty("java.home");
        String sep = System.getProperty("file.separator");
        String javaPath = javaHome + sep + "bin" + sep + "java";
        String className = getClass().getName();
        Long projectOid = project.getOid();
        
        return new String[] { javaPath, className, "project_id=" + projectOid };
    }

	public boolean isStarted() {
		return started;
	}

	public void setStarted(boolean started) {
		this.started = started;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}
		
    public void setMessageLevel(Level lev) {
    	messageLevel = lev;
    }
    
    public Level getMessageLevel() {
    	return messageLevel;
    }
    
	/**
	 * Returns true if the smtp host addresses are not
	 * null or empty, they are require to send the emails.
	 */
	protected boolean validateEmailSetting(MatchMakerSessionContext context) {
		boolean validate = false;
		String host = context.getEmailSmtpHost();
		validate = host != null &&
					host.length() > 0;
		return validate;
	}
	
	/**
	 * Fills each of the users lists for each status. 
	 */
	private void findEmailUsers() throws UnknownFreqCodeException,
			PLSecurityException, SQLException {
		Connection con = session.getConnection();
		greenUsers = EmailNotification.findEmailRecipients(con,
				this, EmailNotification.GREEN_STATUS);
		yellowUsers = EmailNotification.findEmailRecipients(con,
				this, EmailNotification.YELLOW_STATUS);
		redUsers = EmailNotification.findEmailRecipients(con,
				this, EmailNotification.RED_STATUS);
	}
	
	/**
	 * Creates and sets up the emails for each status.
	 */
	protected void setupEmail(MatchMakerSessionContext context) 
			throws Exception {
		findEmailUsers();
		
		String host = context.getEmailSmtpHost();
		
		email = new Email(host);
		email.setFromEmail(session.getAppUserEmail());
		email.setFromName(session.getAppUser());
	}

	public String getObjectName() {
		return getProject().getOid().toString();
	}

	/**
	 * Returns the number of rows in the source table of the project that this
	 * engine is working on.
	 * 
	 * @return
	 * @throws SQLException
	 */
	protected int getNumRowsToProcess() throws SQLException {
		Integer processCount;
		processCount = getProject().getMungeSettings().getProcessCount();
		int rowCount;
		Connection con = null;
		Statement stmt = null;
		try {
			con = getProject().createSourceTableConnection();
			
			stmt = con.createStatement();
			String rowCountSQL = "SELECT COUNT(*) AS ROW_COUNT FROM " + DDLUtils.toQualifiedName(getProject().getSourceTable());
			ResultSet result = stmt.executeQuery(rowCountSQL);
			logger.debug("Getting source table row count with SQL statment " + rowCountSQL);
			if (result.next()) {
				rowCount = result.getInt("ROW_COUNT");
			} else {
				throw new AssertionError("No rows came back from source table row count query!");
			}
		} finally {
			if (stmt != null) stmt.close();
			if (con != null) con.close();
		}
		if (processCount != null && processCount.intValue() > 0 && processCount.intValue() < rowCount) {
			rowCount = processCount.intValue();
		}
		return rowCount;
	}
}
