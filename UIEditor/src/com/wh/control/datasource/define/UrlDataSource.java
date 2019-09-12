package com.wh.control.datasource.define;

public class UrlDataSource extends DataSource{
	private static final long serialVersionUID = 1L;

	public static final String URL_KEY = "remote";
	@Override
	public String getType() {
		return URL_KEY;
	}

}
