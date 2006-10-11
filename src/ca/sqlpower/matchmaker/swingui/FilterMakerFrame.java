package ca.sqlpower.matchmaker.swingui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLTable;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class FilterMakerFrame extends JDialog {

	private static Logger logger = Logger.getLogger(FilterMakerFrame.class);

	private JComboBox duplicate1;
	private JComboBox duplicate2;
	private JComboBox comparisonOperator;
	private JButton pasteButton;
	private JButton undoButton;
	private JButton andButton;
	private JButton orButton;
	private JButton notButton;
	private JButton testButton;
	private JButton helpButton;
	private JButton okButton;
	private JButton cancelButton;
	private JTextArea filterText;
	
	private static final String EQUALS = "=";
	private static final String LIKE = "like";
    private static final String IN = "in";
    private static final String ISNULL = "is null";
    private static final String BRACKETS = "< >";
    private static final String NOTLIKE = "not like";
    private static final String LESSTHAN = "<";
    private static final String GREATERTHAN = ">";
    private static final String LESSTHANOREQUALTHAN = "<=";
    private static final String GREATEROREQUALTHAN = ">=";
    
    private JTextArea filterToSet;
    
    SQLTable t;

	public FilterMakerFrame(JDialog parent, JTextArea filterToSet, SQLTable t) throws ArchitectException{
		super(parent);
        this.filterToSet = filterToSet;
        this.t = t;
		buildUI();                
		setTitle("Filter:");     
	}
	
	public FilterMakerFrame(JFrame parent, JTextArea filterToSet, SQLTable t) throws ArchitectException{
		super(parent);
        this.filterToSet = filterToSet;
        this.t = t;
		buildUI();                
		setTitle("Filter:");
	}

	public void buildUI() throws ArchitectException{

		FormLayout layout = new FormLayout(
				"4dlu,fill:min(70dlu;default), 4dlu, fill:150dlu:grow,4dlu, min(60dlu;default),4dlu",
				"10dlu,pref,4dlu,pref,4dlu,pref,4dlu,20dlu,4dlu,fill:30dlu:grow,10dlu,pref,10dlu");

		CellConstraints cc = new CellConstraints();

		PanelBuilder pb;
		JPanel p = logger.isDebugEnabled()  ? new FormDebugPanel(layout) : new JPanel(layout);

		pb = new PanelBuilder(layout,p);

		duplicate1 = new JComboBox(new ColumnComboBoxModel(t));
		duplicate2 = new JComboBox(new ColumnComboBoxModel(t));
		comparisonOperator = new JComboBox();
		pasteButton = new JButton(pasteAction);
		undoButton = new JButton(undoAction);
		andButton = new JButton(andAction);
		andButton.setSize(new Dimension(1,1));
		orButton = new JButton (orAction);
		orButton.setSize(new Dimension(1,1));
		notButton = new JButton(notAction);
		notButton.setSize(new Dimension(1,1));
		testButton = new JButton(testAction);
		helpButton = new JButton (helpAction);
		okButton = new JButton(okAction);
		cancelButton = new JButton(cancelAction);
		filterText = new JTextArea();

		pb.add(new JLabel("Duplicate1:"), cc.xy(2,2,"l,c"));
		pb.add(duplicate1, cc.xy(4,2,"f,c"));
		pb.add(new JLabel("Comparison Operator:"), cc.xy(2,4,"l,c"));
		pb.add(comparisonOperator, cc.xy(4,4));
		pb.add(new JLabel("Duplicate2:"), cc.xy(2,6,"l,c"));
		pb.add(duplicate2, cc.xy(4,6));

		pb.add(pasteButton, cc.xy(6,2,"r,c"));
		pb.add(undoButton, cc.xy(6,4,"r,c"));

		ButtonBarBuilder syntaxBar = new ButtonBarBuilder();
		syntaxBar.addGridded(andButton);
		syntaxBar.addRelatedGap();
		syntaxBar.addGridded(orButton);
		syntaxBar.addRelatedGap();
		syntaxBar.addGridded(notButton);
		syntaxBar.addRelatedGap();

		pb.add(syntaxBar.getPanel(), cc.xyw(2,8,3));
		pb.add(new JScrollPane(filterText), cc.xyw(2,10,5, "f,f"));

		ButtonBarBuilder bottomButtons = new ButtonBarBuilder();

		bottomButtons.addGridded(testButton);
		bottomButtons.addRelatedGap();		
		bottomButtons.addGridded(helpButton);
		bottomButtons.addRelatedGap();
		bottomButtons.addGlue();
		bottomButtons.addGridded(okButton);
		bottomButtons.addRelatedGap();
		bottomButtons.addGlue();
		bottomButtons.addGridded(cancelButton);
		bottomButtons.addRelatedGap();
		bottomButtons.addGlue();

		pb.add(bottomButtons.getPanel(), cc.xyw(2,12,5,"f,f"));
	
		
		setupOperatorDropdown();
		

		getContentPane().add(pb.getPanel());
	}
    
    /*
     * Adds the operators into the setupOperator Dropdown
     */
    private void setupOperatorDropdown(){
        comparisonOperator.addItem(EQUALS);
        comparisonOperator.addItem(LIKE);
        comparisonOperator.addItem(IN);
        comparisonOperator.addItem(ISNULL);
        comparisonOperator.addItem(BRACKETS);
        comparisonOperator.addItem(NOTLIKE);
        comparisonOperator.addItem(LESSTHAN);
        comparisonOperator.addItem(GREATERTHAN);
        comparisonOperator.addItem(LESSTHANOREQUALTHAN);
        comparisonOperator.addItem(GREATEROREQUALTHAN);
    }
    

    ////////////// The Action Variables//////////////////

	private Action pasteAction = new AbstractAction("Paste"){

		public void actionPerformed(ActionEvent e) {
			if (duplicate1.getSelectedItem() != null && duplicate2.getSelectedItem()!=null &&
					comparisonOperator!= null){
				StringBuffer textBuffer = new StringBuffer();
				String trimmedUpperCaseText = filterText.getText().trim().toUpperCase();
				if (!filterText.getText().trim().equals("") 
						&& !(trimmedUpperCaseText.endsWith(" AND") 
								|| trimmedUpperCaseText.endsWith(" OR")
								|| trimmedUpperCaseText.endsWith(" NOT"))) {
					textBuffer.append(" AND ");
				}
				textBuffer.append("RecDup1."+ duplicate1.getSelectedItem().toString());
				textBuffer.append(" " + comparisonOperator.getSelectedItem().toString()+ " ");
				textBuffer.append("RecDup2."+ duplicate2.getSelectedItem().toString());
				filterText.append(textBuffer.toString());
			}
		}
	};

	private Action undoAction = new AbstractAction("Undo"){

		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub

		}

	};

	private Action andAction = new AbstractAction("AND"){

		public void actionPerformed(ActionEvent e) {
			filterText.append(" AND ");
		}

	};

	private Action notAction = new AbstractAction("NOT"){

		public void actionPerformed(ActionEvent e) {
            filterText.append(" NOT ");
		}
	};

	private Action orAction = new AbstractAction("OR"){

		public void actionPerformed(ActionEvent e) {
		    filterText.append(" OR ");
		}
	};

	private Action testAction = new AbstractAction("Test"){

		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
		}
	};

	private Action helpAction = new AbstractAction("Help"){

		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
		}
	};

	private Action okAction = new AbstractAction("OK"){

		public void actionPerformed(ActionEvent e) {
			if (filterText.getText() != null){
				filterToSet.setText(filterText.getText());
				FilterMakerFrame.this.setVisible(false);
			}
		}
	};
	
	private Action cancelAction = new AbstractAction("Cancel"){

		public void actionPerformed(ActionEvent e) {
			setVisible(false);
		}
	};

}
