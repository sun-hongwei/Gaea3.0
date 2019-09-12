package com.wh.dialog.input;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.Font;

public class MulitTextInput extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			MulitTextInput dialog = new MulitTextInput();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	boolean isok = false;
	private JTextArea textArea;
	/**
	 * Create the dialog.
	 */
	public MulitTextInput() {
		setFont(new Font("微软雅黑", Font.PLAIN, 12));
		setTitle("请输入文本");
		setModalityType(ModalityType.APPLICATION_MODAL);
		setModal(true);
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("确定");
				okButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						isok = true;
						setVisible(false);
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("取消");
				cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						isok = false;
						setVisible(false);
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		{
			JScrollPane scrollPane = new JScrollPane();
			getContentPane().add(scrollPane, BorderLayout.CENTER);
			{
				textArea = new JTextArea();
				textArea.setFont(new Font("微软雅黑", Font.PLAIN, 12));
				scrollPane.setViewportView(textArea);
			}
		}
		
		setLocationRelativeTo(null);
	}

	public static String showDialog(String title, String defaultValue){
		MulitTextInput dialog = new MulitTextInput();
		if (title != null && !title.isEmpty())
			dialog.setTitle(title);
		if (defaultValue != null && !defaultValue.isEmpty())
			dialog.textArea.setText(defaultValue);
		
		dialog.setVisible(true);
		
		String result = defaultValue;
		if (dialog.isok){
			result = dialog.textArea.getText();
		}
		dialog.dispose();
		
		return result;
	}
}
