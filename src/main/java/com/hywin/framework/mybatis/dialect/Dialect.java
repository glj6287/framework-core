package com.hywin.framework.mybatis.dialect;

public abstract class Dialect
{
	public abstract String getLimitString(String paramString, int paramInt1, int paramInt2);

	public abstract String addLog(String paramString);

	public static enum Type
	{
		MYSQL, ORACLE, MSSQL;
	}
}