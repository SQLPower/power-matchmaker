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

package ca.sqlpower.matchmaker.swingui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.matchmaker.PotentialMatchRecord;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.SourceTableRecord;

public class SourceTableRecordViewer {

	/**
	 * A panel whose preferred width is never less than
	 * {@link #toolBarMinWidth}.
	 */
	private class RecordViewerPanel extends JPanel {
		@Override
		public Dimension getPreferredSize() {
			Dimension ps = super.getPreferredSize();

			ps.width = Math.max(toolBarMinWidth, ps.width);

			return ps;
		}
	}

	/**
	 * The maximum width for a column on the Match Table.
	 */
	private static final int MAX_COLUMN_WIDTH = 200;
	
	/**
     * The panel that contains all the column values of a single
     * record from the project's source table.
     */
	private final JToolBar toolBar;

	/**
     * The panel that contains all the column values of a single
     * record from the project's source table.
	 */
	private final RecordViewerPanel panel;

	/**
     * The natural preferred width of this record viewer's button panel.
     * This is our minimum preferred width constraint for the source table
     * record panel itself.
	 */
	private int toolBarMinWidth;

	/**
	 * Label to be shown if there is no node selected.
	 */
	private static final JLabel NO_NODE_SELECTED_LABEL = new JLabel("Please select a node in the graph to see the" +
																	" contents of its source table record.", JLabel.CENTER); 

	/**
	 * Label to be shown is there is no column selected.
	 */
	private static final JLabel NO_COLUMN_SELECTED_LABEL = new JLabel("No columns are selected to be shown!", JLabel.CENTER);

	/**
	 * The string we show for null values.
	 */
	private static final String NULL_STRING = "(null)";

	private static final Logger logger = Logger.getLogger(SourceTableRecordViewer.class);

	public SourceTableRecordViewer(SourceTableRecord view,
			SourceTableRecord master, List<Action> buttonActions,
			List<SQLColumn> shownColumns, int buffer) throws ArchitectException,
			SQLException {
		panel = new RecordViewerPanel();
		panel.setLayout(new GridLayout(0, 1));

		logger.debug("Creating source table record viewer for " + master);

		panel.setBackground(Color.WHITE);

		toolBar = new JToolBar();
		toolBar.setFloatable(false);

		
		for (Action a : buttonActions) {
			toolBar.add(a);
		}

		toolBarMinWidth = toolBar.getPreferredSize().width;

		JLabel label = new JLabel(); // just a label to read defaults from
		Color foreground = label.getForeground();
		Font font = label.getFont();
		Font sameFont = font.deriveFont(Font.PLAIN);
		Color sameBackground = Color.WHITE;
		if (view != master) {
			PotentialMatchRecord pmr = view.getMatchRecordByOriginalAdjacentSourceTableRecord(master);
			if (pmr == null || pmr.getMungeProcess() == null) {
				sameBackground = Color.WHITE;
			} else {
				sameBackground = shallowerColor(pmr.getMungeProcess().getColour());
			}
		}
		Font differentFont = font.deriveFont(Font.BOLD);
		Color differentBackground = deeperColor(sameBackground);

		List<Object> viewList = view.fetchValues(shownColumns);
		List<Object> masterList = master.fetchValues(shownColumns);
		
		// no need to do anything if either list are empty
		if (!(viewList.isEmpty() || masterList.isEmpty())) {
			Iterator<Object> viewIt = viewList.iterator();
			Iterator<Object> masterIt = masterList.iterator();
			boolean darkRow = false;
			while (viewIt.hasNext()) {
				Object viewVal = viewIt.next();
				Object masterVal = masterIt.next();

				boolean same;

				if (viewVal == null) {
					same = masterVal == null;
				} else {
					same = viewVal.equals(masterVal);
				}

				JLabel colValueLabel;
				if (viewVal == null) {
					colValueLabel = new JLabel(NULL_STRING);
        			colValueLabel.setForeground(darkRow ? darkerColor(Color.gray) : Color.gray);
				} else {
					colValueLabel = new JLabel(viewVal.toString());
        			colValueLabel.setForeground(darkRow ? darkerColor(foreground) : foreground);
				}

				if (same) {
					colValueLabel.setFont(sameFont);
        			colValueLabel.setBackground(darkRow ? darkerColor(sameBackground) : sameBackground);
				} else {
					colValueLabel.setFont(differentFont);
        			colValueLabel.setBackground(darkRow ? darkerColor(differentBackground) : differentBackground);
				}

				colValueLabel.setOpaque(true);
				panel.add(colValueLabel);
				darkRow = !darkRow;
			}
		}

		// Calculates the column width with a maximum size of MAX_COLUMN_WIDTH
		int colWidth = Math.min(Math.max(toolBarMinWidth, panel
				.getPreferredSize().width), MAX_COLUMN_WIDTH);
		
		// Add the buffer onto the width so the panel and toolbar line up.
		toolBar.setPreferredSize(new Dimension(colWidth + buffer, toolBar.getPreferredSize().height));
		panel.setPreferredSize(new Dimension(colWidth, panel.getPreferredSize().height));
		logger.debug("toolBar min width is " + toolBarMinWidth + "; actual is " + toolBar.getPreferredSize().width);
	}

