package com.wh.dialog.input;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

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
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wh.control.EditorEnvironment;
import com.wh.control.grid.GridCellEditor;
import com.wh.control.grid.GridCellEditor.ActionResult;
import com.wh.control.grid.GridCellEditor.ButtonActionListener;
import com.wh.control.grid.design.PropertyTableCellEditor.KeyValue;
import com.wh.dialog.editor.JsonEditorDialog;
import com.wh.form.IMainControl;
import com.wh.system.tools.JsonHelp;

public class TableInput extends JDialog {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTable table;

	enum DialogResult {
		drOk, drCancel
	}

	enum SelectType {
		stEdit, stSelect
	}

	public interface IActionListener {
		ActionResult onAction(String key, Object value, List<Object> selects);
	}

	public interface ICheckValue {
		public boolean onCheck(Object[][] originalData, javax.swing.table.TableModel tableModel);
	}

	public interface IEditRow {
		public boolean allowEditCell(ColumnInfo column, int row);

		public Object[] addRow(JTable table);

		public boolean allowDelete(Vector<?> row);

		public void updateRow(Vector<?> row);

	}

	public static class EditRowAdapter implements IEditRow{

		@Override
		public boolean allowEditCell(ColumnInfo column, int row) {
			return false;
		}

		@Override
		public Object[] addRow(JTable table) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean allowDelete(Vector<?> row) {
			// TODO Auto-generated method stub
			return true;
		}

		@Override
		public void updateRow(Vector<?> row) {
			// TODO Auto-generated method stub
			
		}
		
	}
	HashMap<String, Vector<?>> changeRows = new HashMap<>();

	SelectType sType = SelectType.stSelect;

	ICheckValue onCheckValue;

	DialogResult dr = DialogResult.drCancel;

	boolean fireOnCheck() {
		if (onCheckValue != null) {
			return onCheckValue.onCheck(originalData, table.getModel());
		} else
			return true;
	}

	/**
	 * Create the frame.
	 */
	TableInput(JComponent parent) {
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
		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.SOUTH);

		JButton okButton = new JButton("确定");
		okButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (table.isEditing()) {
					if (table.getEditorComponent() instanceof JComboBox) {
						int row = table.getSelectedRow();
						Object value = table.getValueAt(row, 1);
						table.getCellEditor().cancelCellEditing();
						table.setValueAt(value, row, 1);
					} else
						table.getCellEditor().stopCellEditing();
				}

				if (sType == SelectType.stSelect && table.getSelectedRow() == -1) {
					EditorEnvironment.showMessage(TableInput.this, "请先选择一项数据！", "提示",
							JOptionPane.INFORMATION_MESSAGE);
					return;
				}

				if (sType == SelectType.stEdit && !fireOnCheck())
					return;

