package com.wh.form;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
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
import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeMap;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSpinner.NumberEditor;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wh.control.ChangeCanvasConfigure;
import com.wh.control.ControlTreeManager;
import com.wh.control.EditorEnvironment;
import com.wh.control.IconComboBoxItem;
import com.wh.control.IconComboBoxRender;
import com.wh.control.ScrollToolBar;
import com.wh.control.datasource.define.DataSource;
import com.wh.control.grid.ButtonColumn.ButtonLabel;
import com.wh.control.grid.design.DefaultPropertyClient;
import com.wh.control.grid.design.DefaultPropertyClient.IUpdate;
import com.wh.control.grid.design.DefaultPropertyClient.PropertyInfo;
import com.wh.control.grid.design.PropertyPanel;
import com.wh.control.modelsearch.ModelSearchView;
import com.wh.dialog.editor.JsonEditorDialog;
import com.wh.dialog.selector.KeyValueSelector;
import com.wh.dialog.selector.KeyValueSelector.ModelResult;
import com.wh.draws.DrawCanvas;
import com.wh.draws.DrawCanvas.ChangeType;
import com.wh.draws.DrawCanvas.ICreateNodeSerializable;
import com.wh.draws.DrawCanvas.IDataSerializable;
import com.wh.draws.DrawCanvas.IInitPage;
import com.wh.draws.DrawCanvas.INode;
import com.wh.draws.DrawCanvas.IOnPageSizeChanged;
import com.wh.draws.DrawCanvas.IScroll;
import com.wh.draws.DrawCanvas.PageConfig;
import com.wh.draws.DrawCanvas.PageSizeMode;
import com.wh.draws.DrawNode;
import com.wh.draws.UICanvas;
import com.wh.draws.UINode;
import com.wh.draws.WorkflowNode;
import com.wh.draws.control.StatckTreeElement;
import com.wh.draws.drawinfo.ComboInfo;
import com.wh.draws.drawinfo.DivInfo.DivType;
import com.wh.draws.drawinfo.DrawInfo;
import com.wh.draws.drawinfo.DrawInfoDefines;
import com.wh.draws.drawinfo.ImageInfo;
import com.wh.draws.drawinfo.MainTreeInfo;
import com.wh.draws.drawinfo.ReportInfo;
import com.wh.draws.drawinfo.SubUIInfo;
import com.wh.draws.drawinfo.TreeInfo;
import com.wh.system.tools.FileHelp;
import com.wh.system.tools.JsonHelp;

public class UIBuilder extends ChildForm implements IMainMenuOperation, ISubForm {
	private static final String treeName = "窗体";

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

	public UICanvas canvas = new UICanvas();
	private JComboBox<IconComboBoxItem> controls;
	private JButton addButton;
	private JButton delButton;
	private JComboBox<String> pageMode;
	private JLabel label;
	private JLabel label_1;
	private JSplitPane splitPane;
	private JTree tree;
	private JPanel panel;
	private JScrollBar hScrollBar;
	private JScrollBar vScrollBar;
	private JButton alignButton;
	private JPopupMenu popupMenu;
	private JSplitPane splitPane_1;
	private JPanel panel_1;
	private JPanel panel_2;
	private PropertyPanel table;
	private JButton button;
	private JButton button_1;
	private JButton button_2;
	private JButton button_3;
	private JButton button_4;
	private JPanel panel_3;
	private JLabel memo;
	private JPanel panel_4;
	private JButton button_5;
	private JScrollPane scrollPane;
	private JLabel label_2;
	private JSpinner customwidth;
	private JLabel label_3;
	private JSpinner customheight;
	private JLabel label_8;
	private JButton button_11;
	private JComboBox<String> setCombo;
	private JButton setMainNavButton;
	private JButton refreshOrderButton;
	private JCheckBox checkBox_1;
	private JButton button_15;
	private JComboBox<String> androidDpi;
	private JLabel label_6;
	private JSpinner dpiControl;
	private JComboBox<String> dpiTypes;

	ControlTreeManager ctm;

	// public DrawNode workflowNode;
	public HashMap<String, DrawNode> workflowNodes;
	public String workflowRelationTitle;
	public WorkflowNode workflowNode;
	public File uiFile;

	boolean notFire = false;

	public void changePageToCustom() {
		notFire = true;
		pageMode.setSelectedItem(DrawCanvas.pageSizeToString(PageSizeMode.psCustom));
		notFire = false;
		changePage(PageSizeMode.psCustom);
	}

	public void changePage() {
		if (notFire)
			return;

		String text = (String) pageMode.getSelectedItem();
		if (text == null)
			return;

		PageSizeMode pageSize = DrawCanvas.StringToPageSize(text);
		changePage(pageSize);
	}

	public Dimension getCustomPageSize() {
		return new Dimension((int) customwidth.getValue(), (int) customheight.getValue());
	}

