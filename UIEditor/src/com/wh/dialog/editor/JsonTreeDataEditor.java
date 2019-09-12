package com.wh.dialog.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wh.control.EditorEnvironment;
import com.wh.control.modelsearch.ModelSearchView;
import com.wh.control.tree.TreeHelp;
import com.wh.dialog.editor.JsonTreeDataEditor.IEditor.EditType;
import com.wh.system.tools.JsonHelp;

public class JsonTreeDataEditor extends JPanel {

	private static final long serialVersionUID = 1L;
	JSONArray old = null, valueObj = null;

	public static final String id = "id", name = "text", parent = "pid", type = "type", iconCls = "iconCls",
			checked = "checked", checkOnClick = "checkOnClick", jumpid = "jumpid";
	public JTree tree;
	public JTable table;

	public interface IEditor {
		public enum EditType {
			etAdd, etEdit, etRemove
		}

		public void onEdit(DefaultMutableTreeNode node, EditType editType);
	}

	public IEditor editor;

	protected static String getName(JSONObject data) {
		try {
			if (data.has("name"))
				return data.getString("name");
			else if (data.has(name))
				return data.getString(name);
			else {
				return "";
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return "";
		}
	}

	public static class TreeItemInfo implements Externalizable {
		public JSONObject data;

		public String toString() {
			String text = getName(data);
			if (text == null || text.isEmpty()) {
				try {
					if (data.has(type) && data.getString(type).compareTo("separator") == 0)
						return "-";
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return "";
			} else
				return text;
		}

		public void rename(String newName) throws JSONException {
			data.put(name, newName);
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
			else
				data = new JSONObject(tmp);
		}
	}

	protected boolean isEmptyCell(Object cellData) {
		return cellData == null || (cellData instanceof String && ((String) cellData).isEmpty());
	}

	protected boolean isEmptyRow() {
		return isEmptyCell(table.getSelectedRow());
	}

	@SuppressWarnings("unchecked")
	protected boolean isEmptyRow(int rowIndex) {
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		Vector<Vector<Object>> rows = model.getDataVector();
		if (rows.size() > 0 && rowIndex >= 0 && rowIndex < rows.size()) {
			Vector<Object> row = rows.get(rowIndex);
			if (row == null || row.size() == 0)
				return false;
			return isEmptyCell(row.get(0)) && isEmptyCell(row.get(0));
		}
		return true;
	}

	class TableModel extends DefaultTableModel {
		private static final long serialVersionUID = 1L;
		Object[] columns;
		HashMap<Integer, Integer> fixColumns = new HashMap<>();

		public TableModel(Object[] columns, int[] fixColumns) {
			super(new Object[][] {}, columns);
			this.columns = columns;
			if (fixColumns != null)
				for (int i = 0; i < fixColumns.length; i++) {
					this.fixColumns.put(fixColumns[i], fixColumns[i]);
				}
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return !fixColumns.containsKey(columnIndex);
		}

		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			super.setValueAt(aValue, rowIndex, columnIndex);
			fireOnChange(new int[] { rowIndex, columnIndex });
		}

		public Class<?> getColumnClass(int columnIndex) {
			Object obj = columns[columnIndex];
			if (obj instanceof Integer) {
				return Integer.class;
			} else if (obj instanceof Date) {
				return Date.class;
			} else if (obj instanceof Short) {
				return Short.class;
			} else if (obj instanceof Double) {
				return Double.class;
			} else if (obj instanceof Float) {
				return Float.class;
			} else if (obj instanceof Byte) {
				return Byte.class;
			} else if (obj instanceof Boolean) {
				return Boolean.class;
			} else {
				return String.class;
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private static class IntegerStringComparator implements Comparator {
		@SuppressWarnings({ "unchecked" })
		public int compare(Object o1, Object o2) {
			try {
				int i1 = Integer.parseInt(o1.toString());
				int i2 = Integer.parseInt(o2.toString());
				return Integer.compare(i1, i2);
			} catch (Exception e) {
				return ((Comparable) o1).compareTo(o2);
			}
		}
	}

	class MenuTableRowSorter<T> extends TableRowSorter<TableModel> {
		IntegerStringComparator integerStringComparator = new IntegerStringComparator();

		public Comparator<?> getComparator(int column) {
			return integerStringComparator;
		}
	}

	public enum ChangeObjectType {
		otTree, otGrid
	}

	public enum ChangeType {
		ctAdd, ctRemove, ctEdit, ctNone
	}

	public interface IDataNotify {
		public void onChange(ChangeObjectType ot, ChangeType ct);
	}

	public IDataNotify onDataNotify;

	public void refreshGrid() {
		treeToJson();
		updateTable();
	}

	public void refreshTree() {
		try {
			tableToJson();
			updateTree();
		} catch (JSONException e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(null, "数据格式错误，请检查输入的数据！", "错误", JOptionPane.ERROR_MESSAGE);
		}
	}

	protected void fireDataNotify(ChangeObjectType ot, ChangeType ct) {
		// if (ot == ChangeObjectType.otTree)
		// refreshGrid();
		//
		if (onDataNotify != null)
			onDataNotify.onChange(ot, ct);
	}

	void init(Object[] columns, boolean multipleSelection, int[] fixColumns) {
		table.setSelectionMode(multipleSelection ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
				: ListSelectionModel.SINGLE_SELECTION);
		DefaultTableModel tableModel = new TableModel(columns, fixColumns);
		table.setModel(tableModel);

	}

	void init(Object[][] datas, Object[] columns, boolean multipleSelection, int[] fixColumns) {
		init(columns, multipleSelection, fixColumns);
		TableModel tableModel = (TableModel) table.getModel();
		table.setRowSorter(null);
		if (datas != null)
			for (int i = 0; i < datas.length; i++) {
				tableModel.addRow(datas[i]);
			}
		MenuTableRowSorter<TableModel> sorter = new MenuTableRowSorter<TableModel>();
		sorter.setModel(tableModel);
		table.setRowSorter(sorter);
		// tableModel.fireTableDataChanged();
		tableModel.addTableModelListener(new TableModelListener() {

			@Override
			public void tableChanged(TableModelEvent e) {
				ChangeType cType;

				switch (e.getType()) {
				case TableModelEvent.DELETE:
					cType = ChangeType.ctRemove;
					break;

				case TableModelEvent.INSERT:
					cType = ChangeType.ctAdd;
					break;

				case TableModelEvent.UPDATE:
					cType = ChangeType.ctEdit;
					break;

				default:
					cType = ChangeType.ctNone;
					break;
				}
				fireDataNotify(ChangeObjectType.otGrid, cType);
			}
		});

	}

	protected void tableToJson() throws JSONException {
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		valueObj = new JSONArray();
		for (int i = 0; i < model.getRowCount(); i++) {
			JSONObject values = new JSONObject();
			values.put(id, model.getValueAt(i, 0).toString());
			values.put(name, model.getValueAt(i, 1).toString());
			Object parentid = model.getValueAt(i, 2);
			if (parentid != null && (parentid instanceof String && !((String) parentid).isEmpty()))
				values.put(parent, parentid.toString());
			values.put(type, model.getValueAt(i, 3));
			values.put(iconCls, model.getValueAt(i, 4));
			values.put(checked, model.getValueAt(i, 5));
			values.put(checkOnClick, model.getValueAt(i, 6));
			values.put(jumpid, model.getValueAt(i, 7));
			valueObj.put(values);
		}
	}

	public void copy() {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();// 获取系统剪切板
		String str;
		try {
			tableToJson();
			str = valueObj.toString();
		} catch (JSONException e) {
			str = "";
		}
		StringSelection selection = new StringSelection(str);// 构建String数据类型
		clipboard.setContents(selection, selection);// 添加文本到系统剪切板
	}

	public void paste() {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();// 获取系统剪切板
		Transferable content = clipboard.getContents(null);// 从系统剪切板中获取数据
		if (content.isDataFlavorSupported(DataFlavor.stringFlavor)) {// 判断是否为文本类型
			String text;
			try {
				text = (String) content.getTransferData(DataFlavor.stringFlavor);
				if (text == null) {
					return;
				}

				JSONArray jsonArray = new JSONArray(text);
				valueObj = jsonArray;

				jsonToTree();
				updateTable();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	protected void treeToJson() {
		valueObj = new JSONArray();
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		treeToJson(model, (DefaultMutableTreeNode) model.getRoot(), valueObj);
		if (!mustRoot) {
			for (int i = valueObj.length() - 1; i >= 0; i--) {
				JSONObject row = valueObj.getJSONObject(i);
				if (JsonHelp.isEmpty(row, "pid")) {
					valueObj.remove(i);
				}
			}
		}
	}

	protected void treeToJson(DefaultTreeModel model, DefaultMutableTreeNode parent, JSONArray json) {
		if (parent == null)
			return;

		if (parent.isLeaf()) {
			return;
		}

		TreeItemInfo parentInfo = (TreeItemInfo) parent.getUserObject();
		String pid = parentInfo == null ? "" : JsonHelp.getString(parentInfo.data, JsonTreeDataEditor.id);

		for (int i = 0; i < parent.getChildCount(); i++) {
			DefaultMutableTreeNode sub = (DefaultMutableTreeNode) parent.getChildAt(i);

			TreeItemInfo subInfo = (TreeItemInfo) sub.getUserObject();

			subInfo.data.put(JsonTreeDataEditor.parent, pid);
			json.put(subInfo.data);
			if (!sub.isLeaf()) {
				treeToJson(model, sub, json);
			} else {
			}
		}
	}

	HashMap<String, DefaultMutableTreeNode> nodes = new HashMap<>();
	private JPanel panel;
	private JToolBar toolBar;
	private JCheckBox addSub;
	private JButton button;
	private JButton button_2;
	private JPanel panel_1;
	private JToolBar toolBar_1;
	private JButton button_1;
	private JButton button_5;
	private JScrollPane scrollPane;
	private JButton button_6;
	private JScrollPane scrollPane_1;

	public interface IChange {
		public void onChange(Object data);
	}

	public IChange onChange;

	protected void fireOnChange(Object data) {
		if (onChange != null)
			onChange.onChange(data);
	}

	protected DefaultTreeModel newTreeModel() {
		DefaultTreeModel model = new DefaultTreeModel(new DefaultMutableTreeNode());
		tree.setModel(model);
		model.addTreeModelListener(new TreeModelListener() {

			@Override
			public void treeStructureChanged(TreeModelEvent e) {
				fireDataNotify(ChangeObjectType.otTree, ChangeType.ctRemove);
			}

			@Override
			public void treeNodesRemoved(TreeModelEvent e) {
				fireDataNotify(ChangeObjectType.otTree, ChangeType.ctRemove);
			}

			@Override
			public void treeNodesInserted(TreeModelEvent e) {
				fireDataNotify(ChangeObjectType.otTree, ChangeType.ctAdd);
			}

			@Override
			public void treeNodesChanged(TreeModelEvent e) {
				fireDataNotify(ChangeObjectType.otTree, ChangeType.ctEdit);
			}
		});
		return model;
	}

	protected boolean checkTreeNodeName(String name, DefaultMutableTreeNode parent) throws JSONException {
		for (int i = 0; i < parent.getChildCount(); i++) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) parent.getChildAt(i);
			TreeItemInfo json = (TreeItemInfo) node.getUserObject();
			if (JsonHelp.getString(json.data, id).compareToIgnoreCase(name) == 0)
				return false;
		}

		return true;
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
			JOptionPane.showMessageDialog(null, "此节点不能重命名！", "编辑", JOptionPane.WARNING_MESSAGE);
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
			JOptionPane.showMessageDialog(null,
					"编辑节点失败：" + e.getMessage() == null ? e.getClass().getName() : e.getMessage(), "编辑",
					JOptionPane.WARNING_MESSAGE);
		}

		fireOnChange(node);
		tree.updateUI();
	}

	protected void removeTreeNode() {
		if (tree.getSelectionPath() == null || tree.getSelectionPath().getLastPathComponent() == null)
			return;

		if (EditorEnvironment.showConfirmDialog("是否删除选定的项目？", "删除项目",
				JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
			return;

		DefaultMutableTreeNode selectNode = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		if (selectNode == model.getRoot()) {
			newTreeModel();
		} else
			removeNode(selectNode);

		fireOnChange(selectNode);
		tree.updateUI();
	}

	protected void editTreeNode() {
		if (tree.getSelectionPath() == null || tree.getSelectionPath().getLastPathComponent() == null)
			return;

		DefaultMutableTreeNode selectNode = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
		if (tree.isEditable())
			if (tree.getEditingPath().getLastPathComponent() == selectNode)
				return;

		if (editor != null)
			editor.onEdit(selectNode, EditType.etEdit);
		else
			tree.startEditingAtPath(new TreePath(selectNode.getPath()));

		fireOnChange(selectNode);
	}

	protected void addTreeNode() {
		DefaultMutableTreeNode selectNode = null;
		if (tree.getSelectionPath() != null && tree.getSelectionPath().getLastPathComponent() != null)
			selectNode = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();

		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		try {
			DefaultMutableTreeNode node = addTreeNode(model, selectNode, addSub.isSelected());
			if (node != null) {
				if (editor != null)
					editor.onEdit(selectNode, EditType.etAdd);
			}
			if (selectNode == null)
				TreeHelp.expandOrCollapse(tree, (DefaultMutableTreeNode) model.getRoot(), true);
		} catch (JSONException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null,
					"添加节点失败：" + e.getMessage() == null ? e.getClass().getName() : e.getMessage(), "添加节点",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	protected DefaultMutableTreeNode addTreeNode(DefaultTreeModel model, DefaultMutableTreeNode selectNode,
			boolean addChild) throws JSONException {
		if (selectNode == null)
			selectNode = (DefaultMutableTreeNode) model.getRoot();

		DefaultMutableTreeNode parent = selectNode;
		String name = "新建节点";
		int index = 0;
		if (!addChild) {
			parent = (DefaultMutableTreeNode) parent.getParent();
		}

		if (parent == null)
			parent = selectNode;

		while (!checkTreeNodeName(name + String.valueOf(index), parent)) {
			index++;
		}

		JSONObject data = new JSONObject();
		name = name + String.valueOf(index);
		TreeItemInfo parentData = (TreeItemInfo) parent.getUserObject();
		data.put(JsonTreeDataEditor.id, UUID.randomUUID().toString());
		data.put(JsonTreeDataEditor.name, name);
		if (parentData != null)
			data.put(JsonTreeDataEditor.parent, parentData.data.get(JsonTreeDataEditor.id));
		return addTreeNode(model, data, selectNode, parent);
	}

	protected DefaultMutableTreeNode addTreeNode(DefaultTreeModel model, JSONObject data,
			DefaultMutableTreeNode selectNode, DefaultMutableTreeNode parentNode) throws JSONException {
		TreeItemInfo info = new TreeItemInfo();
		info.data = data;
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(info);
		if (parentNode == null)
			model.setRoot(node);
		else {
			if (selectNode == null || selectNode == parentNode)
				model.insertNodeInto(node, parentNode, parentNode.getChildCount());
			else
				model.insertNodeInto(node, parentNode, parentNode.getIndex(selectNode));
		}
		nodes.put(JsonHelp.getString(data, id), node);

		fireOnChange(node);

		tree.updateUI();
		return node;
	}

	protected void jsonToTree() throws JSONException {
		nodes.clear();
		TreeMap<String, List<JSONObject>> values = new TreeMap<>();
		for (int i = 0; i < valueObj.length(); i++) {
			JSONObject value = valueObj.getJSONObject(i);
			String key = null;
			if (value.has(parent))
				key = JsonHelp.getString(value, JsonTreeDataEditor.parent);
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

		// 为true表示仅两级树
		boolean onlyTwo = !values.containsKey("");

		DefaultTreeModel model = newTreeModel();
		DefaultMutableTreeNode parentNode = null;
		List<String> keys = new ArrayList<>(values.keySet());
		List<String> nokeys = new ArrayList<>();
		while (keys.size() > 0) {
			String pid = keys.remove(0);
			if (pid.isEmpty()) {
				parentNode = (DefaultMutableTreeNode) model.getRoot();
			} else {
				if (onlyTwo) {
					JSONObject info = new JSONObject();
					info.put(name, pid);
					info.put(id, pid);
					parentNode = addTreeNode(model, info, null, (DefaultMutableTreeNode) model.getRoot());
				} else
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
			jsonToTree(model, datas, parentNode);
		}

		if (model.getRoot() != null) {
			TreeHelp.expandOrCollapse(tree, (DefaultMutableTreeNode) model.getRoot(), true);
		}

	}

	protected void jsonToTree(DefaultTreeModel model, List<JSONObject> datas, DefaultMutableTreeNode parentNode)
			throws JSONException {

		for (int i = 0; i < datas.size(); i++) {
			JSONObject subInfo = datas.get(i);
			addTreeNode(model, subInfo, null, parentNode);
		}

	}

	protected void updateTree() {
		newTreeModel();
		if (valueObj == null)
			return;

		try {
			jsonToTree();
		} catch (JSONException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "数据格式错误，请检查输入的数据！", "错误", JOptionPane.ERROR_MESSAGE);
		}
	}

	protected void updateTable() {
		Object[][] Data = null;
		try {
			JSONArray datas = valueObj;
			if (valueObj != null) {
				Data = new Object[datas.length()][8];
				for (int i = 0; i < datas.length(); i++) {
					JSONObject rowData = datas.getJSONObject(i);
					Data[i][0] = "";
					if (!rowData.has(id))
						rowData.put(id, UUID.randomUUID().toString());
					Data[i][0] = JsonHelp.getString(rowData, id);
					Data[i][1] = getName(rowData);
					if (rowData.has(parent))
						Data[i][2] = JsonHelp.getString(rowData, parent);
					else
						Data[i][2] = "";

					if (rowData.has(type))
						Data[i][3] = JsonHelp.getString(rowData, type);
					else
						Data[i][3] = "";

					if (rowData.has(iconCls))
						Data[i][4] = JsonHelp.getString(rowData, iconCls);
					else
						Data[i][4] = "";

					if (rowData.has(checked))
						Data[i][5] = JsonHelp.getString(rowData, checked);
					else
						Data[i][5] = "";

					if (rowData.has(checkOnClick))
						Data[i][6] = JsonHelp.getString(rowData, checkOnClick);
					else
						Data[i][6] = "";

					if (rowData.has(jumpid))
						Data[i][7] = JsonHelp.getString(rowData, jumpid);
					else
						Data[i][7] = "";
				}
			}
			init(Data, new Object[] { id, name, parent, type, iconCls, checked, checkOnClick, jumpid }, false, null);
		} catch (JSONException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "数据格式错误，请检查输入的数据！", "错误", JOptionPane.ERROR_MESSAGE);
		}

	}

	protected void updateJson() {
		if (table.isEditing())
			table.getCellEditor().stopCellEditing();

		DefaultTableModel model = (DefaultTableModel) table.getModel();

		JSONArray json = new JSONArray();
		for (int i = 0; i < model.getRowCount(); i++) {
			JSONObject value = new JSONObject();
			value.put(id, model.getValueAt(i, 0).toString());
			value.put(name, model.getValueAt(i, 1).toString());
			Object parentValue = model.getValueAt(i, 2);
			if (parentValue != null || (parentValue instanceof String && !((String) parentValue).isEmpty()))
				value.put(parent, model.getValueAt(i, 2).toString());
			if (model.getValueAt(i, 3) != null)
				value.put(type, model.getValueAt(i, 3).toString());
			if (model.getValueAt(i, 4) != null)
				value.put(iconCls, model.getValueAt(i, 4).toString());
			if (model.getValueAt(i, 5) != null)
				value.put(checked, model.getValueAt(i, 5).toString());
			if (model.getValueAt(i, 6) != null)
				value.put(checkOnClick, model.getValueAt(i, 6).toString());
			if (model.getValueAt(i, 7) != null)
				value.put(jumpid, model.getValueAt(i, 7).toString());
			json.put(value);
		}

		valueObj = json;
	}

	public JsonTreeDataEditor(IChange onChange) {
		this.onChange = onChange;
		setPreferredSize(new Dimension(761, 645));
		setLayout(new BorderLayout(0, 0));

		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.4);
		add(splitPane, BorderLayout.CENTER);

		panel = new JPanel();
		panel.setPreferredSize(new Dimension(0, 0));
		panel.setMinimumSize(new Dimension(0, 0));
		splitPane.setLeftComponent(panel);
		panel.setLayout(new BorderLayout(0, 0));

		tree = new ModelSearchView.TreeDragModelSearchView();
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		tree.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		toolBar = new JToolBar();
		panel.add(toolBar, BorderLayout.SOUTH);

		addSub = new JCheckBox("添加子节点");
		addSub.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		addSub.setSelected(true);
		toolBar.add(addSub);

		button = new JButton(" 添加 ");
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addTreeNode();
			}
		});
		toolBar.add(button);

		button_2 = new JButton(" 删除 ");
		button_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeTreeNode();
			}
		});
		toolBar.add(button_2);

		button_1 = new JButton(" 同步 ");
		button_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshGrid();
			}
		});

		button_6 = new JButton(" 编辑 ");
		button_6.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_6.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				renameTreeNode();
			}
		});
		toolBar.add(button_6);
		toolBar.add(button_1);

		scrollPane = new JScrollPane();
		panel.add(scrollPane, BorderLayout.CENTER);
		scrollPane.setViewportView(tree);

		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				if (e.getNewLeadSelectionPath() == null || e.getNewLeadSelectionPath().getLastPathComponent() == null)
					return;

				DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getNewLeadSelectionPath()
						.getLastPathComponent();
				TreeItemInfo info = (TreeItemInfo) node.getUserObject();
				if (info == null)
					return;

				DefaultTableModel model = (DefaultTableModel) table.getModel();
				String jsonid = null;
				try {
					jsonid = JsonHelp.getString(info.data, id);
				} catch (JSONException e1) {
					e1.printStackTrace();
				}

				if (jsonid == null || jsonid.isEmpty())
					table.removeRowSelectionInterval(0, model.getRowCount() - 1);
				for (int i = 0; i < model.getRowCount(); i++) {
					Object value = model.getValueAt(i, 0);
					if (value == null || value.toString() == null)
						continue;

					if (value.toString().compareTo(jsonid) == 0) {
						int row = table.convertRowIndexToView(i);
						table.setRowSelectionInterval(row, row);
						table.scrollRectToVisible(table.getCellRect(row, 0, true));
					}
				}
			}
		});

		panel_1 = new JPanel();
		panel_1.setPreferredSize(new Dimension(0, 0));
		splitPane.setRightComponent(panel_1);
		panel_1.setLayout(new BorderLayout(0, 0));

		table = new ModelSearchView.TableModelSearchView();
		table.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setSurrendersFocusOnKeystroke(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {
				// 鼠标按下时bl=true释放时bl=false
				// boolean bl=e.getValueIsAdjusting();
				if (e.getValueIsAdjusting())
					return;

				if (table.getSelectedRow() == -1)
					return;

				if (isEmptyRow())
					return;

				int selectRow = table.convertRowIndexToModel(table.getSelectedRow());
				DefaultTableModel model = (DefaultTableModel) table.getModel();
				Object obj = model.getValueAt(selectRow, 0);
				if (obj == null)
					return;
				String idString = obj.toString();
				if (nodes.containsKey(idString)) {
					DefaultMutableTreeNode node = nodes.get(idString);
					TreePath path = new TreePath(node.getPath());
					tree.setSelectionPath(path);
					tree.scrollPathToVisible(path);
				}
			}
		});

		toolBar_1 = new JToolBar();
		panel_1.add(toolBar_1, BorderLayout.SOUTH);

		button_5 = new JButton(" 同步 ");
		button_5.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshTree();
			}
		});
		toolBar_1.add(button_5);

		scrollPane_1 = new JScrollPane();
		panel_1.add(scrollPane_1, BorderLayout.CENTER);
		scrollPane_1.setViewportView(table);
		reset();
	}

	public void removeNode(DefaultMutableTreeNode node) {
		for (int i = node.getChildCount() - 1; i >= 0; i--) {
			removeNode((DefaultMutableTreeNode) node.getChildAt(i));
		}
		((DefaultTreeModel) tree.getModel()).removeNodeFromParent(node);
		TreeItemInfo info = (TreeItemInfo) node.getUserObject();
		if (info != null)
			nodes.remove(JsonHelp.getString(info.data, id));
		if (editor != null)
			editor.onEdit(node, EditType.etRemove);

	}

	public void select(String id) {
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		for (int i = 0; i < model.getRowCount(); i++) {
			String cid = (String) model.getValueAt(i, 0);
			if (cid != null && cid.compareTo(id) == 0) {
				table.setRowSelectionInterval(i, i);
				table.scrollRectToVisible(table.getCellRect(i, 0, true));
				break;
			}
		}
	}

	public void select(int dbIndex) {
		dbIndex = table.convertRowIndexToView(dbIndex);
		if (dbIndex >= 0 && dbIndex < table.getRowCount()) {
			table.scrollRectToVisible(table.getCellRect(dbIndex, 0, true));
			table.setRowSelectionInterval(dbIndex, dbIndex);
		} else
			table.clearSelection();
	}

	public void reset() {
		valueObj = old;
		updateTable();
		updateTree();
	}

	public void setValue(JSONArray obj) {
		old = obj;
		valueObj = obj;
		updateTable();
		updateTree();
	}

	public JSONArray getTableData() {
		updateJson();
		return valueObj;		
	}
	
	public JSONArray getResult() {
		refreshGrid();
		updateJson();
		return valueObj;
	}

	boolean mustRoot = true;

	public void setMustRoot(boolean b) {
		mustRoot = b;
	}
}
