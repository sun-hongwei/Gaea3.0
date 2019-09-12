package com.wh.dialog.selector;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;

import com.wh.control.modelsearch.ModelSearchView;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class TreeSelector extends JDialog {

	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JTree tree;

	/**
	 * Create the dialog.
	 */
	
	boolean isok = false;
	public TreeSelector() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(TreeSelector.class.getResource("/image/browser.png")));
		setModal(true);
		setFont(new Font("微软雅黑", Font.PLAIN, 12));
		setBounds(100, 100, 727, 595);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JScrollPane scrollPane = new JScrollPane();
			contentPanel.add(scrollPane);
			{
				tree = new ModelSearchView.TreeModelSearchView();
				tree.setFont(new Font("微软雅黑", Font.PLAIN, 12));
				scrollPane.setViewportView(tree);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("确定");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if (tree.getSelectionPath() == null || tree.getSelectionPath().getLastPathComponent() == null)
							return;
						
						isok = true;
						
						setVisible(false);
					}
				});
				okButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("取消");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						setVisible(false);
					}
				});
				cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		
		setLocationRelativeTo(null);
	}
	
	public interface IFillTree{
		public void onFill(JTree tree);
	}
	public static DefaultMutableTreeNode showDialog(IFillTree onFilltree){
		TreeSelector dialog = new TreeSelector();
		onFilltree.onFill(dialog.tree);
		dialog.setVisible(true);
		if (!dialog.isok){
			return null;
		}
		
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)dialog.tree.getSelectionPath().getLastPathComponent();
		return node;
	}

}
