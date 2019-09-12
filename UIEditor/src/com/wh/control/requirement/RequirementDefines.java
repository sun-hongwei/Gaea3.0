package com.wh.control.requirement;

import java.util.Date;

import org.json.JSONArray;

import com.wh.control.grid.JsonArrayTableModel.Column;

public abstract class RequirementDefines {
	public static final String KEY_PUBLISH = "publish";
	public static final String KEY_DATA = "data";
	public static final String KEY_VERSION = "version";
	public static final String KEY_TYPE = "type";
	public static final String KEY_MAIN_VERSION = "mainversion";
	public static final String KEY_UNPUBLISH_TIME = "unpublishtime";

	public static final String field_id = "序号", field_requirement = "需求", field_type = "类型", field_version = "版本",
			field_depend_version = "变更版本", field_depend = "依赖", field_user = "用户", field_level = "级别",
			field_model = "关联模块", field_used = "启用", field_close = "关闭", field_role = "规则", field_class = "分类",
					field_use_time = "启用时间", field_close_time = "关闭时间", field_plan_time = "计划完毕时间";

	public static final Column[] ColumnNames = { new Column(field_id, true, String.class),
			new Column(field_requirement, true, String.class), new Column(field_type, true, String.class),
			new Column(field_version, true, String.class), new Column(field_depend_version, true, JSONArray.class),
			new Column(field_depend, true, JSONArray.class), new Column(field_user, true, String.class),
			new Column(field_level, true, Integer.class), new Column(field_model, true, String.class),
			new Column(field_used, true, Boolean.class), new Column(field_close, true, Boolean.class),
			new Column(field_role, true, JSONArray.class), 
			new Column(field_use_time, true, Date.class), 
			new Column(field_close_time, true, Date.class), 
			new Column(field_plan_time, true, Date.class), 
			};

	public static final Column[] View_ColumnNames = { new Column(field_id, true, String.class),
			new Column(field_requirement, true, String.class), new Column(field_type, true, String.class),
			new Column(field_version, true, String.class), new Column(field_depend_version, true, JSONArray.class),
			new Column(field_depend, true, JSONArray.class), new Column(field_user, true, String.class),
			new Column(field_level, true, Integer.class), new Column(field_model, true, String.class),
			new Column(field_used, true, Boolean.class), new Column(field_close, true, Boolean.class),
			new Column(field_role, true, JSONArray.class), 
			new Column(field_class, true, String.class), 
			new Column(field_use_time, true, Date.class), 
			new Column(field_close_time, true, Date.class), 
			new Column(field_plan_time, true, Date.class), 
			};

}
