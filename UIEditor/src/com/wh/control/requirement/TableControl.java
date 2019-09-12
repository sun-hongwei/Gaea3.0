package com.wh.control.requirement;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import org.jdesktop.swingx.JXDatePicker;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wh.control.EditorEnvironment;
import com.wh.control.grid.ColorTableRowRender;
import com.wh.control.grid.JsonArrayTableModel;
import com.wh.system.tools.JsonHelp;

public class TableControl {
	JSONObject versionTree = new JSONObject();
	JSONObject dependTree = new JSONObject();
	public boolean isEdit = false;

	public TreeControl treeControl;
	public JTable table;
	public JTextArea requirementText;
	public JCheckBox usedView;
	public JTextField idView;
	public JTextField versionView;
	public JTextField userView;
	public JSpinner levelView;
	public JComboBox<String> typeView;
	public JCheckBox showVersion;
	public JCheckBox showDepend;
	public JCheckBox closeView;
	public JList<Object> roleView;
	public JComboBox<String> versionFilterView;
	public JCheckBox copyAdd;
	public JTextField mainVersion;
	public JComboBox<String> classView;
	public JXDatePicker planEndTime;
	public JXDatePicker closeTime;
	public JXDatePicker useTime;

	public ActionListener comboBoxActionListener = new ActionListener() {

		@SuppressWarnings("unchecked")
		@Override
		public void actionPerformed(ActionEvent e) {
			JComboBox<String> comboBox = (JComboBox<String>) e.getSource();
			try {
				if (comboBox.getSelectedItem() == null)
					setFilter(RequirementDefines.field_class, null);
				else
					setFilter(RequirementDefines.field_class, comboBox.getSelectedItem());
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	};

	protected boolean isEmptyRow() {
		JsonArrayTableModel tableModel = (JsonArrayTableModel) table.getModel();
		return tableModel.isEmptyRow(table.getSelectedRow());
	}

	public void setClasses(String[] cs) {
		DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) classView.getModel();
		model.addElement(null);
		for (String string : cs) {
			model.addElement(string);
		}
		classView.setModel(model);
	}

	public void setMainVersion(String version) {
		if (mainVersion == null)
			return;
		
		if (mainVersion != null && version != null)
			mainVersion.setText(version.trim());
		else
			mainVersion.setText("");
	}

	public void setDependColors(int row) {
		try {
			setColors(null, Color.GREEN, -1, true);
			if (row == -1)
				return;

			if (showDepend.isSelected())
				setColors(RequirementDefines.field_depend, Color.GREEN, row, false);
			if (showVersion.isSelected())
				setColors(RequirementDefines.field_depend_version, Color.YELLOW, row, false);
		} finally {
			table.repaint();
		}
	}

	public void setColors(String key, Color color, int row, boolean reset) {
		if (reset)
			clearColor();

		if (row == -1)
			return;

		JsonArrayTableModel model = (JsonArrayTableModel)table.getModel();
		try {
			JSONArray ids = (JSONArray) model.getValueAt(row, key);
			for (int i = 0; i < ids.length(); i++) {
				int colorRow = getRowIndex(ids.getString(i), false);
				colorRow = table.convertRowIndexToView(colorRow);
				if (colorRow != -1)
					setColor(colorRow, color);
			}
		} catch (Exception e) {
		}
	}

	@SuppressWarnings({ "unchecked"})
	protected <T> T getModelValue(JsonArrayTableModel model, Vector<Object> row, String key) {
		if (row == null)
			return null;
		
		if (row.size() == 0)
			return null;
		
		if (!model.has(row, RequirementDefines.field_used))
			return null;
		
		Object value = model.getValue(row, key);
		
		switch (key) {
		case RequirementDefines.field_use_time:
		case RequirementDefines.field_close_time:
		case RequirementDefines.field_plan_time:
			if (value != null){
				if (value instanceof String){
					SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					try {
						return (T) formatter.parse((String)value);
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}else
					return (T) value;
			}
			return (T) new Date();
		case RequirementDefines.field_level:
			if (value != null)
				return (T) value;
			return (T) new Integer(0);
		case RequirementDefines.field_used:
		case RequirementDefines.field_close:
			if (value != null)
				return (T) value;
			return (T) new Boolean(false);
		case RequirementDefines.field_depend_version:
		case RequirementDefines.field_role:
		case RequirementDefines.field_depend:
			if (value != null){
				if (value instanceof String){
					return (T) new JSONArray((String)value);
				}else
					return (T) value;
			}
			return (T) new JSONArray();
		default:
			return (T) value;
		}
	}

	DocumentListener documentListener = new DocumentListener() {
		
		@Override
		public void removeUpdate(DocumentEvent e) {
			isEdit = true;
		}
		
		@Override
		public void insertUpdate(DocumentEvent e) {
			isEdit = true;
		}
		
		@Override
		public void changedUpdate(DocumentEvent e) {
			isEdit = true;
		}
	};

	public int lastRow = -1;
	
	public void loadRow() {
		lastRow = table.getSelectedRow();
		if (lastRow == -1)
			return;

		closeTime.setLocale(Locale.CHINA);
		closeTime.setFormats(new SimpleDateFormat("yyyy-MM-dd"));
		planEndTime.setLocale(Locale.CHINA);
		planEndTime.setFormats(new SimpleDateFormat("yyyy-MM-dd"));
		useTime.setLocale(Locale.CHINA);
		useTime.setFormats(new SimpleDateFormat("yyyy-MM-dd"));
		
		JsonArrayTableModel model = (JsonArrayTableModel)table.getModel();
		Vector<Object> row = model.getRowData(lastRow);
		String id = model.getValue(row, RequirementDefines.field_id);
		idView.setText(id);
		
		requirementText.getDocument().removeDocumentListener(documentListener);
		requirementText.setText(getModelValue(model, row, RequirementDefines.field_requirement));
		requirementText.getDocument().addDocumentListener(documentListener);

		if (model.has(row, RequirementDefines.field_used))
			usedView.setSelected(model.getValue(row, RequirementDefines.field_used));
		else
			usedView.setSelected(true);

		if (model.has(row, RequirementDefines.field_close))
			closeView.setSelected(model.getValue(row, RequirementDefines.field_close));
		else
			closeView.setSelected(false);

		userView.setText(getModelValue(model, row, RequirementDefines.field_user));
		
		useTime.setDate(getModelValue(model, row, RequirementDefines.field_use_time));
		closeTime.setDate(getModelValue(model, row, RequirementDefines.field_close_time));
		planEndTime.setDate(getModelValue(model, row, RequirementDefines.field_plan_time));
		
		versionView.setText(getModelValue(model, row, RequirementDefines.field_version));
		
		typeView.setSelectedItem(getModelValue(model, row, RequirementDefines.field_type));
		try {
			levelView.setValue(getModelValue(model, row, RequirementDefines.field_level));
		} catch (Exception e) {
		}

		DefaultListModel<Object> roleModel = new DefaultListModel<>();
		if (model.has(row, RequirementDefines.field_role)) {
			JSONArray rows = null;
			Object tmp = getModelValue(model, row, RequirementDefines.field_role);
			if (tmp instanceof JSONArray)
				rows = (JSONArray) tmp;
			else if (tmp instanceof String) {
				try {
					rows = (JSONArray) JsonHelp.parseJson((String) tmp);
				} catch (Exception e) {
				}
			}
			if (rows != null)
				for (Object role : rows) {
					roleModel.addElement(role);
				}
		}
		roleView.setModel(roleModel);
		treeControl.select(id);

		setDependColors(lastRow);

		treeControl.jsonToTree();

	}

	public String getInputId() {
		return idView.getText();
	}

	public String getInputMainVersion() {
		if (mainVersion == null)
			return null;
		return mainVersion.getText();
	}

	public void setInputMainVersionFocus() {
		mainVersion.requestFocus();
	}

	public void saveRow(Integer row) throws Exception {
		if (row == null)
			row = table.getSelectedRow();
		
		if (row == -1)
			return;

		String id = idView.getText();
		if (id == null || id.isEmpty()) {
			EditorEnvironment.showMessage("编号不能为空！");
			return;
		}

		JsonArrayTableModel model = (JsonArrayTableModel) table.getModel();

		String curVersion = versionView.getText();
		if (curVersion == null || curVersion.isEmpty())
			curVersion = getInputMainVersion();
		
		if (curVersion == null || curVersion.isEmpty()) {
			throw new Exception("请输入主版本号！");
		}

		versionView.setText(curVersion);
		
		try {
			model.setValueAt(id, row, RequirementDefines.field_id);
			model.setValueAt(requirementText.getText(), row, RequirementDefines.field_requirement);
			model.setValueAt(usedView.isSelected(), row, RequirementDefines.field_used);
			model.setValueAt(closeView.isSelected(), row, RequirementDefines.field_close);
			model.setValueAt(userView.getText(), row, RequirementDefines.field_user);
			model.setValueAt(versionView.getText(), row, RequirementDefines.field_version);
			model.setValueAt(typeView.getSelectedItem(), row, RequirementDefines.field_type);
			model.setValueAt(levelView.getValue(), row, RequirementDefines.field_level);
			model.setValueAt(useTime.getDate(), row, RequirementDefines.field_use_time);
//			model.setValueAt(closeTime.getDate(), row, RequirementDefines.field_close_time);
			model.setValueAt(planEndTime.getDate(), row, RequirementDefines.field_plan_time);

			DefaultListModel<Object> roleMode = (DefaultListModel<Object>) roleView.getModel();
			JSONArray tmp = new JSONArray();
			if (roleMode.size() > 0) {
				for (int i = 0; i < roleMode.size(); i++) {
					tmp.put(roleMode.getElementAt(i));
				}
			}
			model.setValueAt(tmp, row, RequirementDefines.field_role);

			this.data = model.getData();

			tableToJson();

			updateVersionSelector();

			table.invalidate();
			
			isEdit = false;

		} catch (Exception e) {
			e.printStackTrace();
			EditorEnvironment.showException(e);
			return;
		}

	}

	public void saveDynamicInfo() {
		int row = table.getSelectedRow();
		if (row == -1)
			return;

		JsonArrayTableModel model = (JsonArrayTableModel) table.getModel();

		try {
			model.setValueAt(usedView.isSelected(), row, RequirementDefines.field_used);
			model.setValueAt(closeView.isSelected(), row, RequirementDefines.field_close);
			Date date = closeTime.getDate();
			
			model.setValueAt(date, row, RequirementDefines.field_close_time);
		} catch (Exception e) {
			e.printStackTrace();
			EditorEnvironment.showException(e);
			return;
		}

		this.data = model.getData();

		table.invalidate();
	}

	protected void updateVersionSelector() throws Exception {
		if (versionFilterView == null)
			return;

		JsonArrayTableModel model = (JsonArrayTableModel) table.getModel();
		HashMap<String, String> versions = new HashMap<>();
		for (int i = 0; i < model.getRowCount(); i++) {
			versions.put((String) model.getValueAt(i, RequirementDefines.field_version), null);
		}

		ActionListener[] actionListeners = versionFilterView.getActionListeners();
		for (ActionListener actionListener : actionListeners) {
			versionFilterView.removeActionListener(actionListener);
		}

		((DefaultComboBoxModel<String>) versionFilterView.getModel()).removeAllElements();
		versionFilterView.addItem("");
		for (String version : versions.keySet()) {
			versionFilterView.addItem(version);
		}

		versionFilterView.setSelectedIndex(0);

		for (ActionListener actionListener : actionListeners) {
			versionFilterView.addActionListener(actionListener);
		}
	}

	public Object getValue(int rowIndex, String field) {
		JsonArrayTableModel tableModel = (JsonArrayTableModel) table.getModel();
		try {
			return tableModel.getValueAt(rowIndex, field);
		} catch (Exception e) {
			e.printStackTrace();
			EditorEnvironment.showException(e);
			return null;
		}
	}

	public void clearColor() {
		colorTableRowRender.clear();
	}

	public void setColor(int row, Color color) {
		if (row != -1) {
			if (colorTableRowRender.exists(row))
				colorTableRowRender.remove(row);
			else
				colorTableRowRender.setColor(row, color);
			table.invalidate();
		}
	}

	public boolean isSelectColorModel(){
		return colorTableRowRender.isSelectModel();
	}
	
	public void setSelectColorModel(int row, Color color) {
		colorTableRowRender.setSelectMode(row, color);
		table.setRowSelectionAllowed(false);
		table.updateUI();
	}

	public void setDrawColorModel(){
		colorTableRowRender.setDrawMode();
		table.setRowSelectionAllowed(true);
		table.updateUI();
	}
	
	public Collection<Integer> getColorRows() {
		return colorTableRowRender.gets().keySet();
	}

	public void setValue(Object aValue, int rowIndex, String field) {
		JsonArrayTableModel tableModel = (JsonArrayTableModel) table.getModel();
		try {
			tableModel.setValueAt(aValue, rowIndex, field);
		} catch (Exception e) {
			e.printStackTrace();
			EditorEnvironment.showException(e);
		}
	}

	ColorTableRowRender colorTableRowRender;

	JSONArray data;

	public boolean isView = false;
	public void setFilter(String key, Object value) throws Exception {
		if (data == null)
			return;

		if (value == null || ((String) value).isEmpty()) {
			setData(data, isView, true);
		} else {
			JSONArray newData = new JSONArray();
			for (Object object : data) {
				JSONObject row = (JSONObject) object;
				if (row.has(key) && row.get(key).equals(value)) {
					newData.put(object);
				}
			}
			setData(newData, isView, true);
		}

	}

	public void setData(JSONArray data) throws Exception {
		setData(data, false, false);
	}

	public void setData(JSONArray data, boolean isView, boolean filter) throws Exception {
		JsonArrayTableModel tableModel = new JsonArrayTableModel(isView ? RequirementDefines.View_ColumnNames : RequirementDefines.ColumnNames);
		tableModel.setTable(table);
		tableModel.load(data);
		tableModel.setKey(new String[] { RequirementDefines.field_id });

		table.setAutoCreateColumnsFromModel(true);
		table.setModel(tableModel);

		table.setRowSorter(tableModel.getSorter());

		colorTableRowRender = new ColorTableRowRender(tableModel);

		for (int i = 0; i < table.getColumnCount(); i++) {
			TableColumn column = table.getColumnModel().getColumn(i);
			column.setCellRenderer(colorTableRowRender);
		}

		if (!filter) {
			updateVersionSelector();
			tableToJson();
			this.data = data;
		}
	}

	public int getSelectRow() {
		return table.getSelectedRow();
	}

	public int getRowIndex(String id, boolean convertIndex) {
		JSONObject row = new JSONObject();
		row.put(RequirementDefines.field_id, id);
		return getRowIndex(row, convertIndex);
	}

	public int getRowIndex(JSONObject values, boolean convertIndex) {
		JsonArrayTableModel tableModel = (JsonArrayTableModel) table.getModel();
		return tableModel.indexOf(values, convertIndex);
	}

	public void select(JSONObject data, ListSelectionListener tableListSelectionListener) {
		JsonArrayTableModel tableModel = (JsonArrayTableModel) table.getModel();
		int row;
		if (data != null){
			row = tableModel.indexOf(data, true);
		}else
			row = table.getSelectedRow();

		if (row != -1) {
			table.getSelectionModel().removeListSelectionListener(tableListSelectionListener);
			table.setRowSelectionInterval(row, row);
			table.getSelectionModel().addListSelectionListener(tableListSelectionListener);
			table.scrollRectToVisible(table.getCellRect(row, 0, true));
		}
	}

	public JSONArray getData() {
		JsonArrayTableModel tableModel = (JsonArrayTableModel) table.getModel();
		return tableModel.getData();
	}

	/**
	 * 将行数据加入依赖树对象
	 * 
	 * @param rows
	 *            依赖树对象，格式：{"id":{id, ...,child:{id:{}}}}
	 * @param row
	 *            行数据
	 * @param key
	 *            行中依赖项目的key
	 */
	protected void refreshDependTree(JSONObject rows, JSONObject row, JSONObject depends, String key) {
		if (treeControl.tree == null)
			return;

		String mid = row.getString(RequirementDefines.field_id);
		if (!depends.has(mid)) {
			JSONObject rowData = new JSONObject();
			rowData.put("row", row);
			rowData.put("child", new JSONObject());
			depends.put(mid, rowData);
		}

		if (row.has(key) && row.get(key) instanceof JSONArray && row.getJSONArray(key).length() > 0) {
			JSONArray ids = row.getJSONArray(key);
			for (Object object : ids) {
				String id = (String) object;
				if (!id.isEmpty()) {
					refreshDependTree(rows, rows.getJSONObject(id), depends.getJSONObject(mid).getJSONObject("child"),
							key);
				}
			}
		}
	}

	public void tableToJson() throws JSONException {
		JsonArrayTableModel model = (JsonArrayTableModel) table.getModel();
		versionTree = new JSONObject();
		dependTree = new JSONObject();

		JSONArray data = model.getData();

		JSONObject rows = new JSONObject();
		for (int i = 0; i < data.length(); i++) {
			JSONObject row = data.getJSONObject(i);
			rows.put(row.getString(RequirementDefines.field_id), row);
		}

		for (int i = 0; i < data.length(); i++) {
			JSONObject row = data.getJSONObject(i);
			refreshDependTree(rows, row, versionTree, RequirementDefines.field_depend_version);
			refreshDependTree(rows, row, dependTree, RequirementDefines.field_depend);
		}
	}

	public String getRowId(JSONObject row){
		return row.getString(RequirementDefines.field_id);
	}
	
	public JSONObject remove() {
		int row = table.getSelectedRow();
		if (row == -1)
			return null;

		JSONObject result = null;
		if (EditorEnvironment.showConfirmDialog("是否删除选定的记录？", "删除",
				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			JsonArrayTableModel model = (JsonArrayTableModel)table.getModel();
			result = model.getRow(row);
			model.removeRow(row);
		}
		
		return result;
	}

	public void add() {
		JsonArrayTableModel model = (JsonArrayTableModel) table.getModel();
		if (copyAdd.isSelected()) {
			int row = getSelectRow();
			if (row != -1) {
				JSONObject rowData = model.getRow(row);
				rowData.put(RequirementDefines.field_id, UUID.randomUUID().toString());
				rowData.put(RequirementDefines.field_version, mainVersion.getText());
				model.addRow(rowData);
				try {
					model.setValueAt(null, model.getRowCount() - 1, RequirementDefines.field_close_time);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return;
			}
		}
		model.addRow((Object[]) null);
		
		int row = model.getRowCount() - 1;
		table.scrollRectToVisible(table.getCellRect(row, 0, true));
		
		table.setRowSelectionInterval(row, row);
	}

	public JSONObject getRow(int row){
		JsonArrayTableModel model = (JsonArrayTableModel)table.getModel();
		return model.getRow(row);
	}
}
