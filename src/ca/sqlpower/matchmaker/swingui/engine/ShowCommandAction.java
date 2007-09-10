package ca.sqlpower.matchmaker.swingui.engine;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerEngine;
import ca.sqlpower.matchmaker.swingui.EditorPane;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.SPSUtils.FileExtensionFilter;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * An action that displays a dialog to the user that contains the
 * command-line command that can be used to invoke the engine outside the
 * MatchMaker GUI.
 */
class ShowCommandAction extends AbstractAction {

	private static final Logger logger = Logger.getLogger(ShowCommandAction.class);
	
	/**
	 * The frame the pop-up window belongs to.
	 */
	private final JFrame parent;

	/**
	 * The editor to save before attempting to generate the engine command-line.
	 */
	private final EditorPane editor;
	
	/**
	 * The engine instance to generate the command-line for.
	 */
	private final MatchMakerEngine engine;

	/**
	 * Creates a new instance bound to the given parent frame, editor, and engine.
	 * 
	 * @param parentFrame The frame the pop-up window will belong to.
	 * @param editor The editor to save before attempting to generate the engine command-line.
	 * @param engine The engine instance to generate the command-line for.
	 */
	public ShowCommandAction(JFrame parentFrame, EditorPane editor, MatchMakerEngine engine) {
		super("Show Command...");
		this.parent = parentFrame;
		this.editor = editor;
		this.engine = engine;
	}

	/**
	 * Tells the editor to save its changes, then asks the engine for its command line.
	 * Displays the resulting command line to the user in a pop-up dialog.
	 */
	public void actionPerformed(ActionEvent e) {
		editor.doSave();
		final String[] cmd = engine.createCommandLine(false);
		final JDialog d = new JDialog(parent,
				"MatchMaker Engine Command Line");

		FormLayout layout = new FormLayout(
				"4dlu,fill:min(pref;200dlu):grow,4dlu", // columns
				"4dlu,fill:min(pref;200dlu):grow,4dlu,pref,4dlu"); // rows
		// 1 2 3 4 5

		PanelBuilder pb;
		JPanel p = logger.isDebugEnabled() ? new FormDebugPanel(layout)
				: new JPanel(layout);
		pb = new PanelBuilder(layout, p);
		CellConstraints cc = new CellConstraints();

		final JTextArea cmdText = new JTextArea(15, 120);
		for (String arg : cmd) {
			boolean hasSpace = arg.contains(" ");
			if (hasSpace) {
				cmdText.append("\"");
			}
			cmdText.append(arg);
			if (hasSpace) {
				cmdText.append("\"");
			}
			cmdText.append(" ");
		}
		cmdText.setEditable(false);
		cmdText.setWrapStyleWord(true);
		cmdText.setLineWrap(true);
		pb.add(new JScrollPane(cmdText), cc.xy(2, 2, "f,f"));

		ButtonBarBuilder bbBuilder = new ButtonBarBuilder();

		Action saveAsAction = new AbstractAction("Save As...") {
			public void actionPerformed(ActionEvent e) {
				SPSUtils.saveDocument(d, cmdText.getDocument(),
						(FileExtensionFilter) SPSUtils.BATCH_FILE_FILTER);
			}
		};
		JButton saveAsButton = new JButton(saveAsAction);
		bbBuilder.addGridded(saveAsButton);
		bbBuilder.addRelatedGap();

		JButton copyButton = new JButton(new AbstractAction(
				"Copy to Clipboard") {
			public void actionPerformed(ActionEvent e) {
				StringSelection selection = new StringSelection(cmdText.getText());
				Clipboard clipboard = Toolkit.getDefaultToolkit()
						.getSystemClipboard();
				clipboard.setContents(selection, selection);
			}
		});
		bbBuilder.addGridded(copyButton);
		bbBuilder.addRelatedGap();
		bbBuilder.addGlue();

		JButton cancelButton = new JButton(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				d.setVisible(false);
			}
		});
		cancelButton.setText("Close");
		bbBuilder.addGridded(cancelButton);

		pb.add(bbBuilder.getPanel(), cc.xy(2, 4));
		d.add(pb.getPanel());
		SPSUtils.makeJDialogCancellable(d, null);
		d.pack();
		d.setLocationRelativeTo(parent);
		d.setVisible(true);
	}

}