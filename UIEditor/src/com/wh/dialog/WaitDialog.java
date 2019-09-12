package com.wh.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.jdesktop.swingx.JXBusyLabel;
import java.awt.Toolkit;
import java.awt.Font;

public class WaitDialog extends JDialog {
	private static final long serialVersionUID = 8201042799428050820L;
	private final JPanel contentPanel = new JPanel();
	private JLabel hint_1;
	private JXBusyLabel icon;
	private JButton cancelButton;
	private JPanel panel;
	private JPanel panel_1;
	private JPanel panel_2;
	private JPanel panel_4;

	/**
	 * Create the dialog.
	 */
	protected WaitDialog(IProcess iProcess) {
		setFont(new Font("微软雅黑", Font.PLAIN, 13));
		setIconImage(Toolkit.getDefaultToolkit().getImage(WaitDialog.class.getResource("/image/browser.png")));
		this.iProcess = iProcess;
		setBounds(100, 100, 355, 148);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			hint_1 = new JLabel("New label");
			hint_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
			contentPanel.add(hint_1, BorderLayout.CENTER);
		}
		{
			JPanel buttonPane = new JPanel();
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.PAGE_AXIS));
			{
				panel_2 = new JPanel();
				panel_2.setPreferredSize(new Dimension(10, 5));
				buttonPane.add(panel_2);
			}
			{
				cancelButton = new JButton("取消");
				cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
				cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
			{
				panel_1 = new JPanel();
				panel_1.setPreferredSize(new Dimension(10, 5));
				buttonPane.add(panel_1);
			}
		}
		setLocationRelativeTo(null);
		{
			panel = new JPanel();
			panel.setPreferredSize(new Dimension(80, 10));
			contentPanel.add(panel, BorderLayout.WEST);
			panel.setLayout(new BorderLayout(0, 0));
			{
				
				icon = new JXBusyLabel();
				icon.setAlignmentX(Component.CENTER_ALIGNMENT);
				panel.add(icon);
			}
			icon.setBusy(true);
			{
				panel_4 = new JPanel();
				panel_4.setPreferredSize(new Dimension(40, 10));
				panel.add(panel_4, BorderLayout.WEST);
			}
		}
	}

	public void show(String title, String hint, boolean allowCancel){
		setTitle(title);
		hint_1.setText(hint);
		if (!allowCancel){
			cancelButton.setEnabled(false);
			setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		}else
			setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setModal(true);
		setVisible(true);
	}
	
	public interface IProcess {
		public boolean doProc(WaitDialog waitDialog);

		public void closed(boolean isok);
	}

	boolean isok = false;

	public Object params;
	Thread mTasks = new Thread() {
		public void run() {
			try {
				isok = iProcess
						.doProc(WaitDialog.this);
			} catch (Throwable e) {
				e.printStackTrace();
				isok = false;
			}
			
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					boolean isok = WaitDialog.this.isok;
					dispose();
					iProcess.closed(isok);
				}
			});
			
		}
	};


	public void Show(String title, String message) {
		mTasks.start();
		show(title, message, false);;
	}

	IProcess iProcess;

	public static void Show(String title, String message, IProcess iProcess, Object params) {
		WaitDialog waitDialog = new WaitDialog(iProcess);
		waitDialog.params = params;
		waitDialog.Show(title, message);
	}

}
