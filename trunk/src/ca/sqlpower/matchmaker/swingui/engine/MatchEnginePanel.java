/*
 * Copyright (c) 2007, SQL Power Group Inc.
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

package ca.sqlpower.matchmaker.swingui.engine;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.Document;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.matchmaker.EngineSettingException;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MatchEngineImpl;
import ca.sqlpower.matchmaker.MatchMakerEngine;
import ca.sqlpower.matchmaker.MatchSettings;
import ca.sqlpower.matchmaker.dao.MatchMakerDAO;
import ca.sqlpower.matchmaker.swingui.EditorPane;
import ca.sqlpower.matchmaker.swingui.MMSUtils;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.matchmaker.swingui.action.ShowMatchStatisticInfoAction;
import ca.sqlpower.swingui.BrowseFileAction;
import ca.sqlpower.swingui.ProgressWatcher;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.SPSwingWorker;
import ca.sqlpower.swingui.SPSUtils.FileExtensionFilter;
import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;
import ca.sqlpower.validation.Validator;
import ca.sqlpower.validation.swingui.FormValidationHandler;
import ca.sqlpower.validation.swingui.StatusComponent;

import com.darwinsys.notepad.Notepad;
import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * An editor pane to allow the user to configure, run and monitor the engine.
 */
public class MatchEnginePanel implements EditorPane {

	private static final Logger logger = Logger.getLogger(MatchEnginePanel.class);

	/**
	 * The session this MatchEnginePanel belongs to.
	 */
	private final MatchMakerSwingSession swingSession;

	/**
	 * The file path to where the match engine is located.
	 */
	private JTextField enginePath;

	/**
	 * The file path to which the engine logs will be written to.
	 * Must be a valid file path that the user has write permissions on.
	 */
	private JTextField logFilePath;

	/**
	 * Opens a file chooser for the user to select the log file they wish
	 * to use for engine output.
	 */
	private BrowseFileAction browseLogFileAction;

	/**
	 * Opens a file chooser for the user to select the engine they want
	 * to use.
	 */
	private BrowseFileAction browseEngineFileAction;

	/**
	 * Denotes whether or not the log file should be overwritten or
	 * appended to.
	 */
	private JCheckBox appendToLog;

	/**
	 * A field for the user to specify how many records they want the
	 * engine to process.
	 */
	private JTextField recordsToProcess;

	/**
	 * A flag for the engine to run in debug mode or not.
	 */
	private JCheckBox debugMode;

	/**
	 * A flag for telling the engine to delete all the records from the match result table before running the match
	 */
	private JCheckBox clearMatchPool;

	/**
	 * A flag for the engine to send emails or not.
	 */
	private JCheckBox sendEmail;

	/**
	 * Select which Oracle rollback segment to store the data necessary
	 * to preform a rollback.
	 * FIXME: This only makes sense for Oracle, so once the engine supports other platforms, this
	 * should be hidden or disabled
	 */
	private JComboBox rollbackSegment;

	/**
	 * The frame that this editor lives in.
	 */
	private JFrame parentFrame;

	/**
	 * The match object the engine should run against.
	 */
	private Match match;
	
	/**
	 * The panel that holds all components of this EditorPane.
	 */
	private JPanel panel;

	/**
	 * Displays the validation status of the match engine preconditions.
	 */
	private StatusComponent status = new StatusComponent();

	/**
	 * The validation handler used to validate the configuration portion
	 * of the editor pane.
	 */
	private FormValidationHandler handler;

