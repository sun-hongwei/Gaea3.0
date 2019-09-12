package com.wh.draws;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wh.control.EditorEnvironment;
import com.wh.draws.DrawCanvas.Config;
import com.wh.draws.DrawCanvas.FixType;
import com.wh.draws.DrawCanvas.ICreateNodeSerializable;
import com.wh.draws.DrawCanvas.IDataSerializable;
import com.wh.draws.DrawCanvas.IJsonObject;
import com.wh.draws.DrawCanvas.IntersectPointHelp;
import com.wh.draws.DrawCanvas.Polyline;
import com.wh.draws.DrawCanvas.ResizeButtonType;
import com.wh.draws.control.ActionCommandManager.CommandInfoType;
import com.wh.system.tools.JsonHelp;

public class DrawNode implements IJsonObject {

	public Font font = new Font("微软雅黑", Font.BOLD, 12);

	public enum LineType {
		ltArrows, ltDot, ltLine
	}

	protected LineType lineType = LineType.ltArrows;
	public String id = UUID.randomUUID().toString();
	protected Point relativeToPage;
	private Rectangle rect;

	protected List<String> prevs = new ArrayList<>();
	protected List<String> nexts = new ArrayList<>();
	protected DrawCanvas canvas;

	public Object userData = null;
	public int zOrder = 0;
	public String title;
	public String name;
	public IDataSerializable userDataSerializable;
	public String memo;

	public String copy_filename;
	
	protected KeyListener keyListener = new KeyAdapter() {
	};
	protected MouseListener mouseListener = new MouseAdapter() {
	};
	protected MouseMotionListener mouseMotionListener = new MouseMotionAdapter() {
	};

	public boolean isParent(DrawNode node) {
		return false;
	}

	protected void removed() {
		
	}
	
	public void pasted() {
		
	}
	
	public void popCommanded(DrawNode oldNode, CommandInfoType cit) {
		
	}
	
	public boolean isDrawTreeRoot() {
		return false;
	}

	protected void changeid(String oldid, String newid) {

	}

	protected ResizeButtonType getResizeButtonType(Point pt) {
		for (ResizeButtonType rt : resizeButtons.keySet()) {
			Rectangle rect = resizeButtons.get(rt);
			if (rect.contains(pt)) {
				return rt;
			}
		}

		return ResizeButtonType.rtNone;
	}

	protected FixType[] move(DrawNode dragNode, int x, int y) {
		FixType[] fixTypes = new FixType[] { FixType.ftNone, FixType.ftNone };

		if (canvas.oldP == null)
			return null;

		int offsetX = x - canvas.oldP.x;
		int offsetY = y - canvas.oldP.y;

		if (Math.abs(x - canvas.oldFixP.x) > DrawCanvas.lineDiv) {
			if (offsetX > 0) {
				fixTypes[0] = FixType.ftXRight;
			} else if (offsetX < 0)
				fixTypes[0] = FixType.ftXLeft;
			else {
				fixTypes[0] = FixType.ftNone;
			}

			if (fixTypes[0] != FixType.ftNone)
				canvas.oldFixP.x = x;
		}

		if (Math.abs(y - canvas.oldFixP.y) > DrawCanvas.lineDiv) {
			if (offsetY > 0) {
				fixTypes[1] = FixType.ftYBottom;
			} else if (offsetY < 0)
				fixTypes[1] = FixType.ftYTop;
			else {
				fixTypes[1] = FixType.ftNone;
			}
			if (fixTypes[1] != FixType.ftNone)
				canvas.oldFixP.y = y;
		}

		if (dragNode == null) {
			canvas.offset.x += offsetX;
			canvas.offset.y += offsetY;
			Rectangle clip = canvas.getClipRect();
			Rectangle rect = new Rectangle(canvas.offset.x, canvas.offset.y, clip.width, clip.height);
			canvas.checkConstraint(rect);
			canvas.offset.x = rect.x;
			canvas.offset.y = rect.y;
		} else {
			dragNode.rect.x += offsetX;
			dragNode.rect.y += offsetY;

			canvas.checkConstraint(dragNode.rect);
		}

		dragNode.onMoved();
		return fixTypes;
	}

	protected enum NeedResizeType {
		rtLocation, rtWidth, rtHeight, rtNone;
	}

