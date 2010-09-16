/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of DQguru
 *
 * DQguru is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DQguru is distributed in the hope that it will be useful,
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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.matchmaker.CleanseEngineImpl;
import ca.sqlpower.matchmaker.FolderParent;
import ca.sqlpower.matchmaker.MatchEngineImpl;
import ca.sqlpower.matchmaker.MatchMakerFolder;
import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchMakerSessionContext;
import ca.sqlpower.matchmaker.MatchMakerTranslateGroup;
import ca.sqlpower.matchmaker.MatchMakerUtils;
import ca.sqlpower.matchmaker.MatchMakerVersion;
import ca.sqlpower.matchmaker.MergeEngineImpl;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.TableMergeRules;
import ca.sqlpower.matchmaker.TranslateGroupParent;
import ca.sqlpower.matchmaker.WarningListener;
import ca.sqlpower.matchmaker.Project.ProjectMode;
import ca.sqlpower.matchmaker.address.AddressCorrectionEngine;
import ca.sqlpower.matchmaker.dao.MatchMakerDAO;
import ca.sqlpower.matchmaker.dao.MatchMakerTranslateGroupDAO;
import ca.sqlpower.matchmaker.dao.MungeProcessDAO;
import ca.sqlpower.matchmaker.dao.PlFolderDAO;
import ca.sqlpower.matchmaker.dao.ProjectDAO;
import ca.sqlpower.matchmaker.event.MatchMakerEvent;
import ca.sqlpower.matchmaker.event.MatchMakerListener;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.munge.MungeResultStep;
import ca.sqlpower.matchmaker.munge.MungeStepOutput;
import ca.sqlpower.matchmaker.munge.SQLInputStep;
import ca.sqlpower.matchmaker.swingui.action.BuildExampleTableAction;
import ca.sqlpower.matchmaker.swingui.action.CreateRepositoryAction;
import ca.sqlpower.matchmaker.swingui.action.DeleteProjectAction;
import ca.sqlpower.matchmaker.swingui.action.EditTranslateAction;
import ca.sqlpower.matchmaker.swingui.action.ExportMungePenToPDFAction;
import ca.sqlpower.matchmaker.swingui.action.ExportProjectAction;
import ca.sqlpower.matchmaker.swingui.action.HelpAction;
import ca.sqlpower.matchmaker.swingui.action.ImportProjectAction;
import ca.sqlpower.matchmaker.swingui.action.NewProjectAction;
import ca.sqlpower.matchmaker.swingui.action.ShowMatchStatisticInfoAction;
import ca.sqlpower.matchmaker.swingui.engine.EngineSettingsPanel;
import ca.sqlpower.matchmaker.swingui.engine.EngineSettingsPanel.EngineType;
import ca.sqlpower.matchmaker.undo.AbstractUndoableEditorPane;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.swingui.AboutPanel;
import ca.sqlpower.swingui.CommonCloseAction;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.DataEntryPanelBuilder;
import ca.sqlpower.swingui.JDefaultButton;
import ca.sqlpower.swingui.MemoryMonitor;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.SPSwingWorker;
import ca.sqlpower.swingui.SwingWorkerRegistry;
import ca.sqlpower.swingui.event.SessionLifecycleEvent;
import ca.sqlpower.swingui.event.SessionLifecycleListener;
import ca.sqlpower.util.BrowserUtil;
import ca.sqlpower.util.SQLPowerUtils;
import ca.sqlpower.util.Version;

/**
 * The Main Window for the MatchMaker Application; contains a main() method that is
 * the conventional way to start the application running.
 */
public class MatchMakerSwingSession implements MatchMakerSession, SwingWorkerRegistry {

	private final SelectRemoveEditorListener removeEditorListener = new SelectRemoveEditorListener();

	private static Logger logger = Logger.getLogger(MatchMakerSwingSession.class);

    /**
     * Controls a few GUI tweaks that we do on OS X, such as moving menu items around.
     */
	public static final boolean MAC_OS_X =
        System.getProperty("os.name").toLowerCase().startsWith("mac os x");

    /**
     * The context that created this session.  Holds all of the static preferences.
     */
    private final SwingSessionContext sessionContext;

    /**
     * The session implementation that we delegate MatchMakerSession requests to.
     */
    private final MatchMakerSession sessionImpl;

    /**
	 * This is the top level application frame
	 */
	private final JFrame frame;

    /**
     * The small MatchMaker application icon (suitable for use as frame/dialog
     * icon images).
     */
	private final ImageIcon smallMMIcon;

	/**
	 * The main part of the UI; the tree lives on the left and the current editor lives on the right.
     *
     * @see setCurrentEditComponent()
	 */
	private JSplitPane splitPane;

    /**
     * The tree that lets users browse the business objects.
     */
	private JTree tree;

    /**
     * The window that pops up to display warning messages for this session.
     */
    private JDialog warningDialog;

    /**
     * The component in the warningDialog which actually contains the messages.
     */
    private JTextArea warningTextArea;
    
    /**
     * The status message which will contain what the swingSession is doing.
     */
    private JLabel statusLabel;

    private List<WarningListener> warningListeners = new ArrayList<WarningListener>();

    /**
     * This variable is used to restore the old selection if the user
     * decides to hit cancel on a request to save or discard unsaved changes.
     */
    private TreePath lastTreePath;

    /**
     * A collection of SPSwingWorkers that are associated with this session.
     * The session keeps track of them in order to cancel their threads
     * when the session closes.
     */
    private final Set<SPSwingWorker> swingWorkers = new HashSet<SPSwingWorker>();
    
    /**
     * A colour chooser used by the MungeProcessEditor to set custom colours.
     * It has been created within a swing session to share recent colours amongst
     * different match rule sets.
     */
    private final JColorChooser colourChooser = new JColorChooser();

    /**
     * This variable is used to determine whether the engines of this session
     * should be enabled.
     */
	private boolean enginesEnabled = true;
	
	/**
	 * A list that helps keep track of the created sessions
	 */
	private List<SessionLifecycleListener<MatchMakerSession>> lifecycleListener;
	
    
    private Action userPrefsAction = new AbstractAction("User Preferences...") {
		public void actionPerformed(ActionEvent e) {
            JDialog d = DataEntryPanelBuilder.createDataEntryPanelDialog(
                    new UserPreferencesEditor((SwingSessionContext) getContext()),
                    frame, "User Preferences", "OK");
            d.setLocationRelativeTo(frame);
            d.setVisible(true);
		}
    };

