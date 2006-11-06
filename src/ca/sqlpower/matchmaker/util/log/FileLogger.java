package ca.sqlpower.matchmaker.util.log;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * An output-only logger writing to a file on disk.
 */
public class FileLogger extends BaseLogger implements Log {

	/**
	 * The Writer that gets stuff into the file.
	 */
	private PrintWriter out;

	/**
	 * Construct this Log objet
	 * @param level The level at or above which this Logger will log
	 * @param fileName The filename to write to.
	 */
	FileLogger(Level level, String fileName) {
		super(level, fileName);
		try {
			out = new PrintWriter(new FileWriter(fileName));
		} catch (IOException e) {
			mapException(e);
		}
	}

	public void close() {
		out.close();
	}

	/** Return false, since this logger is write-only
	 * @see ca.sqlpower.matchmaker.util.log.BaseLogger#isReadable()
	 */
	public boolean isReadable() {
		return false;
	}

	/** Return true iff this logger's writer opened successfully. */
	public boolean isWritable() {
		return out != null;
	}

	@Override
	void print(String mesg) {
		out.print(mesg);
	}

	@Override
	void println(String mesg) {
		out.println(mesg);
		out.flush();
	}
}
