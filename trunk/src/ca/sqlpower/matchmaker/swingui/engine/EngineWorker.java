package ca.sqlpower.matchmaker.swingui.engine;

import java.awt.Component;
import java.io.IOException;

import javax.swing.JProgressBar;
import javax.swing.text.Document;

import org.apache.log4j.Appender;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.matchmaker.EngineSettingException;
import ca.sqlpower.matchmaker.MatchMakerEngine;
import ca.sqlpower.matchmaker.swingui.MMSUtils;
import ca.sqlpower.swingui.ProgressWatcher;
import ca.sqlpower.swingui.SPSwingWorker;
import ca.sqlpower.swingui.SwingWorkerRegistry;

/**
 * A SPSwingWorker implementation that runs a MatchMakerEngine.
 */
class EngineWorker extends SPSwingWorker {

	/**
	 * The MatchMakerEngine that the worker will run
	 */
	private final MatchMakerEngine engine;
	
	/**
	 * A document appender to append the engine output onto the engineOutputDoc
	 */
	private Appender appender;
	
	/**
	 * A Document to contain the engine output
	 */
	private final Document engineOutputDoc;
	
	/**
	 * A parent Component that would own the exception dialog if it gets displayed
	 */
	private Component parentComponent;
	
	/**
	 * @param engine The MatchMakerEngine that the worker will run
	 * @param engineOutputDoc
	 * @param progressBar The progress bar that will visualize the engine progress
	 * @param registry The SwingWorkerRegistry that this worker will register itself with
	 * @throws EngineSettingException
	 * @throws ArchitectException
	 */
	public EngineWorker(MatchMakerEngine engine, Document engineOutputDoc, JProgressBar progressBar, SwingWorkerRegistry registry) throws EngineSettingException, ArchitectException {
		super(registry);
		this.engine = engine;
		this.engineOutputDoc = engineOutputDoc;
		this.parentComponent = progressBar;
		engine.checkPreconditions();
		ProgressWatcher.watchProgress(progressBar, engine);
	}
	
	@Override
	public void doStuff() throws EngineSettingException, IOException {
		appender = new DocumentAppender(engineOutputDoc);
		engine.getLogger().addAppender(appender);
		engine.call();
	}

	@Override
	public void cleanup() throws Exception {
		if (getDoStuffException() != null) {
			MMSUtils.showExceptionDialog(parentComponent, "Error during engine run", getDoStuffException());
			engine.getLogger().error("Error during engine run", getDoStuffException());
		}
		engine.getLogger().removeAppender(appender);
	}
	
}