package ca.sqlpower.matchmaker.dao.hibernate;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.dao.MatchDAO;
import ca.sqlpower.matchmaker.util.SourceTable;

public class MatchDAOHibernate extends AbstractMatchMakerDAOHibernate<Match> implements
		MatchDAO {
    private static final Logger logger = Logger.getLogger(MatchDAOHibernate.class);
    
    private static class MatchWithSQLTableHelper extends Match {
        private String sourceTableCatalog;
        private String sourceTableSchema;
        private String sourceTableName;
        private SQLIndex index;
        private SourceTable cachedSourceTable;
        
        
        public String getSourceTableCatalog() {
            if (cachedSourceTable != null) {
                return cachedSourceTable.getTable().getCatalogName();
            } else {
                return sourceTableCatalog;
            }
        }
        
        public void setSourceTableCatalog(String sourceTableCatalog) {
            cachedSourceTable = null;
            this.sourceTableCatalog = sourceTableCatalog;
        }
        
        public String getSourceTableName() {
            if (cachedSourceTable != null) {
                return cachedSourceTable.getTable().getName();
            } else {
                return sourceTableName;
            }
        }
        
        public void setSourceTableName(String sourceTableName) {
            cachedSourceTable = null;
            this.sourceTableName = sourceTableName;
        }
        
        public String getSourceTableSchema() {
            if (cachedSourceTable != null) {
                return cachedSourceTable.getTable().getSchemaName();
            } else {
                return sourceTableSchema;
            }
        }
        
        public void setSourceTableSchema(String sourceTableSchema) {
            cachedSourceTable = null;
            this.sourceTableSchema = sourceTableSchema;
        }
        
        @Override
        public SourceTable getSourceTable() {
            if (cachedSourceTable != null) return cachedSourceTable;
            try {
                logger.debug("MatchWithSQLTableHelper.getSourceTable()");
                MatchMakerHibernateSession session = (MatchMakerHibernateSession) getSession();
                SQLDatabase db = session.getDatabase();
                SQLTable table;
                table = db.getTableByName(sourceTableCatalog, sourceTableSchema, sourceTableName);
                SourceTable sourceTable = new SourceTable();
                sourceTable.setTable(table);
                sourceTable.setUniqueIndex(index);
                cachedSourceTable = sourceTable;
                return sourceTable;
            } catch (ArchitectException e) {
                throw new RuntimeException(e);
            }
        }
        
        @Override
        public void setSourceTable(SourceTable sourceTable) {
            final SourceTable oldSourceTable = this.cachedSourceTable;
            cachedSourceTable = sourceTable;
            getEventSupport().firePropertyChange("sourceTable", oldSourceTable, sourceTable);
        }
        
        public SQLIndex getIndex() {
            return index;
        }
        public void setIndex(SQLIndex index) {
            this.index = index;
        }
    }
    
	public MatchDAOHibernate(MatchMakerHibernateSession matchMakerSession) {
		super(matchMakerSession);
	}

	public List<Match> findAllMatchesWithoutFolders() {
	        try {
	            List<Match> results = getCurrentSession()
	                    .createCriteria(getBusinessClass()).add(Expression.isNull("folder"))
	                    .list();
	            return results;
	        }
	        catch (RuntimeException re) {
	            throw re;
	        }
	    }

	public Class<Match> getBusinessClass() {
		return Match.class;
	}

	public Match findByName(String name) {
		Session session = getCurrentSession();
		Query query = session.createQuery("from Match m where m.name = :name");
		query.setParameter("name", name);
		List matches = query.list();
		if (matches.size() == 0) {
			return null;
		} else if (matches.size() == 1) {
			return (Match) matches.get(0);
		} else {
			throw new IllegalStateException("More than one match with name \""+name+"\"");
		}
	}

	public boolean isThisMatchNameAcceptable(String name) {
		Long count = countMatchByName(name);
		return (count == 0);
	}

	public long countMatchByName(String name) {
		Session session = getCurrentSession();
		Query query = session.createQuery("select count(*) from Match m where m.name = :name");
		query.setParameter("name", name, Hibernate.STRING);
		Long count = (Long)query.uniqueResult();
		return count;
	}
}
