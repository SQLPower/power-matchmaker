/*
 * Copyright (c) 2010, SQL Power Group Inc.
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

package ca.sqlpower.matchmaker.enterprise;

import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.log4j.Logger;

import ca.sqlpower.dao.json.SPJSONMessageDecoder;
import ca.sqlpower.enterprise.AbstractNetworkConflictResolver;
import ca.sqlpower.enterprise.client.ProjectLocation;
import ca.sqlpower.util.RunnableDispatcher;

public class MatchMakerNetworkConflictResolver extends AbstractNetworkConflictResolver {

	private static Logger logger = Logger.getLogger(AbstractNetworkConflictResolver.class);
	
	public MatchMakerNetworkConflictResolver(ProjectLocation projectLocation,
			SPJSONMessageDecoder jsonDecoder, HttpClient inboundHttpClient,
			HttpClient outboundHttpClient, RunnableDispatcher runnable) {
		super(projectLocation, jsonDecoder, inboundHttpClient, outboundHttpClient,
				runnable);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected List<ConflictMessage> checkForSimultaneousEdit() {
		// TODO Auto-generated method stub
		logger.debug("Stub call: AbstractNetworkConflictResolver.checkForSimultaneousEdit()");
		return null;
	}

	@Override
	protected void flush(boolean reflush) {
		// TODO Auto-generated method stub
		logger.debug("Stub call: AbstractNetworkConflictResolver.flush()");
		
	}

	@Override
	protected List<ConflictMessage> detectConflicts() {
		// TODO Auto-generated method stub
		logger.debug("Stub call: AbstractNetworkConflictResolver.detectConflicts()");
		return null;
	}

}
