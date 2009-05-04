/*
 * Copyright (c) 2009, SQL Power Group Inc.
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

package ca.sqlpower.matchmaker.swingui.address;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.address.AddressPool;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.swingui.SPSwingWorker;

/**
 * A subclass of {@link MonitorableWorker} that will load an {@link AddressPool} in
 * a background thread, and then create an {@link AddressValidationPanel} and then 
 * display then set the current editor of the given {@link MatchMakerSwingSession}
 * to that panel. This is to be able to provide a progress bar when loading the 
 * validation screen.
 */
public class AddressPoolLoadingWorker extends SPSwingWorker {

	private static final Logger logger = Logger.getLogger(AddressPoolLoadingWorker.class);
	
	private boolean started = false;
	private boolean finished = false;
	private String message = "";
	private MatchMakerSwingSession session;
	private AddressPool pool;
	private AddressValidationPanel panel;
	
	/**
	 * @param pool The Address Pool that we want to load and display in an {@link AddressValidationPanel}
	 * @param session The {@link MatchMakerSwingSession} that will contain the panel we generate 
	 */
	public AddressPoolLoadingWorker(AddressPool pool, MatchMakerSwingSession session) {
		super(session);
		this.session = session;
		this.pool = pool;
	}

	@Override
	public void cleanup() throws Exception {
		if (getDoStuffException() != null) {
			throw new Exception("An error occured while loading the Address Pool", getDoStuffException());
		}
		session.setCurrentEditorComponent(panel);
		finished = true;
	}

	@Override
	public void doStuff() throws Exception {
		finished = false;
		started = true;
		message = "Loading invalid addresses";
		pool.load(logger);
		panel = new AddressValidationPanel(session, pool);
	}

	public Integer getJobSize() {
		return (pool.getJobSize() == null) ? null : pool.getJobSize();
	}

	public String getMessage() {
		return message;
	}

	public int getProgress() {
		return pool.getProgress();
	}

	public boolean hasStarted() {
		return started;
	}

	public boolean isFinished() {
		return finished;
	}
}
