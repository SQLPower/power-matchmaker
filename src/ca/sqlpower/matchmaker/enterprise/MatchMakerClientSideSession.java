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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.olap4j.metadata.Datatype;
import org.springframework.security.AccessDeniedException;

import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.enterprise.DomainCategory;
import ca.sqlpower.dao.SPPersistenceException;
import ca.sqlpower.dao.SPPersisterListener;
import ca.sqlpower.dao.json.SPJSONMessageDecoder;
import ca.sqlpower.dao.json.SPJSONPersister;
import ca.sqlpower.dao.session.SessionPersisterSuperConverter;
import ca.sqlpower.diff.DiffChunk;
import ca.sqlpower.diff.DiffInfo;
import ca.sqlpower.diff.SimpleDiffChunkJSONConverter;
import ca.sqlpower.enterprise.ClientSideSessionUtils;
import ca.sqlpower.enterprise.DataSourceCollectionUpdater;
import ca.sqlpower.enterprise.JSONMessage;
import ca.sqlpower.enterprise.JSONResponseHandler;
import ca.sqlpower.enterprise.ServerInfoProvider;
import ca.sqlpower.enterprise.TransactionInformation;
import ca.sqlpower.enterprise.client.ProjectLocation;
import ca.sqlpower.enterprise.client.SPServerInfo;
import ca.sqlpower.enterprise.client.User;
import ca.sqlpower.matchmaker.MMRootNode;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.object.AbstractPoolingSPListener;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sql.SpecificDataSourceCollection;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.UserDefinedSQLType;
import ca.sqlpower.swingui.event.SessionLifecycleEvent;
import ca.sqlpower.swingui.event.SessionLifecycleListener;
import ca.sqlpower.util.RunnableDispatcher;
import ca.sqlpower.util.SQLPowerUtils;
import ca.sqlpower.util.UserPrompter.UserPromptOptions;
import ca.sqlpower.util.UserPrompter.UserPromptResponse;
import ca.sqlpower.util.UserPrompterFactory;
import ca.sqlpower.util.UserPrompterFactory.UserPromptType;
import ca.sqlpower.util.WorkspaceContainer;

public class MatchMakerClientSideSession implements WorkspaceContainer, RunnableDispatcher {

	private static Logger logger = Logger.getLogger(MatchMakerClientSideSession.class);
	
	private static CookieStore cookieStore = new BasicCookieStore();
	
	static {
        securitySessions = new HashMap<String, MatchMakerClientSideSession>();
    }
	
	/**
	 * The prefs node that will store information about the current settings of
	 * the DDL generator and compare DM panels. Currently this is stored in prefs
	 * because we want to store it per user for each project they are using. In the
	 * future we may want to store this in the server, once per user per project.
	 */
	private final Preferences prefs = Preferences.userNodeForPackage(MatchMakerClientSideSession.class);
	
	/**
	 * Describes the location of the project that this session represents.
	 */
	private final ProjectLocation projectLocation;

    /**
     * An {@link HttpClient} used to send updates to the server for changes to
     * the project and to receive updates from other users from the server.
     */
	private final HttpClient outboundHttpClient;
	
	/**
	 * The persister that will update the project in this session with changes from
	 * the server.
	 */
	private final MatchMakerSessionPersister sessionPersister;
	
	/**
	 * Holds the current ddlg
	 */
	private DDLGenerator ddlGenerator;
	
	/**
	 * Used to convert JSON sent from the server into persist calls to forward the
	 * server changes to the {@link #sessionPersister}.
	 */
	private final SPJSONPersister jsonPersister;
	private final NetworkConflictResolver updater;
	private final SPJSONMessageDecoder jsonMessageDecoder;
	private final DataSourceCollectionUpdater dataSourceCollectionUpdater;
	
    private AbstractPoolingSPListener deletionListener;
	
	private DataSourceCollection <JDBCDataSource> dataSourceCollection;
	
	/**
	 * Used to store sessions which hold nothing but security info.
	 */
	public static Map<String, MatchMakerClientSideSession> securitySessions;

	private final MatchMakerSession delegateSession;
    
