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

package ca.sqlpower.matchmaker;

import java.util.concurrent.CancellationException;

import ca.sqlpower.util.MonitorableImpl;

/**
 * An abstract implementation of Processor that implements the 
 * Monitorable methods. 
 */
public abstract class AbstractProcessor implements Processor {

    /**
     * We delegate all the monitorable stuff to this helper.
     */
    protected final MonitorableImpl monitorableHelper = new MonitorableImpl();

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

    /**
     * Checks if another thread has cancelled this process.  If so, a
     * CancellationException is thrown.
     *
     * @throws CancellationException if this process has been aborted
     */
    protected void checkCancelled() {
        if (isCancelled()) {
            throw new CancellationException("User-requested abort");
        }
    }
}
