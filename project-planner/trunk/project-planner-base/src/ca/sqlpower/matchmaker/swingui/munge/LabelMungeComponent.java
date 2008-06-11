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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.munge.LabelMungeStep;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.swingui.ColorScheme;
import ca.sqlpower.validation.swingui.FormValidationHandler;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This class will be used to add a Label to the MungePen. This class will build
 * a JPanel with some text and of a specific color.
 */
public class LabelMungeComponent extends AbstractMungeComponent {
	
	private static final Logger logger = Logger.getLogger(LabelMungeComponent.class);

	private static final int BORDER_WIDTH = 10;

	private static final String TEXT_AREA_TOOL_TIP = "Hold ALT or middle mouse button to move this text";
	
	private static final Dimension DEFAULT_AREA_SIZE = new Dimension(100, 15);

	private JTextArea textArea;

	/**
	 * This is a JDialog that contains the JColorChooser, used to set color of
	 * labels and their components
	 */
	protected JDialog colorChanger;
	
	/**
	 * This is a color chooser used to generate colors.
	 */
	protected JColorChooser colorPanel;

	/**
	 * Indicates whether the text area is being dragged/moved
	 */
	private boolean dragTextArea = false;

	private LabelMungeStep step;
	
	public LabelMungeComponent(MungeStep ms, FormValidationHandler handler,
			MatchMakerSession session, Icon mainIcon) {
		super(ms, handler, session, null);
	}

	@Override
	protected JPanel buildUI() {
		step = (LabelMungeStep) getStep();
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
			}

