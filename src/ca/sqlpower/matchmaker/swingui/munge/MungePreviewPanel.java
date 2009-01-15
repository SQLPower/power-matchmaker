/*
 * Copyright (c) 2008, SQL Power Group Inc.
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

package ca.sqlpower.matchmaker.swingui.munge;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.LineBorder;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.munge.MungePreviewer;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.munge.MungePreviewer.PreviewEvent;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This is the view component of the {@link MungePreviewer}.
 */
public class MungePreviewPanel {
	private static final Logger logger = Logger.getLogger(MungePreviewPanel.class);
	
	/**
	 * The width of the border surrounding the panel the munge
	 * preview is in.
	 */
	private static final int BORDER_WIDTH = 5;
	
	/**
	 * A table model for the input and output tables.
	 */
	private class MSOTableModel implements TableModel {

		private List<TableModelListener> listeners;
		private Object[][] tableGrid;
		private int rows;
		private int cols;
		
		/**
		 * Text to append the column header with.
		 */
		private String columnHeader;
		
		public MSOTableModel(String colHeader, int rows, int cols) {
			listeners = new ArrayList<TableModelListener>();
			tableGrid = new Object[rows][cols];
			columnHeader = colHeader;
			this.rows = rows;
			this.cols = cols;
		}
		
		public void addTableModelListener(TableModelListener l) {
			listeners.add(l);
		}

		public Class<?> getColumnClass(int columnIndex) {
			return Object.class;
		}

		public int getColumnCount() {
			return cols;
		}

		public String getColumnName(int columnIndex) {
			return columnHeader + " " + (columnIndex + 1);
		}

		public int getRowCount() {
			return rows;
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			return tableGrid[rowIndex][columnIndex];
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}

