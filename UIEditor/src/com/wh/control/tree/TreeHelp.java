package com.wh.control.tree;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wh.control.EditorEnvironment;
import com.wh.control.checkboxnode.CheckBoxNode;

public class TreeHelp {
	public static final String ID_KEY = "id";
	public static final String NAME_KEY = "text";
	public static final String PID_KEY = "pid";

	public static class TreeItemInfo implements Externalizable {
		protected String getName(JSONObject data) {
			try {
				if (data.has(nameKey))
					return data.getString(nameKey);
				else {
					return "";
				}
			} catch (JSONException e) {
				e.printStackTrace();
				return "";
			}
		}

		public TreeItemInfo(String nameKey) {
			this.nameKey = nameKey;
		}

		public String nameKey = NAME_KEY;
		public JSONObject data;

		public String toString() {
			String text = getName(data);
			if (text == null || text.isEmpty()) {
				return "";
			} else
				return text;
		}

		public void rename(String newName) throws JSONException {
			data.put(nameKey, newName);
		}

		public TreeItemInfo() {
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeUTF(nameKey);
			if (data == null)
				out.writeObject("null");
			else
				out.writeUTF(data.toString());
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			nameKey = in.readUTF();
			String tmp = in.readUTF();
			if (tmp.compareTo("null") == 0)
				data = null;
			else
				data = new JSONObject(tmp);
		}
	}

	public static void selectChilds(JTree tree, CheckBoxNode selectNode, boolean selected) {
		TreeHelp.traverseTree(tree, new ITraverseTree<CheckBoxNode>() {

			@Override
			public boolean onNode(CheckBoxNode t) {
				if (selectNode == null || t.isNodeChild(selectNode))
					t.setSingleSelected(selected);
				else
					t.setSingleSelected(!selected);
				return true;
			}
		});

		tree.updateUI();
	}

	public static void expandOrCollapse(JTree tree, DefaultMutableTreeNode node, boolean expand) {
		TreePath path;
		if (node == null)
			path = tree.getSelectionPath();
		else
			path = new TreePath(node.getPath());

		if (path == null)
			path = new TreePath((DefaultMutableTreeNode) tree.getModel().getRoot());
		expandOrCollapse(tree, path, expand);
	}

	@SuppressWarnings("rawtypes")
	public static void expandOrCollapse(JTree tree, TreePath parent, boolean expand) {
		if (parent == null) {
			parent = new TreePath((DefaultMutableTreeNode) tree.getModel().getRoot());
		}
		TreeNode node = (TreeNode) parent.getLastPathComponent();
		if (node.getChildCount() >= 0) {
			for (Enumeration e = node.children(); e.hasMoreElements();) {
				TreeNode n = (TreeNode) e.nextElement();
				TreePath path = parent.pathByAddingChild(n);
				expandOrCollapse(tree, path, expand);
			}
		}
		if (expand) {
			tree.expandPath(parent);
		} else {
			tree.collapsePath(parent);
		}
	}

	public static String getValue(JSONObject json, String key) throws JSONException {
		if (json.has(key))
			return json.getString(key);
		else
			return "";
	}

	public interface ITraverseTree<T> {
		public boolean onNode(T t);
	}

	public interface IFindTree<T extends DefaultMutableTreeNode> {
		public boolean onFind(T node);
	}

	public static <T extends DefaultMutableTreeNode> DefaultMutableTreeNode findAndScroll(JTree tree, String text,
			String idKey) {
		DefaultMutableTreeNode buttonNode = TreeHelp.find(tree, text, idKey);
		DefaultMutableTreeNode result = buttonNode;
		if (buttonNode != null) {
			DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
			TreePath selectPath = new TreePath(model.getPathToRoot(buttonNode));
			tree.setSelectionPath(selectPath);
			// if (buttonNode.getLastChild() != null){
			// selectPath = new
			// TreePath(model.getPathToRoot(buttonNode.getLastChild()));
			// }

			if (!tree.getExpandsSelectedPaths())
				tree.setExpandsSelectedPaths(true);
			tree.scrollPathToVisible(selectPath);
		}

		return result;
	}

	public static <T extends DefaultMutableTreeNode> DefaultMutableTreeNode find(JTree tree, String text,
			String idKey) {
		return find(tree, new IFindTree<T>() {

			@Override
			public boolean onFind(T node) {
				if (idKey != null) {
					if (node.getUserObject() != null && node.getUserObject() instanceof TreeItemInfo) {
						TreeItemInfo treeItemInfo = (TreeItemInfo) node.getUserObject();
						JSONObject info = treeItemInfo.data;
						if (info.has(idKey)) {
							try {
								return info.getString(idKey).compareTo(text) == 0;
							} catch (JSONException e) {
								e.printStackTrace();
								return false;
							}
						}
					}
				} else {
					return node.getUserObject().toString().compareTo(text) == 0;
				}
				return false;
			}
		});
	}

