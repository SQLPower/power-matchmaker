/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of DQguru
 *
 * DQguru is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DQguru is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.matchmaker.swingui.engine;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MungeProcessPriorityComparator;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.munge.MungeProcess;

import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public abstract class MungeProcessSelectionList extends JButton {

	private static final Logger logger = Logger.getLogger(MungeProcessSelectionList.class);
	
	/**
	 * The actual JList of munge processes.
	 */
	private JList processesList;
	
	/**
	 * The scrollpane containing the JList of munge processes.
	 */
	private JScrollPane processesPane;
	
	/**
	 * The project of which the Munge Processes will be retrieved from.
	 */
	private Project project;
	
	/**
	 * The popup menu that contains the list of Munge Processes.
	 */
	private JPopupMenu popupMenu;
	
	/**
	 * The list of Munge Process to be selected.
	 */
	private List<MungeProcess> mps;
	
	/**
	 * The action which is run when the selection window is closed.
	 * It can be null.
	 */
	private Runnable closeAction;
	
	public MungeProcessSelectionList(Project project) {
		super();
		this.project = project;
		buildPopupMenu();
		addActionListener(new AbstractAction(){
			public void actionPerformed(ActionEvent e) {
				refreshList();
				popupMenu.show(MungeProcessSelectionList.this, 0, 0);
			}
		});
	}
	
	/** 
	 * This sets the popup button text according to the number
	 * of munge processes selected.
	 */
	private void setPopupButtonText() {
		int count = processesList.getSelectedIndices().length;
		
		if (count == 0) {
			setText("Choose Transformations (None Selected)...");
		} else if (count == mps.size()) {
			setText("Choose Transformations (All Selected)...");
		} else {
			setText("Choose Transformations (" + count + " Selected)...");
		}
	}

	/**
	 * Builds and returns the popup menu for choosing the munge processes. 
	 */
	private void buildPopupMenu() {
		if (mps == null) {
			mps = new ArrayList<MungeProcess>();
		} else {
			mps.clear();
		}
		for(MungeProcess mp : project.getMungeProcesses()) {
			mps.add(mp);
		}
		popupMenu = new JPopupMenu("Choose Processes");
		
		popupMenu.setBorder(BorderFactory.createRaisedBevelBorder());
		
		final JButton selectAll = new JButton("Select All");
		selectAll.addActionListener(new AbstractAction(){
			public void actionPerformed(ActionEvent e) {
				processesList.setSelectionInterval(0, mps.size()-1);
			}			
		});
		
		
		final JButton unselectAll = new JButton(new AbstractAction("Unselect All"){
			public void actionPerformed(ActionEvent e) {
				processesList.clearSelection();
			}			
		});
		
		final JButton close = new JButton(new AbstractAction("OK"){
			public void actionPerformed(ActionEvent e) {
				popupMenu.setVisible(false);
				if (closeAction != null) {
					closeAction.run();
				}
			}
		});
		
		FormLayout layout = new FormLayout("10dlu,pref,10dlu",
				"4dlu,pref,4dlu,pref,4dlu,pref,4dlu");
		JPanel menu = logger.isDebugEnabled() ? new FormDebugPanel(layout) : new JPanel(layout);
		
		JPanel top = new JPanel(new FlowLayout());
		top.add(selectAll);
		top.add(unselectAll);

		CellConstraints cc = new CellConstraints();
		
		int row = 2;
		menu.add(top, cc.xy(2, row));		
		
		row += 2;
		Collections.sort(mps, new MungeProcessPriorityComparator());
		processesList = new JList(mps.toArray());
		processesList.setSelectedIndices(getSelectedIndices());
		
		processesList.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				applyChanges();
			}
		});
		
		processesPane = new JScrollPane(processesList);
		processesPane.setPreferredSize(new Dimension(160, 100));
		menu.add(processesPane, cc.xy(2, row));
		
		row += 2;
		JPanel tmp = new JPanel(new FlowLayout());
		tmp.add(close);
		menu.add(tmp, cc.xy(2 ,row));
		
		popupMenu.add(menu);

		setPopupButtonText();
	}
	
	/**
	 * Refreshes the list so it will contain newly created Munge Processes.
	 */
	public void refreshList() {
		mps.clear();
		for(MungeProcess mp : project.getMungeProcesses()) {
			mps.add(mp);
		}
		Collections.sort(mps, new MungeProcessPriorityComparator());
		processesList.setListData(mps.toArray());
		processesList.setSelectedIndices(getSelectedIndices());
		setPopupButtonText();
	}
	
	/**
	 * Used to keep the UI in synch with the model
	 */
	public void checkModel() {
		processesList.setSelectedIndices(getSelectedIndices());
		setPopupButtonText();
	}
	
	/** 
	 * Returns an int[] of the active munge processes.
	 */
	private int[] getSelectedIndices() {
		int count = 0;
		int index = 0;
		int[] indices;
		
		// This determines the size required for the array
		for (MungeProcess mp : mps) {
			if (getValue(mp)) {
				count++;
			} 
		}
		indices = new int[count];
		count = 0;
		
		// This fills in the array with the active indices.
		// A List.toArray() was not used instead because it
		// returns a Integer[] instead of a int[].
		for (MungeProcess mp : mps) {
			if (getValue(mp)) {
				indices[count++] = index;
			} 
			index++;
		}
		return indices;
	}
	
	public void applyChanges() {
		int index = 0;
		project.getMungeSettings().begin("Commiting the active properties to munge steps");
		for (MungeProcess mp : mps) {
			setValue(mp, processesList.isSelectedIndex(index));
			index++;
		}
		project.getMungeSettings().commit();
		setPopupButtonText();
	}
	
	/**
	 * Call to be invoked whenever the selection window is closed.
	 */
	public abstract void setValue(MungeProcess mp, boolean value);
	
	/**
	 * Call to be invoked whenever the list of munge processes is refreshed.
	 */
	public abstract boolean getValue(MungeProcess mp);
	
	/**
	 * Sets the action to be called when the window is closed.
	 */
	public void setCloseAction(Runnable closeAction) {
		this.closeAction = closeAction; 
	}
	
}
