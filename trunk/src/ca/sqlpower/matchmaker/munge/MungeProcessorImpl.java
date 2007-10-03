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

package ca.sqlpower.matchmaker.munge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.graph.DepthFirstSearch;
import ca.sqlpower.matchmaker.MatchRuleSet;
import ca.sqlpower.matchmaker.munge.MungeProcessGraphModel.Edge;
import ca.sqlpower.util.MonitorableImpl;

public class MungeProcessorImpl implements MungeProcessor {

    private static final Logger logger = Logger.getLogger(MungeProcessorImpl.class);
    
    /**
     * We delegate all the monitorable stuff to this helper.
     */
    private final MonitorableImpl monitorableHelper = new MonitorableImpl();

    /**
     * The munging process this processor is responsible for executing.  The
     * graph of munge steps is retrieved fresh every time this processor is
     * invoked, so that a single munge processor can be used for previewing
     * at design time.
     */
    private final MatchRuleSet mungeProcess;
    
    /**
     * The list of MungeSteps obtained from the MungeProcess that this processor will
     * process, sorted in the exact order that the processor will process them.
     */
    private List<MungeStep> processOrder;
    
    public MungeProcessorImpl(MatchRuleSet mungeProcess) {
        this.mungeProcess = mungeProcess;
        List<MungeStep> steps = new ArrayList<MungeStep>(mungeProcess.getChildren());
        
        // topo sort
        MungeProcessGraphModel gm = new MungeProcessGraphModel(steps);
        DepthFirstSearch<MungeStep, Edge> dfs = new DepthFirstSearch<MungeStep, Edge>();
        dfs.performSearch(gm);
        processOrder = dfs.getFinishOrder();
        Collections.reverse(processOrder);
        logger.debug("Order of processing: " + processOrder);
    }
    
    public Boolean call() throws Exception {
        
    	monitorableHelper.setStarted(true);
    	
        // open everything
        for (MungeStep step: processOrder) {
        	step.open();
        }
        
        // call until one step gives up
        boolean finished = false;
        while(!finished) {
        	for (MungeStep step: processOrder) {
        		boolean continuing = step.call();
        		if (!continuing) {
        			finished = true;
        			break;
        		}
        	}
        }
        
        // close everything
        for (MungeStep step: processOrder) {
        	step.close();
        }
        
        monitorableHelper.setFinished(true);
        
        return Boolean.TRUE;
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
    
    // ========== Monitorable Methods ============
    
    public Integer getJobSize() {
        return monitorableHelper.getJobSize();
    }

    public String getMessage() {
        return monitorableHelper.getMessage();
    }

    public int getProgress() {
        return monitorableHelper.getProgress();
    }

    public boolean hasStarted() {
        return monitorableHelper.hasStarted();
    }

    public boolean isCancelled() {
        return monitorableHelper.isCancelled();
    }

    public boolean isFinished() {
        return monitorableHelper.isFinished();
    }

    public void setCancelled(boolean cancelled) {
        monitorableHelper.setCancelled(cancelled);
    }
}
