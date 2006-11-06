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
public class ReadbackLogger implements Log {

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
		super();
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

	private void mapException(Exception e) {
		throw new RuntimeException("Error", e);
	}

	public String read() {
		// TODO Auto-generated method stub
		return null;
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

}
