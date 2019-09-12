package com.wh.control.datasource.define;

public class SQLDataSource extends DataSource{

	private static final long serialVersionUID = 1L;
	public static final String SQL_KEY = "sql";

	@Override
	public String getType() {
		return SQL_KEY;
	}

}
