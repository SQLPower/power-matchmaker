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

package ca.sqlpower.matchmaker.munge;

/**
 * A runtime exception that is meant to be thrown when a process receives
 * a data that is a different data type from what it expected.
 * <p>
 * For example, a process that was expecting a String input but received
 * an Integer intead could throw this exception.
 */
public class UnexpectedDataTypeException extends RuntimeException {
	public UnexpectedDataTypeException() {
		super();
	}

	public UnexpectedDataTypeException(String message) {
		super(message);
	}
	
	public UnexpectedDataTypeException(String message, Throwable cause) {
		super(message, cause);
	}
}
