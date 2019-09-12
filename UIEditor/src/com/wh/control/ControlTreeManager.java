package com.wh.control;

import java.awt.Component;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.wh.control.tree.TreeHelp;
import com.wh.draws.DrawCanvas;
import com.wh.draws.DrawNode;
import com.wh.draws.FlowNode;
import com.wh.draws.UINode;

public class ControlTreeManager {
	protected String rootName;
	protected JTree tree;
	protected DrawCanvas canvas;

	public static class UITreeCellRender extends DefaultTreeCellRenderer {

		private static final long serialVersionUID = 7173353751862932053L;

		IGetType onGetType;

		public interface IGetType {
			String getType(DefaultMutableTreeNode node);
		}

		public UITreeCellRender() {

		}

		public UITreeCellRender(IGetType onGetType) {
			this.onGetType = onGetType;
		}

		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {
			DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
			JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			if (label.getGraphics() != null && label.getGraphics().getFontMetrics() != null) {
				int textWidth = label.getGraphics().getFontMetrics().stringWidth(label.getText());
				if (textWidth != label.getWidth())
					label.setSize(textWidth, label.getHeight());
			}

			String typeName = null;
			if (!(treeNode.getUserObject() instanceof UINode)) {
				if (onGetType != null) {
					typeName = onGetType.getType(treeNode);

					if (typeName == null || typeName.isEmpty()) {
						return label;
					}

					ImageIcon icon = new ImageIcon(UINode.getImage(typeName));
					label.setIcon(icon);
					label.updateUI();

					return label;
				} else
					return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			} else {
				try {
					UINode node = (UINode) treeNode.getUserObject();
					if (node.getDrawInfo() != null) {
						typeName = node.getDrawInfo().typeName();

					}

				} catch (Exception e) {
				}

				if (onGetType != null)
					typeName = onGetType.getType(treeNode);

				if (typeName == null || typeName.isEmpty()) {
					return label;
				}

				ImageIcon icon = new ImageIcon(UINode.getImage(typeName));
				label.setIcon(icon);
				label.updateUI();

				return label;
			}
		}
	}

	public static class WorkflowTreeCellRender extends DefaultTreeCellRenderer {

		private static final long serialVersionUID = 7173353751862932053L;
		protected DrawCanvas canvas;

		public WorkflowTreeCellRender(DrawCanvas canvas) {
			this.canvas = canvas;
		}

		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {
			DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
			if (!(treeNode.getUserObject() instanceof TreeInfo))
				return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

			JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

			// 这里可以根据value或者其他数据判断需要什么样的图标
			TreeInfo treeInfo = (TreeInfo) treeNode.getUserObject();

			FlowNode node = (FlowNode) canvas.getNode(treeInfo.id);
			if (node == null)
				return label;

			ImageIcon icon = null;
			icon = new ImageIcon(node.getImage());
			label.setIcon(icon);
			label.updateUI();

			return label;
		}
	}

	ControlSearchHelp csh;

	public ControlTreeManager(JTree tree, DrawCanvas canvas, String rootName) {
		this.rootName = rootName;
		this.tree = tree;
		csh = new ControlSearchHelp(tree);
		this.canvas = canvas;
	}

	public static class TreeInfo {
		public String title;
		public String id;

		public String toString() {
			return title;
		}

		public TreeInfo(String title, String id) {
			this.id = id;
			this.title = title;
		}
	}

	public void selectedTreeNode(TreeNode node) {
		TreeNode[] nodes = ((DefaultTreeModel) tree.getModel()).getPathToRoot(node);
		TreePath nodePath = new TreePath(nodes);
		tree.setSelectionPath(nodePath);
		tree.scrollPathToVisible(nodePath);
	}

	public boolean selectedTreeNode(DefaultMutableTreeNode parent, String id) {
		TreeNode node = null;
		if (parent.getUserObject() instanceof TreeInfo) {
			TreeInfo info = (TreeInfo) parent.getUserObject();
			if (info.id.compareTo(id) == 0)
				node = parent;
		}

		if (node == null) {
			if (!parent.isLeaf()) {
				for (int i = 0; i < parent.getChildCount(); i++) {
					DefaultMutableTreeNode child = (DefaultMutableTreeNode) parent.getChildAt(i);
					if (selectedTreeNode(child, id)) {
						return true;
					}
				}
			} else
				return false;
		} else {
			selectedTreeNode(node);
		}
		return true;
	}

	public void selectTreeNode(DrawNode node) {
		if (node == null) {
			tree.setSelectionPath(null);
			return;
		}
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
		if (root == null)
			return;

		for (int i = 0; i < root.getChildCount(); i++) {
			DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) root.getChildAt(i);
			if (treeNode.getUserObject() instanceof UINode) {
				UINode info = (UINode) treeNode.getUserObject();
				if (info.id.compareTo(node.id) == 0) {
					selectedTreeNode(treeNode);
					return;
				}
			} else if (treeNode.getUserObject() instanceof FlowNode) {
				FlowNode info = (FlowNode) treeNode.getUserObject();
				if (info.id.compareTo(node.id) == 0) {
					selectedTreeNode(treeNode);
					return;
				}

			} else if (treeNode.getUserObject() instanceof DrawNode) {
				DrawNode n = (DrawNode) treeNode.getUserObject();
				if (node.name.compareToIgnoreCase(n.name) == 0) {
					selectedTreeNode(treeNode);
					return;
				}
			}
		}
	}

	HashMap<DrawNode, DefaultMutableTreeNode> treeNodes = new HashMap<>();

	public void addToTree(DrawNode info) {
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(info);
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
		model.insertNodeInto(node, root, root.getChildCount());
		treeNodes.put(info, node);
		TreeHelp.expandOrCollapse(tree, (DefaultMutableTreeNode) node.getParent(), true);

	}

	public void removeFromTree(DrawNode info) {
		if (!treeNodes.containsKey(info))
			return;

		DefaultMutableTreeNode node = treeNodes.remove(info);
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		model.removeNodeFromParent(node);
	}

	public void refreshTree() {
		DefaultTreeModel model = new DefaultTreeModel(new DefaultMutableTreeNode(rootName));
		tree.setModel(model);

		for (DrawNode node : canvas.getNodes()) {
			addToTree(node);
		}

		if (canvas.getSelected() != null)
			selectedTreeNode((DefaultMutableTreeNode) model.getRoot(), canvas.getSelected().id);

		EditorEnvironment.expandAll(tree, null, true);

		csh.init();
	}

	public DrawNode selectCanvasNode(TreeNode treenode) {
		if (treenode == null)
			return null;

		DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) treenode;
		if (!(treeNode.getUserObject() instanceof DrawNode))
			return null;
		DrawNode node = (DrawNode) treeNode.getUserObject();
		if (node != null) {
			canvas.setSelected(node);
			canvas.setOffset(node, true);
			canvas.repaint();
			return node;
		}

		return null;
	}

}
