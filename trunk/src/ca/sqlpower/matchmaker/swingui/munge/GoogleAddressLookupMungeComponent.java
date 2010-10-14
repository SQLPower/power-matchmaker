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

package ca.sqlpower.matchmaker.swingui.munge;

import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.munge.GoogleAddressLookup;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.validation.swingui.FormValidationHandler;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class GoogleAddressLookupMungeComponent extends AbstractMungeComponent {

    private JTextField url;
    private JTextField key;
    private JSpinner rateLimit;
    
    private JButton showAllButton;
	private JButton hideAllButton;
	    
    public GoogleAddressLookupMungeComponent(MungeStep ms, FormValidationHandler handler, MatchMakerSession session) {
        super(ms, handler, session);
        setOutputShowNames(true);
    }

    @Override
    protected JPanel buildUI() {
        GoogleAddressLookup temp = (GoogleAddressLookup) getStep();
        
        url = new JTextField(temp.getGoogleGeocoderURL());
        url.getDocument().addDocumentListener(new DocumentListener(){
            public void insertUpdate(DocumentEvent e) {
                doStuff();
            }
            public void removeUpdate(DocumentEvent e) {
                doStuff();
            }
            public void changedUpdate(DocumentEvent e) {
                doStuff();
            }
            private void doStuff() {
                GoogleAddressLookup step = (GoogleAddressLookup) getStep();
                step.setGoogleGeocoderURL(url.getText());
            }
        });
        
        key = new JTextField(temp.getGoogleMapsApiKey());
        key.getDocument().addDocumentListener(new DocumentListener(){
            public void insertUpdate(DocumentEvent e) {
                doStuff();
            }
            public void removeUpdate(DocumentEvent e) {
                doStuff();
            }
            public void changedUpdate(DocumentEvent e) {
                doStuff();
            }
            private void doStuff() {
                GoogleAddressLookup step = (GoogleAddressLookup) getStep();
                step.setGoogleMapsApiKey(key.getText());
            }
        });

        rateLimit = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 10.0, 0.1));
        rateLimit.setValue(temp.getRateLimit());
        
        JPanel mainContent = new JPanel();
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("fill:pref:grow", "fill:pref, pref"), mainContent);
                
        FormLayout layout = new FormLayout(
        		"4dlu,pref,4dlu,fill:pref:grow,4dlu", // columns
                "4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu"); // rows
        CellConstraints cc = new CellConstraints();
        JPanel content = new JPanel(layout);
        content.add(new JLabel("URL:"), cc.xy(2,2));
        content.add(url, cc.xy(4,2));
        content.add(new JLabel("Google Maps API Key:"), cc.xy(2,4));
        content.add(key, cc.xy(4,4));
        content.add(new JLabel("Rate Limit (s):"), cc.xy(2,6));
        content.add(rateLimit, cc.xy(4,6));
        
        JPanel subPanel = new JPanel(new FlowLayout());
        showAllButton = new JButton(new HideShowAllLabelsAction("Show All", false, true, true));
        hideAllButton = new JButton(new HideShowAllLabelsAction("Hide All", false, true, false));
        subPanel.add(showAllButton);
        subPanel.add(hideAllButton);
        
        builder.append(content);
        builder.nextLine();
        builder.append(subPanel);
                
        return mainContent;
    }
    
    
}
