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
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.graph.BreadthFirstSearch;
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
    
    public MungeProcessorImpl(MatchRuleSet mungeProcess) {
        this.mungeProcess = mungeProcess;
    }
    
    public Boolean call() throws Exception {
        List<MungeStep> steps = new ArrayList<MungeStep>(mungeProcess.getChildren());
        
        // topo sort
        MungeProcessGraphModel gm = new MungeProcessGraphModel(steps);
        BreadthFirstSearch<MungeStep, Edge> bfs = new BreadthFirstSearch<MungeStep, Edge>();
        List<MungeStep> searchOrder = bfs.performSearch(gm, steps.get(0));
        
        logger.debug("Order of processing: " + searchOrder);
        
        // open everyting
        
        // call until one step gives up
        
        // close everything
        
        return Boolean.TRUE;
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
