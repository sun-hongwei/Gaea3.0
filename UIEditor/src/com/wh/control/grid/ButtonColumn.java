package com.wh.control.grid;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ButtonColumn {

//	public static class ButtonEdit extends JPanel{
//		private static final long serialVersionUID = 7194409585835737803L;
//		public JButton button;
//		public JTextField textField;
//		public ButtonEdit(Object value){
//			setLayout(new BorderLayout(0, 0));
//			button = new JButton("...");
//			button.setFocusable(false);
//			add(button, BorderLayout.EAST);
//			textField = new JTextField();
//			textField.setEditable(true);
//			add(textField, BorderLayout.CENTER);
//			setValue(value);
//			if (value != null)
//				textField.setText(value.toString());
//		}
//		
//		Object value;
//		public void setValue(Object value){
//			this.value = value;
//		}
//		
//		public Object getValue(){
//			return value;
//		}
//	}
//
	
	public static class JsonButtonLabel extends ButtonLabel{

		private static final long serialVersionUID = 1L;

		public JsonButtonLabel(Object sender, Object value) {
			super(sender, value);
		}
		
	}
	
	public static class ButtonLabel extends JPanel{
		private static final long serialVersionUID = 7194409585835737803L;
		public JButton button;
		public JLabel textField;
		public Object sender;
		public ButtonLabel(Object sender, Object value){
			this.sender = sender;
			setLayout(new BorderLayout(0, 0));
			button = new JButton("...");
			button.setFocusable(false);
			add(button, BorderLayout.EAST);
			textField = new JLabel();
			add(textField, BorderLayout.CENTER);
			setValue(value);
		}
		
		Object value;
		public void setValue(Object value){
			this.value = value;
			if (value == null)
				textField.setText("");
			else
				textField.setText(value.toString());
			textField.updateUI();
		}
		
		public Object getValue(){
			return value;
		}
		
	}
}
