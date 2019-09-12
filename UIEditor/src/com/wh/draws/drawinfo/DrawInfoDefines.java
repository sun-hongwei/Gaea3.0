package com.wh.draws.drawinfo;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.wh.control.ControlTreeManager.TreeInfo;

public abstract class DrawInfoDefines {
	public static final String TypeName_Key = "typename";
	public static final String Full_TypeName_Key = "fulltypename";
	
	public static final String Spinner_Name = "数字选择框";
	public static final String Label_Name = "标签";
	public static final String TextBox_Name = "文本框";
	public static final String Image_Name = "图片";
	public static final String ComboBox_Name = "下拉列表";
	public static final String Separator_Name = "分隔符";
	public static final String ComboTreeBox_Name = "下拉树列表";
	public static final String RadioBox_Name = "单选框";
	public static final String CheckBox_Name = "多选框";
	public static final String DateBox_Name = "日期";
	public static final String TimeBox_Name = "时间";
	public static final String Tree_Name = "树列表";
	public static final String Grid_Name = "表格";
	public static final String Button_Name = "按钮";
	public static final String Password_Name = "密码框";
	public static final String TextArea_Name = "多行文本框";
	public static final String ListBox_Name = "列表框";
	public static final String ScrollBar_Name = "滚动列表框";
	public static final String Report_Name = "报表";
	public static final String Chart_Name = "图表";
	public static final String MainMenu_Name = "主菜单";
	public static final String MainTree_Name = "主导航树";
	public static final String ListView_Name = "视图列表";
	public static final String ProgressBar_Name = "进度条";
	public static final String UpLoad_Name = "上传框";
	public static final String Div_Name = "占位框";
	public static final String SubUI_Name = "子界面";
	public static final String Toolbar_Name = "工具栏";
	public static final String Timer_Name = "定时器";

	public static final String[] TypeNames = new String[] {Label_Name, TextBox_Name, TextArea_Name, ListBox_Name, ScrollBar_Name, Password_Name, Spinner_Name, Image_Name, ComboBox_Name, ComboTreeBox_Name, RadioBox_Name, CheckBox_Name, 
			DateBox_Name, TimeBox_Name, Tree_Name, Grid_Name, Button_Name, Report_Name, Chart_Name, MainTree_Name, MainMenu_Name, 
			ListView_Name, ProgressBar_Name, UpLoad_Name, Div_Name, SubUI_Name, Toolbar_Name, Timer_Name};
	
