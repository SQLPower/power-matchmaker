package ca.sqlpower.matchmaker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.sql.DefaultParameters;
import ca.sqlpower.sql.PLSchemaException;

/**
 * Sets up and runs the C Match Maker engine
 */
public class MatchMakerEngineImpl implements MatchMakerEngine {

	private static final Logger logger = Logger.getLogger(MatchMakerEngineImpl.class);
	
	/**
	 * the session that we are currently connectting to
	 */
	private MatchMakerSession session;
	private final MatchMakerSessionContext context;
	private Match match;
	private final MatchSettings settings;
	private Process proc;
	private Thread processMonitor;
	private Integer engineExitCode;

	public MatchMakerEngineImpl(MatchMakerSession session, Match match) {
		this.session = session;
		this.match = match;
		settings = match.getMatchSettings();
		context = session.getContext();
	}
	
	/* (non-Javadoc)
	 * @see ca.sqlpower.matchmaker.MatchMakerEngine#abort()
	 */
	public void abort() {
		if ( proc != null ) {
			proc.destroy();
			proc = null;
		}
	}

	/**
	 * returns true if the matchmaker engine version is good for the schema that
	 * we currently connect to. false if the engine version is too old. 
	 */
	static boolean validateMatchMakerEngineVersion() {
		// require change the engine
		return true;
	}
	
	/**
	 * returns true if the DEF_PARAM.EMAIL_NOTIFICATION_RETURN_ADRS and 
	 * DEF_PARAM.MAIL_SERVER_NAME column are not null or empty, they are
	 * require to run email_notification engine.                        
	 */
	static boolean validateEmailSetting(DefaultParameters def) {
		boolean validate;
		String emailAddress = def.getEmailReturnAddress();
		String smtpServer = def.getEmailServerName();
		validate = emailAddress != null &&
					emailAddress.length() > 0 &&
					smtpServer != null &&
					smtpServer.length() > 0;
		return validate;
	}
	
	/**
	 * returns true if the given file exists and executable, false otherwise.
	 * @param fileName  the name of the file you want to check.
	 */
	static boolean canExecuteFile(String fileName) {
		final File file = new File(fileName);
		// TODO: switch to file.canExecute when we have java 1.6
		return file.exists() && file.canRead();
	}

	static boolean canExecuteMatchEngine(MatchMakerSessionContext context) {
		return canExecuteFile(
				context.getMatchEngineLocation());
	}
	
	static boolean canExecuteEmailEngine(MatchMakerSessionContext context) {
		return canExecuteFile(
				context.getEmailEngineLocation());
	}
	
	/**
	 * returns true if the log file os this match is writable.
	 */
	static boolean canWriteLogFile(MatchMakerSettings settings) {
		return settings.getLog().isWritable();
	}

