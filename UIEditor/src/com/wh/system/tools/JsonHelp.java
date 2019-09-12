package com.wh.system.tools;

import java.io.File;
import java.io.FileInputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonHelp {

	public static final String Default_Charset = "utf8";
	
	
	public static Object parseJson(String str) throws JSONException{
		str = str.trim();
		if (str.isEmpty())
			return null;
		
		if (str.charAt(0) == '{')
			return new JSONObject(str);
		else
			return new JSONArray(str);
	}
	
	public static Object parseJson(File f, String charset) throws Exception{
		return parseJson(f, charset, true);
	}
	
	public static Object parseJson(File f, String charset, boolean needCopy) throws Exception{
		charset = (charset == null || charset.isEmpty()) ? Default_Charset : charset;
		if (needCopy)
			f = FileCache.open(f);
		
		FileInputStream stream = new FileInputStream(f);
		byte[] datas = new byte[stream.available()];
		int index = 0;
		while (stream.available() > 0){
			int max = Math.min(stream.available(), datas.length - index);
			int readLen = stream.read(datas, 0, max);
			index += readLen;
		}
		stream.close();
		
		String value = new String(datas, charset);
		return parseJson(value);
	}

	public static void saveJson(File f, Object value, String charset) throws Exception{
		saveJson(f, value, charset, true);		
	}
	
	public static void saveJson(File f, Object value, String charset, boolean needCopy) throws Exception{
		saveJson(f, value.toString(), charset, needCopy);
	}
	
	public static void saveJson(File f, String value, String charset) throws Exception{
		saveJson(f, value, charset, true);
	}
	
	public static void saveJson(File f, String value, String charset, boolean needCopy) throws Exception{
		charset = (charset == null || charset.isEmpty()) ? Default_Charset : charset;
		byte[] datas = value.getBytes(charset);
		FileCache.save(f, datas, needCopy);
	}
	
	public static boolean isEmpty(JSONObject json, String key){
		if (!json.has(key))
			return true;
		
		Object object = json.get(key);
		if (object == null)
			return true;
		
		if (object instanceof String){
			if (((String)object).isEmpty())
				return true;
		}
		
		return false;
	}
	public static String getString(JSONObject json, String key){
		if (json.has(key)){
			Object value = json.get(key);
			if (value == null)
				return null;
			
			return value.toString();
		}else
			return null;
	}
}