	private Action aboutAction = new AbstractAction("About SQL Power DQguru...") {

		public void actionPerformed(ActionEvent evt) {
			// This is one of the few JDIalogs that can not get replaced
			// with a call to ArchitectPanelBuilder, because an About
			// box must have only ONE button...
			final JDialog d = new JDialog(getFrame(),
										  "About SQL Power DQguru");
			JPanel cp = new JPanel(new BorderLayout(12,12));
			cp.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
			
			ImageIcon icon = SPSUtils.createIcon("dqguru_128", "DQguru Logo");
			
			final AboutPanel aboutPanel = new AboutPanel(icon, "SQL Power DQguru", "ca/sqlpower/matchmaker/matchmaker.version.properties", MatchMakerVersion.APP_VERSION);
			cp.add(aboutPanel, BorderLayout.CENTER);

			JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

			Action okAction = new AbstractAction() {
				public void actionPerformed(ActionEvent evt) {
						aboutPanel.applyChanges();
						d.setVisible(false);
				}
			};
			okAction.putValue(Action.NAME, "OK");
			JDefaultButton okButton = new JDefaultButton(okAction);
			buttonPanel.add(okButton);

			cp.add(buttonPanel, BorderLayout.SOUTH);
			SPSUtils.makeJDialogCancellable(
					d, new CommonCloseAction(d));
			d.getRootPane().setDefaultButton(okButton);
			d.setContentPane(cp);
			d.pack();
			d.setLocationRelativeTo(getFrame());
			d.setVisible(true);
		}
    };

	private Action exitAction = new AbstractAction("Exit") {
	    public void actionPerformed(ActionEvent e) {
	        getContext().closeAll();
	    }
	};

	private Action remoteLoginAction = new AbstractAction("Connect to Remote Repository...") {
		public void actionPerformed(ActionEvent e) {
			sessionContext.showLoginDialog(null);
		}

	};

    private CreateRepositoryAction createRepositoryAction = new CreateRepositoryAction(this);
    
	private Action newDeDupeAction = null;
//	TODO: Implement Cross-referencing projects first before re-enabling this action
//	private Action newXrefAction = null;
	private Action newCleanseAction = null;
	private Action newAddressAction = null;
	
	private Action deleteProjectAction = new DeleteProjectAction(this);

	private Action helpAction;
	private Action buildExampleTableAction;
	private Action supportOnTheWebAction;
	
	private Action sqlQueryAction = new AbstractAction("Universal SQL Access...") {
		public void actionPerformed(ActionEvent e) {
			QueryDialog d = new QueryDialog(MatchMakerSwingSession.this, frame, "Universal SQL Access");
			d.setVisible(true);
		}
	};

	private Action databaseConnectionAction = new AbstractAction("Manage Database Connections...") {

		public void actionPerformed(ActionEvent e) {
            sessionContext.showDatabaseConnectionManager(frame);
		}
	};

	private Action showMatchStatisticInfoAction = new AbstractAction("Match Statistics") {

		public void actionPerformed(ActionEvent e) {
			Project project = MMSUtils.getTreeObject(getTree(),Project.class);
			if (project == null || project.getType() == ProjectMode.CLEANSE)
				return;

			ShowMatchStatisticInfoAction sm = new ShowMatchStatisticInfoAction(
					MatchMakerSwingSession.this,project,frame);
			sm.actionPerformed(e);
		}
	};

    private Action clearWarningsAction = new AbstractAction("Clear") {
        public void actionPerformed(ActionEvent e) {
            warningTextArea.setText("");
        }
    };

    private Action closeWarningDialogAction = new AbstractAction("Close") {
        public void actionPerformed(ActionEvent e) {
            warningDialog.setVisible(false);
        }
    };
    
    /**
     * This is a special variable to remember the panel that was last on
     * the right side of the splitpane.  If the user decides to cancel on switching
     * screens (via the JTree), this variable is invoked to go back to the original
     * screen so the unsaved change method would still work properly.
     */
    private DataEntryPanel oldPane;

    /**
     * The number of times in a row this session has handled a warning message with
     * exactly the same text as the most recent warning message.
     */
    private int lastMessageCount;

    /**
     * The most recent warning message handled by this session.  A running count is
     * kept of how many times in a row we have seen this message.
     */
    private String lastMessage;

    /**
     * A space filler with semi-useful information about the application, and the database
     * this session is connected to.
     */
    private DataEntryPanel splashScreen;

    /**
     * Tracks whether or not we are currently in the middle of updating the
     * editor pane.  When we are, various behaviours are suppressed in order
     * to prevent asking the user the same questions multiple times, or asking
     * seemingly contradictory questions one after the other.
     */
    private boolean editorComponentUpdateInProgress = false;

    /**
     * A map that links an engine to a panel. This is used so that only
     * one of each engine and panel ever exist per project.
     */
	private Map<MergeEngineImpl, EngineSettingsPanel> mergeEnginePanels;
	
	 /**
     * A map that links an engine to a panel. This is used so that only
     * one of each engine and panel ever exist per project.
     */
	private Map<MatchEngineImpl, EngineSettingsPanel> matchEnginePanels;
	
	 /**
     * A map that links an engine to a panel. This is used so that only
     * one of each engine and panel ever exist per project.
     */
	private Map<CleanseEngineImpl, EngineSettingsPanel> cleanseEnginePanels;

	/**
     * A map that links an engine to a panel. This is used so that only
     * one of each engine and panel ever exist per project.
     */
	private Map<AddressCorrectionEngine, EngineSettingsPanel> addressCorrectionEnginePanels;
	
	private final String DQGURU_SUPPORT_URL = "http://www.sqlpower.ca/page/dqguru_support";
	
	private final Action getPremiumSupportAction = new AbstractAction("Get Premium Support") {
		public void actionPerformed(ActionEvent e) {
			try {
				BrowserUtil.launch(DQGURU_SUPPORT_URL);
			} catch (IOException ex) {
				MMSUtils.showExceptionDialog(frame, "Could not open URL '"
						+ DQGURU_SUPPORT_URL + "' in your web browser", ex);
			}
		}
		
	};
	
