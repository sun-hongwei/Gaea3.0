package com.wh.control.datasource.define;

public class LocalDataSource extends DataSource{

	private static final long serialVersionUID = 1L;

	public static final String LOCAL_KEY = "local";
	
	@Override
	public String getType() {
		return LOCAL_KEY;
	}

}
