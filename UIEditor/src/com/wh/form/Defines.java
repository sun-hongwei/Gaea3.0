package com.wh.form;

import java.io.File;

public class Defines {
	public static final File Java_Dir_Root = new File(System.getProperty("user.dir"));
	public static final String Java_Dir_TmpName = "tmps";
	public static final String Java_Dir_Default = "default";
	public static final File Java_Dir_Tmp = new File(Java_Dir_Root.getAbsolutePath(), Java_Dir_TmpName);
	public static final File Java_Dir_Data = new File(Java_Dir_Root.getAbsolutePath(), "datas");
	public static final File Java_Dir_Plug = new File(Java_Dir_Root.getAbsolutePath(), "plugs");
	public static final File Java_Dir_Sql = new File(Java_Dir_Root.getAbsolutePath(), "sqls");
	public static final File Java_Dir_Config = new File(Java_Dir_Root.getAbsolutePath(), "config");
	public static final File Java_Dir_Project = new File(Java_Dir_Root.getAbsolutePath(), "project");
	public static final File Java_Dir_Resource = new File(Java_Dir_Root.getAbsolutePath(), "resource");
	public static final File Java_Dir_Icon_Resource = new File(Java_Dir_Resource.getAbsolutePath(), "icons");
	public static final File Java_Dir_Page = new File(Java_Dir_Root.getAbsolutePath(), "pages");

	public final static String Default_Image_Ext = "ixt";
	
}