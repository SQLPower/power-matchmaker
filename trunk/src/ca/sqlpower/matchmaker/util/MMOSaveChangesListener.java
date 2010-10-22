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

package ca.sqlpower.matchmaker.util;

import java.beans.PropertyChangeEvent;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.util.SQLPowerUtils;
import ca.sqlpower.util.TransactionEvent;

/**
 * A listener for community edition swing sessions (without DQguru server connections).
 * Keeps track of changes since the last save.
 */
public class MMOSaveChangesListener implements SPListener {
	
	private Logger logger = Logger.getLogger(MMOSaveChangesListener.class);
	
	private MatchMakerSwingSession session;
	
	public MMOSaveChangesListener(MatchMakerSwingSession session) {
		this.session = session;
		SQLPowerUtils.listenToHierarchy(session.getRootNode(), this);
	}
	
	@Override
	public void childAdded(SPChildEvent e) {
		if (!session.isUnsaved() && ((MatchMakerObject)e.getChild()).isMagicEnabled()) {
			logger.error("Child add happened! I'm " + e.getSource().getClass().getSimpleName() +"'s " + e.getChild().getClass().getSimpleName());
			changeHappened();
		}
		SQLPowerUtils.listenToHierarchy(e.getChild(), this);
	}

	@Override
	public void childRemoved(SPChildEvent e) {
		if (!session.isUnsaved() && ((MatchMakerObject)e.getChild()).isMagicEnabled()) {
			logger.error("Child remove happened! On " + e.getSource().getClass().getSimpleName() +"'s " + e.getChild().getClass().getSimpleName());
			changeHappened();
		}
		SQLPowerUtils.unlistenToHierarchy(e.getChild(), this);
	}

	@Override
	public void propertyChanged(PropertyChangeEvent evt) {
		if (!session.isUnsaved() && ((MatchMakerObject)evt.getSource()).isMagicEnabled()) {
			logger.error("Property change happened! On " + evt.getSource().getClass().getSimpleName() + "'s " + evt.getPropertyName());
			changeHappened();
		}
	}

	@Override
	public void transactionEnded(TransactionEvent e) {
	}

	@Override
	public void transactionRollback(TransactionEvent e) {
	}

	@Override
	public void transactionStarted(TransactionEvent e) {
	}
	
	private void changeHappened() {
		session.setUnsaved(true);
	}

}
