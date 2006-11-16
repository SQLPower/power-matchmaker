package ca.sqlpower.matchmaker.dao.hibernate;

import org.hibernate.Session;

import ca.sqlpower.matchmaker.MatchMakerSession;

public interface MatchMakerHibernateSession extends MatchMakerSession {
    
    /**
     * Opens a new Hibernate session.  You have to close it when you're done.
     * 
     * @return A new hibernate session which is connected to the same database as
     * this session's SQLDatabase.
     */
    public Session openSession();
    
}
