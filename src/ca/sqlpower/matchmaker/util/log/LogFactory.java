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

import java.util.Date;
import java.util.List;

/**
 * LogFactory gets access to a Log implementation, using
 * to-be-announced criteria.
 * For now it must be able to read the logs written by the engine.
 */
public class LogFactory {

	/**
	 * Private default logger class, just BaseLogger with System.out
	 * instead of a PrintWriter(FileWriter).
	 */
	private static class DefaultLogger extends BaseLogger implements Log {
		Date date = new Date();
		Level level;

		private DefaultLogger(Level level, Object constraint) {
			super(level, constraint);
			this.level = level;
		}

		public void print(String message) {
			System.out.print(message);
		}

		public void println(String message) {
			System.out.println(message);
		}

		public void close() {
			// nothing to do
		}

		public long size() {
			return -1;
		}

		public void truncate() {
			// nothing to do
		}

		public Object getConstraint() {
			return "System.out";
		}

		public boolean isReadable() {
			return false;
		}

		public boolean isWritable() {
			return false;
		}

		public List<String> readAsList() {
			return null;
		}

	}

	/**
	 * The log factory returns a logger appropriate for the given constraint.
	 * @param constraint See below.
	 * @return Currently returns a default logger that writes to stdout
	 * if the constraint is null, else the constraint is interpreted
	 * as a file name, and a FileLogger is created.
	 */
	public static Log getLogger(Level level, Object constraint) {
		return new DefaultLogger(level, constraint);
	}

	/**
	 * Return a logger that can read back the entries in the
	 * given Engine log file; this is a temporary interface
	 * that will be obviated when the Engine is rewritten in Java.
	 * @param constraint The full path of the existing logfile.
	 * @return a Logger
	 */
	public static Log getReadbackLogger(Object constraint) {
		return new ReadbackLogger((String)constraint);
	}
}
