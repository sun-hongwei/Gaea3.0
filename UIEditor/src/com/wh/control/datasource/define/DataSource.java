package com.wh.control.datasource.define;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import org.json.JSONArray;
import org.json.JSONObject;

import com.wh.control.EditorEnvironment;
import com.wh.system.tools.HttpHelp;
import com.wh.system.tools.HttpHelp.ExecuteResult;
import com.wh.system.tools.HttpHelp.HttpResultState;
import com.wh.system.tools.HttpHelp.IHttpResult;
import com.wh.system.tools.HttpHelp.RequestInfo;
import com.wh.system.tools.JsonHelp;

public abstract class DataSource extends DefaultTableModel implements Cloneable {
	// {ret:0, data:{COLUMN:[{id:"",name:"",type:"",size:"",
	// style:"string,int,float,date, bool"}], DATA:[{field1:"value",
	// field2:""}]}}

	public static final String TYPE_KEY = "type";

	public static final String JSON_COLUMN_COLUMN = "COLUMN";
	public static final String JSON_COLUMN_DATA = "DATA";

	public static final String COLUMN_FIELD = "id";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_TYPE = "type";
	public static final String COLUMN_SIZE = "size";
	public static final String COLUMN_STYLE = "style";
	private static final long serialVersionUID = 5671231011076696340L;

	public String id = UUID.randomUUID().toString();
	public String url;
	public JSONObject params = new JSONObject();
	public JSONObject userData = new JSONObject();
	public String memo;
	public boolean useLocal = false;
	public JSONArray dataset = new JSONArray();
	public JSONObject columns = new JSONObject();

	public enum ColumnStyle {
		csField, csConst, csExpr, csNone
	}

	public abstract String getType();

	public static class FieldNameInfo {
		public String field;
		public String name;

		public FieldNameInfo(String field, String name) {
			this.field = field;
			this.name = name;
		}

		public String toString() {
			return name;
		}

		public JSONObject toJson() {
			JSONObject data = new JSONObject();
			data.put("field", field);
			data.put("name", name);
			return data;
		}

		public FieldNameInfo(JSONObject data) {
			if (data.has("field"))
				field = data.getString("field");
			if (data.has("name"))
				name = data.getString("name");
		}
	}

	protected FieldNameInfo getColumn(int column) {
		FieldNameInfo id = null;
		// This test is to cover the case when
		// getColumnCount has been subclassed by mistake ...
		if (column < columnIdentifiers.size() && (column >= 0)) {
			id = (FieldNameInfo) columnIdentifiers.elementAt(column);
		}
		return (id == null) ? null : id;
	}

	public void copyTo(DataSource ds) {
		ds.columns = columns;
		ds.dataset = dataset;
		ds.params = params;
		ds.url = url;
		ds.userData = userData;
		ds.useLocal = useLocal;
		ds.init();
	}

	public DataSource(JSONObject data) {
		this();
		fromJson(data);
		init();
	}

	public DataSource() {
		this.addTableModelListener(new TableModelListener() {

			@Override
			public void tableChanged(TableModelEvent e) {
				if (e.getLastRow() - e.getLastRow() < 0)
					return;

				int rowIndex = e.getLastRow();
				int colIndex = e.getColumn();
				switch (e.getType()) {
				case TableModelEvent.UPDATE:
					if (rowIndex == -1)
						break;
					JSONObject row = dataset.getJSONObject(rowIndex);
					row.put(getColumn(colIndex).field, getValueAt(rowIndex, colIndex));
					break;
				case TableModelEvent.INSERT:
					dataset.put(new JSONObject());
					break;
				case TableModelEvent.DELETE:
					dataset.remove(rowIndex);
					break;
				}
			}
		});
	}

	protected String getUrl(String baseUrl, String uri) {
		if (uri.trim().startsWith("/"))
			return baseUrl + uri;
		else
			return uri;
	}

	public void setFile(File file) {
		saveFile = file;
	}

	public void saveDataset(File saveFile) throws Exception {
		JSONObject data = new JSONObject();
		data.put("column", columns);
		data.put("data", dataset);
		JsonHelp.saveJson(saveFile, data, null);
	}

	public void loadDataset(File loadFile) throws Exception {
		JSONObject data = (JSONObject) JsonHelp.parseJson(loadFile, null);
		loadDataset(data);
	}

	public void loadDataset(JSONObject data) throws Exception {
		if (data.has("column"))
			columns = data.getJSONObject("column");
		if (data.has("data"))
			dataset = data.getJSONArray("data");

		init();
	}

