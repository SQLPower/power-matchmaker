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

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.matchmaker.CleanseEngineImpl;
import ca.sqlpower.matchmaker.FolderParent;
import ca.sqlpower.matchmaker.MatchEngineImpl;
import ca.sqlpower.matchmaker.MatchMakerFolder;
import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchMakerSessionContext;
import ca.sqlpower.matchmaker.MatchMakerTranslateGroup;
import ca.sqlpower.matchmaker.MergeEngineImpl;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.TableMergeRules;
import ca.sqlpower.matchmaker.TranslateGroupParent;
import ca.sqlpower.matchmaker.WarningListener;
import ca.sqlpower.matchmaker.Project.ProjectMode;
import ca.sqlpower.matchmaker.dao.MatchMakerDAO;
import ca.sqlpower.matchmaker.dao.MatchMakerTranslateGroupDAO;
import ca.sqlpower.matchmaker.dao.MungeProcessDAO;
import ca.sqlpower.matchmaker.dao.PlFolderDAO;
import ca.sqlpower.matchmaker.dao.ProjectDAO;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.prefs.PreferencesManager;
import ca.sqlpower.matchmaker.swingui.action.BuildExampleTableAction;
import ca.sqlpower.matchmaker.swingui.action.DeleteProjectAction;
import ca.sqlpower.matchmaker.swingui.action.EditTranslateAction;
import ca.sqlpower.matchmaker.swingui.action.HelpAction;
import ca.sqlpower.matchmaker.swingui.action.NewProjectAction;
import ca.sqlpower.matchmaker.swingui.action.ShowMatchStatisticInfoAction;
import ca.sqlpower.matchmaker.swingui.engine.CleanseEnginePanel;
import ca.sqlpower.matchmaker.swingui.engine.MatchEnginePanel;
import ca.sqlpower.matchmaker.swingui.engine.MergeEnginePanel;
import ca.sqlpower.matchmaker.undo.AbstractUndoableEditorPane;
import ca.sqlpower.sql.PLSchemaException;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sql.SchemaVersionFormatException;
import ca.sqlpower.swingui.CommonCloseAction;
import ca.sqlpower.swingui.JDefaultButton;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.SPSwingWorker;
import ca.sqlpower.swingui.SwingWorkerRegistry;
import ca.sqlpower.util.Version;

/**
 * The Main Window for the MatchMaker Application; contains a main() method that is
 * the conventional way to start the application running.
 */
public class MatchMakerSwingSession implements MatchMakerSession, SwingWorkerRegistry {

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
    private JFrame warningDialog;

    /**
     * The component in the warningDialog which actually contains the messages.
     */
    private JTextArea warningTextArea;

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
    
    private Action userPrefsAction = new AbstractAction("User Preferences...") {
		public void actionPerformed(ActionEvent e) {
			JOptionPane.showMessageDialog(MatchMakerSwingSession.this.frame,
					"The User Preferences action for OS X is not yet implemented",
					"Apologies",
					JOptionPane.INFORMATION_MESSAGE);
		}
    };

	private Action aboutAction = new AbstractAction("About Power*MatchMaker...") {

		public void actionPerformed(ActionEvent evt) {
			// This is one of the few JDIalogs that can not get replaced
			// with a call to ArchitectPanelBuilder, because an About
			// box must have only ONE button...
			final JDialog d = new JDialog(getFrame(),
										  "About Power*MatchMaker");
			JPanel cp = new JPanel(new BorderLayout(12,12));
			cp.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
			final AboutPanel aboutPanel = new AboutPanel();
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
	        exit();
	    }
	};

	private Action loginAction = new AbstractAction("Login") {
		public void actionPerformed(ActionEvent e) {
			sessionContext.showLoginDialog(null);
		}

	};

	private Action logoutAction = new AbstractAction("Logout") {
		public void actionPerformed(ActionEvent e) {
			logger.debug("Stub call: .actionPerformed()");
			JOptionPane.showMessageDialog(MatchMakerSwingSession.this.frame,
					"The logout action is not yet implemented",
					"Apologies",
					JOptionPane.INFORMATION_MESSAGE);
		}

	};

	private Action newDeDupeAction = null;
	private Action newXrefAction = null;
	private Action newCleanseAction = null;
	
