package com.wh.draws.drawinfo;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.wh.draws.UINode;

public class TimeInfo extends TextInfo{

	String defaultFormat = "HH:mm";
	
	protected String getDefaultValue() {
		DateFormat dFormat = new SimpleDateFormat(format);
		return dFormat.format(new Date());
	}

	public String getDisplayText() {
		Object display = value;
		if (display == null || (display instanceof String && ((String)display).isEmpty()))
			display = getDefaultValue();
		
		SimpleDateFormat dFormatter = new SimpleDateFormat(format);
		String text = "";
		try {
			Date dt = dFormatter.parse(display.toString());
			text = dFormatter.format(dt);
		} catch (ParseException e) {
			value = null;
			format = defaultFormat;
			e.printStackTrace();
		}
		return text;
	}
	
	public String typeName(){
		return DrawInfoDefines.TimeBox_Name;
	}
	
	public TimeInfo(UINode node) {
		super(node);
		this.value = getDefaultValue();
	}

	protected String getButtonImageFileName() {
		return "time.png";
	}
	
	public JSONObject toJson(boolean needAll) throws JSONException{
		JSONObject json = super.toJson(needAll);
		json.put("format", needAll && format == null ? "" : format);
		return json;
	}
	
	public void fromJson(JSONObject json) throws JSONException{
		super.fromJson(json);
		
		if (json.has("format")){
			format = json.getString("format");
		}else
			format = defaultFormat;
		
		getDisplayText();
	}

	public String format = "HH:mm";			
}