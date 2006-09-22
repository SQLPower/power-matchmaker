package ca.sqlpower.matchmaker.swingui;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import ca.sqlpower.matchmaker.MySimpleTable;

public class TableOwnerListChangeListener implements ItemListener {

    private JComboBox sourceTableOwner;
    private JComboBox sourceTableName;
    
    public TableOwnerListChangeListener(JComboBox sourceTableOwner,
            JComboBox sourceTableName) {
        this.sourceTableOwner = sourceTableOwner;
        this.sourceTableName = sourceTableName;
    }

    public void itemStateChanged(ItemEvent e) {
        String owner = (String)sourceTableOwner.getSelectedItem();
        sourceTableName.removeAllItems();
        if ( owner == null ) {
            return;
        }
        List<MySimpleTable> tables = MatchMakerFrame.getMainInstance().getTables(owner);
        MySimpleTable old = (MySimpleTable) sourceTableName.getSelectedItem();
        sourceTableName.setModel(new DefaultComboBoxModel(tables.toArray()));
        if ( old != null ) {
            sourceTableName.setSelectedItem(old);
        }
    }

}
