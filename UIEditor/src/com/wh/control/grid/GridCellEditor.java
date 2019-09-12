package com.wh.control.grid;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.util.Date;
import java.util.EventObject;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

import com.sunking.swing.JDatePicker;
import com.wh.control.combobox.AutoSearchCombobox;

public class GridCellEditor extends DefaultCellEditor {
	private static final long serialVersionUID = 1L;
	private JButton tagButton = new JButton();

	Object columnType;
	ButtonActionListener actionListener;

	public static class VectorEx extends Vector<Object> {
		private static final long serialVersionUID = 1L;
		public Object[] ordData;
	}

	public static class ActionResult {
		public boolean isok = false;
		public Object data = null;

		public ActionResult() {

		}

		public ActionResult(boolean isok, Object data) {
			this.isok = isok;
			this.data = data;
		}
	}

	public interface ButtonActionListener {
		ActionResult actionPerformed(ActionEvent e, String columnName, int columnIndex);
	}

	public interface UpdateValueListener {
		void onUpdateValue(Object value, String columnName, int columnIndex);
	}

	public interface InitComboBoxListener {
		void init(String columnName, int columnIndex, JComboBox<Object> comboBox);
	}

	UpdateValueListener updateValueListener;
	InitComboBoxListener initComboBoxListener;
	String defaultButtonCaption;
	String columnName;
	int columnIndex;

	protected void fireUpdateValueListener(Object value) {
		if (updateValueListener != null) {
			updateValueListener.onUpdateValue(value, columnName, columnIndex);
		}
	}

	protected void setTextFieldDelegate(JTextField textField) {
		this.clickCountToStart = 2;
		delegate = new EditorDelegate() {
			private static final long serialVersionUID = 1L;

			public void setValue(Object value) {
				textField.setText((value != null) ? value.toString() : "");
			}

			public Object getCellEditorValue() {
				return textField.getText();
			}

			public boolean stopCellEditing() {
				fireUpdateValueListener(getCellEditorValue());
				return super.stopCellEditing();
			}
		};
		textField.addActionListener(delegate);
	}

