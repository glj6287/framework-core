package com.hywin.framework.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;


/**
 * 计算工作日/休息日
 * 
 * @author Lee
 * @since 2016-02-25
 */
public class BusiDateUtil {
	
	/**工作日; 记录原为休息日（周六/周日），但是现在被调整至工作日的集合*/
	public static final String WORKDAY = "WORK_DAY"; 
	/**休息日; 记录原为工作日，但是现在被调整至休息日的集合*/
	public static final String HOLIDAY = "HOLIDAY";
	
	/**特殊休息日/工作日集合*/
	private static final Map<String, String> DATE_SET = new HashMap<String, String>(){
		private static final long serialVersionUID = -4983735159234251362L;
		
		{
			//2016年:记录原为休息日（周六/周日），但是现在被调整至工作日的集合
			put(WORKDAY+"2016", "20160206,20160214,20160612,20160918,20161008,20161009");
			//2016年:记录原为工作日，但是现在被调整至休息日的集合
			put(HOLIDAY+"2016", "20160101,20160208,20160209,20160210,20160211,20160212,20160404,20160502,20160609,20160610,20160915,20160916,20161003,20161004,20161005,20161006,20161007");
			
			//2017年:记录原为休息日（周六/周日），但是现在被调整至工作日的集合
			put(WORKDAY+"2017", "20170204,20170401,20170527,20170930");
			//2017年:记录原为工作日，但是现在被调整至休息日的集合
			put(HOLIDAY+"2017", "20170102,20170127,20170130,20170131,20170201,20170202,20170403,20170404,20170501,20170529,20170530,20171002,20171003,20171004,20171005,20171006");
		}
	}; 
	
	//测试
	public static void main(String[] args) throws ParseException {
		System.out.println(AddWorkday("20161231", 1));
	}
	
	/**
	 * 根据指定日期+N天,获取下个工作日的日期
	 * 
	 * @param dateStr 日期字符串 格式"yyyyMMdd"
	 * @param n 距离下个工作日的天数
	 * @return
	 * @throws ParseException 
	 */
	public static Date AddWorkday(String dateStr,int n) throws ParseException{
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		format.setLenient(false);
		Date date = format.parse(dateStr);
		return AddWorkday(date,n);
	} 		

	/**
	 * 根据指定日期+N天,获取下个工作日的日期(资金对账JOB使用)
	 * 
	 * @param dateStr 日期字符串 格式"yyyy-MM-dd HH:mm:ss"
	 * @param n 距离下个工作日的天数
	 * @return date
	 * @throws ParseException 
	 */
	public static Date AddWorkdayTime(String dateStr,int n) throws ParseException{
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		format.setLenient(false);
		Date date = format.parse(dateStr);
		return AddWorkday(date,n);
	} 
	
	/**
	 * 根据指定日期+N天,获取下个工作日的日期
	 * 
	 * @param date 指定日期
	 * @param n    距离下个工作日的天数
	 * @return
	 */
	public static Date AddWorkday(Date date,int n)throws ParseException{
		return AddWorkday(date,n,false);
	}

	/**
	 * 根据指定日期+N天,获取下个工作日的日期
	 * 
	 * @param dateStr 日期字符串 格式"yyyyMMdd"
	 * @param n 距离下个工作日的天数
	 * @param flag ("周六","周日")是否默认为休息日: false:不是默认休息日,要根据法定节假日确认
	 *                                      true :所有的周六周日都认为是休息日
	 * @return
	 * @throws ParseException 
	 */
	public static Date AddWorkday(String dateStr,int n,boolean flag) throws ParseException{
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		format.setLenient(false);
		Date date = format.parse(dateStr);
		return AddWorkday(date,n,flag);
	} 		
	
	/**
	 * 根据指定日期+N天,获取下个工作日的日期
	 * 
	 * @param date 指定日期
	 * @param n    距离下个工作日的天数
	 * @param flag ("周六","周日")是否默认为休息日: false:不是默认休息日,要根据法定节假日确认
	 *                                      true :所有的周六周日都认为是休息日
	 * @return
	 */
	public static Date AddWorkday(Date date,int n,boolean flag)throws ParseException{
		if(n==0)return date;
		
		//当前日期年份
		int year = getYearByDate(date);
		
		// 根据年份到数据库获取 记录原为工作日，但是现在被调整至休息日的集合
		String[] holidayArray = getWorkOrHolidays(HOLIDAY, String.valueOf(year));
	
		String[] workDayArray = {};
		if(!flag){
			// 根据年份到数据库获取 原为休息日（周六/周日），但是现在被调整至工作日的集合
			workDayArray = getWorkOrHolidays(WORKDAY, String.valueOf(year));
		}
		
		
		Date nextWorkDay = null;
		int tempN = n;			// 临时保存+的天数
		int tempYear = year;    // 临时保存当前传入日期年份
		Date tempDate = date;   // 临时保存当前传入日期
		//查找下一个工作日
		do {
			Date nextDate;
			if(tempN>0){
				nextDate = getDateAfterOneDay(tempDate);
			}else{
				nextDate = getDateBeforeOneDay(tempDate);
			}
			int nextYear = getYearByDate(nextDate);
			if(tempYear != nextYear){//重新加载配置
				tempYear = nextYear;
				holidayArray = getWorkOrHolidays(HOLIDAY, String.valueOf(nextYear));
				if(!flag){
					workDayArray = getWorkOrHolidays(WORKDAY, String.valueOf(nextYear));
				}
			}
			
			if(isWorkDay(nextDate, workDayArray, holidayArray)){
				if(tempN>0){
					tempN--;
				}else{
					tempN++;
				}
			}	
			tempDate = nextDate;
			if(tempN==0){
				nextWorkDay = nextDate;
				break;
			}
		} while (true);
		
		return nextWorkDay;
	} 