	/**
     * Creates a new MatchMaker session, complete with Swing GUI. Normally you
     * would use a LoginDialog instead of calling this constructor directly.
     *
     * @param context
     *            the Swing-specific session context
     * @param sessionImpl
     *            The session that actually does the dirty work (ORM and stuff
     *            like that).
	 * @throws SQLException
     */
	public MatchMakerSwingSession(SwingSessionContext context, MatchMakerSession sessionImpl) throws SQLException {
        this.sessionImpl = sessionImpl;
        this.sessionContext = context;
        this.smallMMIcon = MMSUtils.getFrameImageIcon();
        
        matchEnginePanels = new HashMap<MatchEngineImpl, EngineSettingsPanel>();
        mergeEnginePanels = new HashMap<MergeEngineImpl, EngineSettingsPanel>();
        cleanseEnginePanels = new HashMap<CleanseEngineImpl, EngineSettingsPanel>();
        addressCorrectionEnginePanels = new HashMap<AddressCorrectionEngine, EngineSettingsPanel>();
        
        lifecycleListener = new ArrayList<SessionLifecycleListener<MatchMakerSession>>();
        
        // this grabs warnings from the business model and DAO's and lets us handle them.
        sessionImpl.addWarningListener(new WarningListener() {
			public void handleWarning(String message) {
				MatchMakerSwingSession.this.handleWarning(message);
			}
		});

        frame = new JFrame("SQL Power DQguru: "+sessionImpl.getDBUser()+"@"+sessionImpl.getDatabase().getName());
        statusLabel = new JLabel();
        warningDialog = new JDialog(frame, "DQguru Warnings");
        warningTextArea = new JTextArea(6, 40);
        JComponent cp = (JComponent) warningDialog.getContentPane();
        cp.setLayout(new BorderLayout(0, 10));
        cp.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        cp.add(new JScrollPane(warningTextArea), BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(new JButton(clearWarningsAction));
        buttonPanel.add(new JButton(closeWarningDialogAction));
        cp.add(buttonPanel, BorderLayout.SOUTH);
        
        MatchMakerUtils.listenToHierarchy(removeEditorListener, getCurrentFolderParent());
        MatchMakerUtils.listenToHierarchy(removeEditorListener, getTranslateGroupParent());
	}

	public void showGUI() {
	    buildGUI();
        frame.setVisible(true);
		splitPane.setDividerLocation(0.2);
		
		// adds a widget that allows the user to expand/collapse the splitpane
		splitPane.setDividerSize(10);
		splitPane.setOneTouchExpandable(true);
    }

    private void buildGUI() {

        macOSXRegistration();
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(frame));
        frame.setIconImage(smallMMIcon.getImage());
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

		newDeDupeAction = new NewProjectAction(this, "New De-duping Project", Project.ProjectMode.FIND_DUPES);
//		TODO: Implement Cross-referencing projects first before re-enabling this action
//		newXrefAction = new NewProjectAction(this, "New X-refing Project", Project.ProjectMode.BUILD_XREF);
		newCleanseAction = new NewProjectAction(this, "New Cleansing Project", Project.ProjectMode.CLEANSE);
		newAddressAction = new NewProjectAction(this, "New Address Correction Project", Project.ProjectMode.ADDRESS_CORRECTION);
		
        JMenuBar menuBar = new JMenuBar();

		//Settingup
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic('f');

		if (!MAC_OS_X) {
			fileMenu.add(userPrefsAction);
		}
		fileMenu.add(new ExportMungePenToPDFAction(this));
		if (!MAC_OS_X) {
			fileMenu.addSeparator();
			fileMenu.add(exitAction);
		}
		menuBar.add(fileMenu);
		
		JMenu editMenu = new JMenu("Edit");
		editMenu.setMnemonic('e');
		editMenu.add(undoAction);
		editMenu.add(redoAction);
		menuBar.add(editMenu);

		// the connections menu is set up when a new project is created (because it depends on the current DBTree)
		JMenu databaseMenu = new JMenu("Connections");
		databaseMenu.setMnemonic('d');
		databaseMenu.add(remoteLoginAction);
        databaseMenu.add(createRepositoryAction);
        databaseMenu.addSeparator();
		databaseMenu.add(databaseConnectionAction);
		menuBar.add(databaseMenu);
		
		JMenu projectMenu = new JMenu("Project");
		projectMenu.setMnemonic('m');
		projectMenu.add(newDeDupeAction);
		projectMenu.add(newCleanseAction);
		projectMenu.add(newAddressAction);
//		TODO: Implement Cross-referencing projects first before re-enabling this action
//		projectMenu.add(newXrefAction);
		projectMenu.addSeparator();
		projectMenu.add(deleteProjectAction);
		
		// TODO: Match statistics has been disabled until re-implementation
//		projectMenu.addSeparator();
//		projectMenu.add(showMatchStatisticInfoAction);
		projectMenu.addSeparator();
		projectMenu.add(new ImportProjectAction(this, frame));
		projectMenu.add(new ExportProjectAction(this, frame));
		
		menuBar.add(projectMenu);

		JMenu toolsMenu = new JMenu("Tools");
		toolsMenu.setMnemonic('t');
		toolsMenu.add(sqlQueryAction);
		toolsMenu.add(new EditTranslateAction(this));
		menuBar.add(toolsMenu);

		// Commented the 'Window' menu until we actually have something to put in it
//        JMenu windowMenu = new JMenu("Window");
//        windowMenu.setMnemonic('w');
//        menuBar.add(windowMenu);
        
		ImageIcon helpIcon = SPSUtils.createIcon("help", "Help");
        helpAction = new HelpAction(frame, helpIcon);

		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic('h');
        if (!MAC_OS_X) {
            helpMenu.add(aboutAction);
            helpMenu.addSeparator();
        }
        helpMenu.add(helpAction);
		
        buildExampleTableAction = new BuildExampleTableAction(this);
        helpMenu.add(buildExampleTableAction);

        helpMenu.addSeparator();
        helpMenu.add(getPremiumSupportAction);
        
        supportOnTheWebAction = SPSUtils.forumAction;
        supportOnTheWebAction.putValue(Action.NAME, "Community Support Forum");
        helpMenu.add(supportOnTheWebAction);
        
        menuBar.add(helpMenu);
		
		frame.setJMenuBar(menuBar);

		Container projectBarPane = frame.getContentPane();
		projectBarPane.setLayout(new BorderLayout());

		tree = new JTree(new MatchMakerTreeModel(getCurrentFolderParent(),getBackupFolderParent(),getTranslateGroupParent(), this));
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		MatchMakerTreeMouseAndSelectionListener matchMakerTreeMouseAndSelectionListener = new MatchMakerTreeMouseAndSelectionListener(this);
		tree.addMouseListener(matchMakerTreeMouseAndSelectionListener);
		tree.addTreeSelectionListener(matchMakerTreeMouseAndSelectionListener);
		tree.setCellRenderer(new MatchMakerTreeCellRenderer());
		tree.setRootVisible(false);
        tree.setShowsRootHandles(true);

        JScrollPane treePane = new JScrollPane(SPSUtils.getBrandedTreePanel(tree));
        treePane.setMinimumSize(new Dimension(5,5));
        treePane.setPreferredSize(new Dimension(1,1));
		splitPane.setLeftComponent(treePane);
		setCurrentEditorComponent(null);

		JPanel cp = new JPanel(new BorderLayout());
		cp.add(splitPane, BorderLayout.CENTER);
		MemoryMonitor memoryMonitor = new MemoryMonitor();
		memoryMonitor.start();
		
		JPanel statusBarPanel = new JPanel(new BorderLayout());
		JLabel memoryLabel = memoryMonitor.getLabel();
		memoryLabel.setBorder(new EmptyBorder(0, 20, 0, 20));
		statusBarPanel.add(statusLabel, BorderLayout.CENTER);
		statusBarPanel.add(memoryLabel, BorderLayout.EAST);
        cp.add(statusBarPanel, BorderLayout.SOUTH);
		projectBarPane.add(cp, BorderLayout.CENTER);
		
		frame.setBounds(sessionContext.getFrameBounds());
		frame.addWindowListener(new MatchMakerFrameWindowListener());
	}

