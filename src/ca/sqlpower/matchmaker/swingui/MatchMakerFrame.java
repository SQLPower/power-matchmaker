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
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

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
import ca.sqlpower.architect.swingui.action.PreferencesAction;
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
	protected JToolBar projectBar = null;
	protected JToolBar ppBar = null;
	protected JMenuBar menuBar = null;

    private JMenu connectionsMenu;

	protected AboutAction aboutAction;
 	protected  JComponent contentPane;

	protected Action exitAction = new AbstractAction("Exit") {
	    public void actionPerformed(ActionEvent e) {
	        exit();
	    }
	};

	/**
	 * Updates the swing settings and then writes all settings to the
	 * config file whenever actionPerformed is invoked.
	 */
	protected Action saveSettingsAction = new AbstractAction("Save User Preferences") {
	    public void actionPerformed(ActionEvent e) {
	        try {
	            saveSettings();
	        } catch (ArchitectException ex) {
	            logger.error("Couldn't save settings", ex);
	        }
	    }
	};


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
		fileMenu.add(aboutAction);
		fileMenu.add(aboutAction);
		fileMenu.add(aboutAction);
		fileMenu.add(aboutAction);
		fileMenu.addSeparator();
		fileMenu.add(aboutAction);
		fileMenu.add(aboutAction);
		fileMenu.add(aboutAction);
		fileMenu.addSeparator();

		fileMenu.add(exitAction);
		fileMenu.add(exitAction);

		menuBar.add(fileMenu);

		JMenu editMenu = new JMenu("Edit");
		editMenu.setMnemonic('e');
		editMenu.add(aboutAction);
		editMenu.add(aboutAction);
		editMenu.addSeparator();
		editMenu.add(aboutAction);
		editMenu.addSeparator();
		editMenu.add(aboutAction);
		menuBar.add(editMenu);

		// the connections menu is set up when a new project is created (because it depends on the current DBTree)
		connectionsMenu = new JMenu("Connections");
		connectionsMenu.setMnemonic('c');
		menuBar.add(connectionsMenu);

		JMenu etlMenu = new JMenu("ETL");
		etlMenu.setMnemonic('l');
		JMenu etlSubmenuOne = new JMenu("Power*Loader");
		etlSubmenuOne.add(aboutAction);

		// Todo add in ability to run the engine from the architect
        /*
            Action runPL = new RunPLAction();
            runPL.putValue(Action.NAME,"Run Power*Loader");
		    etlSubmenuOne.add(runPL);
        */

		etlSubmenuOne.add(aboutAction);

		etlSubmenuOne.add(aboutAction);
		etlMenu.add(etlSubmenuOne);
        etlMenu.add(aboutAction);
        etlMenu.add(aboutAction);
		menuBar.add(etlMenu);

		JMenu toolsMenu = new JMenu("Tools");
		toolsMenu.setMnemonic('t');
		toolsMenu.add(aboutAction);
		toolsMenu.add(aboutAction);
        toolsMenu.add(new SQLRunnerAction());
		menuBar.add(toolsMenu);

        JMenu profileMenu = new JMenu("Profile");
        profileMenu.setMnemonic('p');
        profileMenu.add(aboutAction);
        profileMenu.add(aboutAction);
        menuBar.add(profileMenu);

		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic('h');
        if (!MAC_OS_X) {
            helpMenu.add(aboutAction);
            helpMenu.addSeparator();
        }
        helpMenu.add(helpAction);
		menuBar.add(helpMenu);

		setJMenuBar(menuBar);

		projectBar = new JToolBar(JToolBar.HORIZONTAL);
		ppBar = new JToolBar(JToolBar.VERTICAL);

		projectBar.add(aboutAction);
		projectBar.add(aboutAction);
        projectBar.addSeparator();
        projectBar.add(helpAction);
		projectBar.setToolTipText("Project Toolbar");
		projectBar.setName("Project Toolbar");




		Container projectBarPane = getContentPane();
		projectBarPane.setLayout(new BorderLayout());
		projectBarPane.add(projectBar, BorderLayout.NORTH);

		JPanel cp = new JPanel(new BorderLayout());
		cp.add(ppBar, BorderLayout.EAST);
		projectBarPane.add(cp, BorderLayout.CENTER);


		Rectangle bounds = new Rectangle();
		bounds.x = prefs.getInt(SwingUserSettings.MAIN_FRAME_X, 40);
		bounds.y = prefs.getInt(SwingUserSettings.MAIN_FRAME_Y, 40);
		bounds.width = prefs.getInt(SwingUserSettings.MAIN_FRAME_WIDTH, 600);
		bounds.height = prefs.getInt(SwingUserSettings.MAIN_FRAME_HEIGHT, 440);
		setBounds(bounds);
	}



	/**
	 * Points all the actions to the correct PlayPen and DBTree
	 * instances.  This method is called by setProject.
	 * @throws ArchitectException if the undo manager can't get the playpen's children on the listener list
	 */
	protected void setupActions() throws ArchitectException {

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
		return projectBar;
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
