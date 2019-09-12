package com.wh.draws.drawinfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wh.draws.UINode;
import com.wh.system.tools.JsonHelp;

public class ComboInfo extends TextInfo{
	public String typeName(){
		return DrawInfoDefines.ComboBox_Name;
	}
	public ComboInfo(UINode node) {
		super(node);
		value = "";
	}

	protected String getDisplayText() {
		if (value == null)
			return "";
		
		String text = "";
		if (data != null && !data.isEmpty() & value != null){
			try {
				JSONArray datas = (JSONArray) JsonHelp.parseJson(data.toString());
				for (int i = 0; i < datas.length(); i++) {
					JSONObject obj = datas.getJSONObject(i);
					if (obj.getString(valueField).compareToIgnoreCase(value.toString()) == 0){
						text = obj.getString(textField);
						break;
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
				
			}
		}

		return text;
	}
	
	protected String getButtonImageFileName() {
		return "combo.png";
	}
	
	public JSONObject toJson(boolean needAll) throws JSONException{
		JSONObject json = super.toJson(needAll);
		json.put("showNullItem", needAll && showNullItem == null ? "" : showNullItem);
		json.put("valueField", needAll && valueField == null ? "" : valueField);
		json.put("textField", needAll && textField == null ? "" : textField);
		json.put("data", needAll && data == null ? "" : data);
		json.put("pinyinField", needAll && pinyinField == null ? "" : pinyinField);
		json.put("url", needAll && url == null ? "" : url);
		json.put("codeTableName", needAll && codeTableName == null ? "" : codeTableName);
		json.put("readonly", readonly);
		json.put("multiSelect", multiSelect);
		json.put("valueFromSelect", valueFromSelect);
		json.put("mode", needAll && mode == null ? "" : mode);
		json.put("popup", needAll && popup == null ? "" : popup);
		json.put("grid", needAll && grid == null ? "" : grid);
		return json;
	}
	
	public void fromJson(JSONObject json) throws JSONException{
		super.fromJson(json);

		if (json.has("popup"))
			popup = json.getString("popup");
		else
			popup = null;
		
		if (json.has("grid"))
			grid = json.getString("grid");
		else
			grid = null;
		
		if (json.has("mode"))
			mode = json.getString("mode");
		else
			mode = "combobox";
		
		if (json.has("readonly"))
			readonly = json.getBoolean("readonly");
		else
			readonly = false;
		
		if (json.has("showNullItem"))
			showNullItem = json.getString("showNullItem");
		else
			showNullItem = Boolean.toString(false);
		
		if (json.has("valueField"))
			valueField = json.getString("valueField");
		else
			valueField = "id";
		
		if (json.has("textField"))
			textField = json.getString("textField");
		else
			textField = "text";
		
		if (json.has("data"))
			data = json.getString("data");
		else
			data = "[]";
		
		if (json.has("url"))
			url = json.getString("url");
		else
			url = null;
		
		if (json.has("codeTableName"))
			codeTableName = json.getString("codeTableName");
		else
			codeTableName = null;
		
		if (json.has("multiSelect"))
			multiSelect = json.getBoolean("multiSelect");
		else
			multiSelect = false;
		
		if (json.has("valueFromSelect"))
			valueFromSelect = json.getBoolean("valueFromSelect");
		else
			valueFromSelect = true;
		
		if (json.has("pinyinField"))
			pinyinField = json.getString("pinyinField");
		else
			pinyinField = null;
		
	}

	public String showNullItem = "true";
	public String valueField = "id";
	public String textField = "text";
	public String data = "[]";
	public String url;
	public String codeTableName;
	public String pinyinField;
	public boolean multiSelect = false;
	public boolean readonly = false;
	public boolean valueFromSelect = true;
	public String mode = "combobox";
	public String popup;
	public String grid;
}