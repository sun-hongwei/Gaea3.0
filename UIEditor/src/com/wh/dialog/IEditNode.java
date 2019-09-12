package com.wh.dialog;

import com.wh.draws.DrawNode;
import com.wh.form.ISubForm;

public interface IEditNode{
	public void onEditUI(ISubForm iSubForm, DrawNode node);
	public void onEditSubWorkflow(ISubForm iSubForm, DrawNode node);
}


