package com.wh.dialog.input;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.json.JSONArray;
import org.json.JSONException;

import com.wh.control.EditorEnvironment;
import com.wh.dialog.editor.JsonEditorDialog;
import com.wh.form.IMainControl;
import com.wh.system.tools.JsonHelp;

public class ListInput extends JDialog {

	private static final long serialVersionUID = 5345568941939595794L;

	private final JPanel contentPanel = new JPanel();

	private JList<Object> list;

	enum ModalResult{
		mrOk, mrCancel
	}
	
	boolean needJson = false;
	ModalResult modalResult = ModalResult.mrCancel;
	
	/**
	 * Create the dialog.
	 */
	IMainControl mainControl;
	public ListInput(IMainControl mainControl) {
		this.mainControl = mainControl;
		setFont(new Font("微软雅黑", Font.PLAIN, 12));
		setIconImage(Toolkit.getDefaultToolkit().getImage(ListInput.class.getResource("/image/browser.png")));
		setModal(true);
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		setBounds(100, 100, 450, 431);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		list = new JList<Object>(new DefaultListModel<Object>());
		list.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		contentPanel.add(list);
		JPanel buttonPane = new JPanel();
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		buttonPane.setLayout(new BorderLayout(0, 0));
		JPanel panel = new JPanel();
		buttonPane.add(panel, BorderLayout.WEST);
		JButton button = new JButton("添加");
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String input = null;
				Object value = list.getSelectedValue();
				if (needJson){
					Object jsonvalue = JsonEditorDialog.show(mainControl, value);
					if (jsonvalue != null)
						input = jsonvalue.toString();
				}else
					input = EditorEnvironment.showInputDialog("请输入条目信息：", value);
				if (input == null || input.isEmpty())
					return;
				
				DefaultListModel<Object> model = (DefaultListModel<Object>)list.getModel();
				if (list.getSelectedIndex() != -1){
					model.insertElementAt(input, list.getSelectedIndex());
				}else
					model.addElement(input);
			}
		});
		panel.add(button);
		JButton button1 = new JButton("删除");
		button1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button1.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (list.getSelectedIndex() == -1)
					return;
					
				if (EditorEnvironment.showConfirmDialog("是否删除选定的条目？", "删除", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
					return;
				
				DefaultListModel<Object> model = (DefaultListModel<Object>)list.getModel();
				model.removeRange(list.getSelectedIndex(), list.getLeadSelectionIndex());
				list.updateUI();
				
			}
		});
		
		JButton button_1 = new JButton("编辑");
		button_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (list.getSelectedIndex() == -1)
					return;
					
				DefaultListModel<Object> model = (DefaultListModel<Object>)list.getModel();
				
				String input = null;
				Object value = list.getSelectedValue();
				if (needJson){
					Object jsonvalue = JsonEditorDialog.show(mainControl, value);
					if (jsonvalue != null)
						input = jsonvalue.toString();
				}else
					input = EditorEnvironment.showInputDialog("请输入条目信息：", value);
				if (input == null || input.isEmpty())
					return;
				
				int index = list.getSelectedIndex();
				model.remove(index);
				model.insertElementAt(input, index);
			}
		});
		panel.add(button_1);
		panel.add(button1);
		
		JPanel panel1 = new JPanel();
		buttonPane.add(panel1, BorderLayout.EAST);
		JButton okButton = new JButton("确定");
		okButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel1.add(okButton);
		
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				modalResult = ModalResult.mrOk;
				setVisible(false);
			}
		});
		okButton.setActionCommand("OK");
		
		JButton cancelButton = new JButton("取消");
		cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel1.add(cancelButton);
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				modalResult = ModalResult.mrCancel;
				setVisible(false);
			}
		});
		cancelButton.setActionCommand("Cancel");
					getRootPane().setDefaultButton(cancelButton);
		
		setLocationRelativeTo(null);
		
	}

	@SuppressWarnings("unchecked")
	public static JSONArray show(IMainControl mainControl, String title, JSONArray json, boolean needjson) throws JSONException{
		List<Object> datas = new ArrayList<>();
		if (json != null){
			for(int i = 0; i < json.length(); i++){
				datas.add(json.get(i));
			}
		}
		datas = (List<Object>)show(mainControl, title, datas, needjson);
		JSONArray result = new JSONArray();
		for (Object object : datas) {
			if (object instanceof String){
				object = JsonHelp.parseJson(object.toString());
			}
			result.put(object);
		}
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public static List<? extends Object> show(IMainControl mainControl, String title, List<? extends Object> datas, boolean needJson){
	
		ListInput dialog = new ListInput(mainControl);
		DefaultListModel<Object> model = (DefaultListModel<Object>)dialog.list.getModel();
		if (datas != null){
			for (int i = 0; i < datas.size(); i++) {
				model.insertElementAt(datas.get(i), model.getSize());
			}
		}
		dialog.setModal(true);
		dialog.setTitle(title);
		dialog.list.setSelectedIndex(0);
		dialog.needJson = needJson;
		dialog.setVisible(true);

		List<Object> result = (List<Object>) datas;
		if (dialog.modalResult == ModalResult.mrOk){
			List<Object> lst = new ArrayList<>();
			model = (DefaultListModel<Object>)dialog.list.getModel();
			for (int i = 0; i < model.getSize(); i++) {
				lst.add(model.getElementAt(i));
			}
			result = lst;
		}
		dialog.dispose();
		
		return result;
	}
}