				dr = DialogResult.drOk;
				dispose();
			}
		});

		addButton = new JButton("添加");
		addButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultTableModel model = (DefaultTableModel) table.getModel();
				if (iEditRow == null)
					model.setRowCount(model.getRowCount() + 1);
				else {
					Object[] row = iEditRow.addRow(table);
					if (row != null) {
						model.addRow(row);
					}
				}
			}
		});
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		panel.add(addButton);

		deleteButton = new JButton("删除");
		deleteButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		deleteButton.addActionListener(new ActionListener() {
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
					if (!iEditRow.allowDelete(row)) {
						return;
					}
				}
				model.removeRow(index);
			}
		});
		panel.add(deleteButton);

		deleteStringButton = new JButton("删除字符值");
		deleteStringButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (table.getSelectedRow() != -1) {
					Object value = table.getValueAt(table.getSelectedRow(), 1);
					if (value instanceof String || value instanceof KeyValue) {
						table.setValueAt(null, table.getSelectedRow(), 1);
					}
				}
			}
		});
		deleteStringButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel.add(deleteStringButton);

		JSeparator separator = new JSeparator();
		separator.setOrientation(SwingConstants.VERTICAL);
		separator.setPreferredSize(new Dimension(0, 10));
		panel.add(separator);
		panel.add(okButton);

		JButton cancelButton = new JButton("取消");
		cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dr = DialogResult.drCancel;
				dispose();
			}
		});
		panel.add(cancelButton);

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

		HashMap<String, ColumnInfo> fixColumns = new HashMap<>();

		public TableModel(ColumnInfo[] columns, List<ColumnInfo> fixColumns) {
			super(new Object[][] {}, columns);
			if (fixColumns != null)
				for (ColumnInfo columnInfo : fixColumns) {
					this.fixColumns.put(columnInfo.getColumnName(), columnInfo);
				}
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			ColumnInfo info = (ColumnInfo) columnIdentifiers.get(columnIndex);
			return sType == SelectType.stEdit && !fixColumns.containsKey(info.getColumnName()) && 
					(iEditRow != null && iEditRow.allowEditCell(info, rowIndex));
		}

		public void setValueAt(Object aValue, int row, int column) {
			Object object = getValueAt(row, column);
			if (object != aValue || (object != null && aValue != null && !object.equals(aValue)))
				super.setValueAt(aValue, row, column);
		}
	}

	protected int getColumnIndex(String columnName) {
		for (int i = 0; i < table.getColumnCount(); i++) {
			if (table.getColumnName(i).equals(columnName))
				return table.convertColumnIndexToModel(i);
		}
		return -1;
	}

	/***
	 * 
	 * @param columns
	 *            列名称列表
	 * @param mulitSelect
	 *            表格是否可以多选
	 * @param fixColumns
	 *            固定列的索引列表
	 * @param columnType
	 *            参见KeyValueGridRender的columnType参数说明
	 * 
	 */
	void init(ColumnInfo[] columns, boolean mulitSelect) {
		table.setSelectionMode(
				mulitSelect ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION : ListSelectionModel.SINGLE_SELECTION);
		HashMap<String, ColumnInfo> columnMap = new HashMap<>();
		List<ColumnInfo> fixColumns = new ArrayList<>();
		for (ColumnInfo columnInfo : columns) {
			columnMap.put(columnInfo.getColumnName(), columnInfo);
			if (columnInfo.fix) {
				fixColumns.add(columnInfo);
			}
		}

		DefaultTableModel tableModel = new TableModel(columns, fixColumns);
		tableModel.addTableModelListener(new TableModelListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void tableChanged(TableModelEvent e) {
				if (sType != SelectType.stEdit)
					return;

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
						iEditRow.updateRow(row);
					}
					break;
				case TableModelEvent.DELETE:
					break;
				}
			}
		});

		table.setModel(tableModel);

		if (sType == SelectType.stEdit) {
			table.setAutoCreateRowSorter(false);
			TableColumnModel model = table.getColumnModel();
			for (int i = 0; i < model.getColumnCount(); i++) {
				TableColumn column = model.getColumn(i);
				String columnName = table.getColumnName(i);
				ColumnInfo columnInfo = columnMap.get(columnName);
				column.setCellEditor(
						new GridCellEditor(columnName, i, "编辑", columnInfo.editorType, new ButtonActionListener() {
							@Override
							public ActionResult actionPerformed(ActionEvent e, String columnName, int columnIndex) {
								if (columnInfo.actionListener == null)
									return new ActionResult();

								int index = getColumnIndex(columnName);
								return columnInfo.actionListener.onAction(columnName,
										table.getValueAt(table.getSelectedRow(), index), null);
							}
						}, null));
			}
		}
		setLocationRelativeTo(null);
	}

	Object[][] originalData;

	void init(Object[][] datas, ColumnInfo[] columns, boolean mulitSelect) {
		originalData = datas;
		init(columns, mulitSelect);
		TableModel tableModel = (TableModel) table.getModel();
		if (datas != null)
			for (int i = 0; i < datas.length; i++) {
				tableModel.addRow(datas[i]);
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

	private JButton addButton;
	private JButton deleteButton;
	private JButton deleteStringButton;

	public static class ColumnInfo {
		public Object name;
		public IActionListener actionListener;
		public Object userData;
		public Class<?> editorType = String.class;

		public boolean fix = false;
		public boolean autoSize = true;

		public ColumnInfo(Object name) {
			this.name = name;
		}

		public String getColumnName() {
			return name.toString();
		}

		@Override
		public String toString() {
			return getColumnName();
		}
	}

	public static String JSON_COLUMNS_KEY = "column";
	public static String JSON_DATA_KEY = "data";

	protected static ColumnInfo[] objectsToColumnInfos(Object[] columns) {
		ColumnInfo[] columnInfos = new ColumnInfo[columns.length];
		int index = 0;
		for (Object obj : columns) {
			ColumnInfo column = new ColumnInfo(obj.toString());
			columnInfos[index++] = column;
		}
		return columnInfos;
	}

	public static JSONObject createJsonParam(JSONArray columns, JSONArray rows) {
		JSONObject result = new JSONObject();
		result.put(JSON_COLUMNS_KEY, columns);
		result.put(JSON_DATA_KEY, rows);
		return result;
	}

	protected static ColumnInfo[] jsonToColumnInfos(JSONObject data) {
		JSONArray columns = data.getJSONArray(JSON_COLUMNS_KEY);
		ColumnInfo[] columnInfos = new ColumnInfo[columns.length()];
		int index = 0;
		for (Object obj : columns) {
			ColumnInfo column = new ColumnInfo(obj.toString());
			columnInfos[index++] = column;
		}

		return columnInfos;
	}

	protected static Object[][] jsonToRows(JSONObject data) {
		JSONArray columns = data.getJSONArray(JSON_COLUMNS_KEY);
		JSONArray datas = data.getJSONArray(JSON_DATA_KEY);
		Object[][] rows = new Object[datas.length()][columns.length()];
		for (int i = 0; i < datas.length(); i++) {
			JSONObject row = datas.getJSONObject(i);
			for (int j = 0; j < columns.length(); j++) {
				String key = columns.getString(j);
				if (row.has(key))
					rows[i][j] = row.get(key);
				else
					rows[i][j] = null;
			}
		}

		return rows;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected static JSONArray vectorToJSON(Vector data, ColumnInfo[] columns) {
		JSONArray resultData = new JSONArray();
		if (data != null && data.size() > 0)
			for (Object obj : (Vector) data) {
				Vector<Object> row = (Vector<Object>) obj;
				JSONObject rowData = new JSONObject();
				for (int i = 0; i < columns.length; i++) {
					rowData.put(columns[i].getColumnName(), row.get(i));
				}
				resultData.put(rowData);
			}
		return resultData;
	}

	/**
	 * 选择列表
	 * 
	 * @param parent
	 *            显示在那个组件中，可以为null
	 * @param datas
	 *            初始数据，格式如下：{column:[columnName], data:[{field:value}]}
	 * @return
	 */
	public static JSONObject showSelector(JComponent parent, JSONObject data) {
		JSONArray result = showMulitSelector(parent, data);
		if (result == null || result.length() == 0)
			return null;
		else
			return result.getJSONObject(0);
	}

	public static JSONArray showMulitSelector(JComponent parent, JSONObject data) {
		ColumnInfo[] columns = jsonToColumnInfos(data);
		Object[][] rows = jsonToRows(data);
		Vector<Vector<Object>> result = showMulitSelector(parent, rows, columns);

		if (result == null || result.size() == 0)
			return new JSONArray();
		else {

			return vectorToJSON(result, columns);
		}
	}

	public static Vector<Object> showSelector(JComponent parent, Object[][] datas, Object[] columns) {
		List<Vector<Object>> result = showMulitSelector(parent, datas, columns);
		if (result == null || result.size() == 0)
			return null;
		else
			return result.get(0);
	}

	public static List<Vector<Object>> showMulitSelector(JComponent parent, Object[][] datas, Object[] columns) {
		return showMulitSelector(parent, datas, objectsToColumnInfos(columns));
	}

	public static Vector<Object> showSelector(JComponent parent, Object[][] datas, ColumnInfo[] columns) {
		List<Vector<Object>> result = showMulitSelector(parent, datas, columns);
		if (result == null || result.size() == 0)
			return null;
		else
			return result.get(0);
	}

	@SuppressWarnings("unchecked")
	public static Vector<Vector<Object>> showMulitSelector(JComponent parent, Object[][] datas, ColumnInfo[] columns) {
		DefaultTableModel model = show(parent, null, null, datas, columns, true, false);
		if (model == null)
			return null;
		
		return model.getDataVector();
	}

	public static JSONArray showJsonEditor(JComponent parent, IMainControl mainControl, ICheckValue onCheckValue,
			IEditRow iEditRow, JSONObject data) {
		ColumnInfo[] columns = jsonToColumnInfos(data);
		Object[][] rows = jsonToRows(data);
		DefaultTableModel model = showJsonEditor(parent, mainControl, onCheckValue, iEditRow, rows, columns);
		if (model == null)
			return new JSONArray();
		else {
			return vectorToJSON(model.getDataVector(), columns);
		}
	}

	public static JSONArray showEditor(JComponent parent, ICheckValue onCheckValue, IEditRow iEditRow,
			JSONObject data) {
		ColumnInfo[] columns = jsonToColumnInfos(data);
		Object[][] rows = jsonToRows(data);
		DefaultTableModel model = showEditor(parent, onCheckValue, iEditRow, rows, columns);
		if (model == null)
			return new JSONArray();
		else {
			return vectorToJSON(model.getDataVector(), columns);
		}

	}

	public static DefaultTableModel showJsonEditor(JComponent parent, IMainControl mainControl,
			ICheckValue onCheckValue, IEditRow iEditRow, Object[][] datas, Object[] columns) {
		return showJsonEditor(parent, mainControl, onCheckValue, iEditRow, datas, objectsToColumnInfos(columns));

	}

	public static DefaultTableModel showEditor(JComponent parent, ICheckValue onCheckValue, IEditRow iEditRow,
			Object[][] datas, Object[] columns) {
		return showEditor(parent, onCheckValue, iEditRow, datas, objectsToColumnInfos(columns));

	}

	public static DefaultTableModel showJsonEditor(JComponent parent, IMainControl mainControl,
			ICheckValue onCheckValue, IEditRow iEditRow, Object[][] datas, ColumnInfo[] columns) {
		for (ColumnInfo columnInfo : columns) {
			columnInfo.actionListener = new IActionListener() {

				@Override
				public ActionResult onAction(String key, Object v, List<Object> selects) {
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
		return show(parent, onCheckValue, iEditRow, datas, columns, false, true);
	}

	public static DefaultTableModel showEditor(JComponent parent, ICheckValue onCheckValue,
			IEditRow iEditRow, Object[][] datas, ColumnInfo[] columns) {
		return show(parent, onCheckValue, iEditRow, datas, columns, false, true);
	}

	@SuppressWarnings("unchecked")
	protected static DefaultTableModel show(JComponent parent, ICheckValue onCheckValue, IEditRow iEditRow,
			Object[][] datas, ColumnInfo[] columns, boolean mulitSelect, boolean editable) {
		TableInput tableDialog = new TableInput(parent);

		tableDialog.sType = editable ? SelectType.stEdit : SelectType.stSelect;
		if (!editable) {
			tableDialog.addButton.setVisible(false);
			tableDialog.deleteButton.setVisible(false);
			tableDialog.deleteStringButton.setVisible(false);
		}

		tableDialog.init(datas, columns, mulitSelect);
		tableDialog.onCheckValue = onCheckValue;
		tableDialog.iEditRow = iEditRow;
		tableDialog.setModal(true);
		tableDialog.setVisible(true);
		if (tableDialog.dr != DialogResult.drOk) {
			tableDialog.dispose();
			return null;
		}

		DefaultTableModel model = (DefaultTableModel) tableDialog.table.getModel();
		if (!editable) {
			int[] selects = tableDialog.table.getSelectedRows();
			DefaultTableModel selectModels = new DefaultTableModel(columns, 0);
			for (int i = 0; i < selects.length; i++) {
				selectModels.addRow((Vector<Object>) model.getDataVector().elementAt(selects[i]));
			}
			model = selectModels;
		}
		tableDialog.dispose();

		return model;
	}

}
