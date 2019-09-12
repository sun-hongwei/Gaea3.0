package com.wh.control.masterdata;

import java.io.File;

import org.json.JSONArray;

import com.wh.control.EditorEnvironment;
import com.wh.system.tools.JsonHelp;

public class MasterDataTypeFile {
	static File file = EditorEnvironment.getProjectFile(EditorEnvironment.MasterData_Dir_Name, "types." + EditorEnvironment.MasterData_Type_File_Extension);
	
	public static JSONArray getTypes() throws Exception{
		if (file.exists())
			return (JSONArray) JsonHelp.parseJson(file, null);
		else
			return new JSONArray();
	}
	
	public static void setTypes(JSONArray types) throws Exception{
		JsonHelp.saveJson(file, types, null);
	}
}
