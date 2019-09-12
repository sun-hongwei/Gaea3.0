package com.wh.control;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class IconComboBoxRender<T extends IconComboBoxItem> extends JLabel implements ListCellRenderer<T>
{
	private static final long serialVersionUID = 1L;

	public IconComboBoxRender()
    {
        setOpaque(true);
    }
    
	@Override
	public Component getListCellRendererComponent(JList<? extends T> list, T value, int index, boolean isSelected,
			boolean cellHasFocus) {
        if (value != null)
        {
            setText(value.name);
            setIcon(value.icon);
        }

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        }
        else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        return this;
	}    
}
