package com.wh.draws;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import javax.swing.JDesktopPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wh.control.EditorEnvironment;
import com.wh.draws.DrawCanvas.IntersectPointHelp.IntersectPoint;
import com.wh.draws.DrawCanvas.IntersectPointHelp.Line;
import com.wh.draws.control.ActionCommandManager;
import com.wh.draws.control.ActionCommandManager.CommandInfoType;
import com.wh.draws.control.StackTreeManager;
import com.wh.draws.control.StatckTreeElement;
import com.wh.form.ChildForm;
import com.wh.system.tools.ColorConvert;
import com.wh.system.tools.JsonHelp;

public class DrawCanvas extends JPanel {
	private static final long serialVersionUID = 1L;

	public static final float LINEWIDTH = 2.5f;

	public static final int LDPI = 120; // 75% 已经被市场淘汰
	public static final int MDPI = 160; // 100% 基本被市场淘汰
	public static final int TVDPI = 213; // 133% Google Nexus 7
	public static final int HDPI = 240; // 150% Sumsong Glaxy S2， Google Nexus
										// S， MOTO Droid
	public static final int XHDPI = 320; // 200% Sumsong Glaxy S3， Sumsong
											// Note2， Google Nexus 4
	public static final int XXHDPI = 480; // 300% 目前市场上各品牌的旗舰机：Sumsong Glaxy S4
	public static final int XXXHDPI = 560; // 350% 4K
	public static final int XX42HDPI = 420; // 263% 非android标准
	public static final int XX44HDPI = 440; // 275% 非android标准

	public static final String[] ALIGNNAMES = { "顶部对齐", "底部对齐", "左部对齐", "右部对齐", "-", "顶部拉齐", "底部拉齐", "左部拉齐", "右部拉齐",
			"-", "横向居中", "纵向剧中", "-", "横向分布", "纵向分布", "-", "等宽", "等高" };
	public static final String[] PAGENAMES = new String[] { "A4【横向】", "A4【纵向】", "A3【横向】", "A3【纵向】", "A2【横向】", "A2【纵向】",
			"A1【横向】", "A1【纵向】", "720p【720 * 1280】", "1080p【1080 * 1920】", "2k【2560 * 1440】", "WXGA+【1440x900】19寸",
			"WSXGA+【1680 * 1050】20-22寸", "WUXGA【1920 * 1200】17-24寸", "WQXGA【2560 * 1600】30寸", "QVGA【320 * 240】",
			"VGA【640 * 480】", "SVGA【800 * 600】", "XGA【1024 * 768】", "qHD【960 * 540】", "WXGA【1366 * 768】",
			"5.0寸，1080 * 1920（手机）", "5.5寸，1080 * 2160（手机）", "5.5寸，1440 * 2560（手机）", "6.0寸，1440 * 2560（手机）",
			"6.0寸，1440 * 2880（手机）", "6.3寸，1440 * 2960（手机）", "7.0寸，800 * 1280（平板）", "7.0寸，1200 * 1920（平板）",
			"8.9寸，2048 * 1536（平板）", "9.9寸，2560 * 1800（平板）", "10.1寸，2560 * 1600（平板）", "自定义" };

	public enum ChangeType {
		ctAdd, ctMove, ctResize, ctPaste, ctAddLink, ctRemoveLink, ctRemove, ctSelected, ctDeselected, ctSelecteds, ctMouseRelease, ctKeyUp, ctLineChanged, ctBackspacing, ctBringToTop, ctSendToBack, ctBackEdited, ctReportApplyTemplate, ctReportRemoveRow, ctReportRemoveCell, ctReportRemoveColumn, ctReportAddRow, ctReportAddColumn, ctReportMerge, ctReportChangeCell, ctReportChangeRowCount, ctReportChangeColCount, ctReportChangeRowSize, ctReportChangeColSize, ctReportResetScale, ctReportResetCellSize, ctReportResetSize, ctReportSplit, ctReportSelected, ctReportDeselected
	}

	enum ResizeButtonType {
		rtNone, rtLeft, rtTop, rtBottom, rtRight, rtLeftTop, rtLeftBottom, rtRightTop, rtRightBottom, rtCustom
	}

	public enum PageSizeMode {
		psA4H, psA4V, psA3H, psA3V, psA2H, psA2V, psA1H, psA1V, ps720p, ps1080P, ps2k, psWXGAP, psWSXGA, psWUXGA, psWQXGA, psQVGA, psVGA, psqHD, psWXGA, psSVGA, psXGA, ps5P420, ps5P440, ps55P560, ps6P560, ps6PX560, ps63P560, ps7P213, ps7P320, ps89P320, ps99P320, ps101P320, psCustom, psNone,
	}

	static final int MIN_WIDTH = 10;
	static final int MIN_HEIGHT = 10;
	static final int lineDiv = 10;

	boolean stopPaint = false;
	boolean firstSelected = false;

	public boolean useCheckConstraint = true;

	Image offScreenImage;

	JPopupMenu menu;

	PageConfig pageConfig;

	Dimension canvasPageSize = new Dimension();
	Dimension configPageSize = new Dimension();
	Rectangle useRect;

	private ICreateNodeSerializable lastCreateUserDataSerializable;

	private ActionCommandManager acm;
	boolean isCtrlDown = false;
	boolean isAltDown = false;
	boolean isMouseDown = false;
	Point oldOffset = new Point();

	boolean keypressed = false;

	private Cursor curCursor;

	private MouseMode mouseMode = MouseMode.mmNone;

	Point oldP = null;
	Point startP = null;
	Point oldFixP = null;
	Point newP = null;
	Point keyMoveP = null;

	DrawNode start, end;
	List<DrawNode> selectNodes = new ArrayList<>();
	List<Polyline> selectedLines = new ArrayList<>();

	Point offset = new Point(0, 0);
	PageSizeMode pageSize = PageSizeMode.psA3V;

	TreeMap<Integer, Point> autoRows = new TreeMap<>();
	TreeMap<Integer, Point> autoCols = new TreeMap<>();

	ResizeButtonType curRt = ResizeButtonType.rtNone;

	protected void addOrRemoveSelect(DrawNode node) {
		int index = selectNodes.indexOf(node);
		if (index == -1) {
			selectNodes.add(node);
			fireChange(new DrawNode[] { node }, ChangeType.ctSelected);
		} else {
			selectNodes.remove(index);
			fireChange(new DrawNode[] { node }, ChangeType.ctDeselected);
		}
	}

	public void clearSelect() {
		clearSelect(true);
	}

	public void clearSelect(boolean needPush) {
		if (getSelected() == null)
			return;

		if (needPush)
			getAcm().pushCommand(selectNodes, CommandInfoType.ctDeselected);

		List<DrawNode> deSelectNodes = new ArrayList<>(selectNodes);
		selectNodes.clear();
		fireChange(deSelectNodes, ChangeType.ctDeselected);
	}

	protected void removeSelect(DrawNode[] nodes) {
		Map<String, DrawNode> mapNodes = new HashMap<>();
		for (DrawNode drawNode : selectNodes) {
			mapNodes.put(drawNode.id, drawNode);
		}

		for (DrawNode node : nodes) {
			if (node != null) {
				if (mapNodes.containsKey(node.id)) {
					mapNodes.remove(node.id);
				}
			}
		}

		getAcm().pushCommand(selectNodes, CommandInfoType.ctDeselected);

		List<DrawNode> deSelects = new ArrayList<>(selectNodes);

		selectNodes.clear();
		fireChange(deSelects, ChangeType.ctDeselected);

		selectNodes.addAll(mapNodes.values());
		fireChange(selectNodes, ChangeType.ctSelecteds);
	}

	protected void removeSelect(DrawNode node) {
		int index = selectNodes.indexOf(node);
		if (index != -1) {
			getAcm().pushCommand(selectNodes, CommandInfoType.ctDeselected);
			selectNodes.remove(index);
			fireChange(selectNodes, ChangeType.ctSelecteds);
		}
	}

	public void setSelected(DrawNode node) {
		if (node == null) {
			clearSelect(false);
			return;
		}

		getAcm().pushCommand(selectNodes, CommandInfoType.ctDeselected);

		setSelects(new DrawNode[] { node }, false, false);

		fireChange(new DrawNode[] { node }, ChangeType.ctSelected);
	}

	protected void setSelects(DrawNode[] nodes, boolean fireChange, boolean pushCommand) {
		setSelects(Arrays.asList(nodes), fireChange, pushCommand);
	}

	public void setSelects(Collection<DrawNode> nodes, boolean fireChange, boolean pushCommand) {
		boolean needDo = nodes != null;
		if (needDo && nodes.size() == selectNodes.size()) {
			boolean hasChange = false;
			for (DrawNode drawNode : nodes) {
				if (selectNodes.indexOf(drawNode) == -1) {
					hasChange = true;
					break;
				}
			}
			needDo = hasChange;
		}

		if (!needDo)
			return;

		if (pushCommand)
			getAcm().pushCommand(selectNodes, CommandInfoType.ctDeselected);

		selectNodes.clear();
		if (nodes != null) {
			Map<String, DrawNode> mapNodes = new HashMap<>();

			for (DrawNode drawNode : nodes) {
				if (drawNode != null) {
					mapNodes.put(drawNode.id, drawNode);
				}
			}
			selectNodes = getRealNodes(mapNodes.values());
		}

		if (fireChange && selectNodes.size() > 0) {
			fireChange(selectNodes, ChangeType.ctSelecteds);
		}
	}

	public boolean isCtrlPressed() {
		return isCtrlDown;
	}

	public boolean isAltPressed() {
		return isAltDown;
	}

	public File canvasFile;
	public HashMap<String, DrawNode> nodes = new HashMap<>();
	public HashMap<String, Polyline> lines = new HashMap<>();
	public INode nodeEvent = null;

	public interface IScroll {
		public void onScroll(int x, int y);
	}

	enum FixType {
		ftNone, ftXLeft, ftXRight, ftYTop, ftYBottom
	}

	public enum MouseMode {
		mmNone, mmDrag, mmMuileSelect, mmLink, mmRemoveLink, mmRectSelect, mmResize, mmCustom
	}

	public enum Config {
		ccGrid, ccFixGrid, ccLink, ccAllowMulSelect, ccAllowResize, ccAllowSelect, ccAllowDrag, ccAllowEdit
	}

	StackTreeManager stackTreeManager;

	public StackTreeManager getStackTreeManager() {
		return stackTreeManager;
	}

	public DrawNode nodeConnectToPrevType(DrawNode node, Class<? extends DrawNode> c) {
		for (String id : node.prevs) {
			if (nodes.containsKey(id)) {
				if (nodes.get(id).getClass().isAssignableFrom(c)) {
					return nodes.get(id);
				}
			}
		}
		return null;
	}

	public DrawNode nodeConnectToNextType(DrawNode node, Class<? extends DrawNode> c) {
		for (String id : node.nexts) {
			if (nodes.containsKey(id)) {
				if (nodes.get(id).getClass().isAssignableFrom(c)) {
					return nodes.get(id);
				}
			}
		}
		return null;
	}

	List<MouseMotionListener> mouseMotionListeners = new ArrayList<>();

	public void addNodeMouseMotionListener(MouseMotionListener listener) {
		if (mouseMotionListeners.indexOf(listener) == -1)
			mouseMotionListeners.add(listener);
	}

	public void removeNodeMouseMotionListener(MouseMotionListener listener) {
		mouseMotionListeners.remove(listener);
	}

	List<MouseListener> mouseListeners = new ArrayList<>();

	public void addNodeMouseListener(MouseListener listener) {
		if (mouseListeners.indexOf(listener) == -1)
			mouseListeners.add(listener);
	}

	public void removeNodeMouseListener(MouseListener listener) {
		mouseListeners.remove(listener);
	}

	List<KeyListener> keyListeners = new ArrayList<>();

	public void addNodeKeyListener(KeyListener listener) {
		if (keyListeners.indexOf(listener) == -1)
			keyListeners.add(listener);
	}

	public void removeNodeKeyListener(KeyListener listener) {
		keyListeners.remove(listener);
	}

	protected void udpateLinkNodes(List<String> list, String oldid, String newid) {
		for (int i = list.size() - 1; i >= 0; i--) {
			String id = list.get(i);
			if (!nodes.containsKey(id)) {
				list.remove(i);
				continue;
			}

			if (id.compareTo(oldid) == 0) {
				list.set(i, newid);
			}

		}
	}

	protected void udpateLinkNodes(DrawNode node, String oldid, String newid) {
		for (String id : node.getPrevs()) {
			udpateLinkNodes(nodes.get(id).getNexts(), oldid, newid);
		}
		for (String id : node.getNexts()) {
			udpateLinkNodes(nodes.get(id).getPrevs(), oldid, newid);
		}
	}

	public void updateID(String oldid, String newid) {
		DrawNode node = nodes.get(oldid);
		for (int i = node.prevs.size() - 1; i >= 0; i--) {
			String id = node.prevs.get(i);
			DrawNode tmp = nodes.get(id);
			tmp.nexts.remove(oldid);
			tmp.nexts.add(newid);
		}
		for (int i = node.nexts.size() - 1; i >= 0; i--) {
			String id = node.nexts.get(i);
			DrawNode tmp = nodes.get(id);
			tmp.prevs.remove(oldid);
			tmp.prevs.add(newid);
		}

		nodes.remove(oldid);
		node.id = newid;
		nodes.put(newid, node);

		udpateLinkNodes(node, oldid, newid);
		repaint();
	}

	public boolean fixNode(DrawNode node) {
		boolean b = false;
		for (int i = node.prevs.size() - 1; i >= 0; i--) {
			String id = node.prevs.get(i);
			if (nodes.containsKey(id) && id.compareToIgnoreCase(node.id) != 0) {
				DrawNode tmp = nodes.get(id);
				if (tmp.nexts.indexOf(node.id) == -1) {
					tmp.nexts.add(node.id);
					b = true;
				}
			} else {
				node.prevs.remove(i);
				b = true;
			}
		}
		for (int i = node.nexts.size() - 1; i >= 0; i--) {
			String id = node.nexts.get(i);
			if (nodes.containsKey(id) && id.compareToIgnoreCase(node.id) != 0) {
				DrawNode tmp = nodes.get(id);
				if (tmp.prevs.indexOf(node.id) == -1) {
					tmp.prevs.add(node.id);
					b = true;
				}
			} else {
				node.nexts.remove(i);
				b = true;
			}
		}

		for (Polyline line : lines.values()) {
			if (line.start.id.compareTo(node.id) == 0)
				line.start = node;
			if (line.end.id.compareTo(node.id) == 0)
				line.end = node;
		}
		return b;
	}

	public static class Polyline {

		List<Integer> xs = new ArrayList<>();
		List<Integer> ys = new ArrayList<>();

		boolean isAddPoint = false;
		Point addPoint = null;

		DrawNode start;
		DrawNode end;

		DrawCanvas canvas;

		String getHashKey() {
			return getHashKey(start, end);
		}

