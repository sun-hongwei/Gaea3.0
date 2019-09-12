package com.wh.draws.drawinfo;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.SwingUtilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wh.draws.DrawNode;
import com.wh.draws.UINode;
import com.wh.form.Defines;
import com.wh.system.tools.ImageUtils;
import com.wh.system.tools.JsonHelp;

public class TreeInfo extends DrawInfo{
	public String typeName(){
		return DrawInfoDefines.Tree_Name;
	}
	public TreeInfo(UINode node) {
		super(node);
		width = "30%";
		height = "35%";
		JSONArray obj = new JSONArray();
		try {
			JSONObject data = new JSONObject();
			data.put("id", 0);
			data.put("text", "根节点");
			obj.put(data);
			
			data = new JSONObject();
			data.put("id", 1);
			data.put("text", "数据1");
			data.put("pid", 0);
			obj.put(data);

			data = new JSONObject();
			data.put("id", 2);
			data.put("text", "数据2");
			data.put("pid", 0);
			obj.put(data);
			value = obj;
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	BufferedImage icon;
	
	protected void drawItem(Rectangle rect, Graphics g, int left, AtomicInteger top, int width, String text){
		int drawTextLeft = left + icon.getWidth() + 5;
		int drawTextWidth = width - icon.getWidth() - 5;

		int height = DrawNode.drawLineText(g, getFont(), textColor, drawTextLeft, top.get(), drawTextWidth, -1, text, false, true);
		if (!rect.contains(new Point(left, top.get() + height)))
			return;

		g.drawImage(icon, left, top.get(), null);
		
		DrawNode.drawLineText(g, getFont(), textColor, drawTextLeft, top.get(), drawTextWidth, -1, text, false);
		top.addAndGet(Math.max(height, icon.getHeight()) + 5);
	}
	
	protected void drawView(Rectangle rect, Graphics g, JSONArray data, int left , AtomicInteger top, int width, int height) throws JSONException {
		TreeMap<String, List<JSONObject>> treedatas = new TreeMap<>();
		for (int i = 0; i < data.length(); i++) {
			JSONObject json = data.getJSONObject(i);
			String key = null;
			if (json.has("pid"))
				key = JsonHelp.getString(json, "pid");
			if (key == null || key.isEmpty())
				key = "";
			
			List<JSONObject> list = null;
			if (treedatas.containsKey(key))
				list = treedatas.get(key);
			else{
				list = new ArrayList<>();
				treedatas.put(key, list);
			}
			
			list.add(json);
		}
		
		if (treedatas.size() > 0){
			drawView(rect, g, treedatas.get(""),  treedatas, left, top, width, height);
		}
	}
	
	protected void drawView(Rectangle rect, Graphics g, List<JSONObject> data, TreeMap<String, List<JSONObject>> treedatas, 
			int left , AtomicInteger top, int width, int height) throws JSONException {
		left += 5;
		top.addAndGet(5);
		
		for(JSONObject value : data){
			drawItem(rect, g, left, top, width, value.getString("text"));
			String id = JsonHelp.getString(value, "id");
			if (treedatas.containsKey(id)){
				drawView(rect, g, treedatas.get(id),  treedatas, left + 20, top, width, height);
			}
		}
	}
	
	public void drawNode(Graphics g, Rectangle rect){
		if (needFrame != border){
			needFrame = border;
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					node.getCanvas().repaint();
				}
			});
			return;
		}
		if (icon == null){
			try {
				icon = ImageUtils.loadImage(new File(Defines.Java_Dir_Icon_Resource.getAbsolutePath(), "nocheck.png"));
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
		
		int left = rect.x + 5;
		AtomicInteger top = new AtomicInteger(rect.y + 5);
		try {
			if (value instanceof String)
				value = new JSONArray((String)value);
			drawView(rect, g, (JSONArray)value, left, top, rect.width, rect.y + rect.height);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public JSONObject toJson(boolean needAll) throws JSONException{
		JSONObject json = super.toJson(needAll);
		json.put("OnGetLoadParam", needAll && OnGetLoadParam == null ? "" : OnGetLoadParam);
		json.put("showTreeIcon", needAll && showTreeIcon == null ? "true" : showTreeIcon);
		json.put("checkedField", needAll && checkedField == null ? "" : checkedField);
		json.put("resultAsTree", needAll && resultAsTree == null ? "true" : resultAsTree);
		json.put("url", needAll && url == null ? "" : url);
		json.put("allowSelect", needAll && allowSelect == null ? "true" : allowSelect);
		json.put("showCheckBox", needAll && showCheckBox == null ? "false" : showCheckBox);
		json.put("showTreeLines", needAll && showTreeLines == null ? "true" : showTreeLines);
		json.put("expandOnLoad", needAll && expandOnLoad == null ? "" : expandOnLoad);
		json.put("showFolderCheckBox", needAll && showFolderCheckBox == null ? "true" : showFolderCheckBox);
		json.put("showExpandButtons", needAll && showExpandButtons == null ? "false" : showExpandButtons);
		json.put("enableHotTrack", needAll && enableHotTrack == null ? "true" : enableHotTrack);
		json.put("expandOnDblClick", needAll && expandOnDblClick == null ? "false" : expandOnDblClick);
		json.put("expandOnNodeClick", needAll && expandOnNodeClick == null ? "true" : expandOnNodeClick);
		json.put("checkRecursive", needAll && checkRecursive == null ? "true" : checkRecursive);
		json.put("autoCheckParent", needAll && autoCheckParent == null ? "false" : autoCheckParent);
		json.put("allowLeafDropIn", needAll && allowLeafDropIn == null ? "false" : allowLeafDropIn);
		json.put("allowDrag", needAll && allowDrag == null ? "false" : allowDrag);
		json.put("allowDrop", needAll && allowDrag == null ? "false" : allowDrop);
		json.put("showFilterRow", needAll && showFilterRow == null ? "false" : showFilterRow);
		json.put("inited", inited);
		json.put("classname", needAll && classname == null ? "mini-outlooktree" : classname);
		json.put("border", border);
		return json;
	}
	
	public void fromJson(JSONObject json) throws JSONException{
		super.fromJson(json);
		if (json.has("OnGetLoadParam"))
			OnGetLoadParam = json.getString("OnGetLoadParam");
		else
			OnGetLoadParam = null;
		
		if (json.has("showTreeIcon"))
			showTreeIcon = json.getString("showTreeIcon");
		else
			showTreeIcon = "true";
		
		if (json.has("checkedField"))
			checkedField = json.getString("checkedField");
		else
			checkedField = "";
			
		if (json.has("resultAsTree"))
			resultAsTree = json.getString("resultAsTree");
		else
			resultAsTree = "true";
		
		if (json.has("allowSelect"))
			allowSelect = json.getString("allowSelect");
		else
			allowSelect = "true";
		
		if (json.has("showCheckBox"))
			showCheckBox = json.getString("showCheckBox");
		else
			showCheckBox = "false";
		
		if (json.has("showTreeLines"))
			showTreeLines = json.getString("showTreeLines");
		else
			showTreeLines = "true";
		
		if (json.has("expandOnLoad"))
			expandOnLoad = json.getString("expandOnLoad");
		else
			expandOnLoad = "true";
		
		if (json.has("showFolderCheckBox"))
			showFolderCheckBox = json.getString("showFolderCheckBox");
		else
			showFolderCheckBox = "false";
		
		if (json.has("showExpandButtons"))
			showExpandButtons = json.getString("showExpandButtons");
		else
			showExpandButtons = "true";
		
		if (json.has("enableHotTrack"))
			enableHotTrack = json.getString("enableHotTrack");
		else
			enableHotTrack = "true";
		
		if (json.has("expandOnDblClick"))
			expandOnDblClick = json.getString("expandOnDblClick");
		else
			expandOnDblClick = "false";
		
		if (json.has("expandOnNodeClick"))
			expandOnNodeClick = json.getString("expandOnNodeClick");
		else
			expandOnNodeClick = "false";
		
		if (json.has("checkRecursive"))
			checkRecursive = json.getString("checkRecursive");
		else
			checkRecursive = "true";
		
		if (json.has("autoCheckParent"))
			autoCheckParent = json.getString("autoCheckParent");
		else
			autoCheckParent = "true";
		
		if (json.has("allowLeafDropIn"))
			allowLeafDropIn = json.getString("allowLeafDropIn");
		else
			allowLeafDropIn = "false";
		
		if (json.has("allowDrag"))
			allowDrag = json.getString("allowDrag");
		else
			allowDrag = "false";
		
		if (json.has("allowDrop"))
			allowDrop = json.getString("allowDrop");
		else
			allowDrop = "false";
		
		if (json.has("showFilterRow"))
			showFilterRow = json.getString("showFilterRow");
		else
			showFilterRow = "false";
		
		if (json.has("inited"))
			inited = json.getBoolean("inited");
		else
			inited = false;

		if (json.has("classname"))
			classname = json.getString("classname");
		else
			classname = "mini-outlooktree";
		
		if (json.has("border"))
			border = json.getBoolean("border");
		else
			border = true;
	}

	public String OnGetLoadParam;
	public String showTreeIcon = "true";
    public String checkedField;
    public String resultAsTree = "false";
    public String url;
    public String allowSelect = "true";
    public String showCheckBox = "false";
    public String showTreeLines = "true";
    public String expandOnLoad = "true";
    public String showFolderCheckBox = "false";
    public String showExpandButtons = "true";
    public String enableHotTrack = "true";
    public String expandOnDblClick = "false";
    public String expandOnNodeClick = "false";
    public String checkRecursive = "true";
    public String autoCheckParent = "true";
    public String allowLeafDropIn = "true";
    public String allowDrag = "false";
    public String allowDrop = "false";
    public String showFilterRow = "false";
    public boolean inited = false;
    public String classname = "mini-outlooktree";
    public boolean border = true;

}