	private Action editProjectAction = new EditProjectAction("Edit Project");
	private Action deleteProjectAction = new DeleteProjectAction(this);

	private Action runMatchAction = new AbstractAction("Run Match") {

		public void actionPerformed(ActionEvent e) {
			Project match = MMSUtils.getTreeObject(getTree(),Project.class);
			if (match != null && match.getType() == ProjectMode.FIND_DUPES) {
				MatchMakerTreeModel treeModel = (MatchMakerTreeModel)getTree().getModel();
			    TreePath treePath = 
			    	treeModel.getPathForNode((MatchMakerObject<?,?>) treeModel.getChild(match,2));
			    getTree().setSelectionPath(treePath);
			}
		}
	};

	private Action runMergeAction = new AbstractAction("Run Merge") {

		public void actionPerformed(ActionEvent e) {
			Project match = MMSUtils.getTreeObject(getTree(),Project.class);
			if (match != null && match.getType() == ProjectMode.FIND_DUPES) {
				MatchMakerTreeModel treeModel = (MatchMakerTreeModel)getTree().getModel();
				TreePath treePath = 
					treeModel.getPathForNode((MatchMakerObject<?,?>) treeModel.getChild(match,5));
				getTree().setSelectionPath(treePath);
			}
		}
	};
	
	private Action runCleanseAction = new AbstractAction("Run Cleanse") {

		public void actionPerformed(ActionEvent e) {
			Project match = MMSUtils.getTreeObject(getTree(),Project.class);
			if (match != null && match.getType() == ProjectMode.CLEANSE) {
				MatchMakerTreeModel treeModel = (MatchMakerTreeModel)getTree().getModel();
				TreePath treePath = 
					treeModel.getPathForNode((MatchMakerObject<?,?>) treeModel.getChild(match,1));
				getTree().setSelectionPath(treePath);
			}
		}
	};

	private Action helpAction;
	private Action buildExampleTableAction;
	private Action supportOnTheWebAction;
	
	private Action tableQueryAction = new AbstractAction("Table Explorer") {
		public void actionPerformed(ActionEvent e) {
			TableQueryFrame f = new TableQueryFrame(MatchMakerSwingSession.this);
			f.setIconImage(new ImageIcon(getClass().getResource("/icons/matchmaker_24.png")).getImage());
			f.pack();
			f.setVisible(true);
		}
	};


