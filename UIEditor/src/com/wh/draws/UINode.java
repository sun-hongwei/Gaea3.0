package com.wh.draws;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import com.wh.draws.DrawCanvas.ICreateNodeSerializable;
import com.wh.draws.control.ActionCommandManager.CommandInfoType;
import com.wh.draws.control.StatckTreeElement;
import com.wh.draws.drawinfo.ButtonInfo;
import com.wh.draws.drawinfo.ChartInfo;
import com.wh.draws.drawinfo.CheckInfo;
import com.wh.draws.drawinfo.ComboInfo;
import com.wh.draws.drawinfo.ComboTreeInfo;
import com.wh.draws.drawinfo.DateInfo;
import com.wh.draws.drawinfo.DivInfo;
import com.wh.draws.drawinfo.DrawInfo;
import com.wh.draws.drawinfo.DrawInfoDefines;
import com.wh.draws.drawinfo.GridInfo;
import com.wh.draws.drawinfo.ImageInfo;
import com.wh.draws.drawinfo.LabelInfo;
import com.wh.draws.drawinfo.ListBoxInfo;
import com.wh.draws.drawinfo.ListViewInfo;
import com.wh.draws.drawinfo.MainMenuInfo;
import com.wh.draws.drawinfo.MainTreeInfo;
import com.wh.draws.drawinfo.PasswordInfo;
import com.wh.draws.drawinfo.ProgressBarInfo;
import com.wh.draws.drawinfo.RadioInfo;
import com.wh.draws.drawinfo.ReportInfo;
import com.wh.draws.drawinfo.ScrollBarInfo;
import com.wh.draws.drawinfo.SpinnerInfo;
import com.wh.draws.drawinfo.SubUIInfo;
import com.wh.draws.drawinfo.TextAreaInfo;
import com.wh.draws.drawinfo.TextInfo;
import com.wh.draws.drawinfo.TimeInfo;
import com.wh.draws.drawinfo.TimerInfo;
import com.wh.draws.drawinfo.ToolbarInfo;
import com.wh.draws.drawinfo.TreeInfo;
import com.wh.draws.drawinfo.UpLoadInfo;
import com.wh.form.Defines;
import com.wh.system.tools.ImageUtils;
import com.wh.system.tools.Tools;

public class UINode extends DrawNode{

	
	@Override
	protected void removed() {
		if (getDrawInfo() instanceof ReportInfo){
			ReportInfo info = (ReportInfo)getDrawInfo();
			info.removeListener();
		}
	}

	@Override
	public void pasted() {
		if (getDrawInfo() instanceof ReportInfo){
			((ReportInfo)getDrawInfo()).autoRect(false);
		}
	}
	
	@Override
	public void popCommanded(DrawNode oldNode, CommandInfoType cit) {
		if (getDrawInfo() instanceof ReportInfo){
			ReportInfo info = (ReportInfo) getDrawInfo(); 
			((ReportInfo)getDrawInfo()).autoRect(false);
			if (oldNode instanceof UINode){
				UINode uiNode = (UINode)oldNode;
				if (uiNode.getDrawInfo() instanceof ReportInfo){
					info.onClick = ((ReportInfo)uiNode.getDrawInfo()).onClick;
				}
			}
		}
	}

	public boolean isParent(DrawNode node) {
		if (node == this)
			return false;
		
		UINode uiNode = (UINode)this;
		if (!(uiNode.getDrawInfo() instanceof DivInfo))
			return false;
		Rectangle sR = node.getRect();
		Rectangle dR = getRect();
		if (dR == null)
			return false;

		if (canvas.stackTreeManager.elements.containsKey(node.id)){
			StatckTreeElement element = canvas.stackTreeManager.elements.get(node.id);
			if (element.parentid != null && !element.parentid.isEmpty() && element.parentid.compareTo(id) == 0){
				boolean b = (dR.contains(sR.getLocation())
						|| dR.contains(new Point(sR.x + sR.width, sR.y))
						|| dR.contains(new Point(sR.x, sR.y + sR.height))
						|| dR.contains(new Point(sR.x + sR.width, sR.y + sR.height))
						);
				if (!b){
					canvas.stackTreeManager.remove(node.id);
				}
				return b;
			}
		}
		
		boolean b = dR.contains(sR.getLocation());
		if (b){
			b = Math.abs(dR.x - sR.x) > 10 && Math.abs(dR.y - sR.y) > 10;
		}
		return b && zOrder <= node.zOrder;
		
	}
	