	protected NeedResizeType[] needResize(DrawNode dragNode, int offsetX, int offsetY) {
		if (offsetX == 0 && offsetY == 0)
			return null;

		NeedResizeType[] rts = new NeedResizeType[2];
		rts[0] = NeedResizeType.rtWidth;
		rts[1] = NeedResizeType.rtHeight;

		Rectangle rect = dragNode.rect;
		Rectangle clip = canvas.getClipRect();
		switch (canvas.curRt) {
		case rtLeft:
		case rtRight:
		case rtTop:
		case rtBottom:
			return new NeedResizeType[] { NeedResizeType.rtLocation };
		case rtLeftBottom:
			if (offsetX < 0 && rect.x == clip.x)
				rts[0] = NeedResizeType.rtNone;
			else if (offsetX > 0 && (rect.width == DrawCanvas.MIN_WIDTH))
				rts[0] = NeedResizeType.rtNone;

			if (offsetY > 0 && rect.y + rect.height == clip.y + clip.height)
				rts[1] = NeedResizeType.rtNone;
			else if (offsetY < 0 && rect.height == DrawCanvas.MIN_HEIGHT)
				rts[1] = NeedResizeType.rtNone;
			break;
		case rtRightBottom:
			if (offsetX < 0 && rect.width == DrawCanvas.MIN_WIDTH)
				rts[0] = NeedResizeType.rtNone;
			else if (offsetX > 0 && (rect.x + rect.width == clip.x + clip.width))
				rts[0] = NeedResizeType.rtNone;

			if (offsetY > 0 && rect.y + rect.height == clip.y + clip.height)
				rts[1] = NeedResizeType.rtNone;
			else if (offsetY < 0 && rect.height == DrawCanvas.MIN_HEIGHT)
				rts[1] = NeedResizeType.rtNone;
			break;
		case rtLeftTop:
			if (offsetX < 0 && rect.x == clip.x)
				rts[0] = NeedResizeType.rtNone;
			else if (offsetX > 0 && (rect.width == DrawCanvas.MIN_WIDTH))
				rts[0] = NeedResizeType.rtNone;

			if (offsetY < 0 && rect.y == clip.y)
				rts[1] = NeedResizeType.rtNone;
			else if (offsetY > 0 && rect.height == DrawCanvas.MIN_HEIGHT)
				rts[1] = NeedResizeType.rtNone;
			break;
		case rtRightTop:
			if (offsetX < 0 && rect.width == DrawCanvas.MIN_WIDTH)
				rts[0] = NeedResizeType.rtNone;
			else if (offsetX > 0 && (rect.x + rect.width == clip.x + clip.width))
				rts[0] = NeedResizeType.rtNone;

			if (offsetY < 0 && rect.y == clip.y)
				rts[1] = NeedResizeType.rtNone;
			else if (offsetY > 0 && rect.height == DrawCanvas.MIN_HEIGHT)
				rts[1] = NeedResizeType.rtNone;
			break;
		case rtNone:
			return null;
		default:
			break;
		}
		return rts;

	}

	protected void resize(int x, int y) {
		Point realPoint = canvas.getRealPoint(new Point(x, y));
		if (!canvas.getClipRect().contains(realPoint))
			return;

		int offsetX = x - canvas.oldP.x;
		int offsetY = y - canvas.oldP.y;

		NeedResizeType[] rts = needResize(this, offsetX, offsetY);
		if (rts == null)
			return;

		switch (canvas.curRt) {
		case rtLeft:
			this.rect.x += offsetX;
			this.rect.width -= offsetX;

			break;
		case rtRight:
			this.rect.width += offsetX;
			break;
		case rtTop:
			this.rect.height -= offsetY;
			this.rect.y += offsetY;
			break;
		case rtBottom:
			this.rect.height += offsetY;
			break;
		case rtLeftBottom:
			if (rts[0] == NeedResizeType.rtWidth) {
				this.rect.x += offsetX;
				this.rect.width -= offsetX;
			}
			if (rts[1] == NeedResizeType.rtHeight) {
				this.rect.height += offsetY;
			}
			break;
		case rtRightBottom:
			if (rts[0] == NeedResizeType.rtWidth) {
				this.rect.width += offsetX;
			}
			if (rts[1] == NeedResizeType.rtHeight) {
				this.rect.height += offsetY;
			}
			break;
		case rtLeftTop:
			if (rts[0] == NeedResizeType.rtWidth) {
				this.rect.x += offsetX;
				this.rect.width -= offsetX;
			}
			if (rts[1] == NeedResizeType.rtHeight) {
				this.rect.y += offsetY;
				this.rect.height -= offsetY;
			}
			break;
		case rtRightTop:
			if (rts[0] == NeedResizeType.rtWidth) {
				this.rect.width += offsetX;
			}
			if (rts[1] == NeedResizeType.rtHeight) {
				this.rect.y += offsetY;
				this.rect.height -= offsetY;
			}
			break;
		case rtNone:
		default:
			break;
		}

		canvas.checkSizeConstraint(this.rect, canvas.curRt);

		this.onRectChanged();
	}

