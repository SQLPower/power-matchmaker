/*
 * Copyright (c) 2008, SQL Power Group Inc.
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

package ca.sqlpower.matchmaker.swingui.munge;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.munge.GoogleAddressLookup;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.validation.swingui.FormValidationHandler;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class GoogleAddressLookupMungeComponent extends AbstractMungeComponent {

    private JTextField url;
    private JTextField key;
    
    public GoogleAddressLookupMungeComponent(MungeStep ms, FormValidationHandler handler, MatchMakerSession session) {
        super(ms, handler, session);
        setOutputShowNames(true);
    }

    @Override
    protected JPanel buildUI() {
        GoogleAddressLookup temp = (GoogleAddressLookup) getStep();
        
        url = new JTextField(temp.getParameter(GoogleAddressLookup.GOOGLE_GEOCODER_URL));
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
                step.setParameter(GoogleAddressLookup.GOOGLE_GEOCODER_URL, url.getText());
            }
        });
        
        key = new JTextField(temp.getParameter(GoogleAddressLookup.GOOGLE_MAPS_API_KEY));
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
                step.setParameter(GoogleAddressLookup.GOOGLE_MAPS_API_KEY, key.getText());
            }
        });
        

        FormLayout layout = new FormLayout(
                "4dlu,pref,4dlu,fill:pref:grow,4dlu", // columns
                "4dlu,pref,4dlu,pref,4dlu"); // rows
        CellConstraints cc = new CellConstraints();

        JPanel content = new JPanel(layout);
        
        content.add(new JLabel("URL:"), cc.xy(2,2));
        content.add(url, cc.xy(4,2));
        content.add(new JLabel("Google Maps API Key:"), cc.xy(2,4));
        content.add(key, cc.xy(4,4));
        
        return content;
    }
}
