package ca.sqlpower.matchmaker.swingui;

import java.lang.Thread.UncaughtExceptionHandler;

import javax.swing.JFrame;

import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.matchmaker.util.MatchMakerQFAFactory;

/**
 * Just a bridge from Java's UncaughtExceptionHandler to our
 * ASUtils.showExceptionDialog.
 */
public class ExceptionHandler implements UncaughtExceptionHandler {

	private JFrame parentFrame;

	public ExceptionHandler(JFrame parentFrame) {
		this.parentFrame = parentFrame;
	}

	public void uncaughtException(Thread t, Throwable e) {
		ASUtils.showExceptionDialog(parentFrame, "Caught Background Exception", e, new MatchMakerQFAFactory());
	}

}
