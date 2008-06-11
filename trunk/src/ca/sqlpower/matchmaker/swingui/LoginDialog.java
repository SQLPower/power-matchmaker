/*
 * Copyright (c) 2008, SQL Power Group Inc.
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
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

import ca.sqlpower.sql.PLSchemaException;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.swingui.ConnectionComboBoxModel;
import ca.sqlpower.swingui.MonitorableWorker;
import ca.sqlpower.swingui.ProgressWatcher;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.SPSwingWorker;
import ca.sqlpower.swingui.SwingWorkerRegistry;

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

	private static Logger logger = Logger.getLogger(LoginDialog.class);

    private class LoginAction extends MonitorableWorker implements ActionListener {

        private boolean loginWasSuccessful = false;
        
        /**
         * Indicates that the login process has begun.
         */
        private boolean started;
        
        /**
         * Indicated that the login process has terminated (with either
         * success or failure).
         */
        private boolean finished;

        public LoginAction(SwingWorkerRegistry registry) {
        	super(registry);
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
                this.finished = true;
            }
        }

        @Override
        /** Called (once) by run() in superclass */
        public void doStuff() throws Exception {
        	logger.debug("LoginAction.doStuff() was invoked!");
            loginWasSuccessful = false;
            started = true;
            finished = false;
            
            // Reset exception to null for each login. Without it,
            // cleanup() would think there was an error if one existed
            // in the previous attempt to login even if things were fixed.
            setDoStuffException(null);
            
            session = sessionContext.createSession(dbSource,
            		userID.getText(), new String(password.getPassword()));
            session.getDatabase().populate();
            loginWasSuccessful = true;
        }

        @Override
        public void cleanup() {
        	logger.debug("LoginAction.cleanup() starting");
            try {
                if (getDoStuffException() != null) {
                	finished = true;
                	if (getDoStuffException() instanceof PLSchemaException) {
                		PLSchemaException ex = (PLSchemaException) getDoStuffException();
                		SPSUtils.showExceptionDialogNoReport(frame,
                                "MatchMaker Repository Problem",
                                "Existing version: "+ex.getCurrentVersion() +
                                "\nRequired Version: "+ex.getRequiredVersion(),
                                ex);
                	} else {
                		SPSUtils.showExceptionDialogNoReport(frame, "Login failed", getDoStuffException());
                	}
                	
                } else if (
                        session != null &&
                        session.getDatabase() != null &&
                        session.getDatabase().isPopulated() &&
                        loginWasSuccessful) {
                	logger.debug("It looks like the login worked.");
                    sessionContext.setLastLoginDataSource(dbSource);
                    session.showGUI();
                    frame.dispose();
                } else {
                    JOptionPane.showMessageDialog(frame, "The login failed for an unknown reason.");
                }
            } finally {
                logger.debug("LoginAction.actionPerformed(): enabling login button (login has either failed or not; dialog might still be showing)");
                loginButton.setEnabled(true);
                userID.setEnabled(true);
                password.setEnabled(true);
                dbList.setEnabled(true);
                logger.debug("Progress bar has been set to NOT visible");
                finished = true;
            }
        }
        
		public Integer getJobSize() {
			return null;
		}

		public String getMessage() {
			return "Logging in...";
		}

		public int getProgress() {
			return 0;
		}

		public boolean hasStarted() {
			return started;
		}

		public boolean isFinished() {
			return finished;
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
    
    private final Action cancelAction = new AbstractAction("Cancel") {
        public void actionPerformed(ActionEvent e) {
        	if (session == null) {
        		// no sessions have been created yet
        		System.exit(0);
        	} else {
        		// there are other sessions, don't terminate the JVM
        		frame.dispose();
        	}
        }
    };

    private final Action connectionManagerAction =
    	new AbstractAction("Manage Connections...") {
		public void actionPerformed(ActionEvent e) {
			sessionContext.showDatabaseConnectionManager(frame);
		};
	};

	/**
	 * The session context for this application.  The list of available
	 * databases lives here, and login attempts will happen via this object.
	 */
	private SwingSessionContextImpl sessionContext;

	/**
	 * The session that we will create upon successful login.
	 */
	private MatchMakerSwingSession session;

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
     * The SPDatasource object the user picked from the combo box on the login dialog.
     * Once the login process has been initiated, this is the data source of the repository
     * database.
     */
    private SPDataSource dbSource;
	
    private ConnectionComboBoxModel connectionModel;
	private JComponent panel;

    private ActionListener loginAction = new LoginAction(this);

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
		        SPDataSource dbSource = (SPDataSource) ((ConnectionComboBoxModel) (e.getSource())).getSelectedItem();
		        dbSourceName.setText(dbSource.getName());
		        userID.setText(dbSource.getUser());
		        password.setText(dbSource.getPass());
		        LoginDialog.this.dbSource = dbSource;
            }
		}};

    /**
     * Creates a new login dialog, but does not display it.  Normally you should use
     * {@link SwingSessionContextImpl#showLoginDialog()} and not create new login dialogs
     * yourself.
     */
	public LoginDialog(SwingSessionContextImpl sessionContext) {
		super();
        this.sessionContext = sessionContext;
        frame = new JFrame("Power*MatchMaker Login");
        frame.setIconImage(new ImageIcon(getClass().getResource("/icons/matchmaker_24.png")).getImage());
		panel = createPanel();
        frame.getContentPane().add(panel);
        SPSUtils.makeJDialogCancellable(frame, cancelAction);
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

		/** If you try to login but have no Connections set up yet,
		 * there is nothing you can do except "Manage Connections",
		 * so we jump you to there.
		 */
		@Override
		public void windowOpened(WindowEvent e) {
			int dbListSize = connectionModel.getSize();

			if (dbListSize == 0 ||
				(dbListSize == 1 && connectionModel.getElementAt(0) == null)) {
				ActionEvent actionEvent = new ActionEvent(LoginDialog.this, 0, "Fill in empty list");
				connectionManagerAction.actionPerformed(actionEvent);
			}
		}
	};

	public JComponent createPanel() {

		FormLayout layout = new FormLayout(
				"4dlu, pref, 4dlu, fill:min(50dlu;pref):grow, min(20dlu;pref), 4dlu, min(15dlu;pref), fill:min, 4dlu", // columns
				" 10dlu,pref,4dlu,pref,10dlu,pref,4dlu,pref,4dlu,pref,10dlu,pref,10dlu,pref,10dlu,pref,10dlu"); // rows

		layout.setColumnGroups(new int [][] { {2,5},{4,7}});
		CellConstraints cc = new CellConstraints();

		JLabel userIDLabel = new JLabel("User ID:");
		JLabel passwordLabel = new JLabel("Password:");

		userID = new JTextField(20);
		password = new JPasswordField(20);
		dbSourceName = new JLabel();

		JLabel line1 = new JLabel("Please choose one of the following databases for login:");
		connectionModel = new ConnectionComboBoxModel(sessionContext.getPlDotIni());
		connectionModel.addListDataListener(connListener);
		dbList = new JComboBox(connectionModel);
		dbList.addActionListener(new DBListListener());
		
		JLabel dbSourceName1 = new JLabel("Database source name:");

		connectionModel.setSelectedItem(sessionContext.getLastLoginDataSource());

		JButton connectionManagerButton = new JButton(connectionManagerAction);
		loginButton.addActionListener(loginAction);
		loginButton.setText("Login");
		if (connectionModel.getSelectedItem() == null) {
			loginButton.setEnabled(false);
		}
		JButton cancelButton = new JButton(cancelAction);

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

		progressBar.setVisible(false);
		pb.add(progressBar,cc.xyw(2,12,6));
		pb.add(bbBuilder.getPanel(), cc.xyw(2,16,7));

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
}