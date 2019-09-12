package com.wh.control.grid;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class HrefTableCellRender extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 1L;

	DefaultTableModel model;

	public interface IOnHrefClick {
		void onClick(DefaultTableModel model, int row, int col, LinkInfo info);
	}

	public HrefTableCellRender(DefaultTableModel model) {
		super();
		this.model = model;
	}

	static class LinkInfos extends HashMap<String, LinkInfo> {
		private static final long serialVersionUID = 1L;

	}

	public static class LinkInfo {
		public String id;
		public String column;
		public Object link;

		protected MouseListener mouseListener;
		
		public LinkInfo(String id, Object link) {
			this.id = id;
			this.link = link;
		}
	}

	List<IOnHrefClick> onHrefClicks = new ArrayList<>();

	LinkInfos infos = new LinkInfos();
	
	public void addOnHrefClick(IOnHrefClick onHrefClick) {
		onHrefClicks.add(onHrefClick);
	}

	public void removeOnHrefClick(IOnHrefClick onHrefClick) {
		onHrefClicks.remove(onHrefClick);
	}

	public void clearOnHrefClick(IOnHrefClick onHrefClick) {
		onHrefClicks.clear();
	}

	public void setLink(int colIndex, LinkInfo info) {
		info.column = model.getColumnName(colIndex);
		infos.put(info.id, info);

		model.fireTableDataChanged();
	}

	@SuppressWarnings("unchecked")
	public void removeLink(String id) {
		Vector<Object> rows = model.getDataVector();
		for (int i = 0; i < model.getRowCount(); i++) {
			Vector<Object> row = (Vector<Object>) rows.get(i);
			if (row.get(row.size() - 1) instanceof LinkInfos) {
				LinkInfos infos = (LinkInfos) row.get(row.size() - 1);
				if (infos.containsKey(id)) {
					infos.remove(id);
					return;
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void clearLink() {
		Vector<Object> rows = model.getDataVector();
		for (int i = 0; i < model.getRowCount(); i++) {
			Vector<Object> row = (Vector<Object>) rows.get(i);
			if (row.get(row.size() - 1) instanceof LinkInfos) {
				row.remove(row.size() - 1);
			}
		}
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int rowIndex, int col) {
    	JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, rowIndex, col);
    	
		String colName = model.getColumnName(col);
		if (infos.containsKey(colName) && value != null && value.toString() != null && !value.toString().isEmpty()){
			LinkInfo info = infos.get(colName);
			label.setForeground(Color.BLUE);
			label.setToolTipText(value.toString());
			label.setText("<html><u>" + label.getToolTipText() + "</u></html>");
			label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

			label.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					for (IOnHrefClick onHrefClick : onHrefClicks) {
						onHrefClick.onClick(model, rowIndex, col, info);
					}
				}
			});
		}else{
			List<MouseListener> listeners = Arrays.asList(label.getMouseListeners());
			for (MouseListener mouseListener : listeners) {
				label.removeMouseListener(mouseListener);
			}
		}
    	
    	return label;
    	
    }

}
