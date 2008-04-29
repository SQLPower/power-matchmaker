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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.swingui.JDefaultButton;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;

/**
 * An IO Handler that provides bidirectional communication with the SQL Power web site.
 */
public class WebsiteIOHandler implements IOHandler {

    private static final Logger logger = Logger.getLogger(WebsiteIOHandler.class);
    
    /**
     * Base URL where the SQL Power website lives. The various action paths will be appended
     * to this string to form the actual request URLs.
     */
    private static final String WEBSITE_BASE_URL = "http://localhost:9999/sqlpower_website/page/";

    private String sessionCookie = null;
    
    private String username = null;
    private String password = null;
    
    public InputStream createInputStream() {
        return null; // FIXME
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
                if (!login()) {
                    JOptionPane.showMessageDialog(null, "Project not saved!", "Login failed", JOptionPane.WARNING_MESSAGE);
                    password = null;
                    return;
                }
                
                String xmlDoc = new String(toByteArray());
                StringBuilder sb = new StringBuilder();
                sb.append("projectXML=").append(URLEncoder.encode(xmlDoc, "UTF-8"));
//                sb.append("&projectId=").append(URLEncoder.encode("1234", "UTF-8")); // FIXME
                sb.append("&projectName=").append(URLEncoder.encode(project.getName(), "UTF-8"));
                sb.append("&projectDescription=").append(URLEncoder.encode(project.getDescription(), "UTF-8"));

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
                InputStream in = urlc.getInputStream();
                in.read();
                in.close();
                
                urlc.disconnect();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    private boolean login() throws IOException {
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
        } else {
            throw new IllegalStateException("Failed to log in (no set-cookie header found).");
        }
        
        // If the login worked, we will be redirected to the nextAction specified in the request
        String redirectLocation = urlc.getHeaderField("Location");
        if (redirectLocation != null && redirectLocation.contains("THE_LOGIN_WORKED")) {
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
        final JDialog d = new JDialog();
        final JTextField usernameField = new JTextField(username);
        final JPasswordField passwordField = new JPasswordField(password);
        JDefaultButton okButton = new JDefaultButton("OK");
        JButton cancelButton = new JButton("Cancel");
        
        okButton.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                username = usernameField.getText();
                password = new String(passwordField.getPassword());
                usernameField.putClientProperty("doLogin", Boolean.TRUE);
                d.dispose();
            }
            
        });
        
        cancelButton.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                d.dispose();
            }
            
        });
        
        DefaultFormBuilder b = new DefaultFormBuilder(new FormLayout("pref,3dlu,30dlu:grow"));
        b.append("Username", usernameField);
        b.append("Password", passwordField);
        b.append(ButtonBarFactory.buildOKCancelBar(okButton,cancelButton), 3);
        b.setDefaultDialogBorder();
        passwordField.setPreferredSize(new Dimension(200, 20));
        
        d.setTitle("Please log in to the SQL Power Web Site");
        d.setModal(true);
        d.getRootPane().setDefaultButton(okButton);
        d.setContentPane(b.getPanel());
        d.pack();
        d.setLocationRelativeTo(null);
        d.setVisible(true);
        
        Boolean doLogin = (Boolean) usernameField.getClientProperty("doLogin");
        return doLogin != null && doLogin == true;
    }
}
