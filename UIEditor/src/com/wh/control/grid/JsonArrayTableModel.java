package com.wh.control.grid;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.json.JSONArray;
import org.json.JSONObject;

public class JsonArrayTableModel extends DefaultTableModel {
	private static final long serialVersionUID = 1L;

	public class ChinaDate extends Date{

		private static final long serialVersionUID = 1L;

		public ChinaDate(Date date) {
			super(date.getTime());
		}
		
		@Override
		public String toString(){
			SimpleDateFormat format = new SimpleDateFormat(dateFormat);
			return format.format(this);
		}
	}
	
	public static class Column{
		public String name;
		public boolean visible = true;
		public Class<?> type;
		public Column() {
		}

		public Column(String name, boolean visible, Class<?> c){
			this.name = name;
			this.visible = visible;
			this.type = c;
		}
		
		@Override
		public String toString(){
			return name;
		}
	}
	
	JTable table;
	public void setTable(JTable table){
		this.table = table;
	}
	
	public int getRealRowIndex(int row) {
		return table.convertRowIndexToModel(row);
	}
	
	public int convertToTableIndex(int row){
		return table.convertRowIndexToView(row);
	}
	
	public boolean has(Vector<Object> row, String field){
		Object vObject = getValue(row, field);
		if (vObject == null)
			return false;
		else if (vObject instanceof String){
			String tmp = (String)vObject;
			return !(tmp == null || tmp.isEmpty());
		}else {
			return true;
		}
	}
	
