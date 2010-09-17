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
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import ca.sqlpower.graph.DepthFirstSearch;
import ca.sqlpower.matchmaker.AbstractProcessor;
import ca.sqlpower.matchmaker.MatchMakerEngine.EngineMode;
import ca.sqlpower.matchmaker.munge.MungeProcessGraphModel.Edge;

public class MungeProcessor extends AbstractProcessor {

    /**
     * The munging process this processor is responsible for executing.  The
     * graph of munge steps is retrieved fresh every time this processor is
     * invoked, so that a single munge processor can be used for previewing
     * at design time.
     */
    private final MungeProcess mungeProcess;
    
    private final Logger engineLogger;
    
    /**
     * The list of MungeSteps obtained from the MungeProcess that this processor will
     * process, sorted in the exact order that the processor will process them.
     */
    private List<MungeStep> processOrder;
    
    public MungeProcessor(MungeProcess mungeProcess, Logger logger) {
        this.mungeProcess = mungeProcess;
        this.engineLogger = logger;
    }
    
    public Boolean call() throws Exception {
    	return this.call(null, -1);
    }
    
    public Boolean call(int rowCount) throws Exception {
    	return this.call(null, rowCount);
    }
    
    public Boolean call(EngineMode mode, int rowCount) throws Exception {
    	if (mungeProcess.getParentProject().getMungeSettings().getDebug()) {
    		engineLogger.setLevel(Level.DEBUG);
    	}
    	
    	MungeResultStep resultStep = mungeProcess.getResultStep();
    	if (!resultStep.hasConnectedInputs()) {
    		throw new IllegalStateException("Munge Process '" + mungeProcess.getName() + 
    				"' has a result step with no inputs connected!");
    	}
    	
    	determineProcessOrder();
        
    	try {
			monitorableHelper.setStarted(true);
			monitorableHelper.setFinished(false);
			monitorableHelper.setJobSize(rowCount);
			
			// open everything
			for (MungeStep step: processOrder) {
				step.open(mode, engineLogger);
			}
			
			// call until one step gives up
			boolean finished = false;
			
			//stops the process if nothing is going to happen
			//this will cause an infinite loop if nothing returns false
			if (processOrder.size() < 2) {
				finished = true;
			}
			while(!finished && (rowCount == -1 || monitorableHelper.getProgress() < rowCount)) {
                checkCancelled();
				for (MungeStep step: processOrder) {
                    checkCancelled();
					boolean continuing = step.call();
					if (!continuing) {
						finished = true;
						break;
					}
				}
				monitorableHelper.incrementProgress();
			}
            
            // Normal termination! Ask all steps to commit.
            for (MungeStep step : processOrder) {
                step.mungeCommit();
            }
            
        } catch (Throwable t) {
            for (MungeStep step : processOrder) {
                try {
                    step.mungeRollback();
                } catch (Throwable tt) {
                    engineLogger.warn(
                            "Failed to rollback a step. Proceeding with rollback," +
                            " and ignoring the following exception:", tt);
                }
            }
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw new Exception(t);
            }
		} finally {
			// close everything
			for (MungeStep step: processOrder) {
				try {
					step.mungeClose();
				} catch (Exception ex) {
					engineLogger.error("Close failed; squishing exception in order" +
							" not to obscure any earlier exceptions.", ex);
				}
			}
			
			monitorableHelper.setFinished(true);
		}
        
        return Boolean.TRUE;
    }

    /**
     * Determines the order that the munge steps should be processed in. It is set to
     * default accessibility so that the unit test can call it.
     */
	void determineProcessOrder() {
		List<MungeStep> steps = new ArrayList<MungeStep>(mungeProcess.getChildren());
    	// topo sort
    	MungeProcessGraphModel gm = new MungeProcessGraphModel(steps);
    	DepthFirstSearch<MungeStep, Edge> dfs = new DepthFirstSearch<MungeStep, Edge>();
    	dfs.performSearch(gm);
    	processOrder = dfs.getFinishOrder();
    	Collections.reverse(processOrder);
    	engineLogger.debug("Order of processing: " + processOrder);
	}
    
    /**
     * A package private method that will return the MungeSteps in the order that
     * this MungeProcessor will process them. Currently, it's only being used by
     * the related test case to ensure that the steps are being processed in a
     * valid order.
     */
    List<MungeStep> getProcessOrder() {
    	return processOrder;
    }

	/**
	 * A package private method that will return the {@link MungeProcess} being
	 * run by this {@link MungeProcessor}.
	 */
    MungeProcess getMungeProcess() {
    	return mungeProcess;
    }

    Logger getLogger() {
    	return engineLogger;
    }
}

