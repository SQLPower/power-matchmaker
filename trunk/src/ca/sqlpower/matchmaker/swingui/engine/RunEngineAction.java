package ca.sqlpower.matchmaker.swingui.engine;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import ca.sqlpower.matchmaker.MatchMakerEngine;
import ca.sqlpower.matchmaker.swingui.EditorPane;
import ca.sqlpower.matchmaker.swingui.MMSUtils;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;

/**
 * This action is used to run the match engine. It also is responsible
 * for constructing the user interface that deals with engine ouput.
 */
class RunEngineAction extends AbstractAction {
	
	/**
	 * The application swing session
	 */
	private MatchMakerSwingSession session;
	
	/**
	 * The panel for outputting the engine output
	 */
	private EngineOutputPanel engineOutputPanel;
	
	/**
	 * The MatchMakerEngine to be run
	 */
	private MatchMakerEngine engine;
	
	/**
	 * The EditorPane (ideally the engine panel) that the engine is being executed from.
	 */
	private EditorPane editorPane;
	
	public RunEngineAction(MatchMakerSwingSession session, MatchMakerEngine engine, String name, EngineOutputPanel engineOutputPanel, EditorPane editorPane) {
		super(name);
		this.session = session;
		this.engineOutputPanel = engineOutputPanel;
		this.engine = engine;
		this.editorPane = editorPane;
	}
	
	public void actionPerformed(ActionEvent e) {
		editorPane.doSave();
		try {
			EngineWorker w = new EngineWorker(engine, engineOutputPanel.getOutputDocument(), engineOutputPanel.getProgressBar(), session);
			new Thread(w).start();
		} catch (Exception ex) {
			MMSUtils.showExceptionDialog(engineOutputPanel.getOutputComponent(), "Engine error", ex);
			return;
		}
	}
}