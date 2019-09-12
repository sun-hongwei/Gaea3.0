package com.wh.form;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXHyperlink;
import org.json.JSONArray;
import org.json.JSONObject;

import com.alee.laf.WebLookAndFeel;
import com.wh.control.EditorEnvironment;
import com.wh.control.EditorEnvironment.CheckType;
import com.wh.control.EditorEnvironment.ICheckCallBack;
import com.wh.control.EditorEnvironment.IDispatchCallback;
import com.wh.control.EditorEnvironment.IPublish;
import com.wh.control.EditorEnvironment.ITraverseDrawNode;
import com.wh.control.EditorEnvironment.ModelNodeInfo;
import com.wh.control.EditorEnvironment.NodeDescInfo;
import com.wh.control.EditorEnvironment.RegionName;
import com.wh.control.ExportToWord;
import com.wh.control.RunFlowFile;
import com.wh.control.grid.GridCellEditor.ActionResult;
import com.wh.control.requirement.RequirementController;
import com.wh.control.requirement.RequirementController.RequirementTypeInfo;
import com.wh.control.requirement.ViewControl.RequirementState;
import com.wh.dialog.DocExportSetupDialog;
import com.wh.dialog.IEditNode;
import com.wh.dialog.WaitDialog;
import com.wh.dialog.WaitDialog.IProcess;
import com.wh.dialog.editor.MenuEditorDialog;
import com.wh.dialog.editor.NavigationEditorDialog;
import com.wh.dialog.editor.UISelectDialog;
import com.wh.dialog.editor.UISelectDialog.Result;
import com.wh.dialog.help.HelpDialog;
import com.wh.dialog.selector.KeyValueSelector;
import com.wh.dialog.selector.KeyValueSelector.IActionListener;
import com.wh.dialog.selector.KeyValueSelector.ICheckValue;
import com.wh.dialog.selector.KeyValueSelector.IEditRow;
import com.wh.dialog.selector.KeyValueSelector.ModelResult;
import com.wh.dialog.selector.KeyValueSelector.RowResult;
import com.wh.dialog.selector.ListSelector;
import com.wh.draws.AppWorkflowNode.JSDefaultFileNode;
import com.wh.draws.DrawCanvas;
import com.wh.draws.DrawNode;
import com.wh.draws.FlowNode.ChildFlowNode;
import com.wh.draws.UICanvas;
import com.wh.draws.UINode;
import com.wh.draws.WorkflowCanvas;
import com.wh.draws.WorkflowNode;
import com.wh.draws.WorkflowNode.BeginNode;
import com.wh.draws.WorkflowNode.ChildWorkflowNode;
import com.wh.draws.WorkflowNode.EndNode;
import com.wh.draws.drawinfo.ClickableInfo;
import com.wh.draws.drawinfo.DrawInfo;
import com.wh.form.MainForm.ToolBarButtons.ButtonInfo;
import com.wh.global.Defines;
import com.wh.system.tools.FileCache;
import com.wh.system.tools.FileHelp;
import com.wh.system.tools.JsonHelp;
import com.wh.system.tools.Tools;

public class MainForm implements IMainControl {

	public static final String itemFieldName = "功能名称";

	private JFrame mainFrame;

	public static final HashMap<String, String> NoCheckTableNames = new HashMap<>();

	/*
	 * public String checkUserID() { IDBConnection db = getDB(); String
	 * workflow_fix = "workflow_"; List<String> tables = db.getTables(); String
	 * updateTables = null; for (String table : tables) { if (table.length() >
	 * workflow_fix.length() && table.substring(0,
	 * workflow_fix.length()).compareTo(workflow_fix) == 0) continue;
	 * 
	 * if (NoCheckTableNames.containsKey(table)) continue;
	 * 
	 * List<FieldMetaInfo> fields = db.getFieldDefines(table); boolean hasUserid
	 * = false; for (FieldMetaInfo fieldMetaInfo : fields) { if
	 * (fieldMetaInfo.name.compareToIgnoreCase("userid") == 0) { hasUserid =
	 * true; break; } }
	 * 
	 * if (!hasUserid) { boolean autoFix = isAutoFixUserID.isSelected(); if
	 * (!autoFix) if (EditorEnvironment.showConfirmDialog("表【" + table +
	 * "】未发现userid字段，是否自动添加", "用户ID字段检查", JOptionPane.YES_NO_OPTION) ==
	 * JOptionPane.YES_OPTION) { autoFix = true; } try { if (autoFix) {
	 * db.directExecute("ALTER TABLE " + table +
	 * " ADD userid varchar(50) not null"); updateTables = updateTables == null
	 * ? table : updateTables + "," + table; } } catch (Exception e1) {
	 * e1.printStackTrace(); EditorEnvironment.showException(e1); } } }
	 * 
	 * return updateTables;
	 * 
	 * }
	 * 
	 */

