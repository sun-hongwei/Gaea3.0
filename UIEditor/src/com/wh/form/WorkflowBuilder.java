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
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
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
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import com.sunking.swing.JFontDialog;
import com.wh.control.ControlTreeManager;
import com.wh.control.EditorEnvironment;
import com.wh.control.modelsearch.ModelSearchView;
import com.wh.dialog.editor.ModelflowSelectDialog;
import com.wh.dialog.editor.ModelflowSelectDialog.Result;
import com.wh.dialog.editor.WorkflowSelectDialog;
import com.wh.dialog.input.TextInput;
import com.wh.dialog.selector.KeyValueSelector;
import com.wh.dialog.selector.KeyValueSelector.ICheckValue;
import com.wh.dialog.selector.KeyValueSelector.ModelResult;
import com.wh.draws.DrawCanvas;
import com.wh.draws.DrawCanvas.ChangeType;
import com.wh.draws.DrawCanvas.EditMode;
import com.wh.draws.DrawCanvas.IDataSerializable;
import com.wh.draws.DrawCanvas.IInitPage;
import com.wh.draws.DrawCanvas.IOnPageSizeChanged;
import com.wh.draws.DrawCanvas.IScroll;
import com.wh.draws.DrawCanvas.PageConfig;
import com.wh.draws.DrawCanvas.PageSizeMode;
import com.wh.draws.DrawNode;
import com.wh.draws.FlowCanvas;
import com.wh.draws.FlowNode;
import com.wh.draws.FlowNode.ANDNode;
import com.wh.draws.FlowNode.BeginNode;
import com.wh.draws.FlowNode.ChildFlowNode;
import com.wh.draws.FlowNode.ConditionNode;
import com.wh.draws.FlowNode.EndNode;
import com.wh.draws.FlowNode.IFNode;
import com.wh.draws.FlowNode.LabelNode;
import com.wh.draws.FlowNode.ORNode;
import com.wh.draws.FlowNode.ProcessNode;
import com.wh.draws.FlowNode.SwitchNode;
import com.wh.draws.FlowNode.XORNode;
import com.wh.system.tools.FileHelp;

public class WorkflowBuilder extends ChildForm implements IMainMenuOperation, ISubForm {

	private static final long serialVersionUID = 1L;

	class DrawNodeInfo {
		public String pageName;

	}

	FlowCanvas canvas = new FlowCanvas();
	private JTree tree;
	private JScrollBar vScrollBar;
	private JScrollBar hScrollBar;
	private JComboBox<String> pageMode;
	private JComboBox<String> nodetype;
	private ControlTreeManager ctm;

	protected void reset() {
		canvas.nodes.clear();
		canvas.repaint();
		ctm.refreshTree();
	}

	protected void selectPageMode(PageConfig pageConfig) {
		pageMode.setSelectedItem(pageConfig.getCurPageSizeMode());
	}

