package com.wh.draws;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import com.wh.control.grid.design.PropertyTableCellEditor.KeyValue;
import com.wh.draws.DrawCanvas.ICreateNodeSerializable;
import com.wh.form.Defines;
import com.wh.system.tools.ImageUtils;

public abstract class FlowNode extends DrawNode {

	public static final String BeginNode_Title = "起始节点";
	public static final String EndNode_Title = "结束节点";
	public static final String ProcessNode_Title = "功能节点";
	public static final String StateNode_Title = "状态节点";
	public static final String ActionNode_Title = "动作节点";
	public static final String SubNode_Title = "子节点";
	public static final String LabelNode_Title = "标签";
	public static final String IFNode_Title = "IF节点";
	public static final String ExceptionNode_Title = "异常场景节点";
	public static final String XORNode_Title = "XOR节点";
	public static final String ORNode_Title = "OR节点";
	public static final String ANDNode_Title = "AND节点";
	public static final String ConditionNode_Title = "条件节点";
	public static final String AdapterNode_Title = "适配器节点";

	BufferedImage icon;
	
	public String toString(){
		return title + "    [" + name + "]";
	}
	
	public FlowNode(DrawCanvas canvas) {
		super(canvas);
	}

	protected Polygon getShape(){
		return null;
	}

	protected String getButtonImageFileName() {
		return "workflow.png";
	}
	
	protected void drawShape(Graphics g) {
		Polygon shape = getShape();
		g.setColor(Color.DARK_GRAY);
		g.fillPolygon(shape);
		g.setColor(Color.LIGHT_GRAY);
		g.drawPolygon(shape);
	}

	protected void drawText(Graphics g) {
		g.setColor(Color.DARK_GRAY);
		drawText(g, font, getRect(), new StringBuilder(title == null ? "" : title));
	}

	protected void drawNode(Graphics g){
		drawShape(g);
		drawText(g);
	}
	
	public BufferedImage getImage() {
		if (icon == null){
			try {
				icon = ImageUtils.loadImage(new File(Defines.Java_Dir_Icon_Resource.getAbsolutePath(), getButtonImageFileName()));
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}					
		}
		return icon;
	}

	public static class BeginNode extends FlowNode{

		public BeginNode(DrawCanvas canvas) {
			super(canvas);
		}
		
