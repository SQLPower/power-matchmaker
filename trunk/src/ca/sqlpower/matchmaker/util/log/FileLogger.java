package ca.sqlpower.matchmaker.util.log;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * An output-only logger writing to a file on disk.
 */
public class FileLogger extends BaseLogger implements Log {

	private PrintWriter out;

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

	public boolean isReadable() {
		return false;
	}

	public boolean isWritable() {
		return out != null;
	}

	public List<String> readAsList() {
		throw new UnsupportedOperationException("Write-only logger");
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