	private Action databaseConnectionAction = new AbstractAction("Database Connection...") {

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
    private EditorPane oldPane;

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
    private EditorPane splashScreen;

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
	private Map<MergeEngineImpl, MergeEnginePanel> mergeEnginPanels;
	
	 /**
     * A map that links an engine to a panel. This is used so that only
     * one of each engine and panel ever exist per project.
     */
	private Map<MatchEngineImpl, MatchEnginePanel> matchEnginPanels;
	
	 /**
     * A map that links an engine to a panel. This is used so that only
     * one of each engine and panel ever exist per project.
     */
	private Map<CleanseEngineImpl, CleanseEnginePanel> cleanseEnginPanels;

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
	 * @throws PLSchemaException
	 * @throws SchemaVersionFormatException
     */
	public MatchMakerSwingSession(SwingSessionContext context, MatchMakerSession sessionImpl) throws IOException, SchemaVersionFormatException, PLSchemaException, SQLException {
        this.sessionImpl = sessionImpl;
        this.sessionContext = context;
        this.smallMMIcon = new ImageIcon(getClass().getResource("/icons/matchmaker_24.png"));
        
        matchEnginPanels = new HashMap<MatchEngineImpl, MatchEnginePanel>();
        mergeEnginPanels = new HashMap<MergeEngineImpl, MergeEnginePanel>();
        cleanseEnginPanels = new HashMap<CleanseEngineImpl, CleanseEnginePanel>();

        // this grabs warnings from the business model and DAO's and lets us handle them.
        sessionImpl.addWarningListener(new WarningListener() {
			public void handleWarning(String message) {
				MatchMakerSwingSession.this.handleWarning(message);
			}
		});

        frame = new JFrame("Power*MatchMaker: "+sessionImpl.getDBUser()+"@"+sessionImpl.getDatabase().getName());

        warningDialog = new JFrame("Power*MatchMaker Warnings");
        warningDialog.setIconImage(smallMMIcon.getImage());
        warningTextArea = new JTextArea(6, 40);
        JComponent cp = (JComponent) warningDialog.getContentPane();
        cp.setLayout(new BorderLayout(0, 10));
        cp.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        cp.add(new JScrollPane(warningTextArea), BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(new JButton(clearWarningsAction));
        buttonPanel.add(new JButton(closeWarningDialogAction));
        cp.add(buttonPanel, BorderLayout.SOUTH);
	}

	void showGUI() {
	    buildGUI();
        frame.setVisible(true);
		splitPane.setDividerLocation(0.2);
    }

    private void buildGUI() {

        macOSXRegistration();
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(frame));
        frame.setIconImage(smallMMIcon.getImage());
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

		newDeDupeAction = new NewProjectAction(this, "New De-duping Project", Project.ProjectMode.FIND_DUPES);
		newXrefAction = new NewProjectAction(this, "New X-refing Project", Project.ProjectMode.BUILD_XREF);
		newCleanseAction = new NewProjectAction(this, "New Cleansing Project", Project.ProjectMode.CLEANSE);
		
        JMenuBar menuBar = new JMenuBar();

		//Settingup
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic('f');
		fileMenu.add(exitAction);
		menuBar.add(fileMenu);
		
		JMenu editMenu = new JMenu("Edit");
		editMenu.setMnemonic('e');
		editMenu.add(undoAction);
		editMenu.add(redoAction);
		menuBar.add(editMenu);

		// the connections menu is set up when a new project is created (because it depends on the current DBTree)
		JMenu databaseMenu = new JMenu("Database");
		databaseMenu.setMnemonic('d');
		databaseMenu.add(loginAction);
		databaseMenu.add(logoutAction);
		databaseMenu.addSeparator();
		databaseMenu.add(databaseConnectionAction );
		menuBar.add(databaseMenu);
		
		JMenu projectMenu = new JMenu("Project");
		projectMenu.setMnemonic('m');
		projectMenu.add(newDeDupeAction);
		projectMenu.add(newCleanseAction);
		projectMenu.add(newXrefAction);
		projectMenu.addSeparator();
		projectMenu.add(editProjectAction);
		projectMenu.add(deleteProjectAction);
		projectMenu.addSeparator();
		projectMenu.add(runMatchAction);
		projectMenu.add(runMergeAction);
		projectMenu.add(runCleanseAction);
		projectMenu.addSeparator();
		projectMenu.add(showMatchStatisticInfoAction);
		projectMenu.addSeparator();
		
		// TODO: Use the commented code once the import and export
		// functions have been implemented. 
//		projectMenu.add(new ProjectImportAction(this, frame));
//		projectMenu.add(new ProjectExportAction(this, frame));
		projectMenu.add(new DummyAction(frame, "Import"));
		projectMenu.add(new DummyAction(frame, "Export"));
		
		menuBar.add(projectMenu);

		JMenu toolsMenu = new JMenu("Tools");
		toolsMenu.setMnemonic('t');
		toolsMenu.add(tableQueryAction);
		toolsMenu.add(new EditTranslateAction(this));
		// We will add this back in if we need the SQLRunner later
        //toolsMenu.add(new SQLRunnerAction(frame));
		menuBar.add(toolsMenu);

		// Commented the 'Window' menu until we actually have something to put in it
//        JMenu windowMenu = new JMenu("Window");
//        windowMenu.setMnemonic('w');
//        menuBar.add(windowMenu);
        
        helpAction = new HelpAction(frame);

		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic('h');
        if (!MAC_OS_X) {
            helpMenu.add(aboutAction);
            helpMenu.addSeparator();
        }
        helpMenu.add(helpAction);
		
        buildExampleTableAction = new BuildExampleTableAction(this);
        helpMenu.add(buildExampleTableAction);

        supportOnTheWebAction = SPSUtils.forumAction;
        helpMenu.add(supportOnTheWebAction);
        
        menuBar.add(helpMenu);
		
		frame.setJMenuBar(menuBar);

		JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);

		toolBar.add(loginAction);
		toolBar.addSeparator();
		toolBar.addSeparator();
		toolBar.add(newDeDupeAction);
		toolBar.add(newCleanseAction);
		toolBar.add(newXrefAction);
        toolBar.addSeparator();
        toolBar.add(runMatchAction);
        toolBar.add(runMergeAction);
        toolBar.add(runCleanseAction);
        toolBar.addSeparator();
        toolBar.addSeparator();
        toolBar.add(helpAction);
        toolBar.add(exitAction);
		toolBar.setToolTipText("MatchMaker Toolbar");
		toolBar.setName("MatchMaker Toolbar");

		Container projectBarPane = frame.getContentPane();
		projectBarPane.setLayout(new BorderLayout());
		projectBarPane.add(toolBar, BorderLayout.NORTH);

		JPanel cp = new JPanel(new BorderLayout());
		projectBarPane.add(cp, BorderLayout.CENTER);
		tree = new JTree(new MatchMakerTreeModel(getCurrentFolderParent(),getBackupFolderParent(),getTranslateGroupParent(), this));
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		MatchMakerTreeMouseAndSelectionListener matchMakerTreeMouseAndSelectionListener = new MatchMakerTreeMouseAndSelectionListener(this);
		tree.addMouseListener(matchMakerTreeMouseAndSelectionListener);
		tree.addTreeSelectionListener(matchMakerTreeMouseAndSelectionListener);
		tree.setCellRenderer(new MatchMakerTreeCellRenderer());
		tree.setRootVisible(false);
        tree.setShowsRootHandles(true);

        JScrollPane treePane = new JScrollPane(tree);
        treePane.setMinimumSize(new Dimension(5,5));
        treePane.setPreferredSize(new Dimension(1,1));
		splitPane.setLeftComponent(treePane);
		setCurrentEditorComponent(null);
		cp.add(splitPane);

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

    public MatchMakerSessionContext getContext() {
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
			exit();
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
	 * Calling this method checks if there are any remaining SPSwingWorker threads
	 * registered with this session. If there are, then it warns the user to wait 
	 * for the threads to close first before exiting again. Otherwise, it quits 
	 * the application and terminates the JVM.
	 */
	public void exit() {
		if (close()){
			saveSettings();
			System.exit(0);
		}
	}

    /**
	 * Shows the given component in the main part of the frame's UI.
	 *
	 * @param editor
	 *            The editor component to display in the UI. If you pass in
	 *            null, then no editor will be showing.
     * @throws SQLException
	 */
	public void setCurrentEditorComponent(EditorPane pane) {
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

            if (oldPane != null && oldPane.hasUnsavedChanges()) {
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
                    if (oldPane != null) {
                        doit = oldPane.doSave();
                        if (!doit){
                            tree.setSelectionPath(lastTreePath);
                        }
                    }
                } else if (doit) {
                	if (oldPane != null) {
                        doit = oldPane.discardChanges();
                        if (!doit){
                        	logger.debug("Cannot Discard Changes");
                            //tree.setSelectionPath(lastTreePath);
                        }
                        doit = true;
                    }
                }
            }
            if (doit) {
            	// clears the undo stack and the listeners to the match
            	// maker object
            	if (oldPane instanceof AbstractUndoableEditorPane) {
            		((AbstractUndoableEditorPane) oldPane).cleanup();
            	}
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

		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		    	try {
		    		SwingSessionContext context = new SwingSessionContextImpl(PreferencesManager.getRootNode());
                    context.showLoginDialog(null);
		    	} catch (Exception ex) {
		    		JDialog d = SPSUtils.showExceptionDialogNoReport(null, "Couldn't start application!", ex);
		    		d.addWindowListener(new WindowAdapter() {
		    			public void windowClosing(WindowEvent e) {
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
		    dao.delete(mmo);
        } else {
            throw new IllegalStateException("I don't know how to delete a parentless object");
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
    
    public SQLTable findPhysicalTableByName(String catalog, String schema, String tableName) throws ArchitectException {
    	return sessionImpl.findPhysicalTableByName(catalog, schema, tableName);
	}

    public SQLTable findPhysicalTableByName(String spDataSourceName, String catalog, String schema, String tableName) throws ArchitectException {
    	return sessionImpl.findPhysicalTableByName(spDataSourceName, catalog, schema, tableName);
	}
    
    public boolean tableExists(String catalog, String schema,
    		String tableName) throws ArchitectException {
    	return sessionImpl.tableExists(catalog, schema, tableName);
	}
    
    public boolean tableExists(String spDataSourceName, String catalog, String schema,
    		String tableName) throws ArchitectException {
    	return sessionImpl.tableExists(spDataSourceName, catalog, schema, tableName);
	}

     public boolean tableExists(SQLTable table) throws ArchitectException {
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
	 * Call this method to close the database connection and cancel
	 * running SPSwingWorker threads. If there are any remaining SPSwingWorker
	 * threads, the GUI will warn the user that there are threads still
	 * waiting to cancel, and to try closing again after the threads are finished.
	 * @return Returns false if there are SPSwingWorker threads remaining.
	 * 	In this case, the application should not close yet.
	 * 	Otherwise, returns true.
	 */
	public boolean close() {
        // If we still have ArchitectSwingWorker threads running, 
        // tell them to cancel, and then ask the user to try again later.
        // Note that it is not safe to force threads to stop, so we will
        // have to wait until the threads stop themselves.
		if (swingWorkers.size() > 0) {
            for (SPSwingWorker currentWorker : swingWorkers) {
                currentWorker.setCancelled(true);
            }
            
            Object[] options = {"Wait", "Force Quit"};
    		int n = JOptionPane.showOptionDialog(frame, 
    				"There are still unfinished tasks running in the MatchMaker.\n" +
    				"You can either wait for them to finish and try closing again later" +
    				", or force the application to close. Quitting will leave these tasks unfinished.", 
    				"Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, 
    				null, options, options[0]);
            if (n == 0) {
            	return false;
            }
        }
		return true;
	}
	
	/**
	 * @return Returns the original editor pane.
	 */
	public EditorPane getOldPane() {
		return oldPane;
	}
	
	/**
	 * Returns or creates the editor panel linked to the given engine
	 * 
	 * @param mei The current engine
	 * @param project The current project
	 */
	public MergeEnginePanel getMergeEnginePanel(MergeEngineImpl mei, Project project) {
		MergeEnginePanel ep = mergeEnginPanels.get(mei);
		if (mergeEnginPanels.get(mei) == null) {
			ep = new MergeEnginePanel(this,project, getFrame());
			mergeEnginPanels.put(mei,ep); 
			ep.setEngineEnabled(enginesEnabled);
		}
		return ep;
	}
	
	/**
	 * Returns or creates the editor panel linked to the given engine
	 * 
	 * @param mei The current engine
	 * @param project The current project
	 */
	public MatchEnginePanel getMatchEnginePanel(MatchEngineImpl mei, Project project) {
		MatchEnginePanel ep = matchEnginPanels.get(mei);
		if (ep == null) {
			ep = new MatchEnginePanel(this,project, getFrame());
			matchEnginPanels.put(mei,ep); 
			ep.setEngineEnabled(enginesEnabled);
		}
		return ep;
	}
	
	/**
	 * Returns or creates the editor panel linked to the given engine
	 * 
	 * @param mei The current engine
	 * @param project The current project
	 */
	public CleanseEnginePanel getCleanseEnginePanel(CleanseEngineImpl mei, Project project) {
		CleanseEnginePanel ep = cleanseEnginPanels.get(mei);
		if (ep == null) {
			ep = new CleanseEnginePanel(this,project, getFrame());
			cleanseEnginPanels.put(mei,ep); 
			ep.setEngineEnabled(enginesEnabled);
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
	 *	Enables/Disables all of this session's engine panels' 
	 *	run engine actions. Note that the actions will only
	 *	be enabled if the form status in the panel is not fail.
	 */
	public void setAllEnginesEnabled(boolean enabled){
		enginesEnabled  = enabled;
		for (MatchEnginePanel ep: matchEnginPanels.values()) {
			ep.setEngineEnabled(enabled);
		}
		for (MergeEnginePanel ep : mergeEnginPanels.values()){
			ep.setEngineEnabled(enabled);
		}
		for (CleanseEnginePanel ep : cleanseEnginPanels.values()) {
			ep.setEngineEnabled(enabled);
		}
 	}

	/**
	 * Returns whether the run engine actions have been 
	 * enabled/disabled in this session.
	 */
	public boolean isEnginesEnabled() {
		return enginesEnabled;
	}

	public SQLDatabase getDatabase(SPDataSource dataSource) {
		return sessionImpl.getDatabase(dataSource);
	}
}