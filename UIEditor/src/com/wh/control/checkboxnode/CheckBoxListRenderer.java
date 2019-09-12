package com.wh.control.checkboxnode;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;


public class CheckBoxListRenderer implements ListCellRenderer<ICheck> {

	public static class ListCheckBox extends JCheckBox{
		private static final long serialVersionUID = 1L;
		public ListCheckBox(ICheck value) {
			super(value.toString());
			this.setSelected(value.getChecked());
			this.value = value;
		}

		public ICheck value;
	}
	
	protected static Border m_noFocusBorder = new EmptyBorder(1, 1, 1, 1);
	
	public interface ISelection{
		public void onSelected(ICheck obj);
	}
	
	ISelection onSelection;

	public CheckBoxListRenderer(JList<ICheck> listView, ISelection onSelection) {
		super();
		this.onSelection = onSelection;
		listView.addMouseListener(new MouseAdapter() {
		    public void mouseReleased(MouseEvent e) {
				ICheck value = listView.getSelectedValue();
		    	if (value == null)
					return;

				value.setChecked(!value.getChecked());
				listView.updateUI();
				if (onSelection != null)
					onSelection.onSelected(listView.getSelectedValue());
		    }

		});
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends ICheck> list, ICheck value, int index,
			boolean isSelected, boolean cellHasFocus) {
		ListCheckBox box = new ListCheckBox(value);
	
		
		box.setSelected(value.getChecked());
		box.setBackground(isSelected ? list.getSelectionBackground() : list
				.getBackground());
		box.setForeground(isSelected ? list.getSelectionForeground() : list
				.getForeground());
		box.setFont(list.getFont());
		box.setBorder((cellHasFocus) ? UIManager
				.getBorder("List.focusCellHighlightBorder") : m_noFocusBorder);
		box.updateUI();

		return box;
	}
}