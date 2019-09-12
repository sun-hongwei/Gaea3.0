package com.wh.dialog.help;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import com.wh.global.Defines;

public class HelpDialog extends JDialog {

	public static final String VERSION = "v3.0";
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();

	public HelpDialog() {
		setTitle("关于" + Defines.AppTitle);
		setIconImage(Toolkit.getDefaultToolkit().getImage(HelpDialog.class.getResource("/image/browser.png")));
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);

		JLabel lblNewLabel = new JLabel(Defines.AppTitle);
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setFont(new Font("微软雅黑", Font.PLAIN, 48));
		lblNewLabel.setBounds(15, 12, 144, 59);
		contentPanel.add(lblNewLabel);

		JLabel lblNewLabel_1 = new JLabel("版权所有©2019 王岩");
		lblNewLabel_1.setFont(new Font("微软雅黑", Font.PLAIN, 16));
		lblNewLabel_1.setBounds(210, 138, 188, 15);
		contentPanel.add(lblNewLabel_1);

		JLabel label = new JLabel("作者：王岩");
		label.setFont(new Font("微软雅黑", Font.PLAIN, 16));
		label.setBounds(210, 108, 122, 15);
		contentPanel.add(label);

		JLabel lblv = new JLabel("版本：" + VERSION);
		lblv.setFont(new Font("微软雅黑", Font.PLAIN, 16));
		lblv.setBounds(174, 47, 122, 15);
		contentPanel.add(lblv);

		JLabel lblEmail = new JLabel("邮箱：HHCWY@163.COM");
		lblEmail.setFont(new Font("微软雅黑", Font.PLAIN, 16));
		lblEmail.setBounds(210, 164, 203, 15);
		contentPanel.add(lblEmail);

		JLabel label_1 = new JLabel("<html><a href='void()'>使用协议</a></html>");
		label_1.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				try {
					Runtime.getRuntime().exec("cmd.exe /c start " + "http://www.google.com");
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});

		// 上面的Runtime语句可用此句代替Runtime.getRuntime().exec("explorer+
		// "http://www.google.com");
		label_1.setHorizontalAlignment(SwingConstants.CENTER);
		label_1.setFont(new Font("微软雅黑", Font.PLAIN, 16));
		label_1.setBounds(15, 160, 81, 21);
		contentPanel.add(label_1);
		JPanel buttonPane = new JPanel();
		buttonPane.setBorder(null);
		buttonPane.setPreferredSize(new Dimension(10, 50));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		JButton cancelButton = new JButton("确定");
		cancelButton.setBounds(182, 15, 57, 25);
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		buttonPane.setLayout(null);
		cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		cancelButton.setActionCommand("Cancel");
		buttonPane.add(cancelButton);
		setLocationRelativeTo(null);
	}

	public static void showDialog() {
		HelpDialog helpDialog = new HelpDialog();
		helpDialog.setModal(true);
		helpDialog.setVisible(true);
	}
}
