package com.wh.form;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wh.control.ControlSearchHelp;
import com.wh.control.ControlTreeManager;
import com.wh.control.EditorEnvironment;
import com.wh.control.EditorEnvironment.ITraverseDrawNode;
import com.wh.control.checkboxnode.CheckBoxListRenderer;
import com.wh.control.checkboxnode.CheckBoxNode;
import com.wh.control.checkboxnode.CheckBoxNodeConfig;
import com.wh.control.checkboxnode.ICheck;
import com.wh.control.grid.IconTableCellRender;
import com.wh.control.tree.TreeHelp;
import com.wh.control.tree.TreeHelp.INewNode;
import com.wh.control.tree.TreeHelp.ITraverseTree;
import com.wh.control.tree.TreeHelp.TreeItemInfo;
import com.wh.draws.DrawCanvas;
import com.wh.draws.DrawCanvas.ICreateNodeSerializable;
import com.wh.draws.DrawCanvas.IDataSerializable;
import com.wh.draws.DrawNode;
import com.wh.draws.UINode;
import com.wh.draws.drawinfo.DrawInfo;
import com.wh.draws.drawinfo.DrawInfoDefines;
import com.wh.system.tools.FileHelp;
import com.wh.system.tools.JsonHelp;

public class CSSEditor extends ChildForm implements IMainMenuOperation {
	
	private static final String TreeItemObject_UI = "ui";
	private static final String TreeItemObject_Control = "control";
	private static final String KEY_UI_ID = "uiid";
	private static final String KEY_WORKFLOW_ID = "wid";
	private static final String KEY_FILE = "uifile";
	private static final String KEY_TYPE = "type";
	private static final String KEY_MEMO = "memo";
	private static final String KEY_STYLE = "style";
	private static final String KEY_STYLECLASS = "styleclass";
	private static final String KEY_DRAWNODE_TYPE = "drawtype";
	private static final String KEY_DRAWNODE_Simple_TYPE = "simpletype";

	private static final String CSS_TEMPLATE_EXT = "cst";
	
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

	private ControlSearchHelp csh;
	private JSplitPane splitPane;
	private JTree tree;
	private JPanel panel;
	private JButton btncss_2;
	private JButton button_1;
	private JScrollPane scrollPane;
	private JSplitPane splitPane_1;
	private JSplitPane splitPane_2;
	private JPanel panel_2;
	private JLabel lblStyleclass;
	private JPanel panel_3;
	private JLabel lblstyle;
	private JScrollPane scrollPane_2;
	private JScrollPane scrollPane_3;
	private JEditorPane styleEditor;
	private JEditorPane styleClassEditor;
		
	public class TemplateTableCellEditor extends DefaultCellEditor{

		private static final long serialVersionUID = 1L;

		public TemplateTableCellEditor(JTextField textField) {
			super(textField);
		}
		