    public FolderParent getBackupFolderParent() {
    	FolderParent backup = sessionImpl.getBackupFolderParent();
        logger.debug("getBackupFolderParent(): Found folder list: "+backup.getChildren());
        return backup;
	}

	public FolderParent getCurrentFolderParent() {
		FolderParent current = sessionImpl.getCurrentFolderParent();
        logger.debug("getCurrentFolderParent(): Found folder list: "+current.getChildren());
        return current;
	}
	
	public TranslateGroupParent getTranslateGroupParent() {
		TranslateGroupParent translate = sessionImpl.getTranslations();
        logger.debug("getTranslateGroupParent(): Found folder list: "+translate.getChildren());
        return translate;
	}

    /**
     * Returns the frame for this Swing session.
     */
    public JFrame getFrame() {
        return frame;
    }

    public SwingSessionContext getContext() {
        return sessionContext;
    }

    private final class EditProjectAction extends AbstractAction {
		private EditProjectAction(String name) {
			super(name);
		}

		public void actionPerformed(ActionEvent e) {
			JTree menuTree = getTree();
			TreePath menuPath;
			Project project = MMSUtils.getTreeObject(menuTree,Project.class);
			if (project == null) return;

			try {
				menuPath = ((MatchMakerTreeModel)menuTree.getModel()).getPathForNode(project);
				menuTree.setSelectionPath(menuPath);
			} catch (Exception ex) {
				MMSUtils.showExceptionDialog(frame, "Couldn't create project editor", ex);
			}
		}
	}

    /**
     * A window listener which calls {@link #exit()}
     */
	class MatchMakerFrameWindowListener extends WindowAdapter {
		@Override
		public void windowClosing(WindowEvent e) {
			close();
		}

	}

    /**
     * This method should become unnecessary soon, since the app will just continually keep
     * the user settings up-to-date...
     */
	public void saveSettings() {
		sessionContext.setFrameBounds(frame.getBounds());  // XXX we should do this in a component listener
	}

    /**
	 * Shows the given component in the main part of the frame's UI.
	 *
	 * @param editor
	 *            The editor component to display in the UI. If you pass in
	 *            null, then no editor will be showing.
     * @throws SQLException
	 */
	public void setCurrentEditorComponent(DataEntryPanel pane) {
		if (pane == oldPane && pane != null) {
			return;	// User clicked on same item, don't hassle them
		}
        if (editorComponentUpdateInProgress) {
            return;
        }
        try {
            editorComponentUpdateInProgress = true;
            if (splashScreen == null){
                splashScreen = new NoEditEditorPane(new MatchMakerSplashScreen(this).getSplashScreen());
            }
            boolean save = false, doit = true;

            if (oldPane != null) {
            	if (oldPane.hasUnsavedChanges()) {
            		String[] options = { "Save", "Discard Changes", "Cancel" };
            		final int O_SAVE = 0, O_DISCARD = 1, O_CANCEL = 2;
            		int ret = JOptionPane.showOptionDialog(
            				frame,
            				String.format("Your %s has unsaved changes", SPSUtils.niceClassName(oldPane)),
            				"Warning", JOptionPane.OK_CANCEL_OPTION,
            				JOptionPane.QUESTION_MESSAGE, null, options,
            				options[0]);

            		switch (ret) {
            		case JOptionPane.CLOSED_OPTION:
            			save = false;
            			doit = false;
            			break;
            		case O_SAVE:
            			save = true;
            			doit = false;
            			break;
            		case O_DISCARD:
            			save = false;
            			doit = true;
            			break;
            		case O_CANCEL:
            			save = false;
            			doit = false;
            			//The treepath should never be null if it reaches here
            			//since prompting this means that the right side of the splitpane
            			//must have at least been replaced once.
            			tree.setSelectionPath(lastTreePath);
            			break;
            		}
            		if (save) {
            			doit = oldPane.applyChanges();
            			logger.debug("Last tree path was " + lastTreePath);
            			logger.debug("Apply changes() in setCurrentEditorComponenet()! save is " + save + " and doit is " + doit);
            			if (!doit){
            				tree.setSelectionPath(lastTreePath);
            			}
            			logger.debug("Tree has selection path " + tree.getSelectionPath());
            		} else if (doit) {
            			oldPane.discardChanges();
            			doit = true;
            		}
            	}
            }
            if (doit) {
            	// clears the undo stack and the listeners to the match
            	// maker object
            	if (oldPane instanceof CleanupModel) {
            		((CleanupModel) oldPane).cleanup();
            	}

            	//TODO change this to a InitModel
            	if (pane instanceof AbstractUndoableEditorPane) {
            		((AbstractUndoableEditorPane) pane).initUndo();
            	}
            		
                //Remebers the treepath to the last node that it clicked on
                if (pane != null){
                    lastTreePath = tree.getSelectionPath();
                    // If this line is not here, the divider would refuse to
                    // move and the left component would not be visible.
                    pane.getPanel().setMinimumSize(new Dimension(5,5));
                    splitPane.setRightComponent(pane.getPanel());
                    oldPane = pane;
                } else {
                    // If this line is not here, the divider would refuse to
                    // move and the left component would not be visible.
                    splashScreen.getPanel().setMinimumSize(new Dimension(5,5));
                	splitPane.setRightComponent(splashScreen.getPanel());
                    oldPane = splashScreen;
                }
                
                // If this line was not here, the left component would get 
                // forced to its minimum size. This sets the divider to remain
                // at the location that has been set before the editor change.
    			splitPane.setDividerLocation(splitPane.getDividerLocation());
            } 
            
        } finally {
            editorComponentUpdateInProgress = false;
        }
	}

