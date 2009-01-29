/*
 * Copyright (c) 2009, SQL Power Group Inc.
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

import org.apache.log4j.Logger;

import ca.sqlpower.sqlobject.SQLObjectException;

public class AddressCorrectionEngineImpl extends AbstractEngine {

	private static final Logger logger = Logger.getLogger(AddressCorrectionEngineImpl.class);
	
	public void checkPreconditions() throws EngineSettingException,
			SQLObjectException, SourceTableException {
		// TODO Auto-generated method stub
		logger.debug("Stub call: AddressCorrectionEngineImpl.checkPreconditions()");

	}

	public Logger getLogger() {
		// TODO Auto-generated method stub
		logger.debug("Stub call: AddressCorrectionEngineImpl.getLogger()");
		return null;
	}

	public String getObjectType() {
		// TODO Auto-generated method stub
		logger.debug("Stub call: AddressCorrectionEngineImpl.getObjectType()");
		return null;
	}

}
