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

import ca.sqlpower.dao.PersistedSPObject;
import ca.sqlpower.dao.SPSessionPersister;
import ca.sqlpower.dao.helper.AbstractSPPersisterHelper;
import ca.sqlpower.dao.session.SessionPersisterSuperConverter;
import ca.sqlpower.matchmaker.MMRootNode;
import ca.sqlpower.object.SPObject;
import org.apache.log4j.Logger;

public class MatchMakerSessionPersister extends SPSessionPersister {

	private static Logger logger = Logger.getLogger(MatchMakerSessionPersister.class);
	
	public MatchMakerSessionPersister(String name, SPObject root,
			SessionPersisterSuperConverter converter) {
		super(name, root, converter);
	}

	@Override
	protected void refreshRootNode(PersistedSPObject pso) {
		MMRootNode matchMakerProject = (MMRootNode) root;
		matchMakerProject.setUUID(pso.getUUID());
        
        String CFPUUID = (String) AbstractSPPersisterHelper.findPropertyAndRemove(
                pso.getUUID(), "currentFolderParent", persistedProperties);
        logger.debug("Finding CFP with UUID " + CFPUUID);
        PersistedSPObject persistedCFP = AbstractSPPersisterHelper.findPersistedSPObject(
                pso.getUUID(), CFPUUID, persistedObjects);
        matchMakerProject.getCurrentFolderParent().setUUID(CFPUUID);
        persistedCFP.setLoaded(true);
        
        String BFPUUID = (String) AbstractSPPersisterHelper.findPropertyAndRemove(
                pso.getUUID(), "backupFolderParent", persistedProperties);
        logger.debug("Finding BFP with UUID " + BFPUUID);
        PersistedSPObject persistedBFP = AbstractSPPersisterHelper.findPersistedSPObject(
                pso.getUUID(), BFPUUID, persistedObjects);
        matchMakerProject.getBackupFolderParent().setUUID(BFPUUID);
        persistedBFP.setLoaded(true);
    
        
        String TGPUUID = (String) AbstractSPPersisterHelper.findPropertyAndRemove(
                pso.getUUID(), "translateGroupParent", persistedProperties);
        logger.debug("Finding TGP with UUID " + TGPUUID);
        PersistedSPObject persistedTGP = AbstractSPPersisterHelper.findPersistedSPObject(
                pso.getUUID(), TGPUUID, persistedObjects);
        matchMakerProject.getTranslateGroupParent().setUUID(TGPUUID);
        persistedTGP.setLoaded(true);
	}

}
