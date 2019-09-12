package com.wh.dialog.editor;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;

import javax.swing.JTable;

import org.json.JSONObject;

import com.wh.control.EditorEnvironment;
import com.wh.control.RunFlowFile;
import com.wh.control.grid.design.PropertyTableCellEditor.KeyValue;
import com.wh.dialog.selector.KeyValueSelector;
import com.wh.dialog.selector.KeyValueSelector.IEditRow;
import com.wh.dialog.selector.KeyValueSelector.Result;
import com.wh.dialog.selector.KeyValueSelector.RowResult;
import com.wh.form.IMainControl;
import com.wh.system.tools.FileHelp;

public class RunFlowSelectDialog {
	protected static boolean save(String id, String name, String memo) {
		try {
			if (id == null || id.isEmpty())
				return false;
			
			RunFlowFile runFlowFile = new RunFlowFile();
			runFlowFile.setFile(EditorEnvironment.getRunFlowFile(id));
			runFlowFile.load();
			runFlowFile.name = name;
			runFlowFile.memo = memo;
			runFlowFile.save();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			EditorEnvironment.showException(e);
			return false;
		}
	}
	
	protected static boolean remove(String id) {
		try {
			if (id == null || id.isEmpty())
				return false;
			
			File file = EditorEnvironment.getRunFlowFile(id);
			return file.delete();
		} catch (Exception e) {
			e.printStackTrace();
			EditorEnvironment.showException(e);
			return false;
		}
	}
	
	public static class RunFlowInfo{
		public String id;
		public String name;
		public String memo;
		public JSONObject flowData;
		
		@Override
		public String toString(){
			return name == null || name.isEmpty() ? id : name;
		}
	}
	
	public static Map<String, RunFlowInfo> getRunFlows() throws Exception {
		Map<String, RunFlowInfo> result = new HashMap<>();
		Object[][] rows = getRunFlowDatas();	
		if (rows == null)
			return new HashMap<>();
		try{
			for (int i = 0; i < rows.length; i++) {
				Object[] rowdata = rows[i];
				RunFlowInfo info = new RunFlowInfo();
				
				info.id = (String)rowdata[0];
				info.name = (String)rowdata[1];
				info.memo = (String)rowdata[2];
				info.flowData = (JSONObject)rowdata[3];
				result.put(info.id, info);
			}
		} catch (Exception e) {
			EditorEnvironment.showException(e);
			result.clear();
		}
		
		return result;
	}

	public static Object[][] getRunFlowDatas() throws Exception {
		File[] files = EditorEnvironment.getRunFlowFiles();
		Object[][] rows = null;
		if (files.length > 0){
			int index = 0;
			rows = new Object[files.length][];
			for (File file : files) {
				RunFlowFile runFlowFile = new RunFlowFile();
				runFlowFile.setFile(file);
				runFlowFile.load();
				Object[] row = new Object[4];
				row[0] = FileHelp.removeExt(file.getName());
				row[1] = runFlowFile.name;
				row[2] = runFlowFile.memo;
				row[3] = runFlowFile.data;
				rows[index++] = row;
			}
		}
		
		return rows;
	}
	
	public static class RunFlowResult extends Result{
		public KeyValue<String, String> runFlowInfo;
		public RunFlowResult(KeyValue<String, String> runFlowInfo, boolean isok) {
			this.runFlowInfo = runFlowInfo;
			this.isok = isok;
		}
	}
	
	public static RunFlowResult show(IMainControl mainControl) throws Exception {
		Object[][] rows = getRunFlowDatas();
		Object[] cols = new Object[]{"编号", "名字", "说明"};
		RowResult result = KeyValueSelector.showForOne(null, mainControl, null, new IEditRow(){

			@Override
			public boolean deleteRow(JTable table, Vector<?> row) {
				return remove((String)row.get(0));
			}

			@Override
			public Object[] addRow(JTable table) {
				String name = EditorEnvironment.showInputDialog("请输入新流程的名字", "新增流程");
				if (name == null || name.isEmpty())
					return null;
				
				String id = UUID.randomUUID().toString();
				if (save(id, name, name))
					return new Object[]{id, name, name};
				return null;
			}

			@Override
			public void updateRow(JTable table, Vector<?> rowdata) {
				save((String)rowdata.get(0), (String)rowdata.get(1), (String)rowdata.get(2));
			}
			
		}, rows, cols, null);
		
		Object[] row = result.isok ? result.row : null;
		if (row == null)
			return new RunFlowResult(null, result.isok);
		else{
			return new RunFlowResult(new KeyValue<>((String)row[1], (String)row[0]), result.isok);
		}
	}
	
}
