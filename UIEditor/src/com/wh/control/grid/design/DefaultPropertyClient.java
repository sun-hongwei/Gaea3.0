package com.wh.control.grid.design;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sunking.swing.JFontDialog;
import com.wh.control.EditorEnvironment;
import com.wh.control.combobox.MulitCombobox;
import com.wh.control.datasource.define.DataSource;
import com.wh.control.datasource.define.DataSource.FieldNameInfo;
import com.wh.control.datasource.define.FileDataSource;
import com.wh.control.datasource.define.SQLDataSource;
import com.wh.control.datasource.define.UrlDataSource;
import com.wh.control.grid.ButtonColumn;
import com.wh.control.grid.ButtonColumn.ButtonLabel;
import com.wh.control.grid.GridCellEditor.ActionResult;
import com.wh.control.grid.design.PropertyPanel.IProperty;
import com.wh.control.grid.design.PropertyTableCellEditor.IClientEvent;
import com.wh.control.grid.design.PropertyTableCellEditor.KeyValue;
import com.wh.dialog.editor.ControlSelectDialog;
import com.wh.dialog.editor.JsonEditorDialog;
import com.wh.dialog.editor.JsonTreeDataConfigDialog;
import com.wh.dialog.editor.MenuEditorDialog;
import com.wh.dialog.editor.ModelflowSelectDialog;
import com.wh.dialog.editor.ModelflowSelectDialog.Result;
import com.wh.dialog.editor.NavigationEditorDialog;
import com.wh.dialog.editor.UISelectDialog;
import com.wh.dialog.editor.UISelectDialog.IAdd;
import com.wh.dialog.input.ListInput;
import com.wh.dialog.input.MulitTextInput;
import com.wh.dialog.selector.CheckSelector;
import com.wh.dialog.selector.KeyValueSelector;
import com.wh.dialog.selector.KeyValueSelector.ICheckValue;
import com.wh.dialog.selector.KeyValueSelector.IEditRow;
import com.wh.dialog.selector.KeyValueSelector.ModelResult;
import com.wh.draws.DrawCanvas;
import com.wh.draws.DrawNode;
import com.wh.draws.UINode;
import com.wh.draws.WorkflowNode;
import com.wh.draws.control.ActionCommandManager.CommandInfoType;
import com.wh.draws.drawinfo.ChartInfo.ChartType;
import com.wh.draws.drawinfo.ComboInfo;
import com.wh.draws.drawinfo.DivInfo;
import com.wh.draws.drawinfo.DivInfo.DivType;
import com.wh.draws.drawinfo.DrawInfo;
import com.wh.draws.drawinfo.DrawInfo.Align;
import com.wh.draws.drawinfo.DrawInfo.PlaceAlign;
import com.wh.draws.drawinfo.DrawInfo.PlaceInType;
import com.wh.draws.drawinfo.DrawInfo.ScaleMode;
import com.wh.draws.drawinfo.DrawInfoDefines;
import com.wh.draws.drawinfo.GridInfo;
import com.wh.draws.drawinfo.ImageInfo;
import com.wh.draws.drawinfo.ListViewInfo;
import com.wh.draws.drawinfo.TimerInfo;
import com.wh.draws.drawinfo.ToolbarInfo;
import com.wh.form.DataSourceManager;
import com.wh.form.IMainControl;
import com.wh.system.tools.ColorConvert;
import com.wh.system.tools.FileHelp;
import com.wh.system.tools.JsonHelp;
import com.wh.system.tools.Tools;

public class DefaultPropertyClient implements IClientEvent {

	PropertyPanel table;
	DrawCanvas canvas;
	IMainControl mainControl;
	IUpdate iUpdate;

	public static class IUpdateAdapter implements IUpdate {

		@Override
		public void setEditState(boolean isEdit) {
		}

		@Override
		public void onEdit(int row, int col) {

		}

		@Override
		public void onUpdateEnd(Object obj, String attrName, Object oldValue, Object attrValue) {
		}

		@Override
		public String getUIID() {
			return null;
		}

		@Override
		public Object showButtonEditJsonEditor(String name, ButtonLabel buttonEdit) throws JSONException {
			return null;
		}

		@Override
		public Component getParent() {
			return null;
		}

	}

	public interface IUpdate {
		void setEditState(boolean isEdit);

		void onEdit(int row, int col);

		void onUpdateEnd(Object obj, String attrName, Object oldValue, Object attrValue);

		String getUIID();

		Object showButtonEditJsonEditor(String name, ButtonLabel buttonEdit) throws JSONException;

		Component getParent();
	}

	public static class PropertyInfo implements IProperty {
		Object node;
		String name;
		Object value;
		String title;
		JComponent editor;
		DrawNode workflowNode;
		HashMap<String, DrawNode> workflowNodes;
		String workflowRelationTitle;
		Map<String, DataSource> dataSources = new HashMap<>();

