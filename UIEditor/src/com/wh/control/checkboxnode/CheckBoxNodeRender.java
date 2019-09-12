package com.wh.control.checkboxnode;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.tree.TreeCellRenderer;

public class CheckBoxNodeRender extends JPanel implements TreeCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2437593000562987698L;
	protected JCheckBox check;
	protected CheckBoxNodeLabel label;

	public interface IGetIcon{
		public Icon onIcon(JTree tree, Object value,
				boolean selected, boolean expanded, boolean leaf, int row,
				boolean hasFocus);
	}
	
	protected IGetIcon onGetIcon;
	
	public CheckBoxNodeRender(){
		this(null);
	}
	
	public CheckBoxNodeRender(IGetIcon onGetIcon) {
		setLayout(null);
		this.onGetIcon = onGetIcon;
		add(check = new JCheckBox());
		add(label = new CheckBoxNodeLabel());
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		String stringValue = tree.convertValueToText(value, selected, expanded,
				leaf, row, hasFocus);
		setEnabled(tree.isEnabled());
		if (value instanceof CheckBoxNode) {
			check.setSelected(((CheckBoxNode) value).isSelected());
		}
		label.setFont(tree.getFont());
		label.setText(stringValue);
		label.setSelected(selected);
		label.setFocus(hasFocus);
		Icon icon = null;
		if (onGetIcon != null){
			icon = onGetIcon.onIcon(tree, value, selected, expanded, leaf, row, hasFocus);
		}
		
		if (icon == null){
			if (leaf)
				icon = UIManager.getIcon("Tree.leafIcon");
			else if (expanded)
				icon = UIManager.getIcon("Tree.openIcon");
			else
				icon = UIManager.getIcon("Tree.closedIcon");
		}
		if (icon != null)
			label.setIcon(icon);
		
		setBackground(new Color(255,255,255,0));
		label.setForeground(UIManager.getColor("Tree.textForeground"));
		return this;
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension dCheck = check.getPreferredSize();
		Dimension dLabel = label.getPreferredSize();
		return new Dimension(dCheck.width + dLabel.width,
				(dCheck.height < dLabel.height ? dLabel.height : dCheck.height) + 10);
	}

	@Override
	public void doLayout() {
		Dimension dCheck = check.getPreferredSize();
		Dimension dLabel = label.getPreferredSize();
		int yCheck = 0;
		int yLabel = 0;
		if (dCheck.height < dLabel.height)
			yCheck = (dLabel.height - dCheck.height) / 2;
		else
			yLabel = (dCheck.height - dLabel.height) / 2;
		check.setLocation(0, yCheck + 5);
		check.setBounds(0, yCheck + 5, dCheck.width, dCheck.height);
		label.setLocation(dCheck.width, yLabel + 5);
		label.setBounds(dCheck.width, yLabel + 5, dLabel.width, dLabel.height);
	}

	@Override
	public void setBackground(Color color) {
		if (color instanceof ColorUIResource)
			color = null;
		super.setBackground(color);
	}
}
