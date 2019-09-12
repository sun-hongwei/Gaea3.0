package com.wh.form;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wh.control.EditorEnvironment;
import com.wh.control.modelsearch.ModelSearchView;
import com.wh.control.tree.ImageTreeRender;
import com.wh.control.tree.TreeHelp;
import com.wh.dialog.editor.ControlSelectDialog;
import com.wh.dialog.editor.ControlSelectDialog.Result;
import com.wh.draws.DrawNode;
import com.wh.draws.UINode;
import com.wh.draws.WorkflowNode;
import com.wh.system.tools.EventHelp;
import com.wh.system.tools.FileHelp;
import com.wh.system.tools.JsonHelp;

public class SceneBuilder extends ChildForm implements IMainMenuOperation, ISubForm {
	private static final long serialVersionUID = 1647458287761184720L;
	private final JPanel contentPanel = new JPanel();
	private JButton button1;
	private JButton button2;
	private JPanel panel_5;
	private JTextField titlelabel;
	private JTextPane steps;
	private JTextPane inputs;
	private JTable table;

	public static final String title = "title", input = "input", step = "step", output = "output",
			invariance = "invariance", memo = "memo", control = "control", nodetype = "nodetype", child = "child",
			endtime = "endtime", scenens = "ns", flow = "flow";
	private JTree tree;

	private JSONArray valueObj = new JSONArray();
	private JTextPane invariances;
	private JTextPane outputs;
	private JTextPane memos;
	private File flowFile;

	public enum NodeType {
		ntDirNode, ntMainNode, ntExceptNode, ntNone
	}

	class TreeControl {
		class TreeItemInfo {
			public JSONObject data;

			public NodeType type() {
				try {
					return NodeType.valueOf(data.getString(nodetype));
				} catch (JSONException e) {
					e.printStackTrace();
					return NodeType.ntNone;
				}
			}

			public String toString() {
				try {
					return data.getString(title);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return "";
			}

			public void rename(String name) throws JSONException {
				data.put(title, name);
			}

			public String getName() throws JSONException {
				if (data.has(title))
					return (String) data.get(title);
				else
					return null;
			}
		}

		public void init() {
			tree.setCellRenderer(new ImageTreeRender(new ImageTreeRender.IGetIcon() {

				@Override
				public Icon getIcon(TreeNode node) {
					DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
					if (!(treeNode.getUserObject() instanceof TreeItemInfo))
						return null;

					JSONObject data = (JSONObject) ((TreeItemInfo) treeNode.getUserObject()).data;
					switch (NodeType.valueOf(JsonHelp.getString(data, nodetype))) {
					case ntExceptNode:
						return error;
					case ntMainNode:
						return succ;
					case ntDirNode:
						return dir;
					default:
						return null;
					}
				}
			}));

			tree.addTreeSelectionListener(new TreeSelectionListener() {

				@Override
				public void valueChanged(TreeSelectionEvent e) {
					if (e.getOldLeadSelectionPath() != null)
						treeControl.saveEditor(
								(DefaultMutableTreeNode) e.getOldLeadSelectionPath().getLastPathComponent());

					if (e.getNewLeadSelectionPath() != null) {
						DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getNewLeadSelectionPath()
								.getLastPathComponent();
						JSONObject value = getTreeNodeValue(node);
						treeControl.updateEditor(node);
						tableControl.jsonToTable(value);
					}
				}
			});
		}

		protected void newJson() {
			valueObj = new JSONArray();
		}

		protected void treeToJson() throws JSONException, Exception {
			if (tree.isEditing())
				tree.getCellEditor().stopCellEditing();
			DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
			DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
			valueObj = new JSONArray();
			treeToJson(model, root, valueObj);
		}

		protected JSONObject getTreeNodeValue(DefaultMutableTreeNode node) {
			JSONObject value = null;
			if (node != null && node.getUserObject() != null) {
				value = ((TreeItemInfo) node.getUserObject()).data;
			}
			return value;
		}

