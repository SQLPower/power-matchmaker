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
    
    public StubMatchMakerObject(String name){
        this.name = name;
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
        // TODO Auto-generated method stub
        logger.debug("Stub call: StubMatchMakerObject.getSession()");
        return null;
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
        // TODO Auto-generated method stub
        logger.debug("Stub call: StubMatchMakerObject.setSession()");

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

}
