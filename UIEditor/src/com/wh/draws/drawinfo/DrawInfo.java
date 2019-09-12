package com.wh.draws.drawinfo;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.swing.SwingUtilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wh.control.datasource.define.DataSource.FieldNameInfo;
import com.wh.draws.DrawCanvas.ChangeType;
import com.wh.draws.DrawNode;
import com.wh.draws.UINode;
import com.wh.system.tools.ColorConvert;
import com.wh.system.tools.JsonHelp;

public abstract class DrawInfo{
	
	public enum ScaleMode{
		smStretch, smHeight, smWidth, smCenterInRectangle
	}
	
	public boolean needFrame = true;
	public boolean needBackground = true;
	
	public JSONObject jsonData = new JSONObject();
	
	public enum Align{
		alTop, alBottom, alLeft, alRight, alCenter
	}
	
	public enum PlaceAlign{
		alTop, alBottom, alLeft, alRight
	}
	
	public String placeAlign;
	
	public enum PlaceInType{
		ptReplace, ptAlign, ptNone
	}
	
	public int alignSpace = 0;
	public boolean keepAlignSpace = false;
	
	public PlaceInType placeInType = PlaceInType.ptNone;
	
	public int placeIndex = 0;
	
	public String placeGroup;
	
	public Align xAlign = Align.alLeft;
	public Align yAlign = Align.alTop;
	
	public String scrollDiv;
	public String scrollControl;
	public boolean isHref = false;
	public String parent;
	public String style;
	public String bodystyle;
	public UINode node;
	public String left = "0px";
	public String top = "0px";
	public String width = "120px";
	public String height = "24px";
	
	public FieldNameInfo field;
	public String id = UUID.randomUUID().toString();
	public String name = id;
	public Object value = "";
	public boolean required = true;
	public boolean allowEdit = true;
	public String onValueChanged;
	public String onClick;
	public Object extendData;
	public String title;
	public Color textColor = Color.BLACK;
	public Color backgroundColor;
	
	public Font font = new Font("微软雅黑", Font.BOLD, 12);
	
	public int margin_top = 0;
	public int margin_left = 0;
	public int margin_right = 0;
	public int margin_bottom = 0;
	public int tabindex = -1;
	
	public String controlData = "";
	public String styleClass;

	public String divClass;
	public String emptyText;
	
	public String dataSource;
	
	public boolean enabled = true;
	
	public boolean useHrefColor = false;
	
	public int row = 0;
		
	public String decideValue;
	
	public boolean border = true;
	
	public boolean autoLoginFire = false;

	public JSONArray dataSourceParams;
	
	protected Image image;
	
    public boolean autoInitDataSource = false;

    public String outputId;
    public String inputId;
    
	protected Image getImage() {
		return null;
	}

	protected static final int SOURCE_INDEX = 0;
	protected static final int DEST_INDEX = 1;
	protected static final int LEFT_INDEX = 0;
	protected static final int TOP_INDEX = 1;
	protected static final int RIGHT_INDEX = 2;
	protected static final int BOTTOM_INDEX = 3;
	
	public void onResize() {
		
	}
	
	public JSONObject toJson() throws JSONException{
		return toJson(false);
	}
	