	public boolean isDrawTreeRoot() {
		return info != null && info instanceof DivInfo;
	}
	
	protected void onMoved() {
		fixRect(FixType.ftXY);
	}
	
	protected void onResized() {
		fixRect(FixType.ftSize);
		if (info != null)
			info.onResize();
	}
	
	protected void onRectChanged() {
		fixRect(FixType.ftRect);
		if (info != null)
			info.onResize();
	}
	
	public String toString(){
		if (info != null && info.title != null)
			return info.title + "    [" + info.name + "]";
		else
			return id;
	}
	
	public JSONObject toJson() throws JSONException{
		JSONObject json = super.toJson();
		if (info != null){
			json.put(DrawInfoDefines.TypeName_Key, info.typeName());
			json.put("data", info.toJson());
		}
		return json;
	}
	
	protected void changeid() {
		if (info != null){
			info.id = UUID.randomUUID().toString();
			info.name = info.id;
		}
	}

	public void fromJson(JSONObject data, ICreateNodeSerializable createUserDataSerializable) throws JSONException{
		super.fromJson(data, createUserDataSerializable);
		String typename = data.getString(DrawInfoDefines.TypeName_Key);
		info = newInstance(typename, this);
		info.fromJson(data.getJSONObject("data"));
	}

	protected String fixBound(String value, float newValue, int max) {
		
		if (value.indexOf('%') != -1){
			return Tools.roundTo(((newValue / max)) * 100, 2) + "%";
		}else
			return String.valueOf(newValue);
	}
	
	protected enum FixType{
		ftSize, ftXY, ftRect
	}
	
	public void fixSize(boolean reset) {
		Rectangle rect = super.getRect();
		if (rect == null)
			return;
		
		String width = fixBound(info.width, rect.width, canvas.canvasPageSize.width);
		String height = fixBound(info.height, rect.height, canvas.canvasPageSize.height);
		if ((width.indexOf("%") != -1 || height.indexOf("%") != -1) && (width.compareTo(info.width) == 0 && height.compareTo(info.height) == 0))
			return;
		info.width = width;
		info.height = height;
		
		if (reset){
			super.setRect(null);
			info.getRect();
		}
	}

	protected void fixLocation(boolean reset) {
		Rectangle rect = super.getRect();
		if (rect == null)
			return;
		
		Rectangle realRect = canvas.getLocalRect(rect);
		info.left = fixBound(info.left, realRect.x, canvas.canvasPageSize.width);
		info.top = fixBound(info.top, realRect.y, canvas.canvasPageSize.height);

		if (reset){
			super.setRect(null);
			info.getRect();
		}
	}
	
	protected void fixRect(FixType ft) {
		switch (ft) {
		case ftSize:
			fixSize(false);
			return;
		case ftXY:
			fixLocation(false);
			return;
		case ftRect:
			fixSize(false);
			fixLocation(false);
			return;
		}
	}
	
	public Rectangle getRealRect() {
		Rectangle r = new Rectangle(getRect());
		r.setLocation(canvas.getRealPoint(r.getLocation()));
		return r;
	}
	
	public Rectangle getRefRect(){
		return super.getRect();
	}
	
	public Rectangle getRect() {
		if (info == null)
			return null;
		return info.getRect();
	}

	public void setRefRect(Rectangle rect){
		super.setRect(rect);
	}
	
