package com.wh.control;

import org.json.JSONException;
import org.json.JSONObject;

import com.wh.control.checkboxnode.ICheck;

public class ListItemData implements ICheck{
	boolean isCheck = false;
	
	public JSONObject data;

	@Override
	public void setChecked(boolean b) {
		isCheck = b;
	}

	@Override
	public boolean getChecked() {
		return isCheck;
	}
	
	public ListItemData(JSONObject data){
		this.data = data;
	}
	
	public String toString(){
		try {
			return data.getString("text");
		} catch (JSONException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	public String getID(){
		try {
			return data.getString("id");
		} catch (JSONException e) {
			e.printStackTrace();
			return "";
		}
	}

	@Override
	public String getTitle() {
		return toString();
	}
	
	public void updateID(String newid){
		try {
			data.put("id", newid);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setIcon() {
		// TODO Auto-generated method stub
		
	}
}