	public JSONObject toJson(boolean needAll) throws JSONException{
		JSONObject json = new JSONObject();
		
		json.put("outputId", needAll && outputId == null ? "" : outputId);
		json.put("inputId", needAll && inputId == null ? "" : inputId);
		json.put("autoInitDataSource", autoInitDataSource);
		json.put("dataSourceParams", needAll && dataSourceParams == null ? "[]" : dataSourceParams);
		
		json.put("autoLoginFire", autoLoginFire);
		
		json.put("decideValue", needAll && decideValue == null ? "" : decideValue);
		
		json.put("dataSource", needAll && dataSource == null ? "" : dataSource);
		json.put("row", row);
		
		json.put("scrollDiv", needAll && scrollDiv == null ? "" : scrollDiv);
		json.put("scrollControl", needAll && scrollControl == null ? "" : scrollControl);
		json.put("enabled", enabled);
		
		json.put("divClass", needAll && divClass == null ? "" : divClass);
		json.put("emptyText", needAll && emptyText == null ? "" : emptyText);;

		json.put("styleClass", needAll && styleClass == null ? "" : styleClass);
		json.put("controlData", controlData);
		json.put("tabindex", tabindex);
		json.put("isHref", isHref);
		json.put("style", needAll && style == null ? "" : style);
		json.put("bodystyle", needAll && bodystyle == null ? "" : bodystyle);
		json.put("type", needAll && typeName() == null ? "" : typeName());
		json.put("parent", needAll && parent == null ? "" : parent);
		
		if (needAll || (left != null && !left.isEmpty()))
			json.put("left", needAll && left == null ? "" : left);
		
		if (needAll || (top != null && !top.isEmpty()))
			json.put("top", needAll && top == null ? "" : top);
		
		json.put("xAlign", xAlign.name());
		json.put("yAlign", yAlign.name());
		
		json.put("width", needAll && width == null ? "" : width);
		json.put("height", needAll && height == null ? "" : height);
		json.put("field", needAll && field == null ? "{}" : (field == null ? null : field.toJson()));
		json.put("name", needAll && name == null ? "" : name);
		json.put("id", needAll && id == null ? "" : id);
		json.put("value", needAll && value == null ? "" : value);
		json.put("required", required);
		json.put("allowEdit", allowEdit);
		json.put("onValueChanged", needAll && onValueChanged == null ? "" : onValueChanged);
		json.put("onClick", needAll && onClick == null ? "" : onClick);
		json.put("extendData", needAll && extendData == null ? "" : extendData);
		json.put("title", needAll && title == null ? "" : title);
		json.put("textColor", needAll && textColor == null ? "" : ColorConvert.toHexFromColor(textColor));
		
		if (backgroundColor != null)
			json.put("backgroundColor", needAll && backgroundColor == null ? "" : ColorConvert.toHexFromColor(backgroundColor));
		json.put("fontName", needAll && font == null ? "" : font.getName());
		json.put("fontSize", needAll && font == null ? "" : font.getSize());
		json.put("fontStyle", needAll && font == null ? "" : font.getStyle());

		json.put("keepAlignSpace", keepAlignSpace);
		json.put("alignSpace", alignSpace);
		json.put("placeInType", needAll && placeInType == null ? "" : placeInType.name());
		json.put("placeAlign", needAll && placeAlign == null ? "" : placeAlign);
		json.put("placeIndex", String.valueOf(placeIndex));
		json.put("placeGroup", needAll && placeGroup == null ? "" : placeGroup);

		json.put("border", border);
		
		json.put("useHrefColor", useHrefColor);
		
		return json;
	}
	
