package com.wh.draws.drawinfo;

import java.awt.Graphics;
import java.awt.Rectangle;

import org.json.JSONException;
import org.json.JSONObject;

import com.wh.draws.DrawNode;
import com.wh.draws.UINode;

public class ScrollBarInfo extends DrawInfo{
	public String typeName(){
		return DrawInfoDefines.ScrollBar_Name;
	}

	public ScrollBarInfo(UINode node) {
		super(node);
		width = "100%";
		height = "200px";
	}

	public void drawNode(Graphics g, Rectangle rect){		
		DrawNode.drawLineText(g, getFont(), textColor, rect.x, rect.y, rect.width, rect.height, "：" + name);
	}
	
	public JSONObject toJson(boolean needAll) throws JSONException{
		JSONObject json = super.toJson(needAll);
		json.put("loadingClass", needAll && loadingClass == null ? "" : loadingClass);
		json.put("nothingClass", needAll && nothingClass == null ? "" : nothingClass);
		json.put("loadPage", needAll && loadPage == null ? "" : loadPage);
		json.put("scrollTarget", needAll && scrollTarget == null ? "" : scrollTarget);
		json.put("heightOffset", needAll && heightOffset == null ? "" : heightOffset);
		json.put("tag", needAll && tag == null ? "" : tag);
		json.put("onGetPageHtml", needAll && onGetPageHtml == null ? "" : onGetPageHtml);
		json.put("inited", inited);
		return json;
	}
	
	public void fromJson(JSONObject json) throws JSONException{
		super.fromJson(json);
		if (json.has("loadingClass"))
			loadingClass = json.getString("loadingClass");
		else
			loadingClass = null;
		
		if (json.has("nothingClass"))
			nothingClass = json.getString("nothingClass");
		else
			nothingClass = null;

		if (json.has("loadPage"))
			loadPage = json.getString("loadPage");
		else
			loadPage = null;
		
		if (json.has("scrollTarget"))
			scrollTarget = json.getString("scrollTarget");
		else
			scrollTarget = "";
		
		if (json.has("heightOffset"))
			heightOffset = json.getString("heightOffset");
		else
			heightOffset = "0";
		
		if (json.has("tag"))
			tag = json.getString("tag");
		else
			tag = null;
		
		if (json.has("onGetPageHtml"))
			onGetPageHtml = json.getString("onGetPageHtml");

		if (json.has("inited"))
			inited = json.getBoolean("inited");
		else
			inited = false;
		
	}

    public String loadingClass;
    public String nothingClass;
    public String loadPage;
    public String scrollTarget;
    public String heightOffset = "0";
    public String tag;
    public String onGetPageHtml;
    public boolean inited = false;

}