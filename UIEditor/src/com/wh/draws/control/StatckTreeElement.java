package com.wh.draws.control;

import java.awt.Point;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wh.draws.DrawCanvas;
import com.wh.draws.DrawNode;

public class StatckTreeElement {
	public String id;
	public String parentid;
	public Point location = new Point();
	DrawCanvas canvas;
	public HashMap<String, String> childs = new HashMap<>();

	public StatckTreeElement(DrawCanvas canvas) {
		this.canvas = canvas;
	}

	public StatckTreeElement(DrawCanvas canvas, String id, String parentid) {
		this.id = id;
		this.parentid = parentid;
		this.canvas = canvas;
		if (this.canvas.getNode(id) == null)
			throw new NullPointerException("未发现id：" + id + "的节点！");
		this.location = this.canvas.getNode(id).getRect().getLocation();
	}

	public JSONObject toJson() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("id", id);
		json.put("parentid", parentid);
		json.put("x", location.x);
		json.put("y", location.y);
		if (parentid != null && !parentid.isEmpty()) {
			DrawNode cur = canvas.getNode(id);
			DrawNode parent = canvas.getNode(parentid);
			cur.invalidRect();
			parent.invalidRect();
			json.put("offx", cur.getRect().x - parent.getRect().x);
			json.put("offy", cur.getRect().y - parent.getRect().y);
		}

		JSONArray childsData = new JSONArray();
		for (String child : childs.keySet()) {
			childsData.put(child);
		}
		json.put("childs", childsData);

		return json;
	}

	public void fromJson(JSONObject json) throws JSONException {
		id = json.getString("id");

		if (json.has("parentid"))
			parentid = json.getString("parentid");

		if (json.has("x"))
			location.x = json.getInt("x");

		if (json.has("y"))
			location.y = json.getInt("y");

		childs.clear();
		JSONArray childsData = json.getJSONArray("childs");
		for (int i = 0; i < childsData.length(); i++) {
			String value = childsData.getString(i);
			childs.put(value, value);
		}
	}
}
