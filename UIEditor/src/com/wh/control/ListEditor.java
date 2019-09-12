package com.wh.control;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.wh.system.tools.BytesHelp;
import com.wh.system.tools.Tools;

public class ListEditor extends JPanel {
	private static final long serialVersionUID = 1L;
	private JLabel label;
	private JList<String> list;

	File path;
	
	public void setPath(File path){
		this.path = path;
		if (!path.exists())
			path.mkdirs();
	}
	
	public void reset(){
		path = null;
		refresh();
	}
	
	public void refresh() {
		list.setModel(new DefaultListModel<String>());
		if (path == null || !path.exists())
			return;
		DefaultListModel<String> model = (DefaultListModel<String>)list.getModel();
		for (File file : path.listFiles()) {
			String name = file.getName();
			if (file.isDirectory() || name.compareTo(".") == 0 || name.compareTo("..") == 0)
				continue;
			
			if (file.isFile()){
				model.insertElementAt(name, model.size());
			}
		}
	}
	/**
	 * Create the panel.
	 */
	
	String curOpenPath = null;
	public ListEditor() {
		setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		add(panel, BorderLayout.NORTH);
		
		JButton button = new JButton("刷新");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refresh();
			}
		});
		panel.add(button);
		
		JButton button_1 = new JButton("添加");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File[] files = Tools.selectOpenFiles(null, curOpenPath, null, null);
				if (files != null){
					for (File file : files) {
						curOpenPath = file.getParent();
						FileInputStream inputStream = null;
						FileOutputStream outputStream = null;
						try {
							inputStream = new FileInputStream(file);
							outputStream = new FileOutputStream(new File(path, file.getName()));
							BytesHelp.copyStream(inputStream, outputStream);
							refresh();
						} catch (Exception e1) {
							JOptionPane.showMessageDialog(null, e1.getMessage() == null ? e1.getClass().getName() : e1.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
							break;
						}finally{
							try {
								if (inputStream != null)
									inputStream.close();
								if (outputStream != null)
									outputStream.close();
							} catch (IOException e1) {}
						}
					}
				}
			}
		});
		panel.add(button_1);
		
		JButton button_2 = new JButton("删除");
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (JOptionPane.showConfirmDialog(null, "是否删除选定的文件？", "删除", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
					return;
				
				int index = list.getSelectedIndex();
				if (index == -1)
					return;
				String name = list.getSelectedValue();
				File file = new File(path, name);
				if (file.exists()){
					if (file.delete())
						refresh();
				}
				
			}
		});
		panel.add(button_2);
		
		list = new JList<String>();
		add(list, BorderLayout.CENTER);
		
		label = new JLabel("");
		label.setFont(new Font("微软雅黑", Font.PLAIN, 14));
		label.setHorizontalAlignment(SwingConstants.CENTER);
		add(label, BorderLayout.SOUTH);

	}

}
