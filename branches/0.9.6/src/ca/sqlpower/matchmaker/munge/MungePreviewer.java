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

package ca.sqlpower.matchmaker.munge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.Project.ProjectMode;
import ca.sqlpower.matchmaker.event.MatchMakerEvent;
import ca.sqlpower.matchmaker.event.MatchMakerListener;

/**
 * This class will select a few rows from a database on a munge process to
 * allow fast previews of a munge process being edited. The data selected
 * from the database should be cached to reduce database access.
 */
public class MungePreviewer extends MungeProcessor {

	private static final Logger logger = Logger.getLogger(MungePreviewer.class);
	
	/**
	 * The maximum number of rows we will process to cache for previews.
	 */
	public static final int MAX_ROWS_PREVIEWED = 5;
	
	/**
	 * A simple event for a refreshed preview.
	 */
	public class PreviewEvent {
		private MungeStep step;
		
		public void setMungeStep(MungeStep s) {
			step = s;
		}
		
		public MungeStep getMungeStep() {
			return step;
		}
	}
	
	/**
	 * An interface for listeners on the munge previewer.
	 */
	public interface PreviewListener {
		
		/**
		 * Fired when the preview is re-established from the munge steps
		 * in the process.
		 */
		public void previewRefreshed(PreviewEvent evt);

		/**
		 * Fired if the preview becomes disabled because it cannot refresh. This
		 * normally happens when the check on the preconditions fails.
		 */
		public void previewDisabled(String reason);
	}
	
	/**
	 * The munge process this previewer corresponds to.
	 */
	private MungeProcess process;
	
	/**
	 * This maps the munge steps to a List of data that is the conversion from
	 * the cached data in cachedRS to the output of the munge step. The order of
	 * the list matters so the values in the list line up with the result set.
	 */
	private Map<MungeStep, ArrayList<ArrayList>> previewStepOutputData;
	
	/**
	 * This maps the munge steps to a List of data that is the conversion from
	 * the cached data in cachedRS to the input of the munge step. The order of
	 * the list matters so the values in the list line up with the result set.
	 */
	private Map<MungeStep, ArrayList<ArrayList>> previewStepInputData;
	
	/**
	 * This is the munge step that was last modified in the munge pen. This
	 * step is associated with the last refresh of the preview.
	 */
	private MungeStep lastModifiedMS;
	
	/**
	 * Listener list for classes listening for a new preview.
	 */
	private List<PreviewListener> listeners;
	
	/**
	 * A listener that will be added to the munge steps. This listener will
	 * refresh the preview on step changes. It also needs to be removed when
	 * the previewer goes away.
	 */
	private MatchMakerListener<MungeStep, MungeStepOutput> stepListener  = new MatchMakerListener<MungeStep, MungeStepOutput>() {

		public void mmChildrenInserted(
				MatchMakerEvent<MungeStep, MungeStepOutput> evt) {
			refreshPreview();
		}

		public void mmChildrenRemoved(
				MatchMakerEvent<MungeStep, MungeStepOutput> evt) {
			refreshPreview();
		}

		public void mmPropertyChanged(
				MatchMakerEvent<MungeStep, MungeStepOutput> evt) {
			lastModifiedMS = evt.getSource();
			refreshPreview();
		}

		public void mmStructureChanged(
				MatchMakerEvent<MungeStep, MungeStepOutput> evt) {
		}
	};

	/**
	 * A flag to tell if the preview refresh should be allowed to run.
	 */
	private boolean refreshEnabled;
	
	public MungePreviewer(MungeProcess process) {
		super(process, logger);
		this.process = process;
		for (MungeStep step : process.getChildren()) {
			step.addMatchMakerListener(stepListener);
		}
		listeners = new ArrayList<PreviewListener>();
		refreshEnabled = true;
	}
	
	/**
	 * This will remove the preview listener added to the munge steps as well
	 * as other cleanup things.
	 */
	public void cleanup() {
		for (MungeStep step : process.getChildren()) {
			step.removeMatchMakerListener(stepListener);
		}
	}
	
