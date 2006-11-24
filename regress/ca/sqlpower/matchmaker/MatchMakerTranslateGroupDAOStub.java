package ca.sqlpower.matchmaker;

import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.dao.MatchMakerTranslateGroupDAO;

public class MatchMakerTranslateGroupDAOStub implements
        MatchMakerTranslateGroupDAO {

    private static final Logger logger = Logger
            .getLogger(MatchMakerTranslateGroupDAOStub.class);
    
    public MatchMakerTranslateGroup findByName(String name) {
        // TODO Auto-generated method stub
        logger.debug("Stub call: MatchMakerTranslateGroupDAOStub.findByName()");
        return null;
    }

    public void delete(MatchMakerTranslateGroup deleteMe) {
        // TODO Auto-generated method stub
        logger.debug("Stub call: MatchMakerTranslateGroupDAOStub.delete()");

    }

    public List<MatchMakerTranslateGroup> findAll() {
        // TODO Auto-generated method stub
        logger.debug("Stub call: MatchMakerTranslateGroupDAOStub.findAll()");
        return null;
    }

    public Class<MatchMakerTranslateGroup> getBusinessClass() {
        // TODO Auto-generated method stub
        logger
                .debug("Stub call: MatchMakerTranslateGroupDAOStub.getBusinessClass()");
        return null;
    }

    public void save(MatchMakerTranslateGroup saveMe) {
        // TODO Auto-generated method stub
        logger.debug("Stub call: MatchMakerTranslateGroupDAOStub.save()");

    }

}
