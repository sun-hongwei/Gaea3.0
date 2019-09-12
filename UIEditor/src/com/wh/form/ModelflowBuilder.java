package com.wh.form;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wh.control.ControlSearchHelp;
import com.wh.control.EditorEnvironment;
import com.wh.control.EditorEnvironment.RegionName;
import com.wh.control.grid.GridCellEditor.ActionResult;
import com.wh.control.grid.design.PropertyTableCellEditor.KeyValue;
import com.wh.control.modelsearch.ModelSearchView;
import com.wh.dialog.IEditNode;
import com.wh.dialog.editor.RunFlowSelectDialog;
import com.wh.dialog.editor.RunFlowSelectDialog.RunFlowResult;
import com.wh.dialog.editor.UISelectDialog;
import com.wh.dialog.editor.UISelectDialog.IAdd;
import com.wh.dialog.editor.UISelectDialog.Result;
import com.wh.dialog.selector.KeyValueSelector;
import com.wh.dialog.selector.KeyValueSelector.IActionListener;
import com.wh.dialog.selector.KeyValueSelector.ICheckValue;
import com.wh.dialog.selector.KeyValueSelector.ModelResult;
import com.wh.draws.DrawCanvas;
import com.wh.draws.DrawCanvas.ChangeType;
import com.wh.draws.DrawCanvas.CopyType;
import com.wh.draws.DrawCanvas.EditMode;
import com.wh.draws.DrawCanvas.IInitPage;
import com.wh.draws.DrawCanvas.ILoad;
import com.wh.draws.DrawCanvas.IOnPageSizeChanged;
import com.wh.draws.DrawCanvas.IScroll;
import com.wh.draws.DrawCanvas.PageConfig;
import com.wh.draws.DrawCanvas.PageSizeMode;
import com.wh.draws.DrawNode;
import com.wh.draws.UINode;
import com.wh.draws.WorkflowCanvas;
import com.wh.draws.WorkflowNode;
import com.wh.draws.WorkflowNode.BeginNode;
import com.wh.draws.WorkflowNode.ChildWorkflowNode;
import com.wh.draws.WorkflowNode.EndNode;
import com.wh.system.tools.FileHelp;
import com.wh.system.tools.JsonHelp;
import com.wh.system.tools.XmlDom;

public class ModelflowBuilder extends ChildForm implements IMainMenuOperation, ISubForm {

	private static final long serialVersionUID = 1L;

	private static final String BeginNode_Title = "起始节点";
	private static final String EndNode_Title = "结束节点";
	private static final String SubNode_Title = "子节点";

	class DrawNodeInfo {
		public String pageName;

	}

	WorkflowCanvas canvas;
	private JTree tree;
	private JScrollBar vScrollBar;
	private JScrollBar hScrollBar;
	private JComboBox<String> pageMode;
	private JComboBox<String> nodetype;

	class TreeInfo {
		public String title;
		public String id;

		public String toString() {
			WorkflowNode node = (WorkflowNode) canvas.getNode(id);
			String name = "";
			if (node != null) {
				name = " [" + node.name + "]";
			}
			return title + name;
		}

		public TreeInfo(String title, String id) {
			this.id = id;
			this.title = title;
		}
	}

	class WorkflowTreeCellRender extends DefaultTreeCellRenderer {

		private static final long serialVersionUID = 7173353751862932053L;

		public WorkflowTreeCellRender() {

		}

		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {
			DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
			if (!(treeNode.getUserObject() instanceof TreeInfo))
				return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

			JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

			// 这里可以根据value或者其他数据判断需要什么样的图标
			TreeInfo treeInfo = (TreeInfo) treeNode.getUserObject();

			WorkflowNode node = (WorkflowNode) canvas.getNode(treeInfo.id);
			if (node == null)
				return label;

			ImageIcon icon = null;
			if (node instanceof ChildWorkflowNode)
				icon = new ImageIcon(UINode.getImage("subworkflow"));
			else
				icon = new ImageIcon(UINode.getImage("workflow"));
			label.setIcon(icon);
			label.updateUI();

			return label;
		}
	}

	public void selectedTreeNode(TreeNode node) {
		TreeNode[] nodes = ((DefaultTreeModel) tree.getModel()).getPathToRoot(node);
		TreePath nodePath = new TreePath(nodes);
		tree.setSelectionPath(nodePath);
		tree.scrollPathToVisible(nodePath);
	}

