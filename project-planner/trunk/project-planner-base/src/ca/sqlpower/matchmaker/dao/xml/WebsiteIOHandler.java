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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.Project;

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
    
    private String username = "cowmoo"; // FIXME
    private String password = "moo"; // FIXME
    
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
                login();
                
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
    
    private void login() throws IOException {
        HttpURLConnection.setFollowRedirects(false);

        StringBuilder sb = new StringBuilder();
        sb.append("username=").append(URLEncoder.encode(username, "UTF-8"));
        sb.append("&password=").append(URLEncoder.encode(password, "UTF-8"));
        sb.append("&doLogin=").append(URLEncoder.encode("true", "UTF-8"));

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
        
    }
}
