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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
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
	public static final ImageIcon FOLDER_ICON = new ImageIcon(MungeStepLibrary.class.getResource("/icons/famfamfam/folder.png")); 
	
	/**
	 * The light blue colour to be used as a background to the munge step
	 * library.
	 */
	private static final Color LIGHT_BLUE = new Color(0xe5eaf2);

	/**
	 * The tree that contains all of the munge steps
	 * in the session.
	 */
	private JTree tree; 
	
	/**
	 * The scroll pane that holds the tree of munge steps.
	 */
	private JScrollPane libraryPane;
	
	/**
	 * The munge pen this class will allow the drag of components
	 * to as well as listen for selections to provide more information.
	 */
	private final MungePen pen;
	private final TransferHandler th;
	
	public MungeStepLibrary(MungePen mungePen, Map<String, StepDescription> stepMap) {
		logger.debug("Creating Library");
	
		pen = mungePen;
		tree = new JTree();
		th = new StepDescriptionTransferHandler();
		
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
		if (categoryNames.contains("")) {
			categoryNames.add("");
			categoryNames.remove("");
		}
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
				
				String nodeText;
				Icon icon = null;
				if (node.getUserObject() instanceof StepDescription) {
					StepDescription sd = (StepDescription) node.getUserObject();
					nodeText = sd.getName();
					icon = ((StepDescription) node.getUserObject()).getIcon();
				} else {
					nodeText = node.getUserObject().toString();
					icon = FOLDER_ICON;
				}
				super.getTreeCellRendererComponent(tree, nodeText, sel, expanded, leaf, row, hasFocus);
				setBackgroundNonSelectionColor(tree.getBackground());
				setIcon(icon);

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
		tree.setBackground(LIGHT_BLUE);
		tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
		
		libraryPane = new JScrollPane(tree);
		libraryPane.getViewport().setBackground(LIGHT_BLUE);
		libraryPane.setPreferredSize(new Dimension(0, 0));
	}
	
	public JScrollPane getScrollPane() {
		return libraryPane;
	}
	
	public JTree getTree() {
		return tree;
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