	/**
	 * 设置节点的尺寸
	 * */
	public void setRect(Rectangle rect){
		super.setRect(rect);
		updateRect(new UpdateType[]{UpdateType.utLeft, UpdateType.utTop, UpdateType.utWidth, UpdateType.utHeight});
	}
	
	protected String round2(float x) {
		DecimalFormat df = new DecimalFormat("0.00");
		df.setRoundingMode(RoundingMode.HALF_UP);
		return df.format(x);
	}
	protected void updateRect(UpdateType[] updateTypes) {
		Rectangle rect = super.getRect();
		
		if (rect == null)
			return;
		
		if (info == null)
			return;
		
		List<UpdateType> types = Arrays.asList(updateTypes);
		Point point = rect.getLocation();//canvas.getVirtualLocation(rect.getLocation());
		point.x -= canvas.useRect.x;
		point.y -= canvas.useRect.y;
		
		if (types.indexOf(UpdateType.utLeft) != -1){
			if (info.left.trim().endsWith("%")){
				info.left = round2((float)point.x / canvas.getPageSize().width * 100) + "%";
			}else
				info.left = String.valueOf(point.x) + "px";
		}
		if (types.indexOf(UpdateType.utTop) != -1){
			if (info.top.trim().endsWith("%")){
				info.top = round2((float)point.y / canvas.getPageSize().height * 100) + "%";
			}else
				info.top = String.valueOf(point.y) + "px";
		}
		if (types.indexOf(UpdateType.utWidth) != -1){
			if (info.width.trim().endsWith("%")){
				info.width = round2((float)rect.width / canvas.getPageSize().width * 100) + "%";
			}else
				info.width = String.valueOf(rect.width) + "px";
		}
		if (types.indexOf(UpdateType.utHeight) != -1){
			if (info.height.trim().endsWith("%")){
				info.height = round2((float)rect.height / canvas.getPageSize().height * 100) + "%";
			}else
				info.height = String.valueOf(rect.height) + "px";
		}
	}
	
	public DrawInfo info;
	public DrawInfo getDrawInfo(){
		return info;
	}
	
	public UINode(DrawCanvas canvas) {
		super(canvas);
	}
	
	public void initRect(){
		if (info == null)
			return;
		
		info.getRect();	
	}
	
	public void drawNode(Graphics g){
		if (info != null) {
			Rectangle rect = super.getRect();
			if (info.needBackground){
				Color old = g.getColor();
				g.setColor(Color.WHITE);
				g.fillRect(rect.x, rect.y, rect.width, rect.height);
				g.setColor(old);
			}
			g.setFont(font);
			if (info.needFrame){
				g.setColor(Color.darkGray);
				g.draw3DRect(rect.x, rect.y, rect.width, rect.height, false);
			}
			info.drawNode(g, rect);
		}
	}
	
