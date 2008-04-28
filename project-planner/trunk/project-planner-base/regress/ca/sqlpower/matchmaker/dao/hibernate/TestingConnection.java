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


package ca.sqlpower.matchmaker.dao.hibernate;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * A JDBC Connection wrapper that we can use for various testing needs.
 * 
 * <p>Current features:
 * <ul>
 *  <li>You can disable the connection.  All methods except isDisabled() and setDisabled() will
 *      throw SQLException when called if this connection is disabled.  Example:
 * <pre>
 * try {
 *     Project project = getDataAccessObject().findByName(projectName);
 *     ((TestingMatchMakerHibernateSession) getSession()).setConnectionDisabled(true);
 *     List&lt;MungeProcess&gt; cglist = project.getMungeProcesses;
 *     for (MungeProcess cg : cglist) {
 *         System.out.println("Project child: "+cg);  // this will fail if the DAO doesn't cascade the retrieval properly
 *     }
 * } finally {
 *     ((TestingMatchMakerHibernateSession) getSession()).setConnectionDisabled(false);
 * }
 * </pre>
 * </ul>
 */
public class TestingConnection implements Connection {
    
    private static final Logger logger = Logger.getLogger(TestingConnection.class);

    /**
     * The connection we delegate all JDBC operations to.
     */
    private final Connection con;
    
    /**
     * Whether or not the entire Connection interface is disabled.  Defaults
     * to false.
     */
    private boolean disabled;
    
    /**
     * Creates a new TestingConnection wrapper for the given connection.
     */
    public TestingConnection(Connection connection) {
        this.con = connection;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    
    
    ////////// Connection interface is below this line ///////////

    public void clearWarnings() throws SQLException {
        if (disabled) throw new SQLException("This connection is disabled");
        con.clearWarnings();
    }

    public void close() throws SQLException {
        if (disabled) throw new SQLException("This connection is disabled");
        con.close();
    }

    public void commit() throws SQLException {
        if (disabled) throw new SQLException("This connection is disabled");
        con.commit();
    }

    public Statement createStatement() throws SQLException {
        if (disabled) throw new SQLException("This connection is disabled");
        return con.createStatement();
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        if (disabled) throw new SQLException("This connection is disabled");
        return con.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        if (disabled) throw new SQLException("This connection is disabled");
        return con.createStatement(resultSetType, resultSetConcurrency);
    }

    public boolean getAutoCommit() throws SQLException {
        if (disabled) throw new SQLException("This connection is disabled");
        return con.getAutoCommit();
    }

    public String getCatalog() throws SQLException {
        if (disabled) throw new SQLException("This connection is disabled");
        return con.getCatalog();
    }

    public int getHoldability() throws SQLException {
        if (disabled) throw new SQLException("This connection is disabled");
        return con.getHoldability();
    }

    public DatabaseMetaData getMetaData() throws SQLException {
        if (disabled) throw new SQLException("This connection is disabled");
        return con.getMetaData();
    }

    public int getTransactionIsolation() throws SQLException {
        if (disabled) throw new SQLException("This connection is disabled");
        return con.getTransactionIsolation();
    }

    public Map<String, Class<?>> getTypeMap() throws SQLException {
        if (disabled) throw new SQLException("This connection is disabled");
        return con.getTypeMap();
    }

    public SQLWarning getWarnings() throws SQLException {
        if (disabled) throw new SQLException("This connection is disabled");
        return con.getWarnings();
    }

    public boolean isClosed() throws SQLException {
        if (disabled) throw new SQLException("This connection is disabled");
        return con.isClosed();
    }

    public boolean isReadOnly() throws SQLException {
        if (disabled) throw new SQLException("This connection is disabled");
        return con.isReadOnly();
    }

    public String nativeSQL(String sql) throws SQLException {
        if (disabled) throw new SQLException("This connection is disabled");
        return con.nativeSQL(sql);
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        if (disabled) throw new SQLException("This connection is disabled");
        return con.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        if (disabled) throw new SQLException("This connection is disabled");
        return con.prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    public CallableStatement prepareCall(String sql) throws SQLException {
        if (disabled) throw new SQLException("This connection is disabled");
        return con.prepareCall(sql);
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        if (disabled) throw new SQLException("This connection is disabled");
        return con.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        if (disabled) throw new SQLException("This connection is disabled");
        return con.prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        if (disabled) throw new SQLException("This connection is disabled");
        return con.prepareStatement(sql, autoGeneratedKeys);
    }

    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        if (disabled) throw new SQLException("This connection is disabled");
        return con.prepareStatement(sql, columnIndexes);
    }

    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        if (disabled) throw new SQLException("This connection is disabled");
        return con.prepareStatement(sql, columnNames);
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        if (disabled) throw new SQLException("This connection is disabled");
        return con.prepareStatement(sql);
    }

    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        if (disabled) throw new SQLException("This connection is disabled");
        con.releaseSavepoint(savepoint);
    }

    public void rollback() throws SQLException {
        if (disabled) throw new SQLException("This connection is disabled");
        con.rollback();
    }

    public void rollback(Savepoint savepoint) throws SQLException {
        if (disabled) throw new SQLException("This connection is disabled");
        con.rollback(savepoint);
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {
        if (disabled) throw new SQLException("This connection is disabled");
        con.setAutoCommit(autoCommit);
    }

    public void setCatalog(String catalog) throws SQLException {
        if (disabled) throw new SQLException("This connection is disabled");
        con.setCatalog(catalog);
    }

    public void setHoldability(int holdability) throws SQLException {
        if (disabled) throw new SQLException("This connection is disabled");
        con.setHoldability(holdability);
    }

    public void setReadOnly(boolean readOnly) throws SQLException {
        if (disabled) throw new SQLException("This connection is disabled");
        con.setReadOnly(readOnly);
    }

    public Savepoint setSavepoint() throws SQLException {
        if (disabled) throw new SQLException("This connection is disabled");
        return con.setSavepoint();
    }

    public Savepoint setSavepoint(String name) throws SQLException {
        if (disabled) throw new SQLException("This connection is disabled");
        return con.setSavepoint(name);
    }

    public void setTransactionIsolation(int level) throws SQLException {
        if (disabled) throw new SQLException("This connection is disabled");
        con.setTransactionIsolation(level);
    }

    public void setTypeMap(Map<String, Class<?>> arg0) throws SQLException {
        if (disabled) throw new SQLException("This connection is disabled");
        con.setTypeMap(arg0);
    }
}
