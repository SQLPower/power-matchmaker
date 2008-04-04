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

package ca.sqlpower.matchmaker.swingui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.swingui.munge.AbstractMungeComponent;
import ca.sqlpower.matchmaker.swingui.munge.MungePen;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This class will be used to add a Label to the MungePen. This class will build
 * a JPanel with some text and of a specific color.
 */
public class LabelPane extends JPanel {

	private static final Logger logger = Logger.getLogger(LabelPane.class);

	/**
	 * This is the color of the Panel
	 */
	private Color color;

	/**
	 * This is the text that will be displayed on this colored label.
	 */
	private String text;

	/**
	 * This is the location of this label in the munge pen.
	 */
	private Point p;

	/**
	 * The point to auto scroll to
	 */
	private Point autoScrollPoint;

	/**
	 * This is the point relative to the top left corner of the Label
	 */
	private Point componentReferencePoint;

	/**
	 * This is the mungepen that contains the Label.
	 */
	private MungePen mp;

	/**
	 * The timer to handle when to autoscroll because always calling it is way
	 * too fast.
	 */
	private Timer autoScrollTimer;

	/**
	 * This boolean will indicate whether or not we are in a resize state of the
	 * Label.
	 */
	private boolean isResizeState;

	/**
	 * This is the default size of the label as well as the minimum size of the
	 * label
	 */
	private static Dimension DEFAULT_SIZE = new Dimension(100, 50);

	/**
	 * This is a mouse Listener on the Label that will listen for clicks.
	 */
	private MouseListener mouseAdapter;

	/**
	 * This is another mouse listener that listens for drags
	 */
	private LabelMouseMoveListener moveListener;

	/**
	 * This is a list of JTextArea objects that exist on the Label
	 */
	private List<JTextArea> textAreaList;

	/**
	 * This is a JDialog that contains the JColorChooser, used to set color of
	 * labels and their components
	 */
	private JDialog colorChanger;

	/**
	 * This is a color chooser used to generate colors.
	 */
	private JColorChooser colorPanel;

	/**
	 * This indecates if we are in a state in which a JTextAre object is being
	 * dragged
	 */
	private boolean dragTextArea;

	/**
	 * When choosing a color, this is the Component that we are making the color
	 * change on.
	 */
	private Component currentComponent;

	/**
	 * This is a hard-coded value that is mainly used in the revalidateComp
	 * method such that dragging a JTextArea outside the borders of the Label
	 * looks relatively good.
	 */
	private int DEFAULT_INSET = 7;

	/**
	 * This will crate a new label with a certain color and a text caption.
	 * 
	 * @param color
	 *            The color of the label
	 * @param text
	 *            The text on the panel.
	 */
	public LabelPane(MungePen mp, Color color, Point p) {
		// Set the initial size on the label.
		setSize(DEFAULT_SIZE);
		textAreaList = new ArrayList<JTextArea>();
		this.mp = mp;
		this.p = p;
		this.color = color;
		this.componentReferencePoint = new Point();
		setUpColorChanger();
		setLocation(p);
		setBackground(color);
		addListeners();
		autoScrollTimer.stop();
		autoScrollTimer.setRepeats(true);
		Border etchedBdr = BorderFactory.createEtchedBorder();
		setMinimumSize(new Dimension(getSize()));
		setLayout(null);
		setBorder(etchedBdr);
	}