	public static HashMap<String, JSONObject> getControlSimpleClassNameForJSONObject(){
		HashMap<String, JSONObject> controls = new HashMap<>();

		try {
			controls.put(SpinnerInfo.class.getSimpleName(), new JSONObject("{" + DrawInfoDefines.Full_TypeName_Key + ":\"" + SpinnerInfo.class.getName() + "\"}"));
			controls.put(LabelInfo.class.getSimpleName(), new JSONObject("{" + DrawInfoDefines.Full_TypeName_Key + ":\"" + LabelInfo.class.getName() + "\"}"));
			controls.put(TextInfo.class.getSimpleName(), new JSONObject("{" + DrawInfoDefines.Full_TypeName_Key + ":\"" + TextInfo.class.getName() + "\"}"));
			controls.put(ImageInfo.class.getSimpleName(), new JSONObject("{" + DrawInfoDefines.Full_TypeName_Key + ":\"" + ImageInfo.class.getName() + "\"}"));
			controls.put(ComboInfo.class.getSimpleName(), new JSONObject("{" + DrawInfoDefines.Full_TypeName_Key + ":\"" + ComboInfo.class.getName() + "\"}"));
			controls.put(ComboTreeInfo.class.getSimpleName(), new JSONObject("{" + DrawInfoDefines.Full_TypeName_Key + ":\"" + ComboTreeInfo.class.getName() + "\"}"));
			controls.put(RadioInfo.class.getSimpleName(), new JSONObject("{" + DrawInfoDefines.Full_TypeName_Key + ":\"" + RadioInfo.class.getName() + "\"}"));
			controls.put(CheckInfo.class.getSimpleName(), new JSONObject("{" + DrawInfoDefines.Full_TypeName_Key + ":\"" + CheckInfo.class.getName() + "\"}"));
			controls.put(DateInfo.class.getSimpleName(), new JSONObject("{" + DrawInfoDefines.Full_TypeName_Key + ":\"" + DateInfo.class.getName() + "\"}"));
			controls.put(TimeInfo.class.getSimpleName(), new JSONObject("{" + DrawInfoDefines.Full_TypeName_Key + ":\"" + TimeInfo.class.getName() + "\"}"));
			controls.put(TreeInfo.class.getSimpleName(), new JSONObject("{" + DrawInfoDefines.Full_TypeName_Key + ":\"" + TreeInfo.class.getName() + "\"}"));
			controls.put(GridInfo.class.getSimpleName(), new JSONObject("{" + DrawInfoDefines.Full_TypeName_Key + ":\"" + GridInfo.class.getName() + "\"}"));
			controls.put(ButtonInfo.class.getSimpleName(), new JSONObject("{" + DrawInfoDefines.Full_TypeName_Key + ":\"" + ButtonInfo.class.getName() + "\"}"));
			controls.put(PasswordInfo.class.getSimpleName(), new JSONObject("{" + DrawInfoDefines.Full_TypeName_Key + ":\"" + PasswordInfo.class.getName() + "\"}"));
			controls.put(TextAreaInfo.class.getSimpleName(), new JSONObject("{" + DrawInfoDefines.Full_TypeName_Key + ":\"" + TextAreaInfo.class.getName() + "\"}"));
			controls.put(ListBoxInfo.class.getSimpleName(), new JSONObject("{" + DrawInfoDefines.Full_TypeName_Key + ":\"" + ListBoxInfo.class.getName() + "\"}"));
			controls.put(ScrollBarInfo.class.getSimpleName(), new JSONObject("{" + DrawInfoDefines.Full_TypeName_Key + ":\"" + ScrollBarInfo.class.getName() + "\"}"));
			controls.put(ReportInfo.class.getSimpleName(), new JSONObject("{" + DrawInfoDefines.Full_TypeName_Key + ":\"" + ReportInfo.class.getName() + "\"}"));
			controls.put(ChartInfo.class.getSimpleName(), new JSONObject("{" + DrawInfoDefines.Full_TypeName_Key + ":\"" + ChartInfo.class.getName() + "\"}"));
			controls.put(MainMenuInfo.class.getSimpleName(), new JSONObject("{" + DrawInfoDefines.Full_TypeName_Key + ":\"" + MainMenuInfo.class.getName() + "\"}"));
			controls.put(ListViewInfo.class.getSimpleName(), new JSONObject("{" + DrawInfoDefines.Full_TypeName_Key + ":\"" + ListViewInfo.class.getName() + "\"}"));
			controls.put(ProgressBarInfo.class.getSimpleName(), new JSONObject("{" + DrawInfoDefines.Full_TypeName_Key + ":\"" + ProgressBarInfo.class.getName() + "\"}"));
			controls.put(UpLoadInfo.class.getSimpleName(), new JSONObject("{" + DrawInfoDefines.Full_TypeName_Key + ":\"" + UpLoadInfo.class.getName() + "\"}"));
			controls.put(DivInfo.class.getSimpleName(), new JSONObject("{" + DrawInfoDefines.Full_TypeName_Key + ":\"" + DivInfo.class.getName() + "\"}"));
			controls.put(SubUIInfo.class.getSimpleName(), new JSONObject("{" + DrawInfoDefines.Full_TypeName_Key + ":\"" + SubUIInfo.class.getName() + "\"}"));
			controls.put(ToolbarInfo.class.getSimpleName(), new JSONObject("{" + DrawInfoDefines.Full_TypeName_Key + ":\"" + ToolbarInfo.class.getName() + "\"}"));
			controls.put(TimerInfo.class.getSimpleName(), new JSONObject("{" + DrawInfoDefines.Full_TypeName_Key + ":\"" + TimerInfo.class.getName() + "\"}"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return controls;
	}
	
	public static<T> HashMap<String, T> getControlSimpleClassName(Class<T> c){
		HashMap<String, T> controls = new HashMap<>();

		
		try {
			controls.put(SpinnerInfo.class.getSimpleName(), c.newInstance());
			controls.put(LabelInfo.class.getSimpleName(), c.newInstance());
			controls.put(TextInfo.class.getSimpleName(), c.newInstance());
			controls.put(ImageInfo.class.getSimpleName(), c.newInstance());
			controls.put(ComboInfo.class.getSimpleName(), c.newInstance());
			controls.put(ComboTreeInfo.class.getSimpleName(), c.newInstance());
			controls.put(RadioInfo.class.getSimpleName(), c.newInstance());
			controls.put(CheckInfo.class.getSimpleName(), c.newInstance());
			controls.put(DateInfo.class.getSimpleName(), c.newInstance());
			controls.put(TimeInfo.class.getSimpleName(), c.newInstance());
			controls.put(TreeInfo.class.getSimpleName(), c.newInstance());
			controls.put(GridInfo.class.getSimpleName(), c.newInstance());
			controls.put(ButtonInfo.class.getSimpleName(), c.newInstance());
			controls.put(PasswordInfo.class.getSimpleName(), c.newInstance());
			controls.put(TextAreaInfo.class.getSimpleName(), c.newInstance());
			controls.put(ListBoxInfo.class.getSimpleName(), c.newInstance());
			controls.put(ScrollBarInfo.class.getSimpleName(), c.newInstance());
			controls.put(ReportInfo.class.getSimpleName(), c.newInstance());
			controls.put(ChartInfo.class.getSimpleName(), c.newInstance());
			controls.put(MainMenuInfo.class.getSimpleName(), c.newInstance());
			controls.put(ListViewInfo.class.getSimpleName(), c.newInstance());
			controls.put(ProgressBarInfo.class.getSimpleName(), c.newInstance());
			controls.put(UpLoadInfo.class.getSimpleName(), c.newInstance());
			controls.put(DivInfo.class.getSimpleName(), c.newInstance());
			controls.put(SubUIInfo.class.getSimpleName(), c.newInstance());
			controls.put(ToolbarInfo.class.getSimpleName(), c.newInstance());
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return controls;
	}
	
	public static String getControlChineseName(String controlName){
		String name = controlName;
		if (SpinnerInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = DrawInfoDefines.Spinner_Name;
		else if (LabelInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = DrawInfoDefines.Label_Name;
		else if (TextInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = DrawInfoDefines.TextBox_Name;
		else if (ImageInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = DrawInfoDefines.Image_Name;
		else if (ComboInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = DrawInfoDefines.ComboBox_Name;
		else if (ComboTreeInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = DrawInfoDefines.ComboTreeBox_Name;
		else if (RadioInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = DrawInfoDefines.RadioBox_Name;
		else if (CheckInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = DrawInfoDefines.CheckBox_Name;
		else if (DateInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = DrawInfoDefines.DateBox_Name;
		else if (TimeInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = DrawInfoDefines.TimeBox_Name;
		else if (TreeInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = DrawInfoDefines.Tree_Name;
		else if (GridInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = DrawInfoDefines.Grid_Name;
		else if (ButtonInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = DrawInfoDefines.Button_Name;
		else if (PasswordInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = DrawInfoDefines.Password_Name;
		else if (TextAreaInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = DrawInfoDefines.TextArea_Name;
		else if (ListBoxInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = DrawInfoDefines.ListBox_Name;
		else if (ScrollBarInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = DrawInfoDefines.ScrollBar_Name;
		else if (ReportInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = DrawInfoDefines.Report_Name;
		else if (ChartInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = DrawInfoDefines.Chart_Name;
		else if (MainMenuInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = DrawInfoDefines.MainMenu_Name;
		else if (ListViewInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = DrawInfoDefines.ListView_Name;
		else if (ProgressBarInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = DrawInfoDefines.ProgressBar_Name;
		else if (UpLoadInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = DrawInfoDefines.UpLoad_Name;
		else if (DivInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = DrawInfoDefines.Div_Name;
		else if (SubUIInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = DrawInfoDefines.SubUI_Name;
		else if (ToolbarInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = DrawInfoDefines.Toolbar_Name;		
		else if (TimerInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = DrawInfoDefines.Timer_Name;
		return name;
	}
	
}
