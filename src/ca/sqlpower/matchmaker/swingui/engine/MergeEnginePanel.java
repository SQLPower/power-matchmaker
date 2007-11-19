/*
 * Copyright (c) 2007, SQL Power Group Inc.
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

package ca.sqlpower.matchmaker.swingui.engine;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerEngine;
import ca.sqlpower.matchmaker.MergeEngineImpl;
import ca.sqlpower.matchmaker.MergeSettings;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.dao.MatchMakerDAO;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.swingui.BrowseFileAction;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;
import ca.sqlpower.validation.swingui.FormValidationHandler;
import ca.sqlpower.validation.swingui.StatusComponent;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * A panel that provides a GUI for setting the parameters for running the Merge engine,
 * as well as running the Merge engine itself and displaying its output on the GUI.
 */
public class MergeEnginePanel implements DataEntryPanel {

	private static final Logger logger = Logger.getLogger(MergeEnginePanel.class);

	/**
	 * The session this MergeEnginePanel belongs to.
	 */
	private MatchMakerSwingSession swingSession;

	/**
	 * The file path to which the engine logs will be written to.
	 * Must be a valid file path that the user has write permissions on.
	 */
	private JTextField logFilePath;

	/**
	 * Opens a file chooser for the user to select the log file they wish
	 * to use for engine output.
	 */
	private BrowseFileAction browseLogFileAction;

	/**
	 * Denotes whether or not the log file should be overwritten or
	 * appended to.
	 */
	private JCheckBox appendToLog;
	
	/**
	 * A field for the user to specify how many records they want the
	 * engine to process.
	 */
	private JSpinner recordsToProcess;
	
	/**
	 * A flag for the engine to run in debug mode or not.
	 */
	private JCheckBox debugMode;
	
	/**
	 * The frame that this editor lives in.
	 */
	private JFrame parentFrame;

	/**
	 * The project object the engine should run against.
	 */
	private Project project;
	
	/**
	 * The panel that displays all the information for the merge engine.
	 */
	private JPanel panel;
	
	/**
	 * Displays the validation status of the match engine preconditions.
	 */
	private StatusComponent status = new StatusComponent();

	/**
	 * The validation handler used to validate the configuration portion
	 * of the editor pane.
	 */
	private FormValidationHandler handler;
	
	/**
	 * The collection of components that show the user what the engine is doing.
	 */
	private final EngineOutputPanel engineOutputPanel;
	
	/**
	 * An action to run the engine and print the output to the engineOutputPanel
	 */
	private Action runEngineAction;

	/**
	 * The merge engine for this panel
	 */
	private MatchMakerEngine engine;

	/**
	 * Keeps track of which level to show the logger info to the panel
	 */
	private JComboBox messageLevel;

	/**
	 * The abort button
	 */
	private JButton abortB;
	
	/**
	 * The action that is called when the engine is finished
	 */
	private Runnable engineFinish = new Runnable(){
		public void run() {
			abortB.setEnabled(false);
		}
	};
	
	/**
	 * The action that is called when the engine is started
	 */
	private Runnable engineStart = new Runnable(){
		public void run() {
			abortB.setEnabled(true);
		}
	};
	
