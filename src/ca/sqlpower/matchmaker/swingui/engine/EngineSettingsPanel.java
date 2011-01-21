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

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.EngineEvent;
import ca.sqlpower.matchmaker.EngineListener;
import ca.sqlpower.matchmaker.MatchMakerEngine;
import ca.sqlpower.matchmaker.MatchMakerFolder;
import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerSessionContext;
import ca.sqlpower.matchmaker.MatchMakerSettings;
import ca.sqlpower.matchmaker.MatchMakerUtils;
import ca.sqlpower.matchmaker.MungeSettings;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.MungeSettings.AutoValidateSetting;
import ca.sqlpower.matchmaker.MungeSettings.PoolFilterSetting;
import ca.sqlpower.matchmaker.address.AddressDatabase;
import ca.sqlpower.matchmaker.event.MatchMakerEvent;
import ca.sqlpower.matchmaker.event.MatchMakerListener;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.swingui.MMSUtils;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.swingui.BrowseFileAction;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.DataEntryPanelBuilder;
import ca.sqlpower.validation.FileNameValidator;
import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;
import ca.sqlpower.validation.Validator;
import ca.sqlpower.validation.swingui.FormValidationHandler;
import ca.sqlpower.validation.swingui.StatusComponent;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.sleepycat.je.DatabaseException;

/**
 * A panel that provides a GUI for setting the parameters for running the MatchMakerEngine,
 * as well as running the MatchMakerEngine itself and displaying its output on the GUI.
 */
public class EngineSettingsPanel implements DataEntryPanel, MatchMakerListener<Project, MatchMakerFolder> {

	private static final String ADDRESS_CORRECTION_ENGINE_PANEL_ROW_SPECS = 
		"4dlu,pref,4dlu,pref,4dlu,pref,10dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu,fill:pref:grow,4dlu,pref,4dlu";
		//  1    2    3    4    5    6     7    8    9   10   11   12   13   14   15   16   17   18   19   20   21   22   23             24   25 	 26   27

	private static final String ADDRESS_COMMITTING_ENGINE_PANEL_ROW_SPECS = 
		"4dlu,pref,4dlu,pref,4dlu,pref,10dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu,fill:pref:grow,4dlu,pref,4dlu";
		//  1    2    3    4    5    6     7    8    9   10   11   12   13   14   15   16   17   18   19             20   21   22   23 

	private static final String MATCH_ENGINE_PANEL_ROW_SPECS = 
		"4dlu,pref,4dlu,pref,4dlu,pref,10dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu,fill:pref:grow,4dlu,pref,4dlu";
		//  1    2    3    4    5    6     7    8    9   10   11   12   13   14   15   16   17   18   19   20   21   22   23             24   25   26   27 	

	private static final String MERGE_ENGINE_PANEL_ROW_SPECS = 
		"4dlu,pref,4dlu,pref,4dlu,pref,10dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu,fill:pref:grow,4dlu,pref,4dlu";
		//  1    2    3    4    5    6     7    8    9   10   11   12   13   14   15   16   17             18   19   20   21  	

	private static final String CLEANSE_ENGINE_PANEL_ROW_SPECS = 
		"4dlu,pref,4dlu,pref,4dlu,pref,10dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu,fill:pref:grow,4dlu,pref,4dlu";
		//  1    2    3    4    5    6     7    8    9   10   11   12   13   14   15   16   17   18   19   20   21             22   23   24   25 	

	
	private static final Logger logger = Logger.getLogger(EngineSettingsPanel.class);
	
	/**
	 * An enumeration for all the different types of MatchMakerEngines.
	 */
	public enum EngineType {
		MATCH_ENGINE("Match Engine"), MERGE_ENGINE("Merge Engine"), 
		CLEANSE_ENGINE("Cleanse Engine"), ADDRESS_CORRECTION_ENGINE("Address Correction Engine"),
		VALIDATED_ADDRESS_COMMITING_ENGINE("Validated Address Committing Engine");
		
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
	 * A flag for telling the engine whether to try to use batch execute when
	 * running insert and update statements into the match/address pool.
	 */
	private JCheckBox useBatchExecute;
	
