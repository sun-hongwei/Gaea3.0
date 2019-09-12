package com.wh.draws.drawinfo;

import com.wh.draws.UINode;

public class TextAreaInfo extends TextInfo{
	public String typeName(){
		return DrawInfoDefines.TextArea_Name;
	}
	public TextAreaInfo(UINode node) {
		super(node);
		height = "48px";
		width = "150px";
	}

	protected String getButtonImageFileName() {
		return "multi-line.png";
	}
		

}