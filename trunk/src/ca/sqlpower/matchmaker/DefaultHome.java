package ca.sqlpower.matchmaker;

import java.sql.Connection;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import ca.sqlpower.matchmaker.util.HibernateUtil;

public class DefaultHome {
	private static Logger logger = Logger.getLogger(DefaultHome.class);
	private Connection con;
	private SessionFactory sessionFactory;
	
	public DefaultHome(){
		sessionFactory = HibernateUtil.getSessionFactory();		
	}
	
	public DefaultHome(Connection con) {
		this();
		this.con = con;
	}
	
	 public Connection getCon() {
		return con;
	}

	public void setCon(Connection con) {
		this.con = con;
	}
	
	public Session getCurrentSession() {
		if (con == null) {
			if(logger.isDebugEnabled()) logger.debug("Using the default session from the sessionFactory");
			return sessionFactory.getCurrentSession();
		} else {
			if(logger.isDebugEnabled()) logger.debug("Using the session from the connection "+con);
			return sessionFactory.openSession(con);
		}
	}

	

}
