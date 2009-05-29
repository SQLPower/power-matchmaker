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

import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerConfigurationException;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchMakerSessionContext;
import ca.sqlpower.matchmaker.dao.hibernate.MatchMakerHibernateSessionContext;
import ca.sqlpower.matchmaker.dao.hibernate.RepositoryUtil;
import ca.sqlpower.matchmaker.dao.hibernate.RepositoryVersionException;
import ca.sqlpower.matchmaker.munge.CleanseResultStep;
import ca.sqlpower.matchmaker.munge.DeDupeResultStep;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.swingui.munge.AbstractMungeComponent;
import ca.sqlpower.matchmaker.swingui.munge.CleanseResultMungeComponent;
import ca.sqlpower.matchmaker.swingui.munge.MungeResultMungeComponent;
import ca.sqlpower.matchmaker.swingui.munge.SQLInputMungeComponent;
import ca.sqlpower.matchmaker.swingui.munge.StepDescription;
import ca.sqlpower.security.PLSecurityException;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.db.DataSourceDialogFactory;
import ca.sqlpower.swingui.db.DataSourceTypeDialogFactory;
import ca.sqlpower.swingui.db.DatabaseConnectionManager;
import ca.sqlpower.swingui.db.DefaultDataSourceTypeDialogFactory;
import ca.sqlpower.swingui.event.SessionLifecycleListener;
import ca.sqlpower.util.BrowserUtil;
import ca.sqlpower.util.ExceptionReport;
import ca.sqlpower.validation.swingui.FormValidationHandler;


public class SwingSessionContextImpl implements MatchMakerSessionContext, SwingSessionContext {

	private static final Logger logger = Logger.getLogger(SwingSessionContextImpl.class);

	/**
	 * The array that looks like the set of types we are expecting for the correct constructor for any munge component
	 *  (excluding the input and output steps).
	 */
	private static final Type[] MUNGECOM_CONSTRUCTOR_PARAMS = {MungeStep.class, FormValidationHandler.class, MatchMakerSession.class};
	
	/**
	 * A link to the page for downloading the latest MatchMaker.
	 */
    private static final String DOWNLOAD_URL = "http://download.sqlpower.ca/matchmaker/current.html";

    /**
     * A link to the page for forum support.
     */
	private static final String FORUM_URL = "http://www.sqlpower.ca/forum/";

    /**
	 * The list of information about mungeSteps, which stores their StepClass, GUIClass, name and icon
	 */
	private final Map<Class, StepDescription> stepProperties = new HashMap<Class, StepDescription>();
    
    /**
     * The underlying context that will deal with Hibernate for us.
     */
    private final MatchMakerSessionContext context;
    
    /**
     * The prefs node that we use for persisting all the basic user settings that are
     * the same for all MatchMaker swing sessions.
     */
    private final Preferences swingPrefs;
    
    /**
     * Action that implements an extra button we put on the database connection manager
     * dialog. It hides that dialog, then shows the login dialog, where the connection
     * that was selected in the connection manager is made into the current selection.
     */
    private final Action loginDatabaseConnectionAction = new AbstractAction("Use as Repository") {

		public void actionPerformed(ActionEvent e) {
			SPDataSource dbcs = dbConnectionManager.getSelectedConnection();
			if (dbcs == null) {
				logger.debug("getSelectedConnection returned null");
				return;
			}
			dbConnectionManager.closeDialog();
            showLoginDialog(dbcs);
		}
	};

    /**
     * The database connection manager GUI for this session context (because all sessions
     * share the same set of database connections).
     */
    private final DatabaseConnectionManager dbConnectionManager;

    /**
     * This factory just passes the request through to the {@link MMSUtils#showDbcsDialog(Window, SPDataSource, Runnable)}
     * method.
     */
    private final DataSourceDialogFactory dsDialogFactory = new DataSourceDialogFactory() {

		public JDialog showDialog(Window parentWindow, JDBCDataSource dataSource,	Runnable onAccept) {
			return MMSUtils.showDbcsDialog(parentWindow, dataSource, onAccept);
		}
    	
    };
    
