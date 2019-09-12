package com.wh.form;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
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
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
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

import com.wh.control.ControlTreeManager;
import com.wh.control.EditorEnvironment;
import com.wh.control.ParseApp;
import com.wh.control.grid.ButtonColumn.ButtonLabel;
import com.wh.control.grid.design.DefaultPropertyClient;
import com.wh.control.grid.design.PropertyPanel;
import com.wh.control.modelsearch.ModelSearchView;
import com.wh.control.grid.design.DefaultPropertyClient.IUpdate;
import com.wh.dialog.IEditNode;
import com.wh.dialog.editor.JsonEditorDialog;
import com.wh.dialog.editor.UISelectDialog;
import com.wh.dialog.selector.KeyValueSelector;
import com.wh.dialog.selector.KeyValueSelector.ModelResult;
import com.wh.draws.AppWorkflowCanvas;
import com.wh.draws.AppWorkflowNode;
import com.wh.draws.DrawCanvas;
import com.wh.draws.DrawNode;
import com.wh.draws.WorkflowNode;
import com.wh.draws.AppWorkflowNode.BeginNode;
import com.wh.draws.AppWorkflowNode.ChildAppWorkflowNode;
import com.wh.draws.AppWorkflowNode.CommandNode;
import com.wh.draws.AppWorkflowNode.EndNode;
import com.wh.draws.AppWorkflowNode.JSCommandNode;
import com.wh.draws.AppWorkflowNode.PhpCommandNode;
import com.wh.draws.DrawCanvas.ChangeType;
import com.wh.draws.DrawCanvas.EditMode;
import com.wh.draws.DrawCanvas.IDataSerializable;
import com.wh.draws.DrawCanvas.IInitPage;
import com.wh.draws.DrawCanvas.IOnPageSizeChanged;
import com.wh.draws.DrawCanvas.IScroll;
import com.wh.draws.DrawCanvas.PageConfig;
import com.wh.draws.DrawCanvas.PageSizeMode;
import com.wh.form.MainForm.IControl;
import com.wh.system.tools.JsonHelp;

public class CodeFlowBuilder extends ChildForm implements IMainMenuOperation, IControl{

	private static final long serialVersionUID = 1L;
	
	AppWorkflowCanvas canvas = new AppWorkflowCanvas();
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
	
	public BeginNode findStartNode(){
		for (DrawNode node : canvas.getNodes()) {
			if (node instanceof EndNode)
				continue;
			if (node instanceof BeginNode)
				return (BeginNode)node;
		}
		return null;
	}

	public EndNode findEndNode(){
		for (DrawNode node : canvas.getNodes()) {
			if (node instanceof EndNode)
				return (EndNode)node;
		}
		return null;
	}