	@SuppressWarnings("unchecked")
	public Vector<Object> getRowData(int row){
		row = getRealRowIndex(row);
		return (Vector<Object>) getDataVector().elementAt(row);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getValue(Vector<Object> row, String columnName){
		int col = indexOfColumn(columnName);
		return (T) row.get(col);
	}
	
	String[] key;
	public JsonArrayTableModel(Column[] columns){
		super(null, columns);
	}
	
	public JsonArrayTableModel(String[] names){
		super();
		Column[] columns = new Column[names.length];
		int index = 0;
		for (String name : names) {
			columns[index++] = new Column(name, true, String.class);
		}
		setDataVector(null, columns);
	}
	
	@SuppressWarnings("rawtypes")
	public static class IntegerStringComparator implements Comparator {
		@SuppressWarnings({ "unchecked" })
		public int compare(Object o1, Object o2) {
			try {
				int i1 = Integer.parseInt(o1.toString());
				int i2 = Integer.parseInt(o2.toString());
				return Integer.compare(i1, i2);
			} catch (Exception e) {
				return ((Comparable) o1).compareTo(o2);
			}
		}
	}

	public void setKey(String[] key){
		if (key == null || key.length == 0)
			this.key = null;
		else
			this.key = Arrays.asList(key).toArray(new String[key.length]);
	}
	
	public int indexOf(String value, boolean convertIndex){
		JSONObject keyValue = new JSONObject();
		keyValue.put(key[0], value);
		return indexOf(keyValue, convertIndex);
	}
	
	public int indexOf(JSONObject keyvalue, boolean convertIndex){
		if (key == null || key.length == 0)
			return -1;
		HashMap<String, Integer> fieldMap = new HashMap<>();
		for (int i = 0; i < columnIdentifiers.size(); i++) {
			fieldMap.put(columnIdentifiers.get(i).toString(), i);
		}
		
		for (int i = 0; i < getRowCount(); i++) {
			boolean isok = true;
			for (String field : key) {
				if (!keyvalue.get(field).equals(getValueAt(i, (int)fieldMap.get(field)))){
					isok = false;
					break;
				}
			}
			if (isok){
				return convertIndex ? convertToTableIndex(i) : i;
			}
		}
		
		return -1;
	}
	
	public static class MenuTableRowSorter<T> extends TableRowSorter<TableModel> {
		IntegerStringComparator integerStringComparator = new IntegerStringComparator();

		public Comparator<?> getComparator(int column) {
			return integerStringComparator;
		}
	}

	public MenuTableRowSorter<JsonArrayTableModel> getSorter(){
		MenuTableRowSorter<JsonArrayTableModel> sorter = new MenuTableRowSorter<>();
		sorter.setModel(this);
		return sorter;
	}

	public String dateFormat = "yyyy-MM-dd HH:mm:ss";
	
	@SuppressWarnings("unchecked")
	public JSONArray getData(){
		JSONArray data = new JSONArray();
		for (Object object : getDataVector()) {
			JSONObject rowData = new JSONObject();
			Vector<Object> row = (Vector<Object>)object;
			
			for (int i = 0; i < columnIdentifiers.size(); i++) {
				String key = columnIdentifiers.get(i).toString();
				Object value = row.get(i);
				if (value instanceof Date){
					SimpleDateFormat format = new SimpleDateFormat(dateFormat);
					value = format.format((Date)value);
				}
				rowData.put(key, value);
			}
			data.put(rowData);
		}
		return data;
	}
	
	protected boolean isEmptyCell(Object cellData) {
		return cellData == null || (cellData instanceof String && ((String) cellData).isEmpty());
	}

	@SuppressWarnings("unchecked")
	public boolean isEmptyRow(int rowIndex) {
		rowIndex = getRealRowIndex(rowIndex);
		Vector<Vector<Object>> rows = getDataVector();
		if (rows.size() > 0 && rowIndex >= 0 && rowIndex < rows.size()) {
			Vector<Object> row = rows.get(rowIndex);
			return isEmptyCell(row.get(0)) && isEmptyCell(row.get(0));
		}
		return true;
	}

	public Column getColumn(int index){
		return (Column) columnIdentifiers.get(index);
	}
	
	public void load(JSONArray data){
		HashMap<String, Object> columns = new HashMap<>();
		for (int i = 0; i < data.length(); i++) {
			JSONObject row = data.getJSONObject(i);
			JSONArray fields = row.names();
			for (Object column : fields) {
				columns.put(column.toString(), column);
			}
		}

		Object[][] rows = new Object[data.length()][columnIdentifiers.size()];
		for (int i = 0; i < data.length(); i++) {
			JSONObject row = data.getJSONObject(i);
			int index = 0;
			for (Object c : columnIdentifiers) {
				String column = c.toString();
				if (columns.containsKey(column)){
					if (row.has(column))
						rows[i][index++] = row.get(column);
					else
						rows[i][index++] = null;
				}
			}
		}
		
		setDataVector(rows, columnIdentifiers.toArray());
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	public JSONObject getRow(int rowIndex) {	
		if (rowIndex == -1)
			return null;
		
		rowIndex = getRealRowIndex(rowIndex);

		JSONObject rowData = new JSONObject();
		for (int i = 0; i < columnIdentifiers.size(); i++) {
			Column column = (Column)columnIdentifiers.get(i);
			Object value = getValueAt(rowIndex, i);
			if (value instanceof Date){
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				value = format.format((Date)value);
			}
			rowData.put(column.name, value == null ? "" : value);
		}
		
		return rowData;
	}
	
	public void addRow(JSONObject row){
		Object[] rowData = new Object[columnIdentifiers.size()];
		for (int i = 0; i < rowData.length; i++) {
			String column = columnIdentifiers.get(i).toString();
			rowData[i] = row.has(column) ? row.get(column) : null;
		}
		addRow(rowData);
	}
	
	@Override
    public void removeRow(int row) {
		row = getRealRowIndex(row);
    	super.removeRow(row);
    }

    public int indexOfColumn(String column){
		int colIndex = -1;
		for (int i = 0; i < columnIdentifiers.size(); i++) {
			if (columnIdentifiers.get(i).toString().equals(column)){
				colIndex = i;
				break;
			}
		}
		
		return colIndex;
    }
    
	public Object getValueAt(int row, Object column) throws Exception {
		row = getRealRowIndex(row);
		int colIndex = indexOfColumn(column.toString());
		
		if (colIndex == -1)
			throw new Exception("列[" + column + "] 未找到！");
		
		return getValueAt(row, colIndex);
	}
	
	public void setValueAt(Object aValue, int row, Object column) throws Exception {
		int colIndex = indexOfColumn(column.toString());
		
		if (colIndex == -1)
			throw new Exception("列[" + column + "] 未找到！");
		
		setValueAt(aValue, row, colIndex);
	}
	
	@Override
	public void setValueAt(Object aValue, int row, int column) {
		row = getRealRowIndex(row);
		if (aValue instanceof Date){
			aValue = new ChinaDate((Date)aValue);
		}
		super.setValueAt(aValue, row, column);
	}

}