		protected JSONObject treeNodeToJson(JSONArray json, DefaultMutableTreeNode node) throws Exception {
			JSONObject value = getTreeNodeValue(node);
			json.put(value);
			return value;
		}

		protected void treeToJson(DefaultTreeModel model, DefaultMutableTreeNode parent, JSONArray json)
				throws Exception {
			if (parent == null || json == null)
				return;

			if (parent.isLeaf()) {
				return;
			}

			for (int x = 0; x < parent.getChildCount(); x++) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) parent.getChildAt(x);
				JSONObject value = treeNodeToJson(json, node);
				if (NodeType.valueOf(JsonHelp.getString(value, nodetype)) == NodeType.ntDirNode) {
					value.put(child, new JSONArray());
					treeToJson(model, (DefaultMutableTreeNode) node, value.getJSONArray(child));
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

		protected void jsonToTree(DefaultTreeModel model, JSONArray valueObj, DefaultMutableTreeNode parent)
				throws JSONException {
			if (valueObj == null)
				return;

			for (int i = 0; i < valueObj.length(); i++) {
				JSONObject value = valueObj.getJSONObject(i);
				DefaultMutableTreeNode node = addTreeNode(model, value, parent);
				if (NodeType.valueOf(JsonHelp.getString(value, nodetype)) == NodeType.ntDirNode) {
					jsonToTree(model, value.getJSONArray(child), node);
				}
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

		public String getCaseName() throws JSONException {
			DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
			if (tree.getSelectionPath() == null || tree.getSelectionPath().getLastPathComponent() == null)
				return null;

			DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();

			if (node == model.getRoot()) {
				return null;
			}

			if (node.getParent() == model.getRoot())
				return null;

			TreeItemInfo info = (TreeItemInfo) node.getUserObject();
			return info.getName();
		}

		public String getDirName() throws JSONException {
			DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
			if (tree.getSelectionPath() == null || tree.getSelectionPath().getLastPathComponent() == null)
				return null;

			DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();

			if (node == model.getRoot()) {
				return null;
			}

			if (node.getParent() != model.getRoot())
				node = (DefaultMutableTreeNode) node.getParent();

			TreeItemInfo info = (TreeItemInfo) node.getUserObject();
			return info.getName();
		}

		protected void renameTreeNode() {
			DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
			if (tree.getSelectionPath() == null || tree.getSelectionPath().getLastPathComponent() == null)
				return;

			DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();

			if (node == model.getRoot()) {
				return;
			}

			String name = EditorEnvironment.showInputDialog("请输入节点的新名字：", node.getUserObject().toString());
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

		protected void addTreeNode(NodeType nodeType) {
			DefaultMutableTreeNode selectNode = null;
			TreeItemInfo info = null;
			if (tree.getSelectionPath() == null || tree.getSelectionPath().getLastPathComponent() == null) {
				switch (nodeType) {
				case ntDirNode:
					selectNode = (DefaultMutableTreeNode) tree.getModel().getRoot();
					break;
				case ntExceptNode:
				case ntMainNode:
				case ntNone:
				default:
					return;
				}
			} else {
				selectNode = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
				info = (TreeItemInfo) selectNode.getUserObject();
			}

			String name = null;
			switch (nodeType) {
			case ntDirNode:
				if (info != null && info.type() != NodeType.ntDirNode) {
					EditorEnvironment.showMessage(null, "仅可以在类型为【目录】的节点下添加【目录】！", "提示", JOptionPane.WARNING_MESSAGE);
					return;
				}
				break;
			case ntExceptNode:
				if (info.type() != NodeType.ntDirNode) {
					EditorEnvironment.showMessage(null, "仅可以在类型为【目录】的节点下添加【异常场景】！", "提示", JOptionPane.WARNING_MESSAGE);
					return;
				}
				break;
			case ntMainNode:
				if (info.type() != NodeType.ntDirNode) {
					for (int i = 0; i < selectNode.getChildCount(); i++) {
						TreeItemInfo child = (TreeItemInfo) ((DefaultMutableTreeNode) selectNode.getChildAt(i))
								.getUserObject();
						if (child.type() == NodeType.ntMainNode) {
							EditorEnvironment.showMessage(null, "一个【目录】下仅可以一个【主场景】！", "提示",
									JOptionPane.WARNING_MESSAGE);
							return;
						}
					}
					EditorEnvironment.showMessage(null, "仅可以在类型为【目录】的节点下添加【主场景】！", "提示", JOptionPane.WARNING_MESSAGE);
					return;
				}

				name = "主场景";
				break;
			default:
				return;
			}

			if (name == null)
				name = EditorEnvironment.showInputDialog("请输入节点的新名字：", "新场景");
			if (name == null || name.isEmpty())
				return;

			try {
				DefaultMutableTreeNode node = addTreeNode((DefaultTreeModel) tree.getModel(), selectNode, name,
						nodeType);
				TreeHelp.expandOrCollapse(tree, node, true);
				tree.updateUI();
			} catch (JSONException e) {
				e.printStackTrace();
				EditorEnvironment.showException(e);
			}
		}

		protected DefaultMutableTreeNode addTreeNode(DefaultTreeModel model, DefaultMutableTreeNode selectNode,
				String name, NodeType type) throws JSONException {
			if (selectNode == null)
				return null;

			int index = 0;
			if (!checkTreeNodeName(name, selectNode)) {
				while (!checkTreeNodeName(name + String.valueOf(index), selectNode)) {
					index++;
				}
				name = name + String.valueOf(index);
			}

			JSONObject data = new JSONObject();
			data.put(SceneBuilder.title, name);
			data.put(SceneBuilder.nodetype, type.name());
			return addTreeNode(model, data, selectNode);
		}

		protected DefaultMutableTreeNode addTreeNode(DefaultTreeModel model, JSONObject data,
				DefaultMutableTreeNode parentNode) throws JSONException {
			TreeItemInfo info = new TreeItemInfo();
			info.data = data;
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(info);
			if (parentNode == null)
				model.setRoot(node);
			else {
				if (parentNode != null)
					parentNode.add(node);
			}
			model.reload();
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
				EditorEnvironment.showException(e);
			}
		}

		protected void updateEditor() {
			if (tree.getSelectionPath() != null && tree.getSelectionPath().getLastPathComponent() != null) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
				updateEditor(node);
			}
		}

		protected void updateEditor(DefaultMutableTreeNode node) {
			if (node != null) {
				JSONObject value = getTreeNodeValue(node);

				if (value == null)
					return;

				try {
					switch (NodeType.valueOf(JsonHelp.getString(value, nodetype))) {
					case ntExceptNode:
					case ntMainNode:
					default:
						if (value.has(title))
							titlelabel.setText(JsonHelp.getString(value, title));
						else {
							titlelabel.setText("");
						}
						if (value.has(input))
							inputs.setText(JsonHelp.getString(value, input));
						else {
							inputs.setText("");
						}
						if (value.has(step))
							steps.setText(JsonHelp.getString(value, step));
						else {
							steps.setText("");
						}
						if (value.has(invariance))
							invariances.setText(JsonHelp.getString(value, invariance));
						else {
							invariances.setText("");
						}
						if (value.has(output))
							outputs.setText(JsonHelp.getString(value, output));
						else {
							outputs.setText("");
						}
						if (value.has(memo))
							memos.setText(JsonHelp.getString(value, memo));
						else {
							memos.setText("");
						}
						if (value.has(endtime))
							endTimeView.setText(JsonHelp.getString(value, endtime));
						else {
							endTimeView.setText("");
						}
						if (value.has(scenens))
							sceneNSView.setText(JsonHelp.getString(value, scenens));
						else {
							sceneNSView.setText("");
						}

						if (value.has(flow)) {
							flowFile = EditorEnvironment.getFlowRelationFile(JsonHelp.getString(value, flow));
						} else {
							String flowid = UUID.randomUUID().toString();
							flowFile = EditorEnvironment.getFlowRelationFile(flowid);
							value.put(flow, flowid);
						}

						tableControl.jsonToTable(value);
						break;
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

		}

		protected void saveEditor() {
			if (tree.getSelectionPath() != null && tree.getSelectionPath().getLastPathComponent() != null) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
				saveEditor(node);
			}
		}

		protected void saveTextEditor(JSONObject value, String key, JTextComponent editor) throws JSONException {
			if (editor.getText() != null)
				value.put(key, editor.getText());
		}

		protected void saveEditor(DefaultMutableTreeNode node) {
			if (node != null) {
				JSONObject value = getTreeNodeValue(node);

				if (value == null)
					return;

				try {
					switch (NodeType.valueOf(JsonHelp.getString(value, nodetype))) {
					case ntExceptNode:
					case ntMainNode:
						if (table.isEditing())
							table.getCellEditor().stopCellEditing();

						saveTextEditor(value, title, titlelabel);
						saveTextEditor(value, input, inputs);
						saveTextEditor(value, step, steps);
						saveTextEditor(value, invariance, invariances);
						saveTextEditor(value, output, outputs);
						if (flowFile != null)
							value.put(flow, FileHelp.removeExt(flowFile.getName()));
						saveTextEditor(value, memo, memos);
						saveTextEditor(value, endtime, endTimeView);
						saveTextEditor(value, scenens, sceneNSView);
						tableControl.tableToJson(node);
						break;
					default:
						break;
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

		}

		protected void saveJson() {
			try {
				if (tree.isEditing()) {
					tree.getCellEditor().stopCellEditing();
				}

				saveEditor();
				treeToJson();
			} catch (Exception e) {
				e.printStackTrace();
				EditorEnvironment.showException(e);
			}
		}

	}

	class TableControl {
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
		private class IntegerStringComparator implements Comparator {
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

		void init(Object[] columns, boolean multipleSelection, int[] fixColumns) {
			table.setSelectionMode(multipleSelection ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
					: ListSelectionModel.SINGLE_SELECTION);
			DefaultTableModel tableModel = new TableModel(columns, fixColumns);
			table.setModel(tableModel);

		}

		void init(Object[][] datas, Object[] columns, boolean multipleSelection, int[] fixColumns) {
			init(columns, multipleSelection, fixColumns);
			TableModel tableModel = (TableModel) table.getModel();
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

					switch (e.getType()) {
					case TableModelEvent.DELETE:
					case TableModelEvent.INSERT:
					case TableModelEvent.UPDATE:
						try {
							tableToJson();
						} catch (JSONException e1) {
							e1.printStackTrace();
							EditorEnvironment.showException(e1);
						}
						break;
					default:
						break;
					}
				}
			});

		}

		public String field_id = "编号", field_title = "标题", field_type = "类型", field_memo = "说明";

		protected void tableToJson() throws JSONException {
			if (tree.getSelectionPath() == null && tree.getSelectionPath().getLastPathComponent() == null) {
				return;
			}
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
			tableToJson(node);
		}

		protected void setTableValue(DefaultTableModel model, JSONObject json, String key, int row, int col)
				throws JSONException {
			Object value = model.getValueAt(row, col);
			if (value != null) {
				json.put(key, value.toString());
			}

		}

		protected void tableToJson(DefaultMutableTreeNode node) throws JSONException {
			if (node == null) {
				return;
			}
			JSONObject value = treeControl.getTreeNodeValue(node);
			if (value == null)
				return;

			DefaultTableModel model = (DefaultTableModel) table.getModel();
			JSONArray tableValue = new JSONArray();
			for (int i = 0; i < model.getRowCount(); i++) {
				JSONObject values = new JSONObject();
				setTableValue(model, values, field_id, i, 0);
				setTableValue(model, values, field_title, i, 1);
				setTableValue(model, values, field_type, i, 2);
				setTableValue(model, values, field_memo, i, 3);
				tableValue.put(values);
			}
			value.put(control, tableValue);
		}

		protected void remove() {
			int row = table.getSelectedRow();
			if (row == -1)
				return;

			if (EditorEnvironment.showConfirmDialog("是否删除选定的记录？", "删除",
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

				((TableModel) table.getModel()).removeRow(row);
			}
		}

		protected void add(DrawNode node) {
			UINode uiNode = (UINode) node;
			Object[] info = new Object[4];
			info[0] = node.id;
			info[1] = node.title;
			info[2] = uiNode.getDrawInfo().typeName();
			info[3] = node.memo;

			TableModel model = (TableModel) table.getModel();
			model.addRow(info);
			model.fireTableDataChanged();
		}

		protected void jsonToTable(JSONObject value) {
			Object[][] Data = null;
			if (value != null && value.has(control)) {
				try {
					JSONArray datas = value.getJSONArray(control);
					Data = new Object[datas.length()][4];
					for (int i = 0; i < datas.length(); i++) {
						JSONObject rowData = datas.getJSONObject(i);
						if (rowData.has(field_id))
							Data[i][0] = rowData.getString(field_id);
						if (rowData.has(field_title))
							Data[i][1] = rowData.getString(field_title);
						if (rowData.has(field_type))
							Data[i][2] = rowData.getString(field_type);
						if (rowData.has(field_memo))
							Data[i][3] = rowData.getString(field_memo);
					}
				} catch (JSONException e) {
					e.printStackTrace();
					EditorEnvironment.showMessage(null, "数据格式错误，请检查输入的数据！", "错误", JOptionPane.ERROR_MESSAGE);
				}
			}
			init(Data, new Object[] { field_id, field_title, field_type, field_memo }, false, null);
		}

	}

	TableControl tableControl = new TableControl();
	TreeControl treeControl = new TreeControl();

	public void save() throws Exception {
		File file = EditorEnvironment.getAppFile(nodeid, true);
		if (file == null) {
			EditorEnvironment.updateApp(nodeid, UUID.randomUUID().toString());
		}

		file = EditorEnvironment.getAppFile(nodeid, true);

		treeControl.saveJson();
		JsonHelp.saveJson(file, valueObj, null);

		EditorEnvironment.lockFile(file);
	}

	public void load() {
		File file = EditorEnvironment.getAppFile(nodeid, true);
		if (file == null || !file.exists()) {
			treeControl.newTreeModel();
			return;
		}
		Object object;
		try {
			object = JsonHelp.parseJson(file, null);
			if (object instanceof JSONObject)
				valueObj = ((JSONObject) object).getJSONArray("data");
			else
				valueObj = (JSONArray) object;

			treeControl.jsonToTree();
		} catch (Exception e) {
			e.printStackTrace();
			EditorEnvironment.showException(e);
			return;
		}
	}

	String nodeid;
	private JTextField endTimeView;
	private JTextField sceneNSView;

	public String getNodeId() {
		return nodeid;
	}

	/**
	 * Create the dialog.
	 */
	public SceneBuilder(IMainControl mainControl) {
		super(mainControl);
		setResizable(false);
		setBounds(100, 100, 1293, 683);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPanel.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPanel);
		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.2);
		contentPanel.add(splitPane);
		JPanel panel = new JPanel();
		splitPane.setLeftComponent(panel);
		panel.setLayout(new BorderLayout(0, 0));
		JScrollPane scrollPane = new JScrollPane();
		panel.add(scrollPane, BorderLayout.CENTER);
		tree = new ModelSearchView.TreeModelSearchView();
		tree.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		tree.setRootVisible(false);
		scrollPane.setViewportView(tree);

		JToolBar toolBar = new JToolBar();
		toolBar.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel.add(toolBar, BorderLayout.SOUTH);
		JButton button = new JButton(" 添加目录 ");
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				treeControl.addTreeNode(NodeType.ntDirNode);
			}
		});
		toolBar.add(button);
		button1 = new JButton(" 添加主场景 ");
		button1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				treeControl.addTreeNode(NodeType.ntMainNode);
			}
		});
		toolBar.add(button1);