    /**
     * Implementation of DataSourceTypeDialogFactory that will display a DataSourceTypeEditor dialog
     */
    private final DataSourceTypeDialogFactory dsTypeDialogFactory = new DataSourceTypeDialogFactory() {

    	public Window showDialog(Window owner) {
    		DefaultDataSourceTypeDialogFactory d = new DefaultDataSourceTypeDialogFactory(context.getPlDotIni());
    		return d.showDialog(owner);
        }
    };
    
    /**
     * The login dialog for this app.  The session context will only create one login
     * dialog.
     */
    private final LoginDialog loginDialog;

    /**
     * Creates a new Swing session context, which is a holding place for all the basic
     * settings in the MatchMaker GUI application.  This constructor creates its own delegate
     * session context object based on information in the given prefs node, or failing that,
     * by prompting the user with a GUI.
     * @throws ClassNotFoundException 
     */
    public SwingSessionContextImpl(Preferences prefsRootNode) throws IOException, ClassNotFoundException {
        this(prefsRootNode, createDelegateContext(prefsRootNode));
    }

    /**
     * Creates a new Swing session context, which is a holding place for all the basic
     * settings in the MatchMaker GUI application.  This implementation uses the delegate
     * context given as an argument.  It is intended for facilitating proper unit tests, and
     * you will most likely prefer using the other constructor in real life.
     * @throws ClassNotFoundException 
     */
    public SwingSessionContextImpl(
            Preferences prefsRootNode,
            MatchMakerSessionContext delegateContext) throws IOException, ClassNotFoundException {
        this.swingPrefs = prefsRootNode;
        this.context = delegateContext;
        ExceptionReport.init();

        try {
            UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
        	logger.error("Unable to set native look and feel. Continuing with default.", ex);
        }
        // Set a login action property so that if there is no connection selected 
        // in the dbConnectionManager GUI, the corresponding button will be disabled.
        loginDatabaseConnectionAction.putValue(DatabaseConnectionManager.DISABLE_IF_NO_CONNECTION_SELECTED, Boolean.TRUE);
        
        dbConnectionManager = new DatabaseConnectionManager(getPlDotIni(), dsDialogFactory,dsTypeDialogFactory, Collections.singletonList(loginDatabaseConnectionAction));
        loginDialog = new LoginDialog(this);
        
        generatePropertiesList();
        
        // sets the icon so exception dialogs handled by SPSUtils instead
        // of MMSUtils can still have the correct icon
        SPSUtils.setMasterIcon(MMSUtils.getFrameImageIcon());
    }


    //////// MatchMakerSessionContext implementation //////////
    /* (non-Javadoc)
     * @see ca.sqlpower.matchmaker.swingui.SwingSessionContext#createSession(ca.sqlpower.sql.SPDataSource, java.lang.String, java.lang.String)
     */
    public MatchMakerSwingSession createSession(JDBCDataSource ds,
			String username, String password) throws PLSecurityException,
			SQLException, SQLObjectException, MatchMakerConfigurationException, RepositoryVersionException {
    	MatchMakerSwingSession session = new MatchMakerSwingSession(this, context.createSession(ds, username, password));
    	getSessions().add(session);
        session.addSessionLifecycleListener(getSessionLifecycleListener());
        return session;
    }

    public MatchMakerSession createDefaultSession() {
    	try {
    		MatchMakerSession session = new MatchMakerSwingSession(this, context.createDefaultSession());
    		getSessions().add(session);
    		session.addSessionLifecycleListener(getSessionLifecycleListener());
    		return session;
        } catch (Exception ex) {
            throw new RuntimeException(
                    "Couldn't create session. See nested exception for details.", ex);
        }
    }
    
