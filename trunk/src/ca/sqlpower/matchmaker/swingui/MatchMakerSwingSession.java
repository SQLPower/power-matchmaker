package ca.sqlpower.matchmaker.swingui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.ArchitectSessionImpl;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.action.SQLRunnerAction;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchMakerSessionContext;
import ca.sqlpower.matchmaker.MatchMakerVersion;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.hibernate.PlMatch;
import ca.sqlpower.matchmaker.hibernate.PlMatchTranslateGroup;
import ca.sqlpower.matchmaker.hibernate.home.PlMatchHome;
import ca.sqlpower.matchmaker.prefs.PreferencesManager;
import ca.sqlpower.matchmaker.swingui.action.EditTranslateAction;
import ca.sqlpower.matchmaker.swingui.action.NewMatchAction;
import ca.sqlpower.matchmaker.swingui.action.PlMatchExportAction;
import ca.sqlpower.matchmaker.swingui.action.PlMatchImportAction;
import ca.sqlpower.matchmaker.swingui.action.ShowMatchStatisticInfoAction;
import ca.sqlpower.matchmaker.util.HibernateUtil;
import ca.sqlpower.sql.PLSchemaException;
import ca.sqlpower.sql.SchemaVersion;
import ca.sqlpower.sql.SchemaVersionFormatException;
import ca.sqlpower.util.Version;

/**
 * The Main Window for the Architect Application; contains a main() method that is
 * the conventional way to start the application running.
 */
public class MatchMakerSwingSession implements MatchMakerSession {

	private static Logger logger = Logger.getLogger(MatchMakerSwingSession.class);

	/**
	 * The minimum PL schema version that the MatchMaker works with.
	 */
	private static final Version MIN_PL_SCHEMA_VERSION = new Version(5,0,26);

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
	 * The main part of the UI; the tree lives on the left and the current editor lives on the right.
     *
     * @see setCurrentEditComponent()
	 */
	private JSplitPane splitPane;

    /**
     * The tree that lets users browse the business objects.
     */
	private JTree tree;

	private Action aboutAction = new AbstractAction("About MatchMaker...") {
        public void actionPerformed(ActionEvent e) {
            String message =
                "<html>" +
                "<h1>Power*MatchMaker</h1>" +
                "<p>Version x.y</p>" +
                "<p>Copyright 2006 SQL Power Group Inc.</p>" +
                "</html>";
            ImageIcon icon = new ImageIcon(getClass().getResource("/icons/matchmaker_128.png"));
            JOptionPane.showOptionDialog(
                    frame, message, "About Power*MatchMaker",
                    JOptionPane.YES_OPTION, JOptionPane.INFORMATION_MESSAGE,
                    icon, new String[] { "Ok" }, "Ok");
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
			// TODO Auto-generated method stub
			JOptionPane.showMessageDialog(frame,
			"This action is not yet available. We apologize for the inconvenience");
		}

	};

	private Action newMatchAction = null;
	private Action editMatchAction = new EditMatchAction("Edit");

	private Action runMatchAction = new AbstractAction("Run Match") {

		public void actionPerformed(ActionEvent e) {
			Match match = ArchitectUtils.getTreeObject(getTree(),Match.class);
			if ( match == null )
				return;
		    RunMatchDialog r = new RunMatchDialog(MatchMakerSwingSession.this, match, frame);
			r.pack();
			r.setVisible(true);
		}

	};