	public void loadDataset(String url) {
		if (useLocal)
			return;

		JSONObject command = new JSONObject();
		command.put("command", params.toString());
		LinkedHashMap<String, String> params = HttpHelp.JsonToMap(command);
		url = getUrl(url, this.url);
		HttpHelp.HttpPost(url, params, new IHttpResult() {

			@Override
			public void OnHttpResult(RequestInfo request, ExecuteResult result) {
				if (result.state == HttpResultState.hrsMessage) {
					JSONObject resultdata = new JSONObject(result.data);
					if (resultdata.getInt("ret") == 0) {
						try {
							init(resultdata.getJSONObject("data"));
						} catch (Exception e) {
							EditorEnvironment.showException(e, "异常", resultdata.toString());
						}
					}
				}
			}
		});
	}

	public static JSONArray setModel(DataSource dataSource, String uri, JTable table, JSONObject params) {
		if (params.names() != null)
			for (Object obj : params.names()) {
				String key = (String) obj;
				dataSource.params.put(key, params.get(key));
			}

		dataSource.loadDataset(uri);
		JSONArray dataset = dataSource.dataset;
		if (dataset == null) {
			return new JSONArray();
		}

		if (table != null) {
			HashMap<String, String> columnMap = new HashMap<>();
			for (Object object : dataset) {
				JSONObject row = (JSONObject) object;
				for (Object object2 : row.names()) {
					String name = (String) object2;
					columnMap.put(name, name);
				}
			}

			List<String> columnDefines = new ArrayList<>(columnMap.values());
			Object[][] rows = new Object[dataset.length()][columnDefines.size()];
			Object[] columns = new Object[columnDefines.size()];
			int index = 0;
			for (String column : columnDefines) {
				columns[index++] = column;
			}

			index = 0;
			for (Object obj : dataset) {
				int colIndex = 0;
				JSONObject row = (JSONObject) obj;
				for (String column : columnDefines) {
					rows[index][colIndex++] = row.get(column);
				}
				index++;
			}
			DefaultTableModel model = new DefaultTableModel(rows, columns);
			table.setModel(model);
		}

		return dataset;
	}

	@Override
	public String toString() {
		return id;
	}

	public void setFieldStyle(int col, ColumnStyle cs) {
		FieldNameInfo info = getColumn(col);
		if (info == null)
			return;

		String field = info.field;
		if (!columns.has(field))
			return;

		JSONObject column = columns.getJSONObject(field);
		column.put(COLUMN_STYLE, cs.name());
	}

	public void clear() {
		columns = new JSONObject();
		dataset = new JSONArray();
		setDataVector((Vector<?>) null, (Vector<?>) null);
	}

	public void removeColumns() {
		columns = new JSONObject();
		setDataVector((Vector<?>) null, (Vector<?>) null);
	}

	public void removeRows() {
		dataset = new JSONArray();
		setDataVector(null, getFieldNames());
	}

	public void init(JSONObject dataset) {
		if (dataset == null) {
			clear();
			return;
		}

		JSONArray columns = dataset.getJSONArray(JSON_COLUMN_COLUMN);
		JSONArray data = dataset.has(JSON_COLUMN_DATA) ? dataset.getJSONArray(JSON_COLUMN_DATA) : new JSONArray();
		init(columns, data);
	}

	public void init(JSONArray columns, JSONArray data) {
		clear();

		for (int i = 0; i < columns.length(); i++) {
			JSONObject column = columns.getJSONObject(i);
			addField(column);
		}

		this.dataset = data;

		init();
	}

	public void init() {
		String[] fields = getFields();
		if (fields == null || fields.length == 0) {
			HashMap<String, String> fieldMap = new HashMap<>();
			for (int i = 0; i < this.dataset.length(); i++) {
				JSONObject row = this.dataset.getJSONObject(i);
				for (Object obj : row.names()) {
					String cn = (String) obj;
					if (!fieldMap.containsKey(cn)) {
						fieldMap.put(cn, cn);
					}
				}
			}

			for (String field : fieldMap.keySet()) {
				JSONObject column = new JSONObject();
				column.put(COLUMN_FIELD, field);
				column.put(COLUMN_NAME, field);
				columns.put(field, column);
			}

			fields = getFields();
		}

		Object[][] rows = new Object[this.dataset.length()][];
		for (int i = 0; i < this.dataset.length(); i++) {
			JSONObject row = this.dataset.getJSONObject(i);
			Object[] rowData = new Object[fields.length];
			for (int j = 0; j < fields.length; j++) {
				String cn = fields[j];
				if (row.has(cn)) {
					rowData[j] = row.get(cn);
				} else
					rowData[j] = null;
			}
			rows[i] = rowData;
		}
		setDataVector(rows, getFieldNames());
	}

	public void setDataset(JSONArray dataset) {
		this.dataset = dataset;
		init();
	}

	public void addField(String field, String name, String typeName, int size, ColumnStyle cs) {
		JSONObject column = new JSONObject();
		column.put(COLUMN_FIELD, field);
		column.put(COLUMN_NAME, name);
		column.put(COLUMN_TYPE, typeName);
		column.put(COLUMN_SIZE, size);
		column.put(COLUMN_STYLE, cs.name());

		addField(column);

		addColumn(field, name);
	}

