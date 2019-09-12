package com.wh.draws.drawinfo;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;

import org.json.JSONException;
import org.json.JSONObject;

import com.wh.control.EditorEnvironment;
import com.wh.draws.UINode;
import com.wh.system.tools.ImageUtils;

public class ButtonInfo extends ClickableInfo{
	public String typeName(){
		return DrawInfoDefines.Button_Name;
	}
	
	Image image;
	String imagename;
	public ButtonInfo(UINode node) {
		super(node);
		needBackground = false;
		needFrame = false;
		textColor = Color.YELLOW;
		width = "82px";
		height = "30px";
	}

	protected Image getButtonImage() {
		if (image == null || (img != null && img.compareTo((imagename == null ? "" : imagename)) != 0)){
			if (img == null || img.isEmpty())
				return null;
			
			try {
				image = ImageUtils.loadImage(EditorEnvironment.getProjectFile(EditorEnvironment.Image_Resource_Path, img));
			} catch (Exception e) {
				image = null;
				e.printStackTrace();
			}
		}
		return image;
	}
	
	public void drawNode(Graphics g, Rectangle rect){	
		drawButton(g, getFont(), textColor, rect, getButtonImage(), value.toString());
	}
	
	public JSONObject toJson(boolean needAll) throws JSONException{
		JSONObject json = super.toJson(needAll);
		json.put("plain", plain);
		json.put("img", needAll && img == null ? "" : img);
		json.put("iconCls", needAll && iconCls == null ? "" : iconCls);
		json.put("mode", needAll && mode == null ? "" : mode);
		return json;
	}
	
	public void fromJson(JSONObject json) throws JSONException{
		super.fromJson(json);

		if (json.has("mode"))
			mode = json.getString("mode");
		else
			mode = "button";

		if (json.has("plain"))
			plain = json.getBoolean("plain");
		else
			plain = false;

		if (json.has("img"))
			img = json.getString("img");
		else
			img = null;

		if (json.has("iconCls"))
			iconCls = json.getString("iconCls");
		else
			iconCls = null;
	}

	public boolean plain = false;
	public String img;
	public String iconCls;
	
	public String mode = "button";
	
}