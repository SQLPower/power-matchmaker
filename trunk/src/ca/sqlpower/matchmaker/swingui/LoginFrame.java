package ca.sqlpower.matchmaker.swingui;





import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ConnectionComboBoxModel;
import ca.sqlpower.architect.swingui.ListerProgressBarUpdater;
import ca.sqlpower.architect.swingui.Populator;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class LoginFrame extends JDialog {

	private static Logger logger = Logger.getLogger(LoginFrame.class);
	private JComboBox dbList;
	private JTextField userID;
	private JPasswordField password;
	private JCheckBox refreshDB;
	private JCheckBox refreshPL;
	private JLabel dbSourceName;
	protected JButton loginButton = new JButton();
	protected JProgressBar progressBar = new JProgressBar();
	protected ArchitectDataSource dbSource;

	private JComponent panel;
	private Action helpLoginAction = new AbstractAction(){

		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub

		}};

	private ActionListener loginAction = new LoginAction();

	private class LoginAction extends Populator implements ActionListener {

		SQLDatabase db = null;
		boolean login = false;

		@Override
		public void cleanup() {
			progressBar.setVisible(false);
			if ( db != null && db.isPopulated() && login ) {
				SQLTable defParam = null;
				SQLColumn schemaVersionCol = null;
				try {

					defParam = db.getTableByName("DEF_PARAM");
					schemaVersionCol = defParam.getColumnByName("SCHEMA_VERSION");

					String ver = null;
			        Statement stmt = null;
			        ResultSet rs = null;
			        try {
			            stmt = db.getConnection().createStatement();
			            String defParamTableName = DDLUtils.toQualifiedName(
			            		defParam.getCatalogName(),
			            		defParam.getSchemaName(),
			            		defParam.getName());
			            StringBuffer sql = new StringBuffer();
			            sql.append("SELECT ");
			            sql.append(schemaVersionCol.getName());
			            sql.append(" FROM ");
			            sql.append(defParamTableName);
			            rs = stmt.executeQuery(sql.toString());

			            if (rs.next()) {
			            	 ver = rs.getString(1);
			            }

			            logger.debug("connected to "+db.getName()+
			            		"  schema version: " + ver );
			            		LoginFrame.this.setVisible(false);
			        } catch (SQLException e) {
			            throw new ArchitectException("could not read def_param",e);
			        } finally {
			        	try {
			        		if (rs != null)
			        			rs.close();
			        		if (stmt != null)
			        			stmt.close();
			        	} catch (SQLException e) {
			        		e.printStackTrace();
			        	}

			        }


				} catch (ArchitectException e) {
					ASUtils.showExceptionDialogNoReport(LoginFrame.this,
							"Pl Schema Access Error", e );
				} finally {
					loginButton.setEnabled(true);
				}
			}
			loginButton.setEnabled(true);
		}

		@Override
		public void doStuff() throws Exception {
			loginButton.setEnabled(false);
			login = false;
			try {
				ListerProgressBarUpdater progressBarUpdater =
					new ListerProgressBarUpdater(progressBar, this);
				new javax.swing.Timer(100, progressBarUpdater).start();
				db.populate();
				login = true;
			} catch (ArchitectException e) {
				logger.debug(
					"Unexpected exception in ConnectionListener",	e);
				ASUtils.showExceptionDialogNoReport(LoginFrame.this,
						"Unexpected exception in ConnectionListener",
						e );
                login = false;
			}
		}

		public void actionPerformed(ActionEvent e) {
			loginButton.setEnabled(false);
			if ( LoginFrame.this.dbSource == null ) {
				JOptionPane.showMessageDialog(LoginFrame.this,
						"Please select a database connection first!",
						"Unknown database connection",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			db = new SQLDatabase(dbSource);
			try {
				progressMonitor = db.getProgressMonitor();
				new Thread(this).start();
			} catch (ArchitectException e1) {
				ASUtils.showExceptionDialogNoReport(LoginFrame.this,
						"Connection Error", e1 );
			} finally {
			}

		}

	}



	public LoginFrame()
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
				" 10dlu,10dlu,4dlu,12dlu,10dlu,12dlu,4dlu,12dlu,4dlu,12dlu,10dlu,10dlu,10dlu,10dlu,10dlu,20dlu,10dlu"); // rows

		layout.setColumnGroups(new int [][] { {2,5},{4,7}});

		CellConstraints cc = new CellConstraints();
		JLabel line1 = new JLabel("Please choose one of the following databases for login:");
		ConnectionComboBoxModel connectionModel = new ConnectionComboBoxModel(MatchMakerFrame.getMainInstance().getUserSettings().getPlDotIni());
		connectionModel.addListDataListener(new ListDataListener(){

			public void intervalAdded(ListDataEvent e) {
			}

			public void intervalRemoved(ListDataEvent e) {
			}

			public void contentsChanged(ListDataEvent e) {
				ArchitectDataSource dbSource = (ArchitectDataSource) ((ConnectionComboBoxModel) (e.getSource())).getSelectedItem();
				dbSourceName.setText(dbSource.getName());
				userID.setText(dbSource.getUser());
				password.setText(dbSource.getPass());
				LoginFrame.this.dbSource = dbSource;
			}});
		dbList = new JComboBox(connectionModel);
		JLabel dbSourceName1 = new JLabel("Database source name:");


		JLabel userIDLabel = new JLabel("User ID:");
		JLabel passwordLabel = new JLabel("Password:");

		userID = new JTextField(20);
		password = new JPasswordField(20);
		refreshDB = new JCheckBox();
		refreshPL = new JCheckBox();
		dbSourceName = new JLabel();


		ButtonBarBuilder bbBuilder = new ButtonBarBuilder();
		JButton helpLoginButton = new JButton(helpLoginAction );
		helpLoginButton.setText("Help");
		bbBuilder.addGridded (helpLoginButton);
		bbBuilder.addRelatedGap();
		bbBuilder.addGlue();


		loginButton.addActionListener(loginAction);
		loginButton.setText("Login");
		JButton cancelButton = new JButton(new AbstractAction(){
			public void actionPerformed(ActionEvent e) {
				LoginFrame.this.setVisible(false);
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
		pb.add(new JLabel("Refresh DB Schema"), cc.xy(2, 14,"r,c"));
		pb.add(refreshDB,cc.xy(4,14));
		pb.add(new JLabel("Refresh PL Schema"), cc.xy(5, 14, "r,c"));
		pb.add(refreshPL,cc.xy(7,14));
		pb.add(bbBuilder.getPanel(), cc.xyw(2,16,7));

		return pb.getPanel();
	}

	public JPanel getPanel() {
		return (JPanel) panel;
	}

	public void setPanel(JPanel panel) {
		this.panel = panel;
	}


	public static void main(String args[]) throws ArchitectException {

		final JDialog d = new LoginFrame();

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

}
