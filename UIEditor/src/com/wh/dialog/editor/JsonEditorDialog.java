package com.wh.dialog.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Externalizable;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.undo.UndoManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wh.control.EditorEnvironment;
import com.wh.control.IconComboBoxItem;
import com.wh.control.IconComboBoxRender;
import com.wh.control.grid.design.PropertyTableCellEditor.KeyValue;
import com.wh.control.tree.TreeHelp;
import com.wh.control.tree.drag.TreeDrag;
import com.wh.control.tree.drag.TreeDrag.IOnDrag;
import com.wh.dialog.editor.ModelflowSelectDialog.Result;
import com.wh.draws.UINode;
import com.wh.draws.drawinfo.DrawInfoDefines;
import com.wh.form.IMainControl;
import com.wh.system.tools.ColorConvert;
import com.wh.system.tools.FileHelp;
import com.wh.system.tools.JsonHelp;
import com.wh.system.tools.TextStreamHelp;
import com.wh.system.tools.Tools;

public class JsonEditorDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	Object old = null, valueObj = null;

	String id, name, parent;

	public static class TreeItemInfo implements Externalizable {
		public Object data;

		public String toString() {
			try {
				String value;
				if (data instanceof JSONObject) {
					JSONObject json = (JSONObject) data;
					if (json.names() == null)
						value = "{}";
					else
						value = json.names().get(0).toString();
				} else {
					value = data.toString();
				}
				return value;
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return "";
		}

		public void rename(String name) throws JSONException {
			if (data instanceof JSONObject) {
				JSONObject json = (JSONObject) data;
				Object value = json.get(json.names().getString(0));
				json = new JSONObject();
				json.put(name, value);
				data = json;
			} else
				data = name;
		}

		public TreeItemInfo() {

		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			if (data == null)
				out.writeObject("null");
			else
				out.writeUTF(data.toString());
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			String tmp = in.readUTF();
			if (tmp.compareTo("null") == 0)
				data = null;
			else {
				try {
					data = new JSONObject(tmp);
				} catch (Exception e) {
					data = tmp;
				}
			}
		}

	}

	protected void newJson() throws InstantiationException, IllegalAccessException {
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		DefaultMutableTreeNode root = ((DefaultMutableTreeNode) model.getRoot());
		if (root.getChildCount() > 0) {
			DefaultMutableTreeNode first = (DefaultMutableTreeNode) root.getChildAt(0);
			if (first.getUserObject().toString().compareTo("{}") == 0)
				valueObj = new JSONObject();
			else
				valueObj = new JSONArray();
		} else
			valueObj = new JSONArray();
	}

	protected void treeToJson() throws JSONException, Exception {
		if (tree.isEditing())
			tree.getCellEditor().stopCellEditing();
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
		valueObj = new JSONArray();
		treeToJson(model, root, valueObj);
		valueObj = ((JSONArray) valueObj).get(0);
	}

	protected Object getTreeNodeValue(DefaultMutableTreeNode node) {
		Object value = null;
		if (node != null) {
			if (node.getUserObject().toString().compareTo("{}") == 0)
				value = new JSONObject();
			else if (node.getUserObject().toString().compareTo("[]") == 0)
				value = new JSONArray();
			else if (node.getUserObject() instanceof TreeItemInfo)
				value = ((TreeItemInfo) node.getUserObject()).data;
		}
		return value;
	}

	protected Object addTreeNodeToJson(Object json, DefaultMutableTreeNode node) throws Exception {
		Object value = getTreeNodeValue(node);

		DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) node.getParent();
		Object parentValue = getTreeNodeValue(parentNode);

		addTreeNodeToJson(json, parentValue, value);
		if (value.toString().compareTo("[]") == 0)
			return value;
		else
			return value;
	}

	protected void addTreeNodeToJson(Object json, Object key, Object value) throws JSONException {
		if (value instanceof TreeItemInfo)
			value = ((TreeItemInfo) value).data;

		if (key instanceof TreeItemInfo)
			key = ((TreeItemInfo) key).data;

		if (json instanceof JSONArray) {
			((JSONArray) json).put(value);
		} else if (json instanceof JSONObject)
			((JSONObject) json).put(key.toString(), value);
	}

	protected void treeToJson(DefaultTreeModel model, DefaultMutableTreeNode parent, Object json) throws Exception {
		if (parent == null || json == null)
			return;

		if (parent.isLeaf()) {
			return;
		}

		for (int x = 0; x < parent.getChildCount(); x++) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) parent.getChildAt(x);
			TreeItemInfo info = (TreeItemInfo) node.getUserObject();
			String key = info.toString();
			if (key.compareTo("{}") == 0 || key.compareTo("[]") == 0) {
				Object parentValue = addTreeNodeToJson(json, node);
				treeToJson(model, (DefaultMutableTreeNode) node, parentValue);
			} else {
				DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) node.getParent();
				TreeItemInfo parentInfo = (TreeItemInfo) parentNode.getUserObject();
				switch (parentInfo.toString()) {
				case "{}":
					treeToJson(model, node, json);
					break;
				case "[]":
				default:
					switch (key) {
					case "{}":
					case "[]":
						Object parentValue = addTreeNodeToJson(json, node);
						treeToJson(model, node, parentValue);
						break;
					default:
						addTreeNodeToJson(json, node);
						break;
					}
					break;
				}
			}
		}
	}

	protected void jsonToTree() throws JSONException {
		DefaultTreeModel model = newTreeModel();
		jsonToTree(model, valueObj, (DefaultMutableTreeNode) model.getRoot());
		tree.updateUI();

		if (model.getRoot() != null) {
			TreeHelp.expandOrCollapse(tree, (DefaultMutableTreeNode) model.getRoot(), true);
		}
	}

	protected void jsonToTree(DefaultTreeModel model, Object valueObj, DefaultMutableTreeNode treeRoot)
			throws JSONException {
		if (valueObj == null)
			return;
		if (valueObj instanceof JSONObject) {
			DefaultMutableTreeNode node = addTreeNode(model, "{}", treeRoot);
			JSONObject values = (JSONObject) valueObj;
			Iterator<String> keys = (values).keys();
			while (keys.hasNext()) {
				String key = keys.next();
				DefaultMutableTreeNode subNode = addTreeNode(model, key, node);
				jsonToTree(model, values.get(key), subNode);
			}
		} else if (valueObj instanceof JSONArray) {
			DefaultMutableTreeNode node = addTreeNode(model, "[]", treeRoot);
			JSONArray values = (JSONArray) valueObj;
			for (int i = 0; i < values.length(); i++) {
				Object value = values.get(i);
				jsonToTree(model, value, node);
			}
		} else {
			addTreeNode(model, valueObj, treeRoot);
		}
	}

	protected DefaultTreeModel newTreeModel() {
		DefaultTreeModel model = new DefaultTreeModel(new DefaultMutableTreeNode("root"));
		tree.setModel(model);
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

	protected void removeTreeNode() {
		if (tree.getSelectionPath() == null || tree.getSelectionPath().getLastPathComponent() == null)
			return;

		if (EditorEnvironment.showConfirmDialog("是否删除选定的节点？", "删除节点",
				JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
			return;

		DefaultMutableTreeNode selectNode = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		if (selectNode == model.getRoot())
			newTreeModel();
		else
			model.removeNodeFromParent(selectNode);

		tree.updateUI();
	}

	protected void renameTreeNode() {
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
			EditorEnvironment.showException(e);
		}

		tree.updateUI();
	}

	protected void addTreeNode(String name) {
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		DefaultMutableTreeNode selectNode = (DefaultMutableTreeNode) model.getRoot();
		if (tree.getSelectionPath() != null && tree.getSelectionPath().getLastPathComponent() != null)
			selectNode = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
		try {
			DefaultMutableTreeNode node = name == null || name.isEmpty()
					? addTreeNode(model, selectNode)
					: addTreeNode(model, selectNode, name);
			if (node != null)
				tree.startEditingAtPath(new TreePath(node.getPath()));
			tree.updateUI();
		} catch (JSONException e) {
			e.printStackTrace();
			EditorEnvironment.showException(e);
		}
	}

	protected DefaultMutableTreeNode addTreeNode(DefaultTreeModel model, DefaultMutableTreeNode parent) throws JSONException {
		if (parent == null)
			return null;

		String name = "新建节点";
		int index = 0;
		while (!checkTreeNodeName(name + String.valueOf(index), parent)) {
			index++;
		}

		return addTreeNode(model, parent, name);
	}

	protected DefaultMutableTreeNode addTreeNode(DefaultTreeModel model, 
			DefaultMutableTreeNode parent, String name) throws JSONException {
		if (parent == null)
			parent = (DefaultMutableTreeNode) model.getRoot();

		if (parent == model.getRoot()) {
			if (!(name.equals("{}") || name.equals("[]"))) {
				EditorEnvironment.showMessage(null, "不能在根节点添加此类节点！", "添加", JOptionPane.WARNING_MESSAGE);
				return null;
			}
		}

		Object data = name;
		switch (name) {
		case "{}":
		case "[]":
			if (parent.getUserObject() instanceof TreeItemInfo) {
				String parentName = parent.getUserObject().toString();

				if (parentName.compareTo("[]") == 0) {
					break;
				} else if (parentName.compareTo("{}") != 0 && parent.getChildCount() == 0) {
					DefaultMutableTreeNode parent_parent = (DefaultMutableTreeNode) parent.getParent();
					if (parent_parent != model.getRoot()) {
						String ppName = ((TreeItemInfo) parent_parent.getUserObject()).toString();
						if (ppName.compareTo("{}") == 0) {
							break;
						}
					}
				}
			} else {
				if (parent.getChildCount() == 0)
					break;
			}
			EditorEnvironment.showMessage(null, "此节点不可以添加节点！", "添加", JOptionPane.WARNING_MESSAGE);
			return null;
		default:
			if (parent.getUserObject() instanceof TreeItemInfo) {
				String parentName = parent.getUserObject().toString();

				if (parentName.compareTo("[]") == 0) {
					break;
				} else if (parentName.compareTo("{}") == 0) {
					break;
				} else {
					DefaultMutableTreeNode parent_parent = (DefaultMutableTreeNode) parent.getParent();
					if (parent_parent != model.getRoot()) {
						String ppName = ((TreeItemInfo) parent_parent.getUserObject()).toString();
						if (ppName.compareTo("{}") == 0 && parent.getChildCount() == 0) {
							break;
						}
					}
				}
			}
			EditorEnvironment.showMessage(null, "此节点不可以添加节点！", "添加", JOptionPane.WARNING_MESSAGE);
			return null;
		}
		return addTreeNode(model, data, parent);
	}

	protected DefaultMutableTreeNode addTreeNode(DefaultTreeModel model, Object data, DefaultMutableTreeNode parentNode)
			throws JSONException {
		TreeItemInfo info = new TreeItemInfo();
		info.data = data;
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(info);
		if (parentNode == null)
			parentNode = (DefaultMutableTreeNode) model.getRoot();

		parentNode.add(node);

		return node;
	}

	protected void updateTree() {
		newTreeModel();
		if (valueObj == null)
			return;

		try {
			jsonToTree();
		} catch (JSONException e) {
			e.printStackTrace();
			EditorEnvironment.showMessage(null, "数据格式错误，请检查输入的数据！", "错误", JOptionPane.ERROR_MESSAGE);
		}
	}

	protected void updateEditor() {
		if (valueObj != null) {
			try {
				if (valueObj instanceof JSONObject)
					editor.setText(((JSONObject) valueObj).toString(4));
				else if (valueObj instanceof JSONArray)
					editor.setText(((JSONArray) valueObj).toString(4));
				else {
					editor.setText(valueObj.toString());
				}
			} catch (JSONException e) {
				e.printStackTrace();
				EditorEnvironment.showMessage(null, "数据格式错误，请检查输入的数据！", "错误", JOptionPane.ERROR_MESSAGE);
			}
		}

	}

	protected int getRealLineIndex(int lineIndex) throws BadLocationException {
		Document document = editor.getDocument();
		Element root = document.getDefaultRootElement();
		int jsonLineCount = 0;
		int realLineIndex = -1;
		int prev = 0;
		for (int i = 0; i < root.getElementCount(); i++) {
			realLineIndex = i;
			Element line = root.getElement(i);
			for (int j = line.getStartOffset(); j < line.getEndOffset(); j++) {
				int key = document.getText(j, 1).charAt(0);
				switch (key) {
				case '\r':
					jsonLineCount++;
					break;
				case '\n':
					if (prev != '\r')
						jsonLineCount++;
				}
				prev = key;
				if (jsonLineCount == lineIndex) {
					return realLineIndex;
				}
			}
		}
		return realLineIndex;
	}

	protected void updateJson() throws Exception {
		if (tree.isEditing()) {
			tree.getCellEditor().stopCellEditing();
			treeToJson();
		} else {
			String text = editor.getText();
			valueObj = null;
			if (text != null && !text.isEmpty()) {
				try {
					valueObj = JsonHelp.parseJson(text);
				} catch (JSONException e) {
					String msg = e.getMessage();
					Pattern regex = Pattern.compile(
							"(['\"]([^\"']+)[\"'])?[\\s]*at[\\s]*([\\d]+)[\\s]*[\\[]character[\\s]*([\\d]+)[\\s]*line[\\s]*([\\d]+)[\\]]");
					Matcher matcher = regex.matcher(msg);
					if (matcher.find()) {
						String key = matcher.group(2);
						// int pos = Integer.parseInt(matcher.group(3));
						int linepos = Integer.parseInt(matcher.group(4));
						int line = Integer.parseInt(matcher.group(5));
						int realLine = getRealLineIndex(line);
						Document document = editor.getDocument();
						Element root = document.getDefaultRootElement();
						Element element = root.getElement(realLine);

						int keysize = key == null ? 0 : key.length();
						int index = element.getStartOffset() + (linepos - keysize);
						editor.setCaretPosition(index);
						editor.setSelectionStart(index);
						editor.setSelectionEnd(index + (keysize == 0 ? 1 : keysize));
						EditorEnvironment.showMessage(
								"JSON格式错误：位置【" + index + "】，行【" + realLine + "】，行位置【" + (linepos - keysize) + "】");
					} else
						EditorEnvironment.showException(e);
					editor.requestFocus();
				}
			}
		}
	}

	public void reset() {
		valueObj = old;
		updateEditor();
		updateTree();
	}

	public void init(Object obj) {
		old = obj;
		valueObj = obj;
		updateEditor();
		updateTree();
	}

	public Object getResult() throws JSONException, Exception {
		updateJson();
		return valueObj;
	}

	boolean isok = false;
	private JTextPane editor;
	private TreeDrag tree;
	private IMainControl mainControl;

	protected void insertText(String text) {
		int start = 0;
		if (editor.getSelectionStart() > 0)
			start = editor.getSelectionStart();
		insertText(text, start);
	}

	protected void resetText(String text) {
		editor.setText(null);
		insertText(text, 0);
	}

	protected void insertText(String text, int start) {
		try {
			editor.getDocument().insertString(start, text, null);
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
	}

	UndoManager undoManager = new UndoManager();
	private JCheckBoxMenuItem isControlName;
	private JCheckBoxMenuItem isUIName;
	private JComboBox<TemplateInfo> templateComboBox;
	private JComboBox<IconComboBoxItem> controlTypeComboBox;
	private JPanel controls;
	private JPanel gridColumnType;
	private JComboBox<KeyValue<String, String>> gridControlTypeComboBox;

	class TemplateInfo {
		public File file;
		public String name;
		public String type;

		public String toString() {
			return name;
		}
	}

	protected void initTemplates(String typename) {
		File controlTemplateDir = EditorEnvironment.getEditorSourcePath(EditorEnvironment.Template_Path,
				EditorEnvironment.Control_Dir_Name);
		if (!controlTemplateDir.exists())
			if (!controlTemplateDir.mkdirs())
				return;

		List<TemplateInfo> templateInfos = new ArrayList<>();
		for (File file : controlTemplateDir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File file) {
				return FileHelp.GetExt(file.getName()).compareTo(EditorEnvironment.Template_Path) == 0;
			}
		})) {
			String[] tmps = file.getName().split("\\.");
			if (typename.compareTo(tmps[0]) != 0)
				continue;

			TemplateInfo templateInfo = new TemplateInfo();
			templateInfo.file = file;
			templateInfo.name = tmps[1];
			templateInfo.type = tmps[0];
			templateInfos.add(templateInfo);
		}
		templateComboBox
				.setModel(new DefaultComboBoxModel<>(templateInfos.toArray(new TemplateInfo[templateInfos.size()])));
	}

	protected void onEditorCaretChanged() {

		if (gridColumnType.isVisible()) {
			Document document = editor.getDocument();
			Element root = document.getDefaultRootElement();
			int index = root.getElementIndex(editor.getCaretPosition());
			if (index == -1)
				return;

			Element cur = root.getElement(index);
			try {
				String text = document.getText(cur.getStartOffset(), cur.getEndOffset() - cur.getStartOffset());
				String[] keys = text.trim().split(":");
				if (keys.length == 2) {
					String key = keys[0].replaceAll("[\"'][\\s]*[tT][yY][Pp][Ee][\\s]*[\"']", "type");

					if (key.compareTo("type") == 0) {
						String v = keys[1];
						if (keys[1].endsWith(",")) {
							v = keys[1].substring(0, keys[1].length() - 1).trim();
						}

						for (int i = 0; i < gridControlTypeComboBox.getItemCount(); i++) {
							KeyValue<String, String> ct = gridControlTypeComboBox.getItemAt(i);
							if (ct.value.compareTo(v) == 0) {
								inited = false;
								gridControlTypeComboBox.setSelectedIndex(i);
								inited = true;
								break;
							}
						}
					}
				}
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}

	boolean inited = false;

	@SuppressWarnings("unchecked")
	public JsonEditorDialog(IMainControl mainControl) {
		setFont(new Font("微软雅黑", Font.PLAIN, 12));
		this.mainControl = mainControl;
		setIconImage(Toolkit.getDefaultToolkit().getImage(JsonEditorDialog.class.getResource("/image/browser.png")));
		setTitle("编辑");
		setModal(true);
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setBounds(100, 100, 1012, 707);
		getContentPane().setLayout(new BorderLayout());

		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.3);
		getContentPane().add(splitPane, BorderLayout.CENTER);

		JPanel panel_1 = new JPanel();
		splitPane.setLeftComponent(panel_1);
		panel_1.setLayout(new BorderLayout(0, 0));

		JToolBar toolBar = new JToolBar();
		panel_1.add(toolBar, BorderLayout.SOUTH);

		JButton addTreeNode = new JButton(" 添加 ");
		addTreeNode.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		addTreeNode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addTreeNode(null);
			}
		});
		toolBar.add(addTreeNode);

		JButton delTreeNode = new JButton(" 删除 ");
		delTreeNode.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		delTreeNode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeTreeNode();
			}
		});

		JButton button_3 = new JButton(" 添加{} ");
		button_3.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addTreeNode("{}");
			}
		});
		toolBar.add(button_3);

		JButton button_4 = new JButton(" 添加[] ");
		button_4.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addTreeNode("[]");
			}
		});
		toolBar.add(button_4);

		JButton button_5 = new JButton(" 编辑 ");
		button_5.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				renameTreeNode();
			}
		});
		toolBar.add(button_5);
		toolBar.add(delTreeNode);

		JButton button_2 = new JButton(" 同步 ");
		button_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					treeToJson();
					updateEditor();
				} catch (Exception e1) {
					e1.printStackTrace();
					EditorEnvironment.showException(e1);
				}
			}
		});
		toolBar.add(button_2);

		tree = new TreeDrag();
		tree.setShowsRootHandles(true);
		tree.setRootVisible(false);
		tree.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		tree.addOnDragListener(new IOnDrag() {

			@Override
			public void onDragEnd(DefaultMutableTreeNode newParent, DefaultMutableTreeNode oldParent, int newIndex,
					int oldIndex, DefaultMutableTreeNode node) {

			}

		});

		JScrollPane scrollPane = new JScrollPane();
		panel_1.add(scrollPane, BorderLayout.CENTER);
		scrollPane.setViewportView(tree);

		JPanel panel_2 = new JPanel();
		splitPane.setRightComponent(panel_2);
		panel_2.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane_1 = new JScrollPane();
		panel_2.add(scrollPane_1, BorderLayout.CENTER);
		editor = new JTextPane();
		editor.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				onEditorCaretChanged();
			}
		});
		editor.getDocument().addUndoableEditListener(undoManager);
		editor.setInheritsPopupMenu(true);
		editor.setPreferredSize(new Dimension(400, 200));
		editor.setFont(new Font("微软雅黑", Font.PLAIN, 14));
		editor.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		editor.setAutoscrolls(false);
		// panel_2.add(editor, BorderLayout.CENTER);
		scrollPane_1.setViewportView(editor);

		TextEditorPopMenu.add(editor);

		JToolBar toolBar_1 = new JToolBar();
		panel_2.add(toolBar_1, BorderLayout.SOUTH);

		JButton btnNewButton_1 = new JButton(" 同步 ");
		btnNewButton_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					updateJson();
					jsonToTree();
				} catch (Exception e1) {
					e1.printStackTrace();
					EditorEnvironment.showMessage(null, "数据格式错误，请检查输入的数据！", "错误", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		toolBar_1.add(btnNewButton_1);

		JButton btnjson = new JButton("格式JSON");
		btnjson.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		btnjson.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					updateJson();
					updateEditor();
				} catch (Exception e1) {
					e1.printStackTrace();
					EditorEnvironment.showMessage(null, "数据格式错误，请检查输入的数据！", "错误", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		toolBar_1.add(btnjson);

		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.SOUTH);
		panel.setLayout(new BorderLayout(0, 0));

		JPanel panel_3 = new JPanel();
		panel.add(panel_3, BorderLayout.EAST);
		panel_3.setLayout(new BoxLayout(panel_3, BoxLayout.X_AXIS));

		JButton button_1 = new JButton("重置");
		panel_3.add(button_1);
		button_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		JButton btnNewButton = new JButton("确定");
		panel_3.add(btnNewButton);
		btnNewButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		JButton button = new JButton("取消");
		panel_3.add(button);
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		JToolBar toolBar_2 = new JToolBar();
		toolBar_2.setBorder(null);
		toolBar_2.setFloatable(false);
		toolBar_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel.add(toolBar_2, BorderLayout.CENTER);

		controls = new JPanel();
		controls.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_2.add(controls);

		JLabel label = new JLabel(" 控件类型 ");
		controls.add(label);
		label.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		controlTypeComboBox = new JComboBox<>();
		controls.add(controlTypeComboBox);
		controlTypeComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (controlTypeComboBox.getSelectedItem() == null)
					return;

				IconComboBoxItem item = (IconComboBoxItem) controlTypeComboBox.getSelectedItem();
				initTemplates(item.name);
				gridColumnType.setVisible(item.name.compareTo(DrawInfoDefines.Grid_Name) == 0);
			}
		});
		controlTypeComboBox.setRenderer(new IconComboBoxRender<IconComboBoxItem>());
		DefaultComboBoxModel<IconComboBoxItem> model = new DefaultComboBoxModel<>();
		for (String typename : DrawInfoDefines.TypeNames) {
			ImageIcon icon = new ImageIcon(UINode.getImage(typename));
			model.addElement(new IconComboBoxItem(typename, icon));
		}

		controlTypeComboBox.setModel(model);

		JLabel label_1 = new JLabel(" 模板 ");
		controls.add(label_1);
		label_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		templateComboBox = new JComboBox<>();
		controls.add(templateComboBox);
		templateComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		JButton button_7 = new JButton(" 设置 ");
		controls.add(button_7);
		button_7.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (templateComboBox.getSelectedItem() == null)
					return;

				TemplateInfo templateInfo = (TemplateInfo) templateComboBox.getSelectedItem();
				try {
					String text = TextStreamHelp.loadFromFile(templateInfo.file);
					resetText(text);
					editor.requestFocus();
				} catch (IOException e1) {
					e1.printStackTrace();
					EditorEnvironment.showException(e1);
				}
			}
		});
		button_7.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		gridColumnType = new JPanel();
		controls.add(gridColumnType);

		JLabel label_2 = new JLabel("表格列类型");
		label_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		gridColumnType.add(label_2);

		gridControlTypeComboBox = new JComboBox<>(
				new DefaultComboBoxModel<>(new KeyValue[] { new KeyValue<String, String>("系统多选类型", "0"),
						new KeyValue<String, String>("系统序号类型", "1"), new KeyValue<String, String>("日期类型", "2"),
						new KeyValue<String, String>("文本类型", "3"), new KeyValue<String, String>("下拉列表类型", "4"),
						new KeyValue<String, String>("多级表头类型", "5"), new KeyValue<String, String>("布尔类型", "6"),
						new KeyValue<String, String>("整数类型", "7"), new KeyValue<String, String>("小数类型", "8"), }));
		gridControlTypeComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox<KeyValue<String, String>> comboBox = (JComboBox<KeyValue<String, String>>) e.getSource();
				if (comboBox.getSelectedItem() == null)
					return;
				if (inited) {
					insertText(((KeyValue<String, String>) comboBox.getSelectedItem()).value);
					editor.requestFocus();
				}
			}
		});
		gridControlTypeComboBox.setSelectedIndex(3);
		gridControlTypeComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		gridColumnType.add(gridControlTypeComboBox);

		JButton button_6 = new JButton("插入对象");
		button_6.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_2.add(button_6);

		JPopupMenu popupMenu = new JPopupMenu();
		addPopup(button_6, popupMenu);

		JMenu menu_1 = new JMenu("控件");
		popupMenu.add(menu_1);
		menu_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		JMenuItem menuItem = new JMenuItem("新建");
		menuItem.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				UINode node = CreateControlDialog.showDialog(null, null, false);
				if (node != null) {
					try {
						insertText(node.toJson().toString());
					} catch (JSONException e1) {
						e1.printStackTrace();
					}
				}
			}
		});

		JMenuItem menuItem_1 = new JMenuItem("选择");
		menu_1.add(menuItem_1);
		menuItem_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		menuItem_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String text = editor.getSelectedText();
				com.wh.dialog.editor.ControlSelectDialog.Result result = ControlSelectDialog.showDialog(text);
				if (!result.isok)
					return;

				if (result.data == null)
					return;
				
				String value = null;
				
				UINode node = (UINode)result.data[0];
				
				if (isControlName.isSelected())
					value = node.getDrawInfo().name;
				else
					try {
						value = node.toJson().toString();
					} catch (JSONException e1) {
						e1.printStackTrace();
					}
				insertText(value, editor.getSelectionEnd());
			}
		});
		menu_1.add(menuItem);

		JSeparator separator_1 = new JSeparator();
		menu_1.add(separator_1);

		isControlName = new JCheckBoxMenuItem("插入Name");
		isControlName.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		isControlName.setSelected(true);
		menu_1.add(isControlName);

		JMenu menu_3 = new JMenu("功能");
		popupMenu.add(menu_3);
		menu_3.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		JMenuItem menuItem_6 = new JMenuItem("模块");
		menuItem_6.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		menuItem_6.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Result result = ModelflowSelectDialog.showDialog(JsonEditorDialog.this.mainControl,
						editor.getSelectedText(), editor.getSelectedText());
				if (result != null) {
					insertText(result.name);
				}
			}
		});
		menu_3.add(menuItem_6);

		JMenuItem menuItem_5 = new JMenuItem("界面");
		menuItem_5.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		menuItem_5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				UISelectDialog.Result result = UISelectDialog.showDialog(JsonEditorDialog.this.mainControl,
						editor.getSelectedText(), null);
				if (result == null)
					return;
				insertText(result.id);
			}
		});
		menu_3.add(menuItem_5);

		JSeparator separator_2 = new JSeparator();
		menu_3.add(separator_2);

		isUIName = new JCheckBoxMenuItem("插入Name");
		isUIName.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		isUIName.setSelected(true);
		menu_3.add(isUIName);

		JMenu menu_2 = new JMenu("权限");
		popupMenu.add(menu_2);
		menu_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		JMenuItem menuItem_2 = new JMenuItem("色彩");
		popupMenu.add(menuItem_2);
		menuItem_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		menuItem_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color rgb = JColorChooser.showDialog(null, "颜色选择", null);
				if (rgb != null) {
					String value = ColorConvert.toHexFromColor(rgb);
					insertText(value);
				}
			}
		});

		JMenuItem menuItem_3 = new JMenuItem("图片");
		popupMenu.add(menuItem_3);
		menuItem_3.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		menuItem_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File ImageFile = Tools.selectOpenImageFile(null, null, null);
				if (ImageFile == null)
					return;

				String value = ImageFile.getName();
				File dest = EditorEnvironment.getProjectFile(EditorEnvironment.Image_Resource_Path, value);
				try {
					FileHelp.copyFileTo(ImageFile, dest);
					insertText(value);
				} catch (IOException e1) {
					e1.printStackTrace();
					EditorEnvironment.showException(e1);
				}
			}
		});

		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isok = false;
				JsonEditorDialog.this.setVisible(false);
			}
		});
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isok = true;
				JsonEditorDialog.this.setVisible(false);
			}
		});
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reset();
			}
		});
		setLocationRelativeTo(null);

		inited = true;
	}

	public static Object showJsonEditor(IMainControl mainControl, Object objValue) {
		return showJsonEditor(mainControl, objValue, null);
	}

	public static Object showJsonEditor(IMainControl mainControl, Object objValue, String controlType) {
		Object data = null;
		if (objValue != null) {
			if (objValue instanceof JSONArray || objValue instanceof JSONObject)
				data = objValue;
			else if (objValue instanceof String && !((String) objValue).isEmpty()) {
				try {
					data = JsonHelp.parseJson((String) objValue);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		Object value = JsonEditorDialog.show(mainControl, data, controlType);
		return value;
	}

	public static Object show(IMainControl mainControl, Object obj) {
		return show(mainControl, obj, null);
	}

	public static Object show(IMainControl mainControl, Object obj, String controlType) {
		JsonEditorDialog config = new JsonEditorDialog(mainControl);
		config.init(obj);
		if (controlType == null || controlType.isEmpty())
			config.controls.setVisible(false);
		else {
			for (int i = 0; i < config.controlTypeComboBox.getItemCount(); i++) {
				IconComboBoxItem item = (IconComboBoxItem) config.controlTypeComboBox.getItemAt(i);
				if (item.name.compareTo(controlType) == 0) {
					config.controlTypeComboBox.setSelectedIndex(i);
					break;
				}
			}
		}
		config.setVisible(true);
		if (config.isok)
			try {
				obj = config.getResult();
			} catch (Exception e) {
				e.printStackTrace();
				obj = null;
			}
		config.dispose();
		return obj;
	}

	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
			}

			public void mouseReleased(MouseEvent e) {
				if (e.getButton() != MouseEvent.BUTTON1)
					return;
				showMenu(e);
			}

			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}
}
