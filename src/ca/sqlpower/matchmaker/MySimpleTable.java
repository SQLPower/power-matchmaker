/**
 * 
 */
package ca.sqlpower.matchmaker;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.SessionFactory;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.matchmaker.hibernate.PlFolder;
import ca.sqlpower.matchmaker.hibernate.PlMatch;
import ca.sqlpower.matchmaker.hibernate.home.PlFolderHome;
import ca.sqlpower.matchmaker.hibernate.home.PlMatchHome;
import ca.sqlpower.matchmaker.swingui.MatchMakerFrame;
import ca.sqlpower.matchmaker.util.HibernateUtil;

public class MySimpleTable {
    String catalog;
    String schema;
    String name;
    String type;
    String remarks;
    private List <MySimpleColumn> columns;
    private List <MySimpleIndex> indices;
    
    public MySimpleTable(String catalog, String schema, String name, String type, String remarks) {
        super();
        // TODO Auto-generated constructor stub
        this.catalog = catalog;
        this.schema = schema;
        this.name = name;
        this.type = type;
        this.remarks = remarks;
        columns = new ArrayList<MySimpleColumn>();
        indices = new ArrayList<MySimpleIndex>();
    }
    public String getCatalog() {
        return catalog;
    }
    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getRemarks() {
        return remarks;
    }
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
    public String getSchema() {
        return schema;
    }
    public void setSchema(String schema) {
        this.schema = schema;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    
    public String getPath() {
        StringBuffer s = new StringBuffer();
        if ( catalog != null && catalog.length() > 0 ) {
            s.append(catalog).append(".");
        }
        if ( schema != null && schema.length() > 0 ) {
            s.append(schema);
        }
        return s.toString();
    }
    @Override
    public String toString() {
        return name;
    }

    public void addColumn(MySimpleColumn col) {
        columns.add(col);
    }
    
    public void populateColumn() throws SQLException, ArchitectException {

        Connection conn = null;
        ResultSet rs = null;
        
        SQLDatabase db = MatchMakerFrame.getMainInstance().getDatabase();
        DatabaseMetaData dmd;
        try {
            conn = db.getConnection();
            dmd = conn.getMetaData();
            rs = dmd.getColumns(catalog,schema,name,null);
            columns.clear();
            while (rs.next()) {
                MySimpleColumn mySimpleColumn = new MySimpleColumn(
                        rs.getString(4),
                        rs.getInt(5),
                        rs.getString(6),
                        this);
                columns.add(mySimpleColumn);
            }
        } finally {
            if ( rs != null )
                rs.close();
            if ( conn != null )
                conn.close();
        }      
    }
    
    public void populateIndex() throws SQLException, ArchitectException {

        Connection conn = null;
        ResultSet rs = null;
        
        SQLDatabase db = MatchMakerFrame.getMainInstance().getDatabase();
        DatabaseMetaData dmd;
        try {
            conn = db.getConnection();
            dmd = conn.getMetaData();
            rs = dmd.getIndexInfo(catalog,schema,name,true,false);
            indices.clear();
            while (rs.next()) {
                if ( rs.getString(6) == null || rs.getString(6).length() == 0 )
                    continue;

                MySimpleIndex idx  = getIndexByName(rs.getString(6));
                if ( idx == null ) {
                    idx = new MySimpleIndex(
                            rs.getString(6),this);
                    indices.add(idx);
                }
            }
        } finally {
            if ( rs != null )
                rs.close();
            if ( conn != null )
                conn.close();
        }      
    }
    public List<MySimpleColumn> getColumns() {
        return columns;
    }
    public List<MySimpleIndex> getIndices() {
        return indices;
    }
    
    public MySimpleIndex getIndexByName(String name) {
        if ( indices == null || name == null )
            return null;
            
        for ( MySimpleIndex idx : indices ) {
            if ( idx.getName().equals(name) )
                return idx;
        }
        return null;
    }
}