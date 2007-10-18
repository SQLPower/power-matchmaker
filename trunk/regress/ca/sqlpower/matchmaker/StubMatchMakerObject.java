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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.event.MatchMakerListener;

public class StubMatchMakerObject implements MatchMakerObject {
    private static final Logger logger = Logger.getLogger(StubMatchMakerObject.class);
    List<MatchMakerObject> children = new ArrayList<MatchMakerObject>();
    String name;
    boolean allowChildren;
    MatchMakerSession session;
    
    public StubMatchMakerObject(String name){
        this.name = name;
    }
    
    public StubMatchMakerObject(){
    }
    
    public void addChild(MatchMakerObject child){       
        children.add(child);
    }

    public void addMatchMakerListener(MatchMakerListener l) {
        // TODO Auto-generated method stub
        logger.debug("Stub call: StubMatchMakerObject.addMatchMakerListener()");

    }

    public boolean allowsChildren() {
        return allowChildren;
    }

    public int getChildCount() {
        // TODO Auto-generated method stub
        logger.debug("Stub call: StubMatchMakerObject.getChildCount()");
        return 0;
    }

    public List getChildren() {
        if(allowChildren) return children;
        else return null;
    }

    public String getName() {
        return name;
    }

    public MatchMakerObject getParent() {
        // TODO Auto-generated method stub
        logger.debug("Stub call: StubMatchMakerObject.getParent()");
        return null;
    }

    public MatchMakerSession getSession() {
        return session;
    }

    public void removeChild(MatchMakerObject child) {
        // TODO Auto-generated method stub
        logger.debug("Stub call: StubMatchMakerObject.removeChild()");

    }

    public void removeMatchMakerListener(MatchMakerListener l) {
        // TODO Auto-generated method stub
        logger
                .debug("Stub call: StubMatchMakerObject.removeMatchMakerListener()");

    }

    public void setParent(MatchMakerObject parent) {
        // TODO Auto-generated method stub
        logger.debug("Stub call: StubMatchMakerObject.setParent()");

    }

    public void setSession(MatchMakerSession matchMakerSession) {
    	this.session = matchMakerSession;
    }

    public Date getCreateDate() {
        // TODO Auto-generated method stub
        logger.debug("Stub call: StubMatchMakerObject.getCreateDate()");
        return null;
    }

    public String getLastUpdateAppUser() {
        // TODO Auto-generated method stub
        logger.debug("Stub call: StubMatchMakerObject.getLastUpdateAppUser()");
        return null;
    }

    public Date getLastUpdateDate() {
        // TODO Auto-generated method stub
        logger.debug("Stub call: StubMatchMakerObject.getLastUpdateDate()");
        return null;
    }

    public String getLastUpdateOSUser() {
        // TODO Auto-generated method stub
        logger.debug("Stub call: StubMatchMakerObject.getLastUpdateOSUser()");
        return null;
    }

    public void registerUpdate() {
        // TODO Auto-generated method stub
        logger.debug("Stub call: StubMatchMakerObject.registerUpdate()");

    }
    
    public void setAllowChildren(boolean allowChildren){
        this.allowChildren = allowChildren;
    }

	public MatchMakerObject duplicate(MatchMakerObject parent, MatchMakerSession s) {
		// TODO Auto-generated method stub
		logger.debug("Stub call: StubMatchMakerObject.duplicate()");
		return null;
	}

	public void setName(String string) {
		// TODO Auto-generated method stub
		logger.debug("Stub call: StubMatchMakerObject.setName()");
		
	}

	public boolean isVisible() {
		return true;
	}

	public void setVisible(boolean v) {
	}

}
