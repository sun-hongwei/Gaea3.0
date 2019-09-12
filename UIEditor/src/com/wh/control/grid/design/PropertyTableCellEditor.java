package com.wh.control.grid.design;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellEditor;
import javax.swing.text.JTextComponent;

import org.json.JSONObject;

import com.wh.control.combobox.MulitCombobox;
import com.wh.control.grid.ButtonColumn;
import com.wh.control.grid.design.PropertyTableCellEditor.IClientEvent.ISetValue;

public class PropertyTableCellEditor extends AbstractCellEditor
		implements TableCellEditor, ActionListener, ItemListener, ChangeListener, Serializable {
	private static final long serialVersionUID = 1L;
	protected ArrayList<JComponent> editors = new ArrayList<JComponent>();
	protected HashMap<JComponent, Integer> hashEditors = new HashMap<>();
	private JTable table;

	public static class KeyValue<K, V>{
		public K key;
		public V value;
		public KeyValue(){
			
		}
		public KeyValue(K key, V value){
			this.key = key;
			this.value = value;
		}
		
		public String toString() {
			if (key == null)
				return "";
			
			return key.toString().trim();
		}
		
		public JSONObject toJson(){
			JSONObject object = new JSONObject();
			object.put("key", key);
			object.put("value", value);
			return object;
		}
		
		@SuppressWarnings("unchecked")
		public KeyValue(JSONObject json){
			if (json == null)
				return;
			
			if (json.has("key"))
				key = (K)json.get("key");
			if (json.has("value"))
				value = (V)json.get("value");
		}
	}
	

	public PropertyTableCellEditor() {
		super();
	}

	public void addTableCellEditor(final JTextField textField, int row) {
//		textField.addCaretListener(new CaretListener() {
//			public void caretUpdate(CaretEvent e) {
//				table.setValueAt(textField.getText(), table.getSelectedRow(), 1);
//			}
//		});
//		
		textField.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				// is Auto-generated method stub
				if (table.getSelectedRow() == -1)
					return;
				
				String text = (String)table.getValueAt(table.getSelectedRow(), 1);
				textField.setText(text);
				textField.selectAll();
			}

			public void focusLost(FocusEvent e) {
				doAction(e.getSource());
			}
		});
		editors.add(row, textField);
	}

	public void addTableCellEditor(final JTextArea textField, int row) {
//		textField.addCaretListener(new CaretListener() {
//			public void caretUpdate(CaretEvent e) {
//				table.setValueAt(textField.getText(), table.getSelectedRow(), 1);
//			}
//		});
		
		textField.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				// is Auto-generated method stub
				String text = (String)table.getValueAt(table.getSelectedRow(), 1);
				textField.setText(text);
				textField.selectAll();
			}

			public void focusLost(FocusEvent e) {
				doAction(e.getSource());
			}
		});
		editors.add(row, textField);
	}

	public void addTableCellEditor(final JCheckBox checkBox, int row) {
		editors.add(row, checkBox);
		checkBox.addActionListener(this);
		checkBox.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				doAction(e.getSource());
			}
			
			@Override
			public void focusGained(FocusEvent e) {
			}
		});
	}

	public void addTableCellEditor(final ButtonColumn.ButtonLabel bl, int row) {
		editors.add(row, bl);
		bl.button.putClientProperty("bc", bl);
		bl.button.addActionListener(this);
		bl.textField.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void focusGained(FocusEvent e) {
			}
		});
	}

	public void addTableCellEditor(final JSpinner spinner, int row) {
		editors.add(row, spinner);
		spinner.addChangeListener(this);
		spinner.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				doAction(e.getSource());
			}
			
			@Override
			public void focusGained(FocusEvent e) {
			}
		});
	}

	public void addTableCellEditor(final JLabel label, int row) {
		editors.add(row, label);
	}

	@SuppressWarnings("rawtypes")
	public void addTableCellEditor(final JComboBox comboBox, int row) {
		editors.add(row, comboBox);
		comboBox.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
		comboBox.addActionListener(this);
		comboBox.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				doAction(e.getSource());
			}
			
			@Override
			public void focusGained(FocusEvent e) {
			}
		});
		
	}

	@SuppressWarnings("rawtypes")
	public void addTableCellEditor(final MulitCombobox comboBox, int row) {
		editors.add(row, comboBox);
		comboBox.addActionListener(this);
		comboBox.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				doAction(e.getSource());
			}
			
			@Override
			public void focusGained(FocusEvent e) {
			}
		});
		
	}

	public Object getCellEditorValue() {
		// is Auto-generated method stub
		if (this.table.getSelectedRow() == -1)
			return null;
		
		Object settedValue = this.table.getValueAt(this.table.getSelectedRow(), 1);
		return settedValue;
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
			int column) {
		Component component = editors.get(row);
		if (component instanceof JLabel)
			return null;
		
		return component;
	}

	public void clear() {
		this.editors.clear();
	}

	public void setTable(JTable table) {
		this.table = table;
	}

	public interface IClientEvent {
		public interface ISetValue {
			public void onSetValue(int row, int col, Object value);
		}
		public void onEdit(int row, int col);
		public void onClick(JComponent sender, int row, int col, ISetValue onSetValue);
		public boolean onUpateValue(JComponent sender, int row, int col, Object value);
	}

	public IClientEvent onClientEvent;

	protected void fireOnUpdateValue(JComponent sender, int row, int col, Object value) {
		boolean b = true;
		if (onClientEvent != null){
			b = onClientEvent.onUpateValue(sender, row, col, value);
		}
		
		if (b)
			this.table.setValueAt(value, row, col);
	}
	
	public void actionPerformed(ActionEvent e) {
		doAction(e.getSource());
	}
	
	protected void doEditAction(Object source){
		if (!(source instanceof JTextComponent))
			return;
		
		JTextComponent editor = (JTextComponent) source;
		JComponent parent = (JComponent)editor.getParent();
		int index = editors.indexOf(parent);		
		if (index == -1){
			return;
		}
		Object newValue = editor.getText();
		fireOnUpdateValue(parent, index, 1, newValue);
	}

	@SuppressWarnings("rawtypes")
	public void doAction(Object source) {
		// is Auto-generated method stub
		JComponent component = (JComponent)source;
		if (source instanceof JButton){
			JButton editor = (JButton) source;
			component = (JComponent)editor.getClientProperty("bc");
		}
		int selectIndex = editors.indexOf(component);		

		if (selectIndex == -1){
			doEditAction(source);
			this.stopCellEditing();
			return;
		}
		
		if (source instanceof JTextField || source instanceof JTextArea) {
			JTextComponent editor = (JTextComponent) source;
			Object newValue = editor.getText();
			fireOnUpdateValue(editor, selectIndex, 1, newValue);
		} else if (source instanceof JButton) {
			if (onClientEvent != null) {
				onClientEvent.onClick(component, selectIndex, 1, new ISetValue() {
					@Override
					public void onSetValue(int row, int col, Object value) {
						PropertyTableCellEditor.this.table.setValueAt(value, row, col);
					}
				});
			}
		} else if (source instanceof JCheckBox) {
			JCheckBox editor = (JCheckBox) source;
			Object newValue = (editor.isSelected());
			fireOnUpdateValue(editor, selectIndex, 1, newValue);
			return;
		} else if (source instanceof MulitCombobox) {
			MulitCombobox editor = (MulitCombobox) source;
			Object newValue = editor.getSelectedTexts();
			fireOnUpdateValue(editor, selectIndex, 1, newValue);
		} else if (source instanceof JComboBox) {
			JComboBox editor = (JComboBox) source;
			Object newValue = editor.getSelectedItem();
			if (newValue instanceof KeyValue){
				KeyValue kv = (KeyValue)newValue;
				newValue = kv.value;
			}
			fireOnUpdateValue(editor, selectIndex, 1, newValue);
		} else if (source instanceof JSpinner) {
			JSpinner spinner= (JSpinner) source;
			Object newValue = spinner.getValue();
			fireOnUpdateValue(spinner, selectIndex, 1, newValue);
			return;
		}
		this.stopCellEditing();
	}

	public void itemStateChanged(ItemEvent e) {
		// is Auto-generated method stub
		this.stopCellEditing();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		doAction(e.getSource());
	}
}