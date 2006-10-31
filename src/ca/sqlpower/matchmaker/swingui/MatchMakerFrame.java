package ca.sqlpower.matchmaker.swingui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
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
import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.ConfigFile;
import ca.sqlpower.architect.CoreUserSettings;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.UserSettings;
import ca.sqlpower.architect.qfa.ExceptionHandler;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.SwingUserSettings;
import ca.sqlpower.architect.swingui.action.AboutAction;
import ca.sqlpower.architect.swingui.action.SQLRunnerAction;
import ca.sqlpower.matchmaker.hibernate.PlFolder;
import ca.sqlpower.matchmaker.hibernate.PlMatch;
import ca.sqlpower.matchmaker.hibernate.PlMatchTranslateGroup;
import ca.sqlpower.matchmaker.hibernate.home.PlFolderHome;
import ca.sqlpower.matchmaker.hibernate.home.PlMatchHome;
import ca.sqlpower.matchmaker.hibernate.home.PlMatchTranslateGroupHome;
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

import com.darwinsys.util.PrefsUtils;

/**
 * The Main Window for the Architect Application; contains a main() method that is
 * the conventional way to start the application running.
 */
public class MatchMakerFrame extends JFrame {

	private static Logger logger = Logger.getLogger(MatchMakerFrame.class);

    /**
     * The minimum PL schema version that the MatchMaker works with.
     */
    private static final Version MIN_PL_SCHEMA_VERSION = new Version(5,0,26);

	/**
	 * The MatchMakerFrame is a singleton; this is the main instance.
	 */
	protected static MatchMakerFrame mainInstance;

    public static final boolean MAC_OS_X = (System.getProperty("os.name").toLowerCase().startsWith("mac os x"));

	protected final Preferences prefs;
	protected ArchitectSession architectSession = null;
	protected ConfigFile configFile = null;
	protected UserSettings sprefs = null;
	protected JToolBar toolBar = null;
	protected JMenuBar menuBar = null;
	protected JTree tree = null;
    private JMenu databaseMenu;
    private JSplitPane splitPane;

	private String lastImportExportAccessPath = null;
	protected AboutAction aboutAction;
 	protected  JComponent contentPane;

    protected ArchitectDataSource dataSource;

	protected Action exitAction = new AbstractAction("Exit") {
	    public void actionPerformed(ActionEvent e) {
	        exit();
	    }
	};

	protected static Action loginAction = new AbstractAction("Login") {
		public void actionPerformed(ActionEvent e) {
			LoginDialog l = new LoginDialog();
			l.pack();
			l.setLocationRelativeTo(MatchMakerFrame.mainInstance);
	    	l.setVisible(true);
		}
	};

