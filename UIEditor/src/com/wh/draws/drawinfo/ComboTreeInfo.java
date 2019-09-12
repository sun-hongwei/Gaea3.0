package com.wh.draws.drawinfo;

import org.json.JSONException;
import org.json.JSONObject;

import com.wh.draws.UINode;

public class ComboTreeInfo extends ComboInfo{
	public String typeName(){
		return DrawInfoDefines.ComboTreeBox_Name;
	}
	public ComboTreeInfo(UINode node) {
		super(node);
		value = "";
	}

	protected String getButtonImageFileName() {
		return "tree.png";
	}
				
	public JSONObject toJson(boolean needAll) throws JSONException{
		JSONObject json = super.toJson(needAll);
		json.put("checkRecursive", needAll && checkRecursive == null ? "" : checkRecursive);
		json.put("parentField", needAll && parentField == null ? "" : parentField);
		json.put("valueField", needAll && valueField == null ? "" : valueField);
		json.put("textField", needAll && textField == null ? "" : textField);
		json.put("pinyinField", needAll && pinyinField == null ? "" : pinyinField);
		json.put("codeTableName", needAll && codeTableName == null ? "" : codeTableName);
		json.put("url", needAll && url == null ? "" : url);
		json.put("showRadioButton", showRadioButton);
		json.put("showFolderCheckBox", showFolderCheckBox);
		json.put("virtualScroll", virtualScroll);
		json.put("showTreeIcon", showTreeIcon);
		json.put("showTreeLines", showTreeLines);
		json.put("autoCheckParent", autoCheckParent);
		json.put("expandOnLoad", expandOnLoad);
		json.put("valueFromSelect", valueFromSelect);
		return json;
	}
	
	public void fromJson(JSONObject json) throws JSONException{
		super.fromJson(json);
		checkRecursive = json.getString("checkRecursive");
		parentField = json.getString("parentField");
		if (json.has("showFolderCheckBox"))
			showFolderCheckBox = json.getBoolean("showFolderCheckBox");
		else
			showFolderCheckBox = false;
		
		if (json.has("showRadioButton"))
			showRadioButton = json.getBoolean("showRadioButton");
		else
			showRadioButton = true;
		
		if (json.has("virtualScroll"))
			virtualScroll = json.getBoolean("virtualScroll");
		else
			virtualScroll = false;
		
		if (json.has("pinyinField"))
			pinyinField = json.getString("pinyinField");
		else
			pinyinField = null;
		
		if (json.has("showTreeIcon"))
			showTreeIcon = json.getBoolean("showTreeIcon");
		else
			showTreeIcon = true;
		
		if (json.has("showTreeLines"))
			showTreeLines = json.getBoolean("showTreeLines");
		else
			showTreeLines = true;
		
		if (json.has("autoCheckParent"))
			autoCheckParent = json.getBoolean("autoCheckParent");
		else
			autoCheckParent = false;
		
		if (json.has("expandOnLoad"))
			expandOnLoad = json.getBoolean("expandOnLoad");
		else
			expandOnLoad = true;
		
		if (json.has("valueFromSelect"))
			valueFromSelect = json.getBoolean("valueFromSelect");
		else
			valueFromSelect = true;
		
		if (json.has("valueField"))
			valueField = json.getString("valueField");
		else
			valueField = "id";
		
		if (json.has("textField"))
			textField = json.getString("textField");
		else
			textField = "text";
		
		if (json.has("url"))
			url = json.getString("url");
		else
			url = null;
		
		if (json.has("codeTableName"))
			codeTableName = json.getString("codeTableName");
		else
			codeTableName = null;
		
	}

	public String checkRecursive = "true";
	public String parentField = "pid";
	public String valueField = "id";
	public String textField = "text";
	public String pinyinField = "";
	public boolean showFolderCheckBox = false;
	public boolean showRadioButton = true;
	public boolean virtualScroll = false;
	public boolean showTreeIcon = true;
	public boolean showTreeLines = true;
	public boolean autoCheckParent = false;
	public boolean expandOnLoad = true;
	public boolean valueFromSelect = true;
	public String url;
	public String codeTableName;
}