	public MergeEnginePanel(MatchMakerSwingSession swingSession, Project project, JFrame parentFrame) {
		this.swingSession = swingSession;
		this.parentFrame = parentFrame;
		this.project = project;
		handler = new FormValidationHandler(status);
		handler.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				refreshActionStatus();
			}
		});
		this.engineOutputPanel = new EngineOutputPanel(parentFrame);
		this.engine = new MergeEngineImpl(swingSession, project);
		this.runEngineAction = new RunEngineAction(swingSession, engine, "Run Merge Engine", engineOutputPanel, this, engineStart, engineFinish);
		this.panel = buildUI();
	}
	
	/**
	 *	Enables/Disables the run engine action. The action will
	 *	only be enabled if the form status is not fail. 
	 * {@link MatchMakerSwingSession#isEnginesEnabled()} should
	 *	be checked before calling this method.
	 */
	public void setEngineEnabled(boolean enabled) {
		ValidateResult worst = handler.getWorstValidationStatus();
		if (worst.getStatus() == Status.FAIL) {
			runEngineAction.setEnabled(false);
		} else {
			runEngineAction.setEnabled(enabled);
		}
	}
	
	/**
	 * Performs a form validation on the configuration portion and sets the
	 * status accordingly as well as disabling the button to run the engine if
	 * necessary.
	 */
	private void refreshActionStatus() {
		ValidateResult worst = handler.getWorstValidationStatus();
		runEngineAction.setEnabled(true);
		
		if (worst.getStatus() == Status.FAIL || !swingSession.isEnginesEnabled()) {
			runEngineAction.setEnabled(false);
		}
	}
	
	/**
	 * An action that just calls {@link #doSave}.
	 */
	private final class SaveAction extends AbstractAction {
		private SaveAction() {
			super("Save");
		}

		public void actionPerformed(ActionEvent e) {
			applyChanges();
		}
	}
	
	/**
	 * Builds the UI for this editor pane. This is broken into two parts,
	 * the configuration and output. Configuration is done in this method
	 * while the output section is handled by the EngineOutputPanel and
	 * this method simply lays out the components that class provides.
	 */	
	private JPanel buildUI() {
		FormLayout layout = new FormLayout(
				"4dlu,fill:pref,4dlu,fill:pref:grow, pref,4dlu,pref,4dlu",
				//  1         2    3         4     5     6    7     8
				"10dlu,pref,10dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu");
		        //   1    2     3    4    5    6    7    8    9   10   11   12   13   14   15   16   17   18   19   20   21
		PanelBuilder pb;
		JPanel p = logger.isDebugEnabled() ? new FormDebugPanel(layout)
				: new JPanel(layout);
		pb = new PanelBuilder(layout, p);

		CellConstraints cc = new CellConstraints();

		MergeSettings settings = project.getMergeSettings();
		
		if (settings.getLog() == null) {
			settings.setLog(new File(project.getName() + ".log"));
		}
		
		File logFile = settings.getLog();
		logFilePath = new JTextField(logFile.getAbsolutePath());
		handler.addValidateObject(logFilePath, new LogFileNameValidator());
		
		browseLogFileAction = new BrowseFileAction(parentFrame, logFilePath);
		
		appendToLog = new JCheckBox("Append to old Log File?", settings.getAppendToLog());
		
		recordsToProcess = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 100));
		if (settings.getProcessCount() != null) {
			recordsToProcess.setValue(settings.getProcessCount());
		}
		
		debugMode = new JCheckBox("Debug Mode?", settings.getDebug());
		debugMode.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (((JCheckBox) e.getSource()).isSelected()) {
					recordsToProcess.setValue(new Integer(1));
					engine.setMessageLevel(Level.ALL);
					messageLevel.setSelectedItem(engine.getMessageLevel());
				} else {
					recordsToProcess.setValue(new Integer(0));
				}
			}
		});
		
		messageLevel = new JComboBox(new Level[] {Level.ALL, Level.DEBUG, Level.ERROR, Level.FATAL, Level.INFO, Level.OFF, Level.WARN});
		messageLevel.setSelectedItem(engine.getMessageLevel());
		messageLevel.setRenderer(new DefaultListCellRenderer(){
			@Override
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				setText(value.toString());
				return this;
			}
		});
		
		messageLevel.addActionListener(new AbstractAction(){
			public void actionPerformed(ActionEvent e) {
				Level sel = (Level)messageLevel.getSelectedItem();
				engine.setMessageLevel(sel);
			}
		});
		
		pb.add(status, cc.xyw(4, 2, 5, "l,c"));

		int y = 4;
		pb.add(new JLabel("Log File:"), cc.xy(2, y, "r,f"));
		pb.add(logFilePath, cc.xy(4, y, "f,f"));
		pb.add(new JButton(browseLogFileAction), cc.xy(5, y, "r,f"));
		pb.add(appendToLog, cc.xy(7, y, "l,f"));

		y += 2;
		pb.add(new JLabel("Records to Process (0 for no limit):"), cc.xy(2, y, "r,c"));
		pb.add(recordsToProcess, cc.xy(4, y, "l,c"));
		
		y += 2;
		pb.add(debugMode, cc.xy(4, y, "l,c"));
		
		y += 2;
		pb.add(new JLabel("Message Level:"), cc.xy(2,y, "r,c"));
		pb.add(messageLevel, cc.xy(4,y,"l,c"));
		
		FormLayout bbLayout = new FormLayout(
				"4dlu,pref,4dlu,pref,4dlu,pref,4dlu",
				"4dlu,pref,4dlu,pref,4dlu,pref,4dlu");
		PanelBuilder bbpb;
		JPanel bbp = logger.isDebugEnabled() ? new FormDebugPanel(bbLayout)
				: new JPanel(bbLayout);
		bbpb = new PanelBuilder(bbLayout, bbp);
		bbpb.add(new JButton(new ShowLogFileAction(logFilePath)), cc.xy(2, 2, "f,f"));
		bbpb.add(new JButton(new ShowCommandAction(parentFrame, this, engine)), cc.xy(4, 2, "f,f"));
		bbpb.add(new JButton(runEngineAction), cc.xy(6, 2, "f,f"));
		
		// TODO: Match statistics has been disabled for now until we
		// re-implement it.
