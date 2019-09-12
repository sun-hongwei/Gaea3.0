package com.wh.draws.drawinfo;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wh.draws.DrawNode;
import com.wh.draws.UINode;
import com.wh.system.tools.JsonHelp;

public class GridInfo extends DrawInfo{
		public String typeName(){
			return DrawInfoDefines.Grid_Name;
		}

		public GridInfo(UINode node) {
			super(node);
			width = "200px";
			height = "200px";
			value = "";
		}

		HashMap<String, Rectangle> rowCells = new HashMap<>();
		
		public void drawNode(Graphics g, Rectangle rect){
			if (header == null || header.isEmpty())
				return;
			
			try {
				JSONObject json = (JSONObject) JsonHelp.parseJson(header);
				
				JSONArray values = null;
				if (value != null && !value.toString().isEmpty()){
					try{
						JSONObject tmp = (JSONObject)JsonHelp.parseJson(value.toString());
						if (tmp.has("data"))
							values = tmp.getJSONArray("data");
					}catch(Exception e){
						value = "{}";
					}
				}
				JSONArray headers = json.getJSONArray("header");
				int rowHeight = 40;
				int headerHeight = rowHeight;
				for (int i = 0; i < headers.length(); i++) {
					JSONObject column = headers.getJSONObject(i);
					if (column.has("subs")){
						headerHeight = headerHeight * 2;
						break;
					}
				}
				
				int left = rect.x;
				int top = rect.y;
				for (int i = 0; i < headers.length(); i++) {
					JSONObject column = headers.getJSONObject(i);
					int realHeight = headerHeight;
					int width = 60;
					if (column.has("width"))
						width = column.getInt("width");
					
					if (column.has("subs")){
						realHeight = realHeight / 2;
						JSONArray subs = column.getJSONArray("subs");
						width = 0;
						int subLeft = left;
						for (int j = 0; j < subs.length(); j++) {
							JSONObject sub = subs.getJSONObject(j);
							int subWidth = 80;
							switch(JsonHelp.getString(sub, "type")){
							case"4":
								break;
							default:
								subWidth = sub.getInt("width");
							}
							int subTop = top + realHeight;
							Rectangle drawRect = new Rectangle(subLeft, subTop, subWidth, realHeight);
							
							if (sub.has("field"))
								rowCells.put(sub.getString("field"), new Rectangle(drawRect.x, drawRect.y + drawRect.height, drawRect.width, rowHeight));

							if (rect.contains(new Point(drawRect.x + drawRect.width, drawRect.y + drawRect.height))){
								g.drawRect(drawRect.x, drawRect.y, drawRect.width, drawRect.height);
								DrawNode.drawLineText(g, getFont(), textColor, drawRect.x, drawRect.y, drawRect.width, drawRect.height, sub.getString("title"));
							}
							subLeft += subWidth;
							width += subWidth;
						}
					}

					if (column.has("field"))
						rowCells.put(column.getString("field"), new Rectangle(left, top + realHeight, width, rowHeight));
					
					if (rect.contains(new Point(left + width, top + realHeight))){
						g.drawRect(left, top, width, realHeight);
						switch(column.get("type").toString()){
//						case "4":
//							DrawNode.drawLineText(g, getFont(), textColor, left, top, 80, realHeight, "");
						case "6":
							DrawNode.drawLineText(g, getFont(), textColor, left, top, width, realHeight, "✔");
							break;
						default:
							DrawNode.drawLineText(g, getFont(), textColor, left, top, width, realHeight, column.getString("title"));
							break;
						}
					}
					
					left += width;
				}
				
				if (values != null){
					for (int i = 0; i < values.length(); i++) {
						JSONObject val = values.getJSONObject(i);
						Iterator<?> keys = val.keys();
						while (keys.hasNext()){
							String key = (String)keys.next();
							String v = JsonHelp.getString(val, key);
							if (rowCells.containsKey(key)){
								Rectangle r = rowCells.get(key);
								if (!rect.contains(new Point(r.x + r.width, r.y + r.height)))
									continue;
								
								DrawNode.drawLineText(g, getFont(), textColor, r.x, r.y, r.width, r.height, v);
							}
						}
						
						for (Rectangle r : rowCells.values()) {
							r.y += r.height;
						}
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		public JSONObject toJson(boolean needAll) throws JSONException{
			JSONObject json = super.toJson(needAll);
			
			json.put("autoRunFlowTasks", autoRunFlowTasks);
			json.put("autoRunFlowHistoryTasks", autoRunFlowHistoryTasks);
			json.put("idField", needAll && idField == null ? "" : idField);
			json.put("sortMode", needAll && sortMode == null ? "" : sortMode);
			json.put("showReloadButton", needAll && showReloadButton == null ? "false" : showReloadButton);
			json.put("allowResize", needAll && allowResize == null ? "false" : allowResize);
			json.put("allowcelledit", needAll && allowcelledit == null ? "false" : allowcelledit);
			json.put("allowcellselect", needAll && allowcellselect == null ? "true" : allowcellselect);
			json.put("multiselect", needAll && multiselect == null ? "false" : multiselect);
			json.put("showSummaryRow", needAll && showSummaryRow == null ? "" : showSummaryRow);
			json.put("showFilterRow", needAll && showFilterRow == null ? "false" : showFilterRow);
			json.put("header", needAll && header == null ? "" : header);
			json.put("initData", needAll && initData == null ? "" : initData);
			json.put("showPager", showPager);
			json.put("links", needAll && links == null ? new JSONArray() : links);
			
			json.put("allowCellWrap", allowCellWrap);
			json.put("allowHeaderWrap", allowHeaderWrap);
			json.put("allowRowSelect", allowRowSelect);
			json.put("onlyCheckSelection", onlyCheckSelection);
			json.put("editNextOnEnterKey", editNextOnEnterKey);
			json.put("cellEditAction", cellEditAction);
			json.put("allowCellValid", allowCellValid);
			json.put("allowUnselect", allowUnselect);
			json.put("allowAlternating", allowAlternating);
			json.put("collapseGroupOnLoad", collapseGroupOnLoad);
			json.put("autoHideRowDetail", autoHideRowDetail);
			json.put("showModified", showModified);
			json.put("enableGroupOrder", enableGroupOrder);
			json.put("skipReadOnlyCell", skipReadOnlyCell);
			json.put("navEditMode", navEditMode);
			json.put("showEmptyText", showEmptyText);
			json.put("frozenStartColumn", frozenStartColumn);
			json.put("frozenEndColumn", frozenEndColumn);
			json.put("emptyText", needAll && emptyText == null ? "" : emptyText);
			json.put("pageSize", pageSize);
			json.put("selectIdField", needAll && selectIdField == null ? "" : selectIdField);
			json.put("selectTextField", needAll && selectTextField == null ? "" : selectTextField);

			return json;
		}
		
		public void fromJson(JSONObject json) throws JSONException{
			super.fromJson(json);

			if (json.has("allowCellWrap"))
				allowCellWrap = json.getBoolean("allowCellWrap");
			else
				allowCellWrap = false;
			
			if (json.has("allowHeaderWrap"))
				allowHeaderWrap = json.getBoolean("allowHeaderWrap");
			else
				allowHeaderWrap = false;
			
			if (json.has("allowRowSelect"))
				allowRowSelect = json.getBoolean("allowRowSelect");
			else
				allowRowSelect = true;
			
			if (json.has("onlyCheckSelection"))
				onlyCheckSelection = json.getBoolean("onlyCheckSelection");
			else
				onlyCheckSelection = false;
			
			if (json.has("editNextOnEnterKey"))
				editNextOnEnterKey = json.getBoolean("editNextOnEnterKey");
			else
				editNextOnEnterKey = false;
			
			if (json.has("cellEditAction"))
				cellEditAction = json.getBoolean("cellEditAction");
			else
				cellEditAction = false;
			
			if (json.has("allowCellValid"))
				allowCellValid = json.getBoolean("allowCellValid");
			else
				allowCellValid = true;
			
			if (json.has("allowUnselect"))
				allowUnselect = json.getBoolean("allowUnselect");
			else
				allowUnselect = false;
			
			if (json.has("allowAlternating"))
				allowAlternating = json.getBoolean("allowAlternating");
			else
				allowAlternating = false;
			
			if (json.has("collapseGroupOnLoad"))
				collapseGroupOnLoad = json.getBoolean("collapseGroupOnLoad");
			else
				collapseGroupOnLoad = true;
			
			if (json.has("autoHideRowDetail"))
				autoHideRowDetail = json.getBoolean("autoHideRowDetail");
			else
				autoHideRowDetail = false;
			
			if (json.has("showModified"))
				showModified = json.getBoolean("showModified");
			else
				showModified = false;
			
			if (json.has("enableGroupOrder"))
				enableGroupOrder = json.getBoolean("enableGroupOrder");
			else
				enableGroupOrder = true;
			
			if (json.has("skipReadOnlyCell"))
				skipReadOnlyCell = json.getBoolean("skipReadOnlyCell");
			else
				skipReadOnlyCell = true;
			
			if (json.has("navEditMode"))
				navEditMode = json.getBoolean("navEditMode");
			else
				navEditMode = false;

			if (json.has("showEmptyText"))
				showEmptyText = json.getBoolean("showEmptyText");
			else
				showEmptyText = false;

			if (json.has("frozenStartColumn") && json.get("frozenStartColumn") instanceof Integer)
				frozenStartColumn = json.getInt("frozenStartColumn");
			else
				frozenStartColumn = -1;
			
			if (json.has("frozenEndColumn") && json.get("frozenEndColumn") instanceof Integer)
				frozenEndColumn = json.getInt("frozenEndColumn");
			else
				frozenEndColumn = -1;
			
			if (json.has("emptyText"))
				emptyText = json.getString("emptyText");
			else
				emptyText = null;

			if (json.has("showPager"))
				showPager = json.getBoolean("showPager");
			else
				showPager = true;
			
			if (json.has("idField"))
				idField = json.getString("idField");
			else
				idField = null;
			
			if (json.has("sortMode"))
				sortMode = json.getString("sortMode");
			else
				sortMode = "client";

			if (json.has("showReloadButton"))
				showReloadButton = json.getString("showReloadButton");
			else
				showReloadButton = "true";
			
			if (json.has("allowResize"))
				allowResize = json.getString("allowResize");
			else
				allowResize = "false";
			
			if (json.has("allowcelledit"))
				allowcelledit = json.getString("allowcelledit");
			else
				allowcelledit = "false";
			
			if (json.has("allowcellselect"))
				allowcellselect = json.getString("allowcellselect");
			else
				allowcellselect = "false";
			
			if (json.has("multiselect"))
				multiselect = json.getString("multiselect");
			else
				multiselect = "true";
			
			if (json.has("showSummaryRow"))
				showSummaryRow = json.getString("showSummaryRow");
			else
				showSummaryRow = "false";
			
			if (json.has("showFilterRow"))
				showFilterRow = json.getString("showFilterRow");
			else
				showFilterRow = "false";
			
			if (json.has("header"))
				header = json.getString("header");
			else
				header = null;

			if (json.has("onSelectCompare"))
				onSelectCompare = json.getString("onSelectCompare");
			else
				onSelectCompare = null;

			if (json.has("onDeselectCompare"))
				onDeselectCompare = json.getString("onDeselectCompare");
			else
				onDeselectCompare = null;

			if (json.has("OnChangeCellStyle"))
				OnChangeCellStyle = json.getString("OnChangeCellStyle");
			else
				OnChangeCellStyle = null;

			if (json.has("initData"))
				initData = json.getString("initData");
			else
				initData = null;

			if (json.has("links"))
				links = json.getJSONArray("links");
			else
				links = null;

			if (json.has("autoRunFlowTasks"))
				autoRunFlowTasks = json.getBoolean("autoRunFlowTasks");
			else
				autoRunFlowTasks = false;

			if (json.has("autoRunFlowHistoryTasks"))
				autoRunFlowHistoryTasks = json.getBoolean("autoRunFlowHistoryTasks");
			else
				autoRunFlowHistoryTasks = false;
			
			if (json.has("pageSize"))
				pageSize = json.getInt("pageSize");
			else
				pageSize = 50;
			
			if (json.has("selectIdField"))
				selectIdField = json.getString("selectIdField");
			else
				selectIdField = null;

			if (json.has("selectTextField"))
				selectTextField = json.getString("selectTextField");
			else
				selectTextField = null;
			
		}

		public boolean autoRunFlowTasks = false;
		public boolean autoRunFlowHistoryTasks = false;
        public String idField;
        public String sortMode = "client";
        public String showReloadButton = "false";
        public String allowResize = "false";
        public String allowcelledit = "false";
        public String allowcellselect = "false";
        public String multiselect = "true";
        public String showSummaryRow = "false";
        public String showFilterRow = "false";
        public String header;
        public String onSelectCompare;
        public String onDeselectCompare;
        public String OnChangeCellStyle;
        public String initData;
        public JSONArray links = null;
        public boolean showPager = true;

        public boolean allowCellWrap = false;
        public boolean allowHeaderWrap = false;
        public boolean allowRowSelect = true;
        public boolean onlyCheckSelection = false;
        public boolean editNextOnEnterKey = false;
        public boolean cellEditAction = false;
        public boolean allowCellValid = true;
        public boolean allowUnselect = false;
        public boolean allowAlternating = false;
        public boolean collapseGroupOnLoad = true;
        public boolean autoHideRowDetail = false;
        public boolean showModified = true;
        public boolean enableGroupOrder = true;
        public boolean skipReadOnlyCell = true;
        public boolean navEditMode = false;
        public boolean showEmptyText = false;
        public int frozenStartColumn = -1;
        public int frozenEndColumn = -1;
        public String emptyText;
        
        public String selectIdField;
        public String selectTextField;
        
        public int pageSize = 50;
        
        	
	}