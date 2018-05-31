package com.hywin.framework.mybatis;

import com.hywin.framework.mybatis.dialect.Dialect;
import com.hywin.framework.mybatis.dialect.MySql5Dialect;
import com.hywin.framework.mybatis.dialect.OracleDialect;

import java.sql.Connection;
import java.util.Properties;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
/**
 * Created by wuyouyang on 2017/4/24.
 */
@Intercepts({@org.apache.ibatis.plugin.Signature(type=StatementHandler.class, method="prepare", args={Connection.class, Integer.class})})
public class SqlInterceptor
		implements Interceptor
{
	private static ThreadLocal<RowBounds> threadRowBounds = new ThreadLocal<RowBounds>();

	private static RowBounds getRowBounds() {
		RowBounds rowBounds = threadRowBounds.get();
		threadRowBounds.remove();
		return rowBounds;
	}

	public static void setRowBounds(RowBounds rowBounds) {
		threadRowBounds.set(rowBounds);
	}

	public Object intercept(Invocation invocation) throws Throwable
	{
		StatementHandler statementHandler = (StatementHandler)invocation.getTarget();
//		BoundSql boundSql = statementHandler.getBoundSql();
		MetaObject metaStatementHandler = MetaObject.forObject(statementHandler, SystemMetaObject.DEFAULT_OBJECT_FACTORY, SystemMetaObject.DEFAULT_OBJECT_WRAPPER_FACTORY, new DefaultReflectorFactory());
		RowBounds rowBounds = getRowBounds();
		if (rowBounds == null) {
			rowBounds = (RowBounds)metaStatementHandler.getValue("delegate.rowBounds");
		}
		Configuration configuration = (Configuration)metaStatementHandler.getValue("delegate.configuration");
		Dialect.Type databaseType;
		try {
			databaseType = Dialect.Type.valueOf(configuration.getVariables().getProperty("dialect").toUpperCase());
		}
		catch (Exception localException) {
			throw new RuntimeException(localException.getMessage());
		}
		if (databaseType == null) {
			throw new RuntimeException("the value of the dialect property in configuration.xml is not defined : " + configuration.getVariables().getProperty("dialect"));
		}
		Dialect dialect = null;
		switch (databaseType.ordinal()) {
		case 1:
			dialect = new MySql5Dialect();
			break;
		case 2:
			dialect = new OracleDialect();
		}

		String sql = (String)metaStatementHandler.getValue("delegate.boundSql.sql");
		if ((rowBounds != null) && (rowBounds != RowBounds.DEFAULT)) {
			sql = dialect.getLimitString(sql, rowBounds.getOffset(), rowBounds.getLimit());
		}

		if (sql == null) {
			throw new RuntimeException("the value of the dialect property in configuration.xml is not defined : " + configuration.getVariables().getProperty("dialect"));
		}

		metaStatementHandler.setValue("delegate.boundSql.sql", sql);
		metaStatementHandler.setValue("delegate.rowBounds.offset", 0);
		metaStatementHandler.setValue("delegate.rowBounds.limit", 2147483647);

		return invocation.proceed();
	}

	public Object plugin(Object target)
	{
		return Plugin.wrap(target, this);
	}

	public void setProperties(Properties properties)
	{
	}
}