		public PropertyInfo(Object node, String name, String title, Object defaultValue, DrawNode workflowNode,
				HashMap<String, DrawNode> workflowNodes, String workflowRelationTitle, List<DataSource> dataSources) {
			if (dataSources != null)
				for (DataSource dataSource : dataSources) {
					this.dataSources.put(dataSource.id, dataSource);
				}

			this.workflowNode = workflowNode;
			this.workflowNodes = workflowNodes;
			this.workflowRelationTitle = workflowRelationTitle;

			this.node = node;
			this.name = name;
			this.title = title;
			this.value = defaultValue;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getValue() {
			if (value == null)
				return "";
			return value.toString();
		}

		@SuppressWarnings("unchecked")
		@Override
		public JComponent getEditor() {
			if (editor != null)
				return editor;

			switch (name) {
			case "attachControlName":
				editor = new ButtonLabel(PropertyInfo.this, getValue());
				break;
			case "pageSize":
				editor = new JSpinner(new SpinnerNumberModel(Integer.parseInt(getValue()), 1, 1000, 1));
				break;
			case "decimalPlaces":
				editor = new JSpinner(new SpinnerNumberModel(Integer.parseInt(getValue()), 0, 20, 0));
				break;
			case "tabindex":
				editor = new JSpinner(
						new SpinnerNumberModel(Integer.parseInt(getValue().toString()), -1, 99999999, -1));
				break;
			case "uitype":
				editor = new JTextField();
				editor.setEnabled(false);
				((JTextField) editor).setText(getValue().toString());
				break;
			case "zOrder":
				editor = new JLabel();
				((JLabel) editor).setText(getValue().toString());
				break;
			case "divType":
				if (node instanceof UINode) {
					JComboBox<DivType> combo = new JComboBox<>(DivType.values());
					editor = combo;
					combo.setEditable(false);
					String vString = getValue();
					if (vString != null && !vString.isEmpty()){
						DivType divType = DivType.valueOf(vString);
						combo.setSelectedItem(divType);
					}
				}
				break;
			case "field":
				if (node instanceof UINode) {
					DrawInfo info = ((UINode) node).getDrawInfo();
					if (info.dataSource == null)
						break;

					if (!dataSources.containsKey(info.dataSource)) {
						value = null;
						break;
					}

					DataSource dataSource = dataSources.get(info.dataSource);

					if (dataSource.columns == null || dataSource.columns.length() == 0) {
						break;
					}

					List<FieldNameInfo> infos = new ArrayList<>(Arrays.asList(dataSource.getFieldNames()));
					infos.add(0, null);
					JComboBox<FieldNameInfo> combo = new JComboBox<>(infos.toArray(new FieldNameInfo[infos.size()]));
					editor = combo;
					combo.setEditable(false);
					combo.setSelectedItem(getValue());
				}
				break;
			case "selectIdField":
			case "selectTextField":
			case "inputId":
			case "outputId":
			case "barCode":
			case "timeFormat":
			case "increment":
			case "titleClass":
			case "placeGroup":
			case "code":
			case "command":
			case "functionParams":
			case "resultName":
			case "divClass":
			case "emptyText":
			case "styleClass":
			case "caption":
			case "download":
			case "href":
			case "url":
			case "parent":
			case "style":
			case "bodystyle":
			case "left":
			case "top":
			case "right":
			case "bottom":
			case "width":
			case "height":
			case "name":
			case "id":
			case "idField":
			case "valueField":
			case "textField":
			case "parentField":
			case "checkedField":
			case "onValueChanged":
			case "format":
			case "onClick":
			case "title":
			case "OnGetLoadParam":
			case "OnNodeClick":
			case "loadPage":
			case "loadingClass":
			case "nothingClass":
			case "onAfterLoad":
			case "onBeforeLoad":
			case "onBeforeSend":
			case "onError":
			case "onGetData":
			case "onGetPageHtml":
			case "onLoad":
			case "scrollTarget":
			case "subTitle":
			case "tag":
			case "pinyinField":
			case "codeTableName":
			case "scriptFileName":
			case "scriptEntryFunctionName":
			case "servicePageName":
			case "serviceCommand":
			case "state":
				editor = new JTextField();
				((JTextField) editor).setText(getValue().toString());
				break;
			case "value":
				if (!(node instanceof UINode)) {
					editor = new JTextField();
					((JTextField) editor).setText(getValue().toString());
					break;
				}

				switch (((UINode) node).getDrawInfo().typeName()) {
				case DrawInfoDefines.RadioBox_Name:
				case DrawInfoDefines.ListBox_Name:
				case DrawInfoDefines.Button_Name:
				case DrawInfoDefines.Label_Name:
				case DrawInfoDefines.TextBox_Name:
				case DrawInfoDefines.Password_Name:
				case DrawInfoDefines.TimeBox_Name:
				case DrawInfoDefines.DateBox_Name:
				case DrawInfoDefines.ComboTreeBox_Name:
				case DrawInfoDefines.ComboBox_Name:
				case DrawInfoDefines.ScrollBar_Name: {
					editor = new JTextField();
					((JTextField) editor).setText(getValue().toString());
					break;
				}
				case DrawInfoDefines.TextArea_Name: {
					String text = "";
					if (getValue() != null)
						text = getValue().toString();
					ButtonLabel bl = new ButtonLabel(PropertyInfo.this, text);
					bl.textField.setText(text);
					editor = bl;
					break;
				}
				case DrawInfoDefines.Image_Name:
					editor = new ButtonColumn.ButtonLabel(PropertyInfo.this, value);
					break;

				case DrawInfoDefines.Grid_Name:
				case DrawInfoDefines.Tree_Name:
					editor = new ButtonColumn.ButtonLabel(PropertyInfo.this, value);
					break;

				case DrawInfoDefines.Spinner_Name:
					editor = new JSpinner(
							new SpinnerNumberModel(Integer.parseInt((String) getValue()), 0, 99999999, 1));
					break;

				case DrawInfoDefines.CheckBox_Name:
					editor = new JCheckBox();
					((JCheckBox) editor).setSelected(Boolean.parseBoolean(getValue().toString()));
					break;
				default:
					break;
				}
				break;
			case "chartData": {
				String text = "";
				if (getValue() != null)
					text = getValue().toString();
				ButtonLabel bl = new ButtonLabel(PropertyInfo.this, text);
				bl.textField.setText(text);
				editor = bl;
				break;
			}
			case "dataSource":
				if (node instanceof UINode) {
					if (dataSources.size() == 0) {
						break;
					}
					List<DataSource> ds = new ArrayList<>(dataSources.values());
					ds.add(0, null);
					JComboBox<DataSource> combo = new JComboBox<>(ds.toArray(new DataSource[ds.size()]));
					editor = combo;
					combo.setEditable(false);
					combo.setSelectedItem(getValue());
				}

				break;
			case "scrollDiv":
			case "scrollControl":
			case "params":
				editor = new ButtonColumn.ButtonLabel(PropertyInfo.this, getValue());
				break;
			case "model_id":
				value = new KeyValue<>((JSONObject) value);
			case "uiInfo":
			case "attachID":
			case "buttonImage":
			case "img":
				editor = new ButtonColumn.ButtonLabel(PropertyInfo.this, value);
				break;
			case "dataSourceParams":
			case "control":
			case "decideValue":
			case "template":
			case "links":
			case "gradient":
			case "allows":
			case "decision":
			case "memo":
			case "data":
			case "header":
			case "extendData":
			case "initData":
			case "controlData":
			case "sharedata":
				editor = new ButtonColumn.ButtonLabel(PropertyInfo.this, value);
				break;

			case "frozenStartColumn":
			case "frozenEndColumn":
			case "row":
			case "rowIndex":
			case "expandTitleHeight":
			case "alignSpace":
			case "placeIndex":
			case "colDiv":
			case "rowDiv":
			case "attatchSpace":
			case "columns":
			case "start":
			case "reportWidth":
			case "reportHeight":
			case "size":
			case "headerHeight":
			case "lineHeight":
			case "margin_left":
			case "margin_top":
			case "margin_right":
			case "margin_bottom":
			case "minValue":
			case "maxValue":
			case "heightOffset":
			case "interval":
			case "times":
				try {
					Object intValue = getValue();
					if (intValue == null)
						intValue = "0";
					editor = new JSpinner(
							new SpinnerNumberModel(Integer.parseInt(intValue.toString()), -100, 99999999, 1));
				} catch (Exception e) {
					throw e;
				}
				break;
			case "fireDataSource":
			case "saveDataSource":
			case "role":
				editor = new ButtonLabel(PropertyInfo.this, value);
				break;
			case "timer_type":
				editor = new JComboBox<String>(new String[] { TimerInfo.CIRCULATION_TIMER, TimerInfo.ONCE_TIMER });
				((JComboBox<String>) editor).setSelectedIndex(0);
				break;
			case "mode":
				if (((UINode) node).getDrawInfo() instanceof ComboInfo)
					editor = new JComboBox<String>(new String[] { "combobox", "lookup" });
				else
					editor = new JComboBox<String>(new String[] { "button", "edit" });
				((JComboBox<String>) editor).setSelectedIndex(0);
				break;
			case "saveRowType":
				editor = new JComboBox<String>(new String[] { "added", "removed", "modified" });
				((JComboBox<ChartType>) editor).setSelectedItem(getValue().toString());
				break;
			case "chartType":
				editor = new JComboBox<ChartType>(ChartType.values());
				((JComboBox<ChartType>) editor).setSelectedItem(ChartType.valueOf(getValue().toString()));
				break;
			case "scaleMode":
				editor = new JComboBox<ScaleMode>(ScaleMode.values());
				((JComboBox<ScaleMode>) editor).setSelectedItem(ScaleMode.valueOf(getValue().toString()));

				break;
			case "vtype":
				JComboBox<String> combo = new JComboBox<String>(new String[] { "email", "url", "int", "float",
						"maxLength:6", "minLength:2", "rangeLength:2,6", "rangeChar:2,6", "range:0,100",
						"date:yyyy-MM-dd", "date:yyyy-MM-dd HH:mm:ss", "onEnglishValidation", "english",
						"onEnglishAndNumberValidation", "onChineseValidation", "onIDCardsValidation" });
				editor = combo;
				combo.setEditable(true);
				combo.setSelectedItem(getValue());
				break;
			case "viewType": {
				editor = new JComboBox<String>(new String[] { "选择框", "日历" });
				JComboBox<String> selector = ((JComboBox<String>) editor);
				selector.setSelectedItem(getValue());
				break;
			}
			case "align":
				editor = new JComboBox<String>(new String[] { "left", "center", "right" });
				((JComboBox<String>) editor).setSelectedItem(getValue());
				break;
			case "classname":
				editor = new JComboBox<String>(new String[] { "mini-outlooktree", "mini-tree" });
				((JComboBox<String>) editor).setSelectedItem(getValue());
				break;
			case "forceRowType":
			case "autoInitDataSource":
			case "fireFormDataSource":
			case "showTodayButton":
			case "showClearButton":
			case "showYearButtons":
			case "showMonthButtons":
			case "showDaysHeader":
			case "showWeekNumber":
			case "showFooter":
			case "validateForm":
			case "showTime":
			case "navEditMode":
			case "showEmptyText":
			case "allowCellWrap":
			case "allowHeaderWrap":
			case "allowRowSelect":
			case "onlyCheckSelection":
			case "editNextOnEnterKey":
			case "cellEditAction":
			case "allowCellValid":
			case "allowUnselect":
			case "allowAlternating":
			case "collapseGroupOnLoad":
			case "autoHideRowDetail":
			case "showModified":
			case "enableGroupOrder":
			case "skipReadOnlyCell":
			case "showVerticalScrollBar":
			case "showHorizontalScrollBar":
			case "showLinkLine":
			case "collapsing":
			case "keepAlignSpace":
			case "expand":
			case "multiSelect":
			case "autosize":
			case "autoColumnWidth":
			case "autoLoad":
			case "showGridLine":
			case "transparent":
			case "showPager":
			case "autoCenter":
			case "isHref":
			case "showTreeIcon":
			case "resultAsTree":
			case "allowSelect":
			case "showCheckBox":
			case "showTreeLines":
			case "expandOnLoad":
			case "showExpandButtons":
			case "enableHotTrack":
			case "expandOnDblClick":
			case "expandOnNodeClick":
			case "autoCheckParent":
			case "allowLeafDropIn":
			case "allowDrag":
			case "allowDrop":
			case "showFilterRow":
			case "checkRecursive":
			case "showNullItem":
			case "allowEdit":
			case "required":
			case "needFrame":
			case "showReloadButton":
			case "allowResize":
			case "allowcelledit":
			case "allowcellselect":
			case "multiselect":
			case "showSummaryRow":
			case "showButton":
			case "autoScroll":
			case "plain":
			case "border":
			case "showScrollbar":
			case "showVScrollbar":
			case "showHScrollbar":
			case "showLine":
			case "showHeader":
			case "inited":
			case "readonly":
			case "needhref":
			case "showToolbar":
			case "showFolderCheckBox":
			case "showRadioButton":
			case "allowInput":
			case "valueFromSelect":
			case "enabled":
			case "changeOnMousewheel":
			case "allowLimitValue":
			case "allowLoopValue":
			case "allowNull":
			case "autoLoginFire":
			case "autoRunFlowTasks":
			case "autoRunFlowHistoryTasks":
			case "useHrefColor":
			case "virtualScroll": {
				editor = new JComboBox<Boolean>(new Boolean[] { true, false });
				JComboBox<Boolean> cBox = (JComboBox<Boolean>) editor;
				Object object = getValue();
				boolean bb = false;
				if (object != null) {
					if (object instanceof Boolean)
						bb = (Boolean) object;
					else {
						try {
							bb = Boolean.parseBoolean((String) object);
						} catch (Exception e) {
						}
					}
				}
				cBox.setSelectedItem(bb);
				break;
			}
			case "fontVariant": {
				editor = new JComboBox<String>(new String[] { "normal", "small-caps" });
				JComboBox<Boolean> cBox = (JComboBox<Boolean>) editor;
				cBox.setSelectedItem(getValue());
				break;
			}
			case "closeDialogId":
			case "popup":
			case "grid":
			case "oper_uiid":
				editor = new ButtonLabel(PropertyInfo.this, value);
				break;
			case "placeInType": {
				JComboBox<PlaceInType> comboBox = new JComboBox<PlaceInType>(
						new PlaceInType[] { PlaceInType.ptNone, PlaceInType.ptAlign, PlaceInType.ptReplace });
				comboBox.setSelectedItem(getValue());
				editor = comboBox;
				break;
			}
			case "xAlign":
			case "yAlign": {
				JComboBox<Align> comboBox;
				switch (name) {
				case "xAlign":
					comboBox = new JComboBox<>(new Align[] { Align.alLeft, Align.alRight, Align.alCenter });
					break;
				default:
					comboBox = new JComboBox<>(new Align[] { Align.alTop, Align.alBottom, Align.alCenter });
					break;
				}
				String text = (String) getValue();
				if (text != null && !text.isEmpty()) {
					comboBox.setSelectedItem(Align.valueOf(text));
				}

				editor = comboBox;
				break;
			}
			case "placeAlign": {
				MulitCombobox<PlaceAlign> comboBox = new MulitCombobox<>(new PlaceAlign[] { PlaceAlign.alLeft,
						PlaceAlign.alTop, PlaceAlign.alRight, PlaceAlign.alBottom });
				String text = (String) getValue();
				if (text != null && !text.isEmpty()) {
					JSONArray jsonArray;
					try {
						jsonArray = new JSONArray(text);
						PlaceAlign[] vs = new PlaceAlign[jsonArray.length()];
						for (int i = 0; i < jsonArray.length(); i++) {
							vs[i] = PlaceAlign.valueOf(jsonArray.getString(i));
						}
						comboBox.setSelectedValues(vs);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				editor = comboBox;
				break;
			}
			case "target": {
				JComboBox<String> comboBox = new JComboBox<String>(
						new String[] { "_self", "_blank", "_parent", "_top" });
				comboBox.setEditable(true);
				comboBox.setSelectedItem(getValue());
				editor = comboBox;
				break;
			}
			case "sortMode": {
				editor = new JComboBox<String>(new String[] { "client", "server" });
				((JComboBox<String>) editor).setSelectedItem(getValue());
				break;
			}
			case "font": {
				editor = new ButtonLabel(PropertyInfo.this, value);
				break;
			}
			case "jumpID": {
				if (workflowNode == null || workflowNodes == null)
					return null;

				List<DrawNode> inputs = null;
				try {
					inputs = EditorEnvironment.getModelRelationInputs(workflowNode, workflowNodes,
							workflowRelationTitle);
				} catch (Exception e) {
					e.printStackTrace();
					inputs = null;
				}

				KeyValue<String, String>[] items = null;
				if (inputs != null && inputs.size() > 0) {
					items = new KeyValue[inputs.size() + 1];
					items[0] = new KeyValue<>();
					int index = 1;
					for (DrawNode node : inputs) {
						KeyValue<String, String> kv = new KeyValue<>();
						kv.key = node.title + "[" + node.name + "]";
						kv.value = node.id;
						items[index++] = kv;
					}

				} else {
					items = new KeyValue[] {};
				}

				editor = new JComboBox<KeyValue<String, String>>(items);

				JComboBox<KeyValue<String, String>> editorCombo = (JComboBox<KeyValue<String, String>>) editor;
				Object value = getValue();
				if (value != null) {
					for (KeyValue<String, String> keyValue : items) {
						if (keyValue.value == null)
							continue;
						if (value.toString().compareTo(keyValue.value) == 0) {
							editorCombo.setSelectedItem(keyValue);
							break;
						}
					}
				}
				break;
			}
			case "color":
			case "lineColor":
			case "backgroundColor":
			case "textColor": {
				if (value != null || !((String) value).isEmpty()) {
					try {
						Color bg = ColorConvert.toColorFromString((String) value);
						ButtonLabel bl = new ButtonLabel(PropertyInfo.this, bg);
						bl.textField.setBackground(bg);
						bl.textField.setText(ColorConvert.toHexFromColor(bg));
						editor = bl;
						break;
					} catch (Exception e) {
					}
				}

				editor = new ButtonLabel(PropertyInfo.this, null);
				break;
			}
			default:
				return null;
			}

			if (editor != null) {
				editor.putClientProperty("node", node);
				String typename = "";
				if (node instanceof UINode)
					typename = ((UINode) node).getDrawInfo().typeName();
				editor.putClientProperty("typename", typename);
				editor.putClientProperty("name", name);
			}
			return editor;
		}

		@Override
		public String getTitle() {
			if (title == null || title.isEmpty())
				return value.toString();
			else
				return title;
		}

		@Override
		public Object getSender() {
			return node;
		}

	}

	public static void propertyBuilder(PropertyPanel table, UINode node, DrawNode workflowNode,
			HashMap<String, DrawNode> workflowNodes, String workflowRelationTitle) throws Exception {
		propertyBuilder(table, node, workflowNode, workflowNodes, workflowRelationTitle, null);
	}

	public static void propertyBuilder(PropertyPanel table, UINode node, DrawNode workflowNode,
			HashMap<String, DrawNode> workflowNodes, String workflowRelationTitle, List<DataSource> dataSources)
			throws Exception {
		table.clearPropertyTable();

		if (node == null || node.getDrawInfo() == null)
			return;

		DrawInfo info = node.getDrawInfo();
		JSONObject rows = info.toJson(true);
		rows.put("zOrder", node.zOrder);
		JSONArray keys = rows.names();

		TreeMap<String, IProperty> properties = new TreeMap<>();

		for (int i = 0; i < keys.length(); i++) {
			String key = keys.getString(i);
			if (key.compareTo("fontName") == 0 || key.compareTo("fontSize") == 0 || key.compareTo("fontStyle") == 0)
				continue;
			Object value = rows.get(key);

			String title = null;
			properties.put(key, new PropertyInfo(node, key, title, value, workflowNode, workflowNodes,
					workflowRelationTitle, dataSources));
		}

		if (rows.has("fontName") && rows.has("fontSize") && rows.has("fontStyle"))
			properties.put("font",
					new PropertyInfo(node, "font", null,
							rows.getString("fontName") + "," + rows.get("fontSize") + "," + rows.get("fontStyle"),
							workflowNode, workflowNodes, workflowRelationTitle, dataSources));

		List<IProperty> infos = new ArrayList<>(properties.values());
		infos.add(0, new PropertyInfo(node, "uitype", "控件类型", "【" + node.getDrawInfo().getClass().getSimpleName() + "】",
				workflowNode, workflowNodes, workflowRelationTitle, dataSources));

		table.showPropertyTable(infos);
	}

	public static void propertyBuilder(PropertyPanel table, JSONObject rows, Object obj) throws Exception {
		propertyBuilder(table, rows, obj, null);
	}

	public static void propertyBuilder(PropertyPanel table, JSONObject rows, Object obj, List<DataSource> dataSources)
			throws Exception {
		table.clearPropertyTable();

		if (obj == null || rows == null || rows.length() == 0)
			return;

		JSONArray keys = rows.names();

		TreeMap<String, IProperty> properties = new TreeMap<>();

		for (int i = 0; i < keys.length(); i++) {
			String key = keys.getString(i);
			Object value = "";
			if (rows.has(key))
				value = rows.get(key);

			String title = null;
			properties.put(key, new PropertyInfo(obj, key, title, value, null, null, "", dataSources));
		}

		List<IProperty> infos = new ArrayList<>(properties.values());
		table.showPropertyTable(infos);
	}

	public boolean checkNameFormat(String name) {
		// 邮箱验证规则
		String regEx = "[\\w-]+";
		// 编译正则表达式
		Pattern pattern = Pattern.compile(regEx);
		// 忽略大小写的写法
		// Pattern pat = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(name);
		// 字符串是否与正则表达式相匹配
		boolean rs = matcher.matches();
		return rs;
	}

	public boolean checkControlIDRepeat(String id, UINode idnode) {
		for (DrawNode tmp : canvas.nodes.values()) {
			UINode node = (UINode) tmp;
			if (node.getDrawInfo().id.compareToIgnoreCase(id) == 0 && node != idnode)
				return true;
		}
		return false;
	}

	public boolean checkControlNameRepeat(String name, UINode idnode) {
		for (DrawNode tmp : canvas.nodes.values()) {
			UINode node = (UINode) tmp;
			if (node.getDrawInfo().name.compareToIgnoreCase(name) == 0 && node != idnode)
				return true;
		}
		return false;
	}

	public DefaultPropertyClient(IUpdate iUpdate, DrawCanvas canvas, IMainControl mainControl, PropertyPanel table) {
		this.iUpdate = iUpdate;
		this.mainControl = mainControl;
		this.canvas = canvas;
		this.table = table;

	}

	public String selectImage(Object sender) {
		File imageFile = Tools.selectOpenImageFile(iUpdate.getParent(), null, null);
		if (imageFile == null)
			return null;

		ButtonLabel ob = (ButtonLabel) sender;
		String value = imageFile.getName();
		File dest = EditorEnvironment.getProjectFile(EditorEnvironment.Image_Resource_Path, value.toString());
		try {
			if (!dest.equals(imageFile))
				FileHelp.copyFileTo(imageFile, dest);
			ob.textField.setText(value.toString());
			ob.setValue(value);
			return value;
		} catch (IOException e) {
			e.printStackTrace();
			EditorEnvironment.showMessage(iUpdate.getParent(), "拷贝文件失败！", "设置失败", JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}

	protected boolean updateValue(JComponent sender, String name, Object value) {
		Object object = sender.getClientProperty("node");

		try {
			if (object == null) {
				return false;
			}

			DrawNode node = null;
			if (object instanceof DrawNode)
				node = (DrawNode) object;

			Class<?> c = null;
			if (node instanceof UINode)
				object = ((UINode) node).getDrawInfo();

			c = object.getClass();
			Field field = c.getField(name);
			field.setAccessible(true);

			if (name.compareTo("font") == 0) {
				String[] datas = ((String) value).split(",");
				value = new Font(datas[0], Integer.parseInt(datas[2]), Integer.parseInt(datas[1]));
			} else if (field.getType().isAssignableFrom(Color.class)
					&& (name.compareTo("color") == 0 || name.compareTo("textColor") == 0
							|| name.compareTo("lineColor") == 0 || name.compareTo("backgroundColor") == 0)
					&& (value != null && !value.toString().isEmpty()))
				value = ColorConvert.toColorFromString((String) value);

			Object oldObject = field.get(object);
			if ((oldObject == null && value == null) || (oldObject != null && oldObject.equals(value)))
				return true;

			Class<?> c1 = field.getType();

			if (c1.getName().compareTo(String.class.getName()) == 0) {
				if (value == null)
					value = "";
				else
					value = String.valueOf(value);
			} else if (c1.getSimpleName().compareToIgnoreCase(Integer.class.getSimpleName()) == 0
					&& value instanceof String) {
				value = Integer.parseInt(value.toString());
			} else if (c1.getSimpleName().compareToIgnoreCase(Double.class.getSimpleName()) == 0
					&& value instanceof String) {
				value = Double.parseDouble(value.toString());
			} else if (c1.getSimpleName().compareToIgnoreCase(Float.class.getSimpleName()) == 0
					&& value instanceof String) {
				value = Float.parseFloat(value.toString());
			} else if (c1.getSimpleName().compareToIgnoreCase(Boolean.class.getSimpleName()) == 0
					&& value instanceof String) {
				value = Boolean.parseBoolean(value.toString());
			} else if (c1.getName().compareToIgnoreCase(Date.class.getName()) == 0) {
				DateFormat dFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				value = dFormat.parse(value.toString());
			}

			Object old = field.get(object);

			boolean isName = name.compareTo("name") == 0;
			boolean isID = name.compareTo("id") == 0;
			if ((isID || isName) && node instanceof UINode) {
				String v = (String) value;
				if (value == null || v.isEmpty()) {
					EditorEnvironment.showMessage(iUpdate.getParent(), (isName ? "name" : "id") + "不能为空，请重新输入！", "提示");
					return false;
				}

				if (!checkNameFormat(v)) {
					EditorEnvironment.showMessage(iUpdate.getParent(), "输入的字符格式不符合要求，请重新输入！", "提示");
					return false;
				}

				if (isID) {
					if (checkControlIDRepeat(v, (UINode) node)) {
						EditorEnvironment.showMessage(iUpdate.getParent(), "id已经存在，请重新输入！", "提示");
						return false;
					}
				}

				if (isName) {
					if (checkControlNameRepeat(v, (UINode) node)) {
						EditorEnvironment.showMessage(iUpdate.getParent(), "name已经存在，请重新输入！", "提示");
						return false;
					}
				}
			}

			JSONObject oldValue = null;
			if (node instanceof DrawNode) {
				oldValue = node.toJson();
			}

			if (value != null && !field.getType().isAssignableFrom(value.getClass())
					&& (value instanceof String && ((String) value).isEmpty())) {
				value = null;
			}

			if (value instanceof String && value != null && !((String) value).isEmpty()) {
				if (JSONObject.class.isAssignableFrom(field.getType())) {
					value = new JSONObject((String) value);
				} else if (JSONArray.class.isAssignableFrom(field.getType())) {
					value = new JSONArray((String) value);
				}
			}
			field.set(object, value);
			if (node instanceof UINode) {
				if (object instanceof ImageInfo && name.compareTo("value") == 0) {
					ImageInfo imageInfo = (ImageInfo) object;
					imageInfo.setValue(value);
				} else if (name.compareTo("left") == 0 || name.compareTo("top") == 0 || name.compareTo("right") == 0
						|| name.compareTo("bottom") == 0 || name.compareTo("width") == 0
						|| name.compareTo("height") == 0) {
					node.invalidRect();
					((UINode) node).getRect();
				}
			} else if (isID) {
				if (node instanceof DrawNode) {
					canvas.nodes.remove(old);
					canvas.nodes.put((String) value, node);
				}
			}

			iUpdate.setEditState(true);

			if (oldValue != null) {
				String newid = null;
				CommandInfoType cit = CommandInfoType.ctUpdateAttr;

				if (isID) {
					newid = (String) value;
					cit = CommandInfoType.ctUpdateID;
				}
				canvas.getACM().pushCommand(oldValue, newid, cit);
			}

			if (name.compareToIgnoreCase("data") == 0 && node instanceof UINode) {
				if (object instanceof ToolbarInfo) {
					((ToolbarInfo) object).reset();
				}
			}

			iUpdate.onUpdateEnd(object, name, oldObject, value);

			canvas.repaint();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			EditorEnvironment.showMessage(iUpdate.getParent(), "不能设置属性值：" + Tools.getExceptionMsg(e) + "！", "设置失败",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onClick(JComponent sender, int row, int col, ISetValue onSetValue) {
		String typename = (String) sender.getClientProperty("typename");
		String name = (String) sender.getClientProperty("name");
		Object value = null;
		switch (name) {
		case "sharedata": {
			switch (typename) {
			case DrawInfoDefines.MainMenu_Name: {
				MenuEditorDialog.showDialog(mainControl,
						EditorEnvironment.getProjectFile(EditorEnvironment.Menu_Dir_Path,
								EditorEnvironment.getMenu_FileName(EditorEnvironment.Main_Menu_FileName)));
				break;
			}
			case DrawInfoDefines.MainTree_Name: {
				NavigationEditorDialog.showDialog(mainControl,
						EditorEnvironment.getProjectFile(EditorEnvironment.Tree_Dir_Path,
								EditorEnvironment.getTree_FileName(EditorEnvironment.Main_Tree_FileName)));
				break;
			}
			}
			break;
		}
		case "control":
		case "attachControlName": {
			ButtonLabel ob = (ButtonLabel) sender;
			com.wh.dialog.editor.ControlSelectDialog.Result result = ControlSelectDialog.showDialog(iUpdate.getUIID(),
					null, true);
			if (result.isok) {
				if (result.data == null)
					value = null;
				else
					value = ((UINode) result.data[0]).getDrawInfo().name;
				ob.setValue(value);
				break;
			} else
				return;
		}
		case "attachID": {
			ButtonLabel ob = (ButtonLabel) sender;
			Result result = ModelflowSelectDialog.showDialog(mainControl, null, (String) ob.getValue(), true,
					WorkflowNode.class);
			if (result != null) {
				value = result.name;
				ob.setValue(value);
			} else
				return;
		}
			break;
		case "buttonImage":
		case "img": {
			value = selectImage(sender);
			if (value == null)
				return;

			break;
		}

		case "saveDataSource": {
			ButtonLabel ob = (ButtonLabel) sender;
			JSONArray sets;
			Object v = ob.getValue();
			if (v instanceof JSONArray)
				sets = (JSONArray) v;
			else
				sets = v == null ? new JSONArray() : new JSONArray((String) v);
			String[] ids = null;
			if (sets.length() > 0) {
				ids = new String[sets.length()];
				for (int i = 0; i < sets.length(); i++) {
					ids[i] = sets.getString(i);
				}
			}

			HashMap<String, Boolean> idMap = new HashMap<>();
			if (ids != null) {
				for (String id : ids) {
					idMap.put(id, true);
				}
			}

			List<DataSource> dataSources = new ArrayList<>();
			List<Integer> checks = new ArrayList<>();
			for (DataSource source : DataSourceManager.getDSM().getDataSources()) {
				switch (source.getType()) {
				case FileDataSource.FILE_KEY:
				case SQLDataSource.SQL_KEY:
					dataSources.add(source);
					if (idMap.containsKey(source.id)) {
						checks.add(dataSources.size() - 1);
					}
					break;
				default:
					break;
				}
			}

			dataSources = CheckSelector.show("选择要保存的数据源", dataSources, checks);
			if (dataSources != null && dataSources.size() > 0) {
				sets = new JSONArray();
				for (DataSource ds : dataSources) {
					sets.put(ds.id);
				}
			}

			value = sets;
			ob.setValue(value);
			break;

		}
		case "fireDataSource": {
			ButtonLabel ob = (ButtonLabel) sender;
			PropertyInfo propertyInfo = (PropertyInfo) ob.sender;
			JSONArray sets;
			Object v = ob.getValue();
			if (v instanceof JSONArray)
				sets = (JSONArray) v;
			else
				sets = v == null ? new JSONArray() : new JSONArray((String) v);
			String[] ids = null;
			if (sets.length() > 0) {
				ids = new String[sets.length()];
				for (int i = 0; i < sets.length(); i++) {
					ids[i] = sets.getJSONObject(i).getString("id");
				}
			}
			String uiid = EditorEnvironment.getUIID(propertyInfo.workflowNode.id);
			com.wh.dialog.editor.ControlSelectDialog.Result result = ControlSelectDialog.showDialog(uiid, ids, null,
					true, true);
			if (result.isok) {
				sets = new JSONArray();
				if (result.data != null)
					for (DrawNode drawNode : result.data) {
						UINode node = (UINode) drawNode;
						JSONObject values = new JSONObject();
						values.put("name", node.getDrawInfo().name);
						values.put("id", node.id);
						sets.put(values);
					}
			}
			value = sets;
			ob.setValue(value);
			break;
		}
		case "dataSourceParams": {
			ButtonLabel ob = (ButtonLabel) sender;

			JSONArray sets;
			Object v = ob.getValue();
			if (v instanceof JSONArray)
				sets = (JSONArray) v;
			else
				sets = v == null ? new JSONArray() : new JSONArray((String) v);
			Object[][] rows = null;

			if (sets != null && sets.length() > 0) {
				rows = new Object[sets.length()][5];
				int index = 0;
				for (Object object : sets) {
					JSONObject rowdata = (JSONObject) object;
					String dsId = JsonHelp.getString(rowdata, "dsId");
					String paramName = rowdata.getString("param");
					rows[index][0] = dsId;
					rows[index][1] = paramName;
					rows[index][2] = rowdata.has("default") ? rowdata.getString("default") : null;
					rows[index][3] = null;
					rows[index][4] = new ArrayList<>();
					rows[index][4] = new ArrayList<>();
					index++;
				}

				if (index != rows.length) {
					rows = Arrays.copyOf(rows, index);
				}
			}

			final HashMap<String, DataSource> dss = new HashMap<>();
			for (DataSource ds : DataSourceManager.getDSM().getDataSources()) {
				dss.put(ds.id, ds);
			}
			ModelResult result = KeyValueSelector.show(null, mainControl, new ICheckValue() {

				@Override
				public boolean onCheck(Object[][] originalData, JTable table) {
					HashMap<String, Integer> map = new HashMap<>();
					for (int i = 0; i < table.getRowCount(); i++) {
						Object obj = table.getValueAt(i, 0);
						if (obj instanceof String) {
							obj = DataSourceManager.getDSM().get(obj.toString());
						}
						DataSource ds = (DataSource) obj;
						String dsid = ds == null ? null : ds.id;
						String param = (String) table.getValueAt(i, 1);
						if (dsid == null || dsid.isEmpty()) {
							EditorEnvironment.showMessage("第【" + ++i + "】行的数据源不能为null！");
							return false;
						}

						if (param == null || param.isEmpty()) {
							EditorEnvironment.showMessage("第【" + ++i + "】行的参数不能为null！");
							return false;
						}

						String key = dsid + param;
						if (map.containsKey(key)) {
							EditorEnvironment.showMessage("第【" + ++i + "】行的参数已经在第【" + map.get(key) + "】行定义！");
							return false;
						}

						map.put(key, i);
					}
					return true;
				}
			}, new IEditRow() {

				@Override
				public void updateRow(JTable table, Vector<?> row) {
				}

				@Override
				public boolean deleteRow(JTable table, Vector<?> row) {
					return true;
				}

				@Override
				public Object[] addRow(JTable table) {
					return new Object[] { null, null, null, null, new ArrayList<>(), new ArrayList<>() };
				}
			}, rows, new Object[] { "数据源", "参数", "缺省值" }, null, null, new KeyValueSelector.IActionListener() {

				@Override
				public ActionResult onAction(TableModel model, String key, Object value, int row, int col,
						List<Object> selects) {
					if (selects != null) {
						selects.clear();
						switch (col) {
						case 0:
							selects.addAll(DataSourceManager.getDSM().getDataSources());
							break;
						case 1:
							DataSource ds = dss.get(key);
							if (key == null || key.isEmpty() || !dss.containsKey(key) || dss.get(key).params == null
									|| dss.get(key).params.length() == 0)
								return null;
							switch (ds.getType()) {
							case UrlDataSource.URL_KEY:
								selects.addAll(dss.get(key).params.names().toList());
								break;
							case SQLDataSource.SQL_KEY:
								break;
							}
							break;
						}
					}
					return null;
				}

			}, false);

			if (result.isok) {
				JSONArray data = new JSONArray();
				if (result.model != null)
					for (int i = 0; i < result.model.getRowCount(); i++) {
						JSONObject rowdata = new JSONObject();
						rowdata.put("dsId", result.model.getValueAt(i, 0));
						rowdata.put("param", result.model.getValueAt(i, 1));
						rowdata.put("default", result.model.getValueAt(i, 2));
						data.put(rowdata);
					}
				value = data;
			} else
				value = sets;
			ob.setValue(value);
			break;
		}
		case "params": {
			ButtonLabel ob = (ButtonLabel) sender;
			String v = (String) ob.getValue();
			JSONArray datas = new JSONArray();
			if (v != null && !v.isEmpty()) {
				try {
					datas = (JSONArray) JsonHelp.parseJson(v);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			try {
				value = ListInput.show(mainControl, "设置参数", datas, true);
				ob.setValue(value);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			break;
		}
		case "template":
		case "dataSource":
		case "gradient":
		case "allows":
		case "decision":
		case "initData":
		case "data":
			boolean needBreak = false;
			switch (typename) {
			case DrawInfoDefines.ComboTreeBox_Name: {
				ButtonLabel ob = (ButtonLabel) sender;
				try {
					JSONArray data = null;
					Object objValue = ob.getValue();
					if (objValue != null) {
						if (objValue instanceof JSONArray)
							data = (JSONArray) objValue;
						else
							data = (JSONArray) JsonHelp.parseJson((String) objValue);
					}
					value = JsonTreeDataConfigDialog.show(data);
				} catch (JSONException e) {
					e.printStackTrace();
					EditorEnvironment.showMessage(iUpdate.getParent(), "json数据格式错误！", "设置失败",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				ob.setValue(value);
				UINode node = (UINode) sender.getClientProperty("node");
				if (name == "data" && node.getDrawInfo() instanceof ListViewInfo) {
					((ListViewInfo) node.getDrawInfo()).clearCacheImages();
				}

				needBreak = true;
				break;
			}
			case DrawInfoDefines.ComboBox_Name: {
				UINode node = (UINode) sender.getClientProperty("node");
				ComboInfo info = (ComboInfo) node.getDrawInfo();

				Object[][] Data = null;

				ButtonLabel ob = (ButtonLabel) sender;
				Object data = ob.getValue();
				try {
					JSONArray datas = null;
					if (data != null) {
						datas = (JSONArray) JsonHelp.parseJson(data.toString());
					}
					Data = new Object[datas.length()][2];
					for (int i = 0; i < datas.length(); i++) {
						JSONObject rowData = datas.getJSONObject(i);
						Data[i][0] = rowData.getString(info.valueField);
						if (rowData.has(info.textField))
							Data[i][1] = rowData.getString(info.textField);
						else
							Data[i][1] = rowData.getString("name");

					}
				} catch (JSONException e) {
					Data = null;
					e.printStackTrace();
					EditorEnvironment.showMessage(iUpdate.getParent(), "json数据格式错误！", "设置失败",
							JOptionPane.ERROR_MESSAGE);
				}

				ModelResult result = KeyValueSelector.show(null, mainControl, null, null, Data,
						new Object[] { info.valueField, info.textField }, null, null, false);

				DefaultTableModel model = null;
				if (result.isok)
					model = result.model;

				if (model != null) {
					JSONArray datas = new JSONArray();
					for (int i = 0; i < model.getRowCount(); i++) {
						Object v = model.getValueAt(i, 0);
						Object text = model.getValueAt(i, 1);
						if (v == null || text == null)
							continue;

						JSONObject rowData = new JSONObject();
						try {
							rowData.put(info.valueField, v.toString());
							rowData.put(info.textField, text.toString());
						} catch (JSONException e) {
							e.printStackTrace();
							EditorEnvironment.showMessage(iUpdate.getParent(), "设置value属性失败，请检查您的输入 ！", "设置失败",
									JOptionPane.ERROR_MESSAGE);
							return;
						}
						datas.put(rowData);
					}
					value = datas.toString();
					ob.setValue(value);
				} else {
					return;
				}
				needBreak = true;
				break;

			}
			default: {
				ButtonLabel ob = (ButtonLabel) sender;
				try {
					value = iUpdate.showButtonEditJsonEditor(name, ob);
				} catch (JSONException e) {
					e.printStackTrace();
					EditorEnvironment.showMessage(iUpdate.getParent(), "json数据格式错误！", "设置失败",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				needBreak = true;
				break;
			}
			}

			if (needBreak)
				break;
		case "header":
		case "extendData": {
			ButtonLabel ob = (ButtonLabel) sender;
			try {
				value = iUpdate.showButtonEditJsonEditor(name, ob);
			} catch (JSONException e) {
				e.printStackTrace();
				EditorEnvironment.showMessage(iUpdate.getParent(), "json数据格式错误！", "设置失败", JOptionPane.ERROR_MESSAGE);
				return;
			}
			break;
		}
		case "closeDialogId": {
			ButtonLabel ob = (ButtonLabel) sender;
			com.wh.dialog.editor.ControlSelectDialog.Result result = ControlSelectDialog.showDialog(iUpdate.getUIID(),
					(String) ob.getValue(), ComboInfo.class, true);

			if (result.isok) {
				if (result.data == null)
					value = null;
				else{

				UINode node = (UINode) result.data[0];
				if (node.getDrawInfo() == null)
					value = null;
				else
					value = node.getDrawInfo().name;
				}
				ob.setValue(value);
			} else
				return;
			break;
		}
		case "popup": {
			ButtonLabel ob = (ButtonLabel) sender;
			com.wh.dialog.editor.ControlSelectDialog.Result result = ControlSelectDialog.showDialog(iUpdate.getUIID(),
					(String) ob.getValue(), DivInfo.class, true);
			if (result.isok) {
				if (result.data == null)
					value = null;
				else{
				UINode node = (UINode) result.data[0];
				if (node.getDrawInfo() == null)
					value = null;
				else
					value = node.getDrawInfo().id;
				}
				ob.setValue(value);
			} else
				return;
			break;
		}
		case "grid": {
			ButtonLabel ob = (ButtonLabel) sender;
			com.wh.dialog.editor.ControlSelectDialog.Result result = ControlSelectDialog.showDialog(iUpdate.getUIID(),
					(String) ob.getValue(), GridInfo.class, true);
			if (result.isok) {
				if (result.data == null)
					value = null;
				else {
					UINode node = (UINode) result.data[0];
					if (node.getDrawInfo() == null)
						value = null;
					else
						value = node.getDrawInfo().id;
				}
				ob.setValue(value);
			} else
				return;
			break;
		}
		case "model_id": {
			String id = null;
			ButtonLabel ob = (ButtonLabel) sender;
			try {
				id = ((KeyValue<String, String>) ob.getValue()).value;
			} catch (Exception e) {
				id = null;
			}

			Result result = ModelflowSelectDialog.showDialog(mainControl, id, id, true, WorkflowNode.class);
			if (result != null) {
				KeyValue<String, String> v = new KeyValue<>();
				v.key = result.title;
				v.value = result.name;
				value = v;
				ob.setValue(value);
			} else
				return;
			break;
		}
		case "oper_uiid":
		case "uiInfo": {
			ButtonLabel ob = (ButtonLabel) sender;
			UISelectDialog.Result result = UISelectDialog.showDialog(mainControl, (String) ob.getValue(), new IAdd() {

				@Override
				public String onAdd() {
					String uiid = UUID.randomUUID().toString();
					return uiid;
				}
			});
			if (result == null)
				return;

			value = result.id;
			ob.setValue(value);
			break;
		}
		case "scrollDiv": {
			ButtonLabel ob = (ButtonLabel) sender;
			com.wh.dialog.editor.ControlSelectDialog.Result result = ControlSelectDialog.showDialog(iUpdate.getUIID(),
					(String) ob.getValue(), DivInfo.class, false);

			if (result.isok) {
				if (result.data == null)
					value = null;
				else {
					UINode node = (UINode) result.data[0];
					if (node.getDrawInfo() == null)
						value = null;
					else
						value = node.getDrawInfo().name;
				}
				ob.setValue(value);
			} else
				return;

			break;
		}
		case "scrollControl": {
			ButtonLabel ob = (ButtonLabel) sender;
			com.wh.dialog.editor.ControlSelectDialog.Result result = ControlSelectDialog.showDialog(iUpdate.getUIID(),
					(String) ob.getValue(), false);

			if (result.isok) {
				if (result.data == null)
					value = null;
				else{

				UINode node = (UINode) result.data[0];
				if (node.getDrawInfo() == null)
					value = null;
				else
					value = node.getDrawInfo().name;
				}
				ob.setValue(value);
			} else
				return;
			break;
		}
		case "controlData": {
			ButtonLabel ob = (ButtonLabel) sender;
			try {
				Object objValue = ob.getValue();
				if (objValue != null) {
					objValue = JsonHelp.parseJson(objValue.toString());
				}
				value = JsonEditorDialog.show(mainControl, objValue);
			} catch (JSONException e) {
				e.printStackTrace();
				EditorEnvironment.showMessage(iUpdate.getParent(), "json数据格式错误！", "设置失败", JOptionPane.ERROR_MESSAGE);
				return;
			}
			ob.setValue(value);
			break;
		}
		case "memo": {
			ButtonLabel ob = (ButtonLabel) sender;
			String defaultValue = (String) ob.getValue();
			value = MulitTextInput.showDialog(null, defaultValue);
			ob.setValue(value);
			break;
		}
		case "links": {
			ButtonLabel ob = (ButtonLabel) sender;
			try {
				value = iUpdate.showButtonEditJsonEditor(name, ob);
				ob.setValue(value);
			} catch (JSONException e) {
				e.printStackTrace();
				EditorEnvironment.showMessage(iUpdate.getParent(), "json数据格式错误！", "设置失败", JOptionPane.ERROR_MESSAGE);
				return;
			}
			break;
		}
		case "value":
			switch (typename) {
			case DrawInfoDefines.Tree_Name: {
				ButtonLabel ob = (ButtonLabel) sender;
				try {
					JSONArray data = null;
					Object objValue = ob.getValue();
					if (objValue != null) {
						if (objValue instanceof JSONArray)
							data = (JSONArray) objValue;
						else
							data = (JSONArray) JsonHelp.parseJson((String) objValue);
					}
					value = JsonTreeDataConfigDialog.show(data);
				} catch (JSONException e) {
					e.printStackTrace();
					EditorEnvironment.showMessage(iUpdate.getParent(), "json数据格式错误！", "设置失败",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				ob.setValue(value);
				break;
			}
			case DrawInfoDefines.TextArea_Name: {
				ButtonLabel ob = (ButtonLabel) sender;
				String defaultValue = (String) ob.getValue();
				value = MulitTextInput.showDialog(null, defaultValue);
				ob.setValue(value);
				break;
			}
			case DrawInfoDefines.Grid_Name: {
				ButtonLabel ob = (ButtonLabel) sender;
				try {
					value = iUpdate.showButtonEditJsonEditor(name, ob);
					ob.setValue(value);
				} catch (JSONException e) {
					e.printStackTrace();
					EditorEnvironment.showMessage(iUpdate.getParent(), "json数据格式错误！", "设置失败",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				break;
			}
			case DrawInfoDefines.Image_Name: {
				ButtonLabel ob = (ButtonLabel) sender;
				value = selectImage(sender);
				if (value == null)
					return;

				ob.setValue(value);
				break;
			}
			}
			break;
		case "chartData": {
			ButtonLabel ob = (ButtonLabel) sender;
			String defaultValue = (String) ob.getValue();
			value = MulitTextInput.showDialog(null, defaultValue);
			ob.setValue(value);
			break;
		}
		case "font": {
			ButtonLabel ob = (ButtonLabel) sender;
			String data = (String) ob.getValue();
			String[] datas = data.split(",");
			Font font = new Font(datas[0], Integer.parseInt(datas[2]), Integer.parseInt(datas[1]));

			Font result = JFontDialog.showDialog(iUpdate.getParent(), "字体选择", true, font);
			if (result != null) {
				value = result.getName() + "," + String.valueOf(canvas.getMTFontSize(result.getSize())) + ","
						+ String.valueOf(result.getStyle());
				ob.setValue(value);
				ob.textField.setText(value.toString());
			} else
				return;
			break;
		}
		case "color":
		case "lineColor":
		case "backgroundColor":
		case "textColor": {
			ButtonLabel bl = (ButtonLabel) (sender);
			Object v = bl.getValue();
			Color bg = null;
			if (v instanceof Color)
				bg = (Color) v;
			else
				bg = ColorConvert.toColorFromString((String) v);
			Color rgb = JColorChooser.showDialog(iUpdate.getParent(), "颜色选择", bg);
			if (rgb != null) {
				bl.textField.setBackground(rgb);
				value = ColorConvert.toHexFromColor(rgb);
				bl.setValue((String) value);
			} else
				return;
			break;
		}
		default:
			return;
		}

		if ((value instanceof JSONArray || value instanceof JSONObject))
			value = value.toString();

		if (updateValue(sender, name, value))
			onSetValue.onSetValue(row, col, value);
	}

	@Override
	public boolean onUpateValue(JComponent sender, int row, int col, Object value) {
		return updateValue(sender, table.getName(row), value);
	}

	@Override
	public void onEdit(int row, int col) {
		iUpdate.onEdit(row, col);
	}
};
