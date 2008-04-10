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
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.matchmaker.MatchMakerConfigurationException;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchMakerSessionContext;
import ca.sqlpower.matchmaker.dao.hibernate.MatchMakerHibernateSessionContext;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.swingui.munge.AbstractMungeComponent;
import ca.sqlpower.matchmaker.swingui.munge.StepDescription;
import ca.sqlpower.security.PLSecurityException;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.PLSchemaException;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.swingui.DataEntryPanelBuilder;
import ca.sqlpower.swingui.JDefaultButton;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.db.DataSourceDialogFactory;
import ca.sqlpower.swingui.db.DataSourceTypeDialogFactory;
import ca.sqlpower.swingui.db.DataSourceTypeEditor;
import ca.sqlpower.swingui.db.DatabaseConnectionManager;
import ca.sqlpower.util.ExceptionReport;
import ca.sqlpower.util.VersionFormatException;
import ca.sqlpower.validation.swingui.FormValidationHandler;

import com.jgoodies.forms.factories.ButtonBarFactory;


public class SwingSessionContextImpl implements MatchMakerSessionContext, SwingSessionContext {

    private static final Logger logger = Logger.getLogger(SwingSessionContextImpl.class);

	/**
	 * The array that looks like the set of types we are expecting for the correct constructor for any munge component
	 *  (excluding the input and output steps).
	 */
	private static final Type[] MUNGECOM_CONSTRUCTOR_PARAMS = {MungeStep.class, FormValidationHandler.class, MatchMakerSession.class, Icon.class};
	
	/**
	 * The array that looks like the set of types we are expecting for the correct munge step constructor.
	 */
	private static final Type[] MUNGESTEP_CONSTRUCTOR_PARAMS = {String.class};

    /**
	 * The list of information about mungeSteps, which stores their StepClass, GUIClass, name and icon
	 */
	private final Map<String, StepDescription> stepProperties = new HashMap<String, StepDescription>();
    
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
    private final Action loginDatabaseConnectionAction = new AbstractAction("Login") {

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

		public JDialog showDialog(Window parentWindow, SPDataSource dataSource,	Runnable onAccept) {
			return MMSUtils.showDbcsDialog(parentWindow, dataSource, onAccept);
		}
    	
    };
    
