package com.wh.control.combobox;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.json.JSONArray;

public class MulitCombobox<T> extends JComboBox<MulitCombobox.CheckValue<? extends T>> {
	private static final long serialVersionUID = 1L;
	
	public static class CheckListCellRenderer<T> extends JCheckBox implements ListCellRenderer<T>, Serializable {
		private static final long serialVersionUID = 1L;
		
		protected static Border noFocusBorder;
		/**
		 * Constructs a default renderer object for an item
		 * in a list.
		 */
		public CheckListCellRenderer() {
		    super();
		    if (noFocusBorder == null) {
		        noFocusBorder = new EmptyBorder(1, 1, 1, 1);
		    }
		    setOpaque(true);
		    setBorder(noFocusBorder);
		}
		@SuppressWarnings("unchecked")
		public Component getListCellRendererComponent(
		        JList<? extends T> list,
		        T value,
		        int index,
		        boolean isSelected,
		        boolean cellHasFocus) {
		    setComponentOrientation(list.getComponentOrientation());
		    if (isSelected) {
		        setBackground(list.getSelectionBackground());
		        setForeground(list.getSelectionForeground());
		    } else {
		        setBackground(list.getBackground());
		        setForeground(list.getForeground());
		    }
		    if (value instanceof CheckValue) {
		        CheckValue<T> ckValue = (CheckValue<T>) value;
		        this.setText(ckValue.value == null ? "" : ckValue.value.toString());
		        this.setSelected(ckValue.bolValue);
		    }
		    setEnabled(list.isEnabled());
		    setFont(list.getFont());
		    setBorder((cellHasFocus) ?
		              UIManager.getBorder("List.focusCellHighlightBorder") :
		              noFocusBorder);
		    return this;
		}
	}
		
	public static class CheckValue<T> {
		public boolean bolValue = false;
		public T value = null;
		public CheckValue() {
		}
		public CheckValue(boolean bolValue, T value) {
		    this.bolValue = bolValue;
		    this.value = value;
		}
		
		public String toString(){
			if (value == null)
				return "";
			
			return value.toString();
		}
	}

    @SuppressWarnings({ "unchecked", "rawtypes" })
	public MulitCombobox(T[] value) {
//    	setRenderer(new CheckListCellRenderer<>());
        addItem(new CheckValue(false, "Select All"));
        if (value != null){
        	for (T t : value) {
				addItem(new CheckValue<T>(false, t));
			}
        }
        
        this.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                itemSelected();
            }
        });
        
    }

    @SuppressWarnings("unchecked")
	public String getSelectedTexts(){
    	JSONArray json = new JSONArray();
    	for (int i = 0; i < getItemCount(); i++) {
			CheckValue<T> ck = (CheckValue<T>) getItemAt(i);
			if (ck.bolValue){
				json.put(ck.value.toString());
			}
		}
    	
    	return json.toString();
    }
    
	@SuppressWarnings("unchecked")
	public T[] getSelectedValues(){
    	List<T> results = new ArrayList<>();
    	for (int i = 0; i < getItemCount(); i++) {
			CheckValue<T> ck = (CheckValue<T>) getItemAt(i);
			if (ck.bolValue){
				results.add(ck.value);
			}
		}
    	
    	return (T[]) results.toArray();
    }
    
    @SuppressWarnings("unchecked")
	public void setSelectedValues(T[] values){
		for (int i = 0; i < getItemCount(); i++) {
			CheckValue<T> ck = (CheckValue<T>) getItemAt(i);
			ck.bolValue = false;
	    	for (T t : values) {
				if (ck.value.equals(t)){
					ck.bolValue = true;
				}
	   		}
		}
		updateUI();
    }
    
    @SuppressWarnings("unchecked")
	private void itemSelected() {
        if (getSelectedItem() instanceof CheckValue) {
            if (getSelectedIndex() == 0) {
                selectedAllItem();
            } else {
                CheckValue<T> jcb = (CheckValue<T>) getSelectedItem();
                jcb.bolValue = (!jcb.bolValue);
                setSelectedIndex(getSelectedIndex());
            }
        }
    }
    
    @SuppressWarnings("unchecked")
	private void selectedAllItem() {
    	
        boolean bl = getSelectedValues().length == 0;
        for (int i = 0; i < getItemCount(); i++) {
        	if (i == 0)
        		continue;
        	
        	CheckValue<T> jcb = (CheckValue<T>) getItemAt(i);
            jcb.bolValue = (bl);
        }
        setSelectedIndex(0);
    }
    
    /*获取选取的对象*/
    @SuppressWarnings("unchecked")
	public Vector<T> getComboVc() {
        Vector<T> vc = new Vector<>();
        for (int i = 1; i < getItemCount(); i++) {
            CheckValue<T> jcb = (CheckValue<T>) getItemAt(i);
            if (jcb.bolValue) {
                vc.add(jcb.value);
            }
        }
        return vc;
    }
}
 
