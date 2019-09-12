package com.wh.draws;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wh.control.EditorEnvironment;
import com.wh.control.datasource.define.DataSource;
import com.wh.draws.drawinfo.ControlInfo;
import com.wh.draws.drawinfo.DrawInfo;
import com.wh.draws.drawinfo.MainTreeInfo;
import com.wh.draws.drawinfo.TreeInfo;
import com.wh.system.tools.JsonHelp;

public class UICanvas extends DrawCanvas {
	private static final long serialVersionUID = 1L;

	boolean drawControlInfo = true;
	
	public boolean getDrawControlInfo() {
		return drawControlInfo;
	}
	
	public void setDrawControlInfo(boolean b){
		drawControlInfo = b;
	}
	
	public class UIPageConfig extends PageConfig {

		public void toJson(JSONObject jsonObject) throws JSONException {
			super.toJson(jsonObject);
			JSONObject json = new JSONObject();
			for (String name : dataSources.keySet()) {
				json.put(name, name);
			}
			jsonObject.put("datasource", json);
		}

		public void fromJson(JSONObject json) throws JSONException {
			super.fromJson(json);
			dataSources.clear();
			if (json.has("datasource")) {
				JSONObject templatesJson = json.getJSONObject("datasource");
				Iterator<String> names = templatesJson.keys();
				while (names.hasNext()) {
					String name = names.next();
					dataSources.put(name, name);
				}
			}

		}

	}

	HashMap<String, String> dataSources = new HashMap<>();

	public double getWidthSize(String value) {
		return getStringSizeValue(value, getPageWidth(), 1.0F);
	}

	public double getHeightSize(String value) {
		return getStringSizeValue(value, getPageHeight(), 1.0F);
	}

	public double getStringSizeValue(String value, double size) {
		return getStringSizeValue(value, size, 1.0F);
	}

	public double roundTo(double value) {
		// 新方法，如果不需要四舍五入，可以使用RoundingMode.DOWN
		BigDecimal bg = new BigDecimal(value).setScale(2, RoundingMode.UP);
		return bg.doubleValue();
	}

	public double getStringSizeValue(String value, double size, float scale) {
		if (value == null || value.isEmpty())
			return 0;

		double v = 0;
		value = value.replace("px", "");
		boolean isAbs = value.indexOf("%") == -1;
		if (!isAbs) {
			v = Float.parseFloat(value.replace("%", "")) * size / 100;
		} else
			v = Float.parseFloat(value);

		return roundTo(v * scale);
	}

	public String appWorkflowid;

	public List<String> getDataSources() {
		return new ArrayList<>(dataSources.values());
	}

	public void addDataSource(DataSource dataSource) {
		dataSources.put(dataSource.id, dataSource.id);
	}

	public void removeDataSource(String id) {
		dataSources.remove(id);
	}

	protected PageConfig getPageConfigInstance() {
		return new UIPageConfig();
	}

	protected JSONArray getNavTreeInfo(DrawNode node) {
		DrawInfo drawInfo = ((UINode) node).getDrawInfo();
		Object value;
		if (drawInfo instanceof MainTreeInfo) {
			value = ((MainTreeInfo) drawInfo).sharedata;
		} else {
			value = ((TreeInfo) drawInfo).value;
		}

		if (value == null)
			return null;

		JSONArray json;
		if (value instanceof JSONArray)
			json = (JSONArray) value;
		else {
			json = new JSONArray((String) value);
		}

		return json;
	}

	protected void saveNavTreeInfo(DrawNode node) throws Exception {
		JSONArray json = getNavTreeInfo(node);
		if (json != null) {
			File file = EditorEnvironment.getMainNavTreeFile();
			JsonHelp.saveJson(file, json, null);
		}
	}

	public void setMainNavTree(String uiid, String id) throws Exception {
		EditorEnvironment.traverseUI(new EditorEnvironment.ITraverseUIFile() {

			@Override
			public boolean callback(File uiFile, UICanvas canvas, Object userObject) {
				if (canvas.getPageConfig().hasMainTree) {
					canvas.getPageConfig().hasMainTree = false;
					return true;
				}
				return false;
			}
		}, null);
		if (!nodes.containsKey(id))
			throw new Exception("not found node of id[" + id + "]");

		UINode node = (UINode) nodes.get(id);
		saveNavTreeInfo(node);

		EditorEnvironment.setMetaInfo(EditorEnvironment.META_MAINTREE_KEY,
				new EditorEnvironment.SimpleEntry<String, String>(EditorEnvironment.META_MAINTREE_CONTROLID_KEY,
						node.getDrawInfo().id),
				new EditorEnvironment.SimpleEntry<String, String>(EditorEnvironment.META_MAINTREE_CONTROLNAME_KEY,
						node.getDrawInfo().name),
				new EditorEnvironment.SimpleEntry<String, String>(EditorEnvironment.META_MAINTREE_UIID_KEY, uiid));
	}

