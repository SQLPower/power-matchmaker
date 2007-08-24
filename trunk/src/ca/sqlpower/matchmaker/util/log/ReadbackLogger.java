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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A read-only logger that only reads entries from an existing
 * Match or Merge log on disk.
 */
public class ReadbackLogger extends BaseLogger implements Log {

	/** The name of the file we should try to read from */
	String fileName;
	/** The File object we are trying to read */
	File file;
	/** The BufferedReader for this file */
	BufferedReader reader;
	/** True if we have ascertained that the file exists and is readable */
	boolean canRead;

	/**
	 * Non-public Constructor so only the LogFactory can
	 * hand out instances of this class.
	 * @param fileName
	 */
	ReadbackLogger(String fileName) {
		super(Level.INFO, fileName);
		this.fileName = fileName;
		this.file = new File(fileName);
		if (!file.exists()) {
			throw new IllegalArgumentException("File does not exist: " + fileName);
		}
		if (!file.canRead()) {
			throw new IllegalArgumentException("File is not readable: " + fileName);
		}
		canRead = true;
	}

	public Object getConstraint() {
		return fileName;
	}

	public boolean isReadable() {
		return canRead;
	}

	public void close() {
		if (reader != null) {
			try {
				reader.close();
			} catch (IOException e) {
				mapException(e);
			}
		}
	}

	public List<String> readAsList() {
		List<String> results = new ArrayList<String>();
		try {
			reader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = reader.readLine()) != null) {
				results.add(line);
			}
		} catch (IOException e) {
			mapException(e);
		}
		return results;
	}

	public long size() {
		if (canRead) {
			return file.length();
		}
		return -1;
	}

	// Everything else should fail; these logs are read-only

	public boolean isWritable() {
		return false;
	}

	public void log(Level level, Object message) {
		throw new UnsupportedOperationException("Read-only logger");
	}

	public void log(Level level, Object message, Throwable t) {
		throw new UnsupportedOperationException("Read-only logger");
	}
	public void truncate() {
		throw new UnsupportedOperationException("Read-only logger");
	}

	@Override
	void print(String mesg) {
		throw new UnsupportedOperationException("Read-only logger");
	}

	@Override
	void println(String mesg) {
		throw new UnsupportedOperationException("Read-only logger");
	}

}
