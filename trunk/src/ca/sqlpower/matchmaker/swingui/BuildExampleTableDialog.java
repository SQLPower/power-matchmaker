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

import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.ddl.DDLStatement;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.swingui.ProgressWatcher;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.SPSwingWorker;
import ca.sqlpower.swingui.SwingWorkerRegistry;
import ca.sqlpower.swingui.SPSUtils.FileExtensionFilter;
import ca.sqlpower.util.Monitorable;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;


/**
 * This is the dialog that lets you create an example table for use with the user guide example.
 */
public class BuildExampleTableDialog extends JDialog{
	
	private static Logger logger = Logger.getLogger(BuildExampleTableDialog.class);
	
	/**
	 * The number of records to create
	 */
	private static final int NUM_RECORDS = 2000;
	
	/**
	 * The current swing session
	 */
	private MatchMakerSwingSession swingSession;
	
	/**
	 * The Table location chooser
	 */
	private SQLObjectChooser sourceChooser;
	
	/**
	 * Holds the name of the table
	 */
	private JTextField tableName;
	
	/**
	 * The table to be created
	 */
	private SQLTable table;
	
	/**
	 * the progress bar for populating
	 */
	private JProgressBar progress;
	
	/**
	 * The watcher that updates up the progress bar
	 */
	private ProgressWatcher pw;
	
	/**
	 * The monitor the progress watcher uses 
	 */
	private PopulationMonitor popMon;
	
	/**
	 * The create button
	 */
	private JButton create;
	
	/**
	 * The close button
	 */
	private JButton cancel;
	
	/**
	 * The list of parsed first names taken from the array at the bottom.
	 */
	private List<String> firstNames = new ArrayList<String>();
	/**
	 * The list of parsed last names taken from the array at the bottom.
	 */
	private List<String> lastNames = new ArrayList<String>();

	/**
	 * Creates a new Dialog
	 */
	public BuildExampleTableDialog(MatchMakerSwingSession swingSession) {
		super(swingSession.getFrame(),"Create Example Table");
		this.swingSession = swingSession;
		sourceChooser = new SQLObjectChooser(swingSession, this);
		tableName = new JTextField(30);
		tableName.setText("MMExampleTable");
		buildGUI();
		setModal(false);
	}
	
