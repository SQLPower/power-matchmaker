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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.munge.CSVWriterMungeStep;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.validation.FileNameValidator;
import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;
import ca.sqlpower.validation.Validator;
import ca.sqlpower.validation.swingui.FormValidationHandler;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This is a component for a csv writer munge step. It has five options:
 * textfields for setting the separator, quote character, and escape
 * character, file chooser with textfield, and a checkbox for the
 * clear file property.
 */
public class CSVWriterMungeComponent extends AbstractMungeComponent {
	
	private JButton addInputButton;
	private JButton removeInputsButton;
	private JTextField separatorField = new JTextField();
	private JTextField quoteField = new JTextField();
	private JTextField escapeField = new JTextField();
	private JFileChooser fileChooser;
	private JTextField filePathField = new JTextField();
	private JButton fileButton;
	private JCheckBox clrFileCheckBox;
    
	public CSVWriterMungeComponent(MungeStep ms, FormValidationHandler handler, MatchMakerSession session) {
		super(ms, handler,session);
		
        handler.addValidateObject(filePathField, new FileNameValidator("Output"));
        handler.addValidateObject(separatorField, new CharacterValidator("separator"));
        handler.addValidateObject(quoteField, new CharacterValidator("quote"));
        handler.addValidateObject(escapeField, new CharacterValidator("escape"));
	}
	
	@Override
	protected JPanel buildUI() {
		final CSVWriterMungeStep step = (CSVWriterMungeStep) getStep();
		
		addInputButton = new JButton(new AddInputAction("Add Input"));
		removeInputsButton = new JButton(new RemoveUnusedInputAction("Clean Up"));
        separatorField = new JTextField(step.getSeparator() + "", 1);
        separatorField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				doStuff();
			}

			public void insertUpdate(DocumentEvent e) {
				doStuff();
			}

			public void removeUpdate(DocumentEvent e) {
				doStuff();
			}
			
			private void doStuff() {
				if (separatorField.getText().length() == 1) {
					step.setSeparator(separatorField.getText().charAt(0));
				}
			}
        });
        
        quoteField = new JTextField(step.getQuoteChar() + "", 1);
        quoteField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				doStuff();
			}

			public void insertUpdate(DocumentEvent e) {
				doStuff();
			}

			public void removeUpdate(DocumentEvent e) {
				doStuff();
			}
			
			private void doStuff() {
				if (quoteField.getText().length() == 1) {
					step.setQuoteChar(quoteField.getText().charAt(0));
				}
			}
        });
        
        escapeField = new JTextField(step.getEscapeChar() + "", 1);
        escapeField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				doStuff();
			}

			public void insertUpdate(DocumentEvent e) {
				doStuff();
			}

			public void removeUpdate(DocumentEvent e) {
				doStuff();
			}
			
			private void doStuff() {
				if (escapeField.getText().length() == 1) {
					step.setEscapeChar(escapeField.getText().charAt(0));
				}
			}
        });
        
        filePathField = new JTextField(step.getFilePath(), 20);
        filePathField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				doStuff();
			}

			public void insertUpdate(DocumentEvent e) {
				doStuff();
			}

			public void removeUpdate(DocumentEvent e) {
				doStuff();
			}
			
			private void doStuff() {
				step.setFilePath(filePathField.getText());
			}
        });
        
        fileChooser = new JFileChooser();
        fileButton = new JButton(new AbstractAction("...") {
			public void actionPerformed(ActionEvent e) {
				if (fileChooser.showSaveDialog(CSVWriterMungeComponent.this) == JFileChooser.APPROVE_OPTION) {
					filePathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
				}
			}
        });
        
        clrFileCheckBox = new JCheckBox();
        clrFileCheckBox.setSelected(step.getDoClearFile());
        clrFileCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				step.setDoClearFile(clrFileCheckBox.isSelected());
			}
        });
        
        FormLayout fl = new FormLayout("pref:grow,4dlu,pref:grow");
        DefaultFormBuilder b = new DefaultFormBuilder(fl);
        b.append(filePathField, fileButton);
        b.append("Separator", separatorField);
        b.append("Quote Char", quoteField);
        b.append("Escape Char", escapeField);
        b.append("Clear File", clrFileCheckBox);
		b.append(ButtonBarFactory.buildAddRemoveBar(addInputButton, removeInputsButton), 3);
		
        content = b.getPanel();
		return content;
	}
	
	/**
	 * This is a validator that checks if the given contents
	 * forms a valid character, that is if it is a string of
	 * length 1.
	 */
	private class CharacterValidator implements Validator {
		
		private final String desc;
		
		/**
		 * Creates a character validator using the given 
		 * string as the description of the input. 
		 */
		public CharacterValidator(String desc) {
			this.desc = desc;
		}
		
		public ValidateResult validate(Object contents) {
			String input = (String) contents;
			if (input == null || input.length() != 1) {
				return ValidateResult.createValidateResult(Status.FAIL,
						"Given " + desc + " is not a valid character.");
			}
			return ValidateResult.createValidateResult(Status.OK, "");
		}
	}
}
