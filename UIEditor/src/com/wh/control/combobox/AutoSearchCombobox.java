package com.wh.control.combobox;
 
import java.util.List;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
 
public class AutoSearchCombobox<T> extends JComboBox<T> {
 
	private static final long serialVersionUID = 1L;
	@SuppressWarnings({ "unused"})
	private AutoCompleter<T> completer;
 
	public AutoSearchCombobox() {
		super();
		addCompleter();
	}
 
	public AutoSearchCombobox(ComboBoxModel<T> cm) {
		super(cm);
		addCompleter();
	}
 
	public AutoSearchCombobox(T[] items) {
		super(items);
		addCompleter();
	}
 
	public AutoSearchCombobox(List<T> v) {
		super((Vector<T>) v);
		addCompleter();
	}
 
	private void addCompleter() {
		setEditable(true);
		completer = new AutoCompleter<>(this);
	}
 
	public String getText() {
		return ((JTextField) getEditor().getEditorComponent()).getText();
	}
 
	public void setText(String text) {
		((JTextField) getEditor().getEditorComponent()).setText(text);
	}
 
	public boolean containsItem(String itemString) {
		for (int i = 0; i < this.getModel().getSize(); i++) {
			String _item = " " + this.getModel().getElementAt(i);
			if (_item.equals(itemString))
				return true;
		}
		return false;
	}

}
 