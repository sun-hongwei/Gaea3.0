package com.wh.draws.drawinfo;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;

import org.json.JSONException;
import org.json.JSONObject;

import com.wh.draws.UINode;

public abstract class ControlInfo extends DrawInfo{
	
	public ControlInfo(UINode node) {
		super(node);
	}

	protected abstract Image getImage();
	
	public JSONObject toJson(boolean needAll) throws JSONException{		
		JSONObject json = new JSONObject();
		json.put("id", needAll && id == null ? "" : id);
		return json;
	}
	
	public void fromJson(JSONObject json) throws JSONException{
		if (json.has("id"))
			id = json.getString("id");
		else
			id = "id";
	}

	public void drawNode(Graphics g, Rectangle rect){
		node.setRect(rect);
		Color old = g.getColor();
		g.setColor(Color.darkGray);
		
		g.drawRect(rect.x, rect.y, rect.width, rect.height);
		
		g.drawImage(getImage(), rect.x + 5, rect.y + 5, rect.width - 10, rect.height - 10, null);

		g.setColor(old);
	}


}