	protected static void createMainForm() {
		MainForm window = new MainForm();
		mainControl = window;
		JFrame frame = window.mainFrame;
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				boolean needClose = true;
				for (JInternalFrame frame : window.desktopPane.getAllFrames()) {
					if (frame instanceof ChildForm) {
						ChildForm form = (ChildForm) frame;
						if (form.isEdit) {
							int hRet = EditorEnvironment.showConfirmDialog("您当前有未保存的工作，是否保存后退出系统？", "退出系统",
									JOptionPane.YES_NO_CANCEL_OPTION);
							switch (hRet) {
							case JOptionPane.YES_OPTION:
								window.saveAll();
								needClose = false;
								break;
							case JOptionPane.NO_OPTION:
								break;
							default:
								return;
							}
							break;
						}
					}
				}

				for (JInternalFrame frame : window.desktopPane.getAllFrames()) {
					if (frame instanceof ChildForm) {
						ChildForm form = (ChildForm) frame;
						if (needClose && form instanceof IMainMenuOperation)
							((IMainMenuOperation) form).onClose();
					}
				}

				System.exit(0);
			}
		});

		Tools.showMaxFrame(frame);
	}

	/**
	 * Launch the application.
	 */
	public static IMainControl mainControl = null;

	public static void main(String[] args) {
		try {
			// JFrame.setDefaultLookAndFeelDecorated(true);
			// JDialog.setDefaultLookAndFeelDecorated(true);

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						Locale.setDefault(Locale.CHINA);

						WebLookAndFeel.globalControlFont = new FontUIResource("微软雅黑", 0, 12);
						WebLookAndFeel.globalMenuFont = new FontUIResource("微软雅黑", 0, 12);
						WebLookAndFeel.globalAcceleratorFont = new FontUIResource("微软雅黑", 0, 12);
						WebLookAndFeel.globalAlertFont = new FontUIResource("微软雅黑", 0, 12);
						WebLookAndFeel.globalTextFont = new FontUIResource("微软雅黑", 0, 12);
						WebLookAndFeel.globalTitleFont = new FontUIResource("微软雅黑", 0, 13);
						WebLookAndFeel.globalTooltipFont = new FontUIResource("微软雅黑", 0, 12);
						WebLookAndFeel.buttonFont = new FontUIResource("微软雅黑", 0, 12);
						// WebTableStyle.rowHeight = 48;

						WebLookAndFeel.install();

						// Tools.selectOpenFiles(null, null, null, null);

						createMainForm();
						// UIManager.put("ScrollBar.width", 20);
						// UIManager.put("ScrollBar.maximumThumbSize", new
						// Dimension(20, 20));
					} catch (Exception e) {
						EditorEnvironment.showException(e);
					}

				}
			});
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	/**
	 * Create the application.
	 */
	public MainForm() {
		initialize();
		mainForm = this;
	}

	JDesktopPane desktopPane = new JDesktopPane();
	/**
	 * Initialize the contents of the frame.
	 */

	ChildForm topFrame;

	class ToolBarButtons implements ActionListener {
		class ButtonInfo {
			public JButton button;
			public ChildForm form;
			public String title;
		}

		HashMap<ChildForm, ButtonInfo> forms = new HashMap<>();
		HashMap<JButton, ButtonInfo> buttons = new HashMap<>();

		protected void setChecked(JButton button) {
			Font defaultFont = new Font("微软雅黑", Font.PLAIN, 12);
			Font foregroundFont = new Font("微软雅黑", Font.ITALIC | Font.BOLD, 12);
			for (ButtonInfo info : buttons.values()) {
				if (info.button == button) {
					info.button.setForeground(Color.MAGENTA);
					info.button.setFont(foregroundFont);
					topFrame = info.form;
					info.button.setSelected(true);
				} else {
					info.button.setForeground(Color.BLACK);
					info.button.setFont(defaultFont);
					info.button.setSelected(false);
				}
				info.button.updateUI();
			}
		}

		public void switchMain() {
			if (desktopPane.getAllFrames().length > 0) {
				for (JInternalFrame frame : desktopPane.getAllFrames()) {
					if (frame instanceof ModelflowBuilder) {
						ModelflowBuilder form = (ModelflowBuilder) frame;
						if (form.workflowName.equals(EditorEnvironment.getMainModelRelationFileName())) {
							add(form, null, true);
							return;
						}
					}
				}
			}
		}

		public void add(ChildForm form, String title, boolean resetLinkToolBar) {
			// if (resetLinkToolBar)
			// refreshFlowPath();

			if (forms.containsKey(form)) {
				ButtonInfo info = forms.get(form);
				switchButton(info.button);
				return;
			}
			ButtonInfo info = new ButtonInfo();
			info.button = new JButton(title);
			info.form = form;
			info.title = title;

			info.button.addActionListener(this);

			buttons.put(info.button, info);
			forms.put(info.form, info);
			ways.add(info.button);

			setChecked(info.button);

			ways.setVisible(true);
			desktopPane.revalidate();
		}

		public void remove(ChildForm form) {
			if (forms.containsKey(form)) {
				ButtonInfo info = forms.remove(form);
				buttons.remove(info.button);

				ways.remove(info.button);

				if (ways.getComponentCount() == 0)
					ways.setVisible(false);
				else
					ways.updateUI();
			}

		}

		public void clear() {
			for (ButtonInfo info : forms.values()) {
				remove(info.form);
			}

			buttons.clear();
			forms.clear();

			ways.removeAll();
			ways.setVisible(false);
		}

		public void switchButton(JButton button) {
			if (buttons.containsKey(button)) {
				ButtonInfo info = buttons.get(button);
				setChecked(info.button);
				setTopForm(info.form);
				// refreshFlowPath();
			}
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			JButton button;
			if (e.getSource() instanceof JTagMenuItem) {
				button = (JButton) ((JTagMenuItem) e.getSource()).tag;
			} else
				button = (JButton) e.getSource();
			switchButton(button);
		}

	}

	public void setTopForm(ChildForm form) {
		topFrame = form;
		for (ButtonInfo buttonInfo : toolBarButtons.buttons.values()) {
			buttonInfo.form.setVisible(false);
		}
		form.setVisible(true);
		form.toFront();
		form.requestFocus();
	}

	ToolBarButtons toolBarButtons = new ToolBarButtons();

	@Override
	public void setTitle(String title) {
		if (title == null || title.isEmpty()) {
			mainFrame.setTitle(Defines.AppTitle + "   " + HelpDialog.VERSION);
		} else
			mainFrame.setTitle(Defines.AppTitle + " 当前项目 【" + title + "】");
	}

	public void updateUIButtonTitle(ChildForm from, String title) {
		toolBarButtons.add(from, title, false);
	}

	public IEditNode onWorkflowEditNode = new IEditNode() {

		@Override
		public void onEditUI(ISubForm iSubForm, DrawNode node) {
			HashMap<String, DrawNode> hashNodes = new HashMap<>();

			DrawCanvas canvas = node.getCanvas();
			if (canvas != null) {
				List<DrawNode> nodes = canvas.getNodes();
				for (DrawNode n : nodes) {
					hashNodes.put(n.id, n);
				}
			}
			openUIBuilder(node, node.title, hashNodes);
		}

		@Override
		public void onEditSubWorkflow(ISubForm iSubForm, DrawNode node) {
			openModelflowRelation(node, null);
		}
	};

	public String getCurModelRelationID() {
		if (topFrame == null)
			return null;

		String nodeid;
		if (topFrame instanceof ModelflowBuilder) {
			ModelflowBuilder form = (ModelflowBuilder) topFrame;
			nodeid = EditorEnvironment.getModelRelationNameFromFileName(form.workflowName);
		} else if (topFrame instanceof UIBuilder) {
			UIBuilder form = (UIBuilder) topFrame;
			try {
				nodeid = EditorEnvironment.getModelRelationNameFromNodeID(form.workflowNode.id);
			} catch (Exception e1) {
				e1.printStackTrace();
				return null;
			}
		} else {
			return null;
		}

		return nodeid;
	}

	public String getCurModelNodeID() {
		if (topFrame == null)
			return null;

		DrawNode node = null;
		if (topFrame instanceof ModelflowBuilder) {
			ModelflowBuilder form = (ModelflowBuilder) topFrame;
			node = form.canvas.getSelected();
		} else if (topFrame instanceof UIBuilder) {
			UIBuilder form = (UIBuilder) topFrame;
			node = form.canvas.getSelected();
		} else {
			return null;
		}

		return node == null ? null : node.id;
	}

	public void switchUIOrModelNode() {
		if (topFrame == null)
			return;

		if (topFrame instanceof ModelflowBuilder) {
			ModelflowBuilder form = (ModelflowBuilder) topFrame;
			WorkflowNode node = (WorkflowNode) form.canvas.getSelected();
			if (node == null || node instanceof BeginNode || node instanceof EndNode
					|| node instanceof ChildWorkflowNode)
				return;

			onWorkflowEditNode.onEditUI(form, node);
		} else if (topFrame instanceof UIBuilder) {
			UIBuilder form = (UIBuilder) topFrame;
			File file;
			try {
				file = EditorEnvironment.getModelRelationFileFromNodeID(form.workflowNode.id);
				openExistsModelflowRelation(file, form.workflowNode);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	class TagJXHyperlink extends JXHyperlink {
		private static final long serialVersionUID = 1L;
		public Object tag = null;
	}

	// public void refreshFlowPath() {
	// linkToolBar.removeAll();
	// String modelRelationID = getCurModelRelationID();
	// if (modelRelationID == null)
	// return;
	//
	// try {
	// File modelRelationFile =
	// EditorEnvironment.getModelNodeFile(modelRelationID);
	// if (modelRelationFile == null)
	// return;
	//
	// List<ResultModelRelationInfo> paths =
	// EditorEnvironment.getParentModelRelationFiles(modelRelationFile);
	//
	// for (int i = paths.size() - 1; i >= 0; i--) {
	// ResultModelRelationInfo info = paths.get(i);
	// NodeDescInfo descInfo = null;
	// if (info.containChildNode != null) {
	// descInfo = EditorEnvironment.getModelNodeDescInfo(
	// EditorEnvironment.getModelRelationName(info.modelRelationFile),
	// info.containChildNode.id);
	// } else {
	// descInfo =
	// EditorEnvironment.getChildModelNodeDescInfo(info.modelRelationFile.getName());
	// }
	//
	// if (descInfo == null)
	// continue;
	//
	// TagJXHyperlink button = new TagJXHyperlink();
	// button.setFont(new Font("微软雅黑", 0, 12));
	// button.setText(descInfo.title);
	// button.tag = info;
	// button.addActionListener(new ActionListener() {
	//
	// @Override
	// public void actionPerformed(ActionEvent e) {
	// TagJXHyperlink button = (TagJXHyperlink) e.getSource();
	// ResultModelRelationInfo info = (ResultModelRelationInfo) button.tag;
	// if (info == null) {
	// return;
	// }
	//
	// openModelflowRelation(EditorEnvironment.getModelRelationName(info.modelRelationFile),
	// false);
	// }
	// });
	// if (linkToolBar.getComponentCount() > 0)
	// linkToolBar.addSeparator(new Dimension(5, 1));
	//
	// linkToolBar.add(button);
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	//
	// }
	//
	public boolean existsAndFront(String id, FormType ft) {
		return existsAndFront(id, ft, true);
	}

	public boolean existsAndFront(String id, FormType ft, boolean resetLinkToolBar) {
		for (JInternalFrame tmp : desktopPane.getAllFrames()) {
			String name = null;
			switch (ft) {
			case ftSystemRegion:
				if (tmp instanceof ModelflowBuilder) {
					ModelflowBuilder editor = (ModelflowBuilder) tmp;
					name = editor.canvas.getPageConfig().id;
					break;
				} else
					continue;
			case ftRun:
				if (tmp instanceof RunFlowBuilder) {
					RunFlowBuilder editor = (RunFlowBuilder) tmp;
					name = editor.canvas.getPageConfig().id;
				} else {
					continue;
				}
				break;
			case ftScene:
				if (tmp instanceof SceneBuilder) {
					SceneBuilder editor = (SceneBuilder) tmp;
					name = editor.getNodeId();
				} else {
					continue;
				}
				break;
			case ftUI:
				if (tmp instanceof UIBuilder) {
					UIBuilder uiForm = (UIBuilder) tmp;
					String uiname = uiForm.getUIName();
					name = FileHelp.removeExt(uiname);
				} else {
					continue;
				}
				break;
			case ftReport:
				if (tmp instanceof ReportBuilder) {
					ReportBuilder editor = (ReportBuilder) tmp;
					name = editor.getId();
				} else {
					continue;
				}
				break;
			case ftWorkflow:
				if (tmp instanceof ModelflowBuilder) {
					ModelflowBuilder workflowForm = (ModelflowBuilder) tmp;
					if (workflowForm.workflowName == null || workflowForm.workflowName.isEmpty())
						continue;

					name = FileHelp.removeExt(workflowForm.workflowName);
				} else {
					continue;
				}
				break;
			case ftFlow:
				if (tmp instanceof WorkflowBuilder) {
					WorkflowBuilder flowForm = (WorkflowBuilder) tmp;
					if (flowForm.workflowName == null || flowForm.workflowName.isEmpty())
						continue;

					name = FileHelp.removeExt(flowForm.workflowName);
				} else {
					continue;
				}
				break;
			case ftRequirement:
				if (tmp instanceof RequirementBuilder) {
					RequirementBuilder form = (RequirementBuilder) tmp;
					name = form.viewControl.requirementFile.getAbsolutePath();
				} else {
					continue;
				}
				break;
			case ftRequirementVersion:
				if (tmp instanceof RequirementListView) {
					RequirementListView form = (RequirementListView) tmp;
					name = form.version + form.requirementState.name();
				} else {
					continue;
				}
				break;
			case ftAppWorkflow:
				if (tmp instanceof CodeFlowBuilder) {
					CodeFlowBuilder flowForm = (CodeFlowBuilder) tmp;
					if (flowForm.file == null)
						continue;

					name = FileHelp.removeExt(flowForm.file.getName());
				} else {
					continue;
				}
				break;
			default:
				continue;

			}
			if (name.compareTo(id) == 0) {
				toolBarButtons.add((ChildForm) tmp, name, resetLinkToolBar);
				return true;
			}
		}

		File subWorkflowFile = EditorEnvironment.getChildModelRelationFile(id, false);
		if (subWorkflowFile != null)
			return existsAndFront(FileHelp.removeExt(subWorkflowFile.getName()), ft);
		return false;
	}

	@Override
	public boolean openModelflowRelation(DrawNode node, String[] selectNodeIds) {
		return openModelflowRelation(node, selectNodeIds, true);
	}

	public boolean openModelflowRelation(DrawNode node, String[] selectNodeIds, boolean resetLinkToolBar) {
		return openModelflowRelation(node.id, node.title, selectNodeIds, resetLinkToolBar);
	}

	public boolean openModelflowRelation(String childNodeId, String title, String[] selectNodeIds,
			boolean resetLinkToolBar) {
		if (existsAndFront(childNodeId, FormType.ftWorkflow))
			return true;

		File subWorkflowFile = EditorEnvironment.getChildModelRelationFile(childNodeId, true);
		return openModelflowRelation(subWorkflowFile, title, selectNodeIds, resetLinkToolBar);
	}

	public boolean openModelflowRelation(File modelflowFile, String title, String[] selectNodeIds,
			boolean resetLinkToolBar) {
		if (!EditorEnvironment.isMainModelRelation(modelflowFile) && !EditorEnvironment.lockFile(modelflowFile)) {
			EditorEnvironment.showMessage("文件【" + modelflowFile.getAbsolutePath() + "】已经被其他用户锁定，请稍后再试！");
			return false;
		}

		try {
			if (modelflowFile != null) {
				if (existsAndFront(FileHelp.removeExt(modelflowFile.getName()), FormType.ftWorkflow))
					return true;
			}

			ModelflowBuilder subForm = (ModelflowBuilder) openFrame(ModelflowBuilder.class, true);
			ISubForm iSubForm = (ISubForm) subForm;
			try {
				// DrawNode childNode =
				// EditorEnvironment.getChildModelNodeFromFile(modelflowFile);
				if (modelflowFile != null) {
					String[] params = new String[] { modelflowFile.getName() };
					iSubForm.onStart(params);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			toolBarButtons.add(topFrame, title + "[模块关系图设计]", resetLinkToolBar);
			return true;
		} finally {
			if (selectNodeIds != null && selectNodeIds.length > 0) {
				DrawCanvas canvas = ((ModelflowBuilder) topFrame).canvas;
				DrawNode[] selNodes = new DrawNode[selectNodeIds.length];
				for (int i = 0; i < selectNodeIds.length; i++) {
					selNodes[i] = canvas.getNode(selectNodeIds[i]);
				}
				canvas.setSelecteds(selNodes);
			}
		}

	}

	public boolean openExistsModelflowRelation(File file, DrawNode selectNode) {
		return openExistsModelflowRelation(file, selectNode == null ? null : new DrawNode[] { selectNode });
	}

	public boolean openExistsModelflowRelation(File file, DrawNode[] selectNodes) {
		DrawNode parentNode;
		try {
			if (file.equals(EditorEnvironment.getMainModelRelationFile())) {
				toolBarButtons.switchMain();
				return true;
			}

			parentNode = EditorEnvironment.getChildModelNodeFromFile(file);
			if (parentNode == null)
				return false;

			String[] idStrings = null;
			if (selectNodes != null && selectNodes.length > 0) {
				idStrings = new String[selectNodes.length];
				int index = 0;
				for (DrawNode drawNode : selectNodes) {
					idStrings[index++] = drawNode.id;
				}
			}
			return openModelflowRelation(parentNode, idStrings);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void openModelflowRelation(String workflowRelationName, String selectNodeId) {
		openModelflowRelation(EditorEnvironment.getModelFile(FileHelp.removeExt(workflowRelationName)), null,
				new String[] { selectNodeId }, true);
	}

	/**
	 * 内部打开模块编辑器，仅openframe使用
	 * 
	 * @param workflowRelationName
	 *            要打开的模块关系图名称
	 * @param resetLinkToolBar
	 *            是否重置快捷按钮，仅当在快捷按钮单击事件中调用时传入false，其他应为true
	 */
	protected void openModelflowRelation(String workflowRelationName, boolean resetLinkToolBar) {
		if (workflowRelationName != null
				&& existsAndFront(FileHelp.removeExt(workflowRelationName), FormType.ftWorkflow, resetLinkToolBar))
			return;

		try {

			boolean hasModelRelationName = workflowRelationName != null && !workflowRelationName.isEmpty();
			if (hasModelRelationName) {
				File path = EditorEnvironment.getProjectFile(EditorEnvironment.Workflow_Dir_Name, workflowRelationName);

				if (!EditorEnvironment.isMainModelRelation(path) && !EditorEnvironment.lockFile(path)) {
					EditorEnvironment.showMessage("文件【" + path.getAbsolutePath() + "】已经被其他用户锁定，请稍后再试！");
					return;
				}
			}

			ModelflowBuilder form = new ModelflowBuilder(this);
			if (hasModelRelationName)
				form.onStart(workflowRelationName);
			topFrame = form;
			form.onEditNode = onWorkflowEditNode;
			if (!hasModelRelationName)
				return;

			String name = workflowRelationName;
			if (name.compareTo(EditorEnvironment.getMainModelRelationFileName()) == 0)
				name = "主关系图";
			else
				name = EditorEnvironment.getModelRelationName(new File(name));
			toolBarButtons.add(topFrame, name + "[模块关系图设计]", resetLinkToolBar);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void openSystemRegionEditor() {
		try {
			File path = EditorEnvironment.getProjectFile(EditorEnvironment.Template_Path,
					EditorEnvironment.System_FileName);

			if (existsAndFront(EditorEnvironment.System_FileName, FormType.ftSystemRegion, false))
				return;

			if (!EditorEnvironment.isMainModelRelation(path) && !EditorEnvironment.lockFile(path)) {
				EditorEnvironment.showMessage("其他用户正在编辑系统区域，请稍后再试！");
				return;
			}

			ModelflowBuilder form = (ModelflowBuilder) openFrame(ModelflowBuilder.class, true);
			form.canvas.getPageConfig().id = EditorEnvironment.System_FileName;
			form.canvas.nodes.put(RegionName.rnTop.name(), EditorEnvironment.getFrameModelNode(RegionName.rnTop));
			form.canvas.nodes.put(RegionName.rnBottom.name(), EditorEnvironment.getFrameModelNode(RegionName.rnBottom));
			form.canvas.nodes.put(RegionName.rnLeft.name(), EditorEnvironment.getFrameModelNode(RegionName.rnLeft));
			form.canvas.nodes.put(RegionName.rnRight.name(), EditorEnvironment.getFrameModelNode(RegionName.rnRight));
			form.onEditNode = onWorkflowEditNode;
			toolBarButtons.add(topFrame, "[系统区域编辑]", false);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void openRunWorkflow(File file) {
		String id = FileHelp.removeExt(file.getName());
		if (existsAndFront(id, FormType.ftRun))
			return;

		RunFlowFile runFlowFile = new RunFlowFile();
		runFlowFile.setFile(file);
		try {
			runFlowFile.load();

			openFrame(RunFlowBuilder.class, true, new Object[] { runFlowFile });
			updateUIButtonTitle(topFrame, runFlowFile.name + "[运行流程设计]");
		} catch (Exception e) {
			e.printStackTrace();
			EditorEnvironment.showException(e);
		}

	}

	public void openCSSEditor() {
		if (existsAndFront("GlobalCSSEditor", FormType.ftCSS))
			return;

		openFrame(CSSEditor.class, false);
		updateUIButtonTitle(topFrame, "[CSS设计]");
	}

	protected void internalOpenUIbuilder(File uiFile, String title, String workflowTitle, Object[] params) {
		if (existsAndFront(FileHelp.removeExt(uiFile.getName()), FormType.ftUI))
			return;

		if (!EditorEnvironment.lockFile(uiFile)) {
			EditorEnvironment.showMessage("文件【" + uiFile.getAbsolutePath() + "】已经被其他用户锁定，请稍后再试！");
			return;
		}

		ChildForm subForm = openFrame(UIBuilder.class, true);
		ISubForm iSubForm = (ISubForm) subForm;
		iSubForm.onStart(params);
		updateUIButtonTitle(topFrame, title + "[界面编辑]");
	}

	public void openUIBuilder(DrawNode node, String title, HashMap<String, DrawNode> nodes) {
		if (node == null)
			return;

		File uiFile = EditorEnvironment.getUIFile(node.id, true);
		if (uiFile == null) {
			EditorEnvironment.createModelNodeConfigFile(node.id);
			uiFile = EditorEnvironment.getUIFile(node.id, true);
		}

		if (!uiFile.exists()) {
			try {
				JsonHelp.saveJson(uiFile, "{}", null);
			} catch (Exception e) {
				e.printStackTrace();
				EditorEnvironment.showException(e);
				return;
			}
		}
		if ((node.title == null || node.title.isEmpty()) && title != null && !title.isEmpty())
			node.title = title;
		internalOpenUIbuilder(uiFile, node.title, "", new Object[] { node, node.title, nodes });
	}

	@Override
	public void openSceneDesign(DrawNode node) {
		if (node == null || !(node instanceof WorkflowNode))
			return;

		if (existsAndFront(node.id, FormType.ftScene))
			return;

		File appFile = SceneBuilder.getAppFile(node.id, true);
		if (!appFile.exists()) {
			try {
				JsonHelp.saveJson(appFile, new JSONArray("[]"), null);
			} catch (Exception e) {
				e.printStackTrace();
				EditorEnvironment.showException(e);
				return;
			}
		}

		if (!EditorEnvironment.lockFile(appFile)) {
			EditorEnvironment.showMessage("文件【" + appFile.getAbsolutePath() + "】已经被其他用户锁定，请稍后再试！");
			return;
		}

		openFrame(SceneBuilder.class, true, node);
		updateUIButtonTitle(topFrame, node.title + "[编辑]");

	}

	public void initRequirementMenu() {
		requireRootMenu.removeAll();
		TreeMap<String, RequirementTypeInfo> types = RequirementController.getRequirementTypes();
		for (String name : types.keySet()) {
			JMenuItem menuItem = new JMenuItem(name);
			menuItem.setToolTipText(types.get(name).name);
			menuItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					File file = RequirementController.getRequirementFile(name);
					openRequirementBuilder(file);
				}
			});
			requireRootMenu.add(menuItem);
		}
	}

	@Override
	public void initRequirementVersionMenu() {
		versionRootMenu.removeAll();
		for (String version : RequirementController.getRequirementVersions()) {
			JMenu menu = new JMenu("需求版本[" + version + "]");
			versionRootMenu.add(menu);

			JMenuItem menuItem = new JMenuItem("查看需求");
			menuItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					openRequirementListView(version);
				}
			});
			menu.add(menuItem);

			menuItem = new JMenuItem("发布需求");
			menuItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						RequirementController.publish(version);
						EditorEnvironment.showMessage("成功发布！");
						initRequirementVersionMenu();
					} catch (Exception e1) {
						e1.printStackTrace();
						EditorEnvironment.showException(e1);
					}
				}
			});
			menu.add(menuItem);

			if (RequirementController.isPublish(version)) {
				menuItem = new JMenuItem("取消发布");
				menuItem.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							RequirementController.unPublish(version);
							EditorEnvironment.showMessage("成功取消此版本的发布！");
							initRequirementVersionMenu();
						} catch (Exception e1) {
							e1.printStackTrace();
							EditorEnvironment.showException(e1);
						}
					}
				});
				menu.add(menuItem);
			}
		}
	}

	public void openRequirementBuilder(File file) {

		if (existsAndFront(file.getName(), FormType.ftRequirement))
			return;

		if (!EditorEnvironment.lockFile(file)) {
			EditorEnvironment.showMessage("需求列表已经被其他用户锁定，请稍后再试！");
			return;
		}

		openFrame(RequirementBuilder.class, true, file);
		updateUIButtonTitle(topFrame, FileHelp.removeExt(file.getName()) + "[需求编辑]");
	}

	public void openRequirementListView(String version, RequirementState requirementState) {
		if (existsAndFront(version + requirementState.name(), FormType.ftRequirementVersion))
			return;

		openFrame(RequirementListView.class, true, new Object[] { version, requirementState });

		String rsTitle = "";
		switch (requirementState) {
		case rsClose:
			rsTitle = " - 已关闭";
			break;
		case rsNoClose:
			rsTitle = " - 未关闭";
			break;
		default:
			break;
		}
		updateUIButtonTitle(topFrame, version + "版本" + rsTitle + "[需求列表查看]");
	}

	public void openRequirementListView(RequirementState requirementState) {
		Collection<String> versions = RequirementController.getRequirementVersions();
		if (versions.size() == 0) {
			EditorEnvironment.showMessage("目前您还没有设置需求类型，请设置后重试！");
			return;
		}

		Object[][] rows = new Object[versions.size()][1];
		int index = 0;
		for (Object version : versions) {
			rows[index++][0] = version;
		}
		RowResult rowResult = KeyValueSelector.showForOne(mainControl, rows, new Object[] { "需求版本" });

		Object[] result = rowResult.isok ? rowResult.row : null;

		if (result == null || result.length == 0)
			return;

		openRequirementListView((String) result[0], requirementState);
	}

	public void openRequirementListView(String version) {
		openRequirementListView(version, RequirementState.rsAll);
	}

	public boolean openUIBuilder(File file) {
		return openUIBuilder(file, (DrawNode) null);
	}

	public boolean openUIBuilder(File file, DrawNode node) {
		if (node == null)
			return openUIBuilder(file, (DrawNode[]) null);

		return openUIBuilder(file, new DrawNode[] { node });
	}

	public boolean openUIBuilder(File uiFile, DrawNode[] nodes) {
		ModelNodeInfo info;
		try {

			info = EditorEnvironment.getModelInfoFromUI(uiFile);
			if (info.node == null) {
				String title = "新建界面";
				internalOpenUIbuilder(uiFile, title, null,
						new Object[] { null, title, new HashMap<String, DrawNode>(), uiFile });
			} else
				openUIBuilder(info.node, null, info.nodes);

			if (nodes != null && nodes.length > 0) {
				DrawCanvas canvas = ((UIBuilder) topFrame).canvas;
				DrawNode[] selNodes = new DrawNode[nodes.length];
				for (int i = 0; i < nodes.length; i++) {
					if (nodes[i] == null)
						continue;
					selNodes[i] = canvas.getNode(nodes[i].id);
				}
				canvas.setSelecteds(selNodes);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public void internalOpenUIBuilder(DrawNode node, String workflowRelationTitle, HashMap<String, DrawNode> nodes) {
		if (node != null) {
			File uiFile = EditorEnvironment.getUIFile(node.id, true);
			if (uiFile == null) {
				EditorEnvironment.createModelNodeConfigFile(node.id);
				uiFile = EditorEnvironment.getUIFile(node.id, true);
			}

			if (!EditorEnvironment.lockFile(uiFile)) {
				EditorEnvironment.showMessage("文件【" + uiFile.getAbsolutePath() + "】已经被其他用户锁定，请稍后再试！");
				return;
			}
		}

		try {
			topFrame = new UIBuilder(this);
			ISubForm subForm = (ISubForm) topFrame;
			if (node != null)
				subForm.onStart(new Object[] { node, workflowRelationTitle, nodes });
			if (workflowRelationTitle == null || workflowRelationTitle.isEmpty()) {
				return;
			}
			toolBarButtons.add(topFrame, workflowRelationTitle + "[界面编辑]", false);
			//
			// updateUIButtonTitle(topFrame, workflowRelationTitle,
			// EditorEnvironment.getUIFile(node.id));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void openWorkflowRelation(String title, String name) {
		if (name == null || name.isEmpty())
			return;

		File flowFile = EditorEnvironment.getFlowRelationFile(name);
		if (flowFile != null)
			if (existsAndFront(FileHelp.removeExt(flowFile.getName()), FormType.ftFlow))
				return;
		ChildForm subForm = openFrame(WorkflowBuilder.class, true);
		ISubForm iSubForm = (ISubForm) subForm;
		openWorkflowRelation(flowFile, iSubForm, true);
		toolBarButtons.add(topFrame, "业务流程设计" + "[" + title + "]", false);
	}

	public void openSubWorkflowRelation(ChildFlowNode node) {
		if (node == null)
			return;

		File flowFile = EditorEnvironment.getFlowRelationFile(node.relationName);
		if (flowFile != null)
			if (existsAndFront(FileHelp.removeExt(flowFile.getName()), FormType.ftFlow))
				return;
		ChildForm subForm = openFrame(WorkflowBuilder.class, true);
		ISubForm iSubForm = (ISubForm) subForm;
		openWorkflowRelation(flowFile, iSubForm, true);
		toolBarButtons.add(topFrame, "流程图设计" + "[" + node.title + "]", true);
	}

	protected void openWorkflowRelation(File subWorkflowFile, ISubForm iSubForm, boolean checkFront) {
		if (subWorkflowFile != null) {
			if (checkFront && existsAndFront(FileHelp.removeExt(subWorkflowFile.getName()), FormType.ftFlow))
				return;

			try {
				iSubForm.onStart(new String[] { subWorkflowFile.getName() });
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.wh.system.form.IMainControl#openFrame(java.lang.Class, boolean,
	 * java.lang.Object)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public ChildForm openFrame(Class<? extends ChildForm> formClass, boolean needNew, Object... args) {
		if (!needNew) {
			for (JInternalFrame frame : desktopPane.getAllFrames()) {
				if (frame.getClass() == formClass) {
					if (frame.isVisible()) {
						frame.toFront();
						topFrame = (ChildForm) frame;
					}
					return null;
				}
			}
		}

		String title = "";
		if (formClass == ModelflowBuilder.class) {
			title = args.length > 0 ? (String) args[0] : null;
			openModelflowRelation(title, true);
		} else if (formClass == RunFlowBuilder.class) {
			RunFlowBuilder editor = new RunFlowBuilder(this);
			topFrame = editor;
			editor.onStart(editor, args);
		} else if (formClass == RequirementBuilder.class) {
			RequirementBuilder editor = new RequirementBuilder(this);
			topFrame = editor;
			editor.onStart((File) args[0]);
		} else if (formClass == RequirementListView.class) {
			RequirementListView editor = new RequirementListView(this);
			topFrame = editor;
			if (args.length == 1)
				editor.onStart(args[0]);
			else if (args.length > 1)
				editor.onStart(args);

		} else if (formClass == CSSEditor.class) {
			CSSEditor editor = new CSSEditor(this);
			topFrame = editor;
			((IMainMenuOperation) editor).onLoad();
		} else if (formClass == CodeFlowBuilder.class) {
			CodeFlowBuilder editor = new CodeFlowBuilder(this);
			topFrame = editor;
			((IMainMenuOperation) editor).onLoad();
		} else if (formClass == WorkflowBuilder.class) {
			title = args.length > 0 ? (String) args[0] : null;
			try {
				topFrame = new WorkflowBuilder(this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (formClass == SceneBuilder.class) {
			SceneBuilder editor = new SceneBuilder(this);
			topFrame = editor;
			editor.onStart(args[0]);
		} else if (formClass == UIBuilder.class) {
			DrawNode node = null;
			HashMap<String, DrawNode> nodes = null;
			if (args.length > 0)
				node = (DrawNode) args[0];
			if (args.length > 1)
				title = (String) args[1];
			if (args.length > 2)
				nodes = (HashMap<String, DrawNode>) args[2];
			internalOpenUIBuilder(node, title, nodes);
		} else if (formClass == ReportBuilder.class) {
			ReportBuilder editor = new ReportBuilder(this);
			topFrame = editor;
			Object[] params = new Object[2];
			if (args.length > 0)
				params[0] = (UIBuilder) args[0];
			if (args.length > 1) {
				params[1] = (UINode) args[1];
			}
			editor.onStart(params);
		} else {
			EditorEnvironment.showMessage(mainFrame, "功能未实现！", "提示", JOptionPane.INFORMATION_MESSAGE);
		}

		topFrame.setClosable(true);

		topFrame.setBounds(0, 0, desktopPane.getWidth(), desktopPane.getHeight());

		BasicInternalFrameUI ui = (BasicInternalFrameUI) topFrame.getUI();
		ui.setNorthPane(null);
		topFrame.putClientProperty("JInternalFrame.isPalette", Boolean.TRUE);

		desktopPane.add(topFrame);
		try {
			topFrame.setMaximum(true);
		} catch (PropertyVetoException e) {
			e.printStackTrace();
		}

		setTopForm(topFrame);

		return topFrame;
	}

	protected boolean close() {
		return close(true);
	}

	protected boolean close(boolean includeMain) {
		for (JInternalFrame frame : desktopPane.getAllFrames()) {
			if (!includeMain) {
				if (frame instanceof ModelflowBuilder) {
					if (((ModelflowBuilder) frame).isMain())
						continue;
				}
			}
			if (frame instanceof ChildForm) {
				if (closeWindow((ChildForm) frame, false) == frame)
					return false;

			}
		}
		// desktopPane.removeAll();
		if (desktopPane.getAllFrames().length > 0)
			topFrame = (ChildForm) desktopPane.getAllFrames()[0];
		else
			topFrame = null;

		if (desktopPane.getAllFrames().length == 0)
			setTitle(null);

		if (includeMain)
			EditorEnvironment.setCurrentProjectName(null);
		return true;
	}

	protected ChildForm closeWindow(ChildForm form) {
		return closeWindow(form, true);
	}

	protected ChildForm closeWindow(ChildForm form, boolean checkAllowQuit) {
		if (form != null && form instanceof IMainMenuOperation) {
			if (checkAllowQuit && !form.allowQuit())
				return form;

			if (form.needPrompt()) {
				int ret = EditorEnvironment.showConfirmDialog("当前有未保存的工作，是否保存？", "关闭", JOptionPane.YES_NO_OPTION);
				if (ret == JOptionPane.CLOSED_OPTION)
					return form;

				if (ret == JOptionPane.YES_OPTION) {
					saveWindow(form);
				}
			}
			((IMainMenuOperation) form).onClose();
			if (form instanceof ISubForm) {
				ChildForm parentForm = ((ISubForm) form).getParentForm();
				if (parentForm != null) {
					if (parentForm instanceof IControl) {
						((IControl) parentForm).onEnd(form);
					}
					parentForm.setVisible(true);
					selectForm(parentForm);
				}
			}

			toolBarButtons.remove(form);
			desktopPane.remove(form);
			form = null;
			JInternalFrame[] frames = desktopPane.getAllFrames();
			if (frames != null && frames.length > 0) {
				frames[0].toFront();
			}

			desktopPane.repaint();
		}
		if (form != null)
			toolBarButtons.add(form, null, true);
		else if (desktopPane.getAllFrames().length > 0)
			form = (ChildForm) desktopPane.getAllFrames()[0];
		return form;
	}

	protected void openProject(String name) {
		if (name == null) {
			return;
		}

		if (close()) {

			try {
				FileCache.clear();
			} catch (IOException e) {
				e.printStackTrace();
				EditorEnvironment.showException(e);
			}

			EditorEnvironment.setCurrentProjectName(name);

			openWorkflowEditor(
					EditorEnvironment.getRelationFileName(EditorEnvironment.Main_Workflow_Relation_FileName));

			initRequirementMenu();
			initRequirementVersionMenu();

			DataSourceManager.reset();
		}
	}

	protected void openWorkflowEditor(String workflowName) {
		openFrame(ModelflowBuilder.class, false, workflowName);
	}

	protected void saveWindow(ChildForm topFrame) {
		if (topFrame == null)
			topFrame = this.topFrame;
		
		if (topFrame != null && topFrame instanceof IMainMenuOperation) {
			IMainMenuOperation mainMenuOperation = (IMainMenuOperation) topFrame;
			mainMenuOperation.onSave();
		}
	}

	protected void saveAll() {
		for (JInternalFrame frame : desktopPane.getAllFrames()) {
			if (frame instanceof IMainMenuOperation) {
				IMainMenuOperation mainMenuOperation = (IMainMenuOperation) frame;
				mainMenuOperation.onSave();
			}
		}
	}

	public void computeTaskSum(ChildWorkflowNode node) throws Exception {
		File file;
		if (node == null) {
			if (topFrame instanceof ModelflowBuilder) {
				ModelflowBuilder form = (ModelflowBuilder) topFrame;
				file = form.canvas.canvasFile;
			} else
				file = EditorEnvironment.getMainModelRelationFile();
		} else
			file = EditorEnvironment.getChildModelRelationFile(node.id, false);

		computeTaskSum(file);
	}

	public void computeTaskSum(File file) throws Exception {
		if (file == null)
			return;

		AtomicInteger counter = new AtomicInteger(0);
		EditorEnvironment.traverseModel(file, new ITraverseDrawNode() {

			@Override
			public boolean onNode(File file, String title, DrawNode node, Object param) {
				if (node.getClass().getName().equals(WorkflowNode.class.getName())) {
					counter.incrementAndGet();
				}
				return true;
			}
		}, null);

		EditorEnvironment.showMessage("任务数量：" + String.valueOf(counter.get()) + "!");
	}

	public void checkUI(CheckType ct) {
		try {
			if (!EditorEnvironment.isOpenProject()) {
				EditorEnvironment.showMessage(null, "请先打开一个项目！");
				return;
			}
			if (EditorEnvironment.checkExistsUINodeName(new ICheckCallBack() {

				@Override
				public boolean onRepeat(String title, File file, DrawNode node, File repeatFile, DrawNode repeatNode,
						Object param) {
					if (file.getName().compareTo(repeatFile.getName()) == 0) {
						return openUIBuilder(file, new DrawNode[] { node, repeatNode });
					} else {
						if (openUIBuilder(file, node))
							return openUIBuilder(repeatFile, repeatNode);
					}
					return false;
				}
			}, ct))
				EditorEnvironment.showMessage("未发现重复项目！");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	private void openProject(File f) {
		if (!f.isDirectory()) {
			EditorEnvironment.showMessage("项目必须是目录形式！");
			return;
		}

		String projectName = FileHelp.removeExt(f.getName());
		EditorEnvironment.setProjectBasePath(f.getParentFile());
		openProject(projectName);
		setTitle(projectName);
		desktopPane.repaint();
	}

	public void publish(boolean needResource, boolean needFrame, boolean needUserJS, boolean needReport,
			boolean needMenu, boolean needNav, boolean needDataSource, boolean needRemoteAuth) {
		if (topFrame == null) {
			EditorEnvironment.showMessage(null, "请先打开设计器！");
			return;
		}

		for (JInternalFrame frame : desktopPane.getAllFrames()) {
			if (frame instanceof ChildForm) {
				ChildForm form = (ChildForm) frame;
				if (form.isEdit) {
					if (EditorEnvironment.showConfirmDialog("有未保存的设计，如果继续发布将全部保存，是否继续发布？", "发布",
							JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
						return;
					}

					saveAll();
				}
			}
		}

		WaitDialog.Show("发布", "发布中，请等待。。。", new IProcess() {
			@Override
			public boolean doProc(WaitDialog waitDialog) {
				return EditorEnvironment.publish(new IPublish() {

					@Override
					public void publishContents() throws Exception {
						ModelflowBuilder.publish(MainForm.this, new HashMap<>());
					}
				}, needResource, needFrame, needUserJS, needReport, needMenu, needNav, needDataSource, needRemoteAuth);
			}

			@Override
			public void closed(boolean isok) {
				if (isok)
					EditorEnvironment.showMessage(null, "恭喜，发布成功完成！", "发布成功", JOptionPane.INFORMATION_MESSAGE);
			}
		}, null);

	}

	protected Image getImage(String resourcePath) {
		try {
			URL url = MainForm.class.getResource(resourcePath);
			if (url == null) {
				EditorEnvironment.showMessage("url is null");
			}

			return Toolkit.getDefaultToolkit().getImage(url);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	protected Icon getIcon(String resourcePath) {
		Image image = getImage(resourcePath);
		return new ImageIcon(image);
	}

	protected void switchValidateForm(boolean b) {
		WaitDialog.Show("批量设置", "正在批量设置属性，请稍后。。。", new IProcess() {

			@Override
			public boolean doProc(WaitDialog waitDialog) {
				try {
					return EditorEnvironment.traverseUI(new EditorEnvironment.ITraverseUIFile() {

						@Override
						public boolean callback(File uiFile, UICanvas canvas, Object userObject) {
							boolean needSave = false;
							for (DrawNode node : canvas.getNodes()) {
								if (node instanceof UINode) {
									DrawInfo info = ((UINode) node).getDrawInfo();
									if (info instanceof ClickableInfo) {
										((ClickableInfo) info).validateForm = b;
										needSave = true;
									}
								}
							}

							if (needSave)
								try {
									canvas.setFile(uiFile);
									canvas.save();
								} catch (Exception e) {
									e.printStackTrace();
									EditorEnvironment.showException(e);
									return false;
								}
							return true;
						}
					}, null);

				} catch (Exception e) {
					e.printStackTrace();
					EditorEnvironment.showException(e);
					return false;
				}
			}

			@Override
			public void closed(boolean isok) {
				if (isok)
					EditorEnvironment.showMessage("成功完成！");

			}
		}, null);
	}

	private void initialize() {

		mainFrame = new JFrame();
		mainFrame.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setTitle(null);
		mainFrame.setIconImage(Toolkit.getDefaultToolkit().getImage(MainForm.class.getResource("/image/browser.png")));

		menuBar = new JMenuBar();
		menuBar.setBackground(Color.WHITE);
		menuBar.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		mainFrame.setJMenuBar(menuBar);

		JMenu mnNewMenu = new JMenu(" 文件 ");
		mnNewMenu.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		menuBar.add(mnNewMenu);

		JMenuItem mntmNewMenuItem = new JMenuItem("打开                                                    ");
		mntmNewMenuItem.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		mntmNewMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (topFrame == null) {
					EditorEnvironment.setProjectBasePath();
					List<String> projects = EditorEnvironment.getProjectNames();
					String projectName = (String) ListSelector.show("选择项目", projects);
					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {
							openProject(projectName);
							setTitle(projectName);
							desktopPane.repaint();
						}
					});
					return;
				}

				if (topFrame != null && topFrame instanceof IMainMenuOperation) {
					IMainMenuOperation mainMenuOperation = (IMainMenuOperation) topFrame;
					mainMenuOperation.onLoad();
					desktopPane.repaint();
				}
			}
		});

		JMenuItem mntmNewMenuItem_7 = new JMenuItem("新建");
		mntmNewMenuItem_7.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		mntmNewMenuItem_7.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.ALT_MASK));
		mntmNewMenuItem_7.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (topFrame != null) {
					EditorEnvironment.showMessage(null, "请先关闭当前项目！");
					return;
				}

				String name = EditorEnvironment.showInputDialog("请输入要新建得项目名称！", "project1");
				if (name == null || name.isEmpty())
					return;

				boolean overwrite = false;
				if (EditorEnvironment.existsProjectName(name)) {
					if (EditorEnvironment.showConfirmDialog("项目已经存在，是否覆盖？", "新建项目",
							JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
						return;
					} else
						overwrite = true;
				}

				WaitDialog.Show("新建项目", "建立中，请等待。。。", new IProcess() {

					@Override
					public boolean doProc(WaitDialog waitDialog) {
						try {
							if (!EditorEnvironment.newProject(name, (boolean) waitDialog.params))
								return false;
							else {
								openProject(name);
							}
							return true;
						} catch (Exception e1) {
							e1.printStackTrace();
						}
						return false;
					}

					@Override
					public void closed(boolean isok) {
						if (!isok)
							EditorEnvironment.showMessage(null, "项目建立失败！", "新建项目", JOptionPane.ERROR_MESSAGE);
						else
							EditorEnvironment.showMessage(null, "项目建立成功！", "新建项目", JOptionPane.INFORMATION_MESSAGE);
					}
				}, overwrite);
			}
		});
		mnNewMenu.add(mntmNewMenuItem_7);

		JSeparator separator = new JSeparator();
		mnNewMenu.add(separator);
		mntmNewMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.ALT_MASK));
		mnNewMenu.add(mntmNewMenuItem);

		JMenuItem mntmNewMenuItem_1 = new JMenuItem("保存");
		mntmNewMenuItem_1.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		mntmNewMenuItem_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!EditorEnvironment.isOpenProject()) {
					EditorEnvironment.showMessage(null, "请先打开一个项目！");
					return;
				}
				saveWindow(null);
			}
		});

		JMenuItem menuItem_21 = new JMenuItem("打开远程项目");
		menuItem_21.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		menuItem_21.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.ALT_MASK));
		menuItem_21.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File dir = Tools.selectOpenDir(null, "打开远程项目", "请选择目录", null);
				if (dir == null)
					return;

				FileCache.addNetDriver(dir);
				openProject(dir);
			}
		});
		mnNewMenu.add(menuItem_21);

		JSeparator separator_37 = new JSeparator();
		mnNewMenu.add(separator_37);

		JMenuItem menuItem_5 = new JMenuItem("导入分发项目");
		mnNewMenu.add(menuItem_5);
		menuItem_5.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		menuItem_5.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK));
		menuItem_5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				List<String> projects = EditorEnvironment.getDispatchNames();
				String projectName = (String) ListSelector.show("选择导入项目", projects);
				if (projectName == null || projectName.isEmpty())
					return;
				
				if (projectName.equalsIgnoreCase(EditorEnvironment.getCurrentProjectName())){
					EditorEnvironment.showMessage("导入项目名称不能与当前项目相同！");
					return;
				}
				
				EditorEnvironment.importDispatchProject(projectName, new IDispatchCallback() {
					
					@Override
					public void ondo(String newProjectName) {
//						openProject(newProjectName);
					}
				});
			}
		});

		JSeparator separator_11 = new JSeparator();
		mnNewMenu.add(separator_11);
		mntmNewMenuItem_1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.ALT_MASK));
		mnNewMenu.add(mntmNewMenuItem_1);

		saveMenuItem = new JMenuItem("全部保存");
		saveMenuItem.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		saveMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!EditorEnvironment.isOpenProject()) {
					EditorEnvironment.showMessage(null, "请先打开一个项目！");
					return;
				}

				saveAll();
			}
		});
		mnNewMenu.add(saveMenuItem);

		closeMenuItem = new JMenuItem("关闭");
		closeMenuItem.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		closeMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.ALT_MASK));
		closeMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (!EditorEnvironment.isOpenProject()) {
					EditorEnvironment.showMessage(null, "请先打开一个项目！");
					return;
				}

				topFrame = closeWindow(topFrame);
				if (topFrame != null) {
					toolBarButtons.add(topFrame, null, true);
				}
			}
		});

		JSeparator separator_6 = new JSeparator();
		mnNewMenu.add(separator_6);
		mnNewMenu.add(closeMenuItem);

		JMenuItem menuItem_2 = new JMenuItem("全部关闭");
		menuItem_2.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		menuItem_2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.ALT_MASK | InputEvent.SHIFT_MASK));
		menuItem_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (!EditorEnvironment.isOpenProject()) {
					EditorEnvironment.showMessage(null, "请先打开一个项目！");
					return;
				}

				close();
			}
		});

		JMenuItem menuItem_3 = new JMenuItem("关闭除主窗口外所有窗口");
		menuItem_3.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		menuItem_3.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
		menuItem_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!EditorEnvironment.isOpenProject()) {
					EditorEnvironment.showMessage(null, "请先打开一个项目！");
					return;
				}

				close(false);
			}
		});
		mnNewMenu.add(menuItem_3);
		mnNewMenu.add(menuItem_2);

		JSeparator mntmNewMenuItem_2 = new JSeparator(SwingConstants.HORIZONTAL);
		mnNewMenu.add(mntmNewMenuItem_2);

		JMenuItem mntmNewMenuItem_3 = new JMenuItem("退出");
		mntmNewMenuItem_3.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		mntmNewMenuItem_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mainFrame.dispatchEvent(new WindowEvent((Window) mainFrame, WindowEvent.WINDOW_CLOSING));
			}
		});
		mntmNewMenuItem_3.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.ALT_MASK));
		mnNewMenu.add(mntmNewMenuItem_3);

		JMenu menu_8 = new JMenu(" 编辑 ");
		menu_8.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		menuBar.add(menu_8);

		JMenuItem menuItem_1 = new JMenuItem("页头编辑             ");
		menuItem_1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
		menu_8.add(menuItem_1);
		menuItem_1.setFont(new Font("微软雅黑", Font.PLAIN, 13));

		JMenuItem menuItem_7 = new JMenuItem("页脚编辑");
		menuItem_7.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
		menu_8.add(menuItem_7);
		menuItem_7.setFont(new Font("微软雅黑", Font.PLAIN, 13));

		JMenuItem menuItem_19 = new JMenuItem("左侧编辑");
		menuItem_19.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
		menu_8.add(menuItem_19);
		menuItem_19.setFont(new Font("微软雅黑", Font.PLAIN, 13));

		JMenuItem menuItem_20 = new JMenuItem("右侧编辑");
		menuItem_20.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
		menu_8.add(menuItem_20);
		menuItem_20.setFont(new Font("微软雅黑", Font.PLAIN, 13));

		JSeparator separator_15 = new JSeparator();
		menu_8.add(separator_15);

		JMenuItem menuItem_36 = new JMenuItem("区域编辑");
		menuItem_36.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
		menuItem_36.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!EditorEnvironment.isOpenProject()) {
					EditorEnvironment.showMessage(null, "请先打开一个项目！");
					return;
				}

				openFrameNodeEditor();
			}
		});
		menuItem_36.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		menu_8.add(menuItem_36);

		JSeparator separator_5 = new JSeparator();
		menu_8.add(separator_5);

		JMenu menu_7 = new JMenu("名称查重");
		menu_8.add(menu_7);
		menu_7.setFont(new Font("微软雅黑", Font.PLAIN, 13));

		JMenuItem mntmid_1 = new JMenuItem("模块关系图ID查重");
		mntmid_1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
		mntmid_1.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		menu_7.add(mntmid_1);

		JMenuItem mntmUi_1 = new JMenuItem("UI ID查重");
		mntmUi_1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
		mntmUi_1.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		menu_7.add(mntmUi_1);

		JMenuItem mntmUi = new JMenuItem("UI名称查重");
		mntmUi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
		mntmUi.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		menu_7.add(mntmUi);

		JMenuItem mntmNewMenuItem_4 = new JMenuItem("控件查重");
		mntmNewMenuItem_4
				.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
		mntmNewMenuItem_4.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		mntmNewMenuItem_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!EditorEnvironment.isOpenProject()) {
					EditorEnvironment.showMessage(null, "请先打开一个项目！");
					return;
				}

				if (topFrame instanceof UIBuilder) {
					UIBuilder builder = (UIBuilder) topFrame;
					HashMap<String, UINode> ids = new HashMap<>();
					HashMap<String, UINode> names = new HashMap<>();
					for (DrawNode drawNode : builder.canvas.getNodes()) {
						UINode node = (UINode) drawNode;
						if (ids.containsKey(node.getDrawInfo().id)) {
							EditorEnvironment.showMessage("控件[" + node.id + "]的id重复！");
							builder.canvas.setSelected(node);
							return;
						}

						if (names.containsKey(node.getDrawInfo().name)) {
							EditorEnvironment.showMessage("控件[" + node.id + "]的name重复！");
							builder.canvas.setSelected(node);
							return;
						}

						ids.put(node.getDrawInfo().id, node);
						names.put(node.getDrawInfo().name, node);
					}
					EditorEnvironment.showMessage(null, "当前界面未发现重复的控件命名！", "恭喜");
				} else
					EditorEnvironment.showMessage("请先打开/选择一个界面设计器！");
			}
		});
		menu_7.add(mntmNewMenuItem_4);
		mntmUi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!EditorEnvironment.isOpenProject()) {
					EditorEnvironment.showMessage(null, "请先打开一个项目！");
					return;
				}

				checkUI(CheckType.ctName);
			}
		});
		mntmUi_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!EditorEnvironment.isOpenProject()) {
					EditorEnvironment.showMessage(null, "请先打开一个项目！");
					return;
				}

				checkUI(CheckType.ctID);
			}
		});
		mntmid_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (!EditorEnvironment.isOpenProject()) {
						EditorEnvironment.showMessage(null, "请先打开一个项目！");
						return;
					}
					if (EditorEnvironment.checkExistsModelNodeName(new ICheckCallBack() {

						@Override
						public boolean onRepeat(String title, File file, DrawNode node, File repeatFile,
								DrawNode repeatNode, Object param) {
							if (file.getName().compareTo(repeatFile.getName()) == 0) {
								return openExistsModelflowRelation(file, new DrawNode[] { node, repeatNode });
							} else {
								if (openExistsModelflowRelation(file, node))
									return openExistsModelflowRelation(repeatFile, repeatNode);
							}
							return false;
						}
					})) {
						EditorEnvironment.showMessage("未发现重复项目！");
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		menuItem_20.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!EditorEnvironment.isOpenProject()) {
					EditorEnvironment.showMessage(null, "请先打开一个项目！");
					return;
				}

				try {
					openUIBuilder(EditorEnvironment.getFrameModelNode(RegionName.rnRight),
							((JMenuItem) e.getSource()).getText(), new HashMap<>());
				} catch (Exception e1) {
					e1.printStackTrace();
					EditorEnvironment.showException(e1);
				}
			}
		});
		menuItem_19.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!EditorEnvironment.isOpenProject()) {
					EditorEnvironment.showMessage(null, "请先打开一个项目！");
					return;
				}

				try {
					openUIBuilder(EditorEnvironment.getFrameModelNode(RegionName.rnLeft),
							((JMenuItem) e.getSource()).getText(), new HashMap<>());
				} catch (Exception e1) {
					e1.printStackTrace();
					EditorEnvironment.showException(e1);
				}
			}
		});
		menuItem_7.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!EditorEnvironment.isOpenProject()) {
					EditorEnvironment.showMessage(null, "请先打开一个项目！");
					return;
				}

				try {
					openUIBuilder(EditorEnvironment.getFrameModelNode(RegionName.rnBottom),
							((JMenuItem) e.getSource()).getText(), new HashMap<>());
				} catch (Exception e1) {
					e1.printStackTrace();
					EditorEnvironment.showException(e1);
				}
			}
		});
		menuItem_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!EditorEnvironment.isOpenProject()) {
					EditorEnvironment.showMessage(null, "请先打开一个项目！");
					return;
				}
				try {
					openUIBuilder(EditorEnvironment.getFrameModelNode(RegionName.rnTop),
							((JMenuItem) e.getSource()).getText(), new HashMap<>());
				} catch (Exception e1) {
					e1.printStackTrace();
					EditorEnvironment.showException(e1);
				}
			}
		});

		JMenu mnNewMenu_2 = new JMenu(" 项目 ");
		mnNewMenu_2.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		menuBar.add(mnNewMenu_2);

		JMenuItem mntmweb_1 = new JMenuItem("设置Web站点根目录");
		mntmweb_1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.ALT_MASK));
		mntmweb_1.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		mntmweb_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (!EditorEnvironment.isOpenProject()) {
						EditorEnvironment.showMessage(null, "请先打开一个项目！");
						return;
					}

					String initPath = EditorEnvironment.getWebRoot();

					File path = Tools.selectOpenDir(null, "请选择目录", "设置Web站点根目录",
							initPath == null || initPath.isEmpty() ? null : initPath);
					if (path == null) {
						return;
					}

					EditorEnvironment.setWebRoot(path);
				} catch (Exception e1) {
					e1.printStackTrace();
					EditorEnvironment.showException(e1);
				}

			}
		});

		mnNewMenu_2.add(mntmweb_1);

		JMenuItem menuItem_54 = new JMenuItem("设置数据服务地址");
		menuItem_54.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (!EditorEnvironment.isOpenProject()) {
						EditorEnvironment.showMessage(null, "请先打开一个项目！");
						return;
					}

					String initValue = EditorEnvironment.getDataServiceRoot();

					String uri = EditorEnvironment.showInputDialog("请输入数据服务uri", initValue);
					if (uri == null || uri.isEmpty()) {
						return;
					}

					EditorEnvironment.setDataServiceRoot(uri);
				} catch (Exception e1) {
					e1.printStackTrace();
					EditorEnvironment.showException(e1);
				}

			}
		});
		mnNewMenu_2.add(menuItem_54);

		JMenu menu_21 = new JMenu("设置远程授权");
		mnNewMenu_2.add(menu_21);

		JMenuItem mntmkey = new JMenuItem("设置授权Key");
		mntmkey.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!EditorEnvironment.isOpenProject()) {
					EditorEnvironment.showMessage(null, "请先打开一个项目！");
					return;
				}

				File file = EditorEnvironment.getProjectFile(EditorEnvironment.Remote_Dir_Name,
						EditorEnvironment.Remote_Auth_FileName);
				JSONObject datas = new JSONObject();
				if (!file.exists()) {
					if (!file.getParentFile().exists())
						if (!file.getParentFile().mkdirs()) {
							EditorEnvironment.showMessage("建立目录：" + file.getParent() + "失败！");
							return;
						}
				} else {
					try {
						datas = (JSONObject) JsonHelp.parseJson(file, null);
					} catch (Exception e1) {
						e1.printStackTrace();
						EditorEnvironment.showException(e1);
						return;
					}
				}

				Object[][] rows = null;
				if (datas.length() > 0) {
					JSONArray names = datas.names();
					rows = new Object[names.length()][2];
					for (int i = 0; i < names.length(); i++) {
						rows[i][0] = names.getString(i);
						rows[i][1] = datas.getString((String) rows[i][0]);
					}
				}
				ModelResult result = KeyValueSelector.show(null, MainForm.this, new ICheckValue() {

					@Override
					public boolean onCheck(Object[][] originalData, JTable table) {
						for (int i = 0; i < table.getRowCount(); i++) {
							String key = (String) table.getValueAt(i, 0);
							if (key == null || key.isEmpty()) {
								EditorEnvironment.showMessage("必须录入授权Key！");
								return false;
							}
							table.setValueAt("auth", i, 1);
						}
						;

						return true;
					}
				}, new IEditRow() {

					@SuppressWarnings({ "rawtypes", "unchecked" })
					@Override
					public void updateRow(JTable table, Vector<?> rowdata) {
						Vector row = rowdata;
						if (row.size() > 1)
							row.set(1, "auth");
						else
							row.add("auth");
					}

					@Override
					public boolean deleteRow(JTable table, Vector<?> row) {
						// TODO Auto-generated method stub
						return false;
					}

					@Override
					public Object[] addRow(JTable table) {
						return new Object[] { "", "auth" };
					}
				}, rows, new Object[] { "授权key", "授权类型" }, null, new int[] { 0 }, false);

				TableModel model = result.isok ? result.model : null;

				if (model == null)
					return;

				datas = new JSONObject();
				for (int i = 0; i < model.getRowCount(); i++) {
					datas.put((String) model.getValueAt(i, 0), model.getValueAt(i, 1));
				}

				try {
					JsonHelp.saveJson(file, datas, null);
				} catch (Exception e1) {
					e1.printStackTrace();
					EditorEnvironment.showException(e1);
				}
			}
		});
		menu_21.add(mntmkey);

		JMenu menu_17 = new JMenu("批量设置属性");
		mnNewMenu_2.add(menu_17);

		JMenuItem menuItem_51 = new JMenuItem("启用窗体校验");
		menuItem_51.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!EditorEnvironment.isOpenProject()) {
					EditorEnvironment.showMessage(null, "请先打开一个项目！");
					return;
				}

				switchValidateForm(true);
			}
		});
		menu_17.add(menuItem_51);

		JMenuItem menuItem_52 = new JMenuItem("停用窗体校验");
		menuItem_52.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!EditorEnvironment.isOpenProject()) {
					EditorEnvironment.showMessage(null, "请先打开一个项目！");
					return;
				}

				switchValidateForm(false);
			}
		});
		menu_17.add(menuItem_52);

		JSeparator separator_4 = new JSeparator();
		mnNewMenu_2.add(separator_4);

		JMenu menu_13 = new JMenu("脚本");
		menu_13.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		mnNewMenu_2.add(menu_13);

		JMenuItem menuItem_30 = new JMenuItem("建立用户脚本");
		menuItem_30.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!EditorEnvironment.isOpenProject()) {
					EditorEnvironment.showMessage(null, "请先打开一个项目！");
					return;
				}

				if (topFrame != null && topFrame instanceof ModelflowBuilder) {
					File file = null;
					ModelflowBuilder modelflowForm = (ModelflowBuilder) topFrame;
					if (modelflowForm.canvas.getSelected() == null) {
						EditorEnvironment.showMessage("请先选择一个模块！");
					} else {
						for (DrawNode drawNode : modelflowForm.canvas.getSelecteds()) {
							WorkflowNode node = (WorkflowNode) drawNode;
							file = EditorEnvironment.getProjectJSPath(node.name);
							if (file.exists())
								EditorEnvironment.showMessage("文件【" + file.getAbsolutePath() + "】已经存在！");
							else {
								JSDefaultFileNode js = new JSDefaultFileNode(null);
								js.name = node.title;
								js.command = node.name;
								js.memo = node.memo;
								try {
									if (!js.createFile()) {
										throw new Exception("文件【" + js.getFile().getAbsolutePath() + "】建立失败！");
									}
								} catch (Exception e1) {
									e1.printStackTrace();
									EditorEnvironment.showException(e1);
									return;
								}
							}
						}
						try {
							EditorEnvironment.showMessage("文件建立成功，关闭后打开用户脚本目录！");

							if (file != null)
								Runtime.getRuntime().exec("rundll32 SHELL32.DLL,ShellExec_RunDLL "
										+ "Explorer.exe /select," + file.getAbsolutePath());
							else
								Desktop.getDesktop().open(EditorEnvironment.getProjectJSPath());
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				} else
					EditorEnvironment.showMessage("请先打开一个模块编辑器！");
			}
		});

		JMenuItem menuItem_31 = new JMenuItem("打开脚本目录");
		menuItem_31.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!EditorEnvironment.isOpenProject()) {
					EditorEnvironment.showMessage(null, "请先打开一个项目！");
					return;
				}

				try {
					Desktop.getDesktop().open(EditorEnvironment.getProjectJSPath());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});

		JMenuItem menuItem_33 = new JMenuItem("打开图片目录");
		menuItem_33.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!EditorEnvironment.isOpenProject()) {
					EditorEnvironment.showMessage(null, "请先打开一个项目！");
					return;
				}

				try {
					Desktop.getDesktop().open(EditorEnvironment.getProjectImagePath());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		menuItem_33.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		menu_13.add(menuItem_33);

		JMenuItem menuItem_32 = new JMenuItem("打开报表模板目录");
		menuItem_32.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!EditorEnvironment.isOpenProject()) {
					EditorEnvironment.showMessage(null, "请先打开一个项目！");
					return;
				}

				try {
					Desktop.getDesktop().open(EditorEnvironment.getProjectReportPath());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		menuItem_32.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		menu_13.add(menuItem_32);
		menuItem_31.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		menu_13.add(menuItem_31);

		JSeparator separator_16 = new JSeparator();
		menu_13.add(separator_16);

		JMenuItem menuItem_46 = new JMenuItem("打开发布目录");
		menuItem_46.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!EditorEnvironment.isOpenProject()) {
					EditorEnvironment.showMessage(null, "请先打开一个项目！");
					return;
				}

				try {
					Desktop.getDesktop().open(EditorEnvironment.getPublishWebPath());
				} catch (Exception e1) {
					e1.printStackTrace();
					EditorEnvironment.showException(e1);
				}
			}
		});
		menu_13.add(menuItem_46);

		JMenuItem menuItem_47 = new JMenuItem("打开发布脚本目录");
		menuItem_47.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!EditorEnvironment.isOpenProject()) {
					EditorEnvironment.showMessage(null, "请先打开一个项目！");
					return;
				}

				try {
					Desktop.getDesktop()
							.open(EditorEnvironment.getPublishWebFile(EditorEnvironment.Client_Dir_Path, null));
				} catch (Exception e1) {
					e1.printStackTrace();
					EditorEnvironment.showException(e1);
				}
			}
		});
		menu_13.add(menuItem_47);

		JMenuItem mntmcss = new JMenuItem("打开发布CSS目录");
		mntmcss.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!EditorEnvironment.isOpenProject()) {
					EditorEnvironment.showMessage(null, "请先打开一个项目！");
					return;
				}

				try {
					Desktop.getDesktop()
							.open(EditorEnvironment.getPublishWebFile(EditorEnvironment.CSS_Dir_Name, null));
				} catch (Exception e1) {
					e1.printStackTrace();
					EditorEnvironment.showException(e1);
				}
			}
		});
		menu_13.add(mntmcss);

		JMenuItem menuItem_48 = new JMenuItem("打开发布图像目录");
		menuItem_48.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!EditorEnvironment.isOpenProject()) {
					EditorEnvironment.showMessage(null, "请先打开一个项目！");
					return;
				}

				try {
					Desktop.getDesktop()
							.open(EditorEnvironment.getPublishWebFile(EditorEnvironment.Image_Resource_Path, null));
				} catch (Exception e1) {
					e1.printStackTrace();
					EditorEnvironment.showException(e1);
				}
			}
		});
		menu_13.add(menuItem_48);

		JSeparator separator_14 = new JSeparator();
		menu_13.add(separator_14);
		menuItem_30.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		menu_13.add(menuItem_30);

		JSeparator separator_17 = new JSeparator();
		mnNewMenu_2.add(separator_17);

		JMenu menu_18 = new JMenu("项目分发");
		menu_18.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		mnNewMenu_2.add(menu_18);

		newProjectChecker = new JCheckBoxMenuItem("创建新项目");
		newProjectChecker.setSelected(true);
		menu_18.add(newProjectChecker);

		linkedChecker = new JCheckBoxMenuItem("使用链接方式拷贝节点");
		menu_18.add(linkedChecker);

		includeImage = new JCheckBoxMenuItem("包含图片资源");
		includeImage.setSelected(true);
		menu_18.add(includeImage);

		includeReport = new JCheckBoxMenuItem("包含报表资源");
		includeReport.setSelected(true);
		menu_18.add(includeReport);

		includeAuth = new JCheckBoxMenuItem("包含远程授权信息");
		includeAuth.setSelected(true);
		menu_18.add(includeAuth);

		includeConfig = new JCheckBoxMenuItem("包含配置信息");
		includeConfig.setSelected(true);
		menu_18.add(includeConfig);

		includeDataSource = new JCheckBoxMenuItem("包含数据源定义");
		includeDataSource.setSelected(true);
		menu_18.add(includeDataSource);

		includeUserJs = new JCheckBoxMenuItem("包含用户脚本");
		includeUserJs.setSelected(true);
		menu_18.add(includeUserJs);

		includeMasterData = new JCheckBoxMenuItem("包含主数据信息");
		includeMasterData.setSelected(true);
		menu_18.add(includeMasterData);
		
		onlySelectedModel = new JCheckBoxMenuItem("仅分发选中模块");
		menu_18.add(onlySelectedModel);

		JSeparator separator_36 = new JSeparator();
		menu_18.add(separator_36);

		JMenuItem menuItem_4 = new JMenuItem("分发项目");
		menu_18.add(menuItem_4);
		menuItem_4.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		menuItem_4.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK));
		menuItem_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!EditorEnvironment.isOpenProject()) {
					EditorEnvironment.showMessage(null, "请先打开一个项目！");
					return;
				}

				if (!(topFrame instanceof ModelflowBuilder)) {
					EditorEnvironment.showMessage(null, "请先打开一个模块关系图设计界面并保持为当前设计器！");
					return;
				}

				final ModelflowBuilder workflowForm = (ModelflowBuilder) topFrame;

				if (workflowForm.workflowName == null || workflowForm.workflowName.isEmpty()) {
					EditorEnvironment.showMessage(null, "请先保存后再试！");
					return;
				}

				if (workflowForm.frameModelNodes != null && workflowForm.frameModelNodes.size() > 0) {
					EditorEnvironment.showMessage("当前模块图不能分发！");
					return;
				}

				String defaultValue = EditorEnvironment.getCurrentProjectName();
				NodeDescInfo nodeInfo;
				try {
					nodeInfo = EditorEnvironment.getChildModelNodeDescInfo(workflowForm.workflowName);
					if (nodeInfo != null)
						defaultValue = nodeInfo.title;
				} catch (Exception e2) {
					e2.printStackTrace();
				}

				String name = EditorEnvironment.showInputDialog("请输入分发项目的保存名称：", defaultValue);
				if (name == null || name.isEmpty())
					return;

				WaitDialog.Show("分发项目", "正在分发，请等待。。。", new IProcess() {

					@Override
					public boolean doProc(WaitDialog waitDialog) {
						try {
							workflowForm.dispatchWorkflows(name, linkedChecker.isSelected(),
									newProjectChecker.isSelected(), includeImage.isSelected(),
									includeReport.isSelected(), includeAuth.isSelected(), includeConfig.isSelected(),
									includeDataSource.isSelected(), includeUserJs.isSelected(),
									includeMasterData.isSelected(),
									onlySelectedModel.isSelected());
							return true;
						} catch (Exception e1) {
							e1.printStackTrace();
							EditorEnvironment.showException(e1);
						}
						return false;
					}

					@Override
					public void closed(boolean isok) {
						if (isok) {
							EditorEnvironment.showMessage(null, "分发项目成功！", "分发项目", JOptionPane.INFORMATION_MESSAGE);
						} else {

						}
					}
				}, null);
			}
		});

		JSeparator separator_7 = new JSeparator();
		menu_18.add(separator_7);

		JMenuItem menuItem_17 = new JMenuItem("合并项目");
		menu_18.add(menuItem_17);
		menuItem_17.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		menuItem_17.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!EditorEnvironment.isOpenProject()) {
					EditorEnvironment.showMessage(null, "请先打开一个项目！");
					return;
				}

				if (!(topFrame instanceof ModelflowBuilder)){
					EditorEnvironment.showMessage(null, "请先打开一个模块编辑器！");
					return;
				}
				
				List<String> projects = EditorEnvironment.getProjectNames();
				projects.remove(EditorEnvironment.getCurrentProjectName());
				String projectName = (String) ListSelector.show("选择项目", projects);
				if (projectName == null || projectName.isEmpty())
					return;

				saveAll();
				
				close(false);
				
				ModelflowBuilder builder = (ModelflowBuilder)topFrame;
				WorkflowCanvas canvas = builder.canvas;
				EditorEnvironment.mergeProject(canvas, projectName, MainForm.this);
				
				builder.isEdit = true;
			}
		});

		JSeparator separator_30 = new JSeparator();
		mnNewMenu_2.add(separator_30);

		JMenu mnNewMenu_1 = new JMenu("发布系统");
		mnNewMenu_1.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		mnNewMenu_2.add(mnNewMenu_1);

		needResource = new JCheckBoxMenuItem("包括资源文件");
		needResource.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		mnNewMenu_1.add(needResource);
		needResource.setSelected(true);

		needReport = new JCheckBoxMenuItem("包括报表文件");
		needReport.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		needReport.setSelected(true);
		mnNewMenu_1.add(needReport);

		needUserJS = new JCheckBoxMenuItem("包括用户脚本");
		needUserJS.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		mnNewMenu_1.add(needUserJS);

		needFrame = new JCheckBoxMenuItem("包括Frame");
		needFrame.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		mnNewMenu_1.add(needFrame);

		needMenu = new JCheckBoxMenuItem("包括主菜单文件");
		needMenu.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		needMenu.setSelected(true);
		mnNewMenu_1.add(needMenu);

		needNav = new JCheckBoxMenuItem("包括主导航树文件");
		needNav.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		needNav.setSelected(true);
		mnNewMenu_1.add(needNav);

		needDataSource = new JCheckBoxMenuItem("包括数据源文件");
		needDataSource.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		needDataSource.setSelected(true);
		mnNewMenu_1.add(needDataSource);

		needRemoteAuth = new JCheckBoxMenuItem("包括远程授权");
		needRemoteAuth.setSelected(true);
		mnNewMenu_1.add(needRemoteAuth);

		androidMode = new JMenu("android模式");
		mnNewMenu_1.add(androidMode);

		noandroidmode = new JCheckBoxMenuItem("NO ANDROID");
		noandroidmode.setSelected(true);
		androidMode.add(noandroidmode);

		ldpimodel = new JCheckBoxMenuItem("LDPI（120dpi）");
		androidMode.add(ldpimodel);

		mdpimode = new JCheckBoxMenuItem("MDPI（160dpi）");
		androidMode.add(mdpimode);

		tvdpimode = new JCheckBoxMenuItem("TVDPI（213dpi）");
		androidMode.add(tvdpimode);

		hdpimode = new JCheckBoxMenuItem("HDPI（240dpi）");
		androidMode.add(hdpimode);

		xhdpimode = new JCheckBoxMenuItem("XHDPI（320dpi）");
		androidMode.add(xhdpimode);

		xxhdpimode = new JCheckBoxMenuItem("XXHDPI（480dpi）");
		androidMode.add(xxhdpimode);

		xxxhdpimode = new JCheckBoxMenuItem("XXXHDPI（560dpi）");
		androidMode.add(xxxhdpimode);

		ButtonGroup dpiGroup = new ButtonGroup();
		dpiGroup.add(noandroidmode);
		dpiGroup.add(ldpimodel);
		dpiGroup.add(hdpimode);
		dpiGroup.add(xhdpimode);
		dpiGroup.add(mdpimode);
		dpiGroup.add(tvdpimode);
		dpiGroup.add(xxhdpimode);
		dpiGroup.add(xxxhdpimode);

		JSeparator separator_18 = new JSeparator();
		mnNewMenu_1.add(separator_18);

		JMenuItem menuItem = new JMenuItem("发布");
		menuItem.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		mnNewMenu_1.add(menuItem);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.ALT_MASK));

		JMenuItem menuItem_25 = new JMenuItem("重新发布");
		menuItem_25.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		menuItem_25.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.ALT_MASK));
		menuItem_25.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!EditorEnvironment.isOpenProject()) {
					EditorEnvironment.showMessage(null, "请先打开一个项目！");
					return;
				}

				if (EditorEnvironment.showConfirmDialog("重新发布会删除已发布的所有文件，是否继续？", "发布",
						JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
					return;
				}

				publish(true, true, true, true, true, true, true, true);
			}
		});
		mnNewMenu_1.add(menuItem_25);
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!EditorEnvironment.isOpenProject()) {
					EditorEnvironment.showMessage(null, "请先打开一个项目！");
					return;
				}

				publish(needResource.isSelected(), needFrame.isSelected(), needUserJS.isSelected(),
						needReport.isSelected(), needMenu.isSelected(), needNav.isSelected(),
						needDataSource.isSelected(), needRemoteAuth.isSelected());
			}
		});

		JMenuItem menuItem_38 = new JMenuItem("发布选定");
		menuItem_38.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
		menuItem_38.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (topFrame == null) {
					EditorEnvironment.showMessage(null, "请先打开设计器！");
					return;
				}

				if (!(topFrame instanceof UIBuilder || topFrame instanceof ModelflowBuilder)) {
					EditorEnvironment.showMessage(null, "请先打开界面或模块设计器！");
					return;
				}

				final List<DrawNode> publishNodes = new ArrayList<>();
				final List<DrawNode> nodes = new ArrayList<>();
				if (topFrame instanceof ModelflowBuilder) {
					ModelflowBuilder builder = (ModelflowBuilder) topFrame;
					if (builder.canvas.getSelected() == null) {
						EditorEnvironment.showMessage(null, "请先选择一个模块！");
						return;
					}
					publishNodes.addAll(builder.canvas.getSelecteds());
					try {
						nodes.addAll(DrawCanvas.loadNodes(null, builder.canvas.getFile(),
								new EditorEnvironment.WorkflowDeserializable(), null, null, null));
					} catch (Exception e1) {
						e1.printStackTrace();
						EditorEnvironment.showException(e1);
						return;
					}
				} else {
					UIBuilder uiBuilder = (UIBuilder) topFrame;
					publishNodes.add(uiBuilder.workflowNode);
					nodes.addAll(uiBuilder.workflowNodes.values());
				}

				ChildForm form = (ChildForm) topFrame;

				if (nodes == null || nodes.size() == 0)
					return;

				if (form.isEdit) {
					if (EditorEnvironment.showConfirmDialog("有未保存的设计，如果继续发布将自动保存内容，是否继续发布？", "发布",
							JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
						return;
					}

					((IMainMenuOperation) form).onSave();
				}

				WaitDialog.Show("发布", "发布中，请等待。。。", new IProcess() {
					@Override
					public boolean doProc(WaitDialog waitDialog) {
						return EditorEnvironment.publishModelNode(new IPublish() {
							@Override
							public void publishContents() throws Exception {
								for (DrawNode drawNode : publishNodes) {
									ModelflowBuilder.publish(drawNode.title, getSelectDPI(), (WorkflowNode) drawNode,
											nodes);
								}
							}
						}, publishNodes, needResource.isSelected(), needUserJS.isSelected(), needReport.isSelected(),
								needDataSource.isSelected());
					}

					@Override
					public void closed(boolean isok) {
						if (isok)
							EditorEnvironment.showMessage(null, "恭喜，发布成功完成！", "发布成功", JOptionPane.INFORMATION_MESSAGE);
					}
				}, null);

			}
		});
		menuItem_38.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		mnNewMenu_2.add(menuItem_38);

		JMenuItem mntmframe = new JMenuItem("更新Frame");
		mntmframe.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		mntmframe.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.ALT_MASK));
		mntmframe.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!EditorEnvironment.isOpenProject()) {
					EditorEnvironment.showMessage(null, "请先打开一个项目！");
					return;
				}

				WaitDialog.Show("更新Frame", "正在更新，请等待。。。", new IProcess() {

					@Override
					public boolean doProc(WaitDialog waitDialog) {
						try {
							return EditorEnvironment.updateFrame();
						} catch (Exception e1) {
							EditorEnvironment.showException(e1);
						}
						return false;
					}

					@Override
					public void closed(boolean isok) {
						if (isok) {
							EditorEnvironment.showMessage(null, "更新frame成功！", "更新frame",
									JOptionPane.INFORMATION_MESSAGE);
						} else {

						}
					}
				}, null);
			}
		});

		JSeparator separator_10 = new JSeparator();
		mnNewMenu_2.add(separator_10);
		mnNewMenu_2.add(mntmframe);

		JMenu menu_10 = new JMenu(" 编辑器 ");
		menu_10.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		menuBar.add(menu_10);

		JMenuItem menuItem_22 = new JMenuItem("报表设计");
		menuItem_22.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.ALT_MASK));
		menu_10.add(menuItem_22);
		menuItem_22.setFont(new Font("微软雅黑", Font.PLAIN, 13));

		JMenuItem menuItem_34 = new JMenuItem("主菜单设计            ");
		menuItem_34.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.ALT_MASK));
		menuItem_34.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!EditorEnvironment.isOpenProject()) {
					EditorEnvironment.showMessage(null, "请先打开一个项目！");
					return;
				}

				MenuEditorDialog.showDialog(MainForm.this,
						EditorEnvironment.getProjectFile(EditorEnvironment.Menu_Dir_Path,
								EditorEnvironment.getMenu_FileName(EditorEnvironment.Main_Menu_FileName)));
			}
		});
		menuItem_34.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		menu_10.add(menuItem_34);

		JMenuItem menuItem_35 = new JMenuItem("主导航书设计");
		menuItem_35.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.ALT_MASK));
		menuItem_35.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!EditorEnvironment.isOpenProject()) {
					EditorEnvironment.showMessage(null, "请先打开一个项目！");
					return;
				}

				NavigationEditorDialog.showDialog(MainForm.this,
						EditorEnvironment.getProjectFile(EditorEnvironment.Tree_Dir_Path,
								EditorEnvironment.getTree_FileName(EditorEnvironment.Main_Tree_FileName)));
			}
		});
		menuItem_35.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		menu_10.add(menuItem_35);

		JSeparator separator_28 = new JSeparator();
		menu_10.add(separator_28);

		JMenuItem mntmCss = new JMenuItem("CSS编辑");
		menu_10.add(mntmCss);
		mntmCss.setFont(new Font("微软雅黑", Font.PLAIN, 13));

		JSeparator separator_27 = new JSeparator();
		menu_10.add(separator_27);

		JMenuItem mntmUi_2 = new JMenuItem("新建界面");
		menu_10.add(mntmUi_2);
		mntmUi_2.setFont(new Font("微软雅黑", Font.PLAIN, 13));

		JMenuItem menuItem_23 = new JMenuItem("打开界面");
		menu_10.add(menuItem_23);
		menuItem_23.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		menuItem_23.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!EditorEnvironment.isOpenProject()) {
					EditorEnvironment.showMessage(null, "请先打开一个项目！");
					return;
				}

				Result uiInfo = UISelectDialog.showDialog(MainForm.this, null, null);

				if (uiInfo == null)
					return;

				File file = EditorEnvironment.getProjectFile(EditorEnvironment.UI_Dir_Name,
						EditorEnvironment.getUI_FileName(uiInfo.id));
				if (file.exists())
					openUIBuilder(file);
			}
		});
		mntmUi_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!EditorEnvironment.isOpenProject()) {
					EditorEnvironment.showMessage(null, "请先打开一个项目！");
					return;
				}

				String name = EditorEnvironment.showInputDialog("请输入新界面的名称");
				if (name.isEmpty())
					return;

				File file = EditorEnvironment.getProjectFile(EditorEnvironment.UI_Dir_Name,
						EditorEnvironment.getUI_FileName(name));
				if (file.exists()) {
					if (EditorEnvironment.showConfirmDialog("文件：" + name + "已经存在，是否覆盖？", "新建界面",
							JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
						return;
				}
				openUIBuilder(file);
			}
		});
		mntmCss.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!EditorEnvironment.isOpenProject()) {
					EditorEnvironment.showMessage(null, "请先打开一个项目！");
					return;
				}

				openCSSEditor();
			}
		});
		menuItem_22.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!EditorEnvironment.isOpenProject()) {
					EditorEnvironment.showMessage(null, "请先打开一个项目！");
					return;
				}

				openReportEditor();
			}
		});

		JMenu menu_14 = new JMenu(" 需求 ");
		menuBar.add(menu_14);
		menu_14.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		JMenuItem menuItem_39 = new JMenuItem("设置需求类别         ");
		menuItem_39.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.ALT_MASK));
		menuItem_39.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!EditorEnvironment.isOpenProject()) {
					EditorEnvironment.showMessage(null, "请先打开一个项目！");
					return;
				}

				TreeMap<String, RequirementTypeInfo> types = RequirementController.getRequirementTypes();
				Object[][] rows = new Object[types.size()][1];
				int index = 0;
				for (RequirementTypeInfo info : types.values()) {
					rows[index++][0] = info;
				}

				Object[] columns = new Object[] { "类别名称" };

				ModelResult result = KeyValueSelector.show(null, mainControl, new ICheckValue() {

					@Override
					public boolean onCheck(Object[][] originalData, JTable table) {
						HashMap<Object, Object> map = new HashMap<>();
						for (int i = 0; i < table.getRowCount(); i++) {
							Object value = table.getValueAt(i, 0);
							if (map.containsKey(value)) {
								EditorEnvironment.showMessage("行【" + i + "】的类别名称重复！");
								return false;
							}

							map.put(value, value);
						}
						return true;
					}
				}, new IEditRow() {

					@Override
					public void updateRow(JTable table, Vector<?> row) {
					}

					@Override
					public boolean deleteRow(JTable table, Vector<?> row) {
						if (EditorEnvironment.showConfirmDialog("删除选中类别,则此类别下的需求信息也将不可用，是否继续？",
								JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
							return true;
						}
						return false;
					}

					@Override
					public Object[] addRow(JTable table) {
						String name = EditorEnvironment.showInputDialog("请输入新类别名称", "新类别");
						if (name == null || name.isEmpty())
							return null;
						RequirementTypeInfo info = new RequirementTypeInfo();
						info.name = name;
						info.memo = "";
						info.file = null;
						return new Object[] { info };
					}
				}, rows, columns, new Object[] { new JButton() }, null, new IActionListener() {

					@Override
					public ActionResult onAction(TableModel model, String key, Object value, int row, int col,
							List<Object> selects) {
						RequirementTypeInfo info = (RequirementTypeInfo) value;
						String name = EditorEnvironment.showInputDialog("请输入新类别名称", info.name);
						if (name != null && !name.isEmpty())
							info.name = name;
						ActionResult actionResult = new ActionResult();
						actionResult.data = info;
						actionResult.isok = true;
						return actionResult;
					}
				}, false);

				DefaultTableModel model = result.isok ? result.model : null;

				if (model == null)
					return;

				HashMap<String, String> data = new HashMap<>();
				for (int i = 0; i < model.getRowCount(); i++) {
					RequirementTypeInfo info = (RequirementTypeInfo) model.getValueAt(i, 0);
					try {
						RequirementController.syncRequirementFile(info);
					} catch (Exception e2) {
						EditorEnvironment.showException(e2);
						info.name = FileHelp.removeExt(info.file.getName());
					}
					data.put(info.name, info.memo);
				}

				RequirementController.setRequirementTypes(data);
				initRequirementMenu();
			}
		});
		menuItem_39.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		menu_14.add(menuItem_39);

		JSeparator separator_20 = new JSeparator();
		menu_14.add(separator_20);

		requireRootMenu = new JMenu("需求维护");
		requireRootMenu.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		menu_14.add(requireRootMenu);

		versionRootMenu = new JMenu("版本查看");
		versionRootMenu.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		menu_14.add(versionRootMenu);

		JMenu menu_16 = new JMenu("进度");
		menu_16.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		menu_14.add(menu_16);

		JMenuItem menuItem_37 = new JMenuItem("已关闭");
		menuItem_37.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!EditorEnvironment.isOpenProject()) {
					EditorEnvironment.showMessage(null, "请先打开一个项目！");
					return;
				}

				openRequirementListView(RequirementState.rsClose);
			}
		});
		menuItem_37.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		menu_16.add(menuItem_37);

		JMenuItem menuItem_40 = new JMenuItem("未关闭");
		menuItem_40.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!EditorEnvironment.isOpenProject()) {
					EditorEnvironment.showMessage(null, "请先打开一个项目！");
					return;
				}

				openRequirementListView(RequirementState.rsNoClose);
			}
		});
		menuItem_40.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		menu_16.add(menuItem_40);

		JMenu menu_12 = new JMenu(" 统计 ");
		menu_12.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		menuBar.add(menu_12);

		JMenuItem menuItem_27 = new JMenuItem("选中节点任务量          ");
		menuItem_27.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.ALT_MASK));
		menu_12.add(menuItem_27);
		menuItem_27.setFont(new Font("微软雅黑", Font.PLAIN, 13));

		JMenuItem menuItem_26 = new JMenuItem("任务总量");
		menu_12.add(menuItem_26);
		menuItem_26.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		menuItem_26.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!EditorEnvironment.isOpenProject()) {
					EditorEnvironment.showMessage(null, "请先打开一个项目！");
					return;
				}

				try {
					computeTaskSum(EditorEnvironment.getMainModelRelationFile());
				} catch (Exception e1) {
					e1.printStackTrace();
					EditorEnvironment.showException(e1);
				}

			}
		});
		menuItem_27.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!EditorEnvironment.isOpenProject()) {
					EditorEnvironment.showMessage(null, "请先打开一个项目！");
					return;
				}

				if (topFrame instanceof ModelflowBuilder) {
					ModelflowBuilder form = (ModelflowBuilder) topFrame;
					WorkflowNode node = (WorkflowNode) form.canvas.getSelected();
					if (node != null && !(node instanceof ChildWorkflowNode)) {
						EditorEnvironment.showMessage("请选择一个子节点类型！");
						return;
					}

					try {
						computeTaskSum((ChildWorkflowNode) node);
					} catch (Exception e1) {
						e1.printStackTrace();
						EditorEnvironment.showException(e1);
					}
				}
			}
		});

		JMenu menu_11 = new JMenu(" 工具 ");
		menu_11.setActionCommand("");
		menu_11.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		menuBar.add(menu_11);

		final ButtonGroup models = new ButtonGroup();
		models.add(exportAllModel);
		models.add(exportSelectModel);
		models.add(exportSelectModelNode);

		JMenu menu_5 = new JMenu("设计                ");
		menu_11.add(menu_5);
		menu_5.setFont(new Font("微软雅黑", Font.PLAIN, 14));

		exportModel = new JCheckBoxMenuItem("导出模块关系图");
		exportModel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		menu_5.add(exportModel);

		exportModalNode = new JCheckBoxMenuItem("导出模块定义");
		exportModalNode.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		exportModalNode.setSelected(true);
		menu_5.add(exportModalNode);

		exportWorflow = new JCheckBoxMenuItem("导出业务流程图");
		exportWorflow.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		exportWorflow.setSelected(true);
		menu_5.add(exportWorflow);

		exportScene = new JCheckBoxMenuItem("导出场景");
		exportScene.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		exportScene.setSelected(true);
		menu_5.add(exportScene);

		exportUI = new JCheckBoxMenuItem("导出界面");
		exportUI.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		exportUI.setSelected(true);
		menu_5.add(exportUI);

		exportNavUI = new JCheckBoxMenuItem("导出导航界面");
		exportNavUI.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		menu_5.add(exportNavUI);

		exportAppFlow = new JCheckBoxMenuItem("导出程序流程图");
		exportAppFlow.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		exportAppFlow.setSelected(true);
		menu_5.add(exportAppFlow);

		exportRunFlow = new JCheckBoxMenuItem("导出运行流程图");
		exportRunFlow.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		exportRunFlow.setSelected(true);
		menu_5.add(exportRunFlow);

		JMenu menu_6 = new JMenu("导出模式");
		menu_6.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		menu_5.add(menu_6);

		exportSelectModelNode = new JRadioButtonMenuItem("仅选定模块");
		exportSelectModelNode.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		exportSelectModelNode.setSelected(true);
		menu_6.add(exportSelectModelNode);

		exportSelectModel = new JRadioButtonMenuItem("仅当前模块关系图");
		exportSelectModel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		menu_6.add(exportSelectModel);

		exportAllModel = new JRadioButtonMenuItem("导出所有关系图");
		exportAllModel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		menu_6.add(exportAllModel);

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(exportSelectModelNode);
		buttonGroup.add(exportSelectModel);
		buttonGroup.add(exportAllModel);
		JSeparator separator_19 = new JSeparator();
		menu_5.add(separator_19);

		JMenuItem mntmword = new JMenuItem("导出详细设计（WORD）");
		mntmword.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		mntmword.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.ALT_MASK));
		menu_5.add(mntmword);
		mntmword.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!EditorEnvironment.isOpenProject()) {
					EditorEnvironment.showMessage(null, "请先打开一个项目！");
					return;
				}

				com.wh.dialog.DocExportSetupDialog.Result result = DocExportSetupDialog.showDialog(null, null, null,
						true);
				if (result == null)
					return;

				try {
					String title = "[" + EditorEnvironment.getCurrentProjectName() + "]详细设计";
					String workflowRelationName = null;
					if (exportSelectModel.isSelected()) {
						if (topFrame == null) {
							EditorEnvironment.showMessage("当前无选定项目，请打开设计器或者选择【导出所有】！");
							return;
						}

						if (topFrame instanceof UIBuilder) {
							UIBuilder uiBuilder = (UIBuilder) topFrame;
							if (uiBuilder.workflowNode == null) {
								EditorEnvironment.showMessage("当前无选定项目，请打开设计器或者选择【导出所有】！");
								return;
							}

							workflowRelationName = EditorEnvironment.getModelRelationName(
									EditorEnvironment.getModelRelationFileFromNodeID(uiBuilder.workflowNode.id));
							title = uiBuilder.workflowNode.title != null && !uiBuilder.workflowNode.title.isEmpty()
									? "[" + uiBuilder.workflowNode.title + "]详细设计" : title;
						} else if (topFrame instanceof ModelflowBuilder) {
							ModelflowBuilder modelflowForm = (ModelflowBuilder) topFrame;
							if (modelflowForm.frameModelNodes != null && modelflowForm.frameModelNodes.size() > 0) {
								EditorEnvironment.showMessage("当前模块图不能导出文档！");
								return;
							}

							workflowRelationName = FileHelp.removeExt(modelflowForm.workflowName);
							String tmp = modelflowForm.canvas.getPageConfig().title;
							title = tmp != null && !tmp.isEmpty() ? "[" + tmp + "]详细设计" : title;
						}
					}

					WorkflowNode selectNode = null;
					if (exportSelectModelNode.isSelected()) {
						if (topFrame instanceof ModelflowBuilder) {
							ModelflowBuilder modelflowForm = (ModelflowBuilder) topFrame;
							selectNode = (WorkflowNode) modelflowForm.canvas.getSelected();
							if (selectNode == null) {
								EditorEnvironment.showMessage("当前无选定项目，请先选择一个模块节点！");
								return;
							}

							workflowRelationName = FileHelp.removeExt(modelflowForm.workflowName);
							title = "[" + selectNode.title + "]详细设计";
						} else {
							EditorEnvironment.showMessage("当前设计器不是模块关系设计器，请打开模块关系设计器后再试！");
							return;
						}
					}

					ExportToWord.asyncExport(workflowRelationName, title, result.headers, result.footers,
							result.saveFile, selectNode, exportModel.isSelected(), exportModalNode.isSelected(),
							exportRunFlow.isSelected(), exportWorflow.isSelected(), exportScene.isSelected(),
							exportUI.isSelected(), exportAppFlow.isSelected(), exportNavUI.isSelected(),
							result.headerFont, result.footerFont, true);
				} catch (Exception e1) {
					e1.printStackTrace();
					EditorEnvironment.showException(e1);
				}
			}
		});

		JMenuItem menuItem_28 = new JMenuItem("导出任务单");
		menuItem_28.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		menuItem_28.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!EditorEnvironment.isOpenProject()) {
					EditorEnvironment.showMessage(null, "请先打开一个项目！");
					return;
				}

				File traveRelationFile = null;
				WorkflowNode selectNode = null;
				if (topFrame instanceof ModelflowBuilder) {
					ModelflowBuilder form = (ModelflowBuilder) topFrame;
					WorkflowNode node = (WorkflowNode) form.canvas.getSelected();
					if (node != null) {
						if (node instanceof ChildWorkflowNode) {
							traveRelationFile = EditorEnvironment.getChildModelRelationFile(node.id, false);
						} else if (node instanceof BeginNode || node instanceof EndNode) {
							return;
						} else {
							selectNode = node;
						}
					}
				}

				if (traveRelationFile == null && selectNode == null) {
					if (EditorEnvironment.showQuestionDialog("您是否需要生成整个项目的任务单？") != JOptionPane.YES_OPTION)
						return;

					traveRelationFile = EditorEnvironment.getMainModelRelationFile();
				}

				File saveFile = null;
				if (selectNode != null) {
					saveFile = EditorEnvironment.getProjectFile(EditorEnvironment.Export_Dir_Name,
							selectNode.title + ".doc");
				}
				DocExportSetupDialog.Result result = DocExportSetupDialog.showDialog(null, null, saveFile,
						saveFile != null);
				if (result == null)
					return;

				try {
					if (selectNode != null) {
						String exporttitle = "[" + selectNode.title + "]任务单";

						ExportToWord.asyncExport(FileHelp.removeExt(selectNode.getCanvas().canvasFile.getName()),
								exporttitle, result.headers, result.footers, result.saveFile, (WorkflowNode) selectNode,
								true, true, false, true, true, true, true, false, result.headerFont, result.footerFont,
								true);

					} else {
						EditorEnvironment.traverseModel(traveRelationFile, new ITraverseDrawNode() {

							@Override
							public boolean onNode(File workflowRelationFile, String title, DrawNode node,
									Object param) {
								if (node instanceof BeginNode || node instanceof EndNode
										|| node instanceof ChildWorkflowNode) {
									return true;
								}

								String exporttitle = "[" + node.title + "]任务单";

								ExportToWord.asyncExport(EditorEnvironment.getModelRelationName(workflowRelationFile),
										exporttitle, result.headers, result.footers,
										new File(result.saveFile, exporttitle + ".doc"), (WorkflowNode) node, true,
										true, false, true, true, true, true, false, result.headerFont,
										result.footerFont, false);
								return true;
							}
						}, null);

						EditorEnvironment.showMessage("成功导出文档，关闭自动打开文档目录！");
						try {
							Desktop.getDesktop().open(result.saveFile);
						} catch (IOException ex) {
							ex.printStackTrace();
						}
					}
				} catch (Exception e1) {
					e1.printStackTrace();
					EditorEnvironment.showException(e1);
				}

			}
		});
		menuItem_28.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.ALT_MASK));
		menu_5.add(menuItem_28);

		JMenu menu_1 = new JMenu(" 帮助 ");
		menu_1.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		menuBar.add(menu_1);

		JMenuItem menuItem_6 = new JMenuItem("关于我们...           ");
		menuItem_6.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		menuItem_6.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.ALT_MASK));
		menuItem_6.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				HelpDialog.showDialog();
			}
		});

		JMenuItem menuItem_50 = new JMenuItem("需求设计手册");
		menuItem_50.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				EditorEnvironment.openHelp("requirement_manual.docx");
			}
		});
		menu_1.add(menuItem_50);

		JMenuItem menuItem_49 = new JMenuItem("报表设计手册");
		menuItem_49.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				EditorEnvironment.openHelp("report_manual.docx");
			}
		});
		menu_1.add(menuItem_49);

		JSeparator separator_32 = new JSeparator();
		menu_1.add(separator_32);

		JMenuItem mntmGaea = new JMenuItem("Gaea操作手册");
		mntmGaea.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				EditorEnvironment.openHelp("operation_manual.docx");
			}
		});
		menu_1.add(mntmGaea);

		JSeparator separator_26 = new JSeparator();
		menu_1.add(separator_26);
		menu_1.add(menuItem_6);
		desktopPane.setBackground(Color.WHITE);

		mainFrame.getContentPane().add(desktopPane, BorderLayout.CENTER);

		ways = new JToolBar();
		ways.setOpaque(false);
		ways.setBorder(null);
		ways.setFloatable(false);
		ways.setAutoscrolls(true);
		ways.setRollover(true);
		ways.setBackground(Color.WHITE);
		ways.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		ways.setVisible(false);

		JPanel panel = new JPanel();
		panel.setOpaque(false);
		panel.setLayout(new BorderLayout());
		mainFrame.getContentPane().add(panel, BorderLayout.NORTH);
		panel.add(ways, BorderLayout.CENTER);

		JLabel toolbarButton = new JLabel("");
		toolbarButton.setBackground(SystemColor.window);
		toolbarButton.setIcon(new ImageIcon(MainForm.class.getResource("/image/play32.png")));
		panel.add(toolbarButton, BorderLayout.EAST);

		toolbarMenu = new JPopupMenu();
		toolbarMenu.addPopupMenuListener(new PopupMenuListener() {

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				needShow = true;
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
				needShow = true;
			}

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			}

		});
		addPopup(toolbarButton, toolbarMenu);

		JToolBar toolBar = new JToolBar();
		toolBar.setOpaque(false);
		toolBar.setFloatable(false);
		toolBar.setRollover(true);
		panel.add(toolBar, BorderLayout.NORTH);
		toolBar.setBorder(null);

		JButton saveButton = new JButton("");
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveMenuItem.doClick();
			}
		});

		JSeparator separator_25 = new JSeparator();
		separator_25.setMinimumSize(new Dimension(0, 5));
		separator_25.setMaximumSize(new Dimension(5, 32767));
		toolBar.add(separator_25);
		saveButton.setToolTipText("保存");
		saveButton.setIcon(new ImageIcon(MainForm.class.getResource("/image/ord/save.png")));
		toolBar.add(saveButton);

		JSeparator separator_21 = new JSeparator();
		separator_21.setMinimumSize(new Dimension(0, 5));
		separator_21.setMaximumSize(new Dimension(5, 32767));
		toolBar.add(separator_21);

		JButton closeButton = new JButton("");
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				closeMenuItem.doClick();
			}
		});
		closeButton.setToolTipText("关闭当前页面");
		closeButton.setIcon(new ImageIcon(MainForm.class.getResource("/image/ord/delete.png")));
		toolBar.add(closeButton);

		JSeparator separator_22 = new JSeparator();
		separator_22.setMinimumSize(new Dimension(0, 5));
		separator_22.setMaximumSize(new Dimension(5, 32767));
		toolBar.add(separator_22);

		backButton = new JButton("");
		backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String nodeid = getCurModelRelationID();
				if (nodeid == null)
					return;

				if (nodeid.equals(EditorEnvironment.Main_Workflow_Relation_FileName)) {
					toolBarButtons.switchMain();
					return;
				}

				try {
					EditorEnvironment.traverseModel(new ITraverseDrawNode() {

						@Override
						public boolean onNode(File workflowRelationFile, String title, DrawNode node, Object param) {
							if (node instanceof ChildWorkflowNode) {
								String cur = EditorEnvironment.getChildModelRelationName(node.id);
								if (cur.compareToIgnoreCase(nodeid) == 0) {
									String relationName = EditorEnvironment.getModelRelationName(workflowRelationFile);
									openModelflowRelation(relationName, null);
									return false;
								}
							}
							return true;
						}
					}, null);
				} catch (Exception e1) {
					e1.printStackTrace();
				}

			}
		});
		backButton.setToolTipText("后退");
		backButton.setIcon(new ImageIcon(MainForm.class.getResource("/image/ord/arrowleft.png")));
		toolBar.add(backButton);

		JSeparator separator_23 = new JSeparator();
		separator_23.setMinimumSize(new Dimension(0, 5));
		separator_23.setMaximumSize(new Dimension(5, 32767));
		toolBar.add(separator_23);

		JButton nextButton = new JButton("");
		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				String nodeid = getCurModelNodeID();
				if (nodeid == null || nodeid.isEmpty())
					return;
				File file = EditorEnvironment.getChildModelRelationFile(nodeid, false);
				if (file != null) {
					openExistsModelflowRelation(file, (DrawNode) null);
				}
			}
		});
		nextButton.setToolTipText("前进");
		nextButton.setIcon(new ImageIcon(MainForm.class.getResource("/image/ord/arrowright.png")));
		toolBar.add(nextButton);

		JSeparator separator_24 = new JSeparator();
		separator_24.setMinimumSize(new Dimension(0, 5));
		separator_24.setMaximumSize(new Dimension(5, 32767));
		toolBar.add(separator_24);

		JButton switchButton = new JButton("");
		switchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				switchUIOrModelNode();
			}
		});
		switchButton.setToolTipText("切换[界面/节点]");
		switchButton.setIcon(new ImageIcon(MainForm.class.getResource("/image/ord/monitor.png")));
		toolBar.add(switchButton);

		JLabel label = new JLabel("   ");
		toolBar.add(label);

		linkToolBar = new JToolBar();
		linkToolBar.setVisible(false);
		toolBar.add(linkToolBar);
		linkToolBar.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		linkToolBar.setBorder(null);
		linkToolBar.setOpaque(false);
		linkToolBar.setFloatable(false);

		setMenuFont();

	}

	protected void setMenuFont() {
		for (int i = 0; i < menuBar.getMenuCount(); i++) {
			JMenu menu = menuBar.getMenu(i);
			if (menu == null)
				continue;
			setMenuItemFont(menu);
		}
	}

	protected void setMenuItemFont(JMenu menu) {
		menu.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		for (int i = 0; i < menu.getItemCount(); i++) {
			JMenuItem item = menu.getItem(i);
			if (item == null)
				continue;

			if (item instanceof JMenu) {
				setMenuItemFont((JMenu) item);
			}
			item.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		}
	}

	static IMainControl mainForm;

	public interface IControl {
		public void onStart(ChildForm subForm, Object param);

		public void onEnd(ChildForm subForm);
	}

	@Override
	public void toFront(ChildForm form) {
		form.toFront();
	}

	@Override
	public void selectForm(ChildForm form) {
		desktopPane.setSelectedFrame(form);
	}

	HashMap<ChildForm, ChildForm> hideForms = new HashMap<ChildForm, ChildForm>();
	private JCheckBoxMenuItem needFrame;
	private JToolBar ways;
	private JCheckBoxMenuItem needResource;
	private JCheckBoxMenuItem needUserJS;
	private JCheckBoxMenuItem needReport;
	private JCheckBoxMenuItem exportModalNode;
	private JCheckBoxMenuItem exportModel;
	private JCheckBoxMenuItem exportWorflow;
	private JCheckBoxMenuItem exportScene;
	private JCheckBoxMenuItem exportUI;
	private JCheckBoxMenuItem exportAppFlow;
	private JCheckBoxMenuItem exportRunFlow;
	private JRadioButtonMenuItem exportSelectModelNode;
	private JRadioButtonMenuItem exportSelectModel;
	private JRadioButtonMenuItem exportAllModel;
	private JPopupMenu toolbarMenu;

	@Override
	public void switchSubForm(ChildForm parent, Class<? extends ChildForm> subFormClass, IControl iControl,
			Object param) throws Exception {

		ChildForm subForm = openFrame(subFormClass, true);
		ISubForm iSubForm = (ISubForm) subForm;
		iSubForm.setParentForm(parent);
		if (iControl != null) {
			iControl.onStart(subForm, param);
		}
		parent.setVisible(false);
	}

	public static IMainControl getMainForm() {
		return mainForm;
	}

	@Override
	public JInternalFrame[] getForms() {
		return desktopPane.getAllFrames();
	}

	@Override
	public ChildForm getFront() {
		return topFrame;
	}

	static {
		NoCheckTableNames.put("runninglogs", "runninglogs");
		NoCheckTableNames.put("runninglogs_content", "runninglogs_content");
		NoCheckTableNames.put("userinfo", "userinfo");
	}

	@Override
	public void openCodeflowRelation(String name, String uiid, String workflowid) {
		String filename = EditorEnvironment.getAppWorkflow_FileName(name);
		File file = EditorEnvironment.getProjectFile(EditorEnvironment.AppWorkflow_Dir_Name, filename);
		if (existsAndFront(FileHelp.removeExt(file.getName()), FormType.ftAppWorkflow))
			return;

		if (!EditorEnvironment.lockFile(file)) {
			EditorEnvironment.showMessage("文件【" + file.getAbsolutePath() + "】已经被其他用户锁定，请稍后再试！");
			return;
		}

		CodeFlowBuilder form = (CodeFlowBuilder) openFrame(CodeFlowBuilder.class, true);
		form.onStart(null, new Object[] { file, uiid, workflowid });
		toolBarButtons.add(topFrame, name + "[代码流程设计]", false);
	}

	class JTagMenuItem extends JMenuItem {
		private static final long serialVersionUID = 1L;
		public Object tag;

		public JTagMenuItem(String text) {
			super(text);
		}
	}

	boolean needShow = true;
	private JCheckBoxMenuItem exportNavUI;
	private JMenuItem saveMenuItem;
	private JMenuItem closeMenuItem;
	private JButton backButton;
	private JToolBar linkToolBar;
	private JCheckBoxMenuItem needMenu;
	private JCheckBoxMenuItem needNav;
	private JCheckBoxMenuItem needDataSource;
	private JMenuBar menuBar;
	private JMenu requireRootMenu;
	private JMenu versionRootMenu;
	private JCheckBoxMenuItem ldpimodel;
	private JCheckBoxMenuItem hdpimode;
	private JCheckBoxMenuItem xhdpimode;
	private JCheckBoxMenuItem mdpimode;
	private JCheckBoxMenuItem tvdpimode;
	private JCheckBoxMenuItem xxhdpimode;
	private JCheckBoxMenuItem xxxhdpimode;
	private JCheckBoxMenuItem noandroidmode;
	private JMenu androidMode;
	private JCheckBoxMenuItem needRemoteAuth;
	private JCheckBoxMenuItem linkedChecker;
	private JCheckBoxMenuItem newProjectChecker;
	private JCheckBoxMenuItem includeImage;
	private JCheckBoxMenuItem includeReport;
	private JCheckBoxMenuItem includeAuth;
	private JCheckBoxMenuItem includeConfig;
	private JCheckBoxMenuItem includeUserJs;
	private JCheckBoxMenuItem includeDataSource;
	private JCheckBoxMenuItem includeMasterData;
	private JCheckBoxMenuItem onlySelectedModel;

	private void fillToolbarButtonMenu() {
		toolbarMenu.removeAll();
		for (int i = 0; i < ways.getComponentCount(); i++) {
			Component component = ways.getComponent(i);
			if (component instanceof JButton) {
				JButton button = (JButton) component;
				JTagMenuItem item = new JTagMenuItem(button.getText());
				item.tag = button;
				item.addActionListener(button.getActionListeners()[0]);
				toolbarMenu.add(item);
			}
		}
	}

	private void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			private void showMenu(MouseEvent e) {
				if (e.getButton() != MouseEvent.BUTTON1)
					return;
				if (!needShow)
					return;
				needShow = false;
				fillToolbarButtonMenu();
				popup.show(e.getComponent(), e.getX(), e.getY());
			}

			public void mousePressed(MouseEvent e) {
				showMenu(e);
			}

			public void mouseReleased(MouseEvent e) {
				showMenu(e);
			}

			public void mouseClicked(MouseEvent e) {
				showMenu(e);
			}

		});
	}

	@Override
	public void openUIBuilder(File file, String controlId) throws Exception {
		if (!openUIBuilder(file))
			throw new Exception("打开界面设计器失败：" + file.getAbsolutePath());

		UIBuilder builder = (UIBuilder) topFrame;
		for (DrawNode drawNode : builder.canvas.getNodes()) {
			UINode node = (UINode) drawNode;
			if (node.getDrawInfo().id.compareTo(controlId) == 0 || node.getDrawInfo().name.compareTo(controlId) == 0) {
				builder.canvas.setSelected(node);
				break;
			}
		}
	}

	@Override
	public void openUIBuilder(String uiid, String controlId) throws Exception {
		File uiFile = EditorEnvironment.getUIFileForUIID(uiid);
		openUIBuilder(uiFile, controlId);
	}

	@Override
	public void openReportEditor() {
		openReportEditor(null, null);
	}

	@Override
	public void openReportEditor(UIBuilder uiBuilder, UINode node) {
		if (node != null && existsAndFront(node.getDrawInfo().id, FormType.ftReport))
			return;

		ReportBuilder subForm = (ReportBuilder) openFrame(ReportBuilder.class, true, uiBuilder, node);

		String title = "新建报表";
		if (node == null) {
			List<DrawNode> nodes = subForm.canvas.getNodes();
			if (nodes.size() > 0)
				node = (UINode) nodes.get(0);
		}
		if (node != null && node.title != null && !node.title.isEmpty()) {
			title = node.title;
		}
		updateUIButtonTitle(topFrame, title + "[报表编辑]");
	}

	@Override
	public void openFrameNodeEditor() {
		File modelflowFile = EditorEnvironment.getFrameNodeLockFile();
		if (!EditorEnvironment.lockFile(modelflowFile)) {
			EditorEnvironment.showMessage("文件【" + modelflowFile.getAbsolutePath() + "】已经被其他用户锁定，请稍后再试！");
			return;
		}

		if (modelflowFile != null) {
			if (existsAndFront(FileHelp.removeExt(modelflowFile.getName()), FormType.ftWorkflow))
				return;
		}

		ModelflowBuilder subForm = (ModelflowBuilder) openFrame(ModelflowBuilder.class, true);
		ISubForm iSubForm = (ISubForm) subForm;
		try {
			if (modelflowFile != null) {

				iSubForm.onStart(new ModelflowBuilder.StartInfo(EditorEnvironment.getFrameModelNodes(),
						EditorEnvironment.FrameModelName));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		toolBarButtons.add(topFrame, "系统模块[模块关系图设计]", true);
	}

	@Override
	public Integer getSelectDPI() {
		if (noandroidmode.isSelected())
			return null;
		else {
			for (int i = 0; i < androidMode.getItemCount(); i++) {
				JCheckBoxMenuItem item = (JCheckBoxMenuItem) androidMode.getItem(i);
				if (item.isSelected()) {
					String text = item.getText().replaceAll("（.*）", "");
					return DrawCanvas.getDpi(text);
				}
			}
		}
		return null;
	}
}