	/**
	 * A flag specific to the Address Correcton Engine.
	 * <p>
	 * If checked, then the engine should automatically correct the results
	 * and then write these corrections directly to the source table without
	 * requiring user intervention.
	 * <p>
	 * If not checked, then the engine should write the results of the Address
	 * Correction Munge Step to the project's result table and then allow the
	 * user to manually correct the addresses before writing them back to the
	 * source table.
	 */
	private JCheckBox autoWriteAutoValidatedAddresses;
	
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
	 * Displays a warning if current address database is out of date and a fail if 
	 * it's missing.
	 */
	private JEditorPane expiryDatePane;
	
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
	private JButton abortButton;
	
	/**
	 * The action that is called when the engine is finished
	 */
	private Runnable engineFinish = new Runnable(){
		public void run() {
			abortButton.setEnabled(false);
		}
	};
	
	/**
	 * The action that is called when the engine is started
	 */
	private Runnable engineStart = new Runnable(){
		public void run() {
			abortButton.setEnabled(true);
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

	/**
	 * This is the date that the address database expires on.
	 * <p>
	 * TODO: Create a settings panel that extends this that is only for the address validation
	 * to handle the date.
	 */
	private Date expiryDate;

	private EngineListener engineListener;
	
	public EngineSettingsPanel(final MatchMakerSwingSession swingSession, Project project, JFrame parentFrame, 
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
			engineSettings = project.getMungeSettings();
		} else if (type == EngineType.VALIDATED_ADDRESS_COMMITING_ENGINE) {
			engine = project.getAddressCommittingEngine();
			engineSettings = project.getMungeSettings();
		} else {
			throw new IllegalArgumentException("There is no engine type with a string " + type);
		}
		
		if (type == EngineType.MERGE_ENGINE ||
			type == EngineType.CLEANSE_ENGINE ||
			type == EngineType.VALIDATED_ADDRESS_COMMITING_ENGINE) {
			this.runEngineAction = new RunWarningEngineAction(swingSession, project, engine, "Run Engine",
					engineOutputPanel, this, engineStart, engineFinish, 
					"This engine will make changes to your source data.\n" +
					"It is strongly recommended that you backup your source data before running this engine.\n");
		} else {
			this.runEngineAction = new RunEngineAction(swingSession, project, engine, "Run Engine",
				engineOutputPanel, this, engineStart, engineFinish);
		}
		
		engineListener = new EngineListener() {
			public void engineStopped(EngineEvent e) {
				runEngineAction.setEnabled(true);
			}
			
			public void engineStarted(EngineEvent e) {
				runEngineAction.setEnabled(false);
			}
		};
		
		engine.addEngineListener(engineListener);
		
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

		logger.debug("We are building the UI of an engine settings panel.");
		if (type == EngineType.ADDRESS_CORRECTION_ENGINE) {
			expiryDatePane = new JEditorPane();
			expiryDatePane.setEditable(false);
			final AddressDatabase addressDatabase;
			try {
				addressDatabase = new AddressDatabase(new File(swingSession.getContext().getAddressCorrectionDataPath()));
				expiryDate = addressDatabase.getExpiryDate();
				expiryDatePane.setText(DateFormat.getDateInstance().format(expiryDate));
			} catch (DatabaseException e1) {
				MMSUtils.showExceptionDialog(parentFrame, "An error occured while loading the Address Correction Data", e1);
				expiryDatePane.setText("Database missing, expiry date invalid");
			}

			logger.debug("We are adding the listener");
			swingSession.getContext().addPreferenceChangeListener(new PreferenceChangeListener() {
				public void preferenceChange(PreferenceChangeEvent evt) {
					if (MatchMakerSessionContext.ADDRESS_CORRECTION_DATA_PATH.equals(evt.getKey())) {
						logger.debug("The new database path is: " + evt.getNewValue());
						final AddressDatabase addressDatabase;
						try {
							addressDatabase = new AddressDatabase(new File(evt.getNewValue()));
							expiryDate = addressDatabase.getExpiryDate();
							expiryDatePane.setText(DateFormat.getDateInstance().format(expiryDate));
						} catch (DatabaseException ex) {
							MMSUtils.showExceptionDialog(parentFrame, "An error occured while loading the Address Correction Data", ex);
							expiryDate = null;
							expiryDatePane.setText("Database missing, expiry date invalid");
						}
					}
				}
			});
			// handler listens to expiryDatePane so whenever the expiryDatePane's text has been changed, the below method will be called.
			handler.addValidateObject(expiryDatePane, new Validator() {
				public ValidateResult validate(Object contents) {
					if (expiryDate == null) {
						return ValidateResult.createValidateResult(Status.FAIL, "Address Correction Database is missing. Please reset your Address Correction Data Path in Preferences.");
					}
					if (Calendar.getInstance().getTime().after(expiryDate)) {
						return ValidateResult.createValidateResult(Status.WARN, "Address Correction Database is expired. The results of this engine run cannot be SERP valid.");
					}
					return ValidateResult.createValidateResult(Status.OK, "");
				}
			});
		}
		
		
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

		debugMode = new JCheckBox("Debug Mode (Changes will be rolled back)", engineSettings.getDebug());
		itemListener = new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (((JCheckBox) e.getSource()).isSelected()) {
					if (type == EngineType.MATCH_ENGINE || type == EngineType.ADDRESS_CORRECTION_ENGINE) {
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
					if (type == EngineType.MATCH_ENGINE || type == EngineType.ADDRESS_CORRECTION_ENGINE) {
						clearMatchPool.setEnabled(true);
					}
					recordsToProcess.setValue(new Integer(0));
				}
			}
		};
		debugMode.addItemListener(itemListener);

		if (type == EngineType.MATCH_ENGINE || type == EngineType.ADDRESS_CORRECTION_ENGINE) {
			if (type == EngineType.MATCH_ENGINE) {
				clearMatchPool = new JCheckBox("Clear match pool", ((MungeSettings)engineSettings).isClearMatchPool());
			} else {
				clearMatchPool = new JCheckBox("Clear address pool" , ((MungeSettings)engineSettings).isClearMatchPool());
			}
			if (debugMode.isSelected()) {
				clearMatchPool.setSelected(false);
				// See comment just above about why this is disabled
				clearMatchPool.setEnabled(false);
			}
		}
		
		if (engineSettings instanceof MungeSettings) {
			useBatchExecute = new JCheckBox("Batch execute SQL statments", ((MungeSettings)engineSettings).isUseBatchExecution());
		}

		if (type == EngineType.ADDRESS_CORRECTION_ENGINE) {
			autoWriteAutoValidatedAddresses = new JCheckBox("Immediately commit auto-corrected addresses", ((MungeSettings)engineSettings).isAutoWriteAutoValidatedAddresses());
		}
		
		messageLevel = new JComboBox(new Level[] {Level.OFF, Level.FATAL, Level.ERROR, Level.WARN, Level.INFO, Level.DEBUG, Level.ALL});
		messageLevel.setSelectedItem(engine.getMessageLevel());
		messageLevel.setRenderer(new DefaultListCellRenderer(){
			@Override
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				setText(value == null ? null : value.toString());
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

		String rowSpecs;
		if (type == EngineType.ADDRESS_CORRECTION_ENGINE) {
			rowSpecs = ADDRESS_CORRECTION_ENGINE_PANEL_ROW_SPECS;
		} else if (type == EngineType.VALIDATED_ADDRESS_COMMITING_ENGINE) {
			rowSpecs = ADDRESS_COMMITTING_ENGINE_PANEL_ROW_SPECS;
		} else if (type == EngineType.MERGE_ENGINE) {
			rowSpecs = MERGE_ENGINE_PANEL_ROW_SPECS;
		} else if (type == EngineType.CLEANSE_ENGINE) {
			rowSpecs = CLEANSE_ENGINE_PANEL_ROW_SPECS;
		} else {
			rowSpecs = MATCH_ENGINE_PANEL_ROW_SPECS;
		}
		
		String columnSpecs = "4dlu,fill:pref,4dlu,pref,pref,40dlu,fill:pref:grow,pref,4dlu";
		
		FormLayout layout = new FormLayout(
				 columnSpecs,
				 rowSpecs);
		
		PanelBuilder pb;
		JPanel p = logger.isDebugEnabled() ? new FormDebugPanel(layout)
		: new JPanel(layout);
		pb = new PanelBuilder(layout, p);

		CellConstraints cc = new CellConstraints();
		
		pb.add(status, cc.xyw(4, 2, 6, "l,c"));

		int y = 4;
		pb.add(new JLabel("Log File:"), cc.xy(2, y, "r,f"));
		pb.add(logFilePath, cc.xyw(4, y, 4, "f,f"));
		pb.add(new JButton(browseLogFileAction), cc.xy(8, y, "l,f"));
		y += 2;
		pb.add(appendToLog, cc.xy(4, y, "l,t"));
		pb.add(new JButton(new ShowLogFileAction(logFilePath)), cc.xy(5, y, "r,t"));

		if (type == EngineType.MATCH_ENGINE || type == EngineType.CLEANSE_ENGINE) {
			y += 2;

			pb.add(new JLabel("Tranformations to run: "), cc.xy(2, y, "r,t"));
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
			pb.add(selectionButton, cc.xyw(4, y, 2, "l,c"));
		}

		y += 2;
		pb.add(new JLabel("# of records to process:"), cc.xy(2, y, "r,c"));
		pb.add(recordsToProcess, cc.xy(4, y, "l,c"));
		pb.add(new JLabel(" (Set to 0 to process all)"), cc.xy(5, y, "l, c"));
		
		if (type == EngineType.ADDRESS_CORRECTION_ENGINE) {
			pb.add(new JLabel("Address Filter Setting:"), cc.xy(7, y));
		}

		if (engineSettings instanceof MungeSettings) {
			MungeSettings mungeSettings = (MungeSettings) engineSettings;
			y += 2;
			pb.add(useBatchExecute, cc.xyw(4, y, 2, "l,c"));
			
			if (type == EngineType.ADDRESS_CORRECTION_ENGINE) {
				final JLabel poolSettingLabel = new JLabel(mungeSettings.getPoolFilterSetting().getLongDescription());
				MatchMakerListener<MatchMakerSettings, MatchMakerObject> poolFilterSettingChangeListener
					= new MatchMakerListener<MatchMakerSettings, MatchMakerObject>() {
						public void mmChildrenInserted(MatchMakerEvent<MatchMakerSettings, MatchMakerObject> evt) {
							// no-op
						}

						public void mmChildrenRemoved(MatchMakerEvent<MatchMakerSettings, MatchMakerObject> evt) {
							// no-op
						}

						public void mmPropertyChanged(MatchMakerEvent<MatchMakerSettings, MatchMakerObject> evt) {
							if (evt.getPropertyName() == "poolFilterSetting") {
								PoolFilterSetting newValue = (PoolFilterSetting) evt.getNewValue();
								poolSettingLabel.setText(newValue.getLongDescription());
							}
						}

						public void mmStructureChanged(MatchMakerEvent<MatchMakerSettings, MatchMakerObject> evt) {
							// no-op
						}
						
				};
				mungeSettings.addMatchMakerListener(poolFilterSettingChangeListener);
				Font f = poolSettingLabel.getFont();
				Font newFont = f.deriveFont(Font.ITALIC);
				poolSettingLabel.setFont(newFont);
				pb.add(poolSettingLabel, cc.xy(7, y));
			}
		}
		
		if (type == EngineType.ADDRESS_CORRECTION_ENGINE) {
			y += 2;
			pb.add(autoWriteAutoValidatedAddresses, cc.xyw(4, y, 2, "l,c"));
			if (type == EngineType.ADDRESS_CORRECTION_ENGINE) {
				pb.add(new JLabel("Auto-correction Setting:"), cc.xy(7, y));
			}
		}
		
		if (type == EngineType.MATCH_ENGINE || type == EngineType.ADDRESS_CORRECTION_ENGINE) {
			y += 2;
			pb.add(clearMatchPool, cc.xyw(4, y, 2, "l,c"));
			if (type == EngineType.ADDRESS_CORRECTION_ENGINE) {
				MungeSettings mungeSettings = (MungeSettings) engineSettings;
				final JLabel autoValidateSettingLabel = new JLabel(((MungeSettings) engineSettings).getAutoValidateSetting().getLongDescription());
				MatchMakerListener<MatchMakerSettings, MatchMakerObject> poolFilterSettingChangeListener
					= new MatchMakerListener<MatchMakerSettings, MatchMakerObject>() {
						public void mmChildrenInserted(
								MatchMakerEvent<MatchMakerSettings, MatchMakerObject> evt) {
							// no-op
						}
	
						public void mmChildrenRemoved(
								MatchMakerEvent<MatchMakerSettings, MatchMakerObject> evt) {
							// no-op
						}
	
						public void mmPropertyChanged(MatchMakerEvent<MatchMakerSettings, MatchMakerObject> evt) {
							if (evt.getPropertyName() == "autoValidateSetting") {
								AutoValidateSetting newValue = (AutoValidateSetting) evt.getNewValue();
								autoValidateSettingLabel.setText(newValue.getLongDescription());
							}
						}
	
						public void mmStructureChanged(
								MatchMakerEvent<MatchMakerSettings, MatchMakerObject> evt) {
							// no-op
						}
				};
				mungeSettings.addMatchMakerListener(poolFilterSettingChangeListener);
				Font f = autoValidateSettingLabel.getFont();
				Font newFont = f.deriveFont(Font.ITALIC);
				autoValidateSettingLabel.setFont(newFont);
				pb.add(autoValidateSettingLabel, cc.xy(7, y));
			}
		}

		y += 2;
		pb.add(debugMode, cc.xyw(4, y, 2, "l,c"));
		if (type == EngineType.ADDRESS_CORRECTION_ENGINE) {
			final AddressValidationSettingsPanel avsp = new AddressValidationSettingsPanel((MungeSettings)engineSettings);
			final JDialog validationSettingsDialog = DataEntryPanelBuilder.createDataEntryPanelDialog(
					avsp, swingSession.getFrame(), "Address Validation Settings", "OK",
					new Callable<Boolean>() {
						public Boolean call() throws Exception {
							boolean returnValue =  avsp.applyChanges();
							swingSession.save(project);
							return returnValue;
						}
					},
					new Callable<Boolean>() {
						public Boolean call() throws Exception {
							return true;
						}
					});
			validationSettingsDialog.setLocationRelativeTo(pb.getPanel());
			
			JButton addressValidationSettings = new JButton(new AbstractAction("Validation Settings...") {
				public void actionPerformed(ActionEvent e) {
					validationSettingsDialog.setVisible(true);
				}
			});
			pb.add(addressValidationSettings, cc.xy(7, y, "l,c"));
		}
		
		y += 2;
		pb.add(new JLabel("Message Level:"), cc.xy(2,y, "r,t"));
		pb.add(messageLevel, cc.xy(4,y,"l,t"));

		abortButton = new JButton(new AbstractAction("Abort!"){
			public void actionPerformed(ActionEvent e) {
				engine.setCancelled(true);
			}
		});

		abortButton.setEnabled(false);

		ButtonBarBuilder bbb = new ButtonBarBuilder();
		bbb.addFixed(new JButton(new SaveAction()));
		bbb.addRelatedGap();
		bbb.addFixed(new JButton(new ShowCommandAction(parentFrame, this, engine)));
		bbb.addRelatedGap();
		bbb.addFixed(new JButton(runEngineAction));
		bbb.addRelatedGap();
		bbb.addFixed(abortButton);
		
		y += 2;
		pb.add(bbb.getPanel(), cc.xyw(2, y, 7, "r,c"));

		y += 2;
		pb.add(engineOutputPanel.getProgressBar(), cc.xyw(2, y, 7));
		
		y += 2;
		pb.add(engineOutputPanel.getOutputComponent(), cc.xyw(2, y, 7));
		
		y += 2;
		pb.add(engineOutputPanel.getButtonBar(), cc.xyw(2, y, 7));
		

		refreshRunActionStatus();
		
		return pb.getPanel();
	}

	/**
	 * Saves the engine settings
	 */
	public boolean applyChanges() {
		refreshRunActionStatus();
		engineSettings.setDebug(debugMode.isSelected());
		if (type == EngineType.MATCH_ENGINE || type == EngineType.ADDRESS_CORRECTION_ENGINE) {
			((MungeSettings)engineSettings).setClearMatchPool(clearMatchPool.isSelected());
		}
		if (engineSettings instanceof MungeSettings) {
			((MungeSettings)engineSettings).setUseBatchExecution(useBatchExecute.isSelected());
		}
		if (type == EngineType.ADDRESS_CORRECTION_ENGINE) {
			((MungeSettings)engineSettings).setAutoWriteAutoValidatedAddresses(autoWriteAutoValidatedAddresses.isSelected());
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
//		engine.removeEngineListener(engineListener);
	}
}