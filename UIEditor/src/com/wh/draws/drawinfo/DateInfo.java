package com.wh.draws.drawinfo;

import org.json.JSONException;
import org.json.JSONObject;

import com.wh.draws.UINode;

public class DateInfo extends TimeInfo{
	public static String CalendarViewType = "日历";
	public static String ComboBoxViewType = "选择框";
	
	public String typeName(){
		return DrawInfoDefines.DateBox_Name;
	}
	
	public DateInfo(UINode node) {
		super(node);
		this.format = "yyyy-MM-dd";
		this.defaultFormat = this.format;
		this.value = getDefaultValue();
	}

	protected String getButtonImageFileName() {
		return "date.png";
	}
	
	public JSONObject toJson(boolean needAll) throws JSONException{
		JSONObject json = super.toJson(needAll);
		json.put("viewType", viewType);
		json.put("timeFormat", needAll && timeFormat == null ? "" : timeFormat);
		json.put("showTime", showTime);
		json.put("showHeader", showHeader);
		json.put("showFooter", showFooter);
		json.put("showWeekNumber", showWeekNumber);
		json.put("showDaysHeader", showDaysHeader);
		json.put("showMonthButtons", showMonthButtons);
		json.put("showYearButtons", showYearButtons);
		json.put("showTodayButton", showTodayButton);
		json.put("showClearButton", showClearButton);
		return json;
	}
	
	public void fromJson(JSONObject json) throws JSONException{
		super.fromJson(json);
		if (json.has("viewType")){
			viewType = json.getString("viewType");
		}else
			viewType = ComboBoxViewType;
		if (json.has("timeFormat")){
			timeFormat = json.getString("timeFormat");
		}else
			timeFormat = "H:mm";
		if (json.has("showTime")){
			showTime = json.getBoolean("showTime");
		}else
			showTime = false;
		if (json.has("showHeader")){
			showHeader = json.getBoolean("showHeader");
		}else
			showHeader = true;
		if (json.has("showFooter")){
			showFooter = json.getBoolean("showFooter");
		}else
			showFooter = true;
		if (json.has("showWeekNumber")){
			showWeekNumber = json.getBoolean("showWeekNumber");
		}else
			showWeekNumber = true;
		if (json.has("showDaysHeader")){
			showDaysHeader = json.getBoolean("showDaysHeader");
		}else
			showDaysHeader = true;
		if (json.has("showMonthButtons")){
			showMonthButtons = json.getBoolean("showMonthButtons");
		}else
			showMonthButtons = true;
		if (json.has("showYearButtons")){
			showYearButtons = json.getBoolean("showYearButtons");
		}else
			showYearButtons = true;
		if (json.has("showTodayButton")){
			showTodayButton = json.getBoolean("showTodayButton");
		}else
			showTodayButton = true;
		if (json.has("showClearButton")){
			showClearButton = json.getBoolean("showClearButton");
		}else
			showClearButton = true;
	}

	public String viewType = ComboBoxViewType;
	public String timeFormat = "H:mm";
	public boolean showTime = false;
	public boolean showHeader = true;
	public boolean showFooter = true;
	public boolean showWeekNumber = true;
	public boolean showDaysHeader = true;
	public boolean showMonthButtons = true;
	public boolean showYearButtons = true;
	public boolean showTodayButton = true;
	public boolean showClearButton = true;
	
}