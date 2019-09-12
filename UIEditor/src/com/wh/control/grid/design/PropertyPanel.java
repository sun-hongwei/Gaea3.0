package com.wh.control.grid.design;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Collection;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import com.wh.control.CustomRenderTable;
import com.wh.control.combobox.MulitCombobox;
import com.wh.control.grid.ButtonColumn;
import com.wh.control.grid.design.PropertyTableCellEditor.IClientEvent;

public class PropertyPanel extends JPanel {
	private static final long serialVersionUID = 7598712670750954217L;
	private JTable table = null;
	private JScrollPane scrollPane = null;
	private DefaultTableModel defaultTableModel = null;
	private DefaultTableColumnModel defaultTableColumnModel = null;
	private TableColumn tableColumn_property = new TableColumn(0, 100, null, null);
	private TableColumn tableColumn_value = new TableColumn(1);
	private PropertyTableCellEditor propertyTableCellEditor = new PropertyTableCellEditor();
	private String[] colNames = new String[] { "属性", "值" };

	public interface IProperty {
		public String getName();

		public Object getValue();

		public String getTitle();
		
		public Object getSender();

		public JComponent getEditor();
	}

	public IClientEvent onClientEvent;

	public PropertyPanel() {
		setLayout(new BorderLayout(0, 0));
		scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);
		table = new JTable() {
			private static final long serialVersionUID = 1L;

			public boolean editCellAt(int row, int column) {
				return editCellAt(row, column, null);
			}
		};
		defaultTableModel = new DefaultTableModel() {
			private static final long serialVersionUID = -4081488768631957930L;

			public boolean isCellEditable(int row, int col) {
				if (col == 0)
					return false;
				else {
					onClientEvent.onEdit(row, col);
					return true;
				}
			}
		};
		// using shared column name.
		defaultTableModel.addColumn(this.colNames[0]);
		defaultTableModel.addColumn(this.colNames[1]);
		table.setModel(defaultTableModel);
		table.setRowHeight(30);
		defaultTableColumnModel = new DefaultTableColumnModel();
		// do not use the different object as the header.
		tableColumn_property.setHeaderValue(this.colNames[0]);
		tableColumn_property.setPreferredWidth(60);
		tableColumn_property.setMinWidth(60);
		tableColumn_value.setHeaderValue(this.colNames[1]);
		tableColumn_value.setPreferredWidth(150);
		tableColumn_value.setMinWidth(60);
		tableColumn_value.setCellRenderer(new CustomRenderTable());
		tableColumn_value.setCellEditor(propertyTableCellEditor);
		defaultTableColumnModel.addColumn(tableColumn_property);
		defaultTableColumnModel.addColumn(tableColumn_value);
		table.setColumnModel(defaultTableColumnModel);
		propertyTableCellEditor.setTable(table);
		propertyTableCellEditor.onClientEvent = new IClientEvent() {
			@Override
			public void onClick(JComponent sender, int row, int col, ISetValue onSetValue) {
				onClientEvent.onClick(sender, row, col, onSetValue);
			}

			@Override
			public boolean onUpateValue(JComponent sender, int row, int col, Object value) {
				return onClientEvent.onUpateValue(sender, row, col, value);
			}

			@Override
			public void onEdit(int row, int col) {
				onClientEvent.onEdit(row, col);
			}

		};

		// table.setColumnControlVisible(true);
		table.setGridColor(Color.LIGHT_GRAY);
		table.setShowGrid(true);
		table.setCellSelectionEnabled(true);
		table.setRowSelectionAllowed(true);
		table.setColumnSelectionAllowed(false);
		table.getTableHeader().setReorderingAllowed(false);

		table.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.isAltDown())
					e.consume();
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.isAltDown())
					e.consume();
			}
		});
		// table.setSortable(false);
		// make it is possible to copy from or paste data in table into excel.

	}

	public void clearPropertyTable() {
		// clear all the rows in table.
		if (table.isEditing()) {
			propertyTableCellEditor.doAction(
					table.getCellEditor().getTableCellEditorComponent(table, null, true, table.getSelectedRow(), 1));
			if (table.isEditing())
				table.getCellEditor().stopCellEditing();
		}

		this.defaultTableModel.setRowCount(0);
		// clear all the cell editor.
		this.propertyTableCellEditor.clear();
	}

	@SuppressWarnings("rawtypes")
	public void showPropertyTable(Collection<IProperty> properties) {
		if (properties == null)
			return;

		clearPropertyTable();

		for (IProperty property : properties) {
			JComponent editor = property.getEditor();
			if (editor == null)
				continue;

			Object[] rowData = new Object[2];
			rowData[0] = property.getName();
			rowData[1] = property.getValue();
			this.defaultTableModel.addRow(rowData);
			int row = this.defaultTableModel.getRowCount() - 1;
			if (editor instanceof JTextField) {
				propertyTableCellEditor.addTableCellEditor((JTextField) editor, row);
			} else if (editor instanceof JTextArea) {
				propertyTableCellEditor.addTableCellEditor((JTextArea) editor, row);
			} else if (editor instanceof JCheckBox) {
				propertyTableCellEditor.addTableCellEditor((JCheckBox) editor, row);
			} else if (editor instanceof MulitCombobox) {
				propertyTableCellEditor.addTableCellEditor((MulitCombobox) editor, row);
			} else if (editor instanceof JComboBox) {
				propertyTableCellEditor.addTableCellEditor((JComboBox) editor, row);
			} else if (editor instanceof ButtonColumn.ButtonLabel) {
				propertyTableCellEditor.addTableCellEditor((ButtonColumn.ButtonLabel) editor, row);
			} else if (editor instanceof JSpinner) {
				propertyTableCellEditor.addTableCellEditor((JSpinner) editor, row);
			} else {
				propertyTableCellEditor.addTableCellEditor((JLabel) editor, row);
			}
		}
		scrollPane.setViewportView(table);
		initDataBindings();
	}

	protected void initDataBindings() {
	}

	public JTable getTable() {
		return table;
	}

	public Object getValue(int row, int col){
		return table.getValueAt(row, col);
	}
	
	public String getName(int row) {
		return (String) table.getValueAt(row, 0);
	}

	public Object getValue(int row) {
		return table.getValueAt(row, 1);
	}
}