	protected static BufferedImage getIcon(String iconName) {
		try {
			return ImageUtils.loadImage(new File(Defines.Java_Dir_Icon_Resource, iconName));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

	public static BufferedImage getImage(String typename){
		switch (typename) {
		case "按钮":
			typename = "button";
			break;		
		case "报表":
			typename = "report";
			break;
		case "标签":
			typename = "label1";
			break;
		case "表格":
			typename = "grid";
			break;
		case "单选框":
			typename = "radiobox";
			break;
		case "多行文本框":
			typename = "mtextbox";
			break;
		case "多选框":
			typename = "checkbox";
			break;
		case "工具栏":
			typename = "toolbar";
			break;
		case "滚动列表框":
			typename = "scrollbar";
			break;
		case "进度条":
			typename = "progressbar";
			break;
		case "列表框":
			typename = "listbox";
			break;
		case "密码框":
			typename = "passwordtextbox";
			break;
		case "日期":
			typename = "datebar";
			break;
		case "上传框":
			typename = "uploader";
			break;
		case "时间":
			typename = "timer";
			break;
		case "视图列表":
			typename = "listview";
			break;
		case "树列表":
			typename = "treeview";
			break;
		case "数字选择框":
			typename = "spinnerbox";
			break;
		case "图表":
			typename = "chart";
			break;
		case "图片":
			typename = "imageview";
			break;
		case "文本框":
			typename = "textbox";
			break;
		case "下拉列表":
			typename = "combobox";
			break;
		case "下拉树列表":
			typename = "treecombobox";
			break;
		case "占位框":
			typename = "div";
			break;
		case "主菜单":
			typename = "mainmenu";
			break;
		case "主导航树":
			typename = "maintree";
			break;
		case "子界面":
			typename = "subview";
			break;
		case "定时器":
			typename = "timer";
			break;
		default:
			break;
		}
		return getIcon(typename + ".png");
	}

	public void setDrawInfo(DrawInfo info){
		this.info = info;
	}
	
	public static DrawInfo newInstance(final String typename, UINode node){
		DrawInfo info = null;
		switch (typename) {
		case DrawInfoDefines.Label_Name:
			info = new LabelInfo(node);
			break;

		case DrawInfoDefines.TextBox_Name:
			info = new TextInfo(node);
			break;

		case DrawInfoDefines.TextArea_Name:
			info = new TextAreaInfo(node);
			break;

		case DrawInfoDefines.ListBox_Name:
			info = new ListBoxInfo(node);
			break;

		case DrawInfoDefines.ScrollBar_Name:
			info = new ScrollBarInfo(node);
			break;
		case DrawInfoDefines.Report_Name:
			info = new ReportInfo(node);
			break;
		case DrawInfoDefines.Chart_Name:
			info = new ChartInfo(node);
			break;
		case DrawInfoDefines.MainMenu_Name:
			info = new MainMenuInfo(node);
			break;
		case DrawInfoDefines.ListView_Name:
			info = new ListViewInfo(node);
			break;
		case DrawInfoDefines.ProgressBar_Name:
			info = new ProgressBarInfo(node);
			break;
		case DrawInfoDefines.UpLoad_Name:
			info = new UpLoadInfo(node);
			break;
		case DrawInfoDefines.Div_Name:
			info = new DivInfo(node);
			break;
		case DrawInfoDefines.SubUI_Name:
			info = new SubUIInfo(node);
			break;
		case DrawInfoDefines.Toolbar_Name:
			info = new ToolbarInfo(node);
			break;
		case DrawInfoDefines.MainTree_Name:
			info = new MainTreeInfo(node);
			break;
		case DrawInfoDefines.Password_Name:
			info = new PasswordInfo(node);
			break;

		case DrawInfoDefines.Spinner_Name:
			info = new SpinnerInfo(node);
			break;

		case DrawInfoDefines.Image_Name:
			info = new ImageInfo(node);
			break;

		case DrawInfoDefines.ComboBox_Name:
			info = new ComboInfo(node);
			break;

		case DrawInfoDefines.ComboTreeBox_Name:
			info = new ComboTreeInfo(node);
			break;

		case DrawInfoDefines.RadioBox_Name:
			info = new RadioInfo(node);
			break;
		case DrawInfoDefines.CheckBox_Name:
			info = new CheckInfo(node);
			break;
		case DrawInfoDefines.DateBox_Name:
			info = new DateInfo(node);
			break;

		case DrawInfoDefines.TimeBox_Name:
			info = new TimeInfo(node);
			break;

		case DrawInfoDefines.Tree_Name:
			info = new TreeInfo(node);
			break;

		case DrawInfoDefines.Grid_Name:
			info = new GridInfo(node);
			break;

		case DrawInfoDefines.Button_Name:
			info = new ButtonInfo(node);
			break;

		case DrawInfoDefines.Timer_Name:
			info = new TimerInfo(node);
			break;

		default:
			break;
		}
		
		return info;
	}

}
