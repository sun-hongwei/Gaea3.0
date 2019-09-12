package com.wh.draws.drawinfo;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import com.alee.utils.ImageUtils;
import com.wh.draws.DrawNode;
import com.wh.draws.UINode;
import com.wh.form.Defines;
import com.wh.system.tools.ColorConvert;

public class DivInfo extends DrawInfo{
	public enum DivType{
		dtCanvas, dtDiv, dtBarCode
	}
	
	public String typeName(){
		return DrawInfoDefines.Div_Name;
	}
	public DivInfo(UINode node) {
		super(node);
		allowEdit = false;
		needFrame = false;
		width = "100px";
		height = "100px";
	}
	
	public JSONObject toJson(boolean needAll) throws JSONException{
		JSONObject json = super.toJson(needAll);
		
		json.put("divType", divType.name());
		json.put("caption", needAll && caption == null ? "" : caption);
		json.put("backgroundColor", needAll && backgroundColor == null ? "" : backgroundColor);
		json.put("border", border);
		json.put("expand", expand);
		json.put("collapsing", collapsing);
		json.put("showScrollbar", showScrollbar);
		json.put("expandTitleHeight", expandTitleHeight);
		json.put("titleClass", needAll && titleClass == null ? "" : titleClass);
		json.put("showVerticalScrollBar", showVerticalScrollBar);
		json.put("showHorizontalScrollBar", showHorizontalScrollBar);
		json.put("barCode", needAll && barCode == null ? "" : barCode);

		return json;
	}
	
	public void fromJson(JSONObject json) throws JSONException{
		super.fromJson(json);
		
		if (json.has("barCode"))
			barCode = json.getString("barCode");
		else
			barCode = null;
		
		if (json.has("divType"))
			divType = DivType.valueOf(json.getString("divType"));
		else
			divType = DivType.dtDiv;
		
		if (json.has("showHorizontalScrollBar"))
			showHorizontalScrollBar = json.getBoolean("showHorizontalScrollBar");
		else
			showHorizontalScrollBar = false;
		
		if (json.has("showVerticalScrollBar"))
			showVerticalScrollBar = json.getBoolean("showVerticalScrollBar");
		else
			showVerticalScrollBar = true;
		
		if (json.has("titleClass"))
			titleClass = json.getString("titleClass");
		else
			titleClass = null;

		if (json.has("collapsing"))
			collapsing = json.getBoolean("collapsing");
		else
			collapsing = false;
		
		if (json.has("caption"))
			caption = json.getString("caption");
		else
			caption = DrawInfoDefines.Div_Name;
		
		if (json.has("expandTitleHeight"))
			expandTitleHeight = json.getInt("expandTitleHeight");
		else
			expandTitleHeight = 20;
		
		if (json.has("showScrollbar"))
			showScrollbar = json.getBoolean("showScrollbar");
		else
			showScrollbar = false;
		
		if (json.has("border"))
			border = json.getBoolean("border");
		else
			border = true;
		
		if (json.has("expand"))
			expand = json.getBoolean("expand");
		else
			expand = true;
		
		if (json.has("backgroundColor"))
			backgroundColor = json.getString("backgroundColor");
		else
			backgroundColor = null;
		
	}

	public boolean showVerticalScrollBar = true;
	public boolean showHorizontalScrollBar = false;

	public String caption = DrawInfoDefines.Div_Name;
	public boolean border = true;
	public boolean showScrollbar = false;
	public String backgroundColor;
	public String titleClass;

	public boolean expand = false;
	public boolean collapsing = false;
	
	public int expandTitleHeight = 20;
	
	public DivType divType = DivType.dtDiv;
	
	public String barCode = null;
	
	public void drawNode(Graphics g, Rectangle rect){
		Color old = g.getColor();
		if (backgroundColor != null){
			g.setColor(ColorConvert.toColorFromString(backgroundColor));
			g.fillRect(rect.x, rect.y, rect.width, rect.height);
		}
		
		if (divType == DivType.dtBarCode){			
			BufferedImage image = ImageUtils.loadImage(new File(Defines.Java_Dir_Icon_Resource.getAbsolutePath(), "barcode.png"));
			g.drawImage(image, rect.x + 5, rect.y + 5, rect.width - 10, rect.height - 10, null);
		}
		g.setColor(Color.darkGray);
		g.drawRect(rect.x, rect.y, rect.width, rect.height);
		
		if (caption != null && !caption.isEmpty()){
			DrawNode.drawLineText(g, font, textColor, rect.x, 
					rect.y, rect.width, rect.height, caption);
		}
		
		g.setColor(old);
	}

}