	public void fromJson(JSONObject json) throws JSONException{
		jsonData = new JSONObject(json.toString());

		if (json.has("outputId"))
			outputId = JsonHelp.getString(json, "outputId");
		else
			outputId = null;

		if (json.has("inputId"))
			inputId = JsonHelp.getString(json, "inputId");
		else
			inputId = null;

		if (json.has("autoInitDataSource"))
			autoInitDataSource = json.getBoolean("autoInitDataSource");
		else
			autoInitDataSource = false;

		if (json.has("dataSourceParams"))
			dataSourceParams = json.getJSONArray("dataSourceParams");
		else
			dataSourceParams = null;

		if (json.has("autoLogin"))
			autoLoginFire = json.getBoolean("autoLoginFire");
		else
			autoLoginFire = false;

		if (json.has("useHrefColor"))
			useHrefColor = json.getBoolean("useHrefColor");
		else
			useHrefColor = false;

		if (json.has("border"))
			border = json.getBoolean("border");
		else
			border = true;

		if (json.has("decideValue"))
			decideValue = JsonHelp.getString(json, "decideValue");
		else
			decideValue = null;

		if (json.has("row"))
			row = json.getInt("row");
		else
			row = 0;

		if (json.has("keepAlignSpace"))
			keepAlignSpace = json.getBoolean("keepAlignSpace");
		else
			keepAlignSpace = false;

		if (json.has("alignSpace"))
			alignSpace = json.getInt("alignSpace");
		else
			alignSpace = 0;
		
		if (json.has("dataSource"))
			dataSource = json.getString("dataSource");
		else
			dataSource = null;
		
		if (json.has("scrollDiv"))
			scrollDiv = json.getString("scrollDiv");
		else
			scrollDiv = null;
		
		if (json.has("scrollControl"))
			scrollControl = json.getString("scrollControl");
		else
			scrollControl = null;
		
		if (json.has("placeInType"))
			placeInType = PlaceInType.valueOf(json.getString("placeInType"));
		else
			placeInType = PlaceInType.ptNone;
		
		if (json.has("placeIndex"))
			placeIndex = json.getInt("placeIndex");
		else
			placeIndex = 0;
		
		if (json.has("placeGroup")){
			placeGroup = json.getString("placeGroup");
		}else
			placeGroup = null;

		if (json.has("placeAlign")){
			placeAlign = json.getString("placeAlign");
		}else
			placeAlign = null;

		if (json.has("enabled"))
			enabled = json.getBoolean("enabled");
		else
			enabled = true;
		
		if (json.has("divClass"))
			divClass = json.getString("divClass");
		else
			divClass = null;
		
		if (json.has("emptyText"))
			emptyText = json.getString("emptyText");
		else
			emptyText = null;

		if (json.has("styleClass"))
			styleClass = json.getString("styleClass");
		else
			styleClass = null;
		
		if (json.has("controlData"))
			controlData = json.getString("controlData");
		else
			controlData = null;
		
		if (json.has("tabindex"))
			tabindex = json.getInt("tabindex");
		else
			tabindex = 0;
		
		if (json.has("isHref"))
			isHref = json.getBoolean("isHref");
		else
			isHref = false;
		
		if (json.has("parent"))
			parent = json.getString("parent");
		else
			parent = null;
		if (json.has("style"))
			style = json.getString("style");
		else
			style = null;
		
		if (json.has("bodystyle"))
			bodystyle = json.getString("bodystyle");
		else
			bodystyle = null;
		
		if (json.has("left"))
			left = json.getString("left");
		else
			left = "0";
		
		if (json.has("top"))
			top = json.getString("top");
		else
			top = "0";
		
		if (json.has("xAlign"))
			xAlign = Align.valueOf(json.getString("xAlign"));
		else
			xAlign = Align.alLeft;
		
		if (json.has("yAlign"))
			yAlign = Align.valueOf(json.getString("yAlign"));
		else
			yAlign = Align.alTop;
		
		width = json.getString("width");
		height = json.getString("height");
		
		if (json.has("field") && json.get("field") instanceof JSONObject){
			try {
				field = new FieldNameInfo(json.getJSONObject("field"));
			} catch (Exception e) {
				e.printStackTrace();
				field = null;
			}
		}else
			field = null;
		
		id = json.getString("id");
		name = json.getString("name");
		
		if (json.has("value"))
			value = json.get("value");
		else
			value = null;
		
		required = json.getBoolean("required");
		allowEdit = json.getBoolean("allowEdit");
		
		if (json.has("onValueChanged"))
			onValueChanged = json.getString("onValueChanged");
		else
			onValueChanged = null;
		
		if (json.has("onClick"))
			onClick = json.getString("onClick");
		else
			onClick = null;
		
		if (json.has("extendData"))
			extendData = json.getString("extendData");
		else
			extendData = null;
		
		if (json.has("title"))
			title = json.getString("title");
		else
			title = null;
		
		if (json.has("backgroundColor"))
			backgroundColor = ColorConvert.toColorFromString(json.getString("backgroundColor"));
		else
			backgroundColor = null;
		
		if (json.has("textColor"))
			textColor = ColorConvert.toColorFromString(json.getString("textColor"));
		else
			textColor = Color.BLACK;
		
		String fontName = font.getFontName();
		if (json.has("fontName"))
			 fontName = json.getString("fontName");
		else
			fontName = "微软雅黑";
		
		int fontSize = font.getSize();
		if (json.has("fontSize"))
			fontSize = json.getInt("fontSize");
		else
			fontSize = 12;
		
		int fontStyle = font.getSize();
		if (json.has("fontStyle"))
			fontStyle = json.getInt("fontStyle");
		else
			fontStyle = 0;

		font = new Font(fontName, fontStyle, fontSize);

		if (json.has("right")){
			xAlign = Align.alRight;
			left = String.valueOf(node.getCanvas().getPageWidth() - getWidth() - Integer.parseInt(json.getString("right")));
			node.getCanvas().postFireChange(ChangeType.ctBackEdited);
		}
	}
	
