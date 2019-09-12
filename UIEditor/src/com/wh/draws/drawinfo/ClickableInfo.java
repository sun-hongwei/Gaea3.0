package com.wh.draws.drawinfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wh.draws.UINode;

public abstract class ClickableInfo extends DrawInfo{

	public ClickableInfo(UINode node) {
		super(node);
	}

	public JSONObject toJson(boolean needAll) throws JSONException{
		JSONObject json = super.toJson(needAll);
		json.put("forceRowType", forceRowType);
		json.put("saveRowType", needAll && saveRowType == null ? "added" : saveRowType);
		json.put("saveDataSource", needAll && saveDataSource == null ? "[]" : saveDataSource);
		json.put("fireDataSource", needAll && fireDataSource == null ? "[]" : fireDataSource);
		json.put("jumpID", needAll && jumpID == null ? "" : jumpID);
		json.put("attachID", needAll && attachID == null ? "" : attachID);
		json.put("validateForm", validateForm);
		json.put("fireFormDataSource", fireFormDataSource);
		json.put("closeDialogId", needAll && closeDialogId == null ? "" : closeDialogId);
		return json;
	}
	
	public void fromJson(JSONObject json) throws JSONException{
		super.fromJson(json);

		if (json.has("closeDialogId"))
			closeDialogId = json.getString("closeDialogId");
		else
			closeDialogId = null;

		if (json.has("forceRowType"))
			forceRowType = json.getBoolean("forceRowType");
		else
			forceRowType = false;

		if (json.has("saveRowType"))
			saveRowType = json.getString("saveRowType");
		else
			saveRowType = "added";
		
		if (json.has("saveDataSource"))
			saveDataSource = json.getJSONArray("saveDataSource");
		else
			saveDataSource = null;
		
		if (json.has("fireDataSource"))
			fireDataSource = json.getJSONArray("fireDataSource");
		else
			fireDataSource = null;
		
		if (json.has("jumpID"))
			jumpID = json.getString("jumpID");
		else
			jumpID = null;
		
		if (json.has("attachID"))
			attachID = json.getString("attachID");
		else
			attachID = null;

		if (json.has("validateForm"))
			validateForm = json.getBoolean("validateForm");
		else
			validateForm = true;
		
		if (json.has("fireFormDataSource"))
			fireFormDataSource = json.getBoolean("fireFormDataSource");
		else
			fireFormDataSource = false;
		
	}
	
	public String jumpID;
	public String attachID;
	public boolean validateForm = true;
	public JSONArray fireDataSource;
	public boolean fireFormDataSource = false;
	public JSONArray saveDataSource;
	public String saveRowType = "added";
	public boolean forceRowType = false;
	public String closeDialogId;
}
