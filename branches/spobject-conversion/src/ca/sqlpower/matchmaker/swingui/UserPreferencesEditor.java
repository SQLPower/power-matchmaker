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

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.address.AddressDatabase;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.SPSUtils;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.sleepycat.je.DatabaseException;

public class UserPreferencesEditor implements DataEntryPanel {

	private static Logger logger = Logger.getLogger(UserPreferencesEditor.class);
	
    /**
     * User preferences are not repository-specific, so they are stored
     * as properties of the session context.  This is the session context
     * we are editing.
     */
    private final SwingSessionContext context;
    
    /**
     * The panel that holds this editor's UI.
     */
    private final JPanel panel;

    private Document addressDataPathDoc = new DefaultStyledDocument();
    private JTextField addressDataPath = new JTextField();
    private JFileChooser addressDataPathChooser = new JFileChooser();
    private JLabel validatePathResult = new JLabel();
    private JTextArea validatePathExInfo = new JTextArea();
    private AddressDatabase testPath;
    
    public UserPreferencesEditor(SwingSessionContext context) {
        this.context = context;
        panel = buildUI();
    }
    
    /**
     * Subroutine of the constructor that puts together the panel, creating
     * all the GUI components and initializing them to the current values
     * in the session context.
     */
    private JPanel buildUI() {
        FormLayout layout = new FormLayout("pref,4dlu,max(300;min),4dlu,min",
				"pref,4dlu,pref,4dlu,pref,12dlu,min,4dlu,pref,2dlu,max(40;min)");
		CellConstraints cc = new CellConstraints();
		DefaultFormBuilder fb = new DefaultFormBuilder(layout);
		fb.setDefaultDialogBorder();
        
        validatePathExInfo.setOpaque(false);
        validatePathExInfo.setEditable(false);
        validatePathExInfo.setLineWrap(true);
        validatePathExInfo.setWrapStyleWord(true);
        validatePathExInfo.setFont(validatePathResult.getFont());
        
        addressDataPath.setDocument(addressDataPathDoc);
        addressDataPathDoc.addDocumentListener(new DocumentListener() {

			public void changedUpdate(DocumentEvent e) {
				
			}

			public void insertUpdate(DocumentEvent e) {
				validatePathResult.setText("");
				validatePathExInfo.setText("");
			}

			public void removeUpdate(DocumentEvent e) {
				validatePathResult.setText("");
				validatePathExInfo.setText("");
			}
        	
        });
        try {
			addressDataPathDoc.remove(0, addressDataPathDoc.getLength());
        	addressDataPathDoc.insertString(0, context.getAddressCorrectionDataPath(), null);
		} catch (BadLocationException e2) {
			SPSUtils.showExceptionDialogNoReport(getPanel(), "", e2);
		}

        JButton selectPath = new JButton(new AbstractAction("...") {

			public void actionPerformed(ActionEvent e) {
				addressDataPathChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int selected = addressDataPathChooser.showOpenDialog(getPanel());
				if (selected == JFileChooser.APPROVE_OPTION) {
					try {
						addressDataPathDoc.remove(0, addressDataPathDoc.getLength());
						addressDataPathDoc.insertString(0, addressDataPathChooser.getSelectedFile().getAbsolutePath(), null);
					} catch (BadLocationException e1) {
						SPSUtils.showExceptionDialogNoReport(getPanel(), "", e1);
					}
				}
			}
		});
        

        JButton validateAddressDataPath = new JButton(new AbstractAction("Test Path") {

			public void actionPerformed(ActionEvent e) {
				try {
					testPath = new AddressDatabase(new File(addressDataPath.getText()));
					validatePathResult.setText("Address Data Path is valid");
				} catch (DatabaseException e1) {
					validatePathResult.setText("Invalid Address Data Path");
					validatePathExInfo.setText(e1.toString());
					logger.error("Invalid Address Data Path", e1);
				} catch (Exception e2) {
					validatePathResult.setText("Invalid Address Data Path");
					validatePathExInfo.setText(e2.toString());
					logger.error("Invalid Address Data Path", e2);
				} finally {
					if (testPath != null) {
						testPath.close();
					}
				}
			}
        	
        });
       
		
        fb.addLabel("Address Correction Data Path:", cc.xy(1, 7));
        fb.add(addressDataPath, cc.xy(3, 7));
        fb.add(selectPath, cc.xy(5, 7));
        fb.add(validateAddressDataPath, cc.xy(1, 9));
        fb.add(validatePathResult, cc.xy(3, 9));
        fb.add(validatePathExInfo, cc.xy(3, 11));
        
        return fb.getPanel();
    }
    
    public boolean applyChanges() {
        context.setAddressCorrectionDataPath(addressDataPath.getText());
        return true;
    }

    public void discardChanges() {
        // nothing to do
    }

    public JComponent getPanel() {
        return panel;
    }

	public boolean hasUnsavedChanges() {
		return true;
	}
}
