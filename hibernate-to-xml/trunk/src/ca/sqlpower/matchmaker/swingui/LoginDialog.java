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

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerSessionContext;
import ca.sqlpower.matchmaker.dao.hibernate.MatchMakerHibernateSessionContext;
import ca.sqlpower.matchmaker.util.ImportHibernateUtil;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.swingui.ConnectionComboBoxModel;
import ca.sqlpower.swingui.ProgressWatcher;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.SPSwingWorker;
import ca.sqlpower.swingui.SwingWorkerRegistry;
import ca.sqlpower.swingui.db.DataSourceTypeDialogFactory;
import ca.sqlpower.swingui.db.DatabaseConnectionManager;
import ca.sqlpower.swingui.db.DefaultDataSourceDialogFactory;
import ca.sqlpower.swingui.db.DefaultDataSourceTypeDialogFactory;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This class appears to contain the GUI for a login dialog for logging
 * into a specific database selected from a list of database connections
 * in a combobox. Note that even though the class is called 'LoginDialog',
 * it doesn't actually extend or even use JDialog. Rather, it contains a
 * JFrame which it uses to display the Login screen.
 */
public class LoginDialog implements SwingWorkerRegistry {

	/**
	 * Creates the delegate context, prompting the user (GUI) for any missing
	 * information.
	 * <p>
	 * Refactored from SwingSessionContextImpl.
	 * 
	 * @throws IOException
	 */
    private static MatchMakerHibernateSessionContext createContext(Preferences prefs) throws IOException {
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
    
    /**
     * Loads the pl.ini file if necessary.
     */
    private static DataSourceCollection<JDBCDataSource> readPlDotIni(String plDotIniPath) {
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
            pld.read(LoginDialog.class.getClassLoader().getResourceAsStream("ca/sqlpower/sql/default_database_types.ini"));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read system resource default_database_types.ini", e);
        }
        
        // Now, merge in the user's own config
        try {
            pld.read(pf);
            return pld;
        } catch (IOException e) {
            SPSUtils.showExceptionDialogNoReport("Could not read " + pf, e);
            return null;
        }
    }
	
	public static void main(String[] args) throws Exception {
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(null));
		Preferences prefs = Preferences.userNodeForPackage(MatchMakerSessionContext.class);

		MatchMakerHibernateSessionContext context = createContext(prefs);
		
