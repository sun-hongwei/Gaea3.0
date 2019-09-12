package com.wh.draws.drawinfo;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wh.control.EditorEnvironment;
import com.wh.draws.DrawNode;
import com.wh.draws.UINode;
import com.wh.system.tools.ColorConvert;
import com.wh.system.tools.ImageUtils;
import com.wh.system.tools.JsonHelp;

public class ListViewInfo extends DrawInfo {

	public String typeName(){
		return DrawInfoDefines.ListView_Name;
	}
	
	class Column{
		public Color textcolor;
		public Rectangle r;
		public Font font;
		public String title;
		public String type = "text";
		public boolean needShow = true;
	}
	
	HashMap<String, Column> columns = new HashMap<>();
	
	public Color lineColor = ColorConvert.toColorFromString("0xFFd3d3d3");
	
	public Color backgroundColor = Color.WHITE;
	
	public String header;
	public String data;
	
	public boolean border = true;
	public boolean showHeader = true;
	public boolean showLine = false;
	public int headerHeight = 40;
	public int lineHeight = 40;
	public boolean showScrollbar = false;
	
	public int visiableColumnCount = 8;
	public boolean autoColumnWidth = true;
	
	public int rowDiv = 3;
	public int colDiv = 3;
	
	public boolean showVerticalScrollBar = true;
	public boolean showHorizontalScrollBar = false;
	
	public JSONObject template;
	
	public JSONObject toJson(boolean needAll) throws JSONException{
		JSONObject json = super.toJson(needAll);
		
		json.put("showVerticalScrollBar", showVerticalScrollBar);
		json.put("showHorizontalScrollBar", showHorizontalScrollBar);
		
		json.put("lineColor", needAll && lineColor == null ? "0xFFd3d3d3" : ColorConvert.toHexFromColor(lineColor));
		json.put("backgroundColor", needAll && backgroundColor == null ? "0xFFFFFFFF" : ColorConvert.toHexFromColor(backgroundColor));
		json.put("header", needAll && header == null ? "" : header);
		json.put("data", needAll && data == null ? "" : data);
		json.put("border", border);
		json.put("showLine", showLine);
		json.put("showHeader", showHeader);
		json.put("headerHeight", headerHeight);		
		json.put("lineHeight", lineHeight);		
		json.put("showScrollbar", showScrollbar);
		json.put("visiableColumnCount", visiableColumnCount);
		json.put("autoColumnWidth", autoColumnWidth);
		json.put("rowDiv", rowDiv);
		json.put("colDiv", colDiv);		
		json.put("template", needAll && template == null ? new JSONArray() : template);
		
		return json;
	}
	
	public void fromJson(JSONObject json) throws JSONException{
		super.fromJson(json);

		if (json.has("template"))
			try {
				template = json.getJSONObject("template");
			} catch (Exception e) {
				// TODO: handle exception
			}
		else
			template = null;

		if (json.has("showHorizontalScrollBar"))
			showHorizontalScrollBar = json.getBoolean("showHorizontalScrollBar");
		else
			showHorizontalScrollBar = false;
		
		if (json.has("showVerticalScrollBar"))
			showVerticalScrollBar = json.getBoolean("showVerticalScrollBar");
		else
			showVerticalScrollBar = true;
		
		if (json.has("rowDiv"))
			rowDiv = json.getInt("rowDiv");
		else
			rowDiv = 0;
		
		if (json.has("colDiv"))
			colDiv = json.getInt("colDiv");
		else
			colDiv = 0;
		
		if (json.has("visiableColumnCount"))
			visiableColumnCount = json.getInt("visiableColumnCount");
		else
			visiableColumnCount = 8;
		
		if (json.has("autoColumnWidth"))
			autoColumnWidth = json.getBoolean("autoColumnWidth");
		else
			autoColumnWidth = true;
		
		if (json.has("lineColor"))
			lineColor = ColorConvert.toColorFromString(json.getString("lineColor"));
		else {
			lineColor = ColorConvert.toColorFromString("0xFFd3d3d3");
		}
		if (json.has("backgroundColor"))
			backgroundColor = ColorConvert.toColorFromString(json.getString("backgroundColor"));
		else
			backgroundColor = Color.WHITE;
		
		if (json.has("header"))
			header = json.getString("header");
		else
			header = null;
		
		if (json.has("data"))
			data = json.getString("data");
		else
			data = null;
		
		if (json.has("border"))
			border = json.getBoolean("border");
		else
			border = true;
		
		if (json.has("showLine"))
			showLine = json.getBoolean("showLine");
		else
			showLine = true;
		
		if (json.has("showHeader"))
			showHeader = json.getBoolean("showHeader");
		else
			showHeader = true;
		
		if (json.has("headerHeight"))
			headerHeight = json.getInt("headerHeight");
		else
			headerHeight = 40;
		
		if (json.has("lineHeight"))
			lineHeight = json.getInt("lineHeight");
		else
			lineHeight = 40;
		
		if (json.has("showScrollbar"))
			showScrollbar = json.getBoolean("showScrollbar");
		else
			showScrollbar = true;
		
	}
	
