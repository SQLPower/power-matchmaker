/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Power*MatchMaker.
 *
 * Power*MatchMaker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*MatchMaker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.matchmaker;

/**
 * A checked exception meant to be thrown when there is a problem with
 * the match maker's environment that the end user should be capable of
 * fixing via user settings/prefs/etc.
 * <p>
 * In general, the message for this exception should include text that
 * will explain to the user how the problem can be solved.
 */
public class MatchMakerConfigurationException extends Exception {

    /**
     * Creates a configuration exception with the given message and no
     * chained cause.
     */
    public MatchMakerConfigurationException(String message) {
        super(message);
    }

    /**
     * Creates a configuration exception with the given message and another
     * exception which is the cause of this one.
     */
    public MatchMakerConfigurationException(String message, Exception cause) {
        super(message, cause);
    }
}