		public boolean ptInPolyline(Rectangle rect) {
			updateBeginEndPoint();

			Rectangle2D advRect = (Rectangle2D) rect;
			for (int i = 0; i < xs.size() - 1; i++) {
				Line2D.Float line = new Line2D.Float((float) xs.get(i), (float) ys.get(i), (float) xs.get(i + 1),
						(float) ys.get(i + 1));

				if (advRect.intersectsLine(line))
					return true;

				// Line line = new Line(new IntersectPoint(xs.get(i),
				// ys.get(i)),
				// new IntersectPoint(xs.get(i + 1), ys.get(i + 1)));
				// IntersectPointHelp.IntersectPoint inPoint =
				// IntersectPointHelp.intersection(line, rect);
				// if (inPoint != null) {
				// return true;
				// }
			}

			return false;

		}

		public boolean ptInPolyline(Point pt) {
			pt = canvas.getRealPoint(pt);
			for (int i = 0; i < xs.size() - 1; i++) {
				Line line = new Line(new IntersectPoint(xs.get(i), ys.get(i)),
						new IntersectPoint(xs.get(i + 1), ys.get(i + 1)));
				if (Line.intersection(line, pt)) {
					return true;
				}
			}

			return false;
		}

		public boolean removePoint(Point pt) {
			pt = canvas.getRealPoint(pt);
			for (int i = 0; i < xs.size() - 1; i++) {
				Point rectPt = new Point(xs.get(i), ys.get(i));
				Rectangle rectangle = IntersectPointHelp.getTestRect(rectPt);
				if (rectangle.contains(pt)) {
					xs.remove(i);
					ys.remove(i);
					return true;
				}
			}

			return false;
		}

		public static String getHashKey(DrawNode start, DrawNode end) {
			return start.id + end.id;
		}

		boolean check(DrawNode vStart, DrawNode vEnd) {
			return vStart == start && vEnd == end;
		}

		protected Polyline(DrawCanvas canvas) {
			this.canvas = canvas;
		}

		public Polyline(DrawCanvas canvas, DrawNode start, DrawNode end) {
			this.canvas = canvas;
			this.start = start;
			this.end = end;

			Point startPt = start.getCenter();
			Point endPt = end.getCenter();
			xs.add(startPt.x);
			xs.add(endPt.x);
			ys.add(startPt.y);
			ys.add(endPt.y);

		}

		public JSONObject toJson() throws JSONException {
			JSONObject jsonObject = new JSONObject();

			jsonObject.put("start", start.id);
			jsonObject.put("end", end.id);

			JSONArray xJson = new JSONArray();
			for (int i = 0; i < xs.size(); i++) {
				xJson.put(xs.get(i));
			}
			jsonObject.put("xs", xJson);
			JSONArray yJson = new JSONArray();
			for (int i = 0; i < ys.size(); i++) {
				yJson.put(ys.get(i));
			}
			jsonObject.put("ys", yJson);
			return jsonObject;
		}

		public static Polyline fromJson(DrawCanvas canvas, JSONObject data) throws JSONException {
			Polyline line = new Polyline(canvas);
			JSONArray xJson = data.getJSONArray("xs");
			line.xs.clear();
			for (int i = 0; i < xJson.length(); i++) {
				line.xs.add(xJson.getInt(i));
			}
			line.ys.clear();
			JSONArray yJson = data.getJSONArray("ys");
			for (int i = 0; i < xJson.length(); i++) {
				line.ys.add(yJson.getInt(i));
			}

			String id = data.getString("start");
			if (!canvas.nodes.containsKey(id))
				return null;
			line.start = canvas.nodes.get(id);

			id = data.getString("end");
			if (!canvas.nodes.containsKey(id))
				return null;
			line.end = canvas.nodes.get(id);

			return line;
		}

		int resetIndex = -1;

		protected ResizeButtonType getResizeButtonType(Point pt) {
			resetIndex = -1;
			for (int i = 0; i < resetButtons.size(); i++) {
				Rectangle rectangle = resetButtons.get(i);
				if (rectangle.contains(pt)) {
					resetIndex = i + 1;
					return ResizeButtonType.rtCustom;
				}
			}

			return ResizeButtonType.rtNone;
		}

		public void mouseReleased(MouseEvent e) {
			if (isAddPoint) {
				for (int i = 0; i < ys.size() - 1; i++) {
					IntersectPoint p1 = new IntersectPoint((double) xs.get(i), (double) ys.get(i));
					IntersectPoint p2 = new IntersectPoint((double) xs.get(i + 1), (double) ys.get(i + 1));

					Line line = new Line(p1, p2);

					IntersectPoint intersectPoint = IntersectPointHelp.intersection(line,
							IntersectPointHelp.getTestRect(addPoint));
					if (intersectPoint != null) {
						xs.add(i + 1, (int) intersectPoint.x);
						ys.add(i + 1, (int) intersectPoint.y);
						canvas.fireChange(ChangeType.ctLineChanged);
						canvas.repaint();
						break;
					}
				}
			}

			isAddPoint = false;
			addPoint = null;
		}

		public void mousePressed(MouseEvent e) {
			if (e.isControlDown()) {
				if (!removePoint(e.getPoint())) {
					isAddPoint = true;
					addPoint = canvas.getRealPoint(e.getPoint());
				}
			} else {

			}
		}

		public void mouseMoved(MouseEvent e) {
			if (canvas.isMouseDown && canvas.curRt == ResizeButtonType.rtCustom && resetIndex != -1) {
				Point pt = canvas.getRealPoint(e.getPoint());
				xs.set(resetIndex, pt.x);
				ys.set(resetIndex, pt.y);
				canvas.fireChange(ChangeType.ctLineChanged);
				canvas.repaint();
			}
			// else{
			// if (canvas.isMouseDown && resetIndex == -1)
			// canvas.repaint();
			// }
		}

		protected void updateBeginEndPoint() {
			Point startPt = start.getCenter();
			Point endPt = end.getCenter();
			xs.set(0, startPt.x);
			ys.set(0, startPt.y);
			xs.set(xs.size() - 1, endPt.x);
			ys.set(xs.size() - 1, endPt.y);
		}

		protected void drawPolyline(Graphics g) {
			int[] xPoints = new int[xs.size()];
			int[] yPoints = new int[ys.size()];
			updateBeginEndPoint();
			for (int i = 0; i < yPoints.length; i++) {
				xPoints[i] = xs.get(i);
				yPoints[i] = ys.get(i);
			}

			Graphics2D graphics2d = (Graphics2D) g;
			graphics2d.setStroke(new BasicStroke(1.5f));
			g.drawPolyline(xPoints, yPoints, xPoints.length);
		}

		protected void drawNode(Graphics g) {
			drawPolyline(g);
		}

		List<Rectangle> resetButtons = new ArrayList<>();

