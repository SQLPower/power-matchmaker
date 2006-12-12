package ca.sqlpower.matchmaker;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.matchmaker.event.EngineEvent;
import ca.sqlpower.matchmaker.event.EngineListener;
import ca.sqlpower.matchmaker.event.EngineEvent.EngineEventType;
/**
 * Common ground for all C engines.  This class handles events
 * output capture, monitoring and starting and stoping the engine. 
 *
 */
public abstract class AbstractCEngine implements MatchMakerEngine {

	private final static Logger logger = Logger.getLogger(AbstractCEngine.class);
	/**
	 * the session that we are currently connectting to
	 */
	private MatchMakerSession session;
	private Match match;
	private Process proc;
	private Thread processMonitor;
	private Integer engineExitCode;
	
	public abstract boolean checkPreconditions() throws EngineSettingException; 
	
		
	protected Match getMatch() {
		return match;
	}


	protected void setMatch(Match match) {
		this.match = match;
	}


	protected MatchMakerSession getSession() {
		return session;
	}


	protected void setSession(MatchMakerSession session) {
		this.session = session;
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
		checkPreconditions();
		if (proc!=null) throw new IllegalStateException("Engine has already been run");
		String commandLine = createCommandLine(session,match,false);
		Runtime rt = Runtime.getRuntime();
		logger.debug("Executing " + commandLine);
		proc = rt.exec(commandLine);
		fireEngineStart();
		processMonitor = new Thread(new Runnable(){

					public void run() {
						try {
							proc.waitFor();
							engineExitCode = proc.exitValue();
							fireEngineEnd();
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

	/** ENGINE EVENT SUPPORT **/
	private List<EngineListener> engineListeners = new ArrayList<EngineListener>();
	
	public void addEngineListener(EngineListener l){
		if (l == null) throw new NullPointerException();
		engineListeners.add(l);
	}
	public void removeEngineListener(EngineListener l){
		engineListeners.remove(l);
	}
	
	void fireEngineStart() {
		for (int i = engineListeners.size()-1; i >= 0; i--){
			EngineEvent e = new EngineEvent(this,EngineEventType.ENGINE_START,match);
			engineListeners.get(i).engineStart(e);
		}
	}

	void fireEngineEnd() {
		for (int i = engineListeners.size()-1; i >= 0; i--){
			EngineEvent e = new EngineEvent(this,EngineEventType.ENGINE_START,match);
			engineListeners.get(i).engineEnd(e);
		}
	}

	///////// Monitorabe support ///////////
	/**
	 * Right now the job size is always indeterminant
	 */
	public Integer getJobSize() throws ArchitectException {
		return null;
	}

	public String getMessage() {
		if(isRunning()){
			return "Running MatchMaker Engine";
		} else {
			return "";
		}
	}

	public int getProgress() throws ArchitectException {
		// since this is always indeterminant  
		return 0;
	}

	public boolean hasStarted() throws ArchitectException {
		return isRunning() || getEngineReturnCode() != null;
	}
	
	// The engine is done when it has an exit code
	public boolean isFinished() throws ArchitectException {
		if (getEngineReturnCode() != null){
			return true;
		} else {
			return false;
		}
	}

	public void setCancelled(boolean cancelled) {
		abort();
	}
}