//		Action showMatchStatsActon = new ShowMatchStatisticInfoAction(swingSession, project, parentFrame);
		Action showMatchStatsAction = new AbstractAction("Match Statistics...") {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(parentFrame,
				"Match statistics is not yet available. We apologize for the inconvenience");
			}
		};
		bbpb.add(new JButton(showMatchStatsAction), cc.xy(2, 4, "f,f"));
		
		bbpb.add(new JButton(new SaveAction()), cc.xy(4, 4, "f,f"));
		
		abortB = new JButton(new AbstractAction("Abort!"){
			public void actionPerformed(ActionEvent e) {
				engine.setCancelled(true);
			}
		});
		
		abortB.setEnabled(false);
		
		bbpb.add(abortB,cc.xy(6,4));

		pb.add(bbpb.getPanel(), cc.xyw(2, 18, 6, "r,c"));
		
		JPanel engineAccessoryPanel = new JPanel(new BorderLayout());
		engineAccessoryPanel.add(engineOutputPanel.getProgressBar(), BorderLayout.NORTH);
		engineAccessoryPanel.add(engineOutputPanel.getButtonBar(), BorderLayout.SOUTH);
		
		JPanel anotherP = new JPanel(new BorderLayout(12, 12));
		anotherP.add(pb.getPanel(), BorderLayout.NORTH);
		anotherP.add(engineOutputPanel.getOutputComponent(), BorderLayout.CENTER);
		anotherP.add(engineAccessoryPanel, BorderLayout.SOUTH);
		anotherP.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		
		return anotherP;
	}
	
	/**
	 * Runs the form validation, and then saves the merge settings into the database.
	 */
	public boolean applyChanges() {
		logger.debug("doSave called");
		refreshActionStatus();
		MergeSettings mergeSettings = project.getMergeSettings();
		mergeSettings.setAppendToLog(appendToLog.isSelected());
		mergeSettings.setLog(new File(logFilePath.getText()));
		if (recordsToProcess.getValue().equals(new Integer(0))) {
			mergeSettings.setProcessCount(null);
		} else {
			mergeSettings.setProcessCount((Integer) recordsToProcess.getValue());
		}
		mergeSettings.setDebug(debugMode.isSelected());
		
		MatchMakerDAO<Project> dao = swingSession.getDAO(Project.class);
		dao.save(project);
		
		return true;
	}

	public JComponent getPanel() {
		return panel;
	}

	public boolean hasUnsavedChanges() {
		return false;
	}

	public void discardChanges() {
		logger.error("Cannot discard chagnes");
	}

}