package com.hywin.framework.utils;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;

/**
 * 内存主键采番工具类
 * 
 * @author Lee
 * @since 2017-06-25
 */
public class SequenceUtil {

	/** 当前局域网IP后缀 */
	private static final String LOCAL_IP_SUFFIX;
	
	/** 系统时间格式:精确到毫秒 */
	private static final String DATE_FORMAT = "yyyyMMddHHmmssSSS";
	
	/** 每个线程各自独立的SimpleDateFormat实例 */
	private static ThreadLocal<SimpleDateFormat> THREADLOCAL_DATEFORMAT = new ThreadLocal<SimpleDateFormat>();
	
	/** 增量序列采番维护 */
	private static final ConcurrentMap<String, AtomicInteger> ATOMIC_SEQ_MAP = new ConcurrentHashMap<String, AtomicInteger>();
	
	static{
		LOCAL_IP_SUFFIX = getLocalHostIP();
	}

	/**
	 * <b>主键生成</b><br>
	 * 
	 * 说明:<br>
	 *  1. 30(长度) = 4(前缀长度)+ 17(时间) + 3(局域网IP后缀) + 6 (序列字符)<br>
	 *  2. 使用该方法生成主键建议字段长度设置为最多32<br>
	 *  
	 *  
	 * @param primaryKeypPrefix 前缀,不超过6位
	 * @return
	 */
	public static String generateSequence(String primaryKeypPrefix){
		
		StringBuilder sb=new StringBuilder(32);
		
		//主键前缀不能为空
		if(StringUtils.isBlank(primaryKeypPrefix)){
			throw new RuntimeException("主键前缀不能为空");
		}
		if(primaryKeypPrefix.length()>6){
			throw new RuntimeException("主键前缀不能超过6位");
		}
		sb.append(primaryKeypPrefix);
				
		//系统时间
		long sysTime ;
		//系统采番序列
		int tmpSeq = 0;
		
		sysTime = System.currentTimeMillis();
		AtomicInteger newSeq = new AtomicInteger(0);
		AtomicInteger seq = ATOMIC_SEQ_MAP.putIfAbsent(primaryKeypPrefix, newSeq);
		if(seq == null){
			seq = newSeq;
		}
		
		//1毫秒内不会运行1000000次
		seq.compareAndSet(999999, 0);
		tmpSeq = seq.incrementAndGet();	
		
		//时间戳
		sb.append(getSysDateTime(sysTime));
		//局域网IP后缀作为当前机器唯一标识
		sb.append(LOCAL_IP_SUFFIX);
		//序列
		sb.append(String.format("%06d", tmpSeq));
	
		return sb.toString();
	}
	
	/**
	 * 获取当前系统时间: yyyyMMddHHmmssSSS 17位
	 * 
	 * @return
	 */
	private static String getSysDateTime(long sysTime){
		return getDateFormat().format(new Date(sysTime));
	}
	
	/**
	 * 获取当前线程的日期格式化器
	 * @return
	 */
	private static SimpleDateFormat getDateFormat() {
		SimpleDateFormat df = THREADLOCAL_DATEFORMAT.get();
		if (df == null) {
			df = new SimpleDateFormat(DATE_FORMAT);
			THREADLOCAL_DATEFORMAT.set(df);
		}
		return df;
	}
	
	/**
	 * 获取本机局域网IP的后三位作为本机唯一标识<br>
	 * 
	 * 默认前提:<br>
	 * 	 1.同一应用集群环境部署时在同一网段<br>
	 * 	 2.getLocalHostIP这个方法在linux环境下,如果没有配置主机名和IP的映射会抛出异常<br>
	 * 
	 * @return Ip地址后三位
	 */
	private static String getLocalHostIP() {
		String ip ;
		try {
			// 返回 IP 地址字符串（以文本表现形式）
			ip = InetAddress.getLocalHost().getHostAddress();
			ip = ip.substring(ip.lastIndexOf(".")+1, ip.length());
			ip = String.format("%03d", Integer.parseInt(ip));
		} catch (Exception e) {
			e.printStackTrace();
			ip = "000";
		}
		
		return ip;
	}
	
}
