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
import ca.sqlpower.matchmaker.MatchMakerFolder;
import ca.sqlpower.matchmaker.MatchMakerSettings;
import ca.sqlpower.matchmaker.MatchMakerUtils;
import ca.sqlpower.matchmaker.MungeSettings;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.event.MatchMakerEvent;
import ca.sqlpower.matchmaker.event.MatchMakerListener;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.swingui.BrowseFileAction;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.validation.FileNameValidator;
import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;
import ca.sqlpower.validation.swingui.FormValidationHandler;
import ca.sqlpower.validation.swingui.StatusComponent;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * A panel that provides a GUI for setting the parameters for running the MatchMakerEngine,
 * as well as running the MatchMakerEngine itself and displaying its output on the GUI.
 */
public class EngineSettingsPanel implements DataEntryPanel, MatchMakerListener<Project, MatchMakerFolder> {

	private static final Logger logger = Logger.getLogger(EngineSettingsPanel.class);
	
	/**
	 * An enumeration for all the different types of MatchMakerEngines.
	 */
	public enum EngineType {
		MATCH_ENGINE("Match Engine"), MERGE_ENGINE("Merge Engine"), 
		CLEANSE_ENGINE("Cleanse Engine"), ADDRESS_CORRECTION_ENGINE("Address Correciton Engine");
		
		String engineName;
		
		private EngineType(String engineName) {
			this.engineName = engineName;
		}
		
		@Override
		public String toString() {
			return engineName;
		}
	}

	/**
	 * The session this panel belongs to.
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
	 * A flag for telling the engine to delete all the records from the match result table before running the match
	 */
	private JCheckBox clearMatchPool;
	
	/**
	 * The frame that this editor lives in.
	 */
	private JFrame parentFrame;

	/**
	 * The project object the engine should run against.
	 */
	private Project project;
	
	/**
	 * The panel that displays all the information for the engine.
	 */
	private JPanel panel;
	
	/**
	 * Displays the validation status of the engine preconditions.
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
	 * The MatchMakerEngine for this panel
	 */
	private final MatchMakerEngine engine;

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

	/**
	 * The engine type for this panel.
	 */
	private final EngineType type;
	
	/**
	 * Contains the saved user settings for this engine panel.
	 */
	private final MatchMakerSettings engineSettings;

	/**
	 * A set of event listeners that were converted into fields so that they can
	 * be removed later via the {@link #cleanup()} method.
	 */
	private PropertyChangeListener propertyChangeListener;
	private ItemListener itemListener;
	private AbstractAction messageLevelActionListener;
	
