package ca.sqlpower.matchmaker.swingui;


import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Connection;
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
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import sun.security.jca.GetInstance;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.ConfigFile;
import ca.sqlpower.architect.CoreUserSettings;
import ca.sqlpower.architect.UserSettings;
import ca.sqlpower.architect.qfa.ExceptionHandler;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.SwingUserSettings;

import ca.sqlpower.architect.swingui.action.AboutAction;
import ca.sqlpower.architect.swingui.action.HelpAction;
import ca.sqlpower.architect.swingui.action.SQLRunnerAction;

import com.darwinsys.util.PrefsUtils;

/**
 * The Main Window for the Architect Application; contains a main() method that is
 * the conventional way to start the application running.
 */
public class MatchMakerFrame extends JFrame {

	private static Logger logger = Logger.getLogger(MatchMakerFrame.class);

	/**
	 * The MatchMakerFrame is a singleton; this is the main instance.
	 */
	protected static MatchMakerFrame mainInstance;

    public static final boolean MAC_OS_X = (System.getProperty("os.name").toLowerCase().startsWith("mac os x"));

	protected Preferences prefs;
	protected ArchitectSession architectSession = null;
	protected ConfigFile configFile = null;
	protected UserSettings sprefs = null;
	protected JToolBar toolBar = null;
	protected JMenuBar menuBar = null;
	
	protected JTree tree = null;

    private JMenu databaseMenu;

	protected AboutAction aboutAction;
 	protected  JComponent contentPane;

	protected Action exitAction = new AbstractAction("Exit") {
	    public void actionPerformed(ActionEvent e) {
	        exit();
	    }
	};

	protected Action loginAction = new AbstractAction("Login") {
		public void actionPerformed(ActionEvent e) {
			LoginFrame l = new LoginFrame();
			l.pack();
	    	l.setVisible(true);
		}
	};

	protected Action logoutAction = new AbstractAction("Logout") {

		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub

		}

	};

	protected Action newMatchAction = new AbstractAction("New") {

		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub

		}

	};

	protected Action runMatchAction = new AbstractAction("Run Match") {

		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub

		}

	};

	protected Action runMergeAction = new AbstractAction("Run Merge") {

		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub

		}

	};

	protected Action dbBrowseAction = new AbstractAction("Database Browser") {

		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub

		}

	};

	protected Action configAction = new AbstractAction("Config") {

		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub

		}

	};

	protected Action helpAction = new AbstractAction("Help") {

		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub

		}

	};

	protected Action databasePreferenceAction = new AbstractAction("Database Preference") {

		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub

		}

	};

	private MatchMakerFrameWindowListener windowListener;

	private Action databaseConnectionAction = new AbstractAction("Database Connection...") {

		public void actionPerformed(ActionEvent e) {
			DatabaseConnectionManager dm = new DatabaseConnectionManager(
					MatchMakerFrame.getMainInstance().getUserSettings().getPlDotIni());
			dm.pack();
			dm.setVisible(true);

		}};

	/**
	 * You can't create an architect frame using this constructor.  You have to
	 * call {@link #getMainInstance()}.
	 *
	 * @throws ArchitectException
	 */
	private MatchMakerFrame() throws ArchitectException {
		synchronized (MatchMakerFrame.class) {
			mainInstance = this;
		}
        setIconImage(new ImageIcon(getClass().getResource("/icons/Architect16.png")).getImage());
	    // close handled by window listener
	    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	    architectSession = ArchitectSession.getInstance();
	    init();
	}

	private void init() throws ArchitectException {
		int accelMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

		prefs = PrefsUtils.getUserPrefsNode(architectSession);
	    CoreUserSettings us;
	    // must be done right away, because a static
	    // initializer in this class effects BeanUtils
	    // behaviour which the XML Digester relies
	    // upon heavily
	    //TypeMap.getInstance();
	    contentPane = (JComponent)getContentPane();

		try {
			ConfigFile cf = ConfigFile.getDefaultInstance();
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

		// Create actions
		aboutAction = new AboutAction();
        Action helpAction = new HelpAction();

		menuBar = new JMenuBar();

		//Settingup
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic('f');
		fileMenu.add(databasePreferenceAction);
		fileMenu.add(exitAction);

		menuBar.add(fileMenu);

		JMenu explorerMenu = new JMenu("Explorers");
		explorerMenu.setMnemonic('x');
		explorerMenu.add("Match Maker");
		explorerMenu.add("Adminstration");
		menuBar.add(explorerMenu);

		// the connections menu is set up when a new project is created (because it depends on the current DBTree)
		databaseMenu = new JMenu("Database");
		databaseMenu.setMnemonic('D');
		databaseMenu.add(logoutAction);
		databaseMenu.addSeparator();
		databaseMenu.add(databaseConnectionAction );
		menuBar.add(databaseMenu);

		JMenu matchesMenu = new JMenu("Matches");
		matchesMenu.setMnemonic('M');
		matchesMenu.add(newMatchAction);
		matchesMenu.add("Edit");
		matchesMenu.add("Delete");
		matchesMenu.add(".....");
		menuBar.add(matchesMenu);

		JMenu mergeMenu = new JMenu("Merges");
		mergeMenu.add(newMatchAction);
		mergeMenu.add("Edit");
		mergeMenu.add("Delete");
		mergeMenu.add(".....");
		menuBar.add(mergeMenu);

		JMenu folderMenu = new JMenu("Folders");
		folderMenu.setMnemonic('F');
		folderMenu.add(newMatchAction);
		folderMenu.add("Edit");
		folderMenu.add("Delete");
		folderMenu.add(".....");
		menuBar.add(folderMenu);

		JMenu toolsMenu = new JMenu("Tools");
		toolsMenu.setMnemonic('t');
		toolsMenu.add(aboutAction);
        toolsMenu.add(new SQLRunnerAction());
		menuBar.add(toolsMenu);

        JMenu windowMenu = new JMenu("Window");
        windowMenu.setMnemonic('w');
        windowMenu.add(aboutAction);
        windowMenu.add(aboutAction);
        menuBar.add(windowMenu);

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
		cp.add(tree);

		Rectangle bounds = new Rectangle();
		bounds.x = prefs.getInt(SwingUserSettings.MAIN_FRAME_X, 40);
		bounds.y = prefs.getInt(SwingUserSettings.MAIN_FRAME_Y, 40);
		bounds.width = prefs.getInt(SwingUserSettings.MAIN_FRAME_WIDTH, 600);
		bounds.height = prefs.getInt(SwingUserSettings.MAIN_FRAME_HEIGHT, 440);
		setBounds(bounds);
		addWindowListener(windowListener = new MatchMakerFrameWindowListener());
	}
	
	public void newLogin(Connection con){
		tree.setModel(new MatchMakerTreeModel(con));
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

}
