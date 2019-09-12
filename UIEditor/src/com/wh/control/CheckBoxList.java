package com.wh.control;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;

public class CheckBoxList<T> extends JList<T> {

	private static final long serialVersionUID = 1L;

	public interface ICheckedListener{
		void onCheck(boolean isCheck, Object value);
	}
	
	List<ICheckedListener> listeners = new ArrayList<>();
	
	public void addCheckedListener(ICheckedListener listener){
		listeners.add(listener);
	}
	
	public void removeCheckedListener(ICheckedListener listener){
		listeners.remove(listener);
	}
	
	@Override
	public void setModel(ListModel<T> model){
		clearSelection();
		clearCheck();
		super.setModel(model);
	}
	
	HashMap<Integer, Boolean> checkMap = new HashMap<>();
	class CheckBoxCellRenderer extends JCheckBox implements ListCellRenderer<T> {
		private static final long serialVersionUID = 1L;

		public CheckBoxCellRenderer() {
			setOpaque(false);
			setFont(CheckBoxList.this.getFont());
			// setHorizontalAlignment((int) 0.5f);
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends T> list, T value, int index, boolean isSelected,
				boolean cellHasFocus) {
			list.clearSelection();
			if (!checkMap.containsKey(index)){
				checkMap.put(index, false);
			}
			setSelected(checkMap.get(index));
			setText((value == null) ? "" : value.toString());
			updateUI();
			return this;
		}

	}

	protected void init() {
		this.setCellRenderer(new CheckBoxCellRenderer());
		this.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mousePressed(MouseEvent e) {
				int row = CheckBoxList.this.locationToIndex(e.getPoint());

				if (!checkMap.containsKey(row)){
					checkMap.put(row, false);
				}
				
				boolean b = !checkMap.get(row);
				checkMap.put(row, b);
				
				DefaultListModel<?> model = (DefaultListModel<?>)getModel();
				
				for (ICheckedListener iCheckedListener : listeners) {
					iCheckedListener.onCheck(b, model.getElementAt(row));
				}
				CheckBoxList.this.updateUI();
			}
		});
	}

	public CheckBoxList(ListModel<T> model) {
		super(model);
		init();
	}

	public CheckBoxList() {
		super();
		init();
	}

	public void setChecks(int[] checks){
		for (int i : checks) {
			checkMap.put(i, true);
		}
		
		updateUI();
	}
	
	public List<Integer> getChecks(){
		List<Integer> checks = new ArrayList<>();
		for (Integer index : checkMap.keySet()) {
			if (checkMap.get(index))
				checks.add(index);
		}
		
		return checks;
	}
	
	public boolean getCheck(int index){
		if (checkMap.containsKey(index))
			return checkMap.get(index);
		
		return false;
	}
	
	public void setCheck(int index, boolean isCheck){
		checkMap.put(index, isCheck);
		updateUI();
	}

	public void clearCheck(){
		checkMap.clear();
	}
	
}