	public void addNode(){
		String title = nodetype.getSelectedItem().toString();
		AppWorkflowNode node = AppWorkflowNode.getInstance(canvas, title);
		if (node == null)
			return;
		
		addNode(node.getClass(), title);
		canvas.setSelected(node);
		canvas.repaint();
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
	
	public AppWorkflowNode addNode(Class<? extends AppWorkflowNode> c, String title){
		AppWorkflowNode node = canvas.add(title, c, null, dataserializable);
		ctm.refreshTree();
		canvas.repaint();
		return node;
	}
	
	protected String getPageMode() {
		return (String)pageMode.getSelectedItem();
	}
	
	protected File getNodeData(DrawNode node){
		return new File((String)node.userData);
	}
	
	protected boolean allowEditNode(DrawNode node) {
		if (node instanceof ChildAppWorkflowNode)
			return true;
		else
			return false;
	}
	
	protected void onEditNode(DrawNode node) {
		if (node instanceof ChildAppWorkflowNode){
			mainControl.openCodeflowRelation(node.name, uiid, workflowid);
		}else if (node instanceof CommandNode){
			CommandNode jsNode = (CommandNode)node;
			try {
				File file = jsNode.getFile();
				if (file.exists()){
					Desktop.getDesktop().open(file); 
				}else{
					EditorEnvironment.showMessage("文件：" + file.getAbsolutePath() + "不存在！");
				}
			} catch (Exception e) {
				e.printStackTrace();
				EditorEnvironment.showException(e);
			}
		}
	}
	
	public IEditNode onEditNode;
	protected void editDrawNode(DrawNode node) {
		if (node == null)
			return;
		
		if (node instanceof EndNode || node instanceof BeginNode)
			return;
		
		onEditNode(node);
	}

	protected void remove() {
		if (canvas.getSelected() == null)
			return;
		
		if (EditorEnvironment.showConfirmDialog("是否删除选定的项目？", "删除", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
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
	
	public String uiid, workflowid;
	private JLabel memo;
	
	private JPanel contentPane;
	
    protected void keyPressed(KeyEvent e){
    	if (!canvas.hasFocus())
    		canvas.keyPressed(e);
    }  

    protected void keyReleased(KeyEvent e){
    	if (!canvas.hasFocus())
    		canvas.keyReleased(e);
    }  

	public CodeFlowBuilder(IMainControl mainControl) {
		super(mainControl);
		setFont(new Font("微软雅黑", Font.PLAIN, 12));
		setTitle("程序流程设计器");

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
				if (canvas.isMuiltSelecting())
					return;
				
				if (e.getNewLeadSelectionPath() != null){
					ctm.selectCanvasNode((TreeNode)e.getNewLeadSelectionPath().getLastPathComponent());
				}
			}
		});
		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (tree.getSelectionPath() != null)
				ctm.selectCanvasNode((TreeNode)tree.getSelectionPath().getLastPathComponent());
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
		splitPane_1.setResizeWeight(0.75);
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
				if (!canvas.isCtrlPressed()){
					adj = vScrollBar;
				}else
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
					if (canvas.getSelected() == null)
						try {
							DefaultPropertyClient.propertyBuilder(table, null, null, null, null);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					break;
				case ctSelected:
				case ctSelecteds:
					if (canvas.isMuiltSelecting())
						break;
					
					if (nodes.length > 0)
						try {
							if (nodes[0] instanceof CommandNode){
								DefaultPropertyClient.propertyBuilder(table, nodes[0].toJson(), nodes[0]);
							}else
								DefaultPropertyClient.propertyBuilder(table, null, null, null, null);
								
							ctm.selectTreeNode(nodes[0]);
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
		table.onClientEvent = new DefaultPropertyClient(new IUpdate(){

			protected Object showButtonEditJsonEditor(ButtonLabel ob) throws JSONException {
				Object value = showJsonEditor(ob.getValue());
				ob.textField.setText(value == null ? "" : value.toString());
				ob.setValue(value);
				return value;
			}
			
			public Object showJsonEditor(Object objValue){
				Object data = null;
				if (objValue != null){
					if (objValue instanceof JSONArray || objValue instanceof JSONObject)
						data = objValue;
					else if (objValue instanceof String && !((String)objValue).isEmpty()){
						try {
							data = JsonHelp.parseJson((String)objValue);
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
				CodeFlowBuilder.this.isEdit = true;
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
				return CodeFlowBuilder.this;
			}

			@Override
			public void onEdit(int row, int col) {
				// TODO Auto-generated method stub
				
			}

			
		}, canvas, mainControl, table);
		splitPane_1.setRightComponent(table);
		
		JPanel header = new JPanel();
		contentPane.add(header, BorderLayout.NORTH);
		
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
				try {
					onPublish(new HashMap<>(), mainControl.getSelectDPI());
					EditorEnvironment.showMessage(null, "导出运行流程图成功！", "恭喜");
				} catch (Exception e2) {
					e2.printStackTrace();
					EditorEnvironment.showException(e2);
				}
			}
		});
		header.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		header.add(button_3);
		
		JButton button_4 = new JButton("生成");
		button_4.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if ((uiid == null || uiid.isEmpty()) && (workflowid == null || workflowid.isEmpty())){
					EditorEnvironment.showMessage("未关联任何界面及节点，无法生成！");
					return;
				}
				
				try {
					
					String jsName = uiid;
					if (workflowid != null && !workflowid.isEmpty())
						jsName = EditorEnvironment.getModelNodeName(workflowid);
					else{
						WorkflowNode workflowNode = EditorEnvironment.getModelNodeFromUI(uiid);
						
						if (workflowNode != null){
							jsName = workflowNode.name;
						}
					}
					
					File path;
					path = new File(EditorEnvironment.getPublishWebPath(), "client/userjs");
					ParseApp app = new ParseApp(new File(path, jsName + ".js").getAbsolutePath());
					List<String> commands = app.parseJS();
					canvas.clear();
					JSCommandNode start = (JSCommandNode)canvas.add(jsName, JSCommandNode.class, null, null);
					start.command = jsName;
					start.setLocation(new Point(100, canvas.getHeight() / 2));
					int top = 100;
					for (String command : commands) {
						PhpCommandNode node = (PhpCommandNode) canvas.add(command, PhpCommandNode.class, null, null);
						node.command = command;
						node.title = command;
						node.setLocation(new Point(300, top));
						canvas.linkTo(start, node);
						top +=  node.getHeight() * 2;
					}
					
					canvas.repaint();
				} catch (Exception e2) {
					e2.printStackTrace();
					EditorEnvironment.showException(e2);
					return;
				}
				
			}
		});
		header.add(button_4);
		
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
		nodetype.setModel(new DefaultComboBoxModel<String>(AppWorkflowNode.names));
		nodetype.setSelectedIndex(0);
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
		
		JButton button_2 = new JButton("关联UI");
		button_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				UISelectDialog.Result result = UISelectDialog.showDialog(mainControl, uiid, null);
				if (result != null){
					uiid = result.id;
				}
				
			}
		});
		panel_1.add(button_2);
		JButton button = new JButton("编辑");
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel_1.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ModelResult result = KeyValueSelector.show(null, mainControl, null, null, new Object[][]{
					{"流程编号", canvas.getPageConfig().id},
					{"流程名称", canvas.getPageConfig().name},
					{"流程说明", canvas.getPageConfig().memo},
				}, new Object[]{
					"属性", "值"
				}, null, new int[]{0}, false);
				
				DefaultTableModel tableModel = result.isok ? result.model : null;
				
				if (tableModel != null){
					canvas.getPageConfig().id = (String)tableModel.getValueAt(0, 1);
					canvas.getPageConfig().name = (String)tableModel.getValueAt(1, 1);
					canvas.getPageConfig().memo = (String)tableModel.getValueAt(2, 1);
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
		
		splitPane.setDividerLocation(0.25);
		splitPane.setResizeWeight(0.25);
		splitPane_1.setDividerLocation(0.75);
		splitPane_1.setResizeWeight(0.75);
		addComponentListener(new ComponentAdapter(){
            public void componentResized(ComponentEvent e) {
				splitPane.setDividerLocation(splitPane.getResizeWeight());
				splitPane.setResizeWeight(splitPane.getResizeWeight());
				splitPane_1.setDividerLocation(splitPane_1.getResizeWeight());
				splitPane_1.setResizeWeight(splitPane_1.getResizeWeight());
            }
        });

		ctm = new ControlTreeManager(tree, canvas, "流程模块");
	}

	public void save(boolean needPrompt){
		try {
			if (file == null)
				return;
			
			canvas.setFile(file);
			canvas.save();
			isEdit = false;
			
			EditorEnvironment.lockFile(canvas.getFile());
		} catch (Exception e) {
			e.printStackTrace();
			EditorEnvironment.showMessage(this, e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	boolean needSetPageMode = true;
	private PropertyPanel table;
	File file;
	public void reload(){
		if (canvas.getWidth() == 0 || canvas.getHeight() == 0){
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					reload();
				}
			});
			return;
		}
		
		canvas.setFile(file);
		try {
			
			canvas.load(null, new IInitPage() {
				
				@Override
				public void onPage(PageConfig pageConfig) {
					pageConfig.setPageSizeMode();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		selectPageMode(canvas.getPageConfig());
		isEdit = false;
		
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
			if (EditorEnvironment.showConfirmDialog(
					"当前未保存的工作都将丢失，是否关闭当前窗口？", "关闭",
					JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
				return;
		
		reload();	
	}

	@Override
	public void onClose() {
		if (canvas.getFile() != null)
			EditorEnvironment.unlockFile(canvas.getFile());
		dispose();
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
		Object[] datas = (Object[])param;
		this.file = (File)datas[0];
		this.uiid = (String)datas[1];
		this.workflowid = (String)datas[2];
		onLoad();
	}

	@Override
	public void onEnd(ChildForm subForm) {
	}

	@Override
	public void onPublish(HashMap<String, DrawNode> uikeysWorkflowNodes, Object param) throws Exception {

	}

}

