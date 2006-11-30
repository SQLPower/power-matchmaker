package ca.sqlpower.matchmaker.swingui;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.DataSourceCollection;
import ca.sqlpower.architect.DatabaseListChangeEvent;
import ca.sqlpower.architect.DatabaseListChangeListener;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.architect.swingui.DBCSPanel;
import ca.sqlpower.architect.swingui.DBConnectionCallBack;
import ca.sqlpower.architect.swingui.action.DBCSOkAction;
import ca.sqlpower.matchmaker.swingui.action.NewDatabaseConnectionAction;

import com.jgoodies.forms.builder.ButtonStackBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;


/**
 * You won't need to create one of these on your own.
 * Use {@link SwingSessionContextImpl#showDatabaseConnectionManager()}.
 * 
 */
public class DatabaseConnectionManager
implements DBConnectionCallBack, DBConnectionUniDialog {

	private static Logger logger = Logger.getLogger(DatabaseConnectionManager.class);

	/**
	 * The session context that this dialog is managing connection properties for.
	 */
	private final SwingSessionContextImpl sessionContext;
	private final NewDatabaseConnectionAction newDatabaseConnectionAction;
    private final JPanel panel;
    
    /**
     * The Dialog that contains all the GUI;
     */
    private JDialog d;

    /**
     * The current owner of the dialog.  Gets updated in the showDialog() method.
     */
    private Window currentOwner;

	private final Action helpAction = new AbstractAction("Help"){

		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
            JOptionPane.showMessageDialog(d, "Help is not available yet.");
		}
	};
	
	private final Action auxLoginAction = new AbstractAction("Aux Login"){

		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
            JOptionPane.showMessageDialog(d, 
            		"This action is not implemented yet.");
		}
	};
	
	private final Action jdbcDriversAction = new AbstractAction("JDBC Drivers"){

		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
            JOptionPane.showMessageDialog(d, 
            		"This action is not implemented yet.");
		}
	};

	private final Action editDatabaseConnectionAction = new AbstractAction("Edit") {

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

            // This is super-ugly.  The DBCSOkAction should rot in hell.
            // I don't know what it's for, so I'm invoking it anyway before
            // saving the pl.ini file
			final DBCSOkAction dbcsOkAction = new DBCSOkAction(
                    dbcsPanel,
			        false,
			        sessionContext.getPlDotIni());
			dbcsOkAction.setConnectionSelectionCallBack(DatabaseConnectionManager.this);
            dbcsOkAction.setConnectionDialog(d);
			Action okAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        dbcsOkAction.actionPerformed(e);
                        plDotIni.write();
                    } catch (Exception ex) {
                        ASUtils.showExceptionDialog("Could not save PL.INI file", ex);
                    }
                }
            };
            

			Action cancelAction = new AbstractAction() {
				public void actionPerformed(ActionEvent evt) {
					dbcsPanel.discardChanges();
					setNewConnectionDialog(null);
				}
			};

			JDialog dialog = ArchitectPanelBuilder.createArchitectPanelDialog(
					dbcsPanel,
					d,
					"Edit Database Connection",
					ArchitectPanelBuilder.OK_BUTTON_LABEL,
					okAction, cancelAction);

			setNewConnectionDialog(d);

            dialog.pack();
			dialog.setLocationRelativeTo(d);
			dialog.setVisible(true);
			logger.debug("Editting existing DBCS on panel: "+dbcs);
		}
	};

	private final Action removeDatabaseConnectionAction = new AbstractAction("Remove") {

		public void actionPerformed(ActionEvent e) {
			int selectedRow = dsTable.getSelectedRow();
			if ( selectedRow == -1 ) {
				return;
			}
			ArchitectDataSource dbcs = (ArchitectDataSource) dsTable.getValueAt(selectedRow,0);
			int option = JOptionPane.showConfirmDialog(
					d,
					"Do you want to delete this database connection? ["+dbcs.getName()+"]",
					"Remove",
					JOptionPane.YES_NO_OPTION);
			if ( option != JOptionPane.YES_OPTION ) {
				return;
			}
			plDotIni.removeDataSource(dbcs);
		}
	};

	private final Action loginDatabaseConnectionAction = new AbstractAction("Login") {

		public void actionPerformed(ActionEvent e) {
			int selectedRow = dsTable.getSelectedRow();
			if ( selectedRow == -1 ) {
				return;
			}
			ArchitectDataSource dbcs = (ArchitectDataSource) dsTable.getValueAt(selectedRow,0);
			closeAction.actionPerformed(null);
            sessionContext.showLoginDialog(dbcs);
		}
	};

	private final Action closeAction = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			if ( getNewConnectionDialog() != null && getNewConnectionDialog().isVisible() )
				return;
			d.dispose();
		}
	};

	private JDialog newConnectionDialog;
	private JTable dsTable;
	private DataSourceCollection plDotIni;

	public DatabaseConnectionManager(SwingSessionContextImpl context) {
        this.sessionContext = context;
        this.plDotIni = context.getPlDotIni();
        newDatabaseConnectionAction = new NewDatabaseConnectionAction(sessionContext, "Add");
        newDatabaseConnectionAction.setCallBack(this);
        newDatabaseConnectionAction.setComponentParent(d);
        newDatabaseConnectionAction.setParent(this);
		panel = createPanel();
	}

    
    /**
     * Makes sure this database connection manager is visible,
     * focused, and in a dialog owned by the given owner.
     *
     * @param owner the Frame or Dialog that should own the
     *              DatabaseConnectionManager dialog.
     */
    public void showDialog(Window owner) {
        if (d != null && d.isVisible() && currentOwner == owner) {
            d.requestFocus();
            return;
        }
        
        if (d != null) {
            d.dispose();
        }
        if (panel.getParent() != null) {
            panel.getParent().remove(panel);
        }
        if (owner instanceof Dialog) {
            d = new JDialog((Dialog) owner);
        } else if (owner instanceof Frame) {
            d = new JDialog((Frame) owner);
        } else {
            throw new IllegalArgumentException(
                    "Owner has to be a Frame or Dialog.  You provided a " +
                    (owner == null ? null : owner.getClass().getName()));
        }
        
        currentOwner = owner;
        d.setTitle("Database Connection Manager");
        d.getContentPane().add(panel);  
        d.pack();
        d.setLocationRelativeTo(owner);
        ASUtils.makeJDialogCancellable(d, closeAction);
        d.setVisible(true);
        d.requestFocus();
    }
    
	private JPanel createPanel() {

		FormLayout layout = new FormLayout(
				"6dlu, fill:min(160dlu;default):grow, 6dlu, pref, 6dlu", // columns
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
		JButton jdbcDriversButton = new JButton(jdbcDriversAction);
		bsb.addGridded(jdbcDriversButton);

		bsb.addUnrelatedGap();
		JButton loginButton = new JButton(loginDatabaseConnectionAction);
		loginButton.setText("Login");
		bsb.addGridded(loginButton);
		bsb.addRelatedGap();
		JButton auxLoginButton = new JButton(auxLoginAction);
		auxLoginButton.setText("Aux Login");
		bsb.addGridded(auxLoginButton);

		bsb.addUnrelatedGap();
		JButton helpButton = new JButton(helpAction);
		helpButton.setText("Help");
		bsb.addGridded(helpButton);
		bsb.addRelatedGap();

		JButton cancelButton = new JButton(closeAction);
		cancelButton.setText("Close");
		bsb.addGridded(cancelButton);

		pb.add(bsb.getPanel(), cc.xy(4,4));
		return pb.getPanel();

	}

	private class ConnectionTableModel extends AbstractTableModel {

		public ConnectionTableModel(DataSourceCollection ini) {
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

    public synchronized JDialog getNewConnectionDialog() {
        return newConnectionDialog;
    }

    public synchronized void setNewConnectionDialog(JDialog d) {
        newConnectionDialog = d;
    }
    
	public DataSourceCollection getPlDotIni() {
		return plDotIni;
	}

	public void setPlDotIni(DataSourceCollection plDotIni) {
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