		LoginDialog loginDialog = new LoginDialog(context);
		loginDialog.showLoginDialog(null);
	}

	private static Logger logger = Logger.getLogger(LoginDialog.class);

    private class LoginAction extends SPSwingWorker implements ActionListener {

        public LoginAction(SwingWorkerRegistry registry) {
        	super(registry);
        	setMessage("Logging in...");
        	setJobSize(null);
        	setProgress(0);
        }
        
        public void actionPerformed(ActionEvent e) {
            logger.debug("LoginAction.actionPerformed(): disabling login button");
            loginButton.setEnabled(false);
            userID.setEnabled(false);
            password.setEnabled(false);
            dbList.setEnabled(false);
            if (dbSource == null) {
                JOptionPane.showMessageDialog(frame,
                        "Please select a database connection first!",
                        "Unknown database connection",
                        JOptionPane.ERROR_MESSAGE);
                logger.debug("LoginAction.actionPerformed(): enabling login button (connection not specified)");
                loginButton.setEnabled(true);
                userID.setEnabled(true);
                password.setEnabled(true);
                dbList.setEnabled(true);
                return;
            }

            String driverClass = dbSource.getDriverClass();
            if (driverClass == null || driverClass.length() == 0) {
                JOptionPane.showMessageDialog(frame,
                        "Datasource not configured (no JDBC Driver)",
                        "Database connection incomplete",
                        JOptionPane.ERROR_MESSAGE);
                logger.debug("LoginAction.actionPerformed(): enabling login button (connection has no driver class");
                loginButton.setEnabled(true);
                userID.setEnabled(true);
                password.setEnabled(true);
                dbList.setEnabled(true);
                return;
            }

            try {
            	progressBar.setVisible(true);
            	logger.debug("Progress Bar has been set to visible");
            	ProgressWatcher watcher = new ProgressWatcher(progressBar, this);
            	watcher.setHideProgressBarWhenFinished(true);
            	watcher.start();
                new Thread(this).start();
            } catch (Exception ex) {
                SPSUtils.showExceptionDialogNoReport(frame,
                        "Connection Error", ex );
                loginButton.setEnabled(true);
                userID.setEnabled(true);
                password.setEnabled(true);
                dbList.setEnabled(true);
            }
        }

        @Override
        /** Called (once) by run() in superclass */
        public void doStuff() throws Exception {
        	String saveDirectoryLocation = saveDirectory.getText();
        	File saveDirectoryFile = new File(saveDirectoryLocation);
        	if (!saveDirectoryFile.isDirectory()) {
        		throw new IllegalArgumentException("The directory " + saveDirectoryLocation + " is not a real directory.");
        	}
        	
        	JDBCDataSource ds = new JDBCDataSource(dbSource);
        	ds.setName(userID.getText());
        	ds.setPass(new String(password.getPassword()));
        	
        	try {
        		ImportHibernateUtil.exportHibernateProjects(sessionContext, ds, saveDirectoryFile);
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
        }

        @Override
        public void cleanup() {
        	logger.debug("LoginAction.actionPerformed(): enabling login button (login has either failed or not; dialog might still be showing)");
        	loginButton.setEnabled(true);
        	userID.setEnabled(true);
        	password.setEnabled(true);
        	dbList.setEnabled(true);
        	logger.debug("Progress bar has been set to NOT visible");
        	if (getDoStuffException() != null) {
        		throw new RuntimeException(getDoStuffException());
        	}
        }
        
    }

    /**
     * The Login button gets disabled if a null database is selected.
     * If the user selects an actually database connection, then the login
     * button will be enabled. If the user somehow selects a null database
     * connection, then the login button should get disabled again.
     */
    private class DBListListener implements ActionListener {
    	public void actionPerformed(ActionEvent e) {
    		JComboBox list = (JComboBox) e.getSource();
    		if (list.getSelectedItem() != null) {
    			LoginDialog.this.loginButton.setEnabled(true);
    		} else {
    			LoginDialog.this.loginButton.setEnabled(false);
    		}
    	}
    }
    
    private final Action closeAction = new AbstractAction("Close") {
        public void actionPerformed(ActionEvent e) {
        	frameClosing();	
        	System.exit(0);
        }
    };

    private final Action connectionManagerAction =
    	new AbstractAction("Manage Connections...") {
		public void actionPerformed(ActionEvent e) {
		    DataSourceTypeDialogFactory dsTypeDialogFactory = new DataSourceTypeDialogFactory() {

		    	public Window showDialog(Window owner) {
		    		DefaultDataSourceTypeDialogFactory d = new DefaultDataSourceTypeDialogFactory(sessionContext.getPlDotIni());
		    		return d.showDialog(owner);
		        }
		    };
			DatabaseConnectionManager dsManager = new DatabaseConnectionManager(sessionContext.getPlDotIni(), new DefaultDataSourceDialogFactory(), dsTypeDialogFactory);
			dsManager.showDialog(frame);
		};
	};

	/**
	 * The session context for this application.  The list of available
	 * databases lives here, and login attempts will happen via this object.
	 */
	private MatchMakerHibernateSessionContext sessionContext;

    /**
     * The frame that this dialog's UI gets displayed in.
     */
    private final JFrame frame;

	private JComboBox dbList;
	private JTextField userID;
	private JPasswordField password;
	private JLabel dbSourceName;
    private JButton loginButton = new JButton();
    private JProgressBar progressBar = new JProgressBar();
    
    /**
     * The {@link JDBCDataSource} object the user picked from the combo box on the login dialog.
     * Once the login process has been initiated, this is the data source of the repository
     * database.
     */
    private JDBCDataSource dbSource;
	
    private ConnectionComboBoxModel connectionModel;
	private JComponent panel;

    private ActionListener loginAction = new LoginAction(this);

    /**
     * This text field contains the directory location where the xml
     * representation of projects in Hibernate will be written to.
     */
    private JTextField saveDirectory;
    
    private Action saveDirectoryChooserAction = new AbstractAction("Choose directory...") {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser fileChooser = new JFileChooser(saveDirectory.getText());
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnValue = fileChooser.showOpenDialog(frame);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				saveDirectory.setText(fileChooser.getSelectedFile().getAbsolutePath());
			}
		}
	};
    
    /**
     * This action is an SPSwingWorker, which means it needs to register itself
     * somewhere.  Normally, the registry would be on the session object, but
     * in this case we're in the process of creating a session, so that won't
     * work.  Instead, we just provide our own registry and register this worker
     * with itself.
     */
    private final Set<SPSwingWorker> swingWorkers = new HashSet<SPSwingWorker>();
    
	private ListDataListener connListener = new ListDataListener() {

		public void intervalAdded(ListDataEvent e) {
		}

		public void intervalRemoved(ListDataEvent e) {
		}

		public void contentsChanged(ListDataEvent e) {
		    if ( e.getType() == ListDataEvent.CONTENTS_CHANGED ) {
		        JDBCDataSource dbSource = (JDBCDataSource) ((ConnectionComboBoxModel) (e.getSource())).getSelectedItem();
		        dbSourceName.setText(dbSource.getName());
		        userID.setText(dbSource.getUser());
		        password.setText(dbSource.getPass());
		        LoginDialog.this.dbSource = dbSource;
            }
		}};

    /**
     * Creates a new login dialog, but does not display it.
     */
	public LoginDialog(MatchMakerHibernateSessionContext sessionContext) {
		super();
        this.sessionContext = sessionContext;
        frame = new JFrame("DQguru Export");
        frame.setIconImage(new ImageIcon(getClass().getResource("/icons/dqguru_24.png")).getImage());
		panel = createPanel();
        frame.getContentPane().add(panel);
        SPSUtils.makeJDialogCancellable(frame, closeAction);
        frame.addWindowListener(optimizationManager);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

    /**
     * Makes this login dialog visible, packing it and centering it on the screen.
     * You will normally not call this method directly; see
     * {@link SwingSessionContext#showLoginDialog(SPDataSource)}.
     *
     * @param selectedDataSource The data source to default the datasource combo box,
     * username, and password fields to.
     */
    void showLoginDialog(SPDataSource selectedDataSource) {
        setDbSource(selectedDataSource);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        loginButton.requestFocus();
    }

	WindowListener optimizationManager = new WindowAdapter() {
		public void windowClosed(java.awt.event.WindowEvent e) {
			frameClosing();
			System.exit(0);
		};
	};

	public JComponent createPanel() {

		FormLayout layout = new FormLayout(
				"4dlu, pref, 4dlu, fill:min(50dlu;pref):grow, pref, 4dlu, min(15dlu;pref), min, 4dlu", // columns
				" 10dlu,pref,4dlu,pref,10dlu,pref,4dlu,pref,4dlu,pref,10dlu,pref,10dlu,pref,10dlu,pref,10dlu,pref,10dlu"); // rows

		CellConstraints cc = new CellConstraints();

		JLabel userIDLabel = new JLabel("User ID:");
		JLabel passwordLabel = new JLabel("Password:");

		userID = new JTextField(20);
		password = new JPasswordField(20);
		dbSourceName = new JLabel();
		saveDirectory = new JTextField(20);

		JLabel line1 = new JLabel("Please choose one of the following databases to export from:");
		connectionModel = new ConnectionComboBoxModel(sessionContext.getPlDotIni());
		connectionModel.addListDataListener(connListener);
		dbList = new JComboBox(connectionModel);
		dbList.addActionListener(new DBListListener());
		
		JLabel dbSourceName1 = new JLabel("Database source name:");

		JButton connectionManagerButton = new JButton(connectionManagerAction);
		loginButton.addActionListener(loginAction);
		loginButton.setText("Export");
		if (connectionModel.getSelectedItem() == null) {
			loginButton.setEnabled(false);
		}
		JButton cancelButton = new JButton(closeAction);

		ButtonBarBuilder bbBuilder = new ButtonBarBuilder();
        bbBuilder.addGridded(connectionManagerButton);
        bbBuilder.addUnrelatedGap();
		bbBuilder.addGridded(loginButton);
		bbBuilder.addRelatedGap();
		bbBuilder.addGridded(cancelButton);

		PanelBuilder pb;
		JPanel p = logger.isDebugEnabled()  ? new FormDebugPanel(layout) : new JPanel(layout);

		pb = new PanelBuilder(layout,p);
		pb.setDefaultDialogBorder();

		pb.add(line1, cc.xyw(2, 2, 6));
        pb.add(dbList,cc.xyw(2, 4, 6));
		pb.add(dbSourceName1, cc.xy(2,6,"r,c"));
		pb.add(dbSourceName, cc.xyw(4,6,3));
		pb.add(userIDLabel, cc.xy(2,8,"r,c"));
		pb.add( userID, cc.xyw(4,8,2,"l,c"));
		pb.add(passwordLabel,cc.xy(2,10,"r,c"));
		pb.add(password,cc.xyw(4,10,2,"l,c"));
		pb.add(new JLabel("Save directory"), cc.xy(2, 12, "r, c"));
		pb.add(saveDirectory, cc.xyw(4, 12, 1, "l, c"));
		pb.add(new JButton(saveDirectoryChooserAction), cc.xyw(5, 12, 1, "l, c"));

		progressBar.setVisible(false);
		pb.add(progressBar,cc.xyw(2,14,6));
		pb.add(bbBuilder.getPanel(), cc.xyw(2,18,7));

		frame.getRootPane().setDefaultButton(loginButton);
		return pb.getPanel();
	}

	public JPanel getPanel() {
		return (JPanel) panel;
	}

	public void setPanel(JPanel panel) {
		this.panel = panel;
	}

	public SPDataSource getDbSource() {
		return dbSource;
	}

	public void setDbSource(SPDataSource dbSource) {
		connectionModel.setSelectedItem(dbSource);
	}

	/**
	 * Workaround for not yet having a session to register with.
	 */
	public void registerSwingWorker(SPSwingWorker worker) {
		swingWorkers.add(worker);
	}

	/**
	 * Workaround for not yet having a session to register with.
	 */
	public void removeSwingWorker(SPSwingWorker worker) {
		swingWorkers.remove(worker);
	}
	
	private void frameClosing() {
		sessionContext.closeAll();
	}
}