package ca.sqlpower.matchmaker.swingui;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.hibernate.PlMatch;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class RunMatchPanel extends JFrame{

    private static final Logger logger = Logger.getLogger(RunMatchPanel.class);
    private JTextField logFilePath;
    String lastAccessPath;
    
    private JButton browse;
    private JCheckBox append;
    private JTextField recordsToProcess;
    private JTextField minWord;
    private JCheckBox debugMode;
    private JCheckBox trancateCandDup;
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
    
    private PlMatch plMatch;
    
    public RunMatchPanel(PlMatch plMatch){
        super();        
        this.plMatch = plMatch;
        buildUI();
    }
    
    private Action browseFileAction = new AbstractAction("...") {

        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(lastAccessPath));
           //Change this to another filter
           //fileChooser.addChoosableFileFilter(ASUtils.ARCHITECT_FILE_FILTER);
           int returnVal = fileChooser.showOpenDialog(RunMatchPanel.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    final File file = fileChooser.getSelectedFile();
                    logFilePath.setText(file.getPath());
                    lastAccessPath = file.getAbsolutePath();
                }

        }};
    
    public void buildUI(){
        FormLayout layout = new FormLayout(
                "4dlu,fill:min(70dlu;default),4dlu,fill:200dlu:grow, 7dlu,4dlu,10dlu,45dlu,10dlu,10dlu,10dlu",
                "10dlu,12dlu,10dlu,10dlu,3dlu,10dlu,3dlu,10dlu,30dlu,40dlu,10dlu,10dlu"); 

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
        trancateCandDup = new JCheckBox();
        sendEmail = new JCheckBox();
        viewLogFile = new JButton();
        viewStats = new JButton();
        showCommand = new JButton();
        viewMatchResults = new JButton();
        startWordCount = new JButton();
        wordCountResults = new JButton();
        maintainTranslateWords = new JButton();
        validateMatches = new JButton();
        save = new JButton();
        runMatchEngine = new JButton();
        exit = new JButton();
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
            
            recordsToProcess.setText(String.valueOf(plMatch.getMatchProcessCnt()));
            
            // fixme
            debugMode.setSelected(Boolean.valueOf(plMatch.getMatchRunStatus()));
        } else {
            recordsToProcess.setText("0");        
        }
        
        
        pb.add(new JLabel("Log File:"), cc.xy(2,2,"r,f"));
        pb.add(logFilePath, cc.xy(4,2,"f,f"));
        pb.add(browse, cc.xy(5,2,"r,f"));
        pb.add(append, cc.xy(7,2,"r,f"));
        pb.add(new JLabel("Append?"), cc.xy(8,2,"r,f"));
        pb.add(new JLabel("Debug Mode?"), cc.xyw(6,4,3,"l,c"));
        pb.add(debugMode, cc.xy(10,4,"r,c"));
        pb.add(new JLabel("Truncate Cand Dup?"), cc.xyw(6,6,3,"l,c"));
        pb.add(trancateCandDup, cc.xy(10,6,"r,c"));
        pb.add(new JLabel("Send E-mails"), cc.xyw(6,8,3,"l,c"));
        pb.add(sendEmail, cc.xy(10,8,"r,c"));
        
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
        pb.add(bb1.getPanel(), cc.xyw(2,10,10));
        
        
        getContentPane().add(pb.getPanel());
    }
    
    private void setupUI() {
        
    }
    
    public static void main(String[] args) {

        final JFrame f = new RunMatchPanel(new PlMatch("Test Match","FIND DUPLICATE"));
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                f.pack();
                f.setVisible(true);
            }
        });
    }
}
