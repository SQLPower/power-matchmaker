/*
 * Copyright (c) 2007, SQL Power Group Inc.
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

/**
 *  <pre>
 *  e.g.:merge_odbc.exe MERGE=xxx USER=xx/xx
 *      DEBUG=[N/Y] COMMIT_FREQ=100 LOG_FILE=xxx ERR_FILE=xxx
 *      PROCESS_CNT=0 USER_PROMPT=[Y/N] SHOW_PROGRESS=0
 *      ROLLBACK_SEGMENT_NAME=[ROLLBACK_SEGMENT_NAME]
 *      APPEND_TO_LOG_IND=[N/Y] SEND_EMAIL=[Y/N]
 *  </pre>
 */
public class MergeEngineImpl extends AbstractCEngine {

	private static final Logger logger = Logger.getLogger(MergeEngineImpl.class);
	
	public void checkPreconditions() throws EngineSettingException {
		throw new EngineSettingException("Merge engine integration is not implemented");
	}

	public String[] createCommandLine(boolean userPrompt) {
		return new String[0];
	}
	
	/**
	 * Returns the logger for the MergeEngineImpl class.
	 */
	public Logger getLogger() {
		return logger;
	}

}