	/**
	 * returns true if the log file of this match is readable.
	 */
	static boolean canReadLogFile(MatchMakerSettings settings) {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see ca.sqlpower.matchmaker.MatchMakerEngine#checkPreConditions()
	 */
	public static boolean checkPreConditions(MatchMakerSession session, Match match) throws EngineSettingException {
		final MatchMakerSessionContext context = session.getContext();
		final MatchSettings settings = match.getMatchSettings();
		
		if ( context == null ) {
			throw new EngineSettingException(
					"PreCondition failed: session context must not be null");
		}
		
		if ( session.getDatabase() == null ) {
			throw new EngineSettingException(
					"PreCondition failed: database of the session must not be null");
		}
		if ( session.getDatabase().getDataSource() == null ) {
			throw new EngineSettingException(
					"PreCondition failed: data source of the session must not be null");
		}

		/**
		 * check the DSN setting for the current database connection, 
		 * that's required by the matchmaker odbc engine, since we will not 
		 * use this odbc engine forever, check for not null is acceptable for now. 
		 */
		final String odbcDsn = session.getDatabase().getDataSource().getOdbcDsn();
		if ( odbcDsn == null || odbcDsn.length() == 0 ) {
			throw new EngineSettingException(
					"ODBC DSN is missing in the Logical database connection setting," +
					" that's required by the matchmaker engine");
		}
		
		if (!canExecuteMatchEngine(session.getContext())) {
			throw new EngineSettingException(
					"The Matchmaker engine executable is not found.\n" +
					" It should be in the directory of pl.ini");
		}
		
		if (!validateMatchMakerEngineVersion()) {
			throw new EngineSettingException(
					"The matchmaker engine version is not up to date");
		}
		
		if (settings.getSendEmail() != null &&
				settings.getSendEmail().booleanValue() ) {
			if (!canExecuteEmailEngine(session.getContext())) {
				throw new EngineSettingException(
						"The email notification executable is not found.\n" +
				" It should be in the directory of pl.ini");
			}
			
			try {
				if (!validateEmailSetting(new DefaultParameters(session.getConnection()))) {
					throw new EngineSettingException(
							"missing email setting information," +
							" the email sender requires smtp server name and" +
							" returning email address!");
				}
			} catch (SQLException e) {
				throw new EngineSettingException("Cannot validate email settings",e);
			} catch (PLSchemaException e) {
				throw new EngineSettingException("Cannot validate email settings",e);
			}
		}
		
		try {
			if (!Match.doesSourceTableExist(session, match)) {
				throw new EngineSettingException("Source table does not exist!");
			}
			if (!Match.doesResultTableExist(session, match)) {
				throw new EngineSettingException("Result table does not exist!");
			}
		} catch (ArchitectException e) {
			throw new EngineSettingException("SQL Error. "+e.getMessage());
		}
		
		if (!canReadLogFile(settings)) {
			throw new EngineSettingException("The log file is not readable.");
		}
		
		if (!canWriteLogFile(settings)) {
			throw new EngineSettingException("The log file is not writable.");
		}
		return true;
	}

	
	public String createCommandLine(MatchMakerSession session, Match match, boolean userPrompt) {
		/*
		 * command line sample:
		 * "M:\Program Files\Power Loader Suite\Match_Oracle.exe"
		 * MATCH="MATCH_CTV_ORGS" USER=PL/pl@arthur_test DEBUG=Y
		 * TRUNCATE_CAND_DUP=N SEND_EMAIL=N APPEND_TO_LOG_IND=N
		 * LOG_FILE="M:\Program Files\Power Loader Suite\Power Loader\script\MATCH_MATCH_CTV_ORGS.log"
		 * SHOW_PROGRESS=10 PROCESS_CNT=1
		 */
		StringBuffer command = new StringBuffer();
		final SQLDatabase db = session.getDatabase();
		final MatchSettings settings = match.getMatchSettings();

		command.append("\"").append(context.getMatchEngineLocation()).append("\"");
		if ( logger.isDebugEnabled() ) {
			command.append(" -k ");
		}
		command.append(" MATCH=\"").append(match.getName()).append("\"");
		command.append(" USER=");
		command.append(db.getDataSource().getUser());
		command.append("/").append(db.getDataSource().getPass());
		command.append("@").append(db.getDataSource().getName());

		command.append(" DEBUG=").append(settings.getDebug() ? "Y" : "N");
		command.append(" TRUNCATE_CAND_DUP=").append(
				settings.getTruncateCandDupe() ? "Y" : "N");
		command.append(" SEND_EMAIL=").append(
				settings.getSendEmail() ? "Y" : "N");
		command.append(" APPEND_TO_LOG_IND=").append(
				settings.getAppendToLog() ? "Y" : "N");
		command.append(" LOG_FILE=\"").append(
				settings.getLog().getConstraint().toString()).append("\"");
		if ( settings.getShowProgressFreq() != null ) {
			command.append(" SHOW_PROGRESS=").append(settings.getShowProgressFreq());
		}
		if ( settings.getProcessCount() != null ) {
			command.append(" PROCESS_CNT=").append(settings.getProcessCount());
		}
		if ( !userPrompt ) {
			command.append(" USER_PROMPT=N");
		}
		return command.toString();
	}
	
	/* (non-Javadoc)
	 * @see ca.sqlpower.matchmaker.MatchMakerEngine#getEngineReturnCode()
	 */
	public Integer getEngineReturnCode() {
		return engineExitCode;
	}

	/* (non-Javadoc)
	 * @see ca.sqlpower.matchmaker.MatchMakerEngine#isRunning()
	 */
	public boolean isRunning() {
		if (processMonitor == null) {
			return false;
		} else {
			return processMonitor.isAlive();
		}
	}

	/* (non-Javadoc)
	 * @see ca.sqlpower.matchmaker.MatchMakerEngine#run()
	 */
	public void run() throws EngineSettingException, IOException {
		checkPreConditions();
		String commandLine = createCommandLine(session,match,false);
		Runtime rt = Runtime.getRuntime();
		logger.debug("Executing " + commandLine);
		proc = rt.exec(commandLine);
		processMonitor = new Thread(new Runnable(){
		
					public void run() {
						try {
							proc.waitFor();
							engineExitCode = proc.exitValue();
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						}				
					}
					
				});
		processMonitor.start();
	}

	public InputStream getEngineErrorOutput() {
		if ( proc != null ) {
			return proc.getErrorStream();
		}
		return null;
	}

	public InputStream getEngineStandardOutput() {
		if ( proc != null ) {
			return proc.getInputStream();
		}
		return null;
	}

	public boolean checkPreConditions() throws EngineSettingException {
		return checkPreConditions(session, match);
	}
}
