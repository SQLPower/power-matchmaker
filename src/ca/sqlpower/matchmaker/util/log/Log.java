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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.i 
 */

package ca.sqlpower.matchmaker.util.log;

import java.util.List;

public interface Log {
	/**
	 * Return the persistence associated with this log;
	 * the object may be a String (filename), or it may be
	 * something quite different in future
	 * (it will always implement a user-meaningful toString()).
	 */
	public Object getConstraint();

	/**
	 *  True if the log contents can be read back
	 */
	public boolean isReadable();
	/**
	 * True if you can write to the log via the log() methods
	 */
	public boolean isWritable();

	/**
	 * The size of the log contents, if known.
	 */
	public long size();

	/**
	 * log the message
	 * @param level the severity of the message
	 * @param message the message to be logged
	 */
	public void log(Level level,Object message );
	/**
	 *  Log a message with an exception
	 */
	public void log(Level level,Object message, Throwable t);

	/** truncate the log */
	public void truncate();

	/** if readable, return the log broken up by messages */
	public List<String> readAsList();

	/** close the log file */
	public void close();


}
