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

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;

import org.apache.log4j.Logger;

public class MungeStepLibrary {
	
	private static final Logger logger = Logger.getLogger(MungeStepLibrary.class);
	public static final DataFlavor STEP_DESC_FLAVOR = new DataFlavor(StepDescription.class, "Step Description");

	private static final Icon PLUS_ON = new ImageIcon(ClassLoader.getSystemResource("icons/chevrons_left2.png"));
	private static final Icon PLUS_OFF = new ImageIcon(ClassLoader.getSystemResource("icons/chevrons_left1.png"));
	
	private static final Icon MINUS_ON = new ImageIcon(ClassLoader.getSystemResource("icons/chevrons_right2.png"));
	private static final Icon MINUS_OFF = new ImageIcon(ClassLoader.getSystemResource("icons/chevrons_right1.png"));
	
	private JList list; 
	private final MungePen pen;
	private final TransferHandler th;
	private JButton hideShow;
	private boolean hidden;
	
	public MungeStepLibrary(MungePen mungePen, Map<Class, StepDescription> stepMap) {
		logger.debug("Creating Library");
	
		pen = mungePen;
		list = new JList();
		th = new StepDescriptionTransferHandler();
		hidden = false;
		
		StepDescription[] vals = stepMap.values().toArray(new StepDescription[0]);
		Arrays.sort(vals);
		
		this.list = new JList(vals);
		list.setTransferHandler(th);
		pen.setTransferHandler(th);

		list.setCellRenderer(new DefaultListCellRenderer(){
			@Override
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				StepDescription sd = (StepDescription) value;
				if (!hidden) {
					super.getListCellRendererComponent(list, sd.getName(), index, isSelected, cellHasFocus);
					setToolTipText(null);
				} else {
					super.getListCellRendererComponent(list, "", index, isSelected, cellHasFocus);
					setToolTipText(sd.getName());
				}
				setIcon(sd.getIcon());
				
				return this;
			}
		});
		
		list.addMouseMotionListener(new MouseMotionAdapter(){			
			@Override
			public void mouseDragged(MouseEvent e) {
				if (list.getCellBounds(0, list.getModel().getSize() -1).contains(e.getPoint())) {
					th.exportAsDrag(list, e, th.getSourceActions(list));
				}
			}
		});
		
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		hideShow = new JButton(new AbstractAction(){
			public void actionPerformed(ActionEvent e) {
				hidden = !hidden;
				
				list.updateUI();
				hideShow.updateUI();

				if (hidden) {
					hideShow.setIcon(PLUS_ON);
				} else {
					hideShow.setIcon(MINUS_ON);
				}
				
				hideShow.repaint();
			}
		});
		
		hideShow.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseEntered(MouseEvent e) {
				if (hidden) {
					hideShow.setIcon(PLUS_ON);
				} else {
					hideShow.setIcon(MINUS_ON);
				}
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				if (hidden) {
					hideShow.setIcon(PLUS_OFF);
				} else {
					hideShow.setIcon(MINUS_OFF);
				}
			}
		});
		
		hideShow.setIcon(MINUS_OFF);
	}
	
	public JList getList() {
		return list;
	}
	
	public JButton getHideShowButton() {
		return hideShow;
	}

	
	private class StepDescriptionTransferHandler extends TransferHandler {
		 public boolean importData(JComponent c, Transferable t) {
			 return c == pen;
		 }

	    public int getSourceActions(JComponent c) {
	        return COPY;
	    }

	    protected void exportDone(JComponent c, Transferable data, int action) {
	    }

	    protected Transferable createTransferable(JComponent c) {
	    	if (c == list) {
	    		return new TransferableStepDescriptor((StepDescription)list.getSelectedValue());
	    	}
	    	return null;
	        
	    }
	    
	    public boolean canImport(JComponent c, DataFlavor[] flavors) {
	    	return c == pen;
	    }
	    
	    @Override
	    public Icon getVisualRepresentation(Transferable t) {
	    	if (t.isDataFlavorSupported(STEP_DESC_FLAVOR)) {
	    		try {
	    			StepDescription sd = (StepDescription)t.getTransferData(STEP_DESC_FLAVOR);
	    			if (sd.getIcon() != null) {
	    				return sd.getIcon();
	    			}
	    			return null;
	    		} catch (UnsupportedFlavorException e) {
	    		} catch (IOException e) {
	    		}
	    	}
	    	return null;
	    }
	}
	
	private class TransferableStepDescriptor implements Transferable {
		
		StepDescription sd;
		
		public TransferableStepDescriptor(StepDescription sd) {
			this.sd = sd;
		}
		
		public Object getTransferData(DataFlavor flavor)
				throws UnsupportedFlavorException, IOException {
			if (isDataFlavorSupported(flavor)) {
				return sd;
			}
			
			throw new UnsupportedFlavorException(flavor);
		}

		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[] { STEP_DESC_FLAVOR };
		}

		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return flavor == STEP_DESC_FLAVOR;
		}
		
	}
}

