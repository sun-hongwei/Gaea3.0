package com.wh.dialog.editor;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.json.JSONObject;

import com.wh.control.EditorEnvironment;
import com.wh.control.EditorEnvironment.NodeDescInfo;
import com.wh.draws.DrawNode;
import com.wh.draws.UICanvas;
import com.wh.draws.UINode;
import com.wh.draws.DrawCanvas.ChangeType;
import com.wh.draws.DrawCanvas.Config;
import com.wh.draws.DrawCanvas.ICreateNodeSerializable;
import com.wh.draws.DrawCanvas.IDataSerializable;
import com.wh.draws.DrawCanvas.IInitPage;
import com.wh.draws.DrawCanvas.INode;
import com.wh.draws.DrawCanvas.IOnPageSizeChanged;
import com.wh.draws.DrawCanvas.IScroll;
import com.wh.draws.DrawCanvas.PageConfig;
import com.wh.draws.drawinfo.DivInfo;
import com.wh.draws.drawinfo.DrawInfo;

public class ControlSelectDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

	HashMap<String, String> names = new HashMap<>();

	UICanvas canvas = new UICanvas();
	private JScrollBar hScrollBar;
	private JScrollBar vScrollBar;
	private JPanel panel_1;

	DrawNode workflowNode;
	HashMap<String, DrawNode> workflowNodes;
	String workflowRelationTitle;

	HashMap<String, Class<DrawInfo>> selectClassMap;

	protected void ok() {
		if (canvas.getSelected() == null) {
			EditorEnvironment.showMessage("请先选择一个控件后再试！");
			return;
		}

		if (selectClassMap != null && selectClassMap.size() > 0) {
			if (!selectClassMap.containsKey(((UINode) canvas.getSelected()).getDrawInfo().getClass().getName())) {
				String[] names = new String[selectClassMap.size()];
				int index = 0;
				for (Class<DrawInfo> c : selectClassMap.values()) {
					try {
						Constructor<DrawInfo> constructor = c.getDeclaredConstructor(UINode.class);
						DrawInfo drawInfo = constructor.newInstance((UINode) null);
						names[index++] = drawInfo.typeName();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				EditorEnvironment.showMessage("请先选择一个" + Arrays.toString(names) + "类型控件后再试！");
				return;
			}
		}
		isok = true;
		setVisible(false);
	}

	public ControlSelectDialog() throws Exception {
		super();
		setFont(new Font("微软雅黑", Font.PLAIN, 12));
		setIconImage(Toolkit.getDefaultToolkit().getImage(ControlSelectDialog.class.getResource("/image/browser.png")));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 1204, 768);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		panel_1 = new JPanel();
		contentPane.add(panel_1, BorderLayout.CENTER);
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
				if (vScrollBar.getMaximum() > 0) {
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
		panel_1.add(hScrollBar, BorderLayout.SOUTH);
		hScrollBar.setUnitIncrement(10);
		hScrollBar.setOrientation(JScrollBar.HORIZONTAL);
		hScrollBar.setMaximum(99999);

		vScrollBar = new JScrollBar();
		panel_1.add(vScrollBar, BorderLayout.EAST);
		vScrollBar.setUnitIncrement(10);
		vScrollBar.setMaximum(99999);
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
		canvas.onScroll = new IScroll() {

			@Override
			public void onScroll(int x, int y) {
				hScrollBar.setValue(Math.abs(x));
				vScrollBar.setValue(Math.abs(y));
			}
		};

		canvas.nodeEvent = new INode() {

			@Override
			public void onChange(DrawNode[] nodes, ChangeType ct) {
				// TODO Auto-generated method stub

			}

			@Override
			public void DoubleClick(DrawNode node) {
				if (!multiSelected)
					ok();
			}

			@Override
			public void Click(DrawNode node) {
			}
		};
		panel = new JPanel();
		contentPane.add(panel, BorderLayout.SOUTH);
		panel.setLayout(new BorderLayout(0, 0));

		toolBar = new JToolBar();
		panel.add(toolBar, BorderLayout.EAST);
		toolBar.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		okButton = new JButton("确定");
		okButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ok();
			}
		});

		btnNull = new JButton("null返回");
		btnNull.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		btnNull.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isok = true;
				canvas.setSelected(null);
				setVisible(false);
			}
		});
		toolBar.add(btnNull);
		toolBar.add(okButton);

		cancelButton = new JButton("取消");
		cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isok = false;
				setVisible(false);
			}
		});
		toolBar.add(cancelButton);

		memo = new JLabel("New label");
		memo.setFont(new Font("微软雅黑", Font.PLAIN, 14));
		memo.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(memo);

		setLocationRelativeTo(null);
	}

	class FileInfo {
		public String id;
		public String title;
		public String memo;
		public File file;

		public FileInfo(File file) throws Exception {
			if (file != null && file.exists()) {
				this.file = file;
				NodeDescInfo info = EditorEnvironment.getNodeDescInfo(file);
				id = info.id;
				title = info.title;
				memo = info.memo;
			}
		}

		public String toString() {
			if (title == null || title.isEmpty())
				return id;
			else
				return title;
		}
	}

	public void load(String name) {
		try {
			if (name == null || name.isEmpty())
				return;

			File f = EditorEnvironment.getProjectFile(EditorEnvironment.UI_Dir_Name,
					EditorEnvironment.getUI_FileName(name));
			if (f == null)
				return;

			canvas.clear();

			if (!f.exists()) {
				// if (needHint)
				// EditorEnvironment.showMessage(this, "文件不存在！", "提示",
				// JOptionPane.WARNING_MESSAGE);
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
					canvas.getPageConfig().setPageSizeMode(pageConfig.getCurPageSizeMode(), pageConfig.width,
							pageConfig.height);
				}
			});

			canvas.getPageConfig()
					.setConfig(multiSelected ? new Config[] { Config.ccAllowSelect, Config.ccAllowMulSelect }
							: new Config[] { Config.ccAllowSelect });

			NodeDescInfo info = EditorEnvironment.getModelNodeDescInfo(name);

			if (info == null)
				memo.setText(canvas.getPageConfig().memo);
			else
				memo.setText(canvas.getPageConfig().memo + " => 关联的工作流[" + info.workflowRelationName + "]中的节点："
						+ info.title + "[" + info.id + "]");

			for (DrawNode tmp : canvas.nodes.values()) {
				UINode node = (UINode) tmp;
				node.invalidRect();
			}

			canvas.repaint();

		} catch (Exception e) {
			e.printStackTrace();
			EditorEnvironment.showMessage(this, e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void select(String[] ids) {
		if (ids == null)
			return;

		List<DrawNode> nodeList = new ArrayList<>();
		for (String id : ids) {
			DrawNode node = canvas.getNode(id);
			if (node != null)
				nodeList.add(node);
		}

		canvas.setSelecteds(nodeList.toArray(new DrawNode[nodeList.size()]));
	}

	boolean isok = false;
	private JPanel panel;
	private JLabel memo;
	private JButton cancelButton;
	private JButton okButton;
	private JButton btnNull;
	boolean multiSelected = false;
	private JToolBar toolBar;

	public static Result showDialog(String uiid) {
		return showDialog(uiid, true);
	}

	public static Result showDialog(String uiid, boolean needOkButton) {
		return showDialog(uiid, null, needOkButton);
	}

	public static Result showDialog(String uiid, String selectControlId, boolean needOkButton) {
		return showDialog(uiid, selectControlId, null, needOkButton);
	}

	@SuppressWarnings("unchecked")
	public static Result showDialog(String uiid, String selectControlId, Class<?> selectClass, boolean needOkButton) {
		Result result = showDialog(uiid,
				selectControlId == null || selectControlId.isEmpty() ? null : new String[] { selectControlId },
				new Class[] { selectClass }, needOkButton, false);
		return result;
	}

	public static class Result{
		public boolean isok = false;
		public DrawNode[] data;
	}
	
	public static Result showDialog(String uiid, String[] selectControlIds, Class<DrawInfo>[] selectClass,
			boolean needOkButton, boolean multiSelected) {
		ControlSelectDialog dialog;
		Result result = new Result();
		try {
			dialog = new ControlSelectDialog();
			if (!needOkButton)
				dialog.toolBar.remove(dialog.okButton);
			dialog.multiSelected = multiSelected;
			dialog.load(uiid);

			if (selectClass != null && selectClass.length > 0) {
				HashMap<String, Class<DrawInfo>> selectClassMap = new HashMap<>();
				for (Class<DrawInfo> class1 : selectClass) {
					if (class1 == null)
						continue;

					selectClassMap.put(class1.getName(), class1);
				}

				if (selectClassMap.size() > 0) {
					dialog.selectClassMap = selectClassMap;
					for (String key : new ArrayList<>(dialog.canvas.nodes.keySet())) {
						UINode node = (UINode) dialog.canvas.nodes.get(key);
						if (node == null)
							continue;

						if (!selectClassMap.containsKey(node.getDrawInfo().getClass().getName())
								&& !(node.getDrawInfo() instanceof DivInfo)) {
							dialog.canvas.remove(key);
						}
					}
				}
				
				dialog.canvas.repaint();
			}

			if (selectControlIds != null && selectControlIds.length > 0)
				dialog.select(selectControlIds);

			dialog.setModal(true);
			dialog.setVisible(true);

			result.isok = dialog.isok;
			List<DrawNode> nodeList = dialog.canvas.getSelecteds();
			result.data = nodeList.size() == 0 ? null : nodeList.toArray(new DrawNode[nodeList.size()]);
			
			dialog.dispose();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			result.isok = false;
			return result;
		}
	}

}
