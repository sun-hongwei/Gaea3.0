package com.wh.draws.drawinfo;

import com.wh.draws.UINode;

public class PasswordInfo extends TextInfo{
	public String typeName(){
		return DrawInfoDefines.Password_Name;
	}
	public PasswordInfo(UINode node) {
		super(node);
	}

	protected String getButtonImageFileName() {
		return "password.png";
	}
				
}