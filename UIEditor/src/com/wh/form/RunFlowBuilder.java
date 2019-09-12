package com.wh.form;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sunking.swing.JFontDialog;
import com.wh.control.ControlTreeManager;
import com.wh.control.EditorEnvironment;
import com.wh.control.RunFlowFile;
import com.wh.control.ScrollToolBar;
import com.wh.control.grid.ButtonColumn.ButtonLabel;
import com.wh.control.grid.design.DefaultPropertyClient;
import com.wh.control.grid.design.DefaultPropertyClient.IUpdate;
import com.wh.control.grid.design.PropertyPanel;
import com.wh.control.modelsearch.ModelSearchView;
import com.wh.dialog.IEditNode;
import com.wh.dialog.editor.JsonEditorDialog;
import com.wh.dialog.input.TextInput;
import com.wh.dialog.selector.KeyValueSelector;
import com.wh.dialog.selector.KeyValueSelector.ModelResult;
import com.wh.draws.DrawCanvas;
import com.wh.draws.DrawCanvas.ChangeType;
import com.wh.draws.DrawCanvas.EditMode;
import com.wh.draws.DrawCanvas.IInitPage;
import com.wh.draws.DrawCanvas.IOnPageSizeChanged;
import com.wh.draws.DrawCanvas.IScroll;
import com.wh.draws.DrawCanvas.PageConfig;
import com.wh.draws.DrawCanvas.PageSizeMode;
import com.wh.draws.DrawNode;
import com.wh.draws.FlowCanvas;
import com.wh.draws.FlowNode;
import com.wh.draws.FlowNode.ANDNode;
import com.wh.draws.FlowNode.ActionNode;
import com.wh.draws.FlowNode.BeginNode;
import com.wh.draws.FlowNode.ChildFlowNode;
import com.wh.draws.FlowNode.ConditionNode;
import com.wh.draws.FlowNode.EndNode;
import com.wh.draws.FlowNode.IFNode;
import com.wh.draws.FlowNode.LabelNode;
import com.wh.draws.FlowNode.ORNode;
import com.wh.draws.FlowNode.ProcessNode;
import com.wh.draws.FlowNode.StateNode;
import com.wh.draws.FlowNode.SwitchNode;
import com.wh.draws.FlowNode.XORNode;
import com.wh.draws.RunFlowCanvas;
import com.wh.form.MainForm.IControl;
import com.wh.system.tools.FileHelp;
import com.wh.system.tools.JsonHelp;

public class RunFlowBuilder extends ChildForm implements IMainMenuOperation, IControl {

	private static final long serialVersionUID = 1L;

	class DrawNodeInfo {
		public String pageName;

	}

	RunFlowCanvas canvas = new RunFlowCanvas();
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

	public BeginNode findStartNode() {
		return findStartNode(canvas);
	}

	protected static BeginNode findStartNode(DrawCanvas canvas) {
		for (DrawNode node : canvas.getNodes()) {
			if (node instanceof EndNode)
				continue;
			if (node instanceof BeginNode)
				return (BeginNode) node;
		}
		return null;
	}

	public EndNode findEndNode() {
		return findEndNode(canvas);
	}