	protected Action logoutAction = new AbstractAction("Logout") {

		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			JOptionPane.showMessageDialog(mainInstance,
			"This action is not yet available. We apologize for the inconvenience");

		}

	};

	protected Action newMatchAction = null;
	protected Action editMatchAction = new EditMatchAction("Edit");

	protected Action runMatchAction = new AbstractAction("Run Match") {

		public void actionPerformed(ActionEvent e) {
			PlMatch match = ArchitectUtils.getTreeObject(getTree(),PlMatch.class);
			if ( match == null )
				return;
		    RunMatchDialog r = new RunMatchDialog(match, MatchMakerFrame.this);
			r.pack();
			r.setVisible(true);
		}

	};

	protected Action runMergeAction = new AbstractAction("Run Merge") {

		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			JOptionPane.showMessageDialog(mainInstance,
			"This action is not yet available. We apologize for the inconvenience");
		}

	};

	protected Action dbBrowseAction = new AbstractAction("Database Browser") {

		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			JOptionPane.showMessageDialog(mainInstance,
				"This action is not yet available. We apologize for the inconvenience");
		}

	};

	protected Action configAction = new AbstractAction("Config") {

		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			JOptionPane.showMessageDialog(mainInstance,
				"This action is not yet available. We apologize for the inconvenience");
		}
	};

	protected Action helpAction;

	protected Action databasePreferenceAction = new AbstractAction("Database Preference") {

		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			JOptionPane.showMessageDialog(mainInstance,
			"This action is not yet available. We apologize for the inconvenience");
		}
	};

	protected Action tableQueryAction = new AbstractAction("Table Explorer") {
		public void actionPerformed(ActionEvent e) {
			TableQueryFrame f = new TableQueryFrame();
			f.setIconImage(new ImageIcon(getClass().getResource("/icons/matchmaker_final.png")).getImage());
			f.pack();
			f.setVisible(true);
		}
	};


	private Action databaseConnectionAction = new AbstractAction("Database Connection...") {

		public void actionPerformed(ActionEvent e) {
			DatabaseConnectionManager dm = new DatabaseConnectionManager(
					MatchMakerFrame.getMainInstance(),
					MatchMakerFrame.getMainInstance().getUserSettings().getPlDotIni()); // XXX
			dm.pack();
			dm.setVisible(true);

		}};

	private Action showMatchStatisticInfoAction = new AbstractAction("Statistics") {

		public void actionPerformed(ActionEvent e) {
			PlMatch match = ArchitectUtils.getTreeObject(getTree(),PlMatch.class);
			if ( match == null )
				return;

			ShowMatchStatisticInfoAction sm = new ShowMatchStatisticInfoAction(match,
					MatchMakerFrame.getMainInstance());
			sm.actionPerformed(e);
		}};

	private List<PlMatch> matches;
	private List<PlFolder> folders;
	private List<PlMatchTranslateGroup> translations = new ArrayList<PlMatchTranslateGroup>();
	private SQLDatabase database;



	/**
	 * You can't create an architect frame using this constructor;
	 * you have to call {@link #getMainInstance()}.
	 *
	 * @throws ArchitectException
	 */
	private MatchMakerFrame() throws ArchitectException {
		synchronized (MatchMakerFrame.class) {
			mainInstance = this;
		}
		setIconImage(new ImageIcon(getClass().getResource("/icons/matchmaker_final.png")).getImage());
	    // close handled by window listener
	    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	    architectSession = ArchitectSession.getInstance();
	    prefs = PrefsUtils.getUserPrefsNode(architectSession);
	    init();
	}

	private void init() throws ArchitectException {

	    CoreUserSettings us;
	    // must be done right away, because a static
	    // initializer in this class effects BeanUtils
	    // behaviour which the XML Digester relies
	    // upon heavily
	    //TypeMap.getInstance();

	    setTitle("MatchMaker");

	    contentPane = (JComponent)getContentPane();

		try {
			ConfigFile cf = ConfigFile.getDefaultInstance();
			cf.setPrefs(prefs);
			us = cf.read(getArchitectSession());
			architectSession.setUserSettings(us);
			sprefs = architectSession.getUserSettings().getSwingSettings();
		} catch (IOException e) {
			throw new ArchitectException("prefs.read", e);
		}

		while (!us.isPlDotIniPathValid()) {
		    String message;
		    String[] options = new String[] {"Browse", "Create"};
		    if (us.getPlDotIniPath() == null) {
		        message = "location is not set";
		    } else if (new File(us.getPlDotIniPath()).isFile()) {
		        message = "file \n\n\""+us.getPlDotIniPath()+"\"\n\n could not be read";
		    } else {
		        message = "file \n\n\""+us.getPlDotIniPath()+"\"\n\n does not exist";
		    }
		    int choice = JOptionPane.showOptionDialog(null,   // blocking wait
		            "The Architect keeps its list of database connections" +
		            "\nin a file called PL.INI.  Your PL.INI "+message+"." +
		            "\n\nYou can browse for an existing PL.INI file on your system" +
		            "\nor allow the Architect to create a new one in your home directory." +
		            "\n\nHint: If you are a Power*Loader Suite user, you should browse for" +
		            "\nan existing PL.INI in your Power*Loader installation directory.",
		            "Missing PL.INI", 0, JOptionPane.INFORMATION_MESSAGE, null, options, null);
		    File newPlIniFile;
            if (choice == JOptionPane.CLOSED_OPTION) {
                throw new ArchitectException("Can't start without a pl.ini file");
            } else if (choice == 0) {
		        JFileChooser fc = new JFileChooser();
		        fc.setFileFilter(ASUtils.INI_FILE_FILTER);
		        fc.setDialogTitle("Locate your PL.INI file");
		        int fcChoice = fc.showOpenDialog(null);       // blocking wait
		        if (fcChoice == JFileChooser.APPROVE_OPTION) {
		            newPlIniFile = fc.getSelectedFile();
		        } else {
		            newPlIniFile = null;
		        }
		    } else if (choice == 1) {
		        newPlIniFile = new File(System.getProperty("user.home"), "pl.ini");
		    } else
                throw new ArchitectException("Unexpected return from JOptionPane.showOptionDialog to get pl.ini");

		    if (newPlIniFile != null) try {
		        newPlIniFile.createNewFile();
		        us.setPlDotIniPath(newPlIniFile.getPath());
		    } catch (IOException e1) {
		        logger.error("Caught IO exception while creating empty PL.INI at \""
		                +newPlIniFile.getPath()+"\"", e1);
		        JOptionPane.showMessageDialog(null, "Failed to create file \""+newPlIniFile.getPath()+"\":\n"+e1.getMessage(),
		                "Error", JOptionPane.ERROR_MESSAGE);
		    }
		}

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

		// Create actions
		Action aboutAction = new AbstractAction("About"){

			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(mainInstance,
					"MatchMaker 0.1", "About MatchMaker",
					JOptionPane.INFORMATION_MESSAGE);
			}};

		newMatchAction = new NewMatchAction("New Match",null,splitPane);
		menuBar = new JMenuBar();

		//Settingup
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic('f');
		fileMenu.add(databasePreferenceAction);
		fileMenu.add(exitAction);

		menuBar.add(fileMenu);

		JMenu explorerMenu = new JMenu("Explorers");
		explorerMenu.setMnemonic('x');
		explorerMenu.add(new DummyAction(mainInstance, "Match Maker"));
		explorerMenu.add(new DummyAction(mainInstance, "Adminstration"));
		menuBar.add(explorerMenu);

		// the connections menu is set up when a new project is created (because it depends on the current DBTree)
		databaseMenu = new JMenu("Database");
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
		matchesMenu.add(new DummyAction(mainInstance, "Delete"));
		matchesMenu.addSeparator();
		matchesMenu.add(runMatchAction);
		matchesMenu.add(showMatchStatisticInfoAction);
		matchesMenu.addSeparator();
		matchesMenu.add(new JMenuItem(new PlMatchImportAction()));
		matchesMenu.add(new JMenuItem(new PlMatchExportAction(null)));
		menuBar.add(matchesMenu);

		JMenu mergeMenu = new JMenu("Merges");
		mergeMenu.add(newMatchAction);
		mergeMenu.add(new DummyAction(mainInstance, "Edit"));
		mergeMenu.add(new DummyAction(mainInstance, "Delete"));
		mergeMenu.add(".....");
		menuBar.add(mergeMenu);

		JMenu folderMenu = new JMenu("Folders");
		folderMenu.setMnemonic('F');
		folderMenu.add(newMatchAction);
		folderMenu.add(new DummyAction(mainInstance, "Edit"));
		folderMenu.add(new DummyAction(mainInstance, "Delete"));
		folderMenu.add(".....");
		menuBar.add(folderMenu);

		JMenu toolsMenu = new JMenu("Tools");
		toolsMenu.setMnemonic('t');
		toolsMenu.add(tableQueryAction);
		toolsMenu.add(new EditTranslateAction(getMainInstance()));
        toolsMenu.add(new SQLRunnerAction(mainInstance));
		menuBar.add(toolsMenu);

        JMenu windowMenu = new JMenu("Window");
        windowMenu.setMnemonic('w');
        menuBar.add(windowMenu);

        helpAction = new HelpAction(mainInstance);

		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic('h');
        if (!MAC_OS_X) {
            helpMenu.add(aboutAction);
            helpMenu.addSeparator();
        }
        helpMenu.add(helpAction);
		menuBar.add(helpMenu);
		setJMenuBar(menuBar);

		toolBar = new JToolBar(JToolBar.HORIZONTAL);

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

		Container projectBarPane = getContentPane();
		projectBarPane.setLayout(new BorderLayout());
		projectBarPane.add(toolBar, BorderLayout.NORTH);

		JPanel cp = new JPanel(new BorderLayout());
		projectBarPane.add(cp, BorderLayout.CENTER);
		tree = new JTree(new MatchMakerTreeModel());
		tree.addMouseListener(new MatchMakerTreeMouseListener(splitPane));

		splitPane.setRightComponent(null );
		splitPane.setLeftComponent(new JScrollPane(tree));
		cp.add(splitPane);

		Rectangle bounds = new Rectangle();
		bounds.x = prefs.getInt(SwingUserSettings.MAIN_FRAME_X, 40);
		bounds.y = prefs.getInt(SwingUserSettings.MAIN_FRAME_Y, 40);
		bounds.width = prefs.getInt(SwingUserSettings.MAIN_FRAME_WIDTH, 600);
		bounds.height = prefs.getInt(SwingUserSettings.MAIN_FRAME_HEIGHT, 440);
		setBounds(bounds);
		addWindowListener(new MatchMakerFrameWindowListener());

		lastImportExportAccessPath = prefs.get(SwingUserSettings.LAST_IMPORT_EXPORT_PATH,null);
	}

    /**
     * Sets up this frame for use with a new PL Respsitory connection.
     * This method has to be called on the Swing EDT, because it interacts
     * with the Hibernate session as well as the GUI.
     *
     * <p>This method will check the PL Schema version in DEF_PARAM
     * to ensure it is a new enough version before attempting to log in.
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
                            MatchMakerFrame.this,
                            "Respository connection not valid",
                            fexp);
                }
            });
            return;
        }

		ArchitectDataSource dbcs = db.getDataSource();
		HibernateUtil.createRepositorySessionFactory(dbcs);


		if (HibernateUtil.getRepositorySessionFactory() != null){

			PlFolderHome folderHome = new PlFolderHome();
			PlMatchHome matchHome = new PlMatchHome();
			folders = new ArrayList<PlFolder>(folderHome.findMatchMakerFolders());
			// Need to make sure the orphaned matches have been added.  But that we don't add two of the same
			// object in the hierachy.
			Set<PlMatch> matchSet = new HashSet<PlMatch>(matchHome.findAll());
			for (PlFolder folder: folders){
				matchSet.addAll(folder.getMatches());
			}
			matches = new ArrayList<PlMatch>(matchSet);
			Collections.sort(matches);
			Collections.sort(folders);
			PlMatchTranslateGroupHome translateHome = new PlMatchTranslateGroupHome();
			translations = new ArrayList<PlMatchTranslateGroup>(translateHome.findAll());
			List<PlMatchTranslateGroup> nullList = new ArrayList<PlMatchTranslateGroup>();
			nullList.add(null);
			for (PlMatchTranslateGroup group: translations){

				group.getPlMatchTranslations().removeAll(nullList);
			}
		}

		tree.setModel(new MatchMakerTreeModel(folders,matches));
		setDatabase(db);
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

	private void setDatabase(SQLDatabase db) {
	    database = db;
    }

	static {
		// Call this in a static initialier to force the
		// lazy evaluation to happen non-lazily. :-)
		MatchMakerFrame.getMainInstance();
	}

    public static synchronized MatchMakerFrame getMainInstance() {
		if (mainInstance == null) {
			try {
				new MatchMakerFrame();
			} catch (ArchitectException e) {
				throw new RuntimeException("Couldn't create MatchMakerFrame instance!");
			}
		}
		return mainInstance;
	}

	/**
	 * Convenience method for getArchitectSession().getUserSettings().
	 */
	public CoreUserSettings getUserSettings() {
		return architectSession.getUserSettings();
	}

	public ArchitectSession getArchitectSession() {
		return architectSession;
	}

	private final class EditMatchAction extends AbstractAction {
		private EditMatchAction(String name) {
			super(name);
		}

		public void actionPerformed(ActionEvent e) {
			PlMatch match = ArchitectUtils.getTreeObject(getTree(),PlMatch.class);
			if ( match == null )
				return;

			MatchEditor me;
			try {
				me = new MatchEditor((PlMatch) match,splitPane);
			} catch (ArchitectException e1) {
				throw new ArchitectRuntimeException(e1);
			}

			splitPane.setRightComponent(me.getPanel());
		}
	}

	class MatchMakerFrameWindowListener extends WindowAdapter {
		@Override
		public void windowClosing(WindowEvent e) {
			exit();
		}
	}

	public void saveSettings() throws ArchitectException {
		if (configFile == null) configFile = ConfigFile.getDefaultInstance();

		prefs.putInt(SwingUserSettings.MAIN_FRAME_X, getLocation().x);
		prefs.putInt(SwingUserSettings.MAIN_FRAME_Y, getLocation().y);
		prefs.putInt(SwingUserSettings.MAIN_FRAME_WIDTH, getWidth());
		prefs.putInt(SwingUserSettings.MAIN_FRAME_HEIGHT, getHeight());
		if ( getDatabase() != null && getDatabase().getDataSource() != null ) {
			prefs.put(SwingUserSettings.LAST_LOGIN_DATA_SOURCE,
					getDatabase().getDataSource().getName());
		}
		if ( lastImportExportAccessPath != null ) {
			prefs.put(SwingUserSettings.LAST_IMPORT_EXPORT_PATH,
					lastImportExportAccessPath);
		}
		configFile.write(getArchitectSession());
		CoreUserSettings us = getUserSettings();
		try {
            us.getPlDotIni().write(new File(us.getPlDotIniPath()));
        } catch (IOException e) {
            logger.error("Couldn't save PL.INI file!", e);
        }
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
	 * Creates an MatchMakerFrame and sets it visible.  This method is
	 * an acceptable way to launch the Architect application.
	 */
	public static void main(String args[]) throws ArchitectException {

        ArchitectUtils.startup();
        System.setProperty("apple.laf.useScreenMenuBar", "true");
		ArchitectUtils.configureLog4j();

		getMainInstance();

		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {

		        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
		        // this doesn't appear to have any effect on the motion threshold
		        // in the Playpen, but it does seem to work on the DBTree...
		        logger.debug("current motion threshold is: " + System.getProperty("awt.dnd.drag.threshold"));
		        System.setProperty("awt.dnd.drag.threshold","10");
		        logger.debug("new motion threshold is: " + System.getProperty("awt.dnd.drag.threshold"));

		        getMainInstance().macOSXRegistration();
		        getMainInstance().setVisible(true);

		        loginAction.actionPerformed(null);
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

                Class[] defArgs = {MatchMakerFrame.class};
                Method registerMethod = osxAdapter.getDeclaredMethod("registerMacOSXApplication", defArgs);
                if (registerMethod != null) {
                    Object[] args = { this };
                    registerMethod.invoke(osxAdapter, args);
                }
                // This is slightly gross.  to reflectively access methods with boolean args,
                // use "boolean.class", then pass a Boolean object in as the arg, which apparently
                // gets converted for you by the reflection system.
                defArgs[0] = boolean.class;
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


	public JToolBar getProjectToolBar() {
		return toolBar;
	}
	public UserSettings getSwingUserSettings() {
		return sprefs;
	}

	public UserSettings getSprefs() {
		return sprefs;
	}

	public void setSprefs(UserSettings sprefs) {
		this.sprefs = sprefs;
	}

	public Preferences getPrefs() {
		return prefs;
	}

	public List<PlFolder> getFolders() {
		return folders;
	}

	public List<PlMatch> getMatches() {
		return matches;
	}

    public PlMatch getMatchByName(String name ) {
        for ( PlMatch m : matches ) {
            if ( m.getMatchId().equals(name) )
                return m;
        }
        return null;
    }

    public SQLDatabase getDatabase() {
        return database;
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

	public String getLastImportExportAccessPath() {
		return lastImportExportAccessPath;
	}

	public void setLastImportExportAccessPath(String lastExportAccessPath) {
		if (this.lastImportExportAccessPath != lastExportAccessPath) {
			this.lastImportExportAccessPath = lastExportAccessPath;
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

}