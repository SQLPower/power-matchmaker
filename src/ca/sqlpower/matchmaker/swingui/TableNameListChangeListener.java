package ca.sqlpower.matchmaker.swingui;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.SQLException;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextArea;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.matchmaker.MySimpleTable;

public class TableNameListChangeListener implements ItemListener {

    JComboBox tableList;
    JComboBox indices;
    JTextArea filter;
    
    public TableNameListChangeListener(JComboBox tableList, JComboBox indices, JTextArea filter) {
        super();
        this.tableList = tableList;
        this.indices = indices;
        this.filter = filter;
    }

    public void itemStateChanged(ItemEvent e) {
        MySimpleTable table = (MySimpleTable) tableList.getSelectedItem();
        if ( table == null )
            return;
        try {
            table.populateColumn();
            table.populateIndex();
        } catch (SQLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (ArchitectException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        filter.setText("");
        indices.removeAllItems();
        indices.setModel(new DefaultComboBoxModel(table.getIndices().toArray()));
    }

}
