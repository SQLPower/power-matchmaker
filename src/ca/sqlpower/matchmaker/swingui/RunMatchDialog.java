package ca.sqlpower.matchmaker.swingui;

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
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
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

import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.SaveDocument;
import ca.sqlpower.architect.swingui.ASUtils.FileExtensionFilter;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MatchSettings;
import ca.sqlpower.matchmaker.RowSetModel;
import ca.sqlpower.matchmaker.dao.MatchMakerDAO;
import ca.sqlpower.matchmaker.swingui.action.ShowMatchStatisticInfoAction;
import ca.sqlpower.matchmaker.util.MatchMakerQFAFactory;
import ca.sqlpower.matchmaker.util.log.Level;
import ca.sqlpower.matchmaker.util.log.LogFactory;
import ca.sqlpower.validation.swingui.FormValidationHandler;
import ca.sqlpower.validation.swingui.StatusComponent;

import com.darwinsys.notepad.Notepad;
import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class RunMatchDialog extends JDialog {

	private static final Logger logger = Logger.getLogger(RunMatchDialog.class);

	private final MatchMakerSwingSession swingSession;

	private JTextField logFilePath;

	private String lastAccessPath;

	private JButton browse;

	private JCheckBox append;

	private JTextField recordsToProcess;

	private JTextField minWord;

	private JCheckBox debugMode;

	private JCheckBox truncateCandDup;

	private JCheckBox sendEmail;

	private JButton viewLogFile;

	private JButton viewStats;

	private JButton showCommand;

	private JButton save;

	private JButton runMatchEngine;

	private JButton exit;

	private JComboBox rollbackSegment;

	private JFrame parentFrame;

	private Match match;

	StatusComponent status = new StatusComponent();
	
	private FormValidationHandler handler;

	private String enginePath;
	
	public RunMatchDialog(MatchMakerSwingSession swingSession, Match match,
			JFrame parentFrame) {
		super(parentFrame, "Run Match: " + match.getName());
		this.swingSession = swingSession;
		this.parentFrame = parentFrame;
		this.match = match;
		handler = new FormValidationHandler(status);
		buildUI();
		setDefaultSelections(match);
		handler.addPropertyChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				refreshActionStatus();
			}
        });
		enginePath = swingSession.getContext().getMatchEngineLocation();
	}
	
	private void refreshActionStatus() {
		// TODO Auto-generated method stub
	}

	private Action browseFileAction = new AbstractAction("...") {

		public void actionPerformed(ActionEvent e) {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setCurrentDirectory(new File(lastAccessPath));
			fileChooser.setSelectedFile(new File(lastAccessPath));
			int returnVal = fileChooser.showOpenDialog(RunMatchDialog.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				final File file = fileChooser.getSelectedFile();
				logFilePath.setText(file.getPath());
				lastAccessPath = file.getAbsolutePath();
			}

		}
	};

	private void buildUI() {
		FormLayout layout = new FormLayout(
				"4dlu,fill:min(70dlu;pref),4dlu,fill:200dlu:grow, pref,20dlu,pref,10dlu,pref,4dlu",
			//   1    2                    3    4                 5    6     7    8    9    10
				"10dlu,pref,10dlu,pref,10dlu,pref,3dlu,pref,3dlu,pref,30dlu,pref,4dlu,pref,4dlu");
			//	 1     2    3     4    5     6    7    8    9    10   11    12   13   14   15
		PanelBuilder pb;
		JPanel p = logger.isDebugEnabled() ? new FormDebugPanel(layout) : new JPanel(layout);
		pb = new PanelBuilder(layout, p);

		CellConstraints cc = new CellConstraints();

		logFilePath = new JTextField();
		rollbackSegment = new JComboBox();
		browse = new JButton(browseFileAction);
		append = new JCheckBox();
		recordsToProcess = new JTextField(5);
		minWord = new JTextField(5);
		minWord.setText("0");
		debugMode = new JCheckBox();
		truncateCandDup = new JCheckBox();
		sendEmail = new JCheckBox();
		viewLogFile = new JButton(new ShowLogFileAction());
		viewStats = new JButton(
				new ShowMatchStatisticInfoAction(swingSession,match,getParentFrame()));
		viewStats.setText("Match Statistics");
		showCommand = new JButton(new ShowCommandAction(match,
				RunMatchDialog.this));

		// might need more buttons here... (check VB app)

		save = new JButton(new AbstractAction("Save") {
			public void actionPerformed(ActionEvent e) {
				applyChange();
				MatchMakerDAO<Match> dao = swingSession.getDAO(Match.class);
		        dao.save(match);
			}
		});

		runMatchEngine = new JButton(new RunEngineAction(match,RunMatchDialog.this));
		exit = new JButton(new AbstractAction("Close") {
			public void actionPerformed(ActionEvent e) {
				RunMatchDialog.this.setVisible(false);
				RunMatchDialog.this.dispose();
			}
		});
		
		pb.add(status, cc.xy(4, 2, "l,c"));

		pb.add(new JLabel("Log File:"), cc.xy(2, 4, "r,f"));
		pb.add(logFilePath, cc.xy(4, 4, "f,f"));
		pb.add(browse, cc.xy(5, 4, "r,f"));
		pb.add(new JLabel("Append to old Log File?"), cc.xy(7, 4, "r,f"));
		pb.add(append, cc.xy(9, 4, "r,f"));
		pb.add(new JLabel("Debug Mode?"), cc.xy(7, 6, "r,c"));
		pb.add(debugMode, cc.xy(9, 6, "r,c"));
		pb.add(new JLabel("Truncate Cand Dup?"), cc.xy(7, 8, "r,c"));
		pb.add(truncateCandDup, cc.xy(9, 8, "r,c"));
		pb.add(new JLabel("Send E-mails?"), cc.xy(7, 10, "r,c"));
		pb.add(sendEmail, cc.xy(9, 10, "r,c"));

		pb.add(new JLabel("Rollback Segment:"), cc.xy(2, 6, "r,c"));
		pb.add(rollbackSegment, cc.xy(4, 6));
		pb.add(new JLabel("Records to Process:"), cc.xy(2, 8, "r,c"));
		pb.add(recordsToProcess, cc.xy(4, 8, "l,c"));
		pb.add(new JLabel("Min Word Count Freq:"), cc.xy(2, 10, "r,c"));
		pb.add(minWord, cc.xy(4, 10, "l,c"));

		
		FormLayout bbLayout = new FormLayout(
				"4dlu,pref,10dlu:grow,pref,10dlu:grow,pref,4dlu",
				"4dlu,pref,4dlu,pref,4dlu,pref,4dlu");
		PanelBuilder bbpb;
		JPanel bbp = logger.isDebugEnabled() ? new FormDebugPanel(bbLayout) : new JPanel(bbLayout);
		bbpb = new PanelBuilder(bbLayout, bbp);
		bbpb.add(viewLogFile, cc.xy(2, 2, "f,f"));
		bbpb.add(showCommand, cc.xy(4, 2, "f,f"));
		bbpb.add(runMatchEngine, cc.xy(6, 2, "f,f"));
		bbpb.add(viewStats, cc.xy(2, 4, "f,f"));
		bbpb.add(save, cc.xy(4, 4, "f,f"));
		bbpb.add(exit, cc.xy(6, 4, "f,f"));

		pb.add(bbpb.getPanel(), cc.xyw(2, 14, 8));

		getContentPane().add(pb.getPanel());
	}

	private void setDefaultSelections(Match match) {
		MatchSettings settings = match.getMatchSettings();
		String logFileName;
		if ( settings.getLog() != null ) {
			logFileName = (settings.getLog().getConstraint()).toString();
		} else {
			logFileName = match.getName() + ".log";
		}
		File file;
		if (lastAccessPath != null && lastAccessPath.length() > 0) {
			file = new File(lastAccessPath, logFileName);
		} else {
			file = new File(logFileName);
		}
		lastAccessPath = file.getAbsolutePath();
		if (logFileName != null){
			logFilePath.setText(logFileName);
		}
		Boolean appendToLog = settings.getAppendToLog();
		append.setSelected(appendToLog);
		
		System.err.println(append+" "+settings);
		if ( settings.getProcessCount() == null ) {
			recordsToProcess.setText("");
		} else {
			recordsToProcess.setText(String.valueOf(settings.getProcessCount()));
		}
		System.err.println(append);
		debugMode.setSelected(settings.getDebug());
		truncateCandDup.setSelected(settings.getTruncateCandDupe());
		sendEmail.setSelected(settings.getSendEmail());

		//TODO add roll back segment
		rollbackSegment.setSelectedItem(settings.getRollbackSegmentName());
		debugMode.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				if ( ((JCheckBox)e.getSource()).isSelected() ) {
					truncateCandDup.setSelected(true);
					recordsToProcess.setText("1");
				} else {
					truncateCandDup.setSelected(false);
					recordsToProcess.setText("0");
				}
			}});
	}
	private void applyChange() {

		MatchSettings settings = match.getMatchSettings();
		settings.setDebug(debugMode.isSelected());
		settings.setTruncateCandDupe(truncateCandDup.isSelected());
		settings.setSendEmail(sendEmail.isSelected());
		settings.setLog(LogFactory.getLogger(Level.INFO, logFilePath.getText()));
		settings.setAppendToLog(append.isSelected());
		if ( recordsToProcess.getText() == null ||
				recordsToProcess.getText().length() == 0 ) {
			settings.setProcessCount(null);
		} else {
			settings.setProcessCount(Integer.valueOf(recordsToProcess.getText()));
		}
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

	private class RunEngineAction extends AbstractAction {

		private final Match match;

		private final JDialog parent;

		SimpleAttributeSet stdoutAtt = new SimpleAttributeSet();

		SimpleAttributeSet stderrAtt = new SimpleAttributeSet();

		public RunEngineAction(Match match, JDialog parent) {
			super("Run Match Engine");
			this.parent = parent;
			this.match = match;
			StyleConstants.setForeground(stdoutAtt, Color.black);
			StyleConstants.setFontFamily(stdoutAtt, "Courier New");
			StyleConstants.setForeground(stderrAtt, Color.red);
		}

		public void actionPerformed(ActionEvent e) {
			applyChange();
			final String cmd = createCommand(match) + " USER_PROMPT=N";

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
			final boolean courierNewExist2 = courierNewExist;

			new Thread(new Runnable() {

				public void run() {

					final DefaultStyledDocument engineOutputDoc = 
						new DefaultStyledDocument();
					final JDialog d = new JDialog(parent);
					d.setTitle("MatchMaker engine output:");

					FormLayout layout = new FormLayout(
							"4dlu,fill:pref:grow,4dlu", // columns
							"4dlu,fill:pref:grow,4dlu,16dlu,4dlu"); // rows
					//		 1    2                        3    4     5

					PanelBuilder pb;
					JPanel p = logger.isDebugEnabled() ? new FormDebugPanel(
							layout) : new JPanel(layout);
					pb = new PanelBuilder(layout, p);
					CellConstraints cc = new CellConstraints();

					JTextArea cmdText = new JTextArea(35,120);
					cmdText.setDocument(engineOutputDoc);
					cmdText.setEditable(false);
					cmdText.setWrapStyleWord(true);
					cmdText.setLineWrap(true);
					cmdText.setAutoscrolls(true);

					if (courierNewExist2) {
						Font oldFont = cmdText.getFont();
						Font f = new Font("Courier New", oldFont.getStyle(),
								oldFont.getSize());
						cmdText.setFont(f);

					}

					JScrollPane scrollPane = new JScrollPane(cmdText);
					scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
					scrollPane.setAutoscrolls(true);
					scrollPane.setWheelScrollingEnabled(true);
					pb.add(scrollPane, cc.xy(2, 2, "f,f"));

					ButtonBarBuilder bbBuilder = new ButtonBarBuilder();

					Action saveAsAction = new AbstractAction() {
						public void actionPerformed(ActionEvent e) {
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									new SaveDocument(
											d,
											engineOutputDoc,
											(FileExtensionFilter) ASUtils.TEXT_FILE_FILTER);
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
														engineOutputDoc
																.getLength()));
										Clipboard clipboard = Toolkit
												.getDefaultToolkit()
												.getSystemClipboard();
										clipboard.setContents(selection,
												selection);
									} catch (BadLocationException e1) {
										ASUtils.showExceptionDialog(d,
												"Document Copy Error", e1, new MatchMakerQFAFactory());
									}
								}
							});
						}
					});
					copyButton.setText("Copy to Clipboard");
					bbBuilder.addGridded(copyButton);
					bbBuilder.addRelatedGap();
					bbBuilder.addGlue();

					JButton cancelButton = new JButton(new AbstractAction() {
						public void actionPerformed(ActionEvent e) {
							d.setVisible(false);
							d.dispose();
						}
					});
					cancelButton.setText("Close");
					bbBuilder.addGridded(cancelButton);

					pb.add(bbBuilder.getPanel(), cc.xy(2, 4));
					d.add(pb.getPanel());
					// don't display dialog until the process started

					Runtime rt = Runtime.getRuntime();
					logger.debug("Executing " + cmd);
					Process proc;
					try {
						proc = rt.exec(cmd);
						StreamGobbler errorGobbler = new StreamGobbler(proc
								.getErrorStream(), "ERROR", engineOutputDoc,
								stderrAtt);

						// any output?
						StreamGobbler outputGobbler = new StreamGobbler(proc
								.getInputStream(), "OUTPUT", engineOutputDoc,
								stdoutAtt);

						// kick them off
						errorGobbler.start();
						outputGobbler.start();

						d.pack();
						d.setVisible(true);

						// any error message?
						// any error???
						final int exitVal = proc.waitFor();
						logger.debug("ExitValue: " + exitVal);
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								try {
									engineOutputDoc
											.insertString(engineOutputDoc
													.getLength(),
													"\nExecutable Return Code: "
															+ exitVal + "\n",
													stderrAtt);
								} catch (BadLocationException e1) {
									ASUtils.showExceptionDialog(d,
											"Document Display Error", e1, new MatchMakerQFAFactory());
								}
							}
						});

					} catch (Throwable e1) {
						ASUtils.showExceptionDialog(parent,
								"Engine Run Time Error", e1, new MatchMakerQFAFactory());
					}
				}
			}).start();
		}
	}

	private class ShowLogFileAction extends AbstractAction {

		public ShowLogFileAction() {
			super("Show Log File");
		}

		public void actionPerformed(ActionEvent e) {
			String logFileName = logFilePath.getText();
			try {
				/**
				 * Notepad has its own frame and should be modified to allow
				 * an icon argument in the constructor
				 */
				new Notepad().doLoad(logFileName);
			} catch (IOException e1) {
				throw new RuntimeException("Unable to view log file "
						+ logFileName, e1);
			}

		}
	}

	private class ShowCommandAction extends AbstractAction {

		private Match match;

		private JDialog parent;

		public ShowCommandAction(Match match, JDialog parent) {
			super("Show Command");
			this.match = match;
			this.parent = parent;
		}

		public void actionPerformed(ActionEvent e) {
			applyChange();
			final String cmd = createCommand(match);
			final JDialog d = new JDialog(parent,
					"MatchMaker Engine Command Line:");

			final DefaultStyledDocument cmdDoc = new DefaultStyledDocument();
			SimpleAttributeSet att = new SimpleAttributeSet();
			StyleConstants.setForeground(att, Color.black);

			try {
				cmdDoc.insertString(0, createCommand(match), att);
			} catch (BadLocationException e1) {
				ASUtils.showExceptionDialog(d, "Unknown Document Error", e1, new MatchMakerQFAFactory());
			}


			
			FormLayout layout = new FormLayout("4dlu,fill:min(pref;200dlu):grow,4dlu", // columns
					"4dlu,fill:min(pref;200dlu):grow,4dlu,pref,4dlu"); // rows
			//		 1    2                        3    4     5

			PanelBuilder pb;
			JPanel p = logger.isDebugEnabled() ? new FormDebugPanel(layout)
					: new JPanel(layout);
			pb = new PanelBuilder(layout, p);
			CellConstraints cc = new CellConstraints();

			JTextArea cmdText = new JTextArea(cmdDoc,createCommand(match),15,120);
			cmdText.setEditable(false);
			cmdText.setWrapStyleWord(true);
			cmdText.setLineWrap(true);
			pb.add(cmdText, cc.xy(2, 2, "f,f"));

			ButtonBarBuilder bbBuilder = new ButtonBarBuilder();

			Action saveAsAction = new AbstractAction("Save As...") {
				public void actionPerformed(ActionEvent e) {
					new SaveDocument(d, cmdDoc,
							(FileExtensionFilter) ASUtils.BATCH_FILE_FILTER);
				}
			};
			JButton saveAsButton = new JButton(saveAsAction);
			bbBuilder.addGridded(saveAsButton);
			bbBuilder.addRelatedGap();

			JButton copyButton = new JButton(new AbstractAction("Copy to Clipboard") {
				public void actionPerformed(ActionEvent e) {
					StringSelection selection = new StringSelection(cmd);
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
			cancelButton.setText("Cancel");
			bbBuilder.addGridded(cancelButton);

			pb.add(bbBuilder.getPanel(), cc.xy(2, 4));
			d.add(pb.getPanel());
			ASUtils.makeJDialogCancellable(d, null);
			d.pack();
			d.setVisible(true);
		}

	}

	/**
	 * Create a command to run the match engine on the match passed in
	 *
	 * @param match
	 * @return the run command for the match
	 */
	protected String createCommand(Match match) {
		/*
		 * command sample:
		 * "M:\Program Files\Power Loader Suite\Match_Oracle.exe"
		 * MATCH="MATCH_CTV_ORGS" USER=PL/pl@arthur_test DEBUG=Y
		 * TRUNCATE_CAND_DUP=N SEND_EMAIL=N APPEND_TO_LOG_IND=N
		 * LOG_FILE="M:\Program Files\Power Loader Suite\Power Loader\script\MATCH_MATCH_CTV_ORGS.log"
		 * SHOW_PROGRESS=10 PROCESS_CNT=1
		 */
		StringBuffer command = new StringBuffer();
		SQLDatabase db = swingSession.getDatabase();
		String programPath = "\"" + enginePath + "\"";
		command.append(programPath);
		command.append(" MATCH=\"").append(match.getName()).append("\"");
		if (db != null) {
			command.append(" USER=");
			command.append(db.getDataSource().getUser());
			command.append("/").append(db.getDataSource().getPass());
			command.append("@").append(db.getDataSource().getName());
		}
		MatchSettings settings = match.getMatchSettings();
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
		return command.toString();
	}

	private JFrame getParentFrame() {
		return parentFrame;
	}

}