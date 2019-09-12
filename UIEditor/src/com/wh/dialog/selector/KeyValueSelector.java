package com.wh.dialog.selector;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import org.json.JSONException;

import com.wh.control.EditorEnvironment;
import com.wh.control.grid.GridCellEditor;
import com.wh.control.grid.GridCellEditor.ActionResult;
import com.wh.control.grid.GridCellEditor.ButtonActionListener;
import com.wh.control.grid.design.PropertyTableCellEditor.KeyValue;
import com.wh.dialog.editor.JsonEditorDialog;
import com.wh.form.IMainControl;
import com.wh.system.tools.JsonHelp;

public class KeyValueSelector extends JDialog {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTable table;

	enum DialogResult {
		drOk, drCancel, drNull
	}

	enum SelectType {
		stOne, stAll
	}

	public interface IActionListener {
		ActionResult onAction(javax.swing.table.TableModel model, String key, Object value, int row, int col,
				List<Object> selects);
	}

	public interface ICheckValue {
		public boolean onCheck(Object[][] originalData, JTable table);
	}

	public interface IEditRow {
		public boolean deleteRow(JTable table, Vector<?> row);

		public Object[] addRow(JTable table);

		public void updateRow(JTable table, Vector<?> row);
	}

	SelectType sType = SelectType.stAll;

	ICheckValue onCheckValue;

	DialogResult dr = DialogResult.drCancel;

	IMainControl mainControl;

	IActionListener actionListener;

	boolean fireOnCheck() {
		if (onCheckValue != null) {
			return onCheckValue.onCheck(originalData, table);
		} else
			return true;
	}

