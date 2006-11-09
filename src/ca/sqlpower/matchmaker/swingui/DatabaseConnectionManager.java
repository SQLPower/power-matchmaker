package ca.sqlpower.matchmaker.swingui;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.DatabaseListChangeEvent;
import ca.sqlpower.architect.DatabaseListChangeListener;
import ca.sqlpower.architect.PlDotIni;
import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.architect.swingui.DBCSPanel;
import ca.sqlpower.architect.swingui.DBConnectionCallBack;
import ca.sqlpower.architect.swingui.action.DBCS_OkAction;
import ca.sqlpower.matchmaker.swingui.action.NewDatabaseConnectionAction;

import com.jgoodies.forms.builder.ButtonStackBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;


/** XXX: should use JDialog instead of extending it */
public class DatabaseConnectionManager extends JDialog
implements DBConnectionCallBack, DBConnectionUniDialog {

	private static Logger logger = Logger.getLogger(DatabaseConnectionManager.class);

	/**
	 * The Swing GUI session that owns this dialog.
	 */
	private MatchMakerMain swingMain;

	private JDialog newConnectionDialog;
	private JTable dsTable;
	private JPanel panel;
	private PlDotIni plDotIni;

	private Action helpAction = new AbstractAction(){

		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
		}};


	public synchronized JDialog getNewConnectionDialog() {
		return newConnectionDialog;
	}

	public synchronized void setNewConnectionDialog(JDialog d) {
		newConnectionDialog = d;
	}
	private NewDatabaseConnectionAction newDatabaseConnectionAction = new NewDatabaseConnectionAction("Add");


	private Action editDatabaseConnectionAction = new AbstractAction("Edit") {

		public void actionPerformed(ActionEvent e) {
			int selectedRow = dsTable.getSelectedRow();
			if ( selectedRow == -1 ) {
				return;
			}
			if (getNewConnectionDialog() != null && getNewConnectionDialog().isVisible()) {
				getNewConnectionDialog().requestFocus();
				return;
			}
			ArchitectDataSource dbcs = (ArchitectDataSource) dsTable.getValueAt(selectedRow,0);

			final DBCSPanel dbcsPanel = new DBCSPanel();
			dbcsPanel.setDbcs(dbcs);

			DBCS_OkAction okAction = new DBCS_OkAction(dbcsPanel,
					false,
					MatchMakerMain.getMainInstance().getUserSettings().getPlDotIni());
			okAction.setConnectionSelectionCallBack(DatabaseConnectionManager.this);

			Action cancelAction = new AbstractAction() {
				public void actionPerformed(ActionEvent evt) {
					dbcsPanel.discardChanges();
					setNewConnectionDialog(null);
				}
			};

			JDialog d = ArchitectPanelBuilder.createArchitectPanelDialog(
					dbcsPanel,
						// XXX Should pass parentFrame as a
						// Constructor arg to DatabaseConnectionManager constructor
						SwingUtilities.getWindowAncestor(
							DatabaseConnectionManager.this),
						"Edit Database Connection",
						ArchitectPanelBuilder.OK_BUTTON_LABEL,
						okAction, cancelAction);

			okAction.setConnectionDialog(d);
			setNewConnectionDialog(d);

			/* d.pack();*/
			d.setLocationRelativeTo(swingMain.getFrame());
			d.setVisible(true);
			logger.debug("Editting existing DBCS on panel: "+dbcs);
		}
	};

	private Action removeDatabaseConnectionAction = new AbstractAction("Remove") {

		public void actionPerformed(ActionEvent e) {
			int selectedRow = dsTable.getSelectedRow();
			if ( selectedRow == -1 ) {
				return;
			}
			ArchitectDataSource dbcs = (ArchitectDataSource) dsTable.getValueAt(selectedRow,0);
			int option = JOptionPane.showConfirmDialog(
					DatabaseConnectionManager.this,
					"Do you want to delete this database connection? ["+dbcs.getName()+"]",
					"Remove",
					JOptionPane.YES_NO_OPTION);
			if ( option != JOptionPane.YES_OPTION ) {
				return;
			}
			plDotIni.removeDataSource(dbcs);
		}
	};

	private Action loginDatabaseConnectionAction = new AbstractAction("Login") {

		public void actionPerformed(ActionEvent e) {
			int selectedRow = dsTable.getSelectedRow();
			if ( selectedRow == -1 ) {
				return;
			}
			ArchitectDataSource dbcs = (ArchitectDataSource) dsTable.getValueAt(selectedRow,0);
			closeAction.actionPerformed(null);
			LoginDialog l = new LoginDialog(swingMain);
			l.setDbSource(dbcs);
			l.pack();
	    	l.setVisible(true);
		}
	};

	private Action closeAction = new AbstractAction(){
		public void actionPerformed(ActionEvent e) {
			if ( getNewConnectionDialog() != null && getNewConnectionDialog().isVisible() )
				return;
			DatabaseConnectionManager.this.setVisible(false);
		}};


	private DatabaseConnectionManager(MatchMakerMain swingSession) throws HeadlessException {
		super(swingSession.getFrame());
		this.swingMain = swingSession;
		newDatabaseConnectionAction.setCallBack(this);
		newDatabaseConnectionAction.setComponentParent(this);
		newDatabaseConnectionAction.setParent(this);
	}

	public DatabaseConnectionManager(MatchMakerMain swingSession, PlDotIni plDotIni) {
		this(swingSession);
		this.plDotIni = plDotIni;
		setTitle("Database Connection Manager");
		panel = createPanel();
		getContentPane().add(panel);
	}

	private JPanel createPanel() {


		FormLayout layout = new FormLayout(
				"6dlu, fill:min(160dlu;default):grow, 6dlu, fill:min(50dlu;default), 6dlu", // columns
				" 6dlu,10dlu,6dlu,fill:min(180dlu;default):grow,10dlu"); // rows

		layout.setColumnGroups(new int [][] { {1,3,5}});
		CellConstraints cc = new CellConstraints();

		PanelBuilder pb;
		JPanel p = logger.isDebugEnabled()  ? new FormDebugPanel(layout) : new JPanel(layout);
		pb = new PanelBuilder(layout,p);
		pb.setDefaultDialogBorder();

		pb.add(new JLabel("Available Database Connection:"), cc.xy(2, 2));

		TableModel tm = new ConnectionTableModel(this.plDotIni);
		dsTable = new JTable(tm);
		dsTable.setTableHeader(null);
		dsTable.setShowGrid(false);
		dsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		dsTable.addMouseListener(new DSTableMouseListener());

		JScrollPane sp = new JScrollPane(dsTable);

		pb.add(sp, cc.xy(2, 4));

		ButtonStackBuilder bsb = new ButtonStackBuilder();

		JButton newButton = new JButton(newDatabaseConnectionAction);
		newButton.setText("New");
		bsb.addGridded(newButton);
		bsb.addRelatedGap();
		JButton editButton = new JButton(editDatabaseConnectionAction);
		editButton.setText("Edit");
		bsb.addGridded(editButton);
		bsb.addRelatedGap();
		JButton removeButton = new JButton(removeDatabaseConnectionAction);
		removeButton.setText("Remove");
		bsb.addGridded(removeButton);


		bsb.addUnrelatedGap();
		JButton loginButton = new JButton(loginDatabaseConnectionAction);
		loginButton.setText("Login");
		bsb.addGridded(loginButton);
		bsb.addRelatedGap();
		JButton auxLoginButton = new JButton(helpAction);
		auxLoginButton.setText("Aux Login");
		bsb.addGridded(auxLoginButton);

		bsb.addUnrelatedGap();
		JButton helpButton = new JButton(helpAction);
		helpButton.setText("Help");
		bsb.addGridded (helpButton);
		bsb.addRelatedGap();


		JButton cancelButton = new JButton(closeAction);
		cancelButton.setText("Close");
		bsb.addGridded(cancelButton);


		JComponent c = (JComponent) getRootPane();
		InputMap inputMap = c.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		ActionMap actionMap = c.getActionMap();

		inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), "cancel");
		actionMap.put("cancel", closeAction);


		pb.add(bsb.getPanel(), cc.xy(4,4));
		return pb.getPanel();

	}

	private class ConnectionTableModel extends AbstractTableModel {

		public ConnectionTableModel(PlDotIni ini) {
			super();
			if ( ini != null ) {
				ini.addDatabaseListChangeListener(new DatabaseListChangeListener(){
					public void databaseAdded(DatabaseListChangeEvent e) {
						fireTableDataChanged();
					}

					public void databaseRemoved(DatabaseListChangeEvent e) {
						fireTableDataChanged();
					}});

			}
		}

		public int getRowCount() {
			return plDotIni == null?0:plDotIni.getConnections().size();
		}

		public int getColumnCount() {
			return 1;
		}

		@Override
		public String getColumnName(int columnIndex) {
			return "Connection Name";
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return ArchitectDataSource.class;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			return plDotIni == null?null:plDotIni.getConnections().get(rowIndex);
		}

	}

	public void selectDBConnection(ArchitectDataSource ds) {
		for ( int i=0; i<dsTable.getRowCount(); i++ ) {
			if ( dsTable.getValueAt(i,0) == ds ) {
				dsTable.setRowSelectionInterval(i,i);
				dsTable.scrollRectToVisible(dsTable.getCellRect(i,0,true));
				break;
			}
		}
	}

	public PlDotIni getPlDotIni() {
		return plDotIni;
	}

	public void setPlDotIni(PlDotIni plDotIni) {
		this.plDotIni = plDotIni;
	}

	private class DSTableMouseListener implements MouseListener {

		public void mouseClicked(MouseEvent evt) {
            if (evt.getClickCount() == 2) {
            	editDatabaseConnectionAction.actionPerformed(null);
            }
        }

		public void mousePressed(MouseEvent e) {
			// we don't care
		}

		public void mouseReleased(MouseEvent e) {
			// we don't care
		}

		public void mouseEntered(MouseEvent e) {
			// we don't care
		}

		public void mouseExited(MouseEvent e) {
			// we don't care
		}

	}

}
