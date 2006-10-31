package ca.sqlpower.matchmaker.swingui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
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
import org.hibernate.Transaction;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.architect.swingui.SaveDocument;
import ca.sqlpower.architect.swingui.ASUtils.FileExtensionFilter;
import ca.sqlpower.matchmaker.EnginePath;
import ca.sqlpower.matchmaker.ExternalEngineUtils;
import ca.sqlpower.matchmaker.RowSetModel;
import ca.sqlpower.matchmaker.hibernate.PlMatch;
import ca.sqlpower.matchmaker.swingui.action.ShowMatchStatisticInfoAction;
import ca.sqlpower.matchmaker.util.HibernateUtil;

import com.darwinsys.notepad.Notepad;
import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class RunMatchDialog extends JDialog{

    private static final Logger logger = Logger.getLogger(RunMatchDialog.class);
    private JTextField logFilePath;
    String lastAccessPath;

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
    private JButton viewMatchResults;
    private JButton startWordCount;
    private JButton wordCountResults;
    private JButton maintainTranslateWords;
    private JButton validateMatches;
    private JButton save;
    private JButton runMatchEngine;
    private JButton exit;
    private JComboBox rollbackSegment;
    private JFrame parentFrame;

    private PlMatch plMatch;

    public RunMatchDialog(PlMatch plMatch, JFrame parentFrame){
        super(parentFrame,"Run Match:["+plMatch.getMatchId()+"]");
        //We store the parentFrame because ShowMatchStatics require a JFrame to
        //be its parent
        this.parentFrame = parentFrame;
        this.plMatch = plMatch;
        buildUI();
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

        }};

    public void buildUI(){
        FormLayout layout = new FormLayout(
                "4dlu,fill:min(70dlu;pref),4dlu,fill:200dlu:grow, 7dlu,20dlu,pref,4dlu,pref,4dlu",
              // 1    2                    3    4                 5    6     7    8    9    10
                "10dlu,pref,10dlu,pref,3dlu,pref,3dlu,pref,30dlu,pref,4dlu,pref,4dlu");
        //		 1     2    3     4    5    6    7    8    9     10   11   12   13
        PanelBuilder pb;
        JPanel p = logger.isDebugEnabled() ? new FormDebugPanel(layout) : new JPanel(layout);
        pb = new PanelBuilder(layout, p);

        CellConstraints cc = new CellConstraints();

        logFilePath = new JTextField();
        browse = new JButton(browseFileAction);
        append = new JCheckBox();
        recordsToProcess = new JTextField(5);
        minWord = new JTextField(5);
        minWord.setText("0");
        debugMode = new JCheckBox();
        truncateCandDup = new JCheckBox();
        sendEmail = new JCheckBox();
        viewLogFile = new JButton(new ShowLogFileAction());
        viewStats = new JButton(new ShowMatchStatisticInfoAction(plMatch,getParentFrame()));
        viewStats.setText("Match Statistics");
        showCommand = new JButton(new ShowCommandAction(plMatch,RunMatchDialog.this));
        viewMatchResults = new JButton();
        startWordCount = new JButton();
        wordCountResults = new JButton();
        maintainTranslateWords = new JButton();
        validateMatches = new JButton();
        save = new JButton(new AbstractAction(){
			public void actionPerformed(ActionEvent e) {
				applyChange();
				Transaction tx = HibernateUtil.primarySession().beginTransaction();
				HibernateUtil.primarySession().flush();
				tx.commit();
			}});
        save.setText("Save");

        runMatchEngine = new JButton(new RunEngineAction(plMatch,RunMatchDialog.this));
        exit = new JButton(new AbstractAction(){
			public void actionPerformed(ActionEvent e) {
				RunMatchDialog.this.setVisible(false);
				RunMatchDialog.this.dispose();
			}});
        exit.setText("Exit");

        rollbackSegment = new JComboBox();


        if ( plMatch != null ) {
            String logFileName = plMatch.getMatchLogFileName();
            File file;
            if ( logFileName == null || logFileName.length() == 0 ) {
                logFileName = plMatch.getMatchId() + ".log";
                file = new File(lastAccessPath,logFileName);
            } else {
                 file = new File(logFileName);
            }
            lastAccessPath = file.getAbsolutePath();
            logFilePath.setText(logFileName);
            append.setSelected(plMatch.isMatchAppendToLogInd());
            if ( plMatch.getMatchProcessCnt() != null ) {
            	recordsToProcess.setText(String.valueOf(plMatch.getMatchProcessCnt()));
            } else {
            	recordsToProcess.setText("0");
            }
            debugMode.setSelected(plMatch.isMatchDebugModeInd());
            truncateCandDup.setSelected(plMatch.isTruncateCandDupInd());
            sendEmail.setSelected(plMatch.isMatchSendEmailInd());
            rollbackSegment.setSelectedItem(plMatch.getMatchRollbackSegmentName());
        } else {
            recordsToProcess.setText("0");
        }


        pb.add(new JLabel("Log File:"), cc.xy(2,2,"r,f"));
        pb.add(logFilePath, cc.xy(4,2,"f,f"));
        pb.add(browse, cc.xy(5,2,"r,f"));
        pb.add(new JLabel("Append?"), cc.xy(7,2,"r,f"));
        pb.add(append, cc.xy(9,2,"r,f"));
        pb.add(new JLabel("Debug Mode?"), cc.xy(7,4,"r,c"));
        pb.add(debugMode, cc.xy(9,4,"r,c"));
        pb.add(new JLabel("Truncate Cand Dup?"), cc.xy(7,6,"r,c"));
        pb.add(truncateCandDup, cc.xy(9,6,"r,c"));
        pb.add(new JLabel("Send E-mails?"), cc.xy(7,8,"r,c"));
        pb.add(sendEmail, cc.xy(9,8,"r,c"));

        pb.add(new JLabel("Rollback Segment:"), cc.xy(2,4,"r,c"));
        pb.add(rollbackSegment, cc.xy(4,4));
        pb.add(new JLabel("Records to Process:"), cc.xy(2,6,"r,c"));
        pb.add(recordsToProcess, cc.xy(4,6,"l,c"));
        pb.add(new JLabel("Min Word Count Freq:"), cc.xy(2,8,"r,c"));
        pb.add(minWord, cc.xy(4,8,"l,c"));

        ButtonBarBuilder bb1 = new ButtonBarBuilder();
        bb1.addGridded(viewLogFile);
        bb1.addRelatedGap();
        bb1.addGlue();
        bb1.addGridded(viewStats);
        bb1.addRelatedGap();
        bb1.addGlue();
        bb1.addGridded(showCommand);
        bb1.addRelatedGap();
        bb1.addGlue();
        bb1.addGridded(viewMatchResults);
        bb1.addRelatedGap();
        bb1.addGlue();
        pb.add(bb1.getPanel(), cc.xyw(2,10,9));


        ButtonBarBuilder bb2 = new ButtonBarBuilder();
        bb2.addGridded(save);
        bb2.addRelatedGap();
        bb2.addGlue();
        bb2.addGridded(runMatchEngine);
        bb2.addRelatedGap();
        bb2.addGlue();
        bb2.addGridded(exit);
        bb2.addRelatedGap();
        bb2.addGlue();
        pb.add(bb2.getPanel(), cc.xyw(2,12,9));

        getContentPane().add(pb.getPanel());
    }

    private void applyChange() {
    	plMatch.setMatchDebugModeInd(debugMode.isSelected());
    	plMatch.setTruncateCandDupInd(truncateCandDup.isSelected());
    	plMatch.setMatchSendEmailInd(sendEmail.isSelected());
    	plMatch.setMatchLogFileName(logFilePath.getText());
    	plMatch.setMatchAppendToLogInd(append.isSelected());
    	plMatch.setMatchProcessCnt(Long.valueOf(recordsToProcess.getText()));
    }

    public class StatsTableMOdel extends RowSetModel {

		public StatsTableMOdel(RowSet set) {
			super(set);
		}

    }

    public class StreamGobbler extends Thread
    {
    	InputStream is;
    	String type;
    	AbstractDocument output;
    	SimpleAttributeSet att;


    	StreamGobbler(InputStream is, String type,
    			AbstractDocument output,
    			SimpleAttributeSet att ) {
    		this.is = is;
    		this.type = type;
    		this.output = output;
    		this.att = att;
    	}

    	public void run() {
    		try {
    			InputStreamReader isr = new InputStreamReader(is);
    			BufferedReader br = new BufferedReader(isr);
    			String line=null;
    			while ( (line = br.readLine()) != null) {
    				logger.debug(type + ">" + line);
    				final String fLine = line;
    				SwingUtilities.invokeLater(new Runnable() {
    					public void run() {
    						try {
    							output.insertString(output.getLength(),fLine+"\n",att);
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

    	private final PlMatch match;
		private final JDialog parent;

		SimpleAttributeSet stdoutAtt = new SimpleAttributeSet();
		SimpleAttributeSet stderrAtt = new SimpleAttributeSet();


    	public RunEngineAction(PlMatch match, JDialog parent) {
    		super("Run Match Engine");
    		this.parent = parent;
    		this.match = match;
			StyleConstants.setForeground(stdoutAtt, Color.black);
			StyleConstants.setFontFamily(stdoutAtt,"Courier New");
			StyleConstants.setForeground(stderrAtt, Color.red);
		}

		public void actionPerformed(ActionEvent e) {
			applyChange();
			final String cmd = createCommand(match) + " USER_PROMPT=N";

			GraphicsEnvironment ge = GraphicsEnvironment.
            	getLocalGraphicsEnvironment();
			Font[] fonts = ge.getAllFonts();
			boolean courierNewExist = false;
			for ( int i=0; i<fonts.length; i++ ) {
				if ( fonts[i].getFamily().equalsIgnoreCase("Courier New")) {
					courierNewExist = true;
					break;
				}
			}
			final boolean courierNewExist2 = courierNewExist;


			new Thread(new Runnable(){

				public void run() {

					final DefaultStyledDocument engineOutputDoc = new DefaultStyledDocument();
					final JDialog d = new JDialog(parent);
					d.setTitle("MatchMaker engine output:");

					FormLayout layout = new FormLayout(
							"4dlu,fill:400dlu:grow,4dlu", // columns
							"4dlu,fill:400dlu:grow,4dlu,16dlu,4dlu"); // rows
			    	//		 1    2                        3    4     5

					PanelBuilder pb;
					JPanel p = logger.isDebugEnabled() ?
							new FormDebugPanel(layout) : new JPanel(layout);
					pb = new PanelBuilder(layout, p);
					CellConstraints cc = new CellConstraints();

					JTextArea cmdText = new JTextArea(engineOutputDoc);
					cmdText.setEditable(false);
					cmdText.setWrapStyleWord(true);
					cmdText.setLineWrap(true);
					cmdText.setAutoscrolls(true);

					if ( courierNewExist2 ) {
						Font oldFont = cmdText.getFont();
						Font f = new Font("Courier New",oldFont.getStyle(),oldFont.getSize());
						cmdText.setFont(f);

					}

					pb.add(new JScrollPane(cmdText), cc.xy(2,2,"f,f"));

					ButtonBarBuilder bbBuilder = new ButtonBarBuilder();

					Action saveAsAction = new AbstractAction(){
						public void actionPerformed(ActionEvent e) {
							SwingUtilities.invokeLater(new Runnable(){
								public void run() {
									new SaveDocument(d,engineOutputDoc,
											(FileExtensionFilter) ASUtils.TEXT_FILE_FILTER );
								}});
						}
					};
					JButton saveAsButton = new JButton(saveAsAction );
					saveAsButton.setText("Save As...");
					bbBuilder.addGridded (saveAsButton);
					bbBuilder.addRelatedGap();

					JButton copyButton = new JButton(new AbstractAction(){
						public void actionPerformed(ActionEvent e) {
							SwingUtilities.invokeLater(new Runnable(){

								public void run() {
									try {
										StringSelection selection = new StringSelection(
												engineOutputDoc.getText(0,engineOutputDoc.getLength()));
										Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
										clipboard.setContents( selection, selection );
									} catch (BadLocationException e1) {
										ASUtils.showExceptionDialog(d,
												"Document Copy Error", e1 );
									}
								}});
						}});
					copyButton.setText("Copy to Clipboard");
					bbBuilder.addGridded (copyButton);
					bbBuilder.addRelatedGap();
					bbBuilder.addGlue();

					JButton cancelButton = new JButton(new AbstractAction(){
						public void actionPerformed(ActionEvent e) {
							d.setVisible(false);
							d.dispose();
						}});
					cancelButton.setText("Close");
					bbBuilder.addGridded(cancelButton);

					pb.add(bbBuilder.getPanel(), cc.xy(2,4));
					d.add(pb.getPanel());
					// don't display dialog until the process started



					Runtime rt = Runtime.getRuntime();
					logger.debug("Executing " + cmd);
					Process proc;
					try {
						proc = rt.exec(cmd);
						StreamGobbler errorGobbler = new
						StreamGobbler(proc.getErrorStream(),
								"ERROR", engineOutputDoc, stderrAtt);

						// any output?
						StreamGobbler outputGobbler = new
						StreamGobbler(proc.getInputStream(),
								"OUTPUT", engineOutputDoc, stdoutAtt);

						// kick them off
						errorGobbler.start();
						outputGobbler.start();

						d.pack();
						d.setVisible(true);

						// any error message?
						// any error???
						final int exitVal = proc.waitFor();
						logger.debug("ExitValue: " + exitVal);
						SwingUtilities.invokeLater(new Runnable(){
							public void run() {
								try {
									engineOutputDoc.insertString(engineOutputDoc.getLength(),
											"\nExecutable Return Code: " + exitVal+"\n",
											stderrAtt);
								} catch (BadLocationException e1) {
									ASUtils.showExceptionDialog(d,
											"Document Display Error", e1 );
								}
							}});

					} catch (Throwable  e1) {
						ASUtils.showExceptionDialog(parent,
								"Engine Run Time Error",e1);
					}
				}}).start();
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
    			throw new RuntimeException("Unable to view log file " + logFileName, e1);
    		}

		}
    }

    private class ShowCommandAction extends AbstractAction {

    	private PlMatch match;
		private JDialog parent;

		public ShowCommandAction(PlMatch match, JDialog parent) {
    		super("Show Command");
    		this.match = match;
    		this.parent = parent;
		}

		public void actionPerformed(ActionEvent e) {
			applyChange();
			final String cmd = createCommand(match);
			final JDialog d= new JDialog (parent,
                        "MatchMaker Engine Command Line:");

			final DefaultStyledDocument cmdDoc = new DefaultStyledDocument();
			SimpleAttributeSet att = new SimpleAttributeSet();
			StyleConstants.setForeground(att, Color.black);

			try {
				cmdDoc.insertString(0,createCommand(match),att );
			} catch (BadLocationException e1) {
				ASUtils.showExceptionDialog(d,"Unknown Document Error",e1);
			}

			FormLayout layout = new FormLayout(
					"4dlu,fill:200dlu:grow,4dlu", // columns
					"4dlu,fill:200dlu:grow,4dlu,16dlu,4dlu"); // rows
	    	//		 1    2                        3    4     5

			PanelBuilder pb;
			JPanel p = logger.isDebugEnabled() ?
					new FormDebugPanel(layout) : new JPanel(layout);
			pb = new PanelBuilder(layout, p);
			CellConstraints cc = new CellConstraints();

			JTextArea cmdText = new JTextArea(cmdDoc);
			cmdText.setEditable(false);
			cmdText.setWrapStyleWord(true);
			cmdText.setLineWrap(true);
			pb.add(cmdText, cc.xy(2,2,"f,f"));

			ButtonBarBuilder bbBuilder = new ButtonBarBuilder();

			Action saveAsAction = new AbstractAction(){
				public void actionPerformed(ActionEvent e) {
					new SaveDocument(d,cmdDoc,
							(FileExtensionFilter) ASUtils.BATCH_FILE_FILTER );
				}
			};
			JButton saveAsButton = new JButton(saveAsAction );
			saveAsButton.setText("Save As...");
			bbBuilder.addGridded (saveAsButton);
			bbBuilder.addRelatedGap();

			JButton copyButton = new JButton(new AbstractAction(){
				public void actionPerformed(ActionEvent e) {
					StringSelection selection = new StringSelection(cmd);
					Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
					clipboard.setContents( selection, selection );
				}});
			copyButton.setText("Copy to Clipboard");
			bbBuilder.addGridded (copyButton);
			bbBuilder.addRelatedGap();
			bbBuilder.addGlue();

			JButton cancelButton = new JButton(new AbstractAction(){
				public void actionPerformed(ActionEvent e) {
					d.setVisible(false);
				}});
			cancelButton.setText("Cancel");
			bbBuilder.addGridded(cancelButton);

			pb.add(bbBuilder.getPanel(), cc.xy(2,4));
			d.add(pb.getPanel());
			ArchitectPanelBuilder.makeJDialogCancellable(d,null);
			d.pack();
			d.setVisible(true);
		}

    }

    protected String createCommand(PlMatch match) {
    	/*
		 * command sample:
		 * "M:\Program Files\Power Loader Suite\Match_Oracle.exe"
		 * MATCH="MATCH_CTV_ORGS" USER=PL/pl@arthur_test DEBUG=Y
		 * TRUNCATE_CAND_DUP=N SEND_EMAIL=N APPEND_TO_LOG_IND=N
		 * LOG_FILE="M:\Program Files\Power Loader Suite\Power Loader\script\MATCH_MATCH_CTV_ORGS.log"
		 * SHOW_PROGRESS=10 PROCESS_CNT=1
		 */
		StringBuffer command = new StringBuffer();
		SQLDatabase db = MatchMakerFrame.getMainInstance().getDatabase();
		String programPath = null;
		programPath = ExternalEngineUtils.getProgramPath(EnginePath.MATCHMAKER);
		// FIXME: comment following line to use executable from user pl.ini config
		programPath = "\"M:\\Program Files\\Power Loader Suite\\Match_ODBC.exe\"";
		command.append(programPath);
		command.append(" MATCH=\"").append(match.getMatchId()).append("\"");
		if ( db != null ) {
			command.append(" USER=");
			command.append(db.getDataSource().getUser());
			command.append("/").append(db.getDataSource().getPass());
			command.append("@").append(db.getDataSource().getName());
		}
		command.append(" DEBUG=").append(match.isMatchDebugModeInd()?"Y":"N");
		command.append(" TRUNCATE_CAND_DUP=").append(match.isTruncateCandDupInd()?"Y":"N");
		command.append(" SEND_EMAIL=").append(match.isMatchSendEmailInd()?"Y":"N");
		command.append(" APPEND_TO_LOG_IND=").append(match.isMatchAppendToLogInd()?"Y":"N");
		command.append(" LOG_FILE=\"").append(match.getMatchLogFileName()).append("\"");
		command.append(" SHOW_PROGRESS=").append(match.getMatchShowProgressFreq());
		command.append(" PROCESS_CNT=").append(match.getMatchProcessCnt());
		return command.toString();
	}

    public JFrame getParentFrame(){
        return parentFrame;
    }
    public static void main(String[] args) {

    	MatchMakerFrame.getMainInstance();
		ArchitectDataSource ds = MatchMakerFrame.getMainInstance().getUserSettings().getPlDotIni().getDataSource("ARTHUR_TEST");
		MatchMakerFrame.getMainInstance().newLogin(new SQLDatabase(ds));
		final PlMatch match = MatchMakerFrame.getMainInstance().getMatchByName("DEMO_MATCH_PEOPLE_MATCH_FIRST");


        final JDialog f = new RunMatchDialog(match,null);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                f.pack();
                f.setVisible(true);
            }
        });
    }
}