	    public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected,
                int row, int column) {
	    	JTextField field = (JTextField)super.getTableCellEditorComponent(table, value, isSelected, row, column);
	    	
	    	if (column == 2 || column == 3){
	    		editorPane.setText((String)value);
	    	}
	    	return field;
	    }

	}
	
	class StyleTextEditor{
		int row = -1;
		int column = -1;

		protected void setStyleText() {
			if (row != -1 && column != -1)
				table.setValueAt(editorPane.getText(), 
					row, column);
			row = -1;
			column = -1;
			row = table.getSelectedRow();
			column = table.getSelectedColumn();
			if (row == -1 || (column != 2 && column != 3)){
				row = -1;
				column = -1;
				return;
			}
			editorPane.setText((String)table.getValueAt(row, column));
		}
	}
	
	StyleTextEditor styleTextEditor = new StyleTextEditor();
	class CSSCheckValue implements ICheck{
		String css;
		boolean isCheck = false;
		public CSSCheckValue(String css, boolean b) {
			this.css = css;
			setChecked(b);
		}
		
		@Override
		public void setChecked(boolean b) {
			isCheck = b;
		}

		@Override
		public boolean getChecked() {
			return isCheck;
		}

		@Override
		public String getTitle() {
			return css;
		}

		@Override
		public String getID() {
			return css;
		}

		@Override
		public void setIcon() {
		}
		
		public String toString(){
			return getTitle();
		}
	}
	
	void initCSSChecks(){
		if (table.getSelectedRow() == -1)
			return;
		
		DefaultListModel<ICheck> styleModel = (DefaultListModel<ICheck>)styleList.getModel();
		DefaultListModel<ICheck> styleclassModel = (DefaultListModel<ICheck>)styleclassList.getModel();
		
		styleModel.clear();
		styleclassModel.clear();
		
		String[] styles = new String[]{};
		String[] styleclasses = new String[]{};
		String tmp = (String)table.getValueAt(table.getSelectedRow(), 2);
		if (tmp != null && !tmp.isEmpty())
			styles = tmp.split(";");
		
		tmp = (String)table.getValueAt(table.getSelectedRow(), 3);
		if (tmp != null && !tmp.isEmpty())
			styleclasses = tmp.split(" ");
		
		for (String style : styleclasses) {
			styleclassModel.addElement(new CSSCheckValue(style, true));
		}
		
		for (String style : styles) {
			styleModel.addElement(new CSSCheckValue(style, true));
		}
		
	}

	protected void setList(JList<ICheck> list){
		list.setModel(new DefaultListModel<ICheck>());
		list.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setCellRenderer(new CheckBoxListRenderer(list, new CheckBoxListRenderer.ISelection() {		
			@Override
			public void onSelected(ICheck obj) {
			}
		}));
	}
	
	protected String getSelectedStyle(JList<ICheck> list, String split) {
		String result = "";
		for (int i = 0; i < list.getModel().getSize(); i++) {
			ICheck value = list.getModel().getElementAt(i);
			if (value.getChecked()){
				if (result.isEmpty())
					result = value.getID();
				else
					result += split + value.getID();
			}
		}
		
		return result;
	}
	
	public CSSEditor(IMainControl mainControl) {
		super(mainControl);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1184, 791);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JToolBar toolBar = new JToolBar();
		toolBar.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		contentPane.add(toolBar, BorderLayout.NORTH);
		
		splitPane = new JSplitPane();
		splitPane.setOneTouchExpandable(true);
		splitPane.setBackground(Color.WHITE);
		contentPane.add(splitPane, BorderLayout.CENTER);
		
		panel = new JPanel();
		panel.setBackground(Color.WHITE);
		splitPane.setRightComponent(panel);
		panel.setLayout(new BorderLayout(0, 0));
		
		splitPane_1 = new JSplitPane();
		splitPane_1.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitPane_1.setResizeWeight(0.35);
		splitPane_1.setOneTouchExpandable(true);
		splitPane_1.setBackground(Color.WHITE);
		panel.add(splitPane_1, BorderLayout.CENTER);
		
		
		splitPane_2 = new JSplitPane();
		splitPane_2.setResizeWeight(0.5);
		splitPane_2.setOneTouchExpandable(true);
		splitPane_2.setBackground(Color.WHITE);
		splitPane_1.setLeftComponent(splitPane_2);
		
		panel_2 = new JPanel();
		panel_2.setBackground(Color.WHITE);
		splitPane_2.setRightComponent(panel_2);
		panel_2.setLayout(new BorderLayout(0, 0));
		
		lblStyleclass = new JLabel("控件StyleClass");
		lblStyleclass.setHorizontalAlignment(SwingConstants.CENTER);
		panel_2.add(lblStyleclass, BorderLayout.NORTH);
		
		scrollPane_3 = new JScrollPane();
		panel_2.add(scrollPane_3, BorderLayout.CENTER);
		
		styleClassEditor = new JEditorPane();
		styleClassEditor.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		styleClassEditor.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				isEdit = true;
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				isEdit = true;
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				isEdit = true;
			}
		});
		scrollPane_3.setViewportView(styleClassEditor);
		
		panel_3 = new JPanel();
		panel_3.setBackground(Color.WHITE);
		splitPane_2.setLeftComponent(panel_3);
		panel_3.setLayout(new BorderLayout(0, 0));
		
		lblstyle = new JLabel("控件Style");
		lblstyle.setHorizontalAlignment(SwingConstants.CENTER);
		panel_3.add(lblstyle, BorderLayout.NORTH);
		
		scrollPane_2 = new JScrollPane();
		panel_3.add(scrollPane_2, BorderLayout.CENTER);
		
		styleEditor = new JEditorPane();
		styleEditor.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		styleEditor.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				isEdit = true;
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				isEdit = true;
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				isEdit = true;
			}
		});
		scrollPane_2.setViewportView(styleEditor);
		splitPane_2.setDividerLocation(0.25);
		
		splitPane_3 = new JSplitPane();
		splitPane_3.setContinuousLayout(true);
		splitPane_1.setRightComponent(splitPane_3);
		
		panel_1 = new JPanel();
		splitPane_3.setLeftComponent(panel_1);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		panel_4 = new JPanel();
		panel_1.add(panel_4, BorderLayout.SOUTH);
		panel_4.setLayout(new BorderLayout(0, 0));
		
		scrollPane_1 = new JScrollPane();
		scrollPane_1.setPreferredSize(new Dimension(2, 100));
		scrollPane_1.setSize(new Dimension(0, 100));
		scrollPane_1.setMinimumSize(new Dimension(23, 100));
		panel_4.add(scrollPane_1, BorderLayout.CENTER);
		
		editorPane = new JEditorPane();
		editorPane.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		editorPane.setMinimumSize(new Dimension(6, 100));
		editorPane.setSize(new Dimension(0, 100));
		scrollPane_1.setViewportView(editorPane);
		
		scrollPane_4 = new JScrollPane();
		panel_1.add(scrollPane_4, BorderLayout.CENTER);
		
		table = new JTable(){
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int row, int column)
            {
				return false;
            }
		};
		
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setRowHeight(40);
		table.setFont(new Font("微软雅黑", Font.PLAIN, 14));
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()){
					return;
				}
				
				styleTextEditor.setStyleText();
				
				initCSSChecks();
			}
		});
		table.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mousePressed(MouseEvent e) {
				styleTextEditor.setStyleText();
			}
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2){
					styleEditor.setText(getSelectedStyle(styleList, ";"));
					styleClassEditor.setText(getSelectedStyle(styleclassList, " "));
				}
			}
		});
		
		scrollPane_4.setViewportView(table);
		splitPane_1.setDividerLocation(0.25);
		button_1 = new JButton("刷新控件树");
		button_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadTree();
			}
		});
		toolBar.add(button_1);
		
		toolBar.addSeparator();
		button_6 = new JButton("折叠/展开");
		button_6.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_6.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (tree.getSelectionPath() == null)
					return;
				
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getSelectionPath().getLastPathComponent();
				if (node == null)
					return;
				
				TreeHelp.expandOrCollapse(tree, tree.getSelectionPath(), !tree.isCollapsed(tree.getSelectionPath()));
			}
		});
		toolBar.add(button_6);
		
		button_7 = new JButton("全选");
		button_7.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_7.addActionListener(new ActionListener() {
			boolean isSelect = false;
			public void actionPerformed(ActionEvent e) {
				CheckBoxNode selectNode = null;
				if (tree.getSelectionPath() != null)
					selectNode = (CheckBoxNode)tree.getSelectionPath().getLastPathComponent();
				isSelect = !isSelect;
				TreeHelp.selectChilds(tree, selectNode, isSelect);
			}
		});
		toolBar.add(button_7);
		
		toolBar.addSeparator();
		
		label_1 = new JLabel(" 模板 ");
		label_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(label_1);
		
		templates = new JComboBox<String>();
		templates.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		templates.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (templates.getSelectedItem() == null)
					return;
				
				selectTemplate(templates.getSelectedItem().toString());
			}
		});
		templates.setMaximumSize(new Dimension(200, 32767));
		toolBar.add(templates);
		
		lblNewLabel = new JLabel(" ");
		toolBar.add(lblNewLabel);
		button_2 = new JButton("新建");
		button_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String name = EditorEnvironment.showInputDialog("请输入模板名称");
				if (name == null || name.isEmpty()){
					return;
				}
				
				if (newTemplate(name)){
					templates.addItem(name);
					templates.setSelectedItem(name);
				}
			}
		});
		button_3 = new JButton("删除");
		button_3.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (templates.getSelectedItem() == null)
					return;
				
				if (EditorEnvironment.showConfirmDialog("是否删除选定的模板？", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
					return;
				
				removeTemplate(templates.getSelectedItem().toString());
			}
		});
		toolBar.add(button_3);
		toolBar.add(button_2);
		
		button_5 = new JButton("保存");
		button_5.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (EditorEnvironment.showConfirmDialog("保存模板将覆盖已经存在的CSS样式，是否继续？", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
					return;
				
				if (templates.getSelectedItem() != null)
					saveTemplate(templates.getSelectedItem().toString());
			}
		});
		
		button_4 = new JButton("复制");
		button_4.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (templates.getSelectedItem() == null)
					return;
				
				String name = EditorEnvironment.showInputDialog("请输入模板名称");
				if (name == null || name.isEmpty()){
					return;
				}
				
				if (newTemplate(name)){
					File source = getTemplateFile(templates.getSelectedItem().toString());
					File dest = getTemplateFile(name);
					try {
						FileHelp.copyFileTo(source, dest);
						templates.addItem(name);
						selectTemplate(name);
					} catch (IOException e1) {
						e1.printStackTrace();
						EditorEnvironment.showMessage(null, "拷贝文件：" + source.getAbsolutePath() + " 到 " + dest.getAbsolutePath() + "失败！", "复制失败");
					}
				}
			}
		});
		toolBar.add(button_4);
		toolBar.add(button_5);
		
		toolBar.addSeparator();
		
		button = new JButton("应用模板到项目");
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (EditorEnvironment.showConfirmDialog("应用模板将使用选定模板样式覆盖当前项目所有UI的CSS样式，是否继续？", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
					return;
				
				if (templates.getSelectedItem() != null){
					if (applyTemplate(templates.getSelectedItem().toString()))
						EditorEnvironment.showMessage("应用成功！");

				}
			}
		});
		toolBar.add(button);
		
		toolBar.addSeparator();
		
		button_8 = new JButton("提取");
		button_8.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_8.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				extractTemplate();
			}
		});
		toolBar.add(button_8);
		
		button_9 = new JButton("清空");
		button_9.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_9.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				emptyTemplate();
			}
		});
		toolBar.add(button_9);
		btncss_2 = new JButton("保存控件样式");
		btncss_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		btncss_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				HashMap<String, HashMap<String, TreeItemInfo>> nodes = getTreeSelectNodes(false);	
				if (nodes.size() == 0)
					return;
				
				if (EditorEnvironment.showConfirmDialog("保存样式将覆盖选定控件已经存在的CSS样式，是否继续？", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
					return;
				
				if (batchSaveCSSStyle(nodes))
					EditorEnvironment.showMessage("保存成功！");
			}
		});
		
		toolBar.addSeparator();
		toolBar.add(btncss_2);
		
		
		tree = new JTree(new DefaultTreeModel(new DefaultMutableTreeNode("所有控件")));
		tree.setRowHeight(40);
		CheckBoxNodeConfig.config(tree, null);
		tree.setBackground(Color.WHITE);
		tree.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		scrollPane = new JScrollPane();
		splitPane.setLeftComponent(scrollPane);
		scrollPane.setViewportView(tree);
		tree.setRowHeight(30);
		tree.setCellRenderer(new ControlTreeManager.UITreeCellRender(new ControlTreeManager.UITreeCellRender.IGetType() {
			@Override
			public String getType(DefaultMutableTreeNode node) {
				if (!(node.getUserObject() instanceof TreeItemInfo))
					return null;
				
				TreeItemInfo info = (TreeItemInfo)node.getUserObject();
				if (info.data.has(KEY_DRAWNODE_TYPE))
					return info.data.getString(KEY_DRAWNODE_TYPE);
				else
					return null;
			}
		}));
		tree.setExpandsSelectedPaths(false);
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				if (e.getNewLeadSelectionPath() == null)
					return;
				
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)e.getNewLeadSelectionPath().getLastPathComponent();
				
				if (node.getUserObject() instanceof TreeItemInfo){
					TreeItemInfo info = (TreeItemInfo)node.getUserObject();
					try {
						if (info.data.getString(KEY_TYPE).compareToIgnoreCase(TreeItemObject_Control) == 0){
							if (isEdit && e.getOldLeadSelectionPath() != null){
								if (EditorEnvironment.showConfirmDialog("已经更改数据，是否保存？", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
									save((DefaultMutableTreeNode)e.getOldLeadSelectionPath().getLastPathComponent());
								}
							}
							String style = info.data.has(KEY_STYLE) ? info.data.getString(KEY_STYLE) : "";
							styleEditor.setText(style);
							style = info.data.has(KEY_STYLECLASS) ? info.data.getString(KEY_STYLECLASS) : "";
							styleClassEditor.setText(style);
							isEdit = false;
						}
					} catch (JSONException e1) {
						e1.printStackTrace();
						EditorEnvironment.showException(e1);
					}
				}
			}
		});

		splitPane.setDividerLocation(0.25);
		splitPane.setResizeWeight(0.25);
		
		splitPane_3.setDividerLocation(0.80);
		splitPane_3.setResizeWeight(0.80);
		
		splitPane_4 = new JSplitPane();
		splitPane_4.setResizeWeight(0.5);
		splitPane_4.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitPane_3.setRightComponent(splitPane_4);
		
		panel_5 = new JPanel();
		splitPane_4.setLeftComponent(panel_5);
		panel_5.setLayout(new BorderLayout(0, 0));
		
		panel_7 = new JPanel();
		panel_5.add(panel_7, BorderLayout.NORTH);
		
		lblStyle = new JLabel("模板项目Style");
		panel_7.add(lblStyle);
		
		scrollPane_5 = new JScrollPane();
		panel_5.add(scrollPane_5, BorderLayout.CENTER);
		
		styleList = new JList<>(new DefaultListModel<ICheck>());
		styleList.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		scrollPane_5.setViewportView(styleList);
		
		panel_6 = new JPanel();
		splitPane_4.setRightComponent(panel_6);
		panel_6.setLayout(new BorderLayout(0, 0));
		
		panel_8 = new JPanel();
		panel_6.add(panel_8, BorderLayout.NORTH);
		
		lblStyleclass_1 = new JLabel("模板项目StyleClass");
		panel_8.add(lblStyleclass_1);
		
		scrollPane_6 = new JScrollPane();
		panel_6.add(scrollPane_6, BorderLayout.CENTER);
		
		styleclassList = new JList<>(new DefaultListModel<ICheck>());
		styleclassList.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		scrollPane_6.setViewportView(styleclassList);

		addComponentListener(new ComponentAdapter(){
	            public void componentResized(ComponentEvent e) {
					splitPane.setDividerLocation(splitPane.getResizeWeight());
					splitPane.setResizeWeight(splitPane.getResizeWeight());
					
					splitPane_3.setDividerLocation(splitPane_3.getResizeWeight());
					splitPane_3.setResizeWeight(splitPane_3.getResizeWeight());
	            }
	        });
		setList(styleList);
		setList(styleclassList);
		
		csh = new ControlSearchHelp(tree);
	}

	@Override
	public void onSave() {
		try {
			if (!isEdit)
				return;
			
			isEdit = false;
			
		} catch (Exception e) {
			e.printStackTrace();
			EditorEnvironment.showMessage(this, e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	public void onLoad() {
		loadTree();
		initTemplate();
	}

	HashMap<String, TreeItemInfo> updateItems = new HashMap<>();
	private JLabel label_1;
	private JButton button;
	private JComboBox<String> templates;
	private JPanel panel_1;
	private JScrollPane scrollPane_4;
	private JTable table;
	private JPanel panel_4;
	private JLabel lblNewLabel;
	private JButton button_2;
	private JButton button_3;
	private JButton button_4;
	private JButton button_5;
	private JButton button_6;
	private JButton button_7;
	private JButton button_8;
	private JButton button_9;
	private JScrollPane scrollPane_1;
	private JEditorPane editorPane;
	private JSplitPane splitPane_3;
	private JSplitPane splitPane_4;
	private JPanel panel_5;
	private JPanel panel_6;
	private JPanel panel_7;
	private JLabel lblStyle;
	private JPanel panel_8;
	private JLabel lblStyleclass_1;
	private JScrollPane scrollPane_5;
	private JList<ICheck> styleList;
	private JScrollPane scrollPane_6;
	private JList<ICheck> styleclassList;
	
	public static File getTemplateFile(String templateName){
		File cssTemplateDir = EditorEnvironment.getEditorSourcePath(EditorEnvironment.Template_Path, EditorEnvironment.CSS_Dir_Name);
		if (!cssTemplateDir.exists())
			if (!cssTemplateDir.mkdirs())
				return null;
		File templateFile = new File(cssTemplateDir, templateName + "." + CSS_TEMPLATE_EXT);
		return templateFile;
	}
	
	public static class NodeList extends ArrayList<UINode>{
		private static final long serialVersionUID = 1L;
	}
	
	private void addToSelectMap(HashMap<String, HashMap<String, TreeItemInfo>> nodes, DefaultMutableTreeNode t){
		TreeItemInfo info = (TreeItemInfo)t.getUserObject();
		try {
			boolean isUI = info.data.getString(KEY_TYPE).compareTo(TreeItemObject_UI) == 0;
			HashMap<String, TreeItemInfo> childs = null;
			TreeItemInfo uiInfo = info;
			if (!isUI){
				uiInfo = (TreeItemInfo)((DefaultMutableTreeNode)t.getParent()).getUserObject();
			}
			String key = uiInfo.data.getString(KEY_FILE);
			
			if (nodes.containsKey(key)){
				childs = nodes.get(key);
			}else{
				childs = new HashMap<>();
				nodes.put(key, childs);
			}
			
			if (isUI){
				if (t.getChildCount() > 0){
					
					for (int i = 0; i < t.getChildCount(); i++) {
						DefaultMutableTreeNode child = (DefaultMutableTreeNode)t.getChildAt(i);
						TreeItemInfo subInfo = (TreeItemInfo)child.getUserObject();
						childs.put(subInfo.data.getString(TreeHelp.ID_KEY), subInfo);
					}
				}
			}else
				childs.put(info.data.getString(TreeHelp.ID_KEY), info);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	
	}
	
	private HashMap<String, HashMap<String, TreeItemInfo>> getTreeSelectNodes(boolean isAll){
		HashMap<String, HashMap<String, TreeItemInfo>> nodes = new HashMap<>();
		if (!isAll){
			if (tree.getSelectionPath() == null)
				return nodes;
			
			addToSelectMap(nodes, (DefaultMutableTreeNode)tree.getSelectionPath().getLastPathComponent());
			return nodes;
		}
		
		TreeHelp.traverseTree(tree, new ITraverseTree<CheckBoxNode>() {
			@Override
			public boolean onNode(CheckBoxNode t) {
				boolean b = true;//t.isSelected()
				if (b){
					addToSelectMap(nodes, t);
				}
				return true;
			}
		});
		
		return nodes;
	}

	private boolean batchSaveCSSStyle(HashMap<String, HashMap<String, TreeItemInfo>> nodes){
		for (String filename : nodes.keySet()) {
			try {
				File uiFile = new File(filename);
				DrawCanvas canvas = new DrawCanvas();
				canvas.setFile(uiFile);
				canvas.load(new ICreateNodeSerializable() {
					
					@Override
					public DrawNode newDrawNode(JSONObject json) {
						return new UINode(canvas);
					}
					
					@Override
					public IDataSerializable getUserDataSerializable(DrawNode node) {
						return null;
					}
				}, null);
				HashMap<String, TreeItemInfo> editControls = nodes.get(filename);
				
				String style = styleEditor.getText();
				String styleclass = styleClassEditor.getText();
							
				for (String id: editControls.keySet()) {
					UINode uiNode = (UINode)canvas.getNode(id);
					if (uiNode == null)
						continue;
					
					TreeItemInfo info = editControls.get(id);
					DrawInfo drawInfo = uiNode.getDrawInfo();
					drawInfo.style = style;
					drawInfo.styleClass = styleclass;
					info.data.put(KEY_STYLE, style);
					info.data.put(KEY_STYLECLASS, styleclass);
				}
			
				canvas.save();
			} catch (Exception e) {
				e.printStackTrace();
				EditorEnvironment.showException(e);
				return false;
			}
			
		}
		
		isEdit = false;
		return true;
	}
	
	static class StyleHash{
		public TreeMap<Integer, String> gsorts = new TreeMap<>();
		public HashMap<String, Integer> gmaps = new HashMap<>();
		
		public String getStyle(String style, String separator){
			TreeMap<Integer, String> sorts = new TreeMap<>(gsorts);
			HashMap<String, Integer> maps = new HashMap<>(gmaps);

			StyleHash source_styles = StyleHash.get(style, separator);
			
			for (Integer index : source_styles.gsorts.keySet()) {
				String css = source_styles.gsorts.get(index);
				if (maps.containsKey(css)){
					sorts.remove(maps.get(css));
				}
			}
			
			style = "";
			for (String tmp : sorts.values()) {
				if (style == "")
					style = tmp;
				else
					style += separator + tmp;
			}

			for (String tmp : source_styles.gsorts.values()) {
				if (style == "")
					style = tmp;
				else
					style += separator + tmp;
			}

			return style;
		}
		
		public static StyleHash get(String cssStyle, String separator){
			StyleHash hash = new StyleHash();
			if (cssStyle == null || cssStyle.isEmpty())
				return hash;
			
			String[] csses = cssStyle.split(separator);
			int index = 0;
			for (String css : csses) {
				int tmp = index++;
				if (css.isEmpty())
					continue;
				css = css.trim();
				hash.gsorts.put(tmp, css);
				hash.gmaps.put(css, tmp);
			}
			
			return hash;
		}

	}
	
	private boolean applyTemplate(String templateName){
		File templateFile = getTemplateFile(templateName);
		if (!templateFile.exists()){
			EditorEnvironment.showMessage(null, "模板文件：" + templateFile.getAbsolutePath() + "不存在！", "应用模板失败");
			return false;
		}

		try {
			
			HashMap<String, HashMap<String, TreeItemInfo>> nodes = getTreeSelectNodes(true);
			
			DefaultTableModel model = (DefaultTableModel)table.getModel();
			HashMap<String, NodeList> controls = DrawInfoDefines.getControlSimpleClassName(NodeList.class);
			for (String filename : nodes.keySet()) {
				File uiFile = new File(filename);
				DrawCanvas canvas = new DrawCanvas();
				canvas.setFile(uiFile);
				canvas.load(new ICreateNodeSerializable() {
					
					@Override
					public DrawNode newDrawNode(JSONObject json) {
						return new UINode(canvas);
					}
					
					@Override
					public IDataSerializable getUserDataSerializable(DrawNode node) {
						return null;
					}
				}, null);
				
				HashMap<String, TreeItemInfo> editControls = nodes.get(filename);
				for (DrawNode drawNode : canvas.getNodes()) {
					UINode uiNode = (UINode)drawNode;
					if (!editControls.containsKey(uiNode.id))
						continue;
					
					String name = uiNode.getDrawInfo().getClass().getSimpleName();
					NodeList list = controls.get(name);
					if (list == null){
						list = new NodeList();
						controls.put(name, list);
					}
					
					list.add(uiNode);
				}
				
				for (int row = 0; row < model.getRowCount(); row++) {
					String simpleName = (String)model.getValueAt(row, 1);
					String style = (String)model.getValueAt(row, 2);
					String styleclass = (String)model.getValueAt(row, 3);
					
					if (!controls.containsKey(simpleName))
						continue;
					
					List<UINode> controlNodes = (List<UINode>)controls.get(simpleName);
					if (controlNodes == null)
						continue;
					
					StyleHash styles = StyleHash.get(style, ";");
					StyleHash styleclasses = StyleHash.get(styleclass, " ");
					for (UINode uiNode : controlNodes) {
						DrawInfo drawInfo = uiNode.getDrawInfo();
						style = styles.getStyle("", ";");
						styleclass = styleclasses.getStyle("", " ");
						
						drawInfo.style = style;
						drawInfo.styleClass = styleclass;
						TreeItemInfo info = editControls.get(uiNode.id);
						if (info == null)
							continue;
						info.data.put(KEY_STYLE, style);
						info.data.put(KEY_STYLECLASS, styleclass);
					}
				}
				
				canvas.save();
			}
			
			return true;
		} catch (Exception e1) {
			e1.printStackTrace();
			EditorEnvironment.showException(e1);
			return false;
		}
	}
	
	private void getCSSInfo(String cssString, StringIntegerHash styles, String separator){
		if (cssString != null && !cssString.isEmpty()){
			String[] csses = cssString.split(separator);
			for (String css : csses) {
				if (css.isEmpty())
					continue;
				
				css = css.trim();
				int count = 1;
				if (styles.containsKey(css))
					count = styles.get(css) + 1;
				styles.put(css, count);
			}
		}
	}
	
	public static class StringIntegerHash extends TreeMap<String, Integer>{
		private static final long serialVersionUID = 1L;
		
		static class StringIntegerComparator implements Comparator<String>{
			StringIntegerHash maps;
			public StringIntegerComparator(StringIntegerHash maps) {
				this.maps = maps;
			}
			@Override
			public int compare(String a, String b) {
		    	return maps.get(a).compareTo(maps.get(b));
			}
		};
		
		public TreeMap<String, Integer> getValueMaps(){
			TreeMap<String, Integer> maps = new TreeMap<>(new StringIntegerComparator(this));
			for (String key : keySet()) {
				maps.put(key, get(key));
			}
			return maps;
		}
	}
	
	private void fillTemplateTable(DefaultTableModel model, int row, int column, String simpleName, HashMap<String, StringIntegerHash> styles, String separator){
		StringIntegerHash values = styles.get(simpleName);
		String style = "";
		if (values.size() > 0){
			TreeMap<String, Integer> maps = values.getValueMaps();
			for (String css : maps.descendingKeySet()) {
				if (style == "")
					style = css;
				else
					style += separator + css;
			}
			
			model.setValueAt(style, row, column);
		}
			
	}
	
	private void emptyTemplate(){
		try {
			DefaultTableModel model = (DefaultTableModel)table.getModel();
			for (int row = 0; row < model.getRowCount(); row++) {
				model.setValueAt("", row, 2);
				model.setValueAt("", row, 3);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			EditorEnvironment.showException(e1);
		}
	}
	
	private void extractTemplate(){
		DefaultTableModel model = (DefaultTableModel)table.getModel();
		if (model.getRowCount() == 0){
			EditorEnvironment.showMessage("请先新建/选择一个模板后再试！");
			return;
		}
		try {
			HashMap<String, StringIntegerHash> class_styles = DrawInfoDefines.getControlSimpleClassName(StringIntegerHash.class);
			HashMap<String, StringIntegerHash> class_styleclasses = DrawInfoDefines.getControlSimpleClassName(StringIntegerHash.class);
			TreeHelp.traverseTree(tree, new ITraverseTree<CheckBoxNode>() {
				@Override
				public boolean onNode(CheckBoxNode t) {
					if (!(t.getUserObject() instanceof TreeItemInfo))
						return true;

					TreeItemInfo info = (TreeItemInfo)t.getUserObject();
					try {
						boolean isUI = info.data.getString(KEY_TYPE).compareTo(TreeItemObject_UI) == 0;
						if (isUI)
							return true;
						
						String key = info.data.getString(KEY_DRAWNODE_Simple_TYPE);
						StringIntegerHash styles = class_styles.get(key);
						StringIntegerHash styleclasses = class_styleclasses.get(key);
						if (info.data.has(KEY_STYLE))
							getCSSInfo(info.data.getString(KEY_STYLE), styles, ";");
						if (info.data.has(KEY_STYLECLASS))
							getCSSInfo(info.data.getString(KEY_STYLECLASS), styleclasses, " ");
					} catch (JSONException e) {
						e.printStackTrace();
					}
					return true;
				}
			});
			
			for (int row = 0; row < model.getRowCount(); row++) {
				String simpleName = (String)model.getValueAt(row, 1);
				
				fillTemplateTable(model, row, 2, simpleName, class_styles, ";");
				fillTemplateTable(model, row, 3, simpleName, class_styleclasses, " ");
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			EditorEnvironment.showException(e1);
		}
	}
	
	private void selectTemplate(String templateName){
		DefaultTableModel model = new DefaultTableModel();
		model.addColumn("控件类型");
		model.addColumn("Class");
		model.addColumn("style");
		model.addColumn("style class");
		
		File templateFile = getTemplateFile(templateName);
		if (!templateFile.exists()){
			table.setModel(model);
			return;
		}

		HashMap<String, JSONObject> controls = DrawInfoDefines.getControlSimpleClassNameForJSONObject();
		
		JSONObject datas;
		try {
			datas = (JSONObject) JsonHelp.parseJson(templateFile, null);
			JSONArray names = datas.names();
			for (int i = 0; i < names.length(); i++) {
				String name = names.getString(i);
				JSONObject info = datas.getJSONObject(name);
				if (controls.containsKey(name)){
					info.put(DrawInfoDefines.Full_TypeName_Key, controls.get(name).getString(DrawInfoDefines.Full_TypeName_Key));
					controls.put(name, info);
				}
			}
			
			for (String controlName : controls.keySet()) {
				JSONObject info = controls.get(controlName);
				Object[] row = new Object[model.getColumnCount()];
				String name = DrawInfoDefines.getControlChineseName(controlName);
				row[0] = name;
				row[1] = controlName;
				try {
					row[2] = info.has(KEY_STYLE) ? info.getString(KEY_STYLE) : "";
					row[3] = info.has(KEY_STYLECLASS) ? info.getString(KEY_STYLECLASS) : "";
				} catch (JSONException e) {
					e.printStackTrace();
				}
				model.addRow(row);
			}

			table.setModel(model);
			TableColumn column = table.getColumnModel().getColumn(0); 
			column.setCellRenderer(new IconTableCellRender());
//			column = table.getColumnModel().getColumn(2); 
//			column.setCellEditor(new TemplateTableCellEditor(new JTextField()));
//			column = table.getColumnModel().getColumn(3); 
//			column.setCellEditor(new TemplateTableCellEditor(new JTextField()));

		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
	private void saveTemplate(String templateName){
		File templateFile = getTemplateFile(templateName);
		if (templateFile.exists()){
			if (!templateFile.delete()){
				EditorEnvironment.showMessage(null, "不能删除模板文件：" + templateFile.getAbsolutePath(), "保存模板失败");
				return;
			}
		}

		try {
			DefaultTableModel model = (DefaultTableModel)table.getModel();
			JSONObject datas = new JSONObject();
			for (int row = 0; row < model.getRowCount(); row++) {
				JSONObject info = new JSONObject();
				info.put(KEY_STYLE, (String)model.getValueAt(row, 2));
				info.put(KEY_STYLECLASS, (String)model.getValueAt(row, 3));
				datas.put((String)model.getValueAt(row, 1), info);
			}
			
			JsonHelp.saveJson(templateFile, datas, null);
			EditorEnvironment.showMessage("保存成功！");
		} catch (Exception e1) {
			e1.printStackTrace();
			EditorEnvironment.showException(e1);
		}
	}
	
	private void removeTemplate(String templateName){
		File templateFile = getTemplateFile(templateName);
		if (templateFile.exists()){
			if (!templateFile.delete()){
				EditorEnvironment.showMessage(null, "不能删除模板文件：" + templateFile.getAbsolutePath(), "保存模板失败");
				return;
			}
		}
		templates.removeItem(templateName);
		if (templates.getItemCount() > 0){
			templates.setSelectedIndex(0);
		}
	}
	
	private boolean newTemplate(String templateName){
		File templateFile = getTemplateFile(templateName);
		if (templateFile.exists()){
			if (EditorEnvironment.showConfirmDialog("要建立的模板已经存在，是否覆盖？", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
				return false;
			
			if (!templateFile.delete()){
				EditorEnvironment.showMessage(null, "删除原模板文件：" + templateFile.getAbsolutePath() + "失败！", "建立模板失败");
				return false;
			}
		}

		try {
			JSONObject datas = new JSONObject();
			HashMap<String, JSONObject> controls = DrawInfoDefines.getControlSimpleClassNameForJSONObject();
			for (String name : controls.keySet()) {
				datas.put(name, controls.get(name));
			}
			JsonHelp.saveJson(templateFile, datas, null);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			EditorEnvironment.showException(e);
			return false;
		}
	}
	
	private void initTemplate(){
		File cssTemplateDir = EditorEnvironment.getEditorSourcePath(EditorEnvironment.Template_Path, EditorEnvironment.CSS_Dir_Name);
		File[] files = cssTemplateDir.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File path) {
				return path.isFile() && FileHelp.GetExt(path.getName()).compareToIgnoreCase(CSS_TEMPLATE_EXT) == 0;
			}
		});
		
		templates.setModel(new DefaultComboBoxModel<String>());
		
		if (files == null)
			return;
		
		for (File file : files) {
			templates.addItem(FileHelp.removeExt(file.getName()));
		}
		
		if (templates.getItemCount() > 0)
			templates.setSelectedIndex(0);
	}
	
	public void save(DefaultMutableTreeNode node){
		TreeItemInfo info = (TreeItemInfo)node.getUserObject();
		try {
			TreeItemInfo uiInfo = info;
			switch (info.data.getString(KEY_TYPE)) {
			case TreeItemObject_Control:
				uiInfo = (TreeItemInfo)((DefaultMutableTreeNode)node.getParent()).getUserObject();
				break;
			}
			
			File uiFile = new File(uiInfo.data.getString(KEY_FILE));
			DrawCanvas canvas = new DrawCanvas();
			canvas.setFile(uiFile);
			canvas.load(new ICreateNodeSerializable() {
				
				@Override
				public DrawNode newDrawNode(JSONObject json) {
					return new UINode(canvas);
				}
				
				@Override
				public IDataSerializable getUserDataSerializable(DrawNode node) {
					return null;
				}
			}, null);
			
			UINode uiNode = (UINode)canvas.getNode(info.data.getString(TreeHelp.ID_KEY));
			DrawInfo drawInfo = uiNode.getDrawInfo();
			info.data.put(KEY_STYLE, styleEditor.getText());
			info.data.put(KEY_STYLECLASS, styleClassEditor.getText());
			drawInfo.style = styleEditor.getText();
			drawInfo.styleClass = styleClassEditor.getText();
			canvas.save();
			isEdit = false;
		} catch (Exception e) {
			e.printStackTrace();
			EditorEnvironment.showException(e);
		}
	}
	
	public void loadTree(){
		HashMap<String, DrawNode> uiWorkflowNodes = new HashMap<>();
		HashMap<String, File> uiFiles = new HashMap<>();
		try {
			EditorEnvironment.traverseModel(new ITraverseDrawNode() {
				
				@Override
				public boolean onNode(File file, String title, DrawNode node, Object param) {
					String id = EditorEnvironment.getUIID(node.id);
					uiWorkflowNodes.put(id, node);
					return true;
				}
			}, null);
			
			JSONArray treeDatas = new JSONArray();
			EditorEnvironment.traverseUI(new ITraverseDrawNode() {
				
				@Override
				public boolean onNode(File file, String title, DrawNode node, Object param) {
					String id = FileHelp.removeExt(file.getName());
					try {
						DrawCanvas canvas = new DrawCanvas();
						canvas.setFile(file);
						canvas.load(new ICreateNodeSerializable() {
							
							@Override
							public DrawNode newDrawNode(JSONObject json) {
								return new	UINode(canvas);
							}
							
							@Override
							public IDataSerializable getUserDataSerializable(DrawNode node) {
								return null;
							}
						}, null);
						
						if (canvas.getNodes().size() == 0)
							return true;
						
						uiFiles.put(id, file);
						String name = id;
						String memo = id;
						DrawNode workflownode = null;
						if (uiWorkflowNodes.containsKey(id)){
							workflownode = uiWorkflowNodes.get(id);
							name = workflownode.title + "[" + workflownode.name + "]";
							memo = workflownode.memo;
						}else{
							String tmp = canvas.getPageConfig().name; 
							if (tmp != null && !tmp.isEmpty())
								name = tmp;
							tmp = canvas.getPageConfig().memo; 
							if (tmp != null && !tmp.isEmpty())
								memo = tmp;
						}
						
						JSONObject info = new JSONObject();
						info.put(TreeHelp.ID_KEY, id);
						info.put(TreeHelp.NAME_KEY, name);
						info.put(KEY_MEMO, memo);
						info.put(KEY_UI_ID, id);
						info.put(KEY_FILE, file.getAbsolutePath());
						if (workflownode != null)
							info.put(KEY_WORKFLOW_ID, workflownode.id);
						info.put(KEY_TYPE, TreeItemObject_UI);
						treeDatas.put(info);
						for (DrawNode drawNode : canvas.getNodes()) {
							DrawInfo drawInfo = ((UINode)drawNode).getDrawInfo();
							JSONObject subInfo = new JSONObject();
							subInfo.put(TreeHelp.ID_KEY, drawNode.id);
							subInfo.put(TreeHelp.NAME_KEY, drawInfo.value + "[" + drawInfo.name + "]");
							subInfo.put(TreeHelp.PID_KEY, id);
							subInfo.put(KEY_STYLE, drawInfo.style);
							subInfo.put(KEY_STYLECLASS, drawInfo.styleClass);
							subInfo.put(KEY_MEMO, drawInfo.title);
							subInfo.put(KEY_TYPE, TreeItemObject_Control);
							subInfo.put(KEY_DRAWNODE_TYPE, drawInfo.typeName());
							subInfo.put(KEY_DRAWNODE_Simple_TYPE, drawInfo.getClass().getSimpleName());
							
							treeDatas.put(subInfo);
						}
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					
					return true;
				}
			}, null, true);
			
			TreeHelp.jsonToTree(tree, treeDatas, new INewNode() {
				
				@Override
				public DefaultMutableTreeNode newNode() {
					return new CheckBoxNode();
				}
			});
			
			csh.init();
			isEdit = false;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClose() {
		dispose();
	}

	@Override
	public void onPublish(HashMap<String, DrawNode> uikeysWorkflowNodes, Object param) throws Exception {
		
	}
	
}
