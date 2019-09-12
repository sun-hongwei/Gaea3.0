package com.wh.draws.drawinfo;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wh.control.EditorEnvironment;
import com.wh.control.datasource.define.DataSource;
import com.wh.draws.DrawCanvas;
import com.wh.draws.DrawCanvas.ChangeType;
import com.wh.draws.DrawCanvas.ICreateNodeSerializable;
import com.wh.draws.DrawCanvas.IDataSerializable;
import com.wh.draws.DrawCanvas.IJsonObject;
import com.wh.draws.DrawCanvas.MouseMode;
import com.wh.draws.control.ActionCommandManager.CommandInfoType;
import com.wh.draws.DrawNode;
import com.wh.draws.UINode;
import com.wh.draws.drawinfo.ReportInfo.CheckResult.ResultType;
import com.wh.system.tools.ColorConvert;
import com.wh.system.tools.JsonHelp;

public class ReportInfo extends DrawInfo {

	public class Template implements IJsonObject {
		List<CellInfo> cells = new ArrayList<>();
		TreeMap<Integer, Integer> rowHeights = new TreeMap<>();
		TreeMap<Integer, Integer> colWidths = new TreeMap<>();
		String name;

		protected Template() {

		}

		public Template(String name, Collection<CellInfo> cells) {
			this.name = name;
			for (CellInfo cellInfo : cells) {
				try {
					CellInfo cell = new CellInfo(ReportInfo.this);
					cell.fromJson(cellInfo.toJson());
					for (int i = cellInfo.startRow; i < cellInfo.endRow; i++) {
						rowHeights.put(i, ReportInfo.this.rows.get(i).height);
					}
					for (int i = cellInfo.startCol; i < cellInfo.endCol; i++) {
						colWidths.put(i, ReportInfo.this.cols.get(i).width);
					}
					this.cells.add(cell);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}

		class TemplateParam {
			int startRow;
			int endRow;
			int startCol;
			int endCol;
		}

		protected TemplateParam getTemplateParam(CellInfo cellInfo) {
			split(cellInfo);
			int startRow = cellInfo.startRow;
			int endRow = cellInfo.endRow;
			int count = endRow - startRow;

			while (count < rowHeights.size()) {
				addRow(endRow++);
				count++;
			}

			if (count > rowHeights.size())
				count = rowHeights.size();

			endRow = startRow + count;

			int startCol = cellInfo.startCol;
			int endCol = cellInfo.endCol;
			count = endCol - startCol;

			while (count < colWidths.size()) {
				addCol(endCol++);
				count++;
			}

			if (count > colWidths.size())
				count = rowHeights.size();

			endCol = startCol + count;

			TemplateParam param = new TemplateParam();
			param.startCol = startCol;
			param.endCol = endCol;
			param.startRow = startRow;
			param.endRow = endRow;

			return param;
		}

		/**
		 * 应用模板
		 * 
		 * @param cellInfo
		 *            要应用的模板数据，此模板数据所引用的行列必须存在与此报表中
		 */
		public void apply(CellInfo cellInfo) {
			pushCommand();

			TemplateParam templateInfo = getTemplateParam(cellInfo);
			List<Integer> rows = new ArrayList<>(rowHeights.values());
			List<Integer> cols = new ArrayList<>(colWidths.values());
			for (int i = 0; i < templateInfo.endRow - templateInfo.startRow; i++) {
				ReportInfo.this.rows.get(templateInfo.startRow + i).height = rows.get(i);
			}
			for (int j = 0; j < templateInfo.endCol - templateInfo.startCol; j++) {
				ReportInfo.this.cols.get(templateInfo.startCol + j).width = cols.get(j);
			}

			rows = new ArrayList<>(rowHeights.keySet());
			cols = new ArrayList<>(colWidths.keySet());
			for (CellInfo info : cells) {
				int indexRow = rows.indexOf(info.startRow);
				int indexCol = cols.indexOf(info.startCol);
				int startRow = templateInfo.startRow + indexRow;
				int startCol = templateInfo.startCol + indexCol;

				try {
					List<CellInfo> mergeLst = new ArrayList<>();
					for (int col = startCol; col < startCol + (info.endCol - info.startCol); col++) {
						for (int row = startRow; row < startRow + (info.endRow - info.startRow); row++) {
							CellInfo cell = ReportInfo.this.getCells().get(new Key(row, col).toString());
							mergeLst.add(cell);
						}
					}

					CellInfo cell;
					if (mergeLst.size() > 1)
						cell = merge(mergeLst);
					else if (mergeLst.size() == 1)
						cell = mergeLst.get(0);
					else {
						throw new NullPointerException("no found cell!");
					}
					String old_id = cell.id;
					int old_StartRow = cell.startRow;
					int old_EndRow = cell.endRow;
					int old_StartCol = cell.startCol;
					int old_EndCol = cell.endCol;

					cell.fromJson(info.toJson());
					cell.id = old_id;
					cell.editor.id = UUID.randomUUID().toString();
					cell.editor.name = cell.editor.id;
					cell.startCol = old_StartCol;
					cell.endCol = old_EndCol;
					cell.startRow = old_StartRow;
					cell.endRow = old_EndRow;

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			fireChange(node, ChangeType.ctReportApplyTemplate, new Object[] { cellInfo, this });
		}

		@Override
		public JSONObject toJson() throws JSONException {
			JSONObject data = new JSONObject();
			data.put("name", name);
			JSONObject tmps = new JSONObject();
			for (Integer key : rowHeights.keySet()) {
				tmps.put(String.valueOf(key), rowHeights.get(key));
			}
			data.put("rows", tmps);

			tmps = new JSONObject();
			for (Integer key : colWidths.keySet()) {
				tmps.put(String.valueOf(key), colWidths.get(key));
			}
			data.put("cols", tmps);
			JSONArray cellJsons = new JSONArray();
			for (CellInfo cell : cells) {
				cellJsons.put(cell.toJson());
			}
			data.put("cells", cellJsons);
			return data;
		}

		@Override
		public void fromJson(JSONObject json, ICreateNodeSerializable createUserDataSerializable) throws JSONException {
			rowHeights.clear();
			colWidths.clear();
			cells.clear();

			name = json.getString("name");
			JSONObject tmps = json.getJSONObject("rows");
			Iterator<String> keys = tmps.keys();
			while (keys.hasNext()) {
				String key = keys.next();
				rowHeights.put(Integer.parseInt(key), tmps.getInt(key));
			}

			tmps = json.getJSONObject("cols");
			keys = tmps.keys();
			while (keys.hasNext()) {
				String key = keys.next();
				colWidths.put(Integer.parseInt(key), tmps.getInt(key));
			}

			JSONArray cellInfos = json.getJSONArray("cells");
			for (int i = 0; i < cellInfos.length(); i++) {
				JSONObject cellInfo = cellInfos.getJSONObject(i);
				CellInfo cell = new CellInfo(ReportInfo.this);
				try {
					cell.fromJson(cellInfo);
					cells.add(cell);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	ResizeMode resizeMode = ResizeMode.rmNode;
	int oldValue = 0;
	int resizeIndex = -1;

	Point oldPoint;

	HashMap<CellInfo, CellInfo> selects = new HashMap<>();

	public IClick onClick;

	List<CellConfigInfo> rows = new ArrayList<>();
	List<CellConfigInfo> cols = new ArrayList<>();
	private TreeMap<String, CellInfo> cells = new TreeMap<>();

	public Color lineColor = ColorConvert.toColorFromString("0xFFd3d3d3");

	public Color backgroundColor = Color.WHITE;

	HashMap<String, Template> templates = new HashMap<>();

	HashMap<String, String> dataSources = new HashMap<>();

	public boolean autoSize = true;

	protected void fireChange(DrawNode node, ChangeType ct, Object data) {
		node.getCanvas().fireChange(node, ct, data);
	}

	protected void pushCommand() {
		node.getCanvas().getAcm().pushCommand(node, CommandInfoType.ctUpdateAttr);
	}

	public List<String> getDataSources() {
		return new ArrayList<>(dataSources.values());
	}

	public void addDataSource(DataSource dataSource) {
		dataSources.put(dataSource.id, dataSource.id);
	}

	public void removeDataSource(String id) {
		dataSources.remove(id);
	}

	public String typeName() {
		return DrawInfoDefines.Report_Name;
	}

	public Template addTemplate(String name) {
		if (templates.containsKey(name)) {
			EditorEnvironment.showMessage("模板名称已经存在！");
			return null;
		}

		if (selects.size() == 0) {
			EditorEnvironment.showMessage("为选定任何单元格！");
			return null;
		}

		Template template = new Template(name, selects.values());
		templates.put(name, template);
		return template;
	}

	public void applyTemplate(String name) {
		if (!templates.containsKey(name)) {
			EditorEnvironment.showMessage("模板不存在！");
			return;
		}
		node.getCanvas().beginPaint();
		for (CellInfo cellInfo : selects.values()) {
			applyTemplate(name, cellInfo);
		}
		node.getCanvas().endPaint();
	}

	public void applyTemplate(String name, CellInfo cellInfo) {
		if (!templates.containsKey(name)) {
			EditorEnvironment.showMessage("模板不存在！");
			return;
		}

		templates.get(name).apply(cellInfo);
	}

	public List<String> getTemplateNames() {
		return new ArrayList<>(templates.keySet());
	}

	public Template getTemplate(String name) {
		if (templates.containsKey(name))
			return templates.get(name);

		return null;
	}

	public Template removeTemplate(String name) {
		if (templates.containsKey(name))
			return templates.remove(name);

		return null;
	}

	protected boolean inRange(CellInfo check, List<CellInfo> ranges) {
		for (CellInfo cellInfo : ranges) {
			if (check.startCol >= cellInfo.startCol && check.endCol <= cellInfo.endCol
					&& check.startRow >= cellInfo.startRow && check.endRow <= cellInfo.endRow) {
				return true;
			}
		}

		return false;
	}

	protected void checkInvalidateCell() {
		int count = getCells().size();
		List<CellInfo> ranges = new ArrayList<>();
		for (String key : new ArrayList<>(getCells().keySet())) {
			CellInfo cellInfo = getCells().get(key);
			if (cellInfo.endCol - cellInfo.startCol > 1 || cellInfo.endRow - cellInfo.startRow > 1) {
				if (!inRange(cellInfo, ranges))
					ranges.add(cellInfo);
				else
					getCells().remove(key);
			} else {
				if (inRange(cellInfo, ranges))
					getCells().remove(key);
			}

		}

		if (count != getCells().size())
			node.getCanvas().fireChange(node, ChangeType.ctReportRemoveCell, new Object[] {});
	}

	public JSONObject toJson(boolean needAll) throws JSONException {
		;
		JSONObject json = super.toJson(needAll);
		JSONArray values = new JSONArray();
		for (CellConfigInfo cellConfigInfo : cols) {
			values.put(cellConfigInfo.toJson());
		}
		json.put("cols", values);

		values = new JSONArray();
		for (CellConfigInfo cellConfigInfo : rows) {
			values.put(cellConfigInfo.toJson());
		}
		json.put("rows", values);

		values = new JSONArray();
		for (CellInfo info : getCells().values()) {
			values.put(info.toJson());
		}
		json.put("cells", values);

		json.put("lineColor", needAll && lineColor == null ? "0xFFd3d3d3" : ColorConvert.toHexFromColor(lineColor));
		json.put("backgroundColor",
				needAll && backgroundColor == null ? "0xFFFFFFFF" : ColorConvert.toHexFromColor(backgroundColor));

		JSONObject templateJson;
		if (!needAll) {
			templateJson = new JSONObject();
			for (String name : templates.keySet()) {
				templateJson.put(name, templates.get(name).toJson());
			}
			
			json.put("template", templateJson);			
		}

		templateJson = new JSONObject();
		for (String name : dataSources.keySet()) {
			templateJson.put(name, name);
		}
		json.put("datasource", templateJson);
		return json;
	}

	public void fromJson(JSONObject json) throws JSONException {
		super.fromJson(json);

		if (json.has("lineColor"))
			lineColor = ColorConvert.toColorFromString(json.getString("lineColor"));

		if (json.has("backgroundColor"))
			backgroundColor = ColorConvert.toColorFromString(json.getString("backgroundColor"));

		for (CellInfo info : new ArrayList<>(getCells().values())) {
			removeCell(new Key(info).toString());
		}
		getCells().clear();
		selects.clear();
		cols.clear();
		rows.clear();

		boolean needReset = false;

		if (json.has("cols")) {
			JSONArray jsonArray = json.getJSONArray("cols");
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				CellConfigInfo info = new CellConfigInfo(null, null);
				info.fromJson(jsonObject);
				if (info.scale == null)
					needReset = true;
				cols.add(info);
			}
		}

		if (json.has("rows")) {
			JSONArray jsonArray = json.getJSONArray("rows");
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				CellConfigInfo info = new CellConfigInfo(null, null);
				info.fromJson(jsonObject);
				if (info.scale == null)
					needReset = true;
				rows.add(info);
			}
		}

		if (needReset)
			resetScales(false, false);

		if (json.has("cells")) {
			JSONArray jsonArray = json.getJSONArray("cells");
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				CellInfo info = new CellInfo(this);
				try {
					info.fromJson(jsonObject);
				} catch (Exception e) {
					e.printStackTrace();
					throw new JSONException(e.getMessage());
				}
				getCells().put(new Key(info).toString(), info);
			}
		}

		if (json.has("template")) {
			JSONObject templatesJson = json.getJSONObject("template");
			Iterator<String> names = templatesJson.keys();
			while (names.hasNext()) {
				String name = names.next();
				JSONObject templateJson = templatesJson.getJSONObject(name);
				Template template = new Template();
				template.fromJson(templateJson, null);
				templates.put(name, template);
			}
		}

		if (json.has("datasource")) {
			JSONObject templatesJson = json.getJSONObject("datasource");
			Iterator<String> names = templatesJson.keys();
			while (names.hasNext()) {
				String name = names.next();
				dataSources.put(name, name);
			}
		}

		checkInvalidateCell();
	}

	class CellConfigInfo {
		public Integer width;
		public Integer height;
		public Double scale = null;

		public CellConfigInfo(Integer width, Integer height) {
			if (width != null)
				this.width = width;
			if (height != null)
				this.height = height;
		}

		public JSONObject toJson() throws JSONException {
			JSONObject jsonObject = new JSONObject();
			if (width != null)
				jsonObject.put("width", width);
			if (height != null)
				jsonObject.put("height", height);
			if (scale != null)
				jsonObject.put("scale", scale);
			return jsonObject;
		}

		public void fromJson(JSONObject json) throws JSONException {
			if (json.has("width"))
				width = json.getInt("width");
			if (json.has("height"))
				height = json.getInt("height");
			if (json.has("scale"))
				scale = json.getDouble("scale");
		}
	}

	public static class CellInfo {
		public String id = UUID.randomUUID().toString();
		public int startCol;
		public int endCol;
		public int startRow;
		public int endRow;
		public UINode editor;

		public ReportInfo reportInfo;

		public JSONObject toJson() throws JSONException {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("startCol", startCol);
			jsonObject.put("endCol", endCol);
			jsonObject.put("startRow", startRow);
			jsonObject.put("endRow", endRow);
			jsonObject.put("editor", editor.toJson());
			return jsonObject;
		}

		protected CellInfo(ReportInfo reportInfo) {
			this.reportInfo = reportInfo;
		}

		public void fromJson(JSONObject json) throws Exception {
			startCol = json.getInt("startCol");
			endCol = json.getInt("endCol");
			startRow = json.getInt("startRow");
			endRow = json.getInt("endRow");
			editor = (UINode) UINode.fromJson(reportInfo.node.getCanvas(), json.getJSONObject("editor"),
					new ICreateNodeSerializable() {

						@Override
						public DrawNode newDrawNode(JSONObject json) {
							return new UINode(reportInfo.node.getCanvas());
						}

						@Override
						public IDataSerializable getUserDataSerializable(DrawNode node) {
							return null;
						}
					});
		}

		public int getHeight() {
			return getRect().height;
		}

		public int getWidth() {
			return getRect().width;
		}

		public Rectangle getRect() {
			return reportInfo.getCellRect(this);
		}

		public void setHeight(int size) {
			reportInfo.setCellHeight(this, size);
		}

		public void setWidth(int size) {
			reportInfo.setCellWidth(this, size);
		}

		public String toString() {
			return "id:" + (editor != null ? editor.id : "") + ",startCol=" + String.valueOf(startCol) + ",endCol="
					+ String.valueOf(endCol) + ",startRow=" + String.valueOf(startRow) + ",endRow="
					+ String.valueOf(endRow);
		}

	}

	class Key {
		public int row;
		public int col;

		public String toString() {
			return String.valueOf(row) + "." + String.valueOf(col);
		}

		public Key(CellInfo info) {
			this(info.startRow, info.startCol);
		}

		public Key(int row, int col) {
			this.row = row;
			this.col = col;
		}

		public Key(String value) {
			String[] tmps = value.split("\\.");
			row = Integer.parseInt(tmps[0]);
			col = Integer.parseInt(tmps[1]);
		}
	}

	protected void newEditor(CellInfo info) {
		info.editor = new UINode(node.getCanvas());
		if (node != null && node.getCanvas() != null) {
			Map<String, CellInfo> cellMap = new HashMap<>();
			for (CellInfo cellInfo : cells.values()) {
				if (cellInfo.editor == null)
					continue;
				
				cellMap.put(cellInfo.editor.id, cellInfo);
			}
			
			String name = "report_node";
			int index = 0;
			while (cellMap.containsKey(name + index)) {
				index++;
			}
			
			info.editor.id = name + index;
			info.editor.name = name + index;
			
		}

		info.editor.info = new LabelInfo(info.editor);
		info.editor.info.needFrame = false;
	}

	public void clear() {
		clearSelected();
		rows.clear();
		cols.clear();
		getCells().clear();
	}

	public void init(int rowcount, int colcount) {
		clear();
		Rectangle r = getRect();
		int cellWidth = r.width / colcount;
		int cellheight = r.height / rowcount;
		for (int i = 0; i < rowcount; i++) {
			for (int j = 0; j < colcount; j++) {
				CellInfo info = new CellInfo(this);
				info.startCol = j;
				info.endCol = j + 1;
				info.startRow = i;
				info.endRow = i + 1;
				newEditor(info);
				getCells().put(new Key(info.startRow, info.startCol).toString(), info);
			}
			rows.add(new CellConfigInfo(null, cellheight));
		}

		for (int j = 0; j < colcount; j++) {
			cols.add(new CellConfigInfo(cellWidth, null));
		}

		resetScales();
		node.getCanvas().repaint();
	}

	public void addRow() {
		int row = rows.size();
		CellInfo info = getSelectCellInfo();
		if (info != null) {
			row = info.endRow;
		}

		addRow(row);
	}

	public void removeCell(String key) {
		if (getCells().containsKey(key)) {
			pushCommand();

			UINode uiNode = getCells().get(key).editor;
			getCells().remove(key);
			if (uiNode != null)
				uiNode.getCanvas().remove(new DrawNode[] { uiNode }, true, false, true);

			fireChange(node, ChangeType.ctReportRemoveCell, new Object[] { key, uiNode });
		}
	}

	public void addRow(int row) {
		pushCommand();

		List<CellInfo> values = new ArrayList<>(getCells().values());
		List<CellInfo> newValues = new ArrayList<>();
		rows.add(row, new CellConfigInfo(null, 40));
		for (CellInfo cellInfo : values) {
			String oldKey = new Key(cellInfo).toString();

			removeCell(oldKey);
			if (cellInfo.startRow >= row) {
				cellInfo.startRow++;
				cellInfo.endRow++;
			} else if (cellInfo.startRow < row && row < cellInfo.endRow) {
				cellInfo.endRow++;
			}

			newValues.add(cellInfo);
		}

		for (CellInfo cellInfo : newValues) {
			getCells().put(new Key(cellInfo).toString(), cellInfo);
		}

		for (int i = 0; i < cols.size(); i++) {
			// if (cells.containsKey(new Key(row, i).toString()))
			// continue;
			boolean b = false;
			for (CellInfo cellInfo : newValues) {
				if (i >= cellInfo.startCol && i < cellInfo.endCol)
					if (row >= cellInfo.startRow && row < cellInfo.endRow) {
						b = true;
						break;
					}
			}
			if (b)
				continue;
			CellInfo cellInfo = new CellInfo(this);
			cellInfo.startCol = i;
			cellInfo.endCol = cellInfo.startCol + 1;
			cellInfo.startRow = row;
			cellInfo.endRow = cellInfo.startRow + 1;
			newEditor(cellInfo);
			getCells().put(new Key(cellInfo).toString(), cellInfo);
		}

		fireChange(node, ChangeType.ctReportAddRow, new Object[] { row });

		node.getCanvas().repaint();

	}

	public void addCol() {
		CellInfo info = getSelectCellInfo();
		int col = cols.size();
		if (info != null) {
			col = info.endCol;
		}

		addCol(col);
	}

	public void addCol(int col) {
		pushCommand();

		List<CellInfo> values = new ArrayList<>(getCells().values());
		List<CellInfo> newValues = new ArrayList<>();
		cols.add(col, new CellConfigInfo(80, null));
		for (CellInfo cellInfo : values) {
			String oldKey = new Key(cellInfo).toString();
			removeCell(oldKey);
			if (cellInfo.startCol >= col) {
				cellInfo.startCol++;
				cellInfo.endCol++;
			} else if (cellInfo.startCol < col && col < cellInfo.endCol) {
				cellInfo.endCol++;
			}

			newValues.add(cellInfo);
		}

		for (CellInfo cellInfo : newValues) {
			getCells().put(new Key(cellInfo).toString(), cellInfo);
		}

		for (int i = 0; i < rows.size(); i++) {
			boolean b = false;
			for (CellInfo cellInfo : newValues) {
				if (i >= cellInfo.startRow && i < cellInfo.endRow)
					if (col >= cellInfo.startCol && col < cellInfo.endCol) {
						b = true;
						break;
					}
			}
			if (b)
				continue;
			CellInfo cellInfo = new CellInfo(this);
			cellInfo.startRow = i;
			cellInfo.endRow = cellInfo.startRow + 1;
			cellInfo.startCol = col;
			cellInfo.endCol = cellInfo.startCol + 1;
			newEditor(cellInfo);
			getCells().put(new Key(cellInfo).toString(), cellInfo);
		}

		fireChange(node, ChangeType.ctReportAddColumn, new Object[] { col });
		node.getCanvas().repaint();

	}

	public void removeCol() {
		pushCommand();
		node.getCanvas().beginPaint();
		TreeMap<Integer, Integer> colIndexs = new TreeMap<>();
		for (CellInfo info : getSelectCellInfos()) {
			int start = info.endCol - 1;
			int end = info.startCol;
			for (int i = start; i >= end; i--) {
				colIndexs.put(i, i);
			}
		}

		for (Integer index : colIndexs.descendingKeySet()) {
			removeCol(index, false);
		}

		node.getCanvas().endPaint();
	}

	public void removeCol(CellInfo info, boolean pushCommand) {
		if (info == null)
			return;
		int start = info.endCol - 1;
		int end = info.startCol;
		for (int i = start; i >= end; i--) {
			removeCol(i, pushCommand);
		}
	}

	public void removeCol(int col, boolean pushCommand) {
		if (pushCommand)
			pushCommand();

		node.getCanvas().beginPaint();
		if (col >= 0 && col < cols.size()) {
			cols.remove(col);
		}

		List<CellInfo> newValues = new ArrayList<>();
		for (CellInfo cellInfo : new ArrayList<>(getCells().values())) {
			removeCell(new Key(cellInfo).toString());
			int count = cellInfo.endCol - cellInfo.startCol;
			if (cellInfo.startCol == col && count == 1)
				continue;
			if (cellInfo.startCol > col) {
				cellInfo.startCol--;
				cellInfo.endCol--;
			} else if (cellInfo.startCol <= col && col < cellInfo.endCol) {
				cellInfo.endCol--;
			}
			if (cellInfo.startCol >= 0 && cellInfo.endCol > cellInfo.startCol) {
				newValues.add(cellInfo);
			}
		}

		for (CellInfo cellInfo : newValues) {
			getCells().put(new Key(cellInfo).toString(), cellInfo);
		}

		fireChange(node, ChangeType.ctReportRemoveColumn, new Object[] { col });

		node.getCanvas().endPaint();
	}

	public void removeRow() {
		pushCommand();

		node.getCanvas().beginPaint();
		TreeMap<Integer, Integer> colIndexs = new TreeMap<>();
		for (CellInfo info : getSelectCellInfos()) {
			int start = info.endRow - 1;
			int end = info.startRow;
			for (int i = start; i >= end; i--) {
				colIndexs.put(i, i);
			}
		}

		for (Integer index : colIndexs.descendingKeySet()) {
			removeRow(index, false);
		}

		node.getCanvas().endPaint();
	}

	public void removeRow(CellInfo info, boolean pushCommand) {
		if (info == null)
			return;
		int start = info.endRow - 1;
		int end = info.startRow;
		for (int i = start; i >= end; i--) {
			removeRow(i, pushCommand);
		}
	}

	public void removeRow(int row, boolean pushCommand) {
		if (pushCommand)
			pushCommand();

		node.getCanvas().beginPaint();
		if (row >= 0 && row < rows.size()) {
			rows.remove(row);
		}

		List<CellInfo> newValues = new ArrayList<>();
		for (CellInfo cellInfo : new ArrayList<>(getCells().values())) {
			removeCell(new Key(cellInfo).toString());
			int count = cellInfo.endRow - cellInfo.startRow;
			if (cellInfo.startRow == row && count == 1)
				continue;
			if (cellInfo.startRow > row) {
				cellInfo.startRow--;
				cellInfo.endRow--;
			} else if (cellInfo.startRow <= row && row < cellInfo.endRow) {
				cellInfo.endRow--;
			}
			if (cellInfo.startRow >= 0 && cellInfo.endRow > cellInfo.startRow) {
				newValues.add(cellInfo);
			}
		}

		for (CellInfo cellInfo : newValues) {
			getCells().put(new Key(cellInfo).toString(), cellInfo);
		}

		fireChange(node, ChangeType.ctReportRemoveRow, new Object[] { row });

		node.getCanvas().endPaint();
	}

	protected void setCellHeight(CellInfo info, int height) {
		int divH = height / (info.endRow - info.startRow);
		for (int i = info.startRow; i < info.endRow; i++) {
			rows.get(i).height = divH;
		}

		CellConfigInfo configInfo = rows.get(info.startRow);
		configInfo.height += (height - (info.endRow - info.startRow) * divH);
	}

	protected void setCellWidth(CellInfo info, int width) {
		int divW = width / (info.endCol - info.startCol);
		for (int i = info.startCol; i < info.endCol; i++) {
			cols.get(i).width = divW;
		}

		CellConfigInfo configInfo = cols.get(info.startCol);
		configInfo.width += (width - (info.endCol - info.startCol) * divW);
	}

	protected Rectangle getCellRect(CellInfo info) {
		Rectangle rect = getRect();

		Rectangle r = new Rectangle();
		int tmp = 0;
		for (int i = 0; i < info.startCol; i++) {
			tmp += cols.get(i).width;
		}
		r.x = rect.x + tmp;

		tmp = 0;
		for (int i = 0; i < info.startRow; i++) {
			tmp += rows.get(i).height;
		}
		r.y = rect.y + tmp;

		tmp = 0;
		for (int i = info.startCol; i < info.endCol; i++) {
			tmp += cols.get(i).width;
		}
		r.width = tmp;

		tmp = 0;
		for (int i = info.startRow; i < info.endRow; i++) {
			tmp += rows.get(i).height;
		}
		r.height = tmp;

		return r;
	}

	public void onResize() {
		resetRowColumn();
	}

	public void setCanvas(DrawCanvas canvas) {
		for (CellInfo info : getCells().values()) {
			info.editor.setCanvas(canvas);
		}
	}

	public UINode getSelected() {
		if (selects.size() > 0)
			return selects.get(selects.keySet().iterator().next()).editor;
		else
			return null;
	}

	public void setSelected(CellInfo selectInfo) {
		selects.put(selectInfo, selectInfo);
		fireChange(node, ChangeType.ctReportSelected, new Object[] { selectInfo });
	}

	public void clearSelected() {
		selects.clear();
		fireChange(node, ChangeType.ctReportDeselected, new Object[] { selects });
	}

	public CellInfo getSelectCellInfo() {
		if (selects.size() > 0)
			return selects.get(selects.keySet().iterator().next());
		else
			return null;
	}

	public List<CellInfo> getSelectCellInfos() {
		return new ArrayList<>(selects.values());
	}

	public int rowCount() {
		return rows.size();
	}

	public int colCount() {
		return cols.size();
	}

	public List<Integer> getSelectRow() {
		HashMap<String, List<Integer>> rows = getSelectRows();
		if (rows.size() > 0) {
			int min = Integer.MAX_VALUE;
			List<Integer> row = null;
			for (List<Integer> rowIndexs : rows.values()) {
				if (rowIndexs.get(0) < min) {
					min = rowIndexs.get(0);
					row = rowIndexs;
				}
			}
			return row;
		}

		return new ArrayList<>();
	}

	public void equalHeight(int height) {
		pushCommand();

		for (CellInfo cellInfo : selects.values()) {
			cellInfo.setHeight(height);
		}

		fireChange(node, ChangeType.ctResize, new Object[] { selects });
	}

	public void equalWidth(int width) {
		pushCommand();

		for (CellInfo cellInfo : selects.values()) {
			cellInfo.setWidth(width);
		}

		fireChange(node, ChangeType.ctResize, new Object[] { selects });
	}

	public HashMap<String, List<Integer>> getSelectRows() {
		HashMap<String, List<Integer>> rows = new HashMap<>();
		for (CellInfo cellInfo : selects.values()) {
			List<Integer> rowIndexs = new ArrayList<>();
			for (int i = cellInfo.startRow; i < cellInfo.endRow; i++) {
				rowIndexs.add(i);
			}

			if (rowIndexs.size() > 0)
				rows.put(cellInfo.id, rowIndexs);
		}

		return rows;
	}

	public void setRowCount(int count) {
		boolean needFire = true;
		try {
			pushCommand();

			if (count > rows.size()) {
				for (int i = rows.size(); i < count; i++) {
					addRow(rows.size());
				}
			} else if (count < rows.size()) {
				for (CellInfo cellInfo : getCells().values()) {
					if (cellInfo.endRow > count) {
						needFire = false;
						EditorEnvironment.showMessage("单元格【" + cellInfo.editor.getDrawInfo().name + " - "
								+ cellInfo.editor.getDrawInfo().title + "】包含得行大于要设置的行数，不能修改行数！");
						selects.clear();
						selects.put(cellInfo, cellInfo);
						node.getCanvas().repaint();
						return;
					}
				}

				while (rows.size() > count) {
					removeRow(rows.size() - 1, false);
				}

				node.getCanvas().repaint();
			}
		} finally {
			if (needFire) {
				fireChange(node, ChangeType.ctReportChangeRowCount, new Object[] {});
			}
		}
	}

	public void setColCount(int count) {
		boolean needFire = true;
		try {
			pushCommand();

			if (count > cols.size()) {
				for (int i = cols.size(); i < count; i++) {
					addCol(cols.size());
				}
			} else if (count < cols.size()) {
				for (CellInfo cellInfo : getCells().values()) {
					if (cellInfo.endCol > count) {
						needFire = false;
						EditorEnvironment.showMessage("单元格【" + cellInfo.editor.getDrawInfo().name + " - "
								+ cellInfo.editor.getDrawInfo().title + "】包含得行大于要设置的列数，不能修改列数！");
						selects.clear();
						selects.put(cellInfo, cellInfo);
						node.getCanvas().repaint();
						return;
					}
				}

				while (cols.size() > count) {
					removeCol(cols.size() - 1, false);
				}

				node.getCanvas().repaint();
			}
		} finally {
			if (needFire) {
				fireChange(node, ChangeType.ctReportChangeColCount, new Object[] {});
			}
		}
	}

	public void setRowSize(int row, int size) {
		CellConfigInfo info = this.rows.get(row);
		if (size > 0) {
			pushCommand();

			info.height = size;
			resetScales(autoSize, false);

			fireChange(node, ChangeType.ctReportChangeRowSize, new Object[] { row, size });
		}
	}

	public void setColSize(int col, int size) {
		CellConfigInfo info = this.cols.get(col);
		if (size > 0) {
			pushCommand();

			info.width = size;
			resetScales(autoSize, false);

			fireChange(node, ChangeType.ctReportChangeColSize, new Object[] { col, size });
		}
	}

	public void resetScales() {
		resetScales(autoSize, true);
	}

	/**
	 * 重置单元格的比例尺，以完成报表的等比例缩放
	 * 
	 * @param resetRect
	 *            是否重新计算报表的尺寸，true重新计算，其他不计算
	 */
	public void resetScales(boolean resetRect, boolean pushCommand) {

		if (pushCommand)
			pushCommand();

		int rowHeight = 0;

		for (CellConfigInfo cellConfigInfo : this.rows) {
			rowHeight += cellConfigInfo.height;
		}
		for (int index = 0; index < this.rows.size(); index++) {
			CellConfigInfo element = this.rows.get(index);
			element.scale = element.height / (double) rowHeight;
		}

		int colWidth = 0;

		for (CellConfigInfo cellConfigInfo : this.cols) {
			colWidth += cellConfigInfo.width;
		}
		for (int index = 0; index < this.cols.size(); index++) {
			CellConfigInfo element = this.cols.get(index);
			element.scale = element.width / (double) colWidth;
		}

		if (resetRect) {
			Rectangle r = getRect();
			r.width = colWidth;
			r.height = rowHeight;

			node.setRect(r);
		}

		fireChange(node, ChangeType.ctReportResetScale, new Object[] { resetRect });

	}

	/**
	 * 根据报表的尺寸重新计算行列的宽高
	 */
	public void resetRowColumn() {
		pushCommand();

		Rectangle r = getRect();
		int width = r.width;
		int height = r.height;

		for (int index = 0; index < this.cols.size(); index++) {
			CellConfigInfo info = this.cols.get(index);
			info.width = (int) Math.floor(width * info.scale);
		}

		for (int index = 0; index < this.rows.size(); index++) {
			CellConfigInfo info = this.rows.get(index);
			info.height = (int) Math.floor(height * info.scale);
		}

		fireChange(node, ChangeType.ctReportResetCellSize, new Object[] {});
	}

	/**
	 * 根据单元格的尺寸重置报表的尺寸
	 */
	public void autoRect(boolean pushCommand) {
		if (pushCommand)
			pushCommand();

		Rectangle last = new Rectangle(Integer.MAX_VALUE, Integer.MAX_VALUE, 0, 0);
		for (CellInfo cell : getCells().values()) {
			Rectangle rectangle = cell.getRect();
			if (rectangle.x < last.x)
				last.x = rectangle.x;
			if (rectangle.y < last.y)
				last.y = rectangle.y;
			if (rectangle.x + rectangle.width > last.x + last.width)
				last.width += rectangle.x + rectangle.width - (last.x + last.width);
			if (rectangle.y + rectangle.height > last.y + last.height)
				last.height += rectangle.y + rectangle.height - (last.y + last.height);
		}
		node.setRect(last);
		fireChange(node, ChangeType.ctReportResetSize, new Object[] {});
	}

	public void drawNode(Graphics g, Rectangle rect) {
		drawNode(g, rect, false);
	}
	
	protected void drawNode(Graphics g, Rectangle rect, boolean clip) {
		Color old = g.getColor();
		g.setColor(backgroundColor);
		g.fillRect(rect.x, rect.y, rect.width, rect.height);
		g.setColor(old);
		for (CellInfo info : getCells().values()) {
			Rectangle r = getCellRect(info);
			if (!info.editor.getRect().equals(r))
				info.editor.setRect(r);

			if (info.editor.getDrawInfo().backgroundColor != null) {
				g.setColor(info.editor.getDrawInfo().backgroundColor);
				g.fillRect(r.x, r.y, r.width, r.height);
			}

			g.setColor(lineColor);

			g.drawRect(r.x, r.y, r.width, r.height);

			g.setColor(old);

			Font oldFont = info.editor.info.font;
			if (info.editor.getDrawInfo().isHref) {
				info.editor.info.font = new Font(oldFont.getName(), oldFont.getStyle() | Font.ITALIC,
						oldFont.getSize());
			} else {
				if ((oldFont.getStyle() & Font.ITALIC) != 0)
					info.editor.info.font = new Font(oldFont.getName(), oldFont.getStyle() ^ Font.ITALIC,
							oldFont.getSize());
			}

			String valueText = info.editor.info.value != null ? info.editor.info.value.toString() : "";

			switch (info.editor.info.typeName()) {
			case DrawInfoDefines.ComboBox_Name:
			case DrawInfoDefines.ComboTreeBox_Name: {
				ComboInfo info2 = (ComboInfo) info.editor.info;
				String text = "";
				if (info2.data != null && !info2.data.isEmpty() & info2.value != null) {
					try {
						JSONArray data = (JSONArray) JsonHelp.parseJson(info2.data);
						for (int i = 0; i < data.length(); i++) {
							JSONObject obj = data.getJSONObject(i);
							if (obj.getString(info2.valueField).compareToIgnoreCase(info2.value.toString()) == 0) {
								text = obj.getString(info2.textField);
								break;
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();

					}
				}
				DrawNode.drawLineText(g, info.editor.info.font, info.editor.info.textColor, r.x, r.y, r.width, r.height,
						text);
			}
				break;
			case DrawInfoDefines.DateBox_Name:
			case DrawInfoDefines.TimeBox_Name: {
				TimeInfo info3 = (TimeInfo) info.editor.info;
				String text = info3.getDisplayText();
				DrawNode.drawLineText(g, info.editor.info.font, info.editor.info.textColor, r.x, r.y, r.width, r.height,
						text);
			}
				break;
			case DrawInfoDefines.Label_Name:
			case DrawInfoDefines.TextBox_Name:
			case DrawInfoDefines.Button_Name:
				DrawNode.drawLineText(g, info.editor.info.font, info.editor.info.textColor, r.x, r.y, r.width, r.height,
						valueText);
				break;
			case DrawInfoDefines.TextArea_Name:
				DrawNode.drawMulitText(g, info.editor.info.font, info.editor.info.textColor, r, valueText);
				break;

			case DrawInfoDefines.ListBox_Name:
				ListBoxInfo listBoxInfo = (ListBoxInfo) info.editor.info;
				if (listBoxInfo.data != null && listBoxInfo.data.isEmpty())
					continue;
				try {
					if (listBoxInfo.data == null || listBoxInfo.data.isEmpty())
						continue;
					JSONArray value = (JSONArray) JsonHelp.parseJson(listBoxInfo.data);
					if (value == null)
						continue;

					String strValue = null;
					for (int i = 0; i < value.length(); i++) {
						JSONObject json = value.getJSONObject(i);
						if (strValue == null)
							strValue = json.getString("text");
						else
							strValue += "\n" + json.getString("text");
					}

					if (strValue != null && !strValue.isEmpty())
						DrawNode.drawMulitText(g, info.editor.info.font, info.editor.info.textColor, r, strValue);

				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			case DrawInfoDefines.CheckBox_Name:
				BufferedImage img;
				if (valueText.compareToIgnoreCase("true") == 0)
					img = UINode.getImage("icon_box-checked");
				else {
					img = UINode.getImage("icon_box-empty");
				}

				int left = (r.width - img.getWidth()) / 2;
				if (left < 0)
					left = 0;
				int top = (r.height - img.getHeight()) / 2;
				if (top < 0)
					top = 0;
				left += r.x;
				top += r.y;
				g.drawImage(img, left, top, null);
				break;
			case DrawInfoDefines.RadioBox_Name:
				info.editor.info.drawNode(g, r);
				break;
			case DrawInfoDefines.Div_Name:
			case DrawInfoDefines.Image_Name:
			case DrawInfoDefines.Tree_Name:
			case DrawInfoDefines.Chart_Name:
			case DrawInfoDefines.ListView_Name:
			case DrawInfoDefines.Grid_Name:
				info.editor.info.drawNode(g, r);
				break;
			}

			if (selects.containsKey(info)) {
				Graphics2D g2d = (Graphics2D) g;
				g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.7f));// 1.0f为透明度
																							// ，值从0-1.0，依次变得不透明
				g2d.setColor(Color.BLUE);
				g2d.fillRect(r.x, r.y, r.width, r.height);
				g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
			}

		}

	}

	static class CheckResult {
		public enum ResultType {
			rtRow, rtCol, rtNone
		}

		public ResultType resultType = ResultType.rtNone;

		public Point point;
		public int index = -1;
	}

	protected boolean checkInCell(Point pt) {
		for (CellInfo cellInfo : getCells().values()) {
			Rectangle r = getCellRect(cellInfo);
			r.x += 2;
			r.y += 2;
			r.width -= (r.width > 4 ? 4 : 0);
			r.height -= (r.height > 4 ? 4 : 0);

			if (r.contains(pt)) {
				return true;
			}
		}
		return false;
	}

	protected boolean checkLine(Point pt, CheckResult result) {
		Rectangle rect = new Rectangle(getRect());
		pt = node.getCanvas().getRealPoint(pt);
		Rectangle r = new Rectangle(pt.x - 1, pt.y - 1, 2, 2);

		if (checkInCell(pt))
			return false;

		for (int i = 0; i < rows.size(); i++) {
			rect.y += rows.get(i).height;
			Line2D.Float line = new Line2D.Float((float) rect.x, (float) rect.y, (float) rect.x + rect.width,
					(float) rect.y);

			if (r.intersectsLine(line)) {
				result.index = i;
				result.point = pt;
				result.resultType = ResultType.rtRow;
				return true;
			}
		}

		rect = new Rectangle(getRect());
		for (int i = 0; i < cols.size(); i++) {
			rect.x += cols.get(i).width;
			Line2D.Float line = new Line2D.Float((float) rect.x, (float) rect.y, (float) rect.x,
					(float) rect.y + rect.height);

			if (r.intersectsLine(line)) {
				result.index = i;
				result.point = pt;
				result.resultType = ResultType.rtCol;
				return true;
			}
		}

		return false;
	}

	enum ResizeMode {
		rmRow, rmCol, rmNode
	}

	public void replaceCellDrawInfo(UINode node, DrawInfo info) {
		pushCommand();

		node.info = info;

		fireChange(node, ChangeType.ctReportChangeCell, new Object[] { node, info });
	}

	public void removeListener() {
		if (node.getCanvas() != null) {
			node.getCanvas().removeNodeMouseListener(mouseListener);
			node.getCanvas().removeNodeMouseMotionListener(mouseMotionAdapter);
		}
	}

	public void finalize() throws Throwable {
		removeListener();
		super.finalize();
	}

	public interface IClick {
		public void onClick();
	}

	MouseListener mouseListener = new MouseAdapter() {

		@Override
		public void mouseReleased(MouseEvent e) {
			if (!allowEdit)
				return;

			node.getCanvas().getAcm().end(e.getPoint());

			resizeMode = ResizeMode.rmNode;
			resizeIndex = -1;
			oldValue = 0;
			oldPoint = null;
			node.getCanvas().setMouseMode(MouseMode.mmNone);
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (!allowEdit)
				return;

			CheckResult result = new CheckResult();
			if (checkLine(e.getPoint(), result)) {
				selects.clear();
				node.getCanvas().setMouseMode(MouseMode.mmCustom);
				oldPoint = new Point(e.getPoint());
				resizeIndex = result.index;
				switch (result.resultType) {
				case rtCol:
					resizeMode = ResizeMode.rmCol;
					oldValue = cols.get(result.index).width;
					break;
				case rtRow:
					resizeMode = ResizeMode.rmRow;
					oldValue = rows.get(result.index).height;
					break;
				default:
					return;
				}

				node.getCanvas().getAcm().startPush(oldPoint, null, CommandInfoType.ctUpdateAttr);
				pushCommand();
			} else {
				Point realPoint = node.getCanvas().getRealPoint(e.getPoint());
				CellInfo selectInfo = null;
				for (CellInfo info : getCells().values()) {
					Rectangle r = getCellRect(info);

					if (r.contains(realPoint)) {
						selectInfo = info;
						break;
					}
				}

				if (selectInfo != null) {
					if (e.isControlDown()) {
						if (selects.containsKey(selectInfo))
							selects.remove(selectInfo);
						else
							selects.put(selectInfo, selectInfo);
					} else {
						selects.clear();
						selects.put(selectInfo, selectInfo);
					}
					node.getCanvas().repaint();
				} else {
					selects.clear();
				}
			}

			if (onClick != null)
				onClick.onClick();
		}

	};

	MouseMotionAdapter mouseMotionAdapter = new MouseMotionAdapter() {
		@Override
		public void mouseMoved(MouseEvent e) {
			if (!allowEdit)
				return;
			switch (resizeMode) {
			case rmCol:
				cols.get(resizeIndex).width = oldValue + (e.getX() - oldPoint.x);
				resetScales(autoSize, false);

				node.getCanvas().repaint();
				break;
			case rmRow:
				rows.get(resizeIndex).height = oldValue + (e.getY() - oldPoint.y);
				resetScales(autoSize, false);
				node.getCanvas().repaint();
				break;
			case rmNode:
				break;
			}

			boolean resize = false;
			CheckResult result = new CheckResult();
			if (node.getCanvas().getMouseMode() == MouseMode.mmNone && checkLine(e.getPoint(), result)) {
				switch (result.resultType) {
				case rtCol:
					node.getCanvas().setCurCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
					resize = true;
					break;
				case rtRow:
					node.getCanvas().setCurCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
					resize = true;
					break;
				default:
					return;
				}
			}

			if (!resize) {
				switch (node.getCanvas().getMouseMode()) {
				case mmRectSelect:
					Rectangle rect = node.getCanvas().getSelectRect();
					selects.clear();
					for (CellInfo cellInfo : getCells().values()) {
						Rectangle r = getCellRect(cellInfo);
						if (r == null)
							continue;

						if (rect.contains(r))
							selects.put(cellInfo, cellInfo);
					}
					node.getCanvas().repaint();
					break;
				default:
					break;
				}
			}
		}
	};

	public CellInfo merge() {
		List<CellInfo> list = new ArrayList<>(selects.values());
		selects.clear();
		return merge(list);
	}

	public CellInfo merge(List<CellInfo> cellInfos) {
		pushCommand();

		CellInfo info = new CellInfo(this);
		info.startCol = Integer.MAX_VALUE;
		info.startRow = Integer.MAX_VALUE;
		for (CellInfo cellInfo : cellInfos) {
			if (info.startCol > cellInfo.startCol)
				info.startCol = cellInfo.startCol;

			if (info.endCol < cellInfo.endCol)
				info.endCol = cellInfo.endCol;

			if (info.startRow > cellInfo.startRow)
				info.startRow = cellInfo.startRow;

			if (info.endRow < cellInfo.endRow)
				info.endRow = cellInfo.endRow;
		}

		for (int i = info.startRow; i < info.endRow; i++) {
			for (int j = info.startCol; j < info.endCol; j++) {
				String key = new Key(i, j).toString();
				if (getCells().containsKey(key))
					removeCell(key);
			}
		}

		info.editor = cellInfos.get(0).editor;
		getCells().put(new Key(info).toString(), info);

		fireChange(node, ChangeType.ctReportMerge, new Object[] { info, cellInfos });

		node.getCanvas().repaint();

		return info;
	}

	public void split() {
		split(new ArrayList<>(selects.values()));
		selects.clear();
	}

	public List<CellInfo> split(CellInfo cellInfo) {
		pushCommand();

		List<CellInfo> result = new ArrayList<>();

		if (cellInfo.endCol > cellInfo.startCol || cellInfo.endRow > cellInfo.startRow) {
			boolean first = true;
			removeCell(new Key(cellInfo).toString());
			List<CellInfo> infos = new ArrayList<>();
			for (int i = cellInfo.startRow; i < cellInfo.endRow; i++) {
				for (int j = cellInfo.startCol; j < cellInfo.endCol; j++) {
					CellInfo info = new CellInfo(this);
					result.add(info);

					info.startCol = j;
					info.startRow = i;
					info.endCol = info.startCol + 1;
					info.endRow = info.startRow + 1;
					if (first) {
						first = false;
						info.editor = cellInfo.editor;
					} else {
						newEditor(info);
					}

					infos.add(info);
					getCells().put(new Key(info).toString(), info);
				}
			}

			fireChange(node, ChangeType.ctReportSplit, new Object[] { cellInfo, infos });

		}

		return result;
	}

	public void split(List<CellInfo> cellInfos) {
		for (CellInfo cellInfo : cellInfos) {
			split(cellInfo);
		}
		node.getCanvas().repaint();
	}

	public void init() {
		if (node.getCanvas() != null) {
			removeListener();
			node.getCanvas().addNodeMouseListener(mouseListener);
			node.getCanvas().addNodeMouseMotionListener(mouseMotionAdapter);
			if (getCells().size() == 0)
				init(9, 5);
		}
	}

	public ReportInfo(UINode node) {
		super(node);
		width = "400";
		height = "400";
		needFrame = true;
		allowEdit = false;
		init();
	}

	public TreeMap<String, CellInfo> getCells() {
		return cells;
	}

	public void setCells(TreeMap<String, CellInfo> cells) {
		this.cells = cells;
	}

}