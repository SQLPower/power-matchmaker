package ca.sqlpower.matchmaker.swingui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;

import javax.swing.JPanel;

import ca.sqlpower.architect.swingui.ArchitectPanel;
import ca.sqlpower.architect.swingui.table.TableModelColumnAutofit;
import ca.sqlpower.architect.swingui.table.TableModelSortDecorator;
import ca.sqlpower.matchmaker.hibernate.PlMatchGroup;
import ca.sqlpower.matchmaker.hibernate.home.PlMatchGroupHome;

import javax.swing.JSlider;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class PlMatchGroupPanel extends JPanel implements ArchitectPanel {

	private static final long serialVersionUID = 1L;

	
	private PlMatchGroup model;


	private JSplitPane jSplitPane = null;


	private JScrollPane matchCriteriaScrollPane = null;


	private JTable matchCriteriaTable = null;


	/**
	 * This is the default constructor
	 */
	public PlMatchGroupPanel(PlMatchGroup model) {
		super();
		this.model = model;
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {

		this.setLayout(new BorderLayout());
		this.add(getJSplitPane(), BorderLayout.CENTER);
		
	}

	public PlMatchGroup getModel() {
		return model;
	}

	public void setModel(PlMatchGroup model) {
		this.model = model;
		TableModelColumnAutofit tableModelColumnAutofit = new TableModelColumnAutofit(new MatchCriteriaTableModel(model),matchCriteriaTable);
		matchCriteriaTable.setModel(tableModelColumnAutofit);
		tableModelColumnAutofit.setTableHeader(matchCriteriaTable.getTableHeader());
		tableModelColumnAutofit.initColumnSizes();
	}

	/**
	 * This method initializes jSplitPane	
	 * 	
	 * @return javax.swing.JSplitPane	
	 */
	private JSplitPane getJSplitPane() {
		if (jSplitPane == null) {
			jSplitPane = new JSplitPane();
			jSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
			jSplitPane.setBottomComponent(getMatchCriteriaScrollPane());
			jSplitPane.setTopComponent(new JPanel());
			
		}
		return jSplitPane;
	}

	/**
	 * This method initializes matchCriteriaScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getMatchCriteriaScrollPane() {
		if (matchCriteriaScrollPane == null) {
			matchCriteriaScrollPane = new JScrollPane(getMatchCriteriaTable());
		}
		return matchCriteriaScrollPane;
	}

	/**
	 * This method initializes matchCriteriaTable	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTable getMatchCriteriaTable() {
		if (matchCriteriaTable == null) {
			matchCriteriaTable = new JTable();
			matchCriteriaTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			setModel(model);
			
			matchCriteriaTable.setDefaultRenderer(Boolean.class,new CheckBoxRenderer());
			
		}
		return matchCriteriaTable;
	}

	public boolean applyChanges() {
		return true;
	}

	public void discardChanges() {
		
		
	}

	public JComponent getPanel() {
		return this;
	}
	class CheckBoxRenderer extends JCheckBox implements TableCellRenderer {

		  CheckBoxRenderer() {
		    setHorizontalAlignment(JLabel.CENTER);
		  }

		  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		    if (isSelected) {
		      setForeground(table.getSelectionForeground());
		      setBackground(table.getSelectionBackground());
		    } else {
		      setForeground(table.getForeground());
		      setBackground(table.getBackground());
		    }
		    setSelected((value != null && ((Boolean) value).booleanValue()));
		    return this;
		  }
		}

		Action action = new AbstractAction("CheckBox") {
		    
		    public void actionPerformed(ActionEvent evt) {
		        
		        JCheckBox cb = (JCheckBox)evt.getSource();

		        boolean isSel = cb.isSelected();
		        if (isSel) {
		            System.out.println("true");
		        } else {
		            System.out.println("false");
		        }
		    }
		};

} 
