package com.wh.draws.drawinfo;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import com.wh.draws.UINode;
import com.wh.form.Defines;
import com.wh.system.tools.ImageUtils;

public class ChartInfo extends DrawInfo{
	public String typeName(){
		return DrawInfoDefines.Chart_Name;
	}
	
	public enum ChartType{
		ctBar, ctLine, ctDash, ctPie, ctRadar, ctNone
	}
	
	public ChartInfo(UINode node) {
		super(node);
	}
	
	public String chartData;
	public boolean showToolbar;
	public String subTitle;
	public boolean autoLoad = true;
	public String resultName = "option";
	public String functionParams;
	
	protected Image getImage() {
		if (image != null){// && oldChartType == chartType){
			return image;
		}
		
		String name = "default.png";

		File file = new File(Defines.Java_Dir_Icon_Resource.getAbsolutePath(), "chart");
		file = new File(file, name);
		
		if (!file.exists())
			return null;
		
		try {
			image = ImageUtils.loadImage(file);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return image;
	}
	
	public JSONObject toJson(boolean needAll) throws JSONException{
		JSONObject json = super.toJson(needAll);
		json.remove("scaleMode");
		json.put("chartData", needAll && chartData == null ? "" : chartData);
		json.put("subTitle", needAll && subTitle == null ? "" : subTitle);
		json.put("resultName", needAll && resultName == null ? "" : resultName);
		json.put("functionParams", needAll && functionParams == null ? "" : functionParams);
		json.put("showToolbar", showToolbar);
		json.put("autoLoad", autoLoad);
		return json;
	}
	
	public void fromJson(JSONObject json) throws JSONException{
		super.fromJson(json);

		if (json.has("autoLoad"))
			autoLoad = json.getBoolean("autoLoad");
		else
			autoLoad = true;
		
		if (json.has("resultName"))
			resultName = json.getString("resultName");
		else
			resultName = "option";
		
		if (json.has("functionParams"))
			functionParams = json.getString("functionParams");
		else
			functionParams = null;
		
		if (json.has("subTitle"))
			subTitle = json.getString("subTitle");
		else
			subTitle = null;
		
		if (json.has("chartData"))
			chartData = json.getString("chartData");
		else
			chartData = null;
		
		if (json.has("showToolbar"))
			showToolbar = json.getBoolean("showToolbar");
		else
			showToolbar = true;
	}

	public void drawNode(Graphics g, Rectangle rectangle){
		super.drawNode(g, rectangle);
	}
}