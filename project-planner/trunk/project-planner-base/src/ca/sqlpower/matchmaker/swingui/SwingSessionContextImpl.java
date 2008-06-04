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

import java.awt.Rectangle;
import java.awt.Window;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.UIManager;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.matchmaker.MatchMakerConfigurationException;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchMakerSessionContext;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.swingui.munge.AbstractMungeComponent;
import ca.sqlpower.matchmaker.swingui.munge.StepDescription;
import ca.sqlpower.security.PLSecurityException;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.PLSchemaException;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.util.ExceptionReport;
import ca.sqlpower.util.VersionFormatException;
import ca.sqlpower.validation.swingui.FormValidationHandler;


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
	private static final Type[] MUNGESTEP_CONSTRUCTOR_PARAMS = {StepDescription.class};

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
     * Creates a new Swing session context, which is a holding place for all the basic
     * settings in the MatchMaker GUI application.  This constructor creates its own delegate
     * session context object based on information in the given prefs node, or failing that,
     * by prompting the user with a GUI.
     * @throws ClassNotFoundException 
     */
    public SwingSessionContextImpl(Preferences prefsRootNode) throws IOException, ClassNotFoundException {
        throw new UnsupportedOperationException("You can't launch the project planner this way. Try creating a MatchMakerXMLSessionContext instead.");
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
        throw new UnsupportedOperationException("Not implemented");
    }

    /* (non-Javadoc)
     * @see ca.sqlpower.matchmaker.swingui.SwingSessionContext#showLoginDialog(ca.sqlpower.sql.SPDataSource)
     */
    public void showLoginDialog(SPDataSource selectedDataSource) {
        throw new UnsupportedOperationException("Not implemented");
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
           	MMSUtils.showExceptionDialogNoReport("Project Planner Startup Failed", ex);
        }
    }
    
    public void launchDefaultSession(long projectId) {
    	try {
            if (!isAutoLoginEnabled()) {
                showLoginDialog(getLastLoginDataSource());
            } else {
                MatchMakerSession sessionDelegate = context.createDefaultSession();
                MatchMakerSwingSession session = new MatchMakerSwingSession(this, sessionDelegate);
                session.showGUI();
                
                List<Project> projects = new ArrayList<Project>();
                projects.addAll(sessionDelegate.getDefaultPlFolder().getChildren());
                projects.addAll(sessionDelegate.findFolder(MatchMakerSession.SHARED_FOLDER_NAME).getChildren());
                projects.addAll(sessionDelegate.findFolder(MatchMakerSession.GALLERY_FOLDER_NAME).getChildren());
                
                for (Project p : projects) {
                	if (p.getOid() == projectId) {
                		TreePath treePath;
                		if (p.getChildren().size() > 0) {
                			MungeProcess mp = p.getChildren().get(0);
                			treePath = (((MatchMakerTreeModel)session.getTree().getModel()).getPathForNode(mp));
                		} else {
                			treePath = ((MatchMakerTreeModel)session.getTree().getModel()).getPathForNode(p);
                		}
        				session.getTree().setSelectionPath(treePath);
                	}
                }
            }
        } catch (Exception ex) {
           	MMSUtils.showExceptionDialogNoReport("Project Planner Startup Failed", ex);
        }
    	
    }

    ///////// Private implementation details ///////////

    /**
     * NOTE: This method creates a new munge component, it does not get an existing one!
     * <p>
     * This will create a new {@link AbstractMungeComponent} through reflection
     * using the given parameters.
     */
    public AbstractMungeComponent getMungeComponent(MungeStep ms,
			FormValidationHandler handler, MatchMakerSession session) {
		
    	StepDescription sd = stepProperties.get(ms.getName());
    	logger.debug("looking for step " + ms.getName());
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
						return (MungeStep)con.newInstance(sd);
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
        return true;
    }

    public void setAutoLoginEnabled(boolean enabled) {
        swingPrefs.putBoolean(MatchMakerSwingUserSettings.AUTO_LOGIN_ENABLED, enabled);
    }
}
