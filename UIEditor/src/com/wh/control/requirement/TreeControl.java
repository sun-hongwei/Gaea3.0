package com.wh.control.requirement;

import java.util.HashMap;

import javax.swing.JRadioButton;
import javax.swing.JTree;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.json.JSONException;
import org.json.JSONObject;

import com.wh.control.EditorEnvironment;
import com.wh.control.tree.TreeHelp;

public class TreeControl {
	class TreeItemInfo {
		public JSONObject data;

		public TreeItemInfo(JSONObject data) {
			this.data = data;
		}

		public String toString() {
			try {
				return data.getString(RequirementDefines.field_id);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return "";
		}

	}

	public TableControl tableControl;
	public JTree tree;
	public JRadioButton dependButton;
	public JRadioButton versionButton;

	public void refresh() {
		if (tree == null)
			return;
		
		jsonToTree();
	}

	public void select(String id) {
		if (tree == null)
			return;
		
		if (!nodeMap.containsKey(id)) {
			return;
		}

		TreeNode[] nodes = ((DefaultTreeModel) tree.getModel()).getPathToRoot(nodeMap.get(id));
		TreePath nodePath = new TreePath(nodes);
		tree.setSelectionPath(nodePath);
		tree.scrollPathToVisible(nodePath);

	}

	public JSONObject getTreeNodeValue(DefaultMutableTreeNode node) {
		JSONObject value = null;
		if (node != null && node.getUserObject() != null) {
			value = ((TreeItemInfo) node.getUserObject()).data;
		}
		return value;
	}

	public void jsonToTree() throws JSONException {
		if (tree == null)
			return;
		
		DefaultTreeModel model = newTreeModel();
		if (tableControl.getSelectRow() == -1) {
			tree.setModel(model);
			return;
		}

		JSONObject selectRow = tableControl.getRow(tableControl.getSelectRow());

		JSONObject data = tableControl.dependTree;
		if (versionButton.isSelected()) {
			data = tableControl.versionTree;
		}

		String rootId = selectRow.getString(RequirementDefines.field_id);

		if (!data.has(rootId)) {
			tree.setModel(model);
			return;
		}

		JSONObject root = data.getJSONObject(rootId);
		jsonToTree(model, root, (DefaultMutableTreeNode) model.getRoot());

		if (model.getRoot() != null) {
			TreeHelp.expandOrCollapse(tree, (DefaultMutableTreeNode) model.getRoot(), true);
		}

		tree.updateUI();

	}

	protected void jsonToTree(DefaultTreeModel model, JSONObject rowMap, DefaultMutableTreeNode parentNode)
			throws JSONException {
		JSONObject row = rowMap.getJSONObject("row");
		DefaultMutableTreeNode node = addTreeNode(model, new TreeItemInfo(row), parentNode);
		JSONObject childs = rowMap.getJSONObject("child");
		if (childs != null && childs.names() != null)
			for (Object id : childs.names()) {
				JSONObject child = childs.getJSONObject((String) id);
				jsonToTree(model, child, node);
			}

	}

	HashMap<String, DefaultMutableTreeNode> nodeMap = new HashMap<>();

	public DefaultTreeModel newTreeModel() {
		if (tree == null)
			return null;
		
		DefaultTreeModel model = new DefaultTreeModel(new DefaultMutableTreeNode("root"));
		tree.setModel(model);
		nodeMap.clear();
		return model;
	}

	protected boolean checkTreeNodeName(String name, DefaultMutableTreeNode parent) {
		for (int i = 0; i < parent.getChildCount(); i++) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) parent.getChildAt(i);
			TreeItemInfo info = (TreeItemInfo) node.getUserObject();
			if (info.toString().compareToIgnoreCase(name) == 0)
				return false;
		}

		return true;
	}

	protected DefaultMutableTreeNode addTreeNode(DefaultTreeModel model, TreeItemInfo info,
			DefaultMutableTreeNode parentNode) throws JSONException {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(info);
		if (parentNode == null)
			model.setRoot(node);
		else {
			if (parentNode != null)
				parentNode.add(node);
		}

		nodeMap.put((String) info.data.get(RequirementDefines.field_id), node);
		return node;
	}

	protected void updateTree() {
		newTreeModel();
		try {
			jsonToTree();
		} catch (JSONException e) {
			e.printStackTrace();
			EditorEnvironment.showException(e);
		}
	}

	protected void saveTextEditor(JSONObject value, String key, JTextComponent editor) throws JSONException {
		if (editor.getText() != null)
			value.put(key, editor.getText());
	}

}

