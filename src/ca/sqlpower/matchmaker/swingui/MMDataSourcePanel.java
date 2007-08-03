package ca.sqlpower.matchmaker.swingui;

import javax.swing.JTextField;

import org.apache.log4j.Logger;

import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.swingui.SPDataSourcePanel;

/**
 * The MMDataSourcePanel is an SPDataSourcePanel with an extra field
 * for setting the PL schema owner.
 */
public class MMDataSourcePanel extends SPDataSourcePanel {
	private static final Logger logger = Logger.getLogger(MMDataSourcePanel.class);
	
	/**
	 * The PL schema owner for this connection.
	 */
	private JTextField repositorySchemaOwnerField;

	/**
	 * Sets up the repository owner field.
	 * 
	 * @param ds The data source to edit.
	 */
	MMDataSourcePanel(SPDataSource ds) {
		super(ds);
		repositorySchemaOwnerField = new JTextField(ds.getPlSchema());
		repositorySchemaOwnerField.putClientProperty(EXTRA_FIELD_LABEL_PROP, "Repository Schema Owner");
		addExtraField(repositorySchemaOwnerField);
	}
	
	@Override
	public boolean applyChanges() {
		boolean success = super.applyChanges();
		if (!success) return false;
		
		logger.debug("Setting respository owner to " + repositorySchemaOwnerField.getText());
		getDbcs().setPlSchema(repositorySchemaOwnerField.getText());
		
		return true;
	}
}
