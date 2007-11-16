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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.swingui.SPSUtils;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;

/**
 * A set of Swing components that allow the user to select a
 * particular database object, up to and including children of SQLTable.
 * This class doesn't include an overall panel that ties all these
 * components together, to it is up to client code to pick and choose
 * the components it wants (you don't have to use all of them), and
 * lay them out in a way that makes sense for the particular application.
 */
public class SQLObjectChooser {
	
    /**
     * Presents a modal dialog with combo boxes for database connections,
     * catalogs, and schemas. Initially, there is no selection in the database
     * combo box, and the others are empty. As databases are chosen by the user,
     * the other combo boxes become enabled depending on the containment
     * hierarchy of the selected datbase (for instance, some databases have
     * catalogs but not schemas; others have schemas but not catalogs; others
     * have both; and still others just have tables directly inside the
     * top-level database connection).
     * <p>
     * The type of the return value depends on the containment hierarchy of the selected database (the possibilities are described above).
     * The guarantee is that the returned object will itself be a "table container;" that is, its
     * children are of type SQLTable.
     * 
     * @param session
     * @param owner
     * @param title
     * @return The selected "table container" object, or <tt>null</tt> if the user cancels or
     * closes the dialog.
     * @throws ArchitectException If there are problems connecting to or populating the chosen databases
     */
    public static SQLObject showSchemaChooserDialog(MatchMakerSwingSession session, Component owner, String title) throws ArchitectException {
        SQLObjectChooser soc = new SQLObjectChooser(session);
        
        // single boolean in final array so the buttons can modify its value
        final boolean[] dialogAccepted = new boolean[1];
        final JDialog d = SPSUtils.makeOwnedDialog(owner, title);
        
        FormLayout layout = new FormLayout("pref,4dlu,pref");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        
        builder.append("Connection", soc.getDataSourceComboBox());
        builder.append(soc.getCatalogTerm(), soc.getCatalogComboBox());
        builder.append(soc.getSchemaTerm(), soc.getSchemaComboBox());
        
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialogAccepted[0] = true;
                d.dispose();
            }
        });
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                d.dispose();
            }
        });
        
        builder.append(ButtonBarFactory.buildOKCancelBar(okButton, cancelButton), 3);
        
        d.setContentPane(builder.getPanel());
        d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        d.setModal(true);
        d.pack();
        d.setLocationRelativeTo(owner);
        
        for (;;) {
            dialogAccepted[0] = false;
            d.setVisible(true);

            if (!dialogAccepted[0]) {
                return null;
            }

            // post-mortem: figure out if we're returning a database, catalog, or schema
            if (soc.schema != null) {
                return soc.schema;
            } else if (soc.catalog != null && !soc.catalog.isSchemaContainer()) {
                return soc.catalog;
            } else if (soc.db != null && (! (soc.db.isSchemaContainer() || soc.db.isCatalogContainer()))) {
                return soc.db;
            } else {
                return null;
            }
        }
    }
    
	private JComboBox dataSourceComboBox = new JComboBox();

	private JComboBox catalogComboBox = new JComboBox();

	private JComboBox schemaComboBox = new JComboBox();

	private JComboBox tableComboBox = new JComboBox();

	private JComboBox columnComboBox = new JComboBox();

	private JComboBox uniqueKeyComboBox = new JComboBox();

	private JLabel catalogTerm = new JLabel("Catalog");

	private JLabel schemaTerm = new JLabel("Schema");

	private JProgressBar progressBar = new JProgressBar();

	private JLabel status = new JLabel();

	private SPDataSource dataSource;

	private SQLCatalog catalog;

	private SQLSchema schema;

	private SQLTable table;

	private SQLDatabase db;
	
	private MatchMakerSwingSession session;

	/**
	 * Creates a new SQLObjectChooser component set.
	 *
	 * @param owningComponent
	 *            the component that will house the sqlobject chooser components
	 *            of this instance. This is used only to attach error report
	 *            dialogs to the correct parent component.
	 * @param dataSources
	 *            The list of data sources that the datasource chooser will
	 *            contain.
	 */
	public SQLObjectChooser(final MatchMakerSwingSession session) {
		
		this.session = session;
			
		db = session.getDatabase();
		dataSource = db.getDataSource();
		setComboBoxStateAndItem(dataSourceComboBox, session.getContext().getDataSources(), -1);
		dataSourceComboBox.setSelectedItem(dataSource);

		catalogComboBox.removeAllItems();
		schemaComboBox.removeAllItems();
		tableComboBox.removeAllItems();
		columnComboBox.removeAllItems();
		uniqueKeyComboBox.removeAllItems();
		catalogComboBox.setEnabled(false);
		schemaComboBox.setEnabled(false);
		tableComboBox.setEnabled(false);
		columnComboBox.setEnabled(false);
		uniqueKeyComboBox.setEnabled(false);
		catalogTerm.setText("Catalog");
		catalogTerm.setEnabled(false);
		schemaTerm.setText("Schema");
		schemaTerm.setEnabled(false);

        try {
            db.populate();
            if (db.isCatalogContainer()) {
                List<SQLCatalog> catalogs = db.getChildren();
                setComboBoxStateAndItem(catalogComboBox, catalogs, -1);
                if ( catalogs != null && catalogs.size() > 0 ) {
                    catalogTerm.setText(catalogs.get(0).getNativeTerm());
                    catalogTerm.setEnabled(true);
                }
            } else if (db.isSchemaContainer()) {

                List<SQLSchema> schemas = db.getChildren();
                
                setComboBoxStateAndItem(schemaComboBox, schemas, -1);
                if ( schemas != null && schemas.size() > 0 ) {
                    schemaTerm.setText(schemas.get(0).getNativeTerm());
                    schemaTerm.setEnabled(true);
                }
            } else {
                List<SQLTable> tables = db.getChildren();
                setComboBoxStateAndItem(tableComboBox, tables, -1);
            }
        } catch (ArchitectException ex) {
            throw new ArchitectRuntimeException(ex);
        }
        
		ItemListener itemListener = new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
                try {
                    validate();
                } catch (Exception ex) {
                    SPSUtils.showExceptionDialogNoReport((Component) e.getSource(), "Database Error", ex);
                }
			}
		};

		dataSourceComboBox.addItemListener(itemListener);        
		catalogComboBox.addItemListener(itemListener);
		schemaComboBox.addItemListener(itemListener);
		tableComboBox.addItemListener(itemListener);
	}

    /**
     * Updates all of the appropriate components after one of them has had a
     * selection change. This method is really a subroutine of the anonymous
     * ItemListener implementation defined in the constructor.
     * 
     * @throws ArchitectException
     *             When any of the database access fails.
     */
	private void validate() throws ArchitectException {

		if (dataSourceComboBox.getSelectedItem() == null) {
			dataSource = null;
			catalog = null;
			schema = null;
			table = null;
			db = null;

			catalogComboBox.removeAllItems();
			schemaComboBox.removeAllItems();
			tableComboBox.removeAllItems();
			columnComboBox.removeAllItems();
			uniqueKeyComboBox.removeAllItems();
			catalogComboBox.setEnabled(false);
			schemaComboBox.setEnabled(false);
			tableComboBox.setEnabled(false);
			columnComboBox.setEnabled(false);
			uniqueKeyComboBox.setEnabled(false);
			catalogTerm.setText("Catalog");
			catalogTerm.setEnabled(false);
			schemaTerm.setText("Schema");
			schemaTerm.setEnabled(false);

		} else {
			if (dataSource != dataSourceComboBox.getSelectedItem()) {

				catalogComboBox.removeAllItems();
				schemaComboBox.removeAllItems();
				tableComboBox.removeAllItems();
				columnComboBox.removeAllItems();
				uniqueKeyComboBox.removeAllItems();
				catalogComboBox.setEnabled(false);
				schemaComboBox.setEnabled(false);
				tableComboBox.setEnabled(false);
				columnComboBox.setEnabled(false);
				uniqueKeyComboBox.setEnabled(false);
				catalogTerm.setText("Catalog");
				catalogTerm.setEnabled(false);
				schemaTerm.setText("Schema");
				schemaTerm.setEnabled(false);

				dataSource = (SPDataSource) dataSourceComboBox
						.getSelectedItem();
				db = session.getDatabase(dataSource);
				db.populate();

				if (db.isCatalogContainer()) {
					List<SQLCatalog> catalogs = db.getChildren();
					setComboBoxStateAndItem(catalogComboBox,catalogs,-1);
					if ( catalogs != null && catalogs.size() > 0 ) {
						catalogTerm.setText(catalogs.get(0).getNativeTerm());
						catalogTerm.setEnabled(true);
					}
				} else if (db.isSchemaContainer()) {

					List<SQLSchema> schemas = db.getChildren();
					setComboBoxStateAndItem(schemaComboBox,schemas,-1);
					if ( schemas != null && schemas.size() > 0 ) {
						schemaTerm.setText(schemas.get(0).getNativeTerm());
						schemaTerm.setEnabled(true);
					}
				} else {
					List<SQLTable> tables = db.getChildren();
					setComboBoxStateAndItem(tableComboBox,tables,-1);
				}
			} else if (catalog != catalogComboBox.getSelectedItem()) {

				schemaComboBox.removeAllItems();
				tableComboBox.removeAllItems();
				columnComboBox.removeAllItems();
				uniqueKeyComboBox.removeAllItems();
				schemaComboBox.setEnabled(false);
				tableComboBox.setEnabled(false);
				columnComboBox.setEnabled(false);
				uniqueKeyComboBox.setEnabled(false);
				catalogTerm.setText("Catalog");
				catalogTerm.setEnabled(false);
				schemaTerm.setText("Schema");
				schemaTerm.setEnabled(false);

				catalog = (SQLCatalog) catalogComboBox.getSelectedItem();
				if (catalog == null) {
					catalogTerm.setText("N/A");
				} else {
					catalogTerm.setText(catalog.getNativeTerm());
					catalogTerm.setEnabled(true);
					if (catalog.isSchemaContainer()) {
						List<SQLSchema> schemas = catalog.getChildren();
						setComboBoxStateAndItem(schemaComboBox,schemas,-1);
						if ( schemas != null && schemas.size() > 0 ) {
							schemaTerm.setText(schemas.get(0).getNativeTerm());
							schemaTerm.setEnabled(true);
						}
					} else {
						List<SQLTable> tables = catalog.getChildren();
						setComboBoxStateAndItem(tableComboBox,tables,-1);
					}
				}
			} else if (schema != schemaComboBox.getSelectedItem()) {

				tableComboBox.removeAllItems();
				columnComboBox.removeAllItems();
				uniqueKeyComboBox.removeAllItems();
				tableComboBox.setEnabled(false);
				columnComboBox.setEnabled(false);
				uniqueKeyComboBox.setEnabled(false);
				schemaTerm.setText("Schema");

				schema = (SQLSchema) schemaComboBox.getSelectedItem();
				if (schema == null) {
					schemaTerm.setText("N/A");
				} else {
					schemaTerm.setText(schema.getNativeTerm());
					schemaTerm.setEnabled(true);
					List<SQLTable> tables = schema.getChildren();
					setComboBoxStateAndItem(tableComboBox,tables,-1);
				}
			} else if (table != tableComboBox.getSelectedItem()) {

				columnComboBox.removeAllItems();
				uniqueKeyComboBox.removeAllItems();
				columnComboBox.setEnabled(false);
				uniqueKeyComboBox.setEnabled(false);

				table = (SQLTable) tableComboBox.getSelectedItem();
				if (table != null) {
					List<SQLColumn> columns = table.getColumns();
					setComboBoxStateAndItem(columnComboBox,columns,-1);

					List<SQLIndex> indices = table.getUniqueIndices();
					setComboBoxStateAndItem(uniqueKeyComboBox,indices,0);
				}
			}
		}
	}

	/**
     * Replaces the combo box items and sets the enable/disable state according
     * to the size of items: Enable if the items size &gt; 0. Also sets the
     * combo box's selected item if the selectedIndex &gt;= 0.
     * <p>
     * Doesn't just reset the combo box's model, because there could be
     * listeners on the combobox.
     * 
     * @param comboBox
     *            the combo box to operate on. All of its items will be replaced by the
     *            items in the given list.
     * @param items
     *            The new list of items that the combo box should have.
     * @param selectedIndex
     *            the index that should be selected after the combo box's contents have
     *            been replaced.
     */
	private void setComboBoxStateAndItem(JComboBox comboBox, List items, int selectedIndex) {
		comboBox.removeAllItems();
		comboBox.setEnabled(false);
		if (items == null || items.size() == 0)		return;
		for ( Object o : items ) {
			comboBox.addItem(o);
		}
		if (items.size() > 0 ) comboBox.setEnabled(true);
		if ( selectedIndex >= 0 && selectedIndex < items.size()) {
			comboBox.setSelectedIndex(selectedIndex);
		} else {
			comboBox.setSelectedIndex(-1);
		}
	}

	public JComboBox getCatalogComboBox() {
		return catalogComboBox;
	}

	public JLabel getCatalogTerm() {
		return catalogTerm;
	}

	public JComboBox getColumnComboBox() {
		return columnComboBox;
	}

	public JComboBox getDataSourceComboBox() {
		return dataSourceComboBox;
	}

	public JProgressBar getProgressBar() {
		return progressBar;
	}

	public JComboBox getSchemaComboBox() {
		return schemaComboBox;
	}

	public JLabel getSchemaTerm() {
		return schemaTerm;
	}

	public JLabel getStatus() {
		return status;
	}

	public JComboBox getTableComboBox() {
		return tableComboBox;
	}

	public JComboBox getUniqueKeyComboBox() {
		return uniqueKeyComboBox;
	}

	public SQLDatabase getDb() {
		return db;
	}

}
