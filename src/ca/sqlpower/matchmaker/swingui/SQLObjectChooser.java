package ca.sqlpower.matchmaker.swingui;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.ASUtils;

public class SQLObjectChooser {

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

	private ArchitectDataSource dataSource;

	private SQLCatalog catalog;

	private SQLSchema schema;

	private SQLTable table;

	private SQLDatabase db;

	/**
	 * Creates a set of Swing components that allow the user to select a
	 * particular database object, up to and including children of SQLTable.
	 *
	 * @param owningComponent
	 *            the component that will house the sqlobject chooser components
	 *            of this instance. This is used only to attach error report
	 *            dialogs to the correct parent component.
	 * @param dataSources
	 *            The list of data sources that the datasource chooser will
	 *            contain.
	 * @throws ArchitectException
	 */
	public SQLObjectChooser(final MatchMakerSwingSession session) throws ArchitectException {

		db = session.getDatabase();
		dataSource = db.getDataSource();
		dataSourceComboBox.addItem(dataSource);
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

		ItemListener itemListener = new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				try {
					validate();
				} catch (ArchitectException e1) {
					ASUtils.showExceptionDialogNoReport(session.getFrame(),
							"Database error", e1);
				}
			}
		};

		/**
		 * data source is fixed now

		dataSourceComboBox.addItemListener(itemListener);
		*/
		catalogComboBox.addItemListener(itemListener);
		schemaComboBox.addItemListener(itemListener);
		tableComboBox.addItemListener(itemListener);
	}

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

				dataSource = (ArchitectDataSource) dataSourceComboBox
						.getSelectedItem();
				db = new SQLDatabase(dataSource);
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
	 * replace the combobox items and set the enable/disable state according to
	 * the size of items, enable if the items size > 0. also set the selected item
	 * if the selectedItem >= 0
	 * we don't want to just reset the combobox model, because we may have
	 * listener on the combobox.
	 * @param comboBox   the JcomboBox, all item in it will be removed
	 * @param items      List of the item that we want to put in the combobox
	 * @param selectedIndex the selectedItem after new items in place.
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