    /* (non-Javadoc)
     * @see ca.sqlpower.matchmaker.swingui.SwingSessionContext#getDataSources()
     */
    public List<JDBCDataSource> getDataSources() {
        return context.getDataSources();
    }

    /* (non-Javadoc)
     * @see ca.sqlpower.matchmaker.swingui.SwingSessionContext#getPlDotIni()
     */
    public DataSourceCollection getPlDotIni() {
        return context.getPlDotIni();
    }


    //////// Persistent Prefs Support /////////

    /* (non-Javadoc)
     * @see ca.sqlpower.matchmaker.swingui.SwingSessionContext#getLastImportExportAccessPath()
     */
    public String getLastImportExportAccessPath() {
        return swingPrefs.get(MatchMakerSwingUserSettings.LAST_IMPORT_EXPORT_PATH, null);
    }

    /* (non-Javadoc)
     * @see ca.sqlpower.matchmaker.swingui.SwingSessionContext#setLastImportExportAccessPath(java.lang.String)
     */
    public void setLastImportExportAccessPath(String lastExportAccessPath) {
    	swingPrefs.put(MatchMakerSwingUserSettings.LAST_IMPORT_EXPORT_PATH, lastExportAccessPath);
    }

    /* (non-Javadoc)
     * @see ca.sqlpower.matchmaker.swingui.SwingSessionContext#getFrameBounds()
     */
    public Rectangle getFrameBounds() {
        Rectangle bounds = new Rectangle();
        bounds.x = swingPrefs.getInt(MatchMakerSwingUserSettings.MAIN_FRAME_X, 100);
        bounds.y = swingPrefs.getInt(MatchMakerSwingUserSettings.MAIN_FRAME_Y, 100);
        bounds.width = swingPrefs.getInt(MatchMakerSwingUserSettings.MAIN_FRAME_WIDTH, 600);
        bounds.height = swingPrefs.getInt(MatchMakerSwingUserSettings.MAIN_FRAME_HEIGHT, 440);
        return bounds;
    }

    /* (non-Javadoc)
     * @see ca.sqlpower.matchmaker.swingui.SwingSessionContext#setFrameBounds(java.awt.Rectangle)
     */
    public void setFrameBounds(Rectangle bounds) {
    	swingPrefs.putInt(MatchMakerSwingUserSettings.MAIN_FRAME_X, bounds.x);
    	swingPrefs.putInt(MatchMakerSwingUserSettings.MAIN_FRAME_Y, bounds.y);
    	swingPrefs.putInt(MatchMakerSwingUserSettings.MAIN_FRAME_WIDTH, bounds.width);
    	swingPrefs.putInt(MatchMakerSwingUserSettings.MAIN_FRAME_HEIGHT, bounds.height);
    }

    /* (non-Javadoc)
     * @see ca.sqlpower.matchmaker.swingui.SwingSessionContext#setLastLoginDataSource(ca.sqlpower.sql.SPDataSource)
     */
    public void setLastLoginDataSource(SPDataSource dataSource) {
        swingPrefs.put(MatchMakerSwingUserSettings.LAST_LOGIN_DATA_SOURCE, dataSource.getName());
    }

    /* (non-Javadoc)
     * @see ca.sqlpower.matchmaker.swingui.SwingSessionContext#getLastLoginDataSource()
     */
    public SPDataSource getLastLoginDataSource() {
        String lastDSName = swingPrefs.get(MatchMakerSwingUserSettings.LAST_LOGIN_DATA_SOURCE, null);
        if (lastDSName == null) return null;
        for (SPDataSource ds : getDataSources()) {
            if (ds.getName().equals(lastDSName)) return ds;
        }
        return null;
    }

    public void setAutoLoginDataSource(SPDataSource ds) {
        swingPrefs.put(MatchMakerSwingUserSettings.AUTO_LOGIN_DATA_SOURCE, ds.getName());
    }