		public void removeTableModelListener(TableModelListener l) {
			listeners.remove(l);
		}

		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			tableGrid[rowIndex][columnIndex] = value;
		}
		
	}
	
	private JPanel panel;
	private MungePreviewer previewer;
	private JTable inputTable;
	private JTable outputTable;
	
	/**
	 * The munge pen this previewer is attached to.
	 */
	private final MungePen mungePen;
	
	/**
	 * Tracks the last step to be modified in the model, or if a component
	 * was selected after modification then that component's model will be
	 * stored here. This will tell us where the preview should go and which
	 * step should be displayed.
	 */
	private MungeStep lastModifiedOrSelectedStep;
	
	/**
	 * This listener will wait for updates from the munge previewer and
	 * update it's input and output tables when the previewer receives
	 * a model change.
	 */
	private MungePreviewer.PreviewListener listener = new MungePreviewer.PreviewListener() {

		public void previewRefreshed(PreviewEvent evt) {
			stepSelectedOrModified(evt.getMungeStep());
		}

		public void previewDisabled(String reason) {
			panel.setVisible(false);
			JOptionPane.showMessageDialog(mungePen, "Preview is disabled. " + reason, "Preview Disabled", JOptionPane.INFORMATION_MESSAGE);
		}
		
	};

	/**
	 * This check box is used to enable and disable this preview panel. The checkbox
	 * should be placed somewhere the user can access it.
	 */
	private final JCheckBox enablePreviewCheckBox;
	
	public MungePreviewPanel(MungeProcess process, MungePen pen) {
		this.mungePen = pen;
		previewer = new MungePreviewer(process);
		previewer.addPreviewListener(listener);
		FormLayout layout = new FormLayout("pref, 4dlu, pref", "pref");
		panel = new JPanel(layout) {
			@Override
			public void paint(Graphics g) {
				AbstractMungeComponent lastModifiedMungeComp = mungePen.getMungeComponent(getLastModifiedOrSelectedStep());
				if (lastModifiedMungeComp != null) {
					Rectangle stepBounds = lastModifiedMungeComp.getBounds();
					setBounds(stepBounds.x + stepBounds.width, stepBounds.y, getPreferredSize().width, getPreferredSize().height);
				}
				g.setColor(getBackground());
	            g.fillRoundRect(0, 0, getWidth(), getHeight(), BORDER_WIDTH * 3, BORDER_WIDTH * 3);
	            super.paint(g);
			}
		};
		CellConstraints cc = new CellConstraints();
		inputTable = new JTable(new MSOTableModel("Input", 0, 0));
		outputTable = new JTable(new MSOTableModel("Output", 0, 0));
		JPanel inputPanel = new JPanel(new BorderLayout());
		inputPanel.add(inputTable, BorderLayout.CENTER);
		inputPanel.add(inputTable.getTableHeader(), BorderLayout.NORTH);
		panel.add(inputPanel, cc.xy(1, 1));
		
		JPanel outputPanel = new JPanel(new BorderLayout());
		outputPanel.add(outputTable, BorderLayout.CENTER);
		outputPanel.add(outputTable.getTableHeader(), BorderLayout.NORTH);
		panel.add(outputPanel, cc.xy(3, 1));
		
        panel.setOpaque(false);
        panel.setBackground(new Color(0x99333333, true));
        panel.setForeground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH));
        inputPanel.setOpaque(false);
        inputPanel.setBackground(null);
        inputPanel.setForeground(null);
        inputTable.setOpaque(false);
        inputTable.setBackground(null);
        inputTable.setForeground(null);
        inputTable.getTableHeader().setOpaque(false);
        inputTable.getTableHeader().setBackground(null);
        inputTable.getTableHeader().setForeground(null);
        
        outputPanel.setOpaque(false);
        outputPanel.setBackground(null);
        outputPanel.setForeground(null);
        outputTable.setOpaque(false);
        outputTable.setBackground(null);
        outputTable.setForeground(null);
        outputTable.getTableHeader().setOpaque(false);
        outputTable.getTableHeader().setBackground(null);
        outputTable.getTableHeader().setForeground(null);
        
        inputTable.getTableHeader().setBorder(new LineBorder(null));
        inputTable.setBorder(new LineBorder(null));
        
        inputTable.getTableHeader().setDefaultRenderer(new TableCellRenderer() {
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus,
					int row, int column) {
				return table.getCellRenderer(0, column).getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			}
		});
        
        outputTable.getTableHeader().setBorder(new LineBorder(null));
        outputTable.setBorder(new LineBorder(null));
        
        outputTable.getTableHeader().setDefaultRenderer(new TableCellRenderer() {
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus,
					int row, int column) {
				return table.getCellRenderer(0, column).getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			}
		});
        
        enablePreviewCheckBox = new JCheckBox(new AbstractAction("Show Preview") {
			public void actionPerformed(ActionEvent e) {
				enablePreview(enablePreviewCheckBox.isSelected());
				logger.debug("Show preview selected " + enablePreviewCheckBox.isSelected() + " is the preview enabled " + previewer.isRefreshEnabled());
				enablePreviewCheckBox.setSelected(previewer.isRefreshEnabled());
			}
		});
	}
	
	public void cleanup() {
		previewer.removePreviewListener(listener);
		previewer.cleanup();
	}
	
	public JPanel getPanel() {
		return panel;
	}
	
	public JCheckBox getEnablePreviewCheckBox() {
		return enablePreviewCheckBox;
	}
	
	public MungeStep getLastModifiedOrSelectedStep() {
		return lastModifiedOrSelectedStep;
	}

	/**
	 * If a step is selected or modified then the input and output tables in our
	 * preview will likely change and need to be updated.
	 */
	public void stepSelectedOrModified(MungeStep lastStep) {
		if (lastStep != null) {
			this.lastModifiedOrSelectedStep = lastStep;
		}
		
		logger.debug("Last step was " + lastModifiedOrSelectedStep);
		
		ArrayList<ArrayList> inputs = previewer.getPreviewInputForStep(lastModifiedOrSelectedStep);
		int colCount = 0;
		if (inputs != null && !inputs.isEmpty()) {
			if (inputs.get(0) != null) {
				colCount = inputs.get(0).size();
			}
			inputTable.setModel(new MSOTableModel("Input", inputs.size(), colCount));
			logger.debug("Adding " + inputs.size() + " to the input preview");
			int row = 0;
			for (ArrayList l : inputs) {
				int col = 0;
				for (Object o: l) {
					inputTable.getModel().setValueAt(o, row, col);
					col++;
				}
				row++;
			}
		} else {
			inputTable.setModel(new MSOTableModel("Input", 0, 0));
		}
		inputTable.revalidate();
		
		ArrayList<ArrayList> outputs = previewer.getPreviewOutputForStep(lastModifiedOrSelectedStep);
		colCount = 0;
		if (outputs != null && !outputs.isEmpty()) {
			if (outputs.get(0) != null) {
				colCount = outputs.get(0).size();
			}
			outputTable.setModel(new MSOTableModel("Output", outputs.size(), colCount));
			logger.debug("Adding " + outputs.size() + " to the output preview");
			int row = 0;
			for (ArrayList l : outputs) {
				int col = 0;
				for (Object o: l) {
					outputTable.getModel().setValueAt(o, row, col);
					col++;
				}
				row++;
			}
		} else {
			outputTable.setModel(new MSOTableModel("Output", 0, 0));
		}
		outputTable.revalidate();
		
		AbstractMungeComponent component = mungePen.getMungeComponent(lastModifiedOrSelectedStep);
		if (component != null) {
			panel.setBounds(component.getX() + component.getWidth(), component.getY(), panel.getPreferredSize().width, panel.getPreferredSize().height);
		}
		panel.repaint();
	}

	private boolean enablePreview(boolean enable) {
		previewer.setRefreshEnabled(enable);
		panel.setVisible(previewer.isRefreshEnabled());
		if (previewer.isRefreshEnabled()) {
			previewer.refreshPreview();
		}
		return previewer.isRefreshEnabled();
	}

}
