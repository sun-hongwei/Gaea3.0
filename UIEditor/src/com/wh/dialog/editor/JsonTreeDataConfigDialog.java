package com.wh.dialog.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import org.json.JSONArray;

import com.wh.control.EditorEnvironment;
import com.wh.dialog.editor.JsonTreeDataEditor.IChange;

import java.awt.Font;

public class JsonTreeDataConfigDialog extends JDialog {
	boolean isEdit = false;
	private static final long serialVersionUID = 1L;
	private final JsonTreeDataEditor contentPanel = new JsonTreeDataEditor(new IChange() {
		
		@Override
		public void onChange(Object data) {
			isEdit = true;
		}
	});

	/**
	 * Create the dialog.
	 */
	
	boolean isok = false;
	void docancel(){
		if (isEdit){
			if (EditorEnvironment.showConfirmDialog("关闭将丢失所有未保存的工作，是否继续？", "退出", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
				return;
		}
		isok = false;
		setVisible(false);
	}
	public JsonTreeDataConfigDialog() {
		setFont(new Font("微软雅黑", Font.PLAIN, 12));
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter(){
			
			@Override
			public void windowClosing(WindowEvent e) {
				docancel();
			}
		});
		setIconImage(Toolkit.getDefaultToolkit().getImage(JsonTreeDataConfigDialog.class.getResource("/image/browser.png")));
		setTitle("编辑");
		setModal(true);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setBounds(100, 100, 800, 700);
		getContentPane().setLayout(new BorderLayout());
		
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		contentPanel.add(panel, BorderLayout.SOUTH);
		
		JButton btnNewButton = new JButton("确定");
		btnNewButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isok = true;
				JsonTreeDataConfigDialog.this.setVisible(false);
			}
		});
		
		JButton button_1 = new JButton("重置");
		button_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				contentPanel.reset();
			}
		});
		panel.add(button_1);
		
		JSeparator separator = new JSeparator();
		separator.setPreferredSize(new Dimension(0, 20));
		separator.setOrientation(SwingConstants.VERTICAL);
		panel.add(separator);
		panel.add(btnNewButton);
		
		JButton button = new JButton("取消");
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				docancel();
			}
		});
		panel.add(button);
		
		JSeparator separator_1 = new JSeparator();
		panel.add(separator_1);
		
		JButton button_2 = new JButton("复制");
		button_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				contentPanel.copy();
				
			}
		});
		panel.add(button_2);
		
		JButton button_3 = new JButton("粘贴");
		button_3.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				contentPanel.paste();
			}
		});
		panel.add(button_3);
		setLocationRelativeTo(null);
	}

	public static JSONArray show(JSONArray obj){
		JsonTreeDataConfigDialog config = new JsonTreeDataConfigDialog();
		config.contentPanel.setValue(obj);;
		config.setVisible(true);
		JSONArray result = config.contentPanel.getResult();
		
		config.dispose();
		if (!config.isok)
			return obj;
		
		return result;
	} 
}
