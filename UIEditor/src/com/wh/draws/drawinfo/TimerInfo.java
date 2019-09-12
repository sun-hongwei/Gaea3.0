package com.wh.draws.drawinfo;

import java.awt.Image;
import java.awt.Toolkit;

import org.json.JSONException;
import org.json.JSONObject;

import com.wh.draws.UINode;
import com.wh.form.MainForm;

public class TimerInfo extends ControlInfo{
	public static final String CIRCULATION_TIMER = "circ";
	public static final String ONCE_TIMER = "once";
	
	public TimerInfo(UINode node) {
		super(node);
	}
	
	public JSONObject toJson(boolean needAll) throws JSONException{		
		JSONObject json = super.toJson(needAll);
		json.put("timer_type", needAll && timer_type == null ? "" : timer_type);
		json.put("interval", interval);
		json.put("times", times);
		return json;
	}
	
	public void fromJson(JSONObject json) throws JSONException{
		super.fromJson(json);
		
		if (json.has("timer_type"))
			timer_type = json.getString("timer_type");
		else
			timer_type = "CIRCULATION_TIMER";
				
		if (json.has("interval"))
			interval = json.getInt("interval");
		else
			interval = 1000;
				
		if (json.has("times"))
			times = json.getInt("times");
		else
			times = 0;
				
	}

	public String timer_type = CIRCULATION_TIMER;
	public int interval = 1000;
	public int times = 0;
	
	@Override
	protected Image getImage() {
		return Toolkit.getDefaultToolkit().getImage(MainForm.class.getResource("/image/timer64.png"));
	}

	@Override
	public String typeName() {
		return DrawInfoDefines.Timer_Name;
	}
	
}