	protected void initWorkflowRelationCanvas(String workflowName) throws Exception {
		reset();

		File workflowPath = EditorEnvironment.getProjectFile(EditorEnvironment.Flow_Dir_Name,
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
		case FlowNode.BeginNode_Title:
			DrawNode startNode = findStartNode();
			if (startNode == null)
				addNode(BeginNode.class, FlowNode.BeginNode_Title);
			else {
				canvas.setSelected(startNode);
				canvas.repaint();
			}
			break;
		case FlowNode.EndNode_Title:
			DrawNode endNode = findEndNode();
			if (endNode == null)
				addNode(EndNode.class, FlowNode.EndNode_Title);
			else {
				canvas.setSelected(endNode);
				canvas.repaint();
			}
			break;
		case FlowNode.XORNode_Title:
			addNode(XORNode.class, FlowNode.XORNode_Title);
			canvas.repaint();
			break;
		case FlowNode.ORNode_Title:
			addNode(ORNode.class, FlowNode.ORNode_Title);
			canvas.repaint();
			break;
		case FlowNode.ANDNode_Title:
			addNode(ANDNode.class, FlowNode.ANDNode_Title);
			canvas.repaint();
			break;
		case FlowNode.IFNode_Title: {
			String name = TextInput.showDialog("请输入判定条件：");
			if (name == null || name.isEmpty())
				return;

			addNode(IFNode.class, name);
			canvas.repaint();
			break;
		}
		case FlowNode.ExceptionNode_Title: {
			String name = TextInput.showDialog("请输入异常场景名称：");
			if (name == null || name.isEmpty())
				return;

			addNode(SwitchNode.class, name);
			canvas.repaint();
			break;
		}
		case FlowNode.ConditionNode_Title: {
			String name = TextInput.showDialog("请输入条件：");
			if (name == null || name.isEmpty())
				return;

			addNode(ConditionNode.class, name);
			canvas.repaint();
			break;
		}
		case FlowNode.SubNode_Title: {
			String name;
			if (EditorEnvironment.showConfirmDialog("是否新建子流程？", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				name = TextInput.showDialog("请输入子流程节点名称：");
				if (name == null || name.isEmpty())
					return;
			} else {
				String[] values = WorkflowSelectDialog.showDialog(mainControl, null, true);
				if (values == null)
					return;

				ChildFlowNode node = (ChildFlowNode) addNode(ChildFlowNode.class, values[1]);
				node.relationName = values[0];
			}
			break;
		}
		case FlowNode.LabelNode_Title: {
			String name = TextInput.showDialog("请输入标签内容：");
			if (name == null || name.isEmpty())
				return;

			addNode(LabelNode.class, name);
			break;
		}
		default: {
			String name = TextInput.showDialog("请输入新工作流节点名称：");
			if (name == null || name.isEmpty())
				return;

			addNode(ProcessNode.class, name);
			break;
		}
		}
	}

	IDataSerializable dataserializable = new IDataSerializable() {

		@Override
		public String save(Object userData) {
			return null;
		}

		@Override
		public DrawNode newDrawNode(Object userdata) {
			return null;
		}

		@Override
		public Object load(String value) {
			return null;
		}

		@Override
		public void initDrawNode(DrawNode node) {
			// TODO Auto-generated method stub

		}
	};

	public FlowNode addNode(Class<? extends FlowNode> c, String title) {
		FlowNode node = canvas.add(title, c, null, dataserializable);
		ctm.refreshTree();
		canvas.repaint();
		return node;
	}

	protected String getPageMode() {
		return (String) pageMode.getSelectedItem();
	}

	protected File getNodeData(DrawNode node) {
		return new File((String) node.userData);
	}

	protected boolean allowEditNode(DrawNode node) {
		if (node instanceof ChildFlowNode || node instanceof IFNode || node instanceof ProcessNode
				|| node instanceof LabelNode || node instanceof SwitchNode || node instanceof ConditionNode)
			return true;
		else
			return false;
	}

	protected void onEditNode(DrawNode node) {
		if (node instanceof ChildFlowNode) {
			ChildFlowNode childFlowNode = (ChildFlowNode) node;
			File file = EditorEnvironment.getFlowRelationFile(childFlowNode.relationName);
			if (file.exists())
				mainControl.openSubWorkflowRelation(childFlowNode);
			else {
				if (EditorEnvironment.showConfirmDialog("未发现子流程，是否新建？",
						JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
					return;

				FlowCanvas flowCanvas = new FlowCanvas();
				flowCanvas.setFile(file);
				PageConfig pageConfig = flowCanvas.getPageConfig();
				pageConfig.title = title;
				pageConfig.memo = node.memo;
				try {
					flowCanvas.save();
					isEdit = true;
				} catch (Exception e) {
					e.printStackTrace();
					EditorEnvironment.showException(e);
				}
			}
		} else if (node instanceof IFNode || node instanceof ProcessNode || node instanceof LabelNode
				|| node instanceof SwitchNode || node instanceof ConditionNode) {
			String name = TextInput.showDialog("请输入新内容：");
			if (name == null || name.isEmpty())
				return;

			node.title = name;
			isEdit = true;
			canvas.repaint();
		}
	}

	protected void setChildFlowNode(ChildFlowNode node) {
		ChildFlowNode flowNode = (ChildFlowNode) node;
		String[] values = WorkflowSelectDialog.showDialog(mainControl, flowNode.relationName, true);
		if (values != null) {
			node.title = values[1];
			node.relationName = values[0];
			isEdit = true;
		}
	}

	protected void editDrawNode(DrawNode node) {
		if (node == null)
			return;

		if (node instanceof EndNode || node instanceof BeginNode)
			return;

		if (!allowEditNode(node)) {
			EditorEnvironment.showMessage(null, "流程图不支持编辑节点", "提示", JOptionPane.WARNING_MESSAGE);
		} else {
			onEditNode(node);
		}
	}

	protected void remove() {
		if (canvas.getSelected() == null)
			return;

		if (EditorEnvironment.showConfirmDialog("是否删除选定的项目？", "删除",
				JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
			return;

		removeNoHint();
	}

	protected void removeNoHint() {
		if (canvas.getSelected() == null)
			return;

		List<String> id = new ArrayList<>();
		for (DrawNode node : canvas.getSelecteds()) {
			id.add(node.id);
		}
		canvas.remove();
		ctm.refreshTree();
	}

	public String workflowName;
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

	public WorkflowBuilder(IMainControl mainControl) throws Exception {
		super(mainControl);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		needSetPageMode = false;
		setBounds(100, 100, 1260, 1058);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.2);
		contentPane.add(splitPane, BorderLayout.CENTER);

		tree = new ModelSearchView.TreeModelSearchView(new DefaultTreeModel(new DefaultMutableTreeNode("流程")));
		tree.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		tree.setRowHeight(24);
		tree.setCellRenderer(new ControlTreeManager.WorkflowTreeCellRender(canvas));
		tree.setExpandsSelectedPaths(true);
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				if (e.getNewLeadSelectionPath() != null) {
					ctm.selectCanvasNode((TreeNode) e.getNewLeadSelectionPath().getLastPathComponent());
				}
			}
		});

		JPopupMenu treeMenu = new JPopupMenu();
		addPopup(tree, treeMenu);

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
		panel.setPreferredSize(new Dimension(200, 10));
		splitPane.setRightComponent(panel);
		panel.setLayout(new BorderLayout(0, 0));

		JPanel header = new JPanel();
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
		nodetype.setModel(new DefaultComboBoxModel<String>(new String[] { FlowNode.BeginNode_Title,
				FlowNode.EndNode_Title, FlowNode.ProcessNode_Title, FlowNode.SubNode_Title, FlowNode.LabelNode_Title,
				FlowNode.IFNode_Title, FlowNode.ExceptionNode_Title, FlowNode.XORNode_Title, FlowNode.ORNode_Title,
				FlowNode.ANDNode_Title, FlowNode.ConditionNode_Title }));
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

				if (node instanceof ChildFlowNode) {
					String name = TextInput.showDialog("请输入新标题：");
					if (name == null || name.isEmpty())
						return;

					node.title = name;
					canvas.repaint();
				} else
					editDrawNode(node);
			}
		});
		header.add(editButton);

		JSeparator separator = new JSeparator();
		separator.setForeground(Color.GRAY);
		separator.setOrientation(SwingConstants.VERTICAL);
		header.add(separator);

		JButton button_3 = new JButton("替换子流程");
		button_3.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (canvas.getSelected() == null)
					return;

				if (!(canvas.getSelected() instanceof ChildFlowNode)) {
					EditorEnvironment.showMessage("请先选择一个类型为【子流程】的节点！");
					return;
				}

				setChildFlowNode((ChildFlowNode) canvas.getSelected());
			}
		});
		header.add(button_3);

		JLabel label_4 = new JLabel(" | ");
		label_4.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		header.add(label_4);

		JLabel label_1 = new JLabel("页面：");
		label_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		header.add(label_1);

		pageMode = new JComboBox<>();
		pageMode.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		pageMode.setModel(new DefaultComboBoxModel<String>(DrawCanvas.PAGENAMES));
		header.add(pageMode);

		JButton btnFont = new JButton("Font");
		btnFont.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		btnFont.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (canvas.getSelected() == null) {
					EditorEnvironment.showMessage(null, "请先选择节点！");
					return;
				}

				Font font = canvas.getSelected().font;

				Font result = JFontDialog.showDialog(getFrame(), "字体选择", true, font);
				if (result != null) {
					for (DrawNode node : canvas.getSelecteds()) {
						node.font = new Font(result.getFontName(), result.getStyle(), result.getSize());
					}
					canvas.repaint();
				}
			}
		});
		header.add(btnFont);

		JLabel label_3 = new JLabel(" | ");
		label_3.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		header.add(label_3);

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
		panel.add(workflowContaint, BorderLayout.CENTER);
		workflowContaint.setLayout(new BorderLayout(0, 0));

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
				ctm.selectTreeNode(node);
			}

			@Override
			public void onChange(DrawNode[] nodes, ChangeType ct) {
				switch (ct) {
				case ctBackEdited:
					isEdit = true;
					return;
				case ctPaste:
					ctm.refreshTree();
				case ctAdd:
				case ctAddLink:
				case ctMove:
				case ctRemove:
				case ctRemoveLink:
				case ctResize:
				case ctLineChanged:
					isEdit = true;
					break;
				case ctSelected:
				case ctSelecteds:
					if (nodes == null || nodes.length == 0)
						memo.setText(canvas.getPageConfig().memo);
					else {
						memo.setText(nodes[nodes.length - 1].memo);
					}
					break;
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
		footer.setLayout(new BorderLayout(0, 0));

		memo = new JLabel("   ");
		memo.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		memo.setHorizontalAlignment(SwingConstants.CENTER);
		footer.add(memo);
		panel.add(footer, BorderLayout.SOUTH);

		JPanel panel_1 = new JPanel();
		footer.add(panel_1, BorderLayout.EAST);

		JButton button_2 = new JButton("关联工作流节点");
		button_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (canvas.getSelected() == null) {
					EditorEnvironment.showMessage(null, "请先选择一个功能节点！");
					return;
				}

				if (!(canvas.getSelected() instanceof ProcessNode)) {
					EditorEnvironment.showMessage(null, "选择的节点不是一个功能节点，请重新选择后重试！");
					return;
				}

				Result result = ModelflowSelectDialog.showDialog(mainControl, canvas.getSelected().id, null);
				if (result != null) {
					DrawNode node = canvas.getSelected();
					if (node != null) {
						if (canvas.getNode(result.id) != null) {
							EditorEnvironment.showMessage("选择的关联模块已经存在！");
							return;
						}
					}

					canvas.remove(node.id);
					node.id = result.id;
					node.name = result.name;
					node.title = result.title;
					canvas.nodes.put(node.id, node);
					canvas.repaint();
				}

			}
		});
		panel_1.add(button_2);
		JButton button = new JButton("编辑");
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel_1.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
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

				FlowNode node = (FlowNode) canvas.getSelected();
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

				}, null, new Object[][] { { "名称", node.name }, { "标题", node.title }, { "说明", node.memo },
						{ "z轴", node.zOrder }, { "id", node.id }, }, new Object[] { "属性", "值" }, null, new int[] { 0 },
						false);

				DefaultTableModel tableModel = result.isok ? result.model : null;

				if (tableModel != null) {
					String name = (String) tableModel.getValueAt(0, 1);
					node.name = name.toLowerCase().trim();
					node.title = (String) tableModel.getValueAt(1, 1);
					node.memo = (String) tableModel.getValueAt(2, 1);
					node.zOrder = Integer.parseInt(tableModel.getValueAt(5, 1).toString());
					String oldid = node.id;
					String newid = (String) tableModel.getValueAt(6, 1);

					File oldFile = EditorEnvironment.getProjectFile(EditorEnvironment.Flow_Dir_Name,
							EditorEnvironment.getNodeFileName(oldid));
					File newFile = EditorEnvironment.getProjectFile(EditorEnvironment.Flow_Dir_Name,
							EditorEnvironment.getNodeFileName(newid));
					if (!FileHelp.renameFile(oldFile.getAbsolutePath(), newFile.getAbsolutePath())) {
						EditorEnvironment.showMessage(null, "更改id失败，请检查文件系统！", "编辑", JOptionPane.ERROR_MESSAGE);
						return;
					}
					canvas.updateID(oldid, newid);
					ctm.refreshTree();
					ctm.selectTreeNode(node);
					isEdit = true;

					save(false);

				}
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

		ctm = new ControlTreeManager(tree, canvas, "流程模块");
	}

	@Override
	public void onSave() {
		if (!isEdit)
			return;
		save(false);
	}

	public void save(boolean needPrompt) {
		try {
			File f = EditorEnvironment.getFlowRelationFile(workflowName);
			// if (f != null && f.exists() && needPrompt) {
			// if (EditorEnvironment.showConfirmDialog((this,
			// "配置信息文件已经存在，是否覆盖？",
			// "提示", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
			// return;
			// }
			if (f == null)
				return;

			canvas.setFile(f);
			canvas.save();
			isEdit = false;
		} catch (Exception e) {
			e.printStackTrace();
			EditorEnvironment.showMessage(this, e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
		}
	}

	boolean needSetPageMode = true;

	public void reload() {
		File path = EditorEnvironment.getFlowRelationFile(workflowName);
		canvas.setFile(path);
		try {
			canvas.load(new EditorEnvironment.WorkflowDeserializable(), new IInitPage() {

				@Override
				public void onPage(PageConfig pageConfig) {
					selectPageMode(pageConfig);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		canvas.fixNodesInPage();
		ctm.refreshTree();
		isEdit = false;
	}

	@Override
	public void onLoad() {
		if (needPrompt())
			if (EditorEnvironment.showConfirmDialog("当前未保存的工作都将丢失，是否关闭当前窗口？", "关闭",
					JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
				return;

		reload();
	}

	@Override
	protected boolean allowQuit() {
		return true;
	}

	@Override
	public void onClose() {
		// save(true);
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

	@Override
	public void onStart(Object param) {
		if (param instanceof String) {
			this.workflowName = FileHelp.removeExt(param.toString());
		} else if (param instanceof String[]) {
			String[] values = (String[]) param;
			this.workflowName = FileHelp.removeExt(values[0]);
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

	@Override
	public void onPublish(HashMap<String, DrawNode> uikeysWorkflowNodes, Object param) throws Exception {

	}
}
