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
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
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
import javax.swing.table.DefaultTableModel;

import org.json.JSONArray;
import org.json.JSONObject;

import com.wh.control.EditorEnvironment;
import com.wh.control.modelsearch.ModelSearchView;

public class TableSelector extends JDialog {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTable table;

	enum DialogResult {
		drOk, drCancel
	}

	DialogResult dr = DialogResult.drCancel;

	/**
	 * Create the frame.
	 */
	TableSelector(JComponent parent) {
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
				if (table.getSelectedRow() == -1) {
					EditorEnvironment.showMessage(TableSelector.this, "请先选择一项数据！", "提示",
							JOptionPane.INFORMATION_MESSAGE);
					return;
				}

				dr = DialogResult.drOk;
				dispose();
			}
		});
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

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

		table = new ModelSearchView.TableModelSearchView();
		table.setRowHeight(40);
		table.setFont(new Font("微软雅黑", Font.PLAIN, 14));
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setSurrendersFocusOnKeystroke(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane.setViewportView(table);

		setLocationRelativeTo(null);
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
	void init(ColumnInfo[] columns, Object[][] rows, boolean mulitSelect) {
	}

	void init(Object[][] datas, ColumnInfo[] columns, boolean mulitSelect) {
		table.setSelectionMode(
				mulitSelect ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION : ListSelectionModel.SINGLE_SELECTION);
		DefaultTableModel tableModel = new DefaultTableModel(datas, columns);

		table.setModel(tableModel);

		setLocationRelativeTo(null);
	}

	@SuppressWarnings("rawtypes")
	DefaultTableModel getResult() {
		DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
		if (dr != DialogResult.drOk)
			return null;

		Object[] names = new Object[tableModel.getColumnCount()];
		for (int i = 0; i < tableModel.getColumnCount(); i++) {
			names[i] = tableModel.getColumnName(i);
		}
		DefaultTableModel result = new DefaultTableModel(names, 0);
		for (int row : table.getSelectedRows()) {
			result.addRow((Vector) tableModel.getDataVector().get(table.convertRowIndexToModel(row)));
		}
		return result;
	}

	public static class ColumnInfo {
		public Object name;
		public Object userData;
		public Class<?> editorType = String.class;

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

	public static JSONObject showSelector(JComponent parent, JSONArray data) {
		JSONArray result = showMulitSelector(parent, data);
		if (result == null || result.length() == 0)
			return null;
		else
			return result.getJSONObject(0);
	}

	public static JSONArray showMulitSelector(JComponent parent, JSONArray data) {
		JSONObject jsondata = new JSONObject();
		HashMap<String, String> columns = new HashMap<>();
		for (Object object : data) {
			JSONObject row = (JSONObject) object;
			for (Object object2 : row.names()) {
				String column = (String) object2;
				if (column == null || column.isEmpty())
					continue;
				if (!columns.containsKey(column))
					columns.put(column, column);
			}
		}
		
		jsondata.put(JSON_COLUMNS_KEY, new JSONArray(columns.keySet()));
		jsondata.put(JSON_DATA_KEY, data);
		ColumnInfo[] jsoncolumns = jsonToColumnInfos(jsondata);
		Object[][] rows = jsonToRows(jsondata);
		List<HashMap<String, Object>> result = showMulitSelector(parent, rows, jsoncolumns);

		if (result == null || result.size() == 0)
			return new JSONArray();
		else {

			return new JSONArray(result);
		}
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
		List<HashMap<String, Object>> result = showMulitSelector(parent, rows, columns);

		if (result == null || result.size() == 0)
			return new JSONArray();
		else {

			return new JSONArray(result);
		}
	}

	public static HashMap<String, Object> showSelector(JComponent parent, Object[][] datas, Object[] columns) {
		List<HashMap<String, Object>> result = showMulitSelector(parent, datas, columns);
		if (result == null || result.size() == 0)
			return null;
		else
			return result.get(0);
	}

	public static List<HashMap<String, Object>> showMulitSelector(JComponent parent, Object[][] datas, Object[] columns) {
		return showMulitSelector(parent, datas, objectsToColumnInfos(columns));
	}

	public static HashMap<String, Object> showSelector(JComponent parent, Object[][] datas, ColumnInfo[] columns) {
		List<HashMap<String, Object>> result = showMulitSelector(parent, datas, columns);
		if (result == null || result.size() == 0)
			return null;
		else
			return result.get(0);
	}

	public static List<HashMap<String, Object>> showMulitSelector(JComponent parent, Object[][] datas, ColumnInfo[] columns) {
		DefaultTableModel model = show(parent, datas, columns, true);
		if (model == null)
			return null;

		List<HashMap<String, Object>> result = new ArrayList<>();
		for (int i = 0; i < model.getRowCount(); i++) {
			HashMap<String, Object> row = new HashMap<>();
			for (int j = 0; j < model.getColumnCount(); j++) {
				row.put(model.getColumnName(j), model.getValueAt(i, j));
			}
			result.add(row);
		}
		
		return result;
	}

	protected static DefaultTableModel show(JComponent parent, Object[][] datas, ColumnInfo[] columns,
			boolean mulitSelect) {
		TableSelector tableDialog = new TableSelector(parent);

		tableDialog.init(datas, columns, mulitSelect);
		tableDialog.setModal(true);
		tableDialog.setVisible(true);
		if (tableDialog.dr != DialogResult.drOk) {
			tableDialog.dispose();
			return null;
		}

		DefaultTableModel model = (DefaultTableModel) tableDialog.getResult();
		tableDialog.dispose();

		return model;
	}

}
