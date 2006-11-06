package ca.sqlpower.matchmaker.util.log;

import java.util.Date;

public abstract class BaseLogger implements Log {
	Object constraint;

	Date date = new Date();
		Level level;

		BaseLogger(Level level, Object constraint) {
			this.level = level;
		}

		abstract void print(String mesg);
		abstract void println(String mesg);

		void mapException(Exception e) {
			throw new RuntimeException("Error", e);
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
				date.setTime(System.currentTimeMillis());
				print(date.toString());
				print(" ");
				println(message.toString());
			}
		}

		public void log(Level level, Object message, Throwable t) {
			if (shouldLog(level)) {
				log(level, message);
				t.printStackTrace();
			}
		}

		private boolean shouldLog(Level messageLevel) {
			return messageLevel.ordinal() >= this.level.ordinal();
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

}