	/**
	 * This will refresh all of the cached rows for each munge step.
	 * Throws a runtime exception if the preconditions on the munge
	 * process fails. A runtime exception is thrown as this is called
	 * from a listener that cannot throw regular exceptions. 
	 */
	public void refreshPreview() {
		
		previewStepOutputData = new HashMap<MungeStep, ArrayList<ArrayList>>();
		previewStepInputData = new HashMap<MungeStep, ArrayList<ArrayList>>();
		
		if (!refreshEnabled) {
			return;
		}		
		
		determineProcessOrder();
		List<MungeStep> processOrder = getProcessOrder();
		
		
		try {
			for (MungeStep step : processOrder) {
				if (step.isOpen()) {
					logger.debug("step " + step.getName() + " is already open!");
				}
				step.open(logger);

				previewStepOutputData.put(step, new ArrayList<ArrayList>());
				previewStepInputData.put(step, new ArrayList<ArrayList>());
			}
			
			for (int i = 0; i < MAX_ROWS_PREVIEWED; i++) {
				for (MungeStep step : processOrder) {
					step.call();
					ArrayList row = new ArrayList();
					previewStepOutputData.get(step).add(row);
					ArrayList inputRow = new ArrayList();
					previewStepInputData.get(step).add(inputRow);

					for (MungeStepOutput<?> mso : step.getChildren()) {
						row.add(mso.getData());
					}
					for (MungeStepOutput<?> mso : step.getMSOInputs()) {
						if (mso != null) {
							inputRow.add(mso.getData());
						}
					}
				}
			}
			
			for (Map.Entry<MungeStep, ArrayList<ArrayList>> entry : previewStepOutputData.entrySet()) {
				logger.debug(" entries for " + entry.getKey().getName() + " from previewer are " + entry.getValue());
			}
			
		} catch (Exception e) {
			refreshEnabled = false;
			logger.error("Could not refresh preview:", e);
			for (PreviewListener l : listeners) {
				l.previewDisabled("Could not refresh preview : " + e.getMessage());
			}
		} finally {
			
			for (MungeStep step : processOrder) {
				try {
					step.rollback();
				} catch (Exception e) {
					logger.error("Exception while rolling back step " + step.getName() + ". " +
							"Exception was " + e.getMessage());
				}
				try {
					step.close();
				} catch (Exception e) {
					logger.error("Closing step " + step.getName() + " caused an exception in a finally clause!" +
							" Exception was: " + e.getMessage());
				}
			}
		}
		
		PreviewEvent evt = new PreviewEvent();
		evt.setMungeStep(lastModifiedMS);
		for (PreviewListener l : listeners) {
			logger.debug("firing refresh event");
			l.previewRefreshed(evt);
		}
	}
	
	/**
	 * Returns an array list of the output steps from an execution of the munge
	 * process based on the cached data. This may return null if the step is
	 * never reached in the munge process.
	 */
	public ArrayList<ArrayList> getPreviewOutputForStep(MungeStep ms) {
		if (previewStepOutputData == null) return null;
		
		return previewStepOutputData.get(ms);
	}
	
	/**
	 * Returns an array list of the input steps from an execution of the munge
	 * process based on the cached data. This may return null if the step is
	 * never reached in the munge process. If the munge step has no input an
	 * empty list will be returned.
	 */
	public ArrayList<ArrayList> getPreviewInputForStep(MungeStep ms) {
		if (previewStepInputData == null) return null;
		
		return previewStepInputData.get(ms);
	}
	
	public MungeStep getLastModifiedStep() {
		return lastModifiedMS;
	}
	
	public void addPreviewListener(PreviewListener l) {
		listeners.add(l);
	}
	
	public void removePreviewListener(PreviewListener l) {
		listeners.remove(l);
	}

	public void setRefreshEnabled(boolean refreshEnabled) {
		this.refreshEnabled = refreshEnabled;
		
		if (refreshEnabled) {
			try {
				Project project = process.getParentProject();
				
				// XXX: A quick fix, but ideally, each project type would be a
				// subclass of Project and have a 'getEngine' method that would
				// return the appropriate engine.
				if (project.getType() == ProjectMode.FIND_DUPES) {
					project.getMatchingEngine().checkPreconditions();
				} else if (project.getType() == ProjectMode.CLEANSE) {
					project.getCleansingEngine().checkPreconditions();
				} else if (project.getType() == ProjectMode.ADDRESS_CORRECTION) {
					project.getAddressCorrectionEngine().checkPreconditions();
				}
			} catch (Exception e1) {
				logger.debug("Preview disabled");
				this.refreshEnabled = false;
				for (PreviewListener l : listeners) {
					l.previewDisabled("Engine precondition failed : " + e1.getMessage());
				}
				return;
			}
			refreshPreview();
		}
	}

	public boolean isRefreshEnabled() {
		return refreshEnabled;
	}
}
