package ca.sqlpower.matchmaker.swingui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
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



public class DatabaseConnectionManager extends JFrame {

	private static Logger logger = Logger.getLogger(DatabaseConnectionManager.class);

	private List<ArchitectDataSource> connections;
	private Action helpAction = new AbstractAction(){

		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub

		}};

	private DatabaseConnectionManager() throws HeadlessException {
		super();
		buildUI();
	}


	public DatabaseConnectionManager(List<ArchitectDataSource> connections) {
		this();
		this.connections = connections;
	}

	private void buildUI() {

		JPanel space1 = new JPanel();
		space1.setPreferredSize(new Dimension(40,30));
		JPanel space2 = new JPanel();
		space2.setPreferredSize(new Dimension(40,30));
		JPanel space3 = new JPanel();
		space3.setPreferredSize(new Dimension(40,30));

		setLayout(new BorderLayout());
		setTitle("Database Connection Manager");

		JPanel tableView = new JPanel(new BorderLayout());
		tableView.add(space1,BorderLayout.NORTH);
		tableView.add(new JLabel("Available Database Connection:"),BorderLayout.CENTER);
		TableModel tm = new ConnectionTableModel();
		JTable connectionList = new JTable(tm);
		connectionList.setTableHeader(null);
		JScrollPane sp = new JScrollPane(connectionList);

		tableView.add(sp,BorderLayout.SOUTH);
		//getContentPane().add(tableView);
		add(tableView, BorderLayout.WEST);

		add(space2,BorderLayout.CENTER);
		add(space3,BorderLayout.SOUTH);


		ButtonStackBuilder bsb = new ButtonStackBuilder();


		bsb.addUnrelatedGap();
		bsb.addUnrelatedGap();
		bsb.addUnrelatedGap();
		JButton newButton = new JButton(helpAction);
		newButton.setText("New");
		bsb.addGridded(newButton);
		bsb.addUnrelatedGap();
		bsb.addGlue();
		JButton editButton = new JButton(helpAction);
		editButton.setText("Edit");
		bsb.addGridded(editButton);
		bsb.addUnrelatedGap();
		bsb.addGlue();
		JButton removeButton = new JButton(helpAction);
		removeButton.setText("Remove");
		bsb.addGridded(removeButton);
		bsb.addGlue();

		bsb.addRelatedGap();
		JButton loginButton = new JButton(helpAction);
		loginButton.setText("Login");
		bsb.addGridded(loginButton);
		bsb.addUnrelatedGap();
		bsb.addGlue();
		JButton auxLoginButton = new JButton(helpAction);
		auxLoginButton.setText("Aux Login");
		bsb.addGridded(auxLoginButton);
		bsb.addGlue();

		bsb.addRelatedGap();
		JButton helpButton = new JButton(helpAction);
		helpButton.setText("Help");
		bsb.addGridded (helpButton);
		bsb.addUnrelatedGap();
		bsb.addGlue();

		JButton cancelButton = new JButton(new AbstractAction(){
			public void actionPerformed(ActionEvent e) {
				DatabaseConnectionManager.this.setVisible(false);
			}});
		cancelButton.setText("Exit");
		bsb.addRelatedGap();
		bsb.addGridded(cancelButton);

		add(bsb.getPanel(), BorderLayout.EAST);
	//	getContentPane().add(bsb.getPanel());
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

		final JFrame d = new DatabaseConnectionManager(
				MatchMakerFrame.getMainInstance().getUserSettings().getConnections());

		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		    	d.pack();
		    	d.setVisible(true);
		    }
		});
	}


}
