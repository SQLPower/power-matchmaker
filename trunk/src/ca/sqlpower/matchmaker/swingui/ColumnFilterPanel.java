package ca.sqlpower.matchmaker.swingui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.ASUtils;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class ColumnFilterPanel extends JDialog {

	private static Logger logger = Logger.getLogger(ColumnFilterPanel.class);

	private JComboBox duplicate1;
	private JTextField conditionTextField;
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

	private JTable tableToSet;
	private SQLTable matchSourceTable;
	private List<ColumnFilterObject> filterList;

	/**
	 * This class should probably be refactoroed to be part of FilterMakerFrame
	 */
	public ColumnFilterPanel(JFrame parent, JTable tableToSet, SQLTable matchSourceTable) {
		super(parent, "Column Filter");
		this.tableToSet = tableToSet;
		this.matchSourceTable =  matchSourceTable;
		filterList = new ArrayList<ColumnFilterObject>();
		buildUI();
	}

	public void buildUI(){
		FormLayout layout = new FormLayout(
				"4dlu,fill:min(70dlu;default), 4dlu, fill:150dlu:grow,4dlu, min(60dlu;default),4dlu",
		"10dlu,pref,4dlu,pref,4dlu,pref,4dlu,20dlu,4dlu,fill:30dlu:grow,10dlu,pref,10dlu");

		CellConstraints cc = new CellConstraints();

		PanelBuilder pb;
		JPanel p = logger.isDebugEnabled()  ? new FormDebugPanel(layout) : new JPanel(layout);

		pb = new PanelBuilder(layout,p);

		try {
			duplicate1 = new JComboBox(new ColumnComboBoxModel(matchSourceTable));
		} catch (ArchitectException e) {
			ASUtils.showExceptionDialog(ColumnFilterPanel.this,
					"Unable to setup the column drop down box", e);
			return;
		}
		conditionTextField = new JTextField();
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
		filterText.setEditable(false);

		pb.add(new JLabel("Duplicate1:"), cc.xy(2,2,"l,c"));
		pb.add(duplicate1, cc.xy(4,2,"f,c"));
		pb.add(new JLabel("Comparison Operator:"), cc.xy(2,4,"l,c"));
		pb.add(comparisonOperator, cc.xy(4,4));
		pb.add(new JLabel("Duplicate2:"), cc.xy(2,6,"l,c"));
		pb.add(conditionTextField, cc.xy(4,6));

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

	private class ColumnFilterObject{
		private String columnName;
		private String operator;
		private String condition;

		private ColumnFilterObject(String columnName, String operator, String condition){
			this.columnName = columnName;
			this.operator = operator;
			this.condition = condition;
		}

		public String getColumnName() {
			return columnName;
		}

		public void setColumnName(String columnName) {
			this.columnName = columnName;
		}

		public String getCondition() {
			return condition;
		}

		public void setCondition(String condition) {
			this.condition = condition;
		}

		public String getOperator() {
			return operator;
		}

		public void setOperator(String operator) {
			this.operator = operator;
		}

	}

	////////////// The Action Variables//////////////////

	private Action pasteAction = new AbstractAction("Paste"){

		public void actionPerformed(ActionEvent e) {
			if (duplicate1.getSelectedItem() != null && conditionTextField.getText()!=null &&
					comparisonOperator!= null){
				StringBuffer textBuffer = new StringBuffer();
				String trimmedUpperCaseText = filterText.getText().trim().toUpperCase();
				if (!filterText.getText().trim().equals("")
						&& !(trimmedUpperCaseText.endsWith(" AND")
								|| trimmedUpperCaseText.endsWith(" OR")
								|| trimmedUpperCaseText.endsWith(" NOT"))) {
					textBuffer.append(" AND ");
				}
				ColumnFilterObject filterObj = new ColumnFilterObject(
						duplicate1.getSelectedItem().toString(),
						comparisonOperator.getSelectedItem().toString(),
						conditionTextField.getText());
				textBuffer.append(convertFilterToText(filterObj));
				filterText.append(textBuffer.toString());
				filterList.add(filterObj);
			}
		}
	};

	private String convertFilterToText(ColumnFilterObject obj){
		StringBuffer text = new StringBuffer();
		text.append(obj.getColumnName());
		text.append(" " + obj.getOperator());
		text.append(obj.getCondition());
		return text.toString();
	}

	//Not working properly yet
	private Action undoAction = new AbstractAction("Undo"){

		public void actionPerformed(ActionEvent e) {
			if (filterList.size() > 0){
				Document doc = filterText.getDocument();
				ColumnFilterObject obj = filterList.get(filterList.size()-1);
				try {
					int index = doc.getText(0,doc.getLength()).lastIndexOf(convertFilterToText(obj));
					if (index != -1){
						filterText.getDocument().remove(index, doc.getLength());
						filterList.remove(obj);
					}
				} catch (BadLocationException e1) {
					//Just don't undo
					logger.debug("Cannot undo");
					return;
				}
			}
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
			// TODO Auto-generated method stub
		}
	};

	private Action cancelAction = new AbstractAction("Cancel"){
		public void actionPerformed(ActionEvent e) {
			setVisible(false);
		}
	};


}
