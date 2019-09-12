package com.wh.form;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.util.HashMap;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

import org.jdesktop.swingx.JXDatePicker;

import com.wh.control.EditorEnvironment;
import com.wh.control.ScrollToolBar;
import com.wh.control.modelsearch.ModelSearchView;
import com.wh.control.requirement.ViewControl;
import com.wh.control.requirement.ViewControl.IEditing;
import com.wh.draws.DrawNode;
import com.wh.system.tools.EventHelp;

public class RequirementBuilder extends ChildForm implements IMainMenuOperation, ISubForm {
	private static final long serialVersionUID = 1647458287761184720L;

	private final JPanel contentPanel = new JPanel();
	private JButton button2;
	private JPanel panel_5;

	private JTree tree;

	public ViewControl viewControl = new ViewControl();

	/**
	 * Create the dialog.
	 */
	public RequirementBuilder(IMainControl mainControl) {
		super(mainControl);
		setResizable(false);
		setBounds(100, 100, 1551, 683);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPanel.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPanel);
		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.3);
		contentPanel.add(splitPane);
		JPanel panel = new JPanel();
		splitPane.setLeftComponent(panel);
		panel.setLayout(new BorderLayout(0, 0));
		JScrollPane scrollPane = new JScrollPane();
		panel.add(scrollPane, BorderLayout.CENTER);
		tree = new ModelSearchView.TreeModelSearchView();
		tree.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		tree.setRootVisible(false);
		tree.addTreeSelectionListener(viewControl.treeSelectionListener);

		scrollPane.setViewportView(tree);

		JToolBar toolBar_1 = new JToolBar();
		toolBar_1.setFloatable(false);
		toolBar_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel.add(toolBar_1, BorderLayout.SOUTH);

		versionButton = new JRadioButton("版本方式");
		versionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				viewControl.resetTree();
			}
		});
		versionButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_1.add(versionButton);

		dependButton = new JRadioButton("依赖方式");
		dependButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				viewControl.resetTree();
			}
		});
		dependButton.setSelected(true);
		dependButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_1.add(dependButton);
		panel_5 = new JPanel();
		splitPane.setRightComponent(panel_5);
		panel_5.setLayout(new BorderLayout(0, 0));

		JSplitPane splitPane_1 = new JSplitPane();
		splitPane_1.setResizeWeight(0.6);
		splitPane_1.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitPane_1.addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				splitPane_1.setResizeWeight(splitPane_1.getResizeWeight());
				splitPane_1.setDividerLocation(splitPane_1.getResizeWeight());
			}
		});
		panel_5.add(splitPane_1, BorderLayout.CENTER);

		contentScrollBar = new JScrollPane();
		splitPane_1.setLeftComponent(contentScrollBar);

		contentScrollBar.getVerticalScrollBar().setUnitIncrement(30);
		EventHelp.setGlobalMouseWheel(contentScrollBar, contentScrollBar.getVerticalScrollBar());

		table = new ModelSearchView.TableModelSearchView();
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		table.setFillsViewportHeight(true);
		contentScrollBar.setViewportView(table);

		JPanel panel_1 = new JPanel();
		panel_1.setOpaque(false);
		splitPane_1.setRightComponent(panel_1);
		panel_1.setLayout(new BorderLayout(0, 0));

		JSplitPane splitPane_2 = new JSplitPane();
		splitPane_2.setResizeWeight(0.6);
		splitPane_2.addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				splitPane_2.setResizeWeight(splitPane_2.getResizeWeight());
				splitPane_2.setDividerLocation(splitPane_2.getResizeWeight());
			}
		});

		JPanel toolPanel = new JPanel();
		JPanel scrollButtonPanel = new JPanel();
		JScrollPane toolbarScrollBar = new JScrollPane();
		JToolBar toolBar_2 = new JToolBar();

		panel_1.add(toolPanel, BorderLayout.NORTH);
		toolPanel.setLayout(new BorderLayout(0, 0));
		toolPanel.add(toolbarScrollBar, BorderLayout.CENTER);
		toolbarScrollBar.setViewportView(toolBar_2);
		new ScrollToolBar(toolPanel, scrollButtonPanel, toolbarScrollBar, toolBar_2);

		usedView = new JCheckBox("启用");
		usedView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		usedView.setSelected(true);
		toolBar_2.add(usedView);

		toolBar_2.addSeparator();

		closeView = new JCheckBox("关闭");
		closeView.setEnabled(false);
		closeView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_2.add(closeView);

		toolBar_2.addSeparator();

		toolBar_2.addSeparator();

		JLabel label = new JLabel(" 编号 ");
		label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_2.add(label);

		idView = new JTextField();
		idView.setMaximumSize(new Dimension(150, 2147483647));
		idView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_2.add(idView);
		idView.setColumns(10);

		JLabel label_4 = new JLabel(" 级别 ");
		label_4.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_2.add(label_4);

		levelView = new JSpinner();
		levelView.setModel(new SpinnerNumberModel(new Integer(0), null, null, new Integer(1)));
		levelView.setMaximumSize(new Dimension(58, 32767));
		levelView.setMinimumSize(new Dimension(58, 28));
		levelView.setPreferredSize(new Dimension(58, 28));
		levelView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_2.add(levelView);

		JLabel label_1 = new JLabel(" 版本 ");
		label_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_2.add(label_1);

		versionView = new JTextField();
		versionView.setEditable(false);
		versionView.setMaximumSize(new Dimension(100, 2147483647));
		versionView.setPreferredSize(new Dimension(100, 27));
		versionView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_2.add(versionView);
		versionView.setColumns(10);

		JLabel label_2 = new JLabel(" 人员 ");
		label_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_2.add(label_2);

		userView = new JTextField();
		userView.setMaximumSize(new Dimension(100, 2147483647));
		userView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		userView.setColumns(10);
		toolBar_2.add(userView);

		JLabel label_5 = new JLabel(" 类型 ");
		label_5.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_2.add(label_5);

		typeView = new JComboBox<>();
		typeView.setModel(new DefaultComboBoxModel<>(new String[] { "基本需求", "增强需求", "规划需求" }));
		typeView.setSelectedIndex(0);
		typeView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_2.add(typeView);

		JButton button_7 = new JButton(" 保存当前条目 ");
		button_7.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					viewControl.saveRow(null);
				} catch (Exception e1) {
					e1.printStackTrace();
					EditorEnvironment.showException(e1);
				}
			}
		});
		button_7.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_2.add(button_7);

		panel_1.add(splitPane_2, BorderLayout.CENTER);

		JPanel panel_2 = new JPanel();
		splitPane_2.setRightComponent(panel_2);
		panel_2.setLayout(new BorderLayout(0, 0));

		JToolBar toolBar_3 = new JToolBar();
		toolBar_3.setFloatable(false);
		toolBar_3.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel_2.add(toolBar_3, BorderLayout.SOUTH);

		JButton button_10 = new JButton(" 添加 ");
		button_10.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String value = EditorEnvironment.showInputDialog("请输入新规则");
				if (value == null || value.isEmpty())
					return;

				DefaultListModel<Object> model = (DefaultListModel<Object>) roleView.getModel();
				model.addElement(value);
			}
		});
		button_10.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_3.add(button_10);

		JButton button_11 = new JButton(" 删除 ");
		button_11.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (roleView.getSelectedValue() == null)
					return;

				if (EditorEnvironment.showConfirmDialog("是否删除选定规则？",
						JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
					return;

				DefaultListModel<Object> model = (DefaultListModel<Object>) roleView.getModel();
				model.removeElementAt(roleView.getSelectedIndex());
			}
		});
		button_11.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_3.add(button_11);

		JButton button_12 = new JButton(" 删除所有 ");
		button_12.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultListModel<Object> model = (DefaultListModel<Object>) roleView.getModel();
				if (model.getSize() == 0)
					return;

				if (EditorEnvironment.showConfirmDialog("是否删除所有规则？",
						JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
					return;

				model.removeAllElements();
			}
		});
		button_12.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_3.add(button_12);
		
		JButton button_1 = new JButton(" 编辑 ");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = roleView.getSelectedIndex();
				if (index == -1)
					return;

				DefaultListModel<Object> model = (DefaultListModel<Object>) roleView.getModel();
				String value = EditorEnvironment.showInputDialog("编辑规则", model.getElementAt(index));
				if (value == null || value.isEmpty())
					return;

				model.set(index, value);
			}
		});
		button_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_3.add(button_1);

		JScrollPane scrollPane_2 = new JScrollPane();
		panel_2.add(scrollPane_2, BorderLayout.CENTER);

		roleView = new ModelSearchView.ListModelSearchView<>();
		roleView.setModel(new DefaultListModel<>());
		roleView.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				DefaultListModel<Object> model = (DefaultListModel<Object>) roleView.getModel();
				int index = roleView.locationToIndex(e.getPoint());
				if (index != -1) {
					roleView.setToolTipText(model.getElementAt(index).toString());
				}
			}
		});
		scrollPane_2.setViewportView(roleView);

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		panel_5.add(toolBar, BorderLayout.SOUTH);
		toolBar.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		JButton button = new JButton("添加");
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				viewControl.addRequirement();
			}
		});

		copyAdd = new JCheckBox("复制新增");
		copyAdd.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(copyAdd);
		toolBar.add(button);
		button2 = new JButton(" 删除 ");
		button2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				viewControl.removeRequirement();
			}
		});
		toolBar.add(button2);

		toolBar.addSeparator();

		JButton button_3 = new JButton("关联模块");
		button_3.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					viewControl.setModelNode(mainControl);
				} catch (Exception e1) {
					e1.printStackTrace();
					EditorEnvironment.showException(e1);
				}
			}
		});
		toolBar.add(button_3);

		JButton button_2 = new JButton("查看模块");
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					viewControl.openModelRelation(mainControl);
				} catch (Exception e1) {
					e1.printStackTrace();
					EditorEnvironment.showException(e1);
				}
			}
		});
		button_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_2);

		toolBar.addSeparator();

		JButton button_4 = new JButton("设置依赖");
		button_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					cancelColorSelectButton.setEnabled(true);
					viewControl.setDepend(button_4);
				} catch (Exception e1) {
					e1.printStackTrace();
					EditorEnvironment.showException(e1);
				}
			}
		});
		button_4.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_4);

		JButton button_5 = new JButton("设置版本关系");
		button_5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					cancelColorSelectButton.setEnabled(true);
					viewControl.setVersionDepend(button_5);
				} catch (Exception e1) {
					e1.printStackTrace();
					EditorEnvironment.showException(e1);
				}
			}
		});
		button_5.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_5);

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(versionButton);
		buttonGroup.add(dependButton);

		cancelColorSelectButton = new JButton("取消");
		cancelColorSelectButton.setEnabled(false);
		cancelColorSelectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				viewControl.cancelSelectColor();
				cancelColorSelectButton.setEnabled(false);
			}
		});
		cancelColorSelectButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(cancelColorSelectButton);

		toolBar.addSeparator();

		JLabel label_6 = new JLabel(" 当前版本 ");
		label_6.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(label_6);

		mainVersion = new JTextField();
		mainVersion.setPreferredSize(new Dimension(100, 27));
		mainVersion.setMaximumSize(new Dimension(100, 2147483647));
		mainVersion.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		mainVersion.setColumns(10);
		toolBar.add(mainVersion);

		JPanel panel_6 = new JPanel();
		splitPane_2.setLeftComponent(panel_6);
		panel_6.setLayout(new BorderLayout(0, 0));

		JPanel panel_7 = new JPanel();
		panel_7.setOpaque(false);
		panel_6.add(panel_7, BorderLayout.SOUTH);

		JLabel label_9 = new JLabel("启用时间");
		label_9.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel_7.add(label_9);
		
		useTime = new JXDatePicker();
		useTime.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel_7.add(useTime);

		JLabel label_7 = new JLabel("计划达成时间 ");
		label_7.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel_7.add(label_7);
		
		planEndTime = new JXDatePicker();
		planEndTime.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel_7.add(planEndTime);

		JLabel label_8 = new JLabel(" 关闭时间");
		label_8.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel_7.add(label_8);
		
		closeTime = new JXDatePicker();
		closeTime.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		closeTime.setEnabled(false);
		panel_7.add(closeTime);

		JScrollPane scrollPane_1 = new JScrollPane();
		panel_6.add(scrollPane_1, BorderLayout.CENTER);

		requirementText = new JTextArea();
		requirementText.setTabSize(4);
		requirementText.setPreferredSize(new Dimension(4, 80));
		requirementText.setMinimumSize(new Dimension(4, 80));
		requirementText.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		scrollPane_1.setViewportView(requirementText);
		viewControl.tableControl.copyAdd = copyAdd;
		viewControl.tableControl.mainVersion = mainVersion;

		toolBar.addSeparator();

		JLabel label_3 = new JLabel(" 版本过滤 ");
		toolBar.add(label_3);
		label_3.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		versionFilterView = new JComboBox<>();
		versionFilterView.setMaximumSize(new Dimension(150, 32767));
		toolBar.add(versionFilterView);
		versionFilterView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (versionFilterView.getSelectedIndex() == -1)
					return;

				try {
					viewControl.setVersionFilter(versionFilterView.getSelectedItem());
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		versionFilterView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		
				showVersion = new JCheckBox("显示版本依赖");
				toolBar.add(showVersion);
				showVersion.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						viewControl.selectDependColor();
					}
				});
				showVersion.setSelected(true);
				showVersion.setFont(new Font("微软雅黑", Font.PLAIN, 12));
				viewControl.tableControl.showVersion = showVersion;
				
						showDepend = new JCheckBox("显示依赖 ");
						toolBar.add(showDepend);
						showDepend.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								viewControl.selectDependColor();
							}
						});
						showDepend.setSelected(true);
						showDepend.setFont(new Font("微软雅黑", Font.PLAIN, 12));
						viewControl.tableControl.showDepend = showDepend;
		
		init();
	}

	protected void init() {
		viewControl.treeControl.tree = tree;
		viewControl.treeControl.dependButton = dependButton;
		viewControl.treeControl.versionButton = versionButton;
		viewControl.tableControl.table = table;
		viewControl.tableControl.usedView = usedView;
		viewControl.tableControl.idView = idView;
		viewControl.tableControl.versionView = versionView;
		viewControl.tableControl.userView = userView;
		viewControl.tableControl.levelView = levelView;
		viewControl.tableControl.typeView = typeView;
		viewControl.tableControl.closeView = closeView;
		viewControl.tableControl.roleView = roleView;
		viewControl.tableControl.requirementText = requirementText;
		viewControl.tableControl.versionFilterView = versionFilterView;
		viewControl.tableControl.useTime = useTime;
		viewControl.tableControl.closeTime = closeTime;
		viewControl.tableControl.planEndTime = planEndTime;
		viewControl.onEditing = new IEditing() {

			@Override
			public void onEdit(boolean isEdit) {
				RequirementBuilder.this.isEdit = isEdit;
			}
		};


		viewControl.setEvents();
	}
	@Override
	public void onSave() {
		try {
			viewControl.save();
			viewControl.saveMainVersion();
			viewControl.fireEditing(false);
		} catch (Exception e) {
			e.printStackTrace();
			EditorEnvironment.showException(e);
		}
	}

	@Override
	public void onLoad() {
		try {
			if (isEdit){
				if (EditorEnvironment.showConfirmDialog("当前项目已经修改，是否保存？", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
					onSave();
			}
			viewControl.load((File) null);
			isEdit = false;
		} catch (Exception e) {
			e.printStackTrace();
			EditorEnvironment.showException(e);
		}
	}

	@Override
	public void onClose() {
		if (viewControl.requirementFile != null)
			EditorEnvironment.unlockFile(viewControl.requirementFile);
		mainControl.initRequirementVersionMenu();
	}

	@Override
	public void onPublish(HashMap<String, DrawNode> uikeysWorkflowNodes, Object param) throws Exception {
	}

	@Override
	public void onStart(Object param) {
		File file = (File) param;
		if (file != null && file.exists())
			if (!EditorEnvironment.lockFile(file)) {
				EditorEnvironment.showMessage("文件【" + file.getAbsolutePath() + "】已经被其他用户锁定，请稍后再试！");
				return;
			}

		try {
			viewControl.load(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	ChildForm parentForm;
	private JScrollPane contentScrollBar;
	private JTable table;
	private JRadioButton dependButton;
	private JRadioButton versionButton;
	private JCheckBox usedView;
	private JTextField idView;
	private JTextField versionView;
	private JTextField userView;
	private JSpinner levelView;
	private JComboBox<String> typeView;
	private JComboBox<String> versionFilterView;
	private JCheckBox copyAdd;
	private JCheckBox closeView;
	private JList<Object> roleView;
	private JCheckBox showVersion;
	private JCheckBox showDepend;
	private JTextField mainVersion;
	private JButton cancelColorSelectButton;
	private JTextArea requirementText;
	private JXDatePicker useTime;
	private JXDatePicker planEndTime;
	private JXDatePicker closeTime;

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
		return viewControl.requirementFile;
	}
}
