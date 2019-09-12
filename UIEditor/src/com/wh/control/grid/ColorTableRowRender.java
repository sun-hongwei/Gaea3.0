package com.wh.control.grid;

import java.awt.Color;
import java.awt.Component;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class ColorTableRowRender extends HrefTableCellRender{

	public ColorTableRowRender(DefaultTableModel model) {
		super(model);
	}

	private static final long serialVersionUID = 1L;

	HashMap<Integer, Color> colors = new HashMap<>();
	SimpleEntry<Integer, Color> masterColor = null;
	
	public void setColor(int row, Color color){
		colors.put(row, color);
	}
	
	public boolean exists(int row){
		return colors.containsKey(row);
	}
	
	public void clear(){
		colors.clear();
	}
	
	public void remove(int row){
		if (colors.containsKey(row))
			colors.remove(row);
	}
	
	public HashMap<Integer, Color> gets(){
		return new HashMap<>(colors);
	}
	
	public void setSelectMode(int row, Color color){
		masterColor = new SimpleEntry<Integer, Color>(row, color);
	}
	
	public void setDrawMode(){
		masterColor = null;
	}

	public boolean isSelectModel(){
		return masterColor != null;
	}
	
	public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int col) {
    	
		setBackground(null);
		
    	JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
    	
    	if (colors.containsKey(row)){
    		label.setBackground(colors.get(row));
    	}
    	
    	if (masterColor != null && row == masterColor.getKey()){
    		label.setBackground(masterColor.getValue());
    	}
    	    	
    	return label;
    	
    }

}
