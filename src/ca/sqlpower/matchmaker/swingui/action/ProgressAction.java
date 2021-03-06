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
package ca.sqlpower.matchmaker.swingui.action;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.swingui.ProgressWatcher;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.SPSwingWorker;
import ca.sqlpower.util.MonitorableImpl;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
/**
 * This class creates an action with a built in progress dialog.  
 * <p>
 * By default this class sets up a dialog with an indeterminate progress
 * bar.  The dialog automatically closes when the job finishes.
 */
public abstract class ProgressAction extends AbstractMatchMakerAction {
    
    public ProgressAction(
            MatchMakerSwingSession session,
            String actionName,
            String actionDescription,
            String iconResourceName) {
        super(session, actionName, actionDescription, iconResourceName);
    }

    public ProgressAction(
            MatchMakerSwingSession session,
            String actionName,
            String actionDescription) {
        super(session, actionName, actionDescription);
    }
    
    /**
     * Setup the dialog, monitors and worker.  Classes that extend this class
     * should use doStuff() and cleanUp()
     */
    public void actionPerformed(ActionEvent e) {
        final JDialog progressDialog;
        final Map<String,Object> properties = new HashMap<String, Object>();
        progressDialog = new JDialog(frame, "Progress...", false);  
        progressDialog.setLocationRelativeTo(frame);
        progressDialog.setTitle(getDialogMessage());
        
        PanelBuilder pb = new PanelBuilder(new FormLayout("4dlu,fill:min(100dlu;default):grow, pref, fill:min(100dlu;default):grow,4dlu",
                "4dlu,pref,4dlu, pref, 6dlu, pref,4dlu"));
        JLabel label = new JLabel(getDialogMessage());
        JProgressBar progressBar = new JProgressBar();
        final MonitorableImpl monitor = new MonitorableImpl();
        if (!setup(monitor,properties)){
            // if setup indicates not to continue (returns false), then exit method
            return;
        }
        CellConstraints c = new CellConstraints();
        pb.add(label, c.xyw(2, 2, 3));
        pb.add(progressBar,c.xyw(2, 4, 3));
        pb.add(new JButton(new AbstractAction(getButtonText()) {
            public void actionPerformed(ActionEvent e) {
                progressDialog.dispose();
                monitor.setCancelled(true);
            }
        }),c.xy(3,6));
        progressDialog.add(pb.getPanel());
        
        SPSwingWorker worker = new SPSwingWorker(session) {
            @Override
            public void cleanup() throws Exception {
                if (getDoStuffException() != null) {
                    SPSUtils.showExceptionDialogNoReport(session.getFrame(), "An unexpected exception occurred during the export", getDoStuffException());
                }
                ProgressAction.this.cleanUp(monitor);
                monitor.setFinished(true);
                progressDialog.dispose();
            }

            @Override
            public void doStuff() throws Exception {
                ProgressAction.this.doStuff(monitor,properties);
            }
        };
        ProgressWatcher.watchProgress(progressBar,monitor);
        progressDialog.pack();
        progressDialog.setVisible(true);
        
        new Thread(worker).start();
    }
    
    /**
     *  Return the text that shows up on the dialog button
     */
    public abstract String getButtonText();

    /**
     * Setup the monitor and doStuff parameters before the progress dialog is shown.
     * @return return true to continue with the action, return false to stop the action
     *          This could be used to respond to user choice to cancel the action
     */
    public abstract boolean setup(MonitorableImpl monitor, Map<String, Object> properties);
    /**
     * doStuff replaces the actionPerformed() call
     */
    public abstract void doStuff(MonitorableImpl monitor, Map<String, Object> properties);
    /**
     * Perform any cleanup code here 
     */
    public abstract void cleanUp(MonitorableImpl monitor);
    /**
     * Gets the message that will be displayed on the progressbar
     * 
     * @return the message displayed on the dialog above the progressbar
     */
    public abstract String getDialogMessage();
}
