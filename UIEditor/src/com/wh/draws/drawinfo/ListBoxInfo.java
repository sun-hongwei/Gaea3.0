package com.wh.draws.drawinfo;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wh.draws.UINode;
import com.wh.form.Defines;
import com.wh.system.tools.ImageUtils;
import com.wh.system.tools.JsonHelp;

public class ListBoxInfo extends ComboInfo{
	public String typeName(){
		return DrawInfoDefines.ListBox_Name;
	}
	
	public ListBoxInfo(UINode node) {
		super(node);
		width = "150";
		height = "150";
		needImage = false;
		drawWay = DrawWay.dwLeft;
		fillSample();
	}

	public void fillSample(){
		JSONArray json = new JSONArray();
		try {
			json.put(new JSONObject("{\"id\":\"id1\",\"text\":\"列表信息项目1\"}"));
			json.put(new JSONObject("{\"id\":\"id2\",\"text\":\"列表信息项目2\"}"));
			json.put(new JSONObject("{\"id\":\"id3\",\"text\":\"列表信息项目3\"}"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		data = json.toString();
	}

	protected String getDisplayText(Object value) {
		try {
			return ((JSONObject)value).getString(textField);
		} catch (JSONException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	protected BufferedImage getIcon(Object value) {
		if (icon == null){
			try {
				icon = ImageUtils.loadImage(new File(Defines.Java_Dir_Icon_Resource.getAbsolutePath(), "nocheck.png"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return icon;
	}

	public void drawNode(Graphics g, Rectangle rect){
		int left = rect.x;
		int top = rect.y + 5;
		try {
			if (this.data == null || this.data.isEmpty())
				return;
			
			JSONArray data = (JSONArray)JsonHelp.parseJson(this.data);
			
			int height = 0;
			for (int i = 0; i < data.length(); i++) {						
				String value = getDisplayText(data.get(i));
				height += getTextHeight(g, value);
			}					

			if (height < rect.height){
				top = rect.y + (rect.height - height) / 2;
			}
			
			for (int i = 0; i < data.length(); i++) {
				icon = getIcon(data.get(i));
				String value = getDisplayText(data.get(i));
				height = getTextHeight(g, value);
				top = drawItem(g, left, top, rect.width, height, false, true, value);
			}					
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public JSONObject toJson(boolean needAll) throws JSONException{
		JSONObject json = super.toJson(needAll);
		json.put("align", align);
		return json;
	}
	
	public void fromJson(JSONObject json) throws JSONException{
		super.fromJson(json);
		if (json.has("align"))
			align = json.getString("align");
		else
			align = "center";
	}

	public String align = "center";


}