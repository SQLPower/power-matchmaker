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

package ca.sqlpower.matchmaker.dao.xml;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.swingui.MMSUtils;
import ca.sqlpower.swingui.JDefaultButton;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.util.BrowserUtil;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;

/**
 * An IO Handler that provides bidirectional communication with the SQL Power web site.
 */
public class WebsiteIOHandler implements IOHandler {

    private static final Logger logger = Logger.getLogger(WebsiteIOHandler.class);
    
    private JDialog d;
    
    /**
     * Base URL where the SQL Power website lives. The various action paths will be appended
     * to this string to form the actual request URLs.
     */
    private static final String WEBSITE_BASE_URL = "http://www.sqlpower.ca/page/";
    
    /**
     * Keys for the JSONObject used with save and load permissions.
     */
	private static final String VIEW_ONLY_USERS_KEY = "viewOnlyUsers";
	private static final String VIEW_AND_MODIFY_USERS_KEY = "viewAndModifyUsers";
	private static final String PUBLIC_GROUP_KEY = "publicGroup";
	private static final String OWNERSHIP_KEY = "isOwner";
	private static final String CAN_MODIFY_KEY = "canModify";
	private static final String OWNER_KEY = "owner";

    private String sessionCookie = null;
    
    private String username = null;
    private String password = null;
    
    private boolean cancelled = false;
    
    /**
     * The DAO that's using this IO Handler. Gets set when this handler is given
     * to the DAO.
     */
    private ProjectDAOXML dao;
    
    private MatchMakerXMLSessionContext context;
    
