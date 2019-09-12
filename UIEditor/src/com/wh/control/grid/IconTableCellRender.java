package com.wh.control.grid;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import com.wh.draws.UINode;

public class IconTableCellRender extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 1L;

	public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
    	JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    	if (column == 0){
    		if (value != null){
				Icon icon = new ImageIcon(UINode.getImage(value.toString()));
				if (icon != null)
					label.setIcon(icon);
    		}
    	}
    	return label;
    }

}
