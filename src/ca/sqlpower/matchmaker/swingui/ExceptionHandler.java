package ca.sqlpower.matchmaker.swingui;

import java.lang.Thread.UncaughtExceptionHandler;

import javax.swing.JFrame;

import ca.sqlpower.swingui.SPSUtils;

/**
 * Just a bridge from Java's UncaughtExceptionHandler to our
 * SPSUtils.showExceptionDialog.
 */
public class ExceptionHandler implements UncaughtExceptionHandler {

	private JFrame parentFrame;

	public ExceptionHandler(JFrame parentFrame) {
		this.parentFrame = parentFrame;
	}

	public void uncaughtException(Thread t, Throwable e) {
		SPSUtils.showExceptionDialog(parentFrame, "Caught Background Exception", e, new MatchMakerQFAFactory());
	}

}