	protected static EndNode findEndNode(DrawCanvas canvas) {
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
		case FlowNode.StateNode_Title: {
			String name = TextInput.showDialog("请输入状态名称：");
			if (name == null || name.isEmpty())
				return;

			addNode(StateNode.class, name);
			break;
		}
		case FlowNode.ActionNode_Title: {
			String name = TextInput.showDialog("请输入动作名称：");
			if (name == null || name.isEmpty())
				return;

			addNode(ActionNode.class, name);
			break;
		}
		}
	}

	public FlowNode addNode(Class<? extends FlowNode> c, String title) {
		FlowNode node = canvas.add(title, c, null, null);
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
		if (node instanceof ProcessNode || node instanceof ConditionNode || node instanceof ChildFlowNode)
			return true;
		else
			return false;
	}

	protected void onEditNode(DrawNode node) {
		if (node instanceof IFNode || node instanceof ProcessNode || node instanceof LabelNode
				|| node instanceof SwitchNode || node instanceof ConditionNode) {
			String name = TextInput.showDialog("请输入新内容：");
			if (name == null || name.isEmpty())
				return;

			node.title = name;
			canvas.repaint();
		}
	}

	public IEditNode onEditNode;

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
		ctm.refreshTree();
		canvas.remove();
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

	public RunFlowBuilder(IMainControl mainControl) {
		super(mainControl);
		setFont(new Font("微软雅黑", Font.PLAIN, 12));
		setTitle("运行流程设计器");

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		needSetPageMode = false;
		setBounds(100, 100, 1260, 1058);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.15);
		contentPane.add(splitPane, BorderLayout.CENTER);

		tree = new ModelSearchView.TreeModelSearchView(new DefaultTreeModel(new DefaultMutableTreeNode("流程")));
		tree.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		tree.setRowHeight(24);
		tree.setCellRenderer(new ControlTreeManager.WorkflowTreeCellRender(canvas));
		tree.setExpandsSelectedPaths(true);
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				if (e.getNewLeadSelectionPath() != null) {
					if (canvas.isMuiltSelecting())
						return;

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

		splitPane.setLeftComponent(tree);

		JSplitPane splitPane_1 = new JSplitPane();
		splitPane_1.setResizeWeight(0.8);
		splitPane.setRightComponent(splitPane_1);

		JPanel panel = new JPanel();
		splitPane_1.setLeftComponent(panel);
		panel.setPreferredSize(new Dimension(200, 10));
		panel.setLayout(new BorderLayout(0, 0));

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
			}

			@Override
			public void onChange(DrawNode[] nodes, ChangeType ct) {
				switch (ct) {
				case ctBackEdited:
					isEdit = true;
					return;
				case ctPaste:
				case ctRemove:
				case ctAdd:
					ctm.refreshTree();
					isEdit = true;
					break;
				case ctMove:
				case ctAddLink:
				case ctRemoveLink:
				case ctResize:
					isEdit = true;
					break;
				case ctDeselected:
					if (nodes.length == 0)
						try {
							DefaultPropertyClient.propertyBuilder(table, null, null, null, null);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					break;
				case ctSelected:
				case ctSelecteds:
					ctm.selectTreeNode(nodes.length == 0 ? null : nodes[0]);
					if (nodes.length > 0)
						try {
							if (nodes[0] instanceof StateNode || nodes[0] instanceof ProcessNode)
								DefaultPropertyClient.propertyBuilder(table, nodes[0].toJson(), nodes[0]);
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					break;
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

		table = new PropertyPanel();
		table.setMinimumSize(new Dimension(0, 0));
		table.setBackground(Color.WHITE);
		table.onClientEvent = new DefaultPropertyClient(new IUpdate() {

			protected Object showButtonEditJsonEditor(ButtonLabel ob) throws JSONException {
				Object value = showJsonEditor(ob.getValue());
				ob.textField.setText(value == null ? "" : value.toString());
				ob.setValue(value);
				return value;
			}

			public Object showJsonEditor(Object objValue) {
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
				Object value = JsonEditorDialog.show(mainControl, data);
				return value;
			}

			@Override
			public void setEditState(boolean isEdit) {
				RunFlowBuilder.this.isEdit = true;
			}

			@Override
			public void onUpdateEnd(Object obj, String name, Object oldValue, Object attrValue) {
				if ((name.compareTo("id") == 0 || name.compareTo("name") == 0 || name.compareTo("title") == 0))
					ctm.refreshTree();
			}

			@Override
			public String getUIID() {
				return null;
			}

			@Override
			public Object showButtonEditJsonEditor(String name, ButtonLabel buttonEdit) throws JSONException {
				return showButtonEditJsonEditor(buttonEdit);
			}

			@Override
			public Component getParent() {
				return RunFlowBuilder.this;
			}

			@Override
			public void onEdit(int row, int col) {
				// TODO Auto-generated method stub

			}

		}, canvas, mainControl, table);
		splitPane_1.setRightComponent(table);

		JPanel toolPanel = new JPanel();
		JPanel scrollButtonPanel = new JPanel();
		JScrollPane toolbarScrollBar = new JScrollPane();
		JToolBar header = new JToolBar();

		contentPane.add(toolPanel, BorderLayout.NORTH);
		toolPanel.setLayout(new BorderLayout(0, 0));
		toolPanel.add(toolbarScrollBar, BorderLayout.CENTER);
		toolbarScrollBar.getViewport().add(header);
		new ScrollToolBar(toolPanel, scrollButtonPanel, toolbarScrollBar, header);

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

		JButton button_3 = new JButton("发布");
		button_3.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				publish();
			}
		});
		header.add(button_3);

		JLabel label_5 = new JLabel(" | ");
		label_5.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		header.add(label_5);
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
				new String[] { FlowNode.BeginNode_Title, FlowNode.EndNode_Title, FlowNode.StateNode_Title,
						FlowNode.ActionNode_Title, FlowNode.XORNode_Title, FlowNode.ANDNode_Title,
				// FlowNode.AdapterNode_Title
				}));
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

				Font result = JFontDialog.showDialog(RunFlowBuilder.this, "字体选择", true, font);
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

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(normalRadio);
		buttonGroup.add(linkRadio);
		buttonGroup.add(disLinkRadio);

		JPanel footer = new JPanel();
		contentPane.add(footer, BorderLayout.SOUTH);
		footer.setLayout(new BorderLayout(0, 0));

		memo = new JLabel("   ");
		memo.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		memo.setHorizontalAlignment(SwingConstants.CENTER);
		footer.add(memo);

		JPanel panel_1 = new JPanel();
		footer.add(panel_1, BorderLayout.EAST);
		JButton button = new JButton("编辑");
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel_1.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ModelResult result = KeyValueSelector.show(null, mainControl, null, null,
						new Object[][] { { "流程编号", canvas.getPageConfig().id }, { "流程名称", canvas.getPageConfig().name },
								{ "流程说明", canvas.getPageConfig().memo }, },
						new Object[] { "属性", "值" }, null, new int[] { 0 }, false);

				DefaultTableModel tableModel = result.isok ? result.model : null;
				
				if (tableModel != null) {
					canvas.getPageConfig().id = (String) tableModel.getValueAt(0, 1);
					canvas.getPageConfig().name = (String) tableModel.getValueAt(1, 1);
					canvas.getPageConfig().memo = (String) tableModel.getValueAt(2, 1);
					canvas.repaint();
					memo.setText(canvas.getPageConfig().memo);
					isEdit = true;
				}

			}
		});

		pageMode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String text = getPageMode();
				canvas.getPageConfig().setPageSizeMode(text, 0, 0);
				if (needSetPageMode)
					isEdit = true;
			}
		});

		needSetPageMode = true;
		splitPane.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				splitPane.setDividerLocation(splitPane.getResizeWeight());
				splitPane.setResizeWeight(splitPane.getResizeWeight());
			}
		});

		splitPane_1.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				splitPane_1.setDividerLocation(splitPane_1.getResizeWeight());
				splitPane_1.setResizeWeight(splitPane_1.getResizeWeight());
			}
		});

		ctm = new ControlTreeManager(tree, canvas, "流程模块");
	}

	RunFlowFile runFlowFile = new RunFlowFile();

	public void save(boolean needPrompt) {
		try {
			runFlowFile.setFile(EditorEnvironment.getRunFlowFile(canvas.getPageConfig().id));
			runFlowFile.data = canvas.saveToJson();
			runFlowFile.name = canvas.getPageConfig().name;
			runFlowFile.memo = canvas.getPageConfig().memo;
			runFlowFile.save();
			isEdit = false;
		} catch (Exception e) {
			e.printStackTrace();
			EditorEnvironment.showMessage(this, e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
		}
	}

	boolean needSetPageMode = true;
	private PropertyPanel table;

	public void reload() {
		try {
			canvas.loadFromJson(runFlowFile.data, new EditorEnvironment.NullDeserializable(), new IInitPage() {

				@Override
				public void onPage(PageConfig pageConfig) {
					selectPageMode(pageConfig);
				}
			}, true);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		canvas.fixNodesInPage();
		ctm.refreshTree();
		isEdit = false;
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
	public void onLoad() {
		if (needPrompt())
			if (EditorEnvironment.showConfirmDialog("当前未保存的工作都将丢失，是否关闭当前窗口？", "关闭",
					JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
				return;

		reload();
	}

	@Override
	public void onClose() {
		dispose();
	}

	public enum OperType {
		otAnd, otOr, otXor, otOne, otNone;
	}

	public static class UIControlInfo {
		public String data;

		public String toString() {
			String result = "";
			if (data != null && !data.isEmpty())
				result += data;

			return result;
		}
	}

	protected static void throwException(FlowNode node, String msg) throws Exception {
		if (node.getCanvas() != null) {
			FlowCanvas canvas = (FlowCanvas) node.getCanvas();
			canvas.setSelected(node);
		}

		throw new Exception(msg);
	}

	public static class Publisher {

		// 起始状态值
		protected static final String START_KEY = "start";
		// 状态节点的根
		protected static final String STATE_ROOT_KEY = "state";
		// 处理节点的根
		protected static final String PROCESS_ROOT_KEY = "process";
		// 处理节点的action节点根
		protected static final String PROCESS_ACTION_ROOT_KEY = "action";
		// 处理节点的adapter节点根
		protected static final String PROCESS_ADAPTER_ROOT_KEY = "adapter";

		// 状态节点的操作类型键名
		protected static final String STATE_OPER_KEY = "oper";
		// 状态节点关联的actionid的键名
		protected static final String STATE_ACTION_ROOT_KEY = "action";
		// 状态节点关联的adapterid的键名
		protected static final String STATE_ADAPTER_ROOT_KEY = "adapter";
		// 状态节点的下一个状态名称的键名
		protected static final String STATE_NEXT_KEY = "next";
		// 状态节点的上一个状态名称的键名
		protected static final String STATE_PREV_KEY = "prev";

		// 状态节点下的action节的归属状态
		protected static final String ACTION_STATE_KEY = "state";
		// 状态节点下的action节的关联的workflow信息
		protected static final String ACTION_WORKFLOW_KEY = "workflow";
		// 状态节点下的action节的关联的初始信息
		protected static final String ACTION_INITDATA_KEY = "initdata";
		// 状态节点下的action节的关联的权限信息
		protected static final String ACTION_ROLE_KEY = "role";

		// 状态节点下的adapter节的关联的url信息
		protected static final String ADAPTER_URL_KEY = "url";
		// 状态节点下的adapter节的关联的请求参数信息
		protected static final String ADAPTER_POSTDATA_KEY = "postdata";

		// adapter或action节点下的下一节点
		protected static final String PROCESS_NEXT_KEY = "next";

		RunFlowCanvas canvas;
		File saveFile;

		public Publisher(RunFlowCanvas canvas, File saveFile) {
			this.canvas = canvas;
			this.saveFile = saveFile;
		}

		/***
		 * 获取根集合的键名
		 * 
		 * @param node
		 *            要获取存储根键名的节点
		 * @return 返回可以存储此节点信息的根键名
		 * @throws Exception
		 */
		protected String getRootKey(FlowNode node) throws Exception {
			if (node instanceof StateNode) {
				return STATE_ROOT_KEY;
			} else if (node instanceof ProcessNode) {
				return PROCESS_ROOT_KEY;
			} else if (node instanceof SwitchNode) {
				return null;
			} else if (node instanceof EndNode) {
				return null;
			} else {
				throwException(node, "仅可以建立状态/处理节点！");
			}
			return null;
		}

		/***
		 * 获取指定节点的存储对象；如果存储对象的集合对象不存在，则创建，如果节点存储对象不存在则创建。
		 * 
		 * @param node
		 *            要获取存储对象的节点
		 * @param root
		 *            要保存的json对象
		 * @return 返回可以存储此节点信息的存储对象
		 * @throws Exception
		 */
		protected JSONObject getInfo(FlowNode node, JSONObject root) throws Exception {
			String rootName = getRootKey(node);
			if (rootName == null)
				return null;

			JSONObject rootInfo;
			if (!root.has(rootName)) {
				rootInfo = new JSONObject();
				root.put(rootName, rootInfo);
			} else
				rootInfo = root.getJSONObject(rootName);

			String name = null;

			if (node instanceof StateNode) {
				name = ((StateNode) node).state;
				if (name == null || name.isEmpty())
					throwException(node, "状态节点的state不能为null！");
			} else if (node instanceof ProcessNode) {
				if (node instanceof ActionNode) {
					ActionNode actionNode = (ActionNode) node;
					if (actionNode.model_id == null)
						throwException(node, "动作节点的model_id不能为null！");
				}
				name = node.id;
			} else {
				throwException(node, "不支持此节点类型！");
			}

			JSONObject childInfo;
			if (!rootInfo.has(name)) {
				childInfo = new JSONObject();
				rootInfo.put(name, childInfo);
			} else
				childInfo = rootInfo.getJSONObject(name);

			return childInfo;
		}

		/***
		 * 仅处理Switch类型节点自身的信息存储
		 * 
		 * @param switchNode
		 *            要存储的节点
		 * @param info
		 *            此节点对应的json对象
		 * @throws Exception
		 */
		protected void processSwitch(SwitchNode switchNode, JSONObject info) throws Exception {
			if (switchNode instanceof XORNode) {
				info.put(STATE_OPER_KEY, OperType.otXor.name());
			} else if (switchNode instanceof ANDNode) {
				info.put(STATE_OPER_KEY, OperType.otAnd.name());
			} else if (switchNode instanceof ORNode) {
				info.put(STATE_OPER_KEY, OperType.otOr.name());
			} else {
				throwException(switchNode, "此处不支持此类型节点！");
			}
		}

		/***
		 * 仅处理状态节点（必须为1个）-》分支节点（必须为1个，switchNode参数指定）-》动作或适配器节点（必须多个）的情况下的信息存储，
		 * 信息将被保存到状态节点(info参数指定)的STATE_ADAPTER_ROOT_KEY/STATE_ACTION_ROOT_KEY域中
		 * 
		 * @param switchNode
		 *            要处理的分支节点
		 * @param info
		 *            关联的状态节点的存储json对象
		 * @param root
		 *            保存所有信息的json对象
		 * @throws Exception
		 */
		protected void processOneStateToMulitActionAnds(SwitchNode switchNode, JSONObject info, JSONObject root) throws Exception {
			processSwitch(switchNode, info);

			if (switchNode.getNexts().size() == 0)
				throwException(switchNode, "分支节点后至少需要连接一个动作或者适配器节点！");

			for (String id : switchNode.getNexts()) {
				FlowNode node = (FlowNode) canvas.getNode(id);
				if (!(node instanceof ProcessNode)) {
					throwException(node, "此处的节点类型仅可以为动作或者适配器节点！");
				}

				addStateProcessInfo(info, node);
			}
		}

		/***
		 * 仅处理状态节点（必须为多个）-》分支节点（必须为1个，switchNode参数指定）-》状态节点（必须为1个，保存的目标对象）
		 * 的情况下的状态信息存储，
		 * state将被保存到目标状态节点的STATE_PREV_KEY域中，同时源状态节点的STATE_NEXT_KEY会
		 * 填入目标状态节点的state
		 * 
		 * @param switchNode
		 *            要处理的分支节点
		 * @param root
		 *            保存所有信息的json对象
		 * @throws Exception
		 */
		protected void processMulitStateToOneState(SwitchNode switchNode, JSONObject root) throws Exception {

			if (switchNode.getNexts().size() != 1)
				throwException(switchNode, "此处分支节点的子节点仅必须连接一个状态节点！");

			FlowNode flowNode = (FlowNode) canvas.getNode(switchNode.getNexts().get(0));
			if (!(flowNode instanceof StateNode)) {
				throwException(switchNode, "此处仅可以使用状态节点！");
			}

			if (switchNode.getPrevs().size() == 1)
				throwException(switchNode, "此处分支节点的父节点需要至少连接两个状态节点！");

			StateNode nextStateNode = (StateNode) flowNode;

			JSONObject nextStateInfo = getInfo(nextStateNode, root);
			processSwitch(switchNode, nextStateInfo);

			for (String id : switchNode.getPrevs()) {
				FlowNode node = (FlowNode) canvas.getNode(id);
				if (!(node instanceof StateNode)) {
					throwException(switchNode, "此处仅可以使用状态节点！");
				}

				StateNode stateNode = (StateNode) node;
				JSONObject stateInfo = getInfo(stateNode, root);

				JSONArray prevs;
				if (nextStateInfo.has(STATE_PREV_KEY))
					prevs = nextStateInfo.getJSONArray(STATE_PREV_KEY);
				else {
					prevs = new JSONArray();
					nextStateInfo.put(STATE_PREV_KEY, prevs);
				}
				prevs.put(stateNode.state);

				stateInfo.put(STATE_NEXT_KEY, nextStateNode.state);
			}
		}

		/***
		 * 保存动作节点信息到关联的状态节点的信息中
		 * 
		 * @param flowNode
		 *            要保存的动作节点
		 * @param processInfo
		 *            状态节点信息中的处理信息对象
		 * @return 是否成功保存，true成功保存，其他失败
		 * @throws Exception
		 */
		protected boolean processAction(FlowNode flowNode, JSONObject processInfo) throws Exception {
			if (flowNode instanceof ActionNode) {
				ActionNode actionNode = (ActionNode) flowNode;
				if (actionNode.model_id == null || actionNode.model_id.value == null) {
					throwException(actionNode, "动作节点的model_id不能为null！");
				}

				String state = null;
				for (String id : flowNode.getPrevs()) {
					FlowNode node = (FlowNode) canvas.getNode(id);
					if (node instanceof StateNode) {
						state = ((StateNode) node).state;
					}
				}

				if (state == null || state.isEmpty() && flowNode.getPrevs().size() > 0) {
					FlowNode node = (FlowNode) canvas.getNode(flowNode.getPrevs().get(0));
					if (node instanceof ANDNode) {
						node = (FlowNode) canvas.getNode(node.getPrevs().get(0));
						state = ((StateNode) node).state;
					}
				}

				if (state == null || state.isEmpty()) {
					throwException(flowNode, "动作节点[" + flowNode.title + "]没有归属状态节点，请连接后再试！");
				}

				processInfo.put("id", actionNode.id);
				processInfo.put(ACTION_WORKFLOW_KEY, actionNode.model_id == null ? null : actionNode.model_id.toJson());
				processInfo.put(ACTION_INITDATA_KEY, actionNode.initData);
				processInfo.put(ACTION_STATE_KEY, state);
				return true;
			}

			return false;
		}

		/***
		 * 将动作节点或者适配器节点的id加入到状态信息中
		 * 
		 * @param info
		 *            要添加的状态信息对象
		 * @param name
		 *            要保存的列表节点名称，可以为：STATE_ACTION_ROOT_KEY/STATE_ADAPTER_ROOT_KEY
		 * @param node
		 *            要保存的节点对象
		 */
		protected void addStateProcessInfo(JSONObject info, String name, FlowNode node) {
			if (!info.has(name)) {
				info.put(name, new JSONArray());
			}
			JSONArray actions = info.getJSONArray(name);
			HashMap<String, String> idMap = new HashMap<>();
			for (Object object : actions) {
				idMap.put((String) object, null);
			}
			idMap.put(node.id, null);
			info.put(name, new JSONArray(idMap.keySet()));
		}

		protected void addStateProcessInfo(JSONObject info, FlowNode node) throws Exception {
			String name = null;
			if (node instanceof ActionNode) {
				name = STATE_ACTION_ROOT_KEY;
			} else {
				throwException(node, "此类型节点加入不支持状态节点的动作列表！");
			}

			addStateProcessInfo(info, name, node);
		}

		/***
		 * 将动作或者适配器对象信息加入到json对象中
		 * 
		 * @param node
		 *            要加入的节点对象
		 * @param root
		 *            保存所有信息的json对象
		 * @throws Exception
		 */
		protected void addProcessInfo(FlowNode node, JSONObject root) throws Exception {
			JSONObject processInfo = getInfo(node, root);
			if (processInfo == null)
				return;

			int statecount = 0;
			for (String id : node.getPrevs()) {
				FlowNode flowNode = (FlowNode) canvas.getNode(id);
				if (flowNode instanceof StateNode) {
					if (++statecount > 1) {
						throwException(node, "动作节点[" + node.title + "]有多于一个以上的状态节点！");
					}
				}
			}
			if (node instanceof ActionNode)
				processAction(node, processInfo);
		}

		/***
		 * 分析指定节点，并将必要的关联信息保存到json对象中，根据对象以及父子节点的关系，会更新相关的节点信息
		 * 
		 * @param node
		 *            要保存的节点对象
		 * @param parent
		 *            要保存节点对象的父节点对象
		 * @param root
		 *            保存所有数据的json对象
		 * @throws Exception
		 */
		protected void processRelation(FlowNode node, FlowNode parent, JSONObject root) throws Exception {
			if (parent == null) {
				// 如果parent说明node是第一个节点，也就是起始状态节点，无关系直接退出
				return;
			} else if (parent instanceof StateNode) {
				JSONObject stateInfo = getInfo(parent, root);

				if (node instanceof ProcessNode) {
					// 添加此状态需要执行的action信息
					addStateProcessInfo(stateInfo, node);
					addProcessInfo(node, root);
				} else if (node instanceof SwitchNode) {
					if (node instanceof XORNode) {
						// 添加此状态节点的连接信息，且仅当节点的连接为【状态】多 =》 【分支】1 =》 【状态】1 情况
						if (node.getNexts().size() == 1) {
							StateNode nextNode = (StateNode) canvas.getNode(node.getNexts().get(0));
							stateInfo.put(STATE_NEXT_KEY, nextNode.state);
						}
					} else if (node instanceof ANDNode){
						// 添加此状态节点的连接信息，且仅当节点的连接为【状态】1 =》 【分支】1 =》 【action】多个 情况
						if (node.getPrevs().size() == 1)
							processOneStateToMulitActionAnds((SwitchNode) node, stateInfo, root);
					}
				} else if (node instanceof EndNode) {
					// 无需记录end节点关系，直接退出
					return;
				} else {
					throwException(node, "当父节点为状态节点时，不允许连接此类型节点！");
				}
			} else if (parent instanceof ProcessNode) {
				JSONObject parentInfo = getInfo(parent, root);
				if (node instanceof StateNode) {
					StateNode stateNode = (StateNode) node;
					JSONArray states;
					if (parentInfo.has(PROCESS_NEXT_KEY))
						states = parentInfo.getJSONArray(PROCESS_NEXT_KEY);
					else {
						states = new JSONArray();
						parentInfo.put(PROCESS_NEXT_KEY, states);
					}
					states.put(stateNode.state);
				} else {
					throwException(node, "当父节点为状态节点时，不允许连接此类型节点！");
				}
			} else if (parent instanceof SwitchNode) {

				if (node instanceof ProcessNode) {
					if (parent instanceof ANDNode) {
						if (parent.getPrevs().size() != 1) {
							throwException(parent, "逻辑分支节点不可以多对多连接！");
						}

						DrawNode stateNode = canvas.getNode(parent.getPrevs().get(0));
						if (!(stateNode instanceof StateNode)) {
							throwException(node, "此节点的上一级应该连接一个状态节点！");
						}

						JSONObject parentInfo = getInfo((FlowNode) stateNode, root);

						if (!(node instanceof ProcessNode)) {
							throwException(node, "当父节点为判定节点，仅动作节点可以连接！");
						}

						addProcessInfo(node, root);
						addStateProcessInfo(parentInfo, node);
					} else if (parent instanceof XORNode) {
						if (parent.getNexts().size() != 1) {
							throwException(node, "XOR节点的下级节点必须仅为1个动作或适配器节点！");
						}

						FlowNode processNode = (FlowNode) canvas.getNode(parent.getNexts().get(0));
						if (!(processNode instanceof ProcessNode))
							throwException(node, "XOR节点的下级节点必须为1个动作或适配器节点！");

						if (parent.getPrevs().size() > 1) {
							for (String id : parent.getPrevs()) {
								FlowNode stateNode = (FlowNode) canvas.getNode(id);
								if (!(stateNode instanceof StateNode)) {
									throwException(stateNode, "XOR节点的上级节点必须为状态节点！");
								}
								JSONObject stateInfo = getInfo(stateNode, root);
								addStateProcessInfo(stateInfo, processNode);
							}
						} else {
							throwException(parent, "XOR节点仅支持多个状态节点连接到一个动作或适配器节点！");
						}
					}
				} else if (node instanceof StateNode) {
					if (parent instanceof ANDNode)
						processMulitStateToOneState((SwitchNode) parent, root);
				} else {
					throwException(node, "当父节点为状态节点时，不允许连接此类型节点！");
				}
			}

		}

		/***
		 * 发布流程图中的所有节点到json对象中
		 * 
		 * @param node
		 *            起始节点
		 * @param parent
		 *            node的父节点对象
		 * @param alreadies
		 *            用于检验是否已经保存过的节点的map对象，用于递归退出
		 * @param root
		 *            保存所有节点信息的json对象
		 * @throws Exception
		 */
		protected void publishContents(FlowNode node, FlowNode parent, HashMap<String, FlowNode> alreadies,
				JSONObject root) throws Exception {
			getInfo(node, root);
			processRelation(node, parent, root);
			if (alreadies.containsKey(node.id))
				return;

			alreadies.put(node.id, node);

			for (String id : node.getNexts()) {
				FlowNode child = (FlowNode) canvas.getNode(id);
				publishContents((FlowNode) child, node, alreadies, root);
			}
		}

		/***
		 * 发布工作流信息到jsonobject对象 格式如下：{ start:"起始状态节点的state值", state:[
		 * "状态节点的state值":{id:"状态节点id",oper:"otAnd|otOne"} ] }
		 * 
		 * @param nodes
		 * @param data
		 * @throws Exception
		 */
		protected void publishToJson(StateNode start, JSONObject root) throws Exception {
			String state = start.state;
			if (state == null || state.isEmpty()) {
				throwException((FlowNode) start, "状态节点必须设置state值！");
			}

			if (!root.has(START_KEY))
				root.put(START_KEY, state);

			publishContents((StateNode) start, null, new HashMap<>(), root);
		}

		/***
		 * 发布流程图到指定文件，用于运行时解析，文件为saveFile设置
		 * 
		 * @throws Exception
		 */
		public void publish() throws Exception {
			if (EditorEnvironment.getPublishWebPath() == null) {
				EditorEnvironment.showMessage(null, "请先设置Web根目录！");
				return;
			}
			BeginNode beginNode = findStartNode(canvas);
			EndNode endNode = findEndNode(canvas);

			if (beginNode == null) {
				EditorEnvironment.showMessage("必须设置起始节点，请添加起始节点并连线后重试！");
				return;
			}
			if (endNode == null) {
				EditorEnvironment.showMessage("必须设置结束节点，请添加结束节点并连线后重试！");
				return;
			}

			if (beginNode.getNexts().size() == 0) {
				throwException(beginNode, "起始节点【" + beginNode.name + "】没有连接任何节点！");
			}

			if (beginNode.getNexts().size() > 1) {
				throwException(beginNode, "起始节点【" + beginNode.name + "】连接的节点数量大于一个！");
			}

			FlowNode start = (FlowNode) canvas.getNode(beginNode.getNexts().get(0));
			if (start == null) {
				throwException(beginNode, "起始节点【" + beginNode.name + "】没有连接任何节点！");
			}

			if (!(start instanceof StateNode)) {
				throwException(beginNode, "起始节点【" + beginNode.name + "】连接的节点类型必须是状态节点！");
			}

			HashMap<String, DrawNode> checkNodes = new HashMap<>();
			for (DrawNode node : canvas.getNodes()) {
				if (node instanceof StateNode) {
					StateNode stateNode = (StateNode) node;
					if (stateNode.state == null || stateNode.state.isEmpty())
						throwException(stateNode, "状态节点【" + node.name + "】未填写状态！");
					String key = stateNode.state.trim().toLowerCase();
					if (checkNodes.containsKey(key))
						throwException(stateNode, "状态节点【" + node.name + "】的状态【" + stateNode.state + "】已经存在！");
					checkNodes.put(key, stateNode);

				}
			}
			if (saveFile.exists())
				if (!saveFile.delete()) {
					throwException(start, "删除文件：" + saveFile.getAbsolutePath() + "失败，请重试");
				}

			JSONObject data = new JSONObject();

			publishToJson((StateNode) start, data);

			JsonHelp.saveJson(saveFile, data.toString(), null);
		}
	}

	/***
	 * 保存工作流信息到文件，文件名为canvas的name，并保存到web目录（/RunWorkflow_Dir_Name）
	 * 发布的文件内容格式为jsonobject
	 */
	public void publish() {
		try {
			if (isEdit) {
				if (EditorEnvironment.showConfirmDialog("当前流程已经修改，是否保存后发布？",
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					save(false);
				}
			}

			File runWorkflowFile = EditorEnvironment.getPublishWebFile(EditorEnvironment.RunFlow_Dir_Name,
					EditorEnvironment.getRelationFileName(canvas.getPageConfig().name));
			Publisher published = new Publisher(canvas, runWorkflowFile);
			published.publish();
			EditorEnvironment.showMessage(null, "导出运行流程图成功，关闭后打开目录！", "恭喜");
			Desktop.getDesktop().open(runWorkflowFile.getParentFile());

		} catch (Exception e2) {
			e2.printStackTrace();
			EditorEnvironment.showException(e2);
		}
	}

	@Override
	public void onPublish(HashMap<String, DrawNode> uikeysWorkflowNodes, Object param) throws Exception {
		publish();
	}

	@Override
	public void onSave() {
		if (!isEdit)
			return;
		save(false);
	}

	@Override
	public void onStart(ChildForm subForm, Object param) {
		canvas.clear();
		if (param != null) {
			runFlowFile = (RunFlowFile) ((Object[]) param)[0];
			onLoad();
			canvas.getPageConfig().id = FileHelp.removeExt(runFlowFile.getFile().getName());
			canvas.getPageConfig().name = runFlowFile.name;
			canvas.getPageConfig().memo = runFlowFile.memo;
		}
	}

	@Override
	public void onEnd(ChildForm subForm) {
	}

}
