package com.wh.form;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONObject;

import com.wh.control.EditorEnvironment;
import com.wh.control.datasource.define.DataSource;
import com.wh.control.datasource.define.FileDataSource;
import com.wh.control.datasource.define.LocalDataSource;
import com.wh.control.datasource.define.SQLDataSource;
import com.wh.control.datasource.define.UrlDataSource;
import com.wh.system.tools.FileHelp;
import com.wh.system.tools.JsonHelp;

public abstract class DataSourceManager {
	
	public static final String DSMExt = "dsm";
	
	HashMap<String, DataSource> dataSources = new HashMap<>();
	
	public List<String> getIDs(){
		return new ArrayList<>(dataSources.keySet());
	}
	
	public List<DataSource> getDataSources(){
		TreeMap<String, DataSource> map = new TreeMap<String, DataSource>(dataSources);
		return new ArrayList<>(map.values());
	}
	
	public void add(DataSource dataSource){
		dataSources.put(dataSource.id, dataSource);
	}
	
	public DataSource get(String id){
		if (dataSources.containsKey(id))
			return dataSources.get(id);
		else
			return null;
	}
	
	public List<DataSource> gets(List<String> ids){
		List<DataSource> dSources = new ArrayList<>();
		for (String id : ids) {
			if (dataSources.containsKey(id))
				dSources.add(dataSources.get(id));
		}
		return dSources;
	}
	
	public void remove(String id) throws IOException{
		if (dataSources.containsKey(id)){
			DataSource dataSource = dataSources.remove(id);
		
			File file = getSaveFile(dataSource);
			if (file.exists())
				if (!file.delete())
					throw new IOException("删除数据源文件失败！");
		}
	}
	
	public JSONArray toJson(){
		JSONArray json = new JSONArray();
		for (DataSource dataSource : dataSources.values()) {
			json.put(dataSource.toJson());
		}
		
		return json;
	}
	
	protected DataSource newDataSource(JSONObject data) throws Exception {
		DataSource dataSource = null;
		String typeName = UrlDataSource.URL_KEY;
		if (data.has(DataSource.TYPE_KEY))
			typeName = data.getString(DataSource.TYPE_KEY);
		
		switch (typeName) {
		case LocalDataSource.LOCAL_KEY:
			dataSource = new LocalDataSource();				
			break;
		case FileDataSource.FILE_KEY:
			dataSource = new FileDataSource();				
			break;
		case SQLDataSource.SQL_KEY:
			dataSource = new SQLDataSource();				
			break;
		case UrlDataSource.URL_KEY:
			dataSource = new UrlDataSource();				
			break;
		default:
			throw new Exception("未知的数据源类型！");
		}
		
		return dataSource;
	}

	public void fromJson(JSONArray json) throws Exception{
		dataSources.clear();
		for (int i = 0; i < json.length(); i++) {
			JSONObject data = json.getJSONObject(i);
			DataSource dataSource = newDataSource(data);
			dataSource.fromJson(data);
			dataSources.put(dataSource.id, dataSource);
		}
	}
	
	public void save(String id, File file) throws Exception{
		if (dataSources.containsKey(id)){
			DataSource dataSource = dataSources.get(id);
			dataSource.save(file);
		}
	}
	
	public void load(File file) throws Exception{
		JSONObject data = (JSONObject) JsonHelp.parseJson(file, null);
		
		DataSource dataSource = newDataSource(data);
		dataSource.setFile(file);
		dataSource.fromJson(data);
		if (dataSources.containsKey(dataSource.id)){
			DataSource ds = dataSources.get(dataSource.id);
			dataSource.copyTo(ds);
		}else
			dataSources.put(dataSource.id, dataSource);
	}
	
	public File getSaveFile(DataSource dataSource){
		return getSaveFile(dataSource, getDefaultPath());
	}
	
	public File getSaveFile(DataSource dataSource, File path){
		return new File(path, dataSource.id + "." + DSMExt);
	}
	
	public void saveAll(File path) throws Exception{
		for (DataSource dataSource : dataSources.values()) {
			save(dataSource.id, getSaveFile(dataSource, path));
		}
	}
	
	public void loadAll(File path) throws Exception{
		for (File file : path.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File file) {
				String name = file.getName();
				return file.isFile() 
						&& name.compareTo(".") != 0 
						&& name.compareTo("..") != 0
						&& FileHelp.GetExt(name).compareTo(DSMExt) == 0;
			}
		})) {
			load(file);
		}
	}

	public File getDefaultPath(){
		File path = EditorEnvironment.getProjectPath(EditorEnvironment.DataSource_Dir_Name);
		return path;
	}
	
	public void saveAll() throws Exception{
		saveAll(getDefaultPath());
	}
	
	public void loadAll() throws Exception{
		loadAll(getDefaultPath());
	}

	static DataSourceManager dsm = new DataSourceManager(){};
	
	static{
		try {
			dsm.loadAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void reset(){
		dsm = new DataSourceManager(){};
		
		try {
			dsm.loadAll();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	public static DataSourceManager getDSM(){
		return dsm;
	}

}
