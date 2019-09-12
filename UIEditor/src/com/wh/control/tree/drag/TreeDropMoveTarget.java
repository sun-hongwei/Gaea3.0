package com.wh.control.tree.drag;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.wh.control.tree.drag.TreeDrag.IOnDrag;

//TreeDropTarget.java
//A quick DropTarget that's looking for drops from draggable JTrees.
//

public class TreeDropMoveTarget implements DropTargetListener {

	DropTarget target;

	TreeDrag targetTree;

	public TreeDropMoveTarget(TreeDrag tree) {
		targetTree = tree;
		target = new DropTarget(targetTree, this);
	}

	/*
	 * Drop Event Handlers
	 */
	private TreeNode getNodeForEvent(DropTargetDragEvent dtde) {
		Point p = dtde.getLocation();
		DropTargetContext dtc = dtde.getDropTargetContext();
		JTree tree = (JTree) dtc.getComponent();
		TreePath path = tree.getClosestPathForLocation(p.x, p.y);
		return (TreeNode) path.getLastPathComponent();
	}

	public void dragEnter(DropTargetDragEvent dtde) {
		TreeNode node = getNodeForEvent(dtde);
		if (!node.isLeaf())
			dtde.rejectDrag();
		else
			dtde.acceptDrag(dtde.getDropAction());
	}

	public void dragOver(DropTargetDragEvent dtde) {
		dtde.acceptDrag(dtde.getDropAction());
		// TreeNode node = getNodeForEvent(dtde);
		// if (node.isLeaf()) {
		// dtde.rejectDrag();
		// } else {
		// // start by supporting move operations
		// // dtde.acceptDrag(DnDConstants.ACTION_MOVE);
		// dtde.acceptDrag(dtde.getDropAction());
		// }
	}

	public void dragExit(DropTargetEvent dte) {
	}

	public void dropActionChanged(DropTargetDragEvent dtde) {
	}

	public void drop(DropTargetDropEvent dtde) {
		Point pt = dtde.getLocation();
		DropTargetContext dtc = dtde.getDropTargetContext();
		JTree tree = (JTree) dtc.getComponent();
		TreePath parentpath = tree.getClosestPathForLocation(pt.x, pt.y);
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode) parentpath.getLastPathComponent();

		try {
			Transferable tr = dtde.getTransferable();
			DataFlavor[] flavors = tr.getTransferDataFlavors();
			for (int i = 0; i < flavors.length; i++) {
				if (tr.isDataFlavorSupported(flavors[i])) {
					Object dropObj = tr.getTransferData(flavors[i]);
					if (!flavors[i].getRepresentationClass().isAssignableFrom(TreePath.class)){
						continue;
					}
					dtde.acceptDrop(dtde.getDropAction());
					TreePath p = (TreePath) dropObj;
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) p.getLastPathComponent();
					DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
					int index = 0;
					if (parent.isLeaf()){
						index = parent.getParent().getIndex(parent);
						parent = (DefaultMutableTreeNode)parent.getParent();
					}
					
					DefaultMutableTreeNode oldParent = (DefaultMutableTreeNode) node.getParent();
					int oldIndex = node.getParent().getIndex(node);
					model.insertNodeInto(node, parent, index);
					dtde.dropComplete(true);
					for (IOnDrag onDrag : targetTree.onDrags) {
						onDrag.onDragEnd((DefaultMutableTreeNode)parent, oldParent, parent.getIndex(node), 
								oldIndex, (DefaultMutableTreeNode)node);
					}
					return;
				}
			}
			dtde.rejectDrop();
		} catch (Exception e) {
			e.printStackTrace();
			dtde.rejectDrop();
		}
	}
}
