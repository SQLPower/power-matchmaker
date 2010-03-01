/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of DQguru
 *
 * DQguru is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DQguru is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */


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