	/**
	 * 休息日判断
	 * 
	 * @param date 需要判断的日期, 如果为null,返回false
	 * @return true:是休息日 ; false:不是休息日 
	 */
	public static boolean isHoliday(Date date) throws ParseException {
		if(date==null)return false;
		//年份
		int year = getYearByDate(date);
		//星期几
		int week = getWeekByDate(date);
				
		// 根据年份到数据库获取 记录原为工作日，但是现在被调整至休息日的集合
		String[] holidayArray = getWorkOrHolidays(HOLIDAY, String.valueOf(year));

		// 根据年份到数据库获取 原为休息日（周六/周日），但是现在被调整至工作日的集合
		String[] workDayArray = getWorkOrHolidays(WORKDAY, String.valueOf(year));
		
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		format.setLenient(false);
		String dateStr = format.format(date);
		
		//根据配置判断传入日期是否是休息日
		if(isInStrArray(holidayArray, dateStr))return true;
		
		//根据配置判断传入日期是否是工作日
		if(isInStrArray(workDayArray, dateStr))return false;
		
		//根据星期几 判断当前日期是工作日还是休息日
		return HOLIDAY.equals(isHolidayOrWorkDay(week));
	}
	
	/**
	 * 休息日判断
	 * 
	 * @param	date 需要判断的日期, 如果为null,返回false
	 * @param	flag ("周六","周日")是否默认为休息日
	 * 				false:不是默认休息日,要根据法定节假日确认
	 * 				true :所有的周六周日都认为是休息日
	 * @return true:是休息日 ; false:不是休息日 
	 */
	public static boolean isHoliday(Date date, boolean flag) throws ParseException {
		if(date==null)return false;
		//年份
		int year = getYearByDate(date);
		//星期几
		int week = getWeekByDate(date);
				
		// 根据年份到数据库获取 记录原为工作日，但是现在被调整至休息日的集合
		String[] holidayArray = getWorkOrHolidays(HOLIDAY, String.valueOf(year));
		
		String[] workDayArray = {};
		if(!flag) {
			// 根据年份到数据库获取 原为休息日（周六/周日），但是现在被调整至工作日的集合
			workDayArray = getWorkOrHolidays(WORKDAY, String.valueOf(year));
		}
		
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		format.setLenient(false);
		String dateStr = format.format(date);
		
		//根据配置判断传入日期是否是休息日
		if(isInStrArray(holidayArray, dateStr))return true;
		
		//根据配置判断传入日期是否是工作日
		if(isInStrArray(workDayArray, dateStr))return false;
		
		//根据星期几 判断当前日期是工作日还是休息日
		return HOLIDAY.equals(isHolidayOrWorkDay(week));
	}
	
	/**
	 * 休息日判断
	 * 
	 * @param	dateStr 日期字符串 格式"yyyyMMdd", 如果传入字符串为空,返回false
	 * @param	flag ("周六","周日")是否默认为休息日
	 * 				false:不是默认休息日,要根据法定节假日确认
	 * 				true :所有的周六周日都认为是休息日
	 * @return
	 * @throws ParseException 
	 */
	public static boolean isHoliday(String dateStr, boolean flag) throws ParseException {
		if(StringUtils.isBlank(dateStr))return false;
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		format.setLenient(false);
		Date date = format.parse(dateStr);
		return isHoliday(date, flag);
	}

	/**
	 * 工作日判断
	 * 
	 * @param date 指定日期,, 如果为null,返回false
	 * @return
	 */
	public static boolean isWorkDay(Date date) throws ParseException {
		if(date==null)return false;
		//年份
		int year = getYearByDate(date);
		//星期几
		int week = getWeekByDate(date);
			
		// 根据年份到数据库获取 原为休息日（周六/周日），但是现在被调整至工作日的集合
		String[] workDayArray = getWorkOrHolidays(WORKDAY, String.valueOf(year));			
		
		// 根据年份到数据库获取 记录原为工作日，但是现在被调整至休息日的集合
		String[] holidayArray = getWorkOrHolidays(HOLIDAY, String.valueOf(year));		
		
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		format.setLenient(false);
		String dateStr = format.format(date);
		
		//根据配置判断传入日期是否是工作日
		if(isInStrArray(workDayArray, dateStr))return true;
		
		//根据配置判断传入日期是否是休息日
		if(isInStrArray(holidayArray, dateStr))return false;
		
		//根据星期几 判断当前日期是工作日还是休息日
		return WORKDAY.equals(isHolidayOrWorkDay(week));		
	}