	public MatchEnginePanel(MatchMakerSwingSession swingSession, Match match,
			JFrame parentFrame) {
		this.swingSession = swingSession;
		this.parentFrame = parentFrame;
		this.match = match;
		handler = new FormValidationHandler(status);
		handler.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				refreshActionStatus();
			}
		});
		engineOutputPanel = new EngineOutputPanel(parentFrame);
		panel = buildUI();
		setDefaultSelections(match);
	}

	/**
	 * Performs a form validation on the configuration portion and sets the
	 * status accordingly as well as disabling the button to run the engine if
	 * necessary.
	 */
	private void refreshActionStatus() {
		ValidateResult worst = handler.getWorstValidationStatus();
		engineAction.setEnabled(true);

		if (worst.getStatus() == Status.FAIL) {
			engineAction.setEnabled(false);
		}
	}

	private final class SaveAction extends AbstractAction {
		private SaveAction(String name) {
			super(name);
		}

		public void actionPerformed(ActionEvent e) {
			doSave();
		}
	}

	/**
	 * Builds the UI for this editor pane. This is broken into two parts,
	 * the configuration and output. Configuration is done in this method
	 * while the output section is handled by the EngineOutputPanel and
	 * this method simply lays out the components that class provides.
	 */
	private JPanel buildUI() {
		FormLayout layout = new FormLayout(
				"4dlu,fill:pref,4dlu,fill:pref:grow, pref,4dlu,pref,4dlu",
				//  1         2    3         4     5     6    7     8
				"10dlu,pref,10dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu");
		        //   1    2     3    4    5    6    7    8    9   10   11   12   13   14   15   16   17   18   19   20   21   22   23
		PanelBuilder pb;
		JPanel p = logger.isDebugEnabled() ? new FormDebugPanel(layout)
				: new JPanel(layout);
		pb = new PanelBuilder(layout, p);

		CellConstraints cc = new CellConstraints();

		logFilePath = new JTextField();
		browseLogFileAction = new BrowseFileAction(parentFrame, logFilePath);
		enginePath = new JTextField();
		browseEngineFileAction = new BrowseFileAction(parentFrame, enginePath);
		rollbackSegment = new JComboBox();
		appendToLog = new JCheckBox("Append to old Log File?");
		recordsToProcess = new JTextField(5);
		debugMode = new JCheckBox("Debug Mode?");
		clearMatchPool = new JCheckBox("Clear match pool?");
		sendEmail = new JCheckBox("Send E-mails?");
		pb.add(status, cc.xyw(4, 2, 5, "l,c"));

		int y = 4;
		pb.add(new JLabel("Engine Location:"), cc.xy(2, y, "r,f"));
		pb.add(enginePath, cc.xy(4, y, "f,f"));
		pb.add(new JButton(browseEngineFileAction), cc.xy(5, y, "r,f"));
		
		y += 2;
		pb.add(new JLabel("Log File:"), cc.xy(2, y, "r,f"));
		pb.add(logFilePath, cc.xy(4, y, "f,f"));
		pb.add(new JButton(browseLogFileAction), cc.xy(5, y, "r,f"));
		pb.add(appendToLog, cc.xy(7, y, "l,f"));

		y += 2;
		pb.add(new JLabel("Rollback Segment:"), cc.xy(2, y, "r,c"));
		pb.add(rollbackSegment, cc.xy(4, y));
		
		y += 2;
		pb.add(new JLabel("Records to Process:"), cc.xy(2, y, "r,c"));
		pb.add(recordsToProcess, cc.xy(4, y, "l,c"));
		
		y += 2;
		pb.add(debugMode, cc.xy(4, y, "l,c"));

		y += 2;
		pb.add(clearMatchPool, cc.xy(4, y, "l,c"));
		
		y += 2;
		pb.add(sendEmail, cc.xy(4, y, "l,c"));

		FormLayout bbLayout = new FormLayout(
				"4dlu,pref,4dlu,pref,4dlu,pref,4dlu",
				"4dlu,pref,4dlu,pref,4dlu,pref,4dlu");
		PanelBuilder bbpb;
		JPanel bbp = logger.isDebugEnabled() ? new FormDebugPanel(bbLayout)
				: new JPanel(bbLayout);
		bbpb = new PanelBuilder(bbLayout, bbp);
		bbpb.add(new JButton(new ShowLogFileAction()), cc.xy(2, 2, "f,f"));
		bbpb.add(new JButton(new ShowCommandAction(match,
				parentFrame)), cc.xy(4, 2, "f,f"));
		bbpb.add(new JButton(engineAction), cc.xy(6, 2, "f,f"));
		bbpb.add(new JButton(new ShowMatchStatisticInfoAction(swingSession,
				match, getParentFrame())), cc.xy(2, 4, "f,f"));
		bbpb.add(new JButton(new SaveAction("Save")), cc.xy(4, 4, "f,f"));

		pb.add(bbpb.getPanel(), cc.xyw(2, 18, 6, "r,c"));

		JPanel engineAccessoryPanel = new JPanel(new BorderLayout());
		engineAccessoryPanel.add(engineOutputPanel.getProgressBar(), BorderLayout.NORTH);
		engineAccessoryPanel.add(engineOutputPanel.getButtonBar(), BorderLayout.SOUTH);
		
		JPanel anotherP = new JPanel(new BorderLayout(12, 12));
		anotherP.add(pb.getPanel(), BorderLayout.NORTH);
		anotherP.add(engineOutputPanel.getOutputComponent(), BorderLayout.CENTER);
		anotherP.add(engineAccessoryPanel, BorderLayout.SOUTH);
		anotherP.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		
		return anotherP;
	}

	/**
	 * Auto-populates check boxes and text fields using the MatchSettings
	 * object in <code>match</code>. Also performs form validation to
	 * initialize the status icon at the top of the configuration portion.
	 */
	private void setDefaultSelections(Match match) {

		/*
		 * we put the validators here because we want to validate the form right
		 * after it being loaded
		 */
		Validator v1 = new LogFileNameValidator();
		handler.addValidateObject(logFilePath, v1);

		Validator v2 = new FileExistsValidator("Match engine");
		handler.addValidateObject(enginePath, v2);

		MatchSettings settings = match.getMatchSettings();
		
		if (settings.getLog() == null) {
			settings.setLog(new File(match.getName() + ".log"));
		}
		File logFile = settings.getLog();
		logFilePath.setText(logFile.getAbsolutePath());
		
		enginePath.setText(swingSession.getContext().getMatchEngineLocation());

		appendToLog.setSelected(settings.getAppendToLog());
		if (settings.getProcessCount() == null) {
			recordsToProcess.setText("");
		} else {
			recordsToProcess
					.setText(String.valueOf(settings.getProcessCount()));
		}
		
		logger.debug("append to log? "+appendToLog);
		debugMode.setSelected(settings.getDebug());
		clearMatchPool.setSelected(settings.getTruncateCandDupe());
		sendEmail.setSelected(settings.getSendEmail());

		// TODO add support for selecting the Oracle rollback segment
		rollbackSegment.setSelectedItem(settings.getRollbackSegmentName());
		debugMode.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (((JCheckBox) e.getSource()).isSelected()) {
					clearMatchPool.setSelected(true);
					recordsToProcess.setText("1");
				} else {
					clearMatchPool.setSelected(false);
					recordsToProcess.setText("0");
				}
			}
		});
		
		/* trigger the validator */
