package ca.sqlpower.matchmaker.swingui;

import java.lang.Thread.UncaughtExceptionHandler;

import javax.swing.JFrame;

/**
 * Just a bridge from Java's UncaughtExceptionHandler to our
 * {@link MMSUtils#showExceptionDialog(java.awt.Component, String, Throwable)}.
 */
public class ExceptionHandler implements UncaughtExceptionHandler {

	/**
     * The URL to post the error report to if the system property
     * that overrides it isn't defined.
     */
    public static final String DEFAULT_REPORT_URL = "http://bugs.sqlpower.ca/matchmaker/postReport";
    
    /**
     * The parent frame to this exception. Used to display the
     * exception to the user.
     */
	private JFrame parentFrame;

	public ExceptionHandler(JFrame parentFrame) {
		this.parentFrame = parentFrame;
	}

	public void uncaughtException(Thread t, Throwable e) {
		MMSUtils.showExceptionDialog(parentFrame, "Caught Background Exception", e);
	}

}
