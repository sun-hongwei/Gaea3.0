package com.wh.draws.drawinfo;

import java.awt.image.BufferedImage;
import java.io.File;

import com.wh.draws.UINode;
import com.wh.form.Defines;
import com.wh.system.tools.ImageUtils;

public class CheckInfo extends TextInfo{
	public String typeName(){
		return DrawInfoDefines.CheckBox_Name;
	}
	public CheckInfo(UINode node) {
		super(node);
		needBackground = false;
		value = false;
	}

	BufferedImage checkButton, noCheckButton;
	
	protected void initImages() throws Exception {
		checkButton = ImageUtils.loadImage(new File(Defines.Java_Dir_Icon_Resource.getAbsolutePath(), getButtonImageFileName(true)));
		noCheckButton = ImageUtils.loadImage(new File(Defines.Java_Dir_Icon_Resource.getAbsolutePath(), getButtonImageFileName(false)));
	}

	protected BufferedImage getImage() {
		if (checkButton == null){
			try {
				initImages();
			} catch (Exception e) {
				e.printStackTrace();
				checkButton = null;
				noCheckButton = null;
			}
		}
		if (value instanceof Boolean)
			return (((boolean)value)? checkButton: noCheckButton);
		else if (value instanceof String)
			return ((Boolean.parseBoolean((String)value))? checkButton: noCheckButton);
		else
			return null;
	}

	protected String getDisplayText() {
		return title;
	}
	
	protected String getButtonImageFileName(boolean checked){
		return checked? "check.png": "nocheck.png";
	}
	
}