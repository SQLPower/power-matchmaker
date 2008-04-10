/*
 * Copyright (c) 2007, SQL Power Group Inc.
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

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeSelectionModel;

import org.apache.log4j.Logger;

/**
 * This class will handle the list of munge steps to the right of
 * the munge pen. The munge steps will be stored in a tree structure
 * that users can expand and then drag and drop steps from the tree
 * to the munge pen to create new steps.
 */
public class MungeStepLibrary {
	
	private static final Logger logger = Logger.getLogger(MungeStepLibrary.class);
	public static final DataFlavor STEP_DESC_FLAVOR = new DataFlavor(StepDescription.class, "Step Description");

	private static final Icon PLUS_ON = new ImageIcon(ClassLoader.getSystemResource("icons/chevrons_left2.png"));
	private static final Icon PLUS_OFF = new ImageIcon(ClassLoader.getSystemResource("icons/chevrons_left1.png"));
	
	private static final Icon MINUS_ON = new ImageIcon(ClassLoader.getSystemResource("icons/chevrons_right2.png"));
	private static final Icon MINUS_OFF = new ImageIcon(ClassLoader.getSystemResource("icons/chevrons_right1.png"));
	
	private JTree tree; 
	private final MungePen pen;
	private final TransferHandler th;
	private JButton hideShow;
	private boolean hidden;
	
	public MungeStepLibrary(MungePen mungePen, Map<String, StepDescription> stepMap) {
		logger.debug("Creating Library");
	
		pen = mungePen;
		tree = new JTree();
		th = new StepDescriptionTransferHandler();
		hidden = false;
		
		Collection<StepDescription> vals = stepMap.values();
		logger.debug("We have " + vals.size() + " step descriptions to display.");
		
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("");
		Map<String, List<StepDescription>> CategoryToStepMap = new HashMap<String, List<StepDescription>>();
		for (StepDescription sd : vals) {
			if (sd.getCategory() != null) {
				if (CategoryToStepMap.get(sd.getCategory()) == null) {
					CategoryToStepMap.put(sd.getCategory(), new ArrayList<StepDescription>());
				}
				CategoryToStepMap.get(sd.getCategory()).add(sd);
			} else {
				if (CategoryToStepMap.get("") == null) {
					CategoryToStepMap.put("", new ArrayList<StepDescription>());
				}
				CategoryToStepMap.get("").add(sd);
			}
		}
		
		List<String> categoryNames = new ArrayList<String>(CategoryToStepMap.keySet());
		Collections.sort(categoryNames);
		for (String categoryName : categoryNames) {
			DefaultMutableTreeNode node;
			if (!categoryName.equals("")) {
				node = new DefaultMutableTreeNode(categoryName);
				root.add(node);
			} else {
				node = root;
			}
			List<StepDescription> steps = CategoryToStepMap.get(categoryName);
			Collections.sort(steps);
			for (StepDescription sd : steps) {
				node.add(new DefaultMutableTreeNode(sd));				
			}
		}
		
		this.tree = new JTree(root);
		tree.setTransferHandler(th);
		pen.setTransferHandler(th);

		tree.setCellRenderer(new DefaultTreeCellRenderer(){
			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
					boolean expanded, boolean leaf, int row, boolean hasFocus) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
				
				//don't show the root node of the tree
				if (node == tree.getModel().getRoot()) {
					super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
					setVisible(false);
					setIcon(null);
					return this;
				}
				
				String nodeText;
				if (node.getUserObject() instanceof StepDescription) {
					StepDescription sd = (StepDescription) node.getUserObject();
					nodeText = sd.getName();
				} else {
					nodeText = node.getUserObject().toString();
				}
				logger.debug("Adding " + nodeText + " to the step library.");
				if (!hidden) {
					super.getTreeCellRendererComponent(tree, nodeText, sel, expanded, leaf, row, hasFocus);
					setToolTipText(null);
				} else {
					super.getTreeCellRendererComponent(tree, "", sel, expanded, leaf, row, hasFocus);
					setToolTipText(nodeText);
				}
				
				if (node.getUserObject() instanceof StepDescription) {
					setIcon(((StepDescription) node.getUserObject()).getIcon());
				}
				return this;
			}
		});
		
		tree.addMouseMotionListener(new MouseMotionAdapter(){			
			@Override
			public void mouseDragged(MouseEvent e) {
				if (tree.contains(e.getPoint())) {
					th.exportAsDrag(tree, e, th.getSourceActions(tree));
				}
			}
		});
		TreeSelectionModel selectionModel = new DefaultTreeSelectionModel();
		selectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setSelectionModel(selectionModel);
		
		hideShow = new JButton(new AbstractAction(){
			public void actionPerformed(ActionEvent e) {
				hidden = !hidden;
				
				tree.updateUI();
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
	
	public JTree getList() {
		return tree;
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
	    	if (c == tree && tree.getSelectionPath() != null) {
	    		DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
	    		if ( treeNode.getUserObject() instanceof StepDescription) {
	    			return new TransferableStepDescriptor((StepDescription)treeNode.getUserObject());
	    		}
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