	/**
	 * Create the frame.
	 */
	public KeyValueSelector(JComponent parent, IMainControl mainControl) {
		this.mainControl = mainControl;
		setResizable(false);
		setModal(true);
		setBounds(100, 100, 611, 501);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				setVisible(false);
			}

		});
		buttons = new JPanel();
		contentPane.add(buttons, BorderLayout.SOUTH);

		JButton okButton = new JButton("确定");
		okButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (table.isEditing()) {
					if (table.getEditorComponent() instanceof JComboBox) {
						int row = table.getSelectedRow();
						int col = table.getSelectedColumn();
						Object value = table.getValueAt(row, col);
						table.getCellEditor().cancelCellEditing();
						table.setValueAt(value, row, col);
					} else
						table.getCellEditor().stopCellEditing();
				}

				if (sType == SelectType.stOne && table.getSelectedRow() == -1) {
					EditorEnvironment.showMessage(KeyValueSelector.this, "请先选择一项数据！", "提示",
							JOptionPane.INFORMATION_MESSAGE);
					return;
				}

				if (!fireOnCheck())
					return;

				dr = DialogResult.drOk;
				dispose();
			}
		});

		JButton button = new JButton("添加");
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultTableModel model = (DefaultTableModel) table.getModel();
				if (iEditRow == null)
					model.setRowCount(model.getRowCount() + 1);
				else {
					Object[] row = iEditRow.addRow(table);
					if (row != null) {
						addRow(row);
					}
				}
			}
		});
		buttons.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		buttons.add(button);

		JButton button_1 = new JButton("删除");
		button_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (table.getSelectedRow() == -1)
					return;

				if (EditorEnvironment.showConfirmDialog("是否删除选定的条目？", "",
						JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
					return;
				DefaultTableModel model = (DefaultTableModel) table.getModel();

				int index = table.getSelectedRow();
				Vector<?> row = (Vector<?>) model.getDataVector().get(index);
				if (iEditRow != null) {
					if (!iEditRow.deleteRow(table, row))
						return;
				}
				model.removeRow(index);
			}
		});
		buttons.add(button_1);

		JButton button_2 = new JButton("删除字符值");
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (table.getSelectedRow() != -1) {
					int col = table.getSelectedColumn();
					Object value = table.getValueAt(table.getSelectedRow(), col);
					if (value instanceof String || value instanceof KeyValue) {
						table.setValueAt(null, table.getSelectedRow(), col);
					}
				}
			}
		});
		button_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		buttons.add(button_2);

		JSeparator separator = new JSeparator();
		separator.setOrientation(SwingConstants.VERTICAL);
		separator.setPreferredSize(new Dimension(0, 10));
		buttons.add(separator);
		buttons.add(okButton);

		JButton cancelButton = new JButton("取消");
		cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dr = DialogResult.drCancel;
				dispose();
			}
		});
		buttons.add(cancelButton);

		btnNull = new JButton("null返回");
		btnNull.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (sType != SelectType.stOne) {
					EditorEnvironment.showMessage("非单选模式不可以null返回！");
					return;
				}

				if (!fireOnCheck())
					return;

				dr = DialogResult.drNull;
				dispose();
			}
		});
		btnNull.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		buttons.add(btnNull);

		JScrollPane scrollPane = new JScrollPane();
		contentPane.add(scrollPane);

		table = new JTable();
		table.setRowHeight(40);
		table.setFont(new Font("微软雅黑", Font.PLAIN, 14));
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setSurrendersFocusOnKeystroke(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane.setViewportView(table);

		setLocationRelativeTo(null);
	}

	class TableModel extends DefaultTableModel {
		private static final long serialVersionUID = 1L;

		Object[] columns;
		HashMap<Integer, Integer> readOnlyColumns = new HashMap<>();

		public TableModel(Object[] columns, int[] readOnlyColumns) {
			super(new Object[][] {}, columns);
			this.columns = columns;
			if (readOnlyColumns != null)
				for (int i = 0; i < readOnlyColumns.length; i++) {
					this.readOnlyColumns.put(readOnlyColumns[i], readOnlyColumns[i]);
				}
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return !readOnlyColumns.containsKey(columnIndex);
		}

		public void setValueAt(Object aValue, int row, int column) {
			Object object = getValueAt(row, column);
			if (object != aValue || (object != null && aValue != null && !object.equals(aValue)))
				super.setValueAt(aValue, row, column);
		}
	}

	/***
	 * 
	 * @param columns
	 *            列名称列表
	 * @param multipleSelection
	 *            表格是否可以多选
	 * @param readOnlyColumns
	 *            固定列的索引列表
	 * @param columnTypes
	 *            参见KeyValueGridRender的columnType参数说明
	 * 
	 */
	void init(Object[] columns, boolean multipleSelection, int[] readOnlyColumns, Object[] columnTypes) {
		table.setSelectionMode(multipleSelection ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
				: ListSelectionModel.SINGLE_SELECTION);
		DefaultTableModel tableModel = new TableModel(columns, readOnlyColumns);
		tableModel.addTableModelListener(new TableModelListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void tableChanged(TableModelEvent e) {
				switch (e.getType()) {
				case TableModelEvent.INSERT:
					break;
				case TableModelEvent.UPDATE:
					int index = e.getLastRow();
					DefaultTableModel model = ((DefaultTableModel) table.getModel());
					if (index >= model.getRowCount() || index < 0)
						return;

					Vector<Object> rows = (Vector<Object>) model.getDataVector();
					if (rows.size() == 0)
						return;
					Vector<Object> row = (Vector<Object>) rows.get(index);
					if (iEditRow != null) {
						iEditRow.updateRow(table, row);
					}
					break;
				case TableModelEvent.DELETE:
					break;
				}
			}
		});
		table.setModel(tableModel);
		Map<Integer, Integer> readColumns = new HashMap<>();
		if (readOnlyColumns != null)
			for (int index : readOnlyColumns) {
				readColumns.put(index, index);
			}

		for (int i = 0; i < columns.length; i++) {
			if (!readColumns.containsKey(i)) {
				Object columnType = columnTypes == null ? null : columnTypes[i];
				table.getColumnModel().getColumn(i).setCellEditor(
						new GridCellEditor(table.getColumnName(i), i, "编辑", columnType, new ButtonActionListener() {
							@Override
							public ActionResult actionPerformed(ActionEvent e, String columnName, int columnIndex) {
								if (actionListener != null) {

									int row = table.convertRowIndexToModel(table.getSelectedRow());
									int col = table.convertColumnIndexToModel(table.getSelectedColumn());
									Object key = table.getValueAt(row, 0);
									if (key != null)
										key = key.toString();
									else
										key = null;
									return actionListener.onAction(table.getModel(), (String) key,
											table.getValueAt(row, columnIndex), row, col, null);
								}
								return new ActionResult();
							}
						}, new GridCellEditor.UpdateValueListener() {

							@Override
							public void onUpdateValue(Object value, String columnName, int columnIndex) {
								tableModel.setValueAt(value, table.getSelectedRow(), columnIndex);
							}
						}, new GridCellEditor.InitComboBoxListener() {

							@Override
							public void init(String columnName, int columnIndex, JComboBox<Object> comboBox) {
								if (actionListener != null) {
									List<Object> list = new ArrayList<>();
									int row = table.convertRowIndexToModel(table.getSelectedRow());
									int col = table.convertColumnIndexToModel(table.getSelectedColumn());
									Object key = table.getValueAt(row, 0);
									actionListener.onAction(table.getModel(), key == null ? null : key.toString(),
											table.getValueAt(row, columnIndex), row, col, list);

									DefaultComboBoxModel<Object> model = (DefaultComboBoxModel<Object>) comboBox
											.getModel();
									model.removeAllElements();
									for (Object object : list) {
										comboBox.addItem(object);
									}
								}
							}
						}));
			}
		}

		setLocationRelativeTo(null);
	}

	Object[][] originalData;

	void addRow(Object[] row) {
		GridCellEditor.VectorEx data = new GridCellEditor.VectorEx();
		data.addAll(Arrays.asList(row));
		data.ordData = row;
		((DefaultTableModel) table.getModel()).addRow(data);
	}

	void init(Object[][] datas, Object[] columns, Object[] columnType, boolean multipleSelection,
			int[] readOnlyColumns) {
		originalData = datas;

		init(columns, multipleSelection, readOnlyColumns, columnType);
		TableModel tableModel = (TableModel) table.getModel();
		if (datas != null)
			for (int i = 0; i < datas.length; i++) {
				addRow(datas[i]);
			}
		tableModel.fireTableDataChanged();
	}

	Object[] getResult() {
		return (Object[]) getResult(null);
	}

	Object getResult(Integer index) {
		DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
		if (dr != DialogResult.drOk)
			return null;

		if (index == null) {
			Object obj = tableModel.getDataVector().get(table.getSelectedRow());
			if (obj instanceof Vector<?>) {
				Vector<?> vector = (Vector<?>) obj;
				return vector.toArray();
			} else
				return obj;
		} else
			return tableModel.getValueAt(table.getSelectedRow(), index);
	}

	IEditRow iEditRow;

	static IActionListener defaultActionListener;
	private JPanel buttons;
	private JButton btnNull;

	public static IActionListener getDefaultActionListener(IMainControl mainControl) {
		if (defaultActionListener == null) {
			defaultActionListener = new IActionListener() {

				@Override
				public ActionResult onAction(javax.swing.table.TableModel model, String key, Object v, int row, int col,
						List<Object> selects) {
					String value = (String) v;
					Object obj = null;
					if (value != null && !value.isEmpty()) {
						try {
							obj = JsonHelp.parseJson(value);
						} catch (JSONException e1) {
							e1.printStackTrace();
							obj = null;
						}
					}
					Object newobj = JsonEditorDialog.show(mainControl, obj);

					ActionResult result = new ActionResult();
					result.isok = newobj != null;
					result.data = newobj;
					return result;
				}
			};
		}

		return defaultActionListener;
	}

	public static abstract class Result {
		public boolean isok = false;
	}

	public static class ModelResult extends Result {
		public DefaultTableModel model;

		public ModelResult(DefaultTableModel model, boolean isok) {
			this.model = model;
			this.isok = isok;
		}
	}

	public static class RowResult extends Result {
		public Object[] row;

		public RowResult(Object[] row, boolean isok) {
			this.row = row;
			this.isok = isok;
		}
	}

	/**
	 * 显示表格数据编辑框，并返回选中的行记录
	 * 
	 * @param mainControl
	 *            控制对象
	 * @param datas
	 *            行数据集合，如果单行数据数目大于列数，表示从后到前的编辑框要求，比如类定义为["a1","a2"],
	 *            行数据为["a1v","a2v",new ArrayList(), new
	 *            JButton()]，则表示此行数据的a1单元格使用下拉列表编辑，a2单元格使用按钮编辑
	 * @param columns
	 *            列定义集合，使用toString()方法显示列头
	 * @return
	 */
	public static RowResult showForOne(IMainControl mainControl, Object[][] datas, Object[] columns) {
		return showForOne(null, mainControl, null, null, datas, columns, null);
	}

	/**
	 * 显示表格数据编辑框，并返回选中的行记录
	 * 
	 * @param parent
	 *            父组件
	 * @param mainControl
	 *            控制对象
	 * @param onCheckValue
	 *            当按【确定】按钮时候调用，返回true本次确认操作有效，其他无效
	 * @param iEditRow
	 *            当编辑行数据时触发
	 * @param datas
	 *            行数据集合，如果单行数据数目大于列数，表示从后到前的编辑框要求，比如类定义为["a1","a2"],
	 *            行数据为["a1v","a2v",new ArrayList(), new
	 *            JButton()]，则表示此行数据的a1单元格使用下拉列表编辑，a2单元格使用按钮编辑
	 * @param columns
	 *            列定义集合，使用toString()方法显示列头
	 * @param readOnlyColumns
	 *            只读列的定义集合
	 * @return
	 */
	public static RowResult showForOne(JComponent parent, IMainControl mainControl, ICheckValue onCheckValue,
			IEditRow iEditRow, Object[][] datas, Object[] columns, int[] readOnlyColumns) {
		return showForOne(parent, mainControl, onCheckValue, iEditRow, datas, columns, readOnlyColumns,
				getDefaultActionListener(mainControl));
	}

	/**
	 * 显示表格数据编辑框
	 * 
	 * @param parent
	 *            父组件
	 * @param mainControl
	 *            控制对象
	 * @param onCheckValue
	 *            当按【确定】按钮时候调用，返回true本次确认操作有效，其他无效
	 * @param iEditRow
	 *            当编辑行数据时触发
	 * @param datas
	 *            行数据集合，如果单行数据数目大于列数，表示从后到前的编辑框要求，比如类定义为["a1","a2"],
	 *            行数据为["a1v","a2v",new ArrayList(), new
	 *            JButton()]，则表示此行数据的a1单元格使用下拉列表编辑，a2单元格使用按钮编辑
	 * @param columns
	 *            列定义集合，使用toString()方法显示列头
	 * @param readOnlyColumns
	 *            只读列的定义集合
	 * @param actionListener
	 *            单击及下拉列表项目初始事件，如果datas传入的列表为0会调用此事件初始列表
	 * @return
	 */
	public static RowResult showForOne(JComponent parent, IMainControl mainControl, ICheckValue onCheckValue,
			IEditRow iEditRow, Object[][] datas, Object[] columns, int[] readOnlyColumns,
			IActionListener actionListener) {
		RowResult row = (RowResult) internalShow(parent, mainControl, onCheckValue, iEditRow, datas, columns, null,
				readOnlyColumns, actionListener, false, true);
		return row;
	}

	/**
	 * 显示表格数据编辑框
	 * 
	 * @param parent
	 *            父组件
	 * @param mainControl
	 *            控制对象
	 * @param onCheckValue
	 *            当按【确定】按钮时候调用，返回true本次确认操作有效，其他无效
	 * @param iEditRow
	 *            当编辑行数据时触发
	 * @param datas
	 *            行数据集合，如果单行数据数目大于列数，表示从后到前的编辑框要求，比如类定义为["a1","a2"],
	 *            行数据为["a1v","a2v",new ArrayList(), new
	 *            JButton()]，则表示此行数据的a1单元格使用下拉列表编辑，a2单元格使用按钮编辑
	 * @param columns
	 *            列定义集合，使用toString()方法显示列头
	 * @param columnTypes
	 *            列的编辑类型，如果datas也设置了，优先使用datas设置
	 * @param readOnlyColumns
	 *            只读列的定义集合
	 * @param multipleSelection
	 *            是否多选
	 * @return
	 */
	public static ModelResult show(JComponent parent, IMainControl mainControl, ICheckValue onCheckValue,
			IEditRow iEditRow, Object[][] datas, Object[] columns, Object[] columnTypes, int[] readOnlyColumns,
			boolean multipleSelection) {
		return show(parent, mainControl, onCheckValue, iEditRow, datas, columns, columnTypes, readOnlyColumns,
				getDefaultActionListener(mainControl), multipleSelection);
	}

	/**
	 * 显示表格数据编辑框
	 * 
	 * @param parent
	 *            父组件
	 * @param mainControl
	 *            控制对象
	 * @param onCheckValue
	 *            当按【确定】按钮时候调用，返回true本次确认操作有效，其他无效
	 * @param iEditRow
	 *            当编辑行数据时触发
	 * @param datas
	 *            行数据集合，如果单行数据数目大于列数，表示从后到前的编辑框要求，比如类定义为["a1","a2"],
	 *            行数据为["a1v","a2v",new ArrayList(), new
	 *            JButton()]，则表示此行数据的a1单元格使用下拉列表编辑，a2单元格使用按钮编辑
	 * @param columns
	 *            列定义集合，使用toString()方法显示列头
	 * @param columnTypes
	 *            列的编辑类型，如果datas也设置了，优先使用datas设置
	 * @param readOnlyColumns
	 *            只读列的定义集合
	 * @param actionListener
	 *            单击及下拉列表项目初始事件，如果datas传入的列表为0会调用此事件初始列表
	 * @param multipleSelection
	 *            是否多选
	 * @return
	 */
	public static ModelResult show(JComponent parent, IMainControl mainControl, ICheckValue onCheckValue,
			IEditRow iEditRow, Object[][] datas, Object[] columns, Object[] columnTypes, int[] readOnlyColumns,
			IActionListener actionListener, boolean multipleSelection) {
		return (ModelResult) internalShow(parent, mainControl, onCheckValue, iEditRow, datas, columns, columnTypes,
				readOnlyColumns, actionListener, multipleSelection, false);
	}

	/**
	 * 显示表格数据编辑框
	 * 
	 * @param parent
	 *            父组件
	 * @param mainControl
	 *            控制对象
	 * @param onCheckValue
	 *            当按【确定】按钮时候调用，返回true本次确认操作有效，其他无效
	 * @param iEditRow
	 *            当编辑行数据时触发
	 * @param datas
	 *            行数据集合，如果单行数据数目大于列数，表示从后到前的编辑框要求，比如类定义为["a1","a2"],
	 *            行数据为["a1v","a2v",new ArrayList(), new
	 *            JButton()]，则表示此行数据的a1单元格使用下拉列表编辑，a2单元格使用按钮编辑
	 * @param columns
	 *            列定义集合，使用toString()方法显示列头
	 * @param columnTypes
	 *            列的编辑类型，如果datas也设置了，优先使用datas设置
	 * @param readOnlyColumns
	 *            只读列的定义集合
	 * @param actionListener
	 *            单击及下拉列表项目初始事件，如果datas传入的列表为0会调用此事件初始列表
	 * @param multipleSelection
	 *            是否多选
	 * @param onlyReturnRow
	 *            是否按行返回，true按行返回，其他按model返回
	 * @return
	 */
	static Result internalShow(JComponent parent, IMainControl mainControl, ICheckValue onCheckValue, IEditRow iEditRow,
			Object[][] datas, Object[] columns, Object[] columnTypes, int[] readOnlyColumns,
			IActionListener actionListener, boolean multipleSelection, boolean onlyReturnRow) {
		KeyValueSelector tableDialog = new KeyValueSelector(parent, mainControl);
		tableDialog.actionListener = actionListener;
		tableDialog.sType = onlyReturnRow ? SelectType.stOne : SelectType.stAll;
		if (!onlyReturnRow)
			tableDialog.buttons.remove(tableDialog.btnNull);
		tableDialog.init(datas, columns, columnTypes, multipleSelection, readOnlyColumns);
		tableDialog.onCheckValue = onCheckValue;
		tableDialog.iEditRow = iEditRow;
		tableDialog.setModal(true);
		tableDialog.setVisible(true);

		if (tableDialog.dr != DialogResult.drOk) {
			tableDialog.dispose();
			if (!onlyReturnRow)
				return new ModelResult(null, false);
			else
				return new RowResult(null, tableDialog.dr == DialogResult.drNull);
		}

		if (!onlyReturnRow) {
			DefaultTableModel model = (DefaultTableModel) tableDialog.table.getModel();
			tableDialog.dispose();
			return new ModelResult(model, true);
		} else {
			Object result = tableDialog.getResult(null);
			tableDialog.dispose();
			return new RowResult((Object[]) result, true);
		}
	}

}
