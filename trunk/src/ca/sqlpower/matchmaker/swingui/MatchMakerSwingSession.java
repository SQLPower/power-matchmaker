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
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.matchmaker.ColumnMergeRules;
import ca.sqlpower.matchmaker.FolderParent;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MatchRule;
import ca.sqlpower.matchmaker.MatchRuleSet;
import ca.sqlpower.matchmaker.MatchMakerFolder;
import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchMakerSessionContext;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.TranslateGroupParent;
import ca.sqlpower.matchmaker.WarningListener;
import ca.sqlpower.matchmaker.dao.MatchRuleSetDAO;
import ca.sqlpower.matchmaker.dao.MatchDAO;
import ca.sqlpower.matchmaker.dao.MatchMakerDAO;
import ca.sqlpower.matchmaker.dao.PlFolderDAO;
import ca.sqlpower.matchmaker.prefs.PreferencesManager;
import ca.sqlpower.matchmaker.swingui.action.EditTranslateAction;
import ca.sqlpower.matchmaker.swingui.action.NewMatchAction;
import ca.sqlpower.matchmaker.swingui.action.PlMatchExportAction;
import ca.sqlpower.matchmaker.swingui.action.PlMatchImportAction;
import ca.sqlpower.matchmaker.swingui.action.ShowMatchStatisticInfoAction;
import ca.sqlpower.matchmaker.swingui.engine.MatchEnginePanel;
import ca.sqlpower.sql.PLSchemaException;
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
    
    private Action userPrefsAction = new AbstractAction("User Preferences...") {
		public void actionPerformed(ActionEvent e) {
			JOptionPane.showMessageDialog(MatchMakerSwingSession.this.frame,
					"The User Preferences action for OS Xis not yet implemented",
					"Apologies",
					JOptionPane.INFORMATION_MESSAGE);
		}
    };

	private Action aboutAction = new AbstractAction("About MatchMaker...") {

		public void actionPerformed(ActionEvent evt) {
			// This is one of the few JDIalogs that can not get replaced
			// with a call to ArchitectPanelBuilder, because an About
			// box must have only ONE button...
			final JDialog d = new JDialog(getFrame(),
										  "About MatchMaker");
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

	private Action newMatchAction = null;
	private Action editMatchAction = new EditMatchAction("Edit");

	private Action runMatchAction = new AbstractAction("Run Match") {

		public void actionPerformed(ActionEvent e) {
			Match match = MMSUtils.getTreeObject(getTree(),Match.class);
			if ( match == null )
				return;
		    MatchEnginePanel r = new MatchEnginePanel(MatchMakerSwingSession.this, match, frame);
		    setCurrentEditorComponent(r);
		}
	};

	private Action runMergeAction = new DummyAction(null, "Run Merge");

	private Action helpAction;

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

	private Action showMatchStatisticInfoAction = new AbstractAction("Statistics") {

		public void actionPerformed(ActionEvent e) {
			Match match = MMSUtils.getTreeObject(getTree(),Match.class);
			if ( match == null )
				return;

			ShowMatchStatisticInfoAction sm = new ShowMatchStatisticInfoAction(
					MatchMakerSwingSession.this,match,frame);
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

        // this grabs warnings from the business model and DAO's and lets us handle them.
        sessionImpl.addWarningListener(new WarningListener() {
			public void handleWarning(String message) {
				MatchMakerSwingSession.this.handleWarning(message);
			}
		});

        frame = new JFrame("MatchMaker: "+sessionImpl.getDBUser()+"@"+sessionImpl.getDatabase().getName());

        warningDialog = new JFrame("MatchMaker Warnings");
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
    }

    private void buildGUI() {

        macOSXRegistration();
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(frame));
        frame.setIconImage(smallMMIcon.getImage());
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

		newMatchAction = new NewMatchAction(this, "New Match");
        JMenuBar menuBar = new JMenuBar();

		//Settingup
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic('f');
		fileMenu.add(exitAction);

		menuBar.add(fileMenu);

		// the connections menu is set up when a new project is created (because it depends on the current DBTree)
		JMenu databaseMenu = new JMenu("Database");
		databaseMenu.setMnemonic('D');
		databaseMenu.add(loginAction);
		databaseMenu.add(logoutAction);
		databaseMenu.addSeparator();
		databaseMenu.add(databaseConnectionAction );
		menuBar.add(databaseMenu);

		JMenu matchesMenu = new JMenu("Matches");
		matchesMenu.setMnemonic('M');
		matchesMenu.add(newMatchAction);
		matchesMenu.add(editMatchAction);
		matchesMenu.add(new DummyAction(frame, "Delete"));
		matchesMenu.addSeparator();
		matchesMenu.add(runMatchAction);
		matchesMenu.add(showMatchStatisticInfoAction);
		matchesMenu.addSeparator();
        matchesMenu.add(new JMenuItem(new PlMatchImportAction(this, frame)));
		matchesMenu.add(new JMenuItem(new PlMatchExportAction(this, frame)));
		menuBar.add(matchesMenu);

		JMenu mergeMenu = new JMenu("Merges");
		mergeMenu.add(newMatchAction);
		menuBar.add(mergeMenu);

		JMenu folderMenu = new JMenu("Folders");
		folderMenu.setMnemonic('F');
		folderMenu.add(newMatchAction);
		menuBar.add(folderMenu);

		JMenu toolsMenu = new JMenu("Tools");
		toolsMenu.setMnemonic('t');
		toolsMenu.add(tableQueryAction);
		toolsMenu.add(new EditTranslateAction(this, frame));
		// We will add this back in if we need the SQLRunner later
        //toolsMenu.add(new SQLRunnerAction(frame));
		menuBar.add(toolsMenu);

        JMenu windowMenu = new JMenu("Window");
        windowMenu.setMnemonic('w');
        menuBar.add(windowMenu);

        helpAction = new HelpAction(frame);

		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic('h');
        if (!MAC_OS_X) {
            helpMenu.add(aboutAction);
            helpMenu.addSeparator();
        }
        helpMenu.add(helpAction);
		menuBar.add(helpMenu);
		frame.setJMenuBar(menuBar);

		JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);

		toolBar.add(loginAction);
		toolBar.add(newMatchAction);
        toolBar.addSeparator();
        toolBar.add(runMatchAction);
        toolBar.add(runMergeAction);
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
		tree = new JTree(new MatchMakerTreeModel(getCurrentFolderParent(),getBackupFolderParent(),this));
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		MatchMakerTreeMouseAndSelectionListener matchMakerTreeMouseAndSelectionListener = new MatchMakerTreeMouseAndSelectionListener(this);
		tree.addMouseListener(matchMakerTreeMouseAndSelectionListener);
		tree.addTreeSelectionListener(matchMakerTreeMouseAndSelectionListener);
		tree.setCellRenderer(new MatchMakerTreeCellRenderer());
		tree.setRootVisible(false);
        tree.setShowsRootHandles(true);

		splitPane.setLeftComponent(new JScrollPane(tree));
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

    /**
     * Returns the frame for this Swing session.
     */
    public JFrame getFrame() {
        return frame;
    }

    public MatchMakerSessionContext getContext() {
        return sessionContext;
    }

    private final class EditMatchAction extends AbstractAction {
		private EditMatchAction(String name) {
			super(name);
		}

		public void actionPerformed(ActionEvent e) {
			Match match = MMSUtils.getTreeObject(getTree(),Match.class);
			if (match == null) return;

			try {
				setCurrentEditorComponent(
					new MatchEditor(MatchMakerSwingSession.this,
						match,(PlFolder<Match>)match.getParent()));
			} catch (Exception ex) {
				MMSUtils.showExceptionDialog(frame, "Couldn't create match editor", ex);
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
                    // TODO refresh the objects in question from the database (to cancel user edits)
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
                }
            }
            if (doit) {
                //Remebers the treepath to the last node that it clicked on
                if (pane != null){
                    lastTreePath = tree.getSelectionPath();
                    splitPane.setRightComponent(pane.getPanel());
                    oldPane = pane;
                } else {
                    splitPane.setRightComponent(splashScreen.getPanel());
                    oldPane = splashScreen;
                }
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
                    Object[] args = { exitAction, userPrefsAction, aboutAction };
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

	public Match getMatchByName(String name) {
		return sessionImpl.getMatchByName(name);
	}

	public boolean isThisMatchNameAcceptable(String name) {
		return sessionImpl.isThisMatchNameAcceptable(name);
	}

    public String createNewUniqueName() {
        return sessionImpl.createNewUniqueName();
    }

	public long countMatchByName(String name) {
		return sessionImpl.countMatchByName(name);
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
				String.format("The %s action is not yet implemented", label),
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
		if (mmo instanceof Match){
			Match match = (Match)mmo;
			MatchDAO dao = (MatchDAO) getDAO(Match.class);
			dao.save(match);
		} else if (mmo instanceof MatchMakerFolder){
			Match match = (Match)mmo.getParent();
			MatchDAO dao = (MatchDAO) getDAO(Match.class);
			dao.save(match);
		} else if (mmo instanceof PlFolder){
			PlFolderDAO dao = (PlFolderDAO) getDAO(PlFolder.class);
			dao.save((PlFolder) mmo);
		} else if (mmo instanceof MatchRuleSet) {
			MatchRuleSet cg = (MatchRuleSet)mmo;
			MatchRuleSetDAO dao = (MatchRuleSetDAO) getDAO(MatchRuleSet.class);
			dao.save(cg);
		} else {
			throw new UnsupportedOperationException("We do not yet support "+mmo.getClass() + " persistance");
		}
	}

	/**
	 * Delete the MatchMakerObject passed in.  This will save the parent of the
	 * mmo.
	 *
	 * XXX Push this into the match maker session interface
	 * @param mmo
	 */
	@SuppressWarnings("unchecked")
	public <T extends MatchMakerObject> void delete(MatchMakerObject<T, ?> mmo) {
		if (mmo.getParent() != null) {
			
			mmo.getParent().removeChild(mmo);
            if (mmo instanceof MatchRule) {
                // XXX Criteria are special because they don't have a DAO of their own
                MatchMakerObject criteriaGroup = mmo.getParent();
                MatchMakerDAO dao = getDAO(criteriaGroup.getClass());
                dao.save(criteriaGroup);
            } else if ( mmo instanceof ColumnMergeRules) {
            	// XXX so are column merge rules
            	MatchMakerObject tableMergeRules = mmo.getParent();
                MatchMakerDAO dao = getDAO(tableMergeRules.getClass());
                dao.save(tableMergeRules);
            } else {
                MatchMakerDAO dao = getDAO(mmo.getClass());
                dao.delete(mmo);
            }


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

    public SQLTable findPhysicalTableByName(String catalog, String schema, String tableName) throws ArchitectException {
    	return sessionImpl.findPhysicalTableByName(catalog, schema, tableName);
	}

    public boolean tableExists(String catalog, String schema,
    		String tableName) throws ArchitectException {
    	return sessionImpl.tableExists(catalog, schema, tableName);
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
            
            JOptionPane.showMessageDialog(frame,
                    "There are still unfinished tasks running in the MatchMaker.\n" +
                    "Please wait for them to finish, and then try closing again.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return false;
        }
		return true;
	}

}