	public void onResize() {
	}

	HashMap<String, ReportInfo> reportInfos = new HashMap<>();
	
	protected ReportInfo getReportInfo(String name) {
		if (reportInfos.containsKey(name)){
			ReportInfo reportInfo = reportInfos.get(name);
			return reportInfo;
		}
		
		UINode reportNode = new UINode(node.getCanvas());
		ReportInfo reportInfo = new ReportInfo(reportNode);
		File file = EditorEnvironment.getReportFile(name);
		if (file.exists()){
			JSONObject data;
			try {
				data = (JSONObject) JsonHelp.parseJson(file, null);
				reportInfo.fromJson(data);
				reportInfos.put(name, reportInfo);
				return reportInfo;
			} catch (Exception e) {
				e.printStackTrace();
				EditorEnvironment.showException(e);
				return null;
			}
		}else
			return null;
	}
	
	protected void initColumns(Rectangle rect) throws JSONException {
		columns.clear();
		if (header == null || header.isEmpty())
			return;
		
		JSONArray json = (JSONArray)JsonHelp.parseJson(header);
		
		int left = rect.x;
		int top = rect.y;
		
		int divWidth = 100;
		int maxCount = Math.min(visiableColumnCount, json.length());
		if (autoColumnWidth){
			divWidth = (rect.width - (rowDiv * (maxCount - 1))) / maxCount;
		}
		
		for (int i = 0; i < json.length(); i++) {
			Column columnItem = new Column();
			
			JSONObject column = json.getJSONObject(i);
			int width = divWidth;
			if ((!autoColumnWidth || i >= maxCount) && column.has("width"))
				width = column.getInt("width");
			columnItem.r = new Rectangle(left, top, width, headerHeight);
			columnItem.needShow = !(columnItem.r.x + columnItem.r.width > rect.x + rect.width || columnItem.r.y + columnItem.r.height > rect.y + rect.height);
			
			left += width + rowDiv;

			String fontname = font.getFontName();
			int fontsize = font.getSize();
			int fontstyle = font.getStyle();
			
			if (column.has("fontname")){
				fontname = column.getString("fontname");
			}
			if (column.has("fontsize")){
				fontsize = column.getInt("fontsize");
			}
			if (column.has("fontstyle")){
				fontstyle = column.getInt("fontstyle");
			}
			
			if (column.has("type"))
				columnItem.type = column.getString("type");
			
			columnItem.font = new Font(fontname, fontstyle, fontsize);
			
			String text = "";
			if (column.has("title"))
				text = column.getString("title");
			columnItem.title = text;
			
			Color color = textColor;
			if (column.has("textcolor"))
				color = ColorConvert.toColorFromString(column.getString("textcolor"));
			columnItem.textcolor = color;
			columns.put(column.getString("id"), columnItem);
		}
	}

	protected int drawHeader(Graphics g, Rectangle rect) throws JSONException {
		if (header == null || header.isEmpty() || !showHeader)
			return rect.y;
		
		for (Column column : columns.values()) {
			if (!column.needShow)
				continue;
					
			Rectangle r = column.r;

			if (showLine){
				Color oldColor = g.getColor();
				g.setColor(lineColor);
				g.drawRect(r.x, r.y, r.width, r.height);
				g.setColor(oldColor);
			}
					
			DrawNode.drawLineText(g, font, column.textcolor, r.x, r.y, r.width, r.height, column.title);

		}
		
		return rect.y + headerHeight;
	}

	HashMap<String, BufferedImage> images = new HashMap<>();
	