	public static <T extends DefaultMutableTreeNode> DefaultMutableTreeNode find(JTree tree, IFindTree<T> onFindTree) {
		AtomicReference<T> result = new AtomicReference<T>(null);
		traverseTree(tree, new ITraverseTree<T>() {
			@Override
			public boolean onNode(T t) {
				if (onFindTree.onFind(t)) {
					result.set(t);
					return false;
				}
				return true;
			}
		});

		return result.get();
	}

	public static <T extends DefaultMutableTreeNode> boolean traverseTree(JTree tree, ITraverseTree<T> onNode) {
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		return traverseTree(model, (DefaultMutableTreeNode) model.getRoot(), onNode);
	}

	@SuppressWarnings("unchecked")
	public static <T extends DefaultMutableTreeNode> boolean traverseTree(DefaultTreeModel model,
			DefaultMutableTreeNode parent, ITraverseTree<T> onNode) {
		if (parent == null)
			return true;

		if (parent.isLeaf()) {
			return true;
		}

		for (int i = 0; i < parent.getChildCount(); i++) {
			T sub = (T) parent.getChildAt(i);

			try {
				if (!onNode.onNode(sub))
					return false;
			} catch (Exception e) {
				return false;
			}

			if (!sub.isLeaf()) {
				if (!traverseTree(model, sub, onNode))
					return false;
			} else {
			}
		}
		return true;
	}

	public static JSONArray treeToJson(JTree tree) {
		JSONArray valueObj = new JSONArray();
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		treeToJson(model, (DefaultMutableTreeNode) model.getRoot(), valueObj);
		return valueObj;
	}

	public static void treeToJson(DefaultTreeModel model, DefaultMutableTreeNode parent, JSONArray json) {
		if (parent == null)
			return;

		if (parent.isLeaf()) {
			return;
		}

		for (int i = 0; i < parent.getChildCount(); i++) {
			DefaultMutableTreeNode sub = (DefaultMutableTreeNode) parent.getChildAt(i);

			TreeItemInfo subInfo = (TreeItemInfo) sub.getUserObject();
			json.put(subInfo.data);
			if (!sub.isLeaf()) {
				treeToJson(model, sub, json);
			} else {
			}
		}
	}

	public static DefaultTreeModel newTreeModel(JTree tree, INewNode oNewNode) {
		DefaultTreeModel model = new DefaultTreeModel(oNewNode.newNode());
		tree.setModel(model);
		return model;
	}