    /**
     * Implementation of DataSourceTypeDialogFactory that will display a DataSourceTypeEditor dialog
     */
    private final DataSourceTypeDialogFactory dsTypeDialogFactory = new DataSourceTypeDialogFactory() {
        
    	private JDialog d; 
    	private DataSourceTypeEditor editor;
    	
    	public Window showDialog(Window owner) {
        	if (d == null) {
	    		d = SPSUtils.makeOwnedDialog(owner, "JDBC Drivers");
	        	editor = new DataSourceTypeEditor(context.getPlDotIni());
	        	
	        	JPanel cp = new JPanel(new BorderLayout(12,12));
	            cp.add(editor.getPanel(), BorderLayout.CENTER);
	            cp.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
	        	
	        	JDefaultButton okButton = new JDefaultButton(DataEntryPanelBuilder.OK_BUTTON_LABEL);
	            okButton.addActionListener(new ActionListener() {
	                    public void actionPerformed(ActionEvent evt) {
	                        editor.applyChanges();
	                        d.dispose();
	                    }
	                });
	        
	            Action cancelAction = new AbstractAction() {
	                    public void actionPerformed(ActionEvent evt) {
	                        editor.discardChanges();
	                        d.dispose();
	                    }
	            };
	            cancelAction.putValue(Action.NAME, DataEntryPanelBuilder.CANCEL_BUTTON_LABEL);
	            JButton cancelButton = new JButton(cancelAction);
	    
	            JPanel buttonPanel = ButtonBarFactory.buildOKCancelBar(okButton, cancelButton);
	    
	            SPSUtils.makeJDialogCancellable(d, cancelAction);
	            d.getRootPane().setDefaultButton(okButton);
	            cp.add(buttonPanel, BorderLayout.SOUTH);
	        	
	        	d.setContentPane(cp);
	        	d.pack();
	        	d.setLocationRelativeTo(owner);
        	}
        	d.setVisible(true);
            return d;
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
    public MatchMakerSwingSession createSession(
            SPDataSource ds, String username, String password)
    throws PLSecurityException, SQLException, IOException, VersionFormatException,
            PLSchemaException, ArchitectException, MatchMakerConfigurationException {
        return new MatchMakerSwingSession(this, context.createSession(ds, username, password));
    }

    public MatchMakerSession createDefaultSession() {
        return context.createDefaultSession();
    }
    
    /* (non-Javadoc)
     * @see ca.sqlpower.matchmaker.swingui.SwingSessionContext#getDataSources()
     */
    public List<SPDataSource> getDataSources() {
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

    public SPDataSource getAutoLoginDataSource() {
        String lastDSName = swingPrefs.get(MatchMakerSwingUserSettings.AUTO_LOGIN_DATA_SOURCE, null);
        if (lastDSName == null) {
            lastDSName = DEFAULT_REPOSITORY_DATA_SOURCE_NAME;
        }
        for (SPDataSource ds : getDataSources()) {
            if (ds.getName().equals(lastDSName)) return ds;
        }
        return null;
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
            if (!isAutoLoginEnabled()) {
                showLoginDialog(getLastLoginDataSource());
            } else {
                MatchMakerSession sessionDelegate = context.createDefaultSession();
                MatchMakerSwingSession session = new MatchMakerSwingSession(this, sessionDelegate);
                session.showGUI();
            }
        } catch (Exception ex) {
           	MMSUtils.showExceptionDialogNoReport("MatchMaker Startup Failed", ex);
        }
    }

    ///////// Private implementation details ///////////

    /**
     * Creates the delegate context, prompting the user (GUI) for any missing information.
     * @throws IOException
     */
    private static MatchMakerSessionContext createDelegateContext(Preferences prefs) throws IOException {
        DataSourceCollection plDotIni = null;
        //XXX: We should NOT be using ArchitectSession for this
        String plDotIniPath = prefs.get(ArchitectSession.PREFS_PL_INI_PATH, null);
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
                    "The MatchMaker keeps its list of database connections" +
                    "\nin a file called PL.INI.  Your PL.INI "+message+"." +
                    "\n\nYou can browse for an existing PL.INI file on your system" +
                    "\nor allow the Architect to create a new one in your home directory." +
                    "\n\nHint: If you are a Power*Loader Suite user, you should browse for" +
                    "\nan existing PL.INI in your Power*Loader installation directory.",
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
        
        //XXX: We should NOT be using ArchitectSession for this
        prefs.put(ArchitectSession.PREFS_PL_INI_PATH, plDotIniPath);
        return new MatchMakerHibernateSessionContext(prefs, plDotIni);
    }

    private static DataSourceCollection readPlDotIni(String plDotIniPath) {
        if (plDotIniPath == null) {
            return null;
        }
        File pf = new File(plDotIniPath);
        if (!pf.exists() || !pf.canRead()) {
            return null;
        }

        DataSourceCollection pld = new PlDotIni();
        
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
    
    /**
     * NOTE: This method creates a new munge component, it does not get an existing one!
     * <p>
     * This will create a new {@link AbstractMungeComponent} through reflection
     * using the given parameters.
     */
    public AbstractMungeComponent getMungeComponent(MungeStep ms,
			FormValidationHandler handler, MatchMakerSession session) {
		
    	StepDescription sd = stepProperties.get(ms.getName());
		if (sd.getName().equals(ms.getName())) {
			Constructor[] constructors = sd.getGuiClass().getDeclaredConstructors();
			
			for (Constructor con : constructors) {
				Type[] paramTypes = con.getGenericParameterTypes();	
				
				if (arrayEquals(paramTypes,MUNGECOM_CONSTRUCTOR_PARAMS)) {
					try {
						logger.debug("Passing the icon " + sd.getMainIcon() + " to the new " + sd.getGuiClass());
						return (AbstractMungeComponent)con.newInstance(ms, handler, session, sd.getMainIcon());
					} catch (Throwable t) {
						throw new RuntimeException("Error generating munge step component: " + sd.getGuiClass().getName() + ". " 
								+ "Possibly caused by an error thrown in the constructor.", t);
					}
				}
			}
			throw new NoSuchMethodError("Error: No constructor (MungeStep, FormValidationHandler, MatchMakerSession, Icon) was found for the MungeComponent :"
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
						logger.debug("Added new step description for " + newKey);
					}
					stepProps.get(newKey).setProperty(st.nextToken(), steps.getProperty(key));
				}
			}
		}
		logger.debug("We have " + stepProps.size() + " step descriptions.");
		
		for (StepDescription sd : stepProps.values()) {
            if (sd.getLogicClass() == null) {
                throw new IllegalStateException("Step Description " + sd + " does not have logicClass set");
            }
			stepProperties.put(sd.getName(), sd);
		}
	}
	
    /**
     * Creates a new instance of the given class, wrapping any possible
     * exceptions into a RuntimeException.
     * 
     * @param sd The class to create a new instance of
     * @return A new instance of the given class.
     * @throws RuntimeException if anything goes wrong with creating an instance
     */
	public MungeStep getMungeStep(StepDescription sd) {
        try {
        	Constructor[] constructors = sd.getLogicClass().getDeclaredConstructors();
			
			for (Constructor con : constructors) {
				Type[] paramTypes = con.getGenericParameterTypes();	
				
				if (arrayEquals(paramTypes,MUNGESTEP_CONSTRUCTOR_PARAMS)) {
					try {
						logger.debug("Passing the icon " + sd.getMainIcon() + " to the new " + sd.getGuiClass());
						return (MungeStep)con.newInstance(sd.getName());
					} catch (Throwable t) {
						throw new RuntimeException("Error generating munge step component: " + sd.getGuiClass().getName() + ". " 
								+ "Possibly caused by an error thrown in the constructor.", t);
					}
				}
			}
			throw new NoSuchMethodError("Error: No constructor (MungeStep, FormValidationHandler, MatchMakerSession, Icon) was found for the MungeComponent :"
					+ sd.getGuiClass());
        } catch (Throwable t) {
            throw new RuntimeException("Error generating munge step: " + sd.getName() + ". " 
                    + "Possibly caused by an error thrown in the constructor.", t);
        }
	}

	public Map<String, StepDescription> getStepMap() {
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
}
