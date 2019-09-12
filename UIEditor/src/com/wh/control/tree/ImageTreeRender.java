package com.wh.control.tree;

import java.awt.Component;
import java.awt.Dimension;
import java.io.File;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;

import com.wh.system.tools.ImageUtils;

public class ImageTreeRender extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = 1L;

	IGetIcon onGetIcon;
	public ImageTreeRender(IGetIcon onGetIcon) {
		super();
		this.onGetIcon = onGetIcon;
	}

	public interface IGetIcon{
		Icon getIcon(TreeNode node);
	}
	
	public static Icon getIcon(File imageFile){
		return ImageUtils.getIcon(imageFile, new Dimension(24, 24));
	}
	
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
			boolean leaf, int row, boolean hasFocus) {
		JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

		DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
		Icon icon = onGetIcon.getIcon(treeNode);
		if (icon == null)
			return label;

		label.setIcon(icon);
//		label.setHorizontalTextPosition(SwingConstants.RIGHT);
		label.setVerticalTextPosition(SwingConstants.CENTER);
		label.updateUI();
		
		return label;
	}
}