	private Action runMergeAction = new AbstractAction("Run Merge") {

		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			JOptionPane.showMessageDialog(frame,
			"This action is not yet available. We apologize for the inconvenience");
		}

	};

	private Action dbBrowseAction = new AbstractAction("Database Browser") {

		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			JOptionPane.showMessageDialog(frame,
				"This action is not yet available. We apologize for the inconvenience");
		}

	};

	private Action configAction = new AbstractAction("Config") {

		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			JOptionPane.showMessageDialog(frame,
				"This action is not yet available. We apologize for the inconvenience");
		}
	};

	private Action helpAction;

	private Action databasePreferenceAction = new AbstractAction("Database Preference") {

		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			JOptionPane.showMessageDialog(frame,
			"This action is not yet available. We apologize for the inconvenience");
		}
	};

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
            sessionContext.showDatabaseConnectionManager();
		}
	};

	private Action showMatchStatisticInfoAction = new AbstractAction("Statistics") {

		public void actionPerformed(ActionEvent e) {
			Match match = ArchitectUtils.getTreeObject(getTree(),Match.class);
			if ( match == null )
				return;

			ShowMatchStatisticInfoAction sm = new ShowMatchStatisticInfoAction(match,
					frame);
			sm.actionPerformed(e);
		}
	};

	private List<PlMatch> matches;
	private List<PlFolder> folders;
	private List<PlMatchTranslateGroup> translations = new ArrayList<PlMatchTranslateGroup>();
	private SQLDatabase plRepositoryDatabase;


	/**
     * Creates a new MatchMaker session, complete with Swing GUI. Normally you
     * would use a LoginDialog instead of calling this constructor directly.
     *
     * @param context
     *            the Swing-specific session context
     * @param sessionImpl
     *            The session that actually does the dirty work (ORM and stuff
     *            like that).
     */
	public MatchMakerSwingSession(SwingSessionContext context, MatchMakerSession sessionImpl) throws IOException, ArchitectException {
        this.sessionImpl = sessionImpl;
        this.sessionContext = context;
        frame = new JFrame("MatchMaker: "+sessionImpl.getDBUser()+"@"+sessionImpl.getDatabase().getName());
	}

	void showGUI() {
	    buildGUI();
        frame.setVisible(true);
    }

    private void buildGUI() {

        macOSXRegistration();
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(frame));
        frame.setIconImage(new ImageIcon(getClass().getResource("/icons/matchmaker_24.png")).getImage());
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

		// Create actions
		Action aboutAction = new AbstractAction("About"){
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(frame,
						"<html>Power*MatchMaker "+
						MatchMakerVersion.APP_VERSION + "<br><br>" +
						"Copyright 2003-2006 SQL Power Group Inc.<br>" +
						"</html>",
					"About MatchMaker",
					JOptionPane.INFORMATION_MESSAGE);
			}};

		newMatchAction = new NewMatchAction(this, "New Match",null);
        JMenuBar menuBar = new JMenuBar();

		//Settingup
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic('f');
		fileMenu.add(databasePreferenceAction);
		fileMenu.add(exitAction);

		menuBar.add(fileMenu);

		JMenu explorerMenu = new JMenu("Explorers");
		explorerMenu.setMnemonic('x');
		explorerMenu.add(new DummyAction(frame, "Match Maker"));
		explorerMenu.add(new DummyAction(frame, "Adminstration"));
		menuBar.add(explorerMenu);

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
		mergeMenu.add(new DummyAction(frame, "Edit"));
		mergeMenu.add(new DummyAction(frame, "Delete"));
		mergeMenu.add(".....");
		menuBar.add(mergeMenu);

		JMenu folderMenu = new JMenu("Folders");
		folderMenu.setMnemonic('F');
		folderMenu.add(newMatchAction);
		folderMenu.add(new DummyAction(frame, "Edit"));
		folderMenu.add(new DummyAction(frame, "Delete"));
		folderMenu.add(".....");
		menuBar.add(folderMenu);

		JMenu toolsMenu = new JMenu("Tools");
		toolsMenu.setMnemonic('t');
		toolsMenu.add(tableQueryAction);
		toolsMenu.add(new EditTranslateAction(this, frame));
        toolsMenu.add(new SQLRunnerAction(frame));
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
        toolBar.add(databasePreferenceAction);
        toolBar.add(configAction);
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
		tree = new JTree(new MatchMakerTreeModel(new ArrayList<PlFolder>()));
		tree.addMouseListener(new MatchMakerTreeMouseListener(this));
		tree.setCellRenderer(new MatchMakerTreeCellRenderer());

		splitPane.setLeftComponent(new JScrollPane(tree));
        setCurrentEditorComponent(null);
		cp.add(splitPane);

		frame.setBounds(sessionContext.getFrameBounds());
		frame.addWindowListener(new MatchMakerFrameWindowListener());
	}

    /**
     * Sets up this frame for use with a new PL Respsitory connection.
     * This method has to be called on the Swing EDT, because it interacts
     * with the Hibernate session as well as the GUI.
     *
     * <p>This method will check the PL Schema version in DEF_PARAM
     * to ensure it is a new enough version before attempting to log in.
     *
     *FIXME: this method is in the middle of refactoring.
     *
     * @param db the database connection to the PL repository.
     */
	public void newLogin(final SQLDatabase db){

        try {
            checkSchema(db);
        } catch (Exception e) {
            final Exception fexp = e;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ASUtils.showExceptionDialog(
                    		frame,
                            "Respository connection not valid",
                            fexp);
                }
            });
            return;
        }

		ArchitectDataSource dbcs = db.getDataSource();
		HibernateUtil.createRepositorySessionFactory(dbcs);


		if (HibernateUtil.getRepositorySessionFactory() != null){
			PlMatchHome matchHome = new PlMatchHome();
			folders = new ArrayList<PlFolder>();

			// Need to make sure the orphaned matches have been added.  But that we don't add two of the same
			// object in the hierachy.  XXX: this is the wrong place for this operation
			Set<PlMatch> matchSet = new HashSet<PlMatch>(matchHome.findAllWithoutFolder());
			matches = new ArrayList<PlMatch>(matchSet);
			Collections.sort(matches);
		}

		tree.setModel(new MatchMakerTreeModel(folders));
		setPlRepositoryDatabase(db);
	}

    /**
     * Checks the PL Schema version in the given database.
     *
     * @param db the database to check
     * @throws PLSchemaException if the schema in db is older than
     * {@link #MIN_PL_SCHEMA_VERSION}.
     * @throws ArchitectException if it is not possible to get a JDBC connection for db.
     * @throws SQLException if there is a miscellaneous database error (including missing
     * DEF_PARAM table).
     * @throws SchemaVersionFormatException if the schema version in DEF_PARAM is not formatted
     * as a dotted triple.
     */
    private void checkSchema(SQLDatabase db) throws PLSchemaException, ArchitectException, SchemaVersionFormatException, SQLException {
        Version ver = null;
        Connection con = null;
        try {
            con = db.getConnection();
            ver = SchemaVersion.makeFromDatabase(con);
            logger.info(
                    "checkSchema(): connected to "+db.getName()+
                    " schema version: " + ver );
            if (ver.compareTo(MIN_PL_SCHEMA_VERSION) < 0) {
                throw new PLSchemaException(
                        "The MatchMaker requires PL Schema version "+
                        MIN_PL_SCHEMA_VERSION+" or newer.  This database " +
                        "is at version "+ver, ver.toString(),
                        MIN_PL_SCHEMA_VERSION.toString());
            }
        } finally {
            try {
                if (con != null) con.close();
            } catch (SQLException e) {
                logger.error("Couldn't close connection", e);
            }
        }
    }

	private void setPlRepositoryDatabase(SQLDatabase db) {
	    plRepositoryDatabase = db;
    }

    /**
     * Returns the frame for this Swing session.
     */
    JFrame getFrame() {
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
			Match match = ArchitectUtils.getTreeObject(getTree(),Match.class);
			if ( match == null )
				return;

			MatchEditor me;
			try {
				me = new MatchEditor(MatchMakerSwingSession.this, match);
			} catch (ArchitectException e1) {
				throw new ArchitectRuntimeException(e1);
			}

			setCurrentEditorComponent(me.getPanel());
		}
	}

	class MatchMakerFrameWindowListener extends WindowAdapter {
		@Override
		public void windowClosing(WindowEvent e) {
			exit();
		}

	}

    /**
     * This method should become unnecessary soon, since the app will just continually keep
     * the user settings up-to-date...
     *
     * @throws ArchitectException
     */
	public void saveSettings() throws ArchitectException {
		sessionContext.setFrameBounds(frame.getBounds());  // XXX we should do this in a component listener
	}

	/**
	 * Calling this method quits the application and terminates the
	 * JVM.
	 */
	public void exit() {
		try {
			saveSettings();
		} catch (ArchitectException e) {
			logger.error("Couldn't save settings: "+e);
		}
	    System.exit(0);
	}

    /**
     * Shows the given component in the main part of the frame's UI.
     *
     * @param editor The editor component to display in the UI.  If you pass
     * in null, then no editor will be showing.
     */
    public void setCurrentEditorComponent(JComponent editor) {
        splitPane.setRightComponent(editor);
    }

	/**
	 * Creates a MatchMakerSwingSession and shows the login prompt.  This method is
	 * an acceptable way to launch the Swing GUI of the MatchMaker application.
     *
     * XXX should move to LoginDialog or its own class, I think
	 */
	public static void main(String args[]) throws ArchitectException {

        ArchitectUtils.startup();
        System.setProperty("apple.laf.useScreenMenuBar", "true");
		ArchitectUtils.configureLog4j();

		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		    	try {
		    		SwingSessionContext context = new SwingSessionContext(ArchitectSessionImpl.getInstance(),
                                                            PreferencesManager.getRootNode());
                    context.showLoginDialog(null);
		    	} catch (Exception ex) {
		    		ASUtils.showExceptionDialog("Couldn't start application!", ex);
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
                    Object[] args = { exitAction, databasePreferenceAction, aboutAction };  // XXX databasePreferenceAction might not be appropriate here.
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
                System.err.println("This version of Mac OS X does not support the Apple EAWT.  Application Menu handling has been disabled (" + e + ")");
            } catch (ClassNotFoundException e) {
                // This shouldn't be reached; if there's a problem with the OSXAdapter we should get the
                // above NoClassDefFoundError first.
                System.err.println("This version of Mac OS X does not support the Apple EAWT.  Application Menu handling has been disabled (" + e + ")");
            } catch (Exception e) {
                System.err.println("Exception while loading the OSXAdapter:");
                e.printStackTrace();
            }
        }
    }


	public List<PlFolder> getFolders() {
		return folders;
	}

	public List<PlMatch> getMatches() {
		return matches;
	}

    /**
     * Finds a Match object in the current session by name.
     *
     * XXX (implementation problem) this is a MatchDAO thing, so we should delegate to that here.
     */
    public PlMatch getMatchByName(String name) {
        for (PlMatch m : matches) {
            if (m.getMatchId().equals(name))
                return m;
        }
        return null;
    }

    public SQLDatabase getPlRepositoryDatabase() {
        return plRepositoryDatabase;
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

	public List<PlMatchTranslateGroup> getTranslations() {
		return translations;
	}

	public void setTranslations(List<PlMatchTranslateGroup> translations) {
		if (this.translations != translations) {
			this.translations = translations;
			//TODO fire event
		}
	}

	public String getLastImportExportAccessPath() {
        return sessionContext.getLastImportExportAccessPath();
    }

    public void setLastImportExportAccessPath(String path) {
        sessionContext.setLastImportExportAccessPath(path);
    }

    ///// MatchMakerSession Implementation //////

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

}