			@Override
			public Dimension getMinimumSize() {
				// minimum size is max of default text area size and the actual text area size
				Dimension d = new Dimension(DEFAULT_AREA_SIZE);
				int textWidth = (int)(textArea.getPreferredSize().getWidth()) + BORDER_WIDTH;
				int textHeight = (int)(textArea.getPreferredSize().getHeight()) + BORDER_WIDTH;
				if (textWidth > d.width) {
					d.width = textWidth;
				}
				if (textHeight > d.height) {
					d.height = textHeight;
				}
				return d;
			}
		};
		
		// this allows for changing the location of the text area
		panel.setLayout(null);
		
		setUpTextArea();
		
		// modify properties from munge step
		String stepText = step.getText();
		if (stepText != null) {
			textArea.setText(stepText);
		}
		
		Color stepColor = step.getColour();
		normalBackground = stepColor;
		selectedBackground = stepColor;
		
		Color textColor = step.getTextAreaColour();
		textArea.setForeground(textColor);
		
		panel.add(textArea);
		return panel;
	}
	
	/**
	 * Sets up the text area with all the listeners and as the correct
	 * size and location.
	 */
	private void setUpTextArea() {
		textArea = new JTextArea() {
			@Override
			public void setLocation(int x, int y) {
				super.setLocation(x, y);
				step.setTextAreaLocation(new Point(x, y));
			}
		};
		
		textArea.setToolTipText(TEXT_AREA_TOOL_TIP);
		textArea.setOpaque(false);
		textArea.setLocation(step.getTextAreaLocation());
		
		StyledDocument doc = new DefaultStyledDocument();
		MutableAttributeSet standard = new SimpleAttributeSet();
		doc.setParagraphAttributes(0, 0, standard, true);
		textArea.setDocument(doc);
		
		// gets the system default font
		Font font = (Font)UIManager.get("Label.font");
		textArea.setFont(font);
		
		// when the text is being changed inside the text area
		doc.addDocumentListener(new DocumentListener() {
			public void removeUpdate(DocumentEvent e) {
				step.setText(textArea.getText());
				textArea.setSize(textArea.getPreferredSize());
			}
			public void insertUpdate(DocumentEvent e) {
				step.setText(textArea.getText());
				textArea.setSize(textArea.getPreferredSize());
				
				// resize content pane to fit text area
				if (content != null) {
					if (content.getWidth() < textArea.getWidth()) {
						content.setSize(textArea.getWidth(), content.getHeight());
						content.setPreferredSize(content.getSize());
					}
					if (content.getHeight() < textArea.getHeight()) {
						content.setSize(content.getWidth(), textArea.getHeight());
						content.setPreferredSize(content.getSize());
					}
					applyChanges();
				}
			}
				
			public void changedUpdate(DocumentEvent e) {
			}
		
		});
		
		// allows for the ALT key dragging
		textArea.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if ((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) == 0) {
					dragTextArea = false;
				}
			}

			public void keyPressed(KeyEvent e) {
				if ((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0) {
					dragTextArea = true;
				}
				
				// overwrites tab's to transfer focus
				if (e.getKeyCode() == KeyEvent.VK_TAB) {
					if ((e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) != 0) {
						textArea.transferFocusBackward();
					} else {
						textArea.transferFocus();
					}
					// don't make changes to the text
					e.consume();
				}
			}

		});
		
		// changes the border according to focus
		textArea.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				textArea.setBorder(new EtchedBorder());
			}
			
			@Override
			public void focusLost(FocusEvent e) {
				textArea.setBorder(null);
			}
		});

		// moves the text area if it is being "dragged"
		textArea.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent e) {
				if (dragTextArea) {
					dragText(e);
				}
			}
			
		});

		textArea.addMouseListener(new MouseAdapter() {
			// handles border modifications on mouse over
			@Override
			public void mouseEntered(MouseEvent e) {
				textArea.setBorder(new EtchedBorder());
			}
			@Override
			public void mouseExited(MouseEvent e) {
				if (!textArea.hasFocus()) {
					textArea.setBorder(null);
				}
			}

			// handles middle mouse button for dragging
			@Override
			public void mousePressed(MouseEvent e) {
				super.mousePressed(e);
				if (SwingUtilities.isMiddleMouseButton(e)) {
					dragTextArea = true;
				}
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				super.mouseReleased(e);
				if (SwingUtilities.isMiddleMouseButton(e)) {
					dragTextArea = false;
				}
			}

			// displays the right click menu for the text area
			@Override
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					JPopupMenu popupOnArea = new JPopupMenu();
					JMenuItem delete = new JMenuItem("Delete (del)");
					popupOnArea.add(delete);
					popupOnArea.add(generateColorSelectSubMenu(false));

					delete.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							remove();
						}
					});

					popupOnArea.show(textArea, (int) e.getX(), (int) e.getY());
				}
			}

		});		
	}

	/**
	 * Relocates the text area according to the given mouse event. Also enlarges and/or
	 * relocates the munge component if the text area was dragged outside of the munge component.
	 */
	private void dragText(MouseEvent e) {
		textArea.setSelectionStart(0);
		textArea.setSelectionEnd(0);
		e.translatePoint((int) textArea.getLocation().getX(), (int) textArea.getLocation()
				.getY());
		e.translatePoint(-textArea.getSize().width / 2, -textArea.getSize().height / 2);

		Dimension dim = new Dimension();
		Point p = new Point(e.getPoint());
		if (e.getX() < 0) {
			dim.setSize(content.getWidth() - (int)e.getX(), content.getHeight());
			if (dim.width >= textArea.getWidth() + BORDER_WIDTH) {
				content.setSize(dim);
				content.setPreferredSize(dim);
			}
			p.x = 0;
		} else if (e.getX() + textArea.getWidth()> content.getWidth()) {
			dim.setSize((int)e.getX() + textArea.getWidth(), content.getHeight());
			content.setSize(dim);
			content.setPreferredSize(dim);
		}
		
		if (e.getY() < 0) {
			dim.setSize(content.getWidth(), content.getHeight() - (int)e.getY());
			if (dim.height >= textArea.getHeight() + BORDER_WIDTH) {
				content.setSize(dim);
				content.setPreferredSize(dim);
			}
			p.y = 0;
		} else if (e.getY() + textArea.getHeight() > content.getHeight()) {
			dim.setSize(content.getWidth(), (int) e.getY()+ textArea.getHeight());
			content.setSize(dim);
			content.setPreferredSize(dim);
		}
		
		textArea.setLocation(p);
		setSize(getPreferredSize());
		
		// relocate the munge component accordingly
		if (p.x == 0 || p.y == 0) {
			if (p.x == 0) {
			setLocation(getLocation().x + e.getPoint().x, getLocation().y);
			}
			if (p.y == 0) {
				setLocation(getLocation().x, getLocation().y + e.getPoint().y);
			}
			applyChanges();
		}
		logger.debug(textArea.getLocation());
	}

	/**
	 * Adds layer modification to the right click menu for labels
	 */
	@Override
	protected JPopupMenu getPopupMenu() {
		JPopupMenu popup = super.getPopupMenu();
		popup.add(generateColorSelectSubMenu(true));
		
		JMenuItem moveToBack = new JMenuItem("Move To Back");
		popup.add(moveToBack);
		moveToBack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getPen().moveLabelToBack(LabelMungeComponent.this);
			}
		});

		JMenuItem moveToFront = new JMenuItem("Move To Front");
		popup.add(moveToFront);
		moveToFront.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getPen().moveLabelToFront(LabelMungeComponent.this);
			}
		});
		
		return popup;
	}


	/**
	 * This will generate a JMenu that can be used for selecting a color from a
	 * defined list, or choosing a custom color using the JColorChooser.
	 * 
	 * @param isBackgroundColor
	 *            Changes affect the background of munge component and text area
	 *            if true, foreground of text area otherwise.
	 * 
	 * @return The JMenu for colour choosing.
	 */
	private JMenu generateColorSelectSubMenu(final boolean isBackgroundColor) {
		JMenu menu = new JMenu("Change color");

		for (final Color c : ColorScheme.LABEL_COLOURS) {
			JMenuItem i = new JMenuItem();
			menu.add(i);
			i.setBackground(c);
			i.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					changeColor(c, isBackgroundColor);
				}

			});
		}
		JMenuItem changeColor = new JMenuItem("Custom Color");
		changeColor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (colorChanger == null) setUpColorChanger();
				colorChanger.setVisible(true);
			}
		});
		menu.add(changeColor);

		return menu;
	}

	/**
	 * Changes colour of the munge component or the text area.
	 * 
	 * @param c
	 *            Resulting colour
	 * @param isBackgroundColor
	 *            Sets the background of munge component and text area
	 *            if true, foreground of text area otherwise.
	 */
	private void changeColor(Color c, boolean isBackgroundColor) {
		if (c == null) {
			if (colorChanger == null) setUpColorChanger();
			colorChanger.setVisible(true);
		} else {
			if (isBackgroundColor) {
				((LabelMungeStep)getStep()).setColour(c);
				normalBackground = c;
				selectedBackground = c;
			} else {
				((LabelMungeStep)getStep()).setTextAreaColour(c);
				textArea.setForeground(c);
			}
			repaint();
		}
	}
	
	/**
	 * This method sets up the color chooser.
	 */
	private void setUpColorChanger() {
		colorChanger = new JDialog();
		colorChanger.setModal(true);
		colorPanel = new JColorChooser();

		FormLayout layout = new FormLayout("default", "default, default");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();

		colorChanger.setLayout(new BorderLayout());
		builder.add(colorPanel);
		JButton ok = new JButton("Ok");
		JButton cancel = new JButton("Cancel");
		JPanel okCancelBar = ButtonBarFactory.buildOKCancelBar(ok, cancel);
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				colorChanger.setVisible(false);
			}
		});
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changeColor(colorPanel.getColor(), true);
				colorChanger.setVisible(false);
			}

		});
		builder.nextRow();
		builder.add(okCancelBar);
		colorChanger.getContentPane().add(builder.getPanel());
		colorChanger.pack();
		colorChanger.setLocationRelativeTo(this);
	}
	
	@Override
	protected void remove() {
		super.remove();
		cleanUp();
	}
	
	@Override
	public void cleanUp() {
		super.cleanUp();
		if (colorChanger != null) colorChanger.dispose();
	}
}