		protected void drawNode(Graphics g){
			g.setColor(Color.DARK_GRAY);		
			Rectangle rect = getRect();
			rect.width = 100;
			rect.height = 40;
			g.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 20, 20);
			drawText(g, font, rect, new StringBuilder(title == null ? "" : title));
		}

	}
	
	public static class ChildFlowNode extends FlowNode{
		public String relationName;
		public ChildFlowNode(DrawCanvas canvas) {
			super(canvas);
		}
		
		protected void drawNode(Graphics g){
			g.setColor(Color.BLUE);		
			Rectangle rect = getRect();
			g.fill3DRect(rect.x, rect.y, rect.width, rect.height, false);
			drawText(g, font, rect, new StringBuilder(title == null ? "" : title));
		}

		public JSONObject toJson() throws JSONException{
			JSONObject jsonObject = super.toJson();
			
			jsonObject.put("relationName", relationName);
			return jsonObject;
		}

		public void fromJson(JSONObject data, ICreateNodeSerializable createUserDataSerializable) throws JSONException{
			super.fromJson(data, createUserDataSerializable);
			if (data.has("relationName"))
				relationName = data.getString("relationName");
		}
		
	}
	
	public static class EndNode extends FlowNode{

		public EndNode(DrawCanvas canvas) {
			super(canvas);
		}
		
		protected void drawNode(Graphics g){
			g.setColor(Color.DARK_GRAY);		
			Rectangle rect = getRect();
			rect.width = 100;
			rect.height = 40;
			g.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 20, 20);
			drawText(g, font, rect, new StringBuilder(title == null ? "" : title));
		}

	}
	
	public static class LabelNode extends FlowNode{

		public LabelNode(DrawCanvas canvas) {
			super(canvas);
		}
		
		protected void drawShape(Graphics g) {
			return;
		}
		
		protected void drawText(Graphics g) {
			g.setColor(Color.DARK_GRAY);
			drawText(g, font, Color.BLACK, getRect(), new StringBuilder(title == null ? "" : title));
		}

	}
	
	public static class ProcessNode extends FlowNode{
		public ProcessNode(DrawCanvas canvas) {
			super(canvas);
		}

		protected Polygon getShape() {
			int[] x = new int[5];
			int[] y = new int[5];
			
			Rectangle rect = getRect();
			x[0] = rect.x;
			x[1] = rect.x + rect.width;
			x[2] = rect.x + rect.width;
			x[3] = rect.x;
			x[4] = rect.x;
			
			y[0] = rect.y;
			y[1] = rect.y;
			y[2] = rect.y + rect.height;
			y[3] = rect.y + rect.height;
			y[4] = rect.y;
			
			return new Polygon(x, y, 5);
		}
		
	}

	public static class StateNode extends FlowNode{
		public StateNode(DrawCanvas canvas) {
			super(canvas);
		}

		protected boolean isInit = false;
		protected Polygon getShape() {
			Rectangle rect = getRect();
			if (!isInit){
				isInit = true;
				rect.height = 40;
			}
			int[] x = new int[7];
			int[] y = new int[7];
			
			int xd = 20;
			x[0] = rect.x;
			x[1] = rect.x + xd;
			x[2] = rect.x + rect.width - xd;
			x[3] = rect.x + rect.width;
			x[4] = x[2];
			x[5] = x[1];
			x[6] = x[0];
			
			y[0] = rect.y + (rect.height / 2);
			y[1] = rect.y;
			y[2] = y[1];
			y[3] = y[0];
			y[4] = rect.y + rect.height;
			y[5] = y[4];
			y[6] = y[0];
			
			return new Polygon(x, y, 7);
		}
		
		public JSONObject toJson() throws JSONException{
			JSONObject json = super.toJson();
			json.put("state", state == null ? "" : state);
			json.put("memo", memo == null ? "" : memo);

			return json;
		}

		public void fromJson(JSONObject data, ICreateNodeSerializable createUserDataSerializable) throws JSONException{
			super.fromJson(data, createUserDataSerializable);
			if (data.has("state"))
				state = data.getString("state");
			else
				state = null;
			
			if (data.has("memo"))
				memo = data.getString("memo");
			else
				memo = null;
			
		}

		public String state;
		public String memo;

	}

	public static class ActionNode extends ProcessNode{
		public ActionNode(DrawCanvas canvas) {
			super(canvas);
		}

		protected Polygon getShape() {			
			return super.getShape();
		}

		public JSONObject toJson() throws JSONException{
			JSONObject json = super.toJson();
			json.put("initData", initData == null ? new JSONObject() : initData);
			json.put("memo", memo == null ? "" : memo);
			json.put("type", this.getClass().getSimpleName());
			json.put("model_id", model_id == null ? new JSONObject() : model_id.toJson());
			return json;
		}

		public void fromJson(JSONObject json, ICreateNodeSerializable createUserDataSerializable) throws JSONException{
			super.fromJson(json, createUserDataSerializable);
			if (json.has("initData"))
				initData = json.getJSONObject("initData");
			else
				initData = null;
			
			if (json.has("memo"))
				memo = json.getString("memo");
			else 
				memo = null;
			
			if (json.has("model_id"))
				model_id = new KeyValue<>(json.getJSONObject("model_id"));
			else 
				model_id = null;

		}

		public JSONObject initData;
		public String memo;
		public KeyValue<String, String> model_id;
	}

	public static class IFNode extends FlowNode{
		public IFNode(DrawCanvas canvas) {
			super(canvas);
		}

		protected Polygon getShape() {
			int[] x = new int[5];
			int[] y = new int[5];
			
			Rectangle rect = getRect();

			x[0] = rect.x + (rect.width / 2);
			x[1] = rect.x + rect.width;
			x[2] = rect.x + (rect.width / 2);
			x[3] = rect.x;
			x[4] = x[0];
			
			y[0] = rect.y;
			y[1] = rect.y + (rect.height / 2);
			y[2] = rect.y + rect.height;
			y[3] = rect.y + (rect.height / 2);
			y[4] = y[0];
			
			return new Polygon(x, y, 5);
		}
		
	}

	public static class SwitchNode extends FlowNode{
		public SwitchNode(DrawCanvas canvas) {
			super(canvas);
		}

		protected void initDrawData() {
			title = "";
		}

		protected Color getColor() {
			return Color.RED;
		}

		protected void drawShape(Graphics g) {
			initDrawData();
			g.setColor(getColor());
			Rectangle rect = getRect();
			g.fillOval(rect.x, rect.y, rect.width, rect.height);
			g.setColor(Color.LIGHT_GRAY);
			g.drawOval(rect.x, rect.y, rect.width, rect.height);
			drawText(g);
		}
	}

	public static class ConditionNode extends FlowNode{
		public ConditionNode(DrawCanvas canvas) {
			super(canvas);
			lineType = LineType.ltDot;
		}

		protected Polygon getShape() {
			int[] x = new int[5];
			int[] y = new int[5];
			
			int space = 20;
			
			Rectangle rect = getRect();

			x[0] = rect.x + space;
			x[1] = rect.x + rect.width;
			x[2] = rect.x + rect.width - space;
			x[3] = rect.x;
			x[4] = x[0];
			
			y[0] = rect.y;
			y[1] = rect.y;
			y[2] = rect.y + rect.height;
			y[3] = rect.y + rect.height;
			y[4] = y[0];
			
			return new Polygon(x, y, 5);
		}
		
		protected void drawText(Graphics g) {
			g.setColor(Color.DARK_GRAY);
			drawMulitText(g, font, Color.WHITE, getRect(), title == null ? "" : title);
		}
	}

	public static class ORNode extends SwitchNode{
		public ORNode(DrawCanvas canvas) {
			super(canvas);
		}

		protected void initDrawData() {
			super.initDrawData();
			title = "OR";
		}
		
		protected Color getColor() {
			return Color.DARK_GRAY;
		}
	}

	public static class XORNode extends ORNode{
		public XORNode(DrawCanvas canvas) {
			super(canvas);
		}

		protected void initDrawData() {
			super.initDrawData();
			title = "XOR";
		}
	}
	
	public static class ANDNode extends ORNode{
		public ANDNode(DrawCanvas canvas) {
			super(canvas);
		}

		protected void initDrawData() {
			super.initDrawData();
			title = "AND";
		}
	}

}