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

import com.wh.control.ControlSearchHelp;
import com.wh.control.EditorEnvironment;
import com.wh.draws.AppWorkflowCanvas;
import com.wh.draws.DrawNode;
import com.wh.draws.DrawCanvas.Config;
import com.wh.draws.DrawCanvas.IInitPage;
import com.wh.draws.DrawCanvas.IOnPageSizeChanged;
import com.wh.draws.DrawCanvas.IScroll;
import com.wh.draws.DrawCanvas.PageConfig;
import com.wh.form.IMainControl;
import com.wh.system.tools.FileHelp;

public class CodeFlowSelectDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	
	HashMap<String, String> names = new HashMap<>();
	
	AppWorkflowCanvas canvas = new AppWorkflowCanvas();
	private JComboBox<FileInfo> appworkflownames;
	private JLabel label_1;
	private JScrollBar hScrollBar;
	private JScrollBar vScrollBar;
	private JPanel panel_1;
	
	
	
	DrawNode workflowNode;
	HashMap<String, DrawNode> workflowNodes;
	String workflowRelationTitle;
	
	IMainControl mainControl;
	public CodeFlowSelectDialog(IMainControl mainControl) throws Exception {
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
		
		label_1 = new JLabel("程序流程：");
		label_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(label_1);
		
		appworkflownames = new JComboBox<FileInfo>();
		appworkflownames.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		appworkflownames.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (appworkflownames.getSelectedItem() != null){
					FileInfo info = (FileInfo)appworkflownames.getSelectedItem();
					load(info.id);
				}
			}
		});
		appworkflownames.setMaximumSize(new Dimension(300, 21));
		toolBar.add(appworkflownames);
		
		toolBar.addSeparator(new Dimension(5, 0));
		
		toolBar.addSeparator(new Dimension(5, 0));
		
		toolBar.addSeparator(new Dimension(5, 0));
		toolBar.addSeparator();
		JButton button = new JButton("确定");
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (appworkflownames.getSelectedItem() == null){
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
		public String id;
		public String title;
		public String memo;
		public File file;
		public FileInfo(File file) throws Exception{
			if (file != null && file.exists()){
				this.file = file;
				id = FileHelp.removeExt(file.getName());
				title = id;
				memo = id;
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
		File[] files = EditorEnvironment.getAppWorkflowFiles();
		if (files == null || files.length == 0)
			return;
		
		FileInfo[] fileinfos = new FileInfo[files.length];
		for (int i = 0; i < fileinfos.length; i++) {
			fileinfos[i] = new FileInfo(files[i]);
		}
		
		appworkflownames.setModel(new DefaultComboBoxModel<>(fileinfos));
		
		if (comboBoxHelp != null)
			appworkflownames.removeKeyListener(comboBoxHelp);
		
		if (appworkflownames.getItemCount() > 0){
			appworkflownames.setSelectedIndex(0);
			comboBoxHelp = new ControlSearchHelp(appworkflownames);
		}
	}
	
	public void load(String name) {
		try {
			File f = EditorEnvironment.getProjectFile(EditorEnvironment.AppWorkflow_Dir_Name, EditorEnvironment.getAppWorkflow_FileName(name));
			if (f == null)
				return;
			
			canvas.clear();
			
			if (!f.exists()) {
//				if (needHint)
//					EditorEnvironment.showMessage(this, "文件不存在！", "提示", JOptionPane.WARNING_MESSAGE);
				return;
			}

			canvas.setFile(f);
			canvas.load(null, new IInitPage() {
				
				@Override
				public void onPage(PageConfig pageConfig) {
					canvas.getPageConfig().setPageSizeMode(pageConfig.getCurPageSizeMode(), pageConfig.width, pageConfig.height);
				}
			});
			
			canvas.getPageConfig().setConfig(new Config[]{});
			
			memo.setText(canvas.getPageConfig().memo);
			
			canvas.repaint();
		} catch (Exception e) {
			e.printStackTrace();
			EditorEnvironment.showMessage(this, e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public static class Result{
		public String id;
		public String name;
	}
	
	
	boolean isok = false;
	private JPanel panel;
	private JLabel memo;
	
	public static Result showDialog(IMainControl mainControl, String id){
		CodeFlowSelectDialog dialog;
		try {
			dialog = new CodeFlowSelectDialog(mainControl);
			
			if (id != null && !id.isEmpty()){
				for (int i = 0; i < dialog.appworkflownames.getItemCount(); i++) {
					FileInfo info = (FileInfo)dialog.appworkflownames.getItemAt(i);
					if (info.id.compareTo(id) == 0){
						dialog.appworkflownames.setSelectedIndex(i);
						break;
					} 
				}
			}else{
				if (dialog.appworkflownames.getItemCount() > 0)
					dialog.appworkflownames.setSelectedIndex(0);
			}
			dialog.setModal(true);
			dialog.setVisible(true);
			
			Result result = new Result();

			if (!dialog.isok)
				return result;
			
			
			FileInfo info = (FileInfo)dialog.appworkflownames.getSelectedItem();
			result.id = info.id;
			result.name = info.title;
			
			dialog.dispose();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
}

