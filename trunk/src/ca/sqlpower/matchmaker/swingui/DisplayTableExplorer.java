package ca.sqlpower.matchmaker.swingui;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class DisplayTableExplorer {

    private static Logger logger = Logger.getLogger(DisplayTableExplorer.class);

    private JComboBox dbDropdown;
    private JComboBox catalogDropdown;
    private JComboBox schemaDropdown;
    private JComboBox tableDropdown;
    private JTextField rowLimit;
    private JTextField nRecords;
    private JTextField recordsFound;
    private JTextArea sqlStatement;
    private JTable columnInformation;
    
    
    public DisplayTableExplorer(){
        buildUI();
    }
    
    public void buildUI(){
        FormLayout layout = new FormLayout(
                "4dlu,fill:min(70dlu;default), 4dlu, fill:min(150dlu;default):grow, min(50dlu;default), 4dlu, min(30dlu;default),4dlu, fill:min(40dlu:default)", // columns
                " 10dlu,10dlu,4dlu,12dlu,10dlu,12dlu,4dlu,12dlu,4dlu,12dlu,10dlu,10dlu,10dlu,10dlu,10dlu,20dlu,10dlu"); // rows
        
        PanelBuilder pb;
        JPanel p = logger.isDebugEnabled()  ? new FormDebugPanel(layout) : new JPanel(layout);
        pb = new PanelBuilder(layout, p);
        
        dbDropdown = new JComboBox();
        catalogDropdown = new JComboBox();
        schemaDropdown = new JComboBox();
        tableDropdown = new JComboBox();
        rowLimit = new JTextField(5);
        nRecords = new JTextField(5);        
        columnInformation = new JTable();
        recordsFound = new JTextField(10);
        recordsFound.setEditable(false);
        sqlStatement = new JTextArea();
        
        CellConstraints cc = new CellConstraints();
        
        //Dropdown lists
        pb.add(new JLabel("Database Connection"), cc.xy(2,2));
        pb.add(dbDropdown, cc.xyw(4,2,4));
        pb.add(new JLabel("Catalog"), cc.xy(2,4));
        pb.add(catalogDropdown, cc.xyw(4,4,4));
        pb.add(new JLabel("Schema"), cc.xy(2,6));
        pb.add(schemaDropdown, cc.xyw(4,6,4));
        pb.add(new JLabel("Table"), cc.xy(2,8));
        pb.add(catalogDropdown, cc.xyw(4,8,4));
        
        pb.add(columnInformation, cc.xyw(2,10,8));
        pb.add(recordsFound, cc.xy(2,12));
        pb.add(new JLabel("Row Limit:"), cc.xy(5,12));
        
        
        


    }
}