	protected void onRectChanged() {

	}

	public DrawCanvas getCanvas() {
		return canvas;
	}

	public List<String> getPrevs() {
		return prevs;
	}

	public List<String> getNexts() {
		return nexts;
	}

	public void setCanvas(DrawCanvas canvas) {
		this.canvas = canvas;
	}

	public DrawNode(DrawCanvas canvas) {
		this.canvas = canvas;
		this.name = id;
	}

	/**
	 * 仅刷新显示区域，如果要永久修改节点的显示区域，应该调用setRect方法
	 */
	public void invalidRect() {
		rect = null;
	}

	/**
	 * 仅刷新显示区域，如果要永久修改节点的显示区域，应该调用setRect方法
	 */
	public void invalidRect(Rectangle rect) {
		this.rect = new Rectangle(rect);
	}

	/**
	 * 仅刷新显示区域，如果要永久修改节点的显示区域，应该调用setRect方法
	 */
	public void invalidRect(Point pt) {
		if (rect == null)
			rect = new Rectangle(0, 0, 100, 100);

		this.rect.setLocation(pt);
	}

	protected void onMoved() {

	}

	protected void onResized() {

	}

	public Rectangle getRect() {
		return rect;
	}

	public enum UpdateType {
		utLeft, utWidth, utTop, utHeight
	}

	public void setRect(Rectangle rect) {
		this.rect = new Rectangle(rect);
	}

	public Point getLocation() {
		return rect.getLocation();
	}

	public int getWidth() {
		getRect();
		return rect.width;
	}

	public int getHeight() {
		getRect();
		return rect.height;
	}

	public void setLocation(Point pos) {
		getRect();
		pos = canvas.getRealPoint(pos);
		pos.x += canvas.useRect.x;
		pos.y += canvas.useRect.y;
		rect.setLocation(pos);
	}

	public void setWidth(int width) {
		getRect();
		rect.width = width;
	}

	public void setHeight(int height) {
		getRect();
		rect.height = height;
	}

	protected void updateRect(UpdateType[] types) {

	}

	public JSONObject toJson() throws JSONException {
		JSONObject data = new JSONObject();
		if (canvas != null) {
			if (rect == null)
				getRect();
			if (rect != null && canvas.useRect != null) {
				data.put("relativeToPage.x", rect.x - canvas.useRect.x);
				data.put("relativeToPage.y", rect.y - canvas.useRect.y);
			}
		}
		data.put("memo", memo);
		data.put("zOrder", zOrder);
		data.put("title", title);
		data.put("id", id);
		data.put("name", name);
		if (rect != null) {
			data.put("rect.x", rect.x);
			data.put("rect.y", rect.y);
			data.put("rect.width", rect.width);
			data.put("rect.height", rect.height);
		}
		data.put("class", this.getClass().getName());
		JSONArray subs = new JSONArray();
		for (String key : prevs) {
			subs.put(key);
		}
		data.put("prevs", subs);

		subs = new JSONArray();
		for (String key : nexts) {
			subs.put(key);
		}
		data.put("nexts", subs);
		data.put("copy_filename", EditorEnvironment.getModelFile(id).getAbsolutePath());
		if (userData != null) {
			data.put("userdata", userDataSerializable.save(userData));
		}

		return data;
	}

	protected static void drawLine(Graphics g, Point start, int width) {
		drawLine(g, start, new Point(start.x + width, start.y));
	}

