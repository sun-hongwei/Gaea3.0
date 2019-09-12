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

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.SwingConstants;

import com.wh.control.EditorEnvironment;
import com.wh.draws.DrawNode;
import com.wh.draws.FlowCanvas;
import com.wh.draws.DrawCanvas.ChangeType;
import com.wh.draws.DrawCanvas.IInitPage;
import com.wh.draws.DrawCanvas.INode;
import com.wh.draws.DrawCanvas.IOnPageSizeChanged;
import com.wh.draws.DrawCanvas.IScroll;
import com.wh.draws.DrawCanvas.PageConfig;
import com.wh.draws.DrawCanvas.PageSizeMode;
import com.wh.draws.FlowNode.ChildFlowNode;
import com.wh.form.IMainControl;
import com.wh.system.tools.FileHelp;

public class WorkflowSelectDialog extends JDialog{

	private static final long serialVersionUID = 1L;

	private boolean allowNull = false;
	class DrawNodeInfo{
		public String pageName;
		
	}

	FlowCanvas canvas;
	private JScrollBar vScrollBar;
	private JScrollBar hScrollBar;
	protected void reset() {
		canvas.nodes.clear();
		canvas.repaint();
	}
	
	protected void ok(){
		result = ModalResult.mrOk;
		setVisible(false);;	
	}
	
	public IMainControl mainControl;
	enum  ModalResult{
		mrOk, mrCancel
	}
	
	class TextValue{
		public String text;
		public String value;
		public TextValue(String value, String text){
			this.text = text;
			this.value = value;
		}
		
		public String toString(){
			return text;
		}
	}
	public void initCombobox(){
		comboBox.removeAllItems();
		HashMap<String, String> values = EditorEnvironment.getFlowRelationNames(); 
		for (String v : values.keySet()) {
			comboBox.addItem(new TextValue(v, values.get(v)));
		}
		
		if (comboBox.getItemCount() > 0)
			comboBox.setSelectedIndex(0);
	}
	
	public WorkflowSelectDialog(IMainControl mainControl) {
		super();
		setFont(new Font("微软雅黑", Font.PLAIN, 12));
		setTitle("工作流节点选择");
		setIconImage(Toolkit.getDefaultToolkit().getImage(ModelflowSelectDialog.class.getResource("/image/browser.png")));
		this.mainControl = mainControl;
		setBounds(100, 100, 1198, 906);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setPreferredSize(new Dimension(200, 10));
		panel.setLayout(new BorderLayout(0, 0));
		
		JPanel workflowContaint = new JPanel();
		panel.add(workflowContaint, BorderLayout.CENTER);
		workflowContaint.setLayout(new BorderLayout(0, 0));
		
		canvas = new FlowCanvas();
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
		canvas.nodeEvent = new INode() {
			
			@Override
			public void onChange(DrawNode[] nodes, ChangeType ct) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void DoubleClick(DrawNode node) {
				if (node instanceof ChildFlowNode){
					File subWorkflowFile = EditorEnvironment.getChildModelRelationFile(node.id, false);
					reload(subWorkflowFile);
				}
			}
			
			@Override
			public void Click(DrawNode node) {
				// TODO Auto-generated method stub
				
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
		
		panel_1 = new JPanel();
		panel.add(panel_1, BorderLayout.SOUTH);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_2 = new JPanel();
		panel_1.add(panel_2, BorderLayout.EAST);
		
		JButton button = new JButton("取消");
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				result = ModalResult.mrCancel;
				setVisible(false);
			}
		});
		
		JButton button_1 = new JButton("确定");
		button_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ok();
			}
		});
		
		JButton btnNull = new JButton("null返回");
		btnNull.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!allowNull){
					EditorEnvironment.showMessage("不允许选择null！");
					return;
				}
				
				result = ModalResult.mrOk;
				canvas.setSelected(null);
				setVisible(false);
			}
		});
		panel_2.add(btnNull);
		panel_2.add(button_1);
		panel_2.add(button);
		
		memo = new JLabel("New label");
		memo.setFont(new Font("微软雅黑", Font.PLAIN, 14));
		memo.setHorizontalAlignment(SwingConstants.CENTER);
		panel_1.add(memo, BorderLayout.CENTER);
		
		JPanel panel_3 = new JPanel();
		getContentPane().add(panel_3, BorderLayout.NORTH);
		panel_3.setLayout(new BorderLayout(0, 0));
		
		title = new JLabel("New label");
		title.setFont(new Font("微软雅黑", Font.PLAIN, 16));
		title.setHorizontalAlignment(SwingConstants.CENTER);
		panel_3.add(title, BorderLayout.CENTER);
		
		JPanel panel_4 = new JPanel();
		panel_3.add(panel_4, BorderLayout.WEST);
		
		JLabel lbls = new JLabel("流程(s)");
		panel_4.add(lbls);
		
		comboBox = new JComboBox<>();
		comboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				canvas.clear();
				if (comboBox.getSelectedIndex() != -1){
					reload(((TextValue)comboBox.getSelectedItem()).value);
				}
			}
		});
		panel_4.add(comboBox);
		
		JButton button_2 = new JButton("刷新");
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				initCombobox();
			}
		});
		panel_4.add(button_2);
		
		setLocationRelativeTo(null);
		
		initCombobox();
	}

	File curWorkflowRelationFile;
	public void reload(String flowname){
		File file = EditorEnvironment.getFlowRelationFile(flowname);
		reload(file);
	}
	
	public void reload(File file){
		curWorkflowRelationFile = file;
		canvas.setFile(curWorkflowRelationFile);
		try {
			canvas.load(new EditorEnvironment.WorkflowDeserializable(), new IInitPage() {
				@Override
				public void onPage(PageConfig pageConfig) {
					canvas.getPageConfig().setPageSizeMode();
					title.setText(canvas.getPageConfig().title);
					memo.setText(canvas.getPageConfig().memo);
				}
			});
			
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}
	
	ModalResult result = ModalResult.mrCancel;
	private JPanel panel_1;
	private JLabel title;
	private JLabel memo;
	private JComboBox<TextValue> comboBox;
	public static class Result{
		public String id = null;
		public String title = null;
		public String name = null;
	}
	
	public static String[] showDialog(IMainControl mainControl, String flowRelationName){
		return showDialog(mainControl, flowRelationName, false);
	}
	
	public static String[] showDialog(IMainControl mainControl, String flowRelationName, boolean allowNull){
		WorkflowSelectDialog editor = new WorkflowSelectDialog(mainControl);
		if (flowRelationName != null && !flowRelationName.isEmpty())
			editor.comboBox.setSelectedItem(flowRelationName);
		
		editor.allowNull = allowNull;
		editor.setModal(true);
		editor.setVisible(true);
		String[] result = null;
		if (editor.result == ModalResult.mrOk){
			result = new String[]{FileHelp.removeExt(editor.canvas.getFile().getName()), editor.canvas.getPageConfig().title};
		}
		editor.dispose();
		return result;
	}	
}

