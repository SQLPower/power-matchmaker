/*
 * Copyright (c) 2009, SQL Power Group Inc.
 *
 * This file is part of Power*MatchMaker.
 *
 * Power*MatchMaker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*MatchMaker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.matchmaker.swingui.address;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.io.File;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.antlr.runtime.RecognitionException;
import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.address.Address;
import ca.sqlpower.matchmaker.address.AddressDatabase;
import ca.sqlpower.matchmaker.address.AddressPool;
import ca.sqlpower.matchmaker.address.AddressResult;
import ca.sqlpower.matchmaker.swingui.MMSUtils;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.matchmaker.swingui.NoEditEditorPane;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.validation.ValidateResult;
import ca.sqlpower.validation.swingui.ValidateResultTableModel;

import com.sleepycat.je.DatabaseException;

public class AddressValidationPanel extends NoEditEditorPane {

    private static final Logger logger = Logger.getLogger(AddressValidationPanel.class);
    
    /**
     * A collection of invalid addresses
     */
    private Collection<AddressResult> addressResults;
    
    private AddressDatabase addressDatabase;
    
    /**
     * The horizontal split pane in the validation screen
     */
    private JSplitPane horizontalSplitPane; 
    
    /**
     * The vertical split pane, which is also the right component
     * of the horizontalSplitPane, in the validation screen
     */
    private JSplitPane verticalSplitPane;
    
    /**
     * The top component verticalSplitPane 
     */
    private JScrollPane validationSuggestionPane = new JScrollPane();
    
    /**
     * This is passed to a JScrollPane (problem pane), which is the bottom 
     * component of verticalSplitPane
     */
    private JTable problemTable;
    
    /**
     * The result after validation step
     */
    private List<ValidateResult> validateResult;
 
    /**
     * The flag to tell whether need to set the divider location or not
     */
    private boolean dividerLocationInitialized = false;
    
    public AddressValidationPanel(MatchMakerSwingSession session, Project project) {
		AddressPool pool = new AddressPool(project);
		try {
			addressDatabase = new AddressDatabase(new File(session.getContext().getAddressCorrectionDataPath()));
			pool.load(logger);
			addressResults = pool.getAddressResults(logger);

			JList needsValidationList = new JList(addressResults.toArray());
			needsValidationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			needsValidationList.addListSelectionListener(new AddressListCellSelectionListener());
			needsValidationList.setCellRenderer(new AddressListCellRenderer());
			JScrollPane addressPane = new JScrollPane(needsValidationList);
			addressPane.setPreferredSize(new Dimension(300, 50));
			addressPane.setMinimumSize(new Dimension(1,1));
			
	        horizontalSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, addressPane,
					new JLabel("To begin address validation, please select an address from the list.",	JLabel.CENTER));
			setPanel(horizontalSplitPane);
			//initialize the verticalSplitPane
			verticalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, validationSuggestionPane, new JScrollPane());
			
			verticalSplitPane.setResizeWeight(0.5);			
		} catch (SQLException e) {
			MMSUtils.showExceptionDialog(
							getPanel(),
							"A SQL Exception occured while trying to load the invalid addresses",
							e);
		} catch (SQLObjectException e) {
			MMSUtils.showExceptionDialog(
							getPanel(),
							"An error occured while trying to load the invalid addresses",
							e);
		} catch (DatabaseException e) {
			MMSUtils.showExceptionDialog(
					        getPanel(), 
					        "A database exception occured while trying to load the invalid addresses", 
					        e);
		} 
	}

	@Override
	public JSplitPane getPanel() {
		return (JSplitPane) super.getPanel();
	}

	class AddressListCellRenderer extends JLabel implements ListCellRenderer {

		final ImageIcon canadaIcon = new ImageIcon(AddressValidationPanel.class.getResource("icons/Canada.png"));
		
		public AddressListCellRenderer() {
			setOpaque(true);
		}
		
		public Component getListCellRendererComponent(final JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {

			LineBorder lineBorder = new LineBorder(Color.LIGHT_GRAY, 2, true) {
				int inset = this.getThickness()+3;
				@Override
				public Insets getBorderInsets(Component c) {
					return new Insets(inset,inset,inset,inset);
				}
				@Override
				public Insets getBorderInsets(Component c, Insets insets) {
					insets.top = insets.bottom = insets.left = insets.right = this.inset;
					return insets;
				}
				@Override
				public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
			        Color oldColor = g.getColor();
			        int i;

			        g.setColor(lineColor);
			        for(i = 0; i < thickness; i++)  {
			        	g.drawRoundRect(x+i, y+i, width-i-i-1, height-i-i-1, 10*thickness, 10*thickness);
			        }
			        g.setColor(oldColor);
			    }
			};
			
			EmptyBorder emptyBorder = new EmptyBorder(3,4,3,4);
			CompoundBorder border = new CompoundBorder(emptyBorder, lineBorder);
			setBorder(border);
			
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			setEnabled(list.isEnabled());
			setFont(list.getFont());
			
			AddressResult address = (AddressResult)value;
			setText(address.htmlToString());
			if (address.getCountry().equals("Canada")) {
				setIcon(canadaIcon);
			}
			
			return this;
		}
	}
	
	class AddressListCellSelectionListener implements ListSelectionListener {

		public void valueChanged(ListSelectionEvent e) {
			
			//remember user's choice of the divider's location
			horizontalSplitPane.setDividerLocation(horizontalSplitPane.getDividerLocation());
			
			horizontalSplitPane.setRightComponent(verticalSplitPane);
			
			if (!dividerLocationInitialized) {
				verticalSplitPane.setDividerLocation((int)(0.8*horizontalSplitPane.getBounds().height));
				dividerLocationInitialized = true;
			} 
			
			verticalSplitPane.setDividerLocation(verticalSplitPane.getDividerLocation());
			
			AddressResult selected = (AddressResult) ((JList)e.getSource()).getSelectedValue();
			try {
				Address address1 = Address.parse(selected.getAddressLine1(),
						selected.getMunicipality(), selected.getProvince(),
						selected.getPostalCode(), selected.getCountry());
				validateResult = addressDatabase.correct(address1);
				problemTable = new JTable(new ValidateResultTableModel(validateResult));
				verticalSplitPane.setBottomComponent(new JScrollPane(problemTable));
				verticalSplitPane.getBottomComponent().setMinimumSize(new Dimension(1, 1));

				problemTable.getTableHeader().setResizingAllowed(true);
				problemTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
				TableColumnModel column = problemTable.getColumnModel();
				// set the prefered width of the columns 
				column.getColumn(0).setPreferredWidth(140);
				column.getColumn(1).setPreferredWidth(verticalSplitPane.getBounds().width - 150);
				
				column.getColumn(0).setCellRenderer(new TableRenderer());
				column.getColumn(1).setCellRenderer(new TableRenderer());
				
			} catch (RecognitionException e1) {
				MMSUtils.showExceptionDialog(getPanel(), 
						"There was an error while trying to parse this address", 
						e1);
			}
			
		}
		
	}
	
	class TableRenderer extends JLabel implements TableCellRenderer {
		
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			setHorizontalAlignment(CENTER);
			String str = (String)value;
			if (str.equals("WARN")) {
				setIcon(new ImageIcon(AddressValidationPanel.class.getResource("icons/warn.png")));
				setText("Warning");
			} else if (str.equals("FAIL")) {
				setIcon(new ImageIcon(AddressValidationPanel.class.getResource("icons/fail.png")));
				setText("Failure");
			} else {
				setText(str);
			}
			return this;
		}
	}

}
