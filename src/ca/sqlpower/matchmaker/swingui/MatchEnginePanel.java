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

package ca.sqlpower.matchmaker.swingui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.sql.RowSet;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.EngineSettingException;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MatchMakerEngine;
import ca.sqlpower.matchmaker.MatchEngineImpl;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchSettings;
import ca.sqlpower.matchmaker.RowSetModel;
import ca.sqlpower.matchmaker.dao.MatchMakerDAO;
import ca.sqlpower.matchmaker.event.EngineEvent;
import ca.sqlpower.matchmaker.event.EngineListener;
import ca.sqlpower.matchmaker.swingui.action.ShowMatchStatisticInfoAction;
import ca.sqlpower.swingui.ProgressWatcher;
import ca.sqlpower.swingui.SPSUtils;
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
	 * The session this RunMatchEditor belongs to.
	 */
	private final MatchMakerSwingSession swingSession;

	/**
	 * The file path to which the engine logs will be written to.
	 * Must be a valid file path that the user has write permissions on.
	 */
	private JTextField logFilePath;

	private String lastAccessPath;

	/**
	 * A button to open up a file chooser so the user can select a file
	 * instead of having to type in the whole file path by hand.
	 */
	private JButton browse;

	/**
	 * Denotes whether or not the log file should be overwritten or
	 * appended to.
	 */
	private JCheckBox append;

	/**
	 * A field for the user to specify how many records they want the
	 * engine to process.
	 */
	private JTextField recordsToProcess;

	private JTextField minWord;

	/**
	 * A flag for the engine to run in debug mode or not.
	 */
	private JCheckBox debugMode;

	private JCheckBox truncateCandDup;

	/**
	 * A flag for the engine to send emails or not.
	 */
	private JCheckBox sendEmail;

	/**
	 * Used to open a dialog that shows the user the log file
	 * denoted by the {@link #logFilePath}.
	 */
	private JButton viewLogFile;

	private JButton viewStats;

	/**
	 * Opens a dialog displaying the command-line command that
	 * will be used to invoke the engine.
	 */
	private JButton showCommand;

	/**
	 * Saves the current configuration such as checkbox selection
	 * and log filepath.
	 */
	private JButton save;

	/**
	 * Starts the engine. Disabled if engine preconditions are not met.
	 */
	private JButton runMatchEngineButton;

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

	/**
	 * The action that is used to start the match engine as well
	 * as provide information about the progress and status of the engine
	 */
	private final RunEngineAction runEngineAction;

	public MatchEnginePanel(MatchMakerSwingSession swingSession, Match match,
			JFrame parentFrame) {
		this.swingSession = swingSession;
		this.parentFrame = parentFrame;
		this.match = match;
		runEngineAction = new RunEngineAction(swingSession, match,
				parentFrame);
		handler = new FormValidationHandler(status);
		handler.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				refreshActionStatus();
			}
		});
		panel = buildUI();
		setDefaultSelections(match);
	}

	/**
	 * Performs a form validation on the configuration portion and sets the
	 * status accordingly as well as diabling the button to run the engine if
	 * necessary.
	 */
	private void refreshActionStatus() {
		ValidateResult worst = handler.getWorstValidationStatus();
		runEngineAction.setEnabled(true);

		if (worst.getStatus() == Status.FAIL) {
			runEngineAction.setEnabled(false);
		}
	}

	/**
	 * Opens a file chooser for the user to select the log file they wish
	 * to use for engine output.
	 */
	private Action browseFileAction = new AbstractAction("...") {

		public void actionPerformed(ActionEvent e) {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setCurrentDirectory(new File(lastAccessPath));
			fileChooser.setSelectedFile(new File(lastAccessPath));
			int returnVal = fileChooser.showOpenDialog(parentFrame);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				final File file = fileChooser.getSelectedFile();
				logFilePath.setText(file.getPath());
				lastAccessPath = file.getAbsolutePath();
			}
		}

	};

	/**
	 * Builds the UI for this editor pane. This is broken into two parts,
	 * the configuration and output. Configuration is done in this method
	 * while the output section is done in the RunEngineAction constructor
	 * and retrieved when needed because the ouput section depends on some
	 * things that only the action knows about.
	 */
	private JPanel buildUI() {
		FormLayout layout = new FormLayout(
				"4dlu,fill:pref,4dlu,fill:pref:grow, pref,4dlu,pref,4dlu",
				//  1         2    3         4     5     6    7     8
				"10dlu,pref,10dlu,pref,10dlu,pref,3dlu,pref,3dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu");
		        //   1    2     3    4     5    6    7    8    9   10    11   12   13   14   15
		PanelBuilder pb;
		JPanel p = logger.isDebugEnabled() ? new FormDebugPanel(layout)
				: new JPanel(layout);
		pb = new PanelBuilder(layout, p);

		CellConstraints cc = new CellConstraints();

		logFilePath = new JTextField();
		rollbackSegment = new JComboBox();
		browse = new JButton(browseFileAction);
		append = new JCheckBox("Append to old Log File?");
		recordsToProcess = new JTextField(5);
		minWord = new JTextField(5);
		minWord.setText("0");
		debugMode = new JCheckBox("Debug Mode?");
		truncateCandDup = new JCheckBox("Clear match pool?");
		sendEmail = new JCheckBox("Send E-mails?");
		viewLogFile = new JButton(new ShowLogFileAction());
		viewStats = new JButton(new ShowMatchStatisticInfoAction(swingSession,
				match, getParentFrame()));
		viewStats.setText("Match Statistics");
		showCommand = new JButton(new ShowCommandAction(match,
				parentFrame));

		save = new JButton(new AbstractAction("Save") {
			public void actionPerformed(ActionEvent e) {
				doSave();
			}
		});

		runMatchEngineButton = new JButton(runEngineAction);

		pb.add(status, cc.xyw(4, 2, 5, "l,c"));

		pb.add(new JLabel("Log File:"), cc.xy(2, 4, "r,f"));
		pb.add(logFilePath, cc.xy(4, 4, "f,f"));
		pb.add(browse, cc.xy(5, 4, "r,f"));
		pb.add(append, cc.xy(7, 4, "l,f"));

		pb.add(new JLabel("Rollback Segment:"), cc.xy(2, 6, "r,c"));
		pb.add(rollbackSegment, cc.xy(4, 6));
		pb.add(new JLabel("Records to Process:"), cc.xy(2, 8, "r,c"));
		pb.add(recordsToProcess, cc.xy(4, 8, "l,c"));
		pb.add(new JLabel("Min Word Count Freq:"), cc.xy(2, 10, "r,c"));
		pb.add(minWord, cc.xy(4, 10, "l,c"));
		pb.add(debugMode, cc.xy(4, 12, "l,c"));
		pb.add(truncateCandDup, cc.xy(4, 14, "l,c"));
		pb.add(sendEmail, cc.xy(4, 16, "l,c"));

		FormLayout bbLayout = new FormLayout(
				"4dlu,pref,4dlu,pref,4dlu,pref,4dlu",
				"4dlu,pref,4dlu,pref,4dlu,pref,4dlu");
		PanelBuilder bbpb;
		JPanel bbp = logger.isDebugEnabled() ? new FormDebugPanel(bbLayout)
				: new JPanel(bbLayout);
		bbpb = new PanelBuilder(bbLayout, bbp);
		bbpb.add(viewLogFile, cc.xy(2, 2, "f,f"));
		bbpb.add(showCommand, cc.xy(4, 2, "f,f"));
		bbpb.add(runMatchEngineButton, cc.xy(6, 2, "f,f"));
		bbpb.add(viewStats, cc.xy(2, 4, "f,f"));
		bbpb.add(save, cc.xy(4, 4, "f,f"));

		pb.add(bbpb.getPanel(), cc.xyw(2, 18, 6, "r,c"));

		JPanel engineAccessoryPanel = new JPanel(new BorderLayout());
		engineAccessoryPanel.add(runEngineAction.getProgressBar(), BorderLayout.NORTH);
		engineAccessoryPanel.add(runEngineAction.getButtonBar(), BorderLayout.SOUTH);
		
		JPanel anotherP = new JPanel(new BorderLayout(12, 12));
		anotherP.add(pb.getPanel(), BorderLayout.NORTH);
		anotherP.add(runEngineAction.getOutputComponent(), BorderLayout.CENTER);
		anotherP.add(engineAccessoryPanel, BorderLayout.SOUTH);
		anotherP.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		
		return anotherP;
	}

	/**
	 * Used to auto-populate check boxes and text fields using the MatchSettings
	 * object in <code>match</code>. Also performs form validation to
	 * intialize the status icon at the top of the configuration portion.
	 */
	private void setDefaultSelections(Match match) {

		/*
		 * we put the validators here because we want to validate the form right
		 * after it being loaded
		 */
		Validator v1 = new LogFileNameValidator();
		handler.addValidateObject(logFilePath, v1);

		Validator v2 = new MatchAndMatchEngineValidator(swingSession, match);
		handler.addValidateObject(sendEmail, v2);

		MatchSettings settings = match.getMatchSettings();
		String logFileName;
		if ( settings.getLog() != null ) {
			logFileName = (settings.getLog().getPath());
		} else {
			logFileName = match.getName() + ".log";
		}
		File file;
		if (lastAccessPath != null && lastAccessPath.length() > 0) {
			file = new File(lastAccessPath, logFileName);
		} else {
			file = new File(logFileName);
		}
		if ( match.getMatchSettings().getLog() == null) {
			match.getMatchSettings().setLog(file);
		}
		lastAccessPath = file.getAbsolutePath();
		if (logFileName != null) {
			logFilePath.setText(logFileName);
		}

		Boolean appendToLog = settings.getAppendToLog();
		append.setSelected(appendToLog);
		if (settings.getProcessCount() == null) {
			recordsToProcess.setText("");
		} else {
			recordsToProcess
					.setText(String.valueOf(settings.getProcessCount()));
		}
		
		logger.debug("append to log? "+append);
		debugMode.setSelected(settings.getDebug());
		truncateCandDup.setSelected(settings.getTruncateCandDupe());
		sendEmail.setSelected(settings.getSendEmail());

		// TODO add roll back segment
		rollbackSegment.setSelectedItem(settings.getRollbackSegmentName());
		debugMode.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (((JCheckBox) e.getSource()).isSelected()) {
					truncateCandDup.setSelected(true);
					recordsToProcess.setText("1");
				} else {
					truncateCandDup.setSelected(false);
					recordsToProcess.setText("0");
				}
			}
		});
		
		/* trigger the validator */
		v2.validate(sendEmail.isSelected());
	}

	public class StatsTableMOdel extends RowSetModel {

		public StatsTableMOdel(RowSet set) {
			super(set);
		}

	}

	public class StreamGobbler extends Thread {
		InputStream is;

		String type;

		AbstractDocument output;

		SimpleAttributeSet att;

		StreamGobbler(InputStream is, String type, AbstractDocument output,
				SimpleAttributeSet att) {
			this.is = is;
			this.type = type;
			this.output = output;
			this.att = att;
		}

		public void run() {
			try {
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line = null;
				while ((line = br.readLine()) != null) {
					logger.debug(type + ">" + line);
					final String fLine = line;
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							try {
								output.insertString(output.getLength(), fLine
										+ "\n", att);
							} catch (BadLocationException e) {
								e.printStackTrace();
							}
						}
					});
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	/**
	 * This action is used to run the match engine. It also is responsible
	 * for constructing the user interface that deals with engine ouput.
	 */
	private class RunEngineAction extends AbstractAction implements
			EngineListener {

		private final JFrame parent;

		SimpleAttributeSet stdoutAtt = new SimpleAttributeSet();

		SimpleAttributeSet stderrAtt = new SimpleAttributeSet();

		private MatchMakerEngine matchEngine;
		private MatchMakerSession session;
		private Match match;
		
		/**
		 * A bar of buttons related to the match engine output textfield.
		 */
		private JPanel buttonBar;

		/**
		 * The actual document that contains the engine's output.
		 */
		private DefaultStyledDocument engineOutputDoc;
		
		/**
		 * The component that houses the engine output field.
		 */
		private JScrollPane outputComponent;
		
		private JProgressBar progressBar;

		public RunEngineAction(MatchMakerSession session, Match match,
				JFrame parent) {
			super("Run Match Engine");
			this.parent = parent;
			this.session = session;
			this.match = match;
			initGUI();
			logger.debug("RunEngineAction instance created");
		}

		/**
		 * Sets up all the GUI components.
		 */
		private void initGUI() {
			StyleConstants.setForeground(stdoutAtt, Color.black);
			StyleConstants.setFontFamily(stdoutAtt, "Courier New");
			StyleConstants.setForeground(stderrAtt, Color.red);
			progressBar = new JProgressBar();

			GraphicsEnvironment ge = GraphicsEnvironment
			.getLocalGraphicsEnvironment();
			Font[] fonts = ge.getAllFonts();
			boolean courierNewExist = false;
			for (int i = 0; i < fonts.length; i++) {
				if (fonts[i].getFamily().equalsIgnoreCase("Courier New")) {
					courierNewExist = true;
					break;
				}
			}
			engineOutputDoc = new DefaultStyledDocument();

			JTextArea outputTextArea = new JTextArea(20, 80);
			outputTextArea.setDocument(engineOutputDoc);
			outputTextArea.setEditable(false);
			outputTextArea.setWrapStyleWord(true);
			outputTextArea.setLineWrap(true);
			outputTextArea.setAutoscrolls(true);

			if (courierNewExist) {
				Font oldFont = outputTextArea.getFont();
				Font f = new Font("Courier New", oldFont.getStyle(), oldFont
						.getSize());
				outputTextArea.setFont(f);
			}

			outputComponent = new JScrollPane(outputTextArea);
			outputComponent.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			outputComponent.setAutoscrolls(true);
			outputComponent.setWheelScrollingEnabled(true);

			ButtonBarBuilder bbBuilder = new ButtonBarBuilder();
			bbBuilder.addGlue();

			Action saveAsAction = new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							SPSUtils.saveDocument(
									parentFrame,
									engineOutputDoc,
									(FileExtensionFilter) SPSUtils.TEXT_FILE_FILTER);
						}
					});
				}
			};
			JButton saveAsButton = new JButton(saveAsAction);
			saveAsButton.setText("Save As...");
			bbBuilder.addGridded(saveAsButton);
			bbBuilder.addRelatedGap();

			JButton copyButton = new JButton(new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {

						public void run() {
							try {
								StringSelection selection = new StringSelection(
										engineOutputDoc.getText(0,
												engineOutputDoc.getLength()));
								Clipboard clipboard = Toolkit
								.getDefaultToolkit()
								.getSystemClipboard();
								clipboard.setContents(selection, selection);
							} catch (BadLocationException e1) {
								MMSUtils.showExceptionDialog(parentFrame,
										"Document Copy Error", e1);
							}
						}
					});
				}
			});
			copyButton.setText("Copy to Clipboard");
			bbBuilder.addGridded(copyButton);
			bbBuilder.addRelatedGap();

			buttonBar = bbBuilder.getPanel();
			
		}

		public JProgressBar getProgressBar() {
			return progressBar;
		}
		
		public JPanel getButtonBar() {
			return buttonBar;
		}
		
		public JComponent getOutputComponent() {
			return outputComponent;
		}
		
		public void actionPerformed(ActionEvent e) {
			doSave();
			try {
				matchEngine = new MatchEngineImpl(session, match);
				matchEngine.checkPreconditions();
				matchEngine.addEngineListener(this);
				matchEngine.run();
				ProgressWatcher.watchProgress(progressBar,matchEngine);
			} catch (EngineSettingException ese) {
				JOptionPane.showMessageDialog(parent, ese.getMessage(),
						"Engine error", JOptionPane.ERROR_MESSAGE);
				return;
			} catch (Exception ex) {
				MMSUtils.showExceptionDialog(parent, "Engine error", ex);
				return;
			}
		}

		public void engineEnd(EngineEvent e) {
			int exitVal = matchEngine.getEngineReturnCode();
			try {
				engineOutputDoc.insertString(engineOutputDoc.getLength(),
						"\nExecutable Return Code: " + exitVal + "\n", stderrAtt);
			} catch (BadLocationException e1) {
				throw new RuntimeException(e1);
			}
			matchEngine.removeEngineListener(this);
		}

		public void engineStart(EngineEvent e) {
			// any output?
			StreamGobbler errorGobbler = new StreamGobbler(matchEngine
					.getEngineErrorOutput(), "ERROR", engineOutputDoc,
					stderrAtt);

			StreamGobbler outputGobbler = new StreamGobbler(matchEngine
					.getEngineStandardOutput(), "OUTPUT", engineOutputDoc,
					stdoutAtt);

			// kick them off
			errorGobbler.start();
			outputGobbler.start();
		}
	}

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
							"Log file name is invalidate.");
				}
				if (!log.canWrite()) {
					return ValidateResult.createValidateResult(Status.FAIL,
							"Log file is not writable.");
				}
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

	/**
	 * A Validator to check the preconditions of the engine.
	 */
	private class MatchAndMatchEngineValidator implements Validator {

		private Match match;
		private MatchMakerSession session;

		public MatchAndMatchEngineValidator(MatchMakerSession session, Match match) {
			this.match = match;
			this.session = session;
		}

		public ValidateResult validate(Object contents) {
			MatchEngineImpl matchEngine = new MatchEngineImpl(session, match);
			try {
				matchEngine.checkPreconditions();
			} catch (EngineSettingException ex) {
				return ValidateResult.createValidateResult(Status.FAIL,
						ex.getMessage());
			} catch (Exception ex) {
                logger.warn("Unexpected exception while checking engine preconditions", ex);
                return ValidateResult.createValidateResult(Status.FAIL,
                        "Unexpected exception: "+ex.getMessage());
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
		settings.setTruncateCandDupe(truncateCandDup.isSelected());
		settings.setSendEmail(sendEmail.isSelected());
		settings.setLog(new File(logFilePath.getText()));
		settings.setAppendToLog(append.isSelected());
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