	/**
	 * 工作日判断(资金对账JOB使用)
	 * 
	 * @param date 指定日期 , 日期字符串 格式"yyyyMMdd", 如果传入字符串为空,返回false
	 * @return
	 * @throws ParseException 
	 */
	public static boolean isWorkDay(String dateStr) throws ParseException {
		if(StringUtils.isBlank(dateStr))return false;
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		format.setLenient(false);
		Date date = format.parse(dateStr);
		return isWorkDay(date);
	}
	
	/**
	 * 工作日判断
	 * 
	 * @param date 指定日期
	 * @param workDayArray 记录 原为休息日（周六/周日），但是现在被调整至工作日的集合
	 * @param holidayArray 记录原为工作日，但是现在被调整至休息日的集合
	 * @return
	 */
	public static boolean isWorkDay(Date date,String[] workDayArray,String[] holidayArray)throws ParseException{
		//星期几
		int week = getWeekByDate(date);

		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		format.setLenient(false);
		String dateStr = format.format(date);
		
		//根据配置判断传入日期是否是工作日
		if(isInStrArray(workDayArray, dateStr))return true;
		
		//根据配置判断传入日期是否是休息日
		if(isInStrArray(holidayArray, dateStr))return false;
		
		//根据星期几 判断当前日期是工作日还是休息日
		return WORKDAY.equals(isHolidayOrWorkDay(week));		
	}
	
	/**
	 * 判断传入的日期,在配置数组中
	 * @param strArray
	 * @param dateStr
	 * @return
	 */
	private static boolean isInStrArray(String[] strArray,String dateStr) {
		if(strArray==null || strArray.length==0)return false;
		for (String workDayStr : strArray) {
			if (dateStr.equals(workDayStr)) {
				return true;
			}
		}
		return false;
	}	
	
	/**
	 *  根据星期几 判断当前日期是工作日还是休息日
	 *  
	 * @param week 星期几
	 * @return
	 */
	private static String isHolidayOrWorkDay(int week) {
		//一周内数组, 星期天到星期六
		String[] weekDaysName = {"0", "1", "2", "3", "4", "5", "6" };
		String weekDay = weekDaysName[week];
		if ("0".equals(weekDay) || "6".equals(weekDay)) {
			return HOLIDAY;
		} else {
			return WORKDAY;
		}
	}	
	
	/**
	 * 根据年份获取对应 原为休息日（周六/周日），但是现在被调整至工作日的集合
	 * 
	 * @param pramService 数据字典Service
	 * @param codeName 数据字典里配置的Code_Name
	 * @param year 年份 , 数据字典里配置的Code_Val
	 * @return
	 */
	private static String[] getWorkOrHolidays(String codeName,String year) {
		String paramShowMsg = DATE_SET.get(codeName+year);
		//返回解析到的工作日集合
		String[] workOrHolidays = null;
		
		if(StringUtils.isBlank(paramShowMsg)){
			workOrHolidays = new String[0];
		}else{
			workOrHolidays = paramShowMsg.split(",");
		}
		return workOrHolidays;
	}

	/**
	 * 根据传入日期判断该日期的年份
	 * 
	 * @param date 传入日期
	 * @return
	 */
	private static int getYearByDate(Date date){
		// 获取指定日期的年份,星期几
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		// 年份
		return cal.get(Calendar.YEAR);
	}
	
	/**
	 * 根据传入日期判断该日期的年份
	 * 
	 * @param date 传入日期
	 * @return
	 */
	private static int getWeekByDate(Date date){
		// 获取指定日期的年份,星期几
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		// 星期几
		return cal.get(Calendar.DAY_OF_WEEK) - 1;		
	}
	
	/**
	 * 获取指定日期前1天的日期
	 * @param date
	 * @return
	 */
	public static Date getDateBeforeOneDay(Date date){
		return getDateAddN(date,-1);
	}
	
	/**
	 * 获取指定日期后1天的日期
	 * @param date
	 * @return
	 */
	public static Date getDateAfterOneDay(Date date){
		return getDateAddN(date,1);
	}
	
	/**
	 * 获取指定日期加/减N天的日期
	 * 
	 * @param date 指定日期
	 * @param n  指定日期(加/减)的天数
	 * @author Lee
	 * @return Date
	 */
	public static Date getDateAddN(Date date, int n) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		// 把日期往增加一天.整数往后推,负数往前移动
		calendar.add(Calendar.DATE, n);
		return calendar.getTime();
	}
}