	/**
	 * Creates a MatchMakerSwingSession and shows the login prompt. This method
	 * is an acceptable way to launch the Swing GUI of the MatchMaker
	 * application.
	 */
	public static void main(String args[]) {

        ArchitectUtils.startup();
        System.setProperty("apple.laf.useScreenMenuBar", "true");
		ArchitectUtils.configureLog4j();
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			logger.error("Unable to set native look and feel. Continuing with default.", e);
		}
		
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		    	try {
		    		Preferences prefs = Preferences.userNodeForPackage(MatchMakerSessionContext.class);
		    		SwingSessionContext context = new SwingSessionContextImpl(prefs);
                    context.launchDefaultSession();
		    	} catch (Exception ex) {
		    		JDialog d = MMSUtils.showExceptionDialogNoReport("Couldn't start application!", ex);
		    		d.addWindowListener(new WindowAdapter() {
		    			@Override
		    			public void windowDeactivated(WindowEvent e) {
		    				System.exit(0);
		    			}
		    			
		    			@Override
		    			public void windowClosed(WindowEvent e) {
		    				System.exit(0);
		    			}
		    		});
		    	}
		    }
		});
	}

    /**
     * Registers this application in Mac OS X if we're running on that platform.
     *
     * <p>This code came from Apple's "OS X Java Adapter" example.
     */
    private void macOSXRegistration() {
        if (MAC_OS_X) {
            try {
                Class osxAdapter = ClassLoader.getSystemClassLoader().loadClass("ca.sqlpower.architect.swingui.OSXAdapter");

                // The main registration method.  Takes quitAction, prefsAction, aboutAction.
                Class[] defArgs = { Action.class, Action.class, Action.class };
                Method registerMethod = osxAdapter.getDeclaredMethod("registerMacOSXApplication", defArgs);
                if (registerMethod != null) {
                    Object[] args = { exitAction, userPrefsAction, aboutAction};
                    registerMethod.invoke(osxAdapter, args);
                }

                // The enable prefs method.  Takes a boolean.
                defArgs = new Class[] { boolean.class };
                Method prefsEnableMethod =  osxAdapter.getDeclaredMethod("enablePrefs", defArgs);
                if (prefsEnableMethod != null) {
                    Object args[] = {Boolean.TRUE};
                    prefsEnableMethod.invoke(osxAdapter, args);
                }
            } catch (NoClassDefFoundError e) {
                // This will be thrown first if the OSXAdapter is loaded on a system without the EAWT
                // because OSXAdapter extends ApplicationAdapter in its def
                logger.error("This version of Mac OS X does not support the Apple EAWT.  Application Menu handling has been disabled (" + e + ")");
            } catch (ClassNotFoundException e) {
                // This shouldn't be reached; if there's a problem with the OSXAdapter we should get the
                // above NoClassDefFoundError first.
                logger.error("This version of Mac OS X does not support the Apple EAWT.  Application Menu handling has been disabled (" + e + ")");
            } catch (Exception e) {
                logger.error("Exception while loading the OSXAdapter:", e);
            }
        }
    }

	public JTree getTree() {
		return tree;
	}

	public void setTree(JTree tree) {
		if (this.tree != tree) {
			this.tree = tree;
			//TODO fire event
		}
	}

	public TranslateGroupParent getTranslations() {
		return sessionImpl.getTranslations();
	}

	public String getLastImportExportAccessPath() {
        return sessionContext.getLastImportExportAccessPath();
    }

    public void setLastImportExportAccessPath(String path) {
        sessionContext.setLastImportExportAccessPath(path);
    }

    /**
     * Appends the warning to the warningTextArea and makes the warningDialog visible
     * as well as telling all the warning listeners about the warning.
     */
    public void handleWarning(String message) {
    	if (message.equals(lastMessage)) {
    		lastMessageCount++;
    	} else {
    		if ( lastMessageCount > 0) {
    			warningTextArea.append("Last message repeated " +lastMessageCount + (lastMessageCount == 1? " time.\n" :" times.\n"));
    			lastMessageCount = 0;
    		}
    		lastMessage = message;
    		logger.debug("Handling warning: " + message);
			warningTextArea.append(message + "\n");
			if (!warningDialog.isVisible()) {
				warningDialog.pack();
				warningDialog.setVisible(true);
				warningDialog.setLocationRelativeTo(frame);
			}
			warningDialog.requestFocus();

			// for (int i = 0; i < Integer.MAX_VALUE; i++) {
			// Toolkit.getDefaultToolkit().beep();
			// }
			synchronized (warningListeners) {
				for (int i = warningListeners.size() - 1; i >= 0; i--) {
					warningListeners.get(i).handleWarning(message);
				}
			}
    	}
    }

    public void addWarningListener(WarningListener l) {
        synchronized (warningListeners) {
            warningListeners.add(l);
        }
    }

    public void removeWarningListener(WarningListener l) {
        synchronized (warningListeners) {
            warningListeners.remove(l);
        }
    }

    public String getAppUser() {
        return sessionImpl.getAppUser();
    }
    
    public String getAppUserEmail() {
    	return sessionImpl.getAppUserEmail();
    }

    public String getDBUser() {
        return sessionImpl.getDBUser();
    }

    public SQLDatabase getDatabase() {
        return sessionImpl.getDatabase();
    }

    public Date getSessionStartTime() {
        return sessionImpl.getSessionStartTime();
    }

    public PlFolder findFolder(String foldername) {
        return sessionImpl.findFolder(foldername);
    }

    public <T extends MatchMakerObject> MatchMakerDAO<T> getDAO(Class<T> businessClass) {
        return sessionImpl.getDAO(businessClass);
    }

    public Connection getConnection() {
        return sessionImpl.getConnection();
    }

	public Project getProjectByName(String name) {
		return sessionImpl.getProjectByName(name);
	}

	public boolean isThisProjectNameAcceptable(String name) {
		return sessionImpl.isThisProjectNameAcceptable(name);
	}

    public String createNewUniqueName() {
        return sessionImpl.createNewUniqueName();
    }

	public long countProjectByName(String name) {
		return sessionImpl.countProjectByName(name);
	}

	/**
	 * XXX This is for actions that aren't written yet.
	 * It will be removed when the application is completed;
	 * only used in about half a dozen places now...
	 */
	private class DummyAction extends AbstractAction {

		private String label;
		private JFrame parent;

		public void actionPerformed(ActionEvent e) {
			JOptionPane.showMessageDialog(parent,
				String.format("The %s function is not yet implemented", label),
				"Apologies",
				JOptionPane.INFORMATION_MESSAGE);
		}

		public DummyAction(JFrame parent, String label) {
			super(label);
			this.label = label;
			this.parent = parent;
		}

		DummyAction(String label) {
			this(frame != null ? frame : null, label);
		}

	}

	/**
	 * persist the match maker object to the database
	 *
	 * XXX Push this into the match maker session interface
	 * @param mmo
	 */
	public void save(MatchMakerObject mmo) {
		if (mmo instanceof Project){
			Project project = (Project)mmo;
			ProjectDAO dao = (ProjectDAO) getDAO(Project.class);
			dao.save(project);
		} else if (mmo instanceof MatchMakerFolder){
			Project project = (Project)mmo.getParent();
			ProjectDAO dao = (ProjectDAO) getDAO(Project.class);
			dao.save(project);
		} else if (mmo instanceof TableMergeRules) {
			Project project = ((TableMergeRules)mmo).getParentProject();
			ProjectDAO dao = (ProjectDAO) getDAO(Project.class);
			dao.save(project);
		} else if (mmo instanceof PlFolder){
			PlFolderDAO dao = (PlFolderDAO) getDAO(PlFolder.class);
			dao.save((PlFolder) mmo);
		} else if (mmo instanceof MungeProcess) {
			MungeProcess cg = (MungeProcess)mmo;
			MungeProcessDAO dao = (MungeProcessDAO) getDAO(MungeProcess.class);
			dao.save(cg);
		} else if (mmo instanceof MatchMakerTranslateGroup) {
			MatchMakerTranslateGroup tg = (MatchMakerTranslateGroup)mmo;
			MatchMakerTranslateGroupDAO dao = (MatchMakerTranslateGroupDAO) getDAO(MatchMakerTranslateGroup.class);
			dao.save(tg);
		} else {
			throw new UnsupportedOperationException("We do not yet support "+mmo.getClass() + " persistance");
		}
	}

	/**
     * Deletes the MatchMakerObject passed in by asking the appropriate DAO to
     * delete it directly. This will also remove <code>mmo</code> from its
     * parent in the in-memory version of the object.
     * 
     * @param mmo The MatchMakerObject to delete, both from the persistence
     * layer and by removing it from its parent object.
     */
	@SuppressWarnings("unchecked")
	public <T extends MatchMakerObject> void delete(MatchMakerObject<T, ?> mmo) {
		if (mmo.getParent() != null) {
		    MatchMakerDAO dao = getDAO(mmo.getClass());
		    logger.debug("dao is:"+ dao+ "mmo is"+ mmo);
		    dao.delete(mmo);
        } else {
            throw new IllegalStateException("I don't know how to delete a parentless object");
        }
		
		// If mmo is a project, clean up and remove its engine panels
		if (mmo instanceof Project) {
			EngineSettingsPanel panel;
			
			panel = matchEnginePanels.remove(((Project) mmo).getMatchingEngine());
			if (panel != null) {
				panel.cleanup();
			}
			logger.debug("matchEnginePanels.size() returns: " + matchEnginePanels.size());

			panel = mergeEnginePanels.remove(((Project) mmo).getMergingEngine());
			if (panel != null) {
				panel.cleanup();
			}
			logger.debug("mergeEnginePanels.size() returns: " + mergeEnginePanels.size());
			
			panel = cleanseEnginePanels.remove(((Project) mmo).getCleansingEngine());
			if (panel != null) {
				panel.cleanup();
			};
			logger.debug("cleanseEnginePanels.size() returns: " + cleanseEnginePanels.size());
			
			panel = addressCorrectionEnginePanels.remove(((Project) mmo).getAddressCorrectionEngine());
			if (panel != null) {
				panel.cleanup();
			};
		}
	}
	
	/**
	 * Move a match maker object from one parent ( can be null) to a new match maker object.
	 * The destination object must support children.  This function persists the
	 * move to the database and will save any other unsaved changes in both parents
	 * and the moving object
	 *
	 * @param objectToMove the object you want to move
	 * @param destination the new parent object
	 */
	@SuppressWarnings("unchecked")
	public void move(MatchMakerObject objectToMove, MatchMakerObject destination) {
		if (!destination.allowsChildren()) throw new IllegalArgumentException("The destination object "+destination+" Does not support children");
		MatchMakerObject oldParent = objectToMove.getParent();
		if (oldParent != null) {
			oldParent.removeChild(objectToMove);
		}
		destination.addChild(objectToMove);
		save(destination);
	}

    /**
     * See {@link #smallMMIcon}.
     */
    public ImageIcon getSmallMMIcon() {
        return smallMMIcon;
    }

    public Version getPLSchemaVersion() {
        return sessionImpl.getPLSchemaVersion();
    }
    
    /**
     * Opens a dialog for the user to choose a custom colour.
     * Returns the choosen colour.
     */
    public Color getCustomColour(Color initial) {
    	if (initial == null) {
    		initial = Color.BLACK;
    	}
    	colourChooser.setColor(initial);
    	ColorTracker ok = new ColorTracker(colourChooser);
        JDialog dialog = JColorChooser.createDialog(getFrame(), "Choose a custom colour", true, colourChooser, ok, null);

        dialog.setVisible(true); 

        return ok.getColor();

    }
    
    /**
     * Action Listener used by the custom colour dialog.
     */
    class ColorTracker implements ActionListener, Serializable {
        JColorChooser chooser;
        Color color;

        public ColorTracker(JColorChooser c) {
            chooser = c;
        }

        public void actionPerformed(ActionEvent e) {
            color = chooser.getColor();
        }

        public Color getColor() {
            return color;
        }
    }
    
    public SQLTable findPhysicalTableByName(String catalog, String schema, String tableName) throws SQLObjectException {
    	return sessionImpl.findPhysicalTableByName(catalog, schema, tableName);
	}

    public SQLTable findPhysicalTableByName(String spDataSourceName, String catalog, String schema, String tableName) throws SQLObjectException {
    	return sessionImpl.findPhysicalTableByName(spDataSourceName, catalog, schema, tableName);
	}
    
    public boolean tableExists(String catalog, String schema,
    		String tableName) throws SQLObjectException {
    	return sessionImpl.tableExists(catalog, schema, tableName);
	}
    
    public boolean tableExists(String spDataSourceName, String catalog, String schema,
    		String tableName) throws SQLObjectException {
    	return sessionImpl.tableExists(spDataSourceName, catalog, schema, tableName);
	}

     public boolean tableExists(SQLTable table) throws SQLObjectException {
         return sessionImpl.tableExists(table);
	}

     /**
      * this method requires real JDBC connection and create sql statement
     * on it.
      */
	public boolean canSelectTable(SQLTable table) {
	    return sessionImpl.canSelectTable(table);
	}

	// Documentation inherited from interface
	public void registerSwingWorker(SPSwingWorker worker) {
		swingWorkers.add(worker);
	}

	// Documentation inherited from interface
	public void removeSwingWorker(SPSwingWorker worker) {
		swingWorkers.remove(worker);
	}

	/**
	 * Call this method to close the delegate session and cancel
	 * running SPSwingWorker threads. If there are any remaining SPSwingWorker
	 * threads, the GUI will warn the user and force quit if necessary.
	 */
	public boolean close() {
		boolean save = false, doit = true;

		if (oldPane != null) {
			if (oldPane.hasUnsavedChanges()) {
				String[] options = { "Save", "Discard Changes", "Cancel" };
				final int O_SAVE = 0, O_DISCARD = 1, O_CANCEL = 2;
				int ret = JOptionPane.showOptionDialog(
						frame,
						String.format("Your %s has unsaved changes", SPSUtils.niceClassName(oldPane)),
						"Warning", JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, options,
						options[0]);

				switch (ret) {
				case JOptionPane.CLOSED_OPTION:
					save = false;
					doit = false;
					return false;
				case O_SAVE:
					save = true;
					doit = false;
					break;
				case O_DISCARD:
					save = false;
					doit = true;
					break;
				case O_CANCEL:
					save = false;
					doit = false;
					return false;
				}
				if (save) {
					doit = oldPane.applyChanges();
        			logger.debug("Doit boolean is " + doit + " and save boolean is " + save);
				} else if (doit) {
					oldPane.discardChanges();
					doit = true;
				}
			}
		}
		if (!doit) {  //don't close, as there are unsaved changes with errors			
			return false;
		} else {
        	// clears the undo stack and the listeners to the match
        	// maker object
        	if (oldPane instanceof CleanupModel) {
        		((CleanupModel) oldPane).cleanup();
        	}
        	
			
			// If we still have SwingWorker threads running, 
		    // tell them to cancel, and then ask the user to try again later.
			// Note that it is not safe to force threads to stop, so we will
			// have to wait until the threads stop themselves.
			if (swingWorkers.size() > 0) {
				for (SPSwingWorker currentWorker : swingWorkers) {
					currentWorker.setCancelled(true);
				}
		
				Object[] options = {"Wait", "Force Quit"};
				int n = JOptionPane.showOptionDialog(frame, 
						"There are still unfinished tasks running in the DQguru.\n" +
						"You can either wait for them to finish and try closing again later" +
						", or force the application to close. Quitting will leave these tasks unfinished.", 
						"Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, 
						null, options, options[0]);
				if (n == 0) {
					return false;
				} else {
					for (SPSwingWorker currentWorker : swingWorkers) {
						currentWorker.kill();
					}
				}
			}
		
			saveSettings();
		
			// It is possible this method will be called again via indirect recursion
			// because the frame has a windowClosing listener that calls session.close().
			// It should be harmless to have this close() method invoked a second time.
			if (frame != null) {
				// XXX this could/should be done by the frame with a session closing listener
				frame.dispose();
			}
		
			if (!sessionImpl.close()) {
				return false;
			}
			fireSessionClosing();
			
			return true;			
	    }
	}
	
	/**
	 * @return Returns the original editor pane.
	 */
	public DataEntryPanel getOldPane() {
		return oldPane;
	}
	
	/**
	 * Returns or creates the editor panel linked to the given engine
	 * 
	 * @param mei The current engine
	 * @param project The current project
	 */
	public EngineSettingsPanel getMergeEnginePanel(MergeEngineImpl mei, Project project) {
		EngineSettingsPanel ep = mergeEnginePanels.get(mei);
		if (mergeEnginePanels.get(mei) == null) {
			ep = new EngineSettingsPanel(this,project, getFrame(), EngineType.MERGE_ENGINE);
			mergeEnginePanels.put(mei,ep); 
		}
		return ep;
	}
	
	/**
	 * Returns or creates the editor panel linked to the given engine
	 * 
	 * @param mei The current engine
	 * @param project The current project
	 */
	public EngineSettingsPanel getMatchEnginePanel(MatchEngineImpl mei, Project project) {
		EngineSettingsPanel ep = matchEnginePanels.get(mei);
		if (ep == null) {
			ep = new EngineSettingsPanel(this,project, getFrame(), EngineType.MATCH_ENGINE);
			matchEnginePanels.put(mei,ep); 
		}
		return ep;
	}
	
	/**
	 * Returns or creates the editor panel linked to the given engine
	 * 
	 * @param mei The current engine
	 * @param project The current project
	 */
	public EngineSettingsPanel getCleanseEnginePanel(CleanseEngineImpl mei, Project project) {
		EngineSettingsPanel ep = cleanseEnginePanels.get(mei);
		if (ep == null) {
			ep = new EngineSettingsPanel(this,project, getFrame(), EngineType.CLEANSE_ENGINE);
			cleanseEnginePanels.put(mei,ep);
		}
		return ep;
	}

	/**
	 * Returns or creates the editor panel linked to the given Address Correction Engine engine
	 * 
	 * @param engine The current engine
	 * @param project The current project
	 */
	public EngineSettingsPanel getAddressCorrectionEnginePanel(AddressCorrectionEngine engine, Project project) {
		EngineSettingsPanel ep = addressCorrectionEnginePanels.get(engine);
		if (ep == null) {
			ep = new EngineSettingsPanel(this,project, getFrame(), EngineType.ADDRESS_CORRECTION_ENGINE);
			addressCorrectionEnginePanels.put(engine,ep);
		}
		return ep;
	}
	
	public EngineSettingsPanel getValidatedAddressCommittingEnginePanel(AddressCorrectionEngine engine, Project project) {
		EngineSettingsPanel ep = addressCorrectionEnginePanels.get(engine);
		if (ep == null) {
			ep = new EngineSettingsPanel(this, project, getFrame(), EngineType.VALIDATED_ADDRESS_COMMITING_ENGINE);
			addressCorrectionEnginePanels.put(engine,ep);
		}
		return ep;
	}
	
	//undo stuff
	
	protected UndoAction undoAction = new UndoAction();
	protected RedoAction redoAction = new RedoAction();
	protected UndoManager undo;
	
	/**
	UndoAction creates an undo menu item with behaviour
	**/
    class UndoAction extends AbstractAction {
        public UndoAction() {
            super("Undo");
            setEnabled(false);
            putValue(MNEMONIC_KEY, KeyEvent.VK_U);
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));

        }

        public void actionPerformed(ActionEvent e) {
            try {
                undo.undo();
            } catch (CannotUndoException ex) {
                SPSUtils.showExceptionDialogNoReport(getFrame(),"Unable to undo: ", ex);
                ex.printStackTrace();
            }
            updateUndoState();
            redoAction.updateRedoState();
        }

        protected void updateUndoState() {
            if (undo.canUndo()) {
                setEnabled(true);
            } else {
                setEnabled(false);
            }
        }
    }

	/**
	RedoAction creates a redo menu item with behaviour
	**/
    class RedoAction extends AbstractAction {
        public RedoAction() {
            super("Redo");
            setEnabled(false);
            putValue(MNEMONIC_KEY, KeyEvent.VK_R);
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
        }

        public void actionPerformed(ActionEvent e) {
            try {
                undo.redo();
            } catch (CannotRedoException ex) {
            	SPSUtils.showExceptionDialogNoReport(getFrame(),"Unable to undo: ", ex);
                ex.printStackTrace();
            }
            updateRedoState();
            undoAction.updateUndoState();
        }

        protected void updateRedoState() {
            if (undo.canRedo()) {
                setEnabled(true);
            } else {
                setEnabled(false);
            }
        }
    }

	public UndoManager getUndo() {
		return undo;
	}

	public void setUndo(UndoManager undo) {
		this.undo = undo;
	}

	public void refreshUndoAction() {
		undoAction.updateUndoState();
		redoAction.updateRedoState();
	}

	/**
	 * Returns whether the run engine actions have been 
	 * enabled/disabled in this session.
	 */
	public boolean isEnginesEnabled() {
		return enginesEnabled;
	}

	public SQLDatabase getDatabase(JDBCDataSource dataSource) {
		return sessionImpl.getDatabase(dataSource);
	}
	
	public void setSelectNewChild(Boolean selectNewChild) {
		removeEditorListener.setSelectNewChild(selectNewChild);
	}
	
	/**
	 * Listens for child removed events and sets the current
	 * editor pane to null if the current editor pane is editing
	 * the child being removed
	 */
	private class SelectRemoveEditorListener implements MatchMakerListener {
		private Boolean selectNewChild = true;

		public void mmChildrenInserted(MatchMakerEvent evt) {
			// selects the new child on the tree if one new child is added
			if (selectNewChild && !evt.isUndoEvent() && !evt.isCompoundEvent() && evt.getChildren().size() == 1) {
				final MatchMakerObject insertedMMO = (MatchMakerObject)evt.getChildren().get(0);
				if (!(insertedMMO instanceof SQLInputStep
						|| insertedMMO instanceof MungeResultStep
						|| insertedMMO instanceof MungeStepOutput)) {
					SwingUtilities.invokeLater(new Runnable(){
						public void run() {
							MatchMakerTreeModel treeModel = (MatchMakerTreeModel)getTree().getModel();
							TreePath treePath = treeModel.getPathForNode(insertedMMO);
							getTree().setSelectionPath(treePath);
						}
					});
				}
			}
			for (MatchMakerObject mmo : (List<MatchMakerObject>)evt.getChildren()) {				
				MatchMakerUtils.listenToHierarchy(this, mmo);
			}
		}

		public void mmChildrenRemoved(MatchMakerEvent evt) {
			//sets the current editor pane to null if object is removed.
			if (oldPane instanceof MatchMakerEditorPane) {
				for (MatchMakerObject removedChild : (List<MatchMakerObject>)evt.getChildren()) {
					MatchMakerObject editorMMO = ((MatchMakerEditorPane)oldPane).getCurrentEditingMMO();
					if (removedChild.equals(editorMMO) || SQLPowerUtils.hierarchyContains(removedChild, editorMMO)) {
						MatchMakerTreeModel treeModel = (MatchMakerTreeModel)getTree().getModel();
						TreePath treePath = treeModel.getPathForNode(evt.getSource());
						getTree().setSelectionPath(treePath);
					}
				}
			}
			for (MatchMakerObject mmo : (List<MatchMakerObject>)evt.getChildren()) {				
				MatchMakerUtils.unlistenToHierarchy(this, mmo);
			}
		}

		// don't care
		public void mmPropertyChanged(MatchMakerEvent evt) {}

		// don't care
		public void mmStructureChanged(MatchMakerEvent evt) {}

		public void setSelectNewChild(Boolean selectNewChild) {
			this.selectNewChild = selectNewChild;
		}
	}
	
	public void addSessionLifecycleListener(SessionLifecycleListener<MatchMakerSession> listener) {
		lifecycleListener.add(listener);
	}
	
	public void removeSessionLifecycleListener(
			SessionLifecycleListener<MatchMakerSession> listener) {
		lifecycleListener.remove(listener);
	}

	private void fireSessionClosing() {
		SessionLifecycleEvent<MatchMakerSession> evt = 
			new SessionLifecycleEvent<MatchMakerSession>(this);
		final List<SessionLifecycleListener<MatchMakerSession>> listeners = 
			new ArrayList<SessionLifecycleListener<MatchMakerSession>>(lifecycleListener);
		for (SessionLifecycleListener<MatchMakerSession> listener: listeners) {
			listener.sessionClosing(evt);
		}
	}

	public void addStatusMessage(String message) {
		statusLabel.setText(message);
		logger.debug("Stub call: MatchMakerSwingSession.addStatusMessage()");
		
	}

	public void removeStatusMessage() {
		statusLabel.setText("");
		logger.debug("Stub call: MatchMakerSwingSession.removeStatusMessage()");
		
	}
}