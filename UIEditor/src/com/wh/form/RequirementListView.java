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
import java.util.HashMap;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

import org.jdesktop.swingx.JXDatePicker;

import com.wh.control.EditorEnvironment;
import com.wh.control.ScrollToolBar;
import com.wh.control.modelsearch.ModelSearchView;
import com.wh.control.requirement.ViewControl;
import com.wh.control.requirement.ViewControl.IEditing;
import com.wh.control.requirement.ViewControl.RequirementState;
import com.wh.draws.DrawNode;
import com.wh.system.tools.EventHelp;

public class RequirementListView extends ChildForm implements IMainMenuOperation, ISubForm {
	private static final long serialVersionUID = 1647458287761184720L;

	private final JPanel contentPanel = new JPanel();
	private JPanel panel_5;

	private ViewControl viewControl = new ViewControl();

	/**
	 * Create the dialog.
	 */
	public RequirementListView(IMainControl mainControl) {
		super(mainControl);
		setResizable(false);
		setBounds(100, 100, 1551, 683);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPanel.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPanel);
		panel_5 = new JPanel();
		panel_5.setOpaque(false);
		contentPanel.add(panel_5, BorderLayout.CENTER);
		panel_5.setLayout(new BorderLayout(0, 0));

		JSplitPane splitPane_1 = new JSplitPane();
		splitPane_1.setOpaque(false);
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
		contentScrollBar.setOpaque(false);
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

		JLabel label_3 = new JLabel(" 类别 ");
		label_3.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_2.add(label_3);

		classView = new JComboBox<String>();
		classView.setMaximumSize(new Dimension(150, 32767));
		classView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		classView.addActionListener(viewControl.tableControl.comboBoxActionListener);
		toolBar_2.add(classView);

		usedView = new JCheckBox("启用");
		usedView.setEnabled(false);
		usedView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		usedView.setSelected(true);
		toolBar_2.add(usedView);

		toolBar_2.addSeparator();

		closeView = new JCheckBox("关闭");
		closeView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_2.add(closeView);

		toolBar_2.addSeparator();