		JButton button_1 = new JButton(" 添加异常场景 ");
		button_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				treeControl.addTreeNode(NodeType.ntExceptNode);
			}
		});
		toolBar.add(button_1);
		button2 = new JButton(" 删除 ");
		button2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				treeControl.removeTreeNode();
			}
		});

		JButton button_3 = new JButton(" 编辑 ");
		button_3.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				treeControl.renameTreeNode();
			}
		});
		toolBar.add(button_3);
		toolBar.add(button2);
		panel_5 = new JPanel();
		splitPane.setRightComponent(panel_5);
		panel_5.setLayout(new BorderLayout(0, 0));

		titlelabel = new JTextField();
		titlelabel.setEditable(false);
		titlelabel.setAutoscrolls(false);
		titlelabel.setBorder(null);
		titlelabel.setPreferredSize(new Dimension(6, 80));
		titlelabel.setMinimumSize(new Dimension(6, 100));
		titlelabel.setFont(new Font("微软雅黑", Font.PLAIN, 18));
		titlelabel.setText("标题");
		titlelabel.setHorizontalAlignment(SwingConstants.CENTER);
		panel_5.add(titlelabel, BorderLayout.NORTH);

		contentScrollBar = new JScrollPane();
		panel_5.add(contentScrollBar, BorderLayout.CENTER);

		JPanel panel_1 = new JPanel();
		contentScrollBar.setViewportView(panel_1);
		panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.PAGE_AXIS));

		JLabel label = new JLabel("进入条件");
		label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel_1.add(label);

		JScrollPane scrollPane_1 = new JScrollPane();
		panel_1.add(scrollPane_1);

		inputs = new JTextPane();
		inputs.setPreferredSize(new Dimension(6, 100));
		inputs.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		scrollPane_1.setViewportView(inputs);

		JLabel label_1 = new JLabel("操作步骤");
		label_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel_1.add(label_1);

		JScrollPane scrollPane_2 = new JScrollPane();
		panel_1.add(scrollPane_2);
		steps = new JTextPane();
		steps.setPreferredSize(new Dimension(6, 200));
		steps.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		scrollPane_2.setViewportView(steps);

		JLabel label_2 = new JLabel("不变条件");
		label_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel_1.add(label_2);

		JScrollPane scrollPane_3 = new JScrollPane();
		scrollPane_3.setMaximumSize(new Dimension(8888888, 20));
		panel_1.add(scrollPane_3);

		invariances = new JTextPane();
		invariances.setPreferredSize(new Dimension(6, 100));
		invariances.setMaximumSize(new Dimension(12, 12));
		invariances.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		scrollPane_3.setViewportView(invariances);

		JLabel label_3 = new JLabel("输出条件");
		label_3.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel_1.add(label_3);

		JScrollPane scrollPane_4 = new JScrollPane();
		panel_1.add(scrollPane_4);

		outputs = new JTextPane();
		outputs.setPreferredSize(new Dimension(6, 100));
		outputs.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		scrollPane_4.setViewportView(outputs);

		JLabel label_4 = new JLabel("备注");
		label_4.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel_1.add(label_4);

		JScrollPane scrollPane_5 = new JScrollPane();
		scrollPane_5.setPreferredSize(new Dimension(2, 100));
		panel_1.add(scrollPane_5);

		memos = new JTextPane();
		memos.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		scrollPane_5.setViewportView(memos);

		JLabel label_5 = new JLabel("关联控件");
		label_5.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel_1.add(label_5);

		JScrollPane scrollPane_6 = new JScrollPane();
		panel_1.add(scrollPane_6);

		table = new JTable();
		table.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		table.setAutoCreateRowSorter(true);
		table.setFillsViewportHeight(true);
		table.setSurrendersFocusOnKeystroke(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		scrollPane_6.setViewportView(table);

		JPanel panel_4 = new JPanel();
		panel_1.add(panel_4);
		panel_4.setLayout(new BoxLayout(panel_4, BoxLayout.X_AXIS));

		JLabel label_6 = new JLabel(" 完成时间");
		panel_4.add(label_6);
		label_6.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		endTimeView = new JTextField();
		endTimeView.setMaximumSize(new Dimension(150, 2147483647));
		panel_4.add(endTimeView);
		endTimeView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		endTimeView.setColumns(10);

		JLabel label_7 = new JLabel("小时 ");
		panel_4.add(label_7);
		label_7.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		JSeparator separator = new JSeparator();
		panel_4.add(separator);

		JLabel label_8 = new JLabel(" 命名空间");
		panel_4.add(label_8);
		label_8.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		sceneNSView = new JTextField();
		panel_4.add(sceneNSView);
		sceneNSView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		sceneNSView.setColumns(10);

		JPanel buttonPane = new JPanel();
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		buttonPane.setLayout(new BorderLayout(0, 0));

		JPanel panel_2 = new JPanel();
		buttonPane.add(panel_2, BorderLayout.EAST);

		JButton button_2 = new JButton("添加关联控件");
		panel_2.add(button_2);
		button_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		JButton button_4 = new JButton("删除关联控件");
		panel_2.add(button_4);
		button_4.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		JButton button_5 = new JButton("编辑业务流程图");
		button_5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					String casename = treeControl.getCaseName();
					if (casename == null || casename.isEmpty()) {
						EditorEnvironment.showMessage("业务流程图只能在场景节点上使用！");
						return;
					}
					mainControl.openWorkflowRelation(treeControl.getDirName() + "-" + casename,
							FileHelp.removeExt(flowFile.getName()));
				} catch (JSONException e1) {
					e1.printStackTrace();
					EditorEnvironment.showException(e1);
				}
			}
		});
		button_5.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel_2.add(button_5);

		JPanel panel_3 = new JPanel();
		buttonPane.add(panel_3, BorderLayout.CENTER);
		panel_3.setLayout(new BorderLayout(0, 0));
		button_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tableControl.remove();
			}
		});
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String uiid = EditorEnvironment.getUIID(nodeid);
				Result result = ControlSelectDialog.showDialog(uiid);
				if (result.isok && result.data != null) {
					tableControl.add(result.data[0]);
				}
			}
		});

		contentScrollBar.getVerticalScrollBar().setUnitIncrement(30);
		EventHelp.setGlobalMouseWheel(contentScrollBar, contentScrollBar.getVerticalScrollBar());
	}

	public void init(String modelNodeId) {
		this.nodeid = modelNodeId;
		try {
			treeControl.init();
			load();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public static class SceneInfo {
		public String name;
		public HashMap<String, String> data = new HashMap<>();
		public NodeType nodeType = NodeType.ntNone;
		public List<SceneInfo> childs = new ArrayList<>();
	}

	public static void parseJson(JSONArray valueObj, SceneInfo result) throws JSONException {
		for (int i = 0; i < valueObj.length(); i++) {
			JSONObject value = valueObj.getJSONObject(i);
			SceneInfo sceneInfo = new SceneInfo();
			sceneInfo.name = JsonHelp.getString(value, title);
			sceneInfo.nodeType = NodeType.valueOf(JsonHelp.getString(value, nodetype));
			result.childs.add(sceneInfo);
			if (NodeType.valueOf(JsonHelp.getString(value, nodetype)) == NodeType.ntDirNode) {
				sceneInfo.data.put(title, value.has(title) ? JsonHelp.getString(value, title) : "");
				parseJson(value.getJSONArray(child), result);
			} else {
				sceneInfo.data.put(title, value.has(title) ? JsonHelp.getString(value, title) : "");
				sceneInfo.data.put(input, value.has(input) ? JsonHelp.getString(value, input) : "");
				sceneInfo.data.put(step, value.has(step) ? JsonHelp.getString(value, step) : "");
				sceneInfo.data.put(invariance, value.has(invariance) ? JsonHelp.getString(value, invariance) : "");
				sceneInfo.data.put(output, value.has(output) ? JsonHelp.getString(value, output) : "");
				sceneInfo.data.put(memo, value.has(memo) ? JsonHelp.getString(value, memo) : "");
				sceneInfo.data.put(control, value.has(control) ? JsonHelp.getString(value, control) : "");
				sceneInfo.data.put(endtime, value.has(endtime) ? JsonHelp.getString(value, endtime) : "");
				sceneInfo.data.put(scenens, value.has(scenens) ? JsonHelp.getString(value, scenens) : "");
				sceneInfo.data.put(flow, value.has(flow) ? JsonHelp.getString(value, flow) : "");
			}
		}
	}

	public static SceneInfo parseJson(JSONArray valueObj) throws JSONException {
		SceneInfo result = new SceneInfo();
		result.name = "";
		result.nodeType = NodeType.ntDirNode;

		if (valueObj == null)
			return result;

		parseJson(valueObj, result);
		return result;
	}

	public static SceneInfo parseJson(String nodeid) throws Exception {
		File file = EditorEnvironment.getAppFile(nodeid, false);
		return parseJson(file);
	}

	public static SceneInfo parseJson(File appDesignFile) throws Exception {
		SceneInfo result = new SceneInfo();
		if (appDesignFile == null || !appDesignFile.exists()) {
			return result;
		}

		try {
			JSONArray valueObj = (JSONArray) JsonHelp.parseJson(appDesignFile, null);
			result = parseJson(valueObj);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public static void getFlowFiles(SceneInfo info, List<File> files) throws Exception {
		if (info.nodeType != NodeType.ntDirNode) {
			if (info.data.containsKey(flow)) {
				files.add(EditorEnvironment.getFlowRelationFile(info.data.get(flow)));
			}
		}
		for (SceneInfo child : info.childs) {
			getFlowFiles(child, files);
		}
	}

	public static List<File> getFlowFiles(String nodeid) throws Exception {
		List<File> files = new ArrayList<>();
		SceneInfo cases = parseJson(nodeid);
		if (cases == null)
			return files;

		getFlowFiles(cases, files);

		return files;
	}

	@Override
	public void onSave() {
		try {
			save();
			EditorEnvironment.showMessage("保存成功！");
		} catch (Exception e) {
			e.printStackTrace();
			EditorEnvironment.showException(e);
		}
	}

	@Override
	public void onLoad() {
		try {
			load();
		} catch (Exception e) {
			e.printStackTrace();
			EditorEnvironment.showException(e);
		}
	}

	protected File getAppFile() {
		return getAppFile(nodeid, false);
	}

	public static File getAppFile(String nodeid, boolean allowNew) {
		return EditorEnvironment.getAppFile(nodeid, allowNew);
	}

	@Override
	public void onClose() {
		File file = getAppFile();
		if (file != null)
			EditorEnvironment.unlockFile(file);
	}

	@Override
	public void onPublish(HashMap<String, DrawNode> uikeysWorkflowNodes, Object param) throws Exception {

	}

	@Override
	public void onStart(Object param) {
		String nodeid = null;
		if (param instanceof WorkflowNode) {
			WorkflowNode node = (WorkflowNode) param;
			File file = EditorEnvironment.getAppFile(node.id, true);
			if (file != null && file.exists())
				if (!EditorEnvironment.lockFile(file)) {
					EditorEnvironment.showMessage("文件【" + file.getAbsolutePath() + "】已经被其他用户锁定，请稍后再试！");
					return;
				}
			nodeid = node.id;
		} else
			return;

		init(nodeid);
	}

	ChildForm parentForm;
	private JScrollPane contentScrollBar;

	@Override
	public void setParentForm(ChildForm form) {
		parentForm = form;
	}

	@Override
	public ChildForm getParentForm() {
		return parentForm;
	}

	@Override
	public Object getResult() {
		return getAppFile();
	}

	public static Icon error = ImageTreeRender.getIcon(EditorEnvironment.getEditorSourcePath("icons", "warning.png"));
	public static Icon succ = ImageTreeRender.getIcon(EditorEnvironment.getEditorSourcePath("icons", "success.png"));
	public static Icon dir = ImageTreeRender.getIcon(EditorEnvironment.getEditorSourcePath("icons", "folder.png"));

	static {

	}
}
