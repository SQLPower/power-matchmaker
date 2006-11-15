package ca.sqlpower.matchmaker.util.log;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * An output-only logger writing to a file on disk.
 */
public class FileLogger extends BaseLogger implements Log {

	/**
	 * The Writer that gets stuff into the file.  It is initialized
	 * lazily, so you should only access
	 * this member via the {@link #getOut(String)} method.
	 */
	private PrintWriter out;

	/**
	 * Construct this Log objet
	 * @param level The level at or above which this Logger will log
	 * @param fileName The filename to write to.
	 */
	FileLogger(Level level, String fileName) {
		super(level, fileName);
	}

	private PrintWriter getOut() {
		if (out == null) {
			try {
				String fileName = (String) getConstraint();
				out = new PrintWriter(new FileWriter(fileName));
			} catch (IOException e) {
				mapException(e);
			}
		}
		return out;
	}

	public void close() {
		getOut().close();
	}

	/** Return false, since this logger is write-only
	 * @see ca.sqlpower.matchmaker.util.log.BaseLogger#isReadable()
	 */
	public boolean isReadable() {
		return false;
	}

	/** Return true iff this logger's writer opened successfully. */
	public boolean isWritable() {
		return getOut() != null;
	}

	@Override
	void print(String mesg) {
		getOut().print(mesg);
	}

	@Override
	void println(String mesg) {
		getOut().println(mesg);
		getOut().flush();
	}
}
