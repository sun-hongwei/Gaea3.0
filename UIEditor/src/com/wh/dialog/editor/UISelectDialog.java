package com.wh.dialog.editor;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Dimension;
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
import java.util.HashMap;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
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

import com.wh.control.ControlSearchHelp;
import com.wh.control.EditorEnvironment;
import com.wh.control.EditorEnvironment.NodeDescInfo;
import com.wh.draws.DrawNode;
import com.wh.draws.UICanvas;
import com.wh.draws.UINode;
import com.wh.draws.DrawCanvas.Config;
import com.wh.draws.DrawCanvas.ICreateNodeSerializable;
import com.wh.draws.DrawCanvas.IDataSerializable;
import com.wh.draws.DrawCanvas.IInitPage;
import com.wh.draws.DrawCanvas.IOnPageSizeChanged;
import com.wh.draws.DrawCanvas.IScroll;
import com.wh.draws.DrawCanvas.PageConfig;
import com.wh.form.IMainControl;

public class UISelectDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	
	HashMap<String, String> names = new HashMap<>();
	
	UICanvas canvas = new UICanvas();
	private JComboBox<FileInfo> uinames;
	private JLabel label_1;
	private JScrollBar hScrollBar;
	private JScrollBar vScrollBar;
	private JPanel panel_1;
	
	
	
	DrawNode workflowNode;
	HashMap<String, DrawNode> workflowNodes;
	String workflowRelationTitle;
	
	IMainControl mainControl;
	public UISelectDialog(IMainControl mainControl) throws Exception {
		super();
		setFont(new Font("微软雅黑", Font.PLAIN, 12));
		setIconImage(Toolkit.getDefaultToolkit().getImage(UISelectDialog.class.getResource("/image/browser.png")));
		this.mainControl = mainControl;
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 1204, 768);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JToolBar toolBar = new JToolBar();
		toolBar.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		contentPane.add(toolBar, BorderLayout.NORTH);
		
		toolBar.addSeparator(new Dimension(5, 0));
		
		label_1 = new JLabel("   界面：");
		label_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(label_1);
		
		uinames = new JComboBox<FileInfo>();
		uinames.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		uinames.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (uinames.getSelectedItem() != null){
					FileInfo info = (FileInfo)uinames.getSelectedItem();
					load(info.id);
				}
			}
		});
		uinames.setMaximumSize(new Dimension(300, 21));
		toolBar.add(uinames);
		
		addButton = new JButton("新增");
		addButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					
					String uiid = onAdd.onAdd();
					FileInfo selectInfo = null;
					if (uinames.getItemCount() > 0){
						FileInfo fileInfo = uinames.getItemAt(uinames.getItemCount() -1);
						if (fileInfo.title.compareTo("新增") == 0){
							selectInfo = fileInfo;
						};
					}
					
					if (selectInfo == null){
						selectInfo = new FileInfo(null);
						uinames.addItem(selectInfo);
					}
					selectInfo.title = "新增";
					selectInfo.id = uiid;
					uinames.setSelectedIndex(uinames.getItemCount() - 1);
				} catch (Exception e1) {
					e1.printStackTrace();
					EditorEnvironment.showException(e1);
				}
			}
		});
		toolBar.add(addButton);
		
		btnNull = new JButton("null返回");
		btnNull.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		btnNull.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				uinames.setSelectedIndex(-1);
				isok = false;
				setVisible(false);
			}
		});
		toolBar.add(btnNull);

		toolBar.addSeparator();
		JButton button = new JButton("确定");
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (uinames.getSelectedItem() == null){
					EditorEnvironment.showMessage(null, "请先选择一个界面后再试！");
					return;
				}
				isok = true;
				setVisible(false);
			}
		});		
		toolBar.add(button);
		
		JButton button_1 = new JButton("取消");
		button_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isok = false;
				setVisible(false);
			}
		});
		toolBar.add(button_1);
		
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
				if (vScrollBar.getMaximum() > 0){
					adj = vScrollBar;
				}else
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
		
		panel = new JPanel();
		contentPane.add(panel, BorderLayout.SOUTH);
		panel.setLayout(new BorderLayout(0, 0));
		
		memo = new JLabel("New label");
		memo.setFont(new Font("微软雅黑", Font.PLAIN, 14));
		memo.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(memo);

		setLocationRelativeTo(null);
		init();
	}

	class FileInfo{
		public String id = null;
		public String title = null;
		public String memo = null;
		public File file = null;
		public FileInfo(){
			
		}
		public FileInfo(File file) throws Exception{
			if (file != null && file.exists()){
				this.file = file;
				NodeDescInfo info = EditorEnvironment.getNodeDescInfo(file);
				id = info.id;
				title = info.title;
				memo = info.memo;
			}
		}
		
		public String toString(){
			if (title == null || title.isEmpty())
				return id;
			else
				return title;
		}
	}
	
	ControlSearchHelp comboBoxHelp;
	public void init() throws Exception{
		File[] files = EditorEnvironment.getUIFiles();
		if (files == null || files.length == 0)
			return;
		
		FileInfo[] fileinfos = new FileInfo[files.length];
		for (int i = 0; i < fileinfos.length; i++) {
			fileinfos[i] = new FileInfo(files[i]);
		}
		
		uinames.setModel(new DefaultComboBoxModel<>(fileinfos));
		
		if (comboBoxHelp != null)
			uinames.removeKeyListener(comboBoxHelp);
		
		if (uinames.getItemCount() > 0){
			uinames.setSelectedIndex(0);
			comboBoxHelp = new ControlSearchHelp(uinames);
		}
	}
	
	public void load(String name) {
		try {
			File f = EditorEnvironment.getProjectFile(EditorEnvironment.UI_Dir_Name, EditorEnvironment.getUI_FileName(name));
			if (f == null)
				return;
			
			canvas.clear();
			
			if (!f.exists()) {
//				if (needHint)
//					EditorEnvironment.showMessage(this, "文件不存在！", "提示", JOptionPane.WARNING_MESSAGE);
				return;
			}

			canvas.setFile(f);
			canvas.load(new ICreateNodeSerializable() {
				
				@Override
				public DrawNode newDrawNode(JSONObject json) {
					return new	UINode(canvas);
				}
				
				@Override
				public IDataSerializable getUserDataSerializable(DrawNode node) {
					return null;
				}
			}, new IInitPage() {
				
				@Override
				public void onPage(PageConfig pageConfig) {
					canvas.getPageConfig().setPageSizeMode(pageConfig.getCurPageSizeMode(), pageConfig.width, pageConfig.height);
				}
			});
			
			canvas.getPageConfig().setConfig(new Config[]{});
			
			NodeDescInfo info = EditorEnvironment.getModelNodeDescInfo(name);
			
			if (info == null)
				memo.setText(canvas.getPageConfig().memo);
			else
				memo.setText(canvas.getPageConfig().memo + " => 关联的工作流[" + info.workflowRelationName + "]中的节点：" + info.title + "[" + info.id + "]");
			
			for (DrawNode tmp : canvas.nodes.values()) {
				UINode node = (UINode)tmp;
				node.invalidRect();
			}
			
			canvas.repaint();
		} catch (Exception e) {
			e.printStackTrace();
			EditorEnvironment.showMessage(this, e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public static class Result{
		public String id = null;
		public String name = null;
	}
	
	
	boolean isok = false;
	private JPanel panel;
	private JLabel memo;
	private JButton addButton;
	
	public interface IAdd{
		public String onAdd();
	}
	
	IAdd onAdd;
	private JButton btnNull;
	public static Result showDialog(IMainControl mainControl, String id, IAdd onAdd){
		UISelectDialog dialog;
		try {
			dialog = new UISelectDialog(mainControl);
			
			dialog.onAdd = onAdd;
			if (onAdd == null)
				dialog.addButton.setVisible(false);
			
			if (id != null && !id.isEmpty()){
				for (int i = 0; i < dialog.uinames.getItemCount(); i++) {
					FileInfo info = (FileInfo)dialog.uinames.getItemAt(i);
					if (info.id.compareTo(id) == 0){
						dialog.uinames.setSelectedIndex(i);
						break;
					} 
				}
			}else{
				if (dialog.uinames.getItemCount() > 0)
					dialog.uinames.setSelectedIndex(0);
			}
			dialog.setModal(true);
			dialog.setVisible(true);
			
			if (!dialog.isok)
				return null;
			
			Result result = new Result();

			FileInfo info = (FileInfo)dialog.uinames.getSelectedItem();
			if (info != null){
				result.id = info.id;
				result.name = info.title;
			}
			if (info.title.compareTo("新增") == 0){
				result.name = result.id;
				File f = EditorEnvironment.getProjectFile(EditorEnvironment.UI_Dir_Name, EditorEnvironment.getUI_FileName(result.id));
				if (!f.exists()){
					UICanvas canvas = new UICanvas();
					canvas.setFile(f);
					canvas.save();
				}
			}
			dialog.dispose();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
}

