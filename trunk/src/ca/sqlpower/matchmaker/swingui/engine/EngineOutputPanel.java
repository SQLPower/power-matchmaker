package ca.sqlpower.matchmaker.swingui.engine;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
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
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.swingui.MMSUtils;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.SPSUtils.FileExtensionFilter;

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

		JTextArea outputTextArea = new JTextArea(20, 80);
		outputTextArea.setDocument(engineOutputDoc);
		outputTextArea.setEditable(false);
		outputTextArea.setWrapStyleWord(true);
		outputTextArea.setLineWrap(true);
		outputTextArea.setAutoscrolls(true);

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

		Action saveAsAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						SPSUtils.saveDocument(
								owner,
								engineOutputDoc,
								(FileExtensionFilter) SPSUtils.TEXT_FILE_FILTER);
					}
				});
			}
		};
		JButton saveAsButton = new JButton(saveAsAction);
		saveAsButton.setText("Save As...");
		bbBuilder.addGridded(saveAsButton);
		bbBuilder.addRelatedGap();

		JButton copyButton = new JButton(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {

					public void run() {
						try {
							StringSelection selection = new StringSelection(
									engineOutputDoc.getText(0,
											engineOutputDoc.getLength()));
							Clipboard clipboard = Toolkit
							.getDefaultToolkit()
							.getSystemClipboard();
							clipboard.setContents(selection, selection);
						} catch (BadLocationException e1) {
							MMSUtils.showExceptionDialog(owner,
									"Document Copy Error", e1);
						}
					}
				});
			}
		});
		copyButton.setText("Copy to Clipboard");
		bbBuilder.addGridded(copyButton);
		bbBuilder.addRelatedGap();

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
