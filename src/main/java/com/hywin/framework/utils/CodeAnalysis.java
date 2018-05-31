package com.hywin.framework.utils;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;

/**
 * <p>代码分析工具</p>
 */
public class CodeAnalysis {
        private static Log logger = LogFactory.getLog(CodeAnalysis.class);
        /**
         * 返回以毫秒为单位的当前时间
         * @return
         */
        public static long currentTimeMillis(String text){
               long current = System.currentTimeMillis() ;
                if(logger.isInfoEnabled()){
                      logger.info(text+">>>>=========== 开始时间 : " +new Date(current) );
                }
                return current;
        }
        /**
         * 获取处理时间
         * @param lastMile 前执行时间
         * @return 秒
         */
        public static long getProcessingTime(long lastMile ){
            return System.currentTimeMillis() - lastMile;
        }
        /**
         * 打印处理时间
         * @param lastMile 前执行时间
         * @param text 
         */
        public static  void printProcessingTime(long  lastMile , String text){
            
            long pt = getProcessingTime(lastMile);
            float  minutes  = Float.valueOf(pt)/ (1000*60) ;
            float seconds = Float.valueOf(pt)/ 1000 ;
                if(logger.isInfoEnabled()){
                    logger.info(text+"<<<<=========== 执行时间：" +minutes+" 分钟 , "+seconds+" 秒 , "+pt+"毫秒");
              }
        }
        
        public static void main(String[] args) throws InterruptedException {
            long now =currentTimeMillis("【 申购 】");
            printProcessingTime(now, "【 申购 】");
        }
}
