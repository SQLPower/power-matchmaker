package ca.sqlpower.matchmaker.swingui;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectException;

import com.jgoodies.forms.builder.ButtonStackBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;



public class DatabaseConnectionManager extends JDialog {

	private static Logger logger = Logger.getLogger(DatabaseConnectionManager.class);

	private List<ArchitectDataSource> connections;
	private Action helpAction = new AbstractAction(){

		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub

		}};

	private JPanel panel;

	private DatabaseConnectionManager() throws HeadlessException {
		super();
	}


	public DatabaseConnectionManager(List<ArchitectDataSource> connections) {
		this();
		this.connections = connections;
		setTitle("Database Connection Manager");
		panel = createPanel();
		getContentPane().add(panel);
		setModal(true);
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

		TableModel tm = new ConnectionTableModel();
		JTable connectionList = new JTable(tm);
		connectionList.setTableHeader(null);
		JScrollPane sp = new JScrollPane(connectionList);

		pb.add(sp, cc.xy(2, 4));

		ButtonStackBuilder bsb = new ButtonStackBuilder();

		bsb.addUnrelatedGap();
		JButton newButton = new JButton(helpAction);
		newButton.setText("New");
		bsb.addGridded(newButton);
		bsb.addRelatedGap();
		JButton editButton = new JButton(helpAction);
		editButton.setText("Edit");
		bsb.addGridded(editButton);
		bsb.addRelatedGap();
		JButton removeButton = new JButton(helpAction);
		removeButton.setText("Remove");
		bsb.addGridded(removeButton);


		bsb.addUnrelatedGap();
		JButton loginButton = new JButton(helpAction);
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

		JButton cancelButton = new JButton(new AbstractAction(){
			public void actionPerformed(ActionEvent e) {
				DatabaseConnectionManager.this.setVisible(false);
			}});
		cancelButton.setText("Exit");
		bsb.addGridded(cancelButton);
		bsb.addGlue();

		pb.add(bsb.getPanel(), cc.xy(4,4));
		return pb.getPanel();

	}

	private class ConnectionTableModel extends AbstractTableModel {

		public int getRowCount() {
			return connections.size();
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
			return connections.get(rowIndex);
		}
	}

	public static void main(String args[]) throws ArchitectException {

		final JDialog d = new DatabaseConnectionManager(
				MatchMakerFrame.getMainInstance().getUserSettings().getConnections());

		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		    	d.pack();
		    	d.setVisible(true);
		    }
		});
	}


}
