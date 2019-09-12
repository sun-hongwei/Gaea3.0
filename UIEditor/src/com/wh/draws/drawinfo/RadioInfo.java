package com.wh.draws.drawinfo;

import java.awt.image.BufferedImage;
import java.io.File;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wh.draws.UINode;
import com.wh.form.Defines;
import com.wh.system.tools.ImageUtils;

public class RadioInfo extends ListBoxInfo{
	public String typeName(){
		return DrawInfoDefines.RadioBox_Name;
	}
	public RadioInfo(UINode node) {
		super(node);
		needImage = true;
		fillSample();
	}

	public void fillSample() {
		JSONArray datas = new JSONArray();
		try {
			JSONObject data = new JSONObject();
			data.put("id", "id1");
			data.put("text", "单选项目1");
			data.put("value", false);
			datas.put(data);
			
			data = new JSONObject();
			data.put("id", "id2");
			data.put("text", "单选项目2");
			data.put("value", true);
			datas.put(data);
			
			value = datas;
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	protected String getButtonImageFileName(boolean checked){
		return checked? "radiocheck.png": "radionocheck.png";
	}

	BufferedImage checkIcon, nocheckIcon;
	
	protected BufferedImage getIcon(Object value) {
		if (nocheckIcon == null){
			try {
				nocheckIcon = ImageUtils.loadImage(new File(Defines.Java_Dir_Icon_Resource.getAbsolutePath(), getButtonImageFileName(false)));
				checkIcon = ImageUtils.loadImage(new File(Defines.Java_Dir_Icon_Resource.getAbsolutePath(), getButtonImageFileName(true)));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		try {
			return checkIcon;
		} catch (JSONException e) {
			e.printStackTrace();
			return nocheckIcon;
		}
	}

	protected String getDisplayText(Object value) {
		try {
			return ((JSONObject)value).getString("text");
		} catch (JSONException e) {
			e.printStackTrace();
			return "";
		}
	}
	
}