	public JPanel getPanel() {
		return panel;
	}

	public JToolBar getToolBar() {
		return toolBar;
	}

	/**
     * Creates the panel of column names that should appear along
     * the left-hand side of a grid of SourceTableRecord panels using 
     * the given list.  If the list is null, then all the columns will
     * appear.
	 */
    public static JPanel headerPanel(Project project, List<SQLColumn> shownColumns) throws ArchitectException {
		JPanel panel = new JPanel(new GridLayout(0, 1));
		panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.BLACK));

		Color baseBg = darkerColor(Color.WHITE);
		Color darkBg = darkerColor(baseBg);

		boolean darkRow = false;
		if (shownColumns == null) {
			for (SQLColumn col : project.getSourceTable().getColumns()) {
				JLabel label = new JLabel(col.getName());
				label.setOpaque(true);
				label.setBackground(darkRow ? darkBg : baseBg);
				panel.add(label);
				darkRow = !darkRow;
			}
		} else {
			for (SQLColumn col : shownColumns) {
				JLabel label = new JLabel(col.getName());
				label.setOpaque(true);
				label.setBackground(darkRow ? darkBg : baseBg);
				panel.add(label);
				darkRow = !darkRow;
			}
		}
		return panel;
	}

	/**
     * A much less severe version of Color.darker().  Adapted from the
     * Sun JDK version of Color.darker().
	 * 
     * @param c The colour to create a darker version of.  This object is
     * not affected by this call.
	 * @return A slightly darker shade of the given colour.
	 */
	private static Color darkerColor(Color c) {
		final float FACTOR = 0.97f;
        return new Color(
                Math.max((int) (c.getRed() * FACTOR), 0),
                Math.max((int) (c.getGreen() * FACTOR), 0),
                Math.max((int) (c.getBlue() * FACTOR), 0),
                Math.min(c.getAlpha() + 15, 255));
	}

	/**
	 * Returns a colour that is less transparent than the given.
	 */
	private static Color deeperColor(Color c) {
		final float FACTOR = 2.5f;
    	return new Color (c.getRed(), c.getGreen(), c.getBlue(), (int) Math.min(c.getAlpha() * FACTOR, 255));
	}

	/**
	 * Returns a colour that is more transparent than the given.
	 */
	private static Color shallowerColor(Color c) {
		final int FACTOR = 70;
		return new Color(c.getRed(), c.getGreen(), c.getBlue(), FACTOR);
	}

	public static JLabel getNoNodeSelectedLabel() {
		return NO_NODE_SELECTED_LABEL;
	}

	public static JLabel getNoColumnSelectedLabel() {
		return NO_COLUMN_SELECTED_LABEL;
	}
}