		showVersion = new JCheckBox("显示版本依赖");
		showVersion.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				viewControl.selectDependColor();
			}
		});
		showVersion.setSelected(true);
		showVersion.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_2.add(showVersion);

		showDepend = new JCheckBox("显示依赖 ");
		showDepend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				viewControl.selectDependColor();
			}
		});
		showDepend.setSelected(true);
		showDepend.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_2.add(showDepend);

		toolBar_2.addSeparator();

		JLabel label = new JLabel(" 编号 ");
		label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_2.add(label);

		idView = new JTextField();
		idView.setEditable(false);
		idView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_2.add(idView);
		idView.setColumns(10);

		JLabel label_4 = new JLabel(" 级别 ");
		label_4.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_2.add(label_4);

		levelView = new JSpinner();
		levelView.setEnabled(false);
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
		userView.setEditable(false);
		userView.setMaximumSize(new Dimension(100, 2147483647));
		userView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		userView.setColumns(10);
		toolBar_2.add(userView);

		JLabel label_5 = new JLabel(" 类型 ");
		label_5.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_2.add(label_5);

		typeView = new JComboBox<>();
		typeView.setEnabled(false);
		typeView.setModel(new DefaultComboBoxModel<>(new String[] { "基本需求", "增强需求", "规划需求" }));
		typeView.setSelectedIndex(0);
		typeView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_2.add(typeView);

		toolBar_2.addSeparator();

		JButton button = new JButton("保存");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					viewControl.saveDynamicInfo();
				} catch (Exception e1) {
					e1.printStackTrace();
					EditorEnvironment.showException(e1);
				}
			}
		});

		JButton button_1 = new JButton("导出");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				viewControl.export(version);
			}
		});
		button_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_2.add(button_1);

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
		toolBar_2.add(button_2);
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_2.add(button);

		panel_1.add(splitPane_2, BorderLayout.CENTER);

		JPanel panel_2 = new JPanel();
		splitPane_2.setRightComponent(panel_2);
		panel_2.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane_2 = new JScrollPane();
		panel_2.add(scrollPane_2, BorderLayout.CENTER);

		roleView = new ModelSearchView.ListModelSearchView<>();
		roleView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
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
		
		JPanel panel_6 = new JPanel();
		splitPane_2.setLeftComponent(panel_6);
		panel_6.setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		panel_6.add(panel, BorderLayout.SOUTH);
		
		JLabel label_6 = new JLabel("启用时间");
		label_6.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel.add(label_6);
		
		useTime = new JXDatePicker();
		useTime.setEnabled(false);
		useTime.setFormats(new String[] {"yyyy-MM-dd hh:mm:ss", "yyyy-MM-dd HH:mm:ss"});
		useTime.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel.add(useTime);
		
		JLabel label_7 = new JLabel("计划达成时间 ");
		label_7.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel.add(label_7);
		
		planEndTime = new JXDatePicker();
		planEndTime.setEnabled(false);
		planEndTime.setFormats(new String[] {"yyyy-MM-dd HH:mm:ss"});
		planEndTime.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel.add(planEndTime);
		
		JLabel label_8 = new JLabel(" 关闭时间");
		label_8.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel.add(label_8);
		
		JScrollPane scrollPane = new JScrollPane();
		panel_6.add(scrollPane, BorderLayout.CENTER);
		
		requirementText = new JTextArea();
		requirementText.setEditable(false);
		requirementText.setTabSize(4);
		requirementText.setPreferredSize(new Dimension(4, 80));
		requirementText.setMinimumSize(new Dimension(4, 80));
		requirementText.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		scrollPane.setViewportView(requirementText);
		
		closeTime = new JXDatePicker();
		closeTime.setFormats(new String[] {"yyyy-MM-dd HH:mm:ss"});
		closeTime.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		closeTime.getEditor().setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel.add(closeTime);

		init();
	}

	protected void init() {
		viewControl.tableControl.isView = true;
		viewControl.tableControl.table = table;
		viewControl.tableControl.usedView = usedView;
		viewControl.tableControl.idView = idView;
		viewControl.tableControl.versionView = versionView;
		viewControl.tableControl.userView = userView;
		viewControl.tableControl.levelView = levelView;
		viewControl.tableControl.typeView = typeView;
		viewControl.tableControl.closeView = closeView;
		viewControl.tableControl.roleView = roleView;
		viewControl.tableControl.classView = classView;
		viewControl.tableControl.showDepend = showDepend;
		viewControl.tableControl.showVersion = showVersion;
		viewControl.tableControl.useTime = useTime;
		viewControl.tableControl.closeTime = closeTime;
		viewControl.tableControl.planEndTime = planEndTime;
		viewControl.tableControl.requirementText = requirementText;
		viewControl.onEditing = new IEditing() {

			@Override
			public void onEdit(boolean isEdited) {
				isEdit = isEdited;
			}
		};
		viewControl.setEvents();
	}
	@Override
	public void onSave() {
		try {
			viewControl.saveDynamicInfos();
		} catch (Exception e) {
			e.printStackTrace();
			EditorEnvironment.showException(e);
		}
	}

	String version;
	RequirementState requirementState = RequirementState.rsAll;

	@Override
	public void onLoad() {
		try {
			viewControl.load(version, requirementState, true);
		} catch (Exception e) {
			e.printStackTrace();
			EditorEnvironment.showException(e);
		}
	}

	@Override
	public void onClose() {
		if (viewControl.requirementFile != null)
			EditorEnvironment.unlockFile(viewControl.requirementFile);
	}

	@Override
	public void onPublish(HashMap<String, DrawNode> uikeysWorkflowNodes, Object param) throws Exception {
	}

	@Override
	public void onStart(Object param) {
		if (param instanceof String) {
			version = (String) param;
		} else if (param instanceof Object[]) {
			Object[] datas = (Object[]) param;
			version = (String) datas[0];
			requirementState = (RequirementState) datas[1];
		}
		onLoad();
	}

	ChildForm parentForm;
	private JScrollPane contentScrollBar;
	private JTable table;
	private JCheckBox usedView;
	private JTextField idView;
	private JTextField versionView;
	private JTextField userView;
	private JSpinner levelView;
	private JComboBox<String> typeView;
	private JCheckBox closeView;
	private JList<Object> roleView;
	private JCheckBox showVersion;
	private JCheckBox showDepend;
	private JComboBox<String> classView;
	private JTextArea requirementText;
	private JXDatePicker useTime;
	private JXDatePicker closeTime;
	private JXDatePicker planEndTime;

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
