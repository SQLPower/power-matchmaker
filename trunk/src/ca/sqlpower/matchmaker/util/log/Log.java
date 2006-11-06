package ca.sqlpower.matchmaker.util.log;

import java.util.List;

public interface Log {
	/**
	 * True if you can write to the log
	 */
	public boolean isWritable();
	/**
	 * The size of the log contents
	 */
	public long size();
	/**
	 *  True if the log contents can be read back
	 */
	public boolean isReadable();
	/**
	 * log the message
	 * @param level the severity of the message
	 * @param message the message to be logged
	 */
	public void log(Level level,Object message );
	/**
	 *  Log a message with an exception
	 */
	public void log(Level level,Object message, Throwable t);
	/** Open the log file 
	 * 
	 * @param destination 
	 */
	public void open(Object destination);
	/** close the log file */
	public void close();
	/** truncate the log */
	public void truncate();
	/** get the entire log */
	public String read();
	/** Get the log broken up by messages */
	public List<String> readStructure();
	

}