    public JDBCDataSource getAutoLoginDataSource() {
        String lastDSName = swingPrefs.get(MatchMakerSwingUserSettings.AUTO_LOGIN_DATA_SOURCE, null);
        if (lastDSName == null) {
            lastDSName = DEFAULT_REPOSITORY_DATA_SOURCE_NAME;
        }
        for (JDBCDataSource ds : getDataSources()) {
            if (ds.getName().equals(lastDSName)) return ds;
        }
        return null;
    }

    public void setAddressCorrectionDataPath(String path) {
    	context.setAddressCorrectionDataPath(path);
    }
    
    public String getAddressCorrectionDataPath() {
    	return context.getAddressCorrectionDataPath();
    }
    
    
    ///////// Global GUI Stuff //////////

    /* (non-Javadoc)
     * @see ca.sqlpower.matchmaker.swingui.SwingSessionContext#showDatabaseConnectionManager()
     */
    public void showDatabaseConnectionManager(Window owner) {
        dbConnectionManager.showDialog(owner);
    }

    /* (non-Javadoc)
     * @see ca.sqlpower.matchmaker.swingui.SwingSessionContext#showLoginDialog(ca.sqlpower.sql.SPDataSource)
     */
    public void showLoginDialog(SPDataSource selectedDataSource) {
        loginDialog.showLoginDialog(selectedDataSource);
    }

    /**
     * This is the normal way of starting up the MatchMaker GUI. Based on the
     * user's preferences, this method either presents the repository login
     * dialog, or delegates the "launch default" operation to the delegate
     * context.
     * <p>
     * Under normal circumstances, the delegate context will be a
     * MatchMakerHibernateSession, so delegating the operation ends up (creating
     * and) logging into the local HSQLDB repository.
     */
    public void launchDefaultSession() {
        try {
        	ensureDefaultRepositoryDefined();
        	
            if (!isAutoLoginEnabled()) {
                showLoginDialog(getLastLoginDataSource());
            } else {
            	autoLogin();
            }
        } catch (Exception ex) {
           	MMSUtils.showExceptionDialogNoReport("DQguru Startup Failed", ex);
        }
    }

    /**
	 * Automatically logs the user into the repository designated as the user's
	 * preferred repository. If the specified repository does not actually exist,
	 * or is out of date, then the MatchMaker will attempt to create/update it.
	 */
	private void autoLogin() {
		JDBCDataSource dbSource = getAutoLoginDataSource();
		final MatchMakerSession sessionDelegate;
		try {
			if (dbSource != null) {
				// tries to login to the auto login database
				sessionDelegate = context.createSession(dbSource,
					dbSource.getUser(), dbSource.getPass());
			} else {
				// first time running match maker, run default
				sessionDelegate = context.createDefaultSession();
			}
			MatchMakerSwingSession session = new MatchMakerSwingSession(this, sessionDelegate);
			getSessions().add(session);
		    session.addSessionLifecycleListener(getSessionLifecycleListener());
			session.showGUI();
		} catch (Exception ex) {
			if (ex instanceof RepositoryVersionException) {
				handleRepositoryVersionException(dbSource, (RepositoryVersionException) ex);
			} else {
				JDialog errorDialog = MMSUtils.showExceptionDialogNoReport("Auto Login Failed!", ex);
				errorDialog.addWindowListener(new WindowAdapter() {
					public void windowDeactivated(WindowEvent evt) {
						showLoginDialog(getAutoLoginDataSource());
					}
				});
			}
		}
	}
    
