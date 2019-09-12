package com.wh.draws;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import org.json.JSONException;
import org.json.JSONObject;

import com.wh.control.grid.design.PropertyTableCellEditor.KeyValue;
import com.wh.draws.DrawCanvas.ICreateNodeSerializable;

public class WorkflowNode extends DrawNode {
	protected Color bgColor = new Color(46, 46, 46);
	
	public String useFrame = "true";
	public String useTab = "true";
	public String useCurrentTab = "false";
	public String useDialog = "false";
	public String useTopRegion = "true";
	public String useBottomRegion = "true";
	public String useLeftRegion = "true";
	public String useRightRegion = "false";
	public String rightRegionTopMax = "false";
	public String rightRegionBottomMax = "false";
	public String leftRegionTopMax = "false";
	public String leftRegionBottomMax = "false";
	public boolean dialogAutoSize = true;
	public boolean showVerticalScrollBar = true;
	public boolean showHorizontalScrollBar = false;
	public boolean useFormDataSource = false;
	public boolean allowClose = true;

	public KeyValue<String, String> runTaskInfo;

	public WorkflowNode(DrawCanvas canvas) {
		super(canvas);
	}

	public void fromJson(JSONObject data, ICreateNodeSerializable createUserDataSerializable) throws JSONException{
		super.fromJson(data, createUserDataSerializable);
		
		if (data.has("allowClose"))
			allowClose = data.getBoolean("allowClose");
		else
			allowClose = true;
		
		if (data.has("useFormDataSource"))
			useFormDataSource = data.getBoolean("useFormDataSource");
		else
			useFormDataSource = false;
		
		if (data.has("showHorizontalScrollBar"))
			showHorizontalScrollBar = data.getBoolean("showHorizontalScrollBar");
		else
			showHorizontalScrollBar = false;
		
		if (data.has("showVerticalScrollBar"))
			showVerticalScrollBar = data.getBoolean("showVerticalScrollBar");
		else
			showVerticalScrollBar = true;
		
		if (data.has("dialogAutoSize"))
			dialogAutoSize = data.getBoolean("dialogAutoSize");
		else
			dialogAutoSize = true;

		if (data.has("leftRegionTopMax"))
			leftRegionTopMax = data.getString("leftRegionTopMax");
		else
			leftRegionTopMax = "false";
		
		if (data.has("leftRegionBottomMax"))
			leftRegionBottomMax = data.getString("leftRegionBottomMax");
		else
			leftRegionBottomMax = "false";
		
		if (data.has("rightRegionTopMax"))
			rightRegionTopMax = data.getString("rightRegionTopMax");
		else
			rightRegionTopMax = "false";
		
		if (data.has("rightRegionBottomMax"))
			rightRegionBottomMax = data.getString("rightRegionBottomMax");
		else
			rightRegionBottomMax = "false";
		
		if (data.has("useFrame"))
			useFrame = data.getString("useFrame");
		else
			useFrame = "false";

		if (data.has("useTab"))
			useTab = data.getString("useTab");
		else
			useTab = "false";

		if (data.has("useDialog"))
			useDialog = data.getString("useDialog");
		else
			useDialog = "false";

		if (data.has("useTopRegion"))
			useTopRegion = data.getString("useTopRegion");
		else
			useTopRegion = "false";

		if (data.has("useBottomRegion"))
			useBottomRegion = data.getString("useBottomRegion");
		else
			useBottomRegion = "false";

		if (data.has("useLeftRegion"))
			useLeftRegion = data.getString("useLeftRegion");
		else
			useLeftRegion = "false";

		if (data.has("useRightRegion"))
			useRightRegion = data.getString("useRightRegion");
		else
			useRightRegion = "false";

		if (data.has("useCurrentTab"))
			useCurrentTab = data.getString("useCurrentTab");
		else
			useCurrentTab = "false";


		if (data.has("runTaskName")){
			try{
				runTaskInfo = new KeyValue<>(data.getJSONObject("runTaskName"));
			}catch (Exception e) {
				e.printStackTrace();
			}
		}else
			runTaskInfo = null;

	}
	
	public JSONObject toJson() throws JSONException{
		JSONObject data = super.toJson();

		if (runTaskInfo != null)
			data.put("runTaskName", runTaskInfo.toJson());
		
		data.put("allowClose", allowClose);
		data.put("useFormDataSource", useFormDataSource);
		data.put("showVerticalScrollBar", showVerticalScrollBar);
		data.put("showHorizontalScrollBar", showHorizontalScrollBar);

		data.put("dialogAutoSize", dialogAutoSize);
		
    	if (leftRegionTopMax != null && !leftRegionTopMax.isEmpty())
    		data.put("leftRegionTopMax", leftRegionTopMax);
    	if (leftRegionBottomMax != null && !leftRegionBottomMax.isEmpty())
    		data.put("leftRegionBottomMax", leftRegionBottomMax);
    	if (rightRegionTopMax != null && !rightRegionTopMax.isEmpty())
    		data.put("rightRegionTopMax", rightRegionTopMax);
    	if (rightRegionBottomMax != null && !rightRegionBottomMax.isEmpty())
    		data.put("rightRegionBottomMax", rightRegionBottomMax);
    	
    	if (useFrame != null && !useFrame.isEmpty())
    		data.put("useFrame", useFrame);
    	if (useTab != null && !useTab.isEmpty())
    		data.put("useTab", useTab);
    	if (useDialog != null && !useDialog.isEmpty())
    		data.put("useDialog", useDialog);
    	if (useTopRegion != null && !useTopRegion.isEmpty())
    		data.put("useTopRegion", useTopRegion);
    	if (useBottomRegion != null && !useBottomRegion.isEmpty())
    		data.put("useBottomRegion", useBottomRegion);
    	if (useLeftRegion != null && !useLeftRegion.isEmpty())
    		data.put("useLeftRegion", useLeftRegion);
    	if (useRightRegion != null && !useRightRegion.isEmpty())
    		data.put("useRightRegion", useRightRegion);
    	if (useCurrentTab != null && !useCurrentTab.isEmpty())
    		data.put("useCurrentTab", useCurrentTab);
    	return data;
	}
	
	protected void drawNode(Graphics g){
		g.setColor(bgColor);	
		Rectangle rect = getRect();
		g.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 10, 10);
		drawText(g, font, rect, new StringBuilder(title == null ? "" : title));
	}
	
	public static class BeginNode extends WorkflowNode{

		public BeginNode(DrawCanvas canvas) {
			super(canvas);
		}
		
		protected void drawNode(Graphics g){
			g.setColor(bgColor);	
			Rectangle rect = getRect();

			rect.width = 100;
			rect.height = 40;
			g.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 20, 20);
			drawText(g, font, rect, new StringBuilder(title == null ? "" : title));
		}
		
	}

	public static class EndNode extends WorkflowNode{

		public EndNode(DrawCanvas canvas) {
			super(canvas);
		}
		
		protected void drawNode(Graphics g){
			g.setColor(bgColor);		
			Rectangle rect = getRect();

			rect.width = 100;
			rect.height = 40;
			g.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 20, 20);
			drawText(g, font, rect, new StringBuilder(title == null ? "" : title));
		}
		
	}


	public static class ChildWorkflowNode extends WorkflowNode{

		public ChildWorkflowNode(DrawCanvas canvas) {
			super(canvas);
			bgColor = new Color(0, 74, 123);
		}

	}
}