	public EngineSettingsPanel(MatchMakerSwingSession swingSession, Project project, JFrame parentFrame, 
			EngineType engineType) {
		this.swingSession = swingSession;
		this.parentFrame = parentFrame;
		this.project = project;
		this.type = engineType;
		handler = new FormValidationHandler(status);
		propertyChangeListener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				refreshRunActionStatus();
			}
		};
		handler.addPropertyChangeListener(propertyChangeListener);
		this.engineOutputPanel = new EngineOutputPanel(parentFrame);
		
		if (type == EngineType.MATCH_ENGINE) {
			engine = project.getMatchingEngine();
			engineSettings = project.getMungeSettings();
		} else if (type == EngineType.MERGE_ENGINE) {
			engine = project.getMergingEngine();
			engineSettings = project.getMergeSettings();
		} else if (type == EngineType.CLEANSE_ENGINE) {
			engine = project.getCleansingEngine();
			engineSettings = project.getMungeSettings();
		} else if (type == EngineType.ADDRESS_CORRECTION_ENGINE) {
			engine = project.getAddressCorrectionEngine();
			engineSettings = project.getAddressCorrectionSettings();
		} else {
			throw new IllegalArgumentException("There is no engine type with a string " + type);
		}
		
		this.runEngineAction = new RunEngineAction(swingSession, project, engine, "Run " + engineType,
				engineOutputPanel, this, engineStart, engineFinish);
		
		this.panel = buildUI();
		
		MatchMakerUtils.listenToShallowHierarchy(this, project);
	}
	
	/**
	 * Performs a form validation on the configuration portion and sets the
	 * status accordingly as well as disabling the button to run the engine if
	 * necessary.
	 */
	private void refreshRunActionStatus() {
		ValidateResult worst = handler.getWorstValidationStatus();
		boolean valid = !(worst.getStatus() == Status.FAIL || project.getRunningEngine() != null);
		runEngineAction.setEnabled(valid);
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

	
		if (engineSettings.getLog() == null) {
			engineSettings.setLog(new File(project.getName() + ".log"));
		}

		File logFile = engineSettings.getLog();
		logFilePath = new JTextField(logFile.getAbsolutePath());
		handler.addValidateObject(logFilePath, new FileNameValidator("Log"));

		browseLogFileAction = new BrowseFileAction(parentFrame, logFilePath);

		appendToLog = new JCheckBox("Append to old Log File?", engineSettings.getAppendToLog());

		recordsToProcess = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 100));
		if (engineSettings.getProcessCount() != null) {
			recordsToProcess.setValue(engineSettings.getProcessCount());
		}

		debugMode = new JCheckBox("Debug Mode? (Changes will be rolled back)", engineSettings.getDebug());
		itemListener = new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (((JCheckBox) e.getSource()).isSelected()) {
					if (type == EngineType.MATCH_ENGINE) {
						clearMatchPool.setSelected(false);
						// I've currently disabled the clear match pool option because the match
						// in debug mode, changes should be rolled back, but if the clearing of the match
						// pool is rolled back but the engine thinks that it is cleared, it can cause
						// unique key violations when it tries to insert 'new' matches. But if the engine
						// is made aware of the rollback, then it would be as if clear match pool wasn't
						// selected in the first place, so I don't see the point in enabling it in debug mode
						clearMatchPool.setEnabled(false);
					}
					recordsToProcess.setValue(new Integer(1));
					engine.setMessageLevel(Level.ALL);
					messageLevel.setSelectedItem(engine.getMessageLevel());
				} else {
					if (type == EngineType.MATCH_ENGINE) {
						clearMatchPool.setEnabled(true);
					}
					recordsToProcess.setValue(new Integer(0));
				}
			}
		};
		debugMode.addItemListener(itemListener);

		if (type == EngineType.MATCH_ENGINE) {
			clearMatchPool = new JCheckBox("Clear match pool?", ((MungeSettings)engineSettings).isClearMatchPool());
			if (debugMode.isSelected()) {
				clearMatchPool.setSelected(false);
				// See comment just above about why this is disabled
				clearMatchPool.setEnabled(false);
			}
		}

		messageLevel = new JComboBox(new Level[] {Level.OFF, Level.FATAL, Level.ERROR, Level.WARN, Level.INFO, Level.DEBUG, Level.ALL});
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

		messageLevelActionListener = new AbstractAction(){
			public void actionPerformed(ActionEvent e) {
				Level sel = (Level)messageLevel.getSelectedItem();
				engine.setMessageLevel(sel);
			}
		};
		messageLevel.addActionListener(messageLevelActionListener);

		pb.add(status, cc.xyw(4, 2, 5, "l,c"));

		int y = 4;
		pb.add(new JLabel("Log File:"), cc.xy(2, y, "r,f"));
		pb.add(logFilePath, cc.xy(4, y, "f,f"));
		pb.add(new JButton(browseLogFileAction), cc.xy(5, y, "r,f"));
		pb.add(appendToLog, cc.xy(7, y, "l,f"));

		if (type == EngineType.MATCH_ENGINE || type == EngineType.CLEANSE_ENGINE) {
			y += 2;

			pb.add(new JLabel("Munge Processes to run: "), cc.xy(2, y, "r,t"));
			MungeProcessSelectionList selectionButton = new MungeProcessSelectionList(project) {

				@Override
				public boolean getValue(MungeProcess mp) {
					return mp.getActive();
				}

				@Override
				public void setValue(MungeProcess mp, boolean value) {
					mp.setActive(value);
				}
				
			};
			pb.add(selectionButton, cc.xy(4, y, "l,c"));
		}

		y += 2;
		pb.add(new JLabel("Records to Process (0 for no limit):"), cc.xy(2, y, "r,c"));
		pb.add(recordsToProcess, cc.xy(4, y, "l,c"));

		y += 2;
		pb.add(debugMode, cc.xy(4, y, "l,c"));

		if (type == EngineType.MATCH_ENGINE) {
			y += 2;
			pb.add(clearMatchPool, cc.xy(4, y, "l,c"));
		}

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
		if (type == EngineType.MATCH_ENGINE || type == EngineType.MERGE_ENGINE) {
			Action showMatchStatsAction = new AbstractAction("Match Statistics...") {
				public void actionPerformed(ActionEvent e) {
					JOptionPane.showMessageDialog(parentFrame,
					"Match statistics is not yet available. We apologize for the inconvenience");
				}
			};
			bbpb.add(new JButton(showMatchStatsAction), cc.xy(2, 4, "f,f"));
		}
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

		refreshRunActionStatus();
		
		return anotherP;
	}

	/**
	 * Saves the engine settings
	 */
	public boolean applyChanges() {
		refreshRunActionStatus();
		engineSettings.setDebug(debugMode.isSelected());
		if (type == EngineType.MATCH_ENGINE) {
			((MungeSettings)engineSettings).setClearMatchPool(clearMatchPool.isSelected());
		}
		engineSettings.setLog(new File(logFilePath.getText()));
		engineSettings.setAppendToLog(appendToLog.isSelected());
		if (recordsToProcess.getValue().equals(new Integer(0))) {
			engineSettings.setProcessCount(null);
		} else {
			engineSettings.setProcessCount((Integer) recordsToProcess.getValue());
		}

		swingSession.save(project);
		return true;
	}
	
	public JComponent getPanel() {
		return panel;
	}

	public boolean hasUnsavedChanges() {
		//XXX This is stubbed for now, should look over the check boxes
		//and text fields in the configuration section
		return false;
	}

	public void discardChanges() {
		logger.error("Cannot discard changes");
	}

	public void mmPropertyChanged(MatchMakerEvent<Project, MatchMakerFolder> evt) {
		if (evt.getPropertyName().equals("engineRunning")) {
			refreshRunActionStatus();
		}
	}

	public void mmChildrenInserted(MatchMakerEvent<Project, MatchMakerFolder> evt) {
		// do nothing
	}

	public void mmChildrenRemoved(MatchMakerEvent<Project, MatchMakerFolder> evt) {
		// do nothing
	}

	public void mmStructureChanged(MatchMakerEvent<Project, MatchMakerFolder> evt) {
		// do nothing		
	}

	/**
	 * Clean up all resources being held up by the EngineSettingsPanel.
	 * including removing all event listeners.
	 */
	public void cleanup() {
		handler.removePropertyChangeListener(propertyChangeListener);
		debugMode.removeItemListener(itemListener);
		messageLevel.removeActionListener(messageLevelActionListener);
	}
}