	public static boolean checkTreeNodeName(String name, String jsonKey, DefaultMutableTreeNode parent)
			throws JSONException {
		for (int i = 0; i < parent.getChildCount(); i++) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) parent.getChildAt(i);
			TreeItemInfo json = (TreeItemInfo) node.getUserObject();
			if (json.data.getString(jsonKey).compareToIgnoreCase(name) == 0)
				return false;
		}

		return true;
	}

	public static void renameTreeNode(JTree tree) {
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		if (tree.getSelectionPath() == null || tree.getSelectionPath().getLastPathComponent() == null)
			return;

		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();

		if (node == model.getRoot()) {
			return;
		}

		String name = node.getUserObject().toString();
		if (!(node.getUserObject() instanceof TreeItemInfo) || name.compareTo("{}") == 0 || name.compareTo("[]") == 0) {
			EditorEnvironment.showMessage(null, "此节点不能重命名！", "编辑", JOptionPane.WARNING_MESSAGE);
			return;
		}

		name = EditorEnvironment.showInputDialog("请输入节点的新名字：", node.getUserObject().toString());
		if (name == null || name.isEmpty())
			return;

		TreeItemInfo info = (TreeItemInfo) node.getUserObject();
		try {
			info.rename(name);
		} catch (JSONException e) {
			e.printStackTrace();
			EditorEnvironment.showMessage(null,
					"编辑节点失败：" + e.getMessage() == null ? e.getClass().getName() : e.getMessage(), "编辑",
					JOptionPane.WARNING_MESSAGE);
		}

		if (needUpdateUI)
			tree.updateUI();
	}

	public static DefaultMutableTreeNode removeTreeNode(JTree tree, INewNode oNewNode) {
		if (tree.getSelectionPath() == null || tree.getSelectionPath().getLastPathComponent() == null)
			return null;

		if (EditorEnvironment.showConfirmDialog("是否删除选定的节点？", "删除节点",
				JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
			return null;

		DefaultMutableTreeNode selectNode = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		if (selectNode == model.getRoot()) {
			newTreeModel(tree, oNewNode);
		} else
			model.removeNodeFromParent(selectNode);

		if (needUpdateUI)
			tree.updateUI();
		return selectNode;
	}

	public static DefaultMutableTreeNode addTreeNode(JTree tree, String name, boolean needEdit, INewNode oNewNode) {
		return addTreeNode(tree, name, ID_KEY, NAME_KEY, PID_KEY, needEdit, oNewNode);
	}

	public static DefaultMutableTreeNode addTreeNode(JTree tree, String name, String idKey, String nameKey,
			String pidKey, boolean needEdit, INewNode oNewNode) {
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		DefaultMutableTreeNode selectNode;
		if (tree.getSelectionPath() == null || tree.getSelectionPath().getLastPathComponent() == null)
			selectNode = (DefaultMutableTreeNode) tree.getModel().getRoot();
		else
			selectNode = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
		try {
			DefaultMutableTreeNode node = addTreeNode(tree, name, model, selectNode, idKey, nameKey, pidKey, oNewNode);
			if (needEdit) {
				tree.startEditingAtPath(new TreePath(node.getPath()));
			}else{
				tree.setSelectionPath(new TreePath(model.getPathToRoot(node)));
			}
			return node;
		} catch (JSONException e) {
			e.printStackTrace();
			EditorEnvironment.showMessage(null,
					"添加节点失败：" + e.getMessage() == null ? e.getClass().getName() : e.getMessage(), "添加节点",
					JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}

	public static DefaultMutableTreeNode addTreeNode(JTree tree, String name, DefaultTreeModel model,
			DefaultMutableTreeNode selectNode, String idKey, String nameKey, String pidKey, INewNode oNewNode)
			throws JSONException {
		if (selectNode == null)
			return null;

		DefaultMutableTreeNode parent = selectNode;

		JSONObject data = new JSONObject();
		TreeItemInfo parentData = (TreeItemInfo) parent.getUserObject();
		data.put(idKey, name);
		data.put(nameKey, name);
		if (pidKey != null && !pidKey.isEmpty() && parentData != null && parentData.data.has(idKey))
			data.put(pidKey, parentData.data.get(idKey));
		return addTreeNode(tree, model, data, nameKey, selectNode, parent, oNewNode);
	}

	protected static DefaultMutableTreeNode addTreeNode(JTree tree, DefaultTreeModel model, JSONObject data,
			String nameKey, DefaultMutableTreeNode selectNode, DefaultMutableTreeNode parentNode, INewNode onNewNode)
			throws JSONException {
		TreeItemInfo info = new TreeItemInfo(nameKey);
		info.data = data;
		DefaultMutableTreeNode node = onNewNode.newNode();
		node.setUserObject(info);
		if (parentNode == null)
			model.setRoot(node);
		else {
			if (selectNode == null || selectNode == parentNode)
				parentNode.add(node);
			else
				parentNode.insert(node, parentNode.getIndex(selectNode));
		}

		if (needUpdateUI)
			tree.updateUI();
		return node;
	}

	protected static boolean needUpdateUI = true;

	public interface INewNode {
		public DefaultMutableTreeNode newNode();
	}

	public static void jsonToTree(JTree tree, JSONArray valueObj, INewNode onNewNode) throws JSONException {
		jsonToTree(tree, valueObj, ID_KEY, NAME_KEY, PID_KEY, onNewNode);
	}

	public static void jsonToTree(JTree tree, JSONArray valueObj, String idKey, String nameKey, String pidKey,
			INewNode onNewNode) throws JSONException {
		if (valueObj == null)
			return;

		needUpdateUI = false;
		try {
			HashMap<String, DefaultMutableTreeNode> nodes = new HashMap<>();
			TreeMap<String, List<JSONObject>> values = new TreeMap<>();
			for (int i = 0; i < valueObj.length(); i++) {
				JSONObject value = valueObj.getJSONObject(i);
				String key = null;
				if (value.has(pidKey))
					key = value.getString(pidKey);
				if (key == null || key.isEmpty()) {
					key = "";
				}

				List<JSONObject> list;
				if (!values.containsKey(key)) {
					list = new ArrayList<>();
					values.put(key, list);
				} else
					list = values.get(key);

				list.add(value);
			}

			DefaultTreeModel model = newTreeModel(tree, onNewNode);
			DefaultMutableTreeNode parentNode = null;
			List<String> keys = new ArrayList<>(values.keySet());
			List<String> nokeys = new ArrayList<>();
			while (keys.size() > 0) {
				String pid = keys.remove(0);
				if (pid.isEmpty()) {
					parentNode = (DefaultMutableTreeNode) model.getRoot();
				} else {
					parentNode = nodes.get(pid);
					if (parentNode == null) {
						nokeys.add(pid);
						continue;
					} else {
						keys.addAll(nokeys);
						nokeys.clear();
					}
				}
				List<JSONObject> datas = values.get(pid);
				jsonToTree(tree, model, datas, idKey, nameKey, parentNode, onNewNode, nodes);
			}

			if (model.getRoot() != null) {
				TreeHelp.expandOrCollapse(tree, (DefaultMutableTreeNode) model.getRoot(), true);
			}

			tree.updateUI();
		} finally {
			needUpdateUI = true;
		}
	}

	protected static void jsonToTree(JTree tree, DefaultTreeModel model, List<JSONObject> datas, String idKey,
			String nameKey, DefaultMutableTreeNode parentNode, INewNode onNewNode,
			HashMap<String, DefaultMutableTreeNode> nodes) throws JSONException {

		for (int i = 0; i < datas.size(); i++) {
			JSONObject subInfo = datas.get(i);
			DefaultMutableTreeNode node = addTreeNode(tree, model, subInfo, nameKey, null, parentNode, onNewNode);
			nodes.put(subInfo.getString(idKey), node);
		}

	}

}
