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

package ca.sqlpower.matchmaker;

import java.util.concurrent.Callable;

import ca.sqlpower.util.Monitorable;

/**
 * The Processor interface exposes all the methods necessary for
 * performing an operation in the Match engine.
 */
public interface Processor extends Callable<Boolean>, Monitorable {

    /**
     * Performs the entire operation that exists in this processor.
     * Progress on this process can be monitored using the Monitorable
     * methods.
     * 
     * @return Always returns Boolean.TRUE, meaning success, or terminates by
     * throwing an exception, meaning failure.
     */
    public Boolean call() throws Exception;
    
}