	protected void selectTreeNode(DrawNode node) {
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
		if (root == null)
			return;

		for (int i = 0; i < root.getChildCount(); i++) {
			DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) root.getChildAt(i);
			if (!(treeNode.getUserObject() instanceof TreeInfo))
				continue;

			TreeInfo info = (TreeInfo) treeNode.getUserObject();
			if (info.id.compareTo(node.id) == 0) {
				selectedTreeNode(treeNode);
				return;
			}
		}
	}

	public void addToTree(String title, String id, DefaultMutableTreeNode root, boolean needSelected) {
		if (id == null || id.isEmpty() || title == null || title.isEmpty())
			return;

		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		if (root == null)
			root = (DefaultMutableTreeNode) model.getRoot();
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(new TreeInfo(title, id));
		model.insertNodeInto(node, root, root.getChildCount());

		if (needSelected) {
			selectedNode(node);
		}
	}

	public TreeInfo getTreeObject(TreePath treePath) {
		if (treePath == null || treePath.getLastPathComponent() == null)
			return null;

		DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();
		if (node.getUserObject() instanceof TreeInfo)
			return (TreeInfo) node.getUserObject();
		else
			return null;

	}

	public Object getTreeSelectObject() {
		return getTreeObject(tree.getSelectionPath());
	}

	protected void reset() {
		canvas.nodes.clear();
		canvas.repaint();
	}

	protected void selectPageMode(PageConfig pageConfig) {
		PageSizeMode pageSize = pageConfig.getCurPageSizeMode();
		pageMode.setSelectedItem(DrawCanvas.pageSizeToString(pageSize));
	}

	protected void initWorkflowRelationCanvas(String workflowName) throws Exception {
		reset();

		File workflowPath = EditorEnvironment.getProjectFile(EditorEnvironment.Workflow_Dir_Name,
				EditorEnvironment.getRelationFileName(workflowName));
		if (workflowPath == null) {
			return;
		}

		canvas.setFile(workflowPath);
		canvas.load(new EditorEnvironment.WorkflowDeserializable(), new IInitPage() {

			@Override
			public void onPage(PageConfig pageConfig) {
				selectPageMode(pageConfig);
			}
		});
	}

	public BeginNode findStartNode() {
		for (DrawNode node : canvas.getNodes()) {
			if (node instanceof EndNode)
				continue;
			if (node instanceof BeginNode)
				return (BeginNode) node;
		}
		return null;
	}

	public EndNode findEndNode() {
		for (DrawNode node : canvas.getNodes()) {
			if (node instanceof EndNode)
				return (EndNode) node;
		}
		return null;
	}

	public void addNode() {
		switch (nodetype.getSelectedItem().toString()) {
		case BeginNode_Title:
			DrawNode startNode = findStartNode();
			if (startNode == null)
				addNode(null, BeginNode.class, BeginNode_Title);
			else {
				canvas.setSelected(startNode);
			}
			break;
		case EndNode_Title:
			DrawNode endNode = findEndNode();
			if (endNode == null)
				addNode(null, EndNode.class, EndNode_Title);
			else {
				canvas.setSelected(endNode);
			}
			break;
		case SubNode_Title: {
			String name = EditorEnvironment.showInputDialog("请输入子关系图节点名称：");
			if (name == null || name.isEmpty())
				return;

			addNode(null, ChildWorkflowNode.class, name);
			break;
		}
		default: {
			String title = EditorEnvironment.showInputDialog("请输入新工作流节点标题：");
			if (title == null || title.isEmpty())
				return;
			int index = 0;
			String name = FileHelp.removeExt(workflowName) + "_defaultName";
			while (canvas.getNode(name + index) != null) {
				index++;
			}
			addNode(name + index, WorkflowNode.class, title);
			break;
		}
		}

		canvas.repaint();
	}

	ControlSearchHelp csh;

	public WorkflowNode addNode(String name, Class<? extends WorkflowNode> c, String title) {
		WorkflowNode node = canvas.add(name, title, c, null, new EditorEnvironment.WorkflowSerializable());
		File file = EditorEnvironment.getProjectFile(EditorEnvironment.Workflow_Dir_Name,
				EditorEnvironment.getNodeFileName(node.id));
		try {
			JsonHelp.saveJson(file, "{}", null);
			node.userData = file.getAbsolutePath();
			addToTree(title, node.id, null, true);
		} catch (Exception e) {
			e.printStackTrace();
			canvas.remove(node);
			return null;
		}
		return node;
	}

	protected String getPageMode() {
		return (String) pageMode.getSelectedItem();
	}

	protected File getNodeData(DrawNode node) {
		return new File((String) node.userData);
	}

	protected boolean allowEditNode(DrawNode node) {
		if (node.title.compareTo(BeginNode_Title) == 0 || node.title.compareTo(EndNode_Title) == 0)
			return false;
		else
			return true;
	}

	protected void onEditNode(DrawNode node) {
		if (onEditNode != null) {
			if (node instanceof ChildWorkflowNode)
				onEditNode.onEditSubWorkflow(this, node);
			else if (node instanceof WorkflowNode)
				onEditNode.onEditUI(this, node);
		}
	}

	public IEditNode onEditNode;

	protected void editDrawNode(DrawNode node) {
		if (node == null)
			return;

		if (node instanceof EndNode || node instanceof BeginNode)
			return;

		if (!allowEditNode(node))
			EditorEnvironment.showMessage(null, "关系图不支持编辑节点", "提示", JOptionPane.WARNING_MESSAGE);
		else {
			onEditNode(node);
		}
	}

	private boolean simpleRemoveNode(DefaultTreeModel model, DefaultMutableTreeNode node, List<String> ids) {
		if (!(node.getUserObject() instanceof TreeInfo))
			return false;

		TreeInfo info = (TreeInfo) node.getUserObject();
		for (String id : ids) {
			if (info.id.compareTo(id) == 0) {
				model.removeNodeFromParent(node);
				ids.remove(id);
				if (ids.size() == 0)
					return true;
				else
					return false;
			}
		}
		return false;
	}

	protected boolean removeTreeNode(DefaultTreeModel model, DefaultMutableTreeNode parent, List<String> id) {
		if (simpleRemoveNode(model, parent, id))
			return true;

		if (!parent.isLeaf()) {
			for (int i = 0; i < parent.getChildCount(); i++) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) parent.getChildAt(i);
				if (simpleRemoveNode(model, node, id))
					return true;

				if (!node.isLeaf()) {
					if (removeTreeNode(model, node, id))
						return true;
				}
			}
		}
		return false;
	}

	protected void remove() {
		if (canvas.getSelected() == null)
			return;

		if (EditorEnvironment.showConfirmDialog("是否删除选定的项目？", "删除",
				JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
			return;

		canvas.remove();
		removeTreeNode(canvas.getSelecteds());
	}

	protected void removeTreeNode(DrawNode[] nodes) {
		if (nodes == null)
			return;

		removeTreeNode(Arrays.asList(nodes));
	}

	protected void removeTreeNode(List<DrawNode> nodes) {
		if (nodes.size() == 0)
			return;

		List<String> ids = new ArrayList<>();
		for (DrawNode node : canvas.getSelecteds()) {
			ids.add(node.id);
		}

		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		removeTreeNode(model, (DefaultMutableTreeNode) model.getRoot(), ids);
	}

	public String workflowName;
	public List<DrawNode> frameModelNodes;

	private JLabel memo;

	private JPanel contentPane;

	protected void keyPressed(KeyEvent e) {
		if (!canvas.hasFocus())
			canvas.keyPressed(e);
	}

	protected void keyReleased(KeyEvent e) {
		if (!canvas.hasFocus())
			canvas.keyReleased(e);
	}

	protected String convertBooleanToString(DefaultTableModel tableModel, int row, int col) {
		boolean b = (boolean) tableModel.getValueAt(row, col);
		return b ? "true" : "false";
	}

	@SuppressWarnings("unchecked")
	protected void editNodeInfo() {
		if (canvas.getSelected() == null) {
			ModelResult result = KeyValueSelector.show(null, mainControl, null, null,
					new Object[][] { { "工作流标题", canvas.getPageConfig().title },
							{ "工作流说明", canvas.getPageConfig().memo }, },
					new Object[] { "属性", "值" }, null, new int[] { 0 }, false);

			DefaultTableModel tableModel = result.isok ? result.model : null;

			if (tableModel != null) {
				canvas.getPageConfig().title = (String) tableModel.getValueAt(0, 1);
				canvas.getPageConfig().memo = (String) tableModel.getValueAt(1, 1);
				canvas.repaint();
				memo.setText(canvas.getPageConfig().memo);
				isEdit = true;

			}
			return;
		}

		WorkflowNode node = (WorkflowNode) canvas.getSelected();
		ModelResult result = KeyValueSelector.show(null, mainControl, new ICheckValue() {

			@Override
			public boolean onCheck(Object[][] orgData, JTable model) {
				String name = (String) model.getValueAt(0, 1);
				if (orgData[0][1] == null || orgData[0][1].toString() == null)
					return true;

				if (name.toLowerCase().compareTo(orgData[0][1].toString().toLowerCase()) == 0)
					return true;
				boolean b;
				try {
					b = !EditorEnvironment.existsModelNodeName(name);
					if (!b) {
						EditorEnvironment.showMessage("名称已经存在，请修改后重试！");
					}
					return b;
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}

		}, null, new Object[][] { { "名称", node.name }, { "标题", node.title }, { "说明", node.memo }, { "id", node.id },
				{ "使用框架", node.useFrame.compareToIgnoreCase("true") == 0 },
				{ "多页模式", node.useTab.compareToIgnoreCase("true") == 0 },
				{ "当前页跳转", node.useCurrentTab.compareToIgnoreCase("true") == 0 },
				{ "显示页头区域", node.useTopRegion.compareToIgnoreCase("true") == 0 },
				{ "显示页脚区域", node.useBottomRegion.compareToIgnoreCase("true") == 0 },
				{ "显示左侧区域", node.useLeftRegion.compareToIgnoreCase("true") == 0 },
				{ "显示右侧区域", node.useRightRegion.compareToIgnoreCase("true") == 0 },
				{ "左侧区域顶部置顶", node.leftRegionTopMax.compareToIgnoreCase("true") == 0 },
				{ "左侧区域底部置底", node.leftRegionBottomMax.compareToIgnoreCase("true") == 0 },
				{ "右侧区域顶部置顶", node.rightRegionTopMax.compareToIgnoreCase("true") == 0 },
				{ "右侧区域底部置底", node.rightRegionBottomMax.compareToIgnoreCase("true") == 0 },
				{ "弹出窗口", node.useDialog.compareToIgnoreCase("true") == 0 }, { "弹出窗口自动尺寸", node.dialogAutoSize },
				{ "显示横向滚动条", node.showHorizontalScrollBar }, { "显示纵向滚动条", node.showVerticalScrollBar },
				{ "运行工作流名称", node.runTaskInfo, "set" }, { "自动应用窗体数据源", node.useFormDataSource },
				{ "显示关闭按钮(仅框架方式)", node.allowClose } }, new Object[] { "属性", "值" }, null, new int[] { 0 },
				new IActionListener() {

					@Override
					public ActionResult onAction(TableModel model, String key, Object value, int row, int col,
							List<Object> selects) {
						try {
							RunFlowResult runResult = RunFlowSelectDialog.show(mainControl);
							KeyValue<String, String> result = runResult.isok ? runResult.runFlowInfo : null;
							if (result == null)
								return new ActionResult(runResult.isok, null);

							return new ActionResult(true, result);
						} catch (Exception e) {
							e.printStackTrace();
						}
						return new ActionResult();
					}
				}, false);

		DefaultTableModel tableModel = result.isok ? result.model : null;

		if (tableModel != null) {
			String name = (String) tableModel.getValueAt(0, 1);
			node.name = name.toLowerCase().trim();
			node.title = (String) tableModel.getValueAt(1, 1);
			node.memo = (String) tableModel.getValueAt(2, 1);
			String oldid = node.id;
			String newid = (String) tableModel.getValueAt(3, 1);
			node.useFrame = convertBooleanToString(tableModel, 4, 1);
			node.useTab = convertBooleanToString(tableModel, 5, 1);
			node.useCurrentTab = convertBooleanToString(tableModel, 6, 1);
			node.useTopRegion = convertBooleanToString(tableModel, 7, 1);
			node.useBottomRegion = convertBooleanToString(tableModel, 8, 1);
			node.useLeftRegion = convertBooleanToString(tableModel, 9, 1);
			node.useRightRegion = convertBooleanToString(tableModel, 10, 1);
			node.leftRegionTopMax = convertBooleanToString(tableModel, 11, 1);
			node.leftRegionBottomMax = convertBooleanToString(tableModel, 12, 1);
			node.rightRegionTopMax = convertBooleanToString(tableModel, 13, 1);
			node.rightRegionBottomMax = convertBooleanToString(tableModel, 14, 1);
			node.useDialog = convertBooleanToString(tableModel, 15, 1);
			node.dialogAutoSize = (boolean) tableModel.getValueAt(16, 1);
			node.showHorizontalScrollBar = (boolean) tableModel.getValueAt(17, 1);
			node.showVerticalScrollBar = (boolean) tableModel.getValueAt(18, 1);
			node.runTaskInfo = (KeyValue<String, String>) tableModel.getValueAt(19, 1);
			node.useFormDataSource = (boolean) tableModel.getValueAt(20, 1);
			node.allowClose = (boolean) tableModel.getValueAt(21, 1);

			File oldFile = EditorEnvironment.getProjectFile(EditorEnvironment.Workflow_Dir_Name,
					EditorEnvironment.getNodeFileName(oldid));
			File newFile = EditorEnvironment.getProjectFile(EditorEnvironment.Workflow_Dir_Name,
					EditorEnvironment.getNodeFileName(newid));
			if (oldid.compareTo(newid) != 0 && (node.getPrevs().size() > 0 || node.getNexts().size() > 0)) {
				EditorEnvironment.showMessage("当前节点已经连线，不允许改变id的值！");
			} else {
				if (!FileHelp.renameFile(oldFile.getAbsolutePath(), newFile.getAbsolutePath())) {
					EditorEnvironment.showMessage(null, "更改id失败，请检查文件系统！", "编辑", JOptionPane.ERROR_MESSAGE);
					return;
				}
				canvas.updateID(oldid, newid);
			}
			refreshWorkflowRelationTree();
			canvas.setSelected(canvas.getNode(node.id));
			isEdit = true;

			save();

		}
	}

	public ModelflowBuilder(IMainControl mainControl) throws Exception {
		super(mainControl);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		needSetPageMode = false;
		setBounds(100, 100, 1039, 701);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.2);
		splitPane.setBackground(Color.WHITE);
		contentPane.add(splitPane, BorderLayout.CENTER);

		tree = new ModelSearchView.TreeModelSearchView(new DefaultTreeModel(new DefaultMutableTreeNode("关系图")));
		tree.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		tree.setRowHeight(24);
		tree.setCellRenderer(new WorkflowTreeCellRender());
		tree.setMinimumSize(new Dimension(100, 0));
		tree.setMaximumSize(new Dimension(0, 0));
		tree.setPreferredSize(new Dimension(210, 16));
		tree.setExpandsSelectedPaths(true);
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				try {
					if (e.getNewLeadSelectionPath() == null)
						return;

					TreeInfo info = getTreeObject(e.getNewLeadSelectionPath());
					if (info == null)
						return;

					canvas.setSelected(canvas.getNode(info.id));
					canvas.repaint();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		JPopupMenu treeMenu = new JPopupMenu();
		addPopup(tree, treeMenu);

		csh = new ControlSearchHelp(tree);

		JMenuItem addMenu = new JMenuItem("添加");
		addMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addNode();
			}
		});
		treeMenu.add(addMenu);

		JMenuItem delMenu = new JMenuItem("删除");
		delMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				remove();
			}
		});
		treeMenu.add(delMenu);

		JMenuItem refreshMenu = new JMenuItem("刷新");
		refreshMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reload();
			}
		});

		JSeparator separator_2 = new JSeparator();
		treeMenu.add(separator_2);
		treeMenu.add(refreshMenu);

		JSeparator separator_1 = new JSeparator();
		treeMenu.add(separator_1);

		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		panel.setPreferredSize(new Dimension(200, 10));
		splitPane.setRightComponent(panel);
		panel.setLayout(new BorderLayout(0, 0));

		JPanel header = new JPanel();
		header.setBackground(Color.WHITE);
		panel.add(header, BorderLayout.NORTH);

		JButton addButton = new JButton("添加");
		addButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		addButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				addNode();
			}
		});

		JButton copyButton = new JButton("复制");
		copyButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		copyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				canvas.copy(false);
			}
		});
		header.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JButton button_4 = new JButton("复制标题");
		button_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				canvas.copySelectNodeToClipboard(CopyType.ctTitle);
			}
		});
		button_4.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		header.add(button_4);
		header.add(copyButton);

		JButton pasteButton = new JButton("粘贴");
		pasteButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		pasteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				canvas.paste();
			}
		});
		header.add(pasteButton);

		JButton button_1 = new JButton("撤销");
		button_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					canvas.getACM().popCommand();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		header.add(button_1);

		JLabel label_2 = new JLabel(" | ");
		label_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		header.add(label_2);

		nodetype = new JComboBox<String>();
		nodetype.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		nodetype.setModel(new DefaultComboBoxModel<String>(
				new String[] { BeginNode_Title, "业务节点", SubNode_Title, EndNode_Title }));
		nodetype.setSelectedIndex(1);
		header.add(nodetype);
		header.add(addButton);

		JButton delButton = new JButton("删除");
		delButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		delButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				remove();
			}
		});
		header.add(delButton);

		JButton editButton = new JButton("修改");
		editButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		editButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (canvas.getSelected() == null)
					return;

				DrawNode node = canvas.getSelected();

				editDrawNode(node);
			}
		});
		header.add(editButton);

		JSeparator separator = new JSeparator();
		separator.setForeground(Color.GRAY);
		separator.setOrientation(SwingConstants.VERTICAL);
		header.add(separator);

		JLabel label_1 = new JLabel("页面：");
		label_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		header.add(label_1);

		pageMode = new JComboBox<>();
		pageMode.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		pageMode.setModel(new DefaultComboBoxModel<String>(DrawCanvas.PAGENAMES));
		header.add(pageMode);

		JLabel label = new JLabel("编辑模式：");
		label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		header.add(label);

		JRadioButton normalRadio = new JRadioButton("正常");
		normalRadio.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		normalRadio.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				canvas.setEditMode(EditMode.emNormal);
			}
		});
		normalRadio.setSelected(true);
		header.add(normalRadio);

		JRadioButton linkRadio = new JRadioButton("连线");
		linkRadio.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		linkRadio.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				canvas.setEditMode(EditMode.emLink);
			}
		});
		header.add(linkRadio);

		JRadioButton disLinkRadio = new JRadioButton("断开");
		disLinkRadio.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		disLinkRadio.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				canvas.setEditMode(EditMode.emRemoveLink);
			}
		});
		header.add(disLinkRadio);

		pageMode.setSelectedIndex(1);

		JPanel workflowContaint = new JPanel();
		workflowContaint.setBackground(Color.WHITE);
		panel.add(workflowContaint, BorderLayout.CENTER);
		workflowContaint.setLayout(new BorderLayout(0, 0));

		canvas = new WorkflowCanvas();
		canvas.onPageSizeChanged = new IOnPageSizeChanged() {

			@Override
			public void onChanged(Point max) {
				hScrollBar.setMaximum(max.x);
				vScrollBar.setMaximum(max.y);
				hScrollBar.setValue(0);
				hScrollBar.setValue(0);
			}
		};
		workflowContaint.add(canvas, BorderLayout.CENTER);

		canvas.addMouseWheelListener(new MouseWheelListener() {

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				Adjustable adj;
				if (!canvas.isCtrlPressed()) {
					adj = vScrollBar;
				} else
					adj = hScrollBar;

				if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
					int totalScrollAmount = e.getUnitsToScroll() * adj.getUnitIncrement();
					adj.setValue(adj.getValue() + totalScrollAmount);
				}
			}
		});

		canvas.nodeEvent = new DrawCanvas.INode() {

			@Override
			public void DoubleClick(DrawNode node) {
				editDrawNode(node);
			}

			@Override
			public void Click(DrawNode node) {
			}

			@Override
			public void onChange(DrawNode[] nodes, ChangeType ct) {
				switch (ct) {
				case ctBackEdited:
					isEdit = true;
					return;
				case ctPaste:
					refreshWorkflowRelationTree();
				case ctRemove:
					removeTreeNode(nodes);
				case ctAdd:
				case ctAddLink:
				case ctMove:
				case ctRemoveLink:
				case ctResize:
				case ctLineChanged:
					isEdit = true;
					break;
				case ctSelecteds:
				case ctSelected:
					if (canvas.isMuiltSelecting() || (nodes != null && nodes.length > 1))
						break;

					selectTreeNode(canvas.getSelected());
					if (nodes == null || nodes.length == 0)
						memo.setText(canvas.getPageConfig().memo);
					else {
						memo.setText(nodes[nodes.length - 1].memo);
					}
					if (memo.getText() != null)
						memo.setToolTipText(memo.getText());
				case ctDeselected:
				case ctMouseRelease:
					break;
				default:
					break;
				}
			}
		};

		hScrollBar = new JScrollBar();
		hScrollBar.setUnitIncrement(10);
		hScrollBar.setMaximum(99999);
		hScrollBar.setOrientation(JScrollBar.HORIZONTAL);
		hScrollBar.addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent e) {
				canvas.setOffset(new Point(-e.getValue(), canvas.getOffset().y));
			}
		});
		workflowContaint.add(hScrollBar, BorderLayout.SOUTH);

		vScrollBar = new JScrollBar();
		vScrollBar.setUnitIncrement(10);
		vScrollBar.setMaximum(99999);
		vScrollBar.addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent e) {
				canvas.setOffset(new Point(canvas.getOffset().x, -e.getValue()));
			}
		});
		workflowContaint.add(vScrollBar, BorderLayout.EAST);

		canvas.onScroll = new IScroll() {

			@Override
			public void onScroll(int x, int y) {
				hScrollBar.setValue(Math.abs(x));
				vScrollBar.setValue(Math.abs(y));
			}
		};

		canvas.getPageConfig().setPageSizeMode(PageSizeMode.psA4V, 0, 0);

		JPanel footer = new JPanel();
		footer.setBorder(new LineBorder(Color.LIGHT_GRAY));
		footer.setBackground(Color.WHITE);
		footer.setLayout(new BorderLayout(0, 0));

		memo = new JLabel("   ");
		memo.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		memo.addMouseListener(new MouseAdapter() {
		});
		memo.setHorizontalAlignment(SwingConstants.CENTER);
		footer.add(memo);
		panel.add(footer, BorderLayout.SOUTH);

		JPanel panel_1 = new JPanel();
		panel_1.setBackground(Color.WHITE);
		footer.add(panel_1, BorderLayout.EAST);

		JButton button_2 = new JButton("关联界面");
		button_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (canvas.getSelected() == null) {
					EditorEnvironment.showMessage("请先选择一个节点！");
					return;
				}
				Result result = UISelectDialog.showDialog(mainControl,
						EditorEnvironment.getUIID(canvas.getSelected().id), new IAdd() {

							@Override
							public String onAdd() {
								String uiid = UUID.randomUUID().toString();
								try {
									EditorEnvironment.updateUI(canvas.getSelected().id, uiid);
								} catch (Exception e) {
									e.printStackTrace();
									return null;
								}
								return uiid;
							}
						});
				if (result == null)
					return;

				try {
					EditorEnvironment.updateUI(canvas.getSelected().id, result.id);
				} catch (Exception e) {
					e.printStackTrace();
					EditorEnvironment.showException(e);
				}
			}
		});

		JButton btnNewButton = new JButton("编辑开发场景");
		btnNewButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (canvas.getSelected() == null)
					return;

				mainControl.openSceneDesign(canvas.getSelected());
			}
		});

		panel_1.add(btnNewButton);
		panel_1.add(button_2);
		JButton button = new JButton("编辑");
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel_1.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editNodeInfo();
			}
		});

		splitPane.setLeftComponent(tree);

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(normalRadio);
		buttonGroup.add(linkRadio);
		buttonGroup.add(disLinkRadio);

		pageMode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String text = getPageMode();
				canvas.getPageConfig().setPageSizeMode(text, 0, 0);
				if (needSetPageMode)
					isEdit = true;
			}
		});

		needSetPageMode = true;

		addComponentListener(new ComponentListener() {

			@Override
			public void componentShown(ComponentEvent e) {
				canvas.fixNodesInPage();
			}

			@Override
			public void componentResized(ComponentEvent e) {
				canvas.fixNodesInPage();
			}

			@Override
			public void componentMoved(ComponentEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void componentHidden(ComponentEvent e) {
				// TODO Auto-generated method stub

			}
		});

		splitPane.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				splitPane.setDividerLocation(splitPane.getResizeWeight());
				splitPane.setResizeWeight(splitPane.getResizeWeight());
			}
		});
	}

	@Override
	public void onSave() {
		if (!isEdit)
			return;
		save();
	}

	public void save() {
		try {
			if (frameModelNodes != null && frameModelNodes.size() > 0) {
				for (DrawNode drawNode : canvas.getNodes()) {
					EditorEnvironment.saveFrameModelNode(drawNode);
				}
				isEdit = false;
				return;
			}

			File f = EditorEnvironment.getProjectFile(EditorEnvironment.Workflow_Dir_Name, workflowName);

			if (f == null)
				return;

			canvas.setFile(f);
			canvas.save();
			isEdit = false;

			EditorEnvironment.lockFile(canvas.getFile());
		} catch (Exception e) {
			e.printStackTrace();
			EditorEnvironment.showMessage(this, e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void selectedNode(DefaultMutableTreeNode node) {
		TreeNode[] nodes = ((DefaultTreeModel) tree.getModel()).getPathToRoot(node);
		TreePath nodePath = new TreePath(nodes);
		tree.setSelectionPath(nodePath);
		tree.scrollPathToVisible(nodePath);
	}

	public void initTree() {
		for (DrawNode node : canvas.getNodes()) {
			addToTree(node.title, node.id, null, false);
		}
	}

	boolean needSetPageMode = true;

	public void reload() {
		if (canvas.getWidth() == 0 || canvas.getHeight() == 0) {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					reload();
				}
			});
			return;
		}

		if (frameModelNodes != null && frameModelNodes.size() > 0) {
			canvas.setNodes(frameModelNodes);
		} else if (workflowName != null && !workflowName.isEmpty()) {
			File path = EditorEnvironment.getProjectFile(EditorEnvironment.Workflow_Dir_Name, workflowName);
			canvas.setFile(path);
			try {
				canvas.load(new EditorEnvironment.WorkflowDeserializable(), new IInitPage() {

					@Override
					public void onPage(PageConfig pageConfig) {
						pageConfig.setPageSizeMode();
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}

		selectPageMode(canvas.getPageConfig());
		refreshWorkflowRelationTree();
		isEdit = false;
	}

	public void refreshWorkflowRelationTree() {
		DefaultTreeModel model = new DefaultTreeModel(new DefaultMutableTreeNode("关系图"));
		tree.setModel(model);

		initTree();

		EditorEnvironment.expandAll(tree, null, true);

		DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
		if (root != null && !root.isLeaf()) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) root.getLastChild();
			if (node == null)
				node = root;
			selectedNode(node);
		}

		csh.init();
	}

	@Override
	public void onLoad() {
		if (needPrompt())
			if (EditorEnvironment.showConfirmDialog("当前未保存的工作都将丢失，是否关闭当前窗口？", "关闭",
					JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
				return;

		reload();
	}

	public boolean isMain() {
		String name = EditorEnvironment.getMainModelRelationFileName();
		if (workflowName.compareTo(name) == 0)
			return true;
		return false;
	}

	@Override
	protected boolean allowQuit() {
		if (!isMain())
			return true;

		boolean b = mainControl.getForms().length <= 1;
		if (!b) {
			EditorEnvironment.showMessage(null, "除主设计器之外当前还有未关闭的其他设计器，请全部关闭后再试！", "关闭",
					JOptionPane.INFORMATION_MESSAGE);
		}

		return b;
	}

	@Override
	public void onClose() {
		if (frameModelNodes != null && frameModelNodes.size() > 0) {
			EditorEnvironment.unlockFile(canvas.getFile());
		} else if (canvas.getFile() != null)
			EditorEnvironment.unlockFile(canvas.getFile());

		dispose();
	}

	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}

			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}

			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}

	public static void publish(String title, Integer dpi, WorkflowNode node, Collection<DrawNode> nodes)
			throws Exception {
		publish(title, node, nodes, dpi, null, false, null, new HashMap<>());
	}

	/**
	 * 发布一个节点
	 * 
	 * @param title
	 *            ui发布时的错误标题
	 * @param node
	 *            要发布的节点
	 * @param nodes
	 *            发布节点所在模块图包含的所有节点
	 * @param filenameInfos
	 *            发布的主文件需要的ui名字列表对象，可以为null，为null表示增量发布，发布单个节点必须为null
	 * @param needSetMain
	 *            是否设置主uiid， true设置，其他不设置，发布单个节点必须为false
	 * @param mainid
	 *            主uiid，单个发布为null
	 * @param uiKeysWorkflowNodes
	 *            节点的查重map，必须不能为null
	 * @throws Exception
	 */
	public static void publish(String title, WorkflowNode node, Collection<DrawNode> nodes, Integer dpi,
			JSONObject filenameInfos, boolean needSetMain, String mainid, HashMap<String, DrawNode> uiKeysWorkflowNodes)
			throws Exception {
		if (node instanceof BeginNode) {
			return;
		}
		if (node instanceof EndNode) {
			return;
		}

		if (node instanceof ChildWorkflowNode) {
			File subFile = EditorEnvironment.getChildModelRelationFile(node.id, false);
			publish(subFile, dpi, filenameInfos, uiKeysWorkflowNodes);
			return;
		}

		if (node instanceof WorkflowNode) {
			File publishFile = UIBuilder.publish(title, node, dpi, EditorEnvironment.createDrawNodeHashMap(nodes),
					uiKeysWorkflowNodes);
			if (needSetMain && mainid != null && node.id.compareToIgnoreCase(mainid) == 0) {
				filenameInfos.put("main", publishFile.getName());
				needSetMain = false;
			}

			if (publishFile != null && filenameInfos != null)
				filenameInfos.getJSONArray("names").put(publishFile.getName());
		}
	}

	/**
	 * 发布一个模块图的所有节点文件
	 * 
	 * @param workflowRelationFile
	 *            模块图所在的文件
	 * @param filenameInfos
	 *            主信息对象
	 * @param uiKeysWorkflowNodes
	 *            节点的查重map，必须不能为null
	 * @throws Exception
	 */
	public static void publish(File workflowRelationFile, Integer dpi, JSONObject filenameInfos,
			HashMap<String, DrawNode> uiKeysWorkflowNodes) throws Exception {
		if (workflowRelationFile == null)
			return;

		boolean needSetMain = EditorEnvironment.isMainModelRelation(workflowRelationFile);
		String[] tmps = new String[] { null };
		String workflowRelationTitle = EditorEnvironment.getModelRelationName(workflowRelationFile);
		List<DrawNode> nodes = DrawCanvas.loadNodes(null, workflowRelationFile,
				new EditorEnvironment.WorkflowDeserializable(), new ILoad() {

					@Override
					public void onBeforeLoad(JSONObject data, Object param) throws JSONException {
						String[] tmps = (String[]) param;
						String tmp = DrawCanvas.getTitle(data);
						if (tmp != null && !tmp.isEmpty())
							tmps[0] = tmp;
					}
				}, null, tmps);

		if (nodes == null || nodes.size() == 0)
			return;

		if (tmps[0] != null) {
			workflowRelationTitle = tmps[0];
		}

		String mainid = null;
		if (needSetMain) {
			for (DrawNode tmp : nodes) {
				WorkflowNode node = (WorkflowNode) tmp;
				if (node instanceof BeginNode) {
					mainid = node.getNexts().get(0);
					break;
				}
			}
		}

		for (DrawNode modelNode : nodes) {
			publish(workflowRelationTitle, (WorkflowNode) modelNode, nodes, dpi, filenameInfos, needSetMain, mainid,
					uiKeysWorkflowNodes);
		}

	}

	protected static void copyDir(File root, String dirName) throws IOException {
		File dir = new File(root, dirName);
		if (dir.exists())
			if (!FileHelp.delDir(dir)) {
				EditorEnvironment.showMessage(null, "失败，无法删除目录：" + dir.getAbsolutePath(), "失败",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

		if (!dir.mkdirs()) {
			EditorEnvironment.showMessage(null, "失败，无法建立目录：" + dir.getAbsolutePath(), "失败", JOptionPane.ERROR_MESSAGE);
			return;
		}

		File source = EditorEnvironment.getProjectPath(EditorEnvironment.getCurrentProjectName(), dirName);
		File dest = dir;

		if (!FileHelp.copyFilesTo(source, dest)) {
			EditorEnvironment.showMessage(null,
					"拷贝目录:" + source.getAbsolutePath() + "到:" + dest.getAbsolutePath() + "失败，请检查文件系统是否异常！", "发布失败",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

	}

	@Override
	public void onPublish(HashMap<String, DrawNode> uiKeysWorkflowNodes, Object param) throws Exception {
		publish(mainControl, uiKeysWorkflowNodes);
	}

	public static void publish(IMainControl mainControl, HashMap<String, DrawNode> uiKeysWorkflowNodes)
			throws Exception {
		File mainWorkflowRelationFile = EditorEnvironment.getProjectFile(EditorEnvironment.Workflow_Dir_Name,
				EditorEnvironment.getRelationFileName(EditorEnvironment.Main_Workflow_Relation_FileName));
		File webPath = EditorEnvironment.getPublishWebPath();
		if (webPath == null) {
			EditorEnvironment.showMessage(null, "请先设置web根目录！", "发布", JOptionPane.WARNING_MESSAGE);
			return;
		}

		File uiDir = new File(webPath, EditorEnvironment.Publish_UI_Dir_Name);
		if (uiDir.exists())
			if (!FileHelp.delDir(uiDir)) {
				EditorEnvironment.showMessage(null, "发布失败，无法删除目录：" + uiDir.getAbsolutePath(), "发布失败",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

		if (!uiDir.mkdirs()) {
			EditorEnvironment.showMessage(null, "发布失败，无法建立目录：" + uiDir.getAbsolutePath(), "发布失败",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		copyDir(webPath, EditorEnvironment.Menu_Dir_Path);
		copyDir(webPath, EditorEnvironment.Tree_Dir_Path);
		copyDir(webPath, EditorEnvironment.Download_Dir_Path);

		File uiFileNamesFile = EditorEnvironment.getPublishWebFile(EditorEnvironment.Publish_UI_Dir_Name,
				EditorEnvironment.Publish_UI_Names_FileName);
		JSONObject filenameInfo = new JSONObject();
		filenameInfo.put("names", new JSONArray());

		publish(mainWorkflowRelationFile, mainControl.getSelectDPI(), filenameInfo, uiKeysWorkflowNodes);

		for (String uiid : uiKeysWorkflowNodes.keySet()) {
			WorkflowNode node = (WorkflowNode) uiKeysWorkflowNodes.get(uiid);
			if (node == null) {
				File file = EditorEnvironment.getUIFileForUIID(uiid);
				UIBuilder.publish("", mainControl.getSelectDPI(), file, uiKeysWorkflowNodes);
			}
		}

		UIBuilder.publish("页头", EditorEnvironment.getFrameModelNode(RegionName.rnTop), mainControl.getSelectDPI(),
				new HashMap<>(), uiKeysWorkflowNodes);
		UIBuilder.publish("页脚", EditorEnvironment.getFrameModelNode(RegionName.rnBottom), mainControl.getSelectDPI(),
				new HashMap<>(), uiKeysWorkflowNodes);
		UIBuilder.publish("左部", EditorEnvironment.getFrameModelNode(RegionName.rnLeft), mainControl.getSelectDPI(),
				new HashMap<>(), uiKeysWorkflowNodes);
		UIBuilder.publish("右部", EditorEnvironment.getFrameModelNode(RegionName.rnRight), mainControl.getSelectDPI(),
				new HashMap<>(), uiKeysWorkflowNodes);

		JsonHelp.saveJson(uiFileNamesFile, filenameInfo, null);

		if (!filenameInfo.has("main")) {
			EditorEnvironment.showMessage(null, "未发现系统入口节点，需要在index.html文件中手动设置系统入口节点！", "发布",
					JOptionPane.WARNING_MESSAGE);
		}
	}

	public void dispatchModelflows(File dispatchPath, List<DrawNode> nodes, File modelflowRelationFile,
			boolean isLinked, boolean first) throws Exception {
		if (nodes == null) {
			nodes = DrawCanvas.loadNodes(null, modelflowRelationFile, new EditorEnvironment.WorkflowDeserializable(),
					new ILoad() {

						@Override
						public void onBeforeLoad(JSONObject data, Object param) throws JSONException {
						}
					}, null, null);
			if (first)
				EditorEnvironment.copyModelRelation(modelflowRelationFile, dispatchPath,
						EditorEnvironment.getMainModelRelationFileName(), null, isLinked);
		} else {
			if (first)
				EditorEnvironment.copyModelRelation(modelflowRelationFile, dispatchPath,
						EditorEnvironment.getMainModelRelationFileName(), nodes, isLinked);
		}

		for (DrawNode tmp : nodes) {
			WorkflowNode node = (WorkflowNode) tmp;
			if (node instanceof BeginNode) {
				continue;
			}
			if (node instanceof EndNode) {
				continue;
			}

			EditorEnvironment.copyNode(node, dispatchPath, isLinked);
			if (node instanceof ChildWorkflowNode) {
				File subFile = EditorEnvironment.getChildModelRelationFile(node.id, false);
				dispatchModelflows(dispatchPath, null, subFile, isLinked, false);
				continue;
			}
		}

	}

	protected List<DrawNode> fillNextNodes(DrawNode node, HashMap<String, DrawNode> nodes) {
		List<DrawNode> result = new ArrayList<>();
		result.add(node);
		for (String id : node.getNexts()) {
			if (id == null || id.isEmpty())
				continue;

			if (!nodes.containsKey(id))
				continue;

			DrawNode nextNode = nodes.get(id);
			result.add(nextNode);

			List<DrawNode> tmps = fillNextNodes(nextNode, nodes);
			if (tmps.size() > 0)
				result.addAll(tmps);
		}

		return result;
	}

	public File dispatchWorkflows(String dispatchName, boolean isLinked, boolean newProject, boolean includeImage,
			boolean includeReport, boolean includeAuth, boolean includeConfig, boolean includeDataSource,
			boolean includeUserJs, boolean includeMasterData, boolean onlySelectedModel) throws Exception {

		File workflowRelationFile = canvas.canvasFile;
		DrawNode node = EditorEnvironment.getChildModelNodeFromFile(workflowRelationFile);
		String pid = "main";
		if (node != null)
			pid = node.id;

		File dispatchPath = EditorEnvironment.getBasePath(EditorEnvironment.Workflow_Dispatch_Dir_Name, dispatchName);
		if (dispatchPath.exists()) {
			if (EditorEnvironment.showConfirmDialog("项目【" + dispatchPath.getAbsolutePath() + "】已经存在，是否继续", "分发项目",
					JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION)
				return null;
			if (!FileHelp.delDir(dispatchPath))
				throw new IOException("del dir[" + dispatchPath.getAbsolutePath() + "] is fail!");

		}

		if (!dispatchPath.mkdirs())
			throw new IOException("mkdir[" + dispatchPath.getAbsolutePath() + "] is fail!");

		File configFile = EditorEnvironment.getProjectFile(null, EditorEnvironment.Project_ConfigFileName);
		if (configFile.exists()) {
			File destConfigFile = new File(dispatchPath, EditorEnvironment.Project_ConfigFileName);
			FileHelp.copyFileTo(configFile, destConfigFile, isLinked);
			XmlDom xmlDom = new XmlDom(destConfigFile.getAbsolutePath());
			xmlDom.Load();

			xmlDom.SetValue("/root/dispatch", "relationid", FileHelp.removeExt(workflowRelationFile.getName()));
			xmlDom.SetValue("/root/dispatch", "parentid", pid);

			xmlDom.Save();
		}

		if (includeImage) {
			FileHelp.copyFilesTo(EditorEnvironment.getProjectPath(EditorEnvironment.Image_Resource_Path),
					new File(dispatchPath, EditorEnvironment.Image_Resource_Path), isLinked);
		}

		if (includeReport) {
			FileHelp.copyFilesTo(EditorEnvironment.getProjectPath(EditorEnvironment.Report_Dir_Path),
					new File(dispatchPath, EditorEnvironment.Report_Dir_Path), isLinked);
		}

		if (includeAuth) {
			FileHelp.copyFilesTo(EditorEnvironment.getProjectPath(EditorEnvironment.Remote_Dir_Name),
					new File(dispatchPath, EditorEnvironment.Remote_Dir_Name), isLinked);
		}

		if (includeConfig) {
			FileHelp.copyFilesTo(EditorEnvironment.getProjectPath(EditorEnvironment.Config_Dir_Path),
					new File(dispatchPath, EditorEnvironment.Config_Dir_Path), isLinked);
		}

		if (includeDataSource) {
			FileHelp.copyFilesTo(EditorEnvironment.getProjectPath(EditorEnvironment.DataSource_Dir_Name),
					new File(dispatchPath, EditorEnvironment.DataSource_Dir_Name), isLinked);
		}

		if (includeUserJs) {
			FileHelp.copyFilesTo(EditorEnvironment.getProjectPath(EditorEnvironment.User_JavaScript_Dir_Path),
					new File(dispatchPath, EditorEnvironment.User_JavaScript_Dir_Path), isLinked);
		}

		if (includeMasterData) {
			FileHelp.copyFilesTo(EditorEnvironment.getProjectPath(EditorEnvironment.MasterData_Dir_Name),
					new File(dispatchPath, EditorEnvironment.MasterData_Dir_Name), isLinked);
		}

		dispatchModelflows(dispatchPath, onlySelectedModel ? canvas.getSelecteds() : null, workflowRelationFile,
				isLinked, true);

		return dispatchPath;
	}

	public static class StartInfo {
		public DrawNode[] nodes;
		public String name;

		public StartInfo(DrawNode[] nodes, String name) {
			this.nodes = nodes;
			this.name = name;
		}
	}

	@Override
	public void onStart(Object param) {
		if (param instanceof String) {
			this.workflowName = param.toString();
		} else if (param instanceof String[]) {
			String[] values = (String[]) param;
			this.workflowName = values[0];
		} else if (param instanceof StartInfo) {
			StartInfo info = (StartInfo) param;
			this.workflowName = info.name;
			this.frameModelNodes = Arrays.asList(info.nodes);
		} else {
			EditorEnvironment.showMessage(null, "参数格式错误！", "打开", JOptionPane.WARNING_MESSAGE);
			return;
		}

		onLoad();
	}

	ChildForm parentForm;

	@Override
	public void setParentForm(ChildForm form) {
		this.parentForm = form;
	}

	@Override
	public ChildForm getParentForm() {
		return parentForm;
	}

	@Override
	public Object getResult() {
		return new String[] { workflowName, "true" };
	}
}
