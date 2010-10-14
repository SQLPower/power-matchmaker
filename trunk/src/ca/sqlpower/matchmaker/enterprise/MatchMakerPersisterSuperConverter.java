package ca.sqlpower.matchmaker.enterprise;

import ca.sqlpower.dao.session.SessionPersisterSuperConverter;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.SPDataSource;

public class MatchMakerPersisterSuperConverter extends SessionPersisterSuperConverter {
	
	/**
	 * This SuperConverter has no use at present. However, we forsee it being useful in the
	 * near future. If you do add implementation, feel free to remove/update this comment.
	 * @param dsCollection
	 * @param root
	 */
	public MatchMakerPersisterSuperConverter(DataSourceCollection<? extends SPDataSource> dsCollection, 
    		SPObject root) {
        super(dsCollection, root);
    }
    
	/**
	 * Since we have no specific implementation needed here, we just call the super function.
	 */
    @Override
    public Object convertToBasicType(Object convertFrom, Object... additionalInfo) {
        return super.convertToBasicType(convertFrom, additionalInfo);
    }
    
    /**
	 * Since we have no specific implementation needed here, we just call the super function.
	 */
    @Override
    public Object convertToComplexType(Object o, Class<? extends Object> type) {
        if (o == null) {
            return null;
        } else {
            return super.convertToComplexType(o, type);
        }
    }
}
