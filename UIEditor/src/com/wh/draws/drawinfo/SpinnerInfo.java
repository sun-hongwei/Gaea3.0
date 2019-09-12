package com.wh.draws.drawinfo;

import org.json.JSONException;
import org.json.JSONObject;

import com.wh.draws.UINode;

public class SpinnerInfo extends TextInfo{
	public String typeName(){
		return DrawInfoDefines.Spinner_Name;
	}
	public SpinnerInfo(UINode node) {
		super(node);
		value = "0";
	}

	protected String getButtonImageFileName() {
		return "spinner.png";
	}
	
	public JSONObject toJson(boolean needAll) throws JSONException{
		JSONObject json = super.toJson(needAll);
		json.put("minValue", needAll && minValue == null ? "" : minValue);
		json.put("maxValue", needAll && maxValue == null ? "" : maxValue);
		json.put("decimalPlaces", decimalPlaces);
		json.put("format", needAll && format == null ? "" : format);
		json.put("changeOnMousewheel", changeOnMousewheel);
		json.put("allowLimitValue", allowLimitValue);
		json.put("decimalPlaces", decimalPlaces);
		json.put("allowLoopValue", allowLoopValue);
		json.put("allowNull", allowNull);
		json.put("increment", increment);
		return json;
	}
	
	public void fromJson(JSONObject json) throws JSONException{
		super.fromJson(json);
		minValue = json.getString("minValue");
		maxValue = json.getString("maxValue");
		if (json.has("decimalPlaces"))
			decimalPlaces = json.getInt("decimalPlaces");
		else
			decimalPlaces = 0;
		
		if (json.has("format"))
			format = json.getString("format");
		else
			format = null;
		
		if (json.has("changeOnMousewheel"))
			changeOnMousewheel = json.getBoolean("changeOnMousewheel");
		else
			changeOnMousewheel = true;
		
		if (json.has("allowLimitValue"))
			allowLimitValue = json.getBoolean("allowLimitValue");
		else
			allowLimitValue = true;
		
		if (json.has("allowLoopValue"))
			allowLoopValue = json.getBoolean("allowLoopValue");
		else
			allowLoopValue = false;
		
		if (json.has("allowNull"))
			allowNull = json.getBoolean("allowNull");
		else
			allowNull = false;
		
		if (json.has("increment"))
			increment = json.getFloat("increment");
		else
			increment = 1.0F;
	}

	public String minValue = "0";
	public String maxValue = "100";
	public int decimalPlaces;
	public String format;
	public boolean changeOnMousewheel = true;
	public boolean allowLimitValue = true;
	public boolean allowLoopValue = false;
	public boolean allowNull = false;
	public float increment = 1.0F;
	
}