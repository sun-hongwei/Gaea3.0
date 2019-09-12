package com.wh.draws.drawinfo;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wh.draws.DrawCanvas;
import com.wh.draws.DrawNode;
import com.wh.draws.UINode;
import com.wh.draws.DrawCanvas.ICreateNodeSerializable;
import com.wh.draws.DrawCanvas.IDataSerializable;
import com.wh.system.tools.ColorConvert;
import com.wh.system.tools.JsonHelp;

public class ToolbarInfo extends DrawInfo {

	public String typeName(){
		return DrawInfoDefines.Toolbar_Name;
	}
	
	public enum AttatchWay{
		awTop, awBottom
	}
	
	public Color lineColor = ColorConvert.toColorFromString("0xFFd3d3d3");
	public Color backgroundColor = Color.WHITE;
	public boolean border = true;
	public String data;
	public String attachControlName;
	public int attatchSpace = 5;
	public AttatchWay attatchWay = AttatchWay.awTop;
	
	public JSONObject toJson(boolean needAll) throws JSONException{
		JSONObject json = super.toJson(needAll);
		json.put("data", needAll && data == null ? "" : data);
		json.put("border", border);
		json.put("attachControlName", needAll && attachControlName == null ? "" : attachControlName);
		json.put("attatchWay", needAll && attatchWay == null ? AttatchWay.awTop.name() : attatchWay.name());
		json.put("lineColor", needAll && lineColor == null ? "0xFFd3d3d3" : ColorConvert.toHexFromColor(lineColor));
		json.put("backgroundColor", needAll && backgroundColor == null ? "0xFFFFFFFF" : ColorConvert.toHexFromColor(backgroundColor));
		json.put("attatchSpace", attatchSpace);
		return json;
	}
	
	public void fromJson(JSONObject json) throws JSONException{
		super.fromJson(json);
		
		if (json.has("border")){
			border = json.getBoolean("border");
		}

		if (json.has("lineColor"))
			lineColor = ColorConvert.toColorFromString(json.getString("lineColor"));

		if (json.has("backgroundColor"))
			backgroundColor = ColorConvert.toColorFromString(json.getString("backgroundColor"));

		if (json.has("data")){
			data = json.getString("data");
		}

		if (json.has("attachControlName")){
			attachControlName = json.getString("attachControlName");
		}

		if (json.has("attatchWay")){
			attatchWay = AttatchWay.valueOf(json.getString("attatchWay"));
		}

		if (json.has("attatchSpace"))
			attatchSpace = json.getInt("attatchSpace");
		
		reset();
	}
	
	public void onResize() {
	}

	public List<UINode> childs = new ArrayList<>();
	
	public void reset(){
		childs.clear();
		if (data == null || data.isEmpty())
			return;
		JSONArray datas;
		try {
			datas = (JSONArray) JsonHelp.parseJson(data);
			for (int i = 0; i < datas.length(); i++) {
				DrawNode childNode = DrawNode.fromJson(node.getCanvas(), datas.getJSONObject(i), new ICreateNodeSerializable() {
					
					@Override
					public DrawNode newDrawNode(JSONObject json) {
						return new UINode(node.getCanvas());
					}
					
					@Override
					public IDataSerializable getUserDataSerializable(DrawNode node) {
						return null;
					}
				});
				childs.add((UINode)childNode);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (node != null && node.getCanvas() != null)
			node.getCanvas().repaint();
	}
	
	public void setCanvas(DrawCanvas canvas){
		this.node.setCanvas(canvas);
		if (childs.size() == 0)
			return;
		
		for (DrawNode node : childs) {
			node.setCanvas(canvas);
		}
	}
	
	public void drawNode(Graphics g, Rectangle rect){	
		if (attachControlName != null && !attachControlName.isEmpty()){
			UINode attachNode = null;
			for (DrawNode node : node.getCanvas().nodes.values()) {
				UINode uiNode = (UINode)node;
				if (uiNode.getDrawInfo() != null && uiNode.getDrawInfo().id.compareTo(attachControlName) == 0){
					attachNode = uiNode;
					break;
				}
			}
			
			if (attachNode != null){
				Rectangle rectangle = attachNode.getRect();
				rect.x = rectangle.x;
				rect.width = rectangle.width;
				switch (attatchWay) {
				case awBottom:
					rect.y = rectangle.y + rectangle.height + attatchSpace;
					break;
				case awTop:
					rect.y = rectangle.y - rect.height - attatchSpace;
					break;
				}
			}
		}
		
		Color old = g.getColor();
		g.setColor(backgroundColor);
		g.fillRect(rect.x, rect.y, rect.width, rect.height);
		if (border){
			g.setColor(lineColor);
			g.drawRect(rect.x, rect.y, rect.width, rect.height);
		}
		g.setColor(old);
		
		int left = rect.x;
		int top = rect.y;
		
		for (UINode node : childs) {
			Rectangle r = node.getRect();
			try {
				r.x = left + (node.getDrawInfo().jsonData.has("space") ? node.getDrawInfo().jsonData.getInt("space") : 3);
			} catch (JSONException e) {
				r.x = left + 3;
				e.printStackTrace();
			}
			r.y = top + 5;
			r.height = rect.height - 10;
			
			if (r.x + r.width > rect.x + rect.width){
				break;
			}
			node.drawNode(g);
			left = r.x + r.width;
		}
	}

	public ToolbarInfo(UINode node) {
		super(node);
		width = "400";
		height = "35";
		needFrame = false;
		allowEdit = false;
	}
	
}