	protected static void drawLine(Graphics g, Point start, Point end) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setStroke(new BasicStroke(2.0f));
		g2.drawLine(start.x, start.y, end.x, end.y);
	}

	public static DrawNode load(File nodeFile, ICreateNodeSerializable createUserDataSerializable)
			throws Exception {
		JSONObject data = (JSONObject) JsonHelp.parseJson(nodeFile, null);
		if (data == null)
			return null;

		return fromJson(null, data, createUserDataSerializable);
	}

	public static DrawNode fromJson(DrawCanvas canvas, JSONObject data,
			ICreateNodeSerializable createUserDataSerializable) throws Exception {
		DrawNode node;
		if (data.has("class")) {
			String className = data.getString("class");
			if (className.compareTo("com.wh.system.form.draws.FlowNode$UIWorkflowNode") == 0) {
				className = "com.wh.system.form.draws.FlowNode$ActionNode";
			}
			ClassLoader classLoader = ClassLoader.getSystemClassLoader();

			if (className.startsWith("com.wh.system.form.draws")){
				className = "com.wh.draws" + className.substring(className.lastIndexOf("."));
			}else if (className.startsWith("com.wh.system.draws")){
				className = "com.wh.draws" + className.substring(className.lastIndexOf("."));
			}
			Class<?> classDeclare = classLoader.loadClass(className);
			node = (DrawNode) classDeclare.getConstructor(DrawCanvas.class).newInstance(canvas);
		} else
			node = createUserDataSerializable.newDrawNode(data);

		node.fromJson(data, createUserDataSerializable);
		return node;
	}

	public void fromJson(JSONObject data, ICreateNodeSerializable createUserDataSerializable) throws JSONException {

		relativeToPage = new Point();

		if (data.has("copy_filename")){
			copy_filename = data.getString("copy_filename");
		}else
			copy_filename = null;
		
		if (data.has("relativeToPage.x"))
			relativeToPage.x = data.getInt("relativeToPage.x");
		if (data.has("relativeToPage.y"))
			relativeToPage.y = data.getInt("relativeToPage.y");

		if (data.has("memo"))
			memo = data.getString("memo");
		else
			memo = null;

		if (data.has("zOrder"))
			zOrder = data.getInt("zOrder");
		else
			zOrder = 0;

		if (data.has("title"))
			title = data.getString("title");
		else
			title = null;

		id = data.getString("id");

		if (data.has("name"))
			name = data.getString("name");
		else
			name = id;

		rect = new Rectangle();

		if (data.has("rect.x")) {
			rect.x = data.getInt("rect.x");
			rect.y = data.getInt("rect.y");
			rect.width = data.getInt("rect.width");
			rect.height = data.getInt("rect.height");
		}

		prevs.clear();
		nexts.clear();

		JSONArray subs = data.getJSONArray("prevs");
		for (int i = 0; i < subs.length(); i++) {
			prevs.add(subs.getString(i));
		}

		subs = data.getJSONArray("nexts");
		for (int i = 0; i < subs.length(); i++) {
			nexts.add(subs.getString(i));
		}

		if (data.has("userdata")) {
			if (createUserDataSerializable != null) {
				userDataSerializable = createUserDataSerializable.getUserDataSerializable(this);
				if (userDataSerializable != null)
					userData = userDataSerializable.load(data.getString("userdata"));
			}
		}
	}

	public IntersectPointHelp.IntersectPoint getIntersectCenter() {
		Point point = getCenter();
		return new IntersectPointHelp.IntersectPoint(point.x, point.y);
	}

	public Point getCenter() {
		return new Point(rect.x + (int) (rect.getWidth() / 2), rect.y + (int) (rect.getHeight() / 2));
	}

	public boolean needShow(Rectangle disRect) {
		if (rect == null)
			return false;
		return !disRect.intersection(rect).isEmpty();
	}

	public boolean isPoint(Point pt) {
		if (rect == null)
			return false;
		return rect.contains(pt);
	}

	protected static void drawText(Graphics g, Font font, Rectangle rect, StringBuilder sBuilder) {
		Color textColor = new Color(250, 250, 250);
		drawText(g, font, textColor, rect, sBuilder);
	}

	protected static void drawText(Graphics g, Font font, Color color, Rectangle rectangle,
			StringBuilder sBuilder) {
		Rectangle rect = new Rectangle(rectangle);
		rect.x += 5;
		rect.y += 5;
		rect.width -= 10;
		rect.height -= 10;

		Color old = g.getColor();
		g.setColor(color);
		List<String> draws = new ArrayList<>();
		List<Rectangle2D> widths = new ArrayList<>();
		int start = 0;
		int end = start;

		int minDivX = 5;
		int maxWidth = (int) rect.getWidth() - minDivX * 2;
		FontMetrics fm = g.getFontMetrics(font);
		do {
			end += 2;
			String tmp = sBuilder.substring(start, end < sBuilder.length() ? end : sBuilder.length());
			Rectangle2D textRect = fm.getStringBounds(tmp, g);
			if (textRect.getWidth() > maxWidth || end >= sBuilder.length()) {
				draws.add(tmp);
				widths.add(textRect);
				start = end;
			}
		} while (end < sBuilder.length());

		int divY = 3;
		int y = rect.y
				+ (rect.height - (int) widths.get(0).getHeight() * draws.size() - divY * (draws.size() - 1)) / 2;
		y = y + (int) widths.get(0).getHeight();
		for (int i = 0; i < draws.size(); i++) {
			int x = rect.x + (rect.width - (int) widths.get(i).getWidth()) / 2;
			g.drawString(draws.get(i), x, y);
			y += widths.get(i).getHeight() + divY;
		}

		g.setColor(old);
	}

	public static void drawMulitText(Graphics g, Font font, Color color, Rectangle rect, String value) {
		String text = value.replace("\r", "");
		String[] texts = text.split("\n");
		int hdiv = 3;
		Rectangle2D[] textRects = new Rectangle2D[texts.length];
		int heights = 0;
		int index = 0;
		int maxWidth = 0;
		FontMetrics fm = g.getFontMetrics(font);
		for (String string : texts) {
			Rectangle2D textRect = fm.getStringBounds(string, g);
			heights += textRect.getHeight() + hdiv;
			textRects[index++] = textRect;
			if (maxWidth < textRect.getWidth())
				maxWidth = (int) textRect.getWidth();
		}

		heights -= hdiv;

		int x = rect.x + (rect.width - maxWidth) / 2;
		int y = (int) (rect.y + (rect.height - heights) / 2 + textRects[0].getHeight());

		Color old = g.getColor();
		g.setColor(color);

		Font oldFont = g.getFont();
		g.setFont(font);

		for (int i = 0; i < textRects.length; i++) {
			g.drawString(texts[i], x, y);
			y += textRects[i].getHeight() + hdiv;
		}

		g.setFont(oldFont);
		g.setColor(old);
	}

	public static Rectangle2D getTextRectangle(Graphics g, Font font, String text) {
		FontMetrics fm = g.getFontMetrics(font);
		Rectangle2D textRect = fm.getStringBounds(text, g);
		return textRect;
	}

	public static int drawLineText(Graphics g, Font font, Color textColor, int x, int y, int width, int height,
			String text) {
		return drawLineText(g, font, textColor, x, y, width, height, text, true);
	}

	public static int drawLineText(Graphics g, Font font, Color textColor, int x, int y, int width, int height,
			String text, boolean center) {
		return drawLineText(g, font, textColor, x, y, width, height, text, center, false);
	}

	public static int getTextHeight(Graphics g, Font font, String text){
		FontMetrics fm = g.getFontMetrics(font);
		Rectangle2D textRect = null;
		textRect = fm.getStringBounds(text, g);
		return (int) textRect.getHeight();
	}
	
	public static int drawLineText(Graphics g, Font font, Color textColor, int x, int y, int width, int height,
			String text, boolean center, boolean onlyCompute) {
		if (text == null || text.isEmpty() || width == 0)
			return 0;

		Color old = g.getColor();
		g.setColor(textColor);

		FontMetrics fm = g.getFontMetrics(font);
		Rectangle2D textRect = null;
		int count = text.length();
		textRect = fm.getStringBounds(text, g);

		if (onlyCompute)
			return (int) textRect.getHeight();

		String oldText = text;
		while (!text.isEmpty()) {
			if (text.length() < 4)
				break;

			textRect = fm.getStringBounds(text, g);
			if (textRect.getWidth() > width) {
				int multiple = (int) textRect.getWidth() / width;
				if (multiple >= 2) {
					int div = text.length() / 2;
					text = text.substring(0, div == 0 ? 1 : div);
				} else {
					text = text.substring(0, text.length() - 4);
				}
			} else if (text.length() == count || text.substring(text.length() - 3).compareTo("...") == 0)
				break;
			else {
				text = text + "...";
			}
		}

		if (oldText != null && !oldText.isEmpty() && (text == null || text.isEmpty())){
			text = "...";
		}
		
		textRect = fm.getStringBounds(text, g);
		
		int top = height;
		if (height == -1)
			top = y + (int) textRect.getHeight();
		else
			top = y + (height - (int) (fm.getAscent() + fm.getDescent())) / 2 + fm.getAscent();
		

		int drawX = x;
		if (center)
			drawX += (width - (int) textRect.getWidth()) / 2;
		
		g.setFont(font);

		g.drawString(text, drawX, top);

		g.setColor(old);

		return (int) textRect.getHeight();
	}

	protected LineType getLineType(DrawNode node) {
		LineType lt1 = node.lineType;
		LineType lt2 = lineType;

		LineType lt = LineType.ltArrows;
		switch (lt1) {
		case ltArrows:
			if (lt2 != LineType.ltArrows)
				lt = lt2;
			break;
		case ltDot:
			lt = LineType.ltDot;
			break;
		case ltLine:
			if (lt2 == LineType.ltDot)
				lt = LineType.ltDot;
			else {
				lt = LineType.ltLine;
			}
			break;
		default:
			break;
		}

		return lt;
	}

	public void drawLins(Graphics g) {
		;
		Rectangle disRect = canvas.getViewPortRect();
		boolean needDraw = needShow(disRect);

		Graphics2D g2d = (Graphics2D) g;
		Color old = g.getColor();
		Stroke oldStroke = g2d.getStroke();

		for (String key : nexts) {
			g.setColor(Color.DARK_GRAY);
			DrawNode node = canvas.nodes.get(key);
			if (node == null)
				continue;

			Polyline line;
			String linekey = Polyline.getHashKey(this, node);
			if (!canvas.lines.containsKey(linekey)) {
				line = new Polyline(canvas, this, node);
				canvas.lines.put(linekey, line);
			} else
				line = canvas.lines.get(linekey);

			if (!node.needShow(disRect) && !needDraw) {
				continue;
			}

			g.setColor(Color.DARK_GRAY);
			for (Polyline selectedLine : canvas.selectedLines) {
				if (selectedLine.check(this, node)) {
					g.setColor(Color.RED);
					break;
				}
			}

			LineType lt = getLineType(node);

			switch (lt) {
			case ltArrows:
			case ltLine:
				g2d.setStroke(oldStroke);
				break;
			case ltDot:
				Stroke dot = new BasicStroke(2.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1.5f,
						new float[] { 5, 3, }, 0f);
				g2d.setStroke(dot);
				break;
			}
			line.drawPolyline(g2d);

			if (canvas.selectedLines.indexOf(line) != -1)
				line.darwResizeButtons(g);

		}

		if (needDraw) {
			Point endPt = getCenter();
			g.setColor(Color.darkGray);
			for (String key : prevs) {
				DrawNode node = canvas.nodes.get(key);
				if (node == null)
					continue;

				Polyline polyline = null;
				String linekey = Polyline.getHashKey(node, this);
				if (!canvas.lines.containsKey(linekey)) {
					continue;
				} else
					polyline = canvas.lines.get(linekey);

				Point startPt = new Point(polyline.xs.get(polyline.xs.size() - 2),
						polyline.ys.get(polyline.ys.size() - 2));
				IntersectPointHelp.Line line = new IntersectPointHelp.Line(
						new IntersectPointHelp.IntersectPoint(startPt.x, startPt.y),
						new IntersectPointHelp.IntersectPoint(endPt.x, endPt.y));

				IntersectPointHelp.IntersectPoint ix = IntersectPointHelp.intersection(line, rect);
				LineType lt = getLineType(node);

				switch (lt) {
				case ltArrows:
					if (ix != null) {
						g.fillRect((int) ix.x - 5, (int) ix.y - 5, 10, 10);
					}
					break;
				case ltLine:
				case ltDot:
					break;
				}
			}
		}
		g.setColor(old);
		g2d.setStroke(oldStroke);
	}

	protected HashMap<ResizeButtonType, Rectangle> resizeButtons = new HashMap<>();

	protected void darwResizeButtons(Graphics g, Rectangle rect) {
		resizeButtons.clear();
		if (!canvas.pageConfig.checkConfig(Config.ccAllowResize))
			return;

		final int size = 8;
		Rectangle leftRect = new Rectangle(rect.x - size / 2, rect.y + (rect.height - size) / 2, size, size);
		Rectangle topRect = new Rectangle(rect.x + (rect.width - size) / 2, rect.y - size / 2, size, size);
		Rectangle rightRect = new Rectangle(rect.x + rect.width - size / 2, rect.y + (rect.height - size) / 2, size,
				size);
		Rectangle bottomRect = new Rectangle(rect.x + (rect.width - size) / 2, rect.y + rect.height - size / 2,
				size, size);
		Rectangle leftTopRect = new Rectangle(rect.x - size / 2, rect.y - size / 2, size, size);
		Rectangle rightTopRect = new Rectangle(rect.x + rect.width - size / 2, rect.y - size / 2, size, size);
		Rectangle LeftBottomRect = new Rectangle(rect.x - size / 2, rect.y + rect.height - size / 2, size, size);
		Rectangle rightBottomRect = new Rectangle(rect.x + rect.width - size / 2, rect.y + rect.height - size / 2,
				size, size);

		resizeButtons.put(ResizeButtonType.rtLeft, leftRect);
		resizeButtons.put(ResizeButtonType.rtRight, rightRect);
		resizeButtons.put(ResizeButtonType.rtTop, topRect);
		resizeButtons.put(ResizeButtonType.rtBottom, bottomRect);
		resizeButtons.put(ResizeButtonType.rtLeftTop, leftTopRect);
		resizeButtons.put(ResizeButtonType.rtLeftBottom, LeftBottomRect);
		resizeButtons.put(ResizeButtonType.rtRightBottom, rightBottomRect);
		resizeButtons.put(ResizeButtonType.rtRightTop, rightTopRect);

		g.setColor(Color.BLUE);
		g.fill3DRect(leftRect.x, leftRect.y, leftRect.width, leftRect.height, true);
		g.fill3DRect(topRect.x, topRect.y, topRect.width, topRect.height, true);
		g.fill3DRect(rightRect.x, rightRect.y, rightRect.width, rightRect.height, true);
		g.fill3DRect(bottomRect.x, bottomRect.y, bottomRect.width, bottomRect.height, true);
		g.fill3DRect(leftTopRect.x, leftTopRect.y, leftTopRect.width, leftTopRect.height, true);
		g.fill3DRect(rightTopRect.x, rightTopRect.y, rightTopRect.width, rightTopRect.height, true);
		g.fill3DRect(LeftBottomRect.x, LeftBottomRect.y, LeftBottomRect.width, LeftBottomRect.height, true);
		g.fill3DRect(rightBottomRect.x, rightBottomRect.y, rightBottomRect.width, rightBottomRect.height, true);

	}

	protected void drawNode(Graphics g) {
	}

	public void draw(Graphics g, boolean needCheckViewPort) {
		getRect();
		if (rect == null || rect.x == -1 || rect.y == -1 || rect.width == -1 || rect.height == -1)
			return;

		if (!canvas.stackTreeManager.isVisiable(id))
			return;

		if (needCheckViewPort && !needShow(canvas.getViewPortRect())) {
			return;
		}

		Color old = g.getColor();

		drawNode(g);

		boolean isselect = false;
		for (DrawNode node : canvas.selectNodes) {
			if (node.id.compareToIgnoreCase(this.id) == 0) {
				g.setColor(Color.RED);
				isselect = true;
				break;
			}
		}

		if (isselect) {
			g.drawRect(rect.x, rect.y, rect.width, rect.height);
			darwResizeButtons(g, rect);
		}

		g.setColor(old);
	}
}