	public JSONObject saveToJson() throws Exception {
		if (pageConfig.hasMainTree) {
			if (pageConfig.mainNavTreeNodeId == null || pageConfig.mainNavTreeNodeId.isEmpty()
					|| !nodes.containsKey(pageConfig.mainNavTreeNodeId)) {
				pageConfig.hasMainTree = false;
				pageConfig.mainNavTreeNodeId = null;
				EditorEnvironment.getMainNavTreeFile().delete();
			} else {
				UINode node = (UINode) nodes.get(pageConfig.mainNavTreeNodeId);
				saveNavTreeInfo(node);
			}
		}

		JSONObject jsonObject = super.saveToJson();
		JSONObject data = new JSONObject();
		data.put("appWorkflowid", appWorkflowid);
		jsonObject.put("uicanvas", data);

		return jsonObject;
	}

	public void loadFromJson(JSONObject json, ICreateNodeSerializable createUserDataSerializable, IInitPage onInitPage,
			boolean clear) throws Exception {
		
		super.loadFromJson(json, createUserDataSerializable, onInitPage, clear);
		
		appWorkflowid = null;
		
		if (json.has("uicanvas")) {
			
			JSONObject uidata = json.getJSONObject("uicanvas");
			
			if (uidata.has("appWorkflowid"))
				appWorkflowid = uidata.getString("appWorkflowid");
			
		}

	}

	protected void changePasteNode(DrawNode node) throws IOException {
		super.changePasteNode(node);
		if (node instanceof UINode) {
			UINode uiNode = (UINode) node;
			if (uiNode.info != null) {
				uiNode.info.id = UUID.randomUUID().toString();
			}
		}

	}

	protected boolean allowPaste(DrawNode node) {
		return node instanceof UINode;
	}

	protected void onLoaded() {
		if (onLoadedEvent != null)
			onLoadedEvent.onloaded();
	}

	protected void paintNodes(Graphics g, Collection<DrawNode> nodes, boolean needCheckViewport) {
		List<DrawNode> controlInfos = new ArrayList<>();
		for (DrawNode node : nodes) {
			UINode uiNode = (UINode) node;
			if (uiNode.getDrawInfo() instanceof ControlInfo){
				controlInfos.add(uiNode);
				continue;
			}
			Font oldfont = g.getFont();
			Color oldColor = g.getColor();

			uiNode.initRect();

			realPaintNode(g, uiNode, needCheckViewport);

			g.setFont(oldfont);
			g.setColor(oldColor);
		}

		if (!drawControlInfo || getWidth() == 0 || controlInfos.size() == 0)
			return;
		
		int left = useRect.x + 5;
		int top = useRect.y + 5;
		int width = 48;
		int height = 48;
		
		for (DrawNode node : controlInfos) {
			UINode uiNode = (UINode)node;
			ControlInfo info = (ControlInfo)uiNode.getDrawInfo();
			if (getSelected() == uiNode){
				Color old = g.getColor();
				g.setColor(Color.cyan);
				g.fillRect(left, top, width, height);
				g.setColor(old);
			}
			info.drawNode(g, new Rectangle(left, top, width, height));
			left += width + 5;
			if (left > useRect.getWidth() - 10){
				left = 5;
				top += height + 5;
			}
		}
	}

	protected void updateCanvasSize(Rectangle oldUseRect) {
		for (DrawNode tmp : nodes.values()) {
			UINode node = (UINode) tmp;
			if (node.info == null)
				continue;

			node.setRefRect(node.info.getRect());
		}

		if (this.getWidth() == 0 || this.getHeight() == 0)
			return;

	}

	public UINode add(String name, String typename) {
		return add(name, typename, new Rectangle(0, 0, 0, 0));
	}

	public UINode add(String title, String typename, int width, int height) {
		return add(title, typename, new Rectangle(0, 0, width, height));
	}

	public UINode add(String name, String typename, Rectangle r) {
		return (UINode) super.add(name, null, null, null, new IDataSerializable() {

			@Override
			public String save(Object userData) {
				return null;
			}

			@Override
			public DrawNode newDrawNode(Object userdata) {
				UINode node = new UINode(UICanvas.this);
				return node;
			}

			@Override
			public Object load(String value) {
				return null;
			}

			@Override
			public void initDrawNode(DrawNode node) {
				DrawInfo info = UINode.newInstance(typename, (UINode) node);
				Point pt = node.getCanvas()
						.getRealPoint(new Point(node.getCanvas().getWidth() / 2, node.getCanvas().getHeight() / 2));

				pt.x -= useRect.x;
				pt.y -= useRect.y;

				info.left = (r.x == 0 ? String.valueOf(pt.x) : String.valueOf(r.x)) + "px";
				info.top = (r.y == 0 ? String.valueOf(pt.y) : String.valueOf(r.y)) + "px";
				info.width = r.width == 0 ? info.width : (String.valueOf(r.width) + "px");
				info.height = r.height == 0 ? info.height : (String.valueOf(r.height) + "px");

				((UINode) node).setDrawInfo(info);
				node.invalidRect();
			}
		});
	}
}
