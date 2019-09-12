package com.wh.draws.drawinfo;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

import org.json.JSONException;
import org.json.JSONObject;

import com.wh.draws.DrawNode;
import com.wh.draws.UINode;

public class LabelInfo extends ClickableInfo{
	public String typeName(){
		return DrawInfoDefines.Label_Name;
	}
	public LabelInfo(UINode node) {
		super(node);
		needBackground = false;
		needFrame = false;
		allowEdit = false;
	}
	
	public void drawNode(Graphics g, Rectangle rect){	
		String text = value == null ? "" : value.toString();
		if (text == null || text.isEmpty())
			return;
		
		if (autosize){
			Rectangle2D textRect = DrawNode.getTextRectangle(g, getFont(), text);
			int newWidth = (int) textRect.getWidth();
			int newHeight = (int) textRect.getHeight();
			if (newWidth != rect.width || newHeight != rect.height){
				rect.width = newWidth;
				rect.height = newHeight;
				node.fixSize(false);
			}
		}
		DrawNode.drawLineText(g, getFont(), textColor, rect.x, rect.y, rect.width, rect.height, text, false, false);
	}

	public void fromJson(JSONObject json) throws JSONException{
		super.fromJson(json);
		if (json.has("align"))
			align = json.getString("align");
		else
			align = "center";
		
		if (json.has("href"))
			href = json.getString("href");
		else
			href = null;

		if (json.has("download"))
			download = json.getString("download");
		else
			download = null;

		if (json.has("target"))
			target = json.getString("target");
		else
			target = null;
		
		if (json.has("fontVariant"))
			fontVariant = json.getString("fontVariant");
		else
			fontVariant = "normal";

		if (json.has("gradient"))
			gradient = json.getString("gradient");
		else
			gradient = null;
		
		if (json.has("autosize"))
			autosize = json.getBoolean("autosize");
		else
			autosize = true;
		
		if (json.has("showLinkLine"))
			showLinkLine = json.getBoolean("showLinkLine");
		else
			showLinkLine = false;
	}

	public JSONObject toJson(boolean needAll) throws JSONException{
		JSONObject json = super.toJson(needAll);
		json.put("align", needAll && align == null ? "center" : align);
		json.put("href", needAll && href == null ? "" : href);
		json.put("download", needAll && download == null ? "" : download);
		json.put("target", needAll && target == null ? "_self" : target);
		json.put("fontVariant", needAll && fontVariant == null ? "normal" : fontVariant);
		json.put("gradient", needAll && gradient == null ? "" : gradient);
		json.put("autosize", autosize);
		json.put("showLinkLine", showLinkLine);
		return json;
	}
	
	public String href;
	public String download;
	public String target = "_self";
	public String align = "center";
	public String fontVariant = "normal";
	public boolean autosize = true;
	public String gradient;
	public boolean showLinkLine = false;

}