/*
 * Copyright (c) 2009, SQL Power Group Inc.
 *
 * This file is part of DQguru
 *
 * DQguru is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DQguru is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.matchmaker;

import java.util.Collections;

import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.ddl.DDLStatement;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.sqlobject.SQLTable;

/**
 * The project is supposed to take appropriate action when any of the
 * following things happen to its source table:
 * 
 * <ul>
 *  <li>The primary key changes (and the project is using the
 *      table's primary key to identify rows)
 *  <li>A unique index changes (and the project is using that
 *      unique index to identify rows)
 *  <li>Columns in the chosen unique identifier go missing
 *  <li>Columns in the chosen unique identifier change type
 * </ul>
 */
public class ProjectRefreshTest extends AbstractRefreshTest {

    /**
     * Automatic project refresh isn't implemented yet. This "test" is just a
     * starter for what will eventually become the setUp() method for this test
     * class. It doesn't actually test anything yet.
     */
    public void testPKColumnAdded() throws Exception {
        p.setResultTableSPDatasource(db.getDataSource().getName());
        p.setResultTableCatalog(null);
        p.setResultTableSchema("PUBLIC");
        p.setResultTableName("CUSTOMER_RESULT");

        assertFalse(p.doesResultTableExist());
        SQLTable resultTable = p.createResultTable();
        DDLGenerator ddlg = DDLUtils.createDDLGenerator(db.getDataSource());
        System.out.println("Using DDL generator " + ddlg + " (" + ddlg.getClass() + ")");
        for (DDLStatement ddl : ddlg.generateDDLStatements(Collections.singletonList(resultTable))) {
            sqlx(ddl.getSQLText());
        }
        
        // TODO: make this work :)
        // assertTrue(p.doesResultTableExist());
        
    }
}
