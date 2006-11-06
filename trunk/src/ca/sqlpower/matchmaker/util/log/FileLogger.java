package ca.sqlpower.matchmaker.util.log;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class FileLogger extends BaseLogger implements Log {

	String fileName;
	PrintWriter out;

	FileLogger(Level level, String fileName) {
		super(level, fileName);
		try {
			out = new PrintWriter(new FileWriter(fileName));
		} catch (IOException e) {
			mapException(e);
		}
	}

	public void close() {
		// TODO Auto-generated method stub

	}

	public boolean isReadable() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isWritable() {
		return out != null;
	}

	public List<String> readAsList() {
		// TODO Auto-generated method stub
		return null;
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
