package com.wh.control.combobox;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

class AutoCompleter<T> implements DocumentListener {

	private Object selectObject;
	private JComboBox<T> owner = null;
	private JTextField editor = null;
	private ComboBoxModel<T> model = null;

	public AutoCompleter(JComboBox<T> comboBox) {
		owner = comboBox;
		owner.addPopupMenuListener(new PopupMenuListener() {
			
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				selectObject = owner.getSelectedItem();
			}
			
			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				owner.setSelectedItem(selectObject);
			}
			
			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		editor = (JTextField) comboBox.getEditor().getEditorComponent();
		editor.setText(null);
		editor.getDocument().addDocumentListener(this);
		model = comboBox.getModel();
	}

	protected Object getMatchingOptions(String str) {
		for (int k = 0; k < model.getSize(); k++) {
			Object itemObj = model.getElementAt(k);
			if (itemObj == null) {
				continue;
			}
			String item = itemObj.toString().toLowerCase();
			if (item.startsWith(str.toLowerCase()))
				return itemObj;
		}
		return null;
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		String text = editor.getText();
		if (text == null || text.isEmpty())
			return;

		selectObject = getMatchingOptions(text);
		if (selectObject == null)
			return;
		
		String stext = selectObject.toString();
		if (stext.equals(text))
			return;
		
		SwingUtilities.invokeLater(new Runnable() {			
			@Override
			public void run() {
				editor.setText(stext);
			}
		});
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
	}
}