    public static HttpClient createHttpClient(SPServerInfo serviceInfo) {
        return ClientSideSessionUtils.createHttpClient(serviceInfo, cookieStore);
    }

    /**
     * Map of server addresses to system workspaces. Use
     * {@link SPServerInfo#getServerAddress()} as the key.
     */
	public static Map<String, MatchMakerClientSideSession> getSecuritySessions() {
        return securitySessions;
    }
	
	/**
	 * Exposes the shared cookie store so we don't spawn useless sessions
	 * through the client.
	 */
	public static CookieStore getCookieStore() {
		return cookieStore;
	}
	
	public static ProjectLocation createNewServerSession(SPServerInfo serviceInfo, String name, MatchMakerSession session)
    throws URISyntaxException, ClientProtocolException, IOException, JSONException {
        return ClientSideSessionUtils.createNewServerSession(serviceInfo,
                name,
                cookieStore,
                session.createUserPrompterFactory().createUserPrompter("You do not have sufficient privileges to create a new workspace.", 
                        UserPromptType.MESSAGE, 
                        UserPromptOptions.OK, 
                        UserPromptResponse.OK, 
                        "OK", "OK"));
    }
	
	public static void deleteServerWorkspace(ProjectLocation projectLocation, MatchMakerSession session) throws URISyntaxException, ClientProtocolException, IOException {
    	
	    ClientSideSessionUtils.deleteServerWorkspace(projectLocation,
	            cookieStore,
	            session.createUserPrompterFactory().createUserPrompter("You do not have sufficient privileges to delete the selected workspace.", 
                       UserPromptType.MESSAGE, 
                       UserPromptOptions.OK, 
                       UserPromptResponse.OK, 
                       "OK", "OK"));
    }
    
    public static void persistRevisionFromServer(ProjectLocation projectLocation, 
            int revisionNo, 
            SPJSONMessageDecoder decoder)
    throws IOException, URISyntaxException, SPPersistenceException, IllegalArgumentException {
        
        ClientSideSessionUtils.persistRevisionFromServer(projectLocation, revisionNo, decoder, cookieStore);
    }
    
    /**
     * This method reverts the server workspace specified by the given project location
     * to the specified revision number.
     * 
     * All sessions should automatically update to the reverted revision due to their Updater.
     * 
     * @returns The new global revision number, right after the reversion, or -1 if the server did not revert.
     * @throws IOException
     * @throws URISyntaxException
     * @throws JSONException 
     */
    public static int revertServerWorkspace(ProjectLocation projectLocation,
    		int revisionNo)
    throws IOException, URISyntaxException, JSONException {
        return ClientSideSessionUtils.revertServerWorkspace(projectLocation, revisionNo, cookieStore);
    }
    
    public MatchMakerClientSideSession(String name,
			ProjectLocation projectLocation,
    		MatchMakerSession delegateSession) throws SQLObjectException {
		
		this.projectLocation = projectLocation;
		this.delegateSession = delegateSession;
		dataSourceCollectionUpdater = new MatchMakerDataSourceCollectionUpdater(projectLocation);
		
		String ddlgClass = prefs.get(this.projectLocation.getUUID() + ".ddlg", null);
		if (ddlgClass != null) {
		    try {
                DDLGenerator ddlg = (DDLGenerator) Class.forName(ddlgClass, true, MatchMakerClientSideSession.class.getClassLoader()).newInstance();
                setDDLGenerator(ddlg);
                ddlg.setTargetCatalog(prefs.get(this.projectLocation.getUUID() + ".targetCatalog", null));
                ddlg.setTargetSchema(prefs.get(this.projectLocation.getUUID() + ".targetSchema", null));
            } catch (Exception e) {
                delegateSession.createUserPrompterFactory().createUserPrompter("Cannot load DDL settings due to missing class " + ddlgClass, 
                        UserPromptType.MESSAGE, UserPromptOptions.OK, UserPromptResponse.OK, null, "OK");
                logger.error("Cannot find DDL Generator for class " + ddlgClass + 
                        ", ddl generator properties are not loaded.");
            }
		}
		
		outboundHttpClient = ClientSideSessionUtils.createHttpClient(projectLocation.getServiceInfo(), cookieStore);
		dataSourceCollection = getDataSources();
		
		sessionPersister = new MatchMakerSessionPersister("inbound-" + projectLocation.getUUID(), getWorkspace(), 
		        new MatchMakerPersisterSuperConverter(dataSourceCollection, getWorkspace()));
		sessionPersister.setWorkspaceContainer(this);
		
		jsonMessageDecoder = new SPJSONMessageDecoder(sessionPersister);
		
		updater = new NetworkConflictResolver(
		        projectLocation, 
		        jsonMessageDecoder, 
		        ClientSideSessionUtils.createHttpClient(projectLocation.getServiceInfo(), cookieStore), 
		        outboundHttpClient, this);
		
		jsonPersister = new SPJSONPersister(updater);
		
		verifyServerLicense(projectLocation);
	}
    
