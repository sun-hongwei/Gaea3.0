package com.wh.dialog.selector;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import com.wh.control.CheckBoxList;
import com.wh.control.EditorEnvironment;

public class CheckSelector<T> extends JDialog {

	private static final long serialVersionUID = 5345568941939595794L;
	private final JPanel contentPanel = new JPanel();

	private CheckBoxList<T> list;


	enum ModalResult {
		mrOk, mrCancel
	}

	ModalResult modalResult = ModalResult.mrCancel;

	/**
	 * Create the dialog.
	 */
	public CheckSelector() {
		setFont(new Font("微软雅黑", Font.PLAIN, 12));
		setIconImage(Toolkit.getDefaultToolkit().getImage(ListSelector.class.getResource("/image/browser.png")));
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		setBounds(100, 100, 450, 431);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(null);
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		JScrollPane scrollPane = new JScrollPane();
		contentPanel.add(scrollPane, BorderLayout.CENTER);
		list = new CheckBoxList<>(new DefaultListModel<>());
		scrollPane.setViewportView(list);
		list.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		JButton okButton = new JButton("确定");
		okButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (list.getChecks().size() == 0) {
					EditorEnvironment.showMessage(null, "请先选择一个项目！", "提示", JOptionPane.WARNING_MESSAGE);
					return;
				}
				modalResult = ModalResult.mrOk;
				setVisible(false);
			}
		});
		okButton.setActionCommand("OK");
		buttonPane.add(okButton);
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
		setLocationRelativeTo(null);
	}

	public static <T> List<T> show(String title, List<T> datas, List<Integer> selects) {
		int[] selectIndexs = new int[selects.size()];
		for (int i = 0; i < selectIndexs.length; i++) {
			selectIndexs[i] = selects.get(i);
		}
		
		return show(title, datas, selectIndexs);
	}
	
	public static <T> List<T> show(String title, List<T> datas, int[] selects) {
		if (datas == null || datas.size() == 0)
			return null;
		
		CheckSelector<T> dialog = new CheckSelector<>();
		DefaultListModel<T> model = new DefaultListModel<>();
		dialog.list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		for (int i = 0; i < datas.size(); i++) {
			model.insertElementAt(datas.get(i), model.getSize());
		}
		
		dialog.list.setChecks(selects);

		dialog.list.setModel(model);
		
		dialog.setModal(true);
		dialog.setTitle(title);
		dialog.setVisible(true);

		List<T> result = new ArrayList<>();
		if (dialog.modalResult == ModalResult.mrOk) {
			for (int index : dialog.list.getChecks()) {
				result.add(model.get(index));
				
			}
		}
		dialog.dispose();

		return result;
	}
}