    public List<Project> createProjectList() {
        try {
        	boolean loggedIn = false;
			while (!loggedIn && !cancelled) {
				try {
					loggedIn = login();
				} catch (IOException e) {
					// Errors connecting to server
					MMSUtils.showExceptionDialog(d, "Failed to connect to server!", e).requestFocus();
					password = null;
					continue;
				}
				
				if (!loggedIn && !cancelled) {
					// Incorrect login info
					JOptionPane.showMessageDialog(null, "Invalid username or password.",
							"Login failed", JOptionPane.ERROR_MESSAGE);
					password = null;
				}
        	}
			
			if (cancelled) {
				System.exit(0);
			}

	        URL baseURL = new URL(WEBSITE_BASE_URL);
            URL url = new URL(baseURL, "get_pp_project_list");
            HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
            urlc.setRequestMethod("GET");
            urlc.setRequestProperty("Cookie", sessionCookie);
            urlc.setDoOutput(false);
            urlc.setDoInput(true);
            urlc.connect();

            Reader in = new InputStreamReader(urlc.getInputStream());
            StringBuilder responseString = new StringBuilder();
            char[] buf = new char[2000];
            while (in.read(buf) > 0) {
                responseString.append(buf);
            }
            in.close();
            urlc.disconnect();
            
            List<Project> projects = new ArrayList<Project>();
            Set<Long> oids = new HashSet<Long>();

            JSONArray response = new JSONArray(responseString.toString());
            for (int i = 0; i < response.length(); i++) {
                JSONObject pdesc = response.getJSONObject(i);
                
                long oid = pdesc.getLong("projectId");
                if (oids.contains(oid)) {
                    logger.error("Duplicate project from server!");
                } else {
                    String description = null;
                    if (!pdesc.isNull("projectDescription")) {
                        description = pdesc.getString("projectDescription");
                    }
                    
                    Project p = new Project(oid, pdesc.getString("projectName"), description, dao);
                    loadPermissions(p);
                    projects.add(p);
                    oids.add(p.getOid());
                }
            }
            
            return projects;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public OutputStream createOutputStream(Project project) {
        return new FancyOutputStream(project);
    }
    
    /**
     * A byte array output stream that sends its content to a remote HTTP server
     * once the output stream has been closed.
     */
    private class FancyOutputStream extends ByteArrayOutputStream {
        
        /**
         * The project that will be/has been written to this stream.
         */
        private final Project project;

        public FancyOutputStream(Project project) {
            this.project = project;
        }

        @Override
        public void close() throws IOException {
            super.close();
            
            try {
            	boolean loggedIn = false;
    			while (!loggedIn && !cancelled) {
    				try {
    					loggedIn = login();
    				} catch (IOException e) {
    					// Errors connecting to server
    					MMSUtils.showExceptionDialog(d, "Failed to connect to server!", e).requestFocus();
    					password = null;
    					continue;
    				}
    				
    				if (!loggedIn && !cancelled) {
    					// Incorrect login info
    					JOptionPane.showMessageDialog(null, "Couldn't save project!",
    							"Login failed", JOptionPane.ERROR_MESSAGE);
    					password = null;
    				}
            	}
                
    			
                String xmlDoc = new String(toByteArray());
                StringBuilder sb = new StringBuilder();
                sb.append("projectXML=").append(URLEncoder.encode(xmlDoc, "UTF-8"));
                // saves a only if you can modify, otherwise makes a copy
                if (project.getOid() != null && project.canModify()) {
                    sb.append("&projectId=").append(project.getOid());
                }
                sb.append("&projectName=").append(URLEncoder.encode(project.getName(), "UTF-8"));
                if (project.getDescription() != null) {
                    sb.append("&projectDescription=").append(URLEncoder.encode(project.getDescription(), "UTF-8"));
                }

                URL baseURL = new URL(WEBSITE_BASE_URL);
                URL url = new URL(baseURL, "save_pp_project");
                HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                urlc.setRequestMethod("POST");
                urlc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                urlc.setRequestProperty("content-length", String.valueOf(sb.length()));
                urlc.setRequestProperty("Cookie", sessionCookie);
                urlc.setDoOutput(true);
                urlc.setDoInput(true);
                urlc.connect();

                OutputStream out = urlc.getOutputStream();
                out.write(sb.toString().getBytes());
                out.flush();
                out.close();
                
                // have to read in order to send request!
                Reader in = new InputStreamReader(urlc.getInputStream());
                StringBuilder responseString = new StringBuilder();
                char[] buf = new char[2000];
                while (in.read(buf) > 0) {
                    responseString.append(buf);
                }
                in.close();
                urlc.disconnect();
                
                JSONObject response = new JSONObject(responseString.toString());
                boolean success = response.getBoolean("success");
                if (!success) {
                    throw new IOException("Failed to save: " + response.getString("message"));
                } else {
                    long oid = response.getLong("projectId");
                    project.setOid(oid);
                    logger.debug("Saved project " + project.getName() + " (oid=" + project.getOid() + ")");
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    private boolean login() throws IOException {
    	cancelled = false;
        if (username == null || password == null) {
			if (!showLoginPrompt()){
                return false;
            }
        }
        
        HttpURLConnection.setFollowRedirects(false);

        StringBuilder sb = new StringBuilder();
        sb.append("username=").append(URLEncoder.encode(username, "UTF-8"));
        sb.append("&password=").append(URLEncoder.encode(password, "UTF-8"));
        sb.append("&doLogin=").append(URLEncoder.encode("true", "UTF-8"));
        sb.append("&nextUri=THE_LOGIN_WORKED");

        URL baseURL = new URL(WEBSITE_BASE_URL);
        URL url = new URL(baseURL, "login");
        HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
        urlc.setRequestMethod("POST");
        urlc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        urlc.setRequestProperty("content-length", String.valueOf(sb.length()));
        urlc.setDoOutput(true);
        urlc.setDoInput(true);
        urlc.connect();

        OutputStream out = urlc.getOutputStream();
        out.write(sb.toString().getBytes());
        out.flush();
        out.close();
        logger.debug("response headers: " + urlc.getHeaderFields());

        // have to read in order to send request!
        InputStream in = urlc.getInputStream();
        in.read();
        in.close();
        urlc.disconnect();

        
        String setCookie = urlc.getHeaderField("Set-Cookie");
        if (setCookie != null) {
            if (setCookie.indexOf(';') > 0) {
                sessionCookie = setCookie.substring(0, setCookie.indexOf(';'));
            } else {
                sessionCookie = setCookie;
            }
        } else if (sessionCookie == null){
        	// Added the sessionCookie == null condition to prevent this exception when using
        	// the client normally with Web Start, which would send an existing cookie with the
        	// login request, resulting in the response not having a set-cookie header
            throw new IllegalStateException("Failed to log in (no set-cookie header found).");
        }
        
        // If the login worked, we will be redirected to the nextAction specified in the request
        String redirectLocation = urlc.getHeaderField("Location");
        if (redirectLocation != null && redirectLocation.contains("THE_LOGIN_WORKED")) {
        	context.setLastLoginUsername(username);
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Breaks all the rules about MVC separation, and prompts the user to log in using a JDialog.
     * @return Whether or not the user accepted to login.
     */
    private boolean showLoginPrompt() {
    	if (d == null) {
    		JFrame dummyFrame = new JFrame();
    		dummyFrame.setIconImage(MMSUtils.getFrameImageIcon().getImage());
    		d = new JDialog(dummyFrame);
    		
    		// start with the last login username from app prefs
    		username = context.getLastLoginUsername();
    	}

    	final JTextField usernameField = new JTextField(username);
    	usernameField.selectAll();
        final JPasswordField passwordField = new JPasswordField(password);

        HTMLEditorKit htmlKit = new HTMLEditorKit();
        JEditorPane needAccount = new JEditorPane();
        needAccount.setEditorKit(htmlKit);
        needAccount.setEditable(false);
        needAccount.setOpaque(false);
        Font font = (Font)UIManager.get("Label.font");
        needAccount.setText("<html><body><font face =\"" + font.getFamily() + "\" size=\"" +
				font.getSize()/4 + "\">Need an account?</font>");
        
        final JEditorPane signUp = new JEditorPane();
		signUp.setEditorKit(htmlKit);
        signUp.setEditable(false);
        signUp.setOpaque(false);
        signUp.setText("<html><body><font face =\"" + font.getFamily() + "\" size=\"" +
				font.getSize()/4 + "\"><a href=\"http://www.sqlpower.ca/page/register\">Sign up!</a></font>");
        
        /* Jump to the URL (in the user's configured browser)
         * when a link is clicked.
         */
        signUp.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent evt) {
                if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    URL url = evt.getURL();
                    try {
                        BrowserUtil.launch(url.toString());
                    } catch (IOException e1) {
                        throw new RuntimeException("Unexpected error in launch", e1);
                    }
                }
            }
        });
        
        // This listener is used to change the cursor when it is over the hyperlink
        signUp.addMouseListener(new MouseAdapter(){
			public void mouseEntered(MouseEvent arg0) {
				d.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}

			public void mouseExited(MouseEvent arg0) {
				d.setCursor(Cursor.getDefaultCursor());
			}
        });
        
        JPanel signUpPanel = new JPanel();
        signUpPanel.add(needAccount);
        signUpPanel.add(signUp);
        
        JDefaultButton okButton = new JDefaultButton("OK");
        JButton quitButton = new JButton("Quit");
        
        okButton.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                username = usernameField.getText();
                password = new String(passwordField.getPassword());
                usernameField.putClientProperty("doLogin", Boolean.TRUE);
                d.dispose();
            }
            
        });
        
        quitButton = new JButton(quitAction);
        
        d.addWindowListener(new WindowAdapter(){
        	@Override
        	public void windowClosing(WindowEvent e) {
        		cancel();
        	}
        });
        
        SPSUtils.makeJDialogCancellable(d, quitAction);
        
        JLabel ppLogo = new JLabel(SPSUtils.createIcon("pp_128", "Project Planner Icon"), JLabel.CENTER);
       
        DefaultFormBuilder b = new DefaultFormBuilder(new FormLayout("pref,3dlu,5dlu:grow"));
        b.append("Username", usernameField);
        b.append("Password", passwordField);
        b.append(signUpPanel, 3);
        b.append(ButtonBarFactory.buildOKCancelBar(okButton, quitButton), 3);
        b.setDefaultDialogBorder();
        passwordField.setPreferredSize(new Dimension(200, 20));
        
        // The Panel which contains the PP Icon and the sign up area
        JPanel outerPanel = new JPanel();
        outerPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        outerPanel.add(ppLogo);
        outerPanel.add(b.getPanel());
        
        d.setTitle("SQL Power Project Planner");
        d.setModal(true);
        d.getRootPane().setDefaultButton(okButton);
        d.setContentPane(outerPanel);
        d.pack();
        d.setLocationRelativeTo(null);
        d.setVisible(true);
        
        Boolean doLogin = (Boolean) usernameField.getClientProperty("doLogin");
        return doLogin != null && doLogin == true;
    }
    
	private Action quitAction = new AbstractAction("Quit") {
	    public void actionPerformed(ActionEvent e) {
	        cancel();
	    }
	};
	
	private void cancel() {
		cancelled = true;
		d.dispose();
	}
    
    public void setDAO(ProjectDAOXML dao) {
        this.dao = dao;
        MatchMakerXMLSession session = (MatchMakerXMLSession) dao.getSession();
        this.context = (MatchMakerXMLSessionContext) session.getContext();
    }

    public InputStream getInputStream(Project project) {
        try {
        	boolean loggedIn = false;
			while (!loggedIn && !cancelled) {
				try {
					loggedIn = login();
				} catch (IOException e) {
					// Errors connecting to server
					MMSUtils.showExceptionDialog(d, "Failed to connect to server!", e).requestFocus();
					password = null;
					continue;
				}
				
				if (!loggedIn && !cancelled) {
					// Incorrect login info
					JOptionPane.showMessageDialog(null, "Couldn't update project, incorrect login info!",
							"Login failed", JOptionPane.ERROR_MESSAGE);
					password = null;
				}
        	}
            
            URL baseURL = new URL(WEBSITE_BASE_URL);
            URL url = new URL(baseURL, "load_pp_project?projectId="+project.getOid());
            HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
            urlc.setRequestMethod("GET");
            urlc.setRequestProperty("Cookie", sessionCookie);
            urlc.setDoOutput(false);
            urlc.setDoInput(true);
            urlc.connect();

            return urlc.getInputStream();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(Project project) {
        try {
        	boolean loggedIn = false;
			while (!loggedIn && !cancelled) {
				try {
					loggedIn = login();
				} catch (IOException e) {
					// Errors connecting to server
					MMSUtils.showExceptionDialog(d, "Failed to connect to server!", e).requestFocus();
					password = null;
					continue;
				}
				
				if (!loggedIn && !cancelled) {
					// Incorrect login info
					JOptionPane.showMessageDialog(null, "Couldn't delete project!",
							"Login failed", JOptionPane.ERROR_MESSAGE);
					password = null;
				}
        	}
            
            URL baseURL = new URL(WEBSITE_BASE_URL);
            URL url = new URL(baseURL, "delete_pp_project?projectId="+project.getOid());
            HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
            urlc.setRequestMethod("GET");
            urlc.setRequestProperty("Cookie", sessionCookie);
            urlc.setDoOutput(false);
            urlc.setDoInput(true);
            urlc.connect();

            // have to read in order to send request!
            Reader in = new InputStreamReader(urlc.getInputStream());
            StringBuilder responseString = new StringBuilder();
            char[] buf = new char[2000];
            while (in.read(buf) > 0) {
                responseString.append(buf);
            }
            in.close();
            urlc.disconnect();
            JSONObject response = new JSONObject(responseString.toString());
            boolean success = response.getBoolean("success");
            if (!success) {
                throw new IOException("Failed to delete: " + response.getString("message"));
            }             
        } catch (Exception e) {
            throw new RuntimeException(e);
        }        
    }

	public boolean savePermissions(Project project) {
		try {
        	boolean loggedIn = false;
			while (!loggedIn && !cancelled) {
				try {
					loggedIn = login();
				} catch (IOException e) {
					// Errors connecting to server
					MMSUtils.showExceptionDialog(d, "Failed to connect to server!", e).requestFocus();
					password = null;
					continue;
				}
				
				if (!loggedIn && !cancelled) {
					// Incorrect login info
					JOptionPane.showMessageDialog(null, "Couldn't delete project!",
							"Login failed", JOptionPane.ERROR_MESSAGE);
					password = null;
				}
        	}
			
			JSONArray viewOnlyUsers = new JSONArray();
			for (String userId : project.getViewOnlyUsers()) {
				viewOnlyUsers.put(userId);
			}
			JSONArray viewAndModifyUsers = new JSONArray();
			for (String userId : project.getViewModifyUsers()) {
				viewAndModifyUsers.put(userId);
			}

			JSONObject permissions = new JSONObject();
			permissions.put(VIEW_ONLY_USERS_KEY, viewOnlyUsers);
			permissions.put(VIEW_AND_MODIFY_USERS_KEY, viewAndModifyUsers);
			permissions.put(PUBLIC_GROUP_KEY, project.isPublic());
			
            URL baseURL = new URL(WEBSITE_BASE_URL);
            URL url = new URL(baseURL, "save_pp_project_permissions?projectId=" +
            		project.getOid() + "&permissions=" + permissions.toString());
            HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
            urlc.setRequestMethod("GET");
            urlc.setRequestProperty("Cookie", sessionCookie);
            urlc.setDoOutput(false);
            urlc.setDoInput(true);
            urlc.connect();

            // have to read in order to send request!
            Reader in = new InputStreamReader(urlc.getInputStream());
            StringBuilder responseString = new StringBuilder();
            char[] buf = new char[2000];
            while (in.read(buf) > 0) {
                responseString.append(buf);
            }
            in.close();
            urlc.disconnect();
            JSONObject response = new JSONObject(responseString.toString());
            
            // refreshes the permission objects in the project
            loadPermissions(project);
            
            boolean success = response.getBoolean("success");
            if (!success) {
                throw new IOException("Failed to save project permissions: " + response.getString("message"));
            }         
            return !response.getBoolean("failedEntry");
        } catch (Exception e) {
            throw new RuntimeException("Exception occured while trying to save project permissions", e);
        }        
	}
	
	public void loadPermissions(Project project) {
		try {
            if (project.getOid() == null){
    			return;
    		}
            
        	boolean loggedIn = false;
			while (!loggedIn && !cancelled) {
				try {
					loggedIn = login();
				} catch (IOException e) {
					// Errors connecting to server
					MMSUtils.showExceptionDialog(d, "Failed to connect to server!", e).requestFocus();
					password = null;
					continue;
				}
				
				if (!loggedIn && !cancelled) {
					// Incorrect login info
					JOptionPane.showMessageDialog(null, "Couldn't delete project!",
							"Login failed", JOptionPane.ERROR_MESSAGE);
					password = null;
				}
        	}
            
            URL baseURL = new URL(WEBSITE_BASE_URL);
            URL url = new URL(baseURL, "load_pp_project_permissions?projectId=" + project.getOid());
            HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
            urlc.setRequestMethod("GET");
            urlc.setRequestProperty("Cookie", sessionCookie);
            urlc.setDoOutput(false);
            urlc.setDoInput(true);
            urlc.connect();

            // have to read in order to send request!
            Reader in = new InputStreamReader(urlc.getInputStream());
            StringBuilder responseString = new StringBuilder();
            char[] buf = new char[2000];
            while (in.read(buf) > 0) {
                responseString.append(buf);
            }
            in.close();
            urlc.disconnect();
            JSONObject response = new JSONObject(responseString.toString());

    		logger.debug("JSONObject = " + response.toString());
    		
    		// sets the permission objects within the project
    		project.setPublic(response.getBoolean(PUBLIC_GROUP_KEY));
    		project.setIsOwner(response.getBoolean(OWNERSHIP_KEY));
    		project.setCanModify(response.getBoolean(CAN_MODIFY_KEY));
    		project.setOwner(response.getString(OWNER_KEY));

    		JSONArray vJArray = response.getJSONArray(VIEW_ONLY_USERS_KEY);
    		JSONArray vamJArray = response.getJSONArray(VIEW_AND_MODIFY_USERS_KEY);
 
    		List<String> viewOnlyUsers = new ArrayList<String>();
    		List<String> viewModifyUsers = new ArrayList<String>();

    		for (int i = 0; i < vJArray.length(); i++) {
    			viewOnlyUsers.add(vJArray.getString(i));
    		}
    		for (int i = 0; i < vamJArray.length(); i++) {
    			viewModifyUsers.add(vamJArray.getString(i));
    		}
    		
    		project.setViewOnlyUsers(viewOnlyUsers);
    		project.setViewModifyUsers(viewModifyUsers);

            boolean success = response.getBoolean("success");
            if (!success) {
                throw new IOException("Failed to load project permissions: " + response.getString("message"));
            }  
        } catch (Exception e) {
            throw new RuntimeException("Exception occured while trying to load project permissions", e);
        }        
	}
	
}