		protected void darwResizeButtons(Graphics g) {
			resetButtons.clear();
			g.setColor(Color.BLUE);
			for (int i = 1; i < xs.size() - 1; i++) {
				int x = xs.get(i);
				int y = ys.get(i);
				int div = 5;
				Rectangle rectangle = new Rectangle(x - div, y - div, div * 2, div * 2);
				resetButtons.add(rectangle);
				g.fill3DRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height, true);
			}
		}

	}

	public class AlignControl {
		public void left() {
			if (selectNodes.size() < 2)
				return;

			getAcm().pushCommand(selectNodes, CommandInfoType.ctMove);
			Integer left = null;
			for (DrawNode dragNode : selectNodes) {
				Rectangle rect = dragNode.getRect();
				if (left == null)
					left = rect.x;
				else {
					rect.x = left;
					dragNode.updateRect(new DrawNode.UpdateType[] { DrawNode.UpdateType.utLeft });
				}
			}
			fireChange(ChangeType.ctMove);
			repaint();
		}

		public void right() {
			if (selectNodes.size() < 2)
				return;

			getAcm().pushCommand(selectNodes, CommandInfoType.ctMove);
			Integer right = null;
			for (DrawNode dragNode : selectNodes) {
				Rectangle rect = dragNode.getRect();
				if (right == null)
					right = rect.x + rect.width;
				else {
					rect.x = right - rect.width;
					dragNode.updateRect(new DrawNode.UpdateType[] { DrawNode.UpdateType.utLeft });
				}
			}
			fireChange(ChangeType.ctMove);
			repaint();
		}

		public void top() {
			if (selectNodes.size() < 2)
				return;

			getAcm().pushCommand(selectNodes, CommandInfoType.ctMove);
			Integer top = null;
			for (DrawNode dragNode : selectNodes) {
				Rectangle rect = dragNode.getRect();
				if (top == null)
					top = rect.y;
				else {
					rect.y = top;
					dragNode.updateRect(new DrawNode.UpdateType[] { DrawNode.UpdateType.utTop });
				}
			}
			fireChange(ChangeType.ctMove);
			repaint();
		}

		public void bottom() {
			if (selectNodes.size() < 2)
				return;

			getAcm().pushCommand(selectNodes, CommandInfoType.ctMove);
			Integer bottom = null;
			for (DrawNode dragNode : selectNodes) {
				Rectangle rect = dragNode.getRect();
				if (bottom == null)
					bottom = rect.y + rect.height;
				else {
					rect.y = bottom - rect.height;
					dragNode.updateRect(new DrawNode.UpdateType[] { DrawNode.UpdateType.utTop });
				}
			}
			fireChange(ChangeType.ctMove);
			repaint();
		}

		public void leftStretch() {
			if (selectNodes.size() < 2)
				return;

			getAcm().pushCommand(selectNodes, CommandInfoType.ctMoveAndResize);
			Integer left = null;
			for (DrawNode dragNode : selectNodes) {
				Rectangle rect = dragNode.getRect();
				if (left == null)
					left = rect.x;
				else {
					rect.width += rect.x - left;
					rect.x = left;
					dragNode.updateRect(
							new DrawNode.UpdateType[] { DrawNode.UpdateType.utLeft, DrawNode.UpdateType.utWidth });
				}
			}
			fireChange(ChangeType.ctMove);
			fireChange(ChangeType.ctResize);
			repaint();
		}

		public void rightStretch() {
			if (selectNodes.size() < 2)
				return;

			getAcm().pushCommand(selectNodes, CommandInfoType.ctMoveAndResize);
			Integer right = null;
			for (DrawNode dragNode : selectNodes) {
				Rectangle rect = dragNode.getRect();
				if (right == null)
					right = rect.x + rect.width;
				else {
					rect.width += right - (rect.x + rect.width);
					rect.x = right - rect.width;
					dragNode.updateRect(
							new DrawNode.UpdateType[] { DrawNode.UpdateType.utLeft, DrawNode.UpdateType.utWidth });
				}
			}
			fireChange(ChangeType.ctMove);
			fireChange(ChangeType.ctResize);
			repaint();
		}

		public void topStretch() {
			if (selectNodes.size() < 2)
				return;

			getAcm().pushCommand(selectNodes, CommandInfoType.ctMoveAndResize);
			Integer top = null;
			for (DrawNode dragNode : selectNodes) {
				Rectangle rect = dragNode.getRect();
				if (top == null)
					top = rect.y;
				else {
					rect.height += rect.y - top;
					rect.y = top;
					dragNode.updateRect(
							new DrawNode.UpdateType[] { DrawNode.UpdateType.utTop, DrawNode.UpdateType.utHeight });
				}
			}
			fireChange(ChangeType.ctMove);
			fireChange(ChangeType.ctResize);
			repaint();
		}

		public void bottomStretch() {
			if (selectNodes.size() < 2)
				return;

			getAcm().pushCommand(selectNodes, CommandInfoType.ctMoveAndResize);
			Integer bottom = null;
			for (DrawNode dragNode : selectNodes) {
				Rectangle rect = dragNode.getRect();
				if (bottom == null)
					bottom = rect.y + rect.height;
				else {
					rect.height += bottom - (rect.y + rect.height);
					rect.y = bottom - rect.height;
					dragNode.updateRect(
							new DrawNode.UpdateType[] { DrawNode.UpdateType.utTop, DrawNode.UpdateType.utHeight });
				}
			}
			fireChange(ChangeType.ctMove);
			fireChange(ChangeType.ctResize);
			repaint();
		}

		public void hDiv() {
			if (selectNodes.size() < 2)
				return;

			getAcm().pushCommand(selectNodes, CommandInfoType.ctMove);
			TreeMap<Integer, DrawNode> nodes = new TreeMap<>();

			for (DrawNode dragNode : selectNodes) {
				Rectangle rect = dragNode.getRect();
				nodes.put(rect.x, dragNode);
			}

			boolean asc = true;
			List<DrawNode> sorts = new ArrayList<>(nodes.values());
			// if (sorts.get(0) == selectNodes.get(selectNodes.size() - 1)){
			// sorts = new ArrayList<>(nodes.descendingMap().values());
			// asc = false;
			// }

			Integer div = null;
			for (int i = 1; i < sorts.size(); i++) {
				DrawNode node1 = sorts.get(i - 1);
				DrawNode node2 = sorts.get(i);
				if (div == null)
					div = node2.getRect().x - (node1.getRect().x + node1.getRect().width);
				else {
					node2.getRect().x = node1.getRect().x
							+ (asc ? node1.getRect().width + div : -node2.getRect().width - div);
					node2.updateRect(new DrawNode.UpdateType[] { DrawNode.UpdateType.utLeft });
				}
			}
			fireChange(ChangeType.ctMove);
			repaint();
		}

		public void vDiv() {
			if (selectNodes.size() < 2)
				return;

			getAcm().pushCommand(selectNodes, CommandInfoType.ctMove);
			TreeMap<Integer, DrawNode> nodes = new TreeMap<>();

			for (DrawNode dragNode : selectNodes) {
				Rectangle rect = dragNode.getRect();
				nodes.put(rect.y, dragNode);
			}

			boolean asc = true;
			List<DrawNode> sorts = new ArrayList<>(nodes.values());
			// if (sorts.get(0) == selectNodes.get(selectNodes.size() - 1)){
			// sorts = new ArrayList<>(nodes.descendingMap().values());
			// asc = false;
			// }

			Integer div = null;
			for (int i = 1; i < sorts.size(); i++) {
				DrawNode node1 = sorts.get(i - 1);
				DrawNode node2 = sorts.get(i);
				if (div == null)
					div = node2.getRect().y - (node1.getRect().y + node1.getRect().height);
				else {
					node2.getRect().y = node1.getRect().y
							+ (asc ? node1.getRect().height + div : -node2.getRect().height - div);
					node2.updateRect(new DrawNode.UpdateType[] { DrawNode.UpdateType.utTop });
				}
			}
			fireChange(ChangeType.ctMove);
			repaint();
		}

		public void equalWidth() {
			if (selectNodes.size() < 2)
				return;

			getAcm().pushCommand(selectNodes, CommandInfoType.ctResize);
			Integer width = null;
			for (DrawNode dragNode : selectNodes) {
				Rectangle rect = dragNode.getRect();
				if (width == null)
					width = rect.width;
				else {
					rect.width = width;
					dragNode.updateRect(new DrawNode.UpdateType[] { DrawNode.UpdateType.utWidth });
				}
			}
			fireChange(ChangeType.ctResize);
			repaint();
		}

		public void vCenter() {
			if (selectNodes.size() < 2)
				return;

			getAcm().pushCommand(selectNodes, CommandInfoType.ctMove);
			Integer div = null;
			for (DrawNode dragNode : selectNodes) {
				Rectangle rect = dragNode.getRect();
				if (div == null)
					div = rect.x + rect.width / 2;
				else {
					rect.x = div - rect.width / 2;
					dragNode.updateRect(new DrawNode.UpdateType[] { DrawNode.UpdateType.utLeft });
				}
			}
			fireChange(ChangeType.ctMove);
			repaint();
		}

		public void hCenter() {
			if (selectNodes.size() < 2)
				return;

			getAcm().pushCommand(selectNodes, CommandInfoType.ctMove);
			Integer div = null;
			for (DrawNode dragNode : selectNodes) {
				Rectangle rect = dragNode.getRect();
				if (div == null)
					div = rect.y + rect.height / 2;
				else {
					rect.y = div - rect.height / 2;
					dragNode.updateRect(new DrawNode.UpdateType[] { DrawNode.UpdateType.utTop });
				}
			}
			fireChange(ChangeType.ctMove);
			repaint();
		}

		public void equalHeight() {
			if (selectNodes.size() < 2)
				return;

			getAcm().pushCommand(selectNodes, CommandInfoType.ctResize);
			Integer height = null;
			for (DrawNode dragNode : selectNodes) {
				Rectangle rect = dragNode.getRect();
				if (height == null)
					height = rect.height;
				else {
					rect.height = height;
					dragNode.updateRect(new DrawNode.UpdateType[] { DrawNode.UpdateType.utHeight });
				}
			}
			fireChange(ChangeType.ctResize);
			repaint();
		}

		public void align(String alignText) {
			switch (alignText.trim()) {
			case "顶部对齐":
				top();
				break;
			case "底部对齐":
				bottom();
				break;
			case "左部对齐":
				left();
				break;
			case "右部对齐":
				right();
				break;
			case "顶部拉齐":
				topStretch();
				break;
			case "底部拉齐":
				bottomStretch();
				break;
			case "左部拉齐":
				leftStretch();
				break;
			case "右部拉齐":
				rightStretch();
				break;
			case "横向分布":
				hDiv();
				break;
			case "纵向分布":
				vDiv();
				break;
			case "等宽":
				equalWidth();
				break;
			case "等高":
				equalHeight();
				break;
			case "纵向剧中":
				vCenter();
				break;
			case "横向居中":
				hCenter();
				break;
			default:
				break;
			}
		}
	}

	public AlignControl alignControl = new AlignControl();

	protected void fixMoveX(Point pt, FixType fixType) {
		Rectangle clip = getClipRect();
		if (clip.x >= pt.x) {
			return;
		}

		if (fixType != FixType.ftNone) {
			List<Integer> indexs = new ArrayList<>(autoCols.keySet());
			for (int i = 0; i < indexs.size(); i++) {
				switch (fixType) {
				case ftXLeft:
					if (indexs.get(i) > pt.x) {
						if (i == 0)
							return;

						pt.x = indexs.get(i - 1);
						return;
					}
					break;
				case ftXRight:
					if (indexs.get(i) > pt.x) {
						pt.x = indexs.get(i);
						return;
					}
					break;
				default:
					break;
				}
			}
		}
	}

	protected void fixMoveY(Point pt, FixType fixType) {
		Rectangle clip = getClipRect();
		if (clip.y >= pt.y) {
			return;
		}

		if (fixType != FixType.ftNone) {
			List<Integer> indexs = new ArrayList<>(autoRows.keySet());
			for (int i = 0; i < indexs.size(); i++) {
				switch (fixType) {
				case ftYTop:
					if (indexs.get(i) > pt.y) {
						if (i == 0)
							return;

						pt.y = indexs.get(i - 1);
						return;
					}
					break;
				case ftYBottom:
					if (indexs.get(i) > pt.y) {
						pt.y = indexs.get(i);
						return;
					}
					break;
				default:
					break;
				}
			}
		}

	}

	protected void fixMove(Point pt, FixType[] fixTypes) {
		fixMoveX(pt, fixTypes[0]);
		fixMoveY(pt, fixTypes[1]);
	}

	protected void drawAutoGrid(Graphics graphics) {
		Graphics2D g = (Graphics2D) graphics;
		// Stroke dot = new BasicStroke(1f, BasicStroke.CAP_BUTT,
		// BasicStroke.JOIN_ROUND, 3.5f, new float[]{15,10}, 0f);
		Stroke dot = new BasicStroke(0.5f);
		g.setStroke(dot);
		g.setColor(new Color(128, 128, 128));

		Rectangle clip = getClipRect();

		int colCount = clip.width / lineDiv;
		int rowCount = clip.height / lineDiv;

		int left = clip.x + lineDiv;
		int top = clip.y;

		autoCols.clear();
		autoRows.clear();

		for (int i = 0; i < colCount; i++) {
			autoCols.put(left, new Point(left, top));
			g.drawLine(left, top, left, clip.y + clip.height);
			left += lineDiv;
		}

		left = clip.x;
		top = clip.y + lineDiv;
		for (int i = 0; i < rowCount; i++) {
			autoRows.put(top, new Point(left, top));
			g.drawLine(left, top, left + clip.width, top);
			top += lineDiv;
		}

	}

	protected List<DrawNode> getRealNodes(Collection<DrawNode> selectNodes) {
		HashMap<String, DrawNode> nodes = new HashMap<>();
		HashMap<String, String> stackElements = new HashMap<>();
		for (DrawNode drawNode : selectNodes) {
			if (stackTreeManager.elements.containsKey(drawNode.id))
				stackElements.put(drawNode.id, drawNode.id);
			nodes.put(drawNode.id, drawNode);
		}

		for (String id : new ArrayList<>(stackElements.values())) {
			if (stackTreeManager.inPath(id, stackElements.keySet())) {
				nodes.remove(id);
				stackElements.remove(id);
			}
		}

		return new ArrayList<>(nodes.values());
	}

	/***
	 * 完成选中控件的拖拽动作 首先判断是否有选中控件，如果没有直接退出
	 * 有则先移动x，y指定的位移，然后检查控件是否没有父容器，如果没有检查是否不是层叠的根容器，如果不是则从层叠中移除
	 * 然后检查是否开启了表格对齐，如果开启则对齐表格 触发移动事件通知
	 * 
	 * @param x
	 * @param y
	 */
	protected void moveAndDraw(int x, int y) {
		DrawNode fixNode = null;
		FixType[] fixTypes = null;
		if (selectNodes.size() > 0) {
			List<DrawNode> realNodes = getRealNodes(selectNodes);
			for (int i = realNodes.size() - 1; i >= 0; i--) {
				DrawNode node = realNodes.get(i);
				FixType[] tmps = node.move(node, x, y);
				if (stackTreeManager.getParent(node.id) == null) {
					if (!stackTreeManager.roots.containsKey(node.id)) {
						stackTreeManager.remove(node.id);
					}
				}
				if (fixNode == null) {
					fixNode = node;
					fixTypes = tmps;
				}
			}
		} else {
			return;
		}

		Point fixSize = new Point(0, 0);
		if (pageConfig.checkConfig(Config.ccFixGrid) && fixNode != null && selectNodes.size() == 1) {
			Point point = fixNode.getRect().getLocation();
			fixSize.x = point.x;
			fixSize.y = point.y;

			fixMove(point, fixTypes);
			fixNode.getRect().setLocation(point);

			fixSize.x = point.x - fixSize.x;
			fixSize.y = point.y - fixSize.y;
		}
		oldP = new Point(x + fixSize.x, y + fixSize.y);

		fireChange(ChangeType.ctMove);
		repaint();
	}

	protected void resizeAndDraw(int x, int y) {
		List<DrawNode> realNodes = getRealNodes(selectNodes);
		for (DrawNode node : realNodes) {
			node.resize(x, y);
		}

		oldP = new Point(x, y);

		fireChange(ChangeType.ctResize);
		repaint();
	}

	protected Rectangle getConstraintRect() {
		Rectangle result = new Rectangle();
		if (useRect.x > 0) {
			result.x = useRect.x;
		} else
			result.x = 0;

		if (canvasPageSize.width > getWidth()) {
			result.width = canvasPageSize.width;
		} else
			result.width = useRect.width;

		if (useRect.y > 0) {
			result.y = useRect.y;
		} else
			result.y = 0;

		if (canvasPageSize.height > getHeight()) {
			result.height = canvasPageSize.height;
		} else
			result.height = useRect.height;

		return result;
	}

	protected void checkConstraint(Rectangle offset) {
		if (!useCheckConstraint)
			return;

		Rectangle clip = getConstraintRect();
		if (offset.x < clip.x)
			offset.x = clip.x;
		else if (offset.x + offset.width > clip.x + clip.width)
			offset.x = clip.x + clip.width - offset.width;

		if (offset.y < clip.y)
			offset.y = clip.y;
		else if (offset.y + offset.height > clip.y + clip.height)
			offset.y = clip.y + clip.height - offset.height;
	}

	protected boolean checkSizeConstraint(Rectangle offset, ResizeButtonType rt) {
		boolean b = true;

		Rectangle clip = getConstraintRect();

		if (offset.x < clip.x) {
			switch (rt) {
			case rtLeft:
			case rtLeftBottom:
			case rtLeftTop:
				offset.width -= clip.x - offset.x;
			case rtRightBottom:
			case rtRightTop:
			default:
				offset.x = clip.x;
				break;
			}
			b = false;
		}

		if (offset.y < clip.y) {
			switch (rt) {
			case rtTop:
			case rtRightTop:
			case rtLeftTop:
				offset.height -= clip.y - offset.y;
			case rtRightBottom:
			case rtLeftBottom:
			default:
				offset.y = clip.y;
				break;
			}
			b = false;
		}

		if (offset.width < MIN_WIDTH) {
			switch (rt) {
			case rtLeft:
			case rtLeftBottom:
			case rtLeftTop:
				offset.x -= MIN_WIDTH - offset.width;
			case rtRightBottom:
			case rtRightTop:
			default:
				offset.width = MIN_WIDTH;
				break;
			}
			b = false;
		} else if (offset.x + offset.width > clip.x + clip.width) {
			int maxWidth = clip.x + clip.width - offset.x;
			switch (rt) {
			case rtRightBottom:
			case rtRightTop:
			case rtLeftBottom:
			case rtLeftTop:
			default:
				offset.width = maxWidth;
				break;
			}
			b = false;
		}
		if (offset.height < MIN_HEIGHT) {
			switch (rt) {
			case rtTop:
			case rtLeftTop:
			case rtRightTop:
				offset.y -= MIN_HEIGHT - offset.height;
			case rtLeftBottom:
			case rtRightBottom:
			default:
				offset.height = MIN_HEIGHT;
				break;
			}
			b = false;
		} else if (offset.y + offset.height > clip.y + clip.height) {
			int maxHeight = clip.y + clip.height - offset.y;
			switch (rt) {
			case rtLeftTop:
			case rtRightTop:
			case rtLeftBottom:
			case rtRightBottom:
			default:
				offset.height = maxHeight;
				break;
			}
			b = false;
		}

		return b;
	}

	public Rectangle getClipRect() {
		final int boardWidth = (int) Math.round(LINEWIDTH + 0.5);

		if (useRect == null)
			return new Rectangle();

		Point start = getRealPoint(useRect.getLocation());
		return new Rectangle(start.x + boardWidth, start.y + boardWidth, useRect.width - boardWidth * 2,
				useRect.height - boardWidth * 2);
	}

	public Point getRealPoint(Point point) {
		Point pt = new Point(point);
		pt.x -= offset.x;
		pt.y -= offset.y;
		return pt;
	}

	public Point getVirtualPoint(Point point) {
		Point pt = new Point(point);
		pt.x -= offset.x;
		pt.y -= offset.y;
		return pt;
	}

	public DrawNode NodeOfPoint(Point point) {
		point = getRealPoint(point);
		DrawNode rnode = null;
		for (DrawNode node : nodes.values()) {
			if (node.isPoint(point)) {
				int zOrder = node.zOrder;
				if (stackTreeManager.elements.containsKey(node.id)) {
					if (!stackTreeManager.isVisiable(node.id))
						continue;

					zOrder = stackTreeManager.getParentZOrder(node.id);
				}
				if (rnode == null)
					rnode = node;
				else {
					if (rnode.zOrder < zOrder)
						rnode = node;
				}
			}
		}

		if (rnode != null && stackTreeManager.elements.containsKey(rnode.id)) {
			DrawNode node = stackTreeManager.NodeOfPoint(point);
			if (node != null)
				return node;
		}
		return rnode;
	}

	public interface INode {
		public void Click(DrawNode node);

		public void DoubleClick(DrawNode node);

		public void onChange(DrawNode[] nodes, ChangeType ct);

		default void onAdvanChange(DrawNode node, ChangeType ct, Object data) {

		}
	}

	public boolean selectLine(Point pt) {
		HashMap<String, Polyline> selects = new HashMap<>();
		pt = getRealPoint(pt);
		Rectangle rect = IntersectPointHelp.getTestRect(new IntersectPointHelp.IntersectPoint(pt.x, pt.y));
		for (Polyline polyline : lines.values()) {
			if (polyline.ptInPolyline(rect)) {
				selects.put(polyline.getHashKey(), polyline);
			}
		}

		selectedLines.clear();
		List<Polyline> lines = new ArrayList<>(selects.values());
		for (Polyline selectedLine : lines) {
			if (selectedLines.size() == 0)
				selectedLines.add(selectedLine);
			else {
				int maxX = Math.abs(selectedLine.start.getRect().x - selectedLine.end.getRect().x);
				int maxY = Math.abs(selectedLine.start.getRect().y - selectedLine.end.getRect().y);
				if (maxX > maxY) {
					int x1 = Math.max(Math.abs(selectedLine.start.getRect().x - pt.x),
							Math.abs(selectedLine.end.getRect().x - pt.x));
					int x2 = Math.max(Math.abs(selectedLines.get(0).start.getRect().x - pt.x),
							Math.abs(selectedLines.get(0).end.getRect().x - pt.x));
					if (x1 < x2) {
						selectedLines.clear();
						selectedLines.add(selectedLine);
					}
				} else {
					int x1 = Math.max(Math.abs(selectedLine.start.getRect().y - pt.y),
							Math.abs(selectedLine.end.getRect().y - pt.y));
					int x2 = Math.max(Math.abs(selectedLines.get(0).start.getRect().y - pt.y),
							Math.abs(selectedLines.get(0).end.getRect().y - pt.y));
					if (x1 < x2) {
						selectedLines.clear();
						selectedLines.add(selectedLine);
					}
				}
			}
		}
		repaint();
		return selectedLines.size() > 0;
	}

	protected String getSelectNodeText() {
		try {
			if (selectNodes.size() > 0) {
				JSONArray data = new JSONArray();
				for (DrawNode node : selectNodes) {
					data.put(node.toJson());
				}
				return data.toString();
			} else
				return "";
		} catch (Exception e) {
			return "";
		}
	}

	public void keyPressed(KeyEvent e) {
		KeyListener[] listeners = getKeyListeners();
		if (listeners != null) {
			for (KeyListener keyListener : listeners) {
				keyListener.keyPressed(e);
			}
		}
	}

	public void keyReleased(KeyEvent e) {
		KeyListener[] listeners = getKeyListeners();
		if (listeners != null) {
			for (KeyListener keyListener : listeners) {
				keyListener.keyReleased(e);
			}
		}
	}

	public void copy(boolean all) {

		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();// 获取系统剪切板
		String str;
		if (all)
			try {
				str = toJson().toString();
			} catch (JSONException e) {
				str = "";
			}
		else {
			str = getSelectNodeText();
		}
		StringSelection selection = new StringSelection(str);// 构建String数据类型
		clipboard.setContents(selection, selection);// 添加文本到系统剪切板
	}

	public enum CopyType {
		ctTitle, ctName, ctID, ctAll
	}

	public void copySelectNodeToClipboard(CopyType ct) {
		DrawNode node = getSelected();
		if (node == null) {
			return;
		}
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();// 获取系统剪切板
		String str = null;

		switch (ct) {
		case ctAll:
			str = getSelectNodeText();
			break;
		case ctID:
			str = node.id;
			break;
		case ctName:
			str = node.name;
			break;
		case ctTitle:
			str = node.title;
			break;
		}

		if (str == null || str.isEmpty())
			return;

		StringSelection selection = new StringSelection(str);// 构建String数据类型
		clipboard.setContents(selection, selection);// 添加文本到系统剪切板
		JOptionPane.showInternalMessageDialog(getParent(), "文本已经拷贝到剪贴板，您可以通过【粘贴】完成后续操作！");
	}

	public void postFireChange(ChangeType ct) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				fireChange(ct);
			}
		});
	}

	protected void fireChange(ChangeType ct) {
		fireChange(getSelecteds(), ct);
	}

	protected void fireChange(DrawNode node, ChangeType ct) {
		fireChange(new DrawNode[] { node }, ct);
	}

	public void fireChange(Collection<DrawNode> nodes, ChangeType ct) {
		fireChange(nodes.toArray(new DrawNode[nodes.size()]), ct);
	}

	protected void fireChange(DrawNode[] nodes, ChangeType ct) {
		if ((nodes == null || nodes.length == 0)) {
			switch (ct) {
			case ctDeselected:
			case ctBackEdited:
				break;
			default:
				return;
			}
		}

		if (nodeEvent != null)
			nodeEvent.onChange(nodes, ct);
	}

	public void fireChange(DrawNode node, ChangeType ct, Object data) {
		if (nodeEvent != null)
			nodeEvent.onAdvanChange(node, ct, data);
	}

	public void changeNodeId(DrawNode node, String newId) throws IOException {
		remove(node);
		node.id = newId;
		addNode(node, false, false);
		fixNode(node);
	}

	protected void changePasteNode(DrawNode node) throws IOException {
		String oldid = node.id;
		node.id = UUID.randomUUID().toString();

		node.name = node.id;
		node.changeid(oldid, node.id);

		node.prevs.clear();
		node.nexts.clear();
	}

	protected boolean allowPaste(DrawNode node) {
		return true;
	}

	protected void endPaste(List<DrawNode> nodes) {

	}

	public List<DrawNode> paste() {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();// 获取系统剪切板
		Transferable content = clipboard.getContents(null);// 从系统剪切板中获取数据
		if (content.isDataFlavorSupported(DataFlavor.stringFlavor)) {// 判断是否为文本类型
			String text;
			try {
				text = (String) content.getTransferData(DataFlavor.stringFlavor);
				// 从数据中获取文本值
				if (text == null || text.isEmpty() || !text.trim().startsWith("[")) {
					return null;
				}

				JSONArray jsonArray = new JSONArray(text);

				List<DrawNode> nodes = fromJson(this, jsonArray, getLastCreateUserDataSerializable());

				if (nodes.size() == 0)
					return nodes;

				getAcm().pushCommand(nodes, CommandInfoType.ctAdd);

				List<DrawNode> notifies = new ArrayList<>();
				for (DrawNode drawNode : nodes) {
					if (!allowPaste(drawNode))
						continue;

					changePasteNode(drawNode);

					this.nodes.put(drawNode.id, drawNode);

					drawNode.pasted();

					notifies.add(drawNode);
				}

				endPaste(nodes);

				fireChange(nodes, ChangeType.ctPaste);

				setSelects(notifies, true, true);

				repaint();
				return nodes;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	protected void popuMenu(Point pt) {
		if (menu != null) {
			Point point = pt;
			menu.setVisible(true);
			menu.setLocation(point.x, point.y);
		}
	}

	protected void needRequestFocus() {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				if (!DrawCanvas.this.hasFocus())
					requestFocus();
			}
		});
	}

	public void fireMouseRelease(MouseEvent e) {
		for (DrawNode snode : getSelecteds()) {
			snode.mouseListener.mouseReleased(e);
		}

		if (getSelected() != null)
			selectedLines.clear();
		else {
			for (Polyline line : selectedLines) {
				line.mouseReleased(e);
			}
		}
		for (MouseListener listener : mouseListeners) {
			listener.mouseReleased(e);
		}
	}

	protected boolean linkingDrawNode(DrawNode start, DrawNode end) {
		return true;
	}

	public void refreshDrawTree(List<DrawNode> nodes, boolean autoAdd) {
		stackTreeManager.resetTree(nodes, autoAdd);
	}

	public void removeLink(Polyline line) {
		getAcm().pushCommand(new DrawNode[] { line.start, line.end }, CommandInfoType.ctRemoveLink);
		line.start.nexts.remove(line.end.id);
		line.end.prevs.remove(line.start.id);
		lines.remove(line.getHashKey());
		fireChange(new DrawNode[] { line.start, line.end }, ChangeType.ctRemoveLink);

	}

	public boolean linkTo(DrawNode start, DrawNode end) {
		if (start != null && start.id.compareTo(end.id) != 0) {
			if (start.nexts.indexOf(end.id) == -1) {
				if (linkingDrawNode(start, end)) {
					getAcm().pushCommand(new DrawNode[] { start, end }, CommandInfoType.ctLink);

					start.nexts.add(end.id);
					end.prevs.add(start.id);

					fireChange(new DrawNode[] { start, end }, ChangeType.ctAddLink);

					return true;
				}
			} else {
				EditorEnvironment.showMessage(null, "同一节点仅可以有一条同向目的节点连线！", "已经存在连线", JOptionPane.WARNING_MESSAGE);
			}
		}

		return false;
	}

	public boolean isMuiltSelecting() {
		switch (getMouseMode()) {
		case mmResize:
		case mmDrag:
			return selectNodes.size() > 1;
		case mmLink:
		case mmMuileSelect:
		case mmRectSelect:
		case mmRemoveLink:
			return true;
		default:
			// if (selectNodes.size() > 1)
			// return true;
			return false;
		}
	}

	protected PageConfig getPageConfigInstance() {
		return new PageConfig();
	}

	protected void setKeyEventListener() {
		addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				for (DrawNode node : getSelecteds()) {
					node.keyListener.keyTyped(e);
				}
				for (KeyListener listener : keyListeners) {
					listener.keyTyped(e);
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				getAcm().end(keyMoveP);

				keyMoveP = null;

				isCtrlDown = false;
				isAltDown = false;
				keypressed = false;
				for (DrawNode node : getSelecteds()) {
					node.keyListener.keyReleased(e);
				}

				for (KeyListener listener : keyListeners) {
					listener.keyReleased(e);
				}

				fireChange(ChangeType.ctKeyUp);

				if (e.getKeyCode() == KeyEvent.VK_ALT) {
					e.consume();
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
				try {
					if (e.getKeyCode() == KeyEvent.VK_DELETE && pageConfig.checkConfig(Config.ccAllowEdit)) {
						remove();
						return;
					}

					if (e.isControlDown()) {
						switch (e.getKeyCode()) {
						case KeyEvent.VK_A:
							if (pageConfig.checkConfig(Config.ccAllowMulSelect))
								setSelecteds(nodes.values().toArray(new DrawNode[nodes.size()]));
							needRequestFocus();
							return;
						case KeyEvent.VK_C:
							copy(false);
							needRequestFocus();
							return;
						case KeyEvent.VK_V:
							if (pageConfig.checkConfig(Config.ccAllowEdit))
								paste();
							needRequestFocus();
							return;
						case KeyEvent.VK_Z:
							getAcm().popCommand();
							needRequestFocus();
							return;
						default:
							isCtrlDown = true;
							break;
						}
					} else if (e.isAltDown()) {
						isAltDown = true;
						return;
					}

					if (getSelected() == null)
						return;

					if (!keypressed) {
						keypressed = true;
					}

					if (pageConfig.checkConfig(Config.ccAllowDrag)) {
						if (oldP == null) {
							oldP = new Point(getSelected().getRect().getLocation());
							oldFixP = new Point(oldP);
						}

						int step = e.isControlDown() ? 5 : 1;
						boolean isKey = true;
						switch (e.getKeyCode()) {
						case KeyEvent.VK_LEFT:
							keyMoveP = new Point(oldP.x - step, oldP.y);
							break;
						case KeyEvent.VK_RIGHT:
							keyMoveP = new Point(oldP.x + step, oldP.y);
							break;
						case KeyEvent.VK_UP:
							keyMoveP = new Point(oldP.x, oldP.y - step);
							break;
						case KeyEvent.VK_DOWN:
							keyMoveP = new Point(oldP.x, oldP.y + step);
							break;
						default:
							isKey = false;
							break;
						}

						if (isKey) {
							getAcm().startPush(keyMoveP, null, CommandInfoType.ctNone);
							needRequestFocus();
							getAcm().pushCommand(getSelecteds(), CommandInfoType.ctMove);
							moveAndDraw(keyMoveP.x, keyMoveP.y);
						}
					}
				} finally {
					for (DrawNode node : getSelecteds()) {
						node.keyListener.keyPressed(e);
					}
					for (KeyListener listener : keyListeners) {
						listener.keyPressed(e);
					}
				}

			}
		});
	}

	protected void setMouseEventListener() {
		addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent evt) {
				try {
					DrawNode node = NodeOfPoint(evt.getPoint());
					if (nodeEvent != null && node != null) {
						nodeEvent.Click(node);
					}

					if (evt.getClickCount() == 2) {
						if (nodeEvent != null && node != null) {
							nodeEvent.DoubleClick(node);
						}
					}
				} finally {
					for (DrawNode snode : getSelecteds()) {
						snode.mouseListener.mouseClicked(evt);
					}
					for (MouseListener listener : mouseListeners) {
						listener.mouseClicked(evt);
					}
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				Point endPoint = e.getPoint();
				try {
					isMouseDown = false;

					if (e.getButton() == MouseEvent.BUTTON3)
						return;

					boolean needFire = false;
					DrawNode node = NodeOfPoint(e.getPoint());

					switch (getMouseMode()) {
					case mmLink:
						if (node != null) {
							end = node;
							needFire = linkTo(start, end);
							start = null;
							end = null;
						}
						break;
					case mmRemoveLink:
						selectLine(e.getPoint());
						if (selectedLines.size() > 0) {
							for (Polyline line : selectedLines) {
								removeLink(line);
							}
							selectedLines.clear();
							needFire = true;
						}
						break;

					case mmResize:
						setCursor(Cursor.getDefaultCursor());
						needFire = true;
						setMouseMode(MouseMode.mmNone);
						// refreshDrawTree(selectNodes);
						break;
					case mmRectSelect:
						if (getSelected() != null)
							fireChange(ChangeType.ctSelecteds);
						else {
							fireChange(ChangeType.ctDeselected);
						}
						needFire = true;
						setMouseMode(MouseMode.mmNone);
						// refreshDrawTree(selectNodes);
						break;
					case mmDrag:
						needFire = true;
						setMouseMode(MouseMode.mmNone);
						break;
					case mmMuileSelect:
					case mmNone:
						setMouseMode(MouseMode.mmNone);
						break;
					default:
						break;
					}
					oldP = null;
					oldFixP = null;
					newP = null;
					startP = null;

					if (needFire) {
						fireChange(ChangeType.ctMouseRelease);
					}
					repaint();

				} finally {
					getAcm().end(endPoint);
					fireMouseRelease(e);
				}
			}

			@Override
			public void mousePressed(MouseEvent e) {
				firstSelected = false;
				try {
					DrawCanvas.this.requestFocus();
					if (e.getButton() == MouseEvent.BUTTON3) {
						popuMenu(e.getLocationOnScreen());
						return;
					}

					getAcm().startPush(e.getPoint(), null, CommandInfoType.ctNone);

					oldP = new Point(e.getPoint());
					startP = new Point(oldP);
					oldFixP = new Point(oldP);
					oldOffset = new Point(offset);

					if (e.getButton() == MouseEvent.BUTTON1) {
						isMouseDown = true;
					}

					curRt = checkResizeButton(e.getPoint());
					DrawNode selectNode = null;
					if (curRt != ResizeButtonType.rtNone) {
						if (!pageConfig.checkConfig(Config.ccAllowResize))
							return;

						setMouseMode(MouseMode.mmResize);
					} else {
						selectNode = NodeOfPoint(e.getPoint());
						firstSelected = selectNodes.indexOf(selectNode) == -1;
					}

					if (selectNode == null && getMouseMode() == MouseMode.mmLink) {
						return;
					}

					switch (getMouseMode()) {
					case mmLink:
						if (!pageConfig.checkConfig(Config.ccLink)) {
							setMouseMode(MouseMode.mmNone);
							return;
						}

						start = selectNode;
						DrawCanvas.this.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
						break;
					case mmRemoveLink:
						if (!pageConfig.checkConfig(Config.ccAllowEdit)) {
							setMouseMode(MouseMode.mmNone);
							return;
						}

						return;
					case mmResize:
						if (!pageConfig.checkConfig(Config.ccAllowResize)) {
							setMouseMode(MouseMode.mmNone);
							return;
						}

						getAcm().pushCommand(getSelecteds(), CommandInfoType.ctResize);
						break;
					default:
						if (getMouseMode() != MouseMode.mmCustom)
							setMouseMode(e.isControlDown() ? MouseMode.mmMuileSelect
									: (selectNode == null || e.isAltDown() ? MouseMode.mmRectSelect
											: MouseMode.mmDrag));
						switch (getMouseMode()) {
						case mmDrag:
							getAcm().pushCommand(getSelecteds(), CommandInfoType.ctMove);
							break;
						case mmMuileSelect:
						case mmRectSelect:
							if (!pageConfig.checkConfig(Config.ccAllowMulSelect)) {
								setMouseMode(MouseMode.mmNone);
								return;
							}
							getAcm().pushCommand(getSelecteds(), CommandInfoType.ctDeselected);
						default:
							break;
						}

						if (selectNode == null) {
							clearSelect(true);
							if (selectLine(e.getPoint()))
								setMouseMode(MouseMode.mmNone);

						} else {
							switch (getMouseMode()) {
							case mmMuileSelect:
								if (selectNode != null) {
									addOrRemoveSelect(selectNode);
								}
								setMouseMode(MouseMode.mmNone);
								break;

							default:
								if (getMouseMode() != MouseMode.mmCustom) {
									if (!pageConfig.checkConfig(Config.ccAllowSelect)) {
										setMouseMode(MouseMode.mmNone);
										return;
									}

									boolean needSetSelectOne = true;
									switch (getMouseMode()) {
									case mmDrag:
										needSetSelectOne = selectNodes.size() < 2;
										if (!needSetSelectOne) {
											needSetSelectOne = selectNodes.indexOf(selectNode) == -1;
										}
										break;
									case mmMuileSelect:
										needSetSelectOne = false;
										break;
									default:
										break;
									}

									if (needSetSelectOne) {
										List<DrawNode> tmps = new ArrayList<>();
										tmps.add(selectNode);
										setSelects(tmps, true, true);
									} else if (!pageConfig.checkConfig(Config.ccAllowDrag)) {
										setMouseMode(MouseMode.mmNone);
										return;
									}
								}
								break;
							}

						}
						break;
					}

					repaint();
				} finally {
					for (DrawNode snode : getSelecteds()) {
						snode.mouseListener.mousePressed(e);
					}

					if (getSelected() != null)
						selectedLines.clear();
					else {
						Point point = getRealPoint(e.getPoint());
						for (Polyline line : selectedLines) {
							curRt = line.getResizeButtonType(point);
							line.mousePressed(e);
						}
					}
					for (MouseListener listener : mouseListeners) {
						listener.mousePressed(e);
					}
				}

			}
		});

		addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseMoved(MouseEvent e) {
				setCurCursor(Cursor.getDefaultCursor());
				try {
					if (e.getButton() == MouseEvent.BUTTON3)
						return;

					if (isCtrlDown && isMouseDown) {
						int x = oldOffset.x + (int) (e.getX() - oldP.getX());
						int y = oldOffset.y + (int) (e.getY() - oldP.getY());
						setOffset(new Point(x, y));
						return;
					}

					if (getMouseMode() == MouseMode.mmNone) {
						ResizeButtonType rt = checkResizeButton(e.getPoint());
						if (rt != ResizeButtonType.rtNone) {
							switch (rt) {
							case rtLeft:
								setCurCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
								break;
							case rtRight:
								setCurCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
								break;
							case rtBottom:
								setCurCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
								break;
							case rtTop:
								setCurCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
								break;
							case rtLeftBottom:
								setCurCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
								break;
							case rtLeftTop:
								setCurCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
								break;
							case rtRightBottom:
								setCurCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
								break;
							case rtRightTop:
								setCurCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
								break;
							default:
								break;
							}
							// }else if (NodeOfPoint(e.getPoint()) != null){
							// setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
						} else {
						}
					}

					switch (getMouseMode()) {
					case mmResize:
						resizeAndDraw(e.getX(), e.getY());
						break;
					case mmDrag:
						if (!firstSelected) {
							moveAndDraw(e.getX(), e.getY());
							refreshDrawTree(selectNodes, true);
						}
						break;
					case mmRectSelect:
						newP = e.getPoint();
						Rectangle rect = getSelectRect();

						List<DrawNode> selects = new ArrayList<>();
						for (DrawNode node : nodes.values()) {
							if (node.getRect() == null)
								continue;

							if (rect.contains(node.getRect()))
								selects.add(node);
						}

						setSelects(selects, true, false);
						repaint();
						break;
					case mmLink:
						newP = e.getPoint();
						repaint();
						break;
					default:
						break;
					}

				} finally {
					for (DrawNode snode : getSelecteds()) {
						snode.mouseMotionListener.mouseMoved(e);
					}

					if (getSelected() != null)
						selectedLines.clear();
					else {
						Point point = getRealPoint(e.getPoint());
						for (Polyline line : selectedLines) {
							line.mouseMoved(e);
							ResizeButtonType rt = line.getResizeButtonType(point);
							if (rt == ResizeButtonType.rtCustom) {
								setCurCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
							}
						}
					}

					for (MouseMotionListener listener : mouseMotionListeners) {
						listener.mouseMoved(e);
					}

					setCursor(getCurCursor());
				}
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				mouseMoved(e);
				for (MouseMotionListener listener : mouseMotionListeners) {
					listener.mouseDragged(e);
				}
			}
		});
	}

	public DrawCanvas() {
		super();
		setAcm(new ActionCommandManager(this));
		pageConfig = getPageConfigInstance();

		this.setFocusable(true);
		this.setEnabled(true);
		stackTreeManager = new StackTreeManager(this);

		setKeyEventListener();
		setMouseEventListener();

	}

	public interface IJsonObject {
		public JSONObject toJson() throws JSONException;

		public void fromJson(JSONObject json, ICreateNodeSerializable createUserDataSerializable) throws JSONException;
	}

	public static class IntersectPointHelp {

		public static Rectangle getTestRect(Point p) {
			return new Rectangle(p.x - 5, p.y - 5, 10, 10);
		}

		public static Rectangle getTestRect(IntersectPoint p) {
			return new Rectangle((int) p.x - 5, (int) p.y - 5, 10, 10);
		}

		public static class IntersectPoint {
			double x;
			double y;

			public IntersectPoint(double x, double y) {
				this.x = x;
				this.y = y;
			}

			public java.awt.Point toPoint() {
				return new java.awt.Point((int) x, (int) y);
			}
		}

		public static boolean checkIntersection(Line start, Rectangle rect) {
			Rectangle2D advRect = (Rectangle2D) rect;
			Line2D.Double line = new Line2D.Double(start.a.x, start.a.y, start.b.x, start.b.y);

			return advRect.intersectsLine(line);
		}

		public static IntersectPoint intersection(Line start, Rectangle rect) {
			Line line = new Line(new IntersectPoint(rect.x, rect.y), new IntersectPoint(rect.x + rect.width, rect.y));
			IntersectPoint result = null;
			IntersectPoint topResult = null;
			IntersectPoint bottomResult = null;
			IntersectPoint rightResult = null;
			IntersectPoint leftResult = null;
			result = Line.intersection(start, line);
			if (result != null) {
				if (rect.contains(result.x, result.y))
					topResult = result;
			}

			line = new Line(new IntersectPoint(rect.x, rect.y), new IntersectPoint(rect.x, rect.y + rect.height));
			result = Line.intersection(start, line);
			if (result != null) {
				if (rect.contains(result.x, result.y))
					leftResult = result;
			}

			line = new Line(new IntersectPoint(rect.x, rect.y + rect.height),
					new IntersectPoint(rect.x + rect.width, rect.y + rect.height));
			result = Line.intersection(start, line);
			if (result != null) {
				if (rect.contains(result.x, result.y - 1))
					bottomResult = result;
			}

			line = new Line(new IntersectPoint(rect.x + rect.width, rect.y + rect.height),
					new IntersectPoint(rect.x + rect.width, rect.y));
			result = Line.intersection(start, line);
			if (result != null) {
				if (rect.contains(result.x - 1, result.y))
					rightResult = result;
			}

			if (start.a.x < start.b.x && start.a.y < start.b.y) {// 左上
				if (leftResult != null) {
					return leftResult;
				} else
					return topResult;
			} else if (start.a.x > start.b.x && start.a.y < start.b.y) {// 右上
				if (rightResult != null) {
					return rightResult;
				} else
					return topResult;
			} else if (start.a.x < start.b.x && start.a.y > start.b.y) {// 左下
				if (bottomResult != null) {
					return bottomResult;
				} else
					return leftResult;
			} else if (start.a.x > start.b.x && start.a.y > start.b.y) {// 右下
				if (bottomResult != null) {
					return bottomResult;
				} else
					return rightResult;
			} else if (start.a.x == start.b.x && start.a.y < start.b.y) {// 中上
				return topResult;
			} else if (start.a.x == start.b.x && start.a.y > start.b.y) {// 中下
				return bottomResult;
			} else if (start.a.x < start.b.x && start.a.y == start.b.y) {// 中左
				return leftResult;
			} else if (start.a.x > start.b.x && start.a.y == start.b.y) {// 中右
				return rightResult;
			}
			return null;
		}

		public static class Line {
			IntersectPoint a;
			IntersectPoint b;

			public Line(IntersectPoint a, IntersectPoint b) {
				this.a = a;
				this.b = b;
			}

			// 求两直线的交点，斜率相同的话res=u.a
			public static boolean intersection(Line u, Point pt) {
				boolean pdline = (pt.x - u.a.x) * (u.a.y - u.b.y) == (u.a.x - u.b.x) * (pt.y - u.a.y);
				return pdline;
			}

			public static IntersectPoint intersection(Line u, Line v) {
				try {
					IntersectPoint res = new IntersectPoint(u.a.x, u.a.y);
					double t = ((u.a.x - v.a.x) * (v.b.y - v.a.y) - (u.a.y - v.a.y) * (v.b.x - v.a.x))
							/ ((u.a.x - u.b.x) * (v.b.y - v.a.y) - (u.a.y - u.b.y) * (v.b.x - v.a.x));
					res.x += (u.b.x - u.a.x) * t;
					res.y += (u.b.y - u.a.y) * t;
					return res;
				} catch (Throwable t) {
					return null;
				}
			}
		}
	}

	public interface IDataSerializable {
		public String save(Object userData);

		public Object load(String value);

		public DrawNode newDrawNode(Object userdata);

		public void initDrawNode(DrawNode node);
	}

	public interface ICreateNodeSerializable {
		public IDataSerializable getUserDataSerializable(DrawNode node);

		public DrawNode newDrawNode(JSONObject json);
	}

	public Rectangle getSelectRect() {
		Point start = getRealPoint(newP);
		Point end = getRealPoint(oldP);
		int x, y = 0;
		if (newP.x > oldP.x) {
			x = end.x;
		} else
			x = start.x;

		if (newP.y > oldP.y) {
			y = end.y;
		} else
			y = start.y;

		Rectangle rect = new Rectangle(x, y, Math.abs(end.x - start.x), Math.abs(end.y - start.y));
		return rect;

	}

	public void setFile(File f) {
		canvasFile = f;
	}

	public void setNodes(List<DrawNode> nodes) {
		if (getWidth() == 0 || getHeight() == 0) {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					setNodes(nodes);
				}
			});

			return;
		}

		try {
			loadFromNodes(nodes, new ICustomLoad() {

				@Override
				public void onLoaded() {
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			EditorEnvironment.showException(e);
			return;
		}
	}

	public File getFile() {
		return canvasFile;
	}

	public void save() throws Exception {
		if (canvasFile != null) {
			File dir = canvasFile.getParentFile();
			if (!dir.exists())
				if (!dir.mkdirs()) {
					throw new IOException("mkdir is fail!");
				}

			JSONObject json = saveToJson();

			JsonHelp.saveJson(canvasFile, json, null);
		}
	}

	public JSONObject saveToJson() throws Exception {
		JSONObject json = new JSONObject();
		pageConfig.toJson(json);
		JSONArray data = toJson();
		json.put("nodes", data);

		if (lines.size() > 0) {
			JSONArray jsonlines = new JSONArray();
			for (Polyline line : lines.values()) {
				JSONObject object = line.toJson();
				jsonlines.put(object);
			}

			json.put("lines", jsonlines);
		}

		JSONObject treedata = stackTreeManager.toJson();
		json.put("drawtree", treedata);

		return json;
	}

	public void load(ICreateNodeSerializable createUserDataSerializable, IInitPage onInitPage, boolean clear)
			throws Exception {
		if (canvasFile == null || !canvasFile.exists()) {
			return;
		}

		JSONObject data = loadJsonFromFile(canvasFile);

		loadFromJson(data, createUserDataSerializable, onInitPage, clear);
	}

	public void loadFromJson(JSONObject data, ICreateNodeSerializable createUserDataSerializable, IInitPage onInitPage,
			boolean clear) throws Exception {

		setLastCreateUserDataSerializable(createUserDataSerializable);

		LoadNodeInfo info = null;
		JSONObject stackTreeInfo = null;

		if (data != null) {
			info = load(this, data, getLastCreateUserDataSerializable(), new ILoad() {

				@Override
				public void onBeforeLoad(JSONObject data, Object param) throws JSONException {
					pageConfig.fromJson(data);
					if (onInitPage != null) {
						onInitPage.onPage(pageConfig);
					}
				}
			}, new IFixLoadNode() {

				@Override
				public void fix(DrawNode node) {
					fixLoadNode(node);
				}
			}, null);

			if (data.has("drawtree"))
				stackTreeInfo = data.getJSONObject("drawtree");

		}

		loadFromLoadNodeInfo(info, stackTreeInfo, clear, null);
	}

	public interface ICustomLoad {
		void onLoaded();
	}

	public void loadFromNodes(List<DrawNode> nodes, ICustomLoad onLoad) throws Exception {
		loadFromNodes(nodes, null, true, onLoad);
	}

	public void loadFromNodes(List<DrawNode> nodes, JSONObject stackTreeInfo, boolean clear, ICustomLoad onLoad)
			throws Exception {
		LoadNodeInfo info = new LoadNodeInfo();
		info.nodes = nodes;
		info.json = null;

		loadFromLoadNodeInfo(info, stackTreeInfo, clear, onLoad);
	}

	public void loadFromLoadNodeInfo(LoadNodeInfo info, JSONObject stackTreeInfo, boolean clear, ICustomLoad onLoad)
			throws Exception {

		if (clear) {
			clear();
		}
		offset = new Point(0, 0);
		getAcm().reset();

		if (info == null || info.nodes == null || info.nodes.size() == 0) {
			repaint();
			return;
		}

		beginPaint();

		if (info.nodes != null) {
			for (DrawNode drawNode : info.nodes) {
				drawNode.canvas = this;
				nodes.put(drawNode.id, drawNode);
			}
		}

		if (info.json != null && info.json.has("lines")) {
			JSONArray jsonArray = info.json.getJSONArray("lines");
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject object = jsonArray.getJSONObject(i);
				Polyline line = Polyline.fromJson(this, object);
				if (line != null)
					lines.put(line.getHashKey(), line);
			}
		}

		if (checkNodes())
			save();

		if (stackTreeInfo != null){
			stackTreeManager.fromJson(stackTreeInfo);
			for (DrawNode node : nodes.values()) {
				if (stackTreeManager.elements.containsKey(node.id)){
					stackTreeManager.elements.get(node.id).location = node.getRect().getLocation();
				}
			}
		}

		stackTreeManager.checkNodes();

		if (onLoad == null)
			onLoaded();
		else
			onLoad.onLoaded();

		endPaint();

	}

	public void load(ICreateNodeSerializable createUserDataSerializable, IInitPage onInitPage) throws Exception {
		load(createUserDataSerializable, onInitPage, true);
	}

	public interface ILoad {
		public void onBeforeLoad(JSONObject json, Object param) throws JSONException;
	}

	public interface IInitPage {
		public void onPage(PageConfig pageConfig);
	}

	protected boolean checkNodes() {
		boolean b = false;
		for (DrawNode node : nodes.values()) {
			if (!b)
				b = fixNode(node);
		}

		return b;
	}

	public void clear() {
		oldP = null;
		oldFixP = null;
		newP = null;

		start = null;
		end = null;
		selectNodes.clear();
		selectedLines.clear();

		curRt = ResizeButtonType.rtNone;

		getAcm().reset();

		lines.clear();
		stackTreeManager.clear();

		for (DrawNode node : nodes.values()) {
			node.removed();
		}

		nodes.clear();
	}

	public void fixNodesInPage() {
		int minleft = Integer.MAX_VALUE;
		int minTop = Integer.MAX_VALUE;

		for (DrawNode node : nodes.values()) {
			Rectangle rect = node.getRect();
			if (minleft > rect.x)
				minleft = rect.x;

			if (minTop > rect.y)
				minTop = rect.y;
		}

		if (minleft < useRect.x) {
			int div = useRect.x - minleft + 5;
			for (DrawNode node : nodes.values()) {
				Rectangle rect = node.getRect();
				rect.x = rect.x + div;
			}
			fireChange(ChangeType.ctMove);
		}

		if (minTop < useRect.y) {
			int div = useRect.y - minTop + 5;
			for (DrawNode node : nodes.values()) {
				Rectangle rect = node.getRect();
				rect.y = rect.y + div;
			}
			fireChange(ChangeType.ctMove);
		}

		repaint();
	}

	public interface ILoaded {
		public void onloaded();
	}

	public ILoaded onLoadedEvent;

	protected void onLoaded() {
		for (DrawNode node : nodes.values()) {
			Rectangle rect = node.getRect();
			if (node.relativeToPage == null || rect == null)
				continue;
			rect.x = node.relativeToPage.x + useRect.x;
			rect.y = node.relativeToPage.y + useRect.y;
		}
		if (onLoadedEvent != null)
			onLoadedEvent.onloaded();
	}

	protected void fixLoadNode(DrawNode node) {

	}

	public interface IFixLoadNode {
		public void fix(DrawNode node);
	}

	public static class LoadNodeInfo {
		List<DrawNode> nodes;
		JSONObject json;
	}

	public static List<DrawNode> loadNodes(DrawCanvas canvas, File workflowFile,
			ICreateNodeSerializable createUserDataSerializable, ILoad onLoad, IFixLoadNode fixLoadNode, Object param)
			throws Exception {
		LoadNodeInfo info = load(canvas, workflowFile, createUserDataSerializable, onLoad, fixLoadNode, param);
		return info.nodes;
	}

	public static JSONObject loadJsonFromFile(File file) throws Exception {
		try {
			return (JSONObject) JsonHelp.parseJson(file, null);
		} catch (Exception e) {
			Runtime.getRuntime()
					.exec("rundll32 SHELL32.DLL,ShellExec_RunDLL " + "Explorer.exe /select," + file.getAbsolutePath());
			throw e;
		}
	}

	public static LoadNodeInfo load(DrawCanvas canvas, File workflowFile,
			ICreateNodeSerializable createUserDataSerializable, ILoad onLoad, IFixLoadNode fixLoadNode, Object param)
			throws Exception {
		if (workflowFile == null || !workflowFile.exists()) {
			return new LoadNodeInfo();
		}

		JSONObject json = loadJsonFromFile(workflowFile);

		return load(canvas, json, createUserDataSerializable, onLoad, fixLoadNode, param);
	}

	public static LoadNodeInfo load(DrawCanvas canvas, JSONObject json,
			ICreateNodeSerializable createUserDataSerializable, ILoad onLoad, IFixLoadNode fixLoadNode, Object param)
			throws Exception {
		LoadNodeInfo info = new LoadNodeInfo();

		info.json = json;

		if (onLoad != null)
			onLoad.onBeforeLoad(info.json, param);

		if (info.json.has("nodes")) {
			JSONArray nodedata = info.json.getJSONArray("nodes");
			info.nodes = fromJson(canvas, nodedata, createUserDataSerializable);
			if (fixLoadNode != null) {
				for (DrawNode node : info.nodes) {
					fixLoadNode.fix(node);
				}
			}
		}
		return info;
	}

	public JSONArray toJson() throws JSONException {
		JSONArray jsonArray = new JSONArray();
		for (DrawNode node : nodes.values()) {
			jsonArray.put(node.toJson());
		}
		return jsonArray;
	}

	public static List<DrawNode> fromJson(DrawCanvas canvas, JSONArray json,
			ICreateNodeSerializable createDataSerializable) throws Exception {
		List<DrawNode> tmps = new ArrayList<>();

		for (int i = 0; i < json.length(); i++) {
			JSONObject data = json.getJSONObject(i);
			try {
				DrawNode node = DrawNode.fromJson(canvas, data, createDataSerializable);
				tmps.add(node);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return tmps;
	}

	public Rectangle getLocalRect(Rectangle rectangle) {
		Point point = rectangle.getLocation();
		point.x -= useRect.x;
		point.y -= useRect.y;
		return new Rectangle(point.x, point.y, (int) rectangle.getWidth(), (int) rectangle.getHeight());
	}

	public Rectangle getPageRectangle(Rectangle rect) {
		Point point = getPageLocation(rect.getLocation());
		return new Rectangle(point.x, point.y, rect.width, rect.height);
	}

	public Point getPageLocation(Point pt) {
		Point point = useRect.getLocation();
		// point = getRealPoint(point);
		return new Point(point.x + pt.x, point.y + pt.y);
	}

	public Point getVirtualLocation(Point pt) {
		Point point = useRect.getLocation();
		pt = getVirtualPoint(pt);
		return new Point(pt.x - point.x, pt.y - point.y);
	}

	public DrawNode add(String name, String title, Rectangle rect, Object userData,
			IDataSerializable dataSerializable) {
		DrawNode node = dataSerializable.newDrawNode(userData);
		if (rect != null) {
			node.setRect(getPageRectangle(rect));
			node.getRect().setLocation(getRealPoint(node.getRect().getLocation()));
		}
		node.title = (title == null || title.isEmpty()) ? name : title;
		if (name != null && !name.isEmpty()) {
			node.id = name;
			node.name = name;
		}
		node.userData = userData;
		node.userDataSerializable = dataSerializable;
		nodes.put(node.id, node);
		bringToTop(node);

		dataSerializable.initDrawNode(node);

		getAcm().pushCommand(node, CommandInfoType.ctAdd);
		fireChange(new DrawNode[] { node }, ChangeType.ctAdd);
		repaint();
		return node;
	}

	public void removeAndHint() {
		if (getSelecteds().size() == 0)
			return;

		if (EditorEnvironment.showConfirmDialog("是否删除选定的项目？", "删除", JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION)
			return;

		remove();
	}

	public void remove() {
		remove(selectNodes, true, true, true);
		repaint();
	}

	public void remove(String id) {
		remove(nodes.get(id));
	}

	public void remove(DrawNode node) {
		remove(node, true, true, true);
	}

	public void addNode(DrawNode node, boolean needPush, boolean needNotify) {
		node.canvas = this;
		nodes.put(node.id, node);
		if (needPush) {
			getAcm().pushCommand(node, CommandInfoType.ctAdd);
		}

		if (needNotify)
			fireChange(node, ChangeType.ctAdd);
	}

	public void remove(DrawNode node, boolean needRepaint, boolean needPushCommand, boolean needNotify) {
		if (node != null) {
			remove(new DrawNode[] { node }, needRepaint, needPushCommand, needNotify);
		}
	}

	protected DrawNode[] getRealRemoveNodes(DrawNode[] removedNodes) {
		HashMap<String, DrawNode> result = new HashMap<>();
		for (DrawNode drawNode : removedNodes) {
			if (drawNode == null)
				continue;

			result.put(drawNode.id, drawNode);
			List<StatckTreeElement> elements = new ArrayList<>();
			stackTreeManager.getChilds(drawNode.id, elements);
			for (StatckTreeElement statckTreeElement : elements) {
				if (nodes.containsKey(statckTreeElement.id))
					result.put(statckTreeElement.id, nodes.get(statckTreeElement.id));
				else {
					stackTreeManager.elements.remove(statckTreeElement.id);
					stackTreeManager.roots.remove(statckTreeElement.id);
				}

			}
		}

		return result.size() == 0 ? null : result.values().toArray(new DrawNode[result.values().size()]);
	}

	protected void remove(Collection<DrawNode> removedNodes, boolean needRepaint, boolean needPushCommand,
			boolean needNotify) {
		if (removedNodes == null || removedNodes.size() == 0)
			return;

		remove(removedNodes.toArray(new DrawNode[removedNodes.size()]), needRepaint, needPushCommand, needNotify);
	}

	public void remove(DrawNode[] removedNodes, boolean needRepaint, boolean needPushCommand, boolean needNotify) {
		remove(removedNodes, needRepaint, needPushCommand, needNotify, true);
	}

	public void remove(DrawNode[] removedNodes, boolean needRepaint, boolean needPushCommand, boolean needNotify,
			boolean removeRef) {
		if (removedNodes == null || removedNodes.length == 0)
			return;

		removedNodes = getRealRemoveNodes(removedNodes);
		if (removedNodes == null)
			return;

		if (needPushCommand)
			getAcm().pushCommand(removedNodes, CommandInfoType.ctRemove);

		HashMap<String, List<Polyline>> lineHash = new HashMap<>();
		for (Polyline line : lines.values()) {
			List<Polyline> list;
			if (!lineHash.containsKey(line.start.id)) {
				list = new ArrayList<>();
				lineHash.put(line.start.id, list);
			} else
				list = lineHash.get(line.start.id);
			list.add(line);

			if (!lineHash.containsKey(line.end.id)) {
				list = new ArrayList<>();
				lineHash.put(line.end.id, list);
			} else
				list = lineHash.get(line.end.id);
			list.add(line);

		}

		for (DrawNode drawNode : removedNodes) {
			nodes.remove(drawNode.id);
			drawNode.removed();
			stackTreeManager.removeTree(drawNode.id, true);
			for (DrawNode node : nodes.values()) {
				if (removeRef) {
					node.nexts.remove(drawNode.id);
					node.prevs.remove(drawNode.id);
				}
				if (lineHash.containsKey(drawNode.id)) {
					for (Polyline line : lineHash.get(drawNode.id)) {
						String key = line.getHashKey();
						if (lines.containsKey(key))
							lines.remove(key);
					}
				}
			}
			stackTreeManager.remove(drawNode.id);
		}

		if (needNotify)
			fireChange(removedNodes, ChangeType.ctRemove);

		if (needRepaint)
			repaint();

	}

	public DrawNode getSelected() {
		if (selectNodes.size() > 0)
			return selectNodes.get(selectNodes.size() - 1);
		else
			return null;
	}

	public List<DrawNode> getSelecteds() {
		return new ArrayList<>(selectNodes);
	}

	public void setSelecteds(DrawNode[] nodes) {
		setSelects(Arrays.asList(nodes), true, true);
		repaint();
	}

	public DrawNode getNode(String id) {
		if (!nodes.containsKey(id))
			return null;

		return nodes.get(id);
	}

	public List<DrawNode> getNodes() {
		return new ArrayList<>(nodes.values());
	}

	public enum EditMode {
		emNormal, emLink, emRemoveLink
	}

	public void setEditMode(EditMode mode) {
		oldP = null;
		newP = null;
		switch (mode) {
		case emNormal:
			setMouseMode(MouseMode.mmNone);
			setCursor(Cursor.getDefaultCursor());
			break;
		case emLink:
			setMouseMode(MouseMode.mmLink);
			setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			break;
		case emRemoveLink:
			setMouseMode(MouseMode.mmRemoveLink);
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			break;
		}
	}

	public IScroll onScroll;

	public void setOffset(DrawNode node, boolean center) {
		Rectangle viewRect = getViewPortRect();
		if (node.needShow(viewRect))
			return;

		Rectangle r = node.getRect();
		Point point = r.getLocation();
		point = getRealPoint(point);

		point.x = -(point.x - viewRect.x - (center ? (useRect.width - r.width) / 2 : 0));
		point.y = -(point.y - viewRect.y - (center ? (useRect.height - r.height) / 2 : 0));
		setOffset(point);
	}

	public void setOffset(Point offset) {
		this.offset = offset;
		if (this.offset.x > 0)
			this.offset.x = 0;
		if (this.offset.y > 0)
			this.offset.y = 0;

		Point max = getMaxOffset();
		this.offset.x = -Math.min(max.x, Math.abs(this.offset.x));
		this.offset.y = -Math.min(max.y, Math.abs(this.offset.y));

		if (onScroll != null) {
			onScroll.onScroll(this.offset.x, this.offset.y);
		}
		repaint();
	}

	public Point getOffset() {
		return offset;
	}

	protected int mmToPixel(int mm) {
		return (int) (mm / 25.4 * getToolkit().getScreenResolution());
	}

	public Rectangle getViewPortRect() {
		return new Rectangle(-offset.x + useRect.x, -offset.y + useRect.y, (int) useRect.getWidth(),
				(int) useRect.getHeight());
	}

	public interface IOnPageSizeChanged {
		void onChanged(Point max);
	}

	public IOnPageSizeChanged onPageSizeChanged;

	public static String getTitle(JSONObject json) {
		return getJsonValue(json, "title");
	}

	public static String getMemo(JSONObject json) {
		return getJsonValue(json, "memo");
	}

	protected static String getJsonValue(JSONObject json, String key) {
		if (json.has(key))
			try {
				return json.getString(key);
			} catch (JSONException e) {
				e.printStackTrace();
				return "";
			}
		else
			return "";
	}

	public static PageSizeMode StringToPageSize(String text) {

		switch (Arrays.asList(PAGENAMES).indexOf(text)) {
		case 0:
			return PageSizeMode.psA4H;
		case 1:
			return PageSizeMode.psA4V;
		case 2:
			return PageSizeMode.psA3H;
		case 3:
			return PageSizeMode.psA3V;
		case 4:
			return PageSizeMode.psA2H;
		case 5:
			return PageSizeMode.psA2V;
		case 6:
			return PageSizeMode.psA1H;
		case 7:
			return PageSizeMode.psA1V;
		case 8:
			return PageSizeMode.ps720p;
		case 9:
			return PageSizeMode.ps1080P;
		case 10:
			return PageSizeMode.ps2k;
		case 11:
			return PageSizeMode.psWXGAP;
		case 12:
			return PageSizeMode.psWSXGA;
		case 13:
			return PageSizeMode.psWUXGA;
		case 14:
			return PageSizeMode.psWQXGA;
		case 15:
			return PageSizeMode.psQVGA;
		case 16:
			return PageSizeMode.psVGA;
		case 17:
			return PageSizeMode.psSVGA;
		case 18:
			return PageSizeMode.psXGA;
		case 19:
			return PageSizeMode.psqHD;
		case 20:
			return PageSizeMode.psWXGA;
		case 21:
			return PageSizeMode.ps5P420;
		case 22:
			return PageSizeMode.ps5P440;
		case 23:
			return PageSizeMode.ps55P560;
		case 24:
			return PageSizeMode.ps6P560;
		case 25:
			return PageSizeMode.ps6PX560;
		case 26:
			return PageSizeMode.ps63P560;
		case 27:
			return PageSizeMode.ps7P213;
		case 28:
			return PageSizeMode.ps7P320;
		case 29:
			return PageSizeMode.ps89P320;
		case 30:
			return PageSizeMode.ps99P320;
		case 31:
			return PageSizeMode.ps101P320;
		case 32:
			return PageSizeMode.psCustom;
		default:
			return PageSizeMode.psNone;
		}

	}

	public static String pageSizeToString(PageSizeMode pageSize) {
		switch (pageSize) {
		case psA4H:
			return PAGENAMES[0];
		case psA4V:
			return PAGENAMES[1];
		case psA3H:
			return PAGENAMES[2];
		case psA3V:
			return PAGENAMES[3];
		case psA2H:
			return PAGENAMES[4];
		case psA2V:
			return PAGENAMES[5];
		case psA1H:
			return PAGENAMES[6];
		case psA1V:
			return PAGENAMES[7];
		case ps720p:
			return PAGENAMES[8];
		case ps1080P:
			return PAGENAMES[9];
		case ps2k:
			return PAGENAMES[10];
		case psWXGAP:
			return PAGENAMES[11];
		case psWSXGA:
			return PAGENAMES[12];
		case psWUXGA:
			return PAGENAMES[13];
		case psWQXGA:
			return PAGENAMES[14];
		case psQVGA:
			return PAGENAMES[15];
		case psVGA:
			return PAGENAMES[16];
		case psSVGA:
			return PAGENAMES[17];
		case psXGA:
			return PAGENAMES[18];
		case psqHD:
			return PAGENAMES[19];
		case psWXGA:
			return PAGENAMES[20];
		case ps5P420:
			return PAGENAMES[21];
		case ps5P440:
			return PAGENAMES[22];
		case ps55P560:
			return PAGENAMES[23];
		case ps6P560:
			return PAGENAMES[24];
		case ps6PX560:
			return PAGENAMES[25];
		case ps63P560:
			return PAGENAMES[26];
		case ps7P213:
			return PAGENAMES[27];
		case ps7P320:
			return PAGENAMES[28];
		case ps89P320:
			return PAGENAMES[29];
		case ps99P320:
			return PAGENAMES[30];
		case ps101P320:
			return PAGENAMES[31];
		case psCustom:
			return PAGENAMES[32];
		default:
			return null;
		}
	}

	public static int getDpi(String dpiName) {
		dpiName = dpiName.trim().toUpperCase();
		try {
			Field field = DrawCanvas.class.getDeclaredField(dpiName);
			return (int) field.get(null);
		} catch (Exception e) {
			e.printStackTrace();
			return Toolkit.getDefaultToolkit().getScreenResolution();
		}

	}

	public static float getAndroidDpiScale(String dpiName) {
		int dpi = getDpi(dpiName);
		return getAndroidDpiScale(dpi);
	}

	public static float getAndroidDpiScale(int dpi) {
		if (dpi > XXHDPI)
			return 3.5F;
		else if (dpi > XX44HDPI)
			return 3.0F;
		else if (dpi > XX42HDPI)
			return 2.75F;
		else if (dpi > XHDPI)
			return 2.63F;
		else if (dpi > HDPI)
			return 2.0F;
		else if (dpi > TVDPI)
			return 1.5F;
		else if (dpi > MDPI)
			return 1.33F;
		else if (dpi > LDPI)
			return 1.0F;
		else {
			return 0.75F;
		}
	}

	public static float getDpiScale(PageSizeMode mode) {
		switch (mode) {
		case ps101P320:
			return 2.0F;
		case ps5P440:
			return 2.75F;
		case ps55P560:
			return 3.5F;
		case ps5P420:
			return 2.63F;
		case ps6PX560:
			return 3.5F;
		case ps63P560:
			return 3.5F;
		case ps6P560:
			return 3.5F;
		case ps7P213:
			return 1.33F;
		case ps7P320:
			return 2.0F;
		case ps89P320:
			return 2.0F;
		case ps99P320:
			return 2.0F;
		default:
			return 1.0F;
		}
	}

	public int convertCanvasSizeToScreenSize(int canvasSize) {
		int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
		return (int) (canvasSize * ((float) dpi / pageConfig.deviceDPI));
	}

	public int convertScreenSizeToCanvasSize(int screenSize) {
		int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
		return (int) (screenSize * (pageConfig.deviceDPI / (float) dpi));
	}

	public class PageConfig {
		PageSizeMode curPageSizeMode = PageSizeMode.psA1V;

		public PageSizeMode getCurPageSizeMode() {
			return curPageSizeMode;
		}

		public String getCurPageSizeModeName() {
			return pageSizeToString(curPageSizeMode);
		}

		public int width = 0;
		public int height = 0;

		public String id;
		public String name;
		public String title;
		public String memo;
		public String data;
		public boolean autoCenter = true;
		public Color color = Color.WHITE;
		public boolean showGridLine = true;

		public boolean border = false;

		public boolean hasMainTree = false;
		public String mainNavTreeNodeId;

		public String saveId = UUID.randomUUID().toString();
		public Date saveTime = new Date();

		public int deviceDPI = Toolkit.getDefaultToolkit().getScreenResolution();
		Config[] configs = new Config[] { Config.ccAllowMulSelect, Config.ccAllowResize, Config.ccGrid, Config.ccLink,
				Config.ccAllowSelect, Config.ccAllowDrag, Config.ccAllowEdit };

		public void load(File file) throws Exception {
			JSONObject json = (JSONObject) JsonHelp.parseJson(file, null);
			fromJson(json);
		}

		public void toJson(JSONObject json) throws JSONException {
			JSONObject size = new JSONObject();

			size.put("width", canvasPageSize.width);
			size.put("height", canvasPageSize.height);
			json.put("size", size);

			json.put("saveTime", new Date().getTime());
			json.put("saveId", UUID.randomUUID().toString());
			json.put("screenDpi", Toolkit.getDefaultToolkit().getScreenResolution());
			json.put("deviceDPI", deviceDPI);
			json.put("pagesize", curPageSizeMode.name());
			json.put("color", ColorConvert.toHexFromColor(color));
			json.put("showGridLine", showGridLine);
			json.put("autoCenter", autoCenter);
			json.put("id", id);
			json.put("name", name);
			json.put("data", data);
			json.put("border", border);
			json.put("width", width);
			json.put("hasMainTree", hasMainTree);
			json.put("mainNavTreeNodeId", mainNavTreeNodeId);
			json.put("height", height);
			if (configPageSize.getWidth() != 0 && configPageSize.getHeight() != 0) {
				JSONObject tmps = new JSONObject();
				tmps.put("width", configPageSize.getWidth());
				tmps.put("height", configPageSize.getHeight());
				json.put("configPageSize", tmps);
			}
			if (title != null && !title.isEmpty())
				json.put("title", title);
			if (memo != null && !memo.isEmpty())
				json.put("memo", memo);
		}

		public void fromJson(JSONObject json) throws JSONException {
			if (json.has("saveId"))
				saveId = json.getString("saveId");
			else
				saveId = UUID.randomUUID().toString();

			if (json.has("saveTime"))
				saveTime = new Date(json.getLong("saveTime"));
			else
				saveTime = new Date();

			if (json.has("deviceDPI"))
				deviceDPI = json.getInt("deviceDPI");
			else
				deviceDPI = Toolkit.getDefaultToolkit().getScreenResolution();

			if (json.has("id"))
				id = json.getString("id");
			else
				id = UUID.randomUUID().toString();

			if (json.has("mainNavTreeNodeId"))
				mainNavTreeNodeId = json.getString("mainNavTreeNodeId");
			else
				mainNavTreeNodeId = null;

			if (json.has("data"))
				data = json.getString("data");
			else
				mainNavTreeNodeId = null;

			if (json.has("name"))
				name = json.getString("name");
			else
				name = null;

			if (json.has("pagesize")) {
				curPageSizeMode = PageSizeMode.valueOf(json.getString("pagesize"));
			} else
				curPageSizeMode = PageSizeMode.psA4V;

			if (json.has("showGridLine"))
				showGridLine = json.getBoolean("showGridLine");
			else
				showGridLine = true;

			if (json.has("autoCenter"))
				autoCenter = json.getBoolean("autoCenter");
			else
				autoCenter = false;

			if (json.has("hasMainTree"))
				hasMainTree = json.getBoolean("hasMainTree");
			else
				hasMainTree = false;

			if (json.has("color"))
				color = ColorConvert.toColorFromString(json.getString("color"));
			else
				color = Color.WHITE;

			if (json.has("border"))
				border = json.getBoolean("border");
			else
				border = false;

			if (json.has("width"))
				width = json.getInt("width");
			else
				width = 0;

			if (json.has("height"))
				height = json.getInt("height");
			else
				height = 0;

			if (json.has("configPageSize")) {
				JSONObject tmpJson = json.getJSONObject("configPageSize");
				configPageSize.width = tmpJson.getInt("width");
				configPageSize.height = tmpJson.getInt("height");
			}

			title = getTitle(json);
			memo = getMemo(json);

			setPageSizeMode();
		}

		public void setConfig(Config[] configs) {
			if (configs == null || configs.length == 0)
				this.configs = new Config[] {};
			else
				this.configs = configs;
		}

		public Config[] getConfig() {
			return Arrays.copyOf(configs, configs.length);
		}

		public boolean checkConfig(Config config) {
			return Arrays.asList(configs).contains(config);
		}

		public void setPageSizeMode() {
			if (configPageSize.width == 0 || configPageSize.height == 0)
				setPageSizeMode(curPageSizeMode, width, height);
			else
				setPageSizeMode(curPageSizeMode, configPageSize.width, configPageSize.height);
		}

		public void setPageSizeMode(String text, int width, int height) {
			setPageSizeMode(StringToPageSize(text), width, height);
		}

		public void setPageSizeMode(final PageSizeMode setPageSize, final int customWidth, final int customHeight) {
			PageSizeMode pageSize = setPageSize;
			int width = customWidth;
			int height = customHeight;

			if (useRect != null && useRect.width > 0 && useRect.height > 0)
				for (DrawNode node : nodes.values()) {
					if (node.relativeToPage == null)
						continue;

					Rectangle rect = node.getRect();
					node.relativeToPage.x = rect.x - useRect.x;
					node.relativeToPage.y = rect.y - useRect.y;
				}

			switch (pageSize) {
			case psA1H:
				canvasPageSize.width = mmToPixel(841);
				canvasPageSize.height = mmToPixel(594);
				break;
			case psA1V:
				canvasPageSize.width = mmToPixel(594);
				canvasPageSize.height = mmToPixel(841);
				break;
			case psA2H:
				canvasPageSize.width = mmToPixel(594);
				canvasPageSize.height = mmToPixel(420);
				break;
			case psA2V:
				canvasPageSize.width = mmToPixel(420);
				canvasPageSize.height = mmToPixel(594);
				break;
			case psA3H:
				canvasPageSize.width = mmToPixel(420);
				canvasPageSize.height = mmToPixel(297);
				break;
			case psA3V:
				canvasPageSize.width = mmToPixel(297);
				canvasPageSize.height = mmToPixel(420);
				break;
			case psA4H:
				canvasPageSize.width = mmToPixel(297);
				canvasPageSize.height = mmToPixel(210);
				break;
			case psA4V:
				canvasPageSize.width = mmToPixel(210);
				canvasPageSize.height = mmToPixel(297);
				break;
			case psCustom:
				this.width = width;
				this.height = height;
				canvasPageSize.width = width;
				canvasPageSize.height = height;
				break;
			case ps1080P:
				canvasPageSize.width = 1920;
				canvasPageSize.height = 1080;
				break;
			case ps2k:
				canvasPageSize.width = 2560;
				canvasPageSize.height = 1440;
				break;
			case psqHD:
				canvasPageSize.width = 960;
				canvasPageSize.height = 540;
				break;
			case psQVGA:
				canvasPageSize.width = 320;
				canvasPageSize.height = 240;
				break;
			case psVGA:
				canvasPageSize.width = 640;
				canvasPageSize.height = 480;
				break;
			case psSVGA:
				canvasPageSize.width = 800;
				canvasPageSize.height = 600;
				break;
			case psXGA:
				canvasPageSize.width = 1024;
				canvasPageSize.height = 768;
				break;
			case psWXGA:
				canvasPageSize.width = 1280;
				canvasPageSize.height = 800;
				break;
			case psWQXGA:
				canvasPageSize.width = 2560;
				canvasPageSize.height = 1600;
				break;
			case psWSXGA:
				canvasPageSize.width = 1680;
				canvasPageSize.height = 1050;
				break;
			case psWUXGA:
				canvasPageSize.width = 1920;
				canvasPageSize.height = 1200;
				break;
			case psWXGAP:
				canvasPageSize.width = 1440;
				canvasPageSize.height = 900;
				break;
			case ps5P420:
				canvasPageSize.width = 1080;
				canvasPageSize.height = 1920;
				break;
			case ps5P440:
				canvasPageSize.width = 1080;
				canvasPageSize.height = 900;
				break;
			case ps55P560:
				canvasPageSize.width = 1440;
				canvasPageSize.height = 2560;
				break;
			case ps6P560:
				canvasPageSize.width = 1440;
				canvasPageSize.height = 2560;
				break;
			case ps6PX560:
				canvasPageSize.width = 1440;
				canvasPageSize.height = 2880;
				break;
			case ps63P560:
				canvasPageSize.width = 1440;
				canvasPageSize.height = 2960;
				break;
			case ps7P213:
				canvasPageSize.width = 800;
				canvasPageSize.height = 1280;
				break;
			case ps7P320:
				canvasPageSize.width = 1200;
				canvasPageSize.height = 1920;
				break;
			case ps89P320:
				canvasPageSize.width = 2048;
				canvasPageSize.height = 1536;
				break;
			case ps99P320:
				canvasPageSize.width = 2560;
				canvasPageSize.height = 1800;
				break;
			case ps101P320:
				canvasPageSize.width = 2560;
				canvasPageSize.height = 1600;
				break;
			case ps720p:
			default:
				pageSize = PageSizeMode.ps720p;
				canvasPageSize.width = 1280;
				canvasPageSize.height = 720;
				break;
			}

			configPageSize = new Dimension(canvasPageSize);

			canvasPageSize.width = convertCanvasSizeToScreenSize(canvasPageSize.width);
			canvasPageSize.height = convertCanvasSizeToScreenSize(canvasPageSize.height);
			int x, y;
			if (canvasPageSize.width < getWidth()) {
				x = (getWidth() - canvasPageSize.width) / 2;
				width = canvasPageSize.width;
			} else {
				x = 0;
				width = getWidth() == 0 ? (useRect != null ? useRect.width : 0) : getWidth();
			}

			if (canvasPageSize.height < getHeight()) {
				y = (getHeight() - canvasPageSize.height) / 2;
				height = canvasPageSize.height;
			} else {
				y = 0;
				height = getHeight() == 0 ? (useRect != null ? useRect.height : 0) : getHeight();
			}

			useRect = new Rectangle(x, y, width, height);
			curPageSizeMode = pageSize;

			Point max = getMaxOffset();
			if (onPageSizeChanged != null) {
				onPageSizeChanged.onChanged(max);
			}

			if (useRect != null && useRect.width > 0 && useRect.height > 0) {
				onLoaded();
			}

			repaint();

		}
	}

	public PageConfig getPageConfig() {
		return pageConfig;
	}

	public void setPageConfig(PageConfig pageConfig) {
		this.pageConfig = pageConfig;
	}

	public Dimension getPageSize() {
		return canvasPageSize;
	}

	public Dimension getConfigPageSize() {
		return configPageSize;
	}

	public int getPageWidth() {
		return canvasPageSize.width;
	}

	public int getPageHeight() {
		return canvasPageSize.height;
	}

	public Point getMaxOffset() {
		Point p = new Point();
		p.x = canvasPageSize.width - (int) useRect.getWidth();
		if (p.x < 0)
			p.x = 0;

		p.y = canvasPageSize.height - (int) useRect.getHeight();
		if (p.y < 0)
			p.y = 0;

		return p;
	}

	public void setPopupMenu(JPopupMenu menu) {
		this.menu = menu;
	}

	public void repaint() {
		if (getWidth() == 0 || getHeight() == 0)
			return;

		Graphics g = this.getGraphics();
		update(g);
	}

	public int[] getZOrderMinAndMax() {
		int[] size = new int[] { Integer.MAX_VALUE, Integer.MIN_VALUE };
		for (DrawNode node : nodes.values()) {
			if (stackTreeManager.elements.containsKey(node.id) && stackTreeManager.getParent(node.id) != null)
				continue;
			size[0] = Math.min(size[0], node.zOrder);
			size[1] = Math.max(size[1], node.zOrder);
		}
		return size;
	}

	public void bringToTop() {
		for (DrawNode node : getSelecteds()) {
			bringToTop(node);
		}
	}

	public void bringToTop(DrawNode node) {
		if (node == null)
			return;
		if (!stackTreeManager.bringToTop(node)) {
			int[] mm = getZOrderMinAndMax();
			node.zOrder = mm[1] + 1;
		}
		fireChange(ChangeType.ctBringToTop);
		repaint();
	}

	public void sendToBack() {
		for (DrawNode node : getSelecteds()) {
			sendToBack(node);
		}
	}

	public void sendToBack(DrawNode node) {
		if (node == null)
			return;

		if (!stackTreeManager.sendToBack(node)) {
			int[] mm = getZOrderMinAndMax();
			node.zOrder = mm[0] - 1;
			if (node.zOrder < 0) {
				int fixOrder = Math.abs(node.zOrder) + 1;
				for (DrawNode n : nodes.values()) {
					if (stackTreeManager.elements.containsKey(n.id))
						continue;

					n.zOrder += fixOrder;
				}

				node.zOrder = 0;
			}
		}
		fireChange(ChangeType.ctSendToBack);
		repaint();
	}

	protected void drawLink(Graphics g) {
		if (getMouseMode() != MouseMode.mmLink)
			return;

		if (oldP == null || newP == null)
			return;

		Point start, end;
		start = getRealPoint(oldP);
		end = getRealPoint(newP);
		g.setColor(Color.BLUE);
		g.drawLine(start.x, start.y, end.x, end.y);
	}

	protected void drawSelectRect(Graphics g) {
		if (getMouseMode() != MouseMode.mmRectSelect)
			return;

		if (oldP == null || newP == null)
			return;

		Graphics2D g2 = (Graphics2D) g;

		Stroke dot = new BasicStroke(LINEWIDTH, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 3.5f,
				new float[] { 15, 10, }, 0f);

		Stroke old = g2.getStroke();
		g2.setStroke(dot);
		g2.setColor(Color.BLUE);

		Rectangle rect = getSelectRect();
		g2.drawRect(rect.x, rect.y, rect.width, rect.height);

		g2.setStroke(old);

	}

	protected void darwPageRect(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;

		g2.setColor(Color.GRAY);

		g2.drawRect(useRect.x, useRect.y, canvasPageSize.width, canvasPageSize.height);

	}

	protected ResizeButtonType checkResizeButton(Point pt) {
		if (getSelected() == null) {
			return ResizeButtonType.rtNone;
		}

		pt = getRealPoint(pt);
		if (selectNodes != null) {
			for (DrawNode node : selectNodes) {
				ResizeButtonType rt = node.getResizeButtonType(pt);
				if (rt != ResizeButtonType.rtNone)
					return rt;
			}
		}
		return ResizeButtonType.rtNone;
	}

	protected void paintNode(Graphics g, DrawNode node, boolean needCheckViewport) {
		node.draw(g, needCheckViewport);
	}

	/***
	 * 绘制有层叠关系的节点，仅绘制当前节点和它所在的根div的rect交集，不绘制下级节点
	 * 
	 * @param tree
	 *            要绘制的层叠节点
	 * @param g
	 *            画布
	 * @param needCheckViewport
	 *            是否需要检查视口
	 */
	protected void paintOnlyTreeNode(StatckTreeElement tree, Graphics g, boolean needCheckViewport) {
		if (!nodes.containsKey(tree.id)) {
			stackTreeManager.remove(tree.id);
			return;
		}
		DrawNode drawNode = nodes.get(tree.id);
		DrawNode parentNode = stackTreeManager.getRoot(drawNode.id);
		if (parentNode != null) {
			Rectangle clip = parentNode.getRect().intersection(drawNode.getRect());
			if (clip != null) {
				g.setClip(clip.x, clip.y, (int) (clip.width + LINEWIDTH), (int) (clip.height + LINEWIDTH));
			}
		}
		paintNode(g, drawNode, needCheckViewport);
		g.setClip(null);
	}

	/***
	 * 绘制层叠关系的节点及其所有子节点
	 * 
	 * @param tree
	 *            要绘制的层叠节点
	 * @param g
	 *            画布
	 * @param needCheckViewport
	 *            是否需要检查视口
	 */
	protected void paintTreeNode(StatckTreeElement tree, Graphics g, boolean needCheckViewport) {
		paintOnlyTreeNode(tree, g, needCheckViewport);

		TreeMap<Integer, List<DrawNode>> drawNodes = new TreeMap<>();
		for (String id : new ArrayList<>(tree.childs.values())) {
			if (nodes.containsKey(id)) {
				if (!stackTreeManager.elements.containsKey(id)) {
					tree.childs.remove(id);
					continue;
				}
				DrawNode node = nodes.get(id);
				List<DrawNode> list;
				if (drawNodes.containsKey(node.zOrder)) {
					list = drawNodes.get(node.zOrder);
				} else {
					list = new ArrayList<>();
					drawNodes.put(node.zOrder, list);
				}
				list.add(node);
			}
		}

		for (List<DrawNode> childs : drawNodes.values()) {
			for (DrawNode node : childs) {
				StatckTreeElement child = stackTreeManager.elements.get(node.id);
				paintTreeNode(child, g, needCheckViewport);
			}

		}
	}

	/***
	 * 绘制一个节点
	 */
	protected void realPaintNode(Graphics g, DrawNode node, boolean needCheckViewport) {
		if (stackTreeManager.roots.containsKey(node.id))
			paintTreeNode(stackTreeManager.roots.get(node.id), g, needCheckViewport);
		else
			paintNode(g, node, needCheckViewport);
	}

	/***
	 * 绘制nodes包含的所有节点
	 * 
	 * @param g
	 *            画布
	 * @param nodes
	 *            要绘制的节点
	 * @param needCheckViewport
	 */
	protected void paintNodes(Graphics g, Collection<DrawNode> nodes, boolean needCheckViewport) {
		for (DrawNode node : nodes) {
			realPaintNode(g, node, needCheckViewport);
		}
	}

	/***
	 * 绘制所有节点
	 * 
	 * @param g
	 *            画布
	 * @param width
	 *            要绘制的宽度
	 * @param height
	 *            要绘制的高度
	 * @param needCheckViewport
	 *            是否需要检查视口
	 */
	protected void realPaint(Graphics g, int width, int height, boolean needCheckViewport) {
		super.paint(g);
		g.setColor(pageConfig.color);
		if (useRect == null)
			pageConfig.setPageSizeMode();
		g.fillRect(0, 0, width, height);

		if (useRect != null && !useRect.isEmpty())
			g.setClip(useRect);

		g.translate(offset.x, offset.y);

		darwPageRect(g);

		if (pageConfig.checkConfig(Config.ccGrid) && pageConfig.showGridLine)
			drawAutoGrid(g);

		if (pageConfig.checkConfig(Config.ccLink))
			drawLink(g);

		TreeMap<Integer, List<DrawNode>> zorderNodes = new TreeMap<>();

		HashMap<String, DrawNode> allNodes = new HashMap<>(nodes);

		for (String id : stackTreeManager.elements.keySet()) {
			if (allNodes.containsKey(id))
				allNodes.remove(id);
		}

		for (DrawNode node : allNodes.values()) {
			List<DrawNode> list;
			if (zorderNodes.containsKey(node.zOrder))
				list = zorderNodes.get(node.zOrder);
			else {
				list = new ArrayList<>();
				zorderNodes.put(node.zOrder, list);
			}

			list.add(node);
		}

		for (String id : stackTreeManager.roots.keySet()) {
			DrawNode drawNode = nodes.get(id);
			List<DrawNode> list;
			if (zorderNodes.containsKey(drawNode.zOrder))
				list = zorderNodes.get(drawNode.zOrder);
			else {
				list = new ArrayList<>();
				zorderNodes.put(drawNode.zOrder, list);
			}

			list.add(drawNode);
		}

		List<DrawNode> drawNodes = new ArrayList<>();
		for (List<DrawNode> dnodes : zorderNodes.values()) {
			drawNodes.addAll(dnodes);
		}

		paintNodes(g, drawNodes, needCheckViewport);

		if (pageConfig.checkConfig(Config.ccAllowMulSelect))
			drawSelectRect(g);
	}

	public void update(Graphics g) {
		paint(g);
	}

	protected void updateCanvasSize(Rectangle oldUseRect) {
	}

	public BufferedImage saveToImage() {
		if (canvasPageSize.width <= 0 || canvasPageSize.height <= 0)
			return null;

		Rectangle oldrect = useRect;
		useRect = new Rectangle(0, 0, canvasPageSize.width, canvasPageSize.height);
		Rectangle max = useRect;

		BufferedImage image = new BufferedImage(canvasPageSize.width, canvasPageSize.height,
				BufferedImage.TYPE_3BYTE_BGR);
		Graphics g = image.getGraphics();
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, image.getWidth(), image.getHeight());
		Config[] old = pageConfig.configs;
		pageConfig.configs = new Config[] {};
		paint(g2, image.getWidth(), image.getHeight(), false);
		pageConfig.configs = old;
		g.dispose();

		if (nodes.size() > 0) {
			max = new Rectangle(Integer.MAX_VALUE, Integer.MAX_VALUE, 0, 0);
			for (DrawNode node : nodes.values()) {
				Rectangle r = node.getRect();
				if (r.x < max.x)
					max.x = r.x;
				if (r.y < max.y)
					max.y = r.y;
				if (r.x + r.width > max.width)
					max.width = r.x + r.width;
				if (r.y + r.height > max.height)
					max.height = r.y + r.height;
			}

			max.x -= 5;
			max.y -= 5;
			max.width = max.width - max.x + 10;
			max.height = max.height - max.y + 10;

		}

		if (max.x < 0)
			max.x = 0;

		if (max.y < 0)
			max.y = 0;
		useRect = oldrect;
		if (max.x + max.width > image.getWidth() || max.y + max.height > image.getHeight() || max.width == 0
				|| max.height == 0)
			max = new Rectangle(0, 0, image.getWidth(), image.getHeight());

		try {
			return image.getSubimage(max.x, max.y, max.width, max.height);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	boolean isPainting = false;

	@SuppressWarnings("unchecked")
	protected <T> T getLayer(Container parent, Class<T> c) {
		if (parent == null)
			return null;
		else if (c.isAssignableFrom(parent.getClass()))
			return (T) parent;
		else {
			return getLayer(parent.getParent(), c);
		}
	}

	protected void checkNeedPaint() {

	}

	public void paint(Graphics g) {
		if (isPainting)
			return;
		ChildForm layer = getLayer(getParent(), ChildForm.class);
		if (layer != null) {
			JDesktopPane desktop = getLayer(getParent(), JDesktopPane.class);
			if (desktop != null) {
				Container top = desktop.getSelectedFrame();
				if (top != layer) {
					return;
				}
			}
		}
		// Rectangle rect = getClipRect();
		// if (rect.isEmpty())
		// return;

		isPainting = true;
		try {
			paint(g, getWidth(), getHeight(), true);
		} finally {
			isPainting = false;
		}
	}

	protected void updateCanvasSize() {
		Rectangle oldUseRect = useRect;
		pageConfig.setPageSizeMode();
		if (oldUseRect != null && !oldUseRect.isEmpty()) {
			updateCanvasSize(oldUseRect);
		}
	}

	public void paint(Graphics g, int width, int height, boolean isComponent) {
		if (g == null || stopPaint)
			return;

		if (pageConfig == null)
			return;

		if (width <= 0 || height <= 0)
			return;

		Graphics offGraphics = g;
		if (isComponent) {
			if (offScreenImage == null || offScreenImage.getWidth(null) != width
					|| offScreenImage.getHeight(null) != height) {
				offScreenImage = createImage(width, height);
				updateCanvasSize();
			}
			if (offScreenImage == null) {
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						repaint();
					}
				});
				return;
			}
			offGraphics = offScreenImage.getGraphics();
		} else {
			updateCanvasSize();
		}

		if (offGraphics == null) {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					repaint();
				}
			});
			return;
		}
		Graphics2D g2 = (Graphics2D) offGraphics;
		g2.setStroke(new BasicStroke(3.0F));
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		realPaint(g2, width, height, isComponent);

		if (isComponent)
			g.drawImage(offScreenImage, 0, 0, null);
	}

	public void beginPaint() {
		stopPaint = true;
	}

	public void endPaint() {
		stopPaint = false;
		repaint();
	}

	public void cancelPaint() {
		stopPaint = false;
	}

	public void invalidate(List<DrawNode> nodes) {
		for (DrawNode drawNode : nodes) {
			drawNode.invalidRect();
		}
	}

	public ActionCommandManager getACM() {
		return getAcm();
	}

	public ActionCommandManager getAcm() {
		return acm;
	}

	public void setAcm(ActionCommandManager acm) {
		this.acm = acm;
	}

	public MouseMode getMouseMode() {
		return mouseMode;
	}

	public void setMouseMode(MouseMode mouseMode) {
		this.mouseMode = mouseMode;
	}

	public Cursor getCurCursor() {
		return curCursor;
	}

	public void setCurCursor(Cursor curCursor) {
		this.curCursor = curCursor;
	}

	public ICreateNodeSerializable getLastCreateUserDataSerializable() {
		return lastCreateUserDataSerializable;
	}

	public void setLastCreateUserDataSerializable(ICreateNodeSerializable lastCreateUserDataSerializable) {
		this.lastCreateUserDataSerializable = lastCreateUserDataSerializable;
	}

	public boolean isMTMode() {
		switch (pageConfig.getCurPageSizeMode()) {
		case ps101P320:
		case ps5P440:
		case ps55P560:
		case ps5P420:
		case ps6PX560:
		case ps63P560:
		case ps6P560:
		case ps7P213:
		case ps7P320:
		case ps89P320:
		case ps99P320:
			return true;
		default:
			return false;
		}
	}

	public int getMTFontSize(int fontSize) {
		if (isMTMode()) {
			return (int) (getAndroidDpiScale() * fontSize);
		} else
			return fontSize;
	}

	public float getAndroidDpiScale() {
		if (isMTMode()) {
			return getDpiScale(pageConfig.getCurPageSizeMode());
		} else
			return 1.0F;
	}

}
