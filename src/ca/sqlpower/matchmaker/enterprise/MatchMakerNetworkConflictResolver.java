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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.security.AccessDeniedException;

import ca.sqlpower.dao.FriendlyRuntimeSPPersistenceException;
import ca.sqlpower.dao.MessageSender;
import ca.sqlpower.dao.PersistedSPObject;
import ca.sqlpower.dao.RemovedObjectEntry;
import ca.sqlpower.dao.SPSessionPersister;
import ca.sqlpower.dao.json.SPJSONMessageDecoder;
import ca.sqlpower.enterprise.AbstractNetworkConflictResolver;
import ca.sqlpower.enterprise.JSONMessage;
import ca.sqlpower.enterprise.client.ProjectLocation;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.util.MonitorableImpl;
import ca.sqlpower.util.UserPrompter.UserPromptOptions;
import ca.sqlpower.util.UserPrompter.UserPromptResponse;
import ca.sqlpower.util.UserPrompterFactory.UserPromptType;

public class MatchMakerNetworkConflictResolver extends AbstractNetworkConflictResolver implements MessageSender<JSONObject> {

	private static Logger logger = Logger.getLogger(AbstractNetworkConflictResolver.class);
	private final MatchMakerClientSideSession session;
	
	public MatchMakerNetworkConflictResolver(ProjectLocation projectLocation,
			SPJSONMessageDecoder jsonDecoder,
			HttpClient inboundHttpClient,
			HttpClient outboundHttpClient,
            MatchMakerClientSideSession session) {
		super(projectLocation, jsonDecoder, inboundHttpClient, outboundHttpClient,
				session);
		// TODO Auto-generated constructor stub
		this.session = session;
	}
    
    @Override
    protected void flush(boolean reflush) {
        if (postingJSON.get() && !reflush) {
            return;
        }
        MonitorableImpl monitor = null;
        long startTimeMillis = System.currentTimeMillis();
        long messageLength = messageBuffer.length();
        try {
            postingJSON.set(true);
            
            // Try to send json message ...
            JSONMessage response = null;
            try {
                response = postJsonArray(messageBuffer.toString());
            } catch (AccessDeniedException e) {
                List<UpdateListener> listenersToRemove = new ArrayList<UpdateListener>();
                for (UpdateListener listener : updateListeners) {
                    if (listener.updateException(MatchMakerNetworkConflictResolver.this, e)) {
                        listenersToRemove.add(listener);
                    }
                }
                updateListeners.removeAll(listenersToRemove);
                if (upf != null) {
                    upf.createUserPrompter(
                            "You do not have sufficient privileges to perform that action. " +
                            "Please hit the refresh button to synchronize with the server.", 
                            UserPromptType.MESSAGE, 
                            UserPromptOptions.OK, 
                            UserPromptResponse.OK, 
                            "OK", "OK").promptUser("");
                } else {
                    throw e;
                }
                return;
            }
            if (response.isSuccessful()) {
                // Sent json message without conflict.
                try {
                    currentRevision = (new JSONObject(response.getBody())).getInt("currentRevision");
                } catch (JSONException e) {
                    throw new RuntimeException("Could not update current revision" + e.getMessage());
                }
                long endTime = System.currentTimeMillis();
                if (messageLength != 0) {
                    double processTimePerObj = (endTime - startTimeMillis) / messageLength;
                    currentWaitPerPersist = currentWaitPerPersist * 0.9 + processTimePerObj * 0.1;
                }
            } else {
                // Did not successfully post json, we must update ourselves, and then try again if we can. 
                if (!reflush) {
                    // These lists should reflect the state of the workspace at the time of the conflict.
                    // The workspace could be updated several times before a successful post is made.
                    fillOutboundPersistedLists();
                }
                // Try to rollback our changes
                try {
                    SPSessionPersister.undoForSession(session.getWorkspace(), 
                            new LinkedList<PersistedSPObject>(outboundObjectsToAdd.values()),
                            outboundPropertiesToChange, 
                            new LinkedList<RemovedObjectEntry>(outboundObjectsToRemove.values()), converter);
                } catch (Exception e) {
                    throw new RuntimeException("Reflush failed on rollback", e);
                }
                
                //If the preconditions failed which caused the persist to fail don't try to 
                //push the persist forward again.
                if (!response.isSuccessful() && response.getStatusCode() == 412) {
                    logger.info("Friendly error occurred, " + response);
                    throw new FriendlyRuntimeSPPersistenceException(response.getBody());
                }
                
                final String json;
                final int newRev;
                try {
                    JSONObject jsonObject = new JSONObject(response.getBody());
                    json = jsonObject.getString("data");
                    newRev = jsonObject.getInt("currentRevision");
                } catch (Exception e) {
                    throw new RuntimeException("Reflush failed on getJson", e);
                }
                // Try to create inboundPersistedLists for comparison with the outbound. These will be used
                // for special case collision detection.
                fillInboundPersistedLists(json);

                // Try to apply update
                decodeMessage(new JSONTokener(json), newRev);
                // We need an additional step here for checking for special case conflicts
                List<ConflictMessage> conflicts = detectConflicts();
                if (conflicts.size() == 0) {
                    // Try to return the persisted objects to their state pre-update.
                    try {
                        SPSessionPersister.redoForSession(getWorkspace(), 
                                new LinkedList<PersistedSPObject>(outboundObjectsToAdd.values()),
                                outboundPropertiesToChange, 
                                new LinkedList<RemovedObjectEntry>(outboundObjectsToRemove.values()), converter);
                        // We want to re-send our changes, but only if we were able to restore them
                        flush(true);
                    } catch (Exception ex) {
                        throw new RuntimeException("Reflush failed on rollforward", ex);
                    }
                } else {
                    String message = "";
                    message += "Your changes have been discarded due to a conflict between you and another user: \n";
                    for (int i = 0; i < AbstractNetworkConflictResolver.MAX_CONFLICTS_TO_DISPLAY && i < conflicts.size(); i++) {
                        message += conflicts.get(i).getMessage() + "\n";
                    }
                    session.createUserPrompterFactory().createUserPrompter(message, 
                            UserPromptType.MESSAGE, 
                            UserPromptOptions.OK, 
                            UserPromptResponse.OK, 
                            "OK", "OK").promptUser("");
                }
            }
        } finally {
            if (monitor != null) {
                monitor.setFinished(true);
            }
            postingJSON.set(false);
            clear(true);
        }
    }
    
    @Override
    protected List<ConflictMessage> detectConflicts() {
        List<ConflictMessage> conflicts = checkForSimultaneousEdit();
        return conflicts;
    }
	
	@Override
	protected SPObject getWorkspace() {
		return session.getWorkspace();
	}
}
