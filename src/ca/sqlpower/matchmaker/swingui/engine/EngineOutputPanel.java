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

package ca.sqlpower.matchmaker.swingui.engine;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.ButtonBarBuilder;

public class EngineOutputPanel {
	Logger logger = Logger.getLogger(EngineOutputPanel.class);

	/**
	 * A bar of buttons related to the match engine output textfield.
	 */
	private JPanel buttonBar;

	/**
	 * The actual document that contains the engine's output.
	 */
	private DefaultStyledDocument engineOutputDoc;
	
	/**
	 * The component that houses the engine output field.
	 */
	private JScrollPane outputComponent;
	
	/**
	 * The actual text area that displays the engine output
	 */
	private JTextArea outputTextArea;
	
	/**
	 * The progress bar that should indicate engine run progress.
	 */
	private JProgressBar progressBar;

	/**
	 * Creates a new engine output panel GUI.
	 * 
	 * @param owner The frame that should own any dialogs created by this gui.
	 */
	public EngineOutputPanel(final JFrame owner) {
		
		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);

		GraphicsEnvironment ge = GraphicsEnvironment
		.getLocalGraphicsEnvironment();
		Font[] fonts = ge.getAllFonts();
		boolean courierNewExist = false;
		for (int i = 0; i < fonts.length; i++) {
			if (fonts[i].getFamily().equalsIgnoreCase("Courier New")) {
				courierNewExist = true;
				break;
			}
		}
		engineOutputDoc = new DefaultStyledDocument();

		outputTextArea = new JTextArea(10, 80);
		outputTextArea.setDocument(engineOutputDoc);
		outputTextArea.setEditable(false);
		outputTextArea.setWrapStyleWord(true);
		outputTextArea.setLineWrap(true);
		outputTextArea.setAutoscrolls(true);
		
		engineOutputDoc.addDocumentListener(new DocumentListener(){

			public void changedUpdate(DocumentEvent e) {
				// not used.
			}

			public void insertUpdate(DocumentEvent e) {
				outputTextArea.setCaretPosition(outputTextArea.getText().length());
			}

			public void removeUpdate(DocumentEvent e) {
				// not used.
			}
			
		});

		if (courierNewExist) {
			Font oldFont = outputTextArea.getFont();
			Font f = new Font("Courier New", oldFont.getStyle(), oldFont
					.getSize());
			outputTextArea.setFont(f);
		}

		outputComponent = new JScrollPane(outputTextArea);
		outputComponent.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		outputComponent.setAutoscrolls(true);
		outputComponent.setWheelScrollingEnabled(true);

		ButtonBarBuilder bbBuilder = new ButtonBarBuilder();
		bbBuilder.addGlue();

		Action clearAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				outputTextArea.setText("");
			}
		};
		JButton clearButton = new JButton(clearAction);
		clearButton.setText("Clear Log");
		bbBuilder.addGridded(clearButton);

		buttonBar = bbBuilder.getPanel();
		
	}

	public JProgressBar getProgressBar() {
		return progressBar;
	}
	
	public JPanel getButtonBar() {
		return buttonBar;
	}
	
	public JComponent getOutputComponent() {
		return outputComponent;
	}

	public Document getOutputDocument() {
		return engineOutputDoc;
	}
}
