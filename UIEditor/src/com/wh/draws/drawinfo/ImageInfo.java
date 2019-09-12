package com.wh.draws.drawinfo;

import java.awt.Image;
import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import com.wh.control.EditorEnvironment;
import com.wh.draws.UINode;
import com.wh.system.tools.ImageUtils;

public class ImageInfo extends ClickableInfo{
	public String typeName(){
		return DrawInfoDefines.Image_Name;
	}
	public ImageInfo(UINode node) {
		super(node);
		needBackground = false;
		width = "200px";
		height = "200px";
		drawTypes = DrawType_Image;
	}
	
	public JSONObject toJson(boolean needAll) throws JSONException{
		JSONObject json = super.toJson(needAll);
		json.put("scaleMode", needAll && scaleMode == null ? "" : scaleMode.name());
		json.put("alt", needAll && alt == null ? "" : alt);
		json.put("href", needAll && href == null ? "" : href);
		json.put("download", needAll && download == null ? "" : download);
		json.put("target", needAll && target == null ? "_self" : target);
		return json;
	}
	
	public void fromJson(JSONObject json) throws JSONException{
		super.fromJson(json);
		if (json.has("scaleMode")){
			try {
				scaleMode = ScaleMode.valueOf(json.getString("scaleMode"));
			} catch (Exception e) {
			}
		}else
			scaleMode = ScaleMode.smStretch;
		
		if (json.has("alt"))
			alt = json.getString("alt");
		else
			alt = null;

		if (json.has("href"))
			href = json.getString("href");
		else
			href = null;

		if (json.has("download"))
			download = json.getString("download");
		else
			download = null;

		if (json.has("target"))
			target = json.getString("target");
		else
			target = "_self";
		
	}

	public String href;
	public String alt;
	public String download;
	public String target = "_self";
	public ScaleMode scaleMode = ScaleMode.smStretch;

	protected Image getImage() {
		if (image != null){
			return image;
		}
		
		if (value == null || !(value instanceof String) || value.toString().isEmpty())
			return null;
		
		File file = EditorEnvironment.getProjectFile(EditorEnvironment.Image_Resource_Path, (String)value);
		
		if (!file.exists())
			return null;
		
		try {
			image = ImageUtils.loadImage(file);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return image;
	}
	
	protected ScaleMode getImageScale(){
		return scaleMode;
	}
				
	public void setValue(Object imageFile){
		this.value = imageFile;
		image = null;
	}
}