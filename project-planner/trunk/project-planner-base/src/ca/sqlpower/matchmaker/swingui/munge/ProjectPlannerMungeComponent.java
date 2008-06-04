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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.munge.ProjectPlannerMungeStep;
import ca.sqlpower.validation.swingui.FormValidationHandler;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * This is the munge component for the {@link ProjectPlannerMungeStep}.
 * This will display the munge step for all of the Project Planner steps
 * in the munge pen.
 */
public class ProjectPlannerMungeComponent extends AbstractMungeComponent {
	
	private static final Logger logger = Logger.getLogger(ProjectPlannerMungeComponent.class);
	
	/**
	 * The minimum width components can be in pixels.
	 */
	private static final int MIN_COMPONENT_WIDTH = 70;
	
	/**
	 * The text pane used to display a description of this munge step
	 * to the user. The user can update this field as they wish.
	 */
	private JTextPane text;

	/**
	 * The label that holds the icon displayed on this component.
	 */
	private JLabel iconLabel;

	public ProjectPlannerMungeComponent(MungeStep step,
			FormValidationHandler handler, MatchMakerSession session, Icon mainIcon) {
		super(step, handler, session, mainIcon);
	}

	@Override
	protected JPanel buildUI() {
		JPanel panel = new JPanel() {
			@Override
			public void setSize(int width, int height) {
				int minimumWidth = (int)getMinimumSize().getWidth();
				if (width <= minimumWidth) {
					width = minimumWidth;
				}
				int minimumHeight = (int)getMinimumSize().getHeight();
				if (height <= minimumHeight) {
					height = minimumHeight;
				}
				super.setSize(width, height);
				
				//Sets the layout sizes otherwise the text field won't expand fully.
				FormLayout formLayout = ((FormLayout) getLayout());
				formLayout.setColumnSpec(1, new ColumnSpec(width + "px"));
				double preferredIconHeight = iconLabel.getPreferredSize().getHeight();
				formLayout.setRowSpec(2, new RowSpec("fill:" + (height - preferredIconHeight) + "px"));
				logger.debug("Panel width is now " + width + " and height is " + height  + " - " + preferredIconHeight);
			}

			@Override
			public Dimension getMinimumSize() {
				Dimension d = new Dimension();
				d.width = MIN_COMPONENT_WIDTH;
				d.height = (int)(iconLabel.getPreferredSize().getHeight() + text.getPreferredSize().getHeight());
				return d;
			}
		};
		FormLayout layout = new FormLayout("100px", "pref, fill:pref:grow");
		panel.setLayout(layout);
		CellConstraints cc = new CellConstraints();
		
		logger.debug("The main icon for this component is " + getMainIcon());
		
		iconLabel = new JLabel(getMainIcon());
		text = new JTextPane();
		text.setBorder(null);
		text.setBackground(null);
		text.setOpaque(false);
		
		// overwrites tab's to transfer focus
		text.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_TAB) {
					if ((e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) != 0) {
						text.transferFocusBackward();
					} else {
						text.transferFocus();
					}
					// don't make changes to the text
					e.consume();
				}
			};
		});
		
		MutableAttributeSet attrs = text.getInputAttributes();

		// gets the system default font
		Font font = (Font)UIManager.get("Label.font");
		StyleConstants.setFontFamily(attrs, font.getFamily());
        StyleConstants.setFontSize(attrs, font.getSize());
        StyledDocument doc = text.getStyledDocument();
        doc.setCharacterAttributes(0, doc.getLength() + 1, attrs, false);
		
        MutableAttributeSet standard = new SimpleAttributeSet();
		StyleConstants.setAlignment(standard, StyleConstants.ALIGN_CENTER);
		doc.setParagraphAttributes(0, 0, standard, true);
		
		text.setStyledDocument(doc);
		
		String stepText = ((ProjectPlannerMungeStep) getStep()).getText();
		if (stepText != null && stepText.trim().length() != 0) {
			text.setText(stepText);
		}
		
		doc.addDocumentListener(new DocumentListener() {
		
			public void removeUpdate(DocumentEvent e) {
				logger.debug("The document's text changed to " + text.getText());
				((ProjectPlannerMungeStep) getStep()).setText(text.getText());
			}
			public void insertUpdate(DocumentEvent e) {
				logger.debug("The document's text changed to " + text.getText());
				((ProjectPlannerMungeStep) getStep()).setText(text.getText());

				// resize content pane to fit text area
				if (content != null) {
					text.setSize(content.getSize().width, text.getPreferredSize().height);
					double height = iconLabel.getPreferredSize().getHeight() + text.getSize().getHeight() + 10;
					if (content.getHeight() < height) {
						content.setSize(content.getWidth(), (int) height);
						content.setPreferredSize(content.getSize());
					}
					applyChanges();
				}
			}
			public void changedUpdate(DocumentEvent e) {
			}
		
		});
		
		panel.add(iconLabel, cc.xy(1, 1));
		panel.add(text, cc.xy(1, 2));
		text.setSize(MIN_COMPONENT_WIDTH, text.getPreferredSize().height);

		return panel;
	}

}
