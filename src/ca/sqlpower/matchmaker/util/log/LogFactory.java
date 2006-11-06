package ca.sqlpower.matchmaker.util.log;

import java.util.List;

/**
 * LogFactory gets access to a Log implementation, using
 * to-be-announced criteria.
 * For now it must be able to read the logs written by the engine.
 */
public class LogFactory {

	private static class DefaultLogger implements Log {
		Level level;

		private DefaultLogger(Level level, Object constraint) {
			this.level = level;
		}

		public void close() {
			// nothing to do
		}

		public boolean isReadable() {
			return false;
		}

		public boolean isWritable() {
			return true;
		}

		public void log(Level level, Object message) {
			if (shouldLog(level)) {
				System.out.println(message);				
			}
		}

		private boolean shouldLog(Level messageLevel) {
			return messageLevel.ordinal() >= this.level.ordinal();
		}

		public void log(Level level, Object message, Throwable t) {
			if (shouldLog(level)) {
				System.out.println(message);
				t.printStackTrace();
			}
		}

		public void open(Object destination) {
			// nothing to do
		}

		public String read() {
			throw new UnsupportedOperationException("can not read back this log");
		}

		public List<String> readAsList() {
			throw new UnsupportedOperationException("can not read back this log");
		}

		public long size() {
			return -1;
		}

		public void truncate() {
			// nothing to do
		}

	}

	/**
	 * The log factory returns a logger appropriate for the given constraint.
	 * @param constraint XXX to be defined.
	 * @return Currently only returns a default logger that writes to stdout.
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
