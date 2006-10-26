package ca.sqlpower.matchmaker.swingui;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
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


	public SQLObjectChooser(final JFrame parent, List<ArchitectDataSource> dataSources) {

		dataSourceComboBox.setModel(new DefaultComboBoxModel(dataSources.toArray()));
		dataSourceComboBox.setSelectedItem(null);

		ItemListener itemListener = new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				try {
					validate();
				} catch (ArchitectException e1) {
					ASUtils.showExceptionDialogNoReport(parent,
							"Database SQL error",e1);
				}
			}};
		dataSourceComboBox.addItemListener(itemListener);
		catalogComboBox.addItemListener(itemListener);
		schemaComboBox.addItemListener(itemListener);
		tableComboBox.addItemListener(itemListener);
	}

	private void validate() throws ArchitectException {

		if ( dataSourceComboBox.getSelectedItem() == null ) {
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
			if ( dataSource != dataSourceComboBox.getSelectedItem() ) {

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

				dataSource = (ArchitectDataSource)dataSourceComboBox.getSelectedItem();
				db = new SQLDatabase(dataSource);
				db.populate();

				if ( db.isCatalogContainer() ) {
					List <SQLCatalog> catalogs = db.getChildren();
					if ( catalogs != null && catalogs.size() > 0 ) {
						catalogComboBox.setEnabled(true);
						catalogComboBox.setModel(
								new DefaultComboBoxModel(
										catalogs.toArray()));
						catalogComboBox.setSelectedItem(null);
						catalogTerm.setText(catalogs.get(0).getNativeTerm());
						catalogTerm.setEnabled(true);
					}
				} else if ( db.isSchemaContainer() ) {

					List <SQLSchema> schemas = db.getChildren();
					if ( schemas != null && schemas.size() > 0 ) {
						schemaComboBox.setEnabled(true);
						DefaultComboBoxModel schemaComboBoxModel = new DefaultComboBoxModel(
										schemas.toArray());
						schemaComboBox.setModel(schemaComboBoxModel);
						schemaComboBox.setSelectedItem(null);
						schemaTerm.setText(schemas.get(0).getNativeTerm());
						schemaTerm.setEnabled(true);
					}
				} else {
					List <SQLTable> tables = db.getChildren();
					if ( tables != null && tables.size() > 0 ) {
						tableComboBox.setEnabled(true);
						tableComboBox.setModel(
								new DefaultComboBoxModel(
										tables.toArray()));
						tableComboBox.setSelectedItem(null);
					}
				}
			} else if ( catalog != catalogComboBox.getSelectedItem() ) {

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
				catalogTerm.setText(catalog.getNativeTerm());
				catalogTerm.setEnabled(true);
				if ( catalog.isSchemaContainer() ) {
					List <SQLSchema> schemas = catalog.getChildren();
					if ( schemas != null && schemas.size() > 0 ) {
						schemaComboBox.setEnabled(true);
						schemaComboBox.setModel(
								new DefaultComboBoxModel(
										schemas.toArray()));
						schemaComboBox.setSelectedItem(null);
						schemaTerm.setText(schemas.get(0).getNativeTerm());
						schemaTerm.setEnabled(true);
					}
				} else {
					List <SQLTable> tables = catalog.getChildren();
					if ( tables != null && tables.size() > 0 ) {
						tableComboBox.setEnabled(true);
						tableComboBox.setModel(
								new DefaultComboBoxModel(
										tables.toArray()));
						tableComboBox.setSelectedItem(null);
					}
				}
			} else if ( schema != schemaComboBox.getSelectedItem() ) {

				tableComboBox.removeAllItems();
				columnComboBox.removeAllItems();
				uniqueKeyComboBox.removeAllItems();
				tableComboBox.setEnabled(false);
				columnComboBox.setEnabled(false);
				uniqueKeyComboBox.setEnabled(false);
				schemaTerm.setText("Schema");

				schema = (SQLSchema) schemaComboBox.getSelectedItem();
				schemaTerm.setText(schema.getNativeTerm());
				schemaTerm.setEnabled(true);
				List <SQLTable> tables = schema.getChildren();
				if ( tables != null && tables.size() > 0 ) {
					tableComboBox.setEnabled(true);
					tableComboBox.setModel(
							new DefaultComboBoxModel(
									tables.toArray()));
					tableComboBox.setSelectedItem(null);
				}
			} else if ( table != tableComboBox.getSelectedItem() ) {

				columnComboBox.removeAllItems();
				uniqueKeyComboBox.removeAllItems();
				columnComboBox.setEnabled(false);
				uniqueKeyComboBox.setEnabled(false);


				table = (SQLTable) tableComboBox.getSelectedItem();
				if (table != null ){
				List <SQLColumn> columns = table.getColumns();
				if ( columns != null && columns.size() > 0 ) {
					columnComboBox.setEnabled(true);
					columnComboBox.setModel(
							new DefaultComboBoxModel(
									columns.toArray()));
					columnComboBox.setSelectedItem(null);
				}

				List <SQLIndex> indices = table.getUniqueIndex();
				if ( indices != null && indices.size() > 0 ) {
					uniqueKeyComboBox.setEnabled(true);
					uniqueKeyComboBox.setModel(
							new DefaultComboBoxModel(indices.toArray()));
					uniqueKeyComboBox.setSelectedIndex(0);
				}
				}
			}
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