	public Font getFont(){
		if (font == null)
			return node.font;
		else
			return font;
	}
	
	public enum SizeType{
		stLeft, stTop, stRight, stBottom, stWidth, stHeight
	}
	
	
	public String fixValue(int offset, SizeType st) {
		String value = null;
		switch (st) {
		case stHeight:
			value = width;
			break;
		case stWidth:
			value = height;
			break;
		case stLeft:
			value = left;
			break;
		case stTop:
			value = top;
			break;
		default:
			return null;
		}
		
		if (value == null || value.isEmpty())
			value = String.valueOf(offset);
		
		if (value.indexOf('%') != -1){
			return value;
		}else if (value.indexOf("px") != -1){
			int size = Integer.parseInt(value.replace("px", ""));
			value = String.valueOf(size + offset) + "px";
		}else 
			value = String.valueOf(Integer.parseInt(value) + offset);
		
		switch (st) {
		case stHeight:
			width = value;
			break;
		case stWidth:
			height = value;
			break;
		case stLeft:
			left = value;
			break;
		case stTop:
			top = value;
			break;
		default:
			break;
		}
		
		return value;
	}
	
	public float getSize(String sizeString, SizeType st){
		try{
			float size = 0;
			if (sizeString == null || sizeString.isEmpty()){
				return size;
			}
			
			if (sizeString.indexOf('%') != -1){
				float parentSize = 1;
				switch (st) {
				case stLeft:
				case stRight:
					if (parent == null || parent.isEmpty())
						parentSize = node.getCanvas().getPageSize().width;
					else {
						UINode parentNode = (UINode)node.getCanvas().getNode(parent);
						if (parentNode == null)
							parentSize = node.getCanvas().getPageSize().width;
						else
							parentSize = parentNode.info.getWidth();
					}
					break;
				case stTop:
				case stBottom:
					UINode parentNode = (UINode)node.getCanvas().getNode(parent);
					if (parent == null || parent.isEmpty())
						parentSize = node.getCanvas().getPageSize().height;
					else {
						parentSize = parentNode.info.getHeight();
					}
					break;
				default:
					break;
				}
				float tmp = Float.parseFloat(sizeString.replace("%", ""));
				size = tmp / 100 * parentSize;
			}else if (sizeString.indexOf("px") != -1){
				size = Float.parseFloat(sizeString.replace("px", ""));
			}else {
				size = Float.parseFloat(sizeString);
			}
			
			return size;
		}catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public float getLeft(){
		return node.getCanvas().getPageLocation(new Point(0, 0)).x + getSize(left, SizeType.stLeft);
	}
	
	public float getTop(){
		return node.getCanvas().getPageLocation(new Point(0, 0)).y + getSize(top, SizeType.stTop);			
	}
	
	public float getWidth(){
		return getSize(width, SizeType.stLeft);
	}
	
	public float getHeight(){
		return getSize(height, SizeType.stTop);			
	}
	
	public Rectangle getRect(){
		if (node.getRefRect() == null){
			float left = getLeft();
			float top = getTop();
			switch (this.xAlign) {
			case alCenter:
				left = (node.getCanvas().getPageWidth() - getWidth()) / 2;
				this.left = String.valueOf((int)left);
				break;
			default:
				break;
			}
			
			switch (this.yAlign) {
			case alCenter:
				top = (node.getCanvas().getPageHeight() - getHeight()) / 2;
				this.top = String.valueOf((int)top);
				break;
			default:
				break;
			}
			
			node.setRefRect(new Rectangle((int)left, (int)top, (int)getWidth(), (int)getHeight()));
			
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					List<DrawNode> nodes = new ArrayList<>();
					nodes.add(node);
					node.getCanvas().refreshDrawTree(nodes, false);
				}
			});
		}
		return node.getRefRect();
	}
	
	protected ScaleMode getImageScale(){
		return ScaleMode.smCenterInRectangle;
	}
	
	protected int[][] getSize(Image image){
		Rectangle rectangle = getRect();
		return getSize(image, rectangle, getImageScale());
	}
	
	protected void drawImage(Graphics g){
		Image image = getImage();
		int[][] size = getSize(image);
		drawImage(size, g, image);
	}

	protected static final int DrawType_Text = 1;
	protected static final int DrawType_Image = 2;
	
	protected int drawTypes = DrawType_Text | DrawType_Image;
	
	public void drawButton(Graphics g, Font font, Color textColor, Rectangle rect, Image image, String caption){
		if (rect.height < 22){
			rect.height = 22;
			node.fixSize(false);
		}
		
		caption = (caption == null || caption.isEmpty() ? "上传" : caption);
		g.setColor(Color.darkGray);
		rect = new Rectangle(rect);
		g.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 20, 20);
		Rectangle imageRect = new Rectangle();
		if (image != null){
			imageRect = new Rectangle(rect.x + 10, rect.y + (rect.height - 16) / 2, 16, 16);
			g.drawImage(image, imageRect.x, imageRect.y, imageRect.width, imageRect.height, null);
			rect.x = imageRect.x + imageRect.width + 5;
			rect.width -= imageRect.width + 5 + 10;
		}
		DrawNode.drawLineText(g, font, textColor, rect.x, 
				rect.y, rect.width, rect.height, caption);
	}

	public void drawNode(Graphics g, Rectangle rect){
		if ((drawTypes & DrawType_Image) == DrawType_Image){
			getImage();
			if (image != null){
				drawImage(g);
			}
		}
		if ((drawTypes & DrawType_Text) == DrawType_Text){
			if (value != null)
				DrawNode.drawLineText(g, getFont(), textColor, rect.x, rect.y, rect.width, rect.height, value.toString());
		}
	}

	public DrawInfo(UINode node){
		if (node == null)
			return;
		
		this.node = node;
		this.title = node.title;
		this.value = node.title;
		this.id = node.id;
		this.name = node.name;
		
		node.info = this;
		node.invalidRect();
	}
	
	protected boolean onEditorChange(Object oldValue, Object newValue) {
		return true;
	}
	
	public void setValue(Object value){
		if (!onEditorChange(this.value, value))
			return;
		
		image = null;
		this.value = value;
		node.getCanvas().repaint();
	}
	
	public abstract String typeName();
	
	public static int[][] getSize(Image image, Rectangle rectangle, ScaleMode scaleMode){
		if (image == null)
			return null;
		
		int[][] result = new int[2][4];
		
		int imageWidth = image.getWidth(null);
		int imageHeight = image.getHeight(null);
		
		result[DEST_INDEX][LEFT_INDEX] = rectangle.x;
		result[DEST_INDEX][TOP_INDEX] = rectangle.y;
		result[DEST_INDEX][RIGHT_INDEX] = rectangle.x + rectangle.width;
		result[DEST_INDEX][BOTTOM_INDEX] = rectangle.y + rectangle.height;
		switch (scaleMode) {
		case smCenterInRectangle:
			result[SOURCE_INDEX][LEFT_INDEX] = 0;
			result[SOURCE_INDEX][TOP_INDEX] = 0;
			result[SOURCE_INDEX][RIGHT_INDEX] = imageWidth;
			result[SOURCE_INDEX][BOTTOM_INDEX] = imageHeight;

			float scale = (float)imageWidth / imageHeight;
			
			int width = rectangle.width;
			int height = rectangle.height;
			if (rectangle.width > rectangle.height){
				width = (int)(scale * height);
				while(width > rectangle.width){
					height -= 5;
					width = (int)(scale * height);
				}
			}else{
				height = (int)(width / scale);
				while(height > rectangle.height){
					width -= 5;
					height = (int)(width / scale);
				}
			}
			
			result[DEST_INDEX][LEFT_INDEX] += (rectangle.width - width) / 2;
			result[DEST_INDEX][TOP_INDEX] += (rectangle.height - height) / 2;
			result[DEST_INDEX][RIGHT_INDEX] = result[DEST_INDEX][LEFT_INDEX] + width;
			result[DEST_INDEX][BOTTOM_INDEX] = result[DEST_INDEX][TOP_INDEX] + height;
			break;
		case smStretch:
			result[SOURCE_INDEX][LEFT_INDEX] = 0;
			result[SOURCE_INDEX][TOP_INDEX] = 0;
			result[SOURCE_INDEX][RIGHT_INDEX] = imageWidth;
			result[SOURCE_INDEX][BOTTOM_INDEX] = imageHeight;
			break;
		case smHeight:{
			result[SOURCE_INDEX][LEFT_INDEX] = 0;
			result[SOURCE_INDEX][TOP_INDEX] = 0;
			result[SOURCE_INDEX][RIGHT_INDEX] = imageWidth;
			result[SOURCE_INDEX][BOTTOM_INDEX] = imageHeight;
			
			float inc = ((float)imageWidth) / imageHeight;
			
			result[DEST_INDEX][LEFT_INDEX] = rectangle.x;
			result[DEST_INDEX][TOP_INDEX] = rectangle.y;
			result[DEST_INDEX][RIGHT_INDEX] = result[DEST_INDEX][LEFT_INDEX] + (int) (rectangle.getHeight() * inc);
			result[DEST_INDEX][BOTTOM_INDEX] = result[DEST_INDEX][TOP_INDEX] + (int) rectangle.getHeight();
			break;
		}case smWidth:{
			result[SOURCE_INDEX][LEFT_INDEX] = 0;
			result[SOURCE_INDEX][TOP_INDEX] = 0;
			result[SOURCE_INDEX][RIGHT_INDEX] = imageWidth;
			result[SOURCE_INDEX][BOTTOM_INDEX] = imageHeight;

			float inc = ((float)imageHeight) / imageWidth;
			
			result[DEST_INDEX][LEFT_INDEX] = rectangle.x;
			result[DEST_INDEX][TOP_INDEX] = rectangle.y;
			result[DEST_INDEX][RIGHT_INDEX] = result[DEST_INDEX][LEFT_INDEX] + (int) rectangle.getWidth();
			result[DEST_INDEX][BOTTOM_INDEX] = result[DEST_INDEX][TOP_INDEX] + (int) (rectangle.getWidth() * inc);
			break;
		}
		}
		return result;
	}
	
	public static void drawImage(int[][] size, Graphics g, Image image){
		if (size == null)
			return;
		
		g.drawImage(image, size[DEST_INDEX][LEFT_INDEX], size[DEST_INDEX][TOP_INDEX], size[DEST_INDEX][RIGHT_INDEX], size[DEST_INDEX][BOTTOM_INDEX], 
				size[SOURCE_INDEX][LEFT_INDEX], size[SOURCE_INDEX][TOP_INDEX], size[SOURCE_INDEX][RIGHT_INDEX], size[SOURCE_INDEX][BOTTOM_INDEX], 
				null);				
	}

}

