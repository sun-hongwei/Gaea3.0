package com.wh.form;

public interface ISubForm {
	public void onStart(Object param);
	
	public void setParentForm(ChildForm form);

	public ChildForm getParentForm();

	public Object getResult();

}