//		v2.validate(sendEmail.isSelected());
	}

	/**
	 * A worker implementation that runs a MatchMakerEngine.
	 */
	private class EngineWorker extends SPSwingWorker {

		private final MatchMakerEngine matchEngine;
		
		private Appender appender;
		
		private final Document engineOutputDoc;
		
		EngineWorker(Document engineOutputDoc, JProgressBar progressBar) throws EngineSettingException, ArchitectException {
			super(swingSession);
			matchEngine = new MatchEngineImpl(swingSession, match);
			matchEngine.checkPreconditions();
			this.engineOutputDoc = engineOutputDoc;
			ProgressWatcher.watchProgress(progressBar,matchEngine);
		}
		
		@Override
		public void doStuff() throws EngineSettingException, IOException {
			appender = new DocumentAppender(engineOutputDoc);
			matchEngine.getLogger().addAppender(appender);
			matchEngine.call();
		}

		@Override
		public void cleanup() throws Exception {
			if (getDoStuffException() != null) {
				MMSUtils.showExceptionDialog(parentFrame, "Error during engine run", getDoStuffException());
				matchEngine.getLogger().error("Error during engine run", getDoStuffException());
			}
			matchEngine.getLogger().removeAppender(appender);
		}
		
	}
	
	private final EngineOutputPanel engineOutputPanel;
	
	/**
	 * This action is used to run the match engine. It also is responsible
	 * for constructing the user interface that deals with engine ouput.
	 */
	private Action engineAction = new AbstractAction("Run Match Engine") {

		public void actionPerformed(ActionEvent e) {
			doSave();
			try {
				EngineWorker w = new EngineWorker(engineOutputPanel.getOutputDocument(), engineOutputPanel.getProgressBar());
				new Thread(w).start();
			} catch (Exception ex) {
				MMSUtils.showExceptionDialog(parentFrame, "Engine error", ex);
				return;
			}
		}
	};

	/**
	 * Displays a dialog to the user that contains the text in the log file
	 * denoted by logFilePath.
	 * 
	 * XXX: WARNING! When this dialog is closed, the whole program exits as
	 * well!
	 */
	private class ShowLogFileAction extends AbstractAction {

		public ShowLogFileAction() {
			super("Show Log File");
		}

		public void actionPerformed(ActionEvent e) {
			String logFileName = logFilePath.getText();
			try {
				/**
				 * Notepad has its own frame and should be modified to allow an
				 * icon argument in the constructor
				 */
				new Notepad().doLoad(logFileName);
			} catch (IOException e1) {
				throw new RuntimeException("Unable to view log file "
						+ logFileName, e1);
			}

		}
	}

	/**
	 * Displays a dialog to the user that contains the command-line command
	 * that will be used to invoke the engine.
	 */
	private class ShowCommandAction extends AbstractAction {

		private Match match;

		private JFrame parent;

		public ShowCommandAction(Match match, JFrame parent) {
			super("Show Command");
			this.match = match;
			this.parent = parent;
		}

		public void actionPerformed(ActionEvent e) {
			doSave();
			MatchMakerEngine engine = new MatchEngineImpl(swingSession,
					match);
			final String[] cmd = engine.createCommandLine(swingSession, match, false);
			final JDialog d = new JDialog(parent,
					"MatchMaker Engine Command Line");

			FormLayout layout = new FormLayout(
					"4dlu,fill:min(pref;200dlu):grow,4dlu", // columns
					"4dlu,fill:min(pref;200dlu):grow,4dlu,pref,4dlu"); // rows
			// 1 2 3 4 5

			PanelBuilder pb;
			JPanel p = logger.isDebugEnabled() ? new FormDebugPanel(layout)
					: new JPanel(layout);
			pb = new PanelBuilder(layout, p);
			CellConstraints cc = new CellConstraints();

			final JTextArea cmdText = new JTextArea(15, 120);
			for (String arg : cmd) {
				boolean hasSpace = arg.contains(" ");
				if (hasSpace) {
					cmdText.append("\"");
				}
				cmdText.append(arg);
				if (hasSpace) {
					cmdText.append("\"");
				}
				cmdText.append(" ");
			}
			cmdText.setEditable(false);
			cmdText.setWrapStyleWord(true);
			cmdText.setLineWrap(true);
			pb.add(new JScrollPane(cmdText), cc.xy(2, 2, "f,f"));

			ButtonBarBuilder bbBuilder = new ButtonBarBuilder();

			Action saveAsAction = new AbstractAction("Save As...") {
				public void actionPerformed(ActionEvent e) {
					SPSUtils.saveDocument(d, cmdText.getDocument(),
							(FileExtensionFilter) SPSUtils.BATCH_FILE_FILTER);
				}
			};
			JButton saveAsButton = new JButton(saveAsAction);
			bbBuilder.addGridded(saveAsButton);
			bbBuilder.addRelatedGap();

			JButton copyButton = new JButton(new AbstractAction(
					"Copy to Clipboard") {
				public void actionPerformed(ActionEvent e) {
					StringSelection selection = new StringSelection(cmdText.getText());
					Clipboard clipboard = Toolkit.getDefaultToolkit()
							.getSystemClipboard();
					clipboard.setContents(selection, selection);
				}
			});
			bbBuilder.addGridded(copyButton);
			bbBuilder.addRelatedGap();
			bbBuilder.addGlue();

			JButton cancelButton = new JButton(new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					d.setVisible(false);
				}
			});
			cancelButton.setText("Close");
			bbBuilder.addGridded(cancelButton);

			pb.add(bbBuilder.getPanel(), cc.xy(2, 4));
			d.add(pb.getPanel());
			SPSUtils.makeJDialogCancellable(d, null);
			d.pack();
			d.setVisible(true);
		}

	}

	/**
	 * A Validator to ensure that the supplied log filepath is a valid
	 * file and that the user has permission to write to it.
	 */
	private class LogFileNameValidator implements Validator {
		public ValidateResult validate(Object contents) {
			String name = (String) contents;
			if (name == null || name.length() == 0) {
				return ValidateResult.createValidateResult(Status.FAIL,
						"Log file is required.");
			}
			File log = new File(name);
			if (log.exists()) {
				if (!log.isFile()) {
					return ValidateResult.createValidateResult(Status.FAIL,
							"Log file name is invalid.");
				}

				// can't reliably check if file is writable on Windows,
				// so we'll just assume it is.
			} else {
				try {
					if (!log.createNewFile()) {
						return ValidateResult.createValidateResult(Status.FAIL,
								"Log file can not be created.");
					}
				} catch (IOException e) {
					return ValidateResult.createValidateResult(Status.FAIL,
							"Log file can not be created.");
				} finally {
					log.delete();
				}
			}
			return ValidateResult.createValidateResult(Status.OK, "");
		}

	}

	private JFrame getParentFrame() {
		return parentFrame;
	}
	
	/*===================== EditorPane implementation ==================*/

	public JPanel getPanel() {
		return panel;
	}
	
	public boolean hasUnsavedChanges() {
		//XXX This is stubbed for now, should look over the check boxes
		//and text fields in the configuration section
		return false;
	}
	
	/**
	 * Updates the engine settings in the match based on the current values in
	 * the GUI, then stores the match using its DAO.
	 */
	public boolean doSave() {
		refreshActionStatus();
		MatchSettings settings = match.getMatchSettings();
		settings.setDebug(debugMode.isSelected());
		settings.setTruncateCandDupe(clearMatchPool.isSelected());
		settings.setSendEmail(sendEmail.isSelected());
		swingSession.getContext().setMatchEngineLocation(enginePath.getText());
		settings.setLog(new File(logFilePath.getText()));
		settings.setAppendToLog(appendToLog.isSelected());
		if (recordsToProcess.getText() == null
				|| recordsToProcess.getText().length() == 0) {
			settings.setProcessCount(null);
		} else {
			settings.setProcessCount(Integer
					.valueOf(recordsToProcess.getText()));
		}
		
		MatchMakerDAO<Match> dao = swingSession.getDAO(Match.class);
		dao.save(match);

		return true;
	}
}