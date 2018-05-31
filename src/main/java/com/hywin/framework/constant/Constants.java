package com.hywin.framework.constant;

/**
 * @author wuyouyang
 * @time 2017年5月2日10:48:54
 */
public interface Constants {

	/**
	 * OO系统
	 * OO△△△xxx
	 * 中间三位△△△预留，默认为000
	 * 后三位xxx表示错误码
	 * 后三位中xxx，001-099预留为公司统一使用的编码
	 */
	// 系统异常（未定义、未捕获、未处理的统一放这里）

	public static final String RUN_TIME_ERROR_CODE = "0S000";
	public static final String RUN_TIME_ERROR_MSG = "系统运行时异常:";

	public static final String SYS_ERROR_CODE01 = "0S001";
	public static final String SYS_ERROR_MSG01 = "报文编码异常";

	public static final String SYS_ERROR_CODE02 = "0S002";
	public static final String SYS_ERROR_MSG02 = "http连接异常";

	public static final String SYS_ERROR_CODE03 = "0S003";
	public static final String SYS_ERROR_MSG03 = "http读取异常";

	public static final String SYS_ERROR_CODE04 = "0S004";
	public static final String SYS_ERROR_MSG04 = "JSON转化成MAP异常";

	public static final String SYS_ERROR_CODE05 = "0S005";
	public static final String SYS_ERROR_MSG05 = "输入参数异常";

	public static final String SYS_ERROR_CODE06 = "0S006";
	public static final String SYS_ERROR_MSG06 = "http请求异常";

	public static final String SYS_ERROR_CODE07 = "0S007";
	public static final String SYS_ERROR_MSG07 = "URISyntaxException";


}
