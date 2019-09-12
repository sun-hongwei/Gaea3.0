package com.wh.dialog.selector;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

import com.wh.control.EditorEnvironment;

public class RadioSelector extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JRadioButton check1;
	private JRadioButton check2;
	private JRadioButton check3;

	public enum Result{
		rt1, rt2, rt3, rt4, rtNone
	}
	
	Result result = Result.rtNone;
	private JRadioButton check4;
	private JPanel panel;
	private JLabel label;
	private JSpinner countInput;
	/**
	 * Create the dialog.
	 */
	public RadioSelector() {
		setFont(new Font("微软雅黑", Font.PLAIN, 12));
		setTitle("选择项目");
		setIconImage(Toolkit.getDefaultToolkit().getImage(RadioSelector.class.getResource("/image/browser.png")));
		setBounds(100, 100, 450, 305);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		check1 = new JRadioButton("New radio button");
		check1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		check1.setVisible(false);
		check1.setBounds(155, 27, 143, 23);
		check1.setAlignmentX(Component.CENTER_ALIGNMENT);
		contentPanel.add(check1);
		check2 = new JRadioButton("New radio button");
		check2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		check2.setVisible(false);
		check2.setBounds(155, 77, 143, 23);
		check2.setAlignmentX(Component.CENTER_ALIGNMENT);
		contentPanel.add(check2);
		check3 = new JRadioButton("New radio button");
		check3.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		check3.setVisible(false);
		check3.setBounds(155, 127, 143, 23);
		check3.setAlignmentX(Component.CENTER_ALIGNMENT);
		contentPanel.add(check3);
		check4 = new JRadioButton("New radio button");
		check4.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		check4.setVisible(false);
		check4.setBounds(155, 177, 143, 23);
		check4.setAlignmentX(Component.CENTER_ALIGNMENT);
		contentPanel.add(check4);
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		JButton okButton = new JButton("确定");
		okButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (check1.isSelected())
					result = Result.rt1;
				else if (check2.isSelected())
					result = Result.rt2;
				else if (check3.isSelected())
					result = Result.rt3;
				else if (check4.isSelected())
					result = Result.rt4;
				else {
					result = Result.rtNone;
				}
				if (result == Result.rtNone){
					EditorEnvironment.showMessage(null, "请先选择一个项目！");
					return;
				}
				
				setVisible(false);
			}
		});
		
		panel = new JPanel();
		buttonPane.add(panel);
		
		label = new JLabel("数量");
		panel.add(label);
		
		countInput = new JSpinner();
		countInput.setModel(new SpinnerNumberModel(new Integer(1), new Integer(1), null, new Integer(1)));
		countInput.setPreferredSize(new Dimension(80, 22));
		panel.add(countInput);
		okButton.setActionCommand("OK");
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);
		JButton cancelButton = new JButton("取消");
		cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		cancelButton.setActionCommand("Cancel");
		buttonPane.add(cancelButton);
	
		ButtonGroup checks = new ButtonGroup();
		checks.add(check1);
		checks.add(check2);
		checks.add(check3);
		checks.add(check4);
		
		setLocationRelativeTo(null);
	}

	public static Result showDialog(String[] titles){
		return showDialog(titles, null);
	}
	
	public static Result showDialog(String[] titles, AtomicInteger count){
		RadioSelector dialog = new RadioSelector();
		int index = 0;
		for (String string : titles) {
			switch (index++) {
			case 0:
				dialog.check1.setText(string);
				dialog.check1.setVisible(true);
				break;
			case 1:
				dialog.check2.setText(string);
				dialog.check2.setVisible(true);
				break;
			case 2:
				dialog.check3.setText(string);
				dialog.check3.setVisible(true);
				break;
			case 3:
				dialog.check4.setText(string);
				dialog.check4.setVisible(true);
				break;
			}
		}
		
		int div = (4- index) * 40;
		
		Rectangle r = dialog.getBounds();
		dialog.setBounds(r.x, r.y, r.width, r.height - div);
		dialog.panel.setVisible(count != null);
		dialog.setModal(true);
		dialog.setVisible(true);
		Result result = dialog.result;
		if (count != null)
			count.set((int)dialog.countInput.getValue());
		dialog.dispose();
		
		return result;
	}
}