	public void changePage(PageSizeMode pageSize) {
		int width = 0;
		int height = 0;
		int dpi = (int) dpiControl.getValue();
		if (dpiControl.getEditor().isShowing()) {
			NumberEditor editor = (NumberEditor) dpiControl.getEditor();
			try {
				editor.commitEdit();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		if (pageSize == PageSizeMode.psCustom) {
			if (customwidth.getEditor().isShowing()) {
				NumberEditor editor = (NumberEditor) customwidth.getEditor();
				try {
					editor.commitEdit();
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			if (customheight.getEditor().isShowing()) {
				NumberEditor editor = (NumberEditor) customheight.getEditor();
				try {
					editor.commitEdit();
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			width = (int) customwidth.getValue();
			height = (int) customheight.getValue();
			if (width <= 0)
				width = 1024;

			if (height <= 0)
				height = 768;
		}
		canvas.getPageConfig().deviceDPI = dpi;
		canvas.getPageConfig().setPageSizeMode(pageSize, width, height);
		customwidth.setValue(canvas.getConfigPageSize().width);
		customheight.setValue(canvas.getConfigPageSize().height);
		for (DrawNode tmp : canvas.nodes.values()) {
			UINode node = (UINode) tmp;
			node.invalidRect();
		}

		canvas.repaint();
		isEdit = true;
	}

	protected List<DataSource> getDataSources() {
		return DataSourceManager.getDSM().gets(canvas.getDataSources());
	}

	protected void showFormProperty() {
		try {
			JSONObject json = new JSONObject();
			canvas.getPageConfig().toJson(json);
			json.remove("width");
			json.remove("height");
			json.remove("size");
			DefaultPropertyClient.propertyBuilder(table, json, canvas.getPageConfig(), getDataSources());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected Object showButtonEditJsonEditor(ButtonLabel ob) throws JSONException {
		PropertyInfo propertyInfo = (PropertyInfo) ob.sender;
		UINode node = (UINode) propertyInfo.getSender();
		Object value = JsonEditorDialog.showJsonEditor(mainControl, ob.getValue(), node.getDrawInfo().typeName());
		ob.textField.setText(value == null ? "" : value.toString());
		ob.setValue(value);
		return value;
	}

	protected void keyPressed(KeyEvent e) {
		if (e.getSource() == table.getTable() && e.isAltDown()) {
			TableCellEditor editor = table.getTable().getCellEditor();
			if (editor != null)
				editor.stopCellEditing();
			return;
		}

		if (!canvas.hasFocus())
			canvas.keyPressed(e);
	}

	protected void keyReleased(KeyEvent e) {
		if (!canvas.hasFocus())
			canvas.keyReleased(e);
	}

	protected UINode searchTreeForNode(String text, DefaultMutableTreeNode root, boolean useid) {
		text = text.toUpperCase();
		for (int i = 0; i < root.getChildCount(); i++) {
			DefaultMutableTreeNode sub = (DefaultMutableTreeNode) root.getChildAt(i);
			UINode node = (UINode) sub.getUserObject();
			if (node.getDrawInfo() == null)
				continue;

			String name = node.getDrawInfo().name.toUpperCase();
			if (useid)
				name = node.getDrawInfo().id.toUpperCase();

			if (name.contains(text)) {
				return node;
			}

			if (sub.getChildCount() > 0) {
				UINode result = searchTreeForNode(text, sub, useid);
				if (result != null)
					return result;
			}
		}

		return null;
	}

	protected void doTreeSelected(TreeNode node) {
		if (node != null) {
			if (node == null || node == tree.getModel().getRoot()) {
				showFormProperty();
				return;
			}
		}
		ctm.selectCanvasNode(node);
	}

	protected final void setZOrders(Collection<DrawNode> nodes, int order) {
		for (DrawNode node : nodes) {
			node.zOrder = order;
		}
	}

	protected void refreshOrder() {
		TreeMap<Integer, List<DrawNode>> nodes = new TreeMap<>();
		for (DrawNode node : canvas.getNodes()) {
			List<DrawNode> list;
			int order = canvas.getStackTreeManager().getParentZOrder(node.id);
			if (nodes.containsKey(order)) {
				list = nodes.get(order);
			} else {
				list = new ArrayList<>();
				nodes.put(order, list);
			}

			list.add(node);
		}

		NavigableSet<Integer> orders = nodes.descendingKeySet();
		if (orders == null || orders.size() == 0)
			return;

		int maxOrder = orders.size() - 1;
		for (Integer order : orders) {
			setZOrders(nodes.get(order), maxOrder--);
		}
	}

	public void searchControlTree(String text, boolean searchId) {
		if (text == null || text.isEmpty())
			return;

		if (tree.getModel().getRoot() == null)
			return;

		UINode node = searchTreeForNode(text, (DefaultMutableTreeNode) tree.getModel().getRoot(), searchId);
		if (node != null) {
			canvas.setSelected(node);
		}
	}

	public UIBuilder(IMainControl mainControl) throws Exception {
		super(mainControl);
		setDoubleBuffered(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1971, 791);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JPanel toolPanel = new JPanel();
		JPanel scrollButtonPanel = new JPanel();
		JScrollPane toolbarScrollBar = new JScrollPane();
		JToolBar toolBar = new JToolBar();

		contentPane.add(toolPanel, BorderLayout.NORTH);
		toolPanel.setLayout(new BorderLayout(0, 0));
		toolPanel.add(toolbarScrollBar, BorderLayout.CENTER);
		toolbarScrollBar.setViewportView(toolBar);
		new ScrollToolBar(toolPanel, scrollButtonPanel, toolbarScrollBar, toolBar);

		popupMenu = new JPopupMenu();
		for (String alignText : DrawCanvas.ALIGNNAMES) {
			if (alignText.equals("-")) {
				popupMenu.addSeparator();
				continue;
			}
			JMenuItem menu = new JMenuItem(alignText + "          ");

			menu.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					JMenuItem menuItem = (JMenuItem) e.getSource();
					canvas.alignControl.align(menuItem.getText());
				}
			});
			popupMenu.add(menu);
		}
		DefaultComboBoxModel<IconComboBoxItem> model = new DefaultComboBoxModel<>();
		for (String typename : DrawInfoDefines.TypeNames) {
			ImageIcon icon = new ImageIcon(UINode.getImage(typename));
			model.addElement(new IconComboBoxItem(typename, icon));
		}

		splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.15);
		splitPane.setOneTouchExpandable(true);
		splitPane.setBackground(Color.WHITE);
		contentPane.add(splitPane, BorderLayout.CENTER);

		panel = new JPanel();
		panel.setBackground(Color.WHITE);
		splitPane.setRightComponent(panel);
		panel.setLayout(new BorderLayout(0, 0));

		splitPane_1 = new JSplitPane();
		splitPane_1.setResizeWeight(0.8);
		splitPane_1.setOneTouchExpandable(true);
		splitPane_1.setBackground(Color.WHITE);
		panel.add(splitPane_1, BorderLayout.CENTER);

		panel_1 = new JPanel();
		panel_1.setBackground(Color.WHITE);
		splitPane_1.setLeftComponent(panel_1);
		panel_1.setLayout(new BorderLayout(0, 0));
		panel_1.add(canvas, BorderLayout.CENTER);

		canvas.onPageSizeChanged = new IOnPageSizeChanged() {

			@Override
			public void onChanged(Point max) {
				hScrollBar.setMaximum(max.x);
				vScrollBar.setMaximum(max.y);
				hScrollBar.setValue(0);
				hScrollBar.setValue(0);
			}
		};

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

		hScrollBar = new JScrollBar();
		hScrollBar.setPreferredSize(new Dimension(20, 20));
		panel_1.add(hScrollBar, BorderLayout.SOUTH);
		hScrollBar.setUnitIncrement(10);
		hScrollBar.setOrientation(JScrollBar.HORIZONTAL);
		hScrollBar.setMaximum(99999);

		vScrollBar = new JScrollBar();
		vScrollBar.setPreferredSize(new Dimension(20, 48));
		panel_1.add(vScrollBar, BorderLayout.EAST);
		vScrollBar.setUnitIncrement(10);
		vScrollBar.setMaximum(99999);

		panel_2 = new JPanel();
		panel_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel_2.setBackground(Color.WHITE);
		splitPane_1.setRightComponent(panel_2);
		panel_2.setLayout(new BorderLayout(0, 0));

		table = new PropertyPanel();
		table.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		table.setBackground(Color.WHITE);
		table.onClientEvent = new DefaultPropertyClient(new IUpdate() {

			List<DrawNode> selectNodes;

			@Override
			public Object showButtonEditJsonEditor(String name, ButtonLabel buttonEdit) throws JSONException {
				return UIBuilder.this.showButtonEditJsonEditor(buttonEdit);
			}

			@Override
			public void setEditState(boolean isEdit) {
				UIBuilder.this.isEdit = true;
			}

			@Override
			public void onUpdateEnd(Object obj, String attrName, Object oldValue, Object attrValue) {
				if ((attrName.compareTo("id") == 0 || attrName.compareTo("name") == 0
						|| attrName.compareTo("title") == 0))
					UIBuilder.this.ctm.refreshTree();

				if (obj instanceof DrawInfo && selectNodes != null) {
					for (DrawNode node : selectNodes) {
						try {
							UINode uiNode = (UINode) node;
							Object valObject = uiNode.getDrawInfo();
							Class<?> c = valObject.getClass();
							Field field = c.getField(attrName);
							field.setAccessible(true);
							field.set(valObject, attrValue);
							uiNode.invalidRect();
							if (attrName.equalsIgnoreCase("divType")) {
								DivType oldDivType = (DivType) oldValue;
								DivType newDivType = (DivType) attrValue;
								if (oldDivType == DivType.dtDiv && newDivType != DivType.dtDiv)
									canvas.getStackTreeManager().removeTree(uiNode.id, true, false);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}

				if (attrName.compareTo("dataSource") == 0)
					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {
							canvas.setSelected(canvas.getSelected());
						}
					});
			}

			@Override
			public String getUIID() {
				return UIBuilder.this.getUIID();
			}

			@Override
			public Component getParent() {
				return UIBuilder.this;
			}

			@Override
			public void onEdit(int row, int col) {
				selectNodes = canvas.getSelecteds();
			}

		}, canvas, mainControl, table);
		panel_2.add(table, BorderLayout.CENTER);
		vScrollBar.addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent e) {
				canvas.setOffset(new Point(canvas.getOffset().x, -e.getValue()));
			}
		});
		hScrollBar.addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent e) {
				canvas.setOffset(new Point(-e.getValue(), canvas.getOffset().y));
			}
		});
		canvas.nodeEvent = new INode() {
			boolean needUpdateGrid = false;

			protected void updateGrid(DrawNode[] nodes) {
				if (nodes.length > 1)
					return;

				ctm.selectTreeNode(nodes.length == 0 ? null : nodes[0]);
				if (nodes.length > 0)
					try {
						DefaultPropertyClient.propertyBuilder(table, (UINode) nodes[nodes.length - 1], workflowNode,
								workflowNodes, workflowRelationTitle, getDataSources());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}

			@Override
			public void onChange(DrawNode[] nodes, ChangeType ct) {
				switch (ct) {
				case ctBackEdited:
					isEdit = true;
					return;
				case ctBackspacing:
					ctm.refreshTree();
					break;
				case ctRemove:
					if (nodes != null) {
						for (DrawNode drawNode : nodes) {
							ctm.removeFromTree(drawNode);
						}
						isEdit = true;
					}
					break;
				case ctAdd:
					if (nodes != null) {
						for (DrawNode node : nodes) {
							ctm.addToTree((UINode) node);
						}
						isEdit = true;
					}
					break;
				case ctPaste:
					ctm.refreshTree();
				case ctMove:
				case ctResize:
					needUpdateGrid = true;
				case ctAddLink:
				case ctRemoveLink:
				case ctBringToTop:
				case ctSendToBack:
					isEdit = true;
					break;
				case ctDeselected:
					if (canvas.getSelected() == null)
						showFormProperty();
					break;
				case ctSelected:
				case ctSelecteds:
					if (canvas.isMuiltSelecting())
						break;
					UINode node = (UINode) canvas.getSelected();
					if (node != null)
						setMainNavButton.setEnabled(node != null && node.getDrawInfo() instanceof TreeInfo);
					updateGrid(nodes);
				case ctKeyUp:
				case ctMouseRelease:
					if (needUpdateGrid) {
						updateGrid(nodes);
						canvas.invalidate(Arrays.asList(nodes));
						canvas.repaint();
					}
					needUpdateGrid = false;
					break;
				default:
					break;
				}
			}

			@Override
			public void DoubleClick(DrawNode node) {
				if (node.getCanvas() != canvas)
					return;

				UINode uiNode = (UINode) node;
				if (uiNode.getDrawInfo() instanceof ReportInfo) {
					mainControl.openReportEditor(UIBuilder.this, uiNode);
				} else if (uiNode.getDrawInfo() instanceof SubUIInfo) {
					SubUIInfo info = (SubUIInfo) uiNode.getDrawInfo();
					File file = EditorEnvironment.getUIFileForUIID(info.uiInfo);
					try {
						mainControl.openUIBuilder(file, null);
					} catch (Exception e) {
						e.printStackTrace();
						EditorEnvironment.showException(e);
					}
				}
			}

			@Override
			public void Click(DrawNode node) {
				// ctm.selectTreeNode(node);
			}

		};

		canvas.onScroll = new IScroll() {

			@Override
			public void onScroll(int x, int y) {
				hScrollBar.setValue(Math.abs(x));
				vScrollBar.setValue(Math.abs(y));
			}
		};

		tree = new ModelSearchView.TreeModelSearchView(new DefaultTreeModel(new DefaultMutableTreeNode(treeName)));
		tree.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		scrollPane = new JScrollPane();
		scrollPane.setPreferredSize(new Dimension(0, 0));
		scrollPane.setMinimumSize(new Dimension(20, 20));
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		splitPane.setLeftComponent(scrollPane);
		scrollPane.setViewportView(tree);
		tree.setRowHeight(30);
		tree.setCellRenderer(new ControlTreeManager.UITreeCellRender());
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				if (canvas.isMuiltSelecting())
					return;

				if (e.getNewLeadSelectionPath() != null) {
					TreeNode node = (TreeNode) e.getNewLeadSelectionPath().getLastPathComponent();
					doTreeSelected(node);
				}
			}
		});
		tree.setExpandsSelectedPaths(true);
		tree.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				doTreeSelected(tree.getSelectionPath() == null ? null
						: (TreeNode) tree.getSelectionPath().getLastPathComponent());
			}
		});

		panel_3 = new JPanel();
		panel_3.setBorder(new LineBorder(Color.LIGHT_GRAY));
		panel_3.setBackground(Color.WHITE);
		contentPane.add(panel_3, BorderLayout.SOUTH);
		panel_3.setLayout(new BorderLayout(0, 0));

		memo = new JLabel("   ");
		memo.setMinimumSize(new Dimension(200, 21));
		memo.setBackground(Color.WHITE);
		memo.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		memo.setHorizontalAlignment(SwingConstants.CENTER);
		panel_3.add(memo, BorderLayout.CENTER);

		panel_4 = new JPanel();
		panel_4.setBackground(Color.WHITE);
		panel_3.add(panel_4, BorderLayout.EAST);

		button_5 = new JButton("编辑");
		button_5.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ModelResult result = KeyValueSelector.show(null, mainControl, null, null,
						new Object[][] { { "界面标题", canvas.getPageConfig().title },
								{ "界面说明", canvas.getPageConfig().memo },
								{ "入口参数", canvas.getPageConfig().data, "json" }, },
						new Object[] { "属性", "值" }, null, new int[] { 0 }, false);

				DefaultTableModel tableModel = result.isok ? result.model : null;

				if (tableModel != null) {
					canvas.getPageConfig().title = (String) tableModel.getValueAt(0, 1);
					canvas.getPageConfig().memo = (String) tableModel.getValueAt(1, 1);
					canvas.getPageConfig().data = (String) tableModel.getValueAt(2, 1);
					updateDesc();
					canvas.repaint();
					isEdit = true;

				}
			}
		});
		panel_4.add(button_5);

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

		ctm = new ControlTreeManager(tree, canvas, treeName);

		button_11 = new JButton(" 转换 ");

		checkBox_1 = new JCheckBox("仅可视区域");
		checkBox_1.setOpaque(false);
		checkBox_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				canvas.useCheckConstraint = checkBox_1.isSelected();
			}
		});
		checkBox_1.setSelected(true);
		checkBox_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(checkBox_1);

		checkBox = new JCheckBox("显示控制组件");
		checkBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				canvas.setDrawControlInfo(checkBox.isSelected());
				canvas.repaint();
			}
		});
		checkBox.setSelected(true);
		checkBox.setOpaque(false);
		checkBox.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(checkBox);

		setCombo = new JComboBox<String>();
		setCombo.setMaximumSize(new Dimension(100, 23));
		setCombo.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(setCombo);

		androidDpi = new JComboBox<String>();
		androidDpi.setMaximumSize(new Dimension(100, 23));
		androidDpi.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_11.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_11);

		label_8 = new JLabel("| ");
		toolBar.add(label_8);

		label = new JLabel("页面：");
		label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(label);

		pageMode = new JComboBox<String>(new DefaultComboBoxModel<String>(DrawCanvas.PAGENAMES));
		pageMode.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		Dimension d = pageMode.getPreferredSize();
		pageMode.setMaximumSize(new Dimension(100, d.height));
		pageMode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changePage();
			}
		});
		toolBar.add(pageMode);

		alignButton = new JButton(" 位置 ");
		alignButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		alignButton.setIconTextGap(0);
		alignButton.setHorizontalAlignment(SwingConstants.LEADING);
		alignButton.setIcon(new ImageIcon(getClass().getResource("/image/align.png")));
		alignButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (popupMenu.isVisible()) {
					popupMenu.setVisible(false);
					return;
				}
				Point pt = alignButton.getLocation();
				popupMenu.show(alignButton.getParent(), pt.x, pt.y + alignButton.getHeight());
			}
		});

		label_2 = new JLabel(" 宽度 ");
		label_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(label_2);

		customwidth = new JSpinner();
		customwidth.setPreferredSize(new Dimension(80, 28));
		customwidth.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		// ((NumberEditor)
		// customwidth.getEditor()).getTextField().addKeyListener(new
		// KeyAdapter() {
		// @Override
		// public void keyPressed(KeyEvent e) {
		// if (e.getKeyCode() == KeyEvent.VK_ENTER) {
		// changePageToCustom();
		// }
		// }
		// });
		customwidth.setMaximumSize(new Dimension(80, 32767));
		customwidth.setMinimumSize(new Dimension(80, 0));
		toolBar.add(customwidth);

		label_3 = new JLabel(" 高度 ");
		label_3.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(label_3);

		customheight = new JSpinner();
		customheight.setPreferredSize(new Dimension(80, 28));
		customheight.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		// ((NumberEditor)
		// customheight.getEditor()).getTextField().addKeyListener(new
		// KeyAdapter() {
		// @Override
		// public void keyPressed(KeyEvent e) {
		// if (e.getKeyCode() == KeyEvent.VK_ENTER)
		// changePageToCustom();
		// }
		// });
		customheight.setMaximumSize(new Dimension(80, 32767));
		customheight.setMinimumSize(new Dimension(80, 0));
		toolBar.add(customheight);

		button_15 = new JButton(" 应用 ");
		button_15.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changePageToCustom();
			}
		});

		label_6 = new JLabel(" 密度 ");
		label_6.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(label_6);

		dpiControl = new JSpinner();
		dpiControl.setPreferredSize(new Dimension(80, 28));
		dpiControl.setMinimumSize(new Dimension(80, 0));
		dpiControl.setMaximumSize(new Dimension(80, 32767));
		dpiControl.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(dpiControl);

		dpiTypes = new JComboBox<String>(new DefaultComboBoxModel<String>(new String[] { "安卓", "显示器", "自定义" }));
		dpiTypes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				switch (((JComboBox<?>) e.getSource()).getSelectedIndex()) {
				case 0:
					dpiControl.setValue(160);
					dpiControl.setEnabled(false);
					break;
				case 1:
					dpiControl.setValue(Toolkit.getDefaultToolkit().getScreenResolution());
					dpiControl.setEnabled(false);
					break;
				case 2:
					dpiControl.setEnabled(true);
					break;
				default:
					break;
				}
			}
		});
		dpiTypes.setSelectedIndex(1);
		dpiTypes.setMaximumSize(new Dimension(100, 23));
		dpiTypes.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(dpiTypes);
		button_15.setToolTipText("将自定义尺寸换算为android尺寸");
		button_15.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_15);

		toolBar.addSeparator();

		label_1 = new JLabel(" 控件：");
		label_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(label_1);

		controls = new JComboBox<>();
		controls.setMaximumRowCount(10);
		controls.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		controls.setRenderer(new IconComboBoxRender<IconComboBoxItem>());
		controls.setModel(model);
		d = controls.getPreferredSize();
		controls.setMaximumSize(new Dimension(100, d.height));
		controls.setSelectedIndex(0);
		toolBar.add(controls);
		addButton = new JButton("添加");
		addButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				IconComboBoxItem item = (IconComboBoxItem) controls.getSelectedItem();
				String name = item.name;
				int index = 0;
				while (canvas.getNode(name + String.valueOf(index++)) != null) {
				}

				name = name + String.valueOf(--index);
				canvas.setSelected(canvas.add(name, item.name));
			}
		});
		toolBar.add(addButton);
		delButton = new JButton("删除");
		delButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		delButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				canvas.removeAndHint();
			}
		});
		toolBar.add(delButton);
		toolBar.addSeparator();
		setMainNavButton = new JButton("设置为主导航树 ");
		setMainNavButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					UINode node = (UINode) canvas.getSelected();
					if (node == null) {
						EditorEnvironment.showMessage("未选择节点！");
						return;
					}
					if (!(node.getDrawInfo() instanceof MainTreeInfo)
							&& EditorEnvironment.getMainNavTreeFile().exists()) {
						if (EditorEnvironment.showConfirmDialog("由于当前选定的控件不是【主导航树控件】，继续将覆盖已经存在的著导航树设置，是否继续？",
								JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
							return;
					}
					canvas.setMainNavTree(getUIID(), node.id);
					EditorEnvironment.showMessage("成功设置主导航树！");
				} catch (Exception e1) {
					e1.printStackTrace();
					EditorEnvironment.showException(e1);
				}
			}
		});
		setMainNavButton.setEnabled(false);
		setMainNavButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(setMainNavButton);

		toolBar.addSeparator();
		toolBar.add(alignButton);
		button_3 = new JButton(" 置前 ");
		button_3.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				canvas.bringToTop();
			}
		});
		toolBar.add(button_3);

		button_4 = new JButton(" 置后 ");
		button_4.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				canvas.sendToBack();
			}
		});
		toolBar.add(button_4);

		refreshOrderButton = new JButton("优化");
		refreshOrderButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshOrder();
			}
		});
		refreshOrderButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(refreshOrderButton);

		toolBar.addSeparator(new Dimension(5, 0));

		pageMode.setSelectedIndex(1);
		toolBar.addSeparator();
		button = new JButton("复制");
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				canvas.copy(false);
			}
		});
		toolBar.add(button);

		button_1 = new JButton("粘贴");
		button_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				canvas.paste();
			}
		});
		toolBar.add(button_1);

		toolBar.addSeparator();
		button_2 = new JButton("撤销");
		button_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					canvas.getACM().popCommand();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		toolBar.add(button_2);

		button_11.addActionListener(
				new ChangeCanvasConfigure(canvas, toolBar, androidDpi, setCombo, new ChangeCanvasConfigure.IUpdate() {

					@Override
					public void notifyEdit(boolean isEdit) {
						UIBuilder.this.isEdit = isEdit;
					}

					@Override
					public void changeCanvasToCustomMode() {
						changePageToCustom();
					}
				}));

	}

	public String getUIName() {
		if (this.uiFile != null)
			return uiFile.getName();

		File uiFile = EditorEnvironment.getUIFile(workflowNode.id, false);
		if (uiFile == null)
			return null;
		return uiFile.getName();
	}

	public String getUIID() {
		if (this.uiFile != null)
			return FileHelp.removeExt(uiFile.getName());

		return EditorEnvironment.getUIID(workflowNode.id);
	}

	public void refreshUIFile() {
		uiFile = EditorEnvironment.getUIFile(workflowNode.id, false);
	}

	@Override
	public void onSave() {
		try {
			if (!isEdit)
				return;

			File f = EditorEnvironment.getProjectFile(EditorEnvironment.UI_Dir_Name, getUIName());
			if (f == null)
				return;

			canvas.setFile(f);
			canvas.save();

			refreshUIFile();
			File uiFile = this.uiFile;
			String key = FileHelp.removeExt(uiFile.getName());
			if (SubUIInfo.subInfos.containsKey(key))
				SubUIInfo.subInfos.remove(key);
			isEdit = false;

			EditorEnvironment.lockFile(canvas.getFile());

		} catch (Exception e) {
			e.printStackTrace();
			EditorEnvironment.showMessage(this, e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	public void onLoad() {
		load(true);
	}

	public void updateDesc() {
		memo.setText(canvas.getPageConfig().memo);
		if (memo.getText() != null)
			memo.setToolTipText(memo.getText());

		mainControl.updateUIButtonTitle(UIBuilder.this, workflowRelationTitle + "[界面编辑]");
	}

	public void load(boolean needHint) {
		try {
			File f = EditorEnvironment.getProjectFile(EditorEnvironment.UI_Dir_Name, getUIName());
			if (f == null)
				return;

			if (!f.exists()) {
				if (needHint)
					EditorEnvironment.showMessage(this, "文件不存在！", "提示", JOptionPane.WARNING_MESSAGE);
				return;
			}

			canvas.setFile(f);
			canvas.load(new ICreateNodeSerializable() {

				@Override
				public DrawNode newDrawNode(JSONObject json) {
					return new UINode(canvas);
				}

				@Override
				public IDataSerializable getUserDataSerializable(DrawNode node) {
					return null;
				}
			}, new IInitPage() {

				@Override
				public void onPage(PageConfig pageConfig) {
					if (pageConfig.getCurPageSizeMode() == PageSizeMode.psCustom) {
						Dimension size = canvas.getConfigPageSize();
						customwidth.setValue(size.width == 0 ? pageConfig.width : size.width);
						customheight.setValue(size.height == 0 ? pageConfig.height : size.height);
						dpiControl.setValue(pageConfig.deviceDPI);
					}
					pageMode.setSelectedItem(pageConfig.getCurPageSizeMode());
				}
			});

			PageSizeMode pageSize = canvas.getPageConfig().getCurPageSizeMode();
			if (pageSize == PageSizeMode.psCustom) {
				customwidth.setValue(canvas.getPageConfig().width);
				customheight.setValue(canvas.getPageConfig().height);
			}
			pageMode.setSelectedItem(DrawCanvas.pageSizeToString(pageSize));
			updateDesc();

			isEdit = false;
		} catch (Exception e) {
			e.printStackTrace();
			EditorEnvironment.showMessage(this, e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	public void onClose() {
		if (canvas.getFile() != null)
			EditorEnvironment.unlockFile(canvas.getFile());
		dispose();
	}

	ChildForm parent;
	private JCheckBox checkBox;

	@SuppressWarnings("unchecked")
	@Override
	public void onStart(Object param) {
		Object[] data = (Object[]) param;
		this.workflowNode = (WorkflowNode) data[0];
		this.workflowRelationTitle = (String) data[1];
		this.workflowNodes = (HashMap<String, DrawNode>) data[2];
		File uiFile = null;
		if (data.length > 3) {
			this.uiFile = (File) data[3];
			uiFile = this.uiFile;
		} else
			uiFile = EditorEnvironment.getUIFile(workflowNode.id, true);
		if (uiFile == null)
			return;
		load(false);
		ctm.refreshTree();
	}

	@Override
	public void setParentForm(ChildForm form) {
		this.parent = form;
	}

	@Override
	public ChildForm getParentForm() {
		// TODO Auto-generated method stub
		return parent;
	}

	@Override
	public Object getResult() {
		return new Object[] { getUIName(), workflowNode, isEdit ? "true" : "false" };
	}

	public static File publish(String workflowRelationTitle, DrawNode workflowNode, Integer dpi,
			HashMap<String, DrawNode> workflowNodes, HashMap<String, DrawNode> uiKeyWorkflowNodes) throws Exception {

		File uiFile = EditorEnvironment.getUIFile(workflowNode.id, false);
		JSONObject json = publishJson(workflowRelationTitle, dpi, uiFile, uiKeyWorkflowNodes, true);
		String savename = null;
		json.put("workflow", workflowNode.toJson());

		JSONArray inputJson = new JSONArray();

		List<DrawNode> inputs = EditorEnvironment.getModelRelationInputs(workflowNode, workflowNodes,
				workflowRelationTitle);
		if (inputs != null && inputs.size() > 0) {
			for (DrawNode drawNode : inputs) {
				JSONObject data = new JSONObject();
				data.put("id", drawNode.id);
				data.put("name", drawNode.name);
				data.put("title", drawNode.title);
				inputJson.put(data);
			}
		}
		json.put("output", inputJson);

		File file = EditorEnvironment.getToolbarFile(workflowNode.id, false);
		if (file != null && file.exists()) {
			JSONArray object = (JSONArray) JsonHelp.parseJson(file, null);
			json.put("toolbar", object);
		}

		savename = EditorEnvironment.getPublish_UI_FileName(workflowNode.name);
		File uiPublishFile = EditorEnvironment.getPublishWebFile(EditorEnvironment.Publish_UI_Dir_Name, savename);
		JsonHelp.saveJson(uiPublishFile, json, null);
		return uiPublishFile;
	}

	public static File publish(String workflowRelationTitle, Integer dpi, File uiFile,
			HashMap<String, DrawNode> uiKeyWorkflowNodes) throws Exception {
		String id = FileHelp.removeExt(uiFile.getName());
		JSONObject json = publishJson(workflowRelationTitle, dpi, uiFile, uiKeyWorkflowNodes, false);
		JSONObject workflowdata = new JSONObject();
		workflowdata.put("name", id);
		workflowdata.put("id", id);
		json.put("workflow", workflowdata);
		json.put("output", "{}");
		json.put("toolbar", "{}");

		File uiPublishFile = EditorEnvironment.getPublishWebFile(EditorEnvironment.Publish_UI_Dir_Name,
				FileHelp.ChangeExt(uiFile.getName(), "js"));
		JsonHelp.saveJson(uiPublishFile, json, null);
		return uiPublishFile;
	}

	protected static JSONObject publishJson(String workflowRelationTitle, Integer dpi, File uiFile,
			HashMap<String, DrawNode> uiKeyWorkflowNodes, boolean needPutKey) throws Exception {
		File webPath;

		webPath = EditorEnvironment.getPublishWebPath();
		if (webPath == null) {
			throw new Exception("web路径未指定，请先完整发布项目后再试！");
		}

		File uiDir = new File(webPath, EditorEnvironment.Publish_UI_Dir_Name);
		if (!uiDir.exists())
			if (!uiDir.mkdirs()) {
				throw new Exception("无法建立路径:" + uiDir.getAbsolutePath());
			}

		DrawCanvas tmpCanvas = new UICanvas();
		tmpCanvas.setFile(uiFile);
		tmpCanvas.load(new ICreateNodeSerializable() {

			@Override
			public DrawNode newDrawNode(JSONObject json) {
				// TODO Auto-generated method stub
				return new UINode(null);
			}

			@Override
			public IDataSerializable getUserDataSerializable(DrawNode node) {
				// TODO Auto-generated method stub
				return null;
			}
		}, new IInitPage() {

			@Override
			public void onPage(PageConfig pageConfig) {
			}
		});

		List<DrawNode> nodes = tmpCanvas.getNodes();
		if (nodes == null)
			return null;

		if (dpi != null) {
			ChangeCanvasConfigure configure = new ChangeCanvasConfigure((UICanvas) tmpCanvas, null, null, null, null);
			configure.changeCanvasFontSize(nodes, DrawCanvas.getAndroidDpiScale(dpi), false);
		}

		Map<String, UINode> nameMap = new HashMap<>();

		for (DrawNode drawNode : nodes) {
			UINode node = (UINode) drawNode;
			nameMap.put(node.getDrawInfo().name, node);
		}

		Map<String, DrawNode> noComputes = new HashMap<>();
		for (DrawNode drawNode : nodes) {
			UINode node = (UINode) drawNode;
			if (node.getDrawInfo() instanceof ComboInfo) {
				ComboInfo info = (ComboInfo) node.getDrawInfo();
				if (info.popup == null || info.popup.isEmpty())
					continue;

				if (!nameMap.containsKey(info.popup)) {
					continue;
				}

				List<StatckTreeElement> lst = new ArrayList<>();
				UINode parentNode = nameMap.get(info.popup);
				tmpCanvas.getStackTreeManager().getChilds(parentNode.id, lst);
				noComputes.put(parentNode.id, parentNode);
				for (StatckTreeElement statckTreeElement : lst) {
					if (tmpCanvas.getNode(statckTreeElement.id) == null)
						continue;
					noComputes.put(statckTreeElement.id, tmpCanvas.getNode(statckTreeElement.id));
				}

			}
		}

		JSONObject json = new JSONObject();

		JSONObject pagejson = new JSONObject();
		tmpCanvas.getPageConfig().toJson(pagejson);
		json.put("page", pagejson);
		json.put("relationtitle", workflowRelationTitle);
		JSONArray nodedatas = new JSONArray();
		if (nodes != null && nodes.size() > 0) {
			for (DrawNode tmp : nodes) {
				UINode node = (UINode) tmp;
				JSONObject obj = node.toJson();
				if (node.getDrawInfo() instanceof ImageInfo && node.getDrawInfo().value != null) {
					File file = new File(node.getDrawInfo().value.toString());
					String name = file.getName();
					name = "image/" + name;
					JSONObject data = obj.getJSONObject("data");
					data.put("value", name);
					obj.put("data", data);
				} else if (node.getDrawInfo() instanceof SubUIInfo) {
					SubUIInfo info = (SubUIInfo) node.getDrawInfo();
					if (info.uiInfo != null && !info.uiInfo.isEmpty()) {
						WorkflowNode wNode = null;
						if (!uiKeyWorkflowNodes.containsKey(info.uiInfo)) {
							wNode = EditorEnvironment.getModelNodeFromUI(info.uiInfo);
							if (needPutKey)
								uiKeyWorkflowNodes.put(info.uiInfo, wNode);
						} else {
							wNode = (WorkflowNode) uiKeyWorkflowNodes.get(info.uiInfo);
						}

						if (wNode != null) {
							JSONObject data = obj.getJSONObject("data");
							data.put("uiInfo", wNode.name);
							obj.put("data", data);
						}
					}
				}

				if (noComputes.containsKey(node.id)) {
					obj.getJSONObject("data").put("nocompute", true);
				} else
					obj.getJSONObject("data").put("nocompute", false);

				nodedatas.put(obj);
			}

		}

		json.put("tree", tmpCanvas.getStackTreeManager().toJson());
		json.put("data", nodedatas);

		return json;
	}

	@Override
	public void onPublish(HashMap<String, DrawNode> uikeysWorkflowNodes, Object dpi) throws Exception {
		if (workflowNode != null)
			publish(workflowRelationTitle, workflowNode, (Integer) dpi, workflowNodes, uikeysWorkflowNodes);
		else
			publish(workflowRelationTitle, (Integer) dpi, uiFile, uikeysWorkflowNodes);
	}
}