	protected void addColumn(String field, String name) {
		for (Object object : columnIdentifiers) {
			FieldNameInfo info = (FieldNameInfo) object;
			if (info.field.compareTo(field) == 0)
				return;
		}
		addColumn(new FieldNameInfo(field, name == null || name.isEmpty() ? field : name));
	}

	public void addField(JSONObject column) {
		String field = column.getString(COLUMN_FIELD);
		JSONObject columnDefine = new JSONObject();
		String name = field;
		if (column.has(COLUMN_NAME)) {
			name = column.getString(COLUMN_NAME);
			columnDefine.put(COLUMN_NAME, name);
		}
		columnDefine.put(COLUMN_FIELD, field);
		columnDefine.put(COLUMN_TYPE, column.has(COLUMN_TYPE) ? column.getString(COLUMN_TYPE) : "string");
		if (column.has(COLUMN_SIZE)) {
			columnDefine.put(COLUMN_SIZE, column.getInt(COLUMN_SIZE));
		}
		if (column.has(COLUMN_STYLE)) {
			columnDefine.put(COLUMN_STYLE, column.getString(COLUMN_STYLE));
		} else
			columnDefine.put(COLUMN_STYLE, ColumnStyle.csField.name());
		columns.put(field, columnDefine);
	}

	public void addRow() {
		Object[] row = new Object[columns.length()];
		addRow(row);
	}

	public JSONObject getField(int col) {
		FieldNameInfo info = getColumn(col);
		if (info != null) {
			return columns.getJSONObject(info.field);
		} else
			return null;
	}

	public void removeField(int col) {
		FieldNameInfo info = getColumn(col);
		if (info != null) {
			columns.remove(info.field);
			columnIdentifiers.remove(col);
			setColumnIdentifiers(columnIdentifiers);
		}
	}

	public String[] getFields() {
		JSONArray names = columns.names();
		if (names == null || names.length() == 0)
			return null;

		String[] result = new String[names.length()];
		for (int i = 0; i < names.length(); i++) {
			result[i] = names.getString(i);
		}

		return result;
	}

	public FieldNameInfo[] getFieldNames() {
		String[] names = getFields();
		if (names == null)
			return null;

		FieldNameInfo[] result = new FieldNameInfo[names.length];
		for (int i = 0; i < names.length; i++) {
			String field = names[i];
			JSONObject column = columns.getJSONObject(field);
			String name = field;
			if (column.has(COLUMN_NAME)) {
				String tmp = column.getString(COLUMN_NAME);
				if (tmp != null && !tmp.isEmpty())
					name = tmp;
			}
			result[i] = new FieldNameInfo(field, name);
		}

		return result;
	}

	public JSONObject toJson() {
		JSONObject value = new JSONObject();
		value.put("id", id);
		value.put("url", url);
		value.put("params", params);
		value.put("useLocal", useLocal);
		value.put("dataset", dataset);
		value.put("columns", columns);
		value.put("userData", userData);
		value.put("memo", memo);
		value.put(TYPE_KEY, getType());
		return value;
	}

	public void fromJson(JSONObject value) {
		if (value.has("memo"))
			memo = value.getString("memo");
		else
			memo = null;

		if (value.has("userData"))
			userData = value.getJSONObject("userData");
		else
			userData = new JSONObject();

		if (value.has("id"))
			id = value.getString("id");
		else
			id = UUID.randomUUID().toString();

		if (value.has("url"))
			url = value.getString("url");
		else
			url = null;

		if (value.has("params"))
			params = value.getJSONObject("params");
		else
			params = new JSONObject();

		if (value.has("useLocal"))
			useLocal = value.getBoolean("useLocal");
		else
			useLocal = false;

		if (value.has("dataset"))
			dataset = value.getJSONArray("dataset");
		else
			dataset = new JSONArray();

		if (value.has("columns"))
			columns = value.getJSONObject("columns");
		else
			columns = new JSONObject();

		init();
	}

	@Override
	public void removeRow(int row) {
		dataset.remove(row);
		super.removeRow(row);
	}

	public DataSource clone() {
		DataSource dSource;
		try {
			dSource = this.getClass().getDeclaredConstructor().newInstance();
			dSource.fromJson(toJson());
			return dSource;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void save(File file) throws Exception {
		setFile(file);
		JsonHelp.saveJson(file, toJson(), null);
	}

	public void load(File file) throws Exception {
		setFile(file);
		fromJson((JSONObject) JsonHelp.parseJson(file, null));
	}

	File saveFile;

	public void save() throws Exception {
		if (saveFile == null)
			return;

		File parentFile = saveFile.getParentFile();
		if (!parentFile.exists())
			if (!parentFile.mkdirs())
				throw new IOException("mk[" + parentFile.getAbsolutePath() + "] failed!");

		save(saveFile);
	}

	public void load() throws Exception {
		if (saveFile == null || !saveFile.exists())
			return;

		load(saveFile);
	}
}