	protected void setButtonDelegate(JButton button, JLabel label) {

		this.clickCountToStart = 2;
		delegate = new EditorDelegate() {
			private static final long serialVersionUID = 1L;
			Object value;

			public void setValue(Object value) {
				label.setText((value != null) ? value.toString() : "");
				this.value = value;
			}

			public Object getCellEditorValue() {
				return value;
			}

			public boolean stopCellEditing() {
				fireUpdateValueListener(getCellEditorValue());
				return super.stopCellEditing();
			}
		};

		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Object old = delegate.getCellEditorValue();
				ActionResult result = actionListener.actionPerformed(e, columnName, columnIndex);
				if (result.isok) {
					delegate.setValue(result.data);
					delegate.actionPerformed(e);
				} else {
					delegate.setValue(old);
				}
			}
		});

	}

	protected void setDateDelegate(JDatePicker datePicker) {
		this.clickCountToStart = 2;
		delegate = new EditorDelegate() {
			private static final long serialVersionUID = 1L;

			public void setValue(Object value) {
				try {
					datePicker.setSelectedDate((value != null) ? (Date) value : new Date());
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			public Object getCellEditorValue() {
				try {
					return datePicker.getSelectedDate();
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return new Date();
				}
			}

			public boolean stopCellEditing() {
				fireUpdateValueListener(getCellEditorValue());
				return super.stopCellEditing();
			}
		};
		datePicker.addActionListener(delegate);
	}

	class SpinnerDelegate extends EditorDelegate implements ChangeListener {
		private static final long serialVersionUID = 1L;
		JSpinner spinner;

		public SpinnerDelegate(JSpinner spinner) {
			this.spinner = spinner;
		}

		public void setValue(Object value) {
			spinner.setValue(value);
		}

		public Object getCellEditorValue() {
			return spinner.getValue();
		}

		public boolean stopCellEditing() {
			fireUpdateValueListener(getCellEditorValue());
			return super.stopCellEditing();
		}

		@Override
		public void stateChanged(ChangeEvent e) {
		}

	}

	protected void setSpinnerDelegate(JSpinner spinner) {
		this.clickCountToStart = 2;
		delegate = new SpinnerDelegate(spinner);
	}

	protected void setComboBoxDelegate(JComboBox<?> comboBox) {
		delegate = new EditorDelegate() {
			private static final long serialVersionUID = 1L;

			public void setValue(Object value) {
				comboBox.setSelectedItem(value);
			}

			public Object getCellEditorValue() {
				return comboBox.getSelectedItem();
			}

			public boolean shouldSelectCell(EventObject anEvent) {
				if (anEvent instanceof MouseEvent) {
					MouseEvent e = (MouseEvent) anEvent;
					return e.getID() != MouseEvent.MOUSE_DRAGGED;
				}
				return true;
			}

			public boolean stopCellEditing() {
				if (comboBox.isEditable()) {
					// Commit edited value.
					comboBox.actionPerformed(new ActionEvent(GridCellEditor.this, 0, ""));
				}

				fireUpdateValueListener(getCellEditorValue());

				return super.stopCellEditing();
			}
		};

		comboBox.addActionListener(delegate);
	}

	@SuppressWarnings("unchecked")
	protected <T> T getControl(Object o) {
		Component control = null;
		if (o instanceof Boolean) {
			control = new AutoSearchCombobox<>(new Boolean[] { true, false });
			setComboBoxDelegate((JComboBox<Boolean>) control);
		} else if (o instanceof List) {
			JComboBox<Object> comboBox = new AutoSearchCombobox<Object>();
			List<Object> list = (List<Object>) o;
			if (list.size() > 0)
				for (Object object : list) {
					comboBox.addItem(object);
				}
			else {
				if (initComboBoxListener != null)
					initComboBoxListener.init(columnName, columnIndex, comboBox);
			}
			control = comboBox;
			setComboBoxDelegate((JComboBox<Object>) control);
		} else if (o instanceof Number) {
			JSpinner spinner;
			spinner = new JSpinner();
			setSpinnerDelegate(spinner);

			if (o instanceof Double || o instanceof Float) {
				spinner.setModel(new SpinnerNumberModel(0.00, 0.00, null, 0.01));
			} else {
				spinner.setModel(new SpinnerNumberModel());
			}
			control = spinner;
		} else if (o instanceof Date) {
			control = new JDatePicker();
			setDateDelegate((JDatePicker) control);
		} else if (o instanceof JButton) {
			JPanel panel;
			JLabel label = null;
			JButton button = null;
			panel = new JPanel();
			panel.setLayout(new BorderLayout());
			label = new JLabel();
			button = new JButton(defaultButtonCaption);
			panel.add(label, BorderLayout.CENTER);
			panel.add(button, BorderLayout.EAST);
			setButtonDelegate(button, label);

			control = panel;
		} else {
			control = new JTextField();
			setTextFieldDelegate((JTextField) control);

		}

		control.setFont(new Font("微软雅黑", 0, 12));
		return (T) control;
	}

	@SuppressWarnings("unchecked")
	protected void setEditorValue(Object value, Object columnType) {
		if (columnType == null) {
			editorComponent = getControl(value);
			if (value instanceof Boolean)
				((JComboBox<Boolean>) editorComponent).setSelectedItem((boolean) value ? "true" : "false");
			else if (value instanceof Number) {
				((JSpinner) editorComponent).setValue(value);
			} else if (value instanceof Date) {
				try {
					((JDatePicker) editorComponent).setSelectedDate((Date) value);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			} else {
				((JTextField) editorComponent).setText((String) value);
			}
		} else {
			if (columnType instanceof List) {
				editorComponent = getControl(columnType);
				List<?> values = (List<?>) columnType;
				JComboBox<Object> comboBox = (JComboBox<Object>) editorComponent;
				if (values.size() == 0 && initComboBoxListener != null) {
					initComboBoxListener.init(columnName, columnIndex, comboBox);
				} else
					comboBox.setModel(new DefaultComboBoxModel<>(values.toArray()));
			} else {
				editorComponent = getControl(tagButton);
			}
		}

		delegate.setValue(value);
	}

	public GridCellEditor(String columnName, int columnIndex, String defaultButtonCaption, Object columnType,
			ButtonActionListener actionListener, UpdateValueListener updateValueListener) {
		this(columnName, columnIndex, defaultButtonCaption, columnType, actionListener, updateValueListener, null);
	}

	/***
	 * GridCellEditor对象的构造方法
	 * 
	 * @param columnName
	 *            关联的列名称
	 * @param columnIndex
	 *            关联列的索引号
	 * @param defaultButtonCaption
	 *            按钮编辑模式下的按钮的文本，可能会被columnType的设置覆盖
	 * @param columnType
	 *            列的编辑器类型定义，含义如下： 1、值为null或者对应行的索引项目为null或不存在则根据对应单元格的数据类型设置编辑器：
	 *            boolean：JComboBox控件（true|false）， Number：JSpinner控件
	 *            Date：JDatePicker控件 其他：JTextField控件
	 * 
	 *            2、不为null columnType为Object[]类型
	 *            columnType对应索引存在且不为null，其值的类型含义如下：
	 *            String：使用按钮，columnType[i].isEmpty()函数返回false，使用其值作为按钮名称，否则按钮名称使用defaultButtonCaption设置，
	 *            List<E>：使用下拉列表方式，size不为0，下拉项目为E.toString()值列表，否则调用initComboBoxListener方法设置下拉项目
	 *            columnType为Class类型 根据columnType的值类型使用不同组件：list则使用下拉列表，其他为按钮
	 * @param actionListener
	 *            按钮编辑模式下的按钮单击事件
	 * @param updateValueListener
	 *            当编辑器中的值被更新时通知，实现者必须在回调事件onUpdateValue中完成表格的索引列的值更新操作。
	 */
	public GridCellEditor(String columnName, int columnIndex, String defaultButtonCaption, Object columnType,
			ButtonActionListener actionListener, UpdateValueListener updateValueListener,
			InitComboBoxListener initComboBoxListener) {
		super(new JTextField());
		this.columnName = columnName;
		this.columnIndex = columnIndex;
		this.initComboBoxListener = initComboBoxListener;
		this.defaultButtonCaption = defaultButtonCaption;
		this.updateValueListener = updateValueListener;
		this.columnType = columnType;
		this.actionListener = actionListener;

	}

	@SuppressWarnings("rawtypes")
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		row = table.convertRowIndexToModel(row);
		Vector vector = (Vector) model.getDataVector().elementAt(row);
		if (vector instanceof VectorEx) {
			Object[] rowData = ((VectorEx)vector).ordData;
			if (rowData.length > table.getColumnCount()) {
				int columnTypeIndex = table.getColumnCount() + table.getColumnCount() - 1 - column;
				if (columnTypeIndex < rowData.length) {
					setEditorValue(value, rowData[columnTypeIndex]);
					return editorComponent;
				}
			}
		}
		if (columnType != null && columnType instanceof Object[]) {
			Object[] columnTypes = (Object[]) columnType;
			if (row > columnTypes.length - 1 || columnTypes[row] == null) {
				setEditorValue(value, null);
			} else
				setEditorValue(value, columnTypes[row]);
		} else
			setEditorValue(value, columnType);

		return editorComponent;
	}

}