    /**
     * Provides the user with options to dealing with a repository schema version problem.
     * If the schema is too old, attempt to run upgrade script. If the schema is too
     * "new", prompts user to download newer mm or visit forum. Redisplays the login screen
     * regardless of the result.
     * 
     * @param dbSource Used for running upgrade scripts to the schema.
     * @param e Stores information of the schema problem.
     */
	public void handleRepositoryVersionException(final JDBCDataSource dbSource,
			RepositoryVersionException e) {
		Throwable t = null;
		
		if (e.getCurrentVersion() == null) {
			// invalid schema version, just show the error dialog
			t = e;
		} else if (e.getCurrentVersion().compareTo(e.getRequiredVersion()) < 0) {
			// schema is too old, prompt user to upgrade
			int response = JOptionPane.showOptionDialog(null, e.getMessage() +
					"\nThe repository schema version is older than the DQguru required version." +
					"\nWould you like to upgrade the schema now?",
					"DQguru Repository Problem", JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE, null, new String[] {"Upgrade Now", "Not Now"}, "Upgrade Now");
			
			if (response == JOptionPane.YES_OPTION) {
				try {
					RepositoryUtil.upgradeSchema(dbSource, e.getCurrentVersion(), e.getRequiredVersion());
					
					JOptionPane.showMessageDialog(null, "The repository schema has been sucessfully upgrade to " +
							e.getRequiredVersion() + ".", "Repository Upgrade Success", JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception ex) {
					// failed upgrade, pass on exception to show user
					t = ex;
				}
			}
		} else if (e.getCurrentVersion().compareTo(e.getRequiredVersion()) > 0) {
			// schema too ...new? prompt user to download new mm or visit forum
			int response = JOptionPane.showOptionDialog(null, e.getMessage() +
					"\nThe repository schema version is newer than the DQguru required version." +
					"\nPlease download a newer version of the DQguru or visit our forum for help.",
					"DQguru Repository Problem", JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.WARNING_MESSAGE, null, new String[] {"Download Now", "Visit Forum", "Not Now"}, "Download Now");
			
			if (response == JOptionPane.YES_OPTION) { 
				launchBrowser(DOWNLOAD_URL);
			} else if (response == JOptionPane.NO_OPTION) {
				launchBrowser(FORUM_URL);
			}
		} else {
			// not something we're handling, just show the error dialog
			t = e;
		}
		
		if (t != null) {
			// error not handled
			JDialog errorDialog = MMSUtils.showExceptionDialogNoReport("Repository Schema Problem!", t);
    		errorDialog.addWindowListener(new WindowAdapter() {
    			public void windowDeactivated(WindowEvent arg0) {
    				showLoginDialog(dbSource);
    			}
    		});
		} else {
			// error successfully handled
			if (!isAutoLoginEnabled()) {
				showLoginDialog(dbSource);
			} else {
				autoLogin();
			}
		}
	}
	
	/**
	 * Displays the given url with the default browser.
	 * 
	 */
	private void launchBrowser(String url) {
		try {
			BrowserUtil.launch(url);
		} catch (IOException e) {
			SPSUtils.showExceptionDialogNoReport("Could not launch browser!", e);
		}
	}

    ///////// Private implementation details ///////////

    /**
     * Creates the delegate context, prompting the user (GUI) for any missing information.
     * @throws IOException
     */
    private static MatchMakerSessionContext createDelegateContext(Preferences prefs) throws IOException {
        DataSourceCollection<JDBCDataSource> plDotIni = null;
        String plDotIniPath = prefs.get(MatchMakerSessionContext.PREFS_PL_INI_PATH, null);
        while ((plDotIni = readPlDotIni(plDotIniPath)) == null) {
            logger.debug("readPlDotIni returns null, trying again...");
            String message;
            String[] options = new String[] {"Browse", "Create"};
            final int BROWSE = 0; // indices into above array
            final int CREATE = 1;
            if (plDotIniPath == null) {
                message = "location is not set";
            } else if (new File(plDotIniPath).isFile()) {
                message = "file \n\n\""+plDotIniPath+"\"\n\n could not be read";
            } else {
                message = "file \n\n\""+plDotIniPath+"\"\n\n does not exist";
            }
            int choice = JOptionPane.showOptionDialog(null,   // blocking wait
                    "The DQguru keeps its list of database connections" +
                    "\nin a file called PL.INI.  Your PL.INI "+message+"." +
                    "\n\nYou can browse for an existing PL.INI file on your system" +
                    "\nor allow the DQguru to create a new one in your home directory.",
                    "Missing PL.INI", 0, JOptionPane.INFORMATION_MESSAGE, null, options, null);

            if (choice == JOptionPane.CLOSED_OPTION) {
                throw new RuntimeException("Can't start without a pl.ini file");
            } else if (choice == BROWSE) {
                JFileChooser fc = new JFileChooser();
                fc.setFileFilter(SPSUtils.INI_FILE_FILTER);
                fc.setDialogTitle("Locate your PL.INI file");
                int fcChoice = fc.showOpenDialog(null);       // blocking wait
                if (fcChoice == JFileChooser.APPROVE_OPTION) {
                    plDotIniPath = fc.getSelectedFile().getAbsolutePath();
                } else {
                    plDotIniPath = null;
                }
            } else if (choice == CREATE) {
                String userHome = System.getProperty("user.home");
                if (userHome == null) {
                	throw new IllegalStateException("user.home property is null!");
                }
				plDotIniPath = userHome + File.separator + "pl.ini";
				// Create an empty file so the read won't throw an IOE
				if (new File(plDotIniPath).createNewFile()) {
					logger.debug("Created file " + plDotIniPath);
				} else {
					logger.debug("Did NOT create file " + plDotIniPath +
							"; mayhap it already exists?");
				}
            } else {
                throw new RuntimeException(
                "Unexpected return from JOptionPane.showOptionDialog to get pl.ini");
            }
        }
        
        prefs.put(MatchMakerSessionContext.PREFS_PL_INI_PATH, plDotIniPath);
        return new MatchMakerHibernateSessionContext(prefs, plDotIni);
    }

    private static DataSourceCollection<JDBCDataSource> readPlDotIni(String plDotIniPath) {
        if (plDotIniPath == null) {
            return null;
        }
        File pf = new File(plDotIniPath);
        if (!pf.exists() || !pf.canRead()) {
            return null;
        }

        DataSourceCollection pld = new PlDotIni<SPDataSource>(SPDataSource.class);
        
        // First, read the defaults
        try {
            logger.debug("Reading PL.INI defaults");
            pld.read(SwingSessionContextImpl.class.getClassLoader().getResourceAsStream("ca/sqlpower/sql/default_database_types.ini"));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read system resource default_database_types.ini", e);
        }
        
        // Now, merge in the user's own config
        try {
            pld.read(pf);
            return pld;
        } catch (IOException e) {
            MMSUtils.showExceptionDialogNoReport("Could not read " + pf, e);
            return null;
        }
    }
    
    public AbstractMungeComponent getMungeComponent(MungeStep ms,
			FormValidationHandler handler, MatchMakerSession session) {
    	//special cases
    	if (ms.isInputStep()) {
			return new SQLInputMungeComponent(ms, handler, session);
		} else if (ms instanceof DeDupeResultStep) {
			return new MungeResultMungeComponent(ms, handler, session);
		} else if (ms instanceof CleanseResultStep) {
			return new CleanseResultMungeComponent(ms,handler,session);
		}
		
    	StepDescription sd = stepProperties.get(ms.getClass());
		if (sd.getLogicClass().equals(ms.getClass())) {
			Constructor[] constructors = sd.getGuiClass().getDeclaredConstructors();
			
			for (Constructor con : constructors) {
				Type[] paramTypes = con.getGenericParameterTypes();	
				
				if (arrayEquals(paramTypes,MUNGECOM_CONSTRUCTOR_PARAMS)) {
					try {
						return (AbstractMungeComponent)con.newInstance(ms, handler, session);
					} catch (Throwable t) {
						throw new RuntimeException("Error generating munge step component: " + sd.getGuiClass().getName() + ". " 
								+ "Possibly caused by an error thrown in the constructor.", t);
					}
				}
			}
			throw new NoSuchMethodError("Error: No constructor (MungeStep, FormValidationHandler, MatchMakerSession) was found for the MungeComponent :"
					+ sd.getGuiClass());
		}
		
		throw new NoClassDefFoundError("Error: No MungeComponent was found for the given munge step: " + ms.getClass());
	}
    
    private static boolean arrayEquals(Object[] a, Object[] b) {
		if (a.length != b.length) {
			return false;
		}
		
		for (int x = 0; x < a.length; x++) {
			if (!a[x].equals(b[x])) {
				return false;
			}
		}
		return true;
	}
	
    /**
     * Populates the stepProperties list with the StepDescriptions that map the 
     * steps to their MungeComponents, name and Icon.
     * 
     * @throws ClassNotFoundException
     * @throws IOException
     */
	private void generatePropertiesList() throws ClassNotFoundException, IOException {
	   	Properties steps = new Properties();
	   	Map<String, StepDescription> stepProps = new HashMap<String, StepDescription>();
	   	
		steps.load(getClass().getClassLoader().getResourceAsStream("ca/sqlpower/matchmaker/swingui/munge/munge_components.properties"));
		
		try {
			steps.load(new FileInputStream((System.getProperty("user.home") + "/.matchmaker/munge_components.properties")));
		} catch (IOException e) {
		}
		
		for (Object oKey : steps.keySet()) {
			if (oKey instanceof String) {
					String key = (String) oKey;
					StringTokenizer st = new StringTokenizer(key, ".");
					
					if (st.nextToken().equals("step")) {
						String newKey = st.nextToken();
					if (!stepProps.containsKey(newKey)) {
						stepProps.put(newKey, new StepDescription());
					}
					stepProps.get(newKey).setProperty(st.nextToken(), steps.getProperty(key));
				}
			}
		}
		
		for (StepDescription sd : stepProps.values()) {
            if (sd.getLogicClass() == null) {
                throw new IllegalStateException("Step Description " + sd + " does not have logicClass set");
            }
			stepProperties.put(sd.getLogicClass(), sd);
		}
	}
	
    /**
     * Creates a new instance of the given class, wrapping any possible
     * exceptions into a RuntimeException.
     * 
     * @param create The class to create a new instance of
     * @return A new instance of the given class.
     * @throws RuntimeException if anything goes wrong with creating an instance
     */
	public MungeStep getMungeStep(Class<? extends MungeStep> create) {
        try {
            return create.newInstance();
        } catch (Throwable t) {
            throw new RuntimeException("Error generating munge step: " + create.getName() + ". " 
                    + "Possibly caused by an error thrown in the constructor.", t);
        }
	}

	public Map<Class, StepDescription> getStepMap() {
		return stepProperties;
	}

	public String getEmailSmtpHost() {
		return context.getEmailSmtpHost();
	}

	public void setEmailSmtpHost(String host) {
		context.setEmailSmtpHost(host);
	}
    
    public boolean isAutoLoginEnabled() {
    	return swingPrefs.getBoolean(MatchMakerSwingUserSettings.AUTO_LOGIN_ENABLED, true);
    }

    public void setAutoLoginEnabled(boolean enabled) {
    	swingPrefs.putBoolean(MatchMakerSwingUserSettings.AUTO_LOGIN_ENABLED, enabled);
    }

    public Collection<MatchMakerSession> getSessions() {
    	return context.getSessions();
    }

    public void closeAll() {
    	context.closeAll();
    }

    public SessionLifecycleListener<MatchMakerSession> getSessionLifecycleListener() {
    	return context.getSessionLifecycleListener();
    }

	public void ensureDefaultRepositoryDefined() {
		context.ensureDefaultRepositoryDefined();
	}

	public void addPreferenceChangeListener(PreferenceChangeListener l) {
		context.addPreferenceChangeListener(l);
	}

	public void removePreferenceChangeListener(PreferenceChangeListener l) {
		context.removePreferenceChangeListener(l);
	}
}
