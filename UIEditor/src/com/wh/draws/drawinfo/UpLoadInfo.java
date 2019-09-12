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

public class UpLoadInfo extends DrawInfo{
	public String typeName(){
		return DrawInfoDefines.UpLoad_Name;
	}
	public UpLoadInfo(UINode node) {
		super(node);
		allowEdit = false;
		width = "100px";
		height = "100px";
	}
	
	Image image;
	protected Image getButtonImage() {
		if (image == null)
			try {
				image = ImageUtils.loadImage(EditorEnvironment.getEditorSourcePath(
						EditorEnvironment.Image_Icons_Path, "upload.png"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		return image;
	}
	public void drawNode(Graphics g, Rectangle rect){
		Color old = g.getColor();
		int top = rect.y;
		int height = rect.height;
		if (showButton){
			caption = (caption == null || caption.isEmpty() ? "上传" : caption);
			Rectangle buttonRect = new Rectangle(rect.x, rect.y, 80, 30);
			drawButton(g, getFont(), textColor, buttonRect, getButtonImage(), caption);
			top += 35;
			height -= 35;
		}
		
		g.setColor(Color.WHITE);
		g.fillRect(rect.x + 2, top, rect.width - 4, height);
		
		g.setColor(old);
	}

	public JSONObject toJson(boolean needAll) throws JSONException{
		JSONObject json = super.toJson(needAll);
		json.put("autoScroll", autoScroll);
		json.put("caption", needAll && caption == null ? "" : caption);
		json.put("showButton", showButton);
		json.put("buttonImage", needAll && buttonImage == null ? "" : buttonImage);
		json.put("columns", columns);
		return json;
	}
	
	public void fromJson(JSONObject json) throws JSONException{
		super.fromJson(json);
		if (json.has("autoScroll"))
			autoScroll = json.getBoolean("autoScroll");
		else
			autoScroll = false;
		
		if (json.has("caption"))
			caption = json.getString("caption");
		else
			caption = "上传";
		
		if (json.has("showButton"))
			showButton = json.getBoolean("showButton");
		else
			showButton = true;
		
		if (json.has("buttonImage"))
			buttonImage = json.getString("buttonImage");
		else
			buttonImage = null;
		
		if (json.has("columns"))
			columns = json.getInt("columns");
		else
			columns = 3;
		
	}

	public boolean autoScroll = false;
	public boolean showButton = true;
	public String caption = "上传";
	public String buttonImage = "";
	public int columns = 3;

}