package com.wh.form;

import java.util.HashMap;

import com.wh.draws.DrawNode;

public interface IMainMenuOperation {
	public void onSave();

	public void onLoad();

	public void onClose();
	
	void onPublish(HashMap<String, DrawNode> uikeysWorkflowNodes, Object param) throws Exception;
}
