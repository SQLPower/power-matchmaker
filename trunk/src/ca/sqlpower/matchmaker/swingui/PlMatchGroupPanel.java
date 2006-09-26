package ca.sqlpower.matchmaker.swingui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellRenderer;

import org.hibernate.Transaction;

import ca.sqlpower.architect.swingui.ArchitectPanel;
import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.matchmaker.hibernate.PlMatchCriteria;
import ca.sqlpower.matchmaker.hibernate.PlMatchGroup;
import ca.sqlpower.matchmaker.util.HibernateUtil;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;


public class PlMatchGroupPanel extends JPanel implements ArchitectPanel {

	private static final long serialVersionUID = 1L;

	
	private PlMatchGroup model;


	private JSplitPane jSplitPane = null;


	private JScrollPane matchCriteriaScrollPane = null;


	private JTable matchCriteriaTable = null;


	private JPanel groupEditPanel;
	JLabel groupId;
	JLabel matches;
	JTextField description;
	JTextField matchPercent;
	JTextField filterCriteria;
	JCheckBox active;
	JLabel	lastUpdateDate;
	JLabel	lastUpdateUser;
	JLabel	lastUpdateOSUser;
	Color textBackground;


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
		matchCriteriaTable.setModel(new MatchCriteriaTableModel(model));
	
		
		loadMatches(model);
	}

	private void loadMatches(PlMatchGroup model) {
		// load the new model
		KeyListener listener = new KeyListener(){
			
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				validateForm();	
			}
			
			public void keyTyped(KeyEvent e) {
				validateForm();				
			}
			
		};
		groupId = new JLabel(model.getId().getGroupId());
		matches = new JLabel(model.getId().getMatchId());

		description = new JTextField(model.getDescription());
		textBackground = description.getBackground();
		matchPercent = new JTextField(model.getMatchPercent().toString());
		matchPercent.addKeyListener(listener);
		filterCriteria = new JTextField(model.getFilterCriteria());
		active = new JCheckBox();
		active.setSelected(model.isActiveInd());
		Date updated = model.getLastUpdateDate();
		
		lastUpdateDate = new JLabel(updated == null? "N/A" :updated.toString());
		lastUpdateUser = new JLabel(model.getLastUpdateUser() == null? "N/A" :model.getLastUpdateUser());
		lastUpdateOSUser= new JLabel(model.getLastUpdateOsUser()== null?"N/A":model.getLastUpdateOsUser());
	}
	
	private boolean validateForm(){
		Boolean valid = true;
		
		try {
			Short d = Short.parseShort(matchPercent.getText());
			
			if (d >= 0 && d <= 100){
				matchPercent.setBackground(textBackground);
			} else {
				valid = false;
				matchPercent.setBackground(Color.red);
			}
			
		} catch (NumberFormatException e){
			valid = false;
			matchPercent.setBackground(Color.red);
		}

		return valid;
		
	}
	private boolean saveMatches(PlMatchGroup model) {
		if ( validateForm()){
			// load the new model
			model.setDescription(description.getText());
			model.setMatchPercent(Short.parseShort(matchPercent.getText()));
			model.setFilterCriteria(filterCriteria.getText());
			model.setActiveInd(active.isSelected());
	
			try {
				System.out.println("Saving "+model);
				Transaction tx = HibernateUtil.primarySession().beginTransaction();
				HibernateUtil.primarySession().flush();
				tx.commit();
			
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return validateForm();
	}

	private JPanel getGroupEditPanel(){
		if (groupEditPanel == null){
			FormLayout formLayout = new FormLayout("3dlu, pref, 5dlu, fill:200dlu:grow, 3dlu");
			PanelBuilder pb = new PanelBuilder(formLayout);
			pb.appendRelatedComponentsGapRow();
			pb.appendRow("pref");
			CellConstraints cc = new CellConstraints();
			CellConstraints cl= new CellConstraints();
			pb.add(new JLabel("Match"), cl.xy(2,2),matches, cc.xy(4,2));
			pb.appendRelatedComponentsGapRow();
			pb.appendRow("pref");
			pb.add(new JLabel("Match Group"), cl.xy(2,4), groupId, cc.xy(4,4));
			pb.appendRelatedComponentsGapRow();
			pb.appendRow("pref");
			pb.add(new JLabel("Description"), cl.xy(2,6),description , cc.xy(4,6));
			pb.appendRelatedComponentsGapRow();
			pb.appendRow("pref");
			pb.add(new JLabel("Match Percent"), cl.xy(2,8),matchPercent, cc.xy(4,8));
			pb.appendRelatedComponentsGapRow();
			pb.appendRow("pref");
			pb.add(new JLabel("Filter Criteria"), cl.xy(2,10),filterCriteria, cc.xy(4,10));
			pb.appendRelatedComponentsGapRow();
			pb.appendRow("pref");
			pb.add(new JLabel("Deactivate?"), cl.xy(2,12),active, cc.xy(4,12));
			pb.appendRelatedComponentsGapRow();
			pb.appendRow("pref");
			pb.add(new JLabel("Last Updated"), cl.xy(2,14),lastUpdateDate, cc.xy(4,14));
			pb.appendRelatedComponentsGapRow();
			pb.appendRow("pref");
			pb.add(new JLabel("Last Updated By"), cl.xy(2,16),lastUpdateUser, cc.xy(4,16));
			pb.appendRelatedComponentsGapRow();
			pb.appendRow("pref");
			pb.add(new JLabel("Last Updated OS User"), cl.xy(2,18),lastUpdateOSUser, cc.xy(4,18));
			groupEditPanel = pb.getPanel();
		}
		return groupEditPanel;
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
			jSplitPane.setTopComponent(getGroupEditPanel());
			
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
			matchCriteriaTable.addMouseListener(new MouseListener(){

				private Window getWindow() {
					Component c = source;
					while(!(c instanceof Window ) && c !=null){
						c = c.getParent();
					}
					return (Window) c;
				}
				
				public void mouseClicked(MouseEvent e) {
					// TODO Auto-generated method stub
					clickAction(e);
					
				}

				public void mouseEntered(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}

				public void mouseExited(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}

				public void mousePressed(MouseEvent e) {

				}

				public void mouseReleased(MouseEvent e) {

				}
				JPopupMenu m;
				Component source;
				
				private void clickAction(MouseEvent e) {
					
					if (e.getButton() == e.BUTTON1 && e.getClickCount() == 2) {
						m = new JPopupMenu();
						JTable t = (JTable) e.getSource();
						source = t;
						int row = t.rowAtPoint(e.getPoint());
						MatchCriteriaTableModel tableModel = (MatchCriteriaTableModel) t.getModel();
						if (row < 0) {
							PlMatchCriteria newCriteria = new PlMatchCriteria();
							newCriteria.setPlMatchGroup(model);
							Action a = new EditMatchCriteriaAction(newCriteria,getWindow());
							a.actionPerformed(new ActionEvent(this,0,""));;
							
						}else {
							Action a = new EditMatchCriteriaAction(tableModel.getRow(row),getWindow());
							a.actionPerformed(new ActionEvent(this,0,""));
						}
					}
				}
				
			});
			
		}
		return matchCriteriaTable;
	}

	public boolean applyChanges() {
		return saveMatches(model);
	}

	public void discardChanges() {

	}

	public JComponent getPanel() {
		return this;
	}
	private class EditMatchCriteriaAction extends AbstractAction {
		private EditMatchCriteriaAction(String name) {
			super(name);
		}

		public EditMatchCriteriaAction(PlMatchCriteria criteria, Window w) {
			
			this("Edit Match Criterion");
			window = w;
			this.criteria = criteria;
		}

		private PlMatchCriteria criteria;
		private Window window;
	

		public void actionPerformed(ActionEvent e) {
			JDialog d = ArchitectPanelBuilder.createArchitectPanelDialog(new PlMatchCriteriaPanel(criteria), window, "Edit Match Criterion", "Save Match Criterion");
			d.setVisible(true);
			
		}
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
