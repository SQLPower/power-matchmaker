package ca.sqlpower.matchmaker.swingui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

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
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ConnectionComboBoxModel;
import ca.sqlpower.architect.swingui.ListerProgressBarUpdater;
import ca.sqlpower.architect.swingui.Populator;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class LoginDialog {

	private static Logger logger = Logger.getLogger(LoginDialog.class);

    private class LoginAction extends Populator implements ActionListener {

        boolean loginWasSuccessful = false;

        public void actionPerformed(ActionEvent e) {
            logger.debug("LoginAction.actionPerformed(): disabling login button");
            loginButton.setEnabled(false);
            if (dbSource == null) {
                JOptionPane.showMessageDialog(frame,
                        "Please select a database connection first!",
                        "Unknown database connection",
                        JOptionPane.ERROR_MESSAGE);
                logger.debug("LoginAction.actionPerformed(): enabling login button (connection not specified)");
                loginButton.setEnabled(true);
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
                return;
            }

            try {
                session = sessionContext.createSession(dbSource,
                        userID.getText(), new String(password.getPassword()));
                ListerProgressBarUpdater progressBarUpdater =
                    new ListerProgressBarUpdater(progressBar, this);
                new javax.swing.Timer(100, progressBarUpdater).start();

                progressMonitor = session.getDatabase().getProgressMonitor();
                new Thread(this).start();
                // doStuff() will get invoked soon on the new thread
            } catch (Exception ex) {
                ASUtils.showExceptionDialogNoReport(frame,
                        "Connection Error", ex );
                loginButton.setEnabled(true);
            }
        }

        @Override
        /** Called (once) by run() in superclass */
        public void doStuff() throws Exception {
            loginWasSuccessful = false;
            session.getDatabase().populate();
            loginWasSuccessful = true;
        }

        @Override
        public void cleanup() {
            try {
                if (getDoStuffException() != null) {
                    ASUtils.showExceptionDialog("Login failed", getDoStuffException());
                } else if (
                        session != null &&
                        session.getDatabase() != null &&
                        session.getDatabase().isPopulated() &&
                        loginWasSuccessful) {
                    sessionContext.setLastLoginDataSource(dbSource);
                    session.showGUI();
                    frame.dispose();
                } else {
                    JOptionPane.showMessageDialog(frame, "The login failed for an unknown reason.");
                }
            } finally {
                logger.debug("LoginAction.actionPerformed(): enabling login button (login has either failed or not; dialog might still be showing)");
                loginButton.setEnabled(true);
            }
        }
    }

    private final Action cancelAction = new AbstractAction("Cancel") {
        public void actionPerformed(ActionEvent e) {
            frame.dispose();
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
    private ArchitectDataSource dbSource;
	private ConnectionComboBoxModel connectionModel;
	private JComponent panel;

    private ActionListener loginAction = new LoginAction();

	private ListDataListener connListener = new ListDataListener() {

		public void intervalAdded(ListDataEvent e) {
		}

		public void intervalRemoved(ListDataEvent e) {
		}

		public void contentsChanged(ListDataEvent e) {
		    if ( e.getType() == ListDataEvent.CONTENTS_CHANGED ) {
		        ArchitectDataSource dbSource = (ArchitectDataSource) ((ConnectionComboBoxModel) (e.getSource())).getSelectedItem();
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
        ASUtils.makeJDialogCancellable(frame, cancelAction);
        frame.addWindowListener(optimizationManager);
	}

    /**
     * Makes this login dialog visible, packing it and centering it on the screen.
     * You will normally not call this method directly; see 
     * {@link SwingSessionContext#showLoginDialog(ArchitectDataSource)}.
     * 
     * @param selectedDataSource The data source to default the datasource combo box,
     * username, and password fields to. 
     */
    void showLoginDialog(ArchitectDataSource selectedDataSource) {
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
		JLabel dbSourceName1 = new JLabel("Database source name:");

		connectionModel.setSelectedItem(sessionContext.getLastLoginDataSource());

		JButton connectionManagerButton = new JButton(connectionManagerAction);
		loginButton.addActionListener(loginAction);
		loginButton.setText("Login");
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

	public ArchitectDataSource getDbSource() {
		return dbSource;
	}

	public void setDbSource(ArchitectDataSource dbSource) {
		connectionModel.setSelectedItem(dbSource);
	}

}