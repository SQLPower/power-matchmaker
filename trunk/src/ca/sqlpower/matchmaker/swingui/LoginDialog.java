package ca.sqlpower.matchmaker.swingui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ConnectionComboBoxModel;
import ca.sqlpower.architect.swingui.ListerProgressBarUpdater;
import ca.sqlpower.architect.swingui.Populator;
import ca.sqlpower.architect.swingui.SwingUserSettings;
import ca.sqlpower.matchmaker.prefs.PreferencesManager;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class LoginDialog extends JDialog {
    
	private static Logger logger = Logger.getLogger(LoginDialog.class);
	private JComboBox dbList;
	private JTextField userID;
	private JPasswordField password;
	private JLabel dbSourceName;
	protected JButton loginButton = new JButton();
	protected JProgressBar progressBar = new JProgressBar();
	protected ArchitectDataSource dbSource;
	private ConnectionComboBoxModel connectionModel;

	private JComponent panel;
	private Action helpLoginAction = new AbstractAction(){

		public void actionPerformed(ActionEvent e) {
			// XXX Hook up real help someday.
			JOptionPane.showMessageDialog(LoginDialog.this,
					"Help is not yet available. We apologize for the inconvenience");
		}};

	private ActionListener loginAction = new LoginAction();

	private class LoginAction extends Populator implements ActionListener {

		SQLDatabase db = null;
		boolean loginWasSuccessful = false;

        public void actionPerformed(ActionEvent e) {
            logger.debug("LoginAction.actionPerformed(): disabling login button");
            loginButton.setEnabled(false);
            if ( LoginDialog.this.dbSource == null ) {
                JOptionPane.showMessageDialog(LoginDialog.this,
                        "Please select a database connection first!",
                        "Unknown database connection",
                        JOptionPane.ERROR_MESSAGE);
                logger.debug("LoginAction.actionPerformed(): enabling login button (connection not specified)");
                loginButton.setEnabled(true);
                return;
            }

            //We create a copy of the data source and change the userID and password
            //and use that instead for the loginWasSuccessful.  We do not want to change the
            //default userID and password for the connection in here.
            ArchitectDataSource tempDbSource = new ArchitectDataSource(dbSource);
            tempDbSource.setUser(userID.getText());
            tempDbSource.setPass(new String(password.getPassword()));

            db = new SQLDatabase(tempDbSource);
            String driverClass = db.getDataSource().getDriverClass();
            if (driverClass == null || driverClass.length() == 0) {
                JOptionPane.showMessageDialog(LoginDialog.this,
                        "Datasource not configured (no JDBC Driver)",
                        "Database connection incomplete",
                        JOptionPane.ERROR_MESSAGE);
                logger.debug("LoginAction.actionPerformed(): enabling login button (connection has no driver class");
                loginButton.setEnabled(true);
                return;
            }
            try {
                ListerProgressBarUpdater progressBarUpdater =
                    new ListerProgressBarUpdater(progressBar, this);
                new javax.swing.Timer(100, progressBarUpdater).start();

                progressMonitor = db.getProgressMonitor();
                new Thread(this).start();
                // doStuff() will get invoked soon on the new thread
            } catch (ArchitectException e1) {
                ASUtils.showExceptionDialogNoReport(LoginDialog.this,
                        "Connection Error", e1 );
            }
        }

        @Override
        /** Called (once) by run() in superclass */
        public void doStuff() throws Exception {
            loginWasSuccessful = false;
            db.populate();
            loginWasSuccessful = true;
        }

		@Override
		public void cleanup() {
			if (getDoStuffException() != null) {
				ASUtils.showExceptionDialog("Login failed", getDoStuffException());
			} else if (db != null && db.isPopulated() && loginWasSuccessful) {
                // XXX Change this to fire an event so we don't need to know who's interested.
                // XXX this takes a while (~10 seconds) and during that time, the UI is frozen.
                MatchMakerFrame.getMainInstance().newLogin(db);
			    LoginDialog.this.setVisible(false);
			} else {
				JOptionPane.showMessageDialog(LoginDialog.this, "The login failed for an unknown reason.");
			}
            logger.debug("LoginAction.actionPerformed(): enabling login button (login has either failed or not; dialog might still be showing)");
			loginButton.setEnabled(true);
		}
	}

	private ListDataListener connListener = new ListDataListener(){

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

	public LoginDialog()
	{
		super(MatchMakerFrame.getMainInstance());
		setTitle("Database Connections");
		panel = createPanel();
		getContentPane().add(panel);
		setModal(true);
	}

	public JComponent createPanel() {

		FormLayout layout = new FormLayout(
				"4dlu,fill:min(70dlu;default), 4dlu, fill:min(50dlu;default):grow, min(20dlu;default), 4dlu, min(15dlu;default), fill:min, 4dlu", // columns
				" 10dlu,pref,4dlu,pref,10dlu,pref,4dlu,pref,4dlu,pref,10dlu,pref,10dlu,pref,10dlu,pref,10dlu"); // rows

		layout.setColumnGroups(new int [][] { {2,5},{4,7}});
		CellConstraints cc = new CellConstraints();

		JLabel userIDLabel = new JLabel("User ID:");
		JLabel passwordLabel = new JLabel("Password:");

		userID = new JTextField(20);
		password = new JPasswordField(20);
		dbSourceName = new JLabel();

		JLabel line1 = new JLabel("Please choose one of the following databases for login:");
		connectionModel = new ConnectionComboBoxModel(MatchMakerFrame.getMainInstance().getUserSettings().getPlDotIni());
		connectionModel.addListDataListener(connListener);
		dbList = new JComboBox(connectionModel);
		JLabel dbSourceName1 = new JLabel("Database source name:");

		Preferences pref = PreferencesManager.getDefaultInstance().getRootNode();
		String lastLogin = pref.get(SwingUserSettings.LAST_LOGIN_DATA_SOURCE,null);
		if ( lastLogin != null ) {
			connectionModel.setSelectedItem(lastLogin);
		}
		ButtonBarBuilder bbBuilder = new ButtonBarBuilder();
		JButton helpLoginButton = new JButton(helpLoginAction);
		helpLoginButton.setText("Help");
		bbBuilder.addGridded (helpLoginButton);
		bbBuilder.addRelatedGap();
		bbBuilder.addGlue();


		loginButton.addActionListener(loginAction);
		loginButton.setText("Login");
		JButton cancelButton = new JButton(new AbstractAction(){
			public void actionPerformed(ActionEvent e) {
				LoginDialog.this.setVisible(false);
			}});
		cancelButton.setText("Cancel");
		bbBuilder.addGridded(loginButton);
		bbBuilder.addUnrelatedGap();
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

		return pb.getPanel();
	}

	public JPanel getPanel() {
		return (JPanel) panel;
	}

	public void setPanel(JPanel panel) {
		this.panel = panel;
	}


	/**
	 * This is just for testing; the real main class is in MatchMakerFrame.
	 */
	public static void main(String args[]) throws ArchitectException {

		final JDialog d = new LoginDialog();

		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		    	d.pack();
		    	d.setVisible(true);
		    }
		});
	}

	public ArchitectDataSource getDbSource() {
		return dbSource;
	}

	public void setDbSource(ArchitectDataSource dbSource) {
		connectionModel.setSelectedItem(dbSource);
	}

}