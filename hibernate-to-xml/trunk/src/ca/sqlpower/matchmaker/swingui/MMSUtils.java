/*
 * Copyright (c) 2008, SQL Power Group Inc.
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


package ca.sqlpower.matchmaker.swingui;

import java.awt.Window;
import java.util.concurrent.Callable;

import javax.swing.JDialog;

import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.DataEntryPanelBuilder;
import ca.sqlpower.swingui.SPSUtils;

public class MMSUtils {
	
    /**
     * Pops up a dialog box that lets the user inspect and change the given db's
     * connection spec. This is very similar to the showDbcsDialog in the Architect's
     * ASUtils class because it is. Architect has additional tabs for additional data
     * source information (eg Kettle) which is not included in MatchMaker.
     * <p>
     * We considered making some sort of generic API in the library for creating a
     * connection dialog with optional extra tabs, but there's honestly not very
     * much code in this method, and it's hard to justify a whole API for something
     * this lightweight.
     * 
     * @param parentWindow
     *            The window that owns the dialog
     * @param dataSource
     *            the data source to edit (null not allowed)
     * @param onAccept
     *            this runnable will be invoked if the user OKs the dialog and
     *            validation succeeds. If you don't need to do anything in this
     *            situation, just pass in null for this parameter.
     */
    public static JDialog showDbcsDialog(
            final Window parentWindow,
            final JDBCDataSource dataSource,
            final Runnable onAccept) {
        
        final DataEntryPanel dbcsPanel = new MMDataSourcePanel(dataSource);
        
        Callable<Boolean> okCall = new Callable<Boolean>() {
            public Boolean call() {
                if (dbcsPanel.applyChanges()) {
                    if (onAccept != null) {
                    	try {
                    		dataSource.getParentCollection().write();
                    	} catch (Exception ex) {
                    		SPSUtils.showExceptionDialogNoReport(parentWindow, "Couldn't save connection information", ex);
                    	}
                        onAccept.run();
                    }
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            }
        };
    
        Callable<Boolean> cancelCall = new Callable<Boolean>() {
            public Boolean call() {
                dbcsPanel.discardChanges();
                return Boolean.TRUE;
            }
        };
    
        JDialog d = DataEntryPanelBuilder.createDataEntryPanelDialog(
                dbcsPanel, parentWindow,
                "Database Connection: " + dataSource.getDisplayName(),
                DataEntryPanelBuilder.OK_BUTTON_LABEL,
                okCall, cancelCall);
    
        d.pack();
        d.setLocationRelativeTo(parentWindow);
    
        d.setVisible(true);
        return d;
    }
    
}
