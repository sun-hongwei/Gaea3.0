package com.wh.dialog.selector;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import com.wh.control.EditorEnvironment;
import com.wh.control.modelsearch.ModelSearchView;

public class ListSelector extends JDialog {

	private static final long serialVersionUID = 5345568941939595794L;
	private final JPanel contentPanel = new JPanel();

	private JList<Object> list;

	enum ModalResult{
		mrOk, mrCancel
	}
	
	ModalResult modalResult = ModalResult.mrCancel;
	
	/**
	 * Create the dialog.
	 */
	public ListSelector() {
		setFont(new Font("微软雅黑", Font.PLAIN, 12));
		setIconImage(Toolkit.getDefaultToolkit().getImage(ListSelector.class.getResource("/image/browser.png")));
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		setBounds(100, 100, 450, 431);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(null);
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JScrollPane scrollPane = new JScrollPane();
			contentPanel.add(scrollPane, BorderLayout.CENTER);
			{
				list = new ModelSearchView.ListModelSearchView<>();
				list.setModel(new DefaultListModel<Object>());
				scrollPane.setViewportView(list);
				list.setFont(new Font("微软雅黑", Font.PLAIN, 12));
				list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("确定");
				okButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if (list.getSelectedValue() == null){
							EditorEnvironment.showMessage(null, "请先选择一个项目！", "提示", JOptionPane.WARNING_MESSAGE);
							return;
						}
						modalResult = ModalResult.mrOk;
						setVisible(false);
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
			}
			{
				JButton cancelButton = new JButton("取消");
				cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						modalResult = ModalResult.mrCancel;
						setVisible(false);
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
				getRootPane().setDefaultButton(cancelButton);
			}
		}
		
		setLocationRelativeTo(null);
	}

	public static Object show(String title, List<? extends Object> datas){
		if (datas == null || datas.size() == 0)
			return null;
		
		ListSelector dialog = new ListSelector();
		DefaultListModel<Object> model = (DefaultListModel<Object>)dialog.list.getModel();
		for (int i = 0; i < datas.size(); i++) {
			model.insertElementAt(datas.get(i), model.getSize());
		}
		dialog.setModal(true);
		dialog.setTitle(title);
		dialog.list.setSelectedIndex(0);
		dialog.setVisible(true);

		Object result = null;
		if (dialog.modalResult == ModalResult.mrOk){
			result = dialog.list.getSelectedValue();
		}
		dialog.dispose();
		
		return result;
	}
}