	/**
	 * Builds the gui for this dialog
	 */
	private void buildGUI() {	
		JPanel panel;
		if (logger.isDebugEnabled()) {
			panel = new FormDebugPanel();
		} else {
			panel = new JPanel();
		}
		
		FormLayout layout = new FormLayout(	"4dlu,pref,4dlu,pref:grow,4dlu" , 
											"4dlu,pref:grow,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu");
		panel.setLayout(layout);
		
		CellConstraints cc = new CellConstraints();
		
		int row = 2;
		panel.add(new JLabel("Note: If you cannot find the newly created table you may need to restart the application."), cc.xyw(2, row,3));
				
		row += 2;
		panel.add(new JLabel("Example Table Location:"), cc.xyw(2, row, 3));
		
		row += 2;
		panel.add(new JLabel("Data Source:"), cc.xy(2, row));
		panel.add(sourceChooser.getDataSourceComboBox(),cc.xy(4, row));
		
		
		row += 2;
		panel.add(new JLabel("Catalog:"), cc.xy(2, row));
		panel.add(sourceChooser.getCatalogComboBox(),cc.xy(4, row));
		
		row += 2;
		panel.add(new JLabel("Schema:"), cc.xy(2, row));
		panel.add(sourceChooser.getSchemaComboBox(),cc.xy(4, row));
		
		row += 2;
		panel.add(new JLabel("Table:"), cc.xy(2, row));
		panel.add(tableName,cc.xy(4, row));
		
		row += 2;
		cancel = new JButton(new AbstractAction("Close"){
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		
		create = new JButton( new AbstractAction("Create"){
			public void actionPerformed(ActionEvent e) {
				try {
					generateTableSQL();
				} catch (Exception ex) {
					SPSUtils.showExceptionDialogNoReport(BuildExampleTableDialog.this,
							"Error while trying to create example table", ex);
				}
			}
		});
		
		panel.add(ButtonBarFactory.buildOKCancelBar(create, cancel), cc.xyw(2, row, 3));
		
		row += 2;
		progress = new JProgressBar(0, NUM_RECORDS);
		popMon = new PopulationMonitor(NUM_RECORDS);
		pw = new ProgressWatcher(progress, popMon);
		
		panel.add(progress, cc.xyw(2, row, 3));
		
		setLocationRelativeTo(swingSession.getFrame());
		setContentPane(panel);
		pack();
	}
	
    /**
	 * Creates and shows a dialog with the generated SQL for the
	 * result table in it. The dialog has buttons with actions 
	 * that can save, execute, or copy the SQL to the clipboard.
	 */
	public void generateTableSQL()
		throws InstantiationException, IllegalAccessException,
		HeadlessException, SQLException, ArchitectException, ClassNotFoundException {

		final DDLGenerator ddlg = DDLUtils.createDDLGenerator(getDataSource());
		if (ddlg == null) {
			JOptionPane.showMessageDialog(swingSession.getFrame(),
					"Couldn't create DDL Generator for database type\n"+
					getDataSource().getDriverClass());
			return;
		}
		
		
		
		ddlg.setTargetCatalog(getTableCatalog());
		ddlg.setTargetSchema(getTableSchema());
		
		table = swingSession.getDatabase(getDataSource()).getTableByName(getTableCatalog(), getTableSchema(), tableName.getText());
		System.out.println(table);
		
		
		if (swingSession.tableExists(table)) {
			int answer = JOptionPane.showConfirmDialog(swingSession.getFrame(),
					"Example table exists, do you want to drop and recreate it?",
					"Table exists",
					JOptionPane.YES_NO_OPTION);
			if ( answer != JOptionPane.YES_OPTION ) {
				return;
			}
			ddlg.dropTable(table);
			
			while (table.getColumns().size() != 0) {
				table.removeColumn(0);
			}
		} else {
			table = ArchitectUtils.addSimulatedTable(swingSession.getDatabase(getDataSource()), getTableCatalog(), getTableSchema(), tableName.getText());
		}
		
		SQLColumn id = new SQLColumn(table,"ID",Types.INTEGER,10,0);
		id.setPrimaryKeySeq(0);
		
		table.addColumn(id);
		table.addColumn(new SQLColumn(table,"FirstName",Types.VARCHAR,100,0));
		table.addColumn(new SQLColumn(table,"LastName",Types.VARCHAR,100,0));
		table.addColumn(new SQLColumn(table, "Email", Types.VARCHAR,100,0));
		table.addColumn(new SQLColumn(table, "Address", Types.VARCHAR,100,0));
		table.addColumn(new SQLColumn(table, "HomePhone", Types.VARCHAR,100,0));
		table.addColumn(new SQLColumn(table, "CellPhone", Types.VARCHAR,100,0));
		
		ddlg.addTable(table);
		
		

	    final JDialog editor = new JDialog(swingSession.getFrame(),
	    		"Create Example Table", true);
	    JComponent cp = (JComponent) editor.getContentPane();

	    Box statementsBox = Box.createVerticalBox();
	    final List<JTextArea> sqlTextFields = new ArrayList<JTextArea>();
	    for (DDLStatement sqlStatement : ddlg.getDdlStatements()) {
	    	final JTextArea sqlTextArea = new JTextArea(sqlStatement.getSQLText());
			statementsBox.add(sqlTextArea);
			sqlTextFields.add(sqlTextArea);
	    }

	    Action saveAction = new AbstractAction("Save") {
			public void actionPerformed(ActionEvent e) {
				AbstractDocument doc = new DefaultStyledDocument();
				for (JTextArea sqlText : sqlTextFields ) {
			    	try {
						doc.insertString(doc.getLength(),
										sqlText.getText(),
										null);
						doc.insertString(doc.getLength(),";\n",null);
					} catch (BadLocationException e1) {
						SPSUtils.showExceptionDialogNoReport(editor, "Unexcepted Document Error",e1);
					}
			    }
				SPSUtils.saveDocument(swingSession.getFrame(),
						doc,
						(FileExtensionFilter)SPSUtils.SQL_FILE_FILTER);
			}
	    };
	    Action copyAction = new AbstractAction("Copy to Clipboard") {
			public void actionPerformed(ActionEvent e) {
				StringBuffer buf = new StringBuffer();
				for (JTextArea sqlText : sqlTextFields ) {
					buf.append(sqlText.getText());
					buf.append(";\n");
			    }
				StringSelection selection = new StringSelection(buf.toString());
				Clipboard clipboard = Toolkit.getDefaultToolkit()
						.getSystemClipboard();
				clipboard.setContents(selection, selection);
			}
	    };
	    Action executeAction = new AbstractAction("Execute") {
			public void actionPerformed(ActionEvent e) {

				Connection con = null;
				Statement stmt = null;
				String sql = null;
				try {
					con = swingSession.getDatabase(getDataSource()).getConnection();
					stmt = con.createStatement();
					int successCount = 0;

					for (JTextArea sqlText : sqlTextFields ) {
						sql = sqlText.getText();
						try {
							stmt.executeUpdate(sql);
							successCount += 1;
						} catch (SQLException e1) {
							int choice = JOptionPane.showOptionDialog(editor,
									"The following SQL statement failed:\n" +
									sql +
									"\nThe error was: " + e1.getMessage() +
									"\n\nDo you want to continue executing the create script?",
									"SQL Error", JOptionPane.YES_NO_OPTION,
									JOptionPane.ERROR_MESSAGE, null,
									new String[] {"Abort", "Continue"}, "Continue" );
							if (choice != 1) {
								break;
							}
						}
					}

					JOptionPane.showMessageDialog(swingSession.getFrame(),
							"Successfully executed " + successCount + " of " +
							sqlTextFields.size() + " SQL Statements." +
							(successCount == 0 ? "\n\nBetter Luck Next Time." : ""));

                    //closes the dialog if all the statement is executed successfully
                    //if not, the dialog remains on the screen
                    if (successCount == sqlTextFields.size()){
					    editor.dispose();
					    //start the thread to populate the table.
					    new Thread(new PopulateTableWorker(swingSession, table)).start();
                    }
				} catch (SQLException ex) {
					SPSUtils.showExceptionDialogNoReport(editor,
							"Create Script Failure", ex);
				} catch (ArchitectException ex) {
					SPSUtils.showExceptionDialogNoReport(editor, 
							"Error Generating Example table", ex);
				} finally {
                    try {
                        if (stmt != null) {
                            stmt.close();
                        }
                    } catch (SQLException ex) {
                        logger.warn("Couldn't close statement", ex);
                    }
                    try {
                        if (con != null) {
                            con.close();
                        }
                    } catch (SQLException ex) {
                        logger.warn("Couldn't close connection", ex);
                    }
				}
			}
	    };
	    Action cancelAction = new AbstractAction("Close") {
			public void actionPerformed(ActionEvent e) {
				editor.dispose();
			}
	    };

	    // the gui layout part
	    cp.setLayout(new BorderLayout(10,10));
	    cp.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

	    cp.add(new JScrollPane(statementsBox), BorderLayout.CENTER);

	    ButtonBarBuilder bbb = ButtonBarBuilder.createLeftToRightBuilder();
	    bbb.addGridded(new JButton(saveAction));
	    bbb.addGridded(new JButton(copyAction));
	    bbb.addGridded(new JButton(executeAction));
	    bbb.addGridded(new JButton(cancelAction));

	    cp.add(bbb.getPanel(), BorderLayout.SOUTH);

	    editor.pack();
	    editor.setLocationRelativeTo(BuildExampleTableDialog.this);
	    editor.setVisible(true);
	}

	/**
	 * Gets the table's schema name from the combobox.
	 */
	private String getTableSchema() {
		if (sourceChooser.getSchemaComboBox().getSelectedItem() != null) {
			return ((SQLSchema)sourceChooser.getSchemaComboBox().getSelectedItem()).getName();
		} 
		return null;
	}
	
	/**
	 * Gets the table's catalog name from the combobox.
	 */
	private String getTableCatalog() {
		if (sourceChooser.getCatalogComboBox().getSelectedItem() != null) {
			return ((SQLCatalog)sourceChooser.getCatalogComboBox().getSelectedItem()).getName();
		}
		return null;
	}
	
	/**
	 * Gets the selected datasource
	 */
	private SPDataSource getDataSource() {
		return (SPDataSource)sourceChooser.getDataSourceComboBox().getSelectedItem();
	}
	
	/**
	 * A swing worker that runs the population of the table so there can be a progress bar.
	 */
	private class PopulateTableWorker extends SPSwingWorker { 
		
		/**
		 * The table we are working on
		 */
		private SQLTable table;
		
		
		/**
		 * Creates the worker and adds it to the registry.
		 * 
		 * @param registry The registry to add it to
		 * @param table The table to populate
		 */
		public PopulateTableWorker(SwingWorkerRegistry registry, SQLTable table) {
			super(registry);
			this.table = table;
		}
		
		/**
		 * Populates the given table.
		 */
		private void populateTable(SQLTable table) {
			popMon.start();
			for (String s: names) {
				int firstSpace = s.indexOf(" ");
				if (firstSpace != -1) {
					firstNames.add(s.substring(0, firstSpace));
					lastNames.add(s.substring(firstSpace+1,s.length()));
				} else {
					firstNames.add(s);
				}
			}
	
			SQLDatabase db = swingSession.getDatabase(getDataSource());
			Statement stmt = null;
			Connection con = null;
			try {
				con = db.getConnection();
				PreparedStatement ps = con.prepareStatement("INSERT INTO " + DDLUtils.toQualifiedName(table) + " (ID,FirstName,LastName,Email,Address,HomePhone,CellPhone) VALUES (?,?,?,?,?,?,?)");
				//Uses the same seed so that all of the examples look the same
				Random r = new Random(0);
					
				String first = null;
				String last = null;
				String st = null;
				String email = null;
				String homePhone = null;
				String cell = null;
				
				//make all the data
				for (int x = 0; x < NUM_RECORDS; x++) {
					popMon.inc();
					first = firstNames.get(r.nextInt(firstNames.size()));
					last = lastNames.get(r.nextInt(lastNames.size()));
					
					st = getStreet(r);
					
					email = getEmail(r);
					st = r.nextInt(999) + 1 + " " + st;
					
					homePhone = getPhoneNumber(r);
					cell = getPhoneNumber(r);
				
					ps.setInt(1, x);
					ps.setString(2, first);
					ps.setString(3, last);
					if (r.nextInt(20) != 0) {
						ps.setString(4, email);
					}
					
					ps.setString(5, st);
					if (r.nextInt(20) != 0) {
						ps.setString(6, homePhone);
					}
					
					if (r.nextInt(20) != 0) {
						ps.setString(7, cell);
					}
					ps.execute();
				}
				
				
				//This scrambles some of the data
				
				stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
				ResultSet rs = stmt.executeQuery("SELECT ID,FirstName,LastName,Email,Address,HomePhone,CellPhone FROM " + DDLUtils.toQualifiedName(table));
				ps = con.prepareStatement("UPDATE " + DDLUtils.toQualifiedName(table) + " SET FirstName=?, LastName=?, Email=?, Address=?, HomePhone=?, CellPhone=? WHERE ID=?");
				
				for (int x = 0; x < 15; x++) {
					int dup = r.nextInt(2000);
					for (int y = r.nextInt(3) + 3; y >= 0; y--) {
						int curr = r.nextInt(2000);
						rs.absolute(dup);
						first = rs.getString(2);
						last = rs.getString(3);
						email = rs.getString(4);
						st = rs.getString(5);
						homePhone = rs.getString(6);
						cell = rs.getString(7);
						
						if (x == 0) {
							email = getEmail(r);
						} else if (x == 1) {
							st = getStreet(r);
							email = getEmail(r);
							st = (r.nextInt(999) + 1) + " "+ st;
							homePhone = getPhoneNumber(r);
							cell = getPhoneNumber(r);
						} else {
							
							if (r.nextInt(7) == 0) {
								first = firstNames.get(r.nextInt(firstNames.size()));
							}
							
							if (r.nextInt(7) == 0) {
								last = lastNames.get(r.nextInt(lastNames.size()));
							}
							
							if (r.nextInt(7) == 0) {
								st = getStreet(r);
								st = (r.nextInt(999) + 1) + " "+ st;
							}
							
							if (r.nextInt(7) == 0) {
								email = getEmail(r);
							}
	
							if (r.nextInt(7) == 0) {
								homePhone = getPhoneNumber(r);
							}
							
							if (r.nextInt(7) == 0) {
								cell = getPhoneNumber(r);
							}
						}
						
						
						ps.setString(1, first);
						ps.setString(2, last);
						ps.setString(3, email);
						ps.setString(4,st);
						ps.setString(5, homePhone);
						ps.setString(6, cell);

						ps.setInt(7,curr);
						ps.execute();
					}
				}
				
				JOptionPane.showMessageDialog(BuildExampleTableDialog.this, "Table Created Successfully", "Example Table", JOptionPane.INFORMATION_MESSAGE);
				dispose();
			} catch (Exception e) {
				SPSUtils.showExceptionDialogNoReport(BuildExampleTableDialog.this,
						"Error while trying to insert into example table", e);
			} finally {
				popMon.stop();
				try {
					if (con != null) {
						con.close();
					}
					
					if (stmt != null) {
						stmt.close();
					}
				} catch (SQLException e) {
					SPSUtils.showExceptionDialogNoReport(BuildExampleTableDialog.this,
							"Error while trying to close connection or statment", e);
				}
			}
			
			
		}

		@Override
		public void cleanup() throws Exception {
		}

		@Override
		public void doStuff() throws Exception {
			populateTable(table);
		}
	}

	/**
	 * Generates a phoneNumber at random. there is a 50% chance that the 
	 * number will have a dash in the middle.
	 * 
	 * @param r The Random Object to use
	 * @return The phone number.
	 */
	private String getPhoneNumber(Random r) {
		int rand = r.nextInt(3);
		if (rand == 0) {
			return "" + rand10(r) + rand10(r) + rand10(r) + rand10(r) + rand10(r) + rand10(r) + rand10(r);
		} else {
			return "" + rand10(r) + rand10(r) + rand10(r) + "-" + rand10(r) + rand10(r) + rand10(r) + rand10(r);
		}
	}

	/**
	 * Generates an email at random. 
	 * 
	 * @param r The Random Object to use
	 * @return The email.
	 */
	private String getEmail(Random r) {
		String email = firstNames.get(r.nextInt(firstNames.size()));
		for (int z = r.nextInt(1); z >= 0; z--) {				
			email += "." + firstNames.get(r.nextInt(firstNames.size())); 
		}
		email += "@" + funnyWords[r.nextInt(funnyWords.length)] + "." + domain[r.nextInt(domain.length)];
		return email;
	}
	
	/**
	 * Generates a street at random. 
	 * 
	 * @param r The Random Object to use
	 * @return The street.
	 */
	private String getStreet(Random r) {
		String st;
		if (r.nextInt(2) == 0) {
			st = funnyWords[r.nextInt(funnyWords.length)];
		} else {
			st = lastNames.get(r.nextInt(lastNames.size()));
		}
		
		st += " " + street[r.nextInt(street.length)];
		return st;
	}
	
	/**
	 * Generates a number between 0 and 9 inclusive at random. 
	 * 
	 * @param r The Random Object to use
	 * @return The phone number.
	 */
	private int rand10(Random r) {
		return r.nextInt(10); 
	}
	
	/**
	 *List of unusual Names from wikipedia
	 */
	private String[] names = new String[]{
			"Armand Hammer",
			"Hapoel Tel Aviv",
			"Iuma Dylan-Lucas Thornhill",
			"Joker Arroyo",
			"Kal-El Coppola",
			"Keldorn",
			"Kenesaw Mountain Landis",
			"MegaZone",
			"Oleúde José Ribeiro",
			"Optimus Prime",
			"Sony PlayStation",
			"Matrix",
			"John Portsmouth Football Club Westwood",
			"Apple Martin",
			"Baby Hospital",
			"Bluebell Madonna Halliwell",
			"Boo Moore",
			"Breece D'J Pancake",
			"Reverend Canaan Banana",
			"Crown Shakur Thomas",
			"Depressed Cupboard Cheesecake",
			"Flipper Reddolphin",
			"Heavenly Hiraani Tiger Lily Hutchence",
			"Island Shuler",
			"Jellyfish McSaveloy",
			"Loser Lan",
			"Maybe Barnes",
			"Moxie CrimeFighter Jillette",
			"Muffin Lord",
			"Peerless Price",
			"Picabo Street",
			"Pilot Inspektor Riesgraf",
			"Little Pixie Geldof",
			"Poppy Montgomery",
			"Poet Siena Rose Goldberg",
			"Thursday October Christian",
			"Urmas-Armas Ingel",
			"Yahoo Serious",
			"Zeppelin Wai Wong",
			"Coco Crisp",
			"Wrigley Fields",
			"Amor De Cosmos",
			"Constant-Désiré Despradelle",
			"Espen Thoresen Hværsaagod",
			"Legal Tender Coxey",
			"Mahershalalhashbaz Ali",
			"Mister Thorne",
			"Nardwuar the Human Serviette",
			"Notwithstanding Griswold",
			"Robin Victory in Europe Strasser",
			"Screaming Lord Sutch",
			"Trout Fishing",
			"Wu Suowei"};
	
	
	/**
	 * Funny words from some fourm
	 */ 
	private String[] funnyWords = new String[]{
			"yolk",
			"guzzle",
			"missmeow",
			"chuckle",
			"golly",
			"ism",
			"egg",
			"kumquat",
			"coagulated",
			"rice cracker",
			"Shizzle",
			"badonkadonk",
			"teaz0r",
			"Emperor_Mike",
			"Antidisestablishmentarianism",
			"applehead",
			"uranus",
			"gizzard",
			"noodlenoodle",
			"Mullet",
			"kumquat",
			"syntax",
			"coccyx",
			"defenestrate",
			"dongle",
			"kashikomarimashita",
			"undulate",
			"ovule",
			"nubbin",
			"peritoneum",
			"baldric",
			"jeep",
			"chaw",
			"unitard",
			"red",
			"Tuesday",
			"Interweb"
	};
	
	/**
	 * Array of possible web endings
	 */
	private String[] domain = new String[]{
			"ca", "com", "org"
	};
	
	/**
	 * Array of possilbe street types
	 */
	private String[] street = new String[]{
			"street", "st", "pl", "rd", "dr"
	};
	
	/**
	 * A monitor that helps to update the progress bar.
	 */
	private class PopulationMonitor implements Monitorable {
		
		private int size;
		private int prog;
		private boolean running;
		
		/**
		 * Sets up using the initial size.
		 * 
		 * @param size The initial size
		 */
		public PopulationMonitor(int size) {
			this.size = size;
			running = false;
		}
		
		/**
		 * Start the monitor, disabling the buttons and reseting the progress.
		 */
		public void start() {
			running = true;
			pw.start();
			cancel.setEnabled(false);
			create.setEnabled(false);
		}
		
		/**
		 * Stops the monitor, enabling the buttons.
		 */
		public void stop() {
			running = false;
			prog = 0;
			cancel.setEnabled(true);
			create.setEnabled(true);
		}
		
		/**
		 * Increment the monitor by one.
		 */
		public void inc() {
			prog++;
		}
		
		public Integer getJobSize() {
			return new Integer(size);
		}

		public String getMessage() {
			return null;
		}

		public int getProgress() {
			return prog;
		}

		public boolean hasStarted() {
			return running;
		}

		public boolean isFinished() {
			return !running;
		}

		public void setCancelled(boolean cancelled) {
			if (cancelled) {
				stop();
			} else {
				start();
			}
		}
		
	}
}
