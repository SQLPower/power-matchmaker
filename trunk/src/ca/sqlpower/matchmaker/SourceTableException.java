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

package ca.sqlpower.matchmaker;

/**
 * This is an exception related to changes in the project source table. This is
 * because such a change would most likely cause the engines to fail and also
 * break other parts of the matchmaker.
 */
public class SourceTableException extends Exception {
	public SourceTableException(String message) {
		super(message);
	}
	
	public SourceTableException(String message,Throwable cause) {
		super(message,cause);
	}
}