	public void clearCacheImages(){
		images.clear();
	}
	
	protected void drawItem(Graphics g, int top, JSONArray row) throws Exception {
		if (top + lineHeight > node.getRect().y + node.getRect().height)
			return;
		
		for (int i = 0; i < row.length(); i++) {
			JSONObject data = row.getJSONObject(i);
			if (!data.has("id"))
				continue;
			
			String id = data.getString("id");
			if (id == null || id.isEmpty() || !columns.containsKey(id))
				continue;
			
			Column column = columns.get(id);
			if (!column.needShow)
				continue;
			
			Rectangle r = new Rectangle(column.r);
			r.y = top;
			r.height = lineHeight;
			
			if (showLine){
				Color oldColor = g.getColor();
				g.setColor(lineColor);
				g.drawRect(r.x, r.y, r.width, r.height);
				g.setColor(oldColor);
			}

			boolean isHref = false;
			if (data.has("href")){
				isHref = true;
			}
			switch (column.type) {
			case "text":
				String text = "";
				if (data.has("text"))
					text = data.getString("text");
				Color color = column.textcolor;
				if (data.has("textcolor"))
					color = ColorConvert.toColorFromString(data.getString("textcolor"));
				
				Font font = column.font;
				String fontname = font.getFontName();
				int fontsize = font.getSize();
				int fontstyle = font.getStyle();		
				if (data.has("fontname")){
					fontname = data.getString("fontname");
				}
				if (data.has("fontsize")){
					fontsize = data.getInt("fontsize");
				}
				if (data.has("fontstyle")){
					fontstyle = data.getInt("fontstyle");
				}
				font = new Font(fontname, fontstyle, fontsize);

				if (isHref){
					font = new Font(column.font.getFontName(), font.getStyle() | Font.ITALIC, font.getSize());
					color = Color.BLUE;
				}
				
				DrawNode.drawLineText(g, font, color, r.x, r.y, r.width, r.height, text);
				break;
			case "image":
				if (!data.has("image"))
					continue;
				
				File imageFile = EditorEnvironment.getProjectFile(EditorEnvironment.Image_Resource_Path, data.getString("image"));
				if (!imageFile.exists())
					continue;
				BufferedImage image = null;
				if (!images.containsKey(imageFile.getName())){
					image = ImageUtils.loadImage(imageFile);
					images.put(imageFile.getName(), image);
				}else {
					image = images.get(imageFile.getName());
				}
				
				ScaleMode mode = ScaleMode.smCenterInRectangle;
				if (data.has("mode")){
					mode = ScaleMode.valueOf(data.getString("mode"));
				}
				ImageInfo.drawImage(ImageInfo.getSize(image, r, mode), g, image);
				break;
			case "report":
				if (!data.has("name"))
					break;
				
				ReportInfo reportInfo = getReportInfo(data.getString("name"));
				if (reportInfo == null)
					break;
				
				reportInfo.node.setRect(r);
				reportInfo.resetRowColumn();
				reportInfo.resetScales(true, false);
				reportInfo.drawNode(g, r, true);
				break;
			default:
				break;
			}
		}
	}
	
	protected int drawData(Graphics g, Rectangle rect) throws Exception {
		if (data == null || data.isEmpty())
			return rect.y;
		
		JSONArray json = (JSONArray)JsonHelp.parseJson(data);
		
		int top = rect.y + (showHeader ? (headerHeight + colDiv) : colDiv);
		
		for (int i = 0; i < json.length(); i++) {
			JSONArray row = json.getJSONArray(i);
			drawItem(g, top, row);
			top += lineHeight + colDiv;
		}
		
		return top;
	}

	public void drawNode(Graphics g, Rectangle rect){		
		if ((header == null || header.isEmpty()))
			return;
		
		Color old = g.getColor();
		g.setColor(backgroundColor);
		g.fillRect(rect.x, rect.y, rect.width, rect.height);
		g.setColor(old);

		if (border){
			old = g.getColor();
			g.setColor(lineColor);
			g.drawRect(rect.x, rect.y, rect.width, rect.height);
			g.setColor(old);
		}

		try {
			initColumns(rect);
			drawHeader(g, rect);
			drawData(g, rect);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ListViewInfo(UINode node) {
		super(node);
		width = "400";
		height = "400";
		needFrame = true;
		allowEdit = false;
	}
	
}