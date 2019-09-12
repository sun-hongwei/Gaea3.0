package com.wh.control;

import java.awt.Component;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import com.wh.control.grid.design.PropertyTableCellEditor.KeyValue;

public class CustomRenderTable extends DefaultTableCellRenderer{

private static final long serialVersionUID = -8279677278537315468L;

@SuppressWarnings("unchecked")
public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column) {
	JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	if (column == 1){
		String name = table.getValueAt(row, 0).toString();
		if (name.compareTo("jumpID") == 0 && value != null){
			try {
				JComboBox<KeyValue<String, String>> comboBox = (JComboBox<KeyValue<String, String>>)table.getCellEditor(row, column).getTableCellEditorComponent(table, value, isSelected, row, column);
				if (comboBox != null){
					for (int i = 0; i < comboBox.getItemCount(); i++) {
						KeyValue<String, String> kv = comboBox.getItemAt(i);
						if (kv.value == null)
							continue;
						
						if (kv.value.compareTo(value.toString()) == 0){
							label.setText(kv.key);					
							break;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	return label;
}
}
