package ca.sqlpower.matchmaker.util.log;

import java.util.Date;
import java.util.List;

/**
 * An abstract base class for a series of output Log implementations.
 */
public abstract class BaseLogger implements Log {

	/** The constraint (most likely a filename) */
	final Object constraint;

	/** The date, for timestamping the log */
	final Date date = new Date();

	/** The level at (or above) which this logger will log */
	final Level level;

	BaseLogger(Level level, Object constraint) {
		this.level = level;
		this.constraint = constraint;
	}

	public Object getConstraint() {
		return constraint;
	}

	abstract void print(String mesg);

	abstract void println(String mesg);

	void mapException(Exception e) {
		throw new RuntimeException("Error", e);
	}

	public boolean isReadable() {
		return false;
	}

	public boolean isWritable() {
		return true;
	}

	/**
	 * Do the logging to the file, using print()/println();
	 * this is the heart of this logger.
	 * @see ca.sqlpower.matchmaker.util.log.Log#log(ca.sqlpower.matchmaker.util.log.Level, java.lang.Object)
	 */
	public void log(Level level, Object message) {
		if (shouldLog(level)) {
			date.setTime(System.currentTimeMillis());
			print(date.toString());
			print(" ");
			println(message.toString());
		}
	}

	/** Delegate to log() method, then format the Throwable */
	public void log(Level level, Object message, Throwable t) {
		if (shouldLog(level)) {
			log(level, message);
			t.printStackTrace();
		}
	}

	boolean shouldLog(Level messageLevel) {
		return messageLevel.ordinal() >= this.level.ordinal();
	}

	public long size() {
		return -1;
	}

	public void truncate() {
		// nothing to do
	}

	/**
	 * Throw an UnsupportedOperationException since this is
	 * a write-only logger.
	 * @see ca.sqlpower.matchmaker.util.log.Log#readAsList()
	 */
	public List<String> readAsList() {
		throw new UnsupportedOperationException("Write-only logger");
	}

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((constraint == null) ? 0 : constraint.hashCode());
        result = PRIME * result + ((level == null) ? 0 : level.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final BaseLogger other = (BaseLogger) obj;
        if (constraint == null) {
            if (other.constraint != null)
                return false;
        } else if (!constraint.equals(other.constraint))
            return false;
        if (level == null) {
            if (other.level != null)
                return false;
        } else if (!level.equals(other.level))
            return false;
        return true;
    }
    
    
    
}