	/**
	 * This method sets up the color chooser that is used on every component in
	 * this label.
	 */
	private void setUpColorChanger() {
		colorChanger = new JDialog((JFrame) mp.getTopLevelAncestor()
				.getParent(), true);
		colorPanel = new JColorChooser(getBackground());

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
				Color lastColorChosen = colorPanel.getColor();
				Component comp = currentComponent;

				if (comp == null) {
					logger
							.error("Why was this null?!?! check the "
									+ "generateColorSelectSubMenu method and make sure "
									+ "the component is being set before we get here");
				} else if (comp instanceof LabelPane) {
					comp.setBackground(lastColorChosen);
					for (JTextArea a : textAreaList) {
						a.setBackground(lastColorChosen);
					}
				} else {
					comp.setForeground(lastColorChosen);
				}

				colorChanger.setVisible(false);
			}

		});
		builder.nextRow();
		builder.add(okCancelBar);
		colorChanger.getContentPane().add(builder.getPanel());
		colorChanger.pack();
		colorChanger.setLocationRelativeTo(this);
	}

	/**
	 * This method will set up all the necessary listeners on the Label.
	 */
	private void addListeners() {

		mouseAdapter = new MouseAdapter() {

			/**
			 * This will normalize the MungePen to make sure that no components
			 * are moved out of the viewport.
			 */
			@Override
			public void mouseReleased(MouseEvent arg0) {
				mp.lockAutoScroll(false);
				autoScrollTimer.stop();
				mp.normalize();
				mp.stopConnection();
				mp.revalidate();
			}

			/**
			 * This will tell us the coordinates of the point relative to the
			 * top left corner of the Label.
			 */
			@Override
			public void mousePressed(MouseEvent e) {
				determineReferencePoint(e);
				int y = (int) componentReferencePoint.getY();
				int x = (int) componentReferencePoint.getX();
				if (x > getWidth() - DEFAULT_INSET
						&& y > getHeight() - DEFAULT_INSET) {
					isResizeState = true;
				} else {
					isResizeState = false;
				}
			}

			/**
			 * This will invoke the right click menu for the Label
			 */
			@Override
			public void mouseClicked(MouseEvent arg0) {
				requestFocus();
				repaint();
				if (arg0.getButton() == MouseEvent.BUTTON3) {
					showRightClickMenu(arg0.getPoint());
				}
				autoScrollTimer.stop();
			}

		};

		addMouseListener(mouseAdapter);

		moveListener = new LabelMouseMoveListener();
		addMouseMotionListener(moveListener);

		autoScrollTimer = new Timer(AbstractMungeComponent.AUTO_SCROLL_TIME,
				new AbstractAction() {
					public void actionPerformed(ActionEvent e) {
						performAutoScroll();
					}
				});

		addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				repaint();
			}

			public void focusLost(FocusEvent e) {
				if (getParent() != null) {
					getParent().repaint();
				}
				if (autoScrollTimer.isRunning()) {
					autoScrollTimer.stop();
				}
			}
		});
		addMouseMotionListener(new MouseMotionListener() {

			public void mouseMoved(MouseEvent e) {
				determineReferencePoint(e);
				int y = (int) componentReferencePoint.getY();
				int x = (int) componentReferencePoint.getX();
				if (x > getWidth() - DEFAULT_INSET
						&& y > getHeight() - DEFAULT_INSET) {
					setCursor(new Cursor(Cursor.HAND_CURSOR));
					setToolTipOnLabel("To resize: Drag this corner");
				} else {
					setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					setToolTipOnLabel(null);
				}
			}

			public void mouseDragged(MouseEvent arg0) {
			}

		});

		mp.addContainerListener(new ContainerAdapter() {
			public void componentAdded(ContainerEvent arg0) {
				moveLabelToBack();
			}

		});

	}

	/**
	 * Set a tooltip for the label
	 * 
	 * @param string
	 */
	private void setToolTipOnLabel(String string) {
		setToolTipText(string);
	}

	/**
	 * Move label to the back of the Mungepen in order to avoid overlapping with
	 * other components.
	 */
	protected void moveLabelToBack() {
		mp.moveToBack(this);
	}

	/**
	 * This will set the ComponentReferencePoint value which will indicate the
	 * x,y coordinates of the moues relative to the top left corner of the label
	 * 
	 * @param e
	 */
	private void determineReferencePoint(MouseEvent e) {
		componentReferencePoint = new Point((int) (e.getX() - getX()), (int) (e
				.getY() - getY()));
		componentReferencePoint.translate(getX(), getY());
	}

	/**
	 * This will perform an autoscroll on a mouse dragged event.
	 */
	protected void performAutoScroll() {
		mp.autoscroll(autoScrollPoint, this);
	}

	/**
	 * This will show a right-Click menu on the label. This currently only
	 * contains a Delete Label action.
	 * 
	 * @param p
	 */
	private void showRightClickMenu(Point p) {
		JPopupMenu popup = new JPopupMenu();
		JMenuItem delete = new JMenuItem("Delete Label");
		popup.add(delete);
		delete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeFromMungePen();
			}
		});
		final Point point = p;

		popup.add(generateColorSelectSubMenu(this, true));

		JMenuItem addText = new JMenuItem("Add Text");
		popup.add(addText);
		addText.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				addText(point);
			}
		});

		popup.show(this, (int) p.getX(), (int) p.getY());

	}

	/**
	 * This will generate a JMenu that can be used for selecting a color from a
	 * defined list, or choosing a custom color using the JColorChooser
	 * 
	 * @param comp
	 * @param isBackgroundColor
	 * @return
	 */
	private JMenu generateColorSelectSubMenu(final Component comp,
			final boolean isBackgroundColor) {
		JMenu menu = new JMenu("Change color");

		for (final Color c : ColorScheme.BREWER_SET19) {
			JMenuItem i = new JMenuItem();
			menu.add(i);
			i.setBackground(c);
			i.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					changeColor(comp, c, isBackgroundColor);
					if (comp instanceof LabelPane) {
						for (JTextArea a : textAreaList) {
							a.setBackground(c);
						}
					}
				}

			});
		}
		JMenuItem changeColor = new JMenuItem("Custom Color");
		changeColor.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				currentComponent = comp;
				logger.debug("currentComponent got set to " + comp);
				colorChanger.setVisible(true);
			}
		});
		menu.add(changeColor);

		return menu;
	}

	/**
	 * This method will add a new JTextArea component to the Label at a specific
	 * point p.
	 * 
	 * @param p
	 */
	protected void addText(Point p) {
		final JTextArea area = new JTextArea("<Enter_text_here>");
		textAreaList.add(area);
		area.setBackground(null);
		area.setLocation(p);
		add(area);
		area.setBackground(getBackground());
		area.setSize(area.getPreferredSize());
		area.setToolTipText("To move: Hold ALT and drag");
		area.repaint();
		revalidateComp(area, false);
		area.getDocument().addDocumentListener(new DocumentListener() {

			public void removeUpdate(DocumentEvent e) {
				resizeThis();
			}

			public void insertUpdate(DocumentEvent e) {
				resizeThis();
			}

			private void resizeThis() {
				area.setSize(area.getPreferredSize());
				area.repaint();
				revalidateComp(area, false);
			}

			public void changedUpdate(DocumentEvent e) {
			}

		});

		area.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if ((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) == 0) {
					dragTextArea = false;
				}
			}

			public void keyPressed(KeyEvent e) {
				if ((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0) {
					dragTextArea = true;
				}
			}

		});

		area.addMouseMotionListener(new MouseMotionAdapter() {

			public void mouseDragged(MouseEvent e) {
				if (dragTextArea) {
					dragText(e, area);
					revalidateComp(area, false);
				}
			}
		});

		area.addFocusListener(new FocusListener() {

			public void focusLost(FocusEvent e) {
				area.setBorder(null);
				area.setSize(area.getPreferredSize());
			}

			public void focusGained(FocusEvent e) {
				area.setBorder(BorderFactory.createEtchedBorder());
				area.setSize(area.getPreferredSize());
			}

		});

		area.addMouseListener(new MouseAdapter() {
			public void mouseExited(MouseEvent e) {
				if (!area.hasFocus()) {
					area.setBorder(null);
					area.setSize(area.getPreferredSize());
				}
			}

			public void mouseEntered(MouseEvent e) {
				area.setBorder(BorderFactory.createEtchedBorder());
				area.setSize(area.getPreferredSize());
			}

			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					JPopupMenu popupOnArea = new JPopupMenu();
					JMenuItem delete = new JMenuItem("Delete Text");
					popupOnArea.add(delete);
					popupOnArea.add(generateColorSelectSubMenu(area, false));

					delete.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent e) {
							textAreaList.remove(area);
							removeTextFromLabel(area);
						}
					});
					
					
					JMenuItem font = new JMenuItem("Change Font");
					popupOnArea.add(font);
					
					font.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							final FontSelector fontSelector = new FontSelector((JFrame)mp.getTopLevelAncestor().getParent(), area.getFont());
							fontSelector.getApplyButton().addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									logger.debug("We have changed the font to: "+fontSelector.getFont());
									area.setFont(fontSelector.getSelectedFont());
									area.setSize(area.getPreferredSize());
									revalidateComp(area, false);
								}
							});
							fontSelector.setVisible(true);
						}
					});
					
					popupOnArea.show(area, (int) e.getX(), (int) e.getY());
				}
			}

		});
		revalidate();
	}

	/**
	 * This method will be dragged whenever we are attempting do drag a
	 * JTextArea inside of the Label
	 * 
	 * @param e
	 * @param a
	 *            The JTextArea component we are trying to drag
	 */
	protected void dragText(MouseEvent e, JTextArea a) {
		a.setSelectionStart(0);
		a.setSelectionEnd(0);
		e.translatePoint((int) a.getLocation().getX(), (int) a.getLocation()
				.getY());
		e.translatePoint(-a.getSize().width / 2, -a.getSize().height / 2);
		a.setLocation((int) (e.getX()), (int) (e.getY()));
	}

	/**
	 * This method will remove a JTextArea component from the Label
	 * 
	 * @param area
	 */
	private void removeTextFromLabel(JTextArea area) {
		remove(area);
		repaint();
	}

	/**
	 * This method will change the color of a component in the label
	 * 
	 * @param comp
	 *            The component we are doing the color change on
	 * @param c
	 *            the new Color
	 * @param isBackgroundColor
	 *            This indicates whether the new color should be set as a
	 *            background color (for the Label) or a foreground color (for
	 *            the text)
	 */
	private void changeColor(Component comp, Color c, boolean isBackgroundColor) {
		if (c == null) {
			colorChanger.setVisible(true);
		} else {
			if (isBackgroundColor)
				comp.setBackground(c);
			else
				comp.setForeground(c);
		}
	}

	/**
	 * Remove the current component from the MungePen
	 */
	protected void removeFromMungePen() {
		mp.remove(this);
		mp.repaint();
	}

	/**
	 * This is a Mouse listener on the label which will be able to handLE
	 * dragging.
	 */
	private class LabelMouseMoveListener extends MouseMotionAdapter {
		@Override
		public void mouseDragged(MouseEvent e) {
			dragMouse(e);
			for (JTextArea area : textAreaList) {
				revalidateComp(area, true);
			}
		}
	}

	/**
	 * This method will get called whenever we get a mouseDragged event. This
	 * method will handle dragging of the label, as well as auto-scrolling the
	 * MungePen. Note: A timer is used to autoscroll because
	 * 
	 * @param e
	 */
	public void dragMouse(MouseEvent e) {
		if (!isResizeState) {
			Point mouse = e.getPoint();
			mouse.x += getX();
			mouse.y += getY();

			MungePen parent = (MungePen) getParent();
			if (mouse.x < 0 || mouse.y < 0) {
				return;
			}

			if (!parent.isConnecting()) {
				// diff is set to null for a right click to prevent dragging
				if (componentReferencePoint != null) {
					// unlocks the bounds (because the pen has no idea when the
					// mouse is moving
					mp.lockAutoScroll(false);
					e.translatePoint(getX(), getY());
					setLocation((int) (e.getX() - componentReferencePoint
							.getX()), (int) (e.getY() - componentReferencePoint
							.getY()));

					// checks if auto scrolling is a good idea at the present
					// time
					MungePen pen = mp;
					Insets autoScroll = pen.getAutoscrollInsets();
					// does not use the mouse point because this looks better
					autoScrollPoint = new Point(pen.getWidth() / 2, pen
							.getHeight() / 2);
					boolean asChanged = false;
					if (getX() + getWidth() > pen.getWidth() - autoScroll.right) {
						autoScrollPoint.x = getX() + getWidth();
						asChanged = true;
					}
					if (getY() + getHeight() > pen.getHeight()
							- autoScroll.bottom) {
						autoScrollPoint.y = getY() + getHeight();
						asChanged = true;
					}
					if (getX() < autoScroll.left) {
						autoScrollPoint.x = getX();
						asChanged = true;
					}
					if (getY() < autoScroll.top) {
						autoScrollPoint.y = getY();
						asChanged = true;
					}

					if (!asChanged) {
						autoScrollTimer.stop();
					} else {
						autoScrollTimer.restart();
					}

				}
			} else {
				parent.mouseX = e.getX() + getX();
				parent.mouseY = e.getY() + getY();
			}
			mp.repaint();
		} else {
			Point currentPoint = new Point(
					(int) (e.getPoint().getX() - getX()), (int) (e.getPoint()
							.getY() - getY()));
			currentPoint.translate(getX(), getY());
			int xDiff = (int) (currentPoint.getX() - componentReferencePoint
					.getX());
			int yDiff = (int) (currentPoint.getY() - componentReferencePoint
					.getY());
			if (componentReferencePoint.getX() > getWidth() - DEFAULT_INSET
					&& componentReferencePoint.getY() > getHeight()
							- DEFAULT_INSET) {
				setSize((int) getSize().getWidth() + xDiff, (int) getSize()
						.getHeight()
						+ yDiff);
			}
			determineReferencePoint(e);
			revalidateComp(null, false);
		}
	}

	/**
	 * This method will re-validate the size of the Label.
	 */
	public void revalidateComp(Component c, boolean resize) {
		if (getSize().getWidth() < DEFAULT_SIZE.getWidth()) {
			setSize((int) DEFAULT_SIZE.getWidth(), (int) getSize().getHeight());
		}
		if (getSize().getHeight() < DEFAULT_SIZE.getHeight()) {
			setSize((int) getSize().getWidth(), (int) DEFAULT_SIZE.getHeight());
		}
		if (c != null) {
			if ((c.getLocation().x + c.getWidth()) > getSize().width
					- DEFAULT_INSET) {
				int xDiff = (c.getLocation().x + c.getWidth())
						- getSize().width;
				setSize((int) getSize().getWidth() + xDiff + DEFAULT_INSET,
						(int) getSize().getHeight());
			}
			if ((c.getLocation().y + c.getHeight()) > getSize().height) {
				int yDiff = (c.getLocation().y + c.getHeight())
						- getSize().height;
				setSize((int) getSize().getWidth(), (int) getSize().getHeight()
						+ yDiff + DEFAULT_INSET);
			}
			if (!resize) {
				if (c.getLocation().x <=0) {
					setSize((int) getSize().getWidth()
							+ Math.abs(c.getLocation().x ),
							(int) getSize().getHeight());
					setLocation(getLocation().x + c.getLocation().x
							, getLocation().y);
					for (JTextArea area : textAreaList) {
						if (area != c) {
							area.setLocation(area.getLocation().x
									- c.getLocation().x, area
									.getLocation().y);
						}
					}
				}
				if (c.getLocation().y <= 0) {
					setSize((int) getSize().getWidth(), (int) getSize()
							.getHeight()
							+ Math.abs(c.getLocation().y ));
					setLocation(getLocation().x, getLocation().y
							+ c.getLocation().y );
					for (JTextArea area : textAreaList) {
						if (area != c) {
							area.setLocation(area.getLocation().x, area
									.getLocation().y
									- c.getLocation().y);
						}
					}
				}
			}

		}
	}

	/**
	 * Paint the small box in the corner of the Label to show the resize drag
	 * area.
	 */
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (hasFocus()) {
			g.setColor(Color.BLACK);
			g.fillRect(getSize().width - DEFAULT_INSET, getSize().height
					- DEFAULT_INSET, DEFAULT_INSET, DEFAULT_INSET);
		}
	}

}