    public boolean close() {
    	if (getDDLGenerator() != null) {
    	    if (getDDLGenerator().getTargetCatalog() != null) {
    	        prefs.put(projectLocation.getUUID() + ".targetCatalog", getDDLGenerator().getTargetCatalog());
    	    }
    	    if (getDDLGenerator().getTargetSchema() != null) {
    	        prefs.put(projectLocation.getUUID() + ".targetSchema", getDDLGenerator().getTargetSchema());
    	    }
    	    prefs.put(projectLocation.getUUID() + ".ddlg", getDDLGenerator().getClass().getName());
    	}
    	
    	try {
    	    //TODO: Figure out how to de-register the session &c.
		} catch (Exception e) {
			try {
				logger.error(e);
				
				delegateSession.createUserPrompterFactory().createUserPrompter("Cannot access the server to close the server session", 
						UserPromptType.MESSAGE,
						UserPromptOptions.OK, 
						UserPromptResponse.OK, 
						UserPromptResponse.OK, "OK");
				
			} catch (Throwable t) {
				//do nothing here because we failed on logging the error.
			}
		}
		
		updater.interrupt();
        outboundHttpClient.getConnectionManager().shutdown();
        
        if (dataSourceCollection != null) {
            dataSourceCollectionUpdater.detach(dataSourceCollection);
        }
        
        getSystemWorkspace().removeSPListener(deletionListener);
        for (DomainCategory cat : getSystemWorkspace().getChildren(DomainCategory.class)) {
            cat.removeSPListener(deletionListener);
        }
        
        return delegateSession.close();
    }

	public void setDDLGenerator(DDLGenerator ddlGenerator) {
			this.ddlGenerator = ddlGenerator;
		
	}

	public DDLGenerator getDDLGenerator() {
		return ddlGenerator;
	}
	
	public MMRootNode getSystemWorkspace() {
		SPObject spo = getSecuritySessions().get(getProjectLocation().getServiceInfo().getServerAddress()).getWorkspace();
		if(!(spo instanceof MMRootNode)) {
			throw new RuntimeException("Your system workspace must be an MMRootNode, not a " + spo.getClass());
		}
		return (MMRootNode)spo;
	}
	
	public MatchMakerClientSideSession getSystemSession() {
	    return getSecuritySessions().get(getProjectLocation().getServiceInfo().getServerAddress());
	}
	
	public ProjectLocation getProjectLocation() {
		return projectLocation;
	}

    public int getCurrentRevisionNumber() {
        return updater.getRevision();
    }
    
    public NetworkConflictResolver getUpdater() {
        return updater;
    }
	
	public User getUser() {
	    String username = getProjectLocation().getServiceInfo().getUsername();
	    User currentUser = null;
        for (User user : getSystemWorkspace().getChildren(User.class)) {
            if (user.getUsername().equals(username)) {
                currentUser = user;
            }
        }
	    return currentUser;
	}
    
    /**
     * Retrieves a locally saved preference of type double.
     * @param prefName The name of the previously saved preference
     * @return The previously saved preference, or 0 if none exists yet
     */
    public double getPrefDouble(String prefName) {
        return getPrefDouble(prefName, 0);
    }
    
    /**
     * Retrieves a locally saved preference of type double.
     * @param prefName The name of the previously saved preference
     * @param def The value this function should return 
     * if no preference was previously saved
     */
    public double getPrefDouble(String prefName, double def) {
        return prefs.getDouble(projectLocation.getUUID() + "." + prefName, def);
    }/**
	 * Enters a double value as a preference for this server session.
	 * It will be able to be loaded by this local user in the future.
	 */
    public void putPref(String prefName, double pref) {
        prefs.putDouble(projectLocation.getUUID() + "." + prefName, pref);
    }    
    
    /**
     * Enters a String as a preference for this server session.
     * It will be able to be loaded by this local user in the future.
     */
    public void putPref(String prefName, String pref) {
        prefs.put(projectLocation.getUUID() + "." + prefName, pref);
    }
    
    /**
     * Retrieves a locally saved preference of type String.
     * @param prefName The name of the previously saved preference
     * @return The previously saved preference, or null if none exists yet
     */
    public String getPref(String prefName) {
        return getPref(prefName, null);
    }
    
    /**
     * Retrieves a locally saved preference of type String.
     * @param prefName The name of the previously saved preference
     * @param def The value this function should return 
     * if no preference was previously saved
     */
    public String getPref(String prefName, String def) {
        return prefs.get(projectLocation.getUUID() + "." + prefName, def);
    }

	public DataSourceCollection<JDBCDataSource> getDataSources() {
	    if (dataSourceCollection == null) {
            dataSourceCollection = getDataSourcesFromServer();
            dataSourceCollectionUpdater.attach(dataSourceCollection);
        }
	    return dataSourceCollection;
	}
	
	private DataSourceCollection<JDBCDataSource> getDataSourcesFromServer() {
		ResponseHandler<DataSourceCollection<JDBCDataSource>> plIniHandler = 
            new ResponseHandler<DataSourceCollection<JDBCDataSource>>() {
            public DataSourceCollection<JDBCDataSource> handleResponse(HttpResponse response)
                    throws ClientProtocolException, IOException {
                
                if (response.getStatusLine().getStatusCode() == 401) {
                    throw new AccessDeniedException("Access Denied");
                }

                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new IOException(
                            "Server error while reading data sources: " + response.getStatusLine());
                }
                
                
                PlDotIni plIni;
                try {
					plIni = new PlDotIni(ClientSideSessionUtils.getServerURI(projectLocation.getServiceInfo(), "/" + ClientSideSessionUtils.REST_TAG +"/jdbc/")) {
					    
					    @Override
					    public List<UserDefinedSQLType> getSQLTypes() {
					        List<UserDefinedSQLType> types = new ArrayList<UserDefinedSQLType>();
					        types.addAll(delegateSession.getSQLTypes());
					        return types;
					    }
					    
					    public SPDataSource getDataSource(String name) {
					        SPDataSource ds = super.getDataSource(name);
					        if (ds == null) {
					            mergeNewDataSources();
					            return super.getDataSource(name);
					        } else {
					            return ds;
					        }
					    }
					    
					    public <C extends SPDataSource> C getDataSource(String name, java.lang.Class<C> classType) {
					        C ds = super.getDataSource(name, classType);
                            if (ds == null) {
                                mergeNewDataSources();
                                return super.getDataSource(name, classType);
                            } else {
                                return ds;
                            }
					    }
					    
					    private void mergeNewDataSources() {
					        DataSourceCollection<JDBCDataSource> dsc = getDataSourcesFromServer();
                            for (SPDataSource merge : dsc.getConnections()) {
                                mergeDataSource(merge);
                            }
					    }
					};
                    plIni.read(response.getEntity().getContent());
                    logger.debug("Data source collection has URI " + plIni.getServerBaseURI());
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
                
                return new SpecificDataSourceCollection<JDBCDataSource>(plIni, JDBCDataSource.class);
            }
        };
        
        DataSourceCollection<JDBCDataSource> dsc;
        try {
            dsc = ClientSideSessionUtils.executeServerRequest(outboundHttpClient, projectLocation.getServiceInfo(), 
                    "/" + ClientSideSessionUtils.REST_TAG + "/data-sources/", plIniHandler);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        
        return dsc;
	}
	
	/**
     * Gets a list of DiffChunks representing the differences between the two revisions from the server.
     */
	public List<DiffChunk<DiffInfo>> getComparisonDiffChunks(int oldRevisionNo, int newRevisionNo) 
	throws IOException, URISyntaxException, JSONException, SPPersistenceException {
	    
        SPServerInfo serviceInfo = projectLocation.getServiceInfo();
        HttpClient httpClient = ClientSideSessionUtils.createHttpClient(serviceInfo, cookieStore);
        
        try {
            JSONMessage response = ClientSideSessionUtils.executeServerRequest(httpClient, projectLocation.getServiceInfo(),
                    "/" + ClientSideSessionUtils.REST_TAG + "/project/" + projectLocation.getUUID() + "/compare",
                    "versions=" + oldRevisionNo + ":" + newRevisionNo, 
                    new JSONResponseHandler());    
                                  
            return SimpleDiffChunkJSONConverter.decode(response.getBody());
            
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
        
	}
	
	public List<TransactionInformation> getTransactionList(long fromVersion, long toVersion)
    throws IOException, URISyntaxException, JSONException, ParseException {
        
        SPServerInfo serviceInfo = projectLocation.getServiceInfo();
        HttpClient httpClient = ClientSideSessionUtils.createHttpClient(serviceInfo, cookieStore);
        
        try {
            
            logger.info("Getting transactions between " + fromVersion + " and " + toVersion);
            JSONMessage message = ClientSideSessionUtils.executeServerRequest(httpClient, projectLocation.getServiceInfo(),
                    "/" + ClientSideSessionUtils.REST_TAG + "/project/" + projectLocation.getUUID() + "/revision_list",
                    "versions=" + fromVersion + ":" + toVersion,
                    new JSONResponseHandler());
            
            return ClientSideSessionUtils.decodeJSONRevisionList(message.getBody());
            
        } finally {
            httpClient.getConnectionManager().shutdown();
        }               
    }
	
	public void persistProjectToServer() throws SPPersistenceException {
		final SPPersisterListener tempListener = new SPPersisterListener(jsonPersister,
						new MatchMakerPersisterSuperConverter(dataSourceCollection, getWorkspace()));
		tempListener.persistObject(getWorkspace(), 0);
	}
    
	public int revertServerWorkspace(int revisionNo) throws IOException, URISyntaxException, JSONException {
	    return revertServerWorkspace(projectLocation, revisionNo);
	}
	
	public void startUpdaterThread() {
		
		final SPPersisterListener listener = new SPPersisterListener(jsonPersister, sessionPersister,
						new MatchMakerPersisterSuperConverter(dataSourceCollection, getWorkspace()));
		SQLPowerUtils.listenToHierarchy(getWorkspace(), listener);
		
		updater.setListener(listener);
		updater.setConverter(new SessionPersisterSuperConverter(dataSourceCollection, getWorkspace()));
		updater.start();
		
		delegateSession.addSessionLifecycleListener(new SessionLifecycleListener<MatchMakerSession>() {
			public void sessionClosing(SessionLifecycleEvent<MatchMakerSession> e) {
				SQLPowerUtils.unlistenToHierarchy(getWorkspace(), listener);
			}

            public void sessionOpening(SessionLifecycleEvent<MatchMakerSession> e) {
            }
		});
	}

    /**
     * This method can update any users password on the server given the correct
     * old password and done by a user with the privileges to change the user's
     * password.
     * 
     * @param session
     *            The client session that has the correct server information to
     *            post requests to the server.
     * @param username
     *            The user name of the user to update.
     * @param oldPassword
     *            The old password of the user to validate that the password can
     *            be updated correctly.
     * @param newPassword
     *            The new password to update to.
     * @param upf
     *            A user prompter to display message and error information to
     *            the user as necessary.
     */
	public void updateUserPassword(User user, 
	        String oldPassword, String newPassword, UserPrompterFactory upf) {
	    SPServerInfo serviceInfo = getProjectLocation().getServiceInfo();
        
        HttpClient client = ClientSideSessionUtils.createHttpClient(serviceInfo, cookieStore);
        
        MessageDigest digester;
        try {
            digester = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
        
        try {
            JSONObject begin = new JSONObject();
            begin.put("uuid", JSONObject.NULL);
            begin.put("method", "begin");
            
            JSONObject persist = new JSONObject();
            persist.put("uuid", user.getUUID());
            persist.put("propertyName", "password");
            persist.put("type", Datatype.STRING.toString());
            if (oldPassword == null) {
                persist.put("method", "persistProperty");
            } else {
                persist.put("method", "changeProperty");
                persist.put("oldValue", new String(Hex.encodeHex(digester.digest(oldPassword.getBytes()))));
            }
            persist.put("newValue", new String(Hex.encodeHex(digester.digest(newPassword.getBytes()))));
            
            JSONObject commit = new JSONObject();
            commit.put("uuid", JSONObject.NULL);
            commit.put("method", "commit");
            
            JSONArray transaction = new JSONArray();
            transaction.put(begin);
            transaction.put(persist);
            transaction.put(commit);

            URI serverURI = new URI("http", null, 
                    serviceInfo.getServerAddress(), 
                    serviceInfo.getPort(),
                    serviceInfo.getPath() + 
                    "/" + ClientSideSessionUtils.REST_TAG + "/project/system", 
                    "currentRevision=" + getCurrentRevisionNumber(), null);
            HttpPost postRequest = new HttpPost(serverURI);
            postRequest.setEntity(new StringEntity(transaction.toString())); 
            postRequest.setHeader("Content-Type", "application/json");
            HttpUriRequest request = postRequest;
            JSONMessage result = client.execute(request, new JSONResponseHandler());
            if (result.getStatusCode() != 200) {
                logger.warn("Failed password change");
                if (result.getStatusCode() == 412) {
                    upf.createUserPrompter("The password you have entered is incorrect.", 
                            UserPromptType.MESSAGE, 
                            UserPromptOptions.OK, 
                            UserPromptResponse.OK, 
                            "OK", "OK").promptUser("");
                } else {
                    upf.createUserPrompter(
                            "Could not change the password due to the following: " + 
                            result.getBody() + " See logs for more details.", 
                            UserPromptType.MESSAGE, 
                            UserPromptOptions.OK, 
                            UserPromptResponse.OK, 
                            "OK", "OK").promptUser("");
                }
            }
        } catch (AccessDeniedException ex) {
            logger.warn("Failed password change", ex);
            upf.createUserPrompter("The password you have entered is incorrect.", 
                    UserPromptType.MESSAGE, 
                    UserPromptOptions.OK, 
                    UserPromptResponse.OK, 
                    "OK", "OK").promptUser("");
        } catch (Exception ex) {
            logger.warn("Failed password change", ex);
            upf.createUserPrompter(
                    "Could not change the password due to the following: " + 
                    ex.getMessage() + " See logs for more details.", 
                    UserPromptType.MESSAGE, 
                    UserPromptOptions.OK, 
                    UserPromptResponse.OK, 
                    "OK", "OK").promptUser("");
        }
	}

    protected void verifyServerLicense(ProjectLocation projectLocation) throws AssertionError {
        try {
            ServerInfoProvider.getServerVersion(
                    projectLocation.getServiceInfo().getServerAddress(), 
                    String.valueOf(projectLocation.getServiceInfo().getPort()), 
                    projectLocation.getServiceInfo().getPath(), 
                    projectLocation.getServiceInfo().getUsername(), 
                    projectLocation.getServiceInfo().getPassword(),
                    cookieStore);
        } catch (Exception e) {
            throw new AssertionError("Exception encountered while verifying the server license:" + e.getMessage());
        }
    }

	@Override
	public void runInForeground(Runnable runner) {
		delegateSession.runInForeground(runner);
		
	}

	@Override
	public void runInBackground(Runnable runner) {
		delegateSession.runInBackground(runner);
	}

	@Override
	public boolean isForegroundThread() {
		return delegateSession.isForegroundThread();
	}

	@Override
	public SPObject getWorkspace() {
		return delegateSession.getWorkspace();
	}
}
