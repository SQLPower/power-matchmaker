package ca.sqlpower.matchmaker.swingui;

import java.lang.Thread.UncaughtExceptionHandler;

import javax.swing.JFrame;

import ca.sqlpower.architect.swingui.ASUtils;

public class ExceptionHandler implements UncaughtExceptionHandler {

	private JFrame parentFrame;

	public ExceptionHandler(JFrame parentFrame) {
		this.parentFrame = parentFrame;
	}

	public void uncaughtException(Thread t, Throwable e) {
		ASUtils.showExceptionDialog(parentFrame, e.